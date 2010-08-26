package translators.jackCompiler;

import java.util.*;

import translators.VMTranslator.*;

/**
 * The Compilation Engine. Compiles .jack files parsed by a JackTokenizer
 * using a SymbolTable and writes the resulting .vm files using a VMWriter.
 */
public class CompilationEngine {

    private static final int GENERAL_TYPE = 1;
    private static final int NUMERIC_TYPE = 2;
    private static final int INT_TYPE = 3;
    private static final int CHAR_TYPE = 4;
    private static final int BOOLEAN_TYPE = 5;
    private static final int STRING_TYPE = 6;
    private static final int THIS_TYPE = 7;
    private static final int NULL_TYPE = 8;

    // The lexical analyzer
    private JackTokenizer input;

    // The virtual machine writer
    private VMWriter output;

    // The symbol table
    private SymbolTable identifiers;

	// All declared subroutines so far
	private HashMap subroutines;

	// All classes compiled successfully so far
	private HashSet classes;

	// All called internal subroutines so far
	private Vector subroutineCalls;

    // Counts the number of "if" statements in a method - used to create
    // unique labels
    private int ifCounter;

    // Counts the number of "while" statements in a method - used to create
    // unique labels
    private int whileCounter;

    // The currently compiled subroutine (method or function).
    private int subroutineType;

    // The types of all the currently compiled expressions
    private int expTypes[];

    // The index of the top compiled expression
    private int expIndex;

	// The current file name
	private String fileName;

	// Validity for the current file - true if no errors occured so far
	// (= in the current file / current validation)
	private boolean validJack;

/*************************************************************************************************/
public /* Constructor
/*************************************************************************************************/
	CompilationEngine() {
		classes = new HashSet();
		subroutines = new HashMap();
		subroutineCalls = new Vector();
	}

/*************************************************************************************************/
/* A method for compiling the class structure and a method for verifying all subroutine calls - the only public methods in the CompilationEngine. */
/*************************************************************************************************/

    /**
     * Compiles a whole class, which resides in the given fileName.
     */
    public boolean compileClass(JackTokenizer input, VMWriter output,
							 String expectedClassName, String fileName) {
        this.input = input;
        this.output = output;
		this.fileName = fileName;
        expTypes = new int[100];
        expIndex = -1;
		validJack = true;
        input.advance();

		String className = null;
		try {
			if (isKeywordClass()) {
				input.advance();
			} else {
				recoverableError("Expected 'class'", -1, "", fileName);
			}

			if (isIdentifier()) {
				className = input.getIdentifier();
				if (!className.equals(expectedClassName)) {
					recoverableError("The class name doesn't match the file name",
								  -1, "", fileName);
				}
				input.advance();
			} else {
				recoverableError("Expected a class name", -1, "", fileName);
				className = expectedClassName;
			}
			identifiers = new SymbolTable(className);

			if (isSymbol('{')) {
				input.advance();
			} else {
				recoverableError("Expected {", -1, "", fileName);
			}

			compileFieldAndStaticDeclarations();
			compileAllSubroutines();

			if (!isSymbol('}')) {
				recoverableError("Expected }", -1, "", fileName);
			}

			if (input.hasMoreTokens()) {
				recoverableError("Expected end-of-file", -1, "", fileName);
			}

		} catch (JackException je) {
		} finally {
			output.close();
			if (validJack) {
				classes.add(className);
			}
			return validJack;
		}
    }

	/**
	 * Verifies that all classes compiled by this CompilationEngine so far
	 * do not contain code that incorrectly calls subroutines in classes
	 * compiled by this CompilationEngine so far. Returns true if no
	 * inconsistencies were found.
	 */
	public boolean verifySubroutineCalls() {
		validJack = true;
		Iterator i = subroutineCalls.iterator();
		while (i.hasNext()) {
			// Call data
			Object[] o = (Object[])i.next();
			String subroutine = (String)o[0];
			boolean calledAsMethod = ((Boolean)o[1]).booleanValue();
			short numberOfCalledParameters = ((Short)o[2]).shortValue();
			String callingFileName = (String)o[3];
			String callingSubroutine = (String)o[4];
			int lineNumber = ((Integer)o[5]).intValue();

			// Allow all calls to subroutines in classes which were not
			// compiled (or not compiled successfully) in this run.
			if (!classes.contains(subroutine.substring(0,
													   subroutine.indexOf(".")))) {
				continue;
			}

			// Make sure the declaration data exists and get it
			if (!subroutines.containsKey(subroutine)) {
				recoverableError((calledAsMethod? "Method " :
								               "Function or constructor ") +
							  subroutine + " doesn't exist", lineNumber,
						      callingSubroutine, callingFileName);
			} else {
				o = (Object[])subroutines.get(subroutine);
				int declaredType = ((Integer)o[0]).intValue();
				short numberOfDeclaredParameters = ((Short)o[1]).shortValue();

				// Verify call matches declaration
				if (calledAsMethod &&
					declaredType != SymbolTable.SUBROUTINE_TYPE_METHOD) {
					recoverableError((declaredType ==
								   SymbolTable.SUBROUTINE_TYPE_FUNCTION?
									  "Function " : "Constructor ") +
								  subroutine +
								  " called as a method", lineNumber,
								  callingSubroutine, callingFileName);
				} else if ((!calledAsMethod) &&
					declaredType == SymbolTable.SUBROUTINE_TYPE_METHOD) {
					recoverableError("Method " + subroutine +
								  " called as a function/constructor",
								  lineNumber, callingSubroutine,
								  callingFileName);
				}
				if (numberOfCalledParameters != numberOfDeclaredParameters) {
					recoverableError("Subroutine " + subroutine +
								  " (declared to accept " +
								  numberOfDeclaredParameters +
								  " parameter(s)) called with " +
								  numberOfCalledParameters +
								  " parameter(s)", lineNumber,
								  callingSubroutine, callingFileName);
				}
			}
		}
		return validJack;
	}

/*************************************************************************************************/
/*			Methods for compiling the structure of all the methods and functions 				 */
/*************************************************************************************************/

    // Compiles all the subroutines in the current class
    private void compileAllSubroutines() throws JackException {
        while (isKeywordMethod() || isKeywordFunction() ||
			   isKeywordConstructor()) {
			try {
				if (isKeywordMethod()) {
					compileMethod();
				} else if (isKeywordFunction()) {
					compileFunction();
				} else {
					compileConstructor();
				}
			} catch (JackException e) {
				// Skip to start of next method	or to last token
				while ((!isKeywordMethod()) && (!isKeywordFunction()) &&
					   (!isKeywordConstructor()) && input.hasMoreTokens()) {
					input.advance();
				}
			}
        }
    }

    // Compiles a complete method
    private void compileMethod() throws JackException {
        compileSubroutine(identifiers.SUBROUTINE_TYPE_METHOD);
    }

    // Compiles a complete function
    private void compileFunction() throws JackException {
        compileSubroutine(identifiers.SUBROUTINE_TYPE_FUNCTION);
    }

    // Compiles a complete constructor
    private void compileConstructor() throws JackException {
        compileSubroutine(identifiers.SUBROUTINE_TYPE_CONSTRUCTOR);
    }

    // Compiles a complete subroutine (called by compileMethod and compileFunction)
    private void compileSubroutine(int subroutineType) throws JackException {
        this.subroutineType = subroutineType;
        ifCounter = 0;
        whileCounter = 0;
        input.advance();
        String type = null;

        // Checks for a legal return type (if not legal, a syntax error 
		// will be raised).
        if (isKeywordVoid()) {
            type = "void";
        } else {
            type = getType();
        }
		int typeLineNumber = input.getLineNumber();

        input.advance();
		String subroutineName = null;
		String fullName;
        if (isIdentifier()) {
			subroutineName = input.getIdentifier();
			fullName = identifiers.getClassName() + "." +
					   subroutineName;
			if (subroutines.containsKey(fullName)) {
				recoverableError("Subroutine " + subroutineName + " redeclared",
							  -1, "", fileName);
			}
			input.advance();
		} else {
            recoverableError("Expected a type followed by a subroutine name", -1, "", fileName); // forgotten type triggers this line as well
			// Just some default (code will be deleted anyway
			fullName = identifiers.getClassName() + "." + "unknownname";
        }

		switch (subroutineType) {
			case SymbolTable.SUBROUTINE_TYPE_METHOD:
				identifiers.startMethod(subroutineName, type);
				break;
			case SymbolTable.SUBROUTINE_TYPE_FUNCTION:
				identifiers.startFunction(subroutineName, type);
				break;
			case SymbolTable.SUBROUTINE_TYPE_CONSTRUCTOR:
				identifiers.startConstructor(subroutineName);
				if (!type.equals(identifiers.getClassName())) {
					recoverableError("The return type of a constructor must be of the class type", typeLineNumber);
				}
				break;
		}

		if (isSymbol('(')) {
			input.advance();
		} else {
			recoverableError("Expected (");
		}

		short numberOfParameters =
			compileParametersList(); // Returns when we've reached ')'
		input.advance();
		compileSubroutineBody(fullName);

		identifiers.endSubroutine();

		if (subroutineName != null) {
			// If this is a redeclaration (an error was already raised)
			// then the last definition rules for verifySubroutineCalls
			// purposes - this does not matter since calls to subroutines
			// in erroneous classes are not verified.
			subroutines.put(fullName,
							new Object[]{new Integer(subroutineType),
										 new Short(numberOfParameters)});
		}
    }

    // Compiles the body of the subroutine, including the enclosing "{}"
    private void compileSubroutineBody(String fullName) throws JackException {

        if (isSymbol('{')) {
			input.advance();
		} else {
            recoverableError("Expected {");
        }

		compileLocalsDeclarations();
		short numberOfLocals =
			identifiers.getNumberOfIdentifiers(identifiers.KIND_LOCAL);
		output.function(fullName,numberOfLocals);

		if (subroutineType == identifiers.SUBROUTINE_TYPE_METHOD) {
			output.push(HVMInstructionSet.ARG_SEGMENT_VM_STRING, (short)0);
			output.pop(HVMInstructionSet.POINTER_SEGMENT_VM_STRING, (short)0);
		} else if (subroutineType == identifiers.SUBROUTINE_TYPE_CONSTRUCTOR) {
			short numberOfFields = identifiers.getNumberOfIdentifiers(identifiers.KIND_FIELD);
			output.push(HVMInstructionSet.CONST_SEGMENT_VM_STRING, numberOfFields);
			output.callFunction("Memory.alloc", (short)1);
			output.pop(HVMInstructionSet.POINTER_SEGMENT_VM_STRING, (short)0);
		}

		// Compile everything until '}'
		if (compileStatements(true)) {
			// The '}' is reachable by the program flow...
			recoverableError("Program flow may reach end of subroutine without 'return'");
		}

		input.advance();
    }

/*************************************************************************************************/
/*			Methods for compiling declarations					 */
/*************************************************************************************************/

    /*
     Compiles a (possibly empty) parameters list, not including
     the enclosing "()" and returns the number of parameters.
    */
    private short compileParametersList() throws JackException {
		short numberOfParameters = 0;

        if (isSymbol(')')) {
            return numberOfParameters;
        }

        boolean hasMoreElements = true;

        while (hasMoreElements) {
			++numberOfParameters;

            String paramType = getType();
            input.advance();

			String paramName = null;
            if (isIdentifier()) {
				paramName = input.getIdentifier();
				input.advance();
			} else {
                recoverableError("Expected a type followed by a variable name"); // forgotten type triggers this line as well
            }
			identifiers.define(paramName,paramType,identifiers.KIND_PARAMETER);

			if (isSymbol(')')) {
				hasMoreElements = false;
			} else if (isSymbol(',')) {
				input.advance();
			} else {
				terminalError("Expected ) or , in parameters list");
			}
        }

		return numberOfParameters;
    }

    // Compiles the field and static declarations
    private void compileFieldAndStaticDeclarations() throws JackException {

        boolean cont = true;

        while (cont) {
            if (isKeywordField()) {
                compileDeclarationLine(identifiers.KIND_FIELD);
			} else if (isKeywordStatic()) {
                compileDeclarationLine(identifiers.KIND_STATIC);
			} else {
                cont = false;
			}
        }
    }

    // Compiles the locals declarations
    private void compileLocalsDeclarations() throws JackException {
        while (isKeywordLocal()) {
            compileDeclarationLine(identifiers.KIND_LOCAL);
        }
    }

    /*
     Compiles a declaration line of the given kind.
     The given kind must be one of the following:
     identifiers.KIND_FIELD,identifiers.KIND_LOCAL or identifiers.KIND_STATIC
    */
    private void compileDeclarationLine(int kind) throws JackException {

        // Gets the type of the identifier in the line
        input.advance();
        String type = getType();

        // Iterates on the identifiers and define them according to the
        // given kind
        boolean hasMoreElements = true;
        while (hasMoreElements) {
            input.advance();
            if (isIdentifier()) {
				identifiers.define(input.getIdentifier(),type,kind);
				input.advance();
			} else {
                recoverableError("Expected a type followed by comma-seperated variable names"); // forgotten type triggers this line as well
            }

			if (isSymbol(';')) {
				input.advance();
				hasMoreElements = false;
			} else if (!isSymbol(',')) {
				terminalError("Expected , or ;");
			}
        }
    }


/*************************************************************************************************/
/*		Methods for compiling the statements of the subroutines				 */
/*************************************************************************************************/

	// Skip to after ; or to } (whichever is nearer)
	private void skipToEndOfStatement() {
		while ((!isSymbol(';')) && (!isSymbol('}')) && input.hasMoreTokens()) {
			input.advance();
		}
		if (isSymbol(';') && input.hasMoreTokens()) {
			input.advance();
		}
	}

    // Compiles a sequence of statements, not including the enclosing "{}"
	// Return true if the reachable argument is true (=the first statement
	// is reachable), and if when starting from the first statement, the program
	// flow could reach past the last one (without being obstructed by a
	// 'return' statement).
    private boolean compileStatements(boolean reachable) throws JackException {
		// if unreachable, caller has reported unreachability
		boolean reportedUnreachable = !reachable;

        while (!isSymbol('}')) {
			if (!reachable && !reportedUnreachable) {
				warning("Unreachable code");
				reportedUnreachable = true;
			}
            if (isKeywordDo()) {
				try {
					compileDo(); // can't change reachable status
				} catch (JackException e) {
					skipToEndOfStatement();
				}
			} else if (isKeywordLet()) {
                try {
					compileLet(); // can't change reachable status
				} catch (JackException e) {
					skipToEndOfStatement();
				}
			} else if (isKeywordWhile()) {
				reachable = compileWhile(reachable); // might control flow
			} else if (isKeywordReturn()) {
                try {
					compileReturn();
				} catch (JackException e) {
					skipToEndOfStatement();
				}
				reachable = false;
			} else if (isKeywordIf()) {
				reachable = compileIf(reachable); // might control flow
			} else if (isSymbol(';')) { // common
				recoverableError("An empty statement is not allowed");
				input.advance();
			} else {
				String err = "Expected statement(do, let, while, return or if)";
				if (isIdentifier()) {
					// probably merely forgot do or let
					recoverableError(err);
					skipToEndOfStatement();
				} else {
					terminalError(err);
				}
			}
        }

		return reachable;
    }

    // Compiles a do statement
    private void compileDo() throws JackException {
        input.advance();
        compileSubroutineCall();
        // Pops the junk return value into the Temp segment (garbage)
        output.pop(HVMInstructionSet.TEMP_SEGMENT_VM_STRING, (short)0);
        if (isSymbol(';')) {
            input.advance();
        } else {
            recoverableError("Expected ;");
        }
    }

    // This method compiles the subroutine call
    private void compileSubroutineCall() throws JackException {
        if (!isIdentifier()) {
            terminalError("Expected class name, subroutine name, field, parameter or local or static variable name");
		}
		// Class name, var name or a name of a subroutine of this class
		String name = input.getIdentifier();
		int lineNumber = input.getLineNumber();
		input.advance();
		if (isSymbol('.')) { // The subroutine doesn't belong to this object
			compileExternalSubroutineCall(name);
		} else {			 // The subrountine belongs to this object
			compileInternalSubroutineCall(name, lineNumber);
		}
    }

    /*
     Compiles an external subroutine call. That is, a call of the form:
     1. Class-name.function-name(expression-list)
     2. Variable-name.method-name(expression-list)
     At the beggining the input stands on '.'
    */
    private void compileExternalSubroutineCall(String name)
			throws JackException {
        String type = null;
        String fullName = null;
        boolean isMethod = true;

        input.advance();
		int lineNumber = input.getLineNumber();

        try {
            type = identifiers.getTypeOf(name);
            int kind = identifiers.getKindOf(name);
            short index = identifiers.getIndexOf(name);
            pushVariable(kind,index);  // Pushes "this" to the stack
        } catch (JackException je) {
            type = name;
            isMethod = false;
        }

        if (isIdentifier()) {
			fullName = type + "." + input.getIdentifier();
            input.advance();
        } else {
            terminalError("Expected subroutine name");
        }

        // Will compile the expression list and push them as parameters
        // into the stack. Will return the number of expressions.
        short numberOfArguments = compileExpressionList();
        output.callFunction(fullName,
							(short)(numberOfArguments + (isMethod? 1 : 0)));

		Object callData =
			new Object[]{fullName,
						 new Boolean(isMethod),
						 new Short(numberOfArguments),
						 fileName,
						 identifiers.getSubroutineName(),
						 new Integer(lineNumber)};
		subroutineCalls.addElement(callData);
    }

    // Compiles an internal subroutine call (call to subroutine of the same class)
    // only internal Methods may be called, not Functions or Constructors.
	// lineNumber is the line number in the source where the subroutine name
	// appeared.
    private void compileInternalSubroutineCall(String name,
			                                   int lineNumber)
			throws JackException {

        String fullName = null;
        fullName = identifiers.getClassName() + "." + name;

        // Pushes "this" of the current object
        output.push(HVMInstructionSet.POINTER_SEGMENT_VM_STRING, (short)0);

        // Will compile the expression list and push them as parameters
        // into the stack. Will return the number of expressions.
        short numberOfArguments = compileExpressionList();
        output.callFunction(fullName,(short)(numberOfArguments+1));

		if (subroutineType == SymbolTable.SUBROUTINE_TYPE_FUNCTION) {
			recoverableError("Subroutine " + fullName +
                             " called as a method from within a function",
                             lineNumber);
		} else {
			Object callData =
				new Object[]{fullName,
							 Boolean.TRUE,
							 new Short(numberOfArguments),
							 fileName,
							 identifiers.getSubroutineName(),
							 new Integer(lineNumber)};
			subroutineCalls.addElement(callData);
		}
    }

	// Skip to ; or } or { (whichever is nearest)
	private void skipFromParensToBlockStart() {
		while ((!isSymbol(';')) && (!isSymbol('}')) && (!isSymbol('{')) &&
				input.hasMoreTokens()) {
			input.advance();
		}
	}

    // Compiles a while statement
	// Return true if the reachable argument is true (=the first statement
	// is reachable), and if when starting from the first statement, the program
	// flow could exit the while loop not via a return.
    private boolean compileWhile(boolean reachable) throws JackException {
        int currentWhileCounter = whileCounter;

        whileCounter++;
        input.advance();

        if (isSymbol('(')) {
            input.advance();
        } else {
            recoverableError("Expected (");
        }

		try {
			output.label("WHILE_EXP" + currentWhileCounter);
			compileNewExpression(GENERAL_TYPE);
			output.not();
			if (isSymbol(')')) {
				input.advance();
			} else {
				recoverableError("Expected )");
			}
		} catch (JackException e) {
			skipFromParensToBlockStart();
			if  (!isSymbol('{')) {
				throw e; // can't fix
			}
		}

		if (isSymbol('{')) {
			input.advance();
		} else {
			recoverableError("Expected {");
		}

        output.ifGoTo("WHILE_END" + currentWhileCounter);
        reachable = compileStatements(reachable);

		if (isSymbol('}')) {
			input.advance();
		} else {
			recoverableError("Expected }");
		}

		output.goTo("WHILE_EXP" + currentWhileCounter);
		output.label("WHILE_END" + currentWhileCounter);

		return reachable;
    }

    // Compiles an If statement - the else part is not mandatory
	// Return true if the reachable argument is true (=the first statement
	// is reachable), and if when starting from the first statement, the program
	// flow could exit the if or the else block not via a return.
    private boolean compileIf(boolean reachable) throws JackException {
        int currentIfCounter = ifCounter;
        ifCounter++;
        input.advance();
        if (isSymbol('(')) {
            input.advance();
        } else {
            recoverableError("Expected (");
        }

		try {
			compileNewExpression(GENERAL_TYPE);
			if (isSymbol(')')) {
				input.advance();
			} else {
				recoverableError("Expected )");
			}
		} catch (JackException e) {
			skipFromParensToBlockStart();
			if  (!isSymbol('{')) {
				throw e; // can't fix
			}
		}

		if (isSymbol('{')) {
			input.advance();
		} else {
			recoverableError("Expected {");
		}

		output.ifGoTo("IF_TRUE" + currentIfCounter);
		output.goTo("IF_FALSE" + currentIfCounter);
		output.label("IF_TRUE" + currentIfCounter);
		boolean ifEndReachable = compileStatements(reachable);

		if (isSymbol('}')) {
			input.advance();
		} else {
			recoverableError("Expected }");
		}

		// Checks whether the statement contain an else part
		if (isKeywordElse()) {
			input.advance();
		} else {
			output.label("IF_FALSE" + currentIfCounter);
			return true; // end of if with no else is always reachable
		}

		if (isSymbol('{')) {
			input.advance();
		} else {
			recoverableError("Expected {");
		}

		output.goTo("IF_END" + currentIfCounter);
		output.label("IF_FALSE" + currentIfCounter);
		boolean elseEndReachable = compileStatements(reachable);

		if (isSymbol('}')) {
			input.advance();
		} else {
			recoverableError("Expected }");
		}

		output.label("IF_END" + currentIfCounter);

		return ifEndReachable || elseEndReachable;
    }

    // Compiles a return statement with or without a return value
    private void compileReturn() throws JackException {
        input.advance();

        // constructors must return 'this'
        if (subroutineType == SymbolTable.SUBROUTINE_TYPE_CONSTRUCTOR &&
		    !isKeywordThis()) {
            recoverableError("A constructor must return 'this'");
		}

        if (isSymbol(';')) {
			if (!identifiers.getReturnType().equals("void")) {
				recoverableError("A non-void function must return a value");
			}
            // No return value: push zero
            output.push(HVMInstructionSet.CONST_SEGMENT_VM_STRING, (short)0);
            output.returnFromFunction();
            input.advance();
        } else {
			if (identifiers.getReturnType().equals("void")) {
				recoverableError("A void function must not return a value");
			}
            compileNewExpression(GENERAL_TYPE);  // Leaves the value of
                                                 // the expression at the top
                                                 // of the stack
            output.returnFromFunction();
            if (isSymbol(';')) {
                input.advance();
            } else {
                recoverableError("Expected ;");
            }
        }
    }

    // Compiles a let expression
    private void compileLet() throws JackException {
        input.advance();

        if (!isIdentifier()) {
            terminalError("Expected field, parameter or local or static variable name");
        }

		String lhsName = input.getIdentifier();
		int lhsKind;
		short lhsIndex;
		String type;
		try {
			lhsKind = identifiers.getKindOf(lhsName);
			lhsIndex = identifiers.getIndexOf(lhsName);
			type = identifiers.getTypeOf(lhsName);
		} catch (JackException je) {
			recoverableError(lhsName + " is not defined as a field, parameter or local or static variable");
			// Default to something. The generated code will be deleted anyway.
			lhsKind = identifiers.KIND_STATIC;
			lhsIndex = 0;
			type = "int";
		}
		input.advance();

		if (isSymbol('=')) {
			input.advance();
			int newType = compileNewExpression(GENERAL_TYPE); // The result of the r.h.s. expression
															  // is now on the top of the stack
			popVariable(lhsKind,lhsIndex);

			if (newType != GENERAL_TYPE) {
				if (type.equals("int") && newType != INT_TYPE && newType != NUMERIC_TYPE)
					recoverableError("an int value is expected",
								  input.getLineNumber()-1);
				else if (type.equals("char") && newType != CHAR_TYPE && newType != NUMERIC_TYPE)
					recoverableError("a char value is expected",
								  input.getLineNumber()-1);
				else if (type.equals("boolean") && newType != NUMERIC_TYPE && newType != INT_TYPE
						 && newType != BOOLEAN_TYPE)
					recoverableError("a boolean value is expected",
								  input.getLineNumber()-1);
			}
		} else if (isSymbol('[')) {
			input.advance();
			compileNewExpression(NUMERIC_TYPE); // Will compile the expression inside the '[...]'

			if (isSymbol(']')) {
				input.advance();
			} else {
				terminalError("Expected ]"); // important enough to be terminal (+ parsing will continue at end of statement)
			}

			pushVariable(lhsKind,lhsIndex);
			output.add();

			if (isSymbol('=')) {
				input.advance();
			} else {
				terminalError("Expected ="); // important enough to be terminal (+ parsing will continue at end of statement)
			}

			compileNewExpression(GENERAL_TYPE);
			output.pop(HVMInstructionSet.TEMP_SEGMENT_VM_STRING, (short)0);
			output.pop(HVMInstructionSet.POINTER_SEGMENT_VM_STRING, (short)1);
			output.push(HVMInstructionSet.TEMP_SEGMENT_VM_STRING, (short)0);
			output.pop(HVMInstructionSet.THAT_SEGMENT_VM_STRING, (short)0);

		} else {
			terminalError("Expected [ or =");
		}

		if (isSymbol(';')) {
			input.advance();
		} else {
			recoverableError("Expected ;");
		}
    }


/*************************************************************************************************/
/*			Methods for compiling expressions					 */
/*************************************************************************************************/

    /*
     Compiles an expression list including the enclosing '()'
     and returns the number of expressions
    */
    private short compileExpressionList() throws JackException {

        if (isSymbol('(')) {
            input.advance();
        } else {
            terminalError("Expected ("); // important enough to be terminal (+ parsing will continue at end of statement / start of block if in while or if condition)
        }

		short numberOfExpressions = 0;
		if (isSymbol(')')) {
			input.advance();
		} else {
			boolean hasMoreElements = true;
			while (hasMoreElements) {
				compileNewExpression(GENERAL_TYPE);
				numberOfExpressions++;
				if (isSymbol(',')) {
					input.advance();
				} else if (isSymbol(')')) {
					hasMoreElements = false;
					input.advance();
				} else  {
					terminalError("Expected , or ) in expression list");
				}
			}
		}
		return numberOfExpressions;
    }

    // Starts compiling an expression
    private int compileNewExpression(int expectedType) throws JackException {
        expIndex++;
        setExpType(expectedType);
        compileExpression();
        expIndex--;

        return expTypes[expIndex + 1];
    }


    /*
     Compiles an expression. An expression is one of:
     1. ( expression )
     2. unary-op expression, where unary-op is one of "-,~".
     3. term op expression, where op is one of "+,-,*,/,&,|,>,<,="
    */
    private void compileExpression() throws JackException {

        boolean cont = false;

        compileTerm();

        do {
            if (input.getTokenType() == input.TYPE_SYMBOL) {
                char symbol = input.getSymbol();
                cont = (symbol == '+' || symbol == '-' ||
						symbol == '*' || symbol == '/' ||
                        symbol == '&' || symbol == '|' ||
					   	symbol == '>' || symbol == '<' ||
                        symbol == '=');

                if (cont) {
                    input.advance();
                    compileTerm();

                    switch (symbol) {
                        case '+':
                            output.add(); break;
                        case '-':
                            output.substract(); break;
                        case '*':
                            output.callFunction("Math.multiply",(short)2); break;
                        case '/':
                            output.callFunction("Math.divide",(short)2); break;
                        case '&':
                            output.and(); break;
                        case '|':
                            output.or(); break;
                        case '>':
                            output.greaterThan(); break;
                        case '<':
                            output.lessThan(); break;
                        case '=':
                            output.equal(); break;
                    }
                }
            }
        } while (cont);
    }

    /*
     Compiles a term in an expression. A term is one of:
     1. A constant (integer,string or a keyword-constant: true, false, void)
     2. A variable name (static, field, parameter or var)
     3. Array-name[expression]
     4. Subroutine-name(expression-list) - The subroutine does not return void
     5. Class-name.functionName(expression-list) - The function does not return void
     6. Variable-name.method-name(expression-list) - The method does not return void
     7. (expression)
    */
    private void compileTerm() throws JackException {
        switch (input.getTokenType()) {
            case JackTokenizer.TYPE_INT_CONST:
                compileIntConst();
                break;
            case JackTokenizer.TYPE_STRING_CONST:
                compileStringConst();
                break;
            case JackTokenizer.TYPE_KEYWORD:
                compileKeywordConst();
                break;
            case JackTokenizer.TYPE_IDENTIFIER:
                compileIdentifierTerm();
                break;
            default:
                if (isSymbol('-')) { // a term of the form -term (negation)
                    input.advance();
                    compileTerm();
                    output.negate();
                } else if (isSymbol('~')) { // a term of the form ~term (not)
                    input.advance();
                    compileTerm();
                    output.not();
                } else if (isSymbol('(')) { // a term of the form (expression)
                    input.advance();
                    compileNewExpression(GENERAL_TYPE);
                    if (isSymbol(')')) {
                        input.advance();
                    } else {
                        terminalError("Expected )"); // important enough to be terminal (+ parsing will continue at end of statement / start of block if in while or if condition)
                    }
                } else {
                    terminalError("Expected - or ~ or ( in term");
                }
        }
    }

    // Compiles an Int constant
    private void compileIntConst() throws JackException {
        if (input.getIntValue() > 32767) {
            recoverableError("Integer constant too big");
		}

        short value = (short)input.getIntValue();
        output.push(HVMInstructionSet.CONST_SEGMENT_VM_STRING, value);

        if (getExpType() < NUMERIC_TYPE) {
            setExpType(NUMERIC_TYPE);
		} else if (getExpType() > CHAR_TYPE) {
			recoverableError("a numeric value is illegal here");
		}

        input.advance();
    }

    // Compiles an String constant
    private void compileStringConst() throws JackException {
        if (getExpType() == GENERAL_TYPE) {
            setExpType(STRING_TYPE);
		} else {
            recoverableError("A string constant is illegal here");
		}

        String stringConst = input.getStringValue();
        short length = (short)stringConst.length();
        output.push(HVMInstructionSet.CONST_SEGMENT_VM_STRING, length);
        output.callFunction("String.new",(short)1);	// Calls OS method "String new(int maxLength)"
        for (short i=0; i<length; i++) {
            output.push(HVMInstructionSet.CONST_SEGMENT_VM_STRING, (short)stringConst.charAt(i));
            // first arg is the reference to the string which was left on the stack
            output.callFunction("String.appendChar",(short)2);
        }
        input.advance();
    }

    // Compiles a keyword-constant (true, false,null,this)
    private void compileKeywordConst() throws JackException {
        int keywordType = input.getKeywordType();

		switch (keywordType) {
			case JackTokenizer.KW_TRUE:
				output.push(HVMInstructionSet.CONST_SEGMENT_VM_STRING, (short)0);
				output.not();
				break;
			case JackTokenizer.KW_FALSE: /* FALLTHRU */
			case JackTokenizer.KW_NULL:
				output.push(HVMInstructionSet.CONST_SEGMENT_VM_STRING, (short)0);
				break;
			case JackTokenizer.KW_THIS:
				if (subroutineType == identifiers.SUBROUTINE_TYPE_FUNCTION) {
					recoverableError("'this' can't be referenced in a function");
				}
				output.push(HVMInstructionSet.POINTER_SEGMENT_VM_STRING, (short)0);
				break;
			default:
				terminalError("Illegal keyword in term");
				break;
		}

		switch (keywordType) {
			case JackTokenizer.KW_TRUE: /* FALLTHRU */
			case JackTokenizer.KW_FALSE:
				if (getExpType() <= NUMERIC_TYPE) {
					setExpType(BOOLEAN_TYPE);
				} else {
					recoverableError("A boolean value is illegal here");
				}
				break;
			case JackTokenizer.KW_NULL:
				if (getExpType() == GENERAL_TYPE) {
					setExpType(NULL_TYPE);
				} else {
					recoverableError("'null' is illegal here");
				}
				break;
			case JackTokenizer.KW_THIS:
				if (getExpType() == GENERAL_TYPE) {
					setExpType(THIS_TYPE);
				} else {
					recoverableError("'this' is illegal here");
				}
				break;
        }

		input.advance();
    }

    /*
     Compiles an identifier term, that can be:
     1. Array-name[expression]
     2. Internal or external Subroutine call
     3. A variable name (static, field, parameter or var)
    */
    private void compileIdentifierTerm() throws JackException {
        if (getExpType() == STRING_TYPE) {
            recoverableError("Illegal casting into String constant");
		}

        String name = input.getIdentifier();
		int lineNumber = input.getLineNumber();
        input.advance();
        if (isSymbol('[')) {			// Handles term of the form Array-name[expression]

            input.advance();
            compileNewExpression(NUMERIC_TYPE);
			try {
				pushVariable(identifiers.getKindOf(name), identifiers.getIndexOf(name));
			} catch (JackException e) {
				recoverableError(name + " is not defined as a field, parameter or local or static variable", lineNumber);
			}
            output.add();

            // set 'that' to point at the required array element
            output.pop(HVMInstructionSet.POINTER_SEGMENT_VM_STRING, (short)1);
            output.push(HVMInstructionSet.THAT_SEGMENT_VM_STRING, (short)0);

            if (isSymbol(']')) {
                input.advance();
            } else {
                terminalError("Expected ]"); // important enough to be terminal (+ parsing will continue at end of statement / start of block if in while or if condition)
            }
        } else if (isSymbol('(')) {
            compileInternalSubroutineCall(name, lineNumber);	// Handles internal subroutine call
        } else if (isSymbol('.')) {
            compileExternalSubroutineCall(name);	// Handles external subroutine call
        } else {									// Handles variables
			try {
				int kind = identifiers.getKindOf(name);
				short index = identifiers.getIndexOf(name);

				// check that a field is not referenced in a function
				if (subroutineType == SymbolTable.SUBROUTINE_TYPE_FUNCTION &&
					kind == SymbolTable.KIND_FIELD) {
					recoverableError("A field may not be referenced in a function", lineNumber);
				}

				pushVariable(kind, index);

				String type = identifiers.getTypeOf(name);

				if (type.equals("int")) {
					if (getExpType() <= NUMERIC_TYPE) {
						setExpType(INT_TYPE);
					} else if (getExpType() > INT_TYPE) {
						recoverableError("An int value is illegal here", lineNumber);
					}
				} else if (type.equals("char")) {
					if (getExpType() <= NUMERIC_TYPE) {
						setExpType(CHAR_TYPE);
					} else if (getExpType() > CHAR_TYPE || getExpType()==INT_TYPE) {
						recoverableError("A char value is illegal here", lineNumber);
					}
				} else if (type.equals("boolean")) {
					if (getExpType() <= NUMERIC_TYPE) {
						setExpType(BOOLEAN_TYPE);
					} else if (getExpType() != BOOLEAN_TYPE) {
						recoverableError("A boolean value is illegal here", lineNumber);
					}
				}
			} catch (JackException e) {
				recoverableError(name + " is not defined as a field, parameter or local or static variable", lineNumber);
			}
        }
    }

    // Return the type of the top compiled expression
    private int getExpType() {
        return expTypes[expIndex];
    }

    // Sets the type of the top compiled expression with the given type
    private int setExpType(int type) {
        return expTypes[expIndex] = type;
    }

/*************************************************************************************************/
/*			General purpose methods							 */
/*************************************************************************************************/

    /*
     Returns the type of the current token.
     This type can be "int","boolean","char" or class name.
     If the type is not any of the above, JackException
     is thrown.
    */
    private String getType() throws JackException {
        String type = null;
        if (input.getTokenType() == input.TYPE_KEYWORD) {
            switch (input.getKeywordType()) {
                case JackTokenizer.KW_INT:
                    type = "int";
                    break;
                case JackTokenizer.KW_BOOLEAN:
                    type = "boolean";
                    break;
                case JackTokenizer.KW_CHAR:
                    type = "char";
                    break;
                default:
                    terminalError("Expected primitive type or class name"); // uncommon enough to be terminal (most probably something else went wrong earlier)
            }
        } else if (isIdentifier()) {
			type = input.getIdentifier();
		} else {
			terminalError("Expected primitive type or class name"); // uncommon enough (forgot both type & variable which other would've been recongized as a type) to be terminal (most probably something else went wrong earlier)
        }
        return type;
    }

    // Pushes a variable of the given kind with the given index
    private void pushVariable(int kind,short index) throws JackException {
        switch (kind) {
            case SymbolTable.KIND_PARAMETER:
                output.push(HVMInstructionSet.ARG_SEGMENT_VM_STRING, index);
                break;
            case SymbolTable.KIND_LOCAL:
                output.push(HVMInstructionSet.LOCAL_SEGMENT_VM_STRING, index);
                break;
            case SymbolTable.KIND_FIELD:
                output.push(HVMInstructionSet.THIS_SEGMENT_VM_STRING, index);
                break;
            case SymbolTable.KIND_STATIC:
                output.push(HVMInstructionSet.STATIC_SEGMENT_VM_STRING, index);
                break;
            default:
                terminalError("Internal Error: Illegal kind");
				break;
        }
    }

    // Pops into a variable of the given kind with the given index
    private void popVariable(int kind,short index) throws JackException {
        switch (kind) {
            case SymbolTable.KIND_LOCAL:
                output.pop(HVMInstructionSet.LOCAL_SEGMENT_VM_STRING, index);
                break;
            case SymbolTable.KIND_PARAMETER:
                output.pop(HVMInstructionSet.ARG_SEGMENT_VM_STRING, index);
                break;
            case SymbolTable.KIND_FIELD:
                output.pop(HVMInstructionSet.THIS_SEGMENT_VM_STRING, index);
                break;
            case SymbolTable.KIND_STATIC:
                output.pop(HVMInstructionSet.STATIC_SEGMENT_VM_STRING, index);
                break;
        }
    }

/*************************************************************************************************/
/*		Boolean methods for checking the type of the current token			 */
/*************************************************************************************************/

    // Returns true is the current token is a "class" keyword, false otherwise
    private boolean isKeywordClass() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_CLASS);
    }

    // Returns true is the current token is a "static" keyword, false otherwise
    private boolean isKeywordStatic() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_STATIC);
    }

    // Returns true is the current token is a "field" keyword, false otherwise
    private boolean isKeywordField() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_FIELD);
    }

    // Returns true is the current token is a "var" keyword, false otherwise
    private boolean isKeywordLocal() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_VAR);
    }

    // Returns true is the current token is an identifier, false otherwise
    private boolean isIdentifier() {
        return (input.getTokenType() == input.TYPE_IDENTIFIER);
    }

    // Returns true is the current token is the given symbol, false otherwise
    private boolean isSymbol(char symbol) {
        return (input.getTokenType() == input.TYPE_SYMBOL &&
                input.getSymbol() == symbol);
    }

    // Returns true is the current token is a "method" keyword, false otherwise
    private boolean isKeywordMethod() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_METHOD);
    }

    // Returns true is the current token is a "function" keyword, false otherwise
    private boolean isKeywordFunction() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_FUNCTION);
    }

    // Returns true is the current token is a "constructor" keyword, false otherwise
    private boolean isKeywordConstructor() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_CONSTRUCTOR);
    }

    // Returns true is the current token is a "void" keyword, false otherwise
    private boolean isKeywordVoid() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_VOID);
    }

    // Returns true is the current token is a "do" keyword, false otherwise
    private boolean isKeywordDo() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_DO);
    }

    // Returns true is the current token is a "let" keyword, false otherwise
    private boolean isKeywordLet() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_LET);
    }

    // Returns true is the current token is a "while" keyword, false otherwise
    private boolean isKeywordWhile() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_WHILE);
    }

    // Returns true is the current token is a "return" keyword, false otherwise
    private boolean isKeywordReturn() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_RETURN);
    }

    // Returns true is the current token is a "if" keyword, false otherwise
    private boolean isKeywordIf() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_IF);
    }

    // Returns true is the current token is a "else" keyword, false otherwise
    private boolean isKeywordElse() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_ELSE);
    }

    // Returns true is the current token is a "this" keyword, false otherwise
    private boolean isKeywordThis() {
        return (input.getTokenType() == input.TYPE_KEYWORD &&
            input.getKeywordType() == input.KW_THIS);
    }

	// Halts compilation due to the given terminal error (can't continue
	// parsing) at the current line number, method and class
	private void terminalError(String error) throws JackException {
		terminalError(error, -1, null, null);
	}
							   
	// Halts compilation due to the given terminal error (can't continue
	// parsing) at the given line number in the current method and class
	private void terminalError(String error, int lineNumber)
			throws JackException {
		terminalError(error, lineNumber, null, null);
	}
							   
	// Halts compilation due to the given terminal error (can't continue
	// parsing) at the given line number, method and file name
	private void terminalError(String error, int lineNumber,
								 String subroutine, String fileName)
			throws JackException {
		recoverableError(error, lineNumber, subroutine, fileName);
		throw new JackException(generateMessage(error, lineNumber, subroutine,
					                            fileName));
	}
	
	// Reports the given compilation warning (code is legal working jack anyway)
	// at the current line number, method and file name
	private void warning(String warning) {
		warning(warning, -1, null, null);
	}
							   
	// Reports the given compilation warning (code is legal working jack anyway)
	// at the given line number in the current method and file name
	private void warning(String warning, int lineNumber) {
		warning(warning, lineNumber, null, null);
	}
							   
	// Reports the given compilation warning (code is legal working jack anyway)
	// at the given line number, method and file name
	private void warning(String warning, int lineNumber,
						 String subroutine, String fileName) {
		System.err.println(generateMessage("Warning: "+warning, lineNumber,
					                       subroutine, fileName));
	}
	
	// Reports the given recoverable compilation error (an error after which
	// the rest of the code can still be checked for errors) at the current
	// line number, method and file name
	private void recoverableError(String error) {
		recoverableError(error, -1, null, null);
	}
							   
	// Reports the given recoverable compilation error (an error after which
	// the rest of the code can still be checked for errors) at the given
	// line number in the current method and file name
	private void recoverableError(String error, int lineNumber) {
		recoverableError(error, lineNumber, null, null);
	}
							   
	// Reports the given recoverable compilation error (an error after which
	// the rest of the code can still be checked for errors) at the given
	// line number, method and file name
	private void recoverableError(String error, int lineNumber,
								  String subroutine, String fileName) {
		System.err.println(generateMessage(error, lineNumber, subroutine,
					                       fileName));
		validJack = false;
	}
	
	// Returns a diagnostics message with the given details, in the given
	// line number (current line number if -1),
	// file name (current filename if null),
	// subroutine (current subroutine if null, no subroutine if "")
	// and line number. If class is null, subroutine should be null as well.
	private String generateMessage(String details, int lineNumber,
			                       String subroutine, String fileName) {
		if (fileName == null) fileName = this.fileName;
		if (subroutine == null) subroutine = identifiers.getSubroutineName();
		if (lineNumber == -1) lineNumber = input.getLineNumber();
		return "In "+fileName+" (line "+lineNumber+"): "+
			   ("".equals(subroutine)? "" :
				("In subroutine" + (subroutine==null? "" : // unknownname case
				 (" " + subroutine)) + ": ")) +
			   details;
	}
}

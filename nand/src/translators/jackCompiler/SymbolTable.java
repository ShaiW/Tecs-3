package translators.jackCompiler;

import java.util.*;

/**
 * A SymbolTable for all the symbols in a program. Each symbol has a scope
 * from which it is visible. Each symbol is given a running number, where the
 * numbers start at 0 and are reset in a new scope. The following
 * kinds of identifiers appear in the SymbolTable:
 *
 * static:		[Scope: class]
 * field:		[Scope: class]
 * parameter:	[Scope: subroutine]
 * local:		[Scope: subroutine]
 */
public class SymbolTable {

    /**
     * The Static identifier type
     */
    public final static int KIND_STATIC = 0;

    /**
     * The Object Field identifier type
     */
    public final static int KIND_FIELD = 1;

    /**
     * The Parameter identifier type
     */
    public final static int KIND_PARAMETER = 2;

    /**
     * The Local identifier type
     */
    public final static int KIND_LOCAL = 3;

    /**
     * The Undefined subroutine type
     */
    public final static int SUBROUTINE_TYPE_UNDEFINED = 0;

    /**
     * The Method subroutine type
     */
    public final static int SUBROUTINE_TYPE_METHOD = 1;

    /**
     * The Function subroutine type
     */
    public final static int SUBROUTINE_TYPE_FUNCTION = 2;

    /**
     * The Constructor subroutine type
     */
    public final static int SUBROUTINE_TYPE_CONSTRUCTOR = 3;

    // Numbering for the different scopes
    private static short staticsNumbering;
    private static short fieldsNumbering;
    private static short parametersNumbering;
    private static short localsNumbering;

    // The current class name
    private String className;

    // The current subroutine name
    private String subroutineName;

    // The current subroutine type
    private int subroutineType;

    // The current subroutine return type
    private String returnType;

    // Hashtables for the different kinds
    private HashMap fields;
    private HashMap parameters;
    private HashMap locals;
    private HashMap statics;

    /**
     * Constructs a new SymbolTable of the given file.
     */
    public SymbolTable(String className) {
        this.className = className;
        fields = new HashMap();
        statics = new HashMap();
        parameters = new HashMap();
        locals = new HashMap();
        fieldsNumbering = 0;
        staticsNumbering = 0;

        localsNumbering = 0;

        parametersNumbering = 1;
        subroutineType = SUBROUTINE_TYPE_UNDEFINED;
    }

    /**
     * Starts the scope of a new method with the given method name
	 * and return type.
     * Resets the numbering of the parameters and local variables.
     */
    public void startMethod(String methodName, String returnType) {
        startSubroutine(methodName, SUBROUTINE_TYPE_METHOD, returnType,
				        (short)1);
    }

    /**
     * Starts the scope of a new function with the given function name
	 * and return type.
     * Resets the numbering of the parameters and local variables.
     */
    public void startFunction(String functionName, String returnType) {
        startSubroutine(functionName, SUBROUTINE_TYPE_FUNCTION, returnType,
				        (short)0);
    }

    /**
     * Starts the scope of a new constructor with the given name.
     * Resets the numbering of the parameters and local variables.
     */
    public void startConstructor(String constructorName) {
        startSubroutine(constructorName, SUBROUTINE_TYPE_CONSTRUCTOR, className,
				        (short)0);
    }

    /**
     * Ends the scope of the current subroutine.
     */
    public void endSubroutine() {
        parameters.clear();
        locals.clear();
        localsNumbering = 0;

        parametersNumbering = 1;
        subroutineType = SUBROUTINE_TYPE_UNDEFINED;
        subroutineName = null;
		returnType = null;
    }

    /**
     * Starts the scope of a new subroutine with the given name.
     * Resets the numbering of the parameters and local variables.
     */
    private void startSubroutine(String name, int type, String rtype,
			                     short parametersStart) {
		endSubroutine();
        subroutineName = name;
        subroutineType = type;
		returnType = rtype;
        parametersNumbering = parametersStart;
    }



    /**
     * Defines a new identifier of a given name, type and kind and assigns it
     * a number according to the scope numbering.
     *
     * @param name The identifier name which is used as a key to its properties
     * @param type The type of the identifier - Should be either a primitive Jack
	 *                                                                                                                                                                         			type ("boolean","int","char") or a class name
     * @param kind The kind of the identifier - must be one the following constants:
	 *                                                                                                                                                                         			KIND_STATIC, KIND_FIELD, KIND_PARAMETER, KIND_LOCAL
     */
    public void define(String name,String type, int kind) {
        switch (kind) {
        case KIND_STATIC:
            statics.put(name,new IdentifierProperties(type,staticsNumbering));
            staticsNumbering++;
            break;
        case KIND_FIELD:
            fields.put(name,new IdentifierProperties(type,fieldsNumbering));
            fieldsNumbering++;
            break;
        case KIND_PARAMETER:
            parameters.put(name,new IdentifierProperties(type,parametersNumbering));
            parametersNumbering++;
            break;
        case KIND_LOCAL:
            locals.put(name,new IdentifierProperties(type,localsNumbering));
            localsNumbering++;
            break;
        }
    }

    /**
     * Returns the kind of the identifier according to its name, or throws
     * a JackException if the name of the identifer does not exist.
     * The possible returned kinds when the current scope is a method scope:
     *     *		KIND_STATIC, KIND_FIELD, KIND_PARAMETER, KIND_LOCAL
     * The possible returned kinds when the current scope is a function scope:
     *     *		KIND_STATIC, KIND_PARAMETER, KIND_LOCAL
     *
     * @param name The name of the identifier
     * @exception JackeException When the name of the identifer does not exist
     */
    public int getKindOf(String name) throws JackException {
        int kind;
        if (parameters.containsKey(name)) {
            kind = KIND_PARAMETER;
        }
        else if (locals.containsKey(name)) {
            kind = KIND_LOCAL;
        }
        else if (subroutineType != SUBROUTINE_TYPE_FUNCTION && fields.containsKey(name)) {
            kind = KIND_FIELD;
        }
        else if (statics.containsKey(name)) {
            kind = KIND_STATIC;
        }
        else {
            throw new JackException(name);
        }
        return kind;
    }

    /**
     * Returns the type of the identifier according to its name, or throws
     * a JackException if the name of the identifer does not exist.
     * The possible returned types are:
     *     *		"boolean","int","char" or a class name
     *
     * @param name The name of the identifier
     * @exception JackeException When the name of the identifer does not exist
     */
    public String getTypeOf(String name) throws JackException {
        return getIdentifierProperties(name).getType();
    }

    /**
     * Returns the index of the identifier according to its name, or throws
     * a JackException if the name of the identifer does not exist.
     *
     * @param name The name of the identifier
     * @exception JackeException When the name of the identifer does not exist
     */
    public short getIndexOf(String name) throws JackException {
        return getIdentifierProperties(name).getIndex();
    }

    /**
     * Returns the number of identifiers that exist in the current scope
     * according to the given kind
     *
     * @param kind The given identifier kind - must be one the following constants:
     *             KIND_STATIC, KIND_FIELD, KIND_PARAMETER, KIND_LOCAL
     */
    public short getNumberOfIdentifiers(int kind) {
        short result = -1;
        switch (kind) {
        case KIND_STATIC:
            result = staticsNumbering;
            break;
        case KIND_FIELD:
            result = fieldsNumbering;
            break;
        case KIND_PARAMETER:
            result = (short)(parametersNumbering - 1);  // Parameters numbering starts from 1 in
                                                        // order to reserve index 0 for "this"
            break;
        case KIND_LOCAL:
            result = localsNumbering;
            break;
        }
        return result;
    }

    /**
     * Returns the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the subroutine name
     */
    public String getSubroutineName() {
        return subroutineName;
    }

    /**
     * Returns the subroutine return type
     */
    public String getReturnType() {
        return returnType;
    }

    /*
     Returns an IdentifierProperties object according to the given
     identifier name or throws a JackException if the name of the
     identifier does not exist
    */
    private IdentifierProperties getIdentifierProperties(String name)
                                                    throws JackException {
        IdentifierProperties properties = null;
        switch (getKindOf(name)) {
            case KIND_PARAMETER:
                properties = (IdentifierProperties)parameters.get(name);
                break;
            case KIND_LOCAL:
                properties = (IdentifierProperties)locals.get(name);
                break;
            case KIND_FIELD:
                properties = (IdentifierProperties)fields.get(name);
                break;
            case KIND_STATIC:
                properties = (IdentifierProperties)statics.get(name);
                break;
        }
        return properties;
    }
}

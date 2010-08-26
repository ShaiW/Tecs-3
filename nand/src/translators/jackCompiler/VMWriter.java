package translators.jackCompiler;

import java.io.*;

import translators.VMTranslator.*;

/**
 * A VMWriter. Writes Virtual Machine commands into a given writer.
 */
public class VMWriter implements VirtualMachine {

    // The PrintWriter of this VMWriter
    private PrintWriter writer;

    /**
     * Constructs a new VMWriter with the given PrintWriter
     *
     * @param writer the given PrintWriter
     */
    public VMWriter(PrintWriter writer) {
        this.writer = writer;
    }

    /**
     * Closes the VMwriter.
     */
    public void close() {
        writer.flush();
        writer.close();
    }

    /**
     * integer addition (binary operation).
     */
    public void add() {
        writer.println(HVMInstructionSet.ADD_STRING);
    }

    /**
     * 2's complement integer substraction (binary operation)
     */
    public void substract() {
        writer.println(HVMInstructionSet.SUBSTRACT_STRING);
    }

    /**
     * 2's complement negation (unary operation)
     */
    public void negate() {
        writer.println(HVMInstructionSet.NEGATE_STRING);
    }

    /**
     * Equalaty operation (binary operation). Returns(to the stack)
     * 0xFFFF as true,0x000 as false
     */
    public void equal() {
        writer.println(HVMInstructionSet.EQUAL_STRING);
    }

    /**
     * Greater than operation (binary operation). Returns(to the stack)
     * 0xFFFF as true,0x0000 as false
     */
    public void greaterThan() {
        writer.println(HVMInstructionSet.GREATER_THAN_STRING);
    }

    /**
     * Less Than operation (binary operation). Returns(to the stack)
     * 0xFFFF as true,0x0000 as false
     */
    public void lessThan() {
        writer.println(HVMInstructionSet.LESS_THAN_STRING);
    }

    /**
     * Bit wise "AND" (binary operation).
     */
    public void and() {
        writer.println(HVMInstructionSet.AND_STRING);
    }

    /**
     * Bit wise "OR" (binary operation).
     */
    public void or() {
        writer.println(HVMInstructionSet.OR_STRING);
    }

    /**
     * Bit wise "NOT" (unary operation).
     */
    public void not() {
        writer.println(HVMInstructionSet.NOT_STRING);
    }


    //----  Memory access commands ---//

    /**
     * Pushes the value of the given segment in the given entry to the stack
     */
    public void push(String segment, short entry) {
        writer.println(HVMInstructionSet.PUSH_STRING + " " + segment + " " + entry);
    }

    /**
     * Pops an item from the stack into the given segment in the given entry
     */
    public void pop(String segment, short entry) {
        writer.println(HVMInstructionSet.POP_STRING + " " + segment + " " + entry);
    }


    //----  Program flow commands ---//

    /**
     * Labels the current location in the function code. Only labeled location
     * can be jumped to from other parts of the function.
     * The label - l is 8 bits and is local to the function
     */
    public void label(String l) {
        writer.println(HVMInstructionSet.LABEL_STRING + " " + l);
    }

    /**
     * Goes to the label l
     * The label - l is 8 bits and is local to the function
     */
    public void goTo(String l) {
        writer.println(HVMInstructionSet.GOTO_STRING + " " + l);
    }

    /**
     * Pops a value from the stack and goes to the label l if the value
     * is not zero.
     * The label - l is 8 bits and is local to the function
     */
    public void ifGoTo(String l) {
        writer.println(HVMInstructionSet.IF_GOTO_STRING + " " + l);
    }


    //----  Function calls commands ---//

    /**
     * Here Starts the code of a function according to the given function number
     * that has the given number of local variables.
     * @param functionName The function name
     * @param numberOfLocals The number of local variables
     */
    public void function(String functionName, short numberOfLocals) {
        writer.println(HVMInstructionSet.FUNCTION_STRING + " " + functionName + " "
                       + numberOfLocals);
    }

    /**
     * Returns the value of the function to the top of the stack.
     */
    public void returnFromFunction() {
        writer.println(HVMInstructionSet.RETURN_STRING);
    }

    /**
     * Calls a function according to the given function number stating
     * that the given number of arguments have been pushed onto the stack
     * @param functionName The function name
     * @param numberOfArguments The number of arguments of the function
     */
    public void callFunction(String functionName, short numberOfArguments) {
        writer.println(HVMInstructionSet.CALL_STRING + " " + functionName + " " +
                       numberOfArguments);
    }
}

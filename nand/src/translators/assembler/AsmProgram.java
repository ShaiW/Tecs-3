/********************************************************************************
 * The contents of this file are subject to the GNU General Public License      *
 * (GPL) Version 2 or later (the "License"); you may not use this file except   *
 * in compliance with the License. You may obtain a copy of the License at      *
 * http://www.gnu.org/copyleft/gpl.html                                         *
 *                                                                              *
 * Software distributed under the License is distributed on an "AS IS" basis,   *
 * without warranty of any kind, either expressed or implied. See the License   *
 * for the specific language governing rights and limitations under the         *
 * License.                                                                     *
 *                                                                              *
 * This file was originally developed as part of the software suite that        *
 * supports the book "The Elements of Computing Systems" by Nisan and Schocken, *
 * MIT Press 2005. If you modify the contents of this file, please document and *
 * mark your changes clearly, for the benefit of others.                        *
 ********************************************************************************/

package translators.assembler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import common.Conversions;
import common.Definitions;
import common.collections.BidiMap;

/**
 * Represents an assembly program
 * 
 * @author shaide
 */
public class AsmProgram implements Iterable<AsmProgram.AsmLine>{
	
	private LinkedList<AsmLine> program;
	
	//current line number (without comments)
	private int line;
	
	//Symbol table
	private BidiMap<String, Short> symbols;
	
	private AsmLine lastLine;
	
	
	/** A numeric value which represents a line with no assembly logic */
													//this hack command ins't defined in our assembly
	public static final short NULL_VALUE = (short) Conversions.binaryToInt("1111111111111111");
	
	/** The maximum program size */
	public static final int MAXIMUM_PROGRAM_SIZE = 32768;
	
	/**
	 * Instantiates a new assembly program with the default symbol table
	 */
	public AsmProgram() {
		symbols = Definitions.getInstance().getAddressesTable();
		program = new LinkedList<AsmLine>();
		line = 0;
	}
	
	/**
	 * Instantiates a new assembly program with a given symbol table
	 */
	public AsmProgram(BidiMap<String, Short> symbols) {
		this.symbols = symbols;
		program = new LinkedList<AsmLine>();
		line = 0;
	}
	
	/**
	 * Adds a new label if such doesn't exist
	 * 
	 * @return true if the new label was added
	 */
	public boolean addLabel(short line, String label){
		if (symbols.containsKey(label)) return false;
		symbols.put(label, line);
		return true;
	}
	
	/**
	 * returns a copy of the symbol Table
	 * 
	 * @return a copy of the symbol Table
	 */
	public BidiMap<String, Short> getSymbols(){
		return new BidiMap<String, Short>(symbols);
	}
	
	/**
	 * Adds a new line to the program
	 * 
	 * @param value the short value of the hack command
	 * @param comment a comment to the line
	 * @param label a label to the line
	 */
	public void addLine(short value, String comment){
		AsmLine added = new AsmLine(value, comment, symbols.getKey((short) line), line);
		program.add(added);
		if (value!=NULL_VALUE) {
			lastLine = added;
			line++;
		}
	}
	
	/**
	 * Changes an existing line in the program
	 * 
	 * @param line the line number
	 * @param value the short value of the hack command
	 * @param comment a comment to the line
	 * @param label a label to the line
	 */
	public void setLine(int line, short value, String comment, String label){
		program.add(line, new AsmLine(value, comment, label, line));
	}
	
	/**
	 * Returns a program line
	 * 
	 * @param index the line index
	 * @return the line
	 */
	public AsmLine getLine(int index){
		return program.get(index);
	}
	
	/**
	 * Returns the last line (with assembly logic) added
	 * 
	 * @return the last line added
	 */
	public AsmLine lastLine() {
		return lastLine;
	}
	
	/**
	 * Return the current line-count of the program
	 * 
	 * @return the length of the program
	 */
	public int size(){
		return program.size();
	}
	
	/**
	 * An iterator which doesn't skip block comments
	 * 
	 * @return an iterator of the assembly lines
	 */
	public Iterator<AsmLine> iteratorWithComments() {
		return program.iterator();
	}
	
	
	@Override
	public Iterator<AsmLine> iterator() {
		return new Iterator<AsmLine>(){

			Iterator<AsmLine> program = iterator();
			AsmLine current;
			
			@Override
			public boolean hasNext() {
				while (program.hasNext()) if ((current = program.next()).value() != NULL_VALUE) return true;
				return false;
			}

			@Override
			public AsmLine next() {
				if (hasNext()) return current;
				else throw new NoSuchElementException();
			}

			@Override
			public void remove() {

			}
		};
	}
	
	/**
	 * Represents a line of assembly code (or comment)
	 * 
	 * @author Shai (Deshe) Wyborski
	 */
	public static class AsmLine{
		
		//fields
		private final short value;
		private final String comment;
		private final String label;
		private int line;
		
		//instantiate a new AsmLine
		private AsmLine(short value, String comment, String label, int line){
			this.value = value;
			this.comment = comment;
			this.label = label;
			this.line = line;
		}
		
		/**
		 * Get the value of the AsmLine
		 * 
		 * @return a short value representing the hack command
		 */
		public short value(){
			return value;
		}
		
		/**
		 * Get the comment
		 * 
		 * @return the comment
		 */
		public String comment(){
			return comment;
		}
		
		/**
		 * Get the label
		 * 
		 * @return the Label
		 */
		public String label(){
			return label;
		}
		
		/**
		 * Get the line number
		 * 
		 * @return the line number
		 */
		public int line(){
			return line;
		}
		
	}
}

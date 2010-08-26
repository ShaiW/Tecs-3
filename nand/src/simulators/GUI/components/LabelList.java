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

package simulators.GUI.components;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import common.collections.BidiMap;

/**
 * A component to display a list of labels in an assembly program
 * 
 * @author Shai (Deshe) Wyborski
 */
@SuppressWarnings("serial")
public class LabelList extends JFrame{
	
	//Column headlines
	String columnName[] = {"Label", "Line"};
	
	//Maps labels to lines
	private BidiMap<String, Short> labels;
	
	//A sorted list of lines
	private TreeSet<Short> sortedLabels;
	
	//The labels table
	private JTable labelTable;
	
	/**
	 * Instantiates a new Label List.
	 */
	public LabelList(){
		super("Labels");
		labels = new BidiMap<String, Short>();
		sortedLabels = new TreeSet<Short>(labels.values());
		jbInit();
	}

	/**
	 * Bulk add a Map of labels.
	 * If some labels point to the same line only one will be displayed
	 * 
	 * @param labels label Map
	 */
	public void addLabels(Map<String, Short> labels){
		this.labels.putAll(labels);
		sortedLabels.addAll(labels.values());
	}
	
	@Override
	public void setVisible(boolean flag){
		if (flag){
			buildTable();
			super.setVisible(true);
		}
		else super.setVisible(false);
		
	}
	
	/**
	 * Add a new label.
	 * If there already is a label pointing to this line it will be replaced.
	 * 
	 * @param label the label
	 * @param line the line
	 */
	public void addLabel(String label, int line){
		if (label == null) return;
		labels.put(label, (short) line);
		sortedLabels.add((short) line);
	}
	
	/**
	 * Clear the label table
	 */
	public void clearTable(){
		labels = new BidiMap<String, Short>();
		sortedLabels = new TreeSet<Short>();
	}
	
	//rebuilds the table - doesn't work very well
	private void buildTable(){
		String rowData[][] = new String[labels.size()][2];
		int i=0;
		
        for (short S: sortedLabels){
        	rowData[i][0] = labels.getKey(S);
        	rowData[i][1] = Integer.toString(S);
        	i++;
        }
        
        labelTable = new JTable(rowData, columnName);
        labelTable.revalidate();
        add(new JScrollPane(labelTable),BorderLayout.CENTER);
        repaint();
	}
	
	//init all the visual components
	private void jbInit(){
        setSize(320,100);
        setLocation(500,250);
	}
}
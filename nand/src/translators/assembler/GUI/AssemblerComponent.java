package translators.assembler.GUI;


import javax.swing.*;

import simulators.ComputerParts.*;
import simulators.GUI.*;
import translators.GUI.TranslatorComponent;
import translators.assembler.AssemblerEvent;


import common.fileFilters.ASMFileFilter;
import common.fileFilters.HackFileFilter;


import java.awt.event.*;
import java.awt.*;
import java.io.*;

/**
 * The GUI component of the Assembler
 */
public class AssemblerComponent extends TranslatorComponent implements HackAssemblerGUI {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// The icon of the equal button
    private ImageIcon equalIcon;

    // The compare button
    private MouseOverJButton compareButton;
    
    //the labels button
    private MouseOverJButton labelsButton;

    // The menu item of the load comparison file
    private JMenuItem loadCompareMenuItem;

    // Constructing a label for the equal sign.
    private JLabel equalLabel;

    // A component for displaying the comparison file
    private TextFileComponent comparison;

    // The file chooser for the comparison file
    protected JFileChooser compareFileChooser;

    /**
     * Constructs a new AssemblerComponent.
     */
    public AssemblerComponent() {
        super(new ASMFileFilter(), new HackFileFilter());
        comparison.disableUserInput();
        comparison.setName("Comparison");

        compareFileChooser = new JFileChooser();
        compareFileChooser.setFileFilter(destFilter);
    }

    public void setWorkingDir(File file) {
        super.setWorkingDir(file);
        compareFileChooser.setCurrentDirectory(file);
    }

    public void disableLoadComparison() {
        compareButton.setEnabled(false);
        loadCompareMenuItem.setEnabled(false);
    }

    public void enableLoadComparison() {
        compareButton.setEnabled(true);
        loadCompareMenuItem.setEnabled(true);
    }

    public void setComparisonName (String name) {
        compareFileChooser.setName(name);
        compareFileChooser.setSelectedFile(new File(name));
    }

    public void showComparison() {
        comparison.setVisible(true);
        equalLabel.setVisible(true);
    }

    public void hideComparison() {
        comparison.setVisible(false);
        equalLabel.setVisible(false);
    }

    public TextFileGUI getComparison() {
        return comparison;
    }

    protected void arrangeMenu() {
        super.arrangeMenu();
        fileMenu.removeAll();
        fileMenu.add(loadSourceMenuItem);
        loadCompareMenuItem = new JMenuItem("Load Comparison File", KeyEvent.VK_C);
        loadCompareMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadCompareMenuItem_actionPerformed(e);
            }
        });
        fileMenu.add(saveDestMenuItem);
        fileMenu.add(loadCompareMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
    }

    protected void init() {
        super.init();
        equalIcon = new ImageIcon(Utilities.imagesDir + "equal.gif");
        equalLabel = new JLabel();
        comparison = new TextFileComponent();
        compareButton = new MouseOverJButton();
        labelsButton = new MouseOverJButton();
    }

    // loads a comparison file
    private void loadComparison() {
        int returnVal = compareFileChooser.showDialog(this, "Load Comparison File");
        if(returnVal == JFileChooser.APPROVE_OPTION)
            notifyHackTranslatorListeners(AssemblerEvent.COMPARISON_LOAD,
                                          compareFileChooser.getSelectedFile().getAbsolutePath());
    }

    protected void arrangeToolBar() {
        super.arrangeToolBar();
        toolBar.addSeparator(separatorDimension);
        toolBar.add(compareButton);
        toolBar.add(labelsButton);
    }

    protected void jbInit() {
        super.jbInit();
        equalLabel.setBounds(new Rectangle(632, 324, 88, 71));
        equalLabel.setIcon(equalIcon);
        equalLabel.setVisible(false);

        comparison.setVisibleRows(31);
        comparison.setVisible(false);
        comparison.setBounds(new Rectangle(725,100,comparison.getWidth(),comparison.getHeight()));

        compareButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadCompareButton_actionPerformed(e);
            }
        });
        compareButton.setMaximumSize(new Dimension(39, 39));
        compareButton.setMinimumSize(new Dimension(39, 39));
        compareButton.setPreferredSize(new Dimension(39, 39));
        compareButton.setSize(new Dimension(39, 39));
        compareButton.setToolTipText("Load Comparison File");
        compareButton.setIcon(new ImageIcon(Utilities.imagesDir + "smallequal.gif"));
        
        labelsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                labelsButton_actionPerformed(e);
            }
        });
        labelsButton.setMaximumSize(new Dimension(39, 39));
        labelsButton.setMinimumSize(new Dimension(39, 39));
        labelsButton.setPreferredSize(new Dimension(39, 39));
        labelsButton.setSize(new Dimension(39, 39));
        labelsButton.setToolTipText("Show Labels");
        labelsButton.setIcon(new ImageIcon(Utilities.imagesDir + "barcode.png"));

        getContentPane().add(equalLabel, null);
        getContentPane().add(comparison, null);
   }

    public void labelsButton_actionPerformed(ActionEvent e) {
		destination.showLabels();
	}

	// Implementing the action of selection the load compare menu item from the file menu.
    public void loadCompareMenuItem_actionPerformed(ActionEvent e) {
        loadComparison();
    }

    // Implementing the action of pressing the load compare button.
    public void loadCompareButton_actionPerformed(ActionEvent e) {
        loadComparison();
    }
    
    @Override
    public void rewindMenuItem_actionPerformed(ActionEvent e) {
    	super.rewindMenuItem_actionPerformed(e);
    	destination.clearAll();
    }
    
    @Override
    public void rewindButton_actionPerformed(ActionEvent e) {
    	super.rewindButton_actionPerformed(e);
    	destination.clearAll();
    }
}

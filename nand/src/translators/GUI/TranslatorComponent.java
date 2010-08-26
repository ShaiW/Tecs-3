package translators.GUI;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;


import simulators.ComputerParts.*;
import simulators.GUI.*;
import translators.*;

import java.io.File;

/**
 * The GUI component of a translator
 */
public class TranslatorComponent extends JFrame implements HackTranslatorGUI {

    // The dimensions of the tool bar.
    protected static final int TOOLBAR_WIDTH = 1016;
    protected static final int TOOLBAR_HEIGHT = 55;

    // The dimensions of this window.
    private static final int TRANSLATOR_WIDTH = 1024;
    private static final int TRANSLATOR_HEIGHT = 741;

    // the dimensions of the tool bar separator
    protected static final Dimension separatorDimension = new Dimension(3, TOOLBAR_HEIGHT - 5);

    // The listeners to this component.
    private Vector listeners;

    // The buttons of this component.
    private MouseOverJButton loadButton;
    private MouseOverJButton saveButton;
    private MouseOverJButton ffwdButton;
    private MouseOverJButton stopButton;
    private MouseOverJButton singleStepButton;
    private MouseOverJButton rewindButton;
    private MouseOverJButton fullTranslationButton;

    // The icons of the buttons.
    private ImageIcon ffwdIcon = new ImageIcon(Utilities.imagesDir + "vcrfastforward.gif");
    private ImageIcon stopIcon = new ImageIcon(Utilities.imagesDir + "vcrstop.gif");
    private ImageIcon singleStepIcon = new ImageIcon(Utilities.imagesDir + "vcrforward.gif");
    private ImageIcon rewindIcon = new ImageIcon(Utilities.imagesDir + "vcrrewind.gif");
    private ImageIcon fullTranslationIcon = new ImageIcon(Utilities.imagesDir + "hex.gif");
    private ImageIcon loadIcon = new ImageIcon(Utilities.imagesDir + "opendoc.gif");
    private ImageIcon saveIcon = new ImageIcon(Utilities.imagesDir + "save.gif");
    private ImageIcon arrowIcon = new ImageIcon(Utilities.imagesDir + "arrow2.gif");

    // The tool bar which contains the buttons
    protected JToolBar toolBar;

    // The menu bar of this component
    protected JMenuBar menuBar;
    protected JMenu fileMenu, runMenu, helpMenu;
    protected JMenuItem loadSourceMenuItem, saveDestMenuItem, exitMenuItem;
    protected JMenuItem singleStepMenuItem, ffwdMenuItem, stopMenuItem, rewindMenuItem, fullTranslationMenuItem;
    protected JMenuItem aboutMenuItem, usageMenuItem;

    // File choosers for the source and destination files
    protected JFileChooser sourceFileChooser;
    protected JFileChooser destFileChooser;



    // The labels
    private JLabel arrowLabel;
    private JLabel messageLbl;

    // The text components of the source and destination files
    private TextFileComponent source;
    protected TextFileComponent destination;

    // The file filters for the source and destination files
    protected FileFilter sourceFilter;
    protected FileFilter destFilter;

    // The HTML view frames
    private HTMLViewFrame usageWindow, aboutWindow;

    /**
     * Constructs a new TranslatorComponent with the given filters of the source
     * and destination files.
     */
    public TranslatorComponent(FileFilter sourceFilter, FileFilter destFilter) {
        this.sourceFilter = sourceFilter;
        this.destFilter = destFilter;
        init();
        jbInit();
        source.setName("Source");
        destination.setName("Destination");

        sourceFileChooser = new JFileChooser();
        sourceFileChooser.setFileFilter(sourceFilter);

        destFileChooser = new JFileChooser();
        destFileChooser.setFileFilter(destFilter);

        source.enableUserInput();
        destination.disableUserInput();
    }


    public void notifyHackTranslatorListeners(byte action, Object data) {
        HackTranslatorEvent event = new HackTranslatorEvent(this,action,data);
        for(int i=0;i<listeners.size();i++)
            ((HackTranslatorEventListener)listeners.elementAt(i)).actionPerformed(event);
    }

    public void removeHackTranslatorListener (HackTranslatorEventListener listener) {
        listeners.removeElement(listener);
    }

    public void addHackTranslatorListener (HackTranslatorEventListener listener) {
        listeners.addElement(listener);
    }

    public void setWorkingDir(File file) {
        sourceFileChooser.setCurrentDirectory(file);
        destFileChooser.setCurrentDirectory(file);
    }

    public void disableStop() {
        stopButton.setEnabled(false);
        stopMenuItem.setEnabled(false);
    }

    public void enableStop() {
        stopButton.setEnabled(true);
        stopMenuItem.setEnabled(true);
    }

    public void disableFastForward() {
        ffwdButton.setEnabled(false);
        ffwdMenuItem.setEnabled(false);
    }

    public void enableFastForward() {
        ffwdButton.setEnabled(true);
        ffwdMenuItem.setEnabled(true);
    }

    public void disableSingleStep() {
        singleStepButton.setEnabled(false);
        singleStepMenuItem.setEnabled(false);
    }

    public void enableSingleStep() {
        singleStepButton.setEnabled(true);
        singleStepMenuItem.setEnabled(true);
    }

    public void disableRewind() {
        rewindButton.setEnabled(false);
        rewindMenuItem.setEnabled(false);
    }

    public void enableRewind() {
        rewindButton.setEnabled(true);
        rewindMenuItem.setEnabled(true);
    }

    public void disableSave() {
        saveButton.setEnabled(false);
        saveDestMenuItem.setEnabled(false);
    }

    public void enableSave() {
        saveButton.setEnabled(true);
        saveDestMenuItem.setEnabled(true);
    }

    public void disableFullCompilation() {
        fullTranslationButton.setEnabled(false);
        fullTranslationMenuItem.setEnabled(false);
    }

    public void enableFullCompilation() {
        fullTranslationButton.setEnabled(true);
        fullTranslationMenuItem.setEnabled(true);
    }

    public void disableLoadSource() {
        loadButton.setEnabled(false);
        loadSourceMenuItem.setEnabled(false);
    }

    public void enableLoadSource() {
        loadButton.setEnabled(true);
        loadSourceMenuItem.setEnabled(true);
    }

    public void enableSourceRowSelection() {
        source.enableUserInput();
    }

    public void disableSourceRowSelection() {
        source.disableUserInput();
    }

    public void setSourceName(String name) {
        sourceFileChooser.setName(name);
        sourceFileChooser.setSelectedFile(new File(name));
    }

    public void setDestinationName(String name) {
        destFileChooser.setName(name);
        destFileChooser.setSelectedFile(new File(name));
    }

    public TextFileGUI getSource() {
        return source;
    }

    public TextFileGUI getDestination() {
        return destination;
    }

    public void setUsageFileName(String fileName) {
        usageWindow = new HTMLViewFrame(fileName);
        usageWindow.setSize(450, 430);
    }

    public void setAboutFileName(String fileName) {
        aboutWindow = new HTMLViewFrame(fileName);
        aboutWindow.setSize(450, 420);
    }

    public void displayMessage (String message, boolean error) {
        if(error)
            messageLbl.setForeground(Color.red);
        else
            messageLbl.setForeground(UIManager.getColor("Label.foreground"));
        messageLbl.setText(message);
    }

    // loads a source file
    private void loadSource() {
        int returnVal = sourceFileChooser.showDialog(this, "Load Source File");
        if(returnVal == JFileChooser.APPROVE_OPTION)
            notifyHackTranslatorListeners(HackTranslatorEvent.SOURCE_LOAD,
                                          sourceFileChooser.getSelectedFile().getAbsolutePath());
    }

    // saves the destination file
    private void saveDest() {
        int returnVal = destFileChooser.showDialog(this, "Save Destination File");
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            if(destFileChooser.getSelectedFile().exists()) {
                Object[] options = {"Yes", "No","Cancel"};
                int pressedButtonValue = JOptionPane.showOptionDialog((JFrame)this,
                "File exists. Replace it ?","Question",JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,null,options,options[2]);

                if(pressedButtonValue != JOptionPane.YES_OPTION)
                    return;
            }

            String fileName = destFileChooser.getSelectedFile().getAbsolutePath();
            notifyHackTranslatorListeners(HackTranslatorEvent.SAVE_DEST, fileName);
        }
    }

    // arranges the tool bar
    protected void arrangeToolBar() {
        toolBar.setSize(new Dimension(TOOLBAR_WIDTH,TOOLBAR_HEIGHT));
        toolBar.add(loadButton);
        toolBar.add(saveButton);
        toolBar.addSeparator(separatorDimension);
        toolBar.add(singleStepButton);
        toolBar.add(ffwdButton);
        toolBar.add(stopButton);
        toolBar.add(rewindButton);
        toolBar.addSeparator(separatorDimension);
        toolBar.add(fullTranslationButton);
    }

    // arranges the menu bar
    protected void arrangeMenu() {
        // Build the first menu.
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        runMenu = new JMenu("Run");
        runMenu.setMnemonic(KeyEvent.VK_R);
        menuBar.add(runMenu);

        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        loadSourceMenuItem = new JMenuItem("Load Source file", KeyEvent.VK_O);
        loadSourceMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadSourceMenuItem_actionPerformed(e);
            }
        });
        fileMenu.add(loadSourceMenuItem);

        saveDestMenuItem = new JMenuItem("Save Destination file", KeyEvent.VK_S);
        saveDestMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveDestMenuItem_actionPerformed(e);
            }
        });
        fileMenu.add(saveDestMenuItem);

        fileMenu.addSeparator();

        exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitMenuItem_actionPerformed(e);
            }
        });
        fileMenu.add(exitMenuItem);

        singleStepMenuItem = new JMenuItem("Single Step", KeyEvent.VK_S);
        singleStepMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                singleStepMenuItem_actionPerformed(e);
            }
        });
        runMenu.add(singleStepMenuItem);

        ffwdMenuItem = new JMenuItem("Fast Forward", KeyEvent.VK_F);
        ffwdMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ffwdMenuItem_actionPerformed(e);
            }
        });
        runMenu.add(ffwdMenuItem);

        stopMenuItem = new JMenuItem("Stop", KeyEvent.VK_T);
        stopMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopMenuItem_actionPerformed(e);
            }
        });
        runMenu.add(stopMenuItem);

        rewindMenuItem = new JMenuItem("Rewind", KeyEvent.VK_R);
        rewindMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rewindMenuItem_actionPerformed(e);
            }
        });
        runMenu.add(rewindMenuItem);
        runMenu.addSeparator();

        fullTranslationMenuItem = new JMenuItem("Fast Translation", KeyEvent.VK_U);
        fullTranslationMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fullTranslationMenuItem_actionPerformed(e);
            }
        });
        runMenu.add(fullTranslationMenuItem);

        usageMenuItem = new JMenuItem("Usage", KeyEvent.VK_U);
        usageMenuItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
        usageMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                usageMenuItem_actionPerformed(e);
            }
        });
        helpMenu.add(usageMenuItem);

        aboutMenuItem = new JMenuItem("About...", KeyEvent.VK_A);
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                aboutMenuItem_actionPerformed(e);
            }
        });
        helpMenu.add(aboutMenuItem);
    }

    // initializes the translator
    protected void init() {
        toolBar = new JToolBar();
        menuBar = new JMenuBar();
        arrowLabel = new JLabel();
        messageLbl = new JLabel();
        listeners = new Vector();
        ffwdButton = new MouseOverJButton();
        rewindButton = new MouseOverJButton();
        stopButton = new MouseOverJButton();
        singleStepButton = new MouseOverJButton();
        fullTranslationButton = new MouseOverJButton();
        saveButton = new MouseOverJButton();
        loadButton = new MouseOverJButton();
        source = new TextFileComponent();
        destination = new TextFileComponent();
    }

    // initializes the internal component of the translator
    protected void jbInit() {
        getContentPane().setLayout(null);

        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadButton_actionPerformed(e);
            }
        });
        loadButton.setMaximumSize(new Dimension(39, 39));
        loadButton.setMinimumSize(new Dimension(39, 39));
        loadButton.setPreferredSize(new Dimension(39, 39));
        loadButton.setSize(new Dimension(39, 39));
        loadButton.setToolTipText("Load Source File");
        loadButton.setIcon(loadIcon);

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveButton_actionPerformed(e);
            }
        });
        saveButton.setMaximumSize(new Dimension(39, 39));
        saveButton.setMinimumSize(new Dimension(39, 39));
        saveButton.setPreferredSize(new Dimension(39, 39));
        saveButton.setSize(new Dimension(39, 39));
        saveButton.setToolTipText("Save Destination File");
        saveButton.setIcon(saveIcon);

        singleStepButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                singleStepButton_actionPerformed(e);
            }
        });
        singleStepButton.setMaximumSize(new Dimension(39, 39));
        singleStepButton.setMinimumSize(new Dimension(39, 39));
        singleStepButton.setPreferredSize(new Dimension(39, 39));
        singleStepButton.setSize(new Dimension(39, 39));
        singleStepButton.setToolTipText("Single Step");
        singleStepButton.setIcon(singleStepIcon);

        ffwdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ffwdButton_actionPerformed(e);
            }
        });
        ffwdButton.setMaximumSize(new Dimension(39, 39));
        ffwdButton.setMinimumSize(new Dimension(39, 39));
        ffwdButton.setPreferredSize(new Dimension(39, 39));
        ffwdButton.setSize(new Dimension(39, 39));
        ffwdButton.setToolTipText("Fast Forward");
        ffwdButton.setIcon(ffwdIcon);

        rewindButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rewindButton_actionPerformed(e);
            }
        });
        rewindButton.setMaximumSize(new Dimension(39, 39));
        rewindButton.setMinimumSize(new Dimension(39, 39));
        rewindButton.setPreferredSize(new Dimension(39, 39));
        rewindButton.setSize(new Dimension(39, 39));
        rewindButton.setToolTipText("Rewind");
        rewindButton.setIcon(rewindIcon);

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopButton_actionPerformed(e);
            }
        });
        stopButton.setMaximumSize(new Dimension(39, 39));
        stopButton.setMinimumSize(new Dimension(39, 39));
        stopButton.setPreferredSize(new Dimension(39, 39));
        stopButton.setSize(new Dimension(39, 39));
        stopButton.setToolTipText("Stop");
        stopButton.setIcon(stopIcon);

        fullTranslationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fullTranslationButton_actionPerformed(e);
            }
        });
        fullTranslationButton.setMaximumSize(new Dimension(39, 39));
        fullTranslationButton.setMinimumSize(new Dimension(39, 39));
        fullTranslationButton.setPreferredSize(new Dimension(39, 39));
        fullTranslationButton.setSize(new Dimension(39, 39));
        fullTranslationButton.setToolTipText("Fast Translation");
        fullTranslationButton.setIcon(fullTranslationIcon);

        messageLbl.setFont(Utilities.statusLineFont);
        messageLbl.setBorder(BorderFactory.createLoweredBevelBorder());
        messageLbl.setBounds(new Rectangle(0, 672, TRANSLATOR_WIDTH - 8, 20));
        getContentPane().add(messageLbl, null);

        arrowLabel.setBounds(new Rectangle(290, 324, 88, 71));
        arrowLabel.setIcon(arrowIcon);

        source.setVisibleRows(31);
        destination.setVisibleRows(31);
        source.setBounds(new Rectangle(35,100,source.getWidth(),source.getHeight()));
        destination.setBounds(new Rectangle(375,100,destination.getWidth(),destination.getHeight()));

        getContentPane().add(source, null);
        getContentPane().add(destination, null);

        // Adding the tool bar to this container.
        toolBar.setFloatable(false);
        toolBar.setLocation(0,0);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        arrangeToolBar();
        getContentPane().add(toolBar, null);
        toolBar.revalidate();
        toolBar.repaint();
        repaint();

        // Creating the menu bar
        arrangeMenu();
        setJMenuBar(menuBar);

        // initializing the window size and visibility
        setDefaultCloseOperation(3);
        setSize(new Dimension(TRANSLATOR_WIDTH,TRANSLATOR_HEIGHT));
        setVisible(true);
        getContentPane().add(arrowLabel, null);
    }

    /**
     * Implementing the action of pressing the single step button.
     */
    public void singleStepButton_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.SINGLE_STEP, null);
    }

    /**
     * Implementing the action of pressing the fast forward button.
     */
    public void ffwdButton_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.FAST_FORWARD, null);
    }

    /**
     * Implementing the action of pressing the stop button.
     */
    public void stopButton_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.STOP, null);
    }

    /**
     * Implementing the action of pressing the rewind button.
     */
    public void rewindButton_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.REWIND, null);
    }

    /**
     * Implementing the action of pressing the full compilation button.
     */
    public void fullTranslationButton_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.FULL_COMPILATION, null);
    }

    /**
     * Implementing the action of pressing the load button.
     */
    public void loadButton_actionPerformed(ActionEvent e) {
        loadSource();
    }

    /**
     * Implementing the action of pressing the save button.
     */
    public void saveButton_actionPerformed(ActionEvent e) {
        saveDest();
    }

    /**
     * Implementing the action of selection the load source menu item from the file menu.
     */
    public void loadSourceMenuItem_actionPerformed(ActionEvent e) {
        loadSource();
    }

    /**
     * Implementing the action of selection the save destination menu item from the file menu.
     */
    public void saveDestMenuItem_actionPerformed(ActionEvent e) {
        saveDest();
    }

    /**
     * Implementing the action of selection the exit menu item from the file menu.
     */
    public void exitMenuItem_actionPerformed(ActionEvent e) {
        System.exit(0);
    }

    /**
     * Implementing the action of pressing the single step menu item from the run menu.
     */
    public void singleStepMenuItem_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.SINGLE_STEP, null);
    }

    /**
     * Implementing the action of pressing the ffwd menu item from the run menu.
     */
    public void ffwdMenuItem_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.FAST_FORWARD, null);
    }

    /**
     * Implementing the action of pressing the stop menu item from the run menu.
     */
    public void stopMenuItem_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.STOP, null);
    }

    /**
     * Implementing the action of pressing the rewind menu item from the run menu.
     */
    public void rewindMenuItem_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.REWIND, null);
    }

    /**
     * Implementing the action of pressing the full compilation menu item from the run menu.
     */
    public void fullTranslationMenuItem_actionPerformed(ActionEvent e) {
        notifyHackTranslatorListeners(HackTranslatorEvent.FULL_COMPILATION, null);
    }

    /**
     * Implementing the action of opening the usage window.
     */
    public void usageMenuItem_actionPerformed(ActionEvent e) {
        if (usageWindow != null)
            usageWindow.setVisible(true);
    }

    /**
     * Implementing the action of opening the about window.
     */
    public void aboutMenuItem_actionPerformed(ActionEvent e) {
        if (aboutWindow != null)
            aboutWindow.setVisible(true);
    }
}

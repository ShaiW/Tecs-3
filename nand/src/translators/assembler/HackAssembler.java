package translators.assembler;

import javax.swing.*;

import translators.*;
import translators.assembler.GUI.AssemblerComponent;
import translators.assembler.GUI.HackAssemblerGUI;

import common.Definitions;


import static common.Environment.*;

/**
 * The HackAssembler.
 */
public class HackAssembler {

  /**
   * The command line Assembler program.
   */
  public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java HackAssembler [.asm name]");
            System.exit(-1);
        }
        
        @SuppressWarnings("unused")
		AssemblyParser assembler;
        
        //If a file is passed as an argument assemble it and quit
        if (args.length == 1) {
            try {
                assembler = new AssemblyParser(args[0] , true);
            } catch (HackTranslatorException ae) {
                System.err.println(ae.getMessage());
                System.exit(1);
            }
        }
        
        //if no arguments were passed start the GUI
        else {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
            }

//            try {
                HackAssemblerGUI gui = new AssemblerComponent();
                gui.setAboutFileName(HELPFILES_PATH+"asmAbout.html");
                gui.setUsageFileName(HELPFILES_PATH+"asmUsage.html");
//                assembler = new AssemblyParser(gui, Definitions.ROM_SIZE, (short)0, null);
//            } catch (HackTranslatorException hte) {
//                System.err.println(hte.getMessage());
//                System.exit(-1);
//            }
        }
    }
}

package translators.jackCompiler;

import java.io.*;


import common.fileFilters.FileFilters;
/**
 * A JackCompiler.
 * Compiles all the .jack files that are under the jack directory to
 * .vm files under a vm directory.
 */
public class JackCompiler {

    // The Compilation Engine
    private CompilationEngine compilationEngine;

    /**
     * Constructs a new JackCompiler
     */
    public JackCompiler() {
        compilationEngine = new CompilationEngine();
    }

    /**
     * Compiles the given file. The resulting file will have the same name,
     * but with the .vm extension. Upon unsuccessful compilation (had errors)
	 * a .vm file is not created and false is returned. 
     */
    public boolean compileFile(File file) {
        String className = file.getName().substring(0, file.getName().indexOf('.'));
        String path = file.getParent();

        try {
            JackTokenizer input = new JackTokenizer(new FileReader(file.getPath()));
			File outfile = new File(path + File.separator + className + ".vm");
            VMWriter output = new VMWriter(new PrintWriter(new FileWriter(outfile)));
            		System.out.println(outfile.getAbsolutePath());
            if (compilationEngine.compileClass(input, output, className, file.getName())) {
				return true;
			} else {
				outfile.delete();
				return false;
			}
        } catch (IOException ioe) {
            System.err.println("Error reading/writing while compiling " + file);
            System.exit(-1);
			return false; // unreachable
        }

    }

    /**
     * Compiles all the .jack files in this directory into .vm files. Returns
	 * true if all files were compiled succesfully (no errors).
     * @param jackDirectory Full name of the source directory with .jack files
     */
    public boolean compileDirectory(String jackDirectory) {
		boolean success = true;
        File directory = new File(jackDirectory);
        File[] files = directory.listFiles(FileFilters.getFilenameFilter("jack"));
        for (int i=0; i<files.length; i++) {
            success &= compileFile(files[i]);
		}
		return success;
    }

    /**
     * Performs some error cross-checking between the files compiled until now.
	 * returns true if no errors were found.
     */
	public boolean verify() {
		return compilationEngine.verifySubroutineCalls();
	}

    /*
     Compiles each .jack file in the jack directory into .vm file
     in the vm directory
    */
    public static void main(String[] args) {
        if (args.length != 1) {
			try {
				BufferedReader message = new BufferedReader(new FileReader(new File("bin/help/compiler.txt")));
				String line;
				while ((line=message.readLine()) != null) {
					System.out.println(line);
				}
				System.out.println("");
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}

            System.out.println("Usage: java JackCompiler <Jack-dir or Jack-file-name>");
            System.exit(-1);
        }

        JackCompiler jackCompiler = new JackCompiler();
        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("Could not find file or directory: " + args[0]);
            System.exit(-1);
        }

		boolean success;
        if (file.isDirectory())
            success = jackCompiler.compileDirectory(args[0]);
        else
            success = jackCompiler.compileFile(file);

		success &= jackCompiler.verify();
		
		System.exit(success? 0 : 1);
    }

}

package common;

import java.io.File;

public class Environment {
	
	private static String GENERAL_PATH = "."+File.separator;
	
	public static String SCRIPTS_PATH = GENERAL_PATH + "scripts"+File.separator;
	public static String HELPFILES_PATH = GENERAL_PATH + "help"+File.separator;
	public static String IMAGES_PATH = GENERAL_PATH + "images"+File.separator;
	public static String DAT_PATH = GENERAL_PATH + "dat"+File.separator;

}

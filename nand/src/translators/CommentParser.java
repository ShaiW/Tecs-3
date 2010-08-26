package translators;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;

public class CommentParser {
	
	//A mapping of a line number to it's comment
	Hashtable<Integer, String> _comments;
	Scanner fileScanner;
	
	public CommentParser(File inputFile){
		try {
			fileScanner = new Scanner(inputFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void weedComments(){
		String current;
		boolean inComment = false;
		int lineCounter = 0;
		String currentComment = "";
		
		while (fileScanner.hasNextLine()){			
			current = fileScanner.nextLine();
			int startIndex = 0;
			if (inComment){
				if (current.indexOf("*/") == -1) continue;
				else{
//					current = current.substring(current.indexOf("*/")+2);
					currentComment += current.substring(0,current.indexOf("*/")-2) + "\n";
					inComment = false;
				}
			}
			if (current.startsWith("//")) {
				currentComment += current + "\n";
				continue;
			}
			while (current.indexOf("//",startIndex+1)>0){
				if (current.indexOf("//",startIndex) > current.indexOf("\"",startIndex) && current.indexOf("//",startIndex) < current.indexOf("\"",current.indexOf("\"",startIndex)+1)){
					startIndex = current.indexOf("\"",current.indexOf("\"")+1);
					currentComment += current + "\n";
					continue;
				}
				else if (current.indexOf("//",startIndex) > current.indexOf("/*",startIndex) && current.indexOf("//",startIndex) < current.indexOf("*/",current.indexOf("/*",startIndex)+1)){
					startIndex = current.indexOf("*/",current.indexOf("/*")+1)+1;
					continue;
				}
				current = current.substring(0, current.indexOf("//",startIndex));
			}
			int comStart,comEnd;
			startIndex = 0;
			while ((comStart = current.indexOf("/*",startIndex))!=-1){
				if (current.indexOf("/*",startIndex) > current.indexOf("\"",startIndex) && current.indexOf("/*",startIndex) < current.indexOf("\"",current.indexOf("\"",startIndex)+1)){
					startIndex = current.indexOf("\"",current.indexOf("\"")+1)+1;
					continue;
				}	
				if ((comEnd = current.indexOf("*/",startIndex))!=-1) current = current.substring(0,comStart) + current.substring(comEnd+2);
				else {
					inComment = true;
					current = current.substring(0,comStart);
					break;
				}
			}
			current = current.trim();
			if (current.length()==0) continue;
		}
	}
	
}

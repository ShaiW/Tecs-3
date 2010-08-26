package common.fileFilters;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * A file filter of '.hack' files, for the use of the ROMComponent.
 */
public class HackFileFilter extends FileFilter {

    /**
     * Determines which files this filter should accept.
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("hack") )
                    return true;
            else {
                return false;
            }
        }

        return false;
    }


    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    // The description of this filter
    public String getDescription() {
        return "HACK Files";
    }
}

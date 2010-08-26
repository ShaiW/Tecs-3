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

package common.fileFilters;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

/**
 * A File filter that only accepts files with the extension that is given
 * in the constructor
 */
public class FileFilters implements FilenameFilter {
	
	private enum fileTypes {
		vm {
			@Override
			public FileFilter getFilter(String type) {
				return new VMFileFilter();
			}
		},
		hack {
			@Override
			public FileFilter getFilter(String type) {
				return new HackFileFilter();
			}
			
		};
		
		/**
		 * Returns a proper object according to the type
		 * 
		 * @param type
		 * @return
		 */
		abstract FileFilter getFilter(String type);
	}
	
	public static FileFilters getFilenameFilter(String fileType){	
		return new FileFilters("."+fileType);
	}
	
	public static FileFilter getFileFilter(String fileType){
		return fileTypes.valueOf(fileType).getFilter(fileType);
	}

    // the accepted extension
    private String _extension;

    /**
     * Constucts a new HackFileFilter with the given extension
     * @param extension The given extension
     */
    private FileFilters(String extension) {
        _extension = extension;
    }

    public boolean accept(File directory, String name) {
        return name.endsWith(_extension);
    }

    /**
     * Returns the accepted extension
     */
    public String getAcceptedExtension() {
        return _extension;
    }
}

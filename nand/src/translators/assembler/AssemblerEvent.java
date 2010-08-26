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

import translators.*;

/**
 * An event for notifying a HackAssemblerEventListener on an action that should be taken,
 * together with a data object which is supplied with the action code.
 */
@SuppressWarnings("serial")
public class AssemblerEvent extends HackTranslatorEvent {
	
	/**
     * Action code for changing the comparison file.
     * supplied data = comparison file name (String)
     */
    public static final byte COMPARISON_LOAD = 9;

    /**
     * Constructs a new HackAssemblerEvent with given source, the action code and the supplied data.
     */
    public AssemblerEvent(Object source, byte action, Object data) {
        super(source, action, data);
    }
}

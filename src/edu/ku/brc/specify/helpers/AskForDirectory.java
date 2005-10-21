/* Filename:    $RCSfile: AskForDirectory.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:28 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.helpers;

import java.io.File;
import java.util.NoSuchElementException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class AskForDirectory
{

    protected JComponent parent;
    
    /**
     * 
     * @param aParent
     */
    public AskForDirectory(JComponent aParent)
    {
        parent = aParent;
    }
    
    public String getDirectory() throws NoSuchElementException
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showDialog(parent, "Select Directory") == JFileChooser.CANCEL_OPTION) // XXX LOCALIZE
        {
            throw new NoSuchElementException("The External File Repository needs a valid directory.");// XXX LOCALIZE
        } else 
        {
            return chooser.getSelectedFile().getAbsolutePath();
        } 
    }
    
    public void showErrorDialog(String aMsg)
    {
        JOptionPane.showMessageDialog(parent, aMsg, "Error", JOptionPane.ERROR_MESSAGE);   // XXX LOCALIZE
    }    
}

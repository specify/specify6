/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.helpers;

import java.util.NoSuchElementException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Implementation of the ExternalFileRepositoryIFace for a local directory/disk based external repository
 
 * @code_status Complete
 **
 * @author rods
 */
public class AskForDirectory
{

    protected JComponent parent;
    
    /**
     * Constructor with UI parent.
     * @param aParent the UI parent
     */
    public AskForDirectory(JComponent aParent)
    {
        parent = aParent;
    }
    
    /**
     * 
     * @return gets a directory using the JFileChooser.
     * @throws NoSuchElementException if no dir is choosen (dialog is cancelled)
     */
    public String getDirectory() throws NoSuchElementException
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showDialog(parent, "Select Directory") == JFileChooser.CANCEL_OPTION) // XXX LOCALIZE
        {
            throw new NoSuchElementException("The External File Repository needs a valid directory.");// XXX LOCALIZE
        }
        // else
        return chooser.getSelectedFile().getAbsolutePath();
    }
    
    /**
     * Displays an error dialog with the provided message.
     * @param msg the message to show
     */
    public void showErrorDialog(String msg)
    {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);   // XXX LOCALIZE
    }    
}

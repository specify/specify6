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
package edu.ku.brc.ui.db;

import java.awt.Frame;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.forms.MultiView;

/**
 * This is just the JFrame portion of a Frame used to display the fields in a data object. Instances of this class are created
 * by the implementation of ViewBasedDialogFactoryIFace interface. This class is consideraed to be a reference implementation.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ViewBasedDisplayFrame extends JFrame implements ViewBasedDisplayIFace
{
    protected ViewBasedDisplayPanel mainPanel;

    /**
     * Constructs a search dialog from form infor and from search info
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param displayName the search name, this is looked up by name in the "search_config.xml" file
     * @param title the title (should be already localized before passing in)
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     */
    public ViewBasedDisplayFrame(final String viewSetName,
                                 final String viewName,
                                 final String displayName,
                                 final String title,
                                 final String className,
                                 final String idFieldName,
                                 final boolean isEdit)
    {
        this.setTitle(title);
        
        mainPanel = new ViewBasedDisplayPanel(viewSetName, viewName, displayName, className, idFieldName, isEdit);
        
        setContentPane(mainPanel);
        pack();
        
        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    //------------------------------------------------------------
    //-- ViewBasedDisplayIFace Interface
    //------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedDisplayIFace#showDisplay(boolean)
     */
    public void showDisplay(boolean show)
    {
        setVisible(show);
    }

    /**
     * Returns the MultiView
     * @return the multiview
     */
    public MultiView getMultiView()
    {
        return mainPanel.getMultiView();
    }

    /**
     * Set a listener to know when the dialog is closed
     * @param propertyChangeListener the listener
     */
    public void setCloseListener(final PropertyChangeListener propertyChangeListener)
    {
        mainPanel.setCloseListener(propertyChangeListener);
    }

    /**
     * Sets data into the dialog
     * @param dataObj the data object
     */
    public void setData(final Object dataObj)
    {
        mainPanel.setData(dataObj);
    }

}

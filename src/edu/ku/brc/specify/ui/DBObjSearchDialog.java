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
/**
 * 
 */
package edu.ku.brc.specify.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.db.ViewBasedSearchDialogIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 18, 2007
 *
 */
public class DBObjSearchDialog extends CustomDialog implements ViewBasedSearchDialogIFace
{
    protected Window           parent;
    protected DBObjSearchPanel panel;
    /**
     * Constructs a search dialog from form infor and from search info.
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param searchName the search name, this is looked up by name in the "search_config.xml" file
     * @param title the title (should be already localized before passing in)
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @throws HeadlessException an exception
     */
    public DBObjSearchDialog(final Frame  parent,
                             final String viewSetName, 
                             final String viewName, 
                             final String searchName,
                             final String title,
                             final String className,
                             final String idFieldName) throws HeadlessException
    {
        //this((Window)parent, viewSetName, viewName, searchName, className, idFieldName);
        super(parent, title, true, null);
        
        this.parent = parent;
        panel  = new DBObjSearchPanel(viewSetName, viewName, searchName, className, idFieldName, SwingConstants.CENTER);
    }
    
    /**
     * Constructs a search dialog from form infor and from search info.
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param searchName the search name, this is looked up by name in the "search_config.xml" file
     * @param title the title (should be already localized before passing in)
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @throws HeadlessException an exception
     */
    public DBObjSearchDialog(final Dialog parent,
                             final String viewSetName, 
                             final String viewName, 
                             final String searchName,
                             final String title,
                             final String className,
                             final String idFieldName) throws HeadlessException
    {
        super(parent, title, true, OK_BTN | CANCEL_BTN, null);
        
        this.parent = parent;
        panel = new DBObjSearchPanel(viewSetName, viewName, searchName, className, idFieldName, SwingConstants.CENTER);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createMainPanel()
     */
    @Override
    protected JPanel createMainPanel()
    {
        return panel;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        panel.setOKBtn(okBtn);
        panel.getScrollPane().setPreferredSize(new Dimension(300, 200));

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchDialogIFace#getDialog()
     */
    public JDialog getDialog()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchDialogIFace#getSelectedObject()
     */
    public Object getSelectedObject()
    {
        if (!isCancelled)
        {
            return ((DBObjSearchPanel)mainPanel).getSelectedObject();
        }
        return null;
    }
}

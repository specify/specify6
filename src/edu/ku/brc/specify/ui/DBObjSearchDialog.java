/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.ui.CustomDialog;

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
    private static final Logger  log             = Logger.getLogger(DBObjSearchDialog.class);
    
            
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
                             final String idFieldName,
                             final String helpContext) throws HeadlessException
    {
        super(parent, title, true, OK_BTN | CANCEL_BTN, null);
        
        if (StringUtils.isNotEmpty(helpContext))
        {
            this.whichBtns |= HELP_BTN;
            setHelpContext(helpContext);
        }
        
        this.parent = parent;
        this.panel  = new DBObjSearchPanel(viewSetName, viewName, searchName, className, idFieldName, SwingConstants.CENTER);
        if (panel == null)
        {
            log.error("ViewSet["+viewSetName+"] View["+viewName+"] searchName["+searchName+"] className["+className+"] could not be created.");
        }
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
                             final String idFieldName,
                             final String helpContext) throws HeadlessException
    {
        super(parent, title, true, OK_BTN | CANCEL_BTN | HELP_BTN, null);
        
        if (StringUtils.isNotEmpty(helpContext))
        {
            this.whichBtns |= HELP_BTN;
            setHelpContext(helpContext);
        }
        
        this.parent = parent;
        this.panel  = new DBObjSearchPanel(viewSetName, viewName, searchName, className, idFieldName, SwingConstants.CENTER);
        if (panel == null)
        {
            log.error("ViewSet["+viewSetName+"] View["+viewName+"] searchName["+searchName+"] className["+className+"] could not be created.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace#getSearchName()
     */
    public String getSearchName()
    {
        return panel.getSearchName();
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
        
        if (panel != null)
        {
            panel.setOKBtn(okBtn);
            //panel.getScrollPane().setPreferredSize(new Dimension(300, 200));
        }

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

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchDialogIFace#registerQueryBuilder(edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace)
     */
    //@Override
    public void registerQueryBuilder(final ViewBasedSearchQueryBuilderIFace builder)
    {
        if (panel != null)
        {
            panel.registerQueryBuilder(builder);
        } else
        {
            log.error("The internal panel is null!");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchDialogIFace#getSelectedObjects()
     */
    public List<Object> getSelectedObjects()
    {
        if (!isCancelled)
        {
            return ((DBObjSearchPanel)mainPanel).getSelectedObjects();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchDialogIFace#setMultipleSelection(boolean)
     */
    public void setMultipleSelection(final boolean isMultiple)
    {
        if (panel != null)
        {
            panel.setMultipleSelection(isMultiple);
        }
        
    }
    
}

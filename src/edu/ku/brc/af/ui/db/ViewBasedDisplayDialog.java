/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.ui.db;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.tasks.subpane.FormPane.FormPaneAdjusterIFace;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This is just the JFrame portion of a Frame used to display the fields in a data object. Instances of this class are created
 * by the implementation of ViewBasedDialogFactoryIFace interface. This class is consideraed to be a reference implementation.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class ViewBasedDisplayDialog extends CustomDialog implements ViewBasedDisplayIFace
{
    protected ViewBasedDisplayPanel         viewBasedPanel = null;
    protected ViewBasedDisplayActionAdapter vbdaa          = null;
    protected Object                        parentDataObj  = null;
    protected boolean                       doSave         = false;
    
    
    /**
     * Constructs a search dialog from form infor and from search info.
     * @param frame the parent frame
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param displayName the search name, this is looked up by name in the "search_config.xml" file
     * @param title the title (should be already localized before passing in)
     * @param closeBtnTitle the title of close btn
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @param options the options needed for creating the form
     */
    public ViewBasedDisplayDialog(final Frame  parentFrame,
                                  final String viewSetName,
                                  final String viewName,
                                  final String displayName,
                                  final String title,
                                  final String closeBtnTitle,
                                  final String className,
                                  final String idFieldName,
                                  final boolean isEdit,
                                  final int     options)
    {
        this(parentFrame, 
             viewSetName, 
             viewName, 
             displayName, 
             title, 
             closeBtnTitle, 
             className, 
             idFieldName, 
             isEdit, 
             true, 
             null, 
             null, 
             options,
             (isEdit ? CustomDialog.OKCANCEL : CustomDialog.OK_BTN) | CustomDialog.HELP_BTN);
    }

    /**
     * Constructs a search dialog from form infor and from search info.
     * @param frame the parent frame
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param displayName the search name, this is looked up by name in the "search_config.xml" file
     * @param title the title (should be already localized before passing in)
     * @param closeBtnTitle the title of close btn
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @param doRegOKBtn Indicates whether the OK btn should be registered so it calls save
     * @param options the options needed for creating the form
     * @param dlgBtnOptions which btns the dialog should display
     */
    public ViewBasedDisplayDialog(final Frame     parentFrame,
                                  final String    viewSetName,
                                  final String    viewName,
                                  final String    displayName,
                                  final String    title,
                                  final String    closeBtnTitle,
                                  final String    className,
                                  final String    idFieldName,
                                  final boolean   isEdit,
                                  final boolean   doRegOKBtn,
                                  final String    cellName,
                                  final MultiView mvParent,
                                  final int       options,
                                  final int       dlgBtnOptions)
    {
        super(parentFrame, title, true, dlgBtnOptions, null);
        
        viewBasedPanel = new ViewBasedDisplayPanel(this, 
                viewSetName, 
                viewName, 
                displayName, 
                className, 
                idFieldName, 
                isEdit, 
                doRegOKBtn,
                cellName,
                mvParent,
                options | MultiView.NO_SCROLLBARS);
        
        if (StringUtils.isNotEmpty(closeBtnTitle))
        {
            this.setOkLabel(closeBtnTitle);
        }
    }
    
    /**
     * Constructs a search dialog from form infor and from search info.
     * @param dlg the parent frame
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param displayName the search name, this is looked up by name in the "search_config.xml" file
     * @param title the title (should be already localized before passing in)
     * @param closeBtnTitle the title of close btn
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @param options the options needed for creating the form
     */
    public ViewBasedDisplayDialog(final Dialog  parentDialog,
                                  final String  viewSetName,
                                  final String  viewName,
                                  final String  displayName,
                                  final String  title,
                                  final String  closeBtnTitle,
                                  final String  className,
                                  final String  idFieldName,
                                  final boolean isEdit,
                                  final int     options)
    {
        this(parentDialog, viewSetName, viewName, displayName, title, closeBtnTitle, className, idFieldName, isEdit, isEdit, null, null, options);
    }
    
    /**
     * Constructs a search dialog from form infor and from search info.
     * @param dlg the parent frame
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param displayName the search name, this is looked up by name in the "search_config.xml" file
     * @param title the title (should be already localized before passing in)
     * @param closeBtnTitle the title of close btn
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @param doRegOKBtn Indicates whether the OK btn should be registered so it calls save
     * @param options the options needed for creating the form
     */
    public ViewBasedDisplayDialog(final Dialog    parentDialog,
                                  final String    viewSetName,
                                  final String    viewName,
                                  final String    displayName,
                                  final String    title,
                                  final String    closeBtnTitle,
                                  final String    className,
                                  final String    idFieldName,
                                  final boolean   isEdit,
                                  final boolean   doRegOKBtn,
                                  final String    cellName,
                                  final MultiView mvParent,
                                  final int       options)
    {
        super(parentDialog, title, true, (isEdit ? CustomDialog.OKCANCEL : CustomDialog.OK_BTN) | CustomDialog.HELP_BTN, null);
        
        viewBasedPanel = new ViewBasedDisplayPanel(this, 
                viewSetName, 
                viewName, 
                displayName, 
                className, 
                idFieldName, 
                isEdit, 
                doRegOKBtn,
                cellName,
                mvParent,
                options | MultiView.NO_SCROLLBARS);
        
        if (StringUtils.isNotEmpty(closeBtnTitle))
        {
            this.setOkLabel(closeBtnTitle);
        }
    }

    public ViewBasedDisplayDialog(Dialog parentDlg,
                                  final String    viewSetName,
                                  final String    viewName,
                                  final String    displayName,
                                  final String    title,
                                  final String    closeBtnTitle,
                                  final String    className,
                                  final String    idFieldName,
                                  final boolean   isEdit,
                                  final boolean   doRegOKBtn,
                                  final String    cellName,
                                  final MultiView mvParent,
                                  final int       options,
                                  final int       dlgBtnOptions)
    {
        super(parentDlg, title, true, dlgBtnOptions, null);

        viewBasedPanel = new ViewBasedDisplayPanel(this,
                viewSetName,
                viewName,
                displayName,
                className,
                idFieldName,
                isEdit,
                doRegOKBtn,
                cellName,
                mvParent,
                options | MultiView.NO_SCROLLBARS);

        if (StringUtils.isNotEmpty(closeBtnTitle))
        {
            this.setOkLabel(closeBtnTitle);
        }
    }

    public static ViewBasedDisplayDialog create(final String    viewSetName,
                                                final String    viewName,
                                                final String    displayName,
                                                final String    title,
                                                final String    closeBtnTitle,
                                                final String    className,
                                                final String    idFieldName,
                                                final boolean   isEdit,
                                                final boolean   doRegOKBtn,
                                                final String    cellName,
                                                final MultiView mvParent,
                                                final int       options,
                                                final int       dlgBtnOptions) {
        Window parentDlg = UIRegistry.getMostRecentWindow();
        if (parentDlg instanceof Dialog) {
            return new ViewBasedDisplayDialog((Dialog)parentDlg, viewSetName, viewName, displayName, title, closeBtnTitle, className,
                    idFieldName, isEdit, doRegOKBtn, cellName, mvParent, options, dlgBtnOptions);
        } else {
            return new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(), viewSetName, viewName, displayName, title, closeBtnTitle, className,
                    idFieldName, isEdit, doRegOKBtn, cellName, mvParent, options, dlgBtnOptions);
        }
    }
    /**
     * Enables the caller to have the UI pre-created before the setVisible 
     */
    public void preCreateUI()
    {
        createUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.ViewBasedDisplayIFace#setDoSave(boolean)
     */
    public void setDoSave(boolean doSave)
    {
        this.doSave = doSave;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setBackground(viewBasedPanel.getBackground());

        JScrollPane scrollPane = UIHelper.createScrollPane(viewBasedPanel, true);
        scrollPane.setBorder(BorderFactory.createLineBorder(getBackground(), 8));
        contentPanel = scrollPane;
        
        super.createUI();
        
        viewBasedPanel.setOkCancelBtns(okBtn, cancelBtn); 
        
        Integer width = (Integer)UIManager.get("ScrollBar.width");
        if (width == null)
        {
        	width = (new JScrollBar()).getPreferredSize().width;
        }
        
        Dimension dim1 = getPreferredSize();
        dim1.height += width * 2;
        if (!UIHelper.isMacOS())
        {
            dim1.width += width;
        }
        setSize(dim1);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedDisplayIFace#setParentData(java.lang.Object)
     */
    @Override
    public void setParentData(Object parentDataObj)
    {
        this.parentDataObj = parentDataObj;
    }

    /*
     * (non-Javadoc)
     * @see java.awt.Dialog#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean visible)
    {
        if (viewBasedPanel != null && !visible)
        {
            viewBasedPanel.aboutToShow(visible);
        }
        pack();
        super.setVisible(visible);
    }
    
    /**
     * Sets the form adjuster into the panel.
     * @param adjusterthe adjuster (usually the BusinessRules)
     */
    public void setFormAdjuster(final FormPaneAdjusterIFace adjuster)
    {
        if (viewBasedPanel != null)
        {
            viewBasedPanel.setFormAdjuster(adjuster);
        }
    }
    
    //------------------------------------------------------------
    //-- ActionListener Interface
    //------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#applyButtonPressed()
     */
    @Override
    protected void applyButtonPressed()
    {
        if (vbdaa != null && !vbdaa.cancelPressed(this))
        {
            return;
        }
        super.applyButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
     */
    @Override
    protected void cancelButtonPressed()
    {
        if (vbdaa != null && !vbdaa.cancelPressed(this))
        {
            return;
        }
        super.cancelButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#helpButtonPressed()
     */
    @Override
    protected void helpButtonPressed()
    {
        if (vbdaa != null && !vbdaa.helpPressed(this))
        {
            return;
        }
        super.helpButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        if (vbdaa != null)
        {
            if (!vbdaa.okPressed(this))
            {
                return;
            }
            FormHelper.updateLastEdittedInfo(viewBasedPanel.getMultiView().getData());
        }
        
        if (viewBasedPanel.isEditMode())
        {
            FormViewObj fvo = viewBasedPanel.getMultiView().getCurrentViewAsFormViewObj();
            if (fvo != null)
            {
                BusinessRulesIFace br = fvo.getBusinessRules();
                if (br != null && fvo.getDataObj() != null)
                {
                    boolean isNewObj = MultiView.isOptionOn(fvo.getMVParent().getOptions(), MultiView.IS_NEW_OBJECT);
                    if (doSave)
                    {
                        try
                        {
                            if (!fvo.saveObject())
                            {
                                return;
                            }
                        } finally
                        {
                            DataProviderSessionIFace session = fvo.getSession();
                            if (session != null && session.isOpen())
                            {
                                session.close();
                            }
                        }
                        
                    } else if (BusinessRulesIFace.STATUS.OK != br.processBusinessRules(parentDataObj, 
                                                                                fvo.getDataObj(), 
                                                                                isNewObj))
                    {
                        String msg = br.getMessagesAsString();
                        if (StringUtils.isBlank(msg)) {
                        	msg = UIRegistry.getResourceString("ViewBasedDisplayDialog.ActionNotCompleted");
                        }
                    	UIRegistry.showError(msg);
                        return;
                    }
                }
            }
        }
        super.okButtonPressed();
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

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedDisplayIFace#getMultiView()
     */
    public MultiView getMultiView()
    {
        return viewBasedPanel.getMultiView();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedDisplayIFace#setCloseListener(edu.ku.brc.ui.db.ViewBasedDisplayActionAdapter)
     */
    public void setCloseListener(final ViewBasedDisplayActionAdapter vbdaa)
    {
        this.vbdaa = vbdaa;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedDisplayIFace#setData(java.lang.Object)
     */
    public void setData(final Object dataObj)
    {
        viewBasedPanel.setData(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedDisplayIFace#isEditMode()
     */
    public boolean isEditMode()
    {
        return viewBasedPanel.isEditMode();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedDisplayIFace#shutdown()
     */
    public void shutdown()
    {
        setVisible(false);
        viewBasedPanel.shutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedDisplayIFace#setsession(edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void setSession(final DataProviderSessionIFace session)
    {
        viewBasedPanel.setSession(session);
    }
}

/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.db;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.tasks.subpane.FormPane.FormPaneAdjusterIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**
 * This is the content panel portion of "display" dialogs/frames that are created by the implemenation of the ViewBasedDialogFactoryIFace
 * interface.<br>
 * <br>
 * Note: The registered PropertyChangeListener will be notified when the user presses "Save/Close" or "Cancel" and the PropertyChangeEvent's propertyName
 * will contain the value "OK" or "Cancel" depending on which button was pressed.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class ViewBasedDisplayPanel extends JPanel implements ActionListener
{
    private static final Logger log  = Logger.getLogger(ViewBasedDisplayPanel.class);

    // Form Stuff
    protected MultiView      multiView;
    protected ViewIFace      formView;
    protected List<String>   fieldNames;

    protected ViewBasedDisplayActionAdapter vbdaa = null;

    // Members needed for creating results
    protected String         className;
    protected String         idFieldName;
    protected String         displayName;

    // UI
    protected JButton        okBtn;
    protected JButton        cancelBtn    = null;
    protected Window         parentWin;
    protected boolean        isCancelled  = false;
    protected boolean        doRegOKBtn;                   // Indicates whether the OK btn should be registered so it calls save

    /**
     * Constructor.
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the class that is the primary key which is filled in from the search table id
     */
    public ViewBasedDisplayPanel(final String className,
                                 final String idFieldName)
    {
        this.className   = className;
        this.idFieldName = idFieldName;
    }

    /**
     * Constructs a search dialog from form infor and from search info.
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param displayName the search name, this is looked up by name in the "search_config.xml" file
     * @param closeBtnTitle the title of close btn
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the class that is the primary key which is filled in from the search table id
     * @param isEdit whether it is in edit mode or not
     * @param options the options needed for creating the form
     */
    public ViewBasedDisplayPanel(final Window  parent,
                                 final String  viewSetName,
                                 final String  viewName,
                                 final String  displayName,
                                 final String  className,
                                 final String  idFieldName,
                                 final boolean isEdit,
                                 final int     options)
    {
        this(parent, viewSetName, viewName, displayName, className, idFieldName, isEdit, true, null, null, options);
    }

    /**
     * @param viewSetName
     * @param viewName
     * @param className
     * @param isEdit
     * @param options
     */
    public ViewBasedDisplayPanel(final String  viewSetName,
                                 final String  viewName,
                                 final String  className,
                                 final boolean isEdit,
                                 final int     options)
    {
        this(null, viewSetName, viewName, null, className, null, isEdit, true, null, null, options);
    }

    /**
     * Constructs a search dialog from form information and from search info.
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param displayName the search name, this is looked up by name in the "search_config.xml" file
     * @param closeBtnTitle the title of close btn
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the class that is the primary key which is filled in from the search table id
     * @param isEdit whether it is in edit mode or not
     * @param doRegOKBtn Indicates whether the OK btn should be registered so it calls save
     * @param cellName the cellName of the data
     * @param options the options needed for creating the form
     */
    public ViewBasedDisplayPanel(final Window    parent,
                                 final String    viewSetName,
                                 final String    viewName,
                                 final String    displayName,
                                 final String    className,
                                 final String    idFieldName,
                                 final boolean   isEdit,
                                 final boolean   doRegOKBtn,
                                 final String    cellName,
                                 final MultiView mvParent,
                                 final int       options)
    {
        this.parentWin   = parent;
        this.className   = className;
        this.idFieldName = idFieldName;
        this.displayName = displayName;
        this.doRegOKBtn  = doRegOKBtn;

        createUI(viewSetName, viewName, isEdit, cellName, mvParent, options);
    }

    /**
     * Creates the Default UI.
     * @param viewSetName the set to to create the form
     * @param viewName the view name to use
     * @param closeBtnTitle the title of close btn
     * @param isEdit true is in edit mode, false is in view mode
     * @param cellName the cellName of the data
     * @param options the options needed for creating the form
     */
    protected void createUI(final String  viewSetName,
                            final String  viewName,
                            final boolean isEdit,
                            final String  cellName,
                            final MultiView mvParent,
                            final int     options)
    {
        //MultiView.printCreateOptions("createUI", options);
        formView = AppContextMgr.getInstance().getView(viewSetName, viewName);
        if (formView != null)
        {
            multiView = new MultiView(mvParent,
                                      cellName, 
                                      formView, 
                                      isEdit ? AltViewIFace.CreationMode.EDIT : AltViewIFace.CreationMode.VIEW,
                                      options | MultiView.DONT_USE_EMBEDDED_SEP, null);

        } else
        {
            log.error("Couldn't load form with ViewSetName ["+viewSetName+"] View Name ["+viewName+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return;
        }

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        add(multiView, BorderLayout.CENTER);
        
        if (parentWin != null && parentWin instanceof JDialog)
        {
            ((JDialog)parentWin).getRootPane().setDefaultButton(okBtn);
        }
    }
    
    /**
     * @param adjuster
     */
    public void setFormAdjuster(final FormPaneAdjusterIFace adjuster)
    {
        if (adjuster != null && multiView != null && multiView.getCurrentViewAsFormViewObj() != null)
        {
            adjuster.adjustForm(multiView.getCurrentViewAsFormViewObj());
        }
    }
    
    /**
     * Sets the OK and Cancel; buttons into the panel
     * @param okBtn ok btn (cannot be null)
     * @param cancelBtn the cancel btn (can be null
     */
    public void setOkCancelBtns(final JButton okBtn, 
                                final JButton cancelBtn)
    {
        this.okBtn     = okBtn;
        this.cancelBtn = cancelBtn;
        
        if (doRegOKBtn && multiView != null)
        {
            for (Viewable v : multiView.getViewables())
            {
                v.registerSaveBtn(okBtn);
            }
        }
        
        for (Viewable viewable : multiView.getViewables())
        {
            FormValidator fv = viewable.getValidator();
            if (fv != null)
            {
                //fv.addEnableItem(okBtn, FormValidator.EnableType.ValidAndChangedItems);
                fv.setSaveComp(okBtn, FormValidator.EnableType.ValidItems);
            }
        }
        
        if (okBtn != null)
        {
            okBtn.addActionListener(this);
            
            // Why are we doing this????? - rods 11/15/07
            // I am commenting it out because the dialog "ok/accept/save" buttons are being enabled
            // when they shouldn't be
            /*if (MultiView.isOptionOn(multiView.getOptions(), MultiView.IS_EDITTING))
            {
                okBtn.setEnabled(true);
            }*/
        }
        
        if (cancelBtn != null)
        {
            cancelBtn.addActionListener(this);
        }
    }

    /**
     * Returns whether the form is in edit mode or not.
     * @return true in edit mode, false it is not
     */
    public boolean isEditMode()
    {
        return multiView.isEditable();
    }

    /**
     * Returns the OK button.
     * @return the OK button
     */
    public JButton getOkBtn()
    {
        return okBtn;
    }

    /**
     * Returns the Cancel button or null if there isn't one.
     * @return the Canel button or null if there isn't one.
     */
    public JButton getCancelBtn()
    {
        return cancelBtn;
    }

    /**
     * Returns true if cancelled.
     * @return true if cancelled.
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        // Handle clicks on the OK and Cancel buttons.
        boolean isOkButton = (e.getSource() == okBtn);
        if (isOkButton)
        {
            multiView.getDataFromUI();
            isCancelled = false;
            
        } else
        {
            isCancelled = true;
        }
    }

    /**
     * Returns the MultiView.
     * @return the multiview
     */
    public MultiView getMultiView()
    {
        return multiView;
    }

    /**
     * Sets data into the dialog.
     * @param dataObj the data object
     */
    public void setData(final Object dataObj)
    {
        if (multiView != null)
        {
            multiView.setData(dataObj);
            
            // This was added 12/18/07 - rods for pop up Taxon form
            // Tells it is is a new form and all the validator painting should be supressed
            // on required fields until the user inputs something
            if (MultiView.isOptionOn(multiView.getOptions(), MultiView.IS_NEW_OBJECT))
            {
                multiView.setIsNewForm(true, false); // traverse immediate children only
            }

            if (multiView.getCurrentView() != null && 
                multiView.getCurrentView().getValidator() != null)
            {
                multiView.getCurrentView().getValidator().validateForm();
            }
        }
    }
    
    /**
     * @param session
     */
    public void setSession(DataProviderSessionIFace session)
    {
        multiView.setSession(session);
    }
    
    /**
     * Tells the multiview that it is about to be shown.
     * We filter out true because it has already been called during the creation process.
     * @param show the is will be shown or hidden
     */
    protected void aboutToShow(final boolean show)
    {
        if (multiView != null && !show)
        {
            multiView.aboutToShow(show);
        }  
    }

    /**
     * Tells the panel that it is being shutdown and it should be cleaned up.
     */
    public void shutdown()
    {
        if (multiView != null)
        {
            multiView.aboutToShutdown();
            multiView.shutdown();
        }
        
        formView = null;
        
        if (fieldNames != null)
        {
            fieldNames.clear();
        }
    }

}

/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;

/**
 * A simple dialog for displaying a form appropriate for editing objects
 * of a specified class.  The user registers an instance of {@link EditDialogCallback}
 * used in signalling when the displayed dialog has been closed by user interaction.
 * 
 * @code_status Complete
 * @author jstewart
 */
@SuppressWarnings("serial")
public class EditFormDialog<T> extends JDialog implements ActionListener
{
    /** The logger to use when emitting any messages. */
    private static final Logger log  = Logger.getLogger(EditFormDialog.class);
    /** The displayed form. */
    protected Viewable       form;
    /** The JButton signalling the 'OK' user action. */
    protected JButton        okBtn;
    /** The JButton signalling the 'Cancel' user action. */
    protected JButton		 cancelBtn;
    /** The registered callback to notify after user action occurs. */
    protected EditDialogCallback<T> callback;
    
    protected boolean isNewObject;
    
    /**
     * Constructs a node edit dialog using the given view to represent objects
     * of the given class.
     * 
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param title the title (should be already localized before passing in)
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @throws HeadlessException an exception
     */
    public EditFormDialog(final String viewSetName,
                                final String viewName,
                                final String title,
                                final EditDialogCallback<T> callback,
                                final boolean isNewObject) throws HeadlessException
    {
        super((Frame)UICacheManager.get(UICacheManager.FRAME), title, true);
        
        this.isNewObject = isNewObject;
        
        this.callback = callback;
        
        createUI(viewSetName, viewName, title);

        setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(false);
    }

    /**
     * Do all of the real work in building the UI.
     *
     * @param viewSetName the viewset name
     * @param viewName the name of the presented view
     * @param title the title of the dialog
     */
    protected void createUI(final String viewSetName,
                            final String viewName,
                            final String title)
    {
    	View formView = AppContextMgr.getInstance().getView(viewSetName, viewName);
    	MultiView multiView = null;
    	if (formView != null)
        {
            // TODO The last arg is set to false, but it should really be passed in from the constructor
        	multiView = new MultiView(null, null, formView, AltView.CreationMode.Edit, MultiView.NO_OPTIONS);
            form = multiView.getCurrentView();

        } else
        {
            log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        panel.add(multiView, BorderLayout.CENTER);

        okBtn = new JButton(getResourceString("OK"));
        okBtn.addActionListener(this);
        getRootPane().setDefaultButton(okBtn);
        
        cancelBtn = new JButton(getResourceString("Cancel"));
        cancelBtn.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelBtn);
        buttonPanel.add(okBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(panel);
        setTitle(title);
        pack();
        setResizable(false);
    }
    
    /**
     * Sets data from the given object into the dialog.
     * 
     * @param dataObj the data object
     */
    public void setData(final T dataObj)
    {
        form.setDataObj(dataObj);
    }

    /**
     * Receives the signal that the user has pressed either the 'OK' or 'Cancel'
     * button.  Based on the button pressed, the {@link ActionEvent} is passed on
     * to <code>okAction(ActionEvent)</code> or <code>cancelAction(ActionEvent)</code>.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     * @param e the action data
     */
    public void actionPerformed(ActionEvent e)
    {
    	if( e.getSource().equals(okBtn) )
    	{
    		okAction(e);
    	}
    	else if( e.getSource().equals(cancelBtn) )
    	{
    		cancelAction(e);
    	}
    	return;
    }
    
    /**
     * Gathers the values from the form and modifies the data object appropriately.
     */
    protected void getData()
    {
    	form.getDataFromUI();
    }
    
    /**
     * Hides the form, gathers values from it into the data object and
     * notifies the registered callback provider.
     *
     * @param e the action data (ignored)
     */
    @SuppressWarnings("unchecked")
	protected void okAction(@SuppressWarnings("unused")	ActionEvent e)
    {
        setVisible(false);
        
        getData();
        
        Object dataObj = form.getDataObj();
        if (dataObj instanceof FormDataObjIFace)
        {
            FormDataObjIFace formObj = (FormDataObjIFace)dataObj;
            String currentUserString = FormHelper.getCurrentUserEditStr();
            formObj.setLastEditedBy(currentUserString);
            formObj.setTimestampModified(new Timestamp(System.currentTimeMillis()));
        }
        
        // save changes to any object that already exists in the DB
        if (!isNewObject)
        {
            ((FormViewObj)form).saveObject();
        }
        
        callback.editCompleted((T)dataObj);
    }
    
    /**
     * Hides the form and notifies the registered callback provider.
     *
     * @param e
     */
    @SuppressWarnings("unchecked")
	protected void cancelAction(@SuppressWarnings("unused")	ActionEvent e)
    {
        setVisible(false);

    	callback.editCancelled((T)form.getDataObj());
    }
    
    /**
     * The interface that must be implemented to register a callback
     * on an {@link EditFormDialog}.
     *
     * @code_status Complete
     * @author jstewart
     */
    public interface EditDialogCallback<U>
    {
    	/**
    	 * The {@link EditFormDialog} was closed by the user pressing the
    	 * 'OK' button.
    	 *
    	 * @param dataObj the data object represented by the form
    	 */
    	public void editCompleted(U dataObj);
    	/**
    	 * The {@link EditFormDialog} was closed by the user pressing the
    	 * 'Cancel' button.
    	 *
    	 * @param dataObj the data object represented by the form
    	 */
    	public void editCancelled(U dataObj);
    }
}

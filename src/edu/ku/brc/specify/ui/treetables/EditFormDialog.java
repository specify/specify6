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
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ViewMgr;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;

/**
 * Comments must be updated.  Most of this code, including comments, was taken from DBObjDisplayDialog.java
 * 
 * @code_status Unknown (auto-generated)
 * @author jstewart
 */
@SuppressWarnings("serial")
public class EditFormDialog extends JDialog implements ActionListener
{
    private static final Logger log  = Logger.getLogger(EditFormDialog.class);

    // Form Stuff
    protected MultiView      multiView;
    protected View           formView;
    protected Viewable       form;
    protected List<String>   fieldNames;
    
    // Members needed for creating results
    protected String         className;
    protected String         idFieldName;

    // UI
    protected JButton        okBtn;
    protected JButton		 cancelBtn;

    protected JPanel         contentPanel;
    
    protected EditDialogCallback callback;
    
    /**
     * Constructs a {@link Treeable} node edit dialog from form info
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
                                final String className,
                                final String idFieldName,
                                final EditDialogCallback callback) throws HeadlessException
    {
        super((Frame)UICacheManager.get(UICacheManager.FRAME), title, true);
        
        this.callback = callback;
        
        this.className   = className;
        this.idFieldName = idFieldName;

        createUI(viewSetName, viewName, title);

        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(false);
    }

    protected void createUI(final String viewSetName,
                            final String viewName,
                            final String title)
    {
        formView = ViewMgr.getView(viewSetName, viewName);
        if (formView != null)
        {
            multiView   = new MultiView(null, formView, AltView.CreationMode.Edit, false, true);
            form = multiView.getCurrentView();

        } else
        {
            log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        panel.add(multiView, BorderLayout.NORTH);
        contentPanel = new JPanel(new NavBoxLayoutManager(0,2));

        okBtn = new JButton(getResourceString("OK"));
        okBtn.addActionListener(this);
        getRootPane().setDefaultButton(okBtn);
        
        cancelBtn = new JButton(getResourceString("Cancel"));
        cancelBtn.addActionListener(this);

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        btnBuilder.addGlue();
        btnBuilder.addGriddedButtons(new JButton[] { cancelBtn, okBtn });

        panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
    }
    
    /**
     * Sets data into the dialog.
     * 
     * @param dataObj the data object
     */
    public void setData(final Object dataObj)
    {
        form.setDataObj(dataObj);
    }

    public void actionPerformed(ActionEvent e)
    {
        // Handle clicks on the OK buttons.
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
    
    protected void getData()
    {
    	form.getDataFromUI();
    }
    
    protected void okAction(ActionEvent e)
    {
        // Handle clicks on the OK buttons.
        setVisible(false);
        
        getData();
        
        callback.editCompleted(form.getDataObj());
    }
    
    protected void cancelAction(ActionEvent e)
    {
        setVisible(false);

    	callback.editCancelled(form.getDataObj());
    }
    
    public interface EditDialogCallback
    {
    	public void editCompleted(Object dataObj);
    	public void editCancelled(Object dataObj);
    }
}

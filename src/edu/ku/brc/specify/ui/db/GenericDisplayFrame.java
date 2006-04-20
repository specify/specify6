/*
 * Filename:    $RCSfile: GenericDisplayFrame.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.3 $
 * Date:        $Date: 2005/10/20 12:53:02 $
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
package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.specify.core.NavBoxLayoutManager;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.MultiView;
import edu.ku.brc.specify.ui.forms.ViewFactory;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.Viewable;
import edu.ku.brc.specify.ui.forms.persist.AltView;
import edu.ku.brc.specify.ui.forms.persist.View;

/**
 * This is a "generic" or more specifically "configurable" search dialog class. This enables you to specify a form to be used to enter the search criteria
 * and then the search definition it is to use to do the search and display the results as a table in the dialog. The resulting class is to be passed in
 * on construction so the results of the search can actually yield a Hibernate object.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class GenericDisplayFrame extends JFrame implements ActionListener
{
    private static Log log  = LogFactory.getLog(GenericDisplayFrame.class);

    // Form Stuff
    protected MultiView      multiView;
    protected View           formView;
    protected Viewable       form;
    protected List<String>   fieldNames;
    
    protected PropertyChangeListener propertyChangeListener = null;

    // Members needed for creating results
    protected String         className;
    protected String         idFieldName;
    protected String         displayName;

    // UI
    protected JButton        okBtn;

    protected JPanel         contentPanel;


    /**
     * Constructs a search dialog from form infor and from search info
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param displayName the search name, this is looked up by name in the "search_config.xml" file
     * @param title the title (should be already localized before passing in)
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @throws HeadlessException an exception
     */
    public GenericDisplayFrame(final String viewSetName,
                                final String viewName,
                                final String displayName,
                                final String title,
                                final String className,
                                final String idFieldName) throws HeadlessException
    {
        //super((Frame)UICacheManager.get(UICacheManager.FRAME), title, true);
        this.setTitle(title);
        
        this.className   = className;
        this.idFieldName = idFieldName;
        this.displayName  = displayName;

        createUI(viewSetName, viewName, title);

        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //this.setModal(false);
    }

    /**
     * Creates the Default UI
     *
     */
    protected void createUI(final String viewSetName,
                            final String viewName,
                            final String title)
    {
        formView = ViewMgr.getView(viewSetName, viewName);
        if (formView != null)
        {
            multiView   = new MultiView(null, formView, AltView.CreationMode.View, false, false);
            form = multiView.getCurrentView();//ViewFactory.createFormView(null, formView, null, null);
            add(form.getUIComponent(), BorderLayout.CENTER);

        } else
        {
            log.info("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        panel.add(form.getUIComponent(), BorderLayout.NORTH);
        contentPanel = new JPanel(new NavBoxLayoutManager(0,2));

        okBtn = new JButton(getResourceString("Close"));
        okBtn.addActionListener(this);
        getRootPane().setDefaultButton(okBtn);

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        btnBuilder.addGlue();
        btnBuilder.addGriddedButtons(new JButton[] { okBtn });

        panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
    }
    
    /**
     * Set a listener to know when the dialog is closed
     * @param propertyChangeListener the listener
     */
    public void setCloseListener(final PropertyChangeListener propertyChangeListener)
    {
        this.propertyChangeListener = propertyChangeListener;
    }
    
    /**
     * Sets data into the dialog
     * @param dataObj the data object
     */
    public void setData(final Object dataObj)
    {
        //if (dataObj != null)
        //{
            form.setDataObj(dataObj);
            form.setDataIntoUI();
        //}
    }


    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        // Handle clicks on the OK and Cancel buttons.
        setVisible(false);
        propertyChangeListener.propertyChange(null);
        propertyChangeListener = null;
    }

}

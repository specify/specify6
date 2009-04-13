/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.config.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 01, 2009
 *
 */
public class MasterUserPanel extends GenericFormPanel
{
    protected String                  propName = "next";
    protected Boolean                 isOK     = null;
    protected JButton                 testBtn;
    protected JLabel                  label;
    protected String                  errorKey = null;

    /**
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param nextBtn
     * @param makeStretchy
     */
    public MasterUserPanel(String name, 
                           String title, 
                           String helpContext, 
                           String[] labels,
                           String[] fields, 
                           JButton nextBtn, 
                           JButton prevBtn, 
                           boolean makeStretchy)
    {
        super(name, title, helpContext, labels, fields, nextBtn, prevBtn, makeStretchy);
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#init(java.lang.String, java.lang.String[], boolean[], java.lang.String[])
     */
    @Override
    protected void init(String title, String[] fields, boolean[] required, String[] types)
    {
        super.init(title, fields, required, types);
        
        label   = UIHelper.createLabel(" ", SwingConstants.CENTER);
        testBtn = UIHelper.createI18NButton("CREATE_MASTER_BTN");
        
        PanelBuilder tstPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        tstPB.add(testBtn,            cc.xy(2, 1));
        
        PanelBuilder panelPB = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p,2px,p,f:p:g"));
        panelPB.add(tstPB.getPanel(), cc.xy(1, 1));
        panelPB.add(getProgressBar(), cc.xy(1, 3));
        panelPB.add(label,            cc.xy(1, 5));
        
        builder.add(panelPB.getPanel(), cc.xyw(3, row, 2));
        row += 2;
        
        testBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                testCreateMU();
            }
        });
        progressBar.setVisible(false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#getValues(java.util.Properties)
     */
    @Override
    public void getValues(Properties props)
    {
        super.getValues(props);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#getAdditionalRowDefs()
     */
    @Override
    protected String getAdditionalRowDefs()
    {
        return ",2px,p";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        boolean isValid = super.isUIValid();
        
        return isValid && isOK != null && isOK;
    }
    
    /**
     * @param enable
     */
    protected void setUIEnabled(final boolean enable)
    {
        for (JComponent c : compList)
        {
            c.setEnabled(enable);
        }
        testBtn.setEnabled(enable);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#textChanged(javax.swing.JTextField)
     */
    @Override
    protected void textChanged(final JTextField txt)
    {
        super.textChanged(txt);
        
        if (isOK != null && !isOK)
        {
            isOK = null;
            testBtn.setVisible(true);
            label.setText(" ");
            properties.put("masterChanged", true);
        }
    }

    /**
     * 
     */
    protected void testCreateMU()
    {
        if (isOK == null || !isOK)
        {
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            
            setUIEnabled(false);
            
            label.setText(UIRegistry.getResourceString("CONN_DB"));
            
            testBtn.setVisible(false);
            
            SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
            {
                @Override
                protected Object doInBackground() throws Exception
                {
                    isOK = false;
                    
                    DBMSUserMgr mgr = DBMSUserMgr.getInstance();
                    
                    String dbUserName = properties.getProperty("dbUserName");
                    String dbPassword = properties.getProperty("dbPassword");
                    String dbName     = properties.getProperty("dbName");
                    String hostName   = properties.getProperty("hostName");
                    
                    String saUserName = ((JTextField)comps.get("saUserName")).getText();
                    String saPassword = ((JTextField)comps.get("saPassword")).getText();
                    
                    if (mgr.connectToDBMS(dbUserName, dbPassword, hostName))
                    {
                        if (mgr.doesUserExists(saUserName))
                        {
                            if (!mgr.dropUser("\'"+saUserName+"\'@\'"+hostName+"\'"))
                            {
                                errorKey = "ERR_DROP_USR";
                            }
                        }
                        
                        if (!mgr.doesUserExists(saUserName))
                        {
                            firePropertyChange(propName, 0, 1);
                            
                            isOK = mgr.createUser(saUserName, saPassword, dbName, DBMSUserMgr.PERM_ALL);
                            if (!isOK)
                            {
                                errorKey = "ERR_CRE_MASTER";
                            }
                        } else
                        {
                            isOK = true;
                        }
                    } else
                    {
                        errorKey = "NO_CONN_ROOT";
                        isOK = false;
                    }
                    mgr.close();
                    
                    return null;
                }
    
                /* (non-Javadoc)
                 * @see javax.swing.SwingWorker#done()
                 */
                @Override
                protected void done()
                {
                    super.done();
                    
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                    
                    setUIEnabled(true);
                    
                    updateBtnUI();
                    
                    label.setText(UIRegistry.getResourceString(isOK ? "MASTER_CREATED" : errorKey));
                    if (isOK)
                    {
                        setUIEnabled(false);
                    }
                }
            };
            
            worker.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public  void propertyChange(final PropertyChangeEvent evt) {
                            if (propName.equals(evt.getPropertyName())) 
                            {
                                MasterUserPanel.this.label.setText(UIRegistry.getLocalizedMessage("CREATE_MASTER"));
                            }
                        }
                    });
            worker.execute();
        }
    }
}

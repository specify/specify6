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

import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;

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

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
    protected JButton                 createMUBtn;
    protected JLabel                  label;
    protected String                  errorKey = null;
    
    // Advanced Part
    protected JButton                 skipStepBtn;
    protected boolean                 manualLoginOK = false;
    protected JLabel                  advLabel;


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
        createMUBtn = UIHelper.createI18NButton("CREATE_MASTER_BTN");
        
        PanelBuilder tstPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        tstPB.add(createMUBtn,            cc.xy(2, 1));
        
        PanelBuilder panelPB = new PanelBuilder(new FormLayout("f:p:g", "20px,p,2px,p,2px,p"));
        panelPB.add(tstPB.getPanel(), cc.xy(1, 2));
        panelPB.add(getProgressBar(), cc.xy(1, 4));
        panelPB.add(label,            cc.xy(1, 6));
        
        builder.add(panelPB.getPanel(), cc.xyw(3, row, 2));
        row += 2;
        
        // Advance part of pane
        advLabel    = UIHelper.createI18NLabel("ADV_MU_DESC", SwingConstants.CENTER);
        skipStepBtn = UIHelper.createI18NButton("ADV_MU_TEST");
        builder.addSeparator(UIRegistry.getResourceString("ADV_TITLE"), cc.xyw(3, row, 2)); row += 2;
        builder.add(advLabel, cc.xyw(3, row, 2)); row += 2;
        
        tstPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        tstPB.add(skipStepBtn,          cc.xy(2, 1));
        builder.add(tstPB.getPanel(),   cc.xyw(3, row, 2)); row += 2;
        
        skipStepBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                createMUBtn.setEnabled(false);
                skipStepBtn.setEnabled(false);
                boolean ok = skipDBCreate();
                createMUBtn.setEnabled(true);
                skipStepBtn.setEnabled(true);
                 advLabel.setText(UIRegistry.getResourceString(ok ? "ADV_DB_OK" : "ADV_DB_ERR"));
            }
        });

        
        createMUBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                createMU();
            }
        });
        progressBar.setVisible(false);
    }
    
    /**
     * @return the Row and Column JGoodies definitions
     */
    protected Pair<String, String> getRowColDefs()
    {
        String rowDef = "p,5px" + (fieldsNames.length > 0 ? ","+createDuplicateJGoodiesDef("p", "2px", fieldsNames.length) : "") + getAdditionalRowDefs() + ",2px,p,2px,p,8px,p";
        return new Pair<String, String>("p,2px,p,f:p:g", rowDef);
    }
    
    /**
     * 
     */
    protected boolean skipDBCreate()
    {
        getValues(properties);
        DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
        
        String dbName     = properties.getProperty("dbName");
        String hostName   = properties.getProperty("hostName");
        
        String saUserName = ((JTextField)comps.get("saUserName")).getText();
        String saPassword = ((JTextField)comps.get("saPassword")).getText();

        
        if (mgr.connect(saUserName, saPassword, hostName, dbName))
        {
            nextBtn.setEnabled(true);
            mgr.close();
            return true;
        }
        return false;
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
        
        if (properties != null)
        {
            String dbUsername = properties.getProperty("dbUserName");
            String saUserName = ((JTextField)comps.get("saUserName")).getText();
            
            if (dbUsername.equals(saUserName))
            {
                label.setText(UIRegistry.getResourceString("DB_SA_USRNAME_MATCH"));
                createMUBtn.setEnabled(false);
                return false;
            }
            createMUBtn.setEnabled(true);
            label.setText("");
        }
        
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
        createMUBtn.setEnabled(enable);
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
            createMUBtn.setVisible(true);
            label.setText(" ");
            properties.put("masterChanged", true);
        }
    }

    /**
     * 
     */
    protected void createMU()
    {
        String saUsrNm = ((JTextField)comps.get("saUserName")).getText();
        if (StringUtils.isNotEmpty(saUsrNm) && saUsrNm.equalsIgnoreCase("root"))
        {
            UIRegistry.showLocalizedError("MASTER_NO_ROOT");
            ((JTextField)comps.get("saUserName")).setText("");
            return;
        }
        
        if (isOK == null || !isOK)
        {
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            
            setUIEnabled(false);
            
            label.setText(UIRegistry.getResourceString("CONN_DB"));
            
            createMUBtn.setVisible(false);
            
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
                            
                            isOK = mgr.createUser(saUserName, saPassword, dbName, DBMSUserMgr.PERM_BASIC);
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
                    
                    createMUBtn.setVisible(!isOK);
                    
                    if (isOK)
                    {
                        setUIEnabled(false);
                        label.setText(UIRegistry.getResourceString("MASTER_CREATED"));
                        
                    } else
                    {
                        label.setText(UIRegistry.getResourceString(errorKey));
                        UIRegistry.showLocalizedError(errorKey);
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

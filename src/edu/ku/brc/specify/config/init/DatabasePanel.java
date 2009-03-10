/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.ui.UIHelper;

/**
 * This is the configuration window for create a new user and new database.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 17, 2008
 *
 */
public class DatabasePanel extends BaseSetupPanel
{
    protected boolean            assumeDerby = false;
    
    protected JTextField         usernameTxt;
    protected JTextField         passwordTxt;
    protected JTextField         dbNameTxt;
    protected JTextField         hostNameTxt;
    protected JComboBox          drivers;
    
    protected Vector<DatabaseDriverInfo> driverList;
    protected boolean                    doSetDefaultValues;
    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public DatabasePanel(final JButton nextBtn, 
                         final boolean doSetDefaultValues)
    {
        super("DATABASE", nextBtn);
        
        this.doSetDefaultValues = doSetDefaultValues;
        
        String header = getResourceString("ENTER_DB_INFO") + ":";

        CellConstraints cc = new CellConstraints();
        
        String rowDef = "p,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 5) + ",p:g";
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", rowDef), this);
        int row = 1;
        
        builder.add(createLabel(header, SwingConstants.CENTER), cc.xywh(1,row,3,1));row += 2;
        
        usernameTxt     = createField(builder, "IT_USERNAME",  true, row);      row += 2;
        passwordTxt     = createField(builder, "IT_PASSWORD",  true, row, true);row += 2;
        dbNameTxt       = createField(builder, "DB_NAME",   true, row);         row += 2;
        hostNameTxt     = createField(builder, "HOST_NAME", true, row);         row += 2;

        driverList  = DatabaseDriverInfo.getDriversList();
        drivers     = createComboBox(driverList);
        
        // Select Derby or MySQL as the default
        drivers.setSelectedItem(DatabaseDriverInfo.getDriver(assumeDerby ? "Derby" : "MySQL"));
        
        JLabel lbl = createI18NFormLabel("DRIVER", SwingConstants.RIGHT);
        lbl.setFont(bold);
        builder.add(lbl,     cc.xy(1, row));
        builder.add(drivers, cc.xy(3, row));
        row += 2;

        updateBtnUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        props.put("dbUserName", usernameTxt.getText());
        props.put("dbPassword", passwordTxt.getText());
        props.put("dbName",     dbNameTxt.getText());
        props.put("hostName",   hostNameTxt.getText());
        props.put("driver",     drivers.getSelectedItem().toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        usernameTxt.setText(values.getProperty("dbUserName"));
        passwordTxt.setText(values.getProperty("dbPassword"));
        dbNameTxt.setText(values.getProperty("dbName"));
        hostNameTxt.setText(values.getProperty("hostName"));
        
        if (doSetDefaultValues)
        {
            drivers.setSelectedIndex(0);
        }
    }

    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public void updateBtnUI()
    {
        boolean isValid = isUIValid();
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isValid);
        }
    }
    
    /**
     * @return
     */
    public boolean isUsingDerby()
    {
        DatabaseDriverInfo database = (DatabaseDriverInfo)drivers.getSelectedItem();
        return database.getDialectClassName().equals("org.hibernate.dialect.DerbyDialect");
    }
    
    /**
     * Checks all the textfeilds to see if they have text
     * @return true of all fields have text
     */
    public boolean isUIValid()
    {
        JTextField[] txtFields = {usernameTxt, passwordTxt, dbNameTxt};
        for (JTextField tf : txtFields)
        {
            if (StringUtils.isEmpty(tf.getText()))
            {
                return false;
            }
        }
        return true;
    }
    
    // Getters 
    
    public DatabaseDriverInfo getDriver()
    {
        return (DatabaseDriverInfo)drivers.getSelectedItem();
    }

    public String getDbName()
    {
        return dbNameTxt.getText();
    }

    public String getPassword()
    {
        return passwordTxt.getText();
    }

    public String getUsername()
    {
        return usernameTxt.getText();
    }
}

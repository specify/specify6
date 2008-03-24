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
import static edu.ku.brc.ui.UIHelper.createLabel;

import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.specify.config.DisciplineType;
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
    protected boolean            doLoginOnly = false;
    protected boolean            assumeDerby = false;
    
    protected JTextField         usernameTxt;
    protected JTextField         passwordTxt;
    protected JTextField         dbNameTxt;
    protected JComboBox          drivers;
    protected JComboBox          disciplines;
    
    protected Vector<DatabaseDriverInfo> driverList;
    protected boolean                    doSetDefaultValues;
    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public DatabasePanel(final JButton nextBtn, final boolean doSetDefaultValues)
    {
        super("Database", nextBtn);
        
        this.doSetDefaultValues = doSetDefaultValues;
        
        String header = "Fill in following information for the database:";

        Vector<DisciplineType> dispList = new Vector<DisciplineType>();
        for (DisciplineType disciplineType : DisciplineType.getDisciplineList())
        {
            if (disciplineType.getType() == 0)
            {
                dispList.add(disciplineType);
            }
        }
        
        driverList  = DatabaseDriverInfo.getDriversList();
        drivers     = createComboBox(driverList);
        disciplines = createComboBox(dispList);
        
        drivers.getModel().addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e)   { adjustLabel(); }
            public void intervalRemoved(ListDataEvent e) { adjustLabel(); }
            public void contentsChanged(ListDataEvent e) { adjustLabel(); }
        });
        
        CellConstraints cc = new CellConstraints();
        
        int numRows = 3 + (doLoginOnly ? 0 : 2);
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", "p:g,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", numRows)+",p:g"), this);
        int row = 1;
        
        builder.add(createLabel(header), cc.xywh(1,row,3,1));row += 2;
        
        usernameTxt     = createField(builder, "Username",      row);row += 2;
        passwordTxt     = createField(builder, "Password",      row, true);row += 2;
        dbNameTxt       = createField(builder, "Database Name", row);row += 2;

        if (!doLoginOnly)
        {
            builder.add(createLabel("DisciplineType:", SwingConstants.RIGHT), cc.xy(1, row));
            builder.add(disciplines, cc.xy(3, row));
            row += 2;
            
            builder.add(createLabel("Driver:", SwingConstants.RIGHT), cc.xy(1, row));
            builder.add(drivers, cc.xy(3, row));
            row += 2;
        }
        
        // Select Derby or MySQL as the default
        drivers.setSelectedItem(DatabaseDriverInfo.getDriver(assumeDerby ? "Derby" : "MySQL"));

        
        // Select Fish as the default
        for (DisciplineType disciplineType : dispList)
        {
            if (disciplineType.getName().equals("fish"))
            {
                disciplines.setSelectedItem(disciplineType);
            }
        }
        
        if (doSetDefaultValues)
        {
            usernameTxt.setText("guest");
            passwordTxt.setText("guest");
            dbNameTxt.setText("WorkBench");  
        }
        
        if (DO_DEBUG) // XXX Debug
        {
            usernameTxt.setText("rods");
            passwordTxt.setText("rods");
            dbNameTxt.setText("WorkBench");
            drivers.setSelectedIndex(0);
        }
        updateBtnUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    protected void getValues(final Properties props)
    {
        props.put(makeName("username"), usernameTxt.getText());
        props.put(makeName("password"), passwordTxt.getText());
        props.put(makeName("dbname"), dbNameTxt.getText());
        props.put(makeName("driver"), drivers.getSelectedItem().toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    protected void setValues(Properties values)
    {
        usernameTxt.setText(values.getProperty(makeName("username")));
        passwordTxt.setText(values.getProperty(makeName("password")));
        dbNameTxt.setText(values.getProperty(makeName("dbname")));
        
        //String driverName = values.get(makeName("driver");
    }

    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    protected void updateBtnUI()
    {
        boolean isValid = isUIValid();
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isValid);
        }
    }
    
    public void adjustLabel()
    {
        if (nextBtn != null)
        {
            //nextBtn.setText(isUsingDerby() ? "Next" : "Finished");
        }
    }
    
    public boolean isUsingDerby()
    {
        DatabaseDriverInfo database = (DatabaseDriverInfo)drivers.getSelectedItem();
        return database.getDialectClassName().equals("org.hibernate.dialect.DerbyDialect");
    }
    
    /**
     * Checks all the textfeilds to see if they have text
     * @return true of all fields have text
     */
    protected boolean isUIValid()
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

    public DisciplineType getDiscipline()
    {
        return (DisciplineType)disciplines.getSelectedItem();
    }
}

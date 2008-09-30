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

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
public class NewAgentPanel extends BaseSetupPanel
{
    protected JTextField firstNameTxt;
    protected JTextField lastNameTxt;
    protected JTextField emailTxt;
    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public NewAgentPanel(final JButton nextBtn)
    {
        super("agent", nextBtn);
        
        CellConstraints cc = new CellConstraints();

        String header = "Fill in your information:";

        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,5px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 3)+",p:g"), this);
        int row = 1;
        
        builder.add(createLabel(header), cc.xywh(1,row,3,1));row += 2;

        firstNameTxt    = createField(builder, "First Name", row);row += 2;
        lastNameTxt     = createField(builder, "Last Name",  row);row += 2;
        emailTxt        = createField(builder, "EMail",      row);row += 2;
        
        if (DO_DEBUG) // XXX Debug
        {
            firstNameTxt.setText("Rod");
            lastNameTxt.setText("Spears");
            emailTxt.setText("rods@ku.edu");
        }
        updateBtnUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues(java.util.Properties)
     */
    @Override
    public void getValues(Properties props)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Checks all the textfeilds to see if they have text
     * @return true of all fields have text
     */
    public void updateBtnUI()
    {
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isUIValid());
        }
    }

    
    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public boolean isUIValid()
    {
        JTextField[] txtFields = {firstNameTxt, lastNameTxt, emailTxt};
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
    public String getEmail()
    {
        return emailTxt.getText();
    }

    public String getFirstName()
    {
        return firstNameTxt.getText();
    }

    public String getLastName()
    {
        return lastNameTxt.getText();
    }
}



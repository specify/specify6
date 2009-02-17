/*
     * Copyright (C) 2009  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.*;

import javax.swing.JButton;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.ui.PasswordStrengthUI;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 16, 2009
 *
 */
public class UserInfoPanel extends GenericFormPanel
{

    /**
     * @param name
     * @param title
     * @param labels
     * @param fields
     * @param nextBtn
     */
    public UserInfoPanel(String name, String title, String[] labels, String[] fields,
            JButton nextBtn)
    {
        super(name, title, labels, fields, nextBtn);
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
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#init(java.lang.String, java.lang.String[], java.lang.String[], boolean[])
     */
    @Override
    protected void init(final String    title, 
                        final String[]  labels, 
                        final String[]  fields, 
                        final boolean[] required)
    {
        super.init(title, labels, fields, required);
        
        CellConstraints cc = new CellConstraints();
        
        PasswordStrengthUI pwdStrength = new PasswordStrengthUI();
        builder.add(createI18NFormLabel("PWDSTRENGTH"), cc.xy(1, row));
        builder.add(pwdStrength, cc.xy(3, row));
        
        JTextField pwdTF = (JTextField)comps.get("usrPassword");
        pwdStrength.setPasswordField(pwdTF, null);
        
    }

    
}

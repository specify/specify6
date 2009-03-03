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

import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.ui.PasswordStrengthUI;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.ui.DocumentAdaptor;

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
    public UserInfoPanel(final String   name, 
                         final String   title, 
                         final String[] labels, 
                         final String[] fields, 
                         final JButton  nextBtn)
    {
        super(name, title, labels, fields, nextBtn, true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#getAdditionalRowDefs()
     */
    @Override
    protected String getAdditionalRowDefs()
    {
        return ",2px,p,2px,p";
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
        builder.add(pwdStrength,                        cc.xyw(3, row, 2)); row += 2;
        
        final JTextField pwdTF = (JTextField)comps.get("usrPassword");
        pwdStrength.setPasswordField(pwdTF, null);
        
        final JTextField encryptedTF = new JTextField(20);
        ViewFactory.changeTextFieldUIForDisplay(encryptedTF, false);
        builder.add(createI18NFormLabel("ENCRYPT_KEY"), cc.xy(1, row));
        builder.add(encryptedTF,                        cc.xyw(3, row, 2));
        
        pwdTF.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                encryptedTF.setText(Encryption.encrypt(pwdTF.getText()));
            }
        });
    }

    
}

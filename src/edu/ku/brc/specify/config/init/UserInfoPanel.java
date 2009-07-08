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

import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import edu.ku.brc.af.ui.PasswordStrengthUI;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
    private JTextField encryptedTF;
    private JLabel     statusLbl;
    
    /**
     * @param name
     * @param title
     * @param labels
     * @param fields
     * @param nextBtn
     */
    public UserInfoPanel(final String   name, 
                         final String   title, 
                         final String   helpContext,
                         final String[] labels, 
                         final String[] fields, 
                         final boolean[] isReq, 
                         final JButton  nextBtn, 
                         final JButton  prevBtn)
    {
        super(name, title, helpContext, labels, fields, isReq, nextBtn, prevBtn, true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#getAdditionalRowDefs()
     */
    @Override
    protected String getAdditionalRowDefs()
    {
        return ",2px,p,20px,p";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#init(java.lang.String, java.lang.String[], java.lang.String[], boolean[])
     */
    @Override
    protected void init(final String    title, 
                        final String[]  fields, 
                        final boolean[] required,
                        final String[] types)
    {
        super.init(title, fields, required, types);
        
        statusLbl = UIHelper.createLabel("");
        
        PasswordStrengthUI pwdStrength = new PasswordStrengthUI();
        builder.add(createI18NFormLabel("PWDSTRENGTH"), cc.xy(1, row));
        builder.add(pwdStrength,                        cc.xyw(3, row, 2)); row += 2;
        builder.add(statusLbl,                           cc.xyw(3, row, 2)); row += 2;
        
        final JTextField pwdTF = (JTextField)comps.get("usrPassword");
        pwdStrength.setPasswordField(pwdTF, null);
        
        encryptedTF = UIHelper.createTextField(20);
        ViewFactory.changeTextFieldUIForDisplay(encryptedTF, false);
        
        pwdTF.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                encryptedTF.setText(Encryption.encrypt(pwdTF.getText()));
            }
        });
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
            String dbUsername  = properties.getProperty("dbUserName");
            String saUserName  = properties.getProperty("saUserName");
            String usrUserName = ((JTextField)comps.get("usrUsername")).getText();
            
            if (usrUserName.equals(saUserName))
            {
                statusLbl.setText(UIRegistry.getResourceString("UR_SA_USRNAME_MATCH"));
                nextBtn.setEnabled(false);
                return false;
                
            }
            
            if (usrUserName.equals(dbUsername))
            {
                statusLbl.setText(UIRegistry.getResourceString("UR_DB_USRNAME_MATCH"));
                nextBtn.setEnabled(false);
                return false;
            }
            
            nextBtn.setEnabled(true);
            statusLbl.setText("");
        }
        
        return isValid;
    }

}

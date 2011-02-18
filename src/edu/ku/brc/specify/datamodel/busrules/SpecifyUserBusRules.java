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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.ui.PasswordStrengthUI;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.EditViewCompSwitcherPanel;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 28, 2008
 *
 */
public class SpecifyUserBusRules extends BaseBusRules
{
    protected char currEcho;
    /**
     * 
     */
    public SpecifyUserBusRules()
    {
        super(SpecifyUser.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        final JPasswordField     pwdTxt       = formViewObj.getCompById("3");
        final JTextField         keyTxt       = formViewObj.getCompById("key");
        final JButton            genBtn       = formViewObj.getCompById("GenerateKey");
        final JButton            copyBtn      = formViewObj.getCompById("CopyToCB");
        final JButton            showPwdBtn   = formViewObj.getCompById("ShowPwd");
        final PasswordStrengthUI pwdStrenthUI = formViewObj.getCompById("6");
        
        // This is in case the BusRules are used without the form.
        if (pwdTxt == null)
        {
            return;
        }
        
        final char echoChar = pwdTxt.getEchoChar();
        currEcho = echoChar;

        
        // For now
        //showPwdBtn.setVisible(false);
        
        if (showPwdBtn != null)
        {
            showPwdBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    currEcho = currEcho == echoChar ? 0 : echoChar;
                    pwdTxt.setEchoChar(currEcho);
                    showPwdBtn.setText(UIRegistry.getResourceString(currEcho == echoChar ? "SHOW_PASSWORD" : "HIDE_PASSWORD"));
                }
            });
        }
        
        
        if (copyBtn != null)
        {
            copyBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    UIHelper.setTextToClipboard(new String(pwdTxt.getPassword()));
                }
            });
        }
        
        if (genBtn != null)
        {
            genBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    String key = createEncryptKey(pwdTxt);
                    if (key != null)
                    {
                        if (keyTxt != null)
                        {
                            keyTxt.setText(key);
                        }
                        UIHelper.setTextToClipboard(key);
                        UIRegistry.showLocalizedMsg("SPUSR_KEYGEN");
                    }
                }
            });
            genBtn.setEnabled(true);
        }
        
        if (pwdStrenthUI == null)
        {
            return;
        }
        
        pwdStrenthUI.setPasswordField(pwdTxt, null);
    }

    /**
     * @return
     */
    protected ValComboBoxFromQuery getAgentCBX()
    {
        if (formViewObj != null)
        {
            Component agentCBX = formViewObj.getControlByName("agent");
            if (agentCBX != null && agentCBX instanceof ValComboBoxFromQuery)
            {
                return (ValComboBoxFromQuery)agentCBX;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (formViewObj != null && formViewObj.getDataObj() instanceof SpecifyUser)
        {
            SpecifyUser spUser  = (SpecifyUser)formViewObj.getDataObj();
            ValComboBoxFromQuery cbx = getAgentCBX();
            if (cbx != null)
            {
                for (Agent agent : spUser.getAgents())
                {
                    //System.err.println(spUser.getName() + "  "+agent.toString()+"  "+agent.getDivision().getName()+"="+currDiv.getName());
                    cbx.setValue(agent, null);
                    break;
                }
            }
        }
    }
    
    /**
     * @param txtFld
     */
    protected String createEncryptKey(final JTextField password)
    {
        String pwdStr = password.getText();
        if (!pwdStr.isEmpty())
        {
            Pair<String, String> usrPwd = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
            Encryption.setEncryptDecryptPassword(pwdStr);
            String key = Encryption.encrypt(usrPwd.first+","+usrPwd.second, pwdStr);
            Encryption.setEncryptDecryptPassword("Specify");
            
            return key;
        }
        return null;
    }
    
    /**
     * NOTE: This is being called when editing an existing person.
     * @param id
     * @param keyName
     * @param isPwd
     * @return
     */
    private boolean isFieldOK(final String id, final String keyName, final boolean isPwd)
    {
        JTextField tf = null;
        Component comp = formViewObj.getCompById(id);
        if (comp instanceof EditViewCompSwitcherPanel)
        {
            tf = (JTextField)((EditViewCompSwitcherPanel)comp).getCurrentComp();
        } else
        {
            tf = (JTextField)comp;
        }
        String value = tf.getText().trim();
        if (StringUtils.contains(value, ' ') || (!isPwd && StringUtils.contains(value, ',')))
        {
            UIRegistry.showLocalizedError(keyName);
            return false;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof SpecifyUser))
        {
            return STATUS.Error;
        }
        
        STATUS nameStatus = isCheckDuplicateNumberOK("name", 
                                                      (FormDataObjIFace)dataObj, 
                                                      SpecifyUser.class, 
                                                      "specifyUserId");
        
        if (isFieldOK("1", "NO_SPC_USRNAME", false))
        {
            return STATUS.Error;
        }
        
        if (isFieldOK("3", "NO_SPC_PWDNAME", true))
        {
            return STATUS.Error;
        }
        
        return nameStatus != STATUS.OK ? STATUS.Error : STATUS.OK;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        beforeMerge(dataObj, session);
        
        if (formViewObj != null)
        {
            SpecifyUser spUser = (SpecifyUser)formViewObj.getDataObj();
            String      pwd    = spUser.getPassword();
            if (pwd.length() < 30)
            {
                spUser.setPassword(Encryption.encrypt(pwd, pwd));
            }
        }
    }
}

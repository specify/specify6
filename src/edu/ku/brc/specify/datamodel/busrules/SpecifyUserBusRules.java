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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.PasswordStrengthUI;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.EditViewCompSwitcherPanel;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.af.ui.forms.validation.ValPasswordField;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Institution;
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
    private static final int PWD_LEN_THRESHOLD = 25; // supposedly anything less than 25 chars is not encrypted
    
    private char    currEcho;
    private String  currentPlainTextPWD = null; // Not encrypted.
    private Integer spUserId            = null;
     
    private ValPasswordField   pwdTxt       = null;
    private JTextField         keyTxt       = null;
    private JButton            showPwdBtn   = null;
    private PasswordStrengthUI pwdStrenthUI = null;

    private JButton            genBtn       = null;
    private JButton            copyBtn      = null;
    
    private int                minPwdLen;
    
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
    public void initialize(final Viewable viewableArg)
    {
        if (viewableArg == null || pwdTxt != null)
        {
            return;
        }
        
        super.initialize(viewableArg);
        
        pwdTxt       = formViewObj.getCompById("3");
        keyTxt       = formViewObj.getCompById("key");
        showPwdBtn   = formViewObj.getCompById("ShowPwd");
        pwdStrenthUI = formViewObj.getCompById("6");
        
        genBtn  = formViewObj.getCompById("GenerateKey");
        copyBtn = formViewObj.getCompById("CopyToCB");
        
        // This is in case the BusRules are used without the form.
        if (pwdTxt == null)
        {
            return;
        }
        
        Institution institution = AppContextMgr.getInstance().getClassObject(Institution.class);
        minPwdLen = (int)institution.getMinimumPwdLength();
        pwdTxt.setMinLen(minPwdLen);
        pwdStrenthUI.setMinPwdLen(minPwdLen);
        
        final char echoChar = pwdTxt.getEchoChar();
        currEcho = echoChar;
        
        pwdTxt.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                super.focusLost(e);
                
                String pwdStr = new String(pwdTxt.getPassword());
                if (StringUtils.isNotEmpty(pwdStr) && pwdStr.length() < PWD_LEN_THRESHOLD)
                {
                    // make sure the password has changed
                    if (currentPlainTextPWD == null || !currentPlainTextPWD.equals(pwdStr))
                    {
                        // this means the password is new
                        currentPlainTextPWD = pwdStr;
                    }
                }
            }
        });
        
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
                    if (StringUtils.isNotEmpty(currentPlainTextPWD) && currentPlainTextPWD.length() > minPwdLen)
                    {
                        String key = createEncryptMasterKey(currentPlainTextPWD);
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
                }
            });
            genBtn.setEnabled(false);
            copyBtn.setEnabled(false);
            
            pwdTxt.getDocument().addDocumentListener(new DocumentAdaptor() {
                @Override
                protected void changed(DocumentEvent e)
                {
                    super.changed(e);
                    
                    char[]  chars  = pwdTxt.getPassword();
                    boolean enable = chars != null && chars.length > minPwdLen;
                    genBtn.setEnabled(enable);
                    copyBtn.setEnabled(enable);
                }
            });
        }
        
        if (pwdStrenthUI == null)
        {
            return;
        }
        
        pwdStrenthUI.setPasswordField(pwdTxt, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean isOkToSave(Object dataObj, DataProviderSessionIFace session)
    {
        return super.isOkToSave(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object, boolean)
     */
    @Override
    public STATUS processBusinessRules(Object parentDataObj,
                                       Object dataObj,
                                       boolean isExistingObject)
    {
        return super.processBusinessRules(parentDataObj, dataObj, isExistingObject);
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
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        SpecifyUser spUser = (SpecifyUser)dataObj;
        
        boolean doReset = true;
        if (spUser != null && spUser.getId() != null)
        {
            if (spUserId != null)
            {
                doReset = !spUserId.equals(spUser.getId());
            } 
            spUserId = spUser.getId();
        }
        
        if (spUser != null && doReset)
        {
            currentPlainTextPWD = null;
            
            if (genBtn != null)
            {
                genBtn.setEnabled(false);
                copyBtn.setEnabled(false);
            }
        }
        
        if (formViewObj != null && formViewObj.getDataObj() instanceof SpecifyUser)
        {
            ValComboBoxFromQuery cbx = getAgentCBX();
            if (cbx != null && spUser != null)
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
     * Encrypt's the Master U/P with a plain text password string.
     * @param pwdStr plain text password string
     */
    protected String createEncryptMasterKey(final String pwdStr)
    {
        String key = null;
        if (!pwdStr.isEmpty())
        {
            String oldPwd = Encryption.getEncryptDecryptPassword();
            try
            {
                Pair<String, String> usrPwd = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
                
                Encryption.setEncryptDecryptPassword(pwdStr);
                key = Encryption.encrypt(usrPwd.first+","+usrPwd.second, pwdStr);
                
            } finally
            {
                Encryption.setEncryptDecryptPassword(oldPwd);
            }
            
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
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        if (formViewObj != null)
        {
            SpecifyUser spUser = (SpecifyUser)formViewObj.getDataObj();
            String      pwd    = spUser.getPassword();
            if (pwd.length() < PWD_LEN_THRESHOLD)
            {
                spUser.setPassword(Encryption.encrypt(pwd, pwd));
            }
        }
    }
}

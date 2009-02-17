/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.ui.PasswordStrengthUI;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.SpecifyUser;
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
        final JButton            showPwdBtn   = formViewObj.getCompById("ShowPwd");
        final PasswordStrengthUI pwdStrenthUI = formViewObj.getCompById("6");
        
        final char echoChar = pwdTxt.getEchoChar();
        currEcho = echoChar;
        
        pwdStrenthUI.setPasswordField(pwdTxt, genBtn);
        
        genBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                createEncryptKey(keyTxt, pwdTxt);
            }
        });
        
        showPwdBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                currEcho = currEcho == echoChar ? 0 : echoChar;
                pwdTxt.setEchoChar(currEcho);
                showPwdBtn.setText(UIRegistry.getResourceString(currEcho == echoChar ? "SHOW_PASSWORD" : "HIDE_PASSWORD"));
            }
        });
        
        // For now
        showPwdBtn.setVisible(false);
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
    protected void createEncryptKey(final JTextField txtFld, 
                                    final JTextField password)
    {
        String pwdStr = password.getText();
        if (!pwdStr.isEmpty())
        {
            Pair<String, String> usrPwd = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
            Encryption.setEncryptDecryptPassword(pwdStr);
            String key = Encryption.encrypt(usrPwd.first+","+usrPwd.second, pwdStr);
            Encryption.setEncryptDecryptPassword("Specify");
            
            txtFld.setText(key);
        }
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
            SpecifyUser spUser  = (SpecifyUser)formViewObj.getDataObj();
            //Division    currDiv = AppContextMgr.getInstance().getClassObject(Division.class);
            
            for (Agent agent : spUser.getAgents())
            {
                //if (agent.getDivision().getId().equals(currDiv.getId()))
                {
                    try
                    {
                        session.saveOrUpdate(agent);
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyUserBusRules.class, ex);
                        ex.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
}

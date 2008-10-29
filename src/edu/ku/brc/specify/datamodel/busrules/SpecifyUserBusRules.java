/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.db.TextFieldWithQuery.QueryWhereClauseProviderIFace;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 28, 2008
 *
 */
public class SpecifyUserBusRules extends BaseBusRules implements QueryWhereClauseProviderIFace
{

    /**
     * 
     */
    public SpecifyUserBusRules()
    {
        super(SpecifyUser.class);
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
            Division    currDiv = AppContextMgr.getInstance().getClassObject(Division.class);
            
            ValComboBoxFromQuery cbx = getAgentCBX();
            if (cbx != null)
            {
                cbx.setQueryWhereClauseProvider(this);
                for (Agent agent : spUser.getAgents())
                {
                    System.err.println(spUser.getName() + "  "+agent.toString());
                    
                    if (agent.getDivision().getId().equals(currDiv.getId()))
                    {
                        cbx.setValue(agent, null);
                        break;
                    }
                }
            }
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
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        SpecifyUser spUser = (SpecifyUser)dataObj;
        
        ValComboBoxFromQuery cbx = getAgentCBX();
        if (cbx != null)
        {
            Agent userAgent = (Agent)cbx.getValue();
            
            spUser.getAgents().add(userAgent);
            userAgent.setSpecifyUser(spUser);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        SpecifyUser spUser  = (SpecifyUser)formViewObj.getDataObj();
        Division    currDiv = AppContextMgr.getInstance().getClassObject(Division.class);
        
        for (Agent agent : spUser.getAgents())
        {
            if (agent.getDivision().getId().equals(currDiv.getId()))
            {
                try
                {
                    session.saveOrUpdate(agent);
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                break;
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.TextFieldWithQuery.QueryWhereClauseProviderIFace#getExtraWhereClause()
     */
    @Override
    public String getExtraWhereClause()
    {
        return "";
    }

}

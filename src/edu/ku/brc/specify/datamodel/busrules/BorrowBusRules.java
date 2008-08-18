/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.util.Hashtable;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Borrow;
import edu.ku.brc.specify.datamodel.BorrowAgent;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 11, 2008
 *
 */
public class BorrowBusRules extends BaseBusRules
{

    public BorrowBusRules()
    {
        super(Borrow.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof Borrow))
        {
            return STATUS.Error;
        }
        
        Borrow borrow = (Borrow)dataObj;
        // Check for BorrowAgent and their Roles (for duplicates)
        Hashtable<String, Boolean> agentRoleHash = new Hashtable<String, Boolean>();
        for (BorrowAgent ba : borrow.getBorrowAgents())
        {
            Agent agent = ba.getAgent();
            if (agent != null)
            {
                String key = agent.getId() + " _ " + ba.getRole();
                if (agentRoleHash.get(key) == null)
                {
                    agentRoleHash.put(key, true);
                } else
                {
                    reasonList.add(UIRegistry.getLocalizedMessage("BORROW_DUP_AGENTROLE", agent.toString(), ba.getRole()));
                    return STATUS.Error;
                }
            }
        }
        return STATUS.Error;
    }

}

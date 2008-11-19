/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.GroupPerson;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 3, 2008
 *
 */
public class GroupPersonBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public GroupPersonBusRules()
    {
        super(GroupPerson.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        // This method needs to be fully implemented in order to use a form for
        // adding GroupPersons instead of just a grid.
        return super.processBusinessRules(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object parentDataObj, final Object dataObj, final boolean isExistingObject)
    {
        reasonList.clear();
        
        // isEdit is false when the data object is new, true when editing an existing object.
        if (isExistingObject &&
            parentDataObj instanceof Agent &&
            dataObj instanceof GroupPerson)
        {
            Agent       agent       = (Agent)parentDataObj;
            GroupPerson groupPerson = (GroupPerson)dataObj;
            
            if (agent.getId() != null && 
                groupPerson.getMember() != null && 
                groupPerson.getMember().getId() != null &&
                agent.getId().equals(groupPerson.getMember().getId()))
            {
                reasonList.add(String.format(getResourceString("GP_SELF_GRPPER"), groupPerson.getIdentityTitle()));
                return STATUS.Error;
            }
            
            for (GroupPerson gp : agent.getGroups())
            {
               if (gp.getMember() != null && 
                   groupPerson.getMember() != null && 
                   gp.getMember().getAgentId().equals(groupPerson.getMember().getAgentId())) 
               {
                   reasonList.add(String.format(getResourceString("GP_DUPLICATE_GRPPER"), groupPerson.getIdentityTitle()));
                   return STATUS.Error;
               }
            }
        }
        
        return super.processBusinessRules(parentDataObj, dataObj, isExistingObject);
    }

}

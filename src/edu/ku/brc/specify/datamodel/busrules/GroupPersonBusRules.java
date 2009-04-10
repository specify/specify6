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

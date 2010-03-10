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

import java.util.Hashtable;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.Division;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 11, 2008
 *
 */
public class CollectorBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public CollectorBusRules()
    {
        super(Collector.class);
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
            parentDataObj instanceof CollectingEvent &&
            dataObj instanceof Collector)
        {
            CollectingEvent ce = (CollectingEvent)parentDataObj;
            Collector       col = (Collector)dataObj;
            
            Hashtable<Integer, Boolean> hash = new Hashtable<Integer, Boolean>();
            for (Collector collector : ce.getCollectors())
            {
                Integer id    = collector.getAgent().getAgentId();
                boolean isBad = false;
                if (hash.get(id) == null)
                {
                    if (collector.getId() != null && id.equals(col.getAgent().getAgentId())) 
                    {
                        isBad = true;
                    }
                    hash.put(id, true);
                } else
                {
                    isBad = true;
                }
                
                if (isBad)
                {
                    reasonList.add(String.format(getResourceString("CE_DUPLICATE_COLLECTORS"), col.getIdentityTitle()));
                    return STATUS.Error;
                }
            }
        }
        
        return super.processBusinessRules(parentDataObj, dataObj, isExistingObject);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        Collector collector = (Collector)dataObj;
        if (collector.getDivision() == null)
        {
            collector.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        Collector collector = (Collector)dataObj;
        if (collector.getDivision() == null)
        {
            collector.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
        }
    }

}

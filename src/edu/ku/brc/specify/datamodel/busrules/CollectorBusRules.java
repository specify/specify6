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

import java.util.Hashtable;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collector;

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

}

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

import javax.swing.JOptionPane;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.BaseBusRules;

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
     * @see edu.ku.brc.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object)
     */
    @Override
    public boolean afterSaveCommit(Object dataObj)
    {
        // TODO Auto-generated method stub
        return super.afterSaveCommit(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        // TODO Auto-generated method stub
        super.beforeSave(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        return super.processBusinessRules(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object parentDataObj, final Object dataObj, final boolean isEdit)
    {
        if (!isEdit &&
            parentDataObj instanceof CollectingEvent &&
            dataObj instanceof Collector)
        {
            CollectingEvent ce = (CollectingEvent)parentDataObj;
            Collector       col = (Collector)dataObj;
            
            for (Collector collector : ce.getCollectors())
            {
               if (collector.getAgent().getAgentId().equals(col.getAgent().getAgentId())) 
               {
                   UIRegistry.showError(String.format(getResourceString("CE_DUPLICATE_COLLECTORS"), col.getIdentityTitle()));
                   return STATUS.Error;
               }
            }
        }
        
        return super.processBusinessRules(parentDataObj, dataObj, isEdit);
    }

}

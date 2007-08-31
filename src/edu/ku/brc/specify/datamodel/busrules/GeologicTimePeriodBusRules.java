/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link GeologicTimePeriod} or
 * {@link GeologicTimePeriodTreeDefItem} objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class GeologicTimePeriodBusRules extends BaseTreeBusRules<GeologicTimePeriod, GeologicTimePeriodTreeDef, GeologicTimePeriodTreeDefItem>
{
    private static final Logger log = Logger.getLogger("edu.ku.brc.specify.datamodel.busrules");
    
    /**
     * Constructor.
     */
    public GeologicTimePeriodBusRules()
    {
        super(GeologicTimePeriod.class,GeologicTimePeriodTreeDefItem.class);
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        return getLocalizedMessage("GEOLOGICAL_TIME_PERIOD_DELETED", ((GeologicTimePeriod)dataObj).getName());
    }

    @Override
    public boolean hasNoConnections(GeologicTimePeriod gtp)
    {
        Integer id = gtp.getTreeId();
        if (id == null)
        {
            return true;
        }
        
        boolean noBSPCs = super.okToDelete("paleocontext",       "BioStratID",     id);
        boolean noCSPCs = super.okToDelete("paleocontext",       "ChronosStratID", id);
        boolean noSyns  = super.okToDelete("geologictimeperiod", "AcceptedID",     id);

        boolean noConns = noBSPCs && noCSPCs && noSyns;
        
        return noConns;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj instanceof GeologicTimePeriod)
        {
            return super.okToDeleteNode((GeologicTimePeriod)dataObj);
        }
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        log.debug("enter");
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof GeologicTimePeriod)
        {
            GeologicTimePeriod gtp = (GeologicTimePeriod)dataObj;
            beforeSaveGeologicTimePeriod(gtp);
            
            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(gtp, session);

            log.debug("exit");
            return;
        }
        
        if (dataObj instanceof GeologicTimePeriodTreeDefItem)
        {
            beforeSaveGeologicTimePeriodTreeDefItem((GeologicTimePeriodTreeDefItem)dataObj);
            log.debug("exit");
            return;
        }
        log.debug("exit");
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link GeologicTimePeriod}.  The real work of this method is to
     * update the 'fullname' field of all {@link GeologicTimePeriod} objects effected by the changes
     * to the passed in {@link GeologicTimePeriod}.
     * 
     * @param gtp the {@link GeologicTimePeriod} being saved
     */
    protected void beforeSaveGeologicTimePeriod(GeologicTimePeriod gtp)
    {
        // nothing specific to GeologicTimePeriod
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link GeologicTimePeriodTreeDefItem}.  The real work of this method is to
     * update the 'fullname' field of all {@link GeologicTimePeriod} objects effected by the changes
     * to the passed in {@link GeologicTimePeriodTreeDefItem}.
     *
     * @param defItem the {@link GeologicTimePeriodTreeDefItem} being saved
     */
    protected void beforeSaveGeologicTimePeriodTreeDefItem(GeologicTimePeriodTreeDefItem defItem)
    {
        // This is a LONG process for some trees.  I wouldn't recommend doing it.  Can
        // we set these options before shipping the DB, then not let them change it ever again?
        // Or perhaps they can't change it if there are records at this level.
        
        log.warn("TODO: need to make a decision here");
        return;

        //super.beforeSaveTreeDefItem(defItem);
    }
}

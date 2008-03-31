/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
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
    
    //private static final Logger log = Logger.getLogger(GeologicTimePeriodBusRules.class);
    
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
    public String[] getRelatedTableAndColumnNames()
    {
        String[] relationships = 
        {
                "geologictimeperiod", "AcceptedID",
                "paleocontext", "BioStratID",
                "paleocontext", "ChronosStratID"
        };

        return relationships;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        if (dataObj instanceof GeologicTimePeriod)
        {
            return super.okToDeleteNode((GeologicTimePeriod)dataObj);
        }
        else if (dataObj instanceof GeologicTimePeriodTreeDefItem)
        {
            return okToDeleteDefItem((GeologicTimePeriodTreeDefItem)dataObj);
        }
        
        return false;
    }
    
    /**
     * Handles the {@link #okToEnableDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link GeologicTimePeriodTreeDefItem}.
     * 
     * @param defItem the {@link GeologicTimePeriodTreeDefItem} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteDefItem(GeologicTimePeriodTreeDefItem defItem)
    {
        // never let the root level be deleted
        if (defItem.getRankId() == 0)
        {
            return false;
        }
        
        // don't let 'used' levels be deleted
        if (!okToDelete("geologictimeperiod", "GeologicTimePeriodTreeDefItemID", defItem.getId()))
        {
            return false;
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof GeologicTimePeriod)
        {
            GeologicTimePeriod gtp = (GeologicTimePeriod)dataObj;
            beforeSaveGeologicTimePeriod(gtp);
            
            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(gtp, session);

            return;
        }
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link GeologicTimePeriod}.  The real work of this method is to
     * update the 'fullname' field of all {@link GeologicTimePeriod} objects effected by the changes
     * to the passed in {@link GeologicTimePeriod}.
     * 
     * @param gtp the {@link GeologicTimePeriod} being saved
     */
    protected void beforeSaveGeologicTimePeriod(@SuppressWarnings("unused") GeologicTimePeriod gtp)
    {
        // nothing specific to GeologicTimePeriod
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSaveCommit(java.lang.Object)
     */
    /*@SuppressWarnings("unchecked")
    @Override
    public boolean beforeSaveCommit(final Object dataObj, final DataProviderSessionIFace session) throws Exception
    {
        boolean success = true;
        
        // compare the dataObj values to the nodeBeforeSave values to determine if a node was moved or added
        if (dataObj instanceof Treeable)
        {
            // NOTE: the instanceof check can't check against 'T' since T isn't a class
            //       this has a SMALL amount of risk to it
            GeologicTimePeriod node = (GeologicTimePeriod)dataObj;
            
            if (!node.getDefinition().getNodeNumbersAreUpToDate() && !node.getDefinition().isUploadInProgress())
            {
                //Scary. If nodes are not up to date, tree rules may not work.
                //The application should prevent edits to items/trees whose tree numbers are not up to date except while uploading
                //workbenches.
                throw new RuntimeException(node.getDefinition().getName() + " has out of date node numbers.");
            }
            
            // if the node doesn't have any assigned node number, it must be new
            boolean added = (node.getNodeNumber() == null);

            if (added && node.getDefinition().getDoNodeNumberUpdates() && node.getDefinition().getNodeNumbersAreUpToDate())
            {
                log.info("Saved tree node was added.  Updating node numbers appropriately.");
                TreeDataService dataServ = TreeDataServiceFactory.createService();
                
                // This is quite bad that we need to start up a new session here
                // because if fails updating the nodes then we don't get to roll everything back
                DataProviderSessionIFace tempInnerSession = DataProviderFactory.getInstance().createSession();
                try
                {
                    success = dataServ.updateNodeNumbersAfterNodeAddition(node, tempInnerSession);
                } catch (Exception ex)
                {
                    
                } finally 
                {
                    tempInnerSession.close();  
                }
            }
            else if (added)
            {
                node.getDefinition().setNodeNumbersAreUpToDate(false);
            }
        }
        
        return success;
    }*/
}

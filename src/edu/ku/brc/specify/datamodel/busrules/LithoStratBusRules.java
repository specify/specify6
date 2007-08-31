package edu.ku.brc.specify.datamodel.busrules;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;

public class LithoStratBusRules extends BaseTreeBusRules<LithoStrat, LithoStratTreeDef, LithoStratTreeDefItem>
{
    private static final Logger log = Logger.getLogger("edu.ku.brc.specify.datamodel.busrules");
    
    public LithoStratBusRules()
    {
        super(LithoStrat.class,LithoStratTreeDefItem.class);
    }
    
    @Override
    public boolean hasNoConnections(LithoStrat ls)
    {
        Integer id = ls.getTreeId();
        if (id == null)
        {
            return true;
        }
        
        boolean noPCs  = super.okToDelete("paleocontext", "LithoStratID", id);
        boolean noSyns = super.okToDelete("lithostrat",   "AcceptedID",   id);

        boolean noConns = noPCs && noSyns;
        
        return noConns;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj instanceof LithoStrat)
        {
            return super.okToDeleteNode((LithoStrat)dataObj);
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
        
        if (dataObj instanceof LithoStrat)
        {
            LithoStrat ls = (LithoStrat)dataObj;
            beforeSaveLithoStrat(ls);
            
            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(ls, session);

            log.debug("exit");
            return;
        }
        
        if (dataObj instanceof LithoStratTreeDefItem)
        {
            beforeSaveLithoStratTreeDefItem((LithoStratTreeDefItem)dataObj);
            log.debug("exit");
            return;
        }
        log.debug("exit");
    }

    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link LithoStrat}.  The real work of this method is to
     * update the 'fullname' field of all {@link LithoStrat} objects effected by the changes
     * to the passed in {@link LithoStrat}.
     * 
     * @param geo the {@link LithoStrat} being saved
     */
    protected void beforeSaveLithoStrat(LithoStrat geo)
    {
        // nothing specific to LithoStrat
    }

    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link LithoStratTreeDefItem}.  The real work of this method is to
     * update the 'fullname' field of all {@link LithoStrat} objects effected by the changes
     * to the passed in {@link LithoStratTreeDefItem}.
     *
     * @param defItem the {@link LithoStratTreeDefItem} being saved
     */
    protected void beforeSaveLithoStratTreeDefItem(LithoStratTreeDefItem defItem)
    {
        // This is a LONG process for some trees.  I wouldn't recommend doing it.  Can
        // we set these options before shipping the DB, then not let them change it ever again?
        // Or perhaps they can't change it if there are records at this level.
        
        log.warn("TODO: need to make a decision here");
        return;

        //super.beforeSaveTreeDefItem(defItem);
    }
}

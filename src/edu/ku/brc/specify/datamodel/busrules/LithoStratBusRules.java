package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;

public class LithoStratBusRules extends BaseTreeBusRules<LithoStrat, LithoStratTreeDef, LithoStratTreeDefItem>
{
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
        else if (dataObj instanceof LithoStratTreeDefItem)
        {
            return okToDeleteDefItem((LithoStratTreeDefItem)dataObj);
        }
        
        return false;
    }
    
    /**
     * Handles the {@link #okToDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link LithoStratTreeDefItem}.
     * 
     * @param defItem the {@link LithoStratTreeDefItem} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteDefItem(LithoStratTreeDefItem defItem)
    {
        // never let the root level be deleted
        if (defItem.getRankId() == 0)
        {
            return false;
        }
        
        // don't let 'used' levels be deleted
        if (!okToDelete("lithostrat", "LithoStratTreeDefItemID", defItem.getId()))
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
        
        if (dataObj instanceof LithoStrat)
        {
            LithoStrat ls = (LithoStrat)dataObj;
            beforeSaveLithoStrat(ls);
            
            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(ls, session);

            return;
        }
    }

    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link LithoStrat}.  The real work of this method is to
     * update the 'fullname' field of all {@link LithoStrat} objects effected by the changes
     * to the passed in {@link LithoStrat}.
     * 
     * @param geo the {@link LithoStrat} being saved
     */
    protected void beforeSaveLithoStrat(@SuppressWarnings("unused") LithoStrat geo)
    {
        // nothing specific to LithoStrat
    }
}

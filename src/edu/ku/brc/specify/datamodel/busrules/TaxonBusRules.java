/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Taxon} or
 * {@link TaxonTreeDefItem} objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class TaxonBusRules extends BaseTreeBusRules<Taxon, TaxonTreeDef, TaxonTreeDefItem>
{
    AttachmentOwnerBaseBusRules attachOwnerRules;
    
    /**
     * Constructor.
     */
    public TaxonBusRules()
    {
        super(Taxon.class,TaxonTreeDefItem.class);
        
        attachOwnerRules = new AttachmentOwnerBaseBusRules()
        {
            @Override
            public boolean okToEnableDelete(Object dataObj)
            {
                return false;
            }
        };
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        return getLocalizedMessage("TAXON_DELETED", ((Taxon)dataObj).getName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        if (dataObj instanceof Taxon)
        {
            return super.okToDeleteNode((Taxon)dataObj);
        }
        else if (dataObj instanceof TaxonTreeDefItem)
        {
            return okToDeleteDefItem((TaxonTreeDefItem)dataObj);
        }
        
        return false;
    }
    
    /**
     * Handles the {@link #okToEnableDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link TaxonTreeDefItem}.
     * 
     * @param defItem the {@link TaxonTreeDefItem} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteDefItem(TaxonTreeDefItem defItem)
    {
        // never let the root level be deleted
        if (defItem.getRankId() == 0)
        {
            return false;
        }
        
        // don't let 'used' levels be deleted
        if (!okToDelete("taxon", "TaxonTreeDefItemID", defItem.getId()))
        {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean hasNoConnections(Taxon taxon)
    {
        Integer id = taxon.getTreeId();
        if (id == null)
        {
            return true;
        }
        
        boolean noDeters = super.okToDelete("determination", "TaxonID",         id);
        boolean noCites  = super.okToDelete("taxoncitation", "TaxonID",         id);
        boolean noHyb1   = super.okToDelete("taxon",         "HybridParent1ID", id);
        boolean noHyb2   = super.okToDelete("taxon",         "HybridParent2ID", id);
        boolean noSyns   = super.okToDelete("taxon",         "AcceptedID",      id);

        boolean noConns = noDeters && noCites && noHyb1 && noHyb2 && noSyns;
        
        return noConns;
    }
    
    public boolean okToDeleteTaxon(Taxon taxon)
    {
        Integer id = taxon.getId();
        if (id == null)
        {
            return true;
        }
        
        boolean noDeters = super.okToDelete("determination", "TaxonID",         id);
        boolean noCites  = super.okToDelete("taxoncitation", "TaxonID",         id);
        boolean noHyb1   = super.okToDelete("taxon",         "HybridParent1ID", id);
        boolean noHyb2   = super.okToDelete("taxon",         "HybridParent2ID", id);
        boolean noSyns   = super.okToDelete("taxon",         "AcceptedID",      id);

        boolean okSoFar = noDeters && noCites && noHyb1 && noHyb2 && noSyns;
        
        if (okSoFar)
        {
            // now check the children

            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            Taxon tmpT = session.load(Taxon.class, id);

            for (Taxon child: tmpT.getChildren())
            {
                if (!okToDeleteTaxon(child))
                {
                    // this child can't be deleted
                    // stop right here
                    okSoFar = false;
                    break;
                }
            }
            session.close();
        }
        
        return okSoFar;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        // make sure to handle all of the attachment stuff
        attachOwnerRules.beforeSave(dataObj, session);
        
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof Taxon)
        {
            Taxon taxon = (Taxon)dataObj;
            beforeSaveTaxon(taxon, session);

            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(taxon, session);
            
            return;
        }
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Taxon}.  The real work of this method is to
     * update the 'fullname' field of all {@link Taxon} objects effected by the changes
     * to the passed in {@link Taxon}.
     * 
     * @param taxon the {@link Taxon} being saved
     */
    protected void beforeSaveTaxon(Taxon taxon, @SuppressWarnings("unused") DataProviderSessionIFace session)
    {
        // if this node is "accepted" then make sure it doesn't point to an accepted parent
        if (taxon.getIsAccepted() == null || taxon.getIsAccepted().booleanValue() == true)
        {
            taxon.setAcceptedTaxon(null);
        }
        
        // if this node isn't a hybrid then make sure it doesn't point at hybrid "parents"
        if (taxon.getIsHybrid() == null || taxon.getIsHybrid().booleanValue() == false)
        {
            taxon.setHybridParent1(null);
            taxon.setHybridParent2(null);
        }
    }

    @Override
    public boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        // make sure to handle all of the attachment stuff
        boolean retVal = attachOwnerRules.beforeDeleteCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        retVal = super.beforeDeleteCommit(dataObj, session);
        return retVal;
    }

    @Override
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        // make sure to handle all of the attachment stuff
        boolean retVal = attachOwnerRules.beforeSaveCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        retVal = super.beforeSaveCommit(dataObj, session);
        return retVal;
    }
}

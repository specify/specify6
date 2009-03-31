/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Taxon;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 19, 2008
 *
 */
public class TreeAdditionalProcFactory
{
    protected static final Logger log = Logger.getLogger(TreeAdditionalProcFactory.class);
    
    protected static final TreeAdditionalProcFactory instance = new TreeAdditionalProcFactory();
    
    
    public static TreeAdditionalProcFactory getInstance() 
    {
        return instance;
    }
    
    /**
     * 
     */
    private TreeAdditionalProcFactory()
    {
        //nothing to do
    }
    
    public TreeAdditionalProcessing createProcessor(final Class<?> implClass)
    {
        if (implClass == Taxon.class)
        {
            return new TaxonAdditionalProcessing();
        }
        return null;
    }
    
    public interface TreeAdditionalProcessing
    {
        public abstract boolean process(Session session, Object source, Object dest);
    }
    
    
    private class TaxonAdditionalProcessing implements TreeAdditionalProcessing
    {
        public TaxonAdditionalProcessing()
        {
            //empty block
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.specify.treeutils.TreeAdditionalProcFactory.TreeAdditionalProcessing#process(org.hibernate.Session, java.lang.Object, java.lang.Object)
         */
        public boolean process(Session session, Object source, Object dest)
        {
            if (session == null || source == null ||
                !(source instanceof Taxon) || (dest != null && !(dest instanceof Taxon)))
            {
                return false; // not sure whether to return true or false
            }
            
            Taxon srcTaxon = (Taxon)source;
            Taxon dstTaxon = (Taxon)dest;
            
            if (dstTaxon != null)
            {
                return processTaxonSynonymy(session, srcTaxon, dstTaxon);
            }
            
            return processTaxonUnSynonymy(session, srcTaxon);
            
        }

        /**
         * Adds Determinations to the Accepted taxon for each existing Determination to the synonymized taxon.
         * Records actions in SpTaxonSynonymy and SpSynonymyDetermination tables.
         * 
         * @param session
         * @param srcTaxon
         * @param dstTaxon
         * @return true if successfully processed.
         */
        protected boolean processTaxonSynonymy(Session session, final Taxon srcTaxon, final Taxon dstTaxon)
        {
            Vector<Taxon> sources = new Vector<Taxon>();
            sources.add(srcTaxon);
            sources.addAll(srcTaxon.getAcceptedChildren());
        	log.debug("Source:");
            for (Taxon srcTax : sources)
            {
            	for (Determination det : new Vector<Determination>(srcTax.getDeterminations()))
            	{
            		log.debug(det.getCollectionObject().getIdentityTitle()+" has  "+det.getIdentityTitle()+ "  "+det.getTaxon().getIdentityTitle()+" "+det.getCollectionObject().getCollectionMemberId());
            		det.setPreferredTaxon(dstTaxon);
            		session.saveOrUpdate(det);
            	}
            }
            return true;
        }

        /**
         * Removes Determinations created during the synonymization of srcTaxon, and updates status
         * of old determinations to srcTaxon if necessary. Clears record of the synonymization of
         * srcTaxon from the SpTaxonSynonymy and SpSynonymyDetermination tables.
         * 
         * 
         * @param session
         * @param srcTaxon
         * @return
         */
        protected boolean processTaxonUnSynonymy(Session session, final Taxon srcTaxon)
        {
            for (Determination det : new Vector<Determination>(srcTaxon.getDeterminations()))
            {
                det.setPreferredTaxon(srcTaxon);
                session.saveOrUpdate(det);
            }
            return true;
        }
    }
    
}

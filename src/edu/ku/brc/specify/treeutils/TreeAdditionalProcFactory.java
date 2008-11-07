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
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.SpSynonymyDetermination;
import edu.ku.brc.specify.datamodel.SpTaxonSynonymy;
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
        
        
//        @SuppressWarnings("unchecked")
//        private DeterminationStatus getDeterminationStatus(final Session session, final Byte type)
//        {
//            Criteria criteria = session.createCriteria(DeterminationStatus.class);
//            criteria.add(Restrictions.eq("type", type));
//            List<DeterminationStatus> list = criteria.list();
//            return list == null || list.size() == 0 ? null : list.get(0);
//        }


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
            //Create and save Synonymy record
            SpTaxonSynonymy syn = new SpTaxonSynonymy();
            syn.initialize();
            syn.setTaxonTreeDef(srcTaxon.getDefinition());
            syn.setAccepted(dstTaxon);
            syn.setNotAccepted(srcTaxon);
            syn.setSynonymizer(Agent.getUserAgent());
            session.saveOrUpdate(syn); //assuming at this point that caller has opened a transaction on session???
            
            try
            {
                log.debug("Source:");
                for (Determination det : new Vector<Determination>(srcTaxon.getDeterminations()))
                {
                    log.debug(det.getCollectionObject().getIdentityTitle()+" has  "+det.getIdentityTitle()+ "  "+det.getTaxon().getIdentityTitle()+" "+det.getCollectionObject().getCollectionMemberId());
                }
                
                log.debug("Source:");
                for (Determination det : new Vector<Determination>(srcTaxon.getDeterminations()))
                {
                    if (det.isCurrentDet())
                    {
                        log.debug(det.getIdentityTitle()+"  "+det.getCollectionObject().getIdentityTitle());
                        
                        // Create a new 'current' determination
                        // and add it to the Source Taxon
                        Determination newDet = (Determination)det.clone();
                        newDet.setTaxon(dstTaxon);
                        newDet.setIsCurrent(true);

                        // Set DeterminationStatus from Current to Old
                        det.setIsCurrent(false);
                        
                        log.debug(newDet.getCollectionObject().getIdentityTitle()+" has new  "+newDet.getIdentityTitle()+ "  "+newDet.getTaxon().getIdentityTitle());
                        log.debug(det.getCollectionObject().getIdentityTitle()+" has OLD  "+det.getIdentityTitle()+ "  "+det.getTaxon().getIdentityTitle());

                        //create Record of Determination changes
                        SpSynonymyDetermination synDet = new SpSynonymyDetermination();
                        synDet.initialize();
                        synDet.setTaxonSynonymy(syn);
                        synDet.setNewDetermination(newDet);
                        synDet.setOldDetermination(det);
                        
                        session.saveOrUpdate(newDet);
                        session.saveOrUpdate(det);
                        session.saveOrUpdate(dstTaxon);
                        session.saveOrUpdate(srcTaxon);
                        session.saveOrUpdate(synDet);
                        session.saveOrUpdate(syn); //hibernate requires this here and for dstTaxon and srcTaxon???
                    }
//                    else if (det.getStatus().getType().equals(DeterminationStatus.CURRENTTOACCEPTED))
//                    {
//                        //srcTaxon has synonyms. Don't create a 'chain':                        
//                        //Just replace references to srcTaxon with references to dstTaxon.
//                        
//                        det.setTaxon(dstTaxon);
//                        session.saveOrUpdate(det);
//                        
//                        Set<SpSynonymyDetermination> newDets = det.getNewSynonymyDeterminations();
//                        if (newDets != null && newDets.size() == 1)
//                        {
//                            SpSynonymyDetermination synDet = newDets.iterator().next();
//                            synDet.getTaxonSynonymy().setAccepted(dstTaxon);
//                            session.saveOrUpdate(synDet);
//                        }
//                        else
//                        {
//                            log.error("'Current Accepted' determination for " +det.getCollectionObject().getIdentityTitle() + " has invalid newSynonymyDeterminations." );
//                        }
//                    }
                }
                
                return true;
                
                /*log.debug("Destination:");
                for (Determination det : dstTaxon.getDeterminations())
                {
                    log.debug(det.getIdentityTitle());
                }*/
                
            } catch (CloneNotSupportedException ex)
            {
                log.error(ex);
            }
            
            return false;
        }

        /**
         * Removes Determinations created during the synonymization of srcTaxon, and updates status of old determinations
         * to srcTaxon if necessary.
         * Clears record of the synonymization of srcTaxon from the SpTaxonSynonymy and SpSynonymyDetermination tables.
         * 
         * 
         * @param session
         * @param srcTaxon
         * @return
         */
        protected boolean processTaxonUnSynonymy(Session session, final Taxon srcTaxon)
        {
            Query q = session.createQuery("from SpTaxonSynonymy where notAcceptedId = " + srcTaxon.getTaxonId());
            SpTaxonSynonymy synRec = null;
            try
            {
                //XXX update SpTaxonSynonymy so NotAccepted is unique.
                synRec = (SpTaxonSynonymy )q.uniqueResult();
            }
            catch (HibernateException ex)
            {
                //either no record exists or something else is wrong. Either way there is nothing more to do.
                log.error("error Un-synonymizing " + srcTaxon.getFullName() + ": " + ex);
                return false; 
            }
            
            if (synRec != null)
            {
                for (SpSynonymyDetermination synDet : new Vector<SpSynonymyDetermination>(synRec.getDeterminations()))
                {
                    log.debug(synDet.getNewDetermination() + " <- " + synDet.getOldDetermination());
                    Determination newDet = synDet.getNewDetermination();
                    Determination oldDet = synDet.getOldDetermination();
                    
                    //Easiest case: newDet exists and isCurrent and has not been  edited since creation
                    //             and oldDet exists and isOld and has not been edited since creation.
                    // (Currently, Status control (if a combobox) is disabled for 'synonymy' determinations)
                    
                    //XXX check for edits. Should edits be prevented???
                    //XXX check for existence??? What happens if user deletes a determination? Should/Is that prevented?
                    //(Currently deletes are prevented)
                    
                    if (newDet != null && newDet.isCurrentDet()
                            && oldDet != null && !oldDet.isCurrentDet())
                    {
                        synRec.removeDetermination(synDet);
                        session.delete(synDet);
                        oldDet.setIsCurrent(true);
                        session.delete(newDet);
                        session.saveOrUpdate(oldDet);
                    }
                    
                }
                session.delete(synRec);
                return true;
            }
            return false;
        }
    }
    
}

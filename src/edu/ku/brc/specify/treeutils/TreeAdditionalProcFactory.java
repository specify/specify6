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

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
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
    private static final Logger log = Logger.getLogger(TreeAdditionalProcFactory.class);
    
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
            
        }
        
        
        @SuppressWarnings("unchecked")
        private DeterminationStatus getDeterminationStatus(final Session session, final Byte type)
        {
            Criteria criteria = session.createCriteria(DeterminationStatus.class);
            criteria.add(Restrictions.eq("type", type));
            List<DeterminationStatus> list = (List<DeterminationStatus>)criteria.list();
            return list == null || list.size() == 0 ? null : list.get(0);
        }


        /* (non-Javadoc)
         * @see edu.ku.brc.specify.treeutils.TreeAdditionalProcFactory.TreeAdditionalProcessing#process(org.hibernate.Session, java.lang.Object, java.lang.Object)
         */
        public boolean process(Session session, Object source, Object dest)
        {
            if (session == null || source == null || dest == null ||
                !(source instanceof Taxon) || !(dest instanceof Taxon))
            {
                return false; // not sure whether to return true or false
            }
            
            Taxon srcTaxon = (Taxon)source;
            Taxon dstTaxon = (Taxon)dest;
            
            DeterminationStatus detStatusOld = getDeterminationStatus(session, DeterminationStatus.OLDDETERMINATION);
            DeterminationStatus detStatusCur = getDeterminationStatus(session, DeterminationStatus.CURRENT);
            
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
                    if (det.getStatus().getDeterminationStatusId().equals(detStatusCur.getDeterminationStatusId()))
                    {
                        log.debug(det.getIdentityTitle()+"  "+det.getCollectionObject().getIdentityTitle());
                        
                        // Create a new 'current' determination
                        // and add it to the Source Taxon
                        Determination newDet = (Determination)det.clone();
                        newDet.setTaxon(dstTaxon);

                        // Set DeterminationStatus from Current to Old
                        det.setStatus(detStatusOld);
                        
                        log.debug(newDet.getCollectionObject().getIdentityTitle()+" has new  "+newDet.getIdentityTitle()+ "  "+newDet.getTaxon().getIdentityTitle());
                        log.debug(det.getCollectionObject().getIdentityTitle()+" has OLD  "+det.getIdentityTitle()+ "  "+det.getTaxon().getIdentityTitle());

                        session.saveOrUpdate(newDet);
                        session.saveOrUpdate(det);
                        session.saveOrUpdate(dstTaxon);
                        session.saveOrUpdate(srcTaxon);
                    }
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
        
    }
}

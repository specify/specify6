/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.ui.db.TreeFinderFactory;

/**
 * A {@link TreeFinderFactoryImpl} is a Specify-centric implementation
 * of {@link TreeFinderFactory}.
 *
 * @code_status Beta
 * @author jstewart
 */
public class TreeFinderFactoryImpl extends TreeFinderFactory
{
    /** The data service that handles all data access. */
    protected TreeDataService<?,?,?> dataServ;
    
    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    public TreeFinderFactoryImpl()
    {
        dataServ = new HibernateTreeDataServiceImpl();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.TreeFinderFactory#findTreeDefinition(java.lang.String)
     */
    @Override
    public TreeDefIface<?, ?, ?> findTreeDefinition(String type)
    {
        // ask the type-specific method to do all of the work
        if (type.equalsIgnoreCase("taxon"))
        {
            return findTaxonTreeDef();
        }
        else if (type.equalsIgnoreCase("geography"))
        {
            return findGeographyTreeDef();
        }
        else if (type.equalsIgnoreCase("geologictimeperiod"))
        {
            return findGeologicTimePeriodTreeDef();
        }
        else if (type.equalsIgnoreCase("location"))
        {
            return findLocationTreeDef();
        }
        return null;
    }
    
    /**
     * Finds the 'first' taxonomy tree associated with the current {@link Collection}.
     * 
     * @return a {@link TaxonTreeDef}
     */
    protected TaxonTreeDef findTaxonTreeDef()
    {
        List<TaxonTreeDef> defs = new Vector<TaxonTreeDef>();
        
        Collection cs = Collection.getCurrentCollection();
        if (cs != null)
        {
            CollectionObjDef cod = cs.getCollectionObjDef();
            if (cod != null)
            {
                TaxonTreeDef def = cod.getTaxonTreeDef();
                if (def != null)
                {
                    defs.add(def);
                }
            }
        }
        
        // TODO: what do I do if there are more than 1
        return defs.get(0);
    }
    
    /**
     * Finds the 'first' geography tree associated with the current {@link Collection}.
     * 
     * @return a {@link GeographyTreeDef}
     */
    protected GeographyTreeDef findGeographyTreeDef()
    {
        List<GeographyTreeDef> defs = new Vector<GeographyTreeDef>();
        
        Collection cs = Collection.getCurrentCollection();
        if (cs != null)
        {
            CollectionObjDef cod = cs.getCollectionObjDef();
            if (cod != null)
            {
                GeographyTreeDef def = cod.getGeographyTreeDef();
                if (def!=null)
                {
                    defs.add(def);
                }
            }
        }
        
        // TODO: what do I do if there are more than 1
        return defs.get(0);
    }
    
    /**
     * Finds the 'first' geologic time period tree associated with the current {@link Collection}.
     * 
     * @return a {@link GeologicTimePeriodTreeDef}
     */
    protected GeologicTimePeriodTreeDef findGeologicTimePeriodTreeDef()
    {
        List<GeologicTimePeriodTreeDef> defs = new Vector<GeologicTimePeriodTreeDef>();
        
        Collection cs = Collection.getCurrentCollection();
        if (cs != null)
        {
            CollectionObjDef cod = cs.getCollectionObjDef();
            if (cod != null)
            {
                GeologicTimePeriodTreeDef def = cod.getGeologicTimePeriodTreeDef();
                if (def!=null)
                {
                    defs.add(def);
                }
            }
        }
        
        // TODO: what do I do if there are more than 1
        return defs.get(0);
    }
    
    /**
     * Finds the 'first' location tree associated with the current {@link Collection}.
     * 
     * @return a {@link LocationTreeDef}
     */
    protected LocationTreeDef findLocationTreeDef()
    {
        List<LocationTreeDef> defs = new Vector<LocationTreeDef>();
        
        Collection cs = Collection.getCurrentCollection();
        if (cs != null)
        {
            CollectionObjDef cod = cs.getCollectionObjDef();
            if (cod != null)
            {
                LocationTreeDef def = cod.getLocationTreeDef();
                if (def!=null)
                {
                    defs.add(def);
                }
            }
        }
        
        // TODO: what do I do if there are more than 1
        return defs.get(0);
    }
}

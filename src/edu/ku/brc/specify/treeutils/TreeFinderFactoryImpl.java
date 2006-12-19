/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.ui.db.TreeFinderFactory;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class TreeFinderFactoryImpl extends TreeFinderFactory
{
    protected TreeDataService<?,?,?> dataServ;
    
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
    
    protected TaxonTreeDef findTaxonTreeDef()
    {
        List<TaxonTreeDef> defs = new Vector<TaxonTreeDef>();
        
        for (CatalogSeries cs: CatalogSeries.getCurrentCatalogSeries())
        {
            for (CollectionObjDef cod: cs.getCollectionObjDefItems())
            {
                TaxonTreeDef def = cod.getTaxonTreeDef();
                if (def!=null)
                {
                    defs.add(def);
                }
            }
        }
        
        // TODO: what do I do if there are more than 1
        return defs.get(0);
    }
    
    protected GeographyTreeDef findGeographyTreeDef()
    {
        List<GeographyTreeDef> defs = new Vector<GeographyTreeDef>();
        
        for (CatalogSeries cs: CatalogSeries.getCurrentCatalogSeries())
        {
            for (CollectionObjDef cod: cs.getCollectionObjDefItems())
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
    
    protected GeologicTimePeriodTreeDef findGeologicTimePeriodTreeDef()
    {
        List<GeologicTimePeriodTreeDef> defs = new Vector<GeologicTimePeriodTreeDef>();
        
        for (CatalogSeries cs: CatalogSeries.getCurrentCatalogSeries())
        {
            for (CollectionObjDef cod: cs.getCollectionObjDefItems())
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
    
    protected LocationTreeDef findLocationTreeDef()
    {
        List<LocationTreeDef> defs = new Vector<LocationTreeDef>();
        
        for (CatalogSeries cs: CatalogSeries.getCurrentCatalogSeries())
        {
            for (CollectionObjDef cod: cs.getCollectionObjDefItems())
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

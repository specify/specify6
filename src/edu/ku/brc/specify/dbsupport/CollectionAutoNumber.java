/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.AutoNumberGeneric;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.util.Pair;

/**
 * Note: 'getHighestObject' from the base class never gets called. This class' getHighestObject gets called directly from
 * the owning object which is CatalogNumberUIFieldFormatter. This only 
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jun 20, 2007
 *
 */
public class CollectionAutoNumber extends AutoNumberGeneric
{
    /**
     * Default Constructor. 
     */
    public CollectionAutoNumber()
    {
        super();
        
        classObj  = CollectionObject.class;
        fieldName = "catalogNumber";
    }

    /**
     * Constructor with args.
     * @param properties the args
     */
    public CollectionAutoNumber(final Properties properties)
    {
        super(properties);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.AutoNumberGeneric#getHighestObject(org.hibernate.Session, java.lang.String, edu.ku.brc.util.Pair, edu.ku.brc.util.Pair)
     */
    @Override
    protected Object getHighestObject(final Session session,
                                      final String value,
                                      final Pair<Integer, Integer> yearPos,
                                      final Pair<Integer, Integer> pos) throws Exception
    {
        boolean doDebug = false;
        
        AutoNumberingScheme cns = AppContextMgr.getInstance().getClassObject(Collection.class).getNumberingSchemesByType(CollectionObject.getClassTableId());
        
        cns = (AutoNumberingScheme)session.merge(cns);
        
        if (doDebug) System.out.println("CatNumScheme: "+cns.getSchemeName());
        
        Vector<Integer> ids = new Vector<Integer>();
        for (Collection collection : cns.getCollections())
        {
            if (doDebug) System.out.println("adding ID: "+collection.getCollectionId()+"  "+collection.getCollectionName());
            ids.add(collection.getCollectionId());
        }
        
        // XXX (Needs try block)
        Criteria criteria = session.createCriteria(classObj);
        criteria.addOrder( Order.desc(fieldName) );
        criteria.createCriteria("collection").add(Restrictions.in("collectionId", ids));
        criteria.setMaxResults(1);
        if (doDebug) System.out.println("Criteria ID: "+criteria.toString());
        List<?> list = criteria.list();
        if (list.size() == 1)
        {
            if (doDebug) System.out.println("Mac Obj: "+list.get(0));
            return list.get(0);
        }
        return null;
    }
    
}

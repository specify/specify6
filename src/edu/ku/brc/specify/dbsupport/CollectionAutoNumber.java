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

import edu.ku.brc.dbsupport.AutoNumberGeneric;
import edu.ku.brc.specify.datamodel.CatalogNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;

/**
 * @author rod
 *
 * @code_status Alpha
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
     * @see edu.ku.brc.dbsupport.AutoNumberGeneric#getHighestObject(org.hibernate.Session)
     */
    protected Object getHighestObject(final Session session) throws Exception
    {
        CatalogNumberingScheme cns = Collection.getCurrentCollection().getCatalogNumberingScheme();
        
        Vector<Long> ids = new Vector<Long>();
        for (Collection collection : cns.getCollections())
        {
            ids.add(collection.getCollectionId());
        }
        
        Criteria criteria = session.createCriteria(classObj);
        criteria.addOrder( Order.desc(fieldName) );
        criteria.createCriteria("collection").add(Restrictions.in("collectionId", ids));
        criteria.setMaxResults(1);
        List list = criteria.list();
        if (list.size() == 1)
        {
            return list.get(0);
        }
        return null;
    }
    
}

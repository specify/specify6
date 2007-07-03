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

    public CollectionAutoNumber()
    {
        super();
    }

    public CollectionAutoNumber(final Properties properties)
    {
        super(properties);
    }
    
    protected Object getHighestObject(final Session session) throws Exception
    {
        Vector<Long> ids = new Vector<Long>();
        ids.add(1L);
        
        Criteria criteria = session.createCriteria(classObj);
        criteria.addOrder( Order.desc(fieldName) );
        criteria.createCriteria("collection").add(Restrictions.in("collectionId", ids));
        //System.out.println(c2.toString());
        //System.out.println(criteria.toString());
        criteria.setMaxResults(1);
        List list = criteria.list();
        if (list.size() == 1)
        {
            return list.get(0);
        }
        return null;
    }
    
}

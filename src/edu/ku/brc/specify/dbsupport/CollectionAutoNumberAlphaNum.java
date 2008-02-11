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

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import edu.ku.brc.dbsupport.AutoNumberGeneric;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 8, 2007
 *
 */
public class CollectionAutoNumberAlphaNum extends AutoNumberGeneric
{
    /**
     * Default Constructor. 
     */
    public CollectionAutoNumberAlphaNum()
    {
        super();
        
        classObj  = CollectionObject.class;
        fieldName = "catalogNumber";
    }

    /**
     * Constructor with args.
     * @param properties the args
     */
    public CollectionAutoNumberAlphaNum(final Properties properties)
    {
        super(properties);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AutoNumberGeneric#getHighestObject(org.hibernate.Session, java.lang.String, edu.ku.brc.util.Pair, edu.ku.brc.util.Pair)
     */
    @Override
    protected Object getHighestObject(final Session session, 
                                      final String  value,
                                      final Pair<Integer, Integer> yearPos, 
                                      final Pair<Integer, Integer> pos) throws Exception
    {
        Collection currCollection = Collection.getCurrentCollection();
        
        Integer yearVal = null;
        if (yearPos != null && StringUtils.isNotEmpty(value) && value.length() >= yearPos.second)
        {
            yearVal = extractIntegerValue(yearPos, value);
        }

        StringBuilder sb = new StringBuilder(" From CollectionObject c Join c.collection col Join col.catalogNumberingScheme cns WHERE cns.catalogNumberingSchemeId = "+currCollection.getCatalogNumberingScheme().getCatalogNumberingSchemeId());
        if (yearVal != null)
        {
            sb.append(" AND ");
            sb.append(yearVal);
            sb.append(" = substring("+fieldName+","+(yearPos.first+1)+","+yearPos.second+")");
        }
        sb.append(" ORDER BY");
        
        try
        {
            if (yearPos != null)
            {
                sb.append(" substring("+fieldName+","+(yearPos.first+1)+","+yearPos.second+") desc");
                
            }
            
            if (pos != null)
            {
                if (yearPos != null)
                {
                    sb.append(", ");
                }
                sb.append(" substring("+fieldName+","+(pos.first+1)+","+pos.second+") desc");
            }
            
            System.out.println(sb.toString());
            List<?> list = session.createQuery(sb.toString()).setMaxResults(1).list();
            if (list.size() == 1)
            {
                Object[] objArray = (Object[]) list.get(0);
                return objArray[0];
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}

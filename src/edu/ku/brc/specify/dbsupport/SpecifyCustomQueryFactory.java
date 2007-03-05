/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.dbsupport;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.CustomQuery;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.specify.dbsupport.customqueries.CustomStatQueries;

/**
 * Factory for creating Custom Specify Queries.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 2, 2007
 *
 */
public class SpecifyCustomQueryFactory extends CustomQueryFactory
{
    private static final Logger  log = Logger.getLogger(SpecifyCustomQueryFactory.class);
    
    /**
     * Constructor.
     */
    public SpecifyCustomQueryFactory()
    {
        // no-op
    }

    /**
     * Returns the singleton instance.
     * @return the singleton instance.
     */
    public static SpecifyCustomQueryFactory getInstance()
    {
        return (SpecifyCustomQueryFactory)CustomQueryFactory.getInstance();
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryFactory#getQuery(java.lang.String)
     */
    @Override
    public CustomQuery getQuery(final String queryName)
    {
        if (queryName.equals("CatalogedLast7Days"))
        {
            return new CustomStatQueries(CustomStatQueries.Type.CatalogedLast7Days);
            
        } else if (queryName.equals("CatalogedLast30Days"))
        {
            return new CustomStatQueries(CustomStatQueries.Type.CatalogedLast30Days);
            
        } else if (queryName.equals("CatalogedLastYear"))
        {
            return new CustomStatQueries(CustomStatQueries.Type.CatalogedLastYear);
            
        } else 
        {
            try
            {
                return (CustomQuery)Class.forName("edu.ku.brc.specify.dbsupport.customqueries." + queryName).newInstance();
                
            } catch(Exception ex)
            {
                log.error(ex);
            }
        }
        return null;
    }

}

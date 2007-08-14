/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.af.core;

import java.security.AccessController;

import org.apache.commons.lang.StringUtils;

/**
 * This is a singleton factory for adjusting the SQL before it is executed by the Express Search.
 * 
 * This class enables the definitions of the SQl for ES to have undefined values that can be filled in
 * right before it's execution. This enables them to be user dependent queries and a user ID can be inserted.
 * 
 * @code_status Beta
 *
 * @author rods
 *
 */
public class ExpressSearchSQLAdjuster
{
    public static final String factoryName = "edu.ku.brc.af.core.ExpressSearchSQLAdjuster";
    
    //private static final Logger log = Logger.getLogger(ExpressSearchSQLAdjuster.class);
    
    protected static ExpressSearchSQLAdjuster instance = null;
    
    /**
     * Protected Constructor
     */
    protected ExpressSearchSQLAdjuster()
    {
        // no-op
    }

    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static ExpressSearchSQLAdjuster getInstance()
    {
        if (instance != null)
        {
            return instance;
        }
        
        // else
        String factoryNameStr = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (StringUtils.isNotEmpty(factoryNameStr)) 
        {
            try 
            {
                return instance = (ExpressSearchSQLAdjuster)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate ExpressSearchSQLAdjuster factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        }
        // if not factory than pass an instance of this in
        // and this does nothing to the SQL.
        return instance = new ExpressSearchSQLAdjuster();
    }

    /**
     * Provides an opportunity for the SQL to get adjusted before it is executed.
     * @param sql the incoming sql
     * @return the adjusted SQL.
     */
    public String adjustSQL(final String sql)
    {
        return sql;
    }
    
    /**
     * Provides an opportunity for the SQL to get adjusted before it is executed.
     * @param text the incoming text to be passed into the parser
     * @return the adjusted text
     */
    public String adjustExpressSearchText(final String text)
    {
        return text;
    }
}

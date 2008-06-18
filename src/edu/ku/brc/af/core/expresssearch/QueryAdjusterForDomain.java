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
package edu.ku.brc.af.core.expresssearch;

import static org.apache.commons.lang.StringUtils.contains;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.security.AccessController;

import edu.ku.brc.dbsupport.DBTableInfo;

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
public class QueryAdjusterForDomain
{
    public static final String factoryName = "edu.ku.brc.af.core.ExpressSearchSQLAdjuster"; //$NON-NLS-1$
    
    //private static final Logger log = Logger.getLogger(ExpressSearchSQLAdjuster.class);
    
    protected static QueryAdjusterForDomain instance = null;
    
    /**
     * Protected Constructor
     */
    protected QueryAdjusterForDomain()
    {
        // no-op
    }

    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static QueryAdjusterForDomain getInstance()
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
            
        if (isNotEmpty(factoryNameStr)) 
        {
            try 
            {
                return instance = (QueryAdjusterForDomain)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate ExpressSearchSQLAdjuster factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        // if not factory than pass an instance of this in
        // and this does nothing to the SQL.
        return instance = new QueryAdjusterForDomain();
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
     * Returns any special columns that must be compared against for this table.
     * @param tableInfo the table in question
     * @param isHQL whether to generate HQL or SQL
     * @return null or a string
     */
    public String getSpecialColumns(final DBTableInfo tableInfo, final boolean isHQL)
    {
        return getSpecialColumns(tableInfo, isHQL, false, null);
    }

    /**
     * @param tableInfo
     * @param isHQL
     * @param tblAlias
     * @param isLeftJoin
     * @return
     */
    public String getSpecialColumns(final DBTableInfo tableInfo, final boolean isHQL, final boolean isLeftJoin, final String tblAlias)
    {
        return null;
    }
    
    /**
     * @param tableInfo
     * @param isHQL
     * @param alias
     * @param useLeftJoin
     * @return
     */
    public String getJoinClause(final DBTableInfo tableInfo, final boolean isHQL, final String alias, final boolean useLeftJoin)
    {
        return null;
    }
    
    /**
     * Checks to make sure the user isn't trying to type in some SQL 
     * to get at some tables they shouldn't.
     * 
     * @param userInputStr user entered string
     * @return true is ok, false if problematic
     */
    public boolean isUserInputNotInjectable(final String userInputStr)
    {
        if (isNotEmpty(userInputStr))
        {
            if (contains(userInputStr, ";") || //$NON-NLS-1$
                contains(userInputStr.toLowerCase(), "select") || //$NON-NLS-1$
                contains(userInputStr.toLowerCase(), "'") || //$NON-NLS-1$
                (contains(userInputStr.toLowerCase(), "drop") && contains(userInputStr.toLowerCase(), "table"))) //$NON-NLS-1$ //$NON-NLS-2$
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Provides an opportunity for the SQL to get adjusted before it is executed.
     * @param tableAbbrev some queries may have multiple CollectionMembers. This is used as
     * a tablePrefix (i.e. col.CollectionMemberID
     * @param sql the incoming sql
     * @param isHQL whether the query is HQL and it should use the field name instead of the Column name
     * @return the adjusted SQL.
     */
    /*public String adjustSQL(final String tablePrefix, final String sql, final boolean isHQL)
    {
        return sql;
    }*/

}

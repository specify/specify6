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

package edu.ku.brc.dbsupport;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * A class that conatains a collection of QueryResultsDatObjects and a single SQL statement to be executed.
 * The container will be asked for it's SQL and the statment will be executed on a thread. Once the results
 * are back this clas is asked to process the results and fill the collection of QueryResultsDataObjects.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class QueryResultsContainer
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(QueryResultsContainer.class);
    
    // Data Members
    protected String                      sql;
    protected Vector<QueryResultsDataObj> qrdos = new Vector<QueryResultsDataObj>();
    
    /**
     * Default constuctor
     */
    public QueryResultsContainer()
    {
        
    }
    
    /**
     * Constructs it with the SQL statment to be executed
     * @param sql the SQL statement to be executed
     */
    public QueryResultsContainer(final String sql)
    {
        this.sql  = sql;
    }
    
    /**
     * Returns a list of QueryResultsDataObj objects
     * @return Returns a list of QueryResultsDataObj objects
     */
    public List<QueryResultsDataObj> getQueryResultsDataObjs()
    {
        return qrdos;
    }

    /**
     * Adds a QueryResultsDataObj to be processed
     * @param qrdo
     */
    public void add(final QueryResultsDataObj qrdo)
    {
        qrdos.addElement(qrdo);
    }
    
    /**
     * Process the SQL's Result Set and fill the QueryResultsDataObj with the results
     * @param resultSet the SQL result set to be processes
     */
    protected void processResultSet(final java.sql.ResultSet resultSet)
    {
        try
        {
            resultSet.first();
            int prvRow = 1;
            for (QueryResultsDataObj qrdo : qrdos) 
            {
                if (qrdo.isProcessable())
                {
                    int col = qrdo.getCol();
                    int row = qrdo.getRow();
                    if (row-1 == prvRow)
                    {
                        resultSet.next();
                    } else if (row != prvRow) 
                    {
                        resultSet.absolute(row);
                    }
                    qrdo.setResult(resultSet.getObject(col)); // XXX Clone ???
                }
            }
        } catch (Exception ex)
        {
            log.error(ex);
        }
    }

    /**
     * Returns the SQL string that is or was executed
     * @return Returns the SQL string that is or was executed
     */
    public String getSql()
    {
        return sql;
    }

    /**
     * Sets the SQL string to be executed
     * @param sql the string of SQL
     */
    public void setSql(String sql)
    {
        this.sql = sql;
    }
    
    /**
     * Clears all the data structures
     *
     */
    public void clear()
    {
        for (QueryResultsDataObj qrdo : qrdos) 
        {
            qrdo.clear();
        }
        qrdos.clear();
    }
    
}

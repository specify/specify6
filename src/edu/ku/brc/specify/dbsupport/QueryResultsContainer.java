/* Filename:    $RCSfile: QueryResultsContainer.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.dbsupport;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryResultsContainer
{
    // Static Data Members
    private static Log log = LogFactory.getLog(QueryResultsContainer.class);
    
    // Data Members
    protected String                      sql;
    protected Vector<QueryResultsDataObj> qrdos = new Vector<QueryResultsDataObj>();
    
    /**
     * 
     * 
     */
    public QueryResultsContainer()
    {
    }
    
    /**
     * 
     * @param name
     * @param sql
     */
    public QueryResultsContainer(final String sql)
    {
        this.sql  = sql;
    }
    
    public List<QueryResultsDataObj> getQueryResultsDataObjs()
    {
        return qrdos;
    }

    public void add(final QueryResultsDataObj qrdo)
    {
        qrdos.addElement(qrdo);
    }
    
    /**
     * 
     * @param resultSet
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

    public String getSql()
    {
        return sql;
    }

    public void setSql(String sql)
    {
        this.sql = sql;
    }
    
    public void clear()
    {
        for (QueryResultsDataObj qrdo : qrdos) 
        {
            qrdo.clear();
        }
        qrdos.clear();
    }
    
}

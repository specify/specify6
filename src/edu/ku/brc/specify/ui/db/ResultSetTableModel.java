/*
 * Filename:    $RCSfile: ResultSetTableModel.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:55 $
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
package edu.ku.brc.specify.ui.db;


import javax.swing.*;
import javax.swing.table.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.*;
import java.sql.Statement;
import java.util.*;

public class ResultSetTableModel extends AbstractTableModel
{
    private static Log log = LogFactory.getLog(SQLQueryPane.class);

    private ResultSet         resultSet  = null;
    private ResultSetMetaData metaData   = null;
    private Vector<Class>     classNames = new Vector<Class>();
    private int               currentRow = 0;   
    private int               numRows    = 0;   
    
    /**
     * 
     * @param aRS
     */
    public ResultSetTableModel(ResultSet aRS)
    {
        resultSet = aRS;
        try
        {
            if (resultSet != null)
            {
                metaData = resultSet.getMetaData();
                for (int i=1;i<=metaData.getColumnCount();i++)
                {
                    System.out.println(i);
                    System.out.println(metaData.getColumnClassName(i));
                    
                    classNames.addElement(Class.forName(metaData.getColumnClassName(i)));
                }
                
                if (resultSet.last())
                {
                    numRows = resultSet.getRow();
                }
                resultSet.first();
                currentRow = 1;
            }
        } catch (SQLException ex)
        {
            log.error("In constructor of ResultSetTableModel", ex);
        }
        catch (Exception ex)
        {
            log.error("In constructor of ResultSetTableModel", ex);
        }
    }
    
    /**
     * Returns the number of columns
     * @return Number of columns
     */
    public int getColumnCount()
    {
        try
        {
            return metaData == null ? 0 : metaData.getColumnCount();
        } catch (SQLException ex)
        {
            log.error("In getColumnCount", ex);
        }
        return 0;
    }

    /**
     * Returns the Class object for a column
     * @param aColumn the column in question
     * @return the Class of the column
     */
    public Class getColumnClass(int aColumn)
    {
        return (Class)classNames.elementAt(aColumn);
    }

    /**
     * Get the column name
     * @param aColumn the column of the cell to be gotten
     */
    public String getColumnName(int aColumn)
    {
        try
        {
            return metaData.getColumnName(aColumn+1);
            
        } catch (SQLException ex)
        {
            return "N/A";
        }
    }
     
    /**
     * Gets the value of the row col
     * @param aRow the row of the cell to be gotten
     * @param aColumn the column of the cell to be gotten
     */
    public Object getValueAt(int aRow, int aColumn)
    {
        aColumn++;
        aRow++;
        
        if (resultSet == null) return null;
        try
        {
            if (aRow == 1)
            {
                if (!resultSet.first())
                {
                    log.error("Error doing resultSet.first");
                    return null;
                }
                currentRow = 1;
            } else
            {
                if (currentRow+1 == aRow)
                {
                    if (!resultSet.next())
                    {
                        log.error("Error doing resultSet.next");
                        return null;
                    }
                    currentRow++;
                } else
                {
                    if (!resultSet.absolute(aRow))
                    {
                        log.error("Error doing resultSet.absolute("+aRow+")");
                        return null;
                    }
                    currentRow = aRow;
                }
            }
            
            try
            {
                return resultSet.getObject(aColumn);
                
            } catch (Exception e)
            {
                throw new RuntimeException("Error doing resultSet.getObject("+aColumn+")");
            }
        } catch (SQLException ex)
        {
            log.error("getValueAt", ex);
        }
        
        return null;
    }

    /**
     * Sets a new value into the Model
     * @param aValue the value to be set
     * @param aRow the row of the cell to be set
     * @param aColumn the column of the cell to be set
     */
    public void setValueAt(Object aValue, int aRow, int aColumn)
    {

    }

    /**
     * Returns the number of rows
     * @return Number of rows
     */
    public int getRowCount()
    {
      return numRows;
    }

}

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

import edu.ku.brc.specify.core.subpane.SQLQueryPane;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.specify.helpers.*;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class ResultSetTableModel extends AbstractTableModel
{
    // Static Data Members
    private static Log log = LogFactory.getLog(ResultSetTableModel.class);

    // Data Members
    protected ResultSet         resultSet  = null;
    protected ResultSetMetaData metaData   = null;
    protected Vector<Class>     classNames = new Vector<Class>();
    protected int               currentRow = 0;   
    protected int               numRows    = 0;
    
    /**
     * Construct with a ResultSet
     * @param resultsSet the recordset
     */
    public ResultSetTableModel(ResultSet resultSet)
    {
        if (this.resultSet != null)
        {
            try
            {
                this.resultSet.close();
            } catch (SQLException ex)
            {
                log.error(ex);
            }
        }
        
        this.resultSet = resultSet;
        try
        {
            if (this.resultSet != null)
            {
                metaData = this.resultSet.getMetaData();
                for (int i=1;i<=metaData.getColumnCount();i++)
                {
                     classNames.addElement(Class.forName(metaData.getColumnClassName(i)));
                }
                
                if (this.resultSet.last())
                {
                    numRows = this.resultSet.getRow();
                }
                this.resultSet.first();
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
     * Returns the ResultSet
     * @return Returns the ResultSet
     */
    public ResultSet getResultSet()
    {
        return resultSet;
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
     * @param column the column in question
     * @return the Class of the column
     */
    public Class getColumnClass(int column)
    {
        return classNames.size() == 0 ? String.class : (Class)classNames.elementAt(column);
    }

    /**
     * Get the column name
     * @param column the column of the cell to be gotten
     */
    public String getColumnName(int column)
    {
        if (metaData == null)
        {
            return "N/A";
        }
        
        try
        {
            return metaData.getColumnName(column+1);
            
        } catch (SQLException ex)
        {
            return "N/A";
        }
    }

    /**
     * Gets the value of the row col
     * @param row the row of the cell to be gotten
     * @param column the column of the cell to be gotten
     */
    public Object getValueAt(int row, int column)
    {
        column++;
        
        if (resultSet == null) return null;
        
        try
        {
            row++;
        
            if (row == 1)
            {
                if (!resultSet.first())
                {
                    log.error("Error doing resultSet.first");
                    return null;
                }
                currentRow = 1;
            } else
            {
                if (currentRow+1 == row)
                {
                    if (!resultSet.next())
                    {
                        log.error("Error doing resultSet.next");
                        return null;
                    }
                    currentRow++;
                } else
                {
                    if (!resultSet.absolute(row))
                    {
                        log.error("Error doing resultSet.absolute("+row+")");
                        return null;
                    }
                    currentRow = row;
                }
            }
            return resultSet.getObject(column);
            
        } catch (SQLException ex)
        {
            log.error("getValueAt", ex);
        }
        
        return null;
    }

    /**
     * Sets a new value into the Model
     * @param aValue the value to be set
     * @param row the row of the cell to be set
     * @param column the column of the cell to be set
     */
    public void setValueAt(Object aValue, int row, int column)
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
    
    /**
     * Clears all the data from the model
     *
     */
    public void clear()
    {
        if (resultSet != null)
        {
            try
            {
                resultSet.close();
            } catch (SQLException ex)
            {
                log.error(ex);
            }
            resultSet  = null;
        }
        
        metaData   = null;
        classNames.clear();
        
        currentRow = 0;   
        numRows    = 0;
    }
    
    /**
     * Returns a RecordSet object from the table
      * @param rows
     * @param column
     * @return
     */
    public RecordSet getRecordSet(final int[] rows, final int column)
    {
        try
        {
            RecordSet rs = new RecordSet();
            
            if (!resultSet.first())
            {
                log.error("Error doing resultSet.first");
                return null;
            }
            
            Set<RecordSetItem> items = new HashSet<RecordSetItem>();
            rs.setItems(items);
            if (rows == null)
            {
                do
                {                   
                    RecordSetItem rsi = new RecordSetItem();
                    rsi.setRecordId(resultSet.getObject(column+1).toString());
                    items.add(rsi);
                } while (resultSet.next());
                
                return rs;
        
            } else
            {
                /*for (int i=0;i<rows.length;i++)
                {
                    if (!resultSet.absolute(rows[row]))
                    {
                        RecordSetItem rsi = new RecordSetItem();
                        obj = resultSet.getObject(column);
                        rsi.setRecordId(UIHelper.getInt(obj));
                        set.add(rsi);
                    }
                }*/
                
            }
        } catch (Exception ex)
        {
            log.error(ex);
        }
        return null;
    }
    
}

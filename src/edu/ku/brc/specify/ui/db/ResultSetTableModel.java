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

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class ResultSetTableModel extends AbstractTableModel
{
    private static Log log = LogFactory.getLog(ResultSetTableModel.class);

    private ResultSet         resultSet  = null;
    private ResultSetMetaData metaData   = null;
    private Vector<Class>     classNames = new Vector<Class>();
    private int               currentRow = 0;   
    private int               numRows    = 0;
    private Vector<Integer>   displayIndexes = null;
    
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
            if (displayIndexes != null)
            {
                if (!resultSet.absolute(displayIndexes.elementAt(row)+1))
                {
                    log.error("Error doing resultSet.absolute("+row+")");
                    return null;
                }
               
            } else
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
      return displayIndexes != null ? displayIndexes.size() : numRows;
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
     * Initializes the display index data structure
     *
     */
    public void initializeDisplayIndexes()
    {
        if (displayIndexes == null)
        {
            displayIndexes = new Vector<Integer>();
        } else
        {
            displayIndexes.clear();
        }
    }
    /**
     * Append an index to the items being displayed
     * @param index the index to be added
     */
    public void addDisplayIndex(final int index)
    {
        if (displayIndexes != null)
        {
            displayIndexes.add(index);
        }
    }
    
    /**
     * Sets the display indexes to display only a portion of the recordset
     * @param indexes the array of indexes
     */
    public void addDisplayIndexes(int[] indexes)
    {
        if (displayIndexes != null)
        {
            
            Hashtable<Integer, Integer> hash = new Hashtable<Integer, Integer>();
            for (Integer inx : displayIndexes)
            {
                hash.put(inx, inx);
            }

            for (int i=0;i<indexes.length;i++)
            {
                if (hash.get(indexes[i]) == null)
                {
                    displayIndexes.add(indexes[i]);
                }
            }
            hash.clear();
            Collections.sort(displayIndexes);
            
            this.fireTableDataChanged();
        }
    }
}

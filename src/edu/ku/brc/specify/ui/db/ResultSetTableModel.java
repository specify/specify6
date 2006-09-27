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
package edu.ku.brc.specify.ui.db;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;

/*
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ResultSetTableModel extends AbstractTableModel
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ResultSetTableModel.class);

    // Data Members
    protected ResultSet         resultSet  = null;
    protected ResultSetMetaData metaData   = null;
    protected Vector<Class>     classNames = new Vector<Class>();
    protected int               currentRow = 0;
    protected int               numRows    = 0;

    /**
     * Construct with a ResultSet
     * @param resultSet the recordset
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

                if (this.resultSet.first())
                {
                    /*do
                    {
                        System.out.println(this.resultSet.getString(1));
                    } while(this.resultSet.next());
                    */
                } else
                {
                    return;
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
    public Class<?> getColumnClass(int column)
    {
        return classNames.size() == 0 ? (Class)String.class : (Class)classNames.elementAt(column);
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
     * Gets the value of the row col.
     * @param rowArg the row of the cell to be gotten
     * @param colArg the column of the cell to be gotten
     */
    public Object getValueAt(final int rowArg, final int colArg)
    {
        int column = colArg + 1;
        int row    = rowArg;
        
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
     * @param rows the selected rows
     * @param column the col that contains the ID
     * @param returnAll indicates whether all the records should be returned if nothing was selected
     * @return Returns a RecordSet object from the table
     */
    public RecordSet getRecordSet(final int[] rows, final int column, final boolean returnAll)
    {
        RecordSet rs = new RecordSet();

        Set<RecordSetItem> items = new HashSet<RecordSetItem>();
        rs.setItems(items);

        // return if now rows are selected
        if (!returnAll && (rows == null || rows.length == 0))
        {
            return rs;
        }

        try
        {
            if (!resultSet.first())
            {
                log.error("Error doing resultSet.first");
                return null;
            }

            if (rows == null)
            {
                do
                {
                    RecordSetItem rsi = new RecordSetItem(resultSet.getLong(column+1));
                    items.add(rsi);
                } while (resultSet.next());

                return rs;

            } else
            {
                for (int i=0;i<rows.length;i++)
                {
                    if (resultSet.absolute(rows[i]))
                    {
                        RecordSetItem rsi = new RecordSetItem(resultSet.getLong(column+1));
                        items.add(rsi);
                    }
                }

            }
        } catch (Exception ex)
        {
            log.error(ex);
        }
        return null;
    }

}

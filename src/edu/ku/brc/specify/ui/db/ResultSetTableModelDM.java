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
import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;

/**
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ResultSetTableModelDM extends ResultSetTableModel
{
    protected DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");

    // Static Data Members
    private static final Logger log = Logger.getLogger(ResultSetTableModelDM.class);
   
    private int[] displayRowIndexes = null;
    
    /**
     * Construct with a ResultSet
     * @param resultSet the recordset
     */
    public ResultSetTableModelDM(final ResultSet resultSet)
    {
        super(resultSet);
        
    }
    
    /**
     * Returns the number of columns
     * @return Number of columns
     */
    public int getColumnCount()
    {
        if (captionInfo != null)
        {
            return captionInfo.length;
        }
        try
        {
            return metaData == null ? 0 : (captionInfo != null ? captionInfo.length : metaData.getColumnCount());
            
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
        if (classNames.size() == 0)
        {
            return String.class; 
        }
        
        if (captionInfo != null)
        { 
            return classNames.elementAt(captionInfo[column].getPosIndex()); 
        }
        
        return classNames.elementAt(column);
    }

    /**
     * Get the column name
     * @param column the column of the cell to be gotten
     */
    public String getColumnName(int column)
    {
        if (captionInfo != null)
        {
            return captionInfo[column].getColName();
        }
        
        if (metaData == null)
        {
            return "N/A";
        }
        
        try
        {
            return captionInfo != null ? metaData.getColumnName(captionInfo[column].getPosIndex()+1) : 
                                         metaData.getColumnName(column+1);          
        } catch (SQLException ex)
        {
            return "N/A";
        }
    }
    
    /**
     * Returns the number of rows
     * @return Number of rows
     */
    public int getRowCount()
    {
      return displayRowIndexes != null ? displayRowIndexes.length : numRows;
    }
    
    /**
     * Gets the value of the row col
     * @param rowArg the row of the cell to be gotten
     * @param column the column of the cell to be gotten
     */
    public Object getValueAt(int rowArg, int colArg)
    {
        int row    = rowArg;
        int column = colArg;
        
        if (resultSet == null) return null;
        
        try
        {
            if (displayRowIndexes != null)
            {
                if (!resultSet.absolute(displayRowIndexes[row]+1))
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
            
            Object data    = resultSet.getObject(captionInfo[column].getPosIndex()+1);
            String fmtName = captionInfo[colArg].getFormatter();
            if (fmtName != null)
            {
                UIFieldFormatterIFace formatter = UIFieldFormatterMgr.getFormatter(fmtName);
                if (formatter != null && formatter.isInBoundFormatter())
                {
                    return formatter.formatInBound(data);
                }
                log.error("Couldn't find UIFieldFormatterIFace ["+fmtName+"] or doesn't support In Bound formatting. InBnd["+formatter != null ? formatter.isInBoundFormatter() : "???"+"]");
            }
            return data;
            
        } catch (SQLException ex)
        {
            log.error("getValueAt", ex);
            ex.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Initializes the display index data structure
     *
     */
    public void initializeDisplayIndexes()
    {
        displayRowIndexes    = null;
    }
 
    /**
     * Sets the display indexes to display only a portion of the recordset
     * @param indexes the array of indexes
     */
    public void addDisplayIndexes(int[] indexes)
    {
        if (indexes != null)
        {
            displayRowIndexes = indexes;
            this.fireTableDataChanged();
        }
    }
   
    /**
     * Returns a RecordSet object from the table
     * @param rows the selected rows
     * @param column the col that contains the ID
     * @param returnAll indicates whether all the records should be returned if nothing was selected
     * @return Returns a RecordSet object from the table
     */
    public RecordSetIFace getRecordSet(final int[] rows, final int column, final boolean returnAll)
    {
        try
        {
            RecordSet rs = new RecordSet();
            rs.initialize();
            
            if (rows == null || rows.length == 0)
            {
                /*if (displayIndexes != null)
                {
                    for (int i=0;i<displayIndexes.length;i++)
                    {
                        if (resultSet.absolute(displayIndexes[i]+1))
                        {
                            RecordSetItem rsi = new RecordSetItem();
                            rsi.setRecordId(resultSet.getObject(column+1).toString());
                            items.add(rsi);
                        }
                    }
                } else
                {*/
                
                if (returnAll)
                {
                    if (!resultSet.first())
                    {
                        log.error("Error doing resultSet.first");
                        return null;
                    }                   
                    do
                    {                   
                        rs.addItem(resultSet.getLong(column));
                    } while (resultSet.next());
                }    

                //}
        
            } else
            {
                for (int i=0;i<rows.length;i++)
                {
                    int rowInx = displayRowIndexes != null ? displayRowIndexes[rows[i]] : rows[i];
                    if (resultSet.absolute(rowInx+1))
                    {
                        rs.addItem(resultSet.getLong(column));
                    }
                }
            }
            return rs;

        } catch (Exception ex)
        {
            log.error(ex);
        }
        return null;
    }
    

}

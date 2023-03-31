/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.conversion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class TableStats
{
    protected SingleTable oldTable;
    protected SingleTable newTable;

    public TableStats(Connection oldDBConn, String oldTableName, Connection newDBConn, String newTableName)
    {
        this.oldTable = new SingleTable(oldDBConn, oldTableName);
        this.newTable = new SingleTable(newDBConn, newTableName);
    }

    public void collectStats()
    {
        oldTable.collectStats();
        newTable.collectStats();
    }


    public SingleTable getNewTable()
    {
        return newTable;
    }

    public void setNewTable(SingleTable newTable)
    {
        this.newTable = newTable;
    }

    public SingleTable getOldTable()
    {
        return oldTable;
    }

    public void setOldTable(SingleTable oldTable)
    {
        this.oldTable = oldTable;
    }
    
    public void compareStats()
    {
        if (oldTable.hasStats() && newTable.hasStats())
        {
            double rowsDiff = oldTable.getNumRows() > 0 ? newTable.getNumRows() / oldTable.getNumRows() * 100.0 : 0.0;
            double sizeDiff = oldTable.getSize() > 0    ? newTable.getSize()    / oldTable.getSize()    * 100.0 : 0.0;
            
            System.out.println("Table "+oldTable.getName()+" "+newTable.getName());
            System.out.println("Rows  "+oldTable.getNumRows()+"  "+oldTable.getNumRows()+" "+(String.format("%5.2f", new Object[] {rowsDiff})));
            System.out.println("Size  "+oldTable.getSize()+"  "+oldTable.getSize()+" "+(String.format("%5.2f", new Object[] {sizeDiff})));
            
            System.out.println(oldTable.conn.hashCode() +" " + newTable.conn.hashCode());
 
        } else
        {
            System.out.println(String.format("No Stats for %s, %s", oldTable.getName(), newTable.getName()));
        }
    }
    
    
    //---------------------------------------------------
    //-- Inner Classes
    //---------------------------------------------------
    protected class SingleTable 
    {
        protected Connection conn;
        protected String     name;
        protected int        size;
        protected int        numRows;
        protected boolean    hasStats = false;
        
        
        public SingleTable(Connection conn, String name)
        {
            super();
            this.conn    = conn;
            this.name    = name;
            this.size    = 0;
            this.numRows = 0;
        }
        
        public SingleTable(String name, int size, int numRows)
        {
            super();
            this.name    = name;
            this.size    = size;
            this.numRows = numRows;
        }
        
        /**
         * @return the hasStats
         */
        public boolean hasStats()
        {
            return hasStats;
        }

        public void collectStats()
        {
            if (conn != null)
            {   
                try
                {
                    readTables();
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        
        public int getColNameIndex(final ResultSet rs, final String colNameArg) throws SQLException
        {
            for (int i=1;i<rs.getMetaData().getColumnCount();i++)
            {
                String colName = rs.getMetaData().getColumnName(i);
                if (colName.equals(colNameArg))
                {
                    return i;
                }
            }
            return -1;
        }
        
        public void readTables() throws SQLException
        {
            try
            {
                Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = st.executeQuery("show table status");
                if (rs.first())
                {
                    do
                    {
                        String tableName = rs.getString(getColNameIndex(rs, "Name"));
                        
                        if (tableName.equals(name))
                        {
                            numRows = BasicSQLUtils.getNumRecords(conn, name);//rs.getInt(getColNameIndex(rs, "Rows"));
                            size    = rs.getInt(getColNameIndex(rs, "Data_length"));
                            
                            break;
                        }
                    } while (rs.next());
                    hasStats = true;
                    
                } else
                {
                    System.err.println("Couldn't get Table Stats for Table ["+name+"]");
                }
                
            } catch (Exception ex)
            {
                System.err.println("Couldn't get Table Stats for Table ["+name+"]");
            }
        }
        
        public String getName()
        {
            return name;
        }
        public void setName(String name)
        {
            this.name = name;
        }
        public int getNumRows()
        {
            return numRows;
        }
        public void setNumRows(int numRows)
        {
            this.numRows = numRows;
        }
        public int getSize()
        {
            return size;
        }
        public void setSize(int size)
        {
            this.size = size;
        }
        
        
    }

}



/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Dec 16, 2009
 *
 */
public class TableDataChecker
{

    private Connection connection;

    /**
     * @param connection
     */
    public TableDataChecker(final Connection connection)
    {
        super();
        this.connection = connection;
    }
    
    /**
     * @param tableName
     * @param skipNames
     * @return
     */
    public List<String> getColumnNamesWithData(final String tableName, 
                                               final HashSet<String> skipNames)
    {
        List<String> fieldsWithData = new Vector<String>();
        
        int numRows = BasicSQLUtils.getNumRecords(connection, tableName);
        if (numRows > 0)
        {
            try
            {
                Vector<Object[]> rows = BasicSQLUtils.query(connection, String.format("SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS " +
                                                                          "WHERE table_name = '%s' AND table_schema = '%s'", 
                                                                          tableName, connection.getCatalog()));
                             
                for (Object[] cols : rows)
                {
                    String fieldName = cols[0].toString();
                    
                    if ((skipNames == null || !skipNames.contains(fieldName.toLowerCase())))
                    {
                        if (cols[2].equals("YES"))
                        {
                            String sql = String.format("SELECT COUNT(*) FROM `%s` WHERE `%s` IS NOT NULL", tableName, fieldName);
                            //System.out.println(sql);
                            int cnt = BasicSQLUtils.getCountAsInt(connection, sql);
                            if (cnt > 0)
                            {
                                fieldsWithData.add(fieldName);
                            }
                        }
                    }
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return fieldsWithData;
    }
    
    /**
     * @param file
     * @param title
     */
    public void createHTMLReport(final TableWriter tblWriter)
    {
        HashSet<String> skipNames = new HashSet<String>();
        String[] sknames = new String[] {"TimestampCreated", "TimestampModified", "LastEditedBy"};
        for (String n : sknames)
        {
            skipNames.add(n.toLowerCase());
        }
        
        try
        {
            String titleStr = String.format("Nullable Table Columns with Data for %s", connection.getCatalog());
            tblWriter.print("<H3>");
            tblWriter.print(titleStr);
            tblWriter.print("</H3>");
            Vector<Object> tableNames = BasicSQLUtils.querySingleCol(connection, "SHOW TABLES");
            for (Object tblObj : tableNames)
            {
                String tblName = tblObj.toString();
                
                if (tblName.startsWith("usys") || tblName.startsWith("web") || tblName.startsWith("ft_") || StringUtils.contains(tblName, "fulltext"))
                {
                    continue;
                }
                System.out.println("Processing "+tblName);
                
                List<String> cols = getColumnNamesWithData(tblName, skipNames);
                if (cols.size() > 0)
                {
                    tblWriter.print("<center>");
                    tblWriter.startTable();
                    tblWriter.print("<tr><th>");
                    tblWriter.print(tblName);
                    tblWriter.println("</td></th>");
                    for (String colName : cols)
                    {
                        tblWriter.print("<tr><td>");
                        tblWriter.print(colName);
                        tblWriter.println("</td></tr>");
                    }
                    tblWriter.endTable();
                    tblWriter.print("</center>");
                    tblWriter.log("<br>");
                }
            }
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    public void doCheckDB()
    {
        try
        {
            String dbName = connection.getCatalog();
            
            boolean fnd = false;
            Vector<Object[]> tables = BasicSQLUtils.query(connection, "show tables");
            for (Object[] tblRow : tables)
            {
                String tableName = tblRow[0].toString();
                if (tableName.equalsIgnoreCase("usysversion"))
                {
                    fnd = true;
                    break;
                }
            }
            
            if (fnd)
            {
                TableWriter tDSTblWriter = new TableWriter(dbName+"_TableDataSummary.html", "Table Data Summary");
                createHTMLReport(tDSTblWriter);
                tDSTblWriter.flush();
                tDSTblWriter.close();
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    public void doAllSp5DBs()
    {
        
        try
        {
            PrintWriter pw = new PrintWriter(new File("index.html"));
            pw.println("<html><body>Databases<br>");
            
            String cacheCatalog = connection.getCatalog();
            
            Vector<Object[]> dbNames = BasicSQLUtils.query(connection, "show databases");
            for (Object[] row : dbNames)
            {
                String dbName = row[0].toString();
                System.out.print("Database Found ["+dbName+"]  ");
                connection.setCatalog(dbName);
                
                boolean fnd = false;
                Vector<Object[]> tables = BasicSQLUtils.query(connection, "show tables");
                for (Object[] tblRow : tables)
                {
                    String tableName = tblRow[0].toString();
                    if (tableName.equalsIgnoreCase("usysversion"))
                    {
                        fnd = true;
                        break;
                    }
                }
                
                if (fnd)
                {
                    pw.println("<a href='"+dbName+".html'>"+dbName+"</a><br>");
                    
                    TableWriter tDSTblWriter = new TableWriter(dbName+"_TableDataSummary.html", "Table Data Summary");
                    createHTMLReport(tDSTblWriter);
                    tDSTblWriter.flush();
                    tDSTblWriter.close();
                }
            }
            
            pw.println("</body></html>");
            pw.close();
            
            if (cacheCatalog != null)
            {
                connection.setCatalog(cacheCatalog);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}

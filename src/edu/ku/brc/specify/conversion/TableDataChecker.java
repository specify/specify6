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
                            System.out.println(sql);
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
    public void createHTMLReport(final File file)
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
            
            PrintWriter pw = new PrintWriter(file);
            pw.print("<html><head><title>");
            pw.println(titleStr);
            pw.print("</title>\n<style>");
            pw.println("  table { border-top: gray 1px solid; border-left: gray 1px solid; }");
            pw.println("  td { border-right: gray 1px solid; border-bottom: gray 1px solid; }");
            pw.println("  th { border-right: gray 1px solid; border-bottom: gray 1px solid; }");
            pw.println("</style>\n</head>");
            pw.println("<body><center>");
            pw.print("<h2>");
            pw.print(titleStr);
            pw.print("</h2>");
            
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
                    pw.println("<table cellspacing='0' cellpadding='4' border='0'>");
                    pw.print("<tr><th>");
                    pw.print(tblName);
                    pw.println("</td></th>");
                    for (String colName : cols)
                    {
                        pw.print("<tr><td>");
                        pw.print(colName);
                        pw.println("</td></tr>");
                    }
                    pw.println("</table><br>");
                }
            }
            
            pw.println("</cetner></body>");
            pw.println("</html>");
            
            pw.flush();
            pw.close();
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
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
                    createHTMLReport(new File(dbName+".html"));
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

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

import edu.ku.brc.util.Pair;

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
    public List<Pair<String, String>> getColumnNamesWithData(final String tableName, 
                                                             final HashSet<String> skipNames)
    {
        List<Pair<String, String>> fieldsWithData = new Vector<Pair<String, String>>();
        
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
                            int    cnt = BasicSQLUtils.getCountAsInt(connection, sql);
                            if (cnt > 0)
                            {
                                sql = String.format("SELECT DISTINCT c.caption, fst.TextForValue from usysmetacontrol c INNER JOIN usysmetaobject o on o.objectid = c.objectid " +
                                                    "INNER JOIN usysmetafieldset fs on fs.fieldsetid = o.fieldsetid LEFT JOIN usysmetafieldsetsubtype fst on fst.fieldsetsubtypeid = c.fieldsetsubtypeid " +
                                                    "WHERE fs.fieldsetname = '%s' and o.objectname = '%s' and (fst.TextForValue is null or (fst.TextForValue not in('TissueOrExtract', 'KaryoSlide', 'HistoSlideSeries', 'Image', 'Sound', 'SoundRecording', 'ImagePrint', 'Spectrogram', 'Container')))",
                                                    tableName, fieldName);
                                 
                                Pair<String, String> namePair = new Pair<String, String>();
                                Vector<Object[]> captions = BasicSQLUtils.query(connection, sql);
                                if (captions.size() > 0)
                                {
                                    namePair.second = (String)captions.get(0)[0];
                                }
                                namePair.first = fieldName;
                                fieldsWithData.add(namePair);
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
        tblWriter.setHasLines();
        
        HashSet<String> skipNames = new HashSet<String>();
        String[] sknames = new String[] {"TimestampCreated", "TimestampModified", "LastEditedBy"};
        for (String n : sknames)
        {
            skipNames.add(n.toLowerCase());
        }
        
        try
        {
            tblWriter.print("<center>");
            String titleStr = String.format("Nullable Table Columns with Data", connection.getCatalog());
            tblWriter.print("<H3>");
            tblWriter.print(titleStr);
            tblWriter.print("</H3>");
            
            Vector<Object> tableNames = BasicSQLUtils.querySingleCol(connection, "SHOW TABLES");
            
            tblWriter.startTable();
            tblWriter.print("<caption style=\"text-align: center; font-weight: bold; font-size: 14pt;\">");
            tblWriter.print(connection.getCatalog());
            tblWriter.print("</caption>");
            
            String[] headings = {"Table", "Field Name", "Sp5 Caption"};
            tblWriter.print("<tr>");
            for (String head : headings)
            {
                tblWriter.print("<th>");// style=\"text-align: center; font-weight: bold;\">");
                tblWriter.print(head);
                tblWriter.println("</th>");
            }
            tblWriter.println("</tr>");
            
            for (Object tblObj : tableNames)
            {
                String tblName = tblObj.toString();
                
                if (tblName.startsWith("usys") || 
                    tblName.startsWith("web") || 
                    tblName.startsWith("ft_") || 
                    tblName.startsWith("data") || 
                    tblName.startsWith("rave") || 
                    tblName.startsWith("reports") || 
                    StringUtils.contains(tblName, "_tmp") || 
                    StringUtils.contains(tblName, "_dup") || 
                    StringUtils.contains(tblName, "fulltext"))
                {
                    continue;
                }
                System.out.println("Processing "+tblName);
                
                List<Pair<String, String>> cols = getColumnNamesWithData(tblName, skipNames);
                if (cols.size() > 0)
                {
                    tblWriter.println("<tr>");
                    tblWriter.println("<td valign=\"top\" rowspan=\"" + cols.size() + "\"><span style=\"font-weight:bold;\">"+tblName+"</span></td>");
                    int cnt = 0;
                    for (Pair<String, String> colName : cols)
                    {
                        if (cnt > 0) tblWriter.print("<tr>");
                        
                        tblWriter.print("<td>");
                        tblWriter.print(colName.first);
                        tblWriter.println("</td><td>");
                        tblWriter.println(colName.second != null ? colName.second : "&nbsp;");
                        tblWriter.println("</td></tr>");
                        cnt++;
                    }
                }
            }
            tblWriter.endTable();
            tblWriter.print("</center>");
            
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
                TableWriter tDSTblWriter = new TableWriter(dbName+"_TableDataSummary.html", "Table Data Summary", true);
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
                    
                    TableWriter tDSTblWriter = new TableWriter(dbName+"_TableDataSummary.html", "Table Data Summary", true);
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

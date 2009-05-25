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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import org.apache.commons.io.FilenameUtils;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2009
 *
 */
public class ConversionLogger
{
    public static String TR ="<tr>";
    public static String TR_ ="</tr>";
    public static String TD ="<td>";
    public static String TD_ ="</td>";
    
    protected Hashtable<String, String>      printWritersNameHash  = new Hashtable<String, String>();
    protected Hashtable<String, TableWriter> printWritersHash      = new Hashtable<String, TableWriter>();
    
    protected File dir;
    
    public ConversionLogger()
    {
    }
    
    /**
     * @param name
     * @return
     */
    public boolean initialize(final String name)
    {
        dir = new File("conversions" + File.separator + name);
        if (!dir.exists())
        {
            return dir.mkdirs();
        }
        return false;
    }
    
    /**
     * @param tableName
     * @return
     * @throws IOException
     */
    public TableWriter getWriter(final String fileName, final String title)
    {
        
        ConversionLogger.TableWriter tblWriter = null;
        try
        {
            String path = dir.getAbsolutePath() + File.separator + fileName;
            tblWriter = new TableWriter(path, title);
            printWritersNameHash.put(fileName, path);
            printWritersHash.put(fileName, tblWriter);
            
        } catch (IOException ex) { ex.printStackTrace(); }

        return tblWriter;
    }
    
    /**
     * 
     */
    public void closeAll()
    {
        try 
        {
            String path = dir.getAbsolutePath() + File.separator + "index.html";
            TableWriter indexWriter = new TableWriter(path, "Index");
            indexWriter.startTable();
            for (TableWriter tw : printWritersHash.values())
            {
                try
                {
                    indexWriter.log(null, "<a href=\""+ FilenameUtils.getName(tw.getFileName())+"\">"+tw.getTitle()+"</a>", null);
                    
                    tw.close();
                    
                } catch (Exception ex)
                {
                    
                }
            }
            indexWriter.endTable();
            indexWriter.close();
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

    }
    
    //-------------------------------------------------------------
    public class TableWriter extends PrintWriter
    {
        private String fName;
        private String title;
        
        public TableWriter(final String fileName, final String title) throws FileNotFoundException
        {
            super(fileName);
            this.fName = fileName;
            this.title = title;
            
            println("<html>\n<head>\n<title>"+title+"</title>\n");
            writeStyle(this);
            println("</head>\n<body>");
            println("<h2>"+title+"</h2>");
        }
        
        /**
         * @param out
         */
        protected void writeStyle(final PrintWriter out)
        {
            out.println("<style>");
            out.println(" span.err { color: red; }");
            out.println(" table.o { border-top: solid 1px rgb(128, 128, 128); border-left: solid 1px rgb(128, 128, 128); }");
            out.println(" table.o td { border-bottom: solid 1px rgb(128, 128, 128); border-right: solid 1px rgb(128, 128, 128); }");
            out.println(" table.i { border-top: solid 1px rgb(192, 192, 192); border-left: solid 1px rgb(192, 192, 192); }");
            out.println(" table.i td { border-bottom: solid 1px rgb(192, 192, 192); border-right: solid 1px rgb(192, 192, 192); }");
            out.println("</style>");
        }
        
        /**
         * @return the fName
         */
        public String getFileName() 
        {
            return fName;
        }

        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        public void log(final String msg)
        {
            print(msg);
            println("<BR>");
            flush();
        }
        
        public void logError(final String msg)
        {
            println("<span class=\"err\">");
            print(msg);
            println("</span><BR>");
            flush();
        }
        
        public void startTable()
        {
            println("<table>");
            flush();
        }
        
        public void endTable()
        {
            println("</table>");
            flush();
        }
        
        public void log(final String id, final String value, final String desc)
        {
            print(TR);
            if (id != null)
            {
                print(TD);
                print(id);
                print(TD_);
            }
            print(TD);
            print(value);
            print(TD_);
            if (desc != null)
            {
                print(TD);
                print(desc);
                print(TD_);
            }
            println(TR_);
            flush();
        }
        
        /* (non-Javadoc)
         * @see java.io.PrintWriter#close()
         */
        public void close()
        {
            println("</body></html>");
            super.close();
            
        }
    }
}

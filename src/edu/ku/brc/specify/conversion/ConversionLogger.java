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
    
    protected Hashtable<String, String> printWriters = new Hashtable<String, String>();
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
    public TableWriter getWriter(final String tableName, final String title)
    {
    	
    	ConversionLogger.TableWriter tblWriter = null;
        try
        {
            String path = dir.getAbsolutePath() + File.separator + tableName;
            tblWriter = new TableWriter(path, title);
            printWriters.put(tableName, path);
            
        } catch (IOException ex) { ex.printStackTrace(); }

        return tblWriter;
    }
    
    //-------------------------------------------------------------
    public class TableWriter extends PrintWriter
    {
        
        public TableWriter(final String fileName, final String title) throws FileNotFoundException
        {
            super(fileName);
            println("<html><head><title>"+title+"</title>\n<style>\n  span.err { color: red; }\n</style>\n</head><body>");
            println("<h2>"+title+"</h2>");
        }
        
        public void log(final String msg)
        {
            print(msg);
            println("<BR>");
        }
        
        public void logError(final String msg)
        {
            println("<span class=\"err\">");
            print(msg);
            println("</span><BR>");
        }
        
        public void startTable()
        {
            println("<table>");
        }
        
        public void endTable()
        {
            println("</table>");
        }
        
        public void log(final String id, final String value, final String desc)
        {
            print(TR);
            print(TD);
            print(id);
            print(TD_);
            print(TD);
            print(value);
            print(TD_);
            print(TD);
            print(desc);
            print(TD_);
            println(TR_);
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

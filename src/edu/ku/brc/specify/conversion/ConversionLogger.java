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
    public static String TR ="<TR>";
    public static String TR_ ="</TR>";
    public static String TD ="<TD>";
    public static String TD_ ="</TD>";
    public static String TH ="<TH>";
    public static String TH_ ="</TH>";
    
    protected Hashtable<String, String>      printWritersNameHash  = new Hashtable<String, String>();
    protected Hashtable<String, TableWriter> printWritersHash      = new Hashtable<String, TableWriter>();
    
    protected String indexTitle = "Index";
    
    protected File dir;
    
    /**
     * 
     */
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
     * @param indexTitle the indexTitle to set
     */
    public void setIndexTitle(String indexTitle)
    {
        this.indexTitle = indexTitle;
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
    public File closeAll()
    {
        try 
        {
            String path = dir.getAbsolutePath() + File.separator + "index.html";
            TableWriter indexWriter = new TableWriter(path, indexTitle);
            indexWriter.startTable();
            
            for (TableWriter tblWriter : printWritersHash.values())
            {
                try
                {
                    if (tblWriter.hasLines())
                    {
                        indexWriter.log("<A href=\""+ FilenameUtils.getName(tblWriter.getFileName())+"\">"+tblWriter.getTitle()+"</A>");
                        tblWriter.close();
                        
                    } else
                    {
                        tblWriter.flush();
                        tblWriter.close();
                        
                        File f = new File(tblWriter.getFileName());
                        if (f.exists())
                        {
                            f.delete();
                        }
                    }
                   
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            indexWriter.endTable();
            indexWriter.close();
            
            return new File(path);
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    //-------------------------------------------------------------
    public class TableWriter extends PrintWriter
    {
        private String fName;
        private String title;
        
        private int errCount = 0;
        private int lineCnt  = 0;
        
        public TableWriter(final String fileName, final String title) throws FileNotFoundException
        {
            super(fileName);
            this.fName = fileName;
            this.title = title;
            
            println("<HTML>\n<HEAD>\n<TITLE>"+title+"</TITLE>\n");
            writeStyle(this);
            println("</HEAD>\n<BODY>");
            println("<H2>"+title+"</H2>");
        }
        
        /**
         * @param out
         */
        protected void writeStyle(final PrintWriter out)
        {
            out.println("<STYLE>");
            out.println(" SPAN.err { color: red; }");
            out.println(" TABLE.o { border-top: solid 1px rgb(128, 128, 128); border-left: solid 1px rgb(128, 128, 128); }");
            out.println(" TABLE.o td { border-bottom: solid 1px rgb(128, 128, 128); border-right: solid 1px rgb(128, 128, 128); }");
            out.println(" TABLE.o th { border-bottom: solid 1px rgb(128, 128, 128); border-right: solid 1px rgb(128, 128, 128); }");
            out.println(" TABLE.i { border-top: solid 1px rgb(192, 192, 192); border-left: solid 1px rgb(192, 192, 192); }");
            out.println(" TABLE.i td { border-bottom: solid 1px rgb(192, 192, 192); border-right: solid 1px rgb(192, 192, 192); }");
            out.println(" TABLE.i th { border-bottom: solid 1px rgb(192, 192, 192); border-right: solid 1px rgb(192, 192, 192); }");
            out.println("</STYLE>");
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
            lineCnt++;
            print(msg);
            println("<BR>");
            flush();
        }
        
        public void logError(final String msg)
        {
            errCount++;
            lineCnt++;
            
            println("<SPAN class=\"err\">");
            print(msg);
            println("</SPAN><BR>");
            flush();
        }
        
        public void startTable()
        {
            println("<TABLE class=\"o\" cellspacing=\"0\">");
            flush();
        }
        
        public void endTable()
        {
            println("</TABLE>");
            flush();
        }
        
        public void log(final String...cols)
        {
            lineCnt++;
            print(TR);
            for (String c : cols)
            {
                if (c != null)
                {
                    print(TD);
                    print(c);
                    print(TD_);
                }
            }
            println(TR_);
            flush();
        }
        
        public void logHdr(final String...cols)
        {
            print(TR);
            for (String c : cols)
            {
                if (c != null)
                {
                    print(TH);
                    print(c);
                    print(TH_);
                }
            }
            println(TR_);
            flush();
        }
        
        /* (non-Javadoc)
         * @see java.io.PrintWriter#close()
         */
        public void close()
        {
            println("</BODY></HTML>");
            super.close();
        }
        
        /**
         * @return
         */
        public boolean hasLines()
        {
            return lineCnt > 0;
        }
    }
}

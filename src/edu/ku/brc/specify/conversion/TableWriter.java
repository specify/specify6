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
/**
 * 
 */
package edu.ku.brc.specify.conversion;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.commons.lang.StringUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 4, 2010
 *
 */
//-------------------------------------------------------------
public class TableWriter extends PrintWriter
{
    public static final String TR ="<TR>";
    public static final String TR_ ="</TR>";
    public static final String TD ="<TD>";
    public static final String TD_ ="</TD>";
    public static final String TH ="<TH>";
    public static final String TH_ ="</TH>";
    
    private String fName;
    private String title;
    
    private int errCount = 0;
    private int lineCnt  = 0;
    
    /**
     * @param fileName
     * @param title
     * @throws FileNotFoundException
     */
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
     * @param pwOut
     */
    private void writeStyle(final PrintWriter pwOut)
    {
        pwOut.println("<STYLE>");
        pwOut.println(" SPAN.err { color: red; }");
        pwOut.println(" TABLE.o { border-top: solid 1px rgb(128, 128, 128); border-left: solid 1px rgb(128, 128, 128); }");
        pwOut.println(" TABLE.o td { border-bottom: solid 1px rgb(128, 128, 128); border-right: solid 1px rgb(128, 128, 128); }");
        pwOut.println(" TABLE.o th { border-bottom: solid 1px rgb(128, 128, 128); border-right: solid 1px rgb(128, 128, 128); }");
        pwOut.println(" TABLE.i { border-top: solid 1px rgb(192, 192, 192); border-left: solid 1px rgb(192, 192, 192); }");
        pwOut.println(" TABLE.i td { border-bottom: solid 1px rgb(192, 192, 192); border-right: solid 1px rgb(192, 192, 192); }");
        pwOut.println(" TABLE.i th { border-bottom: solid 1px rgb(192, 192, 192); border-right: solid 1px rgb(192, 192, 192); }");
        pwOut.println("</STYLE>");
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
    public String getTitle() 
    {
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
    
    public void logErrors(final String...cols)
    {
        errCount++;
        lineCnt++;
        
        print(TR);
        for (int i=0;i<cols.length;i++)
        {
            String str = cols[i];
            if (str != null)
            {
                if (i == cols.length-1)
                {
                    print(TD);
                    print("<SPAN class=\"err\">");
                    print(str);
                    print("</SPAN><BR>");
                    print(TD_);
                } else
                {
                    print(TD);
                    print(str);
                    print(TD_);
                }
            }
        }
        println(TR_);
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
    
    /**
     * @param cols
     */
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

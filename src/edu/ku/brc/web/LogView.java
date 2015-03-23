/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jstewart
 * @version
 */
public class LogView extends HttpServlet
{
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Usage Stats Log Viewer</title>");
        out.println("</head>");
        out.println("<body>");
        
        String webInfPath = getServletContext().getRealPath("/") + File.separator + "WEB-INF" + File.separator;
        File usageLog = new File(webInfPath + "usage_log.txt");
        
        Hashtable<String, List<String>> usageData = summarizeUsageData(usageLog);
        
        // augment the records by adding the hostname to the end of the IP address
        performReverseDNS(usageData);
        
        out.println("<h3>Records found: " + usageData.keySet().size() + "</h3><br/>");
        
        out.println("<pre>");
        for (String id: usageData.keySet())
        {
            for (String entry: usageData.get(id))
            {
                out.println("\t" + entry);
            }
            out.println();
        }
        
        out.println("</pre>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
    
    protected Hashtable<String, List<String>> summarizeUsageData(File usageLogContents) throws FileNotFoundException, IOException
    {
        BufferedReader input = new BufferedReader(new FileReader(usageLogContents));
        
        Hashtable<String, List<String>> compiledData = new Hashtable<String, List<String>>();
        
        String line = null;
        while ((line = input.readLine()) != null)
        {
            // find a non-KU ID line
            if (line.startsWith("ID") && !line.endsWith("(KU)"))
            {
                // we've found the start of a new entry
                // compile all of the data into a record in the hashtable
                List<String> data = new Vector<String>();
                
                data.add(line);
                
                String idString = line.substring(5,54);
                
                while((line = input.readLine()) != null && line.trim().length()>0)
                {
                    data.add(line);
                }
                
                compiledData.put(idString,data);
            }
        }
        
        return compiledData;
    }
    
    protected String doSingleReverseDNS( String ipAddr )
    {
        InetAddress addr;
        try
        {
            addr = InetAddress.getByName(ipAddr);
        }
        catch (UnknownHostException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LogView.class, ex);
            return "";
        }
        return addr.getHostName();
    }
    
    protected void performReverseDNS( Hashtable<String, List<String>> usageData )
    {
        ExecutorService glExecServ = Executors.newFixedThreadPool(25);
        
        int cnt = 0;
        for (String id: usageData.keySet())
        {
            final List<String> record = usageData.get(id);
            final String idLine = record.get(0);
            StringTokenizer st = new StringTokenizer(idLine,"\t");
            // ignore the first token
            st.nextToken();
            final String ipAddr = st.nextToken().trim();
            
            cnt++;
            System.out.println(cnt + " / " + usageData.keySet().size());
            Runnable hostnameLookup = new Runnable()
            {
                public void run()
                {
                    String hostname = doSingleReverseDNS(ipAddr);
                    record.set(0,idLine + "\t(" + hostname + ")");
                }
            };
            
            glExecServ.submit(hostnameLookup);
            
            //String hostname = doSingleReverseDNS(ipAddr);
        }
        
        glExecServ.shutdown();
        try
        {
            // wait at most 10 seconds for all of the reverse DNS results
            glExecServ.awaitTermination(10,TimeUnit.SECONDS);
        }
        catch (InterruptedException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LogView.class, ex);
            // if we didn't get all of the results in the allotted time, oh well
            // do nothing
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo()
    {
        return "Short description";
    }
    // </editor-fold>
    
     
    protected void processStats()
    {
        try
        {
            PrintWriter out = new PrintWriter(new File("stats.html"));
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Usage Stats Log Viewer</title>");
            out.println("</head>");
            out.println("<body>");
            
            File usageLog = new File("/Users/rod/usage_log.txt");
            
            Hashtable<String, List<String>> usageData = summarizeUsageData(usageLog);
            
            // augment the records by adding the hostname to the end of the IP address
            performReverseDNS(usageData);
            
            out.println("<h3>Records found: " + usageData.keySet().size() + "</h3><br/>");
            
            out.println("<pre>");
            for (String id: usageData.keySet())
            {
                for (String entry: usageData.get(id))
                {
                    out.println("\t" + entry);
                }
                out.println();
            }
            
            out.println("</pre>");
            out.println("</body>");
            out.println("</html>");
            out.close();
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LogView.class, ex);
            ex.printStackTrace();
        }
    }
    public static void main(String[] args)
    {
        LogView lv = new LogView();
        lv.processStats();
    }
}

/*
 * LogView.java
 *
 * Created on June 4, 2007, 9:43 AM
 */

package edu.ku.brc.web;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

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
}

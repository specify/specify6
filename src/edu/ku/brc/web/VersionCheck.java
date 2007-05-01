/*
 * VersionCheck.java
 *
 * Created on April 30, 2007, 1:47 PM
 */

package edu.ku.brc.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jstewart
 * @code_status Alpha
 */
public class VersionCheck extends HttpServlet
{    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String webInfPath = getServletContext().getRealPath("/") + File.separator + "WEB-INF" + File.separator;
        
        StringBuilder usageStats = new StringBuilder();
        
        String id      = null;
        String os      = null;
        String version = null;
        
        // get the request params (from POST or GET request)
        Enumeration paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements())
        {
            String paramName = (String)paramNames.nextElement();
            String[] valueArray = request.getParameterValues(paramName);
            String value = null;
            if (valueArray.length>0)
            {
                value = valueArray[0];
            }
            
            if (paramName.equalsIgnoreCase("id"))
            {
                id = value;
            }
            else if (paramName.equalsIgnoreCase("os"))
            {
                os = value;
            }
            else if (paramName.equalsIgnoreCase("version"))
            {
                version = value;
            }
            else
            {
                usageStats.append("\t" + paramName + ":" + value + "\n");
            }
        }
        
        // read the install_info.xml file and send it to the client
        InputStream in = null;
        try
        {
            in = new BufferedInputStream( new FileInputStream(webInfPath + "install_info.xml") );
            int ch;
            while ((ch = in.read()) != -1)
            {
                out.print((char)ch);
            }
        }
        finally
        {
            if (in != null)
            {
                in.close(); // VERY important
            }
        }
        out.close();
        
        // append a new log entry (containing usage stats, if they were sent)
        StringBuilder logEntry = new StringBuilder();
        
        // add the timestamp
        String timestamp = (new Date()).toString();
        logEntry.append(timestamp);
        logEntry.append("\n");
        
        // add the installation ID string
        String idString = "ID = " + id + ", " + "OS = " + os + ", " + version;
        logEntry.append(idString);
        logEntry.append("\n");
        
        // add the usage data
        logEntry.append(usageStats.toString());
        logEntry.append("\n");

        synchronized (this)
        {
            File logFile = new File(webInfPath + "usage_log.txt");
            FileWriter fileWriter = null;
            try
            {
                fileWriter = new FileWriter(logFile, true);
                fileWriter.write(logEntry.toString());
                fileWriter.flush();
            }
            catch( IOException ioe )
            {
                ioe.printStackTrace();
            }
            finally
            {
                if (fileWriter != null)
                {
                    fileWriter.close();
                }
            }
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "A simple application version checker";
    }
    // </editor-fold>
}

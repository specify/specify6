/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
 * This class is a HTTP servlet that handles version checking / usage tracking connections.
 *
 * @author jstewart
 * @code_status Beta
 */
public class VersionCheck extends HttpServlet
{    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @SuppressWarnings("unused")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        // check the user agent string to see if this is a legit request
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.indexOf("VersionChecker") == -1)
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().close();
            return;
        }
        
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String webInfPath = getServletContext().getRealPath("/") + File.separator + "WEB-INF" + File.separator;
        
        StringBuilder usageStats = new StringBuilder();
        
        String id      = null;
        String os      = null;
        String version = null;
        
        // TODO: work this info into the output out the servlet
        //       this will help us filter out the dev and testing boxes' requests
        boolean isKU   = request.getRemoteAddr().startsWith("129.237.");
        
        // get the request params (from POST or GET request)
        Enumeration<?> paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements())
        {
            String paramName = (String)paramNames.nextElement();
            
            if (paramName == null)
            {
                continue;
            }
            
            // limit the parameter name to 32 characters
            if (paramName.length() > 64)
            {
                paramName = paramName.substring(0,64);
            }
            String[] valueArray = request.getParameterValues(paramName);
            String value = null;
            if (valueArray.length>0)
            {
                value = valueArray[0];
                // limit value to 32 characters
                if (value != null && value.length() > 64)
                {
                    value = value.substring(0,64);
                }
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
        
        // add the client IP address (and a 'KU' if they are coming from the ku.edu domain)
        logEntry.append("\t" + request.getRemoteAddr());
        if (isKU)
        {
            logEntry.append(" (KU)");
        }
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
    /**
     * Handles the HTTP <code>GET</code> method.
     * 
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    /**
     * Handles the HTTP <code>POST</code> method.
     * 
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "A simple application version checker / usage tracker";
    }
    // </editor-fold>
}

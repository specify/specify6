/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import edu.ku.brc.af.core.TaskCommandDef;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.ReportsBaseTask;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * May 1, 2008
 *
 */
public class QueryReportHandler
{
    protected Session  session;
    
    /**
     * 
     */
    public QueryReportHandler(final Session  session)
    {
        super();
        this.session = session;
    }

   /**
    * @param request
    * @param response
    * @throws IOException
    */
    @SuppressWarnings("unchecked")
    public void listQueries(final HttpServletRequest request, 
                            final HttpServletResponse response) throws IOException
   {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        SpecifyExplorer.writeTitle(out, "Available Queries");
        out.println("<table border=\"0\">\n");
        out.println("<tr><td>\n");
        out.println("<tr><td class=\"title\" colspan=\"2\" align=\"center\">Available Queries</td></tr>\n");
        //out.println("<tr><td valign=\"top\">\n");

        String sql = "FROM SpQuery as sq Inner Join sq.specifyUser as user where user.specifyUserId = "+SpecifyUser.getCurrentUser().getSpecifyUserId() + " ORDER BY sq.name";
        List<?> rows = (List<?>)session.createQuery(sql).list();
        for (Object row : rows)
        {
            Object[] cols  = (Object[])row;
            SpQuery  query = (SpQuery)cols[0];
            sql = "<a href=\"" + SpecifyExplorer.servletURL + "?cmd=exequery&id=" + query.getId() + "\">";
            out.println("<tr><td nowrap=\"nowrap\" colspan=\"2\" align=\"center\">"+ sql + query.getName()+"</a></td></tr>\n");
        }
        out.println("</table>");
        SpecifyExplorer.writeToEnd(out);
   }
                            
    /**
     * @param request
     * @param response
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void listSpReports(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        SpecifyExplorer.writeTitle(out, "Available Queries");
        out.println("<table border=\"0\">\n");
        out.println("<tr><td>\n");
        out.println("<tr><td class=\"title\" colspan=\"2\" align=\"center\">Available Report</td></tr>\n");
        //out.println("<tr><td valign=\"top\">\n");

        String sql = "FROM SpReport as sq Inner Join sq.specifyUser as user where user.specifyUserId = "+SpecifyUser.getCurrentUser().getSpecifyUserId() + " ORDER BY sq.name";
        List<?> rows = (List<?>)session.createQuery(sql).list();
        for (Object row : rows)
        {
            Object[] cols  = (Object[])row;
            SpReport  report = (SpReport)cols[0];
            sql = "<a href=\"" + SpecifyExplorer.servletURL + "?cmd=reports&id=" + report.getId() + "\">";
            out.println("<tr><td nowrap=\"nowrap\" colspan=\"2\" align=\"center\">"+ sql + report.getName()+"</a></td></tr>\n");
        }
        out.println("</table>");
        SpecifyExplorer.writeToEnd(out);
  
    }
    
    /**
     * @param request
     * @param response
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void listReports(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        SpecifyExplorer.writeTitle(out, "Available Queries");
        
        List<TaskCommandDef> cmds = BaseTask.getAppResourceCommandsByMimeType(ReportsBaseTask.REPORTS_MIME, "Report", null, null);
        if (cmds != null && cmds.size() > 0)
        {
            out.println("<table border=\"0\">\n");
            out.println("<tr><td nowrap=\"nowrap\" class=\"title\" colspan=\"2\" align=\"center\">Available Reports</td></tr>\n");
            for (TaskCommandDef cmd : cmds)
            {
                String id = StringUtils.replace(cmd.getName(), " ", "_") + "&type=report";
                String sql = "<a href=\"" + SpecifyExplorer.servletURL + "?cmd=reports&id=" + id + "\">";
                out.println("<tr><td nowrap=\"nowrap\" colspan=\"2\" align=\"center\">"+ sql +cmd.getName()+"</a></td></tr>\n");
            }
            out.println("</table><br/>");
        }

        cmds = BaseTask.getAppResourceCommandsByMimeType(ReportsBaseTask.LABELS_MIME, "Report", null, null);
        if (cmds != null && cmds.size() > 0)
        {
            out.println("<table border=\"0\">\n");
            out.println("<tr><td nowrap=\"nowrap\" class=\"title\" colspan=\"2\" align=\"center\">Available Labels</td></tr>\n");
            for (TaskCommandDef cmd : cmds)
            {
                String id = StringUtils.replace(cmd.getName(), " ", "_") + "&type=label";
                String sql = "<a href=\"" + SpecifyExplorer.servletURL + "?cmd=reports&id=" + id + "\">";
                out.println("<tr><td nowrap=\"nowrap\" colspan=\"2\" align=\"center\">"+ sql +cmd.getName()+"</a></td></tr>\n");
            }
            out.println("</table>");
        }

        SpecifyExplorer.writeToEnd(out);
    }

    /**
     * @param request
     * @param response
     */
    public void doReport(final HttpServletRequest request,
                         final HttpServletResponse response)
    {
        String name = request.getParameter("id");
        String type = request.getParameter("type");
        System.out.println("id ["+name+"] type["+type+"]");
        
        List<TaskCommandDef> cmds;
        if (type.equals("report"))
        {
            cmds = BaseTask.getAppResourceCommandsByMimeType(ReportsBaseTask.REPORTS_MIME, "Report", null, null);
        } else
        {
            cmds = BaseTask.getAppResourceCommandsByMimeType(ReportsBaseTask.LABELS_MIME, "Report", null, null);
        }
        
        String rptName  = null;
        String fileName = null;
        for (TaskCommandDef cmd : cmds)
        {
            if (StringUtils.isNotEmpty(name) && name.equals(StringUtils.replace(cmd.getName(), " ", "_")))
            {
                System.out.println("Found["+name+"]");
                fileName = cmd.getParams().getProperty("name");
                rptName  = fileName;
                break;
                
            } else
            {
                System.out.println("The name was null. ");
            }
        }
        
        if (rptName == null || fileName == null)
        {
            return;
        }
        
        JasperReportHelper jrh = new JasperReportHelper(new JasperReportHelper.JasperReportHelperListener() 
        {
            public void complete(JasperPrint jasperPrint)
            {
                PrintWriter out = null;
                try
                {
                    out = response.getWriter();
                    
                    System.out.println("Complete");
    
                    if (true)
                    {
                        response.setHeader("Expires", "0");
                        response.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
                        response.setHeader("Pragma", "public");
                        
                        response.setContentType("application/pdf");
                        JRPdfExporter exporter = new JRPdfExporter();
                        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
                        exporter.exportReport();
                        
                    } 
                    
                    if (false)
                    {
                        response.setContentType("application/rtf");
                        response.setHeader("Content-Disposition", "inline; filename=\"file.rtf\"");

                        JRRtfExporter exporter = new JRRtfExporter();
                        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
                        exporter.exportReport();
                    }
                    
                    if (true)
                    {
                        response.setHeader("Expires", "0");
                        response.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
                        response.setHeader("Pragma", "public");

                        try
                        {
    
                            JRHtmlExporter exporter = new JRHtmlExporter();
                            request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
                            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint); 
                            exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, out);                   
                             
                            // gonna set url pattern given for Image servlet with a reponse parameter <url-pattern>/image</url-pattern> 
                             
                            exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "http://localhost/specifyexplorer/");         
            
                            exporter.exportReport();
                            
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
    
                } catch (JRException ex)
                {
                    ex.printStackTrace();
                    out.println(ex.toString());
                    
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                    
                } finally
                {
                    out.close();
                }
                
            }

            public void completedWithError()
            {
                try
                {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println(" Error creating report.");
                    
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }

            public void status(int status)
            {
                // TODO Auto-generated method stub
                
            }
            
        }, false);
        
        try
        {
            //RecordSet rs = (RecordSet)SpecifyExplorer.session.createQuery("from RecordSet where name = \"Beanii RS\"").list().get(0);
            RecordSet rs = (RecordSet)SpecifyExplorer.session.createQuery("from RecordSet where id = 1").list().get(0);
            if (rs != null)
            {
                Properties params = new Properties();
                
                params.put("name", rptName);
                params.put("file", fileName);
                params.put("reqrs", "true");
                params.put("title", "Fish Label");
                
                jrh.createReport(rptName, rs, params);
                
            } else
            {
                try
                {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("Couldn't Locate RecordSet");
                    
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param request
     * @param response
     */
    public void doSpReport(final HttpServletRequest request,
                           final HttpServletResponse response)
    {
        String name = request.getParameter("id");
        String type = request.getParameter("type");
        System.out.println("id ["+name+"] type["+type+"]");
        
        /*if ((runAction != null || rs != null) && repAction != null)
        {
            toRun = loadReport((RecordSet)repAction.getProperty("spreport"));
        }
        
        if (toRun != null)
        {
            QueryBldrPane.runReport(toRun, "XXX", rs);
        }*/

        
    }
}

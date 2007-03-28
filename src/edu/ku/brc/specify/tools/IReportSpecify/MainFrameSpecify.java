/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tools.IReportSpecify;

import it.businesslogic.ireport.ReportReader;
import it.businesslogic.ireport.ReportWriter;
import it.businesslogic.ireport.Style;
import it.businesslogic.ireport.gui.JReportFrame;
import it.businesslogic.ireport.gui.MainFrame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.ChooseFromListDlg;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * Provides Specify-specific code to load and save reports for the iReport report editor. Code for
 * report saving is not complete - issues such as where to save, duplicate name issues etc still
 * need to be handled.
 */
public class MainFrameSpecify extends MainFrame
{

    private static final Logger log = Logger.getLogger(MainFrameSpecify.class);

    /**
     * @param args -
     *            parameters to configure iReport mainframe
     */
    public MainFrameSpecify(Map args)
    {
        setNoExit(true);
        setEmbeddedIreport(true);
    }

    /**
     * @return default map for specify iReport implementation
     */
    @SuppressWarnings("unchecked")
    public static Map getArgs()
    {
        Map map = new HashMap();
        // "noPlaf" prevents iReport from loading its own look and feel.
        map.put("noPlaf", "true");
        return map;
    }

    public void saveAll(javax.swing.JInternalFrame[] frames)
    {
        System.out.println("saveAll() is not implemented.");
    }

    /*
     * (non-Javadoc) Saves a jasper report as a Specify resource. @param jrf - the report to be
     * saved
     * 
     * @see it.businesslogic.ireport.gui.MainFrame#save(it.businesslogic.ireport.gui.JReportFrame)
     */
    public void save(JReportFrame jrf)
    {
        AppResourceIFace rep = getResForFrame(jrf);
        if (rep != null)
        {
            ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
            ReportWriter rw = new ReportWriter(jrf.getReport());
            rw.writeToOutputStream(xmlOut);
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                rep.setDataAsString(xmlOut.toString());
                rep.setDescription("New Name");
                session.saveOrUpdate(rep);
                session.flush();

            } catch (Exception ex)
            {
                log.error(ex);

            } finally
            {
                session.close();
            }
        }
    }

    /**
     * Finds the specify AppResourceIFace associated with an iReport report designer frame.
     * 
     * @param jrf -
     *            iReport frame interface for a report
     * @return
     */
    private AppResourceIFace getResForFrame(JReportFrame jrf)
    {
        List<AppResourceIFace> iFaces = AppContextMgr.getInstance().getResourceByMimeType(
                "jrxml/label");
        if (iFaces.size() > 0)
        {
            for (int i = 0; i < iFaces.size(); i++)
            {
                if (jrf.getReport() instanceof ReportSpecify)
                {
                    if (((ReportSpecify) (jrf.getReport())).resourceMatch(iFaces.get(i))) { return iFaces
                            .get(i); }
                }
            }
        }
        return null;
    }

    public void saveAs(JReportFrame jrf)
    {
        System.out.println("saveAs() is not implemented.");
    }

    /*
     * (non-Javadoc) Presents user with list of available report resources iReport report designer
     * frame for the selected report resource.
     * 
     * @see it.businesslogic.ireport.gui.MainFrame#open()
     */
    public JReportFrame[] open()
    {
        JReportFrame[] result = null;

        Vector<AppResourceIFace> list = new Vector<AppResourceIFace>();
        // DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(
                    "jrxml/label"))
            {
                list.add(ap);
            }
            if (list.size() > 0)
            {
                ChooseFromListDlg<AppResourceIFace> dlg = new ChooseFromListDlg<AppResourceIFace>(
                        null, "Choose Me", list);
                dlg.setVisible(true);

                AppResourceIFace appRes = dlg.getSelectedObject();

                if (appRes != null)
                {
                    result = new JReportFrame[1];
                    result[0] = openReportFromResource(appRes);
                }
            }
            return result;
        } finally
        {
            // session.close();
        }
    }

    /**
     * 
     * @param rep -
     *            a Specify report resource
     * @return - a Report designer frame for rep.
     */
    private JReportFrame findReportFrameByResource(final AppResourceIFace rep)
    {
        if (rep != null)
        {
            javax.swing.JInternalFrame[] frames = getJMDIDesktopPane().getAllFrames();
            JReportFrame jrf;
            for (int i = 0; i < frames.length; ++i)
            {
                if (frames[i] instanceof JReportFrame)
                {
                    jrf = (JReportFrame) frames[i];
                    if (jrf.getReport() instanceof ReportSpecify)
                    {
                        if (((ReportSpecify) (jrf.getReport())).resourceMatch(rep)) { return jrf; }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param rep -
     *            a specify report resource
     * @return - a iReport designer frame for rep
     */
    private JReportFrame openReportFromResource(final AppResourceIFace rep)
    {
        JReportFrame reportFrame = findReportFrameByResource(rep);
        if (reportFrame == null)
        {
            try
            {
                ReportSpecify report = makeReport(rep);
                report.setUsingMultiLineExpressions(false); // this.isUsingMultiLineExpressions());
                reportFrame = openNewReportWindow(report);
                report.addReportDocumentStatusChangedListener(this);
                setActiveReportForm(reportFrame);
            } catch (Exception e)
            {
                e.printStackTrace();
                logOnConsole(e.getMessage() + "\n");
            }

        } else
        {

            try
            {
                setActiveReportForm(reportFrame);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return reportFrame;
    }

    /**
     * @param rep -
     *            specify report resource
     * @return a Report constructed from rep's jrxml definition.
     */
    private ReportSpecify makeReport(final AppResourceIFace rep)
    {
        java.io.InputStream xmlStream = getXML(rep);
        ReportSpecify report = new ReportSpecify(rep);
        // Remove default style...
        while (report.getStyles().size() > 0)
        {
            report.removeStyle((Style) report.getStyles().get(0));
        }
        ReportReader rr = new ReportReader(report);
        try
        {
            rr.readFromStream(xmlStream);
            return report;
        } catch (IOException e)
        {
            return null;
        }
    }

    private InputStream getXML(final AppResourceIFace rep)
    {
        return new ByteArrayInputStream(rep.getDataAsString().getBytes());
    }

}

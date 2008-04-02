/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tools.IReportSpecify;

import it.businesslogic.ireport.JRField;
import it.businesslogic.ireport.Report;
import it.businesslogic.ireport.ReportReader;
import it.businesslogic.ireport.ReportWriter;
import it.businesslogic.ireport.Style;
import it.businesslogic.ireport.gui.JReportFrame;
import it.businesslogic.ireport.gui.MainFrame;
import it.businesslogic.ireport.gui.ReportPropertiesFrame;

import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.ReportsBaseTask;
import edu.ku.brc.specify.tasks.ReportsTask;
import edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceConnection;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
    private static final String REP_CHOOSE_REPORT = "REP_CHOOSE_REPORT";
    
    private static final Logger log = Logger.getLogger(MainFrameSpecify.class);

    /**
     * @param args -
     *            parameters to configure iReport mainframe
     */
    public MainFrameSpecify(Map<?,?> args)
    {
        super(args);
        setNoExit(true);
        setEmbeddedIreport(true);
    }

    public void refreshSpQBConnections()
    {
        this.getConnections().clear();
        addSpQBConns();
    }
    /**
     * adds JR data connections for specify queries.
     */
    protected void addSpQBConns()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            List<SpQuery> qs = session.getDataList(SpQuery.class);
            for (SpQuery q : qs)
            {
                addSpQBConn(q);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally
        {
            session.close();
        }
        
    }
    
    /**
     * @return a list of names of specify queries.
     */
    protected List<String> getQueryNames()
    {
        List<String> result = new LinkedList<String>();
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            List<SpQuery> qs = session.getDataList(SpQuery.class);
            for (SpQuery q : qs)
            {
                result.add(q.getName());
            }
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally
        {
            session.close();
        }
    }
    
    /**
     * @param q
     * 
     * creates and adds a JR data connection for q
     * 
     */
    @SuppressWarnings("unchecked")  //iReport doesn't parameterize generics.
    protected void addSpQBConn(final SpQuery q)
    {
        QBJRDataSourceConnection newq = new QBJRDataSourceConnection(q);
        newq.loadProperties(null);
        this.getConnections().add(newq);
    }
    
    /**
     * @return default map for specify iReport implementation
     */
    public static Map<String, String> getArgs()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("config-file", XMLHelper.getConfigDirPath("ireportconfig.xml"));
        // "noPlaf" prevents iReport from setting it's preferred theme. Don't think we need to worry
        //about the theme since laf changes should be prevented by settings in ireportconfig.xml
        map.put("noPlaf", "true");
        return map;
    }

    @Override
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
    @Override
    public void save(JReportFrame jrf)
    {
        AppResourceIFace appRes = getAppResForFrame(jrf);
        if (appRes != null)
        {
            ReportSpecify rep = (ReportSpecify)jrf.getReport();
            ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
            ReportWriter rw = new ReportWriter(rep);
            rw.writeToOutputStream(xmlOut);
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                appRes.setDataAsString(xmlOut.toString());
                AppContextMgr.getInstance().saveResource(appRes);
                
                if (rep.getSpReport() == null)
                {
                    SpReport spRep = new SpReport();
                    spRep.initialize();
                    spRep.setName(appRes.getName());
                    spRep.setAppResource((SpAppResource)appRes);
                    spRep.setQuery(rep.getConnection().getQuery());
                    spRep.setSpecifyUser(SpecifyUser.getCurrentUser());
                    session.beginTransaction();
                    session.save(spRep);
                    session.commit();
                }               
            } catch (Exception ex)
            {
                log.error(ex);

            } finally
            {
                session.close();
            }
            CommandDispatcher.dispatch(new CommandAction(ReportsBaseTask.REPORTS, ReportsBaseTask.REFRESH, null));
        }
        else
        {
            super.save(jrf);
        }
    }

    /**
     * Finds the specify AppResourceIFace associated with an iReport report designer frame.
     * 
     * @param jrf -
     *            iReport frame interface for a report
     * @return
     */
    private AppResourceIFace getAppResForFrame(final JReportFrame jrf)
    {
        AppResourceIFace result = (AppContextMgr.getInstance().getResourceFromDir("Collection", jrf.getName()));
        if (result != null)
            return result;
        return createAppResForFrame(jrf);
    }

    /**
     * @param jrf
     * @return
     */
    private AppResourceIFace createAppResForFrame(final JReportFrame jrf)
    {
        //XXX - which Dir???
        //XXX - what level???
        AppResourceIFace result = AppContextMgr.getInstance().createAppResourceForDir("Collection");
        result.setName(jrf.getReport().getName());
        result.setDescription(result.getName());
        result.setLevel((short)3); 

        RepResourcePropsPanel propPanel = new RepResourcePropsPanel((ReportSpecify)jrf.getReport(), result);
        CustomDialog cd = new CustomDialog((Frame)UIRegistry.getTopWindow(), 
                UIRegistry.getResourceString("REP_PROPS_DLG_TITLE"),
                true,
                propPanel);
        UIHelper.centerAndShow(cd);
        if (!cd.isCancelled())
        {
            result.setName(propPanel.getNameTxt().getText());
            jrf.getReport().setName(result.getName());
            result.setDescription(propPanel.getTitleTxt().getText());
            result.setLevel(Short.valueOf(propPanel.getLevelTxt().getText()));
            String metaDataStr = "tableid=-1;";
            if (propPanel.getTypeCombo().getSelectedIndex() == 0)
            {
                metaDataStr += "reporttype=Report";
                result.setMimeType("jrxml/report"); 
            }
            else
            {
                metaDataStr += "reporttype=Report";
                result.setMimeType("jrxml/label"); 
            }
            if (StringUtils.isNotEmpty(result.getMetaData()))
            {
                metaDataStr = metaDataStr + ";" + result.getMetaData();
            }
            result.setMetaData(metaDataStr);
            return result;
        }
        return null;
    }
    
    @Override
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
    @Override
    public JReportFrame[] open()
    {
        JReportFrame[] result = null;

        Vector<AppResourceIFace> list = new Vector<AppResourceIFace>();
        // DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            //XXX which of the reports should be editable? by whom? when? ...
            for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(
                    "jrxml/label"))
            {
                list.add(ap);
            }
            for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(
            "jrxml/report"))
            {
                list.add(ap);
            }
            Collections.sort(list, new Comparator<AppResourceIFace>()
                    {

                        /* (non-Javadoc)
                         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                         */
                        //@Override
                        public int compare(AppResourceIFace o1, AppResourceIFace o2)
                        {
                            // TODO Auto-generated method stub
                            return o1.toString().compareTo(o2.toString());
                        }
                        
                    });
            if (list.size() > 0)
            {
                ChooseFromListDlg<AppResourceIFace> dlg = new ChooseFromListDlg<AppResourceIFace>(
                        null, UIRegistry.getResourceString(REP_CHOOSE_REPORT), list);
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
    public JReportFrame openReportFromResource(final AppResourceIFace rep)
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

    private ReportSpecify makeNewReport(final QBJRDataSourceConnection connection)
    {
        ReportSpecify report = new ReportSpecify();
        report.setConnection(connection);
        for (int f=0; f < connection.getFields(); f++)
        {
            QBJRDataSourceConnection.QBJRFieldDef fDef = connection.getField(f);
            report.addField(new JRField(fDef.getFldName(), fDef.getFldClass().getName()));
        }
        return report;
    }
    
    private InputStream getXML(final AppResourceIFace rep)
    {
        return new ByteArrayInputStream(rep.getDataAsString().getBytes());
    }

    /* (non-Javadoc)
     * @see it.businesslogic.ireport.gui.MainFrame#newWizard()
     */
    @Override
    public Report newWizard()
    {
        List<QBJRDataSourceConnection> spConns = new Vector<QBJRDataSourceConnection>();
        for (Object conn : this.getConnections())
        {
            if (conn instanceof QBJRDataSourceConnection)
            {
                spConns.add((QBJRDataSourceConnection)conn);
            }
        }
        ChooseFromListDlg<QBJRDataSourceConnection> dlg = new ChooseFromListDlg<QBJRDataSourceConnection>(this, 
                UIRegistry.getResourceString("REP_CHOOSE_SP_QUERY"), 
                spConns);
        dlg.setVisible(true);
        if (dlg.isCancelled())
        {
            return null;
        }
        Report result = makeNewReport(dlg.getSelectedObject());
        dlg.dispose();
        if (result != null) {
            if (getReportProperties(result))
            {
                openNewReportWindow(result);
                return result;
            }
            return null;
        }
        return result;
    }
    
    /**
     * @param report
     * @return true if properties were gotten and set.
     */
    protected boolean getReportProperties(final Report report)
    {
        ReportPropertiesFrame rpf = new ReportPropertiesFrame(this,true);
        rpf.setModal(true);
        // find the first name free...
        String name = getFirstNameFree();
        rpf.setReportName( name);
        rpf.setVisible(true);
        if (rpf.getDialogResult() == javax.swing.JOptionPane.OK_OPTION) {
            // The user has clicked on OK...
            // Storing in a new report the report characteristics.
            report.setUsingMultiLineExpressions(false); //this.isUsingMultiLineExpressions());
            report.setWidth(rpf.getReportWidth());
            report.setHeight(rpf.getReportHeight());
            report.setOrientation(rpf.getOrientation());
            report.setName(rpf.getReportName());
            report.setTopMargin(rpf.getTopMargin());
            report.setLeftMargin(rpf.getLeftMargin());
            report.setRightMargin(rpf.getRightMargin());
            report.setBottomMargin(rpf.getBottomMargin());
            report.setColumnCount(rpf.getColumns());
            report.setColumnWidth(rpf.getColumnsWidth());
            report.setColumnSpacing(rpf.getColumnsSpacing());
            report.setIsSummaryNewPage(rpf.isSummaryOnNewPage());
            report.setIsTitleNewPage(rpf.isTitleOnNewPage());
            report.setWhenNoDataType(rpf.getWhenNoDataType());
            report.setScriptletClass(rpf.getScriptletClass());
            report.setEncoding(rpf.getXmlEncoding());
            report.setPrintOrder(rpf.getPrintOrder());
            report.setReportFormat(rpf.getReportFormat());
            report.setFloatColumnFooter(rpf.isFloatColumnFooter());
            report.setResourceBundleBaseName( rpf.getResourceBundleBaseName() );
            report.setWhenResourceMissingType( rpf.getWhenResourceMissingType());
            report.setIgnorePagination(rpf.isIgnorePagination());
            report.setFormatFactoryClass(rpf.getFormatFactoryClass());
            report.setLanguage( rpf.getLanguage() );
            return true;
        }
        return false;
    }
}

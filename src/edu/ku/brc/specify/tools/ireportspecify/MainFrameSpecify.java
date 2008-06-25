/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tools.ireportspecify;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceConnection;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.ui.weblink.WebLinkMgr;

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
    protected static final String REP_CHOOSE_REPORT = "REP_CHOOSE_REPORT";
    
    protected boolean refreshingConnections = false;

    /**
     * @param args -
     *            parameters to configure iReport mainframe
     */
    public MainFrameSpecify(Map<?,?> args, boolean noExit, boolean embedded)
    {
        super(args);
        setNoExit(noExit);
        setEmbeddedIreport(embedded);
    }

    public void refreshSpQBConnections()
    {
        refreshingConnections = true;
        try
        {
            this.getConnections().clear();
            addSpQBConns();
        }
        finally
        {
            refreshingConnections = false;
        }
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
            Collections.sort(qs, new Comparator<SpQuery>() {

                /* (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                //@Override
                public int compare(SpQuery o1, SpQuery o2)
                {
                    return o1.toString().compareTo(o2.toString());
                }
            });
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
    public static Map<String, String> getDefaultArgs()
    {
        Map<String, String> map = new HashMap<String, String>();
        //Don't really need to worry about these args when using in-house iReport.jar
        
        //map.put("config-file", XMLHelper.getConfigDirPath("ireportconfig.xml"));
        // "noPlaf" prevents iReport from setting it's preferred theme. Don't think we need to worry
        //about the theme since laf changes should be prevented by settings in ireportconfig.xml
        //map.put("noPlaf", "true");
        return map;
    }

    @Override
    public void saveAll(javax.swing.JInternalFrame[] frames)
    {
        System.out.println("saveAll() is not implemented.");
    }

    /**
     * @param jasperFile
     * @return true if the report is successfully imported, otherwise return false.
     */
    public static boolean importJasperReport(final File jasperFile)
    {
        ByteArrayOutputStream xml = null;
        try
        {
            xml = new ByteArrayOutputStream();
            xml.write(FileUtils.readFileToByteArray(jasperFile));
        }
        catch (IOException e)
        {
            UIRegistry.getStatusBar().setErrorMessage(e.getLocalizedMessage(), e);
            return false;
        }
        AppResourceIFace appRes = getAppRes(jasperFile.getName(), null, true);
        if (appRes != null)
        {
            String metaData = appRes.getMetaData();
            if (StringUtils.isEmpty(metaData))
            {
                metaData = "isimport=1";
            }
            else
            {
                metaData += ";isimport=1";
            }
            appRes.setMetaData(metaData);
            return saveXML(xml, appRes, null);
        }
        return false;
    }
    
    /**
     * @param xml - data to be assigned to appRes
     * @param appRes - appRes to be updated and saved
     * @param rep - ReportSpecify object associataed with appRes
     * @return true if everything turns out OK. Otherwise return false.
     */
    protected static boolean saveXML(final ByteArrayOutputStream xml, final AppResourceIFace appRes, final ReportSpecify rep)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        boolean result = false;
        boolean transOpen = false;
        boolean newRep = ((SpAppResource)appRes).getId() == null;
        try
        {
            if (!newRep)
            {
                session.attach(appRes);
            }
            appRes.setDataAsString(xml.toString());
            AppContextMgr.getInstance().saveResource(appRes);
            
            if (rep != null)
            {
                boolean createReport = true;
                if (rep.getSpReport() != null)
                {
                    try
                    {
                        session.refresh(rep.getSpReport());
                        createReport = false;
                        result = true;
                    }
                    catch (org.hibernate.UnresolvableObjectException e)
                    {
                        log.debug("Report has been deleted in Specify? (" + e + ")");
                    }
                }
                SpReport spRep;
                if (createReport)
                {
                    spRep = new SpReport();
                    spRep.initialize();
                }
                else
                {
                    spRep = rep.getSpReport();
                }
                spRep.setName(appRes.getName());
                spRep.setAppResource((SpAppResource) appRes);
                SpQuery q = rep.getConnection().getQuery();
                // getting a fresh copy of the Query might be helpful
                // in case one of its reports was deleted, but is probably
                // no longer necessary with AppContextMgr.getInstance().setContext() call
                // in the save method.
                SpQuery freshQ = session.get(SpQuery.class, q.getId());
                if (freshQ != null)
                {
                    spRep.setQuery(freshQ);
                    spRep.setSpecifyUser(AppContextMgr.getInstance().getClassObject(
                            SpecifyUser.class));
                    session.beginTransaction();
                    transOpen = true;
                    session.save(spRep);
                    session.commit();
                    transOpen = false;
                    rep.setSpReport(spRep);
                    result = true;
                }
            }               
        } catch (Exception ex)
        {
            if (transOpen)
            {
               session.rollback();
            }
            throw new RuntimeException(ex);
        } finally
        {
            if (!newRep)
            {
                session.evict(appRes);
            }
            if (newRep && !result)
            {
                //XXX - more 'Collection' hard-coding
                AppContextMgr.getInstance().removeAppResource("Collection", appRes);
            }
            session.close();
        }
        return result;
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
        //Reloading the context to prevent weird Hibernate issues that occur when resources are deleted in a
        //concurrently running instance of Specify. 
        AppContextMgr.getInstance().setContext(((SpecifyAppContextMgr)AppContextMgr.getInstance()).getDatabaseName(), 
                ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getUserName(), 
                false);

        AppResourceIFace appRes = getAppResForFrame(jrf);
        if (appRes != null)
        {
            ReportSpecify rep = (ReportSpecify)jrf.getReport();
            ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
            ReportWriter rw = new ReportWriter(rep);
            rw.writeToOutputStream(xmlOut);
            
            boolean success = saveXML(xmlOut, appRes, rep);
            if (!success)
            {
                JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), UIRegistry.getResourceString("REP_UNABLE_TO_SAVE_IREPORT"), UIRegistry.getResourceString("Error"), JOptionPane.ERROR_MESSAGE);                        
            }
        }
//        else
//        {
//            super.save(jrf);
//        }
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
    	//XXX - hard-coded for 'Collection' directory.
    	AppResourceIFace result = AppContextMgr.getInstance().getResourceFromDir("Collection", jrf.getReport().getName());
        if (result != null)
        {
            if (((ReportSpecify) jrf.getReport()).getSpReport() != null) { return result; }
            int response = JOptionPane.showConfirmDialog(UIRegistry.getTopWindow(), String.format(UIRegistry
                    .getResourceString("REP_CONFIRM_IMP_OVERWRITE"), jrf.getReport().getName()),
                    UIRegistry.getResourceString("REP_CONFIRM_IMP_OVERWRITE_TITLE"), JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null);
            if (response == JOptionPane.CANCEL_OPTION || response == -1 /*closed with x-box*/) { return null; }
            if (response == JOptionPane.YES_OPTION) { return result; }
            result = null;
        }
        // else
        result = createAppRes(jrf.getReport().getName(), -1);
        if (result != null)
        {
            jrf.getReport().setName(result.getName());
        }
        return result;
    }

    /**
     * @param appResName
     * @param tableid
     * @return AppResource with the provided name.
     * 
     * If a resource named appResName exists it will be returned, else a new resource is created.
     */
    private static AppResourceIFace getAppRes(final String appResName, final Integer tableid, final boolean confirmOverwrite)
    {
        //XXX - hard-coded for 'Collection' directory.
        AppResourceIFace result = AppContextMgr.getInstance().getResourceFromDir("Collection", appResName);
        if (result != null)
        {
            if (!confirmOverwrite)
            {
                return result;
            }
            //else
            int option = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                    String.format(UIRegistry.getResourceString("REP_CONFIRM_IMP_OVERWRITE"), result.getName()),
                    UIRegistry.getResourceString("REP_CONFIRM_IMP_OVERWRITE_TITLE"), 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION); 
            
            if (option == JOptionPane.YES_OPTION)
            {
                return result;
            }
            //else
            return null;
        }
        //else
        return createAppRes(appResName, tableid);
    }
    
    /**
     * @param jrf
     * @return a new AppResource
     */
    private static AppResourceIFace createAppRes(final String repResName, final Integer tableid)
    {
        //XXX - which Dir???
        //XXX - what level???
        AppResourceIFace result = AppContextMgr.getInstance().createAppResourceForDir("Collection");
        result.setName(repResName);
        result.setDescription(result.getName());
        result.setLevel((short)3); 

        RepResourcePropsPanel propPanel = new RepResourcePropsPanel(repResName, result, tableid == null);
        boolean goodProps = false;
        CustomDialog cd = new CustomDialog((Frame)UIRegistry.getTopWindow(), 
                UIRegistry.getResourceString("REP_PROPS_DLG_TITLE"),
                true,
                propPanel);
        while (!goodProps)
        {
            UIHelper.centerAndShow(cd);
            if (cd.isCancelled())
            {
                return null;
            }
            if (StringUtils.isEmpty(propPanel.getNameTxt().getText()))
            {
                JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), String.format(UIRegistry.getResourceString("REP_NAME_MUST_NOT_BE_BLANK"), propPanel.getNameTxt().getText()));
            }
            //XXX - more 'Collection' dir hard-coding
            else if (AppContextMgr.getInstance().getResourceFromDir("Collection", propPanel.getNameTxt().getText()) != null)
            {
                JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), String.format(UIRegistry.getResourceString("REP_NAME_ALREADY_EXISTS"), propPanel.getNameTxt().getText()));
            }
            else
            {
                goodProps = true;
            }
        }    
        if (goodProps /*just in case*/)
        {
            result.setName(propPanel.getNameTxt().getText());
            //result.setDescription(propPanel.getTitleTxt().getText());
            result.setDescription(propPanel.getNameTxt().getText());
            result.setLevel(Short.valueOf(propPanel.getLevelTxt().getText()));
            String metaDataStr = "tableid=" + propPanel.getTableId() + ";";
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
            String[] mimes = {"jrxml/label", "jrxml/report"};
            for (int m = 0; m < mimes.length; m++)
            {
                for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(mimes[m]))
                {
                    Properties params = ap.getMetaDataMap();
                    
                    String tableid = params.getProperty("tableid"); //$NON-NLS-1$
                    String rptType = params.getProperty("reporttype"); //$NON-NLS-1$
                    
                    if (StringUtils.isNotEmpty(tableid) && 
                       (StringUtils.isNotEmpty(rptType) && rptType.equals("Report")))
                    {
                        list.add(ap);
                    }
                }
            }
            
            Collections.sort(list, new Comparator<AppResourceIFace>()
            {

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                // @Override
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
                report.setConnection(getConnectionByQuery(report.getSpReport().getQuery()));
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

    protected QBJRDataSourceConnection getConnectionByQuery(final SpQuery query)
    {
        for (int c = 0; c < getConnections().size(); c++)
        {
            QBJRDataSourceConnection conn = (QBJRDataSourceConnection )getConnections().get(c);
            if (conn.getName().equals(query.getName()))
            {
                return conn;
            }
        }
        return null;
    }
    
    /**
     * @param rep -
     *            specify report resource
     * @return a Report constructed from rep's jrxml definition.
     */
    private ReportSpecify makeReport(final AppResourceIFace rep)
    {
        java.io.InputStream xmlStream = getXML(rep);
        ReportSpecify report = null;
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            SpReport spRep = session.getData(SpReport.class, "appResource", 
                  rep, DataProviderSessionIFace.CompareType.Equals);
            if (spRep != null)
            {
                report = new ReportSpecify(spRep);
            }
            else
            {
                report = new ReportSpecify(rep);
            }
        }
        finally
        {
            session.close();
        }
        
        
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
        
    
    /* (non-Javadoc)
     * @see it.businesslogic.ireport.gui.MainFrame#getConnections()
     */
    @Override
    @SuppressWarnings("unchecked") //iReport ignores Template/Generic stuff
    public Vector getConnections()
    {
        if (!refreshingConnections)
        {
            this.refreshSpQBConnections(); //in case new query has been in concurrent instance of Specify6
        }
        return super.getConnections();
    }

    protected static void adjustLocaleFromPrefs()
    {
        String language = AppPreferences.getLocalPrefs().get("locale.lang", null); //$NON-NLS-1$
        if (language != null)
        {
            String country  = AppPreferences.getLocalPrefs().get("locale.country", null); //$NON-NLS-1$
            String variant  = AppPreferences.getLocalPrefs().get("locale.var",     null); //$NON-NLS-1$
            
            Locale prefLocale = new Locale(language, country, variant);
            
            Locale.setDefault(prefLocale);
            UIRegistry.setResourceLocale(prefLocale);
        }
        
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String appDir = System.getProperty("appdir"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(appDir))
        {
            UIRegistry.setDefaultWorkingPath(appDir);
        }
        
        String appdatadir = System.getProperty("appdatadir"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(appdatadir))
        {
            UIRegistry.setBaseAppDataDir(appdatadir);
        }
        
        String javadbdir = System.getProperty("javadbdir"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(javadbdir))
        {
            UIRegistry.setJavaDBDir(javadbdir);
        }

        // Set App Name, MUST be done very first thing!
        UIRegistry.setAppName("iReports4Specify");  //$NON-NLS-1$
        //UIRegistry.setAppName("Specify");  //$NON-NLS-1$
        
        // Then set this
        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$

        
        
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences //$NON-NLS-1$
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory"); //$NON-NLS-1$
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");           // Needed for CatalogNumberign //$NON-NLS-1$
        System.setProperty(QueryAdjusterForDomain.factoryName,          "edu.ku.brc.specify.dbsupport.SpecifyQueryAdjusterForDomain"); // Needed for ExpressSearch //$NON-NLS-1$
        System.setProperty(SchemaI18NService.factoryName,               "edu.ku.brc.specify.config.SpecifySchemaI18NService");         // Needed for Localization and Schema //$NON-NLS-1$
        System.setProperty(WebLinkMgr.factoryName,                      "edu.ku.brc.specify.config.SpecifyWebLinkMgr");                // Needed for WebLnkButton //$NON-NLS-1$
        
        
        if (StringUtils.isEmpty(UIRegistry.getJavaDBPath()))
        {
            File userDataDir = new File(UIRegistry.getAppDataDir() + File.separator + "DerbyDatabases"); //$NON-NLS-1$
            UIRegistry.setJavaDBDir(userDataDir.getAbsolutePath());
        }
        log.debug(UIRegistry.getJavaDBPath());

        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        adjustLocaleFromPrefs();

        HibernateUtil.setListener("post-commit-update", new edu.ku.brc.specify.dbsupport.PostUpdateEventListener()); //$NON-NLS-1$
        HibernateUtil.setListener("post-commit-insert", new edu.ku.brc.specify.dbsupport.PostInsertEventListener()); //$NON-NLS-1$
        HibernateUtil.setListener("post-commit-delete", new edu.ku.brc.specify.dbsupport.PostDeleteEventListener()); //$NON-NLS-1$
        
       UIHelper.doLogin(true, false, false, new IReportLauncher(), null, "iReport"); // true means do auto login if it can, second bool means use dialog instead of frame
       
       localPrefs.load();
    }
}

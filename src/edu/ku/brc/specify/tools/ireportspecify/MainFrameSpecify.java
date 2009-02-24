/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tools.ireportspecify;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import it.businesslogic.ireport.JRField;
import it.businesslogic.ireport.Report;
import it.businesslogic.ireport.ReportReader;
import it.businesslogic.ireport.ReportWriter;
import it.businesslogic.ireport.Style;
import it.businesslogic.ireport.gui.JReportFrame;
import it.businesslogic.ireport.gui.MainFrame;
import it.businesslogic.ireport.gui.ReportPropertiesFrame;

import java.awt.Component;
import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
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
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceData;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.ReportsBaseTask;
import edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceConnection;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
    private static final Logger   log                         = Logger
                                                                      .getLogger(MainFrameSpecify.class);

    protected static final String    REP_CHOOSE_REPORT           = "REP_CHOOSE_REPORT";

    protected boolean             refreshingConnections       = false;

    protected static Integer      overwrittenReportId         = null;
    
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
            // XXX Added userId condition to be consistent with QueryTask, but, Users will probably want to share queries??
            String sqlStr = "From SpQuery where specifyUserId = "
                    + AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getSpecifyUserId();
            List<?> qs = session.createQuery(sqlStr, false).list();
            Collections.sort(qs, new Comparator<Object>() {

                /* (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                //@Override
                public int compare(Object o1, Object o2)
                {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            for (Object q : qs)
            {
                addSpQBConn((SpQuery )q);
            }
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
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
        if (!UIHelper.isSecurityOn() ||
                DBTableIdMgr.getInstance().getInfoById(q.getContextTableId()).getPermissions().canView())
        {
            QBJRDataSourceConnection newq = new QBJRDataSourceConnection(q);
            newq.loadProperties(null);
            this.getConnections().add(newq);
        }
    }
    
    /**
     * @return default map for specify iReport implementation
     */
    public static Map<String, Object> getDefaultArgs()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        //Don't need to worry about these args when using launching iReport as separate app.
        
        //map.put("config-file", XMLHelper.getConfigDirPath("ireportconfig.xml"));
        // "noPlaf" prevents iReport from setting it's preferred theme. Don't think we need to worry
        //about the theme since laf changes should be prevented by settings in ireportconfig.xml
        //map.put("noPlaf", "true");
        
        
        return map;
    }

    /* (non-Javadoc)
     * @see it.businesslogic.ireport.gui.MainFrame#saveAll(javax.swing.JInternalFrame[])
     */
    @Override
    public void saveAll(javax.swing.JInternalFrame[] frames)
    {
        for (int f = 0; f < frames.length; f++)
        {
            if (frames[f]  instanceof JReportFrame) 
            {
                JReportFrame jrf = (JReportFrame )frames[f];
                if (jrf.getReport() instanceof ReportSpecify && jrf.getReport().isModified())
                {
                    save(jrf);
                }
            }
        }
    }

    /**
     * @param jasperFile
     * @return true if the report is successfully imported, otherwise return false.
     */
    public static boolean importJasperReport(final File jasperFile, boolean confirmOverwrite)
    {
        ByteArrayOutputStream xml = null;
        try
        {
            xml = new ByteArrayOutputStream();
            xml.write(FileUtils.readFileToByteArray(jasperFile));
        }
        catch (IOException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
            UIRegistry.getStatusBar().setErrorMessage(e.getLocalizedMessage(), e);
            return false;
        }
        AppResAndProps resApp = getAppRes(jasperFile.getName(), null, confirmOverwrite);
        if (resApp != null)
        {
            String metaData = resApp.getAppRes().getMetaData();
            String newMetaData = "isimport=1";
            try
            {
                Element element = XMLHelper.readFileToDOM4J(jasperFile);
                List<?> parameters = element.selectNodes("/jasperReport/parameter");
                boolean isDropSite = false;
                for (Object p : parameters)
                {
                    Element param = (Element) p;
                    if (param.attributeValue("name").equals(ReportsBaseTask.RECORDSET_PARAM))
                    {
                        isDropSite = true;
                        break;
                    }
                }   
                if (isDropSite)
                {
                    newMetaData += ";hasrsdropparam=1";
                }
                else
                {
                    newMetaData += ";hasrsdropparam=0";
                }
            }
            catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
                UIRegistry.getStatusBar().setErrorMessage(e.getLocalizedMessage(), e);
                return false;
            }
            if (StringUtils.isEmpty(metaData))
            {
                metaData = newMetaData;
            }
            else
            {
                metaData += ";" + newMetaData;
            }
            resApp.getAppRes().setMetaData(metaData);
            return saveXML(xml, resApp, null, false);
        }
        return false;
    }
    
    /**
     * @param xml - data to be assigned to appRes
     * @param appRes - appRes to be updated and saved
     * @param rep - ReportSpecify object associataed with appRes
     * @return true if everything turns out OK. Otherwise return false.
     */
    protected static boolean saveXML(final ByteArrayOutputStream xml, final AppResAndProps apr, final ReportSpecify rep, boolean saveAs)
    {
        AppResourceIFace appRes = apr.getAppRes();
        boolean newRep = ((SpAppResource)appRes).getId() == null;
        boolean result = false;
        boolean savedAppRes = false;
        String xmlString = xml.toString();
        if (rep != null)
        {
            xmlString = modifyXMLForSaving(xmlString, rep);
        }
        appRes.setDataAsString(xmlString);
        try
        {
        	savedAppRes = AppContextMgr.getInstance().saveResource(appRes);
        }
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, ex);
            return false;
        }
        
        if (!savedAppRes)
        {
        	return false;
        }
        
        if (rep == null)
        {
            return true;
        }
        else
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            boolean transOpen = false;
            boolean createReport = true;
            try
            {
                if (rep.getSpReport() != null && !saveAs)
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
                SpAppResource freshRes = session.get(SpAppResource.class, ((SpAppResource )appRes).getId());
                rep.setAppResource(freshRes);
                spRep.setAppResource((SpAppResource) freshRes);
                spRep.setRepeats(apr.getRepeats());
                SpQuery q = rep.getConnection().getQuery();
                // getting a fresh copy of the Query might be helpful
                // in case one of its reports was deleted, but is probably
                // no longer necessary with AppContextMgr.getInstance().setContext() call
                // in the save method.
                SpQuery freshQ = session.get(SpQuery.class, q.getId());
                if (freshQ != null)
                {
                    spRep.setQuery(freshQ);
                    spRep.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
                    session.beginTransaction();
                    transOpen = true;
                    session.save(spRep);
                    session.commit();
                    transOpen = false;
                    //refresh report, just because...
                    session.refresh(spRep);
                    rep.setSpReport(spRep);
                    result = true;
                }
            }catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, ex);
                if (transOpen)
                {
                   session.rollback();
                }
                throw new RuntimeException(ex);
            }        
            finally
			{
				if (!newRep)
				{
					session.evict(appRes);
				}
				if (newRep && !result)
				{
					SpecifyAppContextMgr spMgr = (SpecifyAppContextMgr) AppContextMgr
							.getInstance();
					SpAppResource spRes = (SpAppResource) appRes;
					spMgr.removeAppResourceSp(spRes.getSpAppResourceDir(),
							spRes);
				}
				session.close();
			}
			return result;
		}               
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
        doSave(jrf, false);
    }
    
    protected void doSave(JReportFrame jrf, boolean saveAs)
    {
        //Reloading the context to prevent weird Hibernate issues that occur when resources are deleted in a
        //concurrently running instance of Specify. 
        ((SpecifyAppContextMgr )AppContextMgr.getInstance()).setContext(((SpecifyAppContextMgr)AppContextMgr.getInstance()).getDatabaseName(), 
                ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getUserName(), 
                true, false);

        if (UIHelper.isSecurityOn())
        {
            PermissionIFace permissions = SecurityMgr.getInstance().getPermission("Task.Reports");
            if (!permissions.canModify())
            {
                JOptionPane.showMessageDialog(null, getResourceString("IReportLauncher.PERMISSION_TO_MODIFY_DENIED"),
                        getResourceString("IReportLauncher.PERMISSION_DENIED_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        overwrittenReportId = null;
        AppResAndProps apr = getAppResAndPropsForFrame(jrf, saveAs);
        if (overwrittenReportId != null)
        {
            removeFrameForDeletedReport(overwrittenReportId);
            overwrittenReportId = null;
        }
        
        if (apr != null)
        {
            ReportSpecify rep = (ReportSpecify)jrf.getReport();
            ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
            try
            {
            	modifyFieldsForSaving(rep);
            	ReportWriter rw = new ReportWriter(rep);
            	rw.writeToOutputStream(xmlOut);
            }
            finally
            {
            	modifyFieldsForEditing(rep);
            }
            boolean success = saveXML(xmlOut, apr, rep, saveAs);
            if (success)
            {
                jrf.setIsDocDirty(false);
                jrf.getReport().setReportChanges(0);                
            }
            else
            {
                JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), UIRegistry.getResourceString("REP_UNABLE_TO_SAVE_IREPORT"), UIRegistry.getResourceString("Error"), JOptionPane.ERROR_MESSAGE);                        
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
    private AppResAndProps getAppResAndPropsForFrame(final JReportFrame jrf, boolean saveAs)
    {
        /* RULE: SpReport.name == SpAppResource.name (== jrf.getReport().name)*/
        SpReport spRep = ((ReportSpecify) jrf.getReport()).getSpReport();
        AppResourceIFace appRes = null;
        
        if (!saveAs)
    	{
            appRes = spRep == null ? getRepResource(jrf.getReport().getName()) :
    	            spRep.getAppResource();
    	}
        if (appRes != null)
        {
            if (spRep != null) 
            { 
                appRes.setTimestampModified(new Timestamp(System.currentTimeMillis()));
                return getProps(jrf.getReport().getName(), -1, (ReportSpecify )jrf.getReport(), appRes);
            }
        }
        // else
        if (UIHelper.isSecurityOn())
        {
            PermissionIFace permissions = SecurityMgr.getInstance().getPermission("Task.Reports");
            if (!permissions.canAdd())
            {
                JOptionPane.showMessageDialog(null, getResourceString("IReportLauncher.PERMISSION_TO_ADD_DENIED"),
                        getResourceString("IReportLauncher.PERMISSION_DENIED_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        AppResAndProps result = createAppResAndProps(jrf.getReport().getName(), -1, (ReportSpecify )jrf.getReport());
        if (result != null)
        {
            jrf.getReport().setName(result.getAppRes().getName());
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
    private static AppResAndProps getAppRes(final String appResName, final Integer tableid, final boolean confirmOverwrite)
    {
        AppResourceIFace resApp = AppContextMgr.getInstance().getResource(appResName);
        if (resApp != null)
        {
            if (!confirmOverwrite)
            {
                return new AppResAndProps(resApp, null);
            }
            //else
            int option = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                    String.format(UIRegistry.getResourceString("REP_CONFIRM_IMP_OVERWRITE"), resApp.getName()),
                    UIRegistry.getResourceString("REP_CONFIRM_IMP_OVERWRITE_TITLE"), 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION); 
            
            if (option == JOptionPane.YES_OPTION)
            {
                return new AppResAndProps(resApp, null);
            }
            //else
            return null;
        }
        //else
        return createAppResAndProps(appResName, tableid, null);
    }
    
    /**
     * @param repResName
     * @return a report or label resource named repResName.
     */
    protected static SpAppResource getRepResource(final String repResName)
    {
    	List<AppResourceIFace> reps = AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.LABELS_MIME);
    	reps.addAll(AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.REPORTS_MIME));
    	for (AppResourceIFace rep : reps)
    	{
    		if (rep.getName().equals(repResName))
    		{
    			return (SpAppResource )rep;
    		}
    	}
    	return null;
    }
    
    /**
     * @param repResName
     * @param tableId
     * @param rep
     * @param appRes
     * 
     * Allows editing of SpReport and SpAppResource properties for reports.
     */
    protected static AppResAndProps getProps(final String repResName, final Integer tableId, final ReportSpecify rep, 
    		final AppResourceIFace appRes)
    {
        String repType;
        if (appRes == null)
        {
            repType = "Report";
        }
        else
        {
            String mime = appRes.getMimeType();
            String reportType = appRes.getMetaDataMap().getProperty("reporttype", null);
            if (mime.equals(ReportsBaseTask.LABELS_MIME))
            {
            	repType = "Label";
            }
            else if (mime.equals(ReportsBaseTask.SUBREPORTS_MIME))
            {
            	repType = "Subreport";
            }
            else
            {
            	if (reportType != null && reportType.equalsIgnoreCase("invoice"))
            	{
            		repType = "Invoice";
            	}
            	else
            	{
            		repType = "Report";
            	}
            }
        }
        RepResourcePropsPanel propPanel = new RepResourcePropsPanel(repResName, repType, tableId == null, rep);
        boolean goodProps = false;
        boolean overwrite = false;
        SpAppResource match = null;
        CustomDialog cd = new CustomDialog((Frame)UIRegistry.getTopWindow(), 
                UIRegistry.getResourceString("REP_PROPS_DLG_TITLE"),
                true,
                propPanel);
        propPanel.setCanceller(cd.getCancelBtn());
        while (!goodProps)
        {
            UIHelper.centerAndShow(cd);
            if (cd.isCancelled())
            {
                return null;
            }
            if (StringUtils.isEmpty(propPanel.getNameTxt().getText().trim()))
            {
                JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), String.format(UIRegistry.getResourceString("REP_NAME_MUST_NOT_BE_BLANK"), propPanel.getNameTxt().getText()));
            }
            else 
            {
            	match = getRepResource(propPanel.getNameTxt().getText());
            	if (match != null)
            	{
            		if (appRes == null || !((SpAppResource )appRes).getId().equals(match.getId()))
            		{
                        int chc = JOptionPane.showConfirmDialog(UIRegistry.getTopWindow(), String.format(UIRegistry.getResourceString("REP_NAME_ALREADY_EXISTS_OVERWRITE_CONFIRM"), propPanel.getNameTxt().getText()));
                        if (chc == JOptionPane.OK_OPTION)
                        {
                        	goodProps = true;
                        	overwrite = true;
                        }
                        else if (chc != JOptionPane.NO_OPTION)
                        {
                        	return null;
                        }
            		} 
            		else
            		{
            			goodProps = true;
            		}
            	}	 
            	else
            	{
            		goodProps = true;
            	}
 
                goodProps = goodProps && propPanel.validInputs();
            }
        }    
        if (goodProps /*just in case*/)
        {
            if (match != null && overwrite)
            {
                //user has chosen to overwrite an identically named report
                //XXX - Is it possible that another user created the matching report?
                
                //first close design frame for match if one exists.
                /*
                 * Actually, never mind, too hard to do in this method.
                 * Let the user deal with it.
                 */
                
                //delete match
                Integer matchId = null;
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
                    SpReport matchRep = session.getData(SpReport.class, "appResource", match, DataProviderSessionIFace.CompareType.Equals);
                    if (matchRep == null)
                    {
                        JOptionPane.showMessageDialog(null, String.format(UIRegistry.getResourceString("REP_UNABLE_TO_OVERWRITE"), match.getName()), 
                                UIRegistry.getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    matchId = matchRep.getId();
                }
                finally
                {
                    session.close();
                    session = null;
                }
                ReportsBaseTask.deleteReportAndResource(matchId, match);
                overwrittenReportId = matchId;
            }
            
            AppResourceIFace modifiedRes = null;
            if (appRes == null)
            {
                //XXX - what level???
                modifiedRes = AppContextMgr.getInstance().createAppResourceForDir(propPanel.getResDirCombo().getSelectedItem().toString());
            }
            else
            {
                modifiedRes = appRes;
                String dirName = propPanel.getResDirCombo().getSelectedItem().toString();
                SpAppResourceDir dir = ((SpecifyAppContextMgr) AppContextMgr.getInstance()).getSpAppResourceDirByName(dirName);
                ((SpAppResource )modifiedRes).setSpAppResourceDir(dir);
            }
            modifiedRes.setName(propPanel.getNameTxt().getText().trim());
            modifiedRes.setDescription(propPanel.getNameTxt().getText().trim());
            modifiedRes.setLevel(Short.valueOf(propPanel.getLevelTxt().getText()));
            
            propPanel.getResDirCombo().getSelectedItem();
            String metaDataStr = "tableid=" + propPanel.getTableId() + ";";
            if (propPanel.getTypeCombo().getSelectedIndex() == 2)
            {
            	metaDataStr += "reporttype=Invoice;";
            }
            else
            {
            	metaDataStr += "reporttype=Report;";
            }
            if (propPanel.getSubReportsTxt() != null && propPanel.getSubReportsTxt().getText() != null)
            {
            	metaDataStr += "subreports=" + propPanel.getSubReportsTxt().getText() + ";";
            }
            
            if (propPanel.getTypeCombo().getSelectedIndex() == 3)
            {
                modifiedRes.setMimeType("jrxml/subreport"); 
            }
            else if (propPanel.getTypeCombo().getSelectedIndex() == 1)
            {
                modifiedRes.setMimeType("jrxml/label"); 
            }
            else
            {
                modifiedRes.setMimeType("jrxml/report"); 
            }
           	
            if (StringUtils.isNotEmpty(modifiedRes.getMetaData()))
            {
                /* Assuming ReportResources only get edited by this class...
                metaDataStr = metaDataStr + ";" + modifiedRes.getMetaData();*/
                log.info("overwriting existing AppResource metadata (" + modifiedRes.getMetaData() + ") with (" + metaDataStr + ")");
            }
            modifiedRes.setMetaData(metaDataStr);
            AppResAndProps result = new AppResAndProps(modifiedRes, propPanel.getRepeats());
            return result;
        }
        return null;
    }
    
    protected void removeFrameForDeletedReport(final Integer reportId)
    {
        if (reportId != null)
        {
            javax.swing.JInternalFrame[] frames = getJMDIDesktopPane().getAllFrames();
            JReportFrame jrf;
            Vector<Component> toRemove = new Vector<Component>();
            for (int i = 0; i < frames.length; ++i)
            {
                if (frames[i] instanceof JReportFrame)
                {
                    jrf = (JReportFrame) frames[i];
                    if (jrf.getReport() instanceof ReportSpecify  && ((ReportSpecify) (jrf.getReport())).getSpReport() != null)
                    {
                        if (reportId.equals(((ReportSpecify) (jrf.getReport())).getSpReport().getId()))
                        {
                            toRemove.add(jrf);
                        }
                    }
                }
            }
            for (Component c : toRemove)
            {
                getJMDIDesktopPane().remove(c);
            }
        }        
    }
    
    /**
     * @param jrf
     * @return a new AppResource
     */
    protected static AppResAndProps createAppResAndProps(final String repResName, final Integer tableid, final ReportSpecify rep)
    {
        return getProps(repResName, tableid, rep, null);
    }
    
    @Override
    public void saveAs(JReportFrame jrf)
    {
        doSave(jrf, true);
        setActiveReportForm(jrf);    
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
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {

            //XXX which of the reports should be editable? by whom? when? ...
            String[] mimes = {"jrxml/label", "jrxml/report"};
            for (int m = 0; m < mimes.length; m++)
            {
                
                for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(mimes[m]))
                {
                    if (ap instanceof SpAppResource)
                    {
                        if (((SpAppResource) ap).getSpAppResourceId() != null)
                        {
                            session.attach(ap);
                            if (session.getData(SpReport.class, "appResource", ap,
                                    DataProviderSessionIFace.CompareType.Equals) != null)
                            {
                                Properties params = ap.getMetaDataMap();

                                String tableid = params.getProperty("tableid"); //$NON-NLS-1$
                                String rptType = params.getProperty("reporttype"); //$NON-NLS-1$

                                if (StringUtils.isNotEmpty(tableid)
                                        && (StringUtils.isNotEmpty(rptType) && rptType
                                                .equals("Report")))
                                {
                                    list.add(ap);
                                }
                            }
                            session.evict(ap);
                        }
                    }
                }
            }
        } finally
        {
            session.close();
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
            else
            {
                JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), UIRegistry.getResourceString("REP_NO_REPORTS_TO_EDIT"), "", JOptionPane.INFORMATION_MESSAGE);
            }
            return result;
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
     * @param xml
     * @param spRep
     * @return a copy of xml with all field expressions modified to use user-friendly 'titles'.
     */
    protected String modifyXMLForEditing(String xml, final SpReport spRep)
    {
    	String result = new String(xml);
    	if (spRep != null)
    	{
    		QBJRDataSourceConnection c = getConnectionByQuery(spRep.getQuery());
    		if (c != null)
    		{
    			for (int f = 0; f < c.getFields(); f++)
    			{
    				QBJRDataSourceConnection.QBJRFieldDef fld = c.getField(f);
    				result = result.replace("$F{" + fld.getFldName() + "}", "$F{" + fld.getFldTitle() + "}");
    			}
    		}
    	}
    	return result;
    }
    
    /**
     * @param xml
     * @param rep
     * @return a copy of xml with all field expressions modified to use localization-independent identifiers.
     */
    protected static String modifyXMLForSaving(String xml, final ReportSpecify rep) 
    {
		String result = new String(xml);
    	QBJRDataSourceConnection c = rep.getConnection();
		if (c != null) 
		{
			for (int f = 0; f < c.getFields(); f++) {
				QBJRDataSourceConnection.QBJRFieldDef fld = c.getField(f);
 				result = result.replace("$F{" + fld.getFldTitle() + "}", "$F{" + fld.getFldName() + "}");
			}
		}
		return result;
	}
    
    /**
     * @param report
     * 
     * Sets field names to user-friendly 'titles'.
     */
    protected void modifyFieldsForEditing(ReportSpecify report)
    {
    	for (Object jrfObj : report.getFields())
    	{
    		JRField jrf = (JRField )jrfObj;
    		QBJRDataSourceConnection.QBJRFieldDef qbjrf = report.getConnection().getFieldByName(jrf.getName());
    		if (qbjrf != null)
    		{
    			jrf.setName(qbjrf.getFldTitle());
    		}
    	}
    }

    /**
     * @param report
     * 
     * Sets field names to localization-independent identifiers.
     */
    protected void modifyFieldsForSaving(ReportSpecify report)
    {
    	for (Object jrfObj : report.getFields())
    	{
    		JRField jrf = (JRField )jrfObj;
    		QBJRDataSourceConnection.QBJRFieldDef qbjrf = report.getConnection().getFieldByTitle(jrf.getName());
    		if (qbjrf != null)
    		{
    			jrf.setName(qbjrf.getFldName());
    		}
    	}
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
                modifyFieldsForEditing(report);
                updateReportFields(report);
                report.setUsingMultiLineExpressions(false); // this.isUsingMultiLineExpressions());
                reportFrame = openNewReportWindow(report);
                report.addReportDocumentStatusChangedListener(this);
                setActiveReportForm(reportFrame);
            } catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, ex);
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
     * @param rep
     * @return ReportSpecify object for resource rep.
     */
    public static ReportSpecify loadReport(final AppResourceIFace rep)
    {
        ReportSpecify report = null;
        SpReport spRep = null;
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            spRep = session.getData(SpReport.class, "appResource", 
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
        
        java.io.InputStream xmlStream = new ByteArrayInputStream(rep.getDataAsString().getBytes());
        
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
            return null;
        }
    }
    /**
     * @param rep -
     *            specify report resource
     * @return a Report constructed from rep's jrxml definition.
     */
    public ReportSpecify makeReport(final AppResourceIFace rep)
    {
        ReportSpecify report = null;
        SpReport spRep = null;
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            spRep = session.getData(SpReport.class, "appResource", 
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
        
        java.io.InputStream xmlStream = getXML(rep, spRep != null ? spRep : null);
        
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
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
            report.addField(new JRField(fDef.getFldTitle(), fDef.getFldClass().getName()));
        }
        return report;
    }
    
    /**
     * @param report
     * 
     * Removes fields from report that are no longer present in it's specify query.
     * Adds fields to report that are in the query but not report.
     * Assumes that report's connection has been set and that modifyFieldsForEditing has been called for report.
     */
    protected void updateReportFields(final ReportSpecify report)
    {
    	QBJRDataSourceConnection c = report.getConnection();
    	if (c != null)
    	{
    		//first remove fields that are not in the connection.
    		for (int f = report.getFields().size()-1; f >= 0; f--)
    		{
    			JRField jrf = (JRField )report.getFields().get(f);
    			if (c.getFieldByTitle(jrf.getName()) == null)
    			{
    				report.getFields().remove(f);
    			}
    		}
    		//now add new fields
    		for (int f = 0; f < c.getFields(); f++) 
    		{
				boolean isNew = true;
				for (Object jrfObj : report.getFields()) 
				{
					JRField jrf = (JRField) jrfObj;
					if (jrf.getName().equals(c.getField(f).getFldTitle())) 
					{
						isNew = false;
						break;
					}
				}
				if (isNew) 
				{
					report.addField(new JRField(
							c.getField(f).getFldTitle(), c.getField(f)
									.getFldClass().getName()));
				}
			}
		}
    	else
    	{
    		log.info("Skipping fields update for '" + report.getName() + "' because connection is null.");
    	}
    }
    
    private InputStream getXML(final AppResourceIFace rep, final SpReport spRep)
    {
        String xml = modifyXMLForEditing(rep.getDataAsString(), spRep);
       
    	return new ByteArrayInputStream(xml.getBytes());
    }

    /* (non-Javadoc)
     * @see it.businesslogic.ireport.gui.MainFrame#newWizard()
     */
    @Override
    public Report newWizard()
    {
        if (UIHelper.isSecurityOn())
        {
            PermissionIFace permissions = SecurityMgr.getInstance().getPermission("Task.Reports");
            if (!permissions.canAdd())
            {
                JOptionPane.showMessageDialog(null, getResourceString("IReportLauncher.PERMISSION_TO_ADD_DENIED"),
                        getResourceString("IReportLauncher.PERMISSION_DENIED_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        List<QBJRDataSourceConnection> spConns = new Vector<QBJRDataSourceConnection>();
        for (Object conn : this.getConnections())
        {
            if (conn instanceof QBJRDataSourceConnection)
            {
                spConns.add((QBJRDataSourceConnection)conn);
            }
        }
        if (spConns.size() == 0)
        {
            JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), UIRegistry.getResourceString("REP_NO_QUERIES_FOR_DATA_SOURCES"), "", JOptionPane.INFORMATION_MESSAGE);
            return null;
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
        if (result != null) 
        {
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, ex);
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // This is for Windows and Exe4J, turn the args into System Properties
        for (String s : args)
        {
            String[] pairs = s.split("="); //$NON-NLS-1$
            if (pairs.length == 2)
            {
                log.debug("["+pairs[0]+"]["+pairs[1]+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
                {
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            }
        }
        
        // Now check the System Properties
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
        System.setProperty(SecurityMgr.factoryName,                     "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        
        
        if (StringUtils.isEmpty(UIRegistry.getJavaDBPath()))
        {
            File userDataDir = new File(UIRegistry.getAppDataDir() + File.separator + "DerbyDatabases"); //$NON-NLS-1$
            UIRegistry.setJavaDBDir(userDataDir.getAbsolutePath());
        }
        log.debug(UIRegistry.getJavaDBPath());

        final AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        adjustLocaleFromPrefs();
    	final String iRepPrefDir = localPrefs.getDirPath(); 
        int mark = iRepPrefDir.lastIndexOf(UIRegistry.getAppName(), iRepPrefDir.length());
        final String SpPrefDir = iRepPrefDir.substring(0, mark) + "Specify";
        HibernateUtil.setListener("post-commit-update", new edu.ku.brc.specify.dbsupport.PostUpdateEventListener()); //$NON-NLS-1$
        HibernateUtil.setListener("post-commit-insert", new edu.ku.brc.specify.dbsupport.PostInsertEventListener()); //$NON-NLS-1$
        HibernateUtil.setListener("post-commit-delete", new edu.ku.brc.specify.dbsupport.PostDeleteEventListener()); //$NON-NLS-1$
        
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
          public void run()
            {
                
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                    }
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
                    log.error("Can't change L&F: ", e); //$NON-NLS-1$
                }
                
                DatabaseLoginPanel.MasterPasswordProviderIFace usrPwdProvider = new DatabaseLoginPanel.MasterPasswordProviderIFace()
                {
                    @Override
                    public boolean hasMasterUserAndPwdInfo(final String username, final String password)
                    {
                        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
                        {
                            UserAndMasterPasswordMgr.getInstance().setUsersUserName(username);
                            UserAndMasterPasswordMgr.getInstance().setUsersPassword(password);
                            boolean result = false;
                            try
                            {
                            	try
                            	{
                            		AppPreferences.getLocalPrefs().flush();
                            		AppPreferences.getLocalPrefs().setDirPath(SpPrefDir);
                            		AppPreferences.getLocalPrefs().setProperties(null);
                            		result = UserAndMasterPasswordMgr.getInstance().hasMasterUsernameAndPassword();
                            	}
                            	finally
                            	{
                            		AppPreferences.getLocalPrefs().flush();
                            		AppPreferences.getLocalPrefs().setDirPath(iRepPrefDir);
                            		AppPreferences.getLocalPrefs().setProperties(null);
                            	}
                            } catch (Exception e)
                            {
                            	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
    								.capture(MainFrameSpecify.class, e);
                            	result = false;
                            }
                            return result;
                        }
                        return false;
                    }
                    
                    @Override
                    public Pair<String, String> getUserNamePassword(final String username, final String password)
                    {
                        UserAndMasterPasswordMgr.getInstance().setUsersUserName(username);
                        UserAndMasterPasswordMgr.getInstance().setUsersPassword(password);
                        Pair<String, String> result = null;
                        try
                        {
                        	try
                        	{
                        		AppPreferences.getLocalPrefs().flush();
                        		AppPreferences.getLocalPrefs().setDirPath(SpPrefDir);
                        		AppPreferences.getLocalPrefs().setProperties(null);
                        		result = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
                        	}
                        	finally
                        	{
                        		AppPreferences.getLocalPrefs().flush();
                        		AppPreferences.getLocalPrefs().setDirPath(iRepPrefDir);
                        		AppPreferences.getLocalPrefs().setProperties(null);
                        	}
                        } catch (Exception e)
                        {
                        	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
								.capture(MainFrameSpecify.class, e);
                        	result = null;
                        }
                        return result;
                    }
                    @Override
                    public boolean editMasterInfo(final String username, final boolean askFroCredentials)
                    {
                        boolean result = false;
                    	try
                        {
                        	try
                        	{
                        		AppPreferences.getLocalPrefs().flush();
                        		AppPreferences.getLocalPrefs()
									.setDirPath(SpPrefDir);
                        		AppPreferences.getLocalPrefs().setProperties(null);
                        		result =  UserAndMasterPasswordMgr
									.getInstance()
									.editMasterInfo(username, askFroCredentials);
                        	} finally
                        	{
                        		AppPreferences.getLocalPrefs().flush();
                        		AppPreferences.getLocalPrefs().setDirPath(
									iRepPrefDir);
                        		AppPreferences.getLocalPrefs().setProperties(null);
                        	}
                        } catch (Exception e)
                        {
                        	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
								.capture(MainFrameSpecify.class, e);
                        	result = false;
                        }
                    	return result;
                   }
                };
                String nameAndTitle = "Specify iReport"; // I18N
                UIHelper.doLogin(usrPwdProvider, false, false, new IReportLauncher(), "SPIReports", nameAndTitle, nameAndTitle); // true
																																	// means
																																	// do
																																	// auto
																																	// login
																																	// if
																																	// it
																																	// can,
																																	// second
																																	// bool
																																	// means
																																	// use
																																	// dialog
																																	// instead
																																	// of
																																	// frame
                
                localPrefs.load();
                
            }
        });

       
    }
}

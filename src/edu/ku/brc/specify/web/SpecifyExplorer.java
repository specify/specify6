/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.web;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Session;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.DataGetterForObj;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.exceptions.ExceptionTracker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 6, 2007
 *
 */
public class SpecifyExplorer extends HttpServlet 
{
    private final Logger         log      = Logger.getLogger(SpecifyExplorer.class);
    private Hashtable<Class<?>, Boolean> baseClassHash = new Hashtable<Class<?>, Boolean>();
    
    protected String DATABASE_NAME = "db";//"fish_kansas";
    
    public static String contentTag  = "<!-- Content -->";
    public static String servletURL  = "http://localhost:8080/specify/SpecifyExplorer";
    public static String baseURLSite = "http://localhost:8080/specify";
    
    protected String packageName = "edu.ku.brc.specify.datamodel.";
    
    protected static MyFmtMgr  fmtMgr  = null;
    protected SimpleDateFormat dateFormatter;
    protected static String    template        = "";
    protected static int       contentTagIndex = -1;

    
    protected Hashtable<String, ClassDisplayInfo>   classHash       = new Hashtable<String, ClassDisplayInfo>();
    protected Vector<ClassDisplayInfo>              sortedClassList = new Vector<ClassDisplayInfo>();
    
    protected Hashtable<String, String> labelMap  = new Hashtable<String, String>();
    
    //protected Vector<String> classList = new Vector<String>();
    
    protected DataGetterForObj   getter = new DataGetterForObj();
    
    protected QueryTask          queryTask             = null;
    protected ExpressSearchTask  expressSearch         = null;
    protected ExplorerESPanel    expressSearchExplorer = null;
    
    protected QueryReportHandler queryHandler;
    
    
    /**
     * 
     */
    public SpecifyExplorer()
    {
        System.out.println((new File(".")).getAbsolutePath());
        
        UIRegistry.setDefaultWorkingPath(new File("/Users/rods/workspace/Specify/webapp/WEB-INF").getAbsolutePath());
        
        System.out.println("["+UIRegistry.getDefaultWorkingPath()+"]");
        
        File dir = new File("site");
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        for (File f : dir.listFiles())
        {
            if (f.isFile() && f.getName().endsWith("html") && !f.getName().startsWith("template"))
            {
                f.delete();
            }
        }
        
        dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
        
        try
        {
            File templateFile = new File(UIRegistry.getDefaultWorkingPath() + File.separator + "site/template.html");
            template        = FileUtils.readFileToString(templateFile);
            contentTagIndex = template.indexOf(contentTag);

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        Class<?>[] baseClasses = {Boolean.class, Integer.class, Double.class, String.class, Float.class,
                                  Character.class, Short.class, Byte.class, BigDecimal.class, Date.class, Calendar.class};
        for (Class<?> cls : baseClasses)
        {
            baseClassHash.put(cls, true);
        }
        
        loadFieldsToSkip();
        
        setUp();
        
        setUpClasses();
    }
    
    /**
     * @param out
     * @param title
     */
    public static void writeTitle(final PrintWriter out, final String title)
    {
        String subContent = template.substring(0, contentTagIndex);
        out.println(StringUtils.replace(subContent, "<!-- Title -->", title));
    }
    
    /**
     * @param out
     */
    public static void writeToEnd(final PrintWriter out)
    {
        out.println(template.substring(contentTagIndex+contentTag.length()+1, template.length()));
    }
    
    /**
     * Returns the decalring Class for a field.
     * @param dataCls
     * @param fieldName
     * @return
     */
    protected Class<?> getClassForField(final Class<?> dataCls, final String fieldName)
    {
        if (StringUtils.isNotEmpty(fieldName))
        {
            for (Field field : dataCls.getDeclaredFields())
            {
                if (field.getName().equals(fieldName))
                {
                    return field.getType();
                }
            }
        }
        return null;
    }

    
    /**
     * 
     */
    protected void loadFieldsToSkip()
    {
        ClassDisplayInfo.setPackageName(packageName);
        
        try
        {
            Element root = XMLHelper.readFileToDOM4J(new File(UIRegistry.getDefaultWorkingPath() + File.separator + "config/ClassDisplayInfo.xml"));
            for (Object cls : root.selectNodes("/classes/class"))
            {
                Element clsElement = (Element)cls;
                String  clsName    = XMLHelper.getAttr(clsElement, "name", null);
                if (StringUtils.isNotEmpty(clsName))
                {
                    
                    ClassDisplayInfo cdi = null;
                    
                    String  linkField  = XMLHelper.getAttr(clsElement, "linkfield", "");
                    String  indexField = XMLHelper.getAttr(clsElement, "indexfield", "");
                    boolean useIdent   = XMLHelper.getAttr(clsElement, "useindentity", false);
                    
                    try
                    {
                        String fullName = packageName + clsName;
                        Class<?> clsObj = Class.forName(fullName);
                        Class<?> indexClass = getClassForField(clsObj, indexField);
                        
                        cdi = new ClassDisplayInfo(clsName, clsObj, indexField, indexClass, linkField, useIdent);
                        classHash.put(clsName, cdi);
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        continue;
                    }
                    
                    Element titleEl = (Element)clsElement.selectSingleNode("title");
                    if (titleEl != null)
                    {
                        String getterStr = XMLHelper.getAttr(titleEl, "getter", null);
                        if (getterStr != null)
                        {
                            try
                            {
                                Class<?>         getterCls   = Class.forName(getterStr);
                                TitleGetterIFace titleGetter = (TitleGetterIFace)getterCls.newInstance();
                                cdi.setTitleGetter(titleGetter);
                                
                            } catch (Exception ex)
                            {
                                
                            }
                        } else
                        {
                            cdi.setTitleField(XMLHelper.getAttr(titleEl, "field", null));
                        }
                    }
                    
                    for (Object fld : clsElement.selectNodes("skip/field"))
                    {
                        Element fldElement = (Element)fld;
                        boolean  isAvailForSearch = XMLHelper.getAttr(fldElement, "search", false);
                        
                        cdi.addSkipped(new FieldDisplayInfo(fldElement.getTextTrim(), false, isAvailForSearch));
                    }
                    
                    int inx = 0;
                    for (Object fld : clsElement.selectNodes("order/field"))
                    {
                        Element fldElement       = (Element)fld;
                        String  pickList         = XMLHelper.getAttr(clsElement, "pl", "");
                        //boolean isAvailForSearch = XMLHelper.getAttr(fldElement, "skip", false);
                        boolean isForDisplay     = XMLHelper.getAttr(fldElement, "display", true);

                        cdi.addOrdered(new FieldDisplayInfo(inx, fldElement.getTextTrim(), pickList, isForDisplay, true));
                        inx++;
                    }

                    for (Object fld : clsElement.selectNodes("additional/field"))
                    {
                        Element fldElement = (Element)fld;
                        String  name  = fldElement.getTextTrim();
                        String  type  = XMLHelper.getAttr(fldElement, "type", "");
                        String  label = XMLHelper.getAttr(fldElement, "label", "");
                        String  level = XMLHelper.getAttr(fldElement, "labellevel", "");

                        AdditionalDisplayField adf = new AdditionalDisplayField(inx, type, label, level, name);
                        cdi.addAdditional(adf);
                        inx++;
                    }
                    
                    for (Object st : clsElement.selectNodes("stats/stat"))
                    {
                        Element statElement = (Element)st;
                        String url = XMLHelper.getAttr(statElement, "url", null);
                        if (StringUtils.isNotEmpty(url))
                        {
                            cdi.addStat(new StatsDisplayInfo(url, statElement.getTextTrim()));
                        }
                    }
                }
            }
            sortedClassList = new Vector<ClassDisplayInfo>(classHash.values());
            Collections.sort(sortedClassList);
            
        } catch (Exception ex)
        {
            log.error(ex);
            System.out.println(ex.toString());
        }
    }
    
    /**
     * Setup all the System properties. This names all the needed factories. 
     */
    protected void setUpSystemProperties()
    {
        // Name factories
        System.setProperty(ViewFactory.factoryName,                     "edu.ku.brc.specify.config.SpecifyViewFactory");        // Needed by ViewFactory //$NON-NLS-1$
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences //$NON-NLS-1$
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(DataProviderFactory.factoryName,             "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory"); //$NON-NLS-1$
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");           // Needed for CatalogNumberign //$NON-NLS-1$
        System.setProperty(QueryAdjusterForDomain.factoryName,          "edu.ku.brc.specify.dbsupport.SpecifyQueryAdjusterForDomain"); // Needed for ExpressSearch //$NON-NLS-1$
        System.setProperty(SchemaI18NService.factoryName,               "edu.ku.brc.specify.config.SpecifySchemaI18NService");         // Needed for Localization and Schema //$NON-NLS-1$
        System.setProperty(WebLinkMgr.factoryName,                      "edu.ku.brc.specify.config.SpecifyWebLinkMgr");                // Needed for WebLnkButton //$NON-NLS-1$
        System.setProperty(DataObjFieldFormatMgr.factoryName,           "edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr");         // Needed for WebLnkButton //$NON-NLS-1$
        System.setProperty(RecordSetFactory.factoryName,                "edu.ku.brc.specify.config.SpecifyRecordSetFactory");          // Needed for Searching //$NON-NLS-1$
        System.setProperty(DBTableIdMgr.factoryName,                    "edu.ku.brc.specify.config.SpecifyDBTableIdMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        System.setProperty(SecurityMgr.factoryName,                     "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        //System.setProperty(UserAndMasterPasswordMgr.factoryName,               "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        System.setProperty(BackupServiceFactory.factoryName,            "edu.ku.brc.af.core.db.MySQLBackupService");                   // Needed for Backup and Restore //$NON-NLS-1$
        System.setProperty(ExceptionTracker.factoryName,                "edu.ku.brc.specify.web.SpecifyExplorerExceptionTracker");                   // Needed for Backup and Restore //$NON-NLS-1$
    }
    
    /**
     * @param sessionArg
     * @param user
     * @param collectionName
     * @return
     */
    protected Collection setupCurrentCollection(final DataProviderSessionIFace sessionArg, 
                                                final SpecifyUser user,
                                                final String collectionName)
    {
        
        UIFieldFormatterMgr.setDoingLocal(true);
        DataObjFieldFormatMgr.setDoingLocal(true);
        DataObjFieldFormatMgr.getInstance().setLocalFileName(XMLHelper.getConfigDirPath(DataObjFieldFormatMgr.getInstance().getLocalFileName()));

        //System.out.println("############### "+(new File(".")).getAbsolutePath());
        //System.out.println("############### "+DataObjFieldFormatMgr.getLocalFileName());
        
        try
        {
            
            Collection collection = null;
            
            // First get the Collections the User has access to.
            Hashtable<String, Collection> collectionHash = new Hashtable<String, Collection>();
            String sqlStr = "SELECT cs From Discipline as ct Inner Join ct.agents cta Inner Join cta.specifyUser as user Inner Join ct.collections as cs where user.specifyUserId = "+user.getSpecifyUserId();
            for (Object obj : sessionArg.getDataList(sqlStr))
            {
                Collection cs = (Collection)obj; 
                collectionHash.put(cs.getCollectionName(), cs);
                
                if (cs.getCollectionName().equals(collectionName))
                {
                    collection = cs;
                }
            }
            
            AppContextMgr.getInstance().setClassObject(Collection.class, collection);
            
            if (collection != null)
            {
                
                Discipline discipline = collection.getDiscipline();
                if (discipline != null)
                {
                    Institution institution = discipline.getDivision().getInstitution();
                    AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
                    AppContextMgr.getInstance().setClassObject(Division.class, discipline.getDivision());
                    AppContextMgr.getInstance().setClassObject(Institution.class, institution);
                    
                	Agent.setUserAgent(user, discipline);
                	
                	AppContextMgr am = AppContextMgr.getInstance();
                    am.setClassObject(TaxonTreeDef.class, discipline.getTaxonTreeDef());
                    am.setClassObject(GeologicTimePeriodTreeDef.class, discipline.getGeologicTimePeriodTreeDef());
                    am.setClassObject(StorageTreeDef.class, institution.getStorageTreeDef());
                    am.setClassObject(LithoStratTreeDef.class, discipline.getLithoStratTreeDef());
                    am.setClassObject(GeographyTreeDef.class, discipline.getGeographyTreeDef());
                    
                    int disciplineeId = discipline.getDisciplineId();
                    SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.CORE_SCHEMA, disciplineeId, DBTableIdMgr.getInstance(), Locale.getDefault());

                }
            } else
            {
                UIRegistry.showLocalizedError("COLLECTION_WAS_NULL");
            }
            
            return collection;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            UIRegistry.showLocalizedError(ex.toString()); // Yes, I know it isn't localized.
        }
        
        return null;
    }

    
    /** 
     * Drops, Creates and Builds the Database.
     * 
     * @throws SQLException
     * @throws IOException
     */
    public boolean setupDatabase(final DatabaseDriverInfo driverInfo,
                                 final String hostName, 
                                 final String dbName, 
                                 final String username, 
                                 final String password, 
                                 final String firstName, 
                                 final String lastName, 
                                 final String email,
                                 final DisciplineType  disciplineType)
    {
        
        log.info("Logging into "+dbName+"....");
        
        //String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, hostName, dbName);
        //if (connStr == null)
        //{
            String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, dbName);
        //}
            
        log.info(connStr);  
        
        //Pair<String, String> usernamePassword = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
        if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                driverInfo.getDialectClassName(), 
                dbName, 
                connStr, 
                username, 
                password))
        {
            log.info("Login Failed!");
            throw new RuntimeException("Login failed.");
            //return false;
        }  
        
        if (false)
        {
            try
            {
                Connection conn = DBConnection.getInstance().createConnection();
                conn.setCatalog("webmember");
                WebMemberDAO wm = new WebMemberDAO(conn);
                wm.createTable();
                conn.close();  
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        if (false)
        {
            try
            {
                Connection conn = DBConnection.getInstance().createConnection();
                conn.setCatalog("webmember");
                WebMemberDAO wm = new WebMemberDAO(conn);
                wm.addWebMember("Test", "db", "10");
                conn.close();  
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        queryHandler = new QueryReportHandler();

        log.info("Creating database "+dbName+"....");
        
        /*
        try
        {
            Thumbnailer thumb = new Thumbnailer();
            thumb.registerThumbnailers("config/thumbnail_generators.xml");
            thumb.setQuality(.5f);
            thumb.setMaxHeight(128);
            thumb.setMaxWidth(128);
    
            AttachmentManagerIface attachMgr = new FileStoreAttachmentManager(UIRegistry.getAppDataSubDir("AttachmentStorage", true));
            AttachmentUtils.setAttachmentManager(attachMgr);
            AttachmentUtils.setThumbnailer(thumb);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        } */
        
        if (true)
        {
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.setDirPath(UIRegistry.getAppDataDir());
            
            AppContextMgr.CONTEXT_STATUS status = AppContextMgr.getInstance().setContext(DATABASE_NAME, "testuser", false);
            if (status == AppContextMgr.CONTEXT_STATUS.OK)
            {
                if (AppContextMgr.getInstance().getClassObject(Discipline.class) != null)
                {
                    int disciplineeId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
                    SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.CORE_SCHEMA, disciplineeId, DBTableIdMgr.getInstance(), Locale.getDefault());
                }
            } else
            {
                throw new RuntimeException("setContext was NOT OK!");
            }
            
        } else
        {
            
            SpecifyUser user     = null;
            String      userName = "rods";
            boolean     debug    = true;
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                List<?> list = session.getDataList(SpecifyUser.class, "name", userName);
                if (list.size() == 1)
                {
                    user = (SpecifyUser)list.get(0);
                    user.getAgents(); // makes sure the Agent is not lazy loaded
                    //session.evict( user.getAgents() );
                    AppContextMgr.getInstance().setClassObject(SpecifyUser.class, user);
        
                } else
                {
                    //JOptionPane.showMessageDialog(null, 
                    //        getResourceString("USER_NOT_FOUND"), 
                    //        getResourceString("USER_NOT_FOUND_TITLE"), JOptionPane.WARNING_MESSAGE);
                    
                    return false;
                    //throw new RuntimeException("The user ["+userName+"] could  not be located as a Specify user.");
                }
        
                // First we start by getting all the Collection that the User want to
                // work with for this "Context" then we need to go get all the Default View and
                // additional XML Resources.
                
                // Ask the User to choose which Collection they will be working with
                Collection collection = setupCurrentCollection(session, user, "Fish");
                if (collection == null)
                {
                    // Return false but don't mess with anything that has been set up so far
                    return false;
                }
                
                String userType = user.getUserType();
                
                if (debug) log.debug("User["+user.getName()+"] Type["+userType+"]");
        
                userType = StringUtils.replace(userType, " ", "").toLowerCase();
                
                if (debug) log.debug("Def Type["+userType+"]");
                
        
                Discipline discipline = session.getData(Discipline.class, "disciplineId", collection.getDiscipline().getId(), DataProviderSessionIFace.CompareType.Equals) ;
                //discipline.getDeterminationStatuss().size(); // make sure they are loaded
                AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
                AppContextMgr.getInstance().setClassObject(Division.class, discipline.getDivision());
                
            } catch (Exception ex)
            {
                log.error(ex);
                
            } finally
            {
                session.close();
            }
        }
        // AppContextMgr.getInstance().
        //SpecifyAppPrefs.initialPrefs();
         
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
         
        SpecifyAppPrefs.initialPrefs();
        
        fmtMgr = new MyFmtMgr();

        return true;
    }
    
    /**
     * 
     */
    public void setUp()
    {
        UIRegistry.setAppName("Specify");
        IconManager.setApplicationClass(Specify.class);
        
        //UIRegistry.setJavaDBDir(derbyPath != null ? derbyPath : UIRegistry.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
        
        setUpSystemProperties();
        
        DisciplineType     disciplineType = DisciplineType.getDiscipline("fish");
        DatabaseDriverInfo driverInfo     = DatabaseDriverInfo.getDriver("MySQL");
        
        setupDatabase(driverInfo, "localhost", DATABASE_NAME, "rods", "rods", "rods", "rods", "guest@ku.edu", disciplineType);
    }
    
    /**
     * 
     */
    public void setUpClasses()
    {
        //Collection collection = (Collection)session.createCriteria(Collection.class).list().get(0);
        //AppContextMgr.getInstance().setClassObject(Collection.class, collection);
    }
    
    /**
     * @param field
     * @param dataObj
     * @return
     * @throws Exception
     */
    protected Object getData(final String fieldName, final Object dataObj) throws Exception
    {
        //DataObjectGettable getter = DataObjectGettableFactory.get(dataObj.getClass().getName(), FormHelper.DATA_OBJ_GETTER);
        Object data = getter.getFieldValue(dataObj, fieldName);
        if (data instanceof Boolean)
        {
            return ((Boolean)data) ? "Yes" : "No"; // I18N
        }
        return data;
    }
    
    /**
     * @param field
     * @param dataObj
     * @return
     * @throws Exception
     */
    protected Object getData(final Field field, final Object dataObj) throws Exception
    {
        return getData(field.getName(), dataObj);

        /*
        try
        {
            String methodName = "get" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
            Method method = dataObj.getClass().getMethod(methodName, (Class<?>[])null);
            if (method != null)
            {
                return method.invoke(dataObj, (Object[])null);
            }
            log.error("Missing method add(Object) for this type of set ["+dataObj.getClass()+"]");
        } catch (NoSuchMethodException ex) {}
        
        return null;
        */
    }
    
    /**
     * @param fdi
     * @return
     */
    protected String formatFDI(final FormDataObjIFace fdi)
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(fdi.getDataClass().getName());
        if (StringUtils.isNotEmpty(ti.getDataObjFormatter()))
        {
            String title = DataObjFieldFormatMgr.getInstance().format(fdi, ti.getDataObjFormatter());
            formatFDI(fdi, title);
        }
        
        return formatFDI(fdi, fdi.getIdentityTitle());
    }
    
    /**
     * @param fdi
     * @param title
     * @return
     */
    protected String makeURLLink(final FormDataObjIFace fdi, 
                                 final String           title, 
                                 final Properties       props) 
    {
        StringBuffer sb = new StringBuffer("<a href=\""+servletURL+"?");
        if (fdi != null)
        {
            sb.append("cls=");
            sb.append(fdi.getDataClass().getSimpleName());
            sb.append("&id=");
            sb.append(fdi.getId());
        }
        
        boolean needsAmp = fdi != null;
        if (props != null)
        {
            for (Object key : props.keySet())
            {
                if (!needsAmp)
                {
                    needsAmp = true;
                } else
                {
                    sb.append('&');
                }
                sb.append(key.toString());
                sb.append('=');
                sb.append(props.get(key));
            }
        }
        sb.append("\">");
        sb.append(title);
        sb.append("</a>");
        return sb.toString();
    }
    
    /**
     * @param fdi
     * @param title
     * @return
     */
    protected String makeURLLink(final FormDataObjIFace fdi, final String title) 
    {
        return makeURLLink(fdi, title, null);
    }
    
    /**
     * @param fdi
     * @return
     */
    protected String formatFDI(final FormDataObjIFace fdi, final String title)
    {
        if (classHash.get(fdi.getDataClass().getSimpleName()) != null)
        {
            return makeURLLink(fdi, title);
        }
        System.out.println(">>>>>>>>>>>>>>> class not in hash["+fdi.getDataClass().getSimpleName()+"]");
        return fdi.getIdentityTitle();
    }
    
    /**
     * @param data
     * @return
     */
    protected String formatValue(final Object data)
    {
        if (data instanceof FormDataObjIFace)
        {
            return formatFDI((FormDataObjIFace)data);
            
        } else if (data instanceof String || data instanceof Integer)
        {
            return data.toString();
            
        } else if (data instanceof Date)
        {
            return dateFormatter.format((Date)data);
            
        } else if (data instanceof Calendar)
        {
            return dateFormatter.format(((Calendar)data).getTime());
            
        } else if (data instanceof Float)
        {
            return String.format("%5.2f", data);
            
        } else if (data instanceof Double)
        {
            return String.format("%5.2f", data);
            
        } else if (data instanceof BigDecimal)
        {
            return StringUtils.stripEnd(data.toString(), "0");
        }
        return data.toString();
    }
    
    /**
     * @param fieldName
     * @param dataObjSet
     * @return
     */
    protected String processDataObjAsSet(final DBTableInfo       tableInfo,
                                         final DBTableChildIFace childInfo,
                                         final String            fieldName,
                                         final Vector<?>         dataObjSet)
    
    {
        
        String labelStr = childInfo != null ? childInfo.getTitle() : null;
        if (labelStr == null)
        {
            labelStr = labelMap.get(fieldName);
            if (StringUtils.isEmpty(labelStr))
            {
                labelStr = UIHelper.makeNamePretty(fieldName);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<br/>"+labelStr+"<br/>\n");
        sb.append("<table width=\"100%\" cellspacing=\"0\" class=\"brdr\">\n");
        int cnt = 1;
        for (Object setDataObj : dataObjSet)
        {
            String fdiStr = formatFDI((FormDataObjIFace)setDataObj);
            sb.append("<tr><td class=\"brdr"+((cnt % 2 == 0) ? "even" : "odd")+"\">"+fdiStr+"</td></tr>\n");    
            cnt++;
        }
        sb.append("</table>\n");
        return sb.toString();
    }
    
    /**
     * @param dataObj
     * @param fieldNames
     * @param index
     * @param sb
     */
    protected void valueTraversal(final Object       dataObj,
                                  final String[]     fieldNames, 
                                  final int          index,
                                  final boolean      doEvenOdd,
                                  final StringBuilder sb,
                                  final int          cntArg,
                                  final AdditionalDisplayField af)
    {
        //System.out.println("\n---------------------------------------------------------------------------");
        boolean isLabelLevel = false;
        boolean isLabelSet   = false;
        boolean isEnd        = index == fieldNames.length-1;
        String  labelStr     = "";
        
        //for (int j=0;j<index;j++) System.out.print(' ');
        //System.out.println("****** af.getLevel()["+af.getLevel()+"] fieldNames[index]["+fieldNames[index]+"] (af.isSet() "+af.isSet());
        
        if (af.getLevel().equals(fieldNames[index]))
        {
            isLabelLevel = true;
            if (af.isSet())
            {
                isLabelSet = true;
                labelStr = af.getLabel();
                
            } else
            {
                labelMap.get(fieldNames[index]);
                if (StringUtils.isEmpty(labelStr))
                {
                    labelStr = UIHelper.makeNamePretty(fieldNames[index]);
                }
            }
            
            if (isLabelSet && isLabelLevel)
            {
                sb.append("<br/>"+labelStr+"<br/>\n");
                sb.append("<table width=\"100%\" cellspacing=\"0\" class=\"brdr\">\n");
            }
        }
        
        /*
        for (int j=0;j<index;j++) System.out.print(' ');
        System.out.println("****** isLabelSet["+isLabelSet+"] labelStr["+labelStr+"] isLabelLevel["+isLabelLevel+"]");
        
        for (int j=0;j<index;j++) System.out.print(' ');
        System.out.println("****** ["+fieldNames[index]+"] ["+labelStr+"] isEnd["+isEnd+"] fieldNames.length["+fieldNames.length+"]");
        
        for (int j=0;j<index;j++) System.out.print(' ');
        System.out.println("****** Looking up field["+fieldNames[index]+"] in Object["+dataObj.getClass().getSimpleName()+"]");
        */
        
        Object[] values = UIHelper.getFieldValues(new String[] { fieldNames[index]}, dataObj, getter);
        Object   data   = values[0];
        if (data instanceof Set<?>)
        {
            //for (int j=0;j<index;j++) System.out.print(' ');
            //System.out.println("Doing Set");
            
            int cnt = 1;
            for (Object obj : (Set<?>)data)
            {
                //for (int j=0;j<index;j++) System.out.print(' ');
                //System.out.println("Sets " + obj);
                
                if (obj instanceof FormDataObjIFace && !isEnd)
                {
                    valueTraversal(obj, fieldNames, index+1, true, sb, cnt, af);
                    
                } else
                {
                    //for (int j=0;j<index;j++) System.out.print(' ');
                    //System.out.println("Doing0 Obj ");
                    sb.append("<tr><td class=\"brdr"+((cnt % 2 == 0) ? "even" : "odd")+"\">"+formatValue(obj)+"</td></tr>\n");    
                }
            }
        } else if (data instanceof FormDataObjIFace && !isEnd)
        {
            //for (int j=0;j<index;j++) System.out.print(' ');
            //System.out.println("Walking Obj ");

            valueTraversal(data, fieldNames, index+1, false, sb, 0, af);
            
        } else if (isLabelSet || doEvenOdd)
        {
            //for (int j=0;j<index;j++) System.out.print(' ');
            //System.out.println("Doing1 Obj ");
            sb.append("<tr><td class=\"brdr"+((cntArg % 2 == 0) ? "even" : "odd")+"\">"+formatValue(data)+"</td></tr>\n");

        } else
        {
            //for (int j=0;j<index;j++) System.out.print(' ');
            //System.out.println("Doing2 Obj ");
            sb.append("<tr><td align=\"right\">"+labelStr+ ":</td><td >"+formatValue(data)+"</td></tr>\n");
        }
        
        //for (int j=0;j<index;j++) System.out.print(' ');
        //System.out.println("****** About to close table isLabelSet["+isLabelSet+"] isLabelLevel["+isLabelLevel+"] ");
        if (isLabelSet && isLabelLevel)
        {
            sb.append("</table>\n");
        }
    }
    
    /**
     * @param out
     * @param dataObj
     * @param doChildrenSets
     */
    protected void processDataObj(final PrintWriter out,
                                  final FormDataObjIFace dataObj, 
                                  final boolean doChildrenSets)
    
    {
        if (StringUtils.isEmpty(template))
        {
            out.println("The template file is empty!");
        }
        
        fillLabelMap(dataObj, labelMap);
        
        Hashtable<Integer, String> ordered    = new Hashtable<Integer, String>();
        Vector<String>             unOrdered  = new Vector<String>();
        
        Hashtable<Integer, String> orderedSets    = new Hashtable<Integer, String>();
        Vector<String>             unOrderedSets  = new Vector<String>();
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(dataObj.getClass().getName());
        
        ClassDisplayInfo cdi = classHash.get(dataObj.getClass().getSimpleName());
        try
        {
            for (Field field : dataObj.getClass().getDeclaredFields())
            {
                String           fieldName = field.getName();
                FieldDisplayInfo fdi       = cdi.getField(fieldName);
                
                
                if (fdi != null && (fdi.isSkipped() || !fdi.isForDisplay()))
                {
                    continue;
                }
                
                String fldNameLower = fieldName.toLowerCase();
                if (fldNameLower.startsWith(dataObj.getClass().getSimpleName().toLowerCase()) && fldNameLower.endsWith("id"))
                {
                    continue;
                }
                
                DBTableChildIFace child = tableInfo.getItemByName(fieldName);
                if (child != null)
                {
                    if (child.isHidden())
                    {
                        continue;
                    }
                    //System.out.println("******* "+tableInfo.getTitle()+"  ["+fieldName+"]["+child.getTitle()+"]");
                } else
                {
                    System.out.println("******* "+tableInfo.getTitle()+"  ["+fieldName+"]");
                }
                
                String row = null;
                try
                {
                    Object data = getData(field, dataObj);
                    if (data != null)
                    {
                        if (!(data instanceof Set<?>))
                        {
                            String valueStr = null;
                            String labelStr = "";
                            if (child != null)
                            {
                                labelStr = child.getTitle();
                                if (child instanceof DBFieldInfo)
                                {
                                    DBFieldInfo fi = (DBFieldInfo)child;
                                    if (fi.getFormatter() != null)
                                    {
                                        Object valObj = fi.getFormatter().formatToUI(data);
                                        if (valObj != null)
                                        {
                                            valueStr = valObj.toString();
                                        }
                                        //System.out.println("!!!!!!!!!!!!!! "+labelStr+"["+data+"] ["+valueStr+"] "+fi.getFormatter().formatFromUI(data));
                                    }
                                }
                            } else
                            {
                                labelStr = labelMap.get(fieldName);
                            }
                            
                            if (StringUtils.isEmpty(labelStr))
                            {
                                labelStr = UIHelper.makeNamePretty(fieldName);
                            }
                            
                            if (valueStr == null)
                            {
                                valueStr = formatValue(data);
                            }
                            
                            String nowrap = valueStr.length() < 100 ? "nowrap=\"nowrap\" " : "";
                            
                            row = "<tr><td valign=align=\"right\" nowrap=\"nowrap\"><b>"+labelStr+ ":</b></td><td "+nowrap+"class=\"data\">"+valueStr+"</td></tr>\n";
                        }
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
                if (row != null)
                {
                    Integer inx = fdi != null ? fdi.getOrder() : null;
                    if (inx == null)
                    {
                        unOrdered.add(row);
                    } else
                    {
                        ordered.put(inx, row);
                    }
                }
            }
            
            //System.out.println("XXXXXXXXXXXXXXXX "+cdi.getAdditional());
            // Do additional Fields that are not sets
            for (AdditionalDisplayField adf : cdi.getAdditional())
            {
                //System.out.println("-> "+adf.getFieldName()+"  "+adf.isSet());
                if (!adf.isSet())
                {
                    StringBuilder sb     = new StringBuilder();
                    String[]     fNames = StringUtils.split(adf.getFieldName(), ".");
                    valueTraversal(dataObj, fNames, 0, false, sb, 0, adf);
                    unOrdered.add(sb.toString());
                }
            }
            
            if (doChildrenSets)
            {
                for (Field field : dataObj.getClass().getDeclaredFields())
                {
                    String           fieldName = field.getName();
                    FieldDisplayInfo fdi       = cdi.getField(fieldName);
                    
                    if (fdi != null && (fdi.isSkipped() || !fdi.isForDisplay()))
                    {
                        continue;
                    }
                    
                    String fldNameLower = fieldName.toLowerCase();
                    if (fldNameLower.startsWith(dataObj.getClass().getSimpleName().toLowerCase()) && fldNameLower.endsWith("id"))
                    {
                        continue;
                    }
                    
                    String row = null;
                    try
                    {
                        Object data = getData(field, dataObj);
                        if (data != null)
                        {
                            if (data instanceof Set<?>)
                            {
                                Set<?> set = (Set<?>)data;
                                if (set.size() > 0)
                                {
                                    
                                    DBTableChildIFace childInfo = tableInfo.getItemByName(fieldName);
                                    if (childInfo != null && childInfo.isHidden())
                                    {
                                        continue;
                                    }

                                    Vector<?> setAsList = null;
                                    if (fieldName.equals("collectionObjects"))
                                    {
                                        StringBuilder sb = new StringBuilder();
                                        getCollectionObjectList(set, sb);
                                        row = sb.toString();
                                        
                                    } else if (fieldName.equals("determinations"))
                                    {
                                        Vector<Determination> dets = new Vector<Determination>();
                                        for (Object obj : set.toArray())
                                        {
                                            dets.add((Determination)obj);
                                        }
                                        Collections.sort(dets, new Comparator<Determination>() {
                                            public int compare(Determination d1, Determination d2)
                                            {
                                                return d1.compareTo(d2);
                                            }
                                            
                                        });
                                        setAsList = dets;
                                    } else
                                    {
                                        setAsList = new Vector<Object>(set);
                                    }
                                    
                                    if (setAsList != null)
                                    {
                                        row = processDataObjAsSet(tableInfo, childInfo, fieldName, setAsList);
                                    }
                                }
                            }
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    
                    if (row != null)
                    {
                        Integer inx = fdi != null ? fdi.getOrder() : null;
                        if (inx == null)
                        {
                            unOrderedSets.add(row);
                        } else
                        {
                            orderedSets.put(inx, row);
                        }
                    }
                }
                
                // Do Additional Fields that are sets
                for (AdditionalDisplayField adf : cdi.getAdditional())
                {
                    if (adf.isSet())
                    {
                        StringBuilder sb = new StringBuilder();
                        if ((dataObj instanceof Taxon && adf.getFieldName().equals("determinations.collectionObject")))
                        {
                            getCollectionObjectList(dataObj, sb);
                        } else
                        {
                            String[] fNames = StringUtils.split(adf.getFieldName(), ".");
                            valueTraversal(dataObj, fNames, 0, false, sb, 0, adf);
                        }
                        unOrderedSets.add(sb.toString());
                    }
                }

            }

            String pageTitle = null;
            if (cdi.getTitleGetter() != null)
            {
                pageTitle = cdi.getTitleGetter().getTitle(dataObj);
                
            } else if (cdi.getTitleField() != null)
            {
                Object titleObj = getData(cdi.getTitleField(), dataObj);
                if (titleObj != null)
                {
                    if (titleObj instanceof Date)
                    {
                        pageTitle = dateFormatter.format((Date)titleObj);
                        
                    } else if (titleObj instanceof Calendar)
                    {
                        pageTitle = dateFormatter.format(((Calendar)titleObj).getTime());
                    } else
                    {
                        pageTitle = titleObj.toString();
                    }
                    
                }
            }
            
            int inx = template.indexOf(contentTag);
            String subContent = template.substring(0, inx);
            out.println(StringUtils.replace(subContent, "<!-- Title -->", tableInfo.getTitle()));//dataObj.getIdentityTitle()));

            out.println("<table border=\"0\">\n");
            out.println("<tr><td>\n");
            out.println("<tr><td class=\"title\" colspan=\"2\" align=\"center\">"+tableInfo.getTitle()+"</td></tr>\n");
            
            out.println("<tr><td valign=\"top\">\n");

            out.println("<table border=\"0\">\n");
            if (StringUtils.isNotEmpty(pageTitle))
            {
                out.println("<tr><td nowrap=\"nowrap\" class=\"pageTitle\" colspan=\"2\" align=\"center\">" + pageTitle + "</td></tr>\n");
            }
            fillRows(out, ordered, unOrdered);
            out.println("</table>\n");
            out.println("</td>\n");
            
            
            out.println("<td valign=\"top\">\n");
            String imgName = fakeGetImageName(dataObj);
            if (imgName != null)
            {
                out.println("<img border=\"1\" src=\"http://localhost/specifyexplorer/AttachImageCache/"+imgName+"\">\n");
            }
            out.println("</td>\n");
            
            out.println("</tr>\n");
            out.println("</table>\n");

            
           fillRows(out, orderedSets, unOrderedSets);
            
            // This should be externalized
            if (dataObj.getClass() == Locality.class || 
                dataObj.getClass() == CollectingEvent.class || 
                dataObj.getClass() == CollectionObject.class || 
                dataObj.getClass() == Taxon.class || 
                dataObj.getClass() == Accession.class)
            {
                List<Object> list = new Vector<Object>();
                list.add(dataObj);
                createMapLink(out, list, dataObj.getClass());
            }
            
            out.println(template.substring(inx+contentTag.length()+1, template.length()));
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.toString());
            out.println( "Sorry");
        }
        
    }
    
    
    /**
     * @param dataObj
     * @param sb
     */
    @SuppressWarnings("unchecked")
    protected void getCollectionObjectList(final Object dataObj, final StringBuilder sb)
    {
        Vector<CollectionObject> list = new Vector<CollectionObject>();
        
        if (dataObj instanceof Accession)
        {
            for (CollectionObject co : ((Accession)dataObj).getCollectionObjects())
            {
                list.add(co);
            }
            
        } else if (dataObj instanceof Set<?>)
        {
            for (CollectionObject co : (Set<CollectionObject>)dataObj)
            {
                list.add(co);
            }
            
        } else if (dataObj instanceof Taxon)
        {
            for (Determination det : ((Taxon)dataObj).getDeterminations())
            {
                if (det.isCurrentDet())
                {
                    list.add(det.getCollectionObject());
                }
            }
        }
        
        Collections.sort(list, new Comparator<CollectionObject>() {
            public int compare(CollectionObject o1, CollectionObject o2)
            {
                String c1 = o1.getCatalogNumber();
                String c2 = o2.getCatalogNumber();

                if (c1 == null) c1 = "";
                if (c2 == null) c2 = "";
                
                return c1.compareTo(c2);
            }
            
        });
        
        DBTableInfo           ti   = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
        DBFieldInfo           fi   = ti.getFieldByName("catalogNumber");
        final UIFieldFormatterIFace fmtr = fi.getFormatter();

        sb.append("<br/>Collection Objects<br/>\n");
        sb.append("<table width=\"100%\" cellspacing=\"0\" class=\"brdr\">\n");
        sb.append("<tr><td class=\"brdreven\">");
        int cnt = 0;
        for (CollectionObject co : list)
        {
            String catNum = co.getCatalogNumber();
            if (fmtr != null)
            {
                catNum = (String)fmtr.formatToUI(catNum);
            }
            if (cnt > 0) sb.append(", ");
            sb.append(formatFDI(co, catNum));
            cnt++;
        }

        sb.append("</td></tr></table>\n");
    }
    
    /**
     * @param dataObj
     */
    protected void processDataList(final PrintWriter out,
                                   final List<?> list,
                                   final String sql)
    
    {
        if (StringUtils.isEmpty(template))
        {
            out.println("The template file is empty!");
        }
        
        FormDataObjIFace dataObj   = (FormDataObjIFace)list.get(0);
        
        String linkField = "";
        ClassDisplayInfo cdi = classHash.get(dataObj.getClass().getSimpleName());
        if (cdi != null)
        {
            linkField = cdi.getLinkField();
        }
        
        int    contentInx = template.indexOf(contentTag);
        String subContent = template.substring(0, contentInx);
        out.println(StringUtils.replace(subContent, "<!-- Title -->", dataObj.getIdentityTitle()));

        //fillLabelMap(dataObj, labelMap);
        
        Hashtable<Integer, String> ordered    = new Hashtable<Integer, String>();
        Vector<String>             unOrdered  = new Vector<String>();
        
        Hashtable<String, Boolean> hasData      = new Hashtable<String, Boolean>();

        out.println("<table border=\"0\" width=\"100%\"<tr><td nowrap=\"nowrap\">select * "+StringUtils.replace(sql, packageName, "")+"</td></tr>");
        out.println("<tr><td nowrap=\"nowrap\">Records Returned: "+list.size()+"</td></tr></table><br/>");
        out.println("<table width=\"100%\" cellspacing=\"0\" class=\"brdr\">\n");
        try
        {
            for (Object dobj : list)
            {
                dataObj = (FormDataObjIFace)dobj;
                for (Field field : dataObj.getClass().getDeclaredFields())
                {
                    String fieldName = field.getName();
                    FieldDisplayInfo fdi = cdi.getField(fieldName);
                    
                    if (fdi != null && (fdi.isSkipped() || !fdi.isForDisplay()))
                    {
                        continue;
                    }
                    
                    String fldNameLower = fieldName.toLowerCase();
                    if (fldNameLower.startsWith(dataObj.getClass().getSimpleName().toLowerCase()) && fldNameLower.endsWith("id"))
                    {
                        continue;
                    }
                    
                    try
                    {
                        Object data = getData(field, dataObj);
                        if (data != null && !(data instanceof Set<?>))
                        {
                            hasData.put(fieldName, true);   
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            
            out.println("<tr><th class=\"brdr\">Row</th>");
            for (Field field : dataObj.getClass().getDeclaredFields())
            {
                String fieldName = field.getName();
                
                if (hasData.get(fieldName) == null)
                {
                    continue;
                }
                
                FieldDisplayInfo fdi = cdi.getField(fieldName); // should never be null
                
                String labelStr = labelMap.get(fieldName);
                if (StringUtils.isEmpty(labelStr))
                {
                    labelStr = UIHelper.makeNamePretty(fieldName);
                }
    
                String row = "<th class=\"brdr\">" + labelStr + "</th>";
                Integer inx = fdi != null ? fdi.getOrder() : null;
                if (inx == null)
                {
                    unOrdered.add(row);
                } else
                {
                    ordered.put(inx, row);
                }
                
            }
            fillRows(out, ordered, unOrdered);
            
            out.println("</tr>");
        
            int cnt = 1;
            for (Object dobj : list)
            {
                ordered.clear();
                unOrdered.clear();
                
                out.println("<tr><th class=\"brdr\" align=\"center\">" + cnt + "</th>");
                
                dataObj = (FormDataObjIFace)dobj;
                
                for (Field field : dataObj.getClass().getDeclaredFields())
                {
                    String fieldName = field.getName();
                    
                    if (hasData.get(fieldName) == null)
                    {
                        continue;
                    }
                    
                    FieldDisplayInfo fdi = cdi.getField(fieldName);
                    
                    String row = null;
                    try
                    {
                        Object data = getData(field, dataObj);
                        if (data != null && !(data instanceof Set<?>))
                        {
                            String val;
                            if (fieldName.equals(linkField))
                            {
                                val = formatFDI(dataObj, formatValue(data));
                            } else
                            {
                                val = formatValue(data);
                            }
                            row = "<td align=\"center\" class=\"brdr"+((cnt % 2 == 0) ? "even" : "odd")+"\">"+(StringUtils.isNotEmpty(val) ? val : "&nbsp;")+"</td>";    
                        } else
                        {
                            row = "<td align=\"center\" class=\"brdr"+((cnt % 2 == 0) ? "even" : "odd")+"\">&nbsp;</td>";   
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    
                    if (row != null)
                    {
                        Integer inx = fdi != null ? fdi.getOrder() : null;
                        if (inx == null)
                        {
                            unOrdered.add(row);
                        } else
                        {
                            ordered.put(inx, row);
                        }
                    }
                }
                fillRows(out, ordered, unOrdered);
                out.println("</tr>\n");
                cnt++;
            }
            out.println("</table>\n");
            
            // This should be externalized
            if (dataObj.getClass() == Locality.class || 
                dataObj.getClass() == CollectingEvent.class || 
                dataObj.getClass() == CollectionObject.class || 
                dataObj.getClass() == Taxon.class || 
                dataObj.getClass() == Accession.class)
            {
                createMapLink(out, list, dataObj.getClass());
            }
                
            
            out.println(template.substring(contentInx+contentTag.length()+1, template.length()));
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.toString());
            out.println( "Sorry");
        }
    }
    
    /**
     * @param locHash
     * @return
     */
    protected String getLocalityMapString(final Hashtable<Locality, Vector<CollectionObject>> locHash)
    {
        StringBuilder sb = new StringBuilder();
        
        int loc = 0;
        for (Locality locality : locHash.keySet())
        {
            //if (loc > 0) sb.append(';');
            sb.append(locality.getLocalityId()+";");
            int i = 0;
            for (CollectionObject co : locHash.get(locality))
            {
                if (i > 0) sb.append(',');
                sb.append(co.getCollectionObjectId());
                i++;
            }
            sb.append(";");
            loc++;
        }
        return sb.toString();
    }
    
    /**
     * @param out
     * @param list
     * @param clsObj
     */
    protected void createMapLink(final PrintWriter out, final List<?> list, final Class<?> clsObj)
    {
        Hashtable<Locality, Vector<CollectionObject>> locHash = new Hashtable<Locality, Vector<CollectionObject>>();
        
        if (clsObj == Locality.class)
        {
            for (Object obj : list)
            {
                Locality l = (Locality)obj;
                if (l != null)
                {
                    Vector<CollectionObject> colObjs = new Vector<CollectionObject>();
                    for (CollectingEvent ce : l.getCollectingEvents())
                    {
                        for (CollectionObject co : ce.getCollectionObjects())
                        {
                            colObjs.add(co);
                        }
                    }
                    locHash.put(l, colObjs);
                }
            }

        } else if (clsObj == CollectingEvent.class)
        {
            Vector<CollectionObject> colObjs = null;
            
            for (Object obj : list)
            {
                CollectingEvent ce = (CollectingEvent)obj;
                if (ce != null)
                {
                    Locality l = ce.getLocality();
                    if (l != null)
                    {
                        colObjs = locHash.get(l);
                        if (colObjs == null)
                        {
                            colObjs    = new Vector<CollectionObject>();
                            locHash.put(l, colObjs);
                        }
                    } else
                    {
                        colObjs = null;
                    }
                    
                    if (colObjs != null)
                    {
                        for (CollectionObject co : ce.getCollectionObjects())
                        {
                            colObjs.add(co);
                        }
                    }
                }
            }

            
        } else if (clsObj == CollectionObject.class)
        {
            Vector<CollectionObject> colObjs = null;
            
            for (Object obj : list)
            {
                CollectionObject co = (CollectionObject)obj;
                CollectingEvent  ce = co.getCollectingEvent();
                if (ce != null)
                {
                    Locality l = ce.getLocality();
                    if (l != null)
                    {
                        colObjs = locHash.get(l);
                        if (colObjs == null)
                        {
                            colObjs    = new Vector<CollectionObject>();
                            locHash.put(l, colObjs);
                        }
                    } else
                    {
                        colObjs = null;
                    }
                }
                
                if (colObjs != null)
                {
                    colObjs.add(co);
                }
            }
        } else if (clsObj == Taxon.class)
        {
            Vector<CollectionObject> colObjs = null;
            
            for (Object obj : list)
            {
                Taxon txn = (Taxon)obj;
                for (Determination det : txn.getDeterminations())
                {
                    if (det.isCurrentDet())
                    {
                        Locality locality = null;
                        CollectionObject co = det.getCollectionObject();
                        if (co != null)
                        {
                            CollectingEvent ce = co.getCollectingEvent();
                            if (ce != null)
                            {
                                locality = ce.getLocality();
                            }
                            
                            if (locality != null)
                            {
                                colObjs = locHash.get(locality);
                                if (colObjs == null)
                                {
                                    colObjs    = new Vector<CollectionObject>();
                                    locHash.put(locality, colObjs);
                                }
                                if (colObjs != null)
                                {
                                    colObjs.add(co);
                                }
                            }
                        }
                    }
                }
                
            }
        } else if (clsObj == Accession.class)
        {
            for (Object obj : list)
            {
                Accession accesion = (Accession)obj;
                if (accesion != null)
                {
                    Vector<CollectionObject> colObjs = new Vector<CollectionObject>();
                    for (CollectionObject co : accesion.getCollectionObjects())
                    {
                        if (co != null)
                        {
                            Locality locality = null;
                            CollectingEvent ce = co.getCollectingEvent();
                            if (ce != null)
                            {
                                locality = ce.getLocality();
                            }
                            
                            if (locality != null)
                            {
                                colObjs = locHash.get(locality);
                                if (colObjs == null)
                                {
                                    colObjs    = new Vector<CollectionObject>();
                                    locHash.put(locality, colObjs);
                                }
                                if (colObjs != null)
                                {
                                    colObjs.add(co);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        out.println("<br/><a href=\""+servletURL+"?id=map&points="+getLocalityMapString(locHash)+"\">Map Collection Objects</a>");
    }
    
    /**
     * @param out
     * @param ordered
     * @param unOrdered
     */
    protected void fillRows(final PrintWriter out, 
                            final Hashtable<Integer, String> ordered, 
                            final Vector<String> unOrdered)
    {
        Vector<Integer> indexes = new Vector<Integer>(ordered.keySet());
        Collections.sort(indexes);
        int cnt = 1;
        for (Integer index : indexes)
        {
            String row = ordered.get(index);
            if (row != null)
            {
                if (StringUtils.contains(row, "BRDRODDEVEN"))
                {
                    out.println(StringUtils.replace(row, "BRDRODDEVEN", "brdr"+(((cnt+1) % 2 == 0) ? "even" : "odd")));
                } else
                {
                    out.println(row);
                }
                cnt++;
            }
        }
        
        if (unOrdered != null)
        {
            for (String row : unOrdered)
            {
                if (StringUtils.contains(row, "BRDRODDEVEN"))
                {
                    out.println(StringUtils.replace(row, "BRDRODDEVEN", "brdr"+(((cnt+1) % 2 == 0) ? "even" : "odd")));
                } else
                {
                    out.println(row);
                }
                cnt++;
            }
        }
    }
    
    /**
     * @param name
     * @param isNumeric
     * @return
     */
    protected String getCompareCBX(final String name, final boolean isNumeric)
    {
        return "<select name=\""+name+"\" size=\"1\">\n" +
        "<option value=\"=\" selected=\"selected\">Equals</option>\n" + 
        "<option value=\"NOT EQUAL\">Not Equals</option>\n" + 
        "<option value=\"LIKE\">LIKE</option>\n" + 
        (isNumeric ? 
                "<option value=\">\">Greater Than</option>\n" + 
                "<option value=\"<\">Less Than</option>\n" +
                "<option value=\">\">Greater Than Equals</option>\n" + 
                "<option value=\"<\">Less Than Equals</option>\n"
                : "") + 
        "</select>\n";
    }
    
    protected String getAndOfCBX(final String name)
    {
        return "<select name=\""+name+"AndOrCBX\" size=\"1\">\n" +
        "<option value=\"AND\" selected=\"selected\">AND</option>\n" + 
        "<option value=\"OR\">OR</option>\n" + 
        "</select>\n";
    }
    
    protected String getTrueFalse(final String name)
    {
        return "<select name=\""+name+"\" size=\"1\">\n" +
        "<option value=\"-\" selected=\"selected\"> - </option>\n" + 
        "<option value=\"TRUE\">True</option>\n" + 
        "<option value=\"FALSE\">False</option>\n" + 
        "</select>\n";
    }
    
    protected String getInput(final String name, final String type, final Integer len)
    {
        return "<input type=\""+type+"\" name=\""+name+"\""+ (len != null ? (" size=\"" + len + "\"") : "") + ">";
    }

    
    /**
     * @param field
     * @return
     */
    protected String getControlPanel(final Field field)
    {
        String name = field.getName();
        
        StringBuilder sb     = new StringBuilder();
        Class<?>      fldCls = field.getType();
        if (fldCls == Integer.class || fldCls == Double.class || 
                fldCls == Float.class || fldCls == Short.class || 
                fldCls == Byte.class || fldCls == Calendar.class ||
                fldCls == Date.class || fldCls == BigDecimal.class)
        {
            sb.append(getCompareCBX(name + "Compare1", true));
            sb.append(getInput(name + "1", "text", 20));
            sb.append("&nbsp;&nbsp;");
            sb.append(getAndOfCBX(name));
            sb.append("&nbsp;&nbsp;");
            sb.append(getCompareCBX(name + "Compare2", true));
            sb.append(getInput(name + "2", "text", 20));
            
        } else if (fldCls == Boolean.class)
        {
            sb.append(getTrueFalse(name + "1"));
            
        } else
        {
            sb.append(getCompareCBX(name + "Compare1", false));
            sb.append(getInput(name + "1", "text", 20));
        }
        
        return sb.toString();
    }
    
    /**
     * @param out
     * @param className
     * @param fieldsToSkip
     * @param fieldsOrder
     * @param fieldsAdditional
     */
    protected void displaySearchForm(final PrintWriter out, 
                                     final String fullClassName)
    {
        try
        {
            if (StringUtils.isEmpty(template))
            {
                out.println("The template file is empty!");
            }
            Class<?>         clsObj    = Class.forName(fullClassName);
            ClassDisplayInfo cdi       = classHash.get(clsObj.getSimpleName());
            String           className = cdi.getClassName();
            
            //fillLabelMap((FormDataObjIFace)clsObj.newInstance(), labelMap);
            
            int inx = template.indexOf(contentTag);
            String subContent = template.substring(0, inx);
            out.println(StringUtils.replace(subContent, "<!-- Title -->", className+ " Search Form"));
            
            out.println("<form name=plcform action=\""+servletURL+"\" method=\"GET\" onsubmit=\"\"><br/>\n");
            out.println("<input type=\"hidden\" name=\"cls\" value=\""+className+"\">\n");
            out.println("<input type=\"hidden\" name=\"id\" value=\"dosearch\">\n");
            
            out.println("<span style=\"font-size: 14pt;\">"+className+" Search Form</span><br/><br/>\n");
            out.println("<input type=\"radio\" name=\"match\" value=\"OR\" checked=\"checked\"/>Match any of following</input>\n");
            out.println("<input type=\"radio\" name=\"match\" value=\"AND\">Match all of following</input>\n");
            out.println("<br/>\n");
            
            out.println("<br/>\n");
            out.println("<table style=\"border: 1px gray solid\" cellspacing=\"0\" cellpadding=\"4\">\n");
            out.println("<tr><th class=\"brdr\" align=\"center\" nowrap=\"nowrap\">Field</th>");
            out.println("    <th class=\"brdr\" align=\"center\" nowrap=\"nowrap\">Search Values</th>");
            out.println("    <th class=\"brdr\" align=\"center\" nowrap=\"nowrap\">Hide</th></tr>\n");
            //out.println("<tr><td colspan=\"3\"><hr style=\"color: gray\"/></td></tr>\n");
            
            Hashtable<Integer, String> ordered    = new Hashtable<Integer, String>();
            Vector<String>             unOrdered  = new Vector<String>();
            
            StringBuilder sb = new StringBuilder();
            for (Field field : clsObj.getDeclaredFields())
            {
                sb.setLength(0);
                
                String           fieldName = field.getName();
                FieldDisplayInfo fdi       = cdi.getField(fieldName);
                
                //System.out.println(fieldName + "  "+fdi+"  "+(fdi != null ? (fdi.isSkipped()+"  "+fdi.isForDisplay()+"  "+fdi.isAvailForSearch()) : ""));
                if (fdi != null && (fdi.isSkipped() && !fdi.isAvailForSearch()))
                {
                    //System.out.println("skipping");
                    continue;
                }
                
                String fldNameLower = fieldName.toLowerCase();
                if (fldNameLower.startsWith(clsObj.getSimpleName().toLowerCase()) && fldNameLower.endsWith("id"))
                {
                    continue;
                }
                
                try
                {
                    if (!java.util.Collection.class.isAssignableFrom(field.getType()) &&
                        !org.apache.log4j.Logger.class.isAssignableFrom(field.getType()))
                    {
                        // There has to be a better to check for FormDataObjIFace
                        boolean isOK = true;
                        if (baseClassHash.get(field.getType()) == null)
                        {
                            Object obj = field.getType().newInstance();
                            isOK = !(obj instanceof FormDataObjIFace);
                        }
                        
                        if (isOK)
                        {
                            String  labelStr = labelMap.get(fieldName);
                            if (StringUtils.isEmpty(labelStr))
                            {
                                labelStr = UIHelper.makeNamePretty(fieldName);
                            }
                            
                            Integer index = fdi != null ? fdi.getOrder() : null;
                            
                            int cnt = index == null ? unOrdered.size() : ordered.size();
                            
                            sb.append("<tr><td class=\"BRDRODDEVEN"+"\" align=\"right\" nowrap=\"nowrap\" style=\"border-bottom: 1px gray solid;\"><b>"+labelStr+ ":</b></td>");
                            sb.append("<td class=\"BRDRODDEVEN"+"\" nowrap=\"nowrap\" style=\"border-bottom: 1px gray solid;\">"+getControlPanel(field)+"</td>");
                            sb.append("<td class=\"BRDRODDEVEN"+"\" align=\"center\" style=\"border-bottom: 1px gray solid;\">"+getInput(fieldName + "Hide", "checkbox", null)+"</td></tr>\n");
                            cnt++;
                            
                            if (index == null)
                            {
                                unOrdered.add(sb.toString());
                            } else
                            {
                                ordered.put(index, sb.toString());
                            }
                        }
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            
            fillRows(out, ordered, unOrdered);
            
            out.println("<tr><td align=\"center\" colspan=\"2\"><input type=\"submit\" value=\"Search\"></td></tr>\n");
            out.println("</table><br/>");

            out.println("</form>");
            
            out.println(template.substring(inx+contentTag.length()+1, template.length()));

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param out
     * @param request
     * @param name
     * @return
     */
    protected String getParamValue(final PrintWriter out, final HttpServletRequest request, final String name)
    {
        String str = request.getParameter(name);
        //out.println("[ "+name+"] [" +str+"]<br/>");
        return str;
    }
    
    public static String parseDate(final String[] formats, final String dateStr) throws Exception
    {
        for (String fmt : formats)
        {
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt);
                Date     d = sdf.parse(dateStr);
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.format(d.getTime());
                
            } catch (Exception ex)
            {
            }
        }
        return "";
    }
    
    public static String parseDate(final String dateStr) throws Exception
    {
        if (dateStr.length() == 10)
        {
            if (StringUtils.isNumeric(dateStr.substring(2,3)))
            {
                return parseDate(new String[] {"yyyy/MM/dd", "yyyy-MM-dd"}, dateStr);
            }
            return parseDate(new String[] {"MM-dd-yyyy", "MM/dd/yyyy"}, dateStr);
        }
        return "";
    }
    
    /**
     * @param out
     * @param className
     * @param request
     */
    protected void processSearch(final PrintWriter out, 
                                 final String      fullClassName,
                                 final HttpServletRequest request)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            Class<?>         clsObj    = Class.forName(fullClassName);
            ClassDisplayInfo cdi       = classHash.get(clsObj.getSimpleName());
            String           className = cdi.getClassName();
            
            //System.out.println("class: "+className);
            DBTableInfo      ti        = DBTableIdMgr.getInstance().getByShortClassName(className);
            
            //fillLabelMap((FormDataObjIFace)clsObj.newInstance(), labelMap);
            
            StringBuilder sql = new StringBuilder("FROM "+className+" WHERE ");
            
            String matchStr    = " " + getParamValue(out, request, "match") + " ";
            
            boolean addMatch = false;
            for (Field field : clsObj.getDeclaredFields())
            {
                String           fieldName = field.getName();
                FieldDisplayInfo fdi       = cdi.getField(fieldName);
                
                //System.out.println(fieldName + "  "+fdi+"  "+(fdi != null ? (fdi.isSkipped()+"  "+fdi.isForDisplay()+"  "+fdi.isAvailForSearch()) : ""));
                if (fdi != null && (fdi.isSkipped() || !fdi.isAvailForSearch()))
                {
                    continue;
                }
                
                String fldNameLower = fieldName.toLowerCase();
                if (fldNameLower.startsWith(clsObj.getSimpleName().toLowerCase()) && fldNameLower.endsWith("id"))
                {
                    continue;
                }
                
                try
                {
                    if (!java.util.Collection.class.isAssignableFrom(field.getType()) &&
                        !org.apache.log4j.Logger.class.isAssignableFrom(field.getType()))
                    {
                        boolean isOK = true;
                        if (baseClassHash.get(field.getType()) == null)
                        {
                            Object obj = field.getType().newInstance();
                            isOK = !(obj instanceof FormDataObjIFace);
                        }
                        
                        //out.println(field.getName() + " " +field.getType()+ " isOK "+isOK+"<br/>");
                        
                        if (isOK)
                        {
                            //String hideStr     = getParamValue(out, request, fieldName+"Hide");
                            String andOrStr    = getParamValue(out, request, fieldName+"AndOrCBX");
                            String compareStr1 = getParamValue(out, request, fieldName+"Compare1");
                            String compareStr2 = getParamValue(out, request, fieldName+"Compare2");
                            String field1Str   = getParamValue(out, request, fieldName+"1");
                            String field2Str   = getParamValue(out, request, fieldName+"2");
                            
                            Class<?> fldCls = field.getType();
                            if (fldCls == Integer.class || fldCls == Double.class || 
                                fldCls == Float.class || fldCls == Short.class || 
                                fldCls == Byte.class || fldCls == BigDecimal.class)
                            {
                                if (StringUtils.isNotEmpty(field1Str) && StringUtils.isNotEmpty(compareStr1))
                                {
                                    if (addMatch)
                                    {
                                        sql.append(matchStr);
                                        addMatch = false;
                                    }
                                    
                                    String value = field1Str;
                                    if (compareStr1.equals("LIKE"))
                                    {
                                        value = "%" + value + "%";
                                    }
                                    sql.append(fieldName + " " + compareStr1 + " " + value);
                                    
                                    if (StringUtils.isNotEmpty(field2Str) && StringUtils.isNotEmpty(compareStr2))
                                    {
                                        value = field2Str;
                                        if (compareStr2.equals("LIKE"))
                                        {
                                            value = "%" + value + "%";
                                        }
                                        sql.append(" " + andOrStr + " ");
                                        sql.append(fieldName + " " + compareStr2 + " " + value);
                                    }
                                    addMatch = true;
                                } 
                                
                            } else if (fldCls == Calendar.class || fldCls == Date.class)
                            {
                                if (StringUtils.isNotEmpty(field1Str) && StringUtils.isNotEmpty(compareStr1))
                                {
                                    if (addMatch)
                                    {
                                        sql.append(matchStr);
                                        addMatch = false;
                                    }
                                    
                                    String value = parseDate(field1Str);
                                    if (compareStr1.equals("LIKE"))
                                    {
                                        value = "%" + value + "%";
                                    }
                                    sql.append(fieldName + " " + compareStr1 + " '" + value + "'");
                                    
                                    if (StringUtils.isNotEmpty(field2Str) && StringUtils.isNotEmpty(compareStr2))
                                    {
                                        value = parseDate(field2Str);
                                        if (compareStr2.equals("LIKE"))
                                        {
                                            value = "%" + value + "%";
                                        }
                                        sql.append(" " + andOrStr + " ");
                                        sql.append(fieldName + " " + compareStr2 + " '" + value + "'");
                                    }
                                    addMatch = true;
                                } 
                            } else if (fldCls == String.class)
                            {
                                //out.println("["+compareStr+"]------------------- " + field1Str);
                                if (StringUtils.isNotEmpty(field1Str))
                                {
                                    if (addMatch)
                                    {
                                        sql.append(matchStr);
                                        addMatch = false;
                                    }
                                    
                                    DBFieldInfo fi = ti.getFieldByName(fieldName);
                                    
                                    if (fi != null && fi.getFormatter() != null)
                                    {
                                        field1Str = (String)fi.getFormatter().formatFromUI(field1Str);
                                    }
                                    String value = field1Str;
                                    if (compareStr1.equals("LIKE"))
                                    {
                                        value = "%" + field1Str + "%";
                                    }
                                    sql.append(fieldName + " " + compareStr1 + " '" + value+"'");
                                    addMatch = true;
                                }
                            } else if (fldCls == Boolean.class)
                            {
                                if (StringUtils.isNotEmpty(field1Str) && !field1Str.equals("-"))
                                {
                                    if (addMatch)
                                    {
                                        sql.append(matchStr);
                                        addMatch = false;
                                    }
                                    sql.append(fieldName + " = " + field1Str);
                                    addMatch = true;
                                }
                                
                            } else
                            {
                                if (StringUtils.isNotEmpty(field1Str) && StringUtils.isNotEmpty(compareStr1))
                                {
                                    if (addMatch)
                                    {
                                        sql.append(matchStr);
                                        addMatch = false;
                                    }
                                    String value = field1Str;
                                    if (compareStr1.equals("LIKE"))
                                    {
                                        value = "%" + field1Str + "%";
                                    }
                                    sql.append(fieldName + " " + compareStr1 + " " + value);
                                    addMatch = true;
                                }
                            }
                            
                        }
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            
            //out.println("SQL ["+sql + "]");
            List<?> list = session.createQuery(sql.toString(), false).list();
                
            
            if (list == null || list.size() == 0)
            {
                int inx = template.indexOf(contentTag);
                String subContent = template.substring(0, inx);
                out.println(StringUtils.replace(subContent, "<!-- Title -->", "No Results."));
                out.println("<br/></br>"+ (list == null ? "There was error performing the query." : "No results return.")+"<br/><br/>");
                out.println("<table border=\"0\" width=\"100%\"<tr><td nowrap=\"nowrap\">select * "+sql+"</td></tr></table><br/>");
                out.println("</br><br/><a href=\"javascript:window.back()\">Back</a>");
                out.println(template.substring(inx+contentTag.length()+1, template.length()));
                
            } else if (list.size() == 1)
            {
                processDataObj(out, 
                               (FormDataObjIFace)list.get(0), 
                               true);
            } else
            {
               
                processDataList(out, 
                                list,
                                sql.toString());
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /**
     * @param dataObj
     * @param labelHashMap
     */
    protected void fillLabelMap(final FormDataObjIFace dataObj, 
                                final Hashtable<String, String> labelHashMap)
    {
        labelHashMap.clear();
        
        if (dataObj instanceof Treeable<?, ?, ?>)
        {
           Treeable<?, ?, ?> node = (Treeable<?, ?, ?>) dataObj;
           
           TreeDefItemIface<?,?,?> def = node.getDefinitionItem();
           //String defName = def.getName();
           
           Treeable<?, ?, ?> parentNode = node.getParent();
           if (parentNode != null)
           {
               TreeDefItemIface<?,?,?> parentDef  = parentNode.getDefinitionItem();
               if (parentDef != null)
               {
                   String parentDefName = parentDef.getName();
                   labelHashMap.put("parent", parentDefName);
               }
           }
           
           TreeDefItemIface<?,?,?> childDef  = def.getChild();
           if (childDef != null)
           {
               String childDefName = childDef.getName();
               labelHashMap.put("children", childDefName);
           }
           if (dataObj.getClass() == Taxon.class)
           {
               labelHashMap.put("definitionItem", "Taxonomy");
           }
        }
    }
    
    /**
     * @param clazz
     * @param fieldName
     */
    public void doAlphaIndexPage(final PrintWriter out, 
                                 final String className, 
                                 final String fieldName,
                                 final String letter)
    {
        UIFieldFormatterIFace fmt = null;
        boolean     isNumeric  = false;
        int         numLetters = 1;
        DBTableInfo tableInfo  = DBTableIdMgr.getInstance().getByShortClassName(className);
        String      tableName  = tableInfo.getName();
        DBFieldInfo fi         = tableInfo.getFieldByName(fieldName);
        if (fi != null)
        {
            //System.out.println("Found field["+fi.getTitle()+"]");
            fmt = fi.getFormatter();
            if (fmt != null)
            {
                isNumeric = fmt.isNumeric(); 
                if (!isNumeric)
                {
                    //System.out.println("Found fmt ["+fmt.getName()+"] size "+fmt.getFields().size());
                    List<UIFieldFormatterField> fields = fmt.getFields();
                    if (fields.size() > 0)
                    {
                        //System.out.println("FMT "+fields.get(0));
                        if (fields.get(0).getType() == UIFieldFormatterField.FieldType.year)
                        {
                            numLetters = 4;
                        }
                        //System.out.println("Found numLetters "+numLetters);
                    }
                }
            }
        }
        
        String filter = "";
        if (true)
        {
            if (StringUtils.isNotEmpty(fieldName) && StringUtils.isNotEmpty(letter) && !isNumeric)
            {
                filter = " LOWER(SUBSTRING("+fieldName+", 1, "+letter.length()+")) = \""+ letter.toLowerCase() + "\" ";
            }
        }
        
        String joinStr       = QueryAdjusterForDomain.getInstance().getJoinClause(tableInfo, false, null, false);
        String specialFields = QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, false);
        String sql = "SELECT "+tableName + "." + tableInfo.getIdColumnName()+", "+fieldName+
            " FROM "+tableInfo.getName() +  " " +
            (StringUtils.isNotEmpty(joinStr) ? joinStr : "") +
            (StringUtils.isNotEmpty(specialFields) ? " WHERE "+specialFields : "") +
            (StringUtils.isNotEmpty(filter) ? (StringUtils.isNotEmpty(specialFields) ? " AND " : " WHERE ") + filter : "") +
            " ORDER BY "+fieldName+" ASC";
        sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
        System.out.println(sql);
        
        doAlphaIndexPageSQL(out, className, letter, numLetters, fmt, sql);
    }
    
    /**
     * @param clazz
     * @param fieldName
     */
    public void doAlphaIndexPageSQL(final PrintWriter out, 
                                    final String  className,
                                    final String  letter,
                                    final int     numLetters,
                                    final UIFieldFormatterIFace fmt,
                                    final String  sql)
    {
        boolean isNumeric = fmt != null && fmt.isNumeric();
        int    inx        = template.indexOf(contentTag);
        String subContent = template.substring(0, inx);
        
        out.println(StringUtils.replace(subContent, "<!-- Title -->", className));
        
        ClassDisplayInfo cdi = classHash.get(className);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        boolean useLetter = true;

        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(sql);
            
            Vector<NameId>            alphaList = new Vector<NameId>();
            Hashtable<String, NameId> alphaHash = new Hashtable<String, NameId>();
            
            boolean doingIndex = StringUtils.isEmpty(letter);
            System.out.println("\n\ndoingIndex "+doingIndex+" letter["+letter+"]");

            int numMin = Integer.MAX_VALUE;
            int numMax = Integer.MIN_VALUE;
            int cnt    = 0;
            
            
            while (rs.next())
            {
                String name;
                int    id   = rs.getInt(1);
                
                if (isNumeric)
                {
                    
                    name = rs.getString(2);
                    name = (String)fmt.formatToUI(name);
                    Integer numAsInt = null;
                    Integer floor    = null;
                    try
                    {
                        numAsInt = Integer.parseInt(name);
                        floor    = Integer.parseInt(letter);
                        
                    } catch (Exception ex) {}
                    
                    numMin = Math.min(numMin, numAsInt);
                    numMax = Math.min(numMax, numAsInt);
                    
                    if (doingIndex)
                    {
                        int    numSegment = numAsInt / 1000;
                        String c          = Integer.toString(numSegment);
                        NameId nis        = alphaHash.get(c);
                        if (nis == null)
                        {
                            nis = new NameId(c, 0, numSegment);
                            alphaHash.put(c, nis);
                        }
                        nis.add();
                        
                    } else
                    {
                        if (numAsInt >= floor && numAsInt < (floor + 1000))
                        {
                            alphaList.add(new NameId(name, id, numAsInt));
                        }
                    }

                    
                } else
                {
                    
                    name = rs.getString(2);
                    if (StringUtils.isEmpty(name))
                    {
                        name = rs.getString(1);
                    }
                    
                    if (cdi.isUseIdentityTitle())
                    {
                        DataProviderSessionIFace session = null;
                        try
                        {
                            session = DataProviderFactory.getInstance().createSession();
                            FormDataObjIFace fdi = (FormDataObjIFace)session.createQuery("from "+className+" where id = "+id, false).list().get(0);
                            if (fdi != null)
                            {
                                String title = fdi.getIdentityTitle();
                                if (StringUtils.isNotEmpty(title))
                                {
                                    name = title;
                                }
                            }
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                            //log.error(ex);
                            
                        } finally
                        {
                            session.close();    
                        }
                        
                    } else if (cdi.getIndexClass() != null)
                    {
                        if (cdi.getIndexClass() == Calendar.class)
                        {
                            Date date = rs.getDate(2);
                            if (date != null)
                            {
                                name = sdf.format(date);
                            } else
                            {
                                name = "0000";
                            }
                            useLetter = false;
                        }
                    }
                    
                    int len = Math.min(numLetters, name.length());
                    if (doingIndex)
                    {
                        String c = useLetter ? name.substring(0, len).toLowerCase() : name;
                        NameId nis = alphaHash.get(c);
                        if (nis == null)
                        {
                            nis = new NameId(c, 0);
                            alphaHash.put(c, nis);
                        }
                        nis.add();
                        
                    } else
                    {
                        if ((useLetter && name.substring(0, len).toUpperCase().equals(letter)) ||
                            (!useLetter && name.equals(letter)))
                        {
                            alphaList.add(new NameId(name, id));
                        }
                    }
                }

                cnt++;
            }
            
            System.out.println("alphaHash.size: "+alphaHash.size());
            if (doingIndex)
            {
                alphaList = new Vector<NameId>(alphaHash.values());
            }
            
            Collections.sort(alphaList);
            
            System.out.println("alphaList.size: "+alphaList.size());
            
            if (doingIndex)
            {
                DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(className);
                
                out.println("<center><br/><span style=\"font-size: 14pt;\">Index For "+ti.getTitle()+"</span><br/>");
                out.println("<table class=\"brdr\" border=\"0\" cellpadding=\"4\" cellspacing=\"0\">\n");
                out.println("<tr><th class=\"brdr\" align=\"center\" nowrap=\"nowrap\">Index</th>");
                out.println("<th class=\"brdr\" align=\"center\" nowrap=\"nowrap\">Count</th></tr>\n");
                int i = 0;
                for (NameId nis : alphaList)
                {
                    String ltrStr = nis.getNum() != null ? Integer.toString(nis.getNum() * 1000) : nis.getName().toUpperCase();
                    out.println("<tr>");
                    out.println("<td nowrap=\"nowrap\" class=\"brdr"+(((i+1) % 2 == 0) ? "even" : "odd")+"\" align=\"center\">&nbsp;&nbsp;<a href=\""+servletURL+"?cls="+className+"&ltr="+ltrStr+"\">" +ltrStr+ "</a>&nbsp;&nbsp;</td>\n");  
                    out.println("<td nowrap=\"nowrap\" class=\"brdr"+(((i+1) % 2 == 0) ? "even" : "odd")+"\"  align=\"center\"><a href=\""+servletURL+"?cls="+className+"&ltr="+ltrStr+"\">" +nis.getId()+ "</a></td>\n");  
                    out.println("</tr>");
                    i++;
                }
                out.println("</table></center>\n");
            } else
            {
                if (alphaList.size() > 0)
                {
                    if (useLetter)
                    {
                        out.println("<br/>"+alphaList.get(0).getName().charAt(0)+"<br/>\n");
                    }
                    out.println("<table class=\"brdr\" border=\"0\" cellpadding=\4\" cellspacing=\"0\">\n");
                    int i = 1;
                    for (NameId nis : alphaList)
                    {
                        out.println("<tr>");
                        out.println("<td nowrap=\"nowrap\" class=\"brdr"+(((i+1) % 2 == 0) ? "even" : "odd")+"\" >");  
                        out.println("<a href=\""+servletURL+"?cls="+className+"&id="+nis.getId()+"\">" +nis.getName()+ "</a>");
                        out.println("</td></tr>\n");
                        i++;
                    }   
                    out.println("</table>\n");
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        out.println(template.substring(inx+contentTag.length()+1, template.length()));
            
        log.info("Done");
    }
    
    /**
     * @param clazz
     * @param fieldName
     */
    public void doAlphaIndexPageSQLOld(final PrintWriter out, 
                                    final String className,
                                    final String letter,
                                    final String sql)
    {
        int inx = template.indexOf(contentTag);
        String subContent = template.substring(0, inx);
        out.println(StringUtils.replace(subContent, "<!-- Title -->", className));

        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(sql);
            
            char currChar = '_';
            
            while (rs.next())
            {
                int    id   = rs.getInt(1);
                String name = rs.getString(2);
                if (StringUtils.isEmpty(name))
                {
                    name = rs.getString(1);
                }
                
                String lowerName = name.toLowerCase();
                boolean okDisplay = StringUtils.isEmpty(letter) || letter.toLowerCase().charAt(0) == lowerName.charAt(0);
                
                if (currChar != lowerName.charAt(0))
                {
                    currChar = lowerName.charAt(0);
                    if (okDisplay)
                    {
                        out.println("<br/>"+name.charAt(0)+"<br/>\n");
                    }
                }
                if (okDisplay)
                {
                    out.println("<a href=\""+servletURL+"?cls="+className+"&id="+id+"\">" +name+ "</a><br/>\n");
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        out.println(template.substring(inx+contentTag.length()+1, template.length()));
            
        log.info("Done");
    }
    
    /**
     * @param path
     * @return
     */
    protected String getRelativeImagePath(final String path)
    {
        System.out.println("getRelativeImagePath -> ["+path+"]");
        String key = "specify.jar!/edu/ku/brc/specify/";
        int inx = path.indexOf(key);
        if (inx > -1)
        {
            inx += key.length();
            String urlStr = baseURLSite + "/" + path.substring(inx);
            //String urlStr = "classes/" + path.substring(inx);
            System.out.println("getRelativeImagePath -> ["+urlStr+"]");
            return urlStr;
        }
        return null;
    }
    
    /**
     * @param out
     */
    protected void displayAllSearchables(final PrintWriter out)
    {
        try
        {
            if (StringUtils.isEmpty(template))
            {
                out.println("The template file is empty!");
            }
            
            int    inx        = template.indexOf(contentTag);
            String subContent = template.substring(0, inx);
            out.println(StringUtils.replace(subContent, "<!-- Title -->", "Search Forms"));
            
            out.println("<br/>");
            out.println("Seachable Data");
            out.println("<br/>");
            out.println("<table class=\"brdr\"  border=\"0\" cellspacing=\"0\" cellpadding=\"4\">");
            out.println("<tr><th class=\"brdr\" align=\"center\" nowrap=\"nowrap\">Data Type</th>");
            out.println("    <th class=\"brdr\" align=\"center\" nowrap=\"nowrap\">Form</th>");
            out.println("    <th class=\"brdr\" align=\"center\" nowrap=\"nowrap\">Index</th>\n");
            out.println("    <th class=\"brdr\" align=\"center\" nowrap=\"nowrap\">Statistics</th></tr>\n");
            out.println("</tr>\n");
            
            String formIconPath  = getRelativeImagePath(IconManager.getIconEntryByName("Search").getUrl().toString());
            String statsIconPath = getRelativeImagePath(IconManager.getIconEntryByName("Statistics").getUrl().toString());
            String indexIcon     = baseURLSite + "/images/Search32x32.png";//index.png";
            
            int cnt = 1;
            for (ClassDisplayInfo cdi : sortedClassList)
            {
                String className = cdi.getClassName();
                String indexName = cdi.getIndexName();
                
                DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(className);
                
                if (StringUtils.isEmpty(indexName))
                {
                    continue;
                }
                
                String brdCls = " class=\"brdr"+((cnt % 2 == 0) ? "even" : "odd")+"\"";
                		
                out.println("<tr><td nowrap=\"nowrap\" "+brdCls+">"+ti.getTitle()+"</td>");
                //out.println("    <td nowrap=\"nowrap\" "+brdCls+" align=\"center\" ><a href=\""+baseURL+"?cls="+className+"&id=dspsrch\"><img src=\""+formIconPath+"\"/> "+ti.getTitle()+"</a></td>");
                out.println("    <td nowrap=\"nowrap\" "+brdCls+" align=\"center\" ><a href=\""+servletURL+"?cls="+className+"&id=dspsrch\"><img border=\"0\" height=\"24\" src=\""+formIconPath+"\"/></a></td>");
                if (StringUtils.isNotEmpty(cdi.getLinkField()))
                {
                    out.println("    <td nowrap=\"nowrap\" "+brdCls+" align=\"center\"><a href=\""+servletURL+"?cls="+className+"\"><img border=\"0\" height=\"24\" src=\""+indexIcon+"\"/></a></a></td>");
                } else
                {
                    out.println("    <td nowrap=\"nowrap\" "+brdCls+" align=\"center\">&nbsp;</td>");
                }
                out.println("    <td nowrap=\"nowrap\" "+brdCls+">");
                for (StatsDisplayInfo stat : cdi.getStats())
                {
                    out.println("    <a href=\""+stat.getUrl()+"\"><img border=\"0\" height=\"24\" src=\""+statsIconPath+"\"/> "+stat.getTitle()+"</a><br/>");
                }
                out.println("    </td>");
                out.println("</tr>");
                cnt++;
            }
           
            out.println("</table><br/>");

            out.println(template.substring(inx+contentTag.length()+1, template.length()));

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param out
     * @param pointsStr
     * @param sortByCE
     */
    protected void createMap(final PrintWriter out, 
                             final String pointsStr,
                             final boolean sortByCE)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();

            String mapTemplate = "";
            try
            {
                File templateFile = new File(UIRegistry.getDefaultWorkingPath() + File.separator + "site/map_template.html");
                mapTemplate = FileUtils.readFileToString(templateFile);
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
                out.println(ex.toString());
            }
            
            if (StringUtils.isEmpty(template))
            {
                out.println("The template file is empty!");
            }
            
            int inx = mapTemplate.indexOf(contentTag);
            String subContent = mapTemplate.substring(0, inx);
            out.println(StringUtils.replace(subContent, "<!-- Title -->", "Mapping Collection Objects"));
            
            
            String[] points = StringUtils.splitPreserveAllTokens(pointsStr, ';');
            /*System.out.println("["+pointsStr+"]");
            for (int i=0;i<points.length;i++)
            {
                System.out.println("i["+i+"]Loc["+points[i]+"] CO["+points[i+1]+"]");
                i++;
            }*/
            
            double maxLat = Double.MIN_VALUE;
            double minLat = Double.MAX_VALUE;
            
            double maxLon = Double.MIN_VALUE;
            double minLon = Double.MAX_VALUE;
            
    
            
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            Hashtable<Locality, CollectingEvent> locToCE = new Hashtable<Locality, CollectingEvent>();
            
            boolean drawLines = false;
    
            Hashtable<CollectingEvent, CEForPoly> hash = new Hashtable<CollectingEvent, CEForPoly>();
            StringBuilder locStr = new StringBuilder();
            
            int locCnt = 0;
            for (int i=0;i<points.length;i++)
            {
                //System.out.println("i["+i+"]Loc["+points[i]+"]");
                if (StringUtils.isEmpty(points[i]))
                {
                    break;
                }
    
                //String title = "";
                Locality locality = (Locality)session.createQuery("from Locality WHERE id = "+points[i], false).list().get(0);
                if (locality != null)
                {
                    StringBuilder sb = new StringBuilder();
                    String[] colObjsIds = StringUtils.splitPreserveAllTokens(points[i+1], ',');
                    for (String id : colObjsIds)
                    {
                        //System.out.println("co["+id+"]");
                        if (StringUtils.isNotEmpty(id))
                        {
                            CollectionObject co = (CollectionObject)session.createQuery("from CollectionObject WHERE id = "+id, false).list().get(0);
                            if (co != null)
                            {
                                CollectingEvent ce = co.getCollectingEvent();
                                if (ce != null)
                                {
                                    CollectingEvent colEv = locToCE.get(locality);
                                    if (colEv == null)
                                    {
                                        locToCE.put(locality, ce);
                                        
                                    } else if (!ce.getCollectingEventId().equals(colEv.getCollectingEventId()))
                                    {
                                        drawLines = false;
                                    }
                                    //sb.append("<h3>"+sdf.format(ce.getStartDate().getTime())+"</h3>");
                                    Locality loc = ce.getLocality();
                                    if (loc != null && loc.getLatitude1() != null && loc.getLongitude1() != null)
                                    {
                                        CEForPoly cep = hash.get(ce);
                                        if (cep == null)
                                        {
                                            cep = new CEForPoly(ce.getStartDate(), loc.getLatitude1().doubleValue(), loc.getLongitude1().doubleValue(), "");
                                            hash.put(ce, cep);
                                        }
                                        cep.getColObjs().add(co);
                                    }
                                }
                                for (Determination det : co.getDeterminations())
                                {
                                    if (det.isCurrentDet())
                                    {
                                        Taxon txn = det.getPreferredTaxon();
                                        if (txn != null)
                                        {
                                            sb.append("<a href='SpecifyExplorer?cls=CollectionObject&id="+co.getCollectionObjectId()+"'>"+txn.getFullName()+"</a>");
                                            sb.append("<br/>");
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    if (locality.getLatitude1() != null && locality.getLongitude1() != null)
                    {
                        if (locCnt == 0)
                        {
                            maxLat = locality.getLatitude1().doubleValue();
                            minLat = maxLat;
                            
                            maxLon = locality.getLongitude1().doubleValue();
                            minLon = maxLon;
                            
                        } else
                        {
                            maxLat = Math.max(maxLat, locality.getLatitude1().doubleValue());
                            minLat = Math.min(minLat, locality.getLatitude1().doubleValue());
                            
                            maxLon = Math.max(maxLon, locality.getLongitude1().doubleValue());
                            minLon = Math.min(minLon, locality.getLongitude1().doubleValue());   
                        }
                        
                        locStr.append("var point = new GLatLng("+locality.getLatitude1()+","+locality.getLongitude1()+");\n");
                        locStr.append("var marker = createMarker(point,\""+ locality.getLocalityName()+"\",\""+sb.toString()+"\");\n");
                        locStr.append("map.addOverlay(marker);\n");
                        locCnt++;
                    }
    
                }
                i++;
            }
            
            System.out.println("maxLat: "+maxLat);
            System.out.println("minLat: "+minLat);
            System.out.println("maxLon: "+maxLon);
            System.out.println("minLon: "+minLon);
            
            double halfLat = (maxLat - minLat) / 2;
            double halfLon = (maxLon - minLon) / 2;
            System.out.println("halfLat: "+halfLat);
            System.out.println("halfLon: "+halfLon);
            
            int zoom = 2;
            if (halfLat == 0.0 && halfLon == 0.0)
            {
                zoom = 12;
                
            } else if (halfLat < 0.5 && halfLon < 0.5)
            {
                zoom = 10;
                
            } else if (halfLat < 2.0 && halfLon < 2.0)
            {
                zoom = 8;
                
            } else if (halfLat < 7.0 && halfLon < 7.0)
            {
                zoom = 6;
            }
            
            out.println("        map.setCenter(new GLatLng( "+(minLat+halfLat)+","+(minLon+halfLon)+"), "+ zoom+");\n");
    
            out.println(locStr.toString());
            
            if (drawLines)
            {
                if (hash.size() > 0)
                {
                    out.println("var polyline = new GPolyline([");
                    for (CEForPoly cep : hash.values())
                    {
                        out.println("new GLatLng("+cep.getLat()+", "+cep.getLon()+"),\n");
                    }
                }
                out.println("], \"#FF0000\", 5);\n");
                out.println("map.addOverlay(polyline);\n");
                
            }
    
            if (false)
            {
                out.println("var polygon = new GPolygon([");
                for (CEForPoly cep : hash.values())
                {
                    out.println("new GLatLng("+cep.getLat()+", "+cep.getLon()+"),\n");
                }
                out.println("], \"#ff0000\", 5, 0.7, \"#0000ff\", 0.4);\n");
                out.println("map.addOverlay(polygon);\n");
            }
    
            out.println(mapTemplate.substring(inx+contentTag.length()+1, mapTemplate.length()));
        
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /**
     * @param response
     */
    protected void generateDateChart(final HttpServletRequest request, 
                                     final HttpServletResponse response,
                                     final String sql,
                                     final String title,
                                     final String x_AxisTitle,
                                     final String y_AxisTitle)
    {
        String type = request.getParameter("type");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

        Hashtable<String, NameId> alphaHash = new Hashtable<String, NameId>();
        Vector<NameId>            alphaList = null;
        
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(sql);
            
            while (rs.next())
            {
                Date   date = rs.getDate(1);
                String dateStr = sdf.format(date);
                
                int year = Integer.parseInt(dateStr);
                
                int decade = (year / 10) * 10;
                dateStr = Integer.toString(decade);
                
                NameId nis = alphaHash.get(dateStr);
                if (nis == null)
                {
                    nis = new NameId(dateStr, 0);
                    alphaHash.put(dateStr, nis);
                }
                nis.add();
            }
            
        
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        alphaList = new Vector<NameId>(alphaHash.values());
        Collections.sort(alphaList);
        createChart(response, type, alphaList, title, x_AxisTitle, y_AxisTitle);
    }
    
    /**
     * @param request
     * @param response
     */
    protected void generateCEChart(final HttpServletRequest request, 
                                   final HttpServletResponse response)
    {
        generateDateChart(request, 
                          response, 
                          "select startDate from collectingevent where startDate is not null", 
                          "Collecting Events By Decade", 
                          "Decades", 
                          "Number of Collecting Events");
    }
    
    /**
     * @param request
     * @param response
     */
    protected void generateColObjChart(final HttpServletRequest request, 
                                      final HttpServletResponse response)
    {
        generateDateChart(request, 
                          response, 
                          "select catalogedDate from collectionobject where catalogedDate is not null", 
                          "Cataloged By Decade", 
                          "Decades", 
                          "Number of Collection Objects");
    }
    
    /**
     * @param response
     */
    protected void generateCEChartOld(final HttpServletRequest request, 
                                   final HttpServletResponse response)
    {
        String type = request.getParameter("type");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

        Hashtable<String, NameId> alphaHash = new Hashtable<String, NameId>();
        Vector<NameId>            alphaList = null;
        
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery("select startDate from collectingevent where startDate is not null");
            
            while (rs.next())
            {
                Date   date = rs.getDate(1);
                String dateStr = sdf.format(date);
                
                int year = Integer.parseInt(dateStr);
                
                int decade = (year / 10) * 10;
                dateStr = Integer.toString(decade);
                
                NameId nis = alphaHash.get(dateStr);
                if (nis == null)
                {
                    nis = new NameId(dateStr, 0);
                    alphaHash.put(dateStr, nis);
                }
                nis.add();
            }
            
        
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        alphaList = new Vector<NameId>(alphaHash.values());
        Collections.sort(alphaList);
        createChart(response, type, alphaList, "Collecting Events By Decade", "Decades", "Number of Collecting Events");
    }
    
    /**
     * @param response
     * @param type
     * @param alphaList
     * @param title
     * @param xTitle
     * @param yTitle
     */
    protected void createChart(final HttpServletResponse response,
                               final String              type,
                               final Vector<NameId>      alphaList,
                               final String              title,
                               final String              xTitle,
                               final String              yTitle)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
        for (NameId nis : alphaList)
        {
            dataset.addValue(nis.getId(), nis.getName(), ""); 
        }
        
        JFreeChart chart = null;
        if (StringUtils.isEmpty(type) || type.equals("bar"))
        {
            chart = ChartFactory.createBarChart( 
                    title,
                    xTitle,
                    yTitle,
                    dataset, 
                    PlotOrientation.VERTICAL, 
                    true, true, false 
                    ); 
        
        } else if (type.equals("line"))
        {
            chart = ChartFactory.createLineChart(
                    title,
                    xTitle,
                    yTitle, 
                    dataset, 
                    PlotOrientation.VERTICAL, 
                    true, true, false);
        }
        
        //chart.getCategoryPlot().setRenderer(new CustomColorBarChartRenderer());

        chart.setBackgroundPaint(new Color(228, 243, 255));
        
        response.setContentType("image/png"); 
        try
        {
            ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, 700, 600);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param response
     */
    protected void chartIt(final PrintWriter out, final String arg, final String title)
    {
        int inx = template.indexOf(contentTag);
        String subContent = template.substring(0, inx);
        out.println(StringUtils.replace(subContent, "<!-- Title -->", title));

        out.println("<iframe FRAMEBORDER=\"0\" src=\"SpecifyExplorer?id="+arg+"\" width=\"705\" height=\"605\"></iframe>");
        
        out.println(template.substring(inx+contentTag.length()+1, template.length()));
    }
    
    @SuppressWarnings("unchecked")
    protected JSONObject createNode(final Treeable<?, ?, ?> treeable)
    {
        JSONObject json = new JSONObject();
        json.accumulate("name", treeable.getName());
        json.accumulate("fullname", treeable.getName());
        json.accumulate("uiProvider", "col");
               
        JSONArray childrenArray = new JSONArray();
        Set<Treeable<?,?,?>> children = (Set<Treeable<?,?,?>>)treeable.getChildren();
        json.accumulate("leaf", children.size() == 0);
        
        //json.accumulate("count", children.size());
        
        for (Treeable<?,?,?> kid : children)
        {
            childrenArray.add(createNode(kid));
        }
        json.accumulate("children", childrenArray);
        
        return json;
        
    }
    
    /**
     * @param text
     * @param className
     * @return
     */
    @SuppressWarnings("unchecked")
    protected String searchTree(final String text, final String className)
    {
        
        DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(className);
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            List<FormDataObjIFace> list = (List<FormDataObjIFace>)session.getDataList("FROM "+ti.getClassName()+" WHERE name = '"+text+"'");
            if (list.size() > 0)
            {
                JSONObject treeJSON = new JSONObject();
                treeJSON.accumulate("title",        className);
                treeJSON.accumulate("id",         list.iterator().next().getId());
                System.out.println("====>\n"+treeJSON.toString());
                return treeJSON.toString();
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            //log.error(ex);
            
        } finally
        {
            session.close();    
        }
        return "[]";
    }
    
    /**
     * @param cls
     * @param id
     * @return
     */
    protected String createTree(final Class<?> clsObj, 
                                final int id)
    {
        
        System.out.println("************************ \nLooking up ["+clsObj.getName()+"] node: "+id);

        JSONObject rootNode = null;
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            Treeable<?, ?, ?> treeable = (Treeable<?, ?, ?>)session.get(clsObj, id);
            if (treeable != null)
            {
                rootNode = createNode(treeable);
            } else
            {
                System.out.println("************************ \nCouldn't find ["+clsObj.getName()+"] node: "+id);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            //log.error(ex);
            
        } finally
        {
            session.close();    
        }
        
        if (rootNode != null)
        {
            JSONArray treeArray = new JSONArray();
            treeArray.add(rootNode);
            
            //JSONObject treeJSON = new JSONObject();
            //treeJSON.accumulate("title",        cls.getName());
            //treeJSON.accumulate("tree",         treeArray);
            
            System.out.println("Results ************************ \n"+treeArray.toString());
            return treeArray.toString();
        }
        return "{}";
    }
    
    protected Taxon getTaxonFromCollectionObject(final CollectionObject co)
    {
        for (Determination det : co.getDeterminations())
        {
            if (det.isCurrentDet())
            {
                return det.getPreferredTaxon();
            }
        }
        return null;
    }
    
    protected String getTaxonImage(final String fullName)
    {
        if (fullName != null)
        {
            String[] rows = {
            "6792,Lutjanus sebae,Emperor red snapper",
            "6794,Trachinotus blochii,Snubnose pompano",
            "6796,Caranx sexfasciatus,Bigeye trevally",
            "6797,Gerres longirostris,Longtail silverbiddy",
            "6799,Hyporhamphus affinis,Tropical halfbeak",
            "6804,Chaetodon trifasciatus,Melon butterflyfish ",
            "6806,Cephalopholis miniata,Coral hind",
            "6810,Apolemichthys trimaculatus,Threespot angelfish",
            "6813,Parupeneus ciliatus,Whitesaddle goatfish",
            "6814,Zebrasoma scopas,Twotone tang ",
            "6854,Pomacanthus semicirculatus,Semicircle angelfish ",
            "6871,Pterois radiata,Radial firefish ",
            "6881,Oxymonacanthus longirostris,Harlequin filefish",
            "6887,Priacanthus blochii,Paeony bulleye",
            "6907,Sargocentron seychellense,Yellow-tipped squirrelfish",
            "6926,Myripristis berndti,Blotcheye soldierfish",
            "6927,Pempheris vanicolensis,Vanikoro sweeper",
            "6931,Pseudanthias squamipinnis,Sea goldie",
            "6960,Archamia fucata,Orangelined cardinalfish",
            "6961,Archamia fucata,Orangelined cardinalfish",
            "6964,Lutjanus bohar,Two-spot red snapper",
            "6965,Lutjanus kasmira,Common bluestripe snapper",
            "7031,Pempheris vanicolensis,Vanikoro sweeper",
            "7039,Myripristis violacea,Lattice soldierfish",
            };
            
            for (String row : rows)
            {
                String[] cols = StringUtils.split(row, ",");
                if (fullName.trim().equals(cols[1].trim()))
                {
                    return "T"+cols[0]+".jpg";
                }
            }
        }
        return null;
    }
    
    /**
     * @param dataObj
     * @return
     */
    protected String fakeGetImageName(final Object dataObj)
    {
        if (dataObj == null)
        {
            return null;
        }
        
        if (dataObj instanceof CollectionObject)
        {
            return fakeGetImageName(getTaxonFromCollectionObject((CollectionObject)dataObj));
        }
        
        if (dataObj instanceof Taxon)
        {
            Taxon tx = (Taxon)dataObj;
            String fullName = tx.getFullName();
            if (fullName.equals("Ammocrypta clara"))
            {
                return "WesternSandDarterLarge.jpg";
                
            } else if (fullName.equals("Ammocrypta beanii"))
            {
                return "NakedSandDarterLarge.jpg";
            } else
            {
                return getTaxonImage(tx.getFullName());
            }
        } else if (dataObj instanceof Locality)
        {
            Locality loc = (Locality)dataObj;
            if (loc.getId() == 3787)
            {
                return "BSRiver.jpg";
            }
        } else if (dataObj instanceof CollectingEvent)
        {
            CollectingEvent ce = (CollectingEvent)dataObj;
            if (ce.getId() == 4479)
            {
                return "BSRiver.jpg";
            }
        }
        return null;
    }
    
    /**
     * @return
     */
    protected String createJSONTable()
    {
        try
        {
            String[] headers     = {"Company", "Price",  "Change", "% Change",  "Last Updated"};
            String[] dataIndexes = {"company", "price",  "change", "pctChange", "lastChange"};
            String[] renders     = {null,      "usMoney", null,     null,        "Ext.util.Format.dateRenderer('m/d/Y')"};
            int[]    widths  =     {160,       75,        75,       75,           85};
            
            Vector<JSONObject> colModelList = new Vector<JSONObject>();
            for (int i=0;i<headers.length;i++)
            {
                JSONObject colObj = new JSONObject();
                if (i == 0)
                {
                    colObj.accumulate("id", dataIndexes[i]);  
                }
                colObj.accumulate("header", headers[i]);
                colObj.accumulate("width", widths[i]);
                colObj.accumulate("sortable", true);
                
                if (dataIndexes[i] != null)
                {
                    colObj.accumulate("dataIndex", dataIndexes[i]);
                }
                if (renders[i] != null)
                {
                    colObj.accumulate("renderer", renders[i]);
                }
                colModelList.add(colObj);
            }
            
            JSONArray colModel = new JSONArray();
            colModel.addAll(colModelList);
            
            System.out.println("COLMODEL: "+colModel.toString());
            
            String[] hdrName = {"company", "price", "change", "pctChange", "lastChange"};
            String[] hdrType = {"string",  "float",  "float",  "float",     "date"};
            
            Vector<JSONObject> colHdrList = new Vector<JSONObject>();
            for (int i=0;i<hdrName.length;i++)
            {
                JSONObject hdr = new JSONObject();
                hdr.accumulate("name", hdrName[i]);
                hdr.accumulate("type", hdrType[i]); 
                if (hdrType[i].equals("date"))
                {
                    hdr.accumulate("dateFormat", "n/j h:ia"); 
                }
                colHdrList.add(hdr);
            }
            JSONArray headerModel = new JSONArray();
            headerModel.addAll(colHdrList);
            
            System.out.println("HEADER: "+headerModel.toString());
            
            
            Vector<JSONArray> dataList = new Vector<JSONArray>();
            for (int i=0;i<5;i++)
            {
                Object[] data = {"3m Co", 71.72, 0.02+i, 0.03, "9/1 12:00am"};
                Vector<Object> dataItemList = new Vector<Object>();
                for (Object o : data)
                {
                    dataItemList.add(o);
                }
                JSONArray row = new JSONArray();
                row.addAll(dataItemList);
                dataList.add(row);
            }
            JSONArray dataModel = new JSONArray();
            dataModel.addAll(dataList);
            
            JSONObject json = new JSONObject();
            
            json.accumulate("column_model", colModel);
            json.accumulate("headers",      headerModel);
            json.accumulate("rows",         dataModel);
            
            System.out.println("===============");
            System.out.println(json.toString());
            System.out.println("===============");
            
            if (true)
            {
                if (true)
                {
                    String colModelStr = 
                     "[" +
                     "{id:'company',header: \"Company\", width: 160, sortable: true, dataIndex: 'company'}," +
                     "{header: \"Price\", width: 75, sortable: true, renderer: 'usMoney', dataIndex: 'price'}," +
                     "{header: \"Change\", width: 75, sortable: true, dataIndex: 'change'}," +
                     "{header: \"% Change\", width: 75, sortable: true, dataIndex: 'pctChange'}," +
                     "{header: \"Last Updated\", width: 85, sortable: true, renderer: Ext.util.Format.dateRenderer('m/d/Y'), dataIndex: 'lastChange'}" +
                     "]";
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("{");
                    sb.append("rows:[['3m Co',71.72,0.02,0.03,'9/1 12:00am'],['2m Co',71.72,0.02,0.03,'9/1 12:00am']],");
                    sb.append("headers:[{type:'string',name:'company'},{type:'float',name:'price'},{type:'float',name:'change'},{type:'float',name:'pctChange'},{type:'date',dateFormat:'n/j h:ia',name:'lastChange'}],");
                    sb.append("column_model:"+colModelStr);
                    sb.append("}");
                    return sb.toString();
                } else
                {
                    String  str = "{" +
                    		      "column_model:[{id:\"company\",header: \"Company\",width:160,sortable:true,dataIndex:\"company\"},{header:\"Price\",width:75,sortable:true,dataIndex:\"price\",renderer:\"usMoney\"},{header:\"Change\",width:75,sortable:true,dataIndex:\"change\"},{header:\" Change\",width:75,sortable:true,dataIndex:\"pctChange\"},{header:\"Last Updated\",width:85,sortable:true,dataIndex:\"lastChange\",renderer:Ext.util.Format.dateRenderer(\"m/d/Y\")}]," +
                    		      "headers:[{name:\"company\",type:\"string\"},{name:\"price\",type:\"float\"},{name:\"change\",type:\"float\"},{name:\"pctChange\",type:\"float\"},{name:\"lastChange\",type:\"date\",dateFormat:\"n/j h:ia\"}]," +
                    		      "rows:[[\"3m Co\",71.72,0.02,0.03,\"9/1 12:00am\"],[\"2m Co\",71.72,0.02,0.03,\"9/1 12:00am\"],[\"1m Co\",71.72,0.02,0.03,\"9/1 12:00am\"],[\"3m Co\",71.72,0.02,0.03,\"9/1 12:00am\"],[\"3m Co\",71.72,0.02,0.03,\"9/1 12:00am\"]]" +
                    		      "}";
                    return str;

                }
            }
            return json.toString();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(final HttpServletRequest request, 
                          final HttpServletResponse response) throws ServletException, IOException
    {
        System.out.println("doPost -------------------------------");
        
        String treeSearch = request.getParameter("treeSearchStr");
        String treeType   = request.getParameter("tree");
        String exeQuery   = request.getParameter("exequery");
        String detail     = request.getParameter("detail");
        
        System.out.println("detail cls["+detail+"]");
        if (StringUtils.isNotEmpty(detail))
        {
            String className = request.getParameter("cls");
            String idStr     = request.getParameter("id");
            
            System.out.println("cls["+className+"] id["+idStr+"]");
            
            idStr = "4222";
            response.setContentType("text/html");
            PrintWriter out     = response.getWriter();
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                Object      dataObj = session.createQuery("FROM "+className+" WHERE id = "+idStr, false).list().get(0);
                System.out.println(dataObj);
                if (dataObj != null)
                {
                    processDataObj(out, (FormDataObjIFace)dataObj,  true);
                    session.evict(dataObj);
                    
                } else
                {
                    out.println("ID "+idStr + "couldn't be found.");
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                //log.error(ex);
                
            } finally
            {
                session.close();    
            }
            return; 
        }
        
        //System.out.println(treeType);
        
        if (StringUtils.isNotEmpty(treeSearch))
        {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.print(searchTree(treeSearch, treeType));
            out.flush();
            return; 
        }
        
        String searchStr = request.getParameter("searchStr");
        System.out.println("searchStr["+searchStr+"]");
        if (expressSearch == null)
        {
            expressSearch         = new ExpressSearchTask();
            expressSearchExplorer = new ExplorerESPanel();
        } else
        {
            expressSearchExplorer.reset();
        }
        
        if (StringUtils.isNotEmpty(exeQuery))
        {
            doQuery(request, response, exeQuery);
            return; 
        }

        
        if (expressSearch.doQuery(searchStr, expressSearchExplorer))
        {
            if (StringUtils.isNotEmpty(searchStr))
            {
                JSONArray tableArray = expressSearchExplorer.getTables();
                
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.print(tableArray.toString());
                out.flush();
                return; 
                
            }
            return; 
        }
        
        super.doPost(request, response);
    }
    
    /**
     * @param request
     * @param response
     * @param queryName
     * @throws IOException
     */
    protected void doQuery(final HttpServletRequest request, 
                           final HttpServletResponse response,
                           final String queryName) throws IOException
    {
        Session session = null;
        try
        {
            session = HibernateUtil.getNewSession();
            SpQuery spQuery = (SpQuery)session.createQuery("FROM SpQuery WHERE name = '"+queryName+"'").list().get(0);
            if (spQuery != null)
            {
                if (queryTask == null)
                {
                    queryTask = new QueryTask();
                }
                
                expressSearchExplorer.reset();
                
                QueryBldrPane qbPane = new QueryBldrPane("Query", queryTask, spQuery, true);
                qbPane.setEsrp(expressSearchExplorer);
                
                qbPane.doSearch();
                JSONArray tableArray = expressSearchExplorer.getTables();
                
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.print(tableArray.toString());
                out.flush(); 
                
                qbPane.setEsrp(null);
                
            } else
            {
                System.out.println("Couldn't find the query with name = "+queryName);
                
                PrintWriter out = response.getWriter();
                out.print("Couldn't find the query with name = "+queryName);
                out.flush();
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
                          
    /**
     * @param out
     */
    protected void doQuery(final HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        //PrintWriter out = response.getWriter();
    }
    
    /**
     * @param out
     */
    protected void showQueryIndex(final HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        int    inx        = template.indexOf(contentTag);
        String subContent = template.substring(0, inx);
        
        out.println(StringUtils.replace(subContent, "<!-- Title -->", "Queries"));

        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            Properties props = new Properties();
            int cnt = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("<BR><table width=\"100%\" cellspacing=\"0\" class=\"brdr\">\n");
            List<?> list = session.getDataList("SELECT sq.id, sq.name FROM SpQuery sq INNER JOIN sq.specifyUser spu WHERE spu.id = "+AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getId());
            for (Object rowObj : list)
            {
                Object[] cols   = (Object[])rowObj;
                props.clear();
                props.put("id", cols[0]);
                props.put("query", "1");
                String   anchor = makeURLLink(null, cols[1].toString(), props);
                sb.append("<tr><td class=\"brdr"+((cnt % 2 == 0) ? "even" : "odd")+"\">"+anchor+"</td></tr>\n");
                cnt++;
            }
            sb.append("</TABLE>");
            out.println(sb.toString());
            
        } catch (org.hibernate.exception.SQLGrammarException ex)
        {
            log.error(ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        out.println(template.substring(inx+contentTag.length()+1, template.length()));

    }
    
    /**
     * @param request
     * @param response
     */
    protected void doReport(final HttpServletRequest request, 
                            final HttpServletResponse response)
    {
        JasperReportHelper jrh = new JasperReportHelper(new JasperReportHelper.JasperReportHelperListener() 
        {
            public void complete(JasperPrint jasperPrint)
            {
                PrintWriter out = null;
                try
                {
                    out = response.getWriter();
    
                    if (false)
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


                        JRHtmlExporter exporter = new JRHtmlExporter();
                        request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
                        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint); 
                        exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, out);                   
                         
                        // gonna set url pattern given for Image servlet with a reponse parameter <url-pattern>/image</url-pattern> 
                         
                        exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "http://localhost/specifyexplorer/");         
        
                        exporter.exportReport();
                    }
    
                    out.close();
                    
                } catch (JRException ex)
                {
                    ex.printStackTrace();
                    out.println(ex.toString());
                    
                } catch (IOException ex)
                {
                    ex.printStackTrace();
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
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            //RecordSet rs = (RecordSet)session.createQuery("from RecordSet where name = \"Beanii RS\"").list().get(0);
            RecordSet rs = (RecordSet)session.createQuery("from RecordSet where id = 1", false).list().get(0);
            if (rs != null)
            {
                Properties params = new Properties();
                
                String reportName = "fish_label.jrxml";
                params.put("name", reportName);
                params.put("file", reportName);
                params.put("reqrs", "true");
                params.put("title", "Fish Label");
                
                jrh.createReport(reportName, rs, params);
                
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
            
        } finally
        {
            session.close();
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doGet(final HttpServletRequest request, 
                      final HttpServletResponse response) throws ServletException, IOException
    {
        // set session info if needed

        String className = request.getParameter("cls");
        String idStr     = request.getParameter("id");
        String letter    = request.getParameter("ltr");
        String treeStr   = request.getParameter("tree");
        String query     = request.getParameter("query");
        String cmd       = request.getParameter("cmd");
        
        //System.out.println("doGet -------------------------------["+treeStr+"]["+idStr+"]");
        if (StringUtils.isNotEmpty(cmd))
        {
            if (cmd.equals("query"))
            {
                if (StringUtils.isEmpty(idStr))
                {
                    queryHandler.listQueries(request, response);
                } else
                {
                    
                }
            } else if (cmd.equals("reports"))
            {
                if (StringUtils.isEmpty(idStr))
                {
                    queryHandler.listSpReports(request, response);
                } else
                {
                    queryHandler.doReport(request, response);
                }
            }
            return;
        }

        if (StringUtils.isNotEmpty(treeStr))
        {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            int         id  = Integer.parseInt(idStr);
            if (id > 0)
            {
                DBTableInfo tableInfo  = DBTableIdMgr.getInstance().getByShortClassName(treeStr);
                //System.out.println(createTree(tableInfo.getClassObj(), id));
                out.print(createTree(tableInfo.getClassObj(), id));
            }
            out.flush();
            return;
        }
        
        if (StringUtils.isNotEmpty(idStr) && idStr.equals("report"))
        {
            doReport(request, response);
            return;
        }
        
        if (StringUtils.isNotEmpty(query))
        {
            if (StringUtils.isNotEmpty(idStr))
            {
                
            } else
            {
                showQueryIndex(response);
                return;
            }
        }
        
        if (StringUtils.isEmpty(idStr) && StringUtils.isEmpty(className))
        {
            idStr = "search";
        }
        
        if (StringUtils.isNotEmpty(idStr))
        {
            if (idStr.equals("genCEChart"))
            {
                generateCEChart(request, response);
                return;
                
            } else if (idStr.equals("genCOChart"))
            {
                generateColObjChart(request, response);
                return;
            }
            
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            if (idStr.equals("search"))
            {
                displayAllSearchables(out);
                
            } else if (idStr.equals("dspsrch"))
            {
                displaySearchForm(out, packageName+className);
                
            } else if (idStr.equals("dosearch"))
            {
                processSearch(out, packageName+className, request);
                
            } else if (idStr.equals("map"))
            {
                createMap(out, request.getParameter("points"), true);
                
            } else if (idStr.equals("ChartCE"))
            {
                chartIt(out, "genCEChart", "CollectingEvents By Year");
                
            } else if (idStr.equals("ChartCO"))
            {
                chartIt(out, "genCOChart", "Cataloged By Year");
                
            } else
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    Object dataObj = session.createQuery("from "+className+" where id = "+idStr, false).list().get(0);
                    if (dataObj != null)
                    {
                        processDataObj(out, (FormDataObjIFace)dataObj, true);
                    } else
                    {
                        out.println("ID "+idStr + "couldn't be found.");
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    //log.error(ex);
                    
                } finally
                {
                    session.close();    
                }
            }
        } else
        {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            
            if (className.equals("Taxon"))
            {
                String sql = "SELECT DISTINCT taxon.TaxonID, taxon.FullName  "+
                             " FROM determination " +
                             " INNER JOIN taxon ON determination.TaxonID = taxon.TaxonID " +
                             " WHERE determination.IsCurrent = true AND taxon.TaxonTreeDefID = TAXTREEDEFID ORDER BY taxon.FullName ASC";
                sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
                
                doAlphaIndexPageSQL(out, className, letter, 1, null, sql);
                
            } else
            {
                ClassDisplayInfo cdi = classHash.get(className);
                if (cdi != null)
                {
                    doAlphaIndexPage(out, className, cdi.getIndexName(), letter);
                    
                } else
                {
                    out.println("Bad Class name["+className+"]");
                }
            }
        }
    }
    
    class MyFmtMgr extends SpecifyUIFieldFormatterMgr
    {
        /**
         * Returns the DOM it is suppose to load the formatters from.
         * @return Returns the DOM it is suppose to load the formatters from.
         */
        protected Element getDOM() throws Exception
        {
            return XMLHelper.readDOMFromConfigDir("backstop/uiformatters.xml");
        }
        
        public UIFieldFormatterIFace getFmt(final String name)
        {
            return getFormatterInternal(name);

        }
    }
    
     
    class CEForPoly 
    {
        protected Calendar startDate;
        protected Double lat;
        protected Double lon;
        protected String title;
        
        protected List<CollectionObject> colObjs = new Vector<CollectionObject>();

        public CEForPoly(Calendar startDate, Double lat, Double lon, String title)
        {
            super();
            this.startDate = startDate;
            this.lat = lat;
            this.lon = lon;
            this.title = title;
        }

        /**
         * @return the startDate
         */
        public Calendar getStartDate()
        {
            return startDate;
        }

        /**
         * @return the lat
         */
        public Double getLat()
        {
            return lat;
        }

        /**
         * @return the lon
         */
        public Double getLon()
        {
            return lon;
        }

        /**
         * @return the title
         */
        public String getTitle()
        {
            return title;
        }

        /**
         * @return the colObjs
         */
        public List<CollectionObject> getColObjs()
        {
            return colObjs;
        }
    }
    
    class NameId implements Comparable<NameId>
    {
        protected String  name;
        protected int     id;
        protected Integer num;
        
        public NameId(String name, int id)
        {
            super();
            this.name = name;
            this.id = id;
        }
        
        public NameId(String name, int id, Integer num)
        {
            this(name, id);
            this.num = num;
        }
        
        public void add()
        {
            id++;
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return the id
         */
        public int getId()
        {
            return id;
        }

        /**
         * @return the num
         */
        public Integer getNum()
        {
            return num;
        }

        public int compareTo(NameId arg0)
        {
            return num != null ? num.compareTo(arg0.num) : name.compareTo(arg0.name);
        }
    }
}

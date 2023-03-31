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
package edu.ku.brc.specify.utilapps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Session;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterFactory;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonCitation;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 6, 2007
 *
 */
public class SiteGen
{
    private final Logger         log      = Logger.getLogger(SiteGen.class);
    
    protected static Session  session = null;
    protected static MyFmtMgr fmtMgr  = null;
    protected SimpleDateFormat dateFormatter;
    protected Hashtable<Class<?>, Boolean> classHash = new Hashtable<Class<?>, Boolean>();
    
    protected String template = "";
    
    public SiteGen()
    {
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
            template = FileUtils.readFileToString(new File("site/template.html"));
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    protected Writer createWriter(final FormDataObjIFace dataObj)
    {
        Writer oFile = null;
        try
        {
            oFile = new BufferedWriter( new FileWriter(new File("site/"+makeFileName(dataObj))));
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return oFile;
    }
    
    /**
     * Setup all the System properties. This names all the needed factories. 
     */
    protected void setUpSystemProperties()
    {
        // Name factories
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions
        System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory");
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.utilapps.LocalDiskUIFieldFormatterMgr");    // Needed for CatalogNumberign
        System.setProperty(DataObjFieldFormatMgr.factoryName,           "edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr");     // Needed for WebLnkButton //$NON-NLS-1$
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
        
        log.info("Logging into "+dbName+"...");
        
        String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, hostName, dbName);
        if (connStr == null)
        {
            connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, dbName);
        }
        
        Pair<String, String> usernamePassword = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
        if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                driverInfo.getDialectClassName(), 
                dbName, 
                connStr, 
                usernamePassword.first, 
                usernamePassword.second))
        {
            log.info("Login Failed!");
            return false;
        }         
        
        session = HibernateUtil.getCurrentSession();

        log.info("Creating database "+dbName+"...");
        
        try
        {
            Thumbnailer thumb = Thumbnailer.getInstance();
            File thumbFile = XMLHelper.getConfigDir("thumbnail_generators.xml");
            thumb.registerThumbnailers(thumbFile);
            thumb.setQuality(.5f);
            thumb.setMaxSize(128, 128);
    
            AttachmentManagerIface attachMgr = new FileStoreAttachmentManager(UIRegistry.getAppDataSubDir("AttachmentStorage", true));
            AttachmentUtils.setAttachmentManager(attachMgr);
            AttachmentUtils.setThumbnailer(thumb);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
        
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
        
        //UIRegistry.setJavaDBDir(derbyPath != null ? derbyPath : UIRegistry.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
        
        setUpSystemProperties();
        
        DisciplineType         disciplineType = DisciplineType.getDiscipline("fish");
        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
        
        setupDatabase(driverInfo, "localhost", "testfish", "rods", "rods", "rods", "rods", "guest@ku.edu", disciplineType);
    }
    
    /**
     * @param field
     * @param dataObj
     * @return
     * @throws Exception
     */
    protected Object getData(final Field field, final Object dataObj) throws Exception
    {
        try
        {
            String methodName = "get" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
            Method method = dataObj.getClass().getMethod(methodName, (Class<?>[])null);
            if (method != null)
            {
                return method.invoke(dataObj, (Object[])null);
            } else
            {
                log.error("Missing method add(Object) for this type of set ["+dataObj.getClass()+"]");
            }
        } catch (NoSuchMethodException ex) {}
        return null;
    }
    
    /**
     * @param fdi
     * @return
     */
    protected String formatFDI(final FormDataObjIFace fdi)
    {
        if (classHash.get(fdi.getDataClass()) != null)
        {
            return "<a href=\""+makeFileName(fdi)+"\">"+fdi.getIdentityTitle()+"</a>";
        }
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
        } 
        return data.toString();
    }
    
    /**
     * @param dataObj
     * @return
     */
    protected String makeFileName(final FormDataObjIFace dataObj)
    {
        return dataObj.getDataClass().getSimpleName() + dataObj.getId() + ".html";
    }
    
    /**
     * @param dataObj
     */
    protected void processDataObj(final FormDataObjIFace dataObj)
    {
        processDataObj(dataObj, true);
    }
    
    /**
     * @param dataObj
     */
    protected void processDataObj(final FormDataObjIFace dataObj, final boolean doChildrenSets)
    {
        //DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(dataObj.getDataClass().getName());
        
        StringBuilder sb = new StringBuilder();
        try
        {
            //sb.append("<html><head><title>"+dataObj.getIdentityTitle()+"</head><body>");
            sb.append("<table>\n");
            for (Field field : dataObj.getClass().getDeclaredFields())
            {
                if (field.getName().endsWith("Id"))
                {
                    continue;
                }
                
                /*String labelName = field.getName();
                DBFieldInfo fi = tblInfo.getFieldByName(field.getName());
                if (fi != null)
                {
                    fi.getColumn()
                }*/
                try
                {
                    Object data = getData(field, dataObj);
                    if (data != null)
                    {
                        if (!(data instanceof Set<?>))
                        {
                            
                            sb.append("<tr><td align=\"right\">"+UIHelper.makeNamePretty(field.getName())+ ":</td><td nowrap=\"true\">"+formatValue(data)+"</td></tr>\n");
                        }
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            sb.append("</table>\n");
            
            if (doChildrenSets)
            {
                for (Field field : dataObj.getClass().getDeclaredFields())
                {
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
                                    sb.append("<br/>"+UIHelper.makeNamePretty(field.getName())+"<br/>\n");
                                    sb.append("<table width=\"100%\" cellspacing=\"0\" class=\"brdr\">\n");
                                    int cnt = 1;
                                    for (Object setDataObj : set)
                                    {
                                        String fdiStr = formatFDI((FormDataObjIFace)setDataObj);
                                        sb.append("<tr><td class=\"brdr"+((cnt % 2 == 0) ? "even" : "odd")+"\">"+fdiStr+"</td></tr>\n");    
                                        cnt++;
                                    }
                                    sb.append("</table>\n");
                                }
                            }
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            Writer output = createWriter(dataObj);
            String content = template;
            content = StringUtils.replace(content, "<!-- Title -->", dataObj.getIdentityTitle());
            content = StringUtils.replace(content, "<!-- Content -->", sb.toString());
            output.write(content);
            output.flush();
            output.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        log.info("Done "+makeFileName(dataObj));
        /*
        for (Method method : dataObj.getClass().getMethods())
        {
            if (method.getName().startsWith("get"))
            {
                try
                {
                    Object data = method.invoke(dataObj, new Object[]{});
                    if (data)
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }*/
        
    }
    
    public void process(final Class<?> clazz)
    {
        process(clazz, true);
    }
    
    public void process(final Class<?> clazz, final boolean doChildrenSets)
    {
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery("select "+clazz.getSimpleName()+"ID from "+clazz.getSimpleName().toLowerCase());
            while (rs.next())
            {
                int id = rs.getInt(1);
                Object dataObj = session.createQuery("from "+clazz.getSimpleName()+" where id = "+id).list().get(0);
                processDataObj((FormDataObjIFace)dataObj, doChildrenSets);
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
        log.info("Done");
    }
    
    public void process()
    {
        Collection collection = (Collection)session.createCriteria(Collection.class).list().get(0);
        AppContextMgr.getInstance().setClassObject(Collection.class, collection);
        
        Class<?>[] classes = {CollectionObject.class, CollectingEvent.class, Determination.class, Preparation.class, 
                              Agent.class, Locality.class, Taxon.class, Storage.class,Geography.class, Collector.class,
                              Shipment.class, TaxonCitation.class, ReferenceWork.class, Journal.class,
                              //LithoStrat.class,
        };
        process(classes);
        
    }
    
    public void process(Class<?>[] classes)
    {
        for (Class<?> cls : classes)
        {
           classHash.put(cls, true); 
        }
        
        for (Class<?> cls : classes)
        {
           process(cls);
        }
        
    }
    
    public void doAlphaIndexPage(final Class<?> clazz, final String fieldName)
    {
        StringBuilder sb = new StringBuilder();
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery("select "+clazz.getSimpleName()+"ID, "+fieldName+" from "+clazz.getSimpleName().toLowerCase()+" order by "+fieldName+" asc");
            
            char currChar = '_';

            while (rs.next())
            {
                String name = rs.getString(2);
                int    id   = rs.getInt(1);
                if (StringUtils.isEmpty(name))
                {
                    name = rs.getString(1);
                }
                //list.add(new SorterInfoStruct(name, rs.getInt(1)));
                if (currChar != name.charAt(0))
                {
                    currChar = name.charAt(0);
                    sb.append("<br/>"+currChar+"<br/>\n");
                }
                sb.append("<a href=\""+clazz.getSimpleName()+id+".html\">" +name+ "</a><br/>\n");
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
        
        Writer oFile = null;
        try
        {
            oFile = new BufferedWriter( new FileWriter(new File("site/"+clazz.getSimpleName()+".html")));
            String content = template;
            content = StringUtils.replace(content, "<!-- Title -->", clazz.getSimpleName());
            content = StringUtils.replace(content, "<!-- Content -->", sb.toString());
            oFile.write(content);
            oFile.flush();
            oFile.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        log.info("Done");

    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        SiteGen siteGen = new SiteGen();
        siteGen.setUp();
        siteGen.process();
        
        siteGen.doAlphaIndexPage(Taxon.class,      "fullName");
        siteGen.doAlphaIndexPage(Geography.class,  "name");
        //siteGen.doAlphaIndexPage(LithoStrat.class, "name");
        siteGen.doAlphaIndexPage(Storage.class,   "fullName");
        siteGen.doAlphaIndexPage(CollectionObject.class,   "catalogNumber");
    }
    
    
    class MyFmtMgr extends SpecifyUIFieldFormatterMgr
    {
        public MyFmtMgr()
        {
            super();
            load();
        }
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

}

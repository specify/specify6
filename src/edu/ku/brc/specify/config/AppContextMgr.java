/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.config;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;

import edu.ku.brc.af.core.AppContextIFace;
import edu.ku.brc.af.prefs.AppPrefsIFace;
import edu.ku.brc.af.prefs.AppPrefsMgr;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.UIHelper;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.forms.ViewSetMgr;
import edu.ku.brc.ui.forms.ViewSetMgrManager;
import edu.ku.brc.ui.forms.persist.ViewSet;

/**
 * This class provides the current context of the Specify application. The context consists of the following:<br>
 * <ol>
 * <li>The User Name
 * <li>The Database Name (database connection)
 * <li>The Specify User Object
 * <li>The CatalogSeries
 * <li>The CollectionObjDef
 * <li>The Discipline Name
 * </ol>
 * <p>The AppContextMgr will place data in a <i>username</i>/<i>databaseName</i> directory in the "application data" directory of the user.
 * On Windows this is <code>\Documents and Settings\&lt;User Name&gt;\Application Data\Specify</code>. 
 * On Unix platforms it is <code>/<i>user home</i>/.Specify</code> (Note: the app data dir is created by UICacheManager)</p>
 * <p>
 * The ViewSetMgrManager needs to load the "backstop" ViewSetMgr and the "user" ViewSetMgr in order for the application to work correctly.
 * So this class uses the "discipline name" to initialize the APPDATA dir with the appropriate data, which includes a "standard" set of 
 * Views for that discipline. The APPDATA dir is really the "working space" of the application for a particular username/database.
 * </p>
 * @code_status Complete
 *
 * @author rods
 */
public class AppContextMgr implements AppContextIFace
{
    private static final Logger  log      = Logger.getLogger(AppContextMgr.class);
    private static AppContextMgr instance = new AppContextMgr();
    
    protected Hashtable<String, Discipline> hash = new Hashtable<String, Discipline>();
    
    protected File        currentContextDir = null;
    protected File        backStopDir       = null;
    
    protected String      disciplineName    = null;  
    protected String      databaseName      = null;
    protected String      userName          = null;
    protected SpecifyUser user              = null;

    /**
     * Singleton Constructor.
     */
    protected AppContextMgr()
    {
        init();
    }
    
    /**
     * Returns singleton.
     * @return singleton.
     */
    public static AppContextMgr getInstance()
    {
        return AppContextMgr.instance;
    }
    
    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     */
    protected void init()
    {
        try
        {
            Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("disciplines.xml")));
            if (root != null)
            {
                for ( Iterator i = root.elementIterator( "discipline" ); i.hasNext(); ) 
                {
                    Element disciplineNode = (Element) i.next();
                    
                    String name   = getAttr(disciplineNode, "name", null);
                    String title  = getAttr(disciplineNode, "title", null);
                    int    type   = getAttr(disciplineNode, "type", 0);
                    
                    Discipline discipline = new Discipline(name, title, type);
                    hash.put(discipline.getName(), discipline);
                }
            } else
            {
                String msg = "The root element for the document was null!";
                log.error(msg);
                throw new ConfigurationException(msg);
            } 
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }

    }

    
    /**
     * Returns the list of Discipline objects.
     * @return the list of Discipline objects
     */
    public List<Discipline> getDisciplines()
    {
        List<Discipline> list = new ArrayList<Discipline>();
        for (Enumeration<Discipline> e=hash.elements();e.hasMoreElements();)
        {
            list.add(e.nextElement());
        }
        return list;
    }
    
    /**
     * Returns a Discipline by name.
     * @param name the name of the discipline
     * @return a Discipline by name.
     */
    public Discipline get(final String name)
    {
        return hash.get(name);
    }
    
    /**
     * Returns a Discipline by title.
     * @param title the title of the discipline
     * @return a Discipline by title.
     */
    public Discipline getByTitle(final String title)
    {
        for (Enumeration<Discipline> e=hash.elements();e.hasMoreElements();)
        {
            Discipline dis = e.nextElement();
            if (title.equals(dis.getTitle()))
            {
                return dis;
            }
        }
        return null;
    }
    
    /**
     * Sets up the "current" Catalog Series by first checking prefs for the most recent primary key, 
     * if it can't get it then it asks the user to select one. (Note: if there is only one it automatically chooses it)
     * @param user the user object of the current object
     * @param alwaysAsk indicates the User should always be asked which Catalog Series to use
     * @return the current Catalog Series or null
     */
    @SuppressWarnings("unchecked")
    public CatalogSeries setupCurrentCatalogSeries(final SpecifyUser user, final boolean alwaysAsk)
    {
        final String prefName = mkUserDBPrefName("recent_catalogseries_id");
        
        CatalogSeries catSeries = CatalogSeries.getCurrentCatalogSeries();
        
        if (catSeries == null || alwaysAsk)
        {
            AppPrefsIFace appPrefs    = AppPrefsMgr.getInstance();
            boolean       askToSelect = true;
            if (!alwaysAsk)
            {
                Integer recentId = appPrefs.getInt(prefName, null);
                if (recentId != null)
                {
                    Query query = HibernateUtil.getCurrentSession().createQuery( "From CatalogSeries where catalogSeriesId = "+recentId.toString()); 
                    List list = query.list();          
                    if (list.size() == 1)
                    {
                        catSeries = (CatalogSeries)list.get(0);
                        askToSelect = false;
                    }
                }
            }
            
            if (askToSelect)
            {
                String queryStr = "select cs From CollectionObjDef as cod Inner Join cod.specifyUser as user Inner Join cod.catalogSeries as cs where user.specifyUserId = "+user.getSpecifyUserId();
                Query query = HibernateUtil.getCurrentSession().createQuery(queryStr); 
                List list = query.list();
                
                if (list.size() == 1)
                {
                    catSeries = (CatalogSeries)list.get(0);
                    CatalogSeries.setCurrentCatalogSeries(catSeries);
                    
                } else if (list.size() > 0)
                {
                    Collections.sort(list);
                    
                    ChooseFromListDlg dlg = new ChooseFromListDlg("Choose a Catalog Series", list); // TODO I18N
                    dlg.setAlwaysOnTop(true);
                    dlg.setModal(true);
                    
                    UIHelper.centerAndShow(dlg);
                    if (!dlg.isCancelled())
                    {
                        catSeries = (CatalogSeries)dlg.getSelectedObject();
                        CatalogSeries.setCurrentCatalogSeries(catSeries);
                    } 
                } else
                {
                    // TODO error dialog
                    
                }
                
                if (catSeries != null)
                {
                    appPrefs.putInt(prefName, catSeries.getCatalogSeriesId());
                } else
                {
                    appPrefs.remove(prefName);
                }
            }
        }
        
        return catSeries;
    }
    
    /**
     * Sets up the "current" CollectionObjDef by first checking prefs for the most recent primary key, 
     * if it can't get it then it asks the user to select one. (Note: if there is only one it automatically chooses it)
     * @param catalogSeries the current XatalogSeries
     * @param alwaysAsk true means always ask which CollectionObjDef 
     * @return the current CollectionObjDef or null
     */
    @SuppressWarnings("unchecked")
    public CollectionObjDef setupCurrentColObjDef(final CatalogSeries catalogSeries, final boolean alwaysAsk)
    {
        if (catalogSeries == null)
        {
            return null;
        }
        final String prefName = mkUserDBPrefName("recent_colobjdef_id");
        
        CollectionObjDef colObjDef = CollectionObjDef.getCurrentCollectionObjDef();
        
        if (colObjDef == null || alwaysAsk)
        {
            AppPrefsIFace appPrefs    = AppPrefsMgr.getInstance();
            boolean       askToSelect = true;
            if (!alwaysAsk)
            {
                Integer recentId = appPrefs.getInt(prefName, null);
                if (recentId != null)
                {
                    Query query = HibernateUtil.getCurrentSession().createQuery( "From CollectionObjDef where collectionObjDefId = "+recentId.toString()); 
                    List list = query.list();          
                    if (list.size() == 1)
                    {
                        colObjDef = (CollectionObjDef)list.get(0);
                        askToSelect = false;
                    }
                }
            }
            
            if (askToSelect)
            {
                String queryStr = "select cod From CatalogSeries as cs Inner Join cs.collectionObjDefItems as cod where cs.catalogSeriesId = "+catalogSeries.getCatalogSeriesId();
                Query query = HibernateUtil.getCurrentSession().createQuery(queryStr); 
                List list = query.list();
                
                if (list.size() == 1)
                {
                    colObjDef = (CollectionObjDef)list.get(0);
                    CollectionObjDef.setCurrentCollectionObjDef(colObjDef);
                    
                } else if (list.size() > 1)
                {
                    Collections.sort(list);
                    
                    ChooseFromListDlg dlg = new ChooseFromListDlg("Choose a Collection Object Def", list); // TODO I18N
                    dlg.setAlwaysOnTop(true);
                    dlg.setModal(true);
                    
                    UIHelper.centerAndShow(dlg);
                    if (!dlg.isCancelled())
                    {
                        colObjDef = (CollectionObjDef)dlg.getSelectedObject();
                        CollectionObjDef.setCurrentCollectionObjDef(colObjDef);
                    } 
                } else
                {
                    // TODO error dialog
                    
                }
                
                if (colObjDef != null)
                {
                    appPrefs.putInt(prefName, colObjDef.getCollectionObjDefId());
                } else
                {
                    appPrefs.remove(prefName);
                }
            }
        }
        
        return colObjDef;
    }
    
    /**
     * Returns the File object that represents the directory for the current user and database.
     * @return the File object that represents the directory for the current user and database.
     */
    public File getCurrentContext()
    {
        if (currentContextDir == null)
        {
            throw new RuntimeException("currentContextDir is null and not initialized.");
        }
        return currentContextDir;
    }
    
    /**
     * Returns the File object that represents the directory for the current user and database plus the name passed in.
     * @param fileName a file name to be appended to the Current Context Directory
     * @return the File object that represents the directory for the current user and database plus the name passed in.
     */
    public File getCurrentContext(final String fileName)
    {
        if (currentContextDir == null)
        {
            throw new RuntimeException("currentContextDir is null and not initialized.");
        }
        return new File(currentContextDir.getAbsoluteFile() + File.separator + fileName);
    }
    
    /**
     * Returns a File (path) to the file. It first checks the current context directory and if it isn't found then
     * it checks the "config" directory.
     * @param fileName the file name
     * @return return a path to the file.
     */
    public File getFileFromDisciplineOrConfig(final String fileName)
    {
        File pathDir = getCurrentContext(fileName);
        if (!pathDir.exists())
        {
            pathDir = new File(XMLHelper.getConfigDirPath(fileName));
            if (!pathDir.exists())
            {
                throw new RuntimeException("Couldn't find file ["+fileName+"] in discipline or config dir");
            }
        }
        return pathDir;
    }

    /**
     * Reads an XML file from the current context or the config directory (as a backstop)
     * @param fileName the file name to read in as a DOM
     * @return the root Element for the XML document
     * @throws Exception from reading in the XML file
     */
    public Element readFileToDOM4J(final String fileName) throws Exception
    {
        File pathDir = getFileFromDisciplineOrConfig(fileName);
        if (pathDir.exists())
        {
            return XMLHelper.readFileToDOM4J(pathDir);

        }
        return null; // actually won't get here, an exception should have already been thrown
    }
    
    /**
     * Checks to see of the directory is there, and if not, then it creates it
     * @param path the path to the directory
     * @return the File object to the directory
     */
    protected File getOrCreateDir(final String path)
    {
        File file = new File(path);
        if (!file.exists())
        {
            if (!file.mkdir())
            {
                throw new RuntimeException("Couldn't create directory["+path+"]");
            }
        }
        return file;
    }
    
    /**
     * Helper function to create a user and database centric pref name
     * @param prefName the pref names
     * @return  a user and database centric pref name
     */
    protected String mkUserDBPrefName(final String prefName)
    {
        return prefName + "." + userName+ "." + databaseName;
    }
    
    
    /**
     * Sets the current context.
     * @param databaseName the name of the database 
     * @param userName the user name
     * @param user the user object
     * @return  true if the context was set correctly
     */
    public boolean setContext(final String databaseName, 
                              final String userName)
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(SpecifyUser.class);
        criteria.add(Expression.eq("name", userName));
        java.util.List list = criteria.list();
       
        
        if (list.size() == 1)
        {
            user = (SpecifyUser)list.get(0);
        } else
        {
            throw new RuntimeException("The user ["+userName+"] could  not be located as a Specify user.");
        }
        
        this.databaseName = databaseName;
        this.userName     = userName;
        
        AppPrefsIFace appPrefs = AppPrefsMgr.getInstance();
        
        Boolean isAccessionDB  = false;
        
        CatalogSeries catalogSeries = setupCurrentCatalogSeries(user, false);
        if (catalogSeries == null)
        {
            String isAccessionsPrefName = mkUserDBPrefName("isaccessions");
            isAccessionDB = appPrefs.getBoolean(isAccessionsPrefName, null);
            if (isAccessionDB == null) 
            {
                int rv = JOptionPane.showConfirmDialog(null, 
                        UICacheManager.getResourceString("IsAccessionDB"), 
                        UICacheManager.getResourceString("IsAccessionDBTitle"), 
                        JOptionPane.YES_NO_OPTION);
                isAccessionDB = rv == JOptionPane.YES_OPTION;
                appPrefs.putBoolean(isAccessionsPrefName,  isAccessionDB);
    
            }
        }
        
        if (isAccessionDB)
        {
            disciplineName = "accessions";
            
        } else
        {
            
            Integer recentId = appPrefs.getInt(mkUserDBPrefName("recent_catalogseries_id"), null);
            if (recentId == null)
            {
                //ChooseFromListDlg       
            }
            
            CollectionObjDef colObjDef = setupCurrentColObjDef(catalogSeries, false);
            disciplineName = colObjDef.getDiscipline();
        }
        
        String userPath = UICacheManager.getDefaultWorkingPath() + File.separator + userName;
        getOrCreateDir(userPath); // throws Runtime exception if not correct
        
        String dbPath = userPath + File.separator + databaseName;
        File checkDir = new File(dbPath);
        boolean dbDirExists = checkDir.exists() && checkDir.isDirectory() && checkDir.list().length > 0;

        
        currentContextDir  = getOrCreateDir(dbPath); // throws Runtime exception if not correct
        if (!dbDirExists)
        {
            File configDir = new File(XMLHelper.getConfigDirPath(disciplineName));
            
            try
            {
                FileUtils.copyDirectory(configDir, currentContextDir);
                
            } catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        initializeViewSetManager();
        
        return true;
    }
    
    /**
     * 
     */
    protected void initializeViewSetManager()
    {
        
        // Push BackStop on the Stack First
        ViewSetMgrManager.refresh(); // clear stack and adds the BackStop
        
        // The very first time we need to check to see if any ViewSets from the ViewSetMgr have been copied over
        ViewSetMgr contextViewSetMgr = new ViewSetMgr(getCurrentContext());
        if (!contextViewSetMgr.isRegistryExists())
        {
            // Ok, then we need to copy it over from the config directory
            
            ViewSetMgr configViewSetMgr = new ViewSetMgr(XMLHelper.getConfigDir(disciplineName));
            if (configViewSetMgr.isRegistryExists())
            {
                for (ViewSet vs : configViewSetMgr.getViewSets())
                {
                    //if (vs.getType() == ViewSet.Type.User)
                    //{
                        ViewSetMgrManager.copyViewSet(configViewSetMgr, contextViewSetMgr, vs.getName(), false);
                    //}
                }
            } else
            {
                throw new RuntimeException("Couldn't find a config ViewSetMgr at ["+configViewSetMgr.getContextDir()+"]");
            }
            
        }
        
        ViewSetMgrManager.pushViewMgr(contextViewSetMgr);
    }

    
}

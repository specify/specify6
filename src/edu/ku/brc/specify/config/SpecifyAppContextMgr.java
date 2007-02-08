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
import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AppResource;
import edu.ku.brc.specify.datamodel.AppResourceDefault;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.ViewSetObj;
import edu.ku.brc.ui.CheckboxChooserDlg;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.PickListItemIFace;
import edu.ku.brc.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.ViewSetMgr;
import edu.ku.brc.ui.forms.persist.View;
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
 * <p>The SpecifyAppResourceDefaultMgr will place data in a <i>username</i>/<i>databaseName</i> directory in the "application data" directory of the user.
 * On Windows this is <code>\Documents and Settings\&lt;User Name&gt;\Application Data\Specify</code>.
 * On Unix platforms it is <code>/<i>user home</i>/.Specify</code> (Note: the app data dir is created by UICacheManager)</p>
 * <p>
 * The ViewSetMgrManager needs to load the "backstop" ViewSetMgr and the "user" ViewSetMgr in order for the application to work correctly.
 * So this class uses the "discipline name" to initialize the APPDATA dir with the appropriate data, which includes a "standard" set of
 * Views for that discipline. The APPDATA dir is really the "working space" of the application for a particular username/database.
 * </p>
 *
 * @code_status Complete
 *
 * @author rods
 */
public class SpecifyAppContextMgr extends AppContextMgr
{
    private static final Logger  log = Logger.getLogger(SpecifyAppContextMgr.class);

    protected Hashtable<String, Discipline>    hash            = new Hashtable<String, Discipline>();
    protected List<AppResourceDefault>         appResourceList = new ArrayList<AppResourceDefault>();
    protected Hashtable<String, List<ViewSet>> viewSetHash     = new Hashtable<String, List<ViewSet>>();

    protected String         databaseName          = null;
    protected String         userName              = null;
    protected SpecifyUser    user                  = null;

    protected ViewSetMgr     backStopViewSetMgr    = null;
    protected AppResourceMgr backStopAppResMgr     = null;
    protected Agent          currentUserAgent      = null;


    /**
     * Singleton Constructor.
     */
    public SpecifyAppContextMgr()
    {
        init();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getInstance()
     */
    public static SpecifyAppContextMgr getInstance()
    {
        return (SpecifyAppContextMgr)AppContextMgr.getInstance();
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
    public Discipline getDiscipline(final String name)
    {
        return hash.get(name);
    }

    /**
     * Return the DatabaseName
     * @return the DatabaseName
     */
    public String getDatabaseName()
    {
        return databaseName;
    }

    /**
     * Return the UserName
     * @return the UaserName
     */
    public String getUserName()
    {
        return userName;
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
     * Returns the number of CatalogSeries that this user is connected to.
     * @return the number of CatalogSeries that this user is connected to.
     */
    public int getNumOfCatalogSeriesForUser()
    {
        String sqlStr = "select count(cs) From CollectionObjDef as cod Inner Join cod.specifyUser as user Inner Join cod.catalogSeries as cs where user.specifyUserId = "+user.getSpecifyUserId();
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        Object                   result     = session.getData(sqlStr);
        int                      count      =  result != null ? (Integer)result : 0;
        session.close();
        return count;
        
    }

    /**
     * Sets up the "current" Catalog Series by first checking prefs for the most recent primary key,
     * if it can't get it then it asks the user to select one. (Note: if there is only one it automatically chooses it)
     * @param user the user object of the current object
     * @param alwaysAsk indicates the User should always be asked which Catalog Series to use
     * @return the current Catalog Series or null
     */
    @SuppressWarnings("unchecked")
    protected List<CatalogSeries> setupCurrentCatalogSeries(final DataProviderSessionIFace sessionArg, final SpecifyUser user, final boolean alwaysAsk)
    {
        final String prefName = mkUserDBPrefName("recent_catalogseries_id");

        List<CatalogSeries> catSeries = CatalogSeries.getCurrentCatalogSeries(); // always return a List Object (might be empty)

        if (catSeries.size() == 0 || alwaysAsk)
        {
            AppPreferences appPrefs    = AppPreferences.getRemote();
            boolean        askToSelect = true;
            if (!alwaysAsk)
            {
                String recentIds = appPrefs.get(prefName, null);
                if (StringUtils.isNotEmpty(recentIds))
                {
                    List list = sessionArg.getDataList("From CatalogSeries where catalogSeriesId in ("+recentIds + ")");
                    for (Object obj : list)
                    {
                        catSeries.add((CatalogSeries)obj);
                    }
                    askToSelect = false;
                }
            }

            if (askToSelect)
            {
                String queryStr = "select distinct cs From CollectionObjDef as cod Inner Join cod.specifyUser as user Inner Join cod.catalogSeries as cs where user.specifyUserId = "+user.getSpecifyUserId();
                List list = sessionArg.getDataList(queryStr);
                if (list.size() == 1)
                {
                    catSeries.add((CatalogSeries)list.get(0));
                    CatalogSeries.setCurrentCatalogSeries(catSeries);

                } else if (list.size() > 0)
                {
                    //Collections.sort(list); // Why doesn't this work?

                    CheckboxChooserDlg<CatalogSeries> dlg = new CheckboxChooserDlg<CatalogSeries>((Frame)UICacheManager.get(UICacheManager.FRAME),
                                                                                                  "Choose a Catalog Series", 
                                                                                                  list); // TODO I18N
                    //dlg.setSelectedObjects(catSeries);
                    dlg.setModal(true);

                    UIHelper.centerAndShow(dlg);

                    catSeries.clear();
                    if (!dlg.isCancelled())
                    {
                        catSeries.addAll(dlg.getSelectedObjects());
                    } else
                    {
                        return null;
                    }
                    
                } else
                {
                    // Accession / Registrar / Director may not be assigned to any Catalog Series
                    // Or for a stand alone Accessions Database there may not be any 
                }

                if (catSeries.size() > 0)
                {
                    StringBuilder strBuf = new StringBuilder();
                    for (CatalogSeries cs : catSeries)
                    {
                        if (strBuf.length() > 0) strBuf.append(", ");
                        strBuf.append(Long.toString(cs.getCatalogSeriesId()));
                    }
                    appPrefs.put(prefName, strBuf.toString());
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
    public CollectionObjDef setupCurrentColObjDef(final List<CatalogSeries> catalogSeriesList, final boolean alwaysAsk)
    {
        if (catalogSeriesList == null || catalogSeriesList.size() == 0)
        {
            return null;
        }
        
        // OK, at this point the user selected more than one CatalogSeries and each of the CatalogSeries
        // Could have more than one ColObjDef, so the User needs to select a "default ColObjDef. 
        AppPreferences   appPrefs          = AppPreferences.getRemote();
        String           recentColObjDefId = mkUserDBPrefName("recent_colobjdef_id");
        Integer          colObjDefId       = appPrefs.getInt(recentColObjDefId, null);
        CollectionObjDef colObjDef         = null;
        

        boolean askForColObjDef   = true;
        if (!alwaysAsk)
        {
            // At this point if we have an ID then check to make sure it is an ID from one of the currently selected
            // CatalogSeries, and if not then ask for a new one
            if (colObjDefId != null)
            {
                for (CatalogSeries cs : catalogSeriesList)
                {
                    for (CollectionObjDef cod : cs.getCollectionObjDefItems())
                    {
                        if (cod.getCollectionObjDefId().equals(colObjDefId))
                        {
                            askForColObjDef = false;
                            colObjDef = cod;
                            break;
                        }
                    }
                    if (!askForColObjDef)
                    {
                        break;
                    }
                }
            }
        }
        
        // Either one wasn't selected before OR the ID we got back wasn't in any of the CatalogSeries
        // So we need to ask for a new one
        if (askForColObjDef || alwaysAsk)
        {
            class CatSeriesColObjDefItem
            {
                protected String name;
                protected CollectionObjDef collectionObjDef;
                
                public CatSeriesColObjDefItem(CatalogSeries catSeries, CollectionObjDef colObjDefArg)
                {
                    name = catSeries.getSeriesName() + " / " + colObjDefArg.getName();
                    this.collectionObjDef = colObjDefArg;
                }
                
                public String toString()
                {
                    return name;
                }
                
                public CollectionObjDef getColObjDef()
                {
                    return collectionObjDef;
                }
                
            }
            
            List<CatSeriesColObjDefItem> list = new ArrayList<CatSeriesColObjDefItem>(10);
            for (CatalogSeries cs : catalogSeriesList)
            {
                for (CollectionObjDef cod : cs.getCollectionObjDefItems())
                {
                    list.add(new CatSeriesColObjDefItem(cs, cod));
                }
            }

            // TODO Need to add a Help Btn to this Dialog
            ChooseFromListDlg<CatSeriesColObjDefItem> dlg = 
                new ChooseFromListDlg<CatSeriesColObjDefItem>((Frame)UICacheManager.get(UICacheManager.FRAME),
                                                              getResourceString("ChooseCatSeriesColObjDef"), 
                                                              list, 
                                                              false);
            dlg.setModal(true);

            UIHelper.centerAndShow(dlg);
            if (!dlg.isCancelled() && dlg.getSelectedObject() != null) // shouldn't need to check for null (but just in case)
            {
                CatSeriesColObjDefItem item = dlg.getSelectedObject();
                colObjDef = item.getColObjDef();
             }
        }

        if (colObjDef != null)
        {
            CollectionObjDef.setCurrentCollectionObjDef(colObjDef);
            appPrefs.putLong(recentColObjDefId, colObjDef.getCollectionObjDefId());

        }
        return colObjDef;
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
     * Finds a AppResourceDefault from an "object" list where it matches the user, CatSeries and ColObjdef.
     * @param appResDefList the list to search
     * @param userArg the Specify user
     * @param catSeries the CatalogSeries
     * @param colObjDef the CollectionObjDef
     * @return the AppResourceDefault object or null
     */
    protected AppResourceDefault find(final List             appResDefList,
                                      final SpecifyUser      userArg,
                                      final CatalogSeries    catSeries,
                                      final CollectionObjDef colObjDef)
    {
        log.debug("finding AppResourceDefault");
        for (Object obj : appResDefList)
        {
            AppResourceDefault ard = (AppResourceDefault)obj;

            SpecifyUser      spUser = ard.getSpecifyUser();
            CatalogSeries    cs     = ard.getCatalogSeries();
            CollectionObjDef cod    = ard.getCollectionObjDef();

            if (spUser != null && spUser.getSpecifyUserId() == userArg.getSpecifyUserId() &&
                cs != null && cs.getCatalogSeriesId() == catSeries.getCatalogSeriesId() &&
                cod != null && cod.getCollectionObjDefId() == colObjDef.getCollectionObjDefId())
            {
                return ard;
            }
        }
        return null;
    }

    /**
     * Creates an AppResourceDefault object from a directory (note the Id will be null).
     * @param dir the directory in question)
     * @return a new AppResourceDefault object
     */
    protected AppResourceDefault createAppResourceDefFromDir(final File dir)
    {
        log.debug("Creating AppResourceDef from Dir");
        AppResourceDefault appResDef = new AppResourceDefault();
        appResDef.initialize();

        ViewSetMgr viewSetMgr = new ViewSetMgr(dir);
        for (ViewSet vs : viewSetMgr.getViewSets())
        {
            ViewSetObj vso = new ViewSetObj();
            vso.initialize();

            // Set up File Name to load the ViewSet
            vso.setFileName(dir.getAbsoluteFile() + File.separator + vs.getFileName());

            if (false)
            {
                String dataStr = vso.getDataAsString(); // causes the file to be loaded and returned into this string

                // now clear the file names o it thinks it is a database object,
                // and not created from disk
                vso.setFileName(null);

                // Now set the Blob from the string
                vso.setDataAsString(dataStr);
            }

            vso.setLevel((short)0);
            vso.setName(vs.getFileName());

            vso.getAppResourceDefaults().add(appResDef);
            appResDef.getViewSets().add(vso);

        }

        AppResourceMgr appResMgr = new AppResourceMgr(dir);
        for (AppResource appRes : appResMgr.getAppResources())
        {
            appRes.getAppResourceDefaults().add(appResDef);
            appResDef.getAppResources().add(appRes);
        }
        return appResDef;
    }

    /**
     * For debug purposes, display the contents of a AppResourceDefault
     * @param appResDef AppResourceDefault
     * @return string of info
     */
    protected String getAppResDefAsString(final AppResourceDefault appResDef)
    {
        SpecifyUser      spUser = appResDef.getSpecifyUser();
        CatalogSeries    cs   = appResDef.getCatalogSeries();
        CollectionObjDef cod  = appResDef.getCollectionObjDef();

        StringBuilder strBuf = new StringBuilder();
        strBuf.append("CS["+(cs != null ? cs.getSeriesName() : "null") + "]");
        strBuf.append(" SU["+(spUser != null ? spUser.getName() : "null") + "]");
        strBuf.append(" COD["+(cod != null ? cod.getName() : "null") + "]");
        strBuf.append(" DSP["+appResDef.getDisciplineType() + "]");
        strBuf.append(" UTYP["+appResDef.getUserType() + "]");
        log.debug("AppResDefAsString - "  + strBuf.toString());
        return strBuf.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#setContext(java.lang.String, java.lang.String, boolean)
     */
    public CONTEXT_STATUS setContext(final String databaseName,
                                     final String userName,
                                     final boolean startingOver)
    {
        log.debug("setting context - databaseName: " + databaseName + " userName: " + userName);
        this.databaseName = databaseName;
        this.userName     = userName;
        
        // This is where we will read it in from the Database
        // but for now we don't need to do that.
        //
        // We need to search for User, CatalogSeries, CollectionObjDef and UserType
        // Then

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();

        List list = session.getDataList(SpecifyUser.class, "name", userName);
        if (list.size() == 1)
        {
            user = (SpecifyUser)list.get(0);
            user.getAgent(); // makes sure the Agent is not lazy loaded
            SpecifyUser.setCurrentUser(user);

        } else
        {
            throw new RuntimeException("The user ["+userName+"] could  not be located as a Specify user.");
        }

        // First we start by getting all the CatalogSeries that the User want to
        // work with for this "Context" then we need to go get all the Default View and
        // additional XML Resources.
        
        // Ask the User to choose which CatalogSeries they will be working with
        CatalogSeries.getCurrentCatalogSeries().clear();
        
        List<CatalogSeries> catalogSeries = setupCurrentCatalogSeries(session, user, startingOver);
        if (catalogSeries == null)
        {
            //catalogSeries = new ArrayList<CatalogSeries>();
            //catalogSeries.addAll(CatalogSeries.getCurrentCatalogSeries());
            // Return false but on't mess with anything that has been set up so far
            currentStatus  = currentStatus == CONTEXT_STATUS.Initial ? CONTEXT_STATUS.Error : CONTEXT_STATUS.Ignore;
            return currentStatus;
        }
        Hashtable<String, String> disciplineHash = new Hashtable<String, String>();
        
        String userType = user.getUserType();
        log.debug("User["+user.getName()+"] Type["+userType+"]");

        userType = StringUtils.replace(userType, " ", "").toLowerCase();
        log.debug("Def Type["+userType+"]");
        
        appResourceList.clear();
        viewSetHash.clear();

       List appResDefList = session.getDataList( "From AppResourceDefault where specifyUserId = "+user.getSpecifyUserId());


//       Hashtable<String, AppResource> appResHash = new Hashtable<String, AppResource>();
       
       if (catalogSeries.size() == 0)
       {
            //throw new RuntimeException("What does it mean if the current user is not assigned to any CatalogSeries?");
            
            // Accession / Registrar / Director may not be assigned to any Catalog Series
            // Or for a stand alone Accessions Database there may not be any 

            CatalogSeries.getCurrentCatalogSeries().clear();
            CollectionObjDef.setCurrentCollectionObjDef(null);
            
            disciplineHash.put(userType, userType);
            
        } else
        {
    
            // Set up the CollectionObjectDef for the most common case of one CatalogSeries with one CollecionObjDef
            CollectionObjDef.setCurrentCollectionObjDef(null);
            if (catalogSeries.size() == 1)
            {
                log.debug("Choosing CatSeries["+catalogSeries.get(0).getSeriesName()+"]");
                if (catalogSeries.get(0).getCollectionObjDefItems().size() == 1)
                {
                    CollectionObjDef.setCurrentCollectionObjDef(catalogSeries.get(0).getCollectionObjDefItems().iterator().next());
                }
                
            } else
            {
                // OK, at this point the user selected more than one CatalogSeries and each of the CatalogSeries
                // Could have more than one ColObjDef, so the User needs to select a "default ColObjDef. 
                //
                // Also note that this set the "default" CollectionObjDef
                setupCurrentColObjDef(catalogSeries, startingOver);
            }
            
            log.debug("Adding AppResourceDefs from Catalog Series and ColObjDefs");
            for (CatalogSeries cs : catalogSeries)
            {
                log.debug("CatSeries["+cs.getSeriesName()+"]");
                for (CollectionObjDef cod : cs.getCollectionObjDefItems())
                {
                    log.debug("  ColObjDef["+cod.getName()+"]");
                    
                    disciplineHash.put(cod.getDiscipline(), cod.getDiscipline());
                    
                    AppResourceDefault appResourceDef = find(appResDefList, user, cs, cod);
                    if (appResourceDef != null)
                    {
                        log.debug("Adding1 "+getAppResDefAsString(appResourceDef));
                        appResourceList.add(appResourceDef);
                        /*for (AppResource ap : appResourceDef.getPersistedAppResources())
                        {
                            appResHash.put(ap.getName(), ap);
                        }*/
                        
                    }
                }
            }
        }

        // Add Backstop for Discipline and User Type
        for (String discipline : disciplineHash.keySet())
        {
            log.debug("****** Trying add Backstop for ["+discipline+"]["+userType+"]");

            File dir = XMLHelper.getConfigDir(discipline + File.separator + userType);
            if (dir.exists())
            {
                AppResourceDefault appResDef = createAppResourceDefFromDir(dir);
                appResDef.setDisciplineType(discipline);
                appResDef.setUserType(userType);
                appResDef.setSpecifyUser(user);//added to fix not-null constraints
                appResDef.setCatalogSeries(CatalogSeries.getCurrentCatalogSeries().get(0));
                appResDef.setCollectionObjDef(CollectionObjDef.getCurrentCollectionObjDef());
                
                log.debug("Adding2 "+getAppResDefAsString(appResDef));
                appResourceList.add(appResDef);
                
            } else
            {
                log.debug("***** Couldn't add Backstop for ["+discipline+"]["+userType+"] ["+dir.getAbsolutePath()+"]");
            }
        }

        // Add Backstop for just the Discipline
        for (String discipline : disciplineHash.keySet())
        {
            log.debug("***** Trying add Backstop for ["+discipline+"]");
            File dir = XMLHelper.getConfigDir(discipline);
            if (dir.exists())
            {
                AppResourceDefault appResDef = createAppResourceDefFromDir(dir);
                appResDef.setDisciplineType(discipline);
                appResDef.setSpecifyUser(user);
                
                appResDef.setCatalogSeries(CatalogSeries.getCurrentCatalogSeries().size() > 0 ? CatalogSeries.getCurrentCatalogSeries().get(0) : null);
                appResDef.setCollectionObjDef(CollectionObjDef.getCurrentCollectionObjDef());

                log.debug("Adding3 "+getAppResDefAsString(appResDef));
                appResourceList.add(appResDef);
                
            } else
            {
                log.debug("***** Couldn't add Backstop for ["+discipline+"] ["+dir.getAbsolutePath()+"]");
            }
        }

        backStopViewSetMgr = new ViewSetMgr(XMLHelper.getConfigDir("backstop"));
        backStopAppResMgr  = new AppResourceMgr(XMLHelper.getConfigDir("backstop"));
        
        currentStatus = CONTEXT_STATUS.OK;
        
        session.close();
        
        return currentStatus;
    }

    /**
     * Returns a list of ViewSets from a AppResourceDefault, The ViewSets are created from the ViewSetObj.
     * @param appResDef the AppResourceDefault
     * @return list of ViewSet objects
     */
    protected List<ViewSet> getViewSetList(final AppResourceDefault appResDef)
    {
        log.debug("Looking up["+appResDef.getUniqueIdentifer()+"]["+appResDef.getVerboseUniqueIdentifer()+"]");
        
        Boolean reloadViews = AppPreferences.getLocalPrefs().getBoolean("reload_views", false);
        if (reloadViews)
        {
            viewSetHash.clear();
        }
        
        List<ViewSet> viewSetList = viewSetHash.get(appResDef.getUniqueIdentifer());
        if (viewSetList == null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            if (appResDef.getAppResourceDefaultId() != null)
            {
                session.attach(appResDef);
            }
            viewSetList = new Vector<ViewSet>();
            for (ViewSetObj vso : appResDef.getViewSets())
            {
                try
                {
                    Element root = XMLHelper.readStrToDOM4J(vso.getDataAsString());
                    viewSetList.add(new ViewSet(root));

                } catch (Exception ex)
                {
                    log.error(vso.getName());
                    log.error(ex);
                    throw new RuntimeException(ex);
                }
            }
            viewSetHash.put(appResDef.getUniqueIdentifer(), viewSetList);
            session.close();
        }
        return viewSetList;
    }

    /**
     * Finds a View by name using a CollectionObjDef.
     * @param viewName the name of the view
     * @param colObjDef the CollectionObjDef
     * @return the view or null
     */
    public View getView(final String viewName, final CollectionObjDef colObjDef)
    {
        log.debug("Looking Up View ["+viewName+"] colObjDef["+(colObjDef != null ? colObjDef.getName() : "null")+"]");
        
        boolean fndColObjDef = false;
        for (AppResourceDefault appResDef : appResourceList)
        {
            log.debug("Looking["+(appResDef.getCollectionObjDef() != null ? appResDef.getCollectionObjDef().getName() : "null")+"]["+(colObjDef != null ? colObjDef.getName() : "null")+"]");
            
            if (appResDef.getCollectionObjDef() != null && appResDef.getCollectionObjDef() == colObjDef)
            {
                fndColObjDef = true;
                for (ViewSet vs : getViewSetList(appResDef))
                {
                    View view = vs.getView(viewName);
                    if (view != null)
                    {
                        return view;
                    }
                }
            }
        }

        // This is searching the BackStops by Discipline and User Type
        // which were created dynamically
        if (!fndColObjDef)
        {
            String disciplineName = colObjDef != null ? colObjDef.getDiscipline() : null;
            String userType       = SpecifyUser.getCurrentUser().getUserType();
            userType = StringUtils.replace(userType, " ", "").toLowerCase();

            // Search Using the colObjectDef's discipline
            for (AppResourceDefault appResDef : appResourceList)
            {
                String dType = appResDef.getDisciplineType();
                String uType = appResDef.getUserType();

                log.debug("appResDef's DisciplineType["+dType+"] appResDef's UserType["+uType+"] User's userType["+userType+"]");
                
                if ((dType != null && disciplineName != null && dType.equals(disciplineName) || 
                    (dType == null || disciplineName == null)) && (uType == null || uType.equals(userType)) ||
                    (userType != null && dType != null && dType.equals(userType)))
                {
                    for (ViewSet vs : getViewSetList(appResDef))
                    {
                        View view = vs.getView(viewName);
                        if (view != null)
                        {
                            return view;
                        }
                    }
                }
            }
        }
        
        for (ViewSet vs : backStopViewSetMgr.getViewSets())
        {
            View view = backStopViewSetMgr.getView(vs.getName(), viewName);
            if (view != null)
            {
                return view;
            }
        }
        throw new RuntimeException("Can't find View ["+viewName+"] colObjDef["+(colObjDef != null ? colObjDef.getName() : "null")+"] ["+fndColObjDef+"]");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultIFace#getView(java.lang.String, java.lang.String)
     */
    public View getView(final String viewSetName, final String viewName)
    {

        if (StringUtils.isEmpty(viewName))
        {
            throw new RuntimeException("Sorry the View Name cannot be empty.");
        }

        if (StringUtils.isEmpty(viewSetName))
        {
            throw new RuntimeException("Sorry not empty or null ViewSetNames use the call with CollectionObjDef instead.");
        }

        for (AppResourceDefault appResDef : appResourceList)
        {
            log.debug("getView "+getAppResDefAsString(appResDef)+"  ["+appResDef.getUniqueIdentifer()+"]");

            for (ViewSet vs : getViewSetList(appResDef))
            {
                log.debug("VS  ["+vs.getName()+"]["+viewSetName+"]");

                if (vs.getName().equals(viewSetName))
                {
                    View view = vs.getView(viewName);
                    if (view != null)
                    {
                        return view;
                    }
                }
            }
        }

        return backStopViewSetMgr.getView(viewSetName, viewName);

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultIFace#getResource(java.lang.String)
     */
    public AppResourceIFace getResource(final String name)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            for (AppResourceDefault appResDef : appResourceList)
            {
                if (appResDef.getAppResourceDefaultId() != null)
                {
                    session.attach(appResDef);
                }
                
                for (AppResourceIFace appRes : appResDef.getAppResources())
                {
                    if (appRes.getName().equals(name))
                    {
                        return appRes;
                    }
                }
            }
        } catch (Exception ex)
        {
            log.error(ex);
            
        } finally 
        {
            session.close();
            session = null;

        }
        
        if (backStopAppResMgr == null)
        {
            throw new RuntimeException("The backStopAppResMgr is null which means somehow a call was made to this method before the backStopAppResMgr was initialized.");
        }
        return backStopAppResMgr.getAppResource(name);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultMgr#getResourceAsDOM(java.lang.String)
     */
    public Element getResourceAsDOM(final String name)
    {
        AppResourceIFace appResource = getResource(name);
        if (appResource != null && appResource instanceof AppResource)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            AppResource              appRes  = (AppResource)appResource;
            try
            {
                if (appRes.getAppResourceId() != null)
                {
                    session.attach(appRes);
                }
                
                if (appRes.getMimeType() != null && appRes.getMimeType().equals("text/xml"))
                {
                    try
                    {
                        return XMLHelper.readStrToDOM4J(appRes.getDataAsString());
    
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        throw new RuntimeException(ex);
                    }
                } else
                {
                    throw new RuntimeException("MimeType was not 'text/xml'");
                }
                
            } catch (Exception ex)
            {
                log.error(ex);
                
            } finally 
            {
                session.close();
                session = null;
            }
        } else
        {
            log.debug("Couldn't find ["+name+"]");
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultMgr#getResourceByMimeType(java.lang.String)
     */
    public List<AppResourceIFace> getResourceByMimeType(final String mimeType)
    {
        List<AppResourceIFace> list = new ArrayList<AppResourceIFace>();
        for (AppResourceDefault appResDef : appResourceList)
        {
            for (AppResourceIFace appRes : appResDef.getAppResources())
            {
                //log.debug("["+appRes.getMimeType()+"]["+mimeType+"]");
                if (appRes.getMimeType() != null && appRes.getMimeType().equals(mimeType))
                {
                    list.add(appRes);
                }
            }
        }
        return list;
    }



    //----------------------------------------------------------------
    //-- Inner Classes
    //----------------------------------------------------------------
    class AppResourceMgr
    {
        protected File locationDir;
        protected Hashtable<String, AppResource> appResources = null;

        public AppResourceMgr(final File file)
        {
            locationDir = file;
            appResources = new Hashtable<String, AppResource>();
            init(locationDir);

        }

        public AppResource getAppResource(final String name)
        {

            return appResources.get(name);
        }

        /**
         * @param appRes
         */
        public void addAppRes(final AppResource appRes)
        {
            appResources.put(appRes.getName(), appRes);
        }

        /**
         * Reads in the App Resource for a discipline
         */
        protected void init(final File file)
        {
            if (file.exists())
            {
                try
                {
                    Element root = XMLHelper.readFileToDOM4J(new FileInputStream(new File(file.getAbsoluteFile() + File.separator + "app_resources.xml")));
                    if (root != null)
                    {
                        for ( Iterator i = root.elementIterator( "file" ); i.hasNext(); )
                        {
                            Element fileElement = (Element) i.next();
                            String  name        = getAttr(fileElement, "name", null);
                            if (appResources.get(name) == null)
                            {
                                Integer level    = getAttr(fileElement, "level", 0);
                                String mimeType  = getAttr(fileElement, "mimetype", null);
                                String desc      = getAttr(fileElement, "description", null);
                                String fileName  = getAttr(fileElement, "file", null);
                                String metaData  = getAttr(fileElement, "metadata", null);

                                // these can go away once we validate the XML
                                if (level == null)
                                {
                                    throw new RuntimeException("AppResource level cannot be null!");
                                }
                                if (StringUtils.isEmpty(mimeType))
                                {
                                    throw new RuntimeException("AppResource mimeType cannot be null!");
                                }
                                if (StringUtils.isEmpty(fileName))
                                {
                                    throw new RuntimeException("AppResource file cannot be null!");
                                }

                                File resFile = new File(file.getAbsoluteFile() + File.separator + fileName);
                                if (!resFile.exists())
                                {
                                    //throw new RuntimeException("AppResource file cannot be found at["+resFile.getAbsolutePath()+"]");
                                    log.error("AppResource file cannot be found at["+resFile.getAbsolutePath()+"]");
                                }

                                AppResource appRes = new AppResource();
                                appRes.initialize();
                                appRes.setLevel(level.shortValue());
                                appRes.setName(name);
                                appRes.setMimeType(mimeType);
                                appRes.setDescription(desc);
                                appRes.setMetaData(metaData);
                                appRes.setSpecifyUser(user);//added to fix not-null constraint issue

                                appRes.setFileName(resFile.getAbsolutePath());

                                log.debug("Adding ["+name+"] ["+resFile.getAbsolutePath()+"]");
                                
                                appResources.put(name, appRes);

                            } else
                            {
                                log.error("AppResource Name["+name+"] is in use.");
                            }
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
        }

        /**
         * Returns a list of all the AppResources
         * @return a list of all the AppResources
         */
        public List<AppResource> getAppResources()
        {
            return Collections.list(appResources.elements());
        }

    }

    
    
    /**
     * Returns a Default Object from Prefs if there is one.
     */
    @SuppressWarnings("cast")
    public PickListItemIFace getDefaultPickListItem(final String pickListName, final String title, final boolean ask)
    {
        PickListItemIFace dObj        = null;
        CatalogSeries     catSeries   = CatalogSeries.getCurrentCatalogSeries().get(0);
        String            prefName    = (catSeries != null ? catSeries.getIdentityTitle() : "") + pickListName + "_DefaultId";
        AppPreferences    appPrefs    = AppPreferences.getRemote();
        String            idStr       = appPrefs.get(prefName, null);
        
        if (StringUtils.isNotEmpty(idStr))
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            dObj = (PickListItemIFace)session.get(PickListItem.class, Long.valueOf(idStr));
            session.close();
            
            if (dObj != null)
            {
                return dObj;
            }            
        }
            
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            PickList pickList = (PickList)session.getData(PickList.class, "name", pickListName, DataProviderSessionIFace.CompareType.Equals);
            if (pickList != null)
            {
                Vector<PickListItemIFace> list = new Vector<PickListItemIFace>();
                list.addAll(pickList.getItems());
                ChooseFromListDlg<PickListItemIFace> dlg = new ChooseFromListDlg<PickListItemIFace>(null, 
                        UICacheManager.getLocalizedMessage("CHOOSE_DEFAULT_OBJECT", title), list);
                dlg.setModal(true);
                dlg.setVisible(true);
                if (!dlg.isCancelled())
                {
                    appPrefs.put(prefName, dlg.getSelectedObject().getId().toString());
                    return dlg.getSelectedObject();
                }
            }
            throw new RuntimeException("PickList name["+pickListName+"] doesn't exist.");
            
        } catch (Exception ex)
        {
            log.error(ex);
            
        } finally 
        {
            session.close();
        }
        return dObj;
    }

    
    
    /**
     * Returns a Default Object from Prefs if there is one.
     */
    @SuppressWarnings("cast")
    public FormDataObjIFace getDefaultObject(final Class<?> classObj, 
                                             final String prefPrefix,
                                             final String title,
                                             final boolean ask, 
                                             boolean useAllItems)
    {
        CatalogSeries    catSeries   = CatalogSeries.getCurrentCatalogSeries().get(0);
        FormDataObjIFace dObj        = null;
        String           prefName    = (catSeries != null ? catSeries.getIdentityTitle() : "") + prefPrefix + "_DefaultId";
        AppPreferences   appPrefs    = AppPreferences.getRemote();
        String           idStr       = appPrefs.get(prefName, null);
        if (StringUtils.isEmpty(idStr) && ask)
        {
            if (useAllItems)
            {
                class Item {
                    public FormDataObjIFace data;
                    public Item(FormDataObjIFace d) { data = d; }
                    public String toString() { return data.getIdentityTitle(); }
                }
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                List<Item> items = new Vector<Item>();
                for (Object o : session.getDataList(classObj))
                {
                    items.add(new Item((FormDataObjIFace)o));
                }
                session.close();
                
                
                ChooseFromListDlg<Item> dlg = new ChooseFromListDlg<Item>(null, title, items);
                dlg.setModal(true);
                dlg.setVisible(true);
                if (!dlg.isCancelled())
                {
                    dObj = (FormDataObjIFace)dlg.getSelectedObject().data;
                    appPrefs.put(prefName, dObj.getId().toString());
                    return dObj;
                }
                
            } else
            {
                try
                {
                    ViewBasedSearchDialogIFace dlg = UICacheManager.getViewbasedFactory().createSearchDialog(null, classObj.getSimpleName()+"Search");
                    if (dlg != null)
                    {
                        dlg.setTitle(title);
                        dlg.getDialog().setVisible(true);
                        if (!dlg.isCancelled())
                        {
                            dObj = (FormDataObjIFace)dlg.getSelectedObject();
                            appPrefs.put(prefName, dObj.getId().toString());
                            return dObj;
                        }
                    }
                } catch (Exception ex)
                {
                    // it's ok 
                    // we get when it can't find the search dialog
                }
            }
        } else
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            dObj = (FormDataObjIFace)session.get(classObj, Long.valueOf(idStr));
            session.close();
        }
        return dObj;
    }
    
}

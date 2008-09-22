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
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getViewbasedFactory;
import static edu.ku.brc.ui.UIRegistry.showLocalizedError;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.ViewSetMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewLoader;
import edu.ku.brc.af.ui.forms.persist.ViewSet;
import edu.ku.brc.af.ui.forms.persist.ViewSetIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceData;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpViewSetObj;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.prefs.FormattingPrefsPanel;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UnhandledExceptionDialog;
import edu.ku.brc.util.Pair;

/**
 * This class provides the current context of the Specify application. The context consists of the following:<br>
 * <ol>
 * <li>The User Name
 * <li>The Database Name (database connection)
 * <li>The Specify User Object
 * <li>The Collection
 * <li>The Discipline
 * <li>The DisciplineType Name
 * </ol>
 * <p>The SpecifyAppResourceDefaultMgr will place data in a <i>username</i>/<i>databaseName</i> directory in the "application data" directory of the user.
 * On Windows this is <code>\Documents and Settings\&lt;User Name&gt;\Application Data\Specify</code>.
 * On Unix platforms it is <code>/<i>user home</i>/.Specify</code> (Note: the app data dir is created by UIRegistry)</p>
 * <p>
 * The ViewSetMgrManager needs to load the "backstop" ViewSetMgr and the "user" ViewSetMgr in order for the application to work correctly.
 * So this class uses the "disciplineType name" to initialize the APPDATA dir with the appropriate data, which includes a "standard" set of
 * Views for that disciplineType. The APPDATA dir is really the "working space" of the application for a particular username/database.
 * </p>
 *
 * @code_status Complete
 *
 * @author rods
 */
public class SpecifyAppContextMgr extends AppContextMgr
{
    private static final Logger  log = Logger.getLogger(SpecifyAppContextMgr.class);
    
    // Virtual Directory Levels
    public static final String PERSONALDIR   = "Personal"; //$NON-NLS-1$
    public static final String USERTYPEDIR   = "UserType"; //$NON-NLS-1$
    public static final String COLLECTIONDIR = "Collection"; //$NON-NLS-1$
    public static final String DISCPLINEDIR  = "Discipline"; //$NON-NLS-1$
    public static final String COMMONDIR     = "Common"; //$NON-NLS-1$
    public static final String BACKSTOPDIR   = "BackStop"; //$NON-NLS-1$

    protected List<SpAppResourceDir>                spAppResourceList = new ArrayList<SpAppResourceDir>();
    protected Hashtable<String, SpAppResourceDir>   spAppResourceHash = new Hashtable<String, SpAppResourceDir>();
    protected Hashtable<String, List<ViewSetIFace>> viewSetHash       = new Hashtable<String, List<ViewSetIFace>>();
    
    // This hashes the Pair "Name of the ViewSetMgr" and the "Directory" where it is loaded from
    // which enables it to easily reload anyone of them from disk when the ViewSetMgr is reverted.
    // The key to the hash is the name of the "Virtual Directory Level" found above.
    protected Hashtable<String, Pair<String, File>> viewSetMgrHash    = new Hashtable<String, Pair<String, File>>();
    

    protected String         databaseName          = null;
    protected String         userName              = null;
    protected SpecifyUser    user                  = null;

    protected Agent          currentUserAgent      = null;

    protected boolean        debug                 = false;
    protected long           lastLoadTime          = 0;
    protected long           lastLoadTimeBS        = 0;
    protected UnhandledExceptionDialog dlg         = null;
    
    protected DataProviderSessionIFace globalSession    = null;
    protected int                      openSessionCount = 0;
    
    /**
     * Singleton Constructor.
     */
    public SpecifyAppContextMgr()
    {
        // no-op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getInstance()
     */
    public static SpecifyAppContextMgr getInstance()
    {
        return (SpecifyAppContextMgr)AppContextMgr.getInstance();
    }

    /**
     * @return the array of virtual directory names.
     */
    public static String[] getVirtualDirNames()
    {
        //                     0            1             2             3             4           5
        String[] levels = {PERSONALDIR, USERTYPEDIR, COLLECTIONDIR, DISCPLINEDIR, COMMONDIR, BACKSTOPDIR};
        return levels;
    }

    /**
     * Returns the String name of the level given a level index.
     * @param index the index of the level
     * @return the name of the level
     */
    public static String getVirtualDirName(final int index)
    {
        String[] levels = getVirtualDirNames();
        return levels[index];
    }

    /**
     * @param virtualDirName the name of the virtual Directory
     * @return the index of the virtual directory name
     */
    public static int getVirtualDirIndex(final String virtualDirName)
    {
        String[] levels = getVirtualDirNames();
        for (int i=0;i<levels.length;i++)
        {
            if (virtualDirName.equals(levels[i]))
            {
                return i;
            }
        }
        throw new RuntimeException("Virtual Directory name ["+virtualDirName+"] isn't found."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return the viewSetHash
     */
    public Hashtable<String, List<ViewSetIFace>> getViewSetHash()
    {
        return viewSetHash;
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
     * Opens a session global to this object for loading
     * @return the session
     */
    protected DataProviderSessionIFace openSession()
    {
        if (globalSession == null)
        {
            try
            {
                globalSession = DataProviderFactory.getInstance().createSession();
                
            } catch (Exception ex)
            {
                return null;
            }
        }
        openSessionCount++;
        return globalSession;
    }
    
    /**
     * Closes the global session.
     */
    protected void closeSession()
    {
        if (globalSession != null)
        {
            openSessionCount--;
            if (openSessionCount == 0)
            {
                globalSession.close();
                globalSession = null;
                
            } else if (openSessionCount < 0)
            {
                log.error("Open Session Count just went negitive!"); //$NON-NLS-1$
                
            }
        } else
        {
            log.error("There is no existing open session."); //$NON-NLS-1$
        }
    }
    
    /**
     * Returns the number of Collection that this user is connected to.
     * @return the number of Collection that this user is connected to.
     */
    public int getNumOfCollectionsForUser()
    {
        String sqlStr = "select count(cos) From Agent as spua Inner Join spua.specifyUser spu Inner Join spua.disciplines as dsp Inner Join dsp.collections as cos where spu.specifyUserId = "+user.getSpecifyUserId(); //$NON-NLS-1$
        
        DataProviderSessionIFace session = null;
        try
        {
            session = openSession();
            Object  result     = session.getData(sqlStr);
            return result != null ? (Integer)result : 0;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            closeSession();
        }
        return 0;
    }
    
    /**
     * @param sessionArg
     * @return
     */
    public List<Integer> getCollectionIdList(final DataProviderSessionIFace sessionArg)
    {
        Vector<Integer> list   = new Vector<Integer>();
        SpecifyUser     spUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        if (spUser != null)
        {
            sessionArg.attach(spUser);
            for (Agent agent : spUser.getAgents())
            {
                for (Discipline discipline : agent.getDisciplines())
                {
                    for (Collection collection : discipline.getCollections())
                    {
                        list.add(collection.getCollectionId().intValue());
                    }
                }
            }
        } else
        {
            log.error("SpecifyUser was null!"); //$NON-NLS-1$
        }
        return list;
    }
    
    /**
     * @param sessionArg
     * @param userArg
     * @return
     */
    protected Collection getLastUSedCollection(final DataProviderSessionIFace sessionArg, 
                                               final SpecifyUser userArg)
    {
        try
        {
            final String prefName = mkUserDBPrefName("recent_collection_id"); //$NON-NLS-1$
            
            // First get the Collections the User has access to.
            Hashtable<String, Collection> collectionHash = new Hashtable<String, Collection>();
            String sqlStr = "SELECT cs From Discipline as ct Inner Join ct.agents cta Inner Join cta.specifyUser as user Inner Join ct.collections as cs where user.specifyUserId = "+userArg.getSpecifyUserId(); //$NON-NLS-1$
            for (Object obj : sessionArg.getDataList(sqlStr))
            {
                Collection cs = (Collection)obj; 
                cs.getDiscipline();// force load of Discipline
                collectionHash.put(cs.getCollectionName(), cs);
            }
    
            Collection collection = null;
            
            AppPreferences appPrefs  = AppPreferences.getRemote();
            String         recentIds = appPrefs.get(prefName, null);
            if (StringUtils.isNotEmpty(recentIds))
            {
                List<?> list = sessionArg.getDataList("FROM Collection WHERE collectionId = " + recentIds); //$NON-NLS-1$
                if (list.size() == 1)
                {

                    collection = (Collection)list.get(0);

                }
                else
                {
                    log.debug("could NOT find recent ids"); //$NON-NLS-1$
                }
            }
            
            if (collection != null && collectionHash.get(collection.getCollectionName()) == null)
            {
                return null;
            }
            
            return collection;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            showLocalizedError(ex.toString()); // Yes, I know it isn't localized.
        }
        return null;
    }
    
    /**
     * Sets up the "current" Collection by first checking prefs for the most recent primary key,
     * if it can't get it then it asks the user to select one. (Note: if there is only one it automatically chooses it)
     * @param sessionArg a session
     * @param userArg the user object of the current object
     * @param alwaysAsk indicates the User should always be asked which Collection to use
     * @return the current Collection or null
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    protected Collection setupCurrentCollection(final SpecifyUser userArg,
                                                final boolean startingOver)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            SpecifyUser spUser = session.getData(SpecifyUser.class, "id", userArg.getId(), DataProviderSessionIFace.CompareType.Equals); //$NON-NLS-1$
            
            final String prefName = mkUserDBPrefName("recent_collection_id"); //$NON-NLS-1$
            
            // First get the Collections the User has access to.
            Hashtable<String, Collection> collectionHash = new Hashtable<String, Collection>();
            String sqlStr = "SELECT cs From Discipline as ct Inner Join ct.agents cta Inner Join cta.specifyUser as user Inner Join ct.collections as cs where user.specifyUserId = "+spUser.getSpecifyUserId(); //$NON-NLS-1$
            for (Object obj : session.getDataList(sqlStr))
            {
                Collection cs = (Collection)obj; 
                cs.forceLoad();
                
                cs.getDiscipline();// force load of Discipline
                cs.getDiscipline().getAgents(); // force load of agents
                collectionHash.put(cs.getCollectionName(), cs);
            }
    
            Collection collection = null;
            
            AppPreferences appPrefs  = AppPreferences.getRemote();
            String         recentIds = appPrefs.get(prefName, null);
            if (StringUtils.isNotEmpty(recentIds))
            {
                List<?> list = session.getDataList("FROM Collection WHERE collectionId = " + recentIds); //$NON-NLS-1$
                if (list.size() == 1)
                {

                    collection = (Collection)list.get(0);
                    collection.forceLoad();
                }
                else
                {
                    log.debug("could NOT find recent ids"); //$NON-NLS-1$
                }
            }
            
            if (collection != null && collectionHash.get(collection.getCollectionName()) == null)
            {

                collection = null;
            }
            
            if (collection == null || startingOver)
            {
                if (collectionHash.size() == 1)
                {
                    collection = collectionHash.elements().nextElement();
                    collection.forceLoad();
    
                } else if (collectionHash.size() > 0)
                {
                    List<Collection> list = new Vector<Collection>();
                    list.addAll(collectionHash.values());
                    Collections.sort(list);
                    
                    int selectColInx = -1;
                    if (collection != null)
                    {
                        int i = 0;
                        for (Collection c : list)
                        {
                            if (c.getId().intValue() == collection.getId().intValue())
                            {
                                selectColInx = i;
                                break;
                            }
                            i++;
                        }
                    } else
                    {
                        log.error("Collection was null!"); //$NON-NLS-1$
                    }
    
                    
                    session.close();
                    session = null;

                    ChooseCollectionDlg colDlg = null;
                    do {
                        colDlg = new ChooseCollectionDlg(list);
                        colDlg.setSelectedIndex(selectColInx);
                        colDlg.createUI();
                        colDlg.pack();
                        Dimension size = colDlg.getSize();
                        size.width  = Math.max(size.width, 300);
                        if (size.height < 150)
                        {
                            size.height += 100;
                        }
                        colDlg.setSize(size);
                        
                        UIHelper.centerWindow(colDlg);
                        colDlg.setVisible(true);
                        
                    } while (colDlg.getSelectedObject() == null || colDlg.isCancelled());

                    
                    collection = colDlg.getSelectedObject();
                    
                    session = DataProviderFactory.getInstance().createSession();
                    session.attach(collection);
                    session.attach(spUser);
                    
                    collection.forceLoad();
                    
                } else
                {
                    // Accession / Registrar / Director may not be assigned to any Collection
                    // Or for a stand alone Accessions Database there may not be any 
                }
    
                if (collection != null)
                {
                    appPrefs.put(prefName, (Long.toString(collection.getCollectionId())));
                }
            }
            
            AppContextMgr.getInstance().setClassObject(Collection.class, collection);
            // XXX Collection.setCurrentCollectionIds(getCollectionIdList(sessionArg));
            
            String colObjStr = "CollectionObject"; //$NON-NLS-1$
            String iconName = AppPreferences.getRemote().get(FormattingPrefsPanel.getDisciplineImageName(), colObjStr);
            if (StringUtils.isEmpty(iconName) || iconName.equals(colObjStr))
            {
                iconName = "colobj_backstop"; //$NON-NLS-1$
            }
            
            IconManager.aliasImages(iconName,                  // Source
                                    colObjStr);                // Dest //$NON-NLS-1$

            IconManager.aliasImages(iconName,                  // Source
                                    colObjStr.toLowerCase());  // Dest //$NON-NLS-1$
            
            if (collection != null)
            {
                
                Discipline discipline = collection.getDiscipline();
                session.attach(discipline);
                
                Institution institution = discipline.getDivision().getInstitution();
                AppContextMgr.getInstance().setClassObject(Institution.class, institution);
                
                if (discipline != null)
                {
                    Agent.setUserAgent(spUser, discipline.getAgents());
                    
                    AppContextMgr am = AppContextMgr.getInstance();
                	am.setClassObject(TaxonTreeDef.class, discipline.getTaxonTreeDef());
                    am.setClassObject(GeologicTimePeriodTreeDef.class, discipline.getGeologicTimePeriodTreeDef());
                    am.setClassObject(StorageTreeDef.class, institution.getStorageTreeDef());
                    am.setClassObject(LithoStratTreeDef.class, discipline.getLithoStratTreeDef());
                    am.setClassObject(GeographyTreeDef.class, discipline.getGeographyTreeDef());
                }
            } else
            {
                showLocalizedError("SpecifyAppContextMgr.COL_WAS_NULL"); //$NON-NLS-1$
            }
            
            return collection;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            showLocalizedError(ex.toString()); // Yes, I know it isn't localized.
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        //System.exit(0);
        return null;
        
    }

    /**
     * Helper function to create a user and database centric pref name
     * @param prefName the pref names
     * @return  a user and database centric pref name
     */
    protected String mkUserDBPrefName(final String prefName)
    {
        return prefName + "." + userName+ "." + databaseName; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Finds a AppResourceDefault from an "object" list where it matches the user, CatSeries and ColObjdef.
     * @param appResDefList the list to search
     * @param userArg the Specify user
     * @param collection the Collection
     * @param discipline the Discipline
     * @return the AppResourceDefault object or null
     */
    protected SpAppResourceDir find(final List<?>        appResDefList,
                                    final SpecifyUser    userArg,
                                    final Collection     collection,
                                    final Discipline     discipline)
    {
        if (debug) log.debug("finding AppResourceDefault"); //$NON-NLS-1$
        
        for (Object obj : appResDefList)
        {
            SpAppResourceDir ard = (SpAppResourceDir)obj;

            SpecifyUser spUser = ard.getSpecifyUser();
            Collection  cs     = ard.getCollection();
            Discipline  ct     = ard.getDiscipline();

            if (spUser != null && spUser.getSpecifyUserId() == userArg.getSpecifyUserId() &&
                cs != null && cs.getCollectionId() == collection.getCollectionId() &&
                ct != null && ct.getDisciplineId() == discipline.getDisciplineId())
            {
                return ard;
            }
        }
        return null;
    }
    
    /**
     * Get SpAppResourceDir from database.
     * @param sessionArg
     * @param specifyUser
     * @param discipline
     * @param collection
     * @param userType
     * @param isPersonal
     * @param createWhenNotFound
     * @return
     */
    protected SpAppResourceDir getAppResDir(final DataProviderSessionIFace sessionArg,
                                            final SpecifyUser    specifyUser,
                                            final Discipline     discipline,
                                            final Collection     collection,
                                            final String         userType,
                                            final boolean        isPersonal,
                                            final String         localizedTitle,
                                            final boolean        createWhenNotFound)
    {
        StringBuilder sb = new StringBuilder("FROM SpAppResourceDir WHERE specifyUserId = "); //$NON-NLS-1$
        sb.append(specifyUser.getSpecifyUserId());
        if (discipline != null)
        {
            sb.append(" AND disciplineId = "); //$NON-NLS-1$
            sb.append(discipline.getId());
        } else
        {
            sb.append(" AND disciplineId is null"); //$NON-NLS-1$
        }
        
        if (collection != null)
        {
            sb.append(" AND collectionId = "); //$NON-NLS-1$
            sb.append(collection.getId());
        } else
        {
            sb.append(" AND collectionId is null"); //$NON-NLS-1$
        }
        
        if (userType != null)
        {
            sb.append(" AND userType = '"); //$NON-NLS-1$
            sb.append(userType);
            sb.append("'"); //$NON-NLS-1$
        } else
        {
            sb.append(" AND userType is null"); //$NON-NLS-1$
        }
        
        sb.append(" AND isPersonal = "); //$NON-NLS-1$
        sb.append(isPersonal);
        
        log.debug(sb.toString());
        
        List<?> list = sessionArg.getDataList(sb.toString());
        if (list.size() == 1)
        {
            SpAppResourceDir appResDir = (SpAppResourceDir)list.get(0);
            
            // This loads the lazy sets
            appResDir.getSpPersistedAppResources();
            appResDir.getSpPersistedViewSets();
            
            if (true)
            {
                for (SpAppResource appRes : appResDir.getSpPersistedAppResources())
                {
                    log.debug(appRes.getName());
                }
            }
            appResDir.setTitle(localizedTitle);
            return appResDir;
        }
        
        if (createWhenNotFound)
        {
            SpAppResourceDir appResDir = new SpAppResourceDir();
            appResDir.initialize();
            appResDir.setCollection(collection);
            appResDir.setUserType(userType);
            appResDir.setSpecifyUser(specifyUser);
            appResDir.setDiscipline(discipline);
            appResDir.setIsPersonal(isPersonal);
            appResDir.setTitle(localizedTitle);
            return appResDir;
        }
        
        return null;
    }
    
    /**
     * @param virtualDirName
     */
    public AppResourceIFace revertResource(final String virtualDirName,
                                           final AppResourceIFace appResource)
    {
        int                 virtualNameIndex = getVirtualDirIndex(virtualDirName);
        String[] levels = getVirtualDirNames();
        
        SpAppResource    fndAppRes = null;
        SpAppResourceDir fndAppDir = null;
        for (int i=virtualNameIndex;i<levels.length;i++)
        {
            fndAppDir = spAppResourceList.get(i);
            for (SpAppResource appRes : (new ArrayList<SpAppResource>(fndAppDir.getSpAppResources())))
            {
                //System.out.println(appRes.getFileName());
                
                if (appRes.getName().equals(appResource.getName()))
                {
                    fndAppRes = appRes;
                    
                    if (fndAppRes.getSpAppResourceId() == null)
                    {
                        break;
                    }
                }
            }
        }
        
        if (fndAppRes == null)
        {
            throw new RuntimeException("Couldn't find resource with name["+appResource.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        if (fndAppRes.getSpAppResourceId() == null)
        {
            //String         path      = fndAppRes.getFileName();
            //String         dirPath   = path.substring(0, path.length() - (new File(path).getName().length())-1);
            
            //AppResourceMgr appResMgr = new AppResourceMgr();
            //SpAppResource  newAppRes = (SpAppResource)appResMgr.loadResourceByName(new File(dirPath), appResource.getName());
            removeAppResource(virtualDirName, appResource);
            
            return null;
        }
        
        try
        {
            SpAppResource newAppRes = (SpAppResource)fndAppRes.clone();
            for (SpAppResourceData data : fndAppRes.getSpAppResourceDatas())
            {
                SpAppResourceData newData = (SpAppResourceData)data.clone();
                newAppRes.addReference(newData, "spAppResourceDatas"); //$NON-NLS-1$
            }
            removeAppResource(virtualDirName, appResource);
            saveResource(newAppRes);
            
            return newAppRes;
            
        } catch (CloneNotSupportedException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * @param virtualDirName
     */
    public SpViewSetObj revertViewSet(final String virtualDirName,
                                      final String vsoName)
    {
        Pair<String, File> pair = viewSetMgrHash.get(virtualDirName);
        if (pair != null)
        {
            SpAppResourceDir spAppResourceDir = spAppResourceHash.get(virtualDirName);
            if (spAppResourceDir != null)
            {
                SpViewSetObj fndVSO = null;
                for (SpViewSetObj vso : spAppResourceDir.getSpPersistedViewSets())
                {
                    if (vso.getName().equals(vsoName))
                    {
                        fndVSO = vso;
                        break;
                    }
                }
                
                if (fndVSO != null)
                {
                    DataProviderSessionIFace session = null;
                    try
                    {
                        session = DataProviderFactory.getInstance().createSession();
                        session.attach(spAppResourceDir);
                        spAppResourceDir.getSpPersistedViewSets().remove(fndVSO);
                        spAppResourceDir.getSpViewSets().remove(fndVSO);
                        session.beginTransaction();
                        session.save(spAppResourceDir);
                        session.delete(fndVSO);
                        
                        session.commit();
                        session.flush();
                        
                        String viewSetMgrName = pair.first;
                        File   loadFromDir    = pair.second;
                        if (viewSetMgrName != null && loadFromDir != null)
                        {
                            SpViewSetObj vso = loadViewSetMgrFromDir(spAppResourceDir, viewSetMgrName, loadFromDir);
                            return vso;
                        }
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        session.rollback();
                        
                    } finally
                    {
                        if (session != null)
                        {
                            session.close();
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Loads a ViewSetMgr from a directory and adds it to the AppResourceDir object.
     * @param spAppResourceDir the parent object
     * @param viewSetMgrName the name of the manager
     * @param dir the directory it is loaded from
     * @return the ViewSetMgr
     */
    protected SpViewSetObj loadViewSetMgrFromDir(final SpAppResourceDir spAppResourceDir,
                                                 final String           viewSetMgrName, 
                                                 final File             dir)
    {
        if (debug) log.debug("loadViewSetMgrFromDir ["+spAppResourceDir.getIdentityTitle()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        
        SpViewSetObj vso = null;
        ViewSetMgr viewSetMgr = new ViewSetMgr(viewSetMgrName, dir);
        for (ViewSetIFace vs : viewSetMgr.getViewSets())
        {
            vso = new SpViewSetObj();
            vso.initialize();

            // Set up File Name to load the ViewSet
            vso.setFileName(dir.getAbsoluteFile() + File.separator + vs.getFileName());

            vso.setLevel((short)0);
            vso.setName(vs.getName());

            vso.setSpAppResourceDir(spAppResourceDir);
            spAppResourceDir.getSpViewSets().add(vso);
        }
        return vso;
    }
    
    /**
     * Creates an AppResourceDefault object from a directory (note the Id will be null).
     * @param virtualDirName
     * @param spAppResourceDir
     * @param viewSetMgrName
     * @param dir the directory in question)
     * @return a new AppResourceDefault object
     */
    protected SpAppResourceDir mergeAppResourceDirFromDiskDir(final String           virtualDirName,
                                                              final SpAppResourceDir spAppResourceDir,
                                                              final String           viewSetMgrName, 
                                                              final File             dir)
    {
        if (debug) 
        {
            log.debug("Creating AppResourceDef from Dir ["+virtualDirName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            log.debug("mergeAppResourceDirFromDiskDir AppResourceDef from Dir ["+dir.getAbsolutePath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        viewSetMgrHash.put(virtualDirName, new Pair<String, File>(viewSetMgrName, dir));
        
        // Checks to see if there are any ViewSet Resources that may have already been 
        // loaded from the database. If not, then load then from the Directory (file system)
        if (spAppResourceDir.getSpViewSets().size() == 0)
        {
            if (debug) log.debug("Loading ViewSets from Dir ["+dir.getAbsolutePath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            loadViewSetMgrFromDir(spAppResourceDir, viewSetMgrName, dir);
            
        } else
        {
            if (debug) log.debug("ViewSets came from the database ["+dir.getAbsolutePath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Now load all the other generic XML resources 
        AppResourceMgr appResMgr = new AppResourceMgr(dir);
        
        // Load and Hash the Persisted AppResources to we don't load any disk based
        // resource "over on top" of the database loaded resources
        Hashtable<String, SpAppResource> hash = new Hashtable<String, SpAppResource>();
        for (SpAppResource appRes : spAppResourceDir.getSpPersistedAppResources())
        {
            String fName = FilenameUtils.getName(appRes.getFileName());
            hash.put(fName, appRes);
        }
        
        // Now check and merge the two
        for (SpAppResource appRes : appResMgr.getSpAppResources())
        {
            String fName = FilenameUtils.getName(appRes.getFileName());
            SpAppResource permAppRes = hash.get(fName);
            if (permAppRes == null)
            {
                appRes.setSpAppResourceDir(spAppResourceDir);
                spAppResourceDir.getSpAppResources().add(appRes);
            } else
            {
                spAppResourceDir.getSpAppResources().add(permAppRes);
            }
        }
        
        return spAppResourceDir;
    }
    /**
     * Creates an AppResourceDefault object from a directory (note the Id will be null).
     * @param dir the directory in question)
     * @return a new AppResourceDefault object
     */
    protected SpAppResourceDir createAppResourceDefFromDir(final String viewSetMgrName, final File dir)
    {
        if (debug) log.debug("Creating AppResourceDef from Dir ["+dir.getAbsolutePath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        
        SpAppResourceDir spAppResourceDir = new SpAppResourceDir();
        spAppResourceDir.initialize();
        
        loadViewSetMgrFromDir(spAppResourceDir, viewSetMgrName, dir);

        AppResourceMgr appResMgr = new AppResourceMgr(dir);
        for (SpAppResource appRes : appResMgr.getSpAppResources())
        {
            appRes.setSpAppResourceDir(spAppResourceDir);
            spAppResourceDir.getSpAppResources().add(appRes);
        }
        return spAppResourceDir;
    }

    /**
     * For debug purposes, display the contents of a AppResourceDefault
     * @param appResDef AppResourceDefault
     * @return string of info
     */
    protected String getSpAppResDefAsString(final SpAppResourceDir appResDef)
    {
        SpecifyUser spUser      = appResDef.getSpecifyUser();
        Collection  collection  = appResDef.getCollection();
        Discipline  discipline  = appResDef.getDiscipline();

        StringBuilder strBuf = new StringBuilder();
        strBuf.append("CS["+(collection != null ? collection.getCollectionName() : "null") + "]"); //$NON-NLS-1$ //$NON-NLS-3$
        strBuf.append(" SU["+(spUser != null ? spUser.getName() : "null") + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        strBuf.append(" COD["+(discipline != null ? discipline.getTitle() : "null") + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        strBuf.append(" DSP["+appResDef.getDisciplineType() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        strBuf.append(" UTYP["+appResDef.getUserType() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (debug) log.debug("AppResDefAsString - "  + strBuf.toString()); //$NON-NLS-1$
        
        return strBuf.toString();
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#setContext(java.lang.String, java.lang.String, boolean)
     */
    public CONTEXT_STATUS setContext(final String  databaseName,
                                     final String  userName,
                                     final boolean startingOver)
    {
        return setContext(databaseName, userName, startingOver, startingOver);
    }

    public CONTEXT_STATUS setContext(final String  databaseName,
                                     final String  userName,
                                     final boolean startingOver,
                                     final boolean promptForCollection)
    {
        if (debug)  log.debug("setting context - databaseName: [" + databaseName + "] userName: [" + userName + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        this.databaseName = databaseName;
        this.userName     = userName;
        this.hasContext   = true;
        
        classObjHash.clear();
        
        DBTableIdMgr.getInstance().clearPermissions();
        
        // This is where we will read it in from the Database
        // but for now we don't need to do that.
        //
        // We need to search for User, Collection, Discipline and UserType
        // Then

        DataProviderSessionIFace session = null;
        try
        {
            session = openSession();
            
        } catch (org.hibernate.exception.SQLGrammarException ex)
        {
            showLocalizedError("SpecifyAppContextMgr.SCHEMA_OUTOF_SYNC"); //$NON-NLS-1$
            System.exit(0);
        }

        try
        {
            List<?> list = session.getDataList(SpecifyUser.class, "name", userName); //$NON-NLS-1$
            if (list.size() == 1)
            {       
                user = (SpecifyUser)list.get(0);
                user.getAgents(); // makes sure the Agent is not lazy loaded
                session.evict( user.getAgents());
                AppContextMgr.getInstance().setClassObject(SpecifyUser.class, user);
                
                if (!startingOver)
                {
                    if (user.getIsLoggedIn())
                    {
                        Object[] options = { getResourceString("SpecifyAppContextMgr.OVERRIDE"),  //$NON-NLS-1$
                                             getResourceString("SpecifyAppContextMgr.EXIT")  //$NON-NLS-1$
                              };
                        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                     getResourceString("SpecifyAppContextMgr.LOGGED_IN"),
                                                                     getResourceString("SpecifyAppContextMgr.LOGGED_IN_TITLE"),  //$NON-NLS-1$
                                                                     JOptionPane.YES_NO_OPTION,
                                                                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        if (userChoice == JOptionPane.NO_OPTION)
                        {
                            //CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit"));
                            System.exit(0);
                        }
                    }
                    
                    user.setIsLoggedIn(true);
                    user.setLoginOutTime(new Timestamp(System.currentTimeMillis()));
                    
                    try
                    {
                        session.beginTransaction();
                        session.saveOrUpdate(user);
                        session.commit();
                        
                    } catch (Exception ex)
                    {
                        log.error(ex);
                    }
                }
    
            } else
            {
                //JOptionPane.showMessageDialog(null, 
                //        getResourceString("USER_NOT_FOUND"), 
                //        getResourceString("USER_NOT_FOUND_TITLE"), JOptionPane.WARNING_MESSAGE);
                
                return CONTEXT_STATUS.Error;
                //throw new RuntimeException("The user ["+userName+"] could  not be located as a Specify user.");
            }
    
            // First we start by getting all the Collection that the User want to
            // work with for this "Context" then we need to go get all the Default View and
            // additional XML Resources.
            
            int prevCollectionId = AppContextMgr.getInstance().getClassObject(Collection.class) != null ? AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionId() : -1;
            
            // Ask the User to choose which Collection they will be working with
            Collection collection = setupCurrentCollection(user, promptForCollection);
            if (collection == null)
            {
                // Return false but don't mess with anything that has been set up so far
                currentStatus  = currentStatus == CONTEXT_STATUS.Initial ? CONTEXT_STATUS.Error : CONTEXT_STATUS.Ignore;
                return currentStatus;
            }
            
            collection = session.merge(collection);
            
            String userType = user.getUserType();
            
            if (debug) log.debug("User["+user.getName()+"] Type["+userType+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
            userType = StringUtils.replace(userType, " ", "").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
            
            if (debug) log.debug("Def Type["+userType+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            
            spAppResourceList.clear();
            viewSetHash.clear();
            
            Discipline curDis = AppContextMgr.getInstance().getClassObject(Discipline.class);
            int prevDisciplineId = curDis != null ? curDis.getDisciplineId() : -1;
    
            Discipline discipline = session.getData(Discipline.class, "disciplineId", collection.getDiscipline().getId(), DataProviderSessionIFace.CompareType.Equals) ; //$NON-NLS-1$
            discipline.forceLoad();
            
            AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
            
            String disciplineStr = discipline.getName().toLowerCase();
            
            Division division = discipline.getDivision();
            division.forceLoad();
            AppContextMgr.getInstance().setClassObject(Division.class, division);
            
            //---------------------------------------------------------
            // This is the Full Path User / Discipline / Collection / UserType / isPersonal
            // For example: rods/fish/fish/collectionmanager / true (meaning the usr's personal space)
            //---------------------------------------------------------
            String           title     = getResourceString("SpecifyAppContextMgr."+PERSONALDIR);
            SpAppResourceDir appResDir = getAppResDir(session, user, discipline, collection, userType, true, title, true);
            spAppResourceList.add(appResDir);
            spAppResourceHash.put(PERSONALDIR, appResDir);
            viewSetMgrHash.put(PERSONALDIR, new Pair<String, File>(null, null));
            
            //---------------------------------------------------------
            // This is the Full Path User / Discipline / Collection / UserType
            // For example: rods/fish/fish/collectionmanager
            //---------------------------------------------------------
            title     = getResourceString("SpecifyAppContextMgr."+USERTYPEDIR);
            appResDir = getAppResDir(session, user, discipline, collection, userType, false, title, true);
            File dir  = XMLHelper.getConfigDir(disciplineStr + File.separator + userType);
            if (dir.exists())
            {
                mergeAppResourceDirFromDiskDir(USERTYPEDIR, appResDir, disciplineStr+" "+userType, dir); //$NON-NLS-1$
            }
            spAppResourceList.add(appResDir);
            spAppResourceHash.put(USERTYPEDIR, appResDir);
            
            //---------------------------------------------------------
            // This is the Full Path User / Discipline / Collection
            // For example: rods/fish/fish
            //---------------------------------------------------------
            title     = getResourceString("SpecifyAppContextMgr."+COLLECTIONDIR);
            appResDir = getAppResDir(session, user, discipline, collection, null, false, title, true);
            spAppResourceList.add(appResDir);
            spAppResourceHash.put(COLLECTIONDIR, appResDir);
            viewSetMgrHash.put(COLLECTIONDIR, new Pair<String, File>(null, null));

            //---------------------------------------------------------
            // This is the Full Path User / Discipline
            // For example: rods/fish
            //---------------------------------------------------------
            title     = getResourceString("SpecifyAppContextMgr."+DISCPLINEDIR);
            appResDir = getAppResDir(session, user, discipline, null, null, false, title, true);
            dir       = XMLHelper.getConfigDir(disciplineStr);
            if (dir.exists())
            {
                mergeAppResourceDirFromDiskDir(DISCPLINEDIR, appResDir, disciplineStr, dir);
            }
            spAppResourceList.add(appResDir);
            spAppResourceHash.put(DISCPLINEDIR, appResDir);

            //---------------------------------------------------------
            // Common Views 
            //---------------------------------------------------------
            if (true)
            {
                title     = getResourceString("SpecifyAppContextMgr."+COMMONDIR);
                appResDir = getAppResDir(session, user, null, null, null, false, title, true);
                dir = XMLHelper.getConfigDir("common"); //$NON-NLS-1$
                if (dir.exists())
                {
                    mergeAppResourceDirFromDiskDir(COMMONDIR, appResDir, COMMONDIR, dir);
                    appResDir.setUserType(COMMONDIR);
                }
                spAppResourceList.add(appResDir);
                spAppResourceHash.put(COMMONDIR, appResDir);
            }

            //---------------------------------------------------------
            // BackStop
            //---------------------------------------------------------
            String backStopStr = "backstop";
            if (true)
            {
                dir = XMLHelper.getConfigDir(backStopStr); //$NON-NLS-1$
                if (dir.exists())
                {
                    appResDir = createAppResourceDefFromDir(BACKSTOPDIR, dir); //$NON-NLS-1$
                    appResDir.setUserType(BACKSTOPDIR); //$NON-NLS-1$
                    appResDir.setTitle(getResourceString("SpecifyAppContextMgr."+BACKSTOPDIR)); //$NON-NLS-1$
                    
                    spAppResourceList.add(appResDir);
                    spAppResourceHash.put(BACKSTOPDIR, appResDir);
                }
            } else
            {   // this is the old way
                // I don't think we want to merge 
                title = getResourceString("SpecifyAppContextMgr."+BACKSTOPDIR);
                appResDir = getAppResDir(session, user, discipline, null, BACKSTOPDIR, false, title, true); //$NON-NLS-1$
                dir = XMLHelper.getConfigDir(backStopStr); //$NON-NLS-1$
                if (dir.exists())
                {
                    mergeAppResourceDirFromDiskDir(BACKSTOPDIR, appResDir, backStopStr, dir); //$NON-NLS-1$
                }
                spAppResourceList.add(appResDir);
                spAppResourceHash.put(BACKSTOPDIR, appResDir);
            }
            
            SpecifyAppPrefs.initialPrefs();
            
            if (prevDisciplineId != -1)
            {
                CommandDispatcher.dispatch(new CommandAction("Discipline", "Changed")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            if (prevCollectionId != -1)
            {
                CommandDispatcher.dispatch(new CommandAction("Collection", "Changed")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            int disciplineeId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
            if (disciplineeId != prevDisciplineId)
            {
                SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.CORE_SCHEMA, disciplineeId, DBTableIdMgr.getInstance(), Locale.getDefault());
            }
            
            // We close the session here so all SpAppResourceDir get unattached to hibernate
            // because UIFieldFormatterMgr and loading views all need a session
            // and we don't want to reuse it and get a double session
            closeSession();
            session = null;
            
            // Here is where you turn on View/Viewdef re-use.
            if (true)
            {
                boolean cacheDoVerify = ViewLoader.isDoFieldVerification();
                ViewLoader.setDoFieldVerification(false);
                
                UIFieldFormatterMgr.getInstance();
                
                //backStopViewSetMgr.getView("Global", "Accession"); // force the loading of all the views
                
                ViewLoader.setDoFieldVerification(cacheDoVerify);
            }
            
            currentStatus = CONTEXT_STATUS.OK;
        
            return currentStatus;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally 
        {
            if (session != null)
            {
                closeSession();
            }
        }
        
        showLocalizedError("SpecifyAppContextMgr.CRITICAL_LOGIN_ERR"); //$NON-NLS-1$
        System.exit(0);
        return null;
    }

    /**
     * Returns a list of ViewSets from a AppResourceDefault, The ViewSets are created from the ViewSetObj.
     * @param dir the AppResourceDefault
     * @return list of ViewSet objects
     */
    public List<ViewSetIFace> getViewSetList(final SpAppResourceDir dir)
    {
        if (debug) log.debug("Looking up["+dir.getUniqueIdentifer()+"]["+dir.getVerboseUniqueIdentifer()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        Boolean reloadViews = AppPreferences.getLocalPrefs().getBoolean("reload_views", false); //$NON-NLS-1$
        if (reloadViews)
        {
            long rightNow = (Calendar.getInstance().getTimeInMillis()/1000);
            if ((rightNow - lastLoadTime) > 10)
            {
                viewSetHash.clear();
                lastLoadTime = rightNow;
            }
        }
        
        List<ViewSetIFace> viewSetList = viewSetHash.get(dir.getUniqueIdentifer());
        if (viewSetList == null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = openSession();
                if (dir.getSpAppResourceDirId() != null)
                {
                    session.attach(dir);
                }
                viewSetList = new Vector<ViewSetIFace>();
                for (SpViewSetObj vso : dir.getSpViewSets())
                {
                    try
                    {
                        //log.error(vso.getFileName());
                        
                        // This call assumes there is already a Session open and attached
                        Element root = XMLHelper.readStrToDOM4J(vso.getDataAsString());
                        ViewSet vs = new ViewSet(root, true);
                        vs.setFileName(vso.getFileName());
                        viewSetList.add(vs);
    
                    } catch (org.dom4j.DocumentException ex)
                    {
                        /*if (dlg == null)
                        {
                            dlg = new UnhandledExceptionDialog("SAX Parser", ex);
                            dlg.setModal(true);
                            dlg.setVisible(true);
                            dlg = null;
                            return viewSetList;
                        }*/
                        log.error(ex);
                        
                    } catch (final Exception ex)
                    {
                        log.error(vso.getName());
                        log.error(ex);
                        ex.printStackTrace();
                        
                        // This way we don't send a stack trace
                        /*SwingUtilities.invokeLater(new Runnable() {
                            public void run()
                            {
                                //UnhandledExceptionDialog dlg = new UnhandledExceptionDialog(ex);
                                //dlg.setVisible(true);
                                String str = ex.toString();
                                JOptionPane.showConfirmDialog((Frame)getTopWindow(), "Error parsing Form", ex.toString(), JOptionPane.ERROR_MESSAGE);
                            }
                            
                        });*/
                    }
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
                closeSession();
            }
            
            viewSetHash.put(dir.getUniqueIdentifer(), viewSetList);
            
        }
        return viewSetList;
    }
    

    /**
     * @return the appResourceList
     */
    public List<SpAppResourceDir> getSpAppResourceList()
    {
        return spAppResourceList;
    }
    
    /**
     * @return all unique views (does NOT return any internal views)
     */
    public List<ViewIFace> getAllViews()
    {
        Hashtable<String, ViewIFace> viewHash = new Hashtable<String, ViewIFace>();
        
        for (SpAppResourceDir appResDir : spAppResourceList)
        {
            //if (appResDir.getDiscipline() != null && appResDir.getDiscipline() == discipline)
            {
                for (ViewSetIFace vs : getViewSetList(appResDir))
                {
                    Hashtable<String, ViewIFace> vsHash = vs.getViews();
                    for (ViewIFace view : vsHash.values())
                    {
                        if (!view.isInternal() && viewHash.get(view.getName()) == null)
                        {
                            viewHash.put(view.getName(), view);
                        }
                    }
                }
            }
        }
        
        return new Vector<ViewIFace>(viewHash.values());
    }

    /**
     * @return all unique views (also returns internal views)
     */
    public List<ViewIFace> getEntirelyAllViews()
    {
        Hashtable<String, ViewIFace> viewHash = new Hashtable<String, ViewIFace>();
        
        for (SpAppResourceDir appResDir : spAppResourceList)
        {
            for (ViewSetIFace vs : getViewSetList(appResDir))
            {
                Hashtable<String, ViewIFace> vsHash = vs.getViews();
                for (ViewIFace view : vsHash.values())
                {
                    if (viewHash.get(view.getName()) == null)
                    {
                        viewHash.put(view.getName(), view);
                    }
                }
            }
        }
        
        return new Vector<ViewIFace>(viewHash.values());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getView(java.lang.String)
     */
    public ViewIFace getView(final String viewName)
    {
        return getView(null, viewName);
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getFormatter(java.lang.String, java.lang.String)
     */
    @Override
    public UIFieldFormatterIFace getFormatter(String shortClassName, String fieldName)
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(shortClassName);
        if (ti != null)
        {
            DBFieldInfo fi = ti.getFieldByName(fieldName);
            if (fi != null)
            {
                return fi.getFormatter();
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultIFace#getView(java.lang.String, java.lang.String)
     */
    public ViewIFace getView(final String viewSetName, final String viewName)
    {
        if (StringUtils.isEmpty(viewName))
        {
            throw new RuntimeException("Sorry the View Name cannot be empty."); //$NON-NLS-1$
        }

        // We now allow "null" viewset names so it can find the first one it runs into.
        
        for (SpAppResourceDir dir : spAppResourceList)
        {
            //if (debug) log.debug("getView "+getSpAppResDefAsString(appResDef)+"  ["+appResDef.getUniqueIdentifer()+"]\n  ["+appResDef.getIdentityTitle()+"]");
            if (debug)
            {
                log.debug(dir.getIdentityTitle());
            }
            
            for (ViewSetIFace vs : getViewSetList(dir))
            {
                if (debug) log.debug("VS  ["+vs.getName()+"]["+viewSetName+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                
                if (StringUtils.isEmpty(viewSetName) || vs.getName().equals(viewSetName))
                {
                    ViewIFace view = vs.getView(viewName);
                    if (view != null)
                    {
                        return view;
                    }
                }
            }
        }

        return null;
    }

    /**
     * @param appRes
     * @return
     */
    public boolean saveResource(final AppResourceIFace appRes)
    {
        if (appRes instanceof SpAppResource)
        {
            SpAppResource    spAppResource = (SpAppResource)appRes;
            SpAppResourceDir appResDir     = spAppResource.getSpAppResourceDir(); 
            if (!appResDir.getSpPersistedAppResources().contains(spAppResource))
            {
                appResDir.getSpPersistedAppResources().add(spAppResource);
            }
            
            log.debug(appResDir.getIdentityTitle());
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                session.beginTransaction();
                session.saveOrUpdate(appResDir);
                session.saveOrUpdate(spAppResource);
                session.commit();
                session.flush();
                return true;
                
            } catch (Exception ex)
            {
                session.rollback();
                log.error(ex);
                
            } finally 
            {
                if (session != null)
                {
                    session.close();
                }
            }
        } else
        {
            log.error("AppResource was not of class SpAppResource!"); //$NON-NLS-1$
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getResource(java.lang.String)
     */
    public AppResourceIFace getResource(final String name)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = openSession();
            for (SpAppResourceDir appResDir : spAppResourceList)
            {
                //log.debug(appResDir.getIdentityTitle());
                
                if (appResDir.getSpAppResourceDirId() != null)
                {
                    session.attach(appResDir);
                }
                
                for (AppResourceIFace appRes : appResDir.getSpAppResources())
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
            ex.printStackTrace();
            
        } finally 
        {
            closeSession();
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getResourceFromDir(java.lang.String, java.lang.String)
     */
    @Override
    public AppResourceIFace getResourceFromDir(final String appResDirName, final String appResName)
    {
        SpAppResourceDir appResDir = spAppResourceHash.get(appResDirName);
        if (appResDir != null)
        {
            for (SpAppResource ar : appResDir.getSpAppResources())
            {
                if (ar.getName().equals(appResName))
                {
                    return ar;
                }
            }
        } else
        {
            log.error("Couldn't find AppResDir with name["+appResDirName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultMgr#getResourceAsDOM(java.lang.String)
     */
    public Element getResourceAsDOM(final String appResName)
    {
        try
        {
            String xmlStr = getResourceAsXML(appResName);
            if (StringUtils.isNotEmpty(xmlStr))
            {
                return XMLHelper.readStrToDOM4J(xmlStr);
            }

        } catch (Exception ex)
        {
            log.error(ex);
            throw new RuntimeException(ex);
        }

        return null;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getResourceAsDOM(edu.ku.brc.af.core.AppResourceIFace)
     */
    @Override
    public Element getResourceAsDOM(final AppResourceIFace appRes)
    {
        try
        {
            String xmlStr = getResourceAsXML(appRes);
            if (StringUtils.isNotEmpty(xmlStr))
            {
                return XMLHelper.readStrToDOM4J(xmlStr);
            }

        } catch (Exception ex)
        {
            log.error(ex);
            throw new RuntimeException(ex);
        }

        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getResourceAsXML(java.lang.String)
     */
    public String getResourceAsXML(final AppResourceIFace appResource)
    {
        if (appResource != null && appResource instanceof SpAppResource)
        {
            DataProviderSessionIFace session = null;
            SpAppResource            appRes  = (SpAppResource)appResource;
            try
            {
                session = openSession();
                
                if (appRes.getSpAppResourceId() != null)
                {
                    session.attach(appRes);
                }
                
                if (appRes.getMimeType() != null && appRes.getMimeType().equals("text/xml")) //$NON-NLS-1$
                {
                    try
                    {
                        return appRes.getDataAsString(session);
    
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        throw new RuntimeException(ex);
                    }
                }
                // else
                throw new RuntimeException("MimeType was not 'text/xml'"); //$NON-NLS-1$
                
            } catch (Exception ex)
            {
                log.error(ex);
                
            } finally 
            {
                closeSession();
            }
        } else
        {
            log.debug("AppResourceIFace was null"); //$NON-NLS-1$
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getResourceAsXML(java.lang.String)
     */
    @Override
    public String getResourceAsXML(final String appResName)
    {
        return getResourceAsXML(getResource(appResName));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#putResourceAsXML(edu.ku.brc.af.core.AppResourceIFace, java.lang.String)
     */
    @Override
    public void putResourceAsXML(final AppResourceIFace appResArg, final String xmlStr)
    {
        if (appResArg == null || !(appResArg instanceof SpAppResource))
        {
            return;
        }
        
        SpAppResourceDir appResDir = null;
        SpAppResource    appRes    = (SpAppResource)appResArg;
        
        DataProviderSessionIFace session = null;
        try
        {
            session = openSession();
            
            appResDir = appRes.getSpAppResourceDir();
            
            if (appRes.getSpAppResourceId() != null)
            {
                session.attach(appRes);
            }
            
            if (appRes.getMimeType() != null && appRes.getMimeType().equals("text/xml")) //$NON-NLS-1$
            {
                try
                {
                    session.beginTransaction();
                    if (appResDir != null)
                    {
                        appResDir.setTimestampModified(new Timestamp(System.currentTimeMillis()));
                        appResDir.setModifiedByAgent(Agent.getUserAgent());
                        session.saveOrUpdate(appResDir);
                    }
                    appRes.setTimestampModified(new Timestamp(System.currentTimeMillis()));
                    appRes.setModifiedByAgent(Agent.getUserAgent());
                    appRes.setDataAsString(xmlStr);
                    session.saveOrUpdate(appRes);
                    session.commit();
                    session.flush();
                    
                } catch (Exception ex)
                {
                    session.rollback();
                    
                    log.error(ex);
                    throw new RuntimeException(ex);
                }
            }
                
        } catch (Exception ex)
        {
            log.error(ex);
            
        } finally
        {
            closeSession();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#putResourceAsXMLFromUserArea(java.lang.String, java.lang.String)
     */
    @Override
    public void putResourceAsXML(String appResName, String xmlStr)
    {
        AppResourceIFace appRes = getResource(appResName);
        if (appRes != null)
        {
            putResourceAsXML(appRes, xmlStr);
            
        } else
        {
            log.error("Couldn't find respource ["+appRes+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultMgr#getResourceByMimeType(java.lang.String)
     */
    public List<AppResourceIFace> getResourceByMimeType(final String mimeType)
    {
        List<AppResourceIFace> list = new ArrayList<AppResourceIFace>();
        for (SpAppResourceDir appResDef : spAppResourceList)
        {
            for (AppResourceIFace appRes : appResDef.getSpAppResources())
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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#createUserAreaAppResource()
     */
    @Override
    public AppResourceIFace createAppResourceForDir(final String appResDirName)
    {
        SpAppResourceDir appResDir = spAppResourceHash.get(appResDirName);
        if (appResDir != null)
        {
            SpAppResource appRes = new SpAppResource();
            appRes.initialize();
            appRes.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
            
            appResDir.getSpAppResources().add(appRes);
            appRes.setSpAppResourceDir(appResDir);
            return appRes;
                
        }
        log.error("Couldn't find AppResDir with name["+appResDirName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#removeAppResource(java.lang.String, edu.ku.brc.af.core.AppResourceIFace)
     */
    @Override
    public boolean removeAppResource(final String appResDirName, final AppResourceIFace appResource)
    {
        if (!(appResource instanceof SpAppResource))
        {
            return false;
        }
        SpAppResource appRes = (SpAppResource)appResource;
        SpAppResourceDir appResDir = spAppResourceHash.get(appResDirName);
        if (appResDir != null)
        {
            if (!appResDir.containsResource(appRes, true))
            {
                return false;
            }
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                session.beginTransaction();
                if (!appResDir.removeResource(appRes))
                {
                    session.rollback();
                    log.error("Unable to remove AppResource '" + appResource + "' from directory '" + appResDirName + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return false;
                }
                session.delete(appRes);
                session.saveOrUpdate(appResDir);
                session.commit();
                session.flush();
                return true;
            } catch (Exception ex)
            {
                session.rollback();
                log.error(ex);
                return false;
            } finally 
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }   
        log.error("Couldn't find AppResDir with name["+appResDirName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        return false;
    }

    /**
     * @param appResDir
     * @param appResource
     * @return
     */
    public boolean removeAppResourceSp(final SpAppResourceDir appResDir,
                                       final AppResourceIFace appResource)
    {
        for (Map.Entry<String, SpAppResourceDir> entry : spAppResourceHash.entrySet())
        {
            if (appResDir.getId().equals(entry.getValue().getId()))
            {
                return removeAppResource(entry.getKey(), appResource);
            }
        }
        return false;
    }


    // ----------------------------------------------------------------
    // -- Inner Classes
    // ----------------------------------------------------------------
    class AppResourceMgr
    {
        protected File locationDir;
        protected Hashtable<String, SpAppResource> appResources = null;

        public AppResourceMgr()
        {
            locationDir = null;
        }
        
        public AppResourceMgr(final File locationDir)
        {
            this.locationDir = locationDir;
            
            appResources     = new Hashtable<String, SpAppResource>();
            
            init(locationDir, null);

        }

        public SpAppResource getSpAppResource(final String name)
        {

            return appResources.get(name);
        }

        /**
         * @param appRes
         */
        public void addAppRes(final SpAppResource appRes)
        {
            appResources.put(appRes.getName(), appRes);
        }
        
        /**
         * @param locDir
         * @param resName
         * @return
         */
        public AppResourceIFace loadResourceByName(final File locDir, final String resName)
        {
            locationDir  = locDir;
            appResources = new Hashtable<String, SpAppResource>();
            
            init(locDir, resName);
            
            if (appResources.size() == 1)
            {
                return appResources.values().iterator().next();
            }
            return null;
        }

        /**
         * Reads in the App Resource for a disciplineType
         */
        protected void init(final File file, final String resName)
        {
            if (file.exists())
            {
                try
                {
                    Element root = XMLHelper.readFileToDOM4J(new FileInputStream(new File(file.getAbsoluteFile() + File.separator + "app_resources.xml"))); //$NON-NLS-1$
                    if (root != null)
                    {
                        for ( Iterator<?> i = root.elementIterator( "file" ); i.hasNext(); ) //$NON-NLS-1$
                        {
                            Element fileElement = (Element) i.next();
                            String  name        = getAttr(fileElement, "name", null); //$NON-NLS-1$
                            if (appResources.get(name) == null && (resName == null || name.equals(resName)))
                            {
                                Integer level    = getAttr(fileElement, "level", 0); //$NON-NLS-1$
                                String mimeType  = getAttr(fileElement, "mimetype", null); //$NON-NLS-1$
                                String desc      = getAttr(fileElement, "description", null); //$NON-NLS-1$
                                String fileName  = getAttr(fileElement, "file", null); //$NON-NLS-1$
                                String metaData  = getAttr(fileElement, "metadata", null); //$NON-NLS-1$

                                // these can go away once we validate the XML
                                if (level == null)
                                {
                                    throw new RuntimeException("AppResource level cannot be null!"); //$NON-NLS-1$
                                }
                                if (StringUtils.isEmpty(mimeType))
                                {
                                    throw new RuntimeException("AppResource mimeType cannot be null!"); //$NON-NLS-1$
                                }
                                if (StringUtils.isEmpty(fileName))
                                {
                                    throw new RuntimeException("AppResource file cannot be null!"); //$NON-NLS-1$
                                }

                                File resFile = new File(file.getAbsoluteFile() + File.separator + fileName);
                                if (!resFile.exists())
                                {
                                    //throw new RuntimeException("AppResource file cannot be found at["+resFile.getAbsolutePath()+"]");
                                    log.error("AppResource file cannot be found at["+resFile.getAbsolutePath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                                }

                                SpAppResource appRes = new SpAppResource();
                                appRes.initialize();
                                appRes.setLevel(level.shortValue());
                                appRes.setName(name);
                                appRes.setMimeType(mimeType);
                                appRes.setDescription(desc);
                                appRes.setMetaData(metaData);
                                appRes.setSpecifyUser(user); //added to fix not-null constraint issue
                                
                                appRes.setTimestampCreated(new Timestamp(System.currentTimeMillis()));

                                appRes.setFileName(resFile.getAbsolutePath());

                                if (debug) log.debug("Adding ["+name+"] ["+resFile.getAbsolutePath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                
                                appResources.put(name, appRes);

                            } else if (name == null)
                            {
                                log.error("AppResource Name["+name+"] is in use."); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    } else
                    {
                        String msg = "The root element for the document was null!"; //$NON-NLS-1$
                        log.error(msg);
                        throw new ConfigurationException(msg);
                    }

                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    log.error(ex);
                }
            } else
            {
                log.error("Directory ["+file.getAbsolutePath()+"] doesn't exist!"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        /**
         * Returns a list of all the AppResources
         * @return a list of all the AppResources
         */
        public List<SpAppResource> getSpAppResources()
        {
            return Collections.list(appResources.elements());
        }

    }

    
    
    /**
     * Returns a Default Object from Prefs if there is one.
     */
    @SuppressWarnings("cast") //$NON-NLS-1$
    public PickListItemIFace getDefaultPickListItem(final String pickListName, final String title)
    {
        PickListItemIFace dObj        = null;
        Collection        collection  = AppContextMgr.getInstance().getClassObject(Collection.class);
        String            prefName    = (collection != null ? collection.getIdentityTitle() : "") + pickListName + "_DefaultId"; //$NON-NLS-1$ //$NON-NLS-2$
        AppPreferences    appPrefs    = AppPreferences.getRemote();
        String            idStr       = appPrefs.get(prefName, null);
        
        if (StringUtils.isNotEmpty(idStr))
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                dObj = (PickListItemIFace)session.get(PickListItem.class, Integer.valueOf(idStr));
                
            } catch (Exception ex)
            {
                log.error(ex);
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
            
            if (dObj != null)
            {
                return dObj;
            }            
        }
            
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            PickList pickList = (PickList)session.getData(PickList.class, "name", pickListName, DataProviderSessionIFace.CompareType.Equals); //$NON-NLS-1$
            if (pickList != null)
            {
                Vector<PickListItemIFace> list = new Vector<PickListItemIFace>();
                for (PickListItem itm : pickList.getPickListItems())
                {
                    itm.getTitle();
                }
                list.addAll(pickList.getItems());
                ChooseFromListDlg<PickListItemIFace> plDlg = new ChooseFromListDlg<PickListItemIFace>(null, 
                        getLocalizedMessage("SpecifyAppContextMgr.CHS_DEF_OBJ", title), list); //$NON-NLS-1$
                plDlg.setModal(true);
                plDlg.setVisible(true);
                if (!plDlg.isCancelled())
                {
                    appPrefs.put(prefName, plDlg.getSelectedObject().getId().toString());
                    return plDlg.getSelectedObject();
                }
                return null;
            }
            // error dialog "Unable load the PickList and it's items."
            
            throw new RuntimeException("PickList name["+pickListName+"] doesn't exist."); //$NON-NLS-1$ //$NON-NLS-2$
            
        } catch (Exception ex)
        {
            log.error(ex);
            // error dialog "Unable load the PickList and it's items."
            
        } finally 
        {
            session.close();
        }
        return dObj;
    }

    
    
    /**
     * Returns a Default Object from Prefs if there is one.
     */
    @SuppressWarnings("cast") //$NON-NLS-1$
    public FormDataObjIFace getDefaultObject(final Class<?> classObj, 
                                             final String prefPrefix,
                                             final String title,
                                             final boolean ask, 
                                             boolean useAllItems)
    {
        Collection       collection  = AppContextMgr.getInstance().getClassObject(Collection.class);
        FormDataObjIFace dObj        = null;
        String           prefName    = (collection != null ? collection.getIdentityTitle() : "") + prefPrefix + "_DefaultId"; //$NON-NLS-1$ //$NON-NLS-2$
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
                
                List<Item> items = null;
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    items = new Vector<Item>();
                    for (Object o : session.getDataList(classObj))
                    {
                        FormDataObjIFace dataObj = (FormDataObjIFace)o;
                        dataObj.getId();
                        items.add(new Item(dataObj));
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
                if (items != null)
                {
                    
                    ChooseFromListDlg<Item> colDlg = new ChooseFromListDlg<Item>(null, title, items);
                    colDlg.setModal(true);
                    colDlg.setVisible(true);
                    if (!colDlg.isCancelled())
                    {
                        dObj = (FormDataObjIFace)colDlg.getSelectedObject().data;
                        appPrefs.put(prefName, dObj.getId().toString());
                        return dObj;
                    }
                } else
                {
                    // xxx error dialog "Unable to retrieve default data object"
                }
                
            } else
            {
                try
                {
                    ViewBasedSearchDialogIFace srchDlg = getViewbasedFactory().createSearchDialog(null, classObj.getSimpleName()+"Search"); //$NON-NLS-1$
                    if (srchDlg != null)
                    {
                        srchDlg.setTitle(title);
                        srchDlg.getDialog().setVisible(true);
                        if (!srchDlg.isCancelled())
                        {
                            dObj = (FormDataObjIFace)srchDlg.getSelectedObject();
                            appPrefs.put(prefName, dObj.getId().toString());
                            return dObj;
                        }
                    }
                } catch (Exception ex)
                {
                    // it's ok 
                    // we get when it can't find the search dialog
                    
                 // xxx error dialog "Unable to retrieve default search dialog"
                }
            }
        } else
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                dObj = (FormDataObjIFace)session.get(classObj, Integer.valueOf(idStr));
                
            } catch (Exception ex)
            {
                log.error(ex);
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
                
        }
        return dObj;
    }
    
    
    
    //--------------------------------------------------------
    // There is no greate place for this because the Pref system
    // has to have been initialized and the Prefs are defined
    // in package edu.ku.brc.af.prefs
    //--------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getCurrentContextDescription()
     */
    @Override
    public String getCurrentContextDescription()
    {
        StringBuilder sb = new StringBuilder();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss zzz"); //$NON-NLS-1$
        
        sb.append(sdf.format(Calendar.getInstance().getTime())+"\n"); //$NON-NLS-1$
        sb.append(Specify.getSpecify().getAppBuildVersion()+"\n"); //$NON-NLS-1$
        
        SpecifyUser spUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        if (spUser != null)
        {
            sb.append(spUser.toString() + "\n"); //$NON-NLS-1$
        }
        Agent uAgent = Agent.getUserAgent();
        if (uAgent != null)
        {
            sb.append(uAgent.toString() + "\n"); //$NON-NLS-1$
            if (StringUtils.isNotEmpty(uAgent.getEmail()))
            {
                sb.append(uAgent.getEmail() + "\n"); //$NON-NLS-1$
            }
            
            //if (uAgent.getAddresses())
            
            Division div = uAgent.getDivision();
            if (div != null)
            {
                Institution inst = div.getInstitution();
                if (inst != null)
                {
                    sb.append(inst.toString() + "\n"); //$NON-NLS-1$
                }
                sb.append(div.toString() + "\n"); //$NON-NLS-1$
                sb.append(uAgent.toString() + "\n"); //$NON-NLS-1$
            }
        }

        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        if (collection != null)
        {
            sb.append(collection.toString() + "\n"); //$NON-NLS-1$
        }
        
        return sb.toString();
    }



    protected static Boolean isNewJavaVersion = null;
    
    /**
     * Returns true is the Pref's java.version match the current System properties java.version and
     * sets the Prefs appropriately (so if it has changed it will only return true the first time 
     * is is called. (see isNewJavaVersionAtAppStart).
     * @return true is the Pref's java.version match the current System properties java.version.
     */
    public static boolean isNewJavaVersion()
    {
        String javaVersionPropName = "java.version"; //$NON-NLS-1$

        String prefsJavaVersion  = AppPreferences.getLocalPrefs().get(javaVersionPropName, null);
        String systemJavaVersion = System.getProperty("java.version"); //$NON-NLS-1$
        
        boolean isNewVersion = StringUtils.isEmpty(prefsJavaVersion) || 
                               StringUtils.isEmpty(systemJavaVersion) ||
                               !prefsJavaVersion.equals(systemJavaVersion);
        if (isNewVersion)
        {
            AppPreferences.getLocalPrefs().put(javaVersionPropName, System.getProperty("java.version")); //$NON-NLS-1$
        }
        
        if (isNewJavaVersion == null)
        {
            isNewJavaVersion = isNewVersion;
        }
        return isNewVersion;
    }
    
    /**
     * Returns whether the java.version was different when the app started, this will return 
     * the same answer each time it is called until the application terminates.
     * (see isNewJavaVersion).
     * @return
     */
    public static boolean isNewJavaVersionAtAppStart()
    {
        if (isNewJavaVersion == null)
        {
            return isNewJavaVersion();
        }
        return isNewJavaVersion;
    }

    /**
     * @param treeableClass
     * @return the TreeDefinition for the argument.
     * 
     * Finds the first TreeDefIface object in the classObjHash that maps nodes of treeableClass.
     */
    public TreeDefIface<?,?,?> getTreeDefForClass(Class<? extends Treeable<?,?,?>> treeableClass)
    {
        Enumeration<Object> objects = classObjHash.elements();
        while (objects.hasMoreElements())
        {
            Object object = objects.nextElement();
            if (object instanceof TreeDefIface<?,?,?>)
            {
                TreeDefIface<?,?,?> td = (TreeDefIface<?,?,?> )object;
                if (td.getNodeClass().equals(treeableClass))
                {
                    return td;
                }
            }
        }
       return null; 
    }
    
    /**
     * Returns a list of pre-formatted Agent names of those that are logged in.
     * Note: the current logged in person is not added to the list.
     * @param discipline the current discipline
     * @return null on error, an empty list if no one else iis logged in, or a populated list
     */
    public List<String> getAgentListLoggedIn(final Discipline discipline)
    {
        SpecifyUser spu = getClassObject(SpecifyUser.class);
        Vector<Integer> ids = new Vector<Integer>();

        StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT specifyuser.SpecifyUserID ");
        sb.append("FROM specifyuser INNER JOIN specifyuser_spprincipal ON specifyuser.SpecifyUserID = specifyuser_spprincipal.SpecifyUserID ");
        sb.append("INNER JOIN spprincipal ON specifyuser_spprincipal.SpPrincipalID = spprincipal.SpPrincipalID ");
        sb.append("WHERE spprincipal.userGroupScopeID = ");
        sb.append(discipline.getId());
        sb.append(" AND specifyuser.IsLoggedIn <> 0");
        
        Connection conn = null;        
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sb.toString());
            if (rs.next())
            {
                int id = rs.getInt(1);
                if (id != spu.getId())
                {
                    ids.add(id);
                }
            }
            rs.close();

        } 
        catch (SQLException ex)
        {
            log.error("SQLException: " + ex.toString()); //$NON-NLS-1$
            log.error(ex.getMessage());
            
            return null;
            
        } finally
        {
            try 
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (conn != null)
                {
                    conn.close();
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        List<String> names = new Vector<String>();
        if (ids.size() > 0)
        {
            sb.setLength(0);
            sb.append("SELECT ag FROM SpecifyUser spu INNER JOIN spu.agents ag INNER JOIN ag.division dv INNER JOIN dv.disciplines dsp WHERE dsp.id = ");
            sb.append(discipline.getId());
            sb.append(" AND ag.id in (");
            for (int i=0;i<ids.size();i++)
            {
                if (i > 0) sb.append(",");
                sb.append(ids.get(i));
            }
            sb.append(")");
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                List<?> dataRows = session.getDataList(sb.toString());
                for (Object obj : dataRows)
                {
                    String name = ((Agent)obj).getIdentityTitle();
                    names.add(name);
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                return null;
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
  
        return names;
    }
    
}

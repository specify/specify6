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
package edu.ku.brc.specify.config;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getViewbasedFactory;
import static edu.ku.brc.ui.UIRegistry.showLocalizedError;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.specify.principal.UserPrincipal;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
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
import edu.ku.brc.af.ui.forms.persist.FormDevHelper;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewSet;
import edu.ku.brc.af.ui.forms.persist.ViewSetIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.init.RegisterSpecify;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Borrow;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.ExchangeIn;
import edu.ku.brc.specify.datamodel.ExchangeOut;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.Gift;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpViewSetObj;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.prefs.FormattingPrefsPanel;
import edu.ku.brc.specify.tasks.BaseTreeTask;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconEntry;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UnhandledExceptionDialog;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.Triple;

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
    private static final Logger log  = Logger.getLogger(SpecifyAppContextMgr.class);
    private static final String L10N = "SpecifyAppContextMgr.";
    
    // Virtual Directory Levels
    public static final String PERSONALDIR   = "Personal"; //$NON-NLS-1$
    public static final String USERTYPEDIR   = "UserType"; //$NON-NLS-1$
    public static final String COLLECTIONDIR = "Collection"; //$NON-NLS-1$
    public static final String DISCPLINEDIR  = "Discipline"; //$NON-NLS-1$
    public static final String COMMONDIR     = "Common"; //$NON-NLS-1$
    public static final String BACKSTOPDIR   = "BackStop"; //$NON-NLS-1$
    
    private static Boolean isNewJavaVersion = null;
    
    private enum UpdateEmailType {eNone, eAgent, eSpUser, eBoth};

    protected Vector<SpAppResourceDir>              spAppResourceList = new Vector<SpAppResourceDir>();
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

    protected boolean        forceReloadViews      = false;
    protected boolean        debug                 = false;
    protected long           lastLoadTime          = 0;
    protected long           lastLoadTimeBS        = 0;
    protected UnhandledExceptionDialog uheDlg      = null;
    
    protected DataProviderSessionIFace globalSession    = null;
    protected int                      openSessionCount = 0;
    
    protected Boolean                  isSecurityOn     = null;
    
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
     * @param forceReloadViews the forceReloadViews to set
     */
    public void setForceReloadViews(boolean forceReloadViews)
    {
        this.forceReloadViews = forceReloadViews;
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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getViewSetHash()
     */
    @Override
    public Hashtable<String, List<ViewSetIFace>> getViewSetHash()
    {
        return viewSetHash;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getTaskFromTableId(int)
     */
    @Override
    public Taskable getTaskFromTableId(final int tableId)
    {
        String taskName =  tableId == Accession.getClassTableId() ||
                            tableId == Permit.getClassTableId() ||
                            tableId == Loan.getClassTableId() ||
                            tableId == InfoRequest.getClassTableId() ||
                            tableId == Gift.getClassTableId() ||
                            tableId == ExchangeIn.getClassTableId() ||
                            tableId == ExchangeOut.getClassTableId() ||
                            tableId == Borrow.getClassTableId() ? "Interactions" : "Data_Entry";
                
        return TaskMgr.getTask(taskName);
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
     * Replaces the the old Dir object with a new one.
     * @param index the index of the old
     * @param newDir the new Dir object
     */
    public void replaceSpDirItem(final int index, final SpAppResourceDir newDir)
    {
        SpAppResourceDir oldDir = spAppResourceList.get(index);
        spAppResourceList.remove(index);
        spAppResourceList.insertElementAt(newDir, index);
        
        String key = null;
        for (String hashKey : spAppResourceHash.keySet())
        {
            if (spAppResourceHash.get(hashKey) == oldDir)
            {
                key = hashKey;
                break;
            }
        }
        if (key != null)
        {
            spAppResourceHash.put(key, newDir);
        }
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
        String sqlStr = "SELECT count(cs) From Collection as cs INNER JOIN cs.userGroups as princ INNER JOIN princ.specifyUsers as user where user.specifyUserId = "+user.getSpecifyUserId(); //$NON-NLS-1$
        
        DataProviderSessionIFace session = null;
        try
        {
            session = openSession();
            Object  result     = session.getData(sqlStr);
            return result != null ? (Integer)result : 0;
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
        SpecifyUser     spUser = getClassObject(SpecifyUser.class);
        if (spUser != null)
        {
            sessionArg.attach(spUser);
            for (Agent agent : spUser.getAgents())
            {
                for (Discipline discipline : agent.getDivision().getDisciplines())
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
     * Sets up the "current" Collection by first checking prefs for the most recent primary key,
     * @param userArg the user object of the current object
     * @param promptForCollection indicates the User should always be asked which Collection to use
     * @param collectionName name of collection to choose (can be null)
     * @return the current Collection or null
     */
    protected Collection setupCurrentCollection(final SpecifyUser userArg,
                                                final boolean     promptForCollection,
                                                final String      collectionName)
    {
        DataProviderSessionIFace session = null;
        try
        {
            AppPreferences remotePrefs = AppPreferences.getRemote();
            
            session = DataProviderFactory.getInstance().createSession();
            
            SpecifyUser spUser = session.getData(SpecifyUser.class, "id", userArg.getId(), DataProviderSessionIFace.CompareType.Equals); //$NON-NLS-1$
            
            String  alwaysAskPref = "ALWAYS.ASK.COLL"; //$NON-NLS-1$
            boolean askForColl    = remotePrefs.getBoolean(alwaysAskPref, true);
            String   prefName     = mkUserDBPrefName("recent_collection_id"); //$NON-NLS-1$
            
            // First get the Collections the User has access to.
            Hashtable<String, Pair<String, Integer>> collectionHash = new Hashtable<String, Pair<String, Integer>>();
            
            String sqlStr = String.format("SELECT DISTINCT cln.CollectionID, cln.CollectionName, cln.DisciplineID FROM collection AS cln " +
                            "Inner Join spprincipal AS p ON cln.UserGroupScopeId = p.userGroupScopeID " +
                            "Inner Join specifyuser_spprincipal AS su_pr ON p.SpPrincipalID = su_pr.SpPrincipalID " +
                            "WHERE su_pr.SpecifyUserID = %d AND GroupSubClass = '%s'", spUser.getSpecifyUserId(), UserPrincipal.class.getName()); //$NON-NLS-1$
            
            //log.debug(sqlStr);
            
            for (Object[] row : BasicSQLUtils.query(sqlStr))
            {
                String  collName = row[1].toString();
                Integer collId   = (Integer)row[0];
                
                if (collectionHash.get(collName) != null)
                {
                    String dispName = BasicSQLUtils.querySingleObj("SELECT Name FROM discipline WHERE DisciplineID = " + row[2]);
                    collName += " - " + dispName;
                    if (collectionHash.get(collName) != null)
                    {
                        String sql = "SELECT d.DivisionID FROM collection c INNER JOIN discipline d ON c.DisciplineID = d.UserGroupScopeId WHERE d.DisciplineID = " + row[2];
                        String divName = BasicSQLUtils.querySingleObj(sql);
                        collName += " - " + divName;
                    }
                }
                collectionHash.put(collName, new Pair<String, Integer>(collName, collId));
            }
    
            Pair<String, Integer> currColl = null;
            
            if (collectionName == null)
            {
                String recentIds = askForColl || promptForCollection ? null : remotePrefs.get(prefName, null);
                if (StringUtils.isNotEmpty(recentIds))
                {
                    Vector<Object[]> rows = BasicSQLUtils.query("SELECT CollectionName, UserGroupScopeId FROM collection WHERE UserGroupScopeId = " + recentIds); //$NON-NLS-1$
                    if (rows.size() == 1)
                    {
                        String  collName = rows.get(0)[0].toString();
                        Integer collId   = (Integer)rows.get(0)[1];
                        currColl = new Pair<String, Integer>(collName, collId);
                        
                    } else
                    {
                        log.debug("could NOT find recent ids"); //$NON-NLS-1$
                    }
                }
                
                if (currColl != null && collectionHash.get(currColl.first) == null)
                {
                    currColl = null;
                }
            }
            
            
            if (currColl == null || (askForColl && promptForCollection))
            {
                if (collectionHash.size() == 1)
                {
                    currColl = collectionHash.elements().nextElement();
    
                } else if (collectionHash.size() > 0)
                {
                    if (collectionName == null)
                    {
                        List<Pair<String, Integer>> list = new Vector<Pair<String, Integer>>();
                        list.addAll(collectionHash.values());
                        Collections.sort(list, new Comparator<Pair<String, Integer>>() {
                            @Override
                            public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
                            {
                                return o1.first.compareTo(o2.first);
                            }
                        });
                        
                        int selectColInx = -1;
                        
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
                        
                        currColl = colDlg.getSelectedObject();
                    } else
                    {
                        Integer colId = BasicSQLUtils.getCount(String.format("SELECT CollectionID FROM collection WHERE CollectionName = '%s'", collectionName));
                        if (colId != null)
                        {
                            currColl = new Pair<String, Integer>(collectionName, colId);
                        } else
                        {
                            return null;
                        }
                    }
                }
            }
            
            Collection collection = null;
            
            if (currColl != null)
            {
                collection = (Collection)session.getData("FROM Collection WHERE id = " + currColl.second);
                if (collection != null)
                {
                    collection.forceLoad();
                    remotePrefs.put(prefName, (Long.toString(collection.getCollectionId())));
                    remotePrefs.flush();
                }
            }
            
            if (collection == null)
            {
                UIRegistry.showLocalizedError(L10N + "ERR_NO_COLL");
                return null;
            }
            
            setClassObject(Collection.class, collection);
            
            if (collectionName == null)
            {
                String colObjStr = "CollectionObject"; //$NON-NLS-1$
                String iconName = remotePrefs.get(FormattingPrefsPanel.getDisciplineImageName(), colObjStr);
                if (StringUtils.isEmpty(iconName) || iconName.equals(colObjStr))
                {
                    iconName = "colobj_backstop"; //$NON-NLS-1$
                }
            
                IconManager.aliasImages(iconName,                  // Source
                                        colObjStr);                // Dest //$NON-NLS-1$
    
                IconManager.aliasImages(iconName,                  // Source
                                        colObjStr.toLowerCase());  // Dest //$NON-NLS-1$
            }
            
            Discipline discipline = collection.getDiscipline();
            session.attach(discipline);
            
            Institution institution = discipline.getDivision().getInstitution();
            session.attach(institution);
            
            setClassObject(Institution.class, institution);
            
            if (!Agent.setUserAgent(spUser, discipline.getDivision()))
            {
                return null;
            }
            
            AppContextMgr am = AppContextMgr.getInstance();
            discipline.getTaxonTreeDef().forceLoad();
            am.setClassObject(TaxonTreeDef.class,              discipline.getTaxonTreeDef());
            discipline.getGeologicTimePeriodTreeDef().forceLoad();
            am.setClassObject(GeologicTimePeriodTreeDef.class, discipline.getGeologicTimePeriodTreeDef());
            institution.getStorageTreeDef().forceLoad();
            am.setClassObject(StorageTreeDef.class,            institution.getStorageTreeDef());
            discipline.getLithoStratTreeDef().forceLoad();
            am.setClassObject(LithoStratTreeDef.class,         discipline.getLithoStratTreeDef());
            discipline.getGeographyTreeDef().forceLoad();
            am.setClassObject(GeographyTreeDef.class,          discipline.getGeographyTreeDef());
            
            return collection;
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            ex.printStackTrace();
            showLocalizedError(ex.toString()); // Yes, I know it isn't localized.
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
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
    public SpAppResourceDir getAppResDir(final DataProviderSessionIFace sessionArg,
                                         final SpecifyUser    specifyUser,
                                         final Discipline     discipline,
                                         final Collection     collection,
                                         final String         userType,
                                         final boolean        isPersonal,
                                         final String         localizedTitle,
                                         final boolean        createWhenNotFound)
    {
        return getAppResDir(sessionArg, specifyUser, discipline, collection, userType, isPersonal, localizedTitle, createWhenNotFound, false);
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
     * @param checkForNullSpUser
     * @return
     */
    public SpAppResourceDir getAppResDir(final DataProviderSessionIFace sessionArg,
                                         final SpecifyUser    specifyUser,
                                         final Discipline     discipline,
                                         final Collection     collection,
                                         final String         userType,
                                         final boolean        isPersonal,
                                         final String         localizedTitle,
                                         final boolean        createWhenNotFound,
                                         final boolean        checkForNullSpUser)
    {
        StringBuilder sb = new StringBuilder("FROM SpAppResourceDir WHERE");
        sb.append(" isPersonal = "); //$NON-NLS-1$
        sb.append(isPersonal);
        
        if (checkForNullSpUser)
        {
            sb.append(" AND specifyUserId is null"); //$NON-NLS-1$
            
        } else if (isPersonal)
        {
            sb.append(" AND specifyUserId = ");
            sb.append(specifyUser.getSpecifyUserId());
        }
        
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
        sb.append(" ORDER BY id ASC"); //$NON-NLS-1$
        
        //log.debug(sb.toString());
        
        List<?> list = sessionArg.getDataList(sb.toString());
        if (list.size() > 0)
        {
            SpAppResourceDir appResDir = (SpAppResourceDir)list.get(0);
            
            // This loads the lazy sets
            appResDir.getSpPersistedAppResources().size();
            appResDir.getSpPersistedViewSets().size();
            
            // forces load of resource
            for (SpAppResource appRes : appResDir.getSpPersistedAppResources())
            {
                appRes.forceLoad();
                //log.debug(appRes.getName());
            }
            for (SpViewSetObj vso : appResDir.getSpPersistedViewSets())
            {
                vso.forceLoad();
                //log.debug(vso.getName());
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
            appResDir.setDisciplineType(discipline != null ? discipline.getName() : null);
            return appResDir;
        }
        
        return null;
    }
    
    /**
     * @param resourceName
     * @param includeUserType
     * @return
     */
    public AppResourceIFace getDiskDisciplineResourceByName(final String resourceName, 
                                                            final boolean includeUserType)
    {
        String userType = user.getUserType();
        
        AppResourceIFace diskResource = null;
        
        Discipline     discipline     = getClassObject(Discipline.class);
        DisciplineType disciplineType = DisciplineType.getDiscipline(discipline.getType());
        String         folderName     = disciplineType.getFolder();
        File           dir            = XMLHelper.getConfigDir(folderName);
        if (dir.exists())
        {
            if (includeUserType)
            {
                dir = XMLHelper.getConfigDir(dir.getAbsolutePath() + File.separator + userType);
                if (!dir.exists())
                {
                    return null;
                }
            }
            
            AppResourceMgr appResMgr = new AppResourceMgr(dir);
            
            for (SpAppResource appRes : appResMgr.getSpAppResources())
            {
                String fileAppResName = appRes.getName();
                if (fileAppResName.equals(resourceName))
                {
                    diskResource = appRes;
                    break;
                }
            }
        }
        
        return diskResource;
    }
    
    
    /**
     * @param resDirName
     * @param resourceName
     */
    public void addDiskResourceToAppDir(final String resDirName, 
                                        final String resourceName)
    {
        SpAppResourceDir appResDir = spAppResourceHash.get(resDirName);
        if (appResDir != null)
        {
            if (appResDir.getId() != null)
            {
                Discipline     discipline     = getClassObject(Discipline.class);
                DisciplineType disciplineType = DisciplineType.getDiscipline(discipline.getType());
                String         folderName     = disciplineType.getFolder();
                File           dir            = XMLHelper.getConfigDir(folderName);
                if (dir.exists())
                {
                    AppResourceMgr appResMgr = new AppResourceMgr(dir);
                    
                    for (SpAppResource appRes : appResMgr.getSpAppResources())
                    {
                        String fileAppResName = appRes.getName();
                        if (fileAppResName.equals(resourceName))
                        {
                            appRes.setSpAppResourceDir(appResDir);
                            appResDir.getSpAppResources().add(appRes);
                            DataProviderSessionIFace session = null;
                            try
                            {
                                session = DataProviderFactory.getInstance().createSession();
                                session.attach(appResDir);
                                session.save(appResDir);
                                session.commit();
                                session.flush();
                                
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                                if (session != null) session.rollback();
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
                                
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
            }
            
        } else
        {
            // error
        }
    }
    
    
    /**
     * @param virtualDirName
     */
    public AppResourceIFace revertResource(final String virtualDirName,
                                           final AppResourceIFace appResource)
    {
        int      virtualNameIndex = getVirtualDirIndex(virtualDirName);
        String[] levels           = getVirtualDirNames();
        
        SpAppResource    fndAppRes = null;
        for (int i=virtualNameIndex;i<levels.length && fndAppRes == null;i++)
        {
            SpAppResourceDir fndAppDir = spAppResourceList.get(i);
            for (SpAppResource appRes : (new ArrayList<SpAppResource>(fndAppDir.getSpAppResources())))
            {
                if (appRes.getName() != null && appRes.getName().equals(appResource.getName()))
                {
                    fndAppRes = appRes;
                    break;
                }
            }
        }
        
        if (fndAppRes == null)
        {
            return null;
        }
        
        removeAppResource(virtualDirName, fndAppRes);
            
        return null;
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
                        
                        boolean shouldDelDir = spAppResourceDir.getSpPersistedViewSets().size() == 0 && 
                                               spAppResourceDir.getSpViewSets().size() == 0 &&
                                               spAppResourceDir.getSpAppResources().size() == 0 &&
                                               spAppResourceDir.getSpPersistedViewSets().size() == 0;
                        
                        session.beginTransaction();
                        
                        if (!shouldDelDir)
                        {
                            session.save(spAppResourceDir);
                        }
                        
                        session.delete(fndVSO);
                        
                        if (shouldDelDir)
                        {
                            session.delete(spAppResourceDir);
                        }
                        
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
                        if (session != null) session.rollback();
                        ex.printStackTrace();
                        
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
                        
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
     * Creates an SpAppResourceDir object from a directory (note the Id will be null).
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
            String fName = appRes.getName();//FilenameUtils.getName(appRes.getFileName());
            hash.put(fName, appRes);
            if (debug) log.debug("Persisted AppRes ["+fName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        // Now check and merge the two
        for (SpAppResource appRes : appResMgr.getSpAppResources())
        {
            String        fName      = appRes.getName();//FilenameUtils.getName(appRes.getFileName());
            SpAppResource permAppRes = hash.get(fName);
            if (permAppRes == null)
            {
                appRes.setSpAppResourceDir(spAppResourceDir);
                spAppResourceDir.getSpAppResources().add(appRes);
                if (debug) log.debug("Add File AppRes ["+fName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            } else
            {
                spAppResourceDir.getSpAppResources().add(permAppRes);
                if (debug) log.debug("Add DB AppRes ["+fName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        
        for (SpAppResource appRes : spAppResourceDir.getSpAppResources())
        {
            if (debug) log.debug("In AppResDir ["+appRes.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        if (debug)
        {
            log.debug("-------------["+spAppResourceDir.getTitle()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            for (SpViewSetObj vso : spAppResourceDir.getSpViewSets())
            {
                log.debug("    VSO["+vso.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        
        return spAppResourceDir;
    }
    /**
     * Creates an SpAppResourceDir object from a directory (note the Id will be null).
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
        strBuf.append(" COD["+(discipline != null ? discipline.getType() : "null") + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        strBuf.append(" DSP["+appResDef.getDisciplineType() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        strBuf.append(" UTYP["+appResDef.getUserType() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (debug) log.debug("AppResDefAsString - "  + strBuf.toString()); //$NON-NLS-1$
        
        return strBuf.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#setContext(java.lang.String, java.lang.String, boolean, boolean)
     */
    @Override
    public CONTEXT_STATUS setContext(final String  databaseName,
                                     final String  userName,
                                     final boolean startingOver,
                                     final boolean isFirstTime,
                                     final boolean doPrompt)
    {
        return setContext(databaseName, userName, startingOver, doPrompt, isFirstTime, null, true);
    }

    /**
     * @param databaseName
     * @param userName
     * @param startingOver
     * @param isFirstTime
     * @param doPrompt
     * @param isMainAppContext
     * @return
     */
    public CONTEXT_STATUS setContext(final String  databaseName,
            final String  userName,
            final boolean startingOver,
            final boolean isFirstTime,
            final boolean doPrompt,
            final boolean isMainAppContext)
    {
    	return setContext(databaseName, userName, startingOver, doPrompt, isFirstTime, null, isMainAppContext);
    }

    /**
     * @param databaseName
     * @param userName
     * @param startingOver
     * @param doPrompt
     * @param collectionName
     * @return
     */
    public CONTEXT_STATUS setContext(final String  databaseName,
                                     final String  userName,
                                     final boolean startingOver,
                                     final boolean doPrompt,
                                     final boolean isFirstTime,
                                     final String collectionName,
                                     final boolean isMainSpecifyApp)
    {
        if (debug)  log.debug("setting context - databaseName: [" + databaseName + "] userName: [" + userName + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        this.databaseName = databaseName;
        this.userName     = userName;
        this.hasContext   = true;
        
        if (isFirstTime)
        {
            DBTableIdMgr.getInstance().clearPermissions();
        }
        
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            showLocalizedError(L10N + "SCHEMA_OUTOF_SYNC"); //$NON-NLS-1$
            System.exit(0);
        }
        
        if (session == null)
        {
            return CONTEXT_STATUS.Error;
        }
        
        try
        {
            List<?> list = session.getDataList(SpecifyUser.class, "name", userName); //$NON-NLS-1$
            if (list.size() == 1)
            {       
                user = (SpecifyUser)list.get(0);
                user.getAgents().size(); // makes sure the Agent is not lazy loaded
                session.evict( user.getAgents());
                setClassObject(SpecifyUser.class, user);
                
                if (!startingOver && isMainSpecifyApp)
                {                    
                    if (user.getIsLoggedIn())
                    {
                        Object[] options = { getResourceString(L10N + "OVERRIDE"),  //$NON-NLS-1$
                                             getResourceString(L10N + "EXIT")  //$NON-NLS-1$
                              };
                        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                     getResourceString(L10N + "LOGGED_IN"),
                                                                     getResourceString(L10N + "LOGGED_IN_TITLE"),  //$NON-NLS-1$
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
                        session.rollback();
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
            
            if (isFirstTime)
            {
                FixDBAfterLogin.fixUserPermissions(false);
            }
            
            if (!AppPreferences.getGlobalPrefs().getBoolean("ExsiccataUpdateFor1_7", false))
            {
                FixDBAfterLogin.fixExsiccata();
            }

            
            Collection curColl = getClassObject(Collection.class);
            int prevCollectionId =  curColl != null ? curColl.getCollectionId() : -1;
            
            Discipline curDis = getClassObject(Discipline.class);
            int prevDisciplineId = curDis != null ? curDis.getDisciplineId() : -1;
            
            classObjHash.clear();

            setClassObject(SpecifyUser.class, user);

            // Ask the User to choose which Collection they will be working with
            Collection collection = setupCurrentCollection(user, doPrompt, collectionName);
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
    
            Discipline discipline = session.getData(Discipline.class, "disciplineId", collection.getDiscipline().getId(), DataProviderSessionIFace.CompareType.Equals) ; //$NON-NLS-1$
            discipline.forceLoad();
            
            setClassObject(Discipline.class, discipline);
            
            String disciplineStr = discipline.getType().toLowerCase();
            
            Division division = discipline.getDivision();
            division.forceLoad();
            setClassObject(Division.class, division);
            
            DataType dataType = discipline.getDataType();
            dataType.forceLoad();
            setClassObject(DataType.class, dataType);
            
            Agent userAgent = null;
            for (Agent agt : user.getAgents())
            {
                if (agt.getDivision().getId().equals(division.getId()))
                {
                    userAgent = agt;
                    userAgent.getAddresses().size();
                    userAgent.getVariants().size();
                    break;
                }
            }
            setClassObject(Agent.class, userAgent);
            
            IconEntry ceEntry = IconManager.getIconEntryByName("CollectingEvent");
            if (ceEntry != null)
            {
                boolean isEmbedded = collection.getIsEmbeddedCollectingEvent();
                IconEntry ciEntry = IconManager.getIconEntryByName(isEmbedded ? "collectinginformation" : "ce_restore");
                if (ciEntry != null)
                {
                    ceEntry.setIcon(ciEntry.getIcon());
                    ceEntry.getIcons().clear();
                }
            }
            
            if (isFirstTime)
            {
                AppPreferences.startup();
            
                //--------------------------------------------------------------------------------
                // Check for locks set on uploader, tree update, ...
                //--------------------------------------------------------------------------------
                
                int uploadLockCheckResult = Uploader.checkUploadLock(null);
                boolean noLocks = uploadLockCheckResult != Uploader.LOCKED;
                boolean goodTrees = true;
                if (uploadLockCheckResult != Uploader.LOCK_IGNORED)
                {
                    if (noLocks)
                    {
                        if (!discipline.getTaxonTreeDef()
                                .checkNodeRenumberingLock())
                        {
                            noLocks = false;
                            UIRegistry.showLocalizedError("Specify.TreeUpdateLock",
                                    discipline.getTaxonTreeDef().getName());
                        }
                    }
                    if (noLocks)
                    {
                        if (!discipline.getGeographyTreeDef()
                                .checkNodeRenumberingLock())
                        {
                            noLocks = false;
                            UIRegistry.showLocalizedError("Specify.TreeUpdateLock",
                                    discipline.getGeographyTreeDef().getName());
                        }
                    }
                    if (noLocks)
                    {
                        if (!division.getInstitution().getStorageTreeDef()
                                .checkNodeRenumberingLock())
                        {
                            noLocks = false;
                            UIRegistry.showLocalizedError("Specify.TreeUpdateLock",
                                    division.getInstitution().getStorageTreeDef().getName());
                        }
                    }
                    if (noLocks
                            && discipline.getGeologicTimePeriodTreeDef() != null)
                    {
                        if (!discipline.getGeologicTimePeriodTreeDef()
                                .checkNodeRenumberingLock())
                        {
                            noLocks = false;
                            UIRegistry.showLocalizedError("Specify.TreeUpdateLock",
                                    discipline.getGeologicTimePeriodTreeDef().getName());
                        }
                    }
                    if (noLocks && discipline.getLithoStratTreeDef() != null)
                    {
                        if (!discipline.getLithoStratTreeDef()
                                .checkNodeRenumberingLock())
                        {
                            noLocks = false;
                            UIRegistry.showLocalizedError("Specify.TreeUpdateLock",
                                    discipline.getLithoStratTreeDef().getName());
                        }
                    }
    
                    if (noLocks)
                    {
                        // Now force node number updates for trees that are
                        // out-of-date
                        goodTrees = discipline.getTaxonTreeDef()
                                .checkNodeNumbersUpToDate(true);
                        if (goodTrees)
                        {
                            goodTrees = discipline.getGeographyTreeDef()
                                    .checkNodeNumbersUpToDate(true);
                        }
                        if (goodTrees)
                        {
                            goodTrees = division.getInstitution()
                                    .getStorageTreeDef().checkNodeNumbersUpToDate(true);
                        }
                        if (goodTrees
                                && discipline.getGeologicTimePeriodTreeDef() != null)
                        {
                            goodTrees = discipline.getGeologicTimePeriodTreeDef()
                                    .checkNodeNumbersUpToDate(true);
                        }
                        if (goodTrees && discipline.getLithoStratTreeDef() != null)
                        {
                            goodTrees = discipline.getLithoStratTreeDef()
                                    .checkNodeNumbersUpToDate(true);
                        }
                    }
                }
                
                if (!noLocks || !goodTrees)
                {
                    user.setIsLoggedIn(false);
                    user.setLoginOutTime(new Timestamp(System.currentTimeMillis()));
                    try
                    {
                        session.beginTransaction();
                        session.saveOrUpdate(user);
                        session.commit();
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
                        log.error(ex);
                    }
                    System.exit(0);
                }
                else
                {
                    user.setLoginCollectionName(collection.getCollectionName());
                    user.setLoginDisciplineName(discipline.getName());
                    try
                    {
                        session.beginTransaction();
                        session.saveOrUpdate(user);
                        session.commit();
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
                        log.error(ex);
                    }
                }
            }
            
            DisciplineType disciplineType = DisciplineType.getDiscipline(discipline.getType());
            String         folderName     = disciplineType.getFolder();
            
            //---------------------------------------------------------
            // This is the Full Path User / Discipline / Collection / UserType / isPersonal
            // For example: rods/fish/fish/manager / true (meaning the usr's personal space)
            //---------------------------------------------------------
            String           title     = getResourceString(L10N + ""+PERSONALDIR);
            SpAppResourceDir appResDir = getAppResDir(session, user, discipline, collection, userType, true, title, true);
            //System.out.println("PERSONALDIR Dir: "+appResDir.getId()+", UT: "+appResDir.getUserType()+",  IsPers: "+appResDir.getIsPersonal()+", Disp: "+appResDir.getDisciplineType());  
            spAppResourceList.add(appResDir);
            spAppResourceHash.put(PERSONALDIR, appResDir);
            viewSetMgrHash.put(PERSONALDIR, new Pair<String, File>(null, null));
            
            //---------------------------------------------------------
            // This is the Full Path User / Discipline / Collection / UserType
            // For example: rods/fish/fish/manager
            //---------------------------------------------------------
            title     = getResourceString(L10N + ""+USERTYPEDIR);
            appResDir = getAppResDir(session, user, discipline, collection, userType, false, title, true);
            //System.out.println("USERTYPEDIR Dir: "+appResDir.getId()+", UT: "+appResDir.getUserType()+",  IsPers: "+appResDir.getIsPersonal()+", Disp: "+appResDir.getDisciplineType());
            File dir  = XMLHelper.getConfigDir(folderName + File.separator + userType);
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
            title     = getResourceString(L10N + ""+COLLECTIONDIR);
            appResDir = getAppResDir(session, user, discipline, collection, null, false, title, true);
            //System.out.println("COLLECTIONDIR Dir: "+appResDir.getId()+", UT: "+appResDir.getUserType()+",  IsPers: "+appResDir.getIsPersonal()+", Disp: "+appResDir.getDisciplineType());
            spAppResourceList.add(appResDir);
            spAppResourceHash.put(COLLECTIONDIR, appResDir);
            viewSetMgrHash.put(COLLECTIONDIR, new Pair<String, File>(null, null));

            //---------------------------------------------------------
            // This is the Full Path User / Discipline
            // For example: rods/fish
            //---------------------------------------------------------
            title     = getResourceString(L10N + ""+DISCPLINEDIR);
            appResDir = getAppResDir(session, user, discipline, null, null, false, title, true);
            //System.out.println("DISCPLINEDIR Dir: "+appResDir.getId()+", UT: "+appResDir.getUserType()+",  IsPers: "+appResDir.getIsPersonal()+", Disp: "+appResDir.getDisciplineType());
            dir       = XMLHelper.getConfigDir(folderName);
            if (dir.exists())
            {
                mergeAppResourceDirFromDiskDir(DISCPLINEDIR, appResDir, disciplineStr, dir);
            }
            spAppResourceList.add(appResDir);
            spAppResourceHash.put(DISCPLINEDIR, appResDir);

            //---------------------------------------------------------
            // Common Views 
            //---------------------------------------------------------
            title     = getResourceString(L10N + ""+COMMONDIR);
            appResDir = getAppResDir(session, user, null, null, COMMONDIR, false, title, true);
            //System.out.println("COMMONDIR Dir: "+appResDir.getId()+", UT: "+appResDir.getUserType()+",  IsPers: "+appResDir.getIsPersonal()+", Disp: "+appResDir.getDisciplineType());
            dir = XMLHelper.getConfigDir("common"); //$NON-NLS-1$
            if (dir.exists())
            {
                mergeAppResourceDirFromDiskDir(COMMONDIR, appResDir, COMMONDIR, dir);
                appResDir.setUserType(COMMONDIR);
            }
            spAppResourceList.add(appResDir);
            spAppResourceHash.put(COMMONDIR, appResDir);

            //---------------------------------------------------------
            // BackStop
            //---------------------------------------------------------
            String backStopStr = "backstop";
            dir = XMLHelper.getConfigDir(backStopStr); //$NON-NLS-1$
            if (dir.exists())
            {
                appResDir = createAppResourceDefFromDir(BACKSTOPDIR, dir); //$NON-NLS-1$
                //System.out.println("appResDir Dir: "+appResDir.getId()+", UT: "+appResDir.getUserType()+",  IsPers: "+appResDir.getIsPersonal()+", Disp: "+appResDir.getDisciplineType());
                appResDir.setUserType(BACKSTOPDIR); //$NON-NLS-1$
                appResDir.setTitle(getResourceString(L10N + ""+BACKSTOPDIR)); //$NON-NLS-1$
                
                spAppResourceList.add(appResDir);
                spAppResourceHash.put(BACKSTOPDIR, appResDir);
            }
            
            if (isFirstTime)
            {
                SpecifyAppPrefs.initialPrefs();
            }
            
            closeSession();
            session = null;
            
            if (isFirstTime)
            {
                FixDBAfterLogin.fixDefaultDates();
                
                // Reset the form system because 
                // 'fixDefaultDates' loads all the forms.
                FormDevHelper.clearErrors();
                viewSetHash.clear();
                lastLoadTime = 0;
                
                // Now notify everyone
                if (prevDisciplineId != -1)
                {
                    CommandDispatcher.dispatch(new CommandAction("Discipline", "Changed")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                
                if (prevCollectionId != -1)
                {
                    CommandDispatcher.dispatch(new CommandAction("Collection", "Changed")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            
            // We must check here before we load the schema
            checkForInitialFormats();
            
            session = openSession();
            
            // Now load the Schema, but make sure the Discipline has a localization.
            // for the current locale.
            //
            // Bug Fix 9167 - 04/01/2013 - Must always redo the Schema because any formatters at the collection level
            // otherwise will not get set
            //
            int disciplineId = getClassObject(Discipline.class).getDisciplineId();
            //if (disciplineId != prevDisciplineId)
            //{
                Locale       engLocale  = null;
                Locale       fndLocale  = null;
                Locale       currLocale = SchemaI18NService.getCurrentLocale();
                List<Locale> locales    = SchemaI18NService.getInstance().getLocalesFromData(SpLocaleContainer.CORE_SCHEMA, disciplineId);
                for (Locale locale : locales)
                {
                    if (locale.equals(currLocale))
                    {
                        fndLocale = currLocale;
                    }
                    if (locale.getLanguage().equals("en"))
                    {
                        engLocale = currLocale;
                    }
                }
                if (fndLocale == null)
                {
                    if (engLocale != null)
                    {
                        fndLocale = engLocale;
                        
                    } else if (locales.size() > 0)
                    {
                        fndLocale = locales.get(0);
                        
                    } else
                    {
                        currentStatus = CONTEXT_STATUS.Error;
                        String msg = "Specify was unable to a Locale in the Schema Config for this discipline.\nPlease contact Specify support immediately.";
                        UIRegistry.showError(msg);
                        AppPreferences.shutdownAllPrefs();
                        DataProviderFactory.getInstance().shutdown();
                        DBConnection.shutdown();
                        System.exit(0);
                        return currentStatus;
                    }
                    
                    fndLocale = engLocale != null ? engLocale : locales.get(0);
                    SchemaI18NService.setCurrentLocale(fndLocale);
                    Locale.setDefault(fndLocale);
                    UIRegistry.displayErrorDlgLocalized(L10N + "NO_LOCALE", discipline.getName(), currLocale.getDisplayName(), fndLocale.getDisplayName());
                }
                SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.CORE_SCHEMA, disciplineId, DBTableIdMgr.getInstance(), Locale.getDefault());
            //}
            
            //setUpCatNumAccessionFormatters(getClassObject(Institution.class), collection);
            
            // We close the session here so all SpAppResourceDir get unattached to hibernate
            // because UIFieldFormatterMgr and loading views all need a session
            // and we don't want to reuse it and get a double session
            closeSession();
            session = null;
            
            if (isFirstTime)
            {
                for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
                {
                    ti.setPermissions(SecurityMgr.getInstance().getPermission("DO."+ti.getName().toLowerCase()));
                }
                
                // Here is where you turn on View/Viewdef re-use.
                /*if (true)
                {
                    boolean cacheDoVerify = ViewLoader.isDoFieldVerification();
                    ViewLoader.setDoFieldVerification(false);
                    
                    UIFieldFormatterMgr.getInstance();
                    
                    ViewLoader.setDoFieldVerification(cacheDoVerify);
                }*/
                
                RegisterSpecify.register(false, 0);
            }
            
            return currentStatus= CONTEXT_STATUS.OK;
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            ex.printStackTrace();
            
        } finally 
        {
            if (session != null)
            {
                closeSession();
            }
        }
        
        showLocalizedError(L10N + "CRITICAL_LOGIN_ERR"); //$NON-NLS-1$
        System.exit(0);
        return null;
    }
    
    /**
     * @param institution
     * @param collection
     */
    public static void setUpCatNumAccessionFormatters(final Institution institution, final Collection collection)
    {
        UIFieldFormatterIFace catNumFmtr = UIFieldFormatterMgr.getInstance().getFormatter(collection.getCatalogNumFormatName());
        if (catNumFmtr != null)
        {
            DBFieldInfo field = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId()).getFieldByName("catalogNumber");
            field.setFormatter(catNumFmtr);
        }
        
        if (!institution.getIsAccessionsGlobal())
        {
            for (AutoNumberingScheme ans : collection.getNumberingSchemes())
            {
                if (ans.getTableNumber() != null && ans.getTableNumber().equals(Accession.getClassTableId()))
                {
                    DBFieldInfo field = DBTableIdMgr.getInstance().getInfoById(Accession.getClassTableId()).getFieldByName("accessionNumber");
                    if (field != null)
                    {
                        UIFieldFormatterIFace accNumFmtr = UIFieldFormatterMgr.getInstance().getFormatter(ans.getFormatName());
                        if (accNumFmtr != null)
                        {
                            field.setFormatter(accNumFmtr);
                        }
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Checks a for a format file created by the Wizard for reading and adding a new
     * wizard to the UIFieldFormatMgr.
     * @param fmtFileName the name of the file.
     * @return true if the formatter was added
     */
    protected boolean addFormatFromFile(final String fmtFileName, final boolean isCatNum)
    {
        Collection  coll        = getClassObject(Collection.class);
        Institution inst        = getClassObject(Institution.class);
        boolean     isAccGlobal = inst != null && inst.getIsAccessionsGlobal();
        
        String prefix  = isCatNum || !isAccGlobal ? coll.getCollectionName() : null;
        
        String  path     = UIRegistry.getAppDataDir() + File.separator + (prefix != null ? (prefix + "_") : "") + fmtFileName;
        File    uifFile  = new File(path);
        boolean loadedOK = false;
        if (uifFile.exists())
        {
            try
            {
                loadedOK =  UIFieldFormatterMgr.getInstance().addFormatter(uifFile);
                if (loadedOK)
                {
                    try
                    {
                        uifFile.delete();
                    } catch (SecurityException ex) {}
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return loadedOK;
    }
    
    /**
     * Check for additional Formatters to be added from the Wizard.
     */
    public void checkForInitialFormats()
    {
        if (addFormatFromFile("catnumfmt.xml", true) || addFormatFromFile("accnumfmt.xml", false))
        {
            UIFieldFormatterMgr.getInstance().save();
        }
    }

    /**
     * Returns a list of ViewSets from a AppResourceDefault, The ViewSets are created from the ViewSetObj.
     * @param dirArg the AppResourceDefault
     * @return list of ViewSet objects
     */
    public List<ViewSetIFace> getViewSetList(final SpAppResourceDir dirArg)
    {
        SpAppResourceDir dir = dirArg;
        if (debug) log.debug("Looking up ["+dir.toString()+"] ["+dir.getUniqueIdentifer()+"]["+dir.getVerboseUniqueIdentifer()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        Boolean reloadViews = AppPreferences.getLocalPrefs().getBoolean("reload_views", false); //$NON-NLS-1$
        if (reloadViews || forceReloadViews)
        {
            long rightNow = (Calendar.getInstance().getTimeInMillis()/1000);
            if ((rightNow - lastLoadTime) > 10)
            {
                FormDevHelper.clearErrors();
                viewSetHash.clear();
                lastLoadTime = rightNow;
            }
            forceReloadViews = false;
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
                    try
                    {
                        session.attach(dir);
                        
                    } catch (org.hibernate.HibernateException ex)
                    {
                        dir = session.merge(dir);
                    }
                }
                
                viewSetList = new Vector<ViewSetIFace>();
                for (SpViewSetObj vso : dir.getSpViewSets())
                {
                    // This call assumes there is already a Session open and attached
                    Element root = null;
                    try
                    {
                        //if (debug) log.debug(vso.getDataAsString());
                        root = XMLHelper.readStrToDOM4J(vso.getDataAsString());
                        
                    } catch (Exception ex)
                    {
                        String msg = "Error error parsing XML: `"+vso.getName() + "`\n" + StringUtils.replace(ex.getMessage(), "Nested", "\nNested");
                        FormDevHelper.appendFormDevError(msg);
                        return viewSetList;
                    }
                    
                    ViewSet vs = new ViewSet(root, true);
                    vs.setDiskBased(vso.getId() == null);
                    vs.setFileName(vso.getFileName());
                    viewSetList.add(vs);
                    
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
                
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
                        //log.debug(view.isInternal()+"  "+view.getName());
                        
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
    	Vector<ViewIFace> list        = new Vector<ViewIFace>();
        HashSet<String>   viewHashSet = new HashSet<String>();
        
        for (SpAppResourceDir appResDir : spAppResourceList)
        {
            for (ViewSetIFace vs : getViewSetList(appResDir))
            {
                Hashtable<String, ViewIFace> vsHash = vs.getViews();
                for (ViewIFace view : vsHash.values())
                {
                    if (!viewHashSet.contains(view.getName()))
                    {
                    	viewHashSet.add(view.getName());
                    	list.add(view);
                    }
                }
            }
        }
        
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getView(java.lang.String)
     */
    @Override
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
    @Override
    public ViewIFace getView(final String viewSetName, final String viewName)
    {
        //log.debug("Looking for: "+viewName);
        ViewIFace view = null;
        Triple<ViewIFace, Boolean, Integer> viewInfoDB = getViewInternal(viewSetName, viewName, true);
        if (viewInfoDB != null)
        {
            view = viewInfoDB.first;
            //log.debug("viewInfoDB: "+viewInfoDB+" "+viewInfoDB.first+"  Lvl: "+viewInfoDB.third+"  Disk: "+viewInfoDB.second);
        }
        
        if (viewInfoDB == null || viewInfoDB.second) // viewInfoDB.second -> true means it was from the disk
        {
            Triple<ViewIFace, Boolean, Integer> viewInfo = getViewInternal(viewSetName, viewName, false);
            if (viewInfo != null && viewInfo.first != null)
            {
                //log.debug("viewInfo: "+viewInfo+"  "+viewInfo.first+" Lvl:"+viewInfo.third);
                if (viewInfoDB != null)
                {
                    view = viewInfoDB.third < viewInfo.third ? viewInfoDB.first : viewInfo.first;
                } else
                {
                    view = viewInfo.first;
                }
            }
        }
        
        return view;
    }

    /**
     * @param viewSetName
     * @param viewName
     * @param doCheckDB
     * @return
     */
    private Triple<ViewIFace, Boolean, Integer> getViewInternal(final String viewSetName, final String viewName, final boolean doCheckDB)
    {
        if (debug) log.debug("getView - viewSetName[" + viewSetName + "][" + viewName + "]");
        
        if (StringUtils.isEmpty(viewName))
        {
            throw new RuntimeException("Sorry the View Name cannot be empty."); //$NON-NLS-1$
        }

        // We now allow "null" viewset names so it can find the first one it runs into.
        
        int level = 0;
        for (SpAppResourceDir dir : spAppResourceList)
        {
            if (debug) log.debug("getView - " + dir.getIdentityTitle());
            
            if ((dir.getId() == null && !doCheckDB) || (dir.getId() != null && doCheckDB))
            {
                //if (debug) log.debug("getView "+getSpAppResDefAsString(appResDef)+"  ["+appResDef.getUniqueIdentifer()+"]\n  ["+appResDef.getIdentityTitle()+"]");
                if (debug) log.debug("  getView - " + dir.getIdentityTitle());
                
                for (ViewSetIFace vs : getViewSetList(dir))
                {
                    if (debug) log.debug("VS  ["+vs.getName()+"]["+viewSetName+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    
                    if (StringUtils.isEmpty(viewSetName) || vs.getName().equals(viewSetName))
                    {
                        ViewIFace view = vs.getView(viewName);
                        if (view != null)
                        {
                            return new Triple<ViewIFace, Boolean, Integer>(view, vs.isDiskBased(), level);
                        }
                    }
                }
            }
            level++;
        }

        return null;
    }

    /**
     * @param appRes
     * @return
     */
    @Override
    public boolean saveResource(final AppResourceIFace appRes)
    {
        if (appRes instanceof SpAppResource)
        {
            SpAppResource    spAppResource = (SpAppResource)appRes;
            SpAppResourceDir appResDir     = spAppResource.getSpAppResourceDir(); 
            
            //See if spAppResource is already saved.
            //getSpPersistedAppResources().contains(spAppResource) should
            //use SpAppResource.equals() right? 
            //But it doesn't, so need to check SpAppResourceIds explicitly.
            boolean dirContainsResource = false;
            if (spAppResource.getId() != null)
            {
                for (SpAppResource persisted : appResDir.getSpPersistedAppResources())
                {
                    if (spAppResource.getId().equals(persisted.getId()))
                    {
                        dirContainsResource = true;
                        break;
                    }
                }
            }
            
            if (!dirContainsResource)
            {
                appResDir.getSpPersistedAppResources().add(spAppResource);
            }
            log.debug(appResDir.getIdentityTitle());
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                appRes.setTimestampModified(new Timestamp(System.currentTimeMillis()));
                appRes.setModifiedByAgent(Agent.getUserAgent());
                appRes.setLevel((short)0);
                session.beginTransaction();
                if (!dirContainsResource)
                {
                    session.saveOrUpdate(appResDir);
                }
                else
                {
                    //saveOrUpdate(spAppResource) shouldn't be necessary if resource is new, 
                    //it also shouldn't cause problems, but it is only called if resource 
                    //already existed in the directory, because it often generates 
                    //hibernate exceptions for newly created resources.
                    session.saveOrUpdate(spAppResource);
                }
                session.commit();
                session.flush();
                return true;
                
            } catch (Exception ex)
            {
                if (session != null) session.rollback();
                
                ex.printStackTrace();
                
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
                
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

        
    /**
     * @param appRes
     * @return
     */
//    @Override
//    public boolean saveResource(final AppResourceIFace appRes)
//    {
//        if (appRes instanceof SpAppResource)
//        {
//            SpAppResource    spAppResource = (SpAppResource)appRes;
//            SpAppResourceDir appResDir     = spAppResource.getSpAppResourceDir(); 
//            if (!appResDir.getSpPersistedAppResources().contains(spAppResource))
//            {
//                appResDir.getSpPersistedAppResources().add(spAppResource);
//            }
//            log.debug(appResDir.getIdentityTitle());
//            
//            DataProviderSessionIFace session = null;
//            try
//            {
//                session = DataProviderFactory.getInstance().createSession();
//                session.beginTransaction();
//                session.saveOrUpdate(appResDir);
//                session.saveOrUpdate(spAppResource);
//                session.commit();
//                session.flush();
//                return true;
//                
//            } catch (Exception ex)
//            {
//                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
//                session.rollback();
//                log.error(ex);
//                
//            } finally 
//            {
//                if (session != null)
//                {
//                    session.close();
//                }
//            }
//        } else
//        {
//            log.error("AppResource was not of class SpAppResource!"); //$NON-NLS-1$
//        }
//        return false;
//    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getResource(java.lang.String)
     */
    @Override
    public AppResourceIFace getResource(final String name)
    {
        if (debug) log.debug("getting resource["+name+"]");
        
        DataProviderSessionIFace session = null;
        try
        {
            session = openSession();
            for (SpAppResourceDir appResDir : new ArrayList<SpAppResourceDir>(spAppResourceList))
            {
                //log.debug(appResDir.getIdentityTitle()+"  "+appResDir.getId());
                
                if (appResDir.getSpAppResourceDirId() != null)
                {
                    try
                    {
                        session.attach(appResDir);
                        
                    } catch (org.hibernate.HibernateException ex)
                    {
                        // if attach fails then go get the entire obj.
                        SpAppResourceDir oldObj = appResDir;
                        appResDir = session.get(SpAppResourceDir.class, appResDir.getId());
                        spAppResourceList.remove(oldObj);
                        spAppResourceList.add(appResDir);
                    }
                }
                
                for (AppResourceIFace appRes : appResDir.getSpAppResources())
                {
                    //log.debug("    "+appRes.getName());
                    if (appRes.getName().equals(name))
                    {
                        return appRes;
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            log.error(ex);
            
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
            return appResDir.getResourceByName(appResName);
        }
        log.error("Couldn't find AppResDir with name["+appResDirName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
    }

    
    /**
     * @param appResDirName
     * @return Directory with the supplied name.
     */
    public SpAppResourceDir getSpAppResourceDirByName(final String appResDirName)
    {
        return spAppResourceHash.get(appResDirName);
    }
    
    /**
     * @param appResDir
     * @return appResDir name.
     */
    public String getDirName(final SpAppResourceDir appResDir)
    {
        for (Map.Entry<String, SpAppResourceDir> dir : spAppResourceHash.entrySet())
        {
            if (appResDir.getSpAppResourceDirId().equals(dir.getValue().getSpAppResourceDirId()))
            {
                return dir.getKey();
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultMgr#getResourceAsDOM(java.lang.String)
     */
    @Override
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
            //log.debug(xmlStr);
            if (StringUtils.isNotEmpty(xmlStr))
            {
                return XMLHelper.readStrToDOM4J(xmlStr);
            }

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            log.error(ex);
            throw new RuntimeException(ex);
        }

        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getResourceAsXML(java.lang.String)
     */
    @Override
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
                    // This needs to be looked into, hack for 6.2.04
                    try
                    {
                        session.attach(appRes);
                    } catch (org.hibernate.NonUniqueObjectException ex) {}
                }
                
                if (appRes.getMimeType() != null && appRes.getMimeType().equals("text/xml")) //$NON-NLS-1$
                {
                    try
                    {
                        return appRes.getDataAsString(session);
    
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
                        log.error(ex);
                        throw new RuntimeException(ex);
                    }
                }
                // else
                throw new RuntimeException("MimeType was not 'text/xml'"); //$NON-NLS-1$
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
                    //appRes.setDataAsString(xmlStr); // this uses a session, which we can't do
                    appRes.setDataStr(xmlStr);
                    session.saveOrUpdate(appRes);
                    session.commit();
                    session.flush();
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
                    session.rollback();
                    
                    log.error(ex);
                    throw new RuntimeException(ex);
                }
            }
                
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
    @Override
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
            return createAppResourceForDir(appResDir);
        }
        log.error("Couldn't find AppResDir with name["+appResDirName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
    }
    
    /**
     * @param appResDir
     * @return
     */
    public AppResourceIFace createAppResourceForDir(final SpAppResourceDir appResDir)
    {
        SpAppResource appRes = new SpAppResource();
        appRes.initialize();
        appRes.setSpecifyUser(getClassObject(SpecifyUser.class));
        
        appResDir.getSpAppResources().add(appRes);
        appRes.setSpAppResourceDir(appResDir);
        return appRes;
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
        
        SpAppResource    appRes    = (SpAppResource)appResource;
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
                if (!appResDir.removeResource(appRes))
                {
                    //session.rollback();
                    log.error("Unable to remove AppResource '" + appResource + "' from directory '" + appResDirName + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return false;
                }
                session = DataProviderFactory.getInstance().createSession();
                session.beginTransaction();
                /* actually, the appRes will be deleted when the appResDir is saved
                session.delete(appRes); 
                */
                session.saveOrUpdate(appResDir);
                session.commit();
                session.flush();
                return true;
                
            } catch (Exception ex)
            {
                if (session != null) session.rollback();
                
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
        Collection        collection  = getClassObject(Collection.class);
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
                ChooseFromListDlg<PickListItemIFace> plDlg = new ChooseFromListDlg<PickListItemIFace>((Frame)null, 
                        getLocalizedMessage(L10N + "CHS_DEF_OBJ", title), list); //$NON-NLS-1$
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
        Collection       collection  = getClassObject(Collection.class);
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
                    @Override public String toString() { return data.getIdentityTitle(); }
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
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
                    
                    ChooseFromListDlg<Item> colDlg = new ChooseFromListDlg<Item>((Frame)null, title, items);
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
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
    // There is not great place for this because the Pref system
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
        
        SpecifyUser spUser = getClassObject(SpecifyUser.class);
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

        Collection collection = getClassObject(Collection.class);
        if (collection != null)
        {
            sb.append(collection.toString() + "\n"); //$NON-NLS-1$
        }
        
        return sb.toString();
    }

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
     * Returns the TreeDefClass object for the View if the View is for editing a Tree Object
     * or it returns false.
     * @param view the view that is being opened
     * @return TreeDefClass object or null
     */
    public Class<?> getTreeDefClass(final ViewIFace view)
    {
        if (view != null)
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
            if (tableInfo != null)
            {
                if (Treeable.class.isAssignableFrom(tableInfo.getClassObj()))
                {
                    for (Taskable tsk : TaskMgr.getInstance().getAllTasks())
                    {
                        if (tsk.getName().equals(tableInfo.getClassObj().getSimpleName()+"Tree"))
                        {
                            return ((BaseTreeTask<?, ?, ?>)tsk).getTreeDefClass();
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /*
     * 
     * Commenting  isLockOK() because it is never used
     */
//    /**
//     * @param view
//     * @param isNewForm
//     * @return
//     */
//    protected boolean isLockOK(final String    lockTitle, 
//                               final ViewIFace view, 
//                               final boolean   isNewForm)
//    {
//        Class<?> treeDefClass = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getTreeDefClass(view);
//        if (treeDefClass != null)
//        {
//            if (TaskSemaphoreMgr.isLocked(lockTitle, treeDefClass.getSimpleName(), TaskSemaphoreMgr.SCOPE.Discipline))
//            {
//                if (isNewForm)
//                {
//                    UIRegistry.showError("The tree is locked!");
//                    return false;
//                    
//                }
//                return false;
//            } 
//            TaskSemaphoreMgr.USER_ACTION action = TaskSemaphoreMgr.lock(lockTitle, treeDefClass.getSimpleName(), "def", TaskSemaphoreMgr.SCOPE.Discipline, false);
//            if (action != TaskSemaphoreMgr.USER_ACTION.OK)
//            {
//                UIRegistry.showError("Unable to Lock the tree!");
//                return false;
//            }
//        }
//        return true;
//    }

    /**
     * Checks to see if the view can be opened.
     * @param view the view to be opened
     * @return true/false
     */
    protected boolean isViewOKToOpen(final ViewIFace view)
    {
        Class<?> treeDefClass = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getTreeDefClass(view);
        if (treeDefClass != null)
        {
            //empty block
        }
        return true;
    }
    
    
    
    /**
     * Returns a list of pre-formatted Agent names of those that are logged in.
     * Note: the current logged in person is not added to the list.
     * @param discipline the current discipline (if null no discipline restrictions are applied)
     * @return null on error, an empty list if no one else is logged in, or a populated list
     */
    public List<String> getAgentListLoggedIn(final Discipline discipline)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT specifyuser.SpecifyUserID from specifyuser ");
        sb.append(" WHERE specifyuser.IsLoggedIn <> 0 and loginDisciplineName = '" + discipline.getName() + "'");
        
        SpecifyUser spUser = getClassObject(SpecifyUser.class);
        sb.append(" AND specifyuser.SpecifyUserID <> " + spUser.getId());
        
        Vector<Integer>  ids    = new Vector<Integer>();
        Vector<Object[]> idList = BasicSQLUtils.query(sb.toString());
        if (idList != null && idList.size() > 0)
        {
            for (Object[] row : idList)
            {
                ids.add((Integer)row[0]);
            }
        }
        
        List<String> names = new Vector<String>();
        if (ids.size() > 0)
        {
            sb.setLength(0);
            sb.append("SELECT ag FROM SpecifyUser spu INNER JOIN spu.agents ag ");
            sb.append("INNER JOIN ag.division dv INNER JOIN dv.disciplines dsp  WHERE dsp.id = ");
            sb.append(discipline.getId());
            sb.append(" AND spu.id in (");
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
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
    
    /**
     * @param titleKey
     * @param msgKey
     * @return true if no users are logged in, false if users are logged in
     */
    public boolean displayAgentsLoggedInDlg(final String msgKey)
    {
        int rv = displayAgentsLoggedInDlg(null, msgKey, false);
        
        return rv == CustomDialog.NONE_BTN; 
    }
    
    /**
     * @param titleKey
     * @param msgKey
     * @param includeOverride whether to include asking the user to clear all logged in users
     * @return CustomDialog.NONE_BTN - no one on, CustomDialog.OK_BTN user are on do override, 
     * CustomDialog.OK_CANCEL users logged on, but don't override
     */
    public int displayAgentsLoggedInDlg(final String titleKey, 
                                        final String msgKey, 
                                        final boolean includeOverride)
    {
        List<String> logins = getAgentListLoggedIn(getClassObject(Discipline.class));
        return displayAgentsLoggedInDlg(titleKey, msgKey, logins, includeOverride);
    }
    
    /**
     * @param titleKey
     * @param msgKey
     * @param logins
     * @param includeOverride whether to include asking the user to clear all logged in users
     * @return CustomDialog.NONE_BTN - no one on, CustomDialog.OK_BTN user are on do override, 
     * CustomDialog.OK_CANCEL users logged on, but don't override
     */
    public int displayAgentsLoggedInDlg(final String titleKey, 
                                        final String msgKey, 
                                        final List<String> logins,
                                        final boolean includeOverride)
    {
        
        if (logins.size() > 0)
        {
            SpecifyUser currUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            
            String sql = "SELECT Name, IsLoggedIn, IsLoggedInReport, LoginCollectionName, LoginDisciplineName FROM specifyuser WHERE IsLoggedIn <> 0 AND SpecifyUserID <> "+currUser.getId();
            Vector<Object[]> dataRows = BasicSQLUtils.query(sql);
            if (dataRows.size() > 0)
            {
                Object[][] rows = new Object[dataRows.size()][4];
                for (int i=0;i<rows.length;i++)
                {
                    rows[i] = dataRows.get(i);
                }
                DefaultTableModel model = new DefaultTableModel(rows, new Object[] {"User", "Is Logged In", "Is Logged In to Report", "Login Collection", "Login Discipline"});
                JTable table = new JTable(model);
                UIHelper.calcColumnWidths(table, 5);
                UIHelper.makeTableHeadersCentered(table, true);
                
                JScrollPane scrollPane = UIHelper.createScrollPane(table);
                
                String          rowDef   = "f:p:g, 2dlu, f:p:g, 2dlu, f:p:g";
                int             btns     = includeOverride ? CustomDialog.OKCANCEL : CustomDialog.OK_BTN;
                String          titleStr = UIRegistry.getResourceString(titleKey != null ? titleKey : L10N + "OU_TITLE");
                CellConstraints cc       = new CellConstraints();
                PanelBuilder    pb       = new PanelBuilder(new FormLayout("f:p:g", rowDef));
                
                pb.add(UIHelper.createI18NLabel(L10N + "OTHER_USERS"), cc.xy(1, 1));
                pb.add(scrollPane,   cc.xy(1, 3));
                pb.add(UIHelper.createI18NLabel(msgKey), cc.xy(1, 5));
    
                pb.setDefaultDialogBorder();
                
                CustomDialog infoDlg = new CustomDialog((Dialog)null, titleStr, true, btns, pb.getPanel());
                
                if (includeOverride) infoDlg.setOkLabel(UIRegistry.getResourceString(L10N + "LOGIN_OVRDE"));
                
                infoDlg.createUI();
                infoDlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                UIHelper.centerAndShow(infoDlg);
                return infoDlg.getBtnPressed();
            }
        }
        return CustomDialog.NONE_BTN;
    }
    
    /**
     * @param currentUserName
     * @return
     */
    public boolean checkToOverrideLogins(final String currentUserName)
    {
        SpecifyUser  spUser = getClassObject(SpecifyUser.class);
        Integer      userId = spUser != null ? spUser.getId() : null; 
        Discipline   disp   = getClassObject(Discipline.class);
        List<String> logins = null;
        
        if (currentUserName == null && disp != null)
        {
            logins = getAgentListLoggedIn(getClassObject(Discipline.class));
        } else
        {
            userId = BasicSQLUtils.getCountAsInt(String.format("SELECT SpecifyUserID FROM specifyuser WHERE Name = '%s'", currentUserName));
            if (userId != null)
            {
                ArrayList<String> userNames = new ArrayList<String>();
                String sql = String.format("SELECT name FROM specifyuser WHERE name <> '%s'", currentUserName);
                for (Object usr : BasicSQLUtils.querySingleCol(sql))
                {
                    userNames.add((String)usr);
                }
                logins = userNames;
            }
        }
        
        int rv = CustomDialog.NONE_BTN;
        if (logins != null && spUser != null)
        {
            rv = displayAgentsLoggedInDlg(null, L10N + "OVRDE_MSG",  logins, true);
            if (rv == CustomDialog.OK_BTN)
            {
                
                if (BasicSQLUtils.update("UPDATE specifyuser SET IsLoggedIn=0 WHERE SpecifyUserID <> "+spUser.getId()) > 0)
                {
                    return true;
                }
            }
        }
        return rv == CustomDialog.NONE_BTN;
    }
    
    /**
     * @return true if security is on
     */
    @Override
    public boolean isSecurity()
    {
        if (isSecurityOn == null)
        {
            String sql = "SELECT COUNT(IsSecurityOn) FROM institution WHERE IsSecurityOn = 0";
            
            Integer count = BasicSQLUtils.getCount(sql);
            if (count == null)
            {
                count = BasicSQLUtils.getCount(sql);
            }
            isSecurityOn = count == null || count == 0;
        }
        return isSecurityOn;
    }
    
    /**
     * @param secVal
     */
    @Override
    public boolean setSecurity(final boolean secVal)
    {
        isSecurityOn = secVal;
        
        boolean status = true;
        DataProviderSessionIFace session = null;
        try
        {
            session = openSession();
            Institution inst = (Institution)session.getData("FROM edu.ku.brc.specify.datamodel.Institution");
            inst.setIsSecurityOn(secVal);
            session.beginTransaction();
            session.saveOrUpdate(inst);
            session.commit();
            
        } catch (Exception ex)
        {
            if (session != null) session.rollback();
            
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            status = false;
             
        } finally
        {
            closeSession();
        }
        return status;
    }
    
    /**
     * Gets the current email address from (first) the SpecifyUser and then the User Agent
     * @param doAskForIt indicates whether to ask for the email address
     * @return return an email address
     */
    public String getMailAddr(final boolean doAskForIt)
    {
        UpdateEmailType updateType = UpdateEmailType.eNone;
        
        String      email       = null;
        SpecifyUser spUser      = getClassObject(SpecifyUser.class);
        Agent       userAgent   = getClassObject(Agent.class);
        
        if (spUser != null && StringUtils.isNotEmpty(spUser.getEmail()))
        {
            email = spUser.getEmail();
            if (StringUtils.isNotEmpty(email)) updateType = UpdateEmailType.eAgent;
        }
        
        if (StringUtils.isEmpty(email) && 
            userAgent != null && StringUtils.isNotEmpty(userAgent.getEmail()))
        {
            email = userAgent.getEmail();
            if (StringUtils.isNotEmpty(email)) updateType = UpdateEmailType.eSpUser;
        }
        
        if (!doAskForIt)
        {
            return  StringUtils.isEmpty(email) ? "" : email;
        }
        
        if (StringUtils.isEmpty(email) || !UIHelper.isValidEmailAddress(email))
        {
            boolean isValidEmailAddr = true;
            do
            {
                email = UIRegistry.askForString(L10N + "ENT_EMAIL_LABEL", 
                                                L10N + "ENT_EMAIL_TITLE", 
                                                L10N + (isValidEmailAddr ? "ENT_EMAIL_WHY" : "ENT_EMAIL_ERR"),
                                                true);
                isValidEmailAddr = UIHelper.isValidEmailAddress(email);
            } while (!isValidEmailAddr);
            
            updateType = UpdateEmailType.eBoth;
        }
        
        if (updateType != UpdateEmailType.eNone)
        {
            DataProviderSessionIFace session = openSession();
            if (session != null)
            {
                try
                {
                    spUser = session.get(SpecifyUser.class, spUser.getId());
                    spUser.setEmail(email);
                    session.beginTransaction();
                    
                    if (updateType == UpdateEmailType.eBoth || updateType == UpdateEmailType.eAgent)
                    {
                        for (Agent agt : spUser.getAgents())
                        {
                            agt.setEmail(email);
                            session.saveOrUpdate(agt);
                        }
                    }
                    if (updateType == UpdateEmailType.eBoth || updateType == UpdateEmailType.eSpUser)
                    {
                        session.saveOrUpdate(spUser);
                    }
                    session.commit();
                    
                    setClassObject(Agent.class, session.get(Agent.class, userAgent.getId()));
                    setClassObject(SpecifyUser.class, spUser);
                    
                } catch (Exception ex)
                {
                    session.rollback();
                    ex.printStackTrace();
                    
                } finally
                {
                    closeSession();
                }
            }
        }

        return email;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#clear()
     */
    @Override
    public void clear()
    {
        super.clear();
        
        spAppResourceList.clear();
        spAppResourceHash.clear();
        viewSetHash.clear();
        viewSetMgrHash.clear();
    }
    
}

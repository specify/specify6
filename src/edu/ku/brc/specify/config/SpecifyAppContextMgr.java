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

import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpViewSetObj;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UnhandledExceptionDialog;
import edu.ku.brc.ui.ToggleButtonChooserPanel.Type;
import edu.ku.brc.ui.db.PickListItemIFace;
import edu.ku.brc.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.ViewSetMgr;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.ui.forms.persist.ViewIFace;
import edu.ku.brc.ui.forms.persist.ViewLoader;
import edu.ku.brc.ui.forms.persist.ViewSet;
import edu.ku.brc.ui.forms.persist.ViewSetIFace;

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

    protected List<SpAppResourceDir>            spAppResourceList = new ArrayList<SpAppResourceDir>();
    protected Hashtable<String, List<ViewSetIFace>> viewSetHash       = new Hashtable<String, List<ViewSetIFace>>();
    

    protected String         databaseName          = null;
    protected String         userName              = null;
    protected SpecifyUser    user                  = null;

    protected ViewSetMgr     backStopViewSetMgr    = null;
    protected AppResourceMgr backStopAppResMgr     = null;
    protected Agent          currentUserAgent      = null;

    protected boolean        debug                 = false;
    protected long           lastLoadTime          = 0;
    protected long           lastLoadTimeBS        = 0;
    protected UnhandledExceptionDialog dlg         = null;
    
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
     * @return the viewSetHash
     */
    public Hashtable<String, List<ViewSetIFace>> getViewSetHash()
    {
        return viewSetHash;
    }

    /**
     * Returns the backstop ViewSetMgr.
     * @return the backstop ViewSetMgr.
     */
    public ViewSetMgr getBackstopViewSetMgr()
    {
        return backStopViewSetMgr;
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
     * Returns the number of Collection that this user is connected to.
     * @return the number of Collection that this user is connected to.
     */
    public int getNumOfCollectionsForUser()
    {
        String sqlStr = "select count(cs) From Discipline as ct Inner Join ct.agents cta Inner Join cta.specifyUser as user Inner Join ct.collections as cs where user.specifyUserId = "+user.getSpecifyUserId();
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        Object                   result     = session.getData(sqlStr);
        int                      count      =  result != null ? (Integer)result : 0;
        session.close();
        return count;
        
    }
    
    /**
     * @param sessionArg
     * @return
     */
    public List<Integer> getCollectionIdList(final DataProviderSessionIFace sessionArg)
    {
        Vector<Integer> list   = new Vector<Integer>();
        SpecifyUser     spUser = SpecifyUser.getCurrentUser();
        if (spUser != null)
        {
            sessionArg.attach(spUser);
            for (Agent agent : spUser.getAgents())
            {
                Discipline type = agent.getDiscipline();
                for (Collection collection : type.getCollections())
                {
                    list.add(collection.getCollectionId().intValue());
                }
            }
        } else
        {
            log.error("SpecifyUser was null!");
        }
        return list;
    }
    
    /**
     * Sets up the "current" Collection by first checking prefs for the most recent primary key,
     * if it can't get it then it asks the user to select one. (Note: if there is only one it automatically chooses it)
     * @param sessionArg a session
     * @param user the user object of the current object
     * @param alwaysAsk indicates the User should always be asked which Collection to use
     * @return the current Collection or null
     */
    @SuppressWarnings("unchecked")
    protected Collection setupCurrentCollection(final DataProviderSessionIFace sessionArg, 
                                                final SpecifyUser user,
                                                final boolean startingOver)
    {
        if (sessionArg == null)
        {
            UIRegistry.showLocalizedError("SESSION_WAS_NULL");
            System.exit(0);
        }
        
        try
        {
            final String prefName = mkUserDBPrefName("recent_collection_id");
            
            // First get the Collections the User has access to.
            Hashtable<String, Collection> collectionHash = new Hashtable<String, Collection>();
            String sqlStr = "SELECT cs From Discipline as ct Inner Join ct.agents cta Inner Join cta.specifyUser as user Inner Join ct.collections as cs where user.specifyUserId = "+user.getSpecifyUserId();
            for (Object obj : sessionArg.getDataList(sqlStr))
            {
                Collection cs = (Collection)obj; 
                collectionHash.put(cs.getCollectionName(), cs);
            }
    
            Collection collection = null;
            
            AppPreferences appPrefs  = AppPreferences.getRemote();
            String         recentIds = appPrefs.get(prefName, null);
            if (StringUtils.isNotEmpty(recentIds))
            {
                List<?> list = sessionArg.getDataList("FROM Collection WHERE collectionId = " + recentIds);
                if (list.size() == 1)
                {
                    collection = (Collection)list.get(0);
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
    
                } else if (collectionHash.size() > 0)
                {
                    //Collections.sort(list); // Why doesn't this work?
                    
                    List<Collection> list = new Vector<Collection>();
                    list.addAll(collectionHash.values());
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
                        log.error("Collection was null!");
                    }
    
                    ToggleButtonChooserDlg<Collection> colDlg = new ToggleButtonChooserDlg<Collection>((Frame)UIRegistry.get(UIRegistry.FRAME),
                                                                                                  UIRegistry.getResourceString("CHOOSE_COLLECTION_TITLE"), 
                                                                                                  null,
                                                                                                  list,
                                                                                                  IconManager.getIcon("Collection"),
                                                                                                  CustomDialog.OK_BTN, Type.RadioButton);
                    colDlg.setSelectedIndex(selectColInx);
                    colDlg.setModal(true);
                    colDlg.setUseScrollPane(true);
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
    
                    if (!colDlg.isCancelled())
                    {
                        collection = colDlg.getSelectedObject();
                    }
                    
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
            
            Collection.setCurrentCollection(collection);
            Collection.setCurrentCollectionIds(getCollectionIdList(sessionArg));
            
            if (collection != null)
            {
                
                Discipline discipline = collection.getDiscipline();
                if (discipline != null)
                {
                    for (Agent uAgent : discipline.getAgents())
                    {
                        SpecifyUser spu = uAgent.getSpecifyUser();
                        if (spu != null && spu.getSpecifyUserId().equals(user.getSpecifyUserId()))
                        {
                            Agent.setUserAgent(uAgent);
                            break;
                        }
                    }
                    TaxonTreeDef.setCurrentTaxonTreeDef(discipline.getTaxonTreeDef());
                    GeologicTimePeriodTreeDef.setCurrentGeologicTimePeriodTreeDef(discipline.getGeologicTimePeriodTreeDef());
                    StorageTreeDef.setCurrentStorageTreeDef(discipline.getStorageTreeDef());
                    LithoStratTreeDef.setCurrentLithoStratTreeDef(discipline.getLithoStratTreeDef());
                    GeographyTreeDef.setCurrentGeographyTreeDef(discipline.getGeographyTreeDef());
                    
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
        
        System.exit(0);
        return null;
        
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
     * @param catSeries the Collection
     * @param discipline the Discipline
     * @return the AppResourceDefault object or null
     */
    protected SpAppResourceDir find(final List<?>        appResDefList,
                                    final SpecifyUser    userArg,
                                    final Collection     catSeries,
                                    final Discipline discipline)
    {
        if (debug) log.debug("finding AppResourceDefault");
        
        for (Object obj : appResDefList)
        {
            SpAppResourceDir ard = (SpAppResourceDir)obj;

            SpecifyUser      spUser = ard.getSpecifyUser();
            Collection    cs        = ard.getCollection();
            Discipline ct    = ard.getDiscipline();

            if (spUser != null && spUser.getSpecifyUserId() == userArg.getSpecifyUserId() &&
                cs != null && cs.getCollectionId() == catSeries.getCollectionId() &&
                ct != null && ct.getDisciplineId() == discipline.getDisciplineId())
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
    protected SpAppResourceDir createAppResourceDefFromDir(final String viewSetMgrName, final File dir)
    {
        if (debug) log.debug("Creating AppResourceDef from Dir ["+dir.getAbsolutePath()+"]");
        
        SpAppResourceDir appResDef = new SpAppResourceDir();
        appResDef.initialize();

        ViewSetMgr viewSetMgr = new ViewSetMgr(viewSetMgrName, dir);
        for (ViewSetIFace vs : viewSetMgr.getViewSets())
        {
            SpViewSetObj vso = new SpViewSetObj();
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

            vso.getSpAppResourceDirs().add(appResDef);
            appResDef.getSpViewSets().add(vso);

        }

        AppResourceMgr appResMgr = new AppResourceMgr(dir);
        for (SpAppResource appRes : appResMgr.getSpAppResources())
        {
            appRes.getSpAppResourceDirs().add(appResDef);
            appResDef.getSpAppResources().add(appRes);
        }
        return appResDef;
    }

    /**
     * For debug purposes, display the contents of a AppResourceDefault
     * @param appResDef AppResourceDefault
     * @return string of info
     */
    protected String getSpAppResDefAsString(final SpAppResourceDir appResDef)
    {
        SpecifyUser      spUser = appResDef.getSpecifyUser();
        Collection    cs   = appResDef.getCollection();
        Discipline ct  = appResDef.getDiscipline();

        StringBuilder strBuf = new StringBuilder();
        strBuf.append("CS["+(cs != null ? cs.getCollectionName() : "null") + "]");
        strBuf.append(" SU["+(spUser != null ? spUser.getName() : "null") + "]");
        strBuf.append(" COD["+(ct != null ? ct.getName() : "null") + "]");
        strBuf.append(" DSP["+appResDef.getDisciplineType() + "]");
        strBuf.append(" UTYP["+appResDef.getUserType() + "]");
        
        if (debug) log.debug("AppResDefAsString - "  + strBuf.toString());
        
        return strBuf.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#setContext(java.lang.String, java.lang.String, boolean)
     */
    public CONTEXT_STATUS setContext(final String  databaseName,
                                     final String  userName,
                                     final boolean startingOver)
    {
        if (debug)  log.debug("setting context - databaseName: [" + databaseName + "] userName: [" + userName + "]");
        
        this.databaseName = databaseName;
        this.userName     = userName;
        
        // This is where we will read it in from the Database
        // but for now we don't need to do that.
        //
        // We need to search for User, Collection, Discipline and UserType
        // Then

        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
        } catch (org.hibernate.exception.SQLGrammarException ex)
        {
            UIRegistry.showLocalizedError("SCHEMA_OUTOF_SYNC");
            System.exit(0);
        }

        try
        {
            List<?> list = session.getDataList(SpecifyUser.class, "name", userName);
            if (list.size() == 1)
            {
                user = (SpecifyUser)list.get(0);
                user.getAgents(); // makes sure the Agent is not lazy loaded
                session.evict( user.getAgents());
                SpecifyUser.setCurrentUser(user);
    
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
            
            // Ask the User to choose which Collection they will be working with
            Collection collection = setupCurrentCollection(session, user, startingOver);
            if (collection == null)
            {
                // Return false but don't mess with anything that has been set up so far
                currentStatus  = currentStatus == CONTEXT_STATUS.Initial ? CONTEXT_STATUS.Error : CONTEXT_STATUS.Ignore;
                return currentStatus;
            }
            Hashtable<String, String> dispHash = new Hashtable<String, String>();
            
            String userType = user.getUserType();
            
            if (debug) log.debug("User["+user.getName()+"] Type["+userType+"]");
    
            userType = StringUtils.replace(userType, " ", "").toLowerCase();
            
            if (debug) log.debug("Def Type["+userType+"]");
            
            spAppResourceList.clear();
            viewSetHash.clear();
    
            List<?> appResDefList = session.getDataList( "From SpAppResourceDir where specifyUserId = "+user.getSpecifyUserId());
    
            Discipline dsp = session.getData(Discipline.class, "disciplineId", collection.getDiscipline().getId(), DataProviderSessionIFace.CompareType.Equals) ;
            dsp.getDeterminationStatuss().size(); // make sure they are loaded
            Discipline.setCurrentDiscipline(dsp);
        
                
            if (debug) log.debug("Adding AppResourceDefs from Collection and ColObjDefs ColObjDef["+dsp.getName()+"]");
            
            System.out.println(dsp.getDiscipline());
            
            dispHash.put(dsp.getDiscipline(), dsp.getDiscipline());
            
            SpAppResourceDir appResourceDir = find(appResDefList, user, collection, dsp);
            if (appResourceDir != null)
            {
                if (debug) log.debug("Adding1 "+getSpAppResDefAsString(appResourceDir));
                spAppResourceList.add(appResourceDir);
            }
            
            backStopViewSetMgr = new ViewSetMgr("BackStop", XMLHelper.getConfigDir("backstop"));
            backStopAppResMgr  = new AppResourceMgr(XMLHelper.getConfigDir("backstop"));
            
            // We close the session here so all SpAppResourceDir get unattached to hibernate
            // because UIFieldFormatterMgr and loading views all need a session
            // and we don't want to reuse in and get a double session
            session.close();
            
            // Here is where you turn on View/Viewdef re-use.
            if (true)
            {
                boolean cacheDoVerify = ViewLoader.isDoFieldVerification();
                ViewLoader.setDoFieldVerification(false);
                
                UIFieldFormatterMgr.getInstance();
                
                backStopViewSetMgr.getView("Global", "Accession"); // force the loading of all the views
                
                ViewLoader.setDoFieldVerification(cacheDoVerify);
            }
            
            // Add Backstop for DisciplineType and User Type
            for (String discipline : dispHash.keySet())
            {
                if (debug) log.debug("****** Trying add Backstop for ["+discipline+"]["+userType+"]");
    
                File dir = XMLHelper.getConfigDir(discipline.toLowerCase() + File.separator + userType);
                if (dir.exists())
                {
                    SpAppResourceDir appResDef = createAppResourceDefFromDir(discipline+" "+userType, dir);
                    appResDef.setDisciplineType(discipline);
                    appResDef.setUserType(userType);
                    appResDef.setSpecifyUser(user);//added to fix not-null constraints
                    appResDef.setCollection(Collection.getCurrentCollection());
                    appResDef.setDiscipline(Discipline.getCurrentDiscipline());
                    
                    if (debug) log.debug("Adding2 "+getSpAppResDefAsString(appResDef));
                    spAppResourceList.add(appResDef);
                    
                } else
                {
                    if (debug) log.debug("***** Couldn't add Backstop for ["+discipline+"]["+userType+"] ["+dir.getAbsolutePath()+"]");
                }
            }
    
            // Add Backstop for just the DisciplineType
            for (String discipline : dispHash.keySet())
            {
                if (debug) log.debug("***** Trying add Backstop for ["+discipline.toLowerCase()+"]");
                File dir = XMLHelper.getConfigDir(discipline.toLowerCase());
                if (dir.exists())
                {
                    SpAppResourceDir appResDef = createAppResourceDefFromDir(discipline, dir);
                    appResDef.setDisciplineType(discipline);
                    appResDef.setSpecifyUser(user);
                    
                    appResDef.setCollection(collection);
                    appResDef.setDiscipline(dsp);
    
                    if (debug) log.debug("Adding3 "+getSpAppResDefAsString(appResDef));
                    spAppResourceList.add(appResDef);
                    
                } else
                {
                    if (debug) log.debug("***** Couldn't add Backstop for ["+discipline+"] ["+dir.getAbsolutePath()+"]");
                }
            }

            // Common Views 
            File dir = XMLHelper.getConfigDir("common");
            if (dir.exists())
            {
                SpAppResourceDir appResDef = createAppResourceDefFromDir("Common", dir);
                if (debug) log.debug("Adding4 "+getSpAppResDefAsString(appResDef));
                spAppResourceList.add(appResDef);
                
            } else
            {
                if (debug) log.debug("***** Couldn't add Backstop for [core] ["+dir.getAbsolutePath()+"]");
            }
            
            currentStatus = CONTEXT_STATUS.OK;
        
            return currentStatus;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally 
        {
            if (session != null && session.isOpen())
            {
                session.close();
            }
        }
        
        UIRegistry.showLocalizedError("CRITICAL_LOGIN_ERROR");
        System.exit(0);
        return null;
    }

    /**
     * Returns a list of ViewSets from a AppResourceDefault, The ViewSets are created from the ViewSetObj.
     * @param dir the AppResourceDefault
     * @return list of ViewSet objects
     */
    protected List<ViewSetIFace> getViewSetList(final SpAppResourceDir dir)
    {
        if (debug) log.debug("Looking up["+dir.getUniqueIdentifer()+"]["+dir.getVerboseUniqueIdentifer()+"]");
        
        Boolean reloadViews = AppPreferences.getLocalPrefs().getBoolean("reload_views", false);
        if (reloadViews)
        {
            long rightNow = (Calendar.getInstance().getTimeInMillis()/1000);
            //System.out.println((rightNow - lastLoadTime));
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
                session = DataProviderFactory.getInstance().createSession();
                if (dir.getSpAppResourceDirId() != null)
                {
                    session.attach(dir);
                }
                viewSetList = new Vector<ViewSetIFace>();
                for (SpViewSetObj vso : dir.getSpViewSets())
                {
                    try
                    {
                        Element root = XMLHelper.readStrToDOM4J(vso.getDataAsString());
                        viewSetList.add(new ViewSet(root, true));
    
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
                                JOptionPane.showConfirmDialog((Frame)UIRegistry.getTopWindow(), "Error parsing Form", ex.toString(), JOptionPane.ERROR_MESSAGE);
                            }
                            
                        });*/
                    }
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
     * Finds a View by name using a Discipline.
     * @param viewName the name of the view
     * @param discipline the Discipline
     * @return the view or null
     */
    public ViewIFace getView(final String viewName, final Discipline discipline)
    {
        if (debug) log.debug("Looking Up View ["+viewName+"] discipline["+(discipline != null ? discipline.getName() : "null")+"]");
        
        boolean fndColObjDef = false;
        for (SpAppResourceDir appResDir : spAppResourceList)
        {
            if (debug) log.debug("Looking["+(appResDir.getDiscipline() != null ? appResDir.getDiscipline().getName() : "null")+"]["+(discipline != null ? discipline.getName() : "null")+"]");
            
            int x = 0;
            x++;
            
            if (appResDir.getDiscipline() != null && appResDir.getDiscipline() == discipline)
            {
                fndColObjDef = true;
                for (ViewSetIFace vs : getViewSetList(appResDir))
                {
                    ViewIFace view = vs.getView(viewName);
                    if (view != null)
                    {
                        return view;
                    }
                }
            }
        }

        // This is searching the BackStops by DisciplineType and User Type
        // which were created dynamically
        if (!fndColObjDef)
        {
            String disciplineName = discipline != null ? discipline.getDiscipline() : null;
            String userType       = SpecifyUser.getCurrentUser().getUserType();
            userType = StringUtils.replace(userType, " ", "").toLowerCase();

            // Search Using the colObjectDef's disciplineType
            for (SpAppResourceDir appResDef : spAppResourceList)
            {
                String dType = appResDef.getDisciplineType();
                String uType = appResDef.getUserType();

                if (debug) log.debug("appResDef's DisciplineType["+dType+"] appResDef's UserType["+uType+"] User's userType["+userType+"]");
                
                if ((dType != null && disciplineName != null && dType.equals(disciplineName) || 
                    (dType == null || disciplineName == null)) && (uType == null || uType.equals(userType)) ||
                    (userType != null && dType != null && dType.equals(userType)))
                {
                    for (ViewSetIFace vs : getViewSetList(appResDef))
                    {
                        ViewIFace view = vs.getView(viewName);
                        if (view != null)
                        {
                            return view;
                        }
                    }
                }
            }
        }
        
        for (ViewSetIFace vs : backStopViewSetMgr.getViewSets())
        {
            ViewIFace view = backStopViewSetMgr.getView(vs.getName(), viewName);
            if (view != null)
            {
                return view;
            }
        }
        throw new RuntimeException("Can't find View ["+viewName+"] discipline["+(discipline != null ? discipline.getName() : "null")+"] ["+fndColObjDef+"]");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultIFace#getView(java.lang.String, java.lang.String)
     */
    public ViewIFace getView(final String viewSetName, final String viewName)
    {
        Boolean reloadViews = AppPreferences.getLocalPrefs().getBoolean("reload_backstop_views", false);
        if (reloadViews)
        {
            long rightNow = (Calendar.getInstance().getTimeInMillis()/1000);
            if ((rightNow - lastLoadTimeBS) > 10)
            {
                backStopViewSetMgr = new ViewSetMgr("BackStop", XMLHelper.getConfigDir("backstop"));
                lastLoadTimeBS = rightNow;
            }
        }
        
        if (StringUtils.isEmpty(viewName))
        {
            throw new RuntimeException("Sorry the View Name cannot be empty.");
        }

        // We now allow "null" viewset names so it can find the first one it runs into.
        
        //if (StringUtils.isEmpty(viewSetName))
        //{
        //    throw new RuntimeException("Sorry not empty or null ViewSetNames use the call with Discipline instead.");
        //}

        for (SpAppResourceDir appResDef : spAppResourceList)
        {
            if (debug) log.debug("getView "+getSpAppResDefAsString(appResDef)+"  ["+appResDef.getUniqueIdentifer()+"]\n  ["+appResDef.getIdentityTitle()+"]");
            
            int x = 0;
            x++;
            
            for (ViewSetIFace vs : getViewSetList(appResDef))
            {
                if (debug) log.debug("VS  ["+vs.getName()+"]["+viewSetName+"]");
                
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

        return backStopViewSetMgr.getView(viewSetName, viewName);

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultIFace#getResource(java.lang.String)
     */
    public AppResourceIFace getResource(final String name)
    {
        return  getResource(name, true);
    }

    /**
     * Get A Resource with the option of checking the backstop.
     * @param name the name of the resource
     * @param checkBackStop whether to check the backstop
     * @return the resource
     */
    protected AppResourceIFace getResource(final String name, final boolean checkBackStop)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            for (SpAppResourceDir appResDef : spAppResourceList)
            {
                //log.debug(appResDef.getId());
                
                if (appResDef.getSpAppResourceDirId() != null)
                {
                    session.attach(appResDef);
                }
                
                for (AppResourceIFace appRes : appResDef.getSpAppResources())
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
        
        if (checkBackStop)
        {
            if (backStopAppResMgr == null)
            {
                throw new RuntimeException("The backStopAppResMgr is null which means somehow a call was made to this method before the backStopAppResMgr was initialized.");
            }
            return backStopAppResMgr.getSpAppResource(name);
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceDefaultMgr#getResourceAsDOM(java.lang.String)
     */
    public Element getResourceAsDOM(final String name)
    {
        try
        {
            String xmlStr = getResourceAsXML(name);
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
    @Override
    public String getResourceAsXML(final String name)
    {
        AppResourceIFace appResource = getResource(name);
        
        if (appResource != null && appResource instanceof SpAppResource)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            SpAppResource              appRes  = (SpAppResource)appResource;
            try
            {
                if (appRes.getSpAppResourceId() != null)
                {
                    session.attach(appRes);
                }
                
                if (appRes.getMimeType() != null && appRes.getMimeType().equals("text/xml"))
                {
                    try
                    {
                        return appRes.getDataAsString();
    
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        throw new RuntimeException(ex);
                    }
                }
                // else
                throw new RuntimeException("MimeType was not 'text/xml'");
                
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
     * @see edu.ku.brc.af.core.AppContextMgr#putResourceAsXML(java.lang.String, java.lang.String)
     */
    @Override
    public void putResourceAsXML(String name, String xmlStr)
    {
        SpAppResourceDir appResourceDef = null;
        SpAppResource    appRes         = null;
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            // Do Not Check Back implicitly
            AppResourceIFace appResource = getResource(name, false);
            if (appResource == null)
            {
                appResource = backStopAppResMgr.getSpAppResource(name);
                
                if (session != null)
                {
                    List<?> appResDefList = session.getDataList( "From SpAppResourceDir where specifyUserId = "+user.getSpecifyUserId());
                    appResourceDef = find(appResDefList, user, Collection.getCurrentCollection(), Discipline.getCurrentDiscipline());
                    if (appResourceDef == null)
                    {
                        appResourceDef = new SpAppResourceDir();
                        appResourceDef.initialize();
                        appResourceDef.setCollection(Collection.getCurrentCollection());
                        appResourceDef.setDiscipline(Discipline.getCurrentDiscipline());
                        appResourceDef.setDisciplineType(Discipline.getCurrentDiscipline().getDiscipline());
                        appResourceDef.setSpecifyUser(SpecifyUser.getCurrentUser());
                        appResourceDef.setUserType(SpecifyUser.getCurrentUser().getUserType());
                        appResourceDef.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
                    }
                    
                    if (appResource instanceof SpAppResource)
                    {
                        try
                        {
                            appRes = (SpAppResource)((SpAppResource)appResource).clone();
                            appRes.getSpAppResourceDirs().add(appResourceDef);
                            appResourceDef.getSpPersistedAppResources().add(appRes);
                            
                        } catch (CloneNotSupportedException ex)
                        {
                            ex.printStackTrace();
                            return;
                        }
                        
                    } else
                    {
                        log.error("Can't cast from AppResourceIFace to AppResource for["+appResource.getName()+"]" );
                        return;
                    }
                    
                } else
                {
                    return;
                }
            } else
            {
                appRes  = (SpAppResource)appResource;
            }
    
            if (appResource instanceof SpAppResource)
            {
                if (appRes.getSpAppResourceId() != null)
                {
                    session.attach(appRes);
                }
                
                if (appRes.getMimeType() != null && appRes.getMimeType().equals("text/xml"))
                {
                    try
                    {
                        session.beginTransaction();
                        if (appResourceDef != null)
                        {
                            session.save(appResourceDef);
                        }
                        appRes.setTimestampModified(new Timestamp(System.currentTimeMillis()));
                        appRes.setModifiedByAgent(Agent.getUserAgent());
                        appRes.setDataAsString(xmlStr);
                        session.save(appRes);
                        session.commit();
                        
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        throw new RuntimeException(ex);
                    }
                }
                
            } else
            {
                log.debug("Couldn't find ["+name+"]");
            }
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



    //----------------------------------------------------------------
    //-- Inner Classes
    //----------------------------------------------------------------
    class AppResourceMgr
    {
        protected File locationDir;
        protected Hashtable<String, SpAppResource> appResources = null;

        public AppResourceMgr(final File file)
        {
            locationDir = file;
            appResources = new Hashtable<String, SpAppResource>();
            init(locationDir);

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
         * Reads in the App Resource for a disciplineType
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
                        for ( Iterator<?> i = root.elementIterator( "file" ); i.hasNext(); )
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

                                if (debug) log.debug("Adding ["+name+"] ["+resFile.getAbsolutePath()+"]");
                                
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
        public List<SpAppResource> getSpAppResources()
        {
            return Collections.list(appResources.elements());
        }

    }

    
    
    /**
     * Returns a Default Object from Prefs if there is one.
     */
    @SuppressWarnings("cast")
    public PickListItemIFace getDefaultPickListItem(final String pickListName, final String title)
    {
        PickListItemIFace dObj        = null;
        Collection        catSeries   = Collection.getCurrentCollection();
        String            prefName    = (catSeries != null ? catSeries.getIdentityTitle() : "") + pickListName + "_DefaultId";
        AppPreferences    appPrefs    = AppPreferences.getRemote();
        String            idStr       = appPrefs.get(prefName, null);
        
        if (StringUtils.isNotEmpty(idStr))
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            dObj = (PickListItemIFace)session.get(PickListItem.class, Integer.valueOf(idStr));
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
                for (PickListItem itm : pickList.getPickListItems())
                {
                    itm.getTitle();
                }
                list.addAll(pickList.getItems());
                ChooseFromListDlg<PickListItemIFace> plDlg = new ChooseFromListDlg<PickListItemIFace>(null, 
                        UIRegistry.getLocalizedMessage("CHOOSE_DEFAULT_OBJECT", title), list);
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
            
            throw new RuntimeException("PickList name["+pickListName+"] doesn't exist.");
            
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
    @SuppressWarnings("cast")
    public FormDataObjIFace getDefaultObject(final Class<?> classObj, 
                                             final String prefPrefix,
                                             final String title,
                                             final boolean ask, 
                                             boolean useAllItems)
    {
        Collection       collection  = Collection.getCurrentCollection();
        FormDataObjIFace dObj        = null;
        String           prefName    = (collection != null ? collection.getIdentityTitle() : "") + prefPrefix + "_DefaultId";
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
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
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
                    session.close();
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
                    ViewBasedSearchDialogIFace srchDlg = UIRegistry.getViewbasedFactory().createSearchDialog(null, classObj.getSimpleName()+"Search");
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
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            dObj = (FormDataObjIFace)session.get(classObj, Integer.valueOf(idStr));
            session.close();
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
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss zzz");
        
        sb.append(sdf.format(Calendar.getInstance().getTime())+"\n");
        sb.append(Specify.getSpecify().getAppBuildVersion()+"\n");
        
        SpecifyUser spUser = SpecifyUser.getCurrentUser();
        if (spUser != null)
        {
            sb.append(spUser.toString() + "\n");
        }
        Agent uAgent = Agent.getUserAgent();
        if (uAgent != null)
        {
            sb.append(uAgent.toString() + "\n");
            if (StringUtils.isNotEmpty(uAgent.getEmail()))
            {
                sb.append(uAgent.getEmail() + "\n");
            }
            
            //if (uAgent.getAddresses())
            
            Division div = uAgent.getDivision();
            if (div != null)
            {
                Institution inst = div.getInstitution();
                if (inst != null)
                {
                    sb.append(inst.toString() + "\n");
                }
                sb.append(div.toString() + "\n");
                sb.append(uAgent.toString() + "\n");
            }
        }

        Collection collection = Collection.getCurrentCollection();
        if (collection != null)
        {
            sb.append(collection.toString() + "\n");
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
        String javaVersionPropName = "java.version";

        String prefsJavaVersion  = AppPreferences.getLocalPrefs().get(javaVersionPropName, null);
        String systemJavaVersion = System.getProperty("java.version");
        
        boolean isNewVersion = StringUtils.isEmpty(prefsJavaVersion) || 
                               StringUtils.isEmpty(systemJavaVersion) ||
                               !prefsJavaVersion.equals(systemJavaVersion);
        if (isNewVersion)
        {
            AppPreferences.getLocalPrefs().put(javaVersionPropName, System.getProperty("java.version"));
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


    
}

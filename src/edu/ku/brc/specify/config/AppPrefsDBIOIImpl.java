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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.prefs.BackingStoreException;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceData;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class is responsible for performing the "database" based IO for the prefs.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class AppPrefsDBIOIImpl implements AppPrefsIOIFace
{
    protected static final Logger log       = Logger.getLogger(AppPrefsDBIOIImpl.class);
    protected static final String PREF_NAME = "preferences";
    
    protected AppPreferences    appPrefsMgr      = null;
    protected SpAppResourceDir  spAppResourceDir = null;
    protected SpAppResource     spAppResource    = null;
    protected boolean           found            = false;
    protected String            xmlTitle         = "Remote User Prefs";
    
    /**
     * Constructor.
     */
    public AppPrefsDBIOIImpl()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#setAppPrefsMgr(edu.ku.brc.af.prefs.AppPreferences)
     */
    public void setAppPrefsMgr(final AppPreferences appPrefsMgr)
    {
        this.appPrefsMgr = appPrefsMgr;
    }
    
    /**
     * Check to make sure we have prefs.
     */
    protected void checkForAppPrefs()
    {
        if (appPrefsMgr == null)
        {
            throw new RuntimeException("The AppPreferences is null for " + this.getClass().getCanonicalName());
        }
        
        load();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#isAvailable()
     */
    @Override
    public boolean isAvailable()
    {
        return found;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#exists(edu.ku.brc.af.prefs.AppPreferences)
     */
    public boolean exists()
    {
        load(); // throws exception on error
        return found;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#lastSavedDate(edu.ku.brc.af.prefs.AppPreferences)
     */
    public Date lastSavedDate()
    {
        load(); // throws exception on error
        
        return spAppResource.getTimestampModified();
    }
    
    /**
     * @param session
     * @param specifyAppContext
     * @return
     */
    protected SpAppResourceDir createResDir(final DataProviderSessionIFace session, final SpecifyAppContextMgr specifyAppContext)
    {
        return specifyAppContext.getAppResDir(session, AppContextMgr.getInstance().getClassObject(SpecifyUser.class), null, null, "Prefs", false, "Prefs", true);
    }
    
    /**
     * @return
     */
    protected SpAppResource createAndInitResource()
    {
        SpAppResource appRes = new SpAppResource();
        appRes.initialize();
        
        appRes.setName(PREF_NAME);
        appRes.setLevel((short)3); // TODO WHAT LEVEL IS USER???????
        appRes.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        
        return appRes;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#load(edu.ku.brc.af.prefs.AppPreferences)
     */
    public void load()
    {
        if (spAppResource == null && appPrefsMgr != null)
        {
            //log.debug("loading creating Properties");
            Properties properties = new Properties(); // must be done fist thing
            appPrefsMgr.setProperties(properties);

            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                if (session != null)
                {
                    SpecifyAppContextMgr specifyAppContext = (SpecifyAppContextMgr)AppContextMgr.getInstance();
                    
                    SpAppResourceData appData = null;
                    spAppResourceDir = createResDir(session, specifyAppContext);
                    if (spAppResourceDir.getSpAppResourceDirId() == null)
                    {
                        spAppResource = createAndInitResource();

                        appData = new SpAppResourceData();
                        appData.initialize();
                        appData.setSpAppResource(spAppResource);
                        spAppResource.getSpAppResourceDatas().add(appData);
                        
                        spAppResourceDir.getSpPersistedAppResources().add(spAppResource);
                        spAppResource.setSpAppResourceDir(spAppResourceDir);
                        
                    } else
                    {
                        for (SpAppResource appRes : spAppResourceDir.getSpPersistedAppResources())
                        {
                            if (appRes.getName().equals(PREF_NAME))
                            {
                                spAppResource = appRes;
                                appData       = spAppResource.getSpAppResourceDatas().iterator().next();
                                break;
                            }
                        }
                        
                        if (spAppResource == null)
                        {
                            log.error("Couldn't find Prefs object");
                            UIRegistry.showLocalizedError(String.format("Couldn't find '%s'. Application will exit.", spAppResourceDir != null ? spAppResourceDir.getTitle() : "(Unknown)")); // I18N
                            session.close();
                            DBConnection.shutdown();
                            System.exit(0);
                        }
                    }
                    
                    if (appData == null)
                    {
                        throw new RuntimeException("AppResource pref name["+PREF_NAME+"] has not AppResourceData object.");
                    }
                    
                    found = true;
                    
                    if (appData.getData() != null) // the very first time it might be null (empty)
                    {
                        properties.load(new ByteArrayInputStream(appData.getData()));
                        
                        /*System.out.println("--------------------------------------Loading--------------------------------------");
                        TreeSet<Object> sortedKeys = new TreeSet<Object>(properties.keySet());
                        for (Object key : sortedKeys)
                        {
                            System.out.println(String.format("[%s][%s]", key, properties.get(key)));
                        }
                        System.out.println("----------------------------------------------------------------------------------");*/
                    }
                }
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AppPrefsGlobalDBIOIImpl.class, ex);
                ex.printStackTrace();
                log.error(ex);
                
            } finally 
            {
                 if (session != null)
                 {
                     session.close();
                 }
            }
        }    
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#save(edu.ku.brc.af.prefs.AppPreferences)
     */
    public void flush() throws BackingStoreException
    {        
        if (spAppResource != null && spAppResourceDir != null && appPrefsMgr.isChanged())
        {
            if (spAppResource.getSpAppResourceDatas().size() == 0)
            {
                throw new RuntimeException("AppResource has no AppResourceData object!");
            }
            
            try
            {
                // Clone current Properties
                Properties currentProps = (Properties)appPrefsMgr.getProperties().clone();
                //System.out.println(currentProps.getProperty("rodsx"));
                
                // Load existing.
                spAppResource = null;
                load();
                Properties dbProps = (Properties)appPrefsMgr.getProperties();
                
                // Merge the properties
                for (Object key : currentProps.keySet())
                {
                    dbProps.put(key, currentProps.get(key));
                }
                
                for (Object key : new HashSet<Object>(dbProps.keySet()))
                {
                    if (currentProps.get(key) == null)
                    {
                        dbProps.remove(key);
                    }
                }
                
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                dbProps.store(byteOut, xmlTitle);
                appPrefsMgr.setChanged(false);
                
                SpAppResourceData apData = spAppResource.getSpAppResourceDatas().iterator().next();

                if (apData != null)
                {
                    apData.setData(byteOut.toByteArray());
                    
                } else
                {
                    log.error("AppResourceData shouldn't be null!");
                }
                byteOut.close();
                
                /*System.out.println("--------------------------------------Saving--------------------------------------");
                TreeSet<Object> sortedKeys = new TreeSet<Object>(dbProps.keySet());
                for (Object key : sortedKeys)
                {
                    System.out.println(String.format("[%s][%s]", key, dbProps.get(key)));
                }
                System.out.println("----------------------------------------------------------------------------------");
                */
                
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    session.beginTransaction();
                    
                    if (spAppResource.getSpecifyUser() == null)
                    {
                        SpecifyUser globalPrefUser = new SpecifyUser();
                        globalPrefUser.initialize();
                        globalPrefUser.setName("__GLOBAL_PREFS_USER");
                        globalPrefUser.setPassword(Long.toString(System.currentTimeMillis()));
                        session.saveOrUpdate(globalPrefUser);
                        spAppResource.setSpecifyUser(globalPrefUser);
                    }
                    
                    session.saveOrUpdate(spAppResourceDir);
                    session.saveOrUpdate(spAppResource);
                    session.commit();
                    
                } catch (Exception ex)
                {
                    if (session != null) session.rollback();
                    ex.printStackTrace();
                    
                } finally
                {
                    if (session != null) session.close();
                }
                
                //DataModelObjBase.save(true, spAppResourceDir, spAppResource);
                
            } catch (IOException ex)
            {
                throw new BackingStoreException(ex);
            }
        } else
        {
            log.error("Number of ResourceData objects: "+spAppResource.getSpAppResourceDatas().size());
        }
    }
}

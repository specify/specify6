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
package edu.ku.brc.af.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.BackingStoreException;

import edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace;

/**
 * This class is responsible for performing the "local" disk based IO for the prefs.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class AppPrefsDiskIOIImpl implements AppPrefsIOIFace
{
    protected AppPreferences appPrefsMgr = null;
    
    /**
     * Constructor.
     */
    public AppPrefsDiskIOIImpl()
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#setAppPrefsMgr(edu.ku.brc.af.prefs.AppPreferences)
     */
    public void setAppPrefsMgr(AppPreferences appPrefsMgr)
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
    }
    
    /**
     * Check to make sure there is a path.
     */
    protected void checkPath()
    {
        checkForAppPrefs();
        
        if (appPrefsMgr.getDirPath() == null || appPrefsMgr.getDirPath().length() == 0)
        {
            throw new RuntimeException("The directory path for the prefs ["+appPrefsMgr.getDirPath()+"] ["+appPrefsMgr.getLocalFileName()+"] cannot be empty!");
        } 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#exists(edu.ku.brc.af.prefs.AppPreferences)
     */
    public boolean exists()
    {
        checkPath(); // throws exception on error
        
        File file = new File(appPrefsMgr.getDirPath() + File.separator + appPrefsMgr.getLocalFileName());
        return file.exists();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#lastSavedDate(edu.ku.brc.af.prefs.AppPreferences)
     */
    public Date lastSavedDate()
    {
        checkPath(); // throws exception on error
        
        File file = new File(appPrefsMgr.getDirPath() + File.separator + appPrefsMgr.getLocalFileName());
        return file.exists() ? new Date(file.lastModified()) : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#load(edu.ku.brc.af.prefs.AppPreferences)
     */
    public void load()
    {
        checkPath(); // throws exception on error

        if (appPrefsMgr.getProperties() != null)
        {
            if (!appPrefsMgr.getDirPath().equals(appPrefsMgr.getDirPath()))
            {
                throw new RuntimeException("The AppPrefs have already been loaded! ["+appPrefsMgr.getDirPath()+"]");
            }
        } else
        {
            String fullName = appPrefsMgr.getDirPath() + File.separator + appPrefsMgr.getLocalFileName();

            Properties properties = new Properties();
            appPrefsMgr.setProperties(properties);
            
            if ((new File(fullName)).exists())
            {
                try
                {
                    properties.load(new FileInputStream(fullName));

                } catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
            appPrefsMgr.setProperties(properties);
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPreferences.AppPrefsIOIFace#save(edu.ku.brc.af.prefs.AppPreferences)
     */
    public void flush() throws BackingStoreException
    {
        checkPath(); // throws exception on error
        
        if (appPrefsMgr.getProperties() == null)
        {
            throw new RuntimeException("AppPrefs properties has not been initialized or loaded. "+appPrefsMgr.getLocalFileName());
        }
        if (appPrefsMgr.isChanged())
        {
            try
            {
                String fullName = appPrefsMgr.getDirPath() + File.separator + appPrefsMgr.getLocalFileName();
                appPrefsMgr.getProperties().store(new FileOutputStream(fullName), "User Prefs");
                appPrefsMgr.setChanged(false);
                
            } catch (IOException ex)
            {
                throw new BackingStoreException(ex);
            }
        }
    }
}

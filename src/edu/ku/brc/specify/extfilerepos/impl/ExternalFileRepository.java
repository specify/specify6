/* Filename:    $RCSfile: ExternalFileRepository.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:28 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.extfilerepos.impl;

import java.io.File;
import javax.swing.*;

import edu.ku.brc.specify.extfilerepos.*;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.apache.commons.logging.*;
import org.apache.commons.configuration.*;
import org.apache.commons.configuration.PropertiesConfiguration;
import java.util.NoSuchElementException;
import edu.ku.brc.specify.helpers.AskForDirectory;
import java.util.*;

public class ExternalFileRepository implements ExternalFileRepositoryIFace
{
    private static final String EXTERNAL_FILE_REPOS_PATH = "external.file.repository.path";
    
    private static Log log = LogFactory.getLog(ExternalFileRepository.class);
    
    protected static ExternalFileRepository singleton = null;
    
    
    protected File reposDir = null;
    
    /**
     * 
     * @param aPath
     */
    protected ExternalFileRepository(String aPath)
    {
        //reposDir = new File(aPath); 
    }
    
    /**
     * 
     * @return the singleton for the repository
     */
    public static ExternalFileRepository getInstance()
    {
        return singleton;
    }
    
    protected static String getValidPath()
    {
        return null;
    }
     
    /**
     * 
     * @param aPath the directory path to check
     * @return return null if no errors or returns a string describing the error
     */
    protected static String checkPath(String aPath)
    {
        String errMsg = null;
        File   dir    = new File(aPath);
        if (!dir.exists())
        {
            errMsg = "Directory " + aPath + " doesn`t exist."; // XXX LOCALIZE
        } else if (!dir.isDirectory())
        {
            errMsg = "Path " + aPath + " is not a directory"; // XXX LOCALIZE         
        }
        return errMsg;
    }

   
    /**
     * 
     * @param aAskForDir
     * @param aConfig
     * @throws NoSuchElementException
     */
    public static void createInstance(AskForDirectory aAskForDir, 
                                      org.apache.commons.configuration.Configuration aConfig) throws NoSuchElementException
    {
        String  msg   = null;
        String  path  = aConfig.getString(EXTERNAL_FILE_REPOS_PATH);
        if (path == null)
        {
            msg = "Properties file is missing setting `" + EXTERNAL_FILE_REPOS_PATH + "`"; // XXX LOCALIZE
            if (aAskForDir != null)
            {
                aAskForDir.showErrorDialog(msg);
            }            
            log.fatal(msg);
        }
        
        if (aAskForDir != null)
        {
            // the dir prop didn't exist so ask for one
            if (msg != null || path == null)
            {
                path = aAskForDir.getDirectory();
                aConfig.addProperty(EXTERNAL_FILE_REPOS_PATH, path);
            } else
            {
                // Property existed, now check to see if it is valid
                msg = checkPath(path);
                if (msg == null)
                {
                    aAskForDir.showErrorDialog(msg);
                    path = aAskForDir.getDirectory();
                    aConfig.addProperty(EXTERNAL_FILE_REPOS_PATH, path);
                }
            }
        } else 
        {
            msg = checkPath(path);
            if (msg != null)
            {
                path = null;
            }
        }
        
        if (path == null)
        {
            throw new NoSuchElementException(msg);
        }

        singleton = new ExternalFileRepository(path);
    }
    
    /**
     * Copies file into the repository
     * @param aName Logical name of file (must be unique
     * @param aDesc a description of the file
     * @param aMimeType the mime type of the file, it can be null then it will assume it is a octet-stream
     *                   (see http://www.iana.org/assignments/media-types/application/)
     * @param aLocation the external file location
     */
    public boolean put(String aName, String aDesc, String aMimeType, String aLocation)
    {
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            
            ExternalFileEntry entry = new ExternalFileEntry();
            entry.setName(aName);
            entry.setDescr(aDesc);
            entry.setMimeType(aMimeType);
            entry.setCreated(new Date());
        
            session.save(entry);
        
            HibernateUtil.commitTransaction();
            
        } catch (Exception ex)
        {
            log.error("Error saving ExternalFileEntry to database.", ex);
            return false;
        }
        
        return true;
    }
    
    /**
     * Removes a ExternalFileEntry
     * @param aId
     * @return
     */
    public boolean remove(Long aId)
    {
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            
            ExternalFileEntry entry = new ExternalFileEntry();
            entry.setId(aId);
        
            session.delete(entry);
        
            HibernateUtil.commitTransaction();
            
        } catch (Exception ex)
        {
            log.error("Error saving ExternalFileEntry to database.", ex);
            return false;
        }
        return true;
    }
    

    
    /**
     * Returns an image by logical name
     * @param aLogicalName
     * @return the image or null if it wasn't found
     */
    public File get(String aLogicalName)
    {
        return null;
    }
    
    /**
     * Returns an image by records ID
     * @param aImageID
     * @return the image or null if it wasn't found
     */
    public File get(long aImageID)
    {
        return null;
    }
    
}

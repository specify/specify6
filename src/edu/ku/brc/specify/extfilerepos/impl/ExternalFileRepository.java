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
package edu.ku.brc.specify.extfilerepos.impl;

import java.io.File;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import edu.ku.brc.helpers.AskForDirectory;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.extfilerepos.ExternalFileRepositoryIFace;

/**
 * Implementation of the ExternalFileRepositoryIFace for a local directory/disk based external repository
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 */
public class ExternalFileRepository implements ExternalFileRepositoryIFace
{
    private static final String EXTERNAL_FILE_REPOS_PATH = "external.file.repository.path";
    
    private static final Logger log = Logger.getLogger(ExternalFileRepository.class);
    
    protected static final ExternalFileRepository instance = new ExternalFileRepository();
    
    
    protected File reposDir = null;
    
    /**
     * 
     * @param aPath path of the repository
     */
    protected ExternalFileRepository()
    {
     }
    
    /**
     * 
     * @return the instance for the repository
     */
    public static ExternalFileRepository getInstance()
    {
        return instance;
    }
    
    /**
     * 
     * @return the valid path to the repository (not implemented)
     */
    protected static String getValidPath()
    {
        return null;
    }
     
    /**
     * 
     * @param path the directory path to check
     * @return return null if no errors or returns a string describing the error
     */
    protected static String checkPath(final String path)
    {
        String errMsg = null;
        File   dir    = new File(path);
        if (!dir.exists())
        {
            errMsg = "Directory " + path + " doesn`t exist."; // XXX LOCALIZE
        } else if (!dir.isDirectory())
        {
            errMsg = "Path " + path + " is not a directory"; // XXX LOCALIZE         
        }
        return errMsg;
    }

   
    /**
     * 
     * @param askForDir Can be null, a class that is used as the UI to get a valid directory path for the repository
     * @param config config object
     * @throws NoSuchElementException throws expection if ultimately no path is found or given
     */
    public static void createInstance(final AskForDirectory askForDir, 
                                      final org.apache.commons.configuration.Configuration config) throws NoSuchElementException
    {
        String  msg   = null;
        String  path  = config.getString(EXTERNAL_FILE_REPOS_PATH);
        if (path == null)
        {
            msg = "Properties file is missing setting `" + EXTERNAL_FILE_REPOS_PATH + "`"; // XXX LOCALIZE
            if (askForDir != null)
            {
                askForDir.showErrorDialog(msg);
            }            
            log.fatal(msg);
        }
        
        if (askForDir != null)
        {
            // the dir prop didn't exist so ask for one
            if (msg != null || path == null)
            {
                path = askForDir.getDirectory();
                config.addProperty(EXTERNAL_FILE_REPOS_PATH, path);
            } else
            {
                // Property existed, now check to see if it is valid
                msg = checkPath(path);
                if (msg == null)
                {
                    askForDir.showErrorDialog(msg);
                    path = askForDir.getDirectory();
                    config.addProperty(EXTERNAL_FILE_REPOS_PATH, path);
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

        instance.reposDir = new File(path);
    }
    
    //--------------------------------------
    // ExternalFileRepositoryIFace Interface
    //--------------------------------------
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.extfilerepos.ExternalFileRepositoryIFace#put(java.lang.String, java.lang.String, java.lang.String, java.lang.String, edu.ku.brc.specify.datamodel.Agent)
     */
    public boolean put(final String fileName, 
                       final String mimeType, 
                       final String externalLocation, 
                       final String remarks,
                       final Agent  agent)
    {
        /*
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            
            ExternalFile entry = new ExternalFile();
            entry.setFileName(fileName);
            entry.setRemarks(remarks);
            entry.setMimeType(mimeType);
            entry.setExternalLocation(externalLocation);
            entry.setCreatedByAgent(agent);
            entry.setTimestampCreated(new Date());
            entry.setTimestampModified(new Date());
        
            session.save(entry);
        
            HibernateUtil.commitTransaction();
            
        } catch (Exception ex)
        {
            log.error("Error saving ExternalFileEntry to database.", ex);
            return false;
        }
        */
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.extfilerepos.ExternalFileRepositoryIFace#remove(java.lang.Integer)
     */
    public boolean remove(final Integer id)
    {
        /*
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            
            ExternalFile entry = new ExternalFile();
            entry.setExternalFileId(id);
        
            session.delete(entry);
        
            HibernateUtil.commitTransaction();
            
        } catch (Exception ex)
        {
            log.error("Error saving ExternalFileEntry to database.", ex);
            return false;
        }*/
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.extfilerepos.ExternalFileRepositoryIFace#get(java.lang.String)
     */
    public File get(final String fileName)
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.extfilerepos.ExternalFileRepositoryIFace#get(long)
     */
    public File get(final long imageID)
    {
        return null;
    }
    
}

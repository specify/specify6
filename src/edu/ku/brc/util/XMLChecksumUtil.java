/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.util;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.io.FileUtils.checksumCRC32;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.helpers.XMLHelper;

/**
 * This class is used for creating a Checksum properties file. At this time it checksums all the files in the config directory.
 * But that shouldn't imply that all files MUST be checksumed upon loading although we should probably do that because
 * we really view those files as being read-only.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Mar 1, 2007
 *
 */
public class XMLChecksumUtil
{
    //private static final Logger log = Logger.getLogger(XMLSignatureUtil.class);
    
    protected static final String checksumFileName = "checksum.ini";
        
    /**
     * Strips the leading path up to "config".
     * @param file the file 
     * @return the full path after and including the config
     */
    protected static String getRelativeName(final File file)
    {
        String absName = file.getAbsolutePath();
        int inx = absName.indexOf("config");
        if (inx != -1)
        {
            return absName.substring(inx, absName.length());
        }
        return null;
    }
    
    /**
     * Returns whether the checksum in the file matched the checksum of the file.
     * @param file the File object to be checked
     * @return true if check sum matches, false if not.
     */
    public static boolean checkSignature(final File file)
    {
        Properties checksumProps = new Properties();
        File       checksumFile  = XMLHelper.getConfigDir(checksumFileName);
        if (checksumFile.exists())
        {
            try
            {
                checksumProps.load(new FileInputStream(checksumFile));
                return checkSignature(checksumProps, file);
                
            } catch (IOException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(XMLChecksumUtil.class, ex);
                throw new RuntimeException("Couldn't locate Checksum file ["+checksumFile.getAbsolutePath()+"]");
            }
        } else
        {
            JOptionPane.showMessageDialog(null, getResourceString("CHECKSUM_MSG"), getResourceString("CHECKSUM_TITLE"), JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        return false;
    }
    
    /**
     * Returns whether the checksum in the file matched the checksum of the file.
     * @param checksumProps the mapping of files to their checksum
     * @param file the File object to be checked
     * @return true if check sum matches, false if not.
     */
    protected static boolean checkSignature(final Properties checksumProps, final File file) throws IOException
    {
        String relativeName = getRelativeName(file);
        if (StringUtils.isNotEmpty(relativeName))
        {
            String checksumStr = checksumProps.getProperty(relativeName);
            if (StringUtils.isNotEmpty(checksumStr))
            {
                long checkSum = checksumCRC32(file);
                
                long newCheckSum = Long.parseLong(checksumStr);
                //System.out.println(file.getName()+" ["+checkSum+"]["+newCheckSum+"]");
                return checkSum == newCheckSum; 
            }
        } else
        {
            throw new RuntimeException("The file isn't in the config directory ["+file.getAbsolutePath()+"]");
        }
        return false;
    }
    
    /**
     * Creates the Checksum properties file in the config directory.
     * @param files the list of files to be checksumed
     */
    public static void createChecksumProps(final File[] files)
    {
        Properties checksumProps = new Properties();
        File       checksumFile  = XMLHelper.getConfigDir(checksumFileName);
        try
        {        
            createChecksumProps(checksumProps, files);
            checksumProps.store(new FileOutputStream(checksumFile), "Checksum Properties");
    
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(XMLChecksumUtil.class, ex);
            throw new RuntimeException("Couldn't locate Checksum file ["+checksumFile.getAbsolutePath()+"]");
        }

    }
    
    /**
     * Fills in the properties with a relative path name and the checksum.
     * @param checksumProps the properties that will hold all the checksums
     * @param files the files to be checksumed
     * @throws IOException
     */
    protected static void createChecksumProps(final Properties checksumProps, final File[] files) throws IOException
    {
        for (File file : files)
        {
            String relativeName = getRelativeName(file);
            if (StringUtils.isNotEmpty(relativeName))
            {
                String checksumStr = checksumProps.getProperty(relativeName);
                if (StringUtils.isEmpty(checksumStr))
                {
                    checksumProps.put(relativeName, Long.toString(checksumCRC32(file)));
                    
                } else
                {
                    throw new RuntimeException("Duplicate checksum name ["+relativeName+"]"); 
                }
            } else
            {
                throw new RuntimeException("The file isn't in the config directory ["+file.getAbsolutePath()+"]");
            }
        }
    }
    
    /**
     * Build the entire checksum file. 
     */
    @SuppressWarnings("unchecked")
    public static void createChecksum()
    {
        File[] files = FileUtils.convertFileCollectionToFileArray(FileUtils.listFiles(XMLHelper.getConfigDir(null), new String[] {"xml"}, true));
        createChecksumProps(files);
    }
}

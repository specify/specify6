/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks.subpane;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.DataCacheIFace;

/**
 * Cache for JasperReports. This class cal refresh the actual reports from the AppResourceManager and 
 * also check to see if they need to be recompiled.
 * 
 * @author rod
 *
 * @code_status Compete
 *
 * Apr 29, 2007
 *
 */
public class JasperReportsCache implements DataCacheIFace
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(LabelsPane.class);
    
    private static boolean            reportsCacheWasCleared = false;
    private static JasperReportsCache instance         = new JasperReportsCache();
    private static String             reportsCacheName;
    /**
     * Constructor.
     */
    protected JasperReportsCache()
    {
        // XXX FOR WORKBENCH RELEASE ONLY (Maybe we need to keep this)
        reportsCacheName = "reportsCache_" + UIHelper.getOSType().toString();
    }

    /**
     * @return the instance
     */
    public static JasperReportsCache getInstance()
    {
        return instance;
    }

    /**
     * Returns the path to where the images are located that will be usd in the reports/labels.
     * This path comes from the preference REPORT_IMAGE_PATH
     * 
     * @return a path to the directory where the ijmages are stored
     */
    public static File getImagePath()
    {
        File imageDir;
        
        String imgPath = AppPreferences.getRemote().get("REPORT_IMAGE_PATH", null);
        if (UIHelper.isMacOS()) 
        {
        	String macPath = AppPreferences.getRemote().get("REPORT_IMAGE_PATH_MAC", "");
        	if (StringUtils.isNotEmpty(macPath)) {
        		imgPath = macPath;
        	}
        } else if (UIHelper.isLinux())
        {
        	String linuxPath = AppPreferences.getRemote().get("REPORT_IMAGE_PATH_LINUX", "");
        	if (StringUtils.isNotEmpty(linuxPath)) {
        		imgPath = linuxPath;
        	}
        }
        if (StringUtils.isNotEmpty(imgPath))
        {
            imageDir = new File(imgPath);
        } else
        {
            imageDir = UIRegistry.getAppDataSubDir("report_images", false);
        }
            
        // XXXX RELEASE - This Reference to demo_files will need to be removed
        if (!imageDir.exists())
        {
            imageDir = new File(UIRegistry.getDefaultWorkingPath() + File.separator + "demo_files");
        }
        
        return imageDir;
    }
    
    /**
     * @return
     */
    public static File getCachePath()
    {
        File path = UIRegistry.getAppDataSubDir(reportsCacheName, true); 
        if (path == null)
        {
            String msg = "The reportsCache directory path is empty.";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return path;
    }
    
    /**
     * XXX This really needs to be moved to a more centralized storage
     *
     */
    public static File checkAndCreateReportsCache()
    {
        File path = getCachePath();
        
        // If the JVM version (Major Version) has changed the JasperReports need to be recompiled
        // so we remove all the ".jasper" files so they can be recompiled.
        // XXX isNewJavaVersionAtAppStart should be moved to a more generic place
        if (SpecifyAppContextMgr.isNewJavaVersionAtAppStart() && !reportsCacheWasCleared)
        {
            clearJasperFilesOnly(path);
            
            reportsCacheWasCleared = true;
        }
        
        return path;
    }
    
    /**
     * Clears the compiled files that are JVM dependent.
     * @param cachePath the path to the cache
     */
    protected static void clearJasperFilesOnly(final File cachePath)
    {
        try
        {
            for (Iterator<?> iter = FileUtils.iterateFiles(cachePath, new String[] {"jasper"}, false);iter.hasNext();)
            {
                FileUtils.forceDelete((File)iter.next());
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JasperReportsCache.class, ex);
           log.error(ex);
        }
   }
    
    /**
     * Checks to see if any files in the database need to be copied to the database. A file may not
     * exist or be out of date.
     */
    public static void refreshCacheFromDatabase()
    {
        refreshCacheFromDatabase("jrxml/label");
        refreshCacheFromDatabase("jrxml/report");
        refreshCacheFromDatabase("jrxml/subreport");
    }
    
    /**
     * Checks to see if any files in the database need to be copied to the database. A file may not
     * exist or be out of date.
     */
    protected static void refreshCacheFromDatabase(final String mimeType)
    {
        File cachePath = getCachePath();
        
        Hashtable<String, File> hash = new Hashtable<String, File>();
        for (File f : cachePath.listFiles())
        {
            //log.info("Report Cache File["+f.getName()+"]");
            hash.put(f.getName(), f);  
        }
        
        for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(mimeType))
        {
            // Check to see if the resource or file has changed.
            boolean updateCache = false;
            File fileFromJasperCache = hash.get(ap.getName());
            if (fileFromJasperCache == null)
            {
                updateCache = true;
                
            } else
            {
                Date fileDate = new Date(fileFromJasperCache.lastModified());
                Timestamp apTime = ap.getTimestampModified() == null ? ap.getTimestampCreated() : ap.getTimestampModified();
                updateCache   = apTime == null || fileDate.getTime() < apTime.getTime();
            }
            
            // If it has changed then copy the contents into the cache and delete the compiled file
            // so it forces it to be recompiled.
            if (updateCache)
            {
                File localFilePath = new File(cachePath.getAbsoluteFile() + File.separator + ap.getName());
                try
                {
                    XMLHelper.setContents(localFilePath, ap.getDataAsString());
                    
                    File compiledFile = getCompiledFile(localFilePath);
                    if (compiledFile.exists())
                    {
                        compiledFile.delete();
                    }
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JasperReportsCache.class, ex);
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    /**
     * @param file the jrxml file
     * @return the xxxx.jasper file name (the compiled name)
     */
    protected static File getCompiledFile(final File file)
    {
        String fileName     = file.getName();
        
        String compiledName = FilenameUtils.getBaseName(fileName) + ".jasper";
        
        return new File(getCachePath().getAbsoluteFile() + File.separator + compiledName);
    }
    
    /**
     * @return the reportsCacheWasCleared
     */
    public static boolean isReportsCacheWasCleared()
    {
        return reportsCacheWasCleared;
    }
    

    /**
     * Starts the report creation process
     * @param fileName the XML file name of the report definition
     * @param recrdSet the recordset to use to fill the labels
     */
    public static ReportCompileInfo checkReport(final File file)
    {
        File   cachePath    = getCachePath();
        String fileName     = file.getName();
        File   compiledPath = getCompiledFile(file);

        AppResourceIFace appRes = AppContextMgr.getInstance().getResource(fileName);

        File reportFileFromCache = new File(cachePath.getAbsoluteFile() + File.separator + fileName);

        // Check to see if it needs to be recompiled, if it doesn't need compiling then
        // call "compileComplete" directly to have it start filling the labels
        // otherswise create the compiler runnable and have it be compiled 
        // asynchronously
        
        Timestamp apTime = appRes.getTimestampModified() == null ? appRes.getTimestampCreated() : appRes.getTimestampModified();
        boolean needsCompiling = apTime == null || 
                                 !compiledPath.exists() || 
                                 apTime.getTime() > reportFileFromCache.lastModified();
        
        //log.debug(appRes.getTimestampModified().getTime()+" > "+reportFileFromCache.lastModified() +"  "+(appRes.getTimestampModified().getTime() > reportFileFromCache.lastModified()));
        //log.debug(compiledPath.exists());
        //log.debug("needsCompiling "+needsCompiling);
        return new ReportCompileInfo(reportFileFromCache, compiledPath, needsCompiling);
    }
    
    public static void clearCache() {
        try
        {
            FileUtils.cleanDirectory(getCachePath());
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JasperReportsCache.class, ex);
           log.error(ex);
        }
    }
    //------------------------------------------------------
    // DataCacheIFace Interface
    //------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.util.DataCacheIFace#clear()
     */
    public void clear()
    {
    	clearCache();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.DataCacheIFace#shutdown()
     */
    public void shutdown() throws Exception
    {
        //TODO Auto-generated method stub
    }
}

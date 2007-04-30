/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane;

import java.io.File;
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
    
    private static boolean reportsCacheWasCleared = false;
    
    private static JasperReportsCache instance = new JasperReportsCache();
    
    /**
     * Constructor.
     */
    protected JasperReportsCache()
    {
        // no-op
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
        
        String imgPath = AppPreferences.getLocalPrefs().get("REPORT_IMAGE_PATH", null);
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
            imageDir = new File("demo_files");
        }
        
        return imageDir;
    }
    
    /**
     * @return
     */
    public static File getCachePath()
    {
        File path = UIRegistry.getAppDataSubDir("reportsCache", true); 
        if (path == null)
        {
            String msg = "The reportsCache directory path is empty.";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return path;
    }
    
    /**
     * XXX This really needs to be moved to a more centralized location
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
            for (Iterator iter = FileUtils.iterateFiles(cachePath, new String[] {"jasper"}, false);iter.hasNext();)
            {
                FileUtils.forceDelete((File)iter.next());
            }
        } catch (Exception ex)
        {
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
            
            boolean updateCache = false;
            File file = hash.get(ap.getName());
            if (file == null)
            {
                updateCache = true;
                
            } else
            {
                Date fileDate = new Date(file.lastModified());
                updateCache = fileDate.getTime() < ap.getTimestampModified().getTime();
            }
            
            log.debug("Report Cache File["+ap.getName()+"]  updateCache["+updateCache+"]");
            if (updateCache)
            {
                File localFilePath = new File(cachePath.getAbsoluteFile() + File.separator + ap.getName());
                try
                {
                    XMLHelper.setContents(localFilePath, ap.getDataAsString());
                    
                } catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
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
        String compiledName = FilenameUtils.getBaseName(fileName) + ".jasper";
        File   compiledPath = new File(cachePath.getAbsoluteFile() + File.separator + compiledName);

        AppResourceIFace appRes = AppContextMgr.getInstance().getResource(fileName);

        File reportPath = new File(cachePath.getAbsoluteFile() + File.separator + fileName);
        try
        {
            XMLHelper.setContents(reportPath, appRes.getDataAsString());
            
        } catch (Exception ex)
        {
            log.error(ex);
            throw new RuntimeException(ex);
        }
       

        // Check to see if it needs to be recompiled, if it doesn't need compiling then
        // call "compileComplete" directly to have it start filling the labels
        // otherswise create the compiler runnable and have it be compiled 
        // asynchronously
        boolean needsCompiling = compiledPath.exists() && 
                                 appRes.getTimestampModified().getTime() < compiledPath.lastModified();
        
        return new ReportCompileInfo(reportPath, compiledPath, needsCompiling);
    }
    
    //------------------------------------------------------
    // DataCacheIFace Interface
    //------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.util.DataCacheIFace#clear()
     */
    public void clear()
    {
        try
        {
            FileUtils.cleanDirectory(getCachePath());
            
        } catch (Exception ex)
        {
           log.error(ex);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.DataCacheIFace#shutdown()
     */
    public void shutdown() throws Exception
    {
        // TODO Auto-generated method stub

    }
}

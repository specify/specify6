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
/**
 * 
 */
package edu.ku.brc.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 2, 2009
 *
 */
public class ZipFileHelper
{
    
    private static ZipFileHelper instance = null;
    
    protected Vector<File> dirsToRemoveList = new Vector<File>();
    
    /**
     * 
     */
    public ZipFileHelper()
    {
        super();
        
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() 
            {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        cleanUp();
                    }
                });
                return null;
            }
        });
    }
    
    /**
     * Removes any temp files that were created while unzipping.
     */
    public void cleanUp()
    {
        for (File dir : dirsToRemoveList)
        {
            try 
            {
                if (dir.exists())
                {
                    FileUtils.deleteDirectory(dir);
                }
            } catch (IOException ex)
            {
                System.err.println(ex);
            }
        }
    }

    /**
     * @param zipFile
     * @return
     * @throws ZipException
     * @throws IOException
     */
    public List<File> unzipToFiles(final File zipFile) throws ZipException, IOException
    {
        Vector<File> files = new Vector<File>();
        
        final int bufSize = 65535;
        
        
        File dir = UIRegistry.getAppDataSubDir(Long.toString(System.currentTimeMillis())+"_zip", true);
        dirsToRemoveList.add(dir);
        
        File             outFile = null;
        FileOutputStream fos     = null;
        try
        {
            ZipInputStream zin   = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry       entry = zin.getNextEntry();
            while (entry != null)
            {
                if (zin.available() > 0)
                {
                    outFile = new File(dir.getCanonicalPath() + File.separator + entry.getName());
                    fos     = new FileOutputStream(outFile);
                    
                    byte[] bytes     = new byte[bufSize]; // 64k
                    int    bytesRead = zin.read(bytes, 0, bufSize);
                    while (bytesRead > 0)
                    {
                        fos.write(bytes, 0, bytesRead);
                        bytesRead = zin.read(bytes, 0, bufSize);
                    }
                    
                    files.insertElementAt(outFile.getCanonicalFile(), 0);
                }
                entry = zin.getNextEntry();
            }
            
        } finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }
        return files;
    }

    /**
     * Unzips a a zip file cntaining just one file.
     * @param zipFile the backup file
     * @return the file of the new uncompressed back up file.
     */
    public File unzipToSingleFile(final File zipFile)
    {
        final int bufSize = 102400;
        
        File outFile = null;
        try
        {
            ZipInputStream zin   = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry       entry = zin.getNextEntry();
            if (entry == null)
            {
                return null;
            }
            if (zin.available() == 0)
            {
                return null;
            }
            outFile = File.createTempFile("zip_", "sql");
            FileOutputStream fos = new FileOutputStream(outFile);
            
            byte[] bytes = new byte[bufSize]; // 10K
            int bytesRead = zin.read(bytes, 0, bufSize);
            while (bytesRead > 0)
            {
                fos.write(bytes, 0, bytesRead);
                bytesRead = zin.read(bytes, 0, 100);
            }
            
        } catch (ZipException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ZipFileHelper.class, ex);
            return null; //I think this means it is not a zip file.
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ZipFileHelper.class, ex);
            return null;
        }
        return outFile;
    }

    /**
     * @return the instance
     */
    public static ZipFileHelper getInstance()
    {
        if (instance == null)
        {
            instance = new ZipFileHelper();
        }
        return instance;
    }
    
    
}

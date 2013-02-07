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
package edu.ku.brc.util;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * Static class that is the singleton for the Attachment Manger.
 * @code_status Complete
 * 
 * @author jstewart
 * @author rods
 */
public class AttachmentUtils
{
    private static final Logger           log              = Logger.getLogger(AttachmentUtils.class);
    private static AttachmentManagerIface attachMgr;
    private static Thumbnailer            thumbnailer;
    private static boolean                isConfigForPath = true;
    private static MimetypesFileTypeMap   mimeMap;
    
    static
    {
        MimetypesFileTypeMap mimeMap = (MimetypesFileTypeMap)FileTypeMap.getDefaultFileTypeMap();
        mimeMap.addMimeTypes("image/tif    tif");
        mimeMap.addMimeTypes("image/tif    TIF");
        mimeMap.addMimeTypes("image/jpeg   jpg");
        mimeMap.addMimeTypes("image/jpeg   JPG");
        mimeMap.addMimeTypes("image/jpeg   jpeg");
        mimeMap.addMimeTypes("image/jpeg   JPEG");
        mimeMap.addMimeTypes("image/png    png");
        mimeMap.addMimeTypes("image/png    PNG");
        mimeMap.addMimeTypes("application/vnd.google-earth.kml+xml kml");
    }
    
    /**
     * @return the mimeMap
     */
    public static MimetypesFileTypeMap getMimeMap()
    {
        return mimeMap;
    }

    /**
     * @return the manager
     */
    public static AttachmentManagerIface getAttachmentManager()
    {
        return attachMgr;
    }

    /**
     * @param mgr sets the manager
     */
    public static void setAttachmentManager(final AttachmentManagerIface mgr)
    {
        attachMgr = mgr;
    }
    
    /**
     * @param thumber sets the thumbnailer
     */
    public static void setThumbnailer(Thumbnailer thumber)
    {
        thumbnailer = thumber;
    }
    
    /**
     * The location of the directory in preferences may not exist.
     * 
     * @return whether there is an attachment manager.
     */
    public static boolean isAvailable()
    {
        return attachMgr != null;
    }
    
    /**
     * @return thumbnailer
     */
    public static Thumbnailer getThumbnailer()
    {
        return thumbnailer;
    }
    
    /**
     * @return the isConfigForPath
     */
    public static boolean isConfigForPath()
    {
        return isConfigForPath;
    }

    /**
     * @param isConfigForPath the isConfigForPath to set
     */
    public static void setConfigForPath(boolean isConfigForPath)
    {
        AttachmentUtils.isConfigForPath = isConfigForPath;
    }

    /**
     * @return whether the AttachmentManger can be used.
     */
    protected static boolean isAttachLocOK()
    {
        if (attachMgr != null)
        {
            return true;
            
        } else
        {
            UIRegistry.showLocalizedError("AttachmentUtils.NOT_AVAIL");
        }
        return false;
    }
    
    /**
     * @param attachmentLocation
     * @return
     */
    public static boolean isAttachmentDirMounted(final File attachmentLocation)
    {
        String fullPath = "";
        String statsMsg = "The test to write to the AttachmentLocation [%s] %s.";
        try
        {
            fullPath = attachmentLocation.getCanonicalPath();
            
            if (attachmentLocation.exists())
            {
                if (attachmentLocation.isDirectory())
                {
                    File tmpFile = new File(attachmentLocation.getAbsoluteFile() + File.separator + System.currentTimeMillis() + System.getProperty("user.name"));
                    //log.debug(String.format("Trying to write a file to AttachmentLocation [%s]", tmpFile.getCanonicalPath()));
                    if (tmpFile.createNewFile())
                    {
                        // I don't think I need this anymore
                        FileOutputStream fos = FileUtils.openOutputStream(tmpFile);
                        fos.write(1);
                        fos.close();
                        tmpFile.delete();
                        
                        //log.debug(String.format(statsMsg, fullPath, "succeeded"));
                        
                        return true;
                        
                    } else
                    {
                        log.error(String.format("The Attachment Location [%s] atachment file couldn't be created", fullPath));
                    }
                } else
                {
                    log.error(String.format("The Attachment Location [%s] is not a directory.", fullPath));
                }
            } else
            {
                log.error(String.format("The Attachment Location [%s] doesn't exist.", fullPath));
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        log.debug(String.format(statsMsg, fullPath, "failed"));
        
        return false;
    }
    
    /**
     * @return the actionlistener for when things need to be displayed
     */
    public static ActionListener getAttachmentDisplayer()
    {
        ActionListener displayer = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Object source = e.getSource();
                if (!(source instanceof Attachment) && !(source instanceof ObjectAttachmentIFace<?>))
                {
                    throw new IllegalArgumentException("Passed object must be an Attachment or ObjectAttachmentIFace");
                }
                
                Attachment attachment = (source instanceof Attachment) ? (Attachment)source : ((ObjectAttachmentIFace<?>)source).getAttachment();
                File original = null;
                if (attachment.getId() != null)
                {
                    if (isAttachLocOK())
                    {
                        original = attachMgr.getOriginal(attachment);
                    } else
                    {
                        return;
                    }
                }
                else
                {
                    String origFile = attachment.getOrigFilename();
                    original = new File(origFile);
                }

                String errMsg = null;
                if (original != null && original.exists())
                {
                    try
                    {
                    	openFile(original);
                        
                    } catch (java.io.IOException ex)
                    {
                        errMsg = ex.getMessage();
                        ex.printStackTrace();
                    } catch (Exception ex)
                    {
                        errMsg = ex.getMessage();
                        ex.printStackTrace();
                    }
                } else
                {
                    errMsg = attachMgr.getDirectory() != null ? attachMgr.getDirectory().getAbsolutePath() : "N/A";
                }
                
                if (errMsg != null)
                {
                    UIRegistry.showLocalizedMsg("AttachmentUtils.ANF_TITLE", "AttachmentUtils.ANF", errMsg);
                }
            }
        };
        
        return displayer;
    }
    
    /**
     * @param filename the file name to be checked
     * @return the mime type
     */
    public static String getMimeType(final String filename)
    {
        if (filename == null)
        {
            return null;
        }
        
        return mimeMap.getContentType(filename);
    }
    
    /**
     * @param f the file to be opened
     * @throws Exception
     */
    public static void openFile(final File f) throws Exception
    {
        if (UIHelper.isWindows())
        {
        	HashSet<String> hashSet = new HashSet<String>();
        	Collections.addAll(hashSet, new String[] {"wav", "mp3", "snd", "mid", "aif", "aiff", });
        	String ext = FilenameUtils.getExtension(f.getName()).toLowerCase();
        	if (hashSet.contains(ext))
        	{
        		Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + f.getAbsolutePath());
        		return;
        	}
        }
        Desktop.getDesktop().open(f);
    }
    
    /**
     * @param uri the uri to be opened
     * @throws Exception
     */
    public static void openURI(final URI uri) throws Exception
    {
        Desktop.getDesktop().browse(uri);
    }
    
    /**
     * @param args app args
     */
    public static void main(String[] args)
    {
        String[] filenames = {"hello.txt","a.bmp","b.pdf","hello.gif","blha.tiff","c.jpg",null,"blah.kml"};
    
        for (String name: filenames)
        {
            System.out.println(AttachmentUtils.getMimeType(name));
        }
    }
    

}

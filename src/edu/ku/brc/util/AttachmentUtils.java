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
import java.io.IOException;
import java.net.URI;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
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
        try
        {
            if (attachmentLocation.exists())
            {
                if (attachmentLocation.isDirectory())
                {
                    File tmpFile = new File(attachmentLocation.getAbsoluteFile() + File.separator + System.currentTimeMillis() + System.getProperty("user.name"));
                    if (tmpFile.createNewFile())
                    {
                        // I don't think I need this anymore
                        FileOutputStream fos = FileUtils.openOutputStream(tmpFile);
                        fos.write(1);
                        fos.close();
                        tmpFile.delete();
                        
                        return true;
                        
                    } else
                    {
                        log.error("The Attachment Location ["+attachmentLocation.getCanonicalPath()+"] atachment file couldn't be created");
                    }
                } else
                {
                    log.error("The Attachment Location ["+attachmentLocation.getCanonicalPath()+"] is not a directory.");
                }
            } else
            {
                log.error("The Attachment Location ["+attachmentLocation.getCanonicalPath()+"] doesn't exist.");
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * @return the actionlister for when things need to be displayed
     */
    public static ActionListener getAttachmentDisplayer()
    {
        ActionListener displayer = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Object source = e.getSource();
                if (!(source instanceof Attachment) && !(source instanceof ObjectAttachmentIFace))
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

                try
                {
                    if (original != null && original.exists())
                    {
                        Desktop.getDesktop().open(original);
                    } else
                    {
                        UIRegistry.showLocalizedMsg("AttachmentUtils.ANF_TITLE", "AttachmentUtils.ANF", 
                                attachMgr.getDirectory() != null ? attachMgr.getDirectory().getAbsoluteFile() : "N/A");
                    }
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
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
        
        MimetypesFileTypeMap mimeMap = (MimetypesFileTypeMap)FileTypeMap.getDefaultFileTypeMap();
        mimeMap.addMimeTypes("image/tif    tif");
        mimeMap.addMimeTypes("image/tif    TIF");
        mimeMap.addMimeTypes("image/png    png");
        mimeMap.addMimeTypes("application/vnd.google-earth.kml+xml kml");

        return mimeMap.getContentType(filename);
    }
    
    /**
     * @param f the file to be opened
     * @throws Exception
     */
    public static void openFile(final File f) throws Exception
    {
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

/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

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
    protected static AttachmentManagerIface attachMgr;
    protected static Thumbnailer            thumbnailer;
    
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
     * @return the manager
     */
    public static AttachmentManagerIface getAttachmentManager()
    {
        return attachMgr;
    }
    
    /**
     * @return thumbnailer
     */
    public static Thumbnailer getThumbnailer()
    {
        return thumbnailer;
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
                if (attachment.getId()!= null)
                {
                    original = attachMgr.getOriginal(attachment);
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

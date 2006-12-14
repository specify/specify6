/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;
import org.jdesktop.jdic.filetypes.Association;
import org.jdesktop.jdic.filetypes.AssociationService;

import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class AttachmentUtils
{
    protected static AttachmentManagerIface attachMgr;
    protected static Thumbnailer thumbnailer;
    
    public static void setAttachmentManager(AttachmentManagerIface mgr)
    {
        attachMgr = mgr;
    }
    
    public static void setThumbnailer(Thumbnailer thumber)
    {
        thumbnailer = thumber;
    }
    
    public static AttachmentManagerIface getAttachmentManager()
    {
        return attachMgr;
    }
    
    public static Thumbnailer getThumbnailer()
    {
        return thumbnailer;
    }
    
    public static ActionListener getAttachmentDisplayer()
    {
        ActionListener displayer = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Object source = e.getSource();
                if (!(source instanceof Attachment))
                {
                    throw new IllegalArgumentException("Passed object must be an Attachment");
                }
                Attachment attachment = (Attachment)source;
                File original = null;
                if (attachment.getId()!=null)
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
                    Desktop.open(original);
                }
                catch (DesktopException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        };
        
        return displayer;
    }
    
    public static String getMimeType(String filename)
    {
        if (filename==null)
        {
            return null;
        }
        
        String fileExt = "";
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex != -1)
        {
            fileExt = filename.substring(lastDotIndex+1).toLowerCase();
        }
        
        // XXX Remove for Java 6.0
        if (UIHelper.getOSType() == UIHelper.OSTYPE.MacOSX)
        {
            
            if (fileExt.equals("jpg"))
            {
                return "image/jpeg";
            }
            if (fileExt.equals("png"))
            {
                return "image/png";
            }
            if (fileExt.equals("pdf"))
            {
                return "application/pdf";
            }
            if (fileExt.equals("mpg"))
            {
                return "video/mpeg";
            }
    
            return "application/octet-stream";
        }
        // else
        try
        {
            AssociationService assServ   = new AssociationService();
        	Association        fileAssoc = assServ.getFileExtensionAssociation(fileExt);
            if (fileAssoc==null)
            {
            	return null;
            }
            return fileAssoc.getMimeType();
        }
        catch (Exception e)
        {
        	return null;
        }
    }
    
    public static void main(String[] args)
    {
        String[] filenames = {"hello.txt","a.bmp","b.pdf","c.jpg",null,"blah.kml"};
        for (String name: filenames)
        {
            System.out.println(AttachmentUtils.getMimeType(name));
        }
    }
}

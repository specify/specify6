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
                File original = attachMgr.getOriginal(attachment);

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
        
        AssociationService assServ = new AssociationService();
        String fileExt = "";
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex != -1)
        {
            fileExt = filename.substring(lastDotIndex+1);
        }
        Association fileAssoc = assServ.getFileExtensionAssociation(fileExt);
        return fileAssoc.getMimeType();
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

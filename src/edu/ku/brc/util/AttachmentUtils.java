/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.ui.UICacheManager;
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
                String title = attachment.getIdentityTitle();
                
                Component parent = UICacheManager.get(UICacheManager.TOPFRAME);
                File original = attachMgr.getOriginal(attachment);
				String absolutePath = original.getAbsolutePath();
				ImageIcon icon = new ImageIcon(absolutePath);
                JOptionPane.showMessageDialog(parent, null, title, JOptionPane.INFORMATION_MESSAGE, icon);
            }
        };
        
        return displayer;
    }
}

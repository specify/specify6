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
package edu.ku.brc.specify.tasks.subpane.images;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.ui.*;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 30, 2012
 *
 */
public class FullImagePane extends BaseSubPane implements ImageLoaderListener
{
    private ImageDisplay  imgDisp;
    private ImageDataItem imgDataItem;
    
    private File          imageFile = null;
    /**
     * @param name
     * @param task
     * @param idi
     */
    public FullImagePane(final String name, final Taskable task, final ImageDataItem idi)
    {
        super(name, task);
        this.imgDataItem = idi;
        
        createUI();
    }
    
    public FullImagePane(final String name, final Taskable task, final File imageFile)
    {
        super(name, task);
        this.imageFile   = imageFile;

        createUI();
    }
    
    /**
     * Creates the UI.
     */
    protected void createUI()
    {
        
        CellConstraints cc = new CellConstraints();
        
        imgDisp = new ImageDisplay(1024, 768, false, false);
        imgDisp.setFullImage(true);
        
        final JScrollPane  sp = UIHelper.createScrollPane(imgDisp, true);
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"), this);
        pb.add(sp, cc.xy(1, 1));

        imgDisp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showContextMenu(e);
            }
            @Override
            public void mouseReleased(MouseEvent event) {
                showContextMenu(event);
            }
        });
        
        if (imageFile != null && imageFile.exists())
        {
            ImageIcon imgIcon = new ImageIcon(imageFile.getAbsolutePath());
            imgDisp.setImage(imgIcon);
            FullImagePane.this.repaint();
            
        } else if (imgDataItem != null)
        {
            ImageLoader loader = new ImageLoader(imgDataItem.getImgName(), 
                                                imgDataItem.getMimeType(), 
                                                true, -1, this);
            ImageLoaderExector.getInstance().loadImage(loader);
            imgDisp.setImage(IconManager.getImage("Loading"));
        }
    }

    protected void showContextMenu(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(UIRegistry.getResourceString("AttachmentUtils.OPEN_IN_EV"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    File file = (imageFile != null && imageFile.exists()) ? imageFile : imgDataItem.getLocalFile();
                    if (file != null)
                    {
                        try
                        {
                            AttachmentUtils.openFile(file);
                        } catch (Exception ex)
                        {
                            UIRegistry.showLocalizedMsg("AttachmentUtils.NEV_TITLE", "AttachmentUtils.NEV_MSG");
                        }
                    }
                }
            });
            popup.add(menuItem);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    @Override
    public void imageLoaded(final String    imageName,
                             final String    mimeType,
                             final boolean   doLoadFullImage,
                             final int       scale,
                             final boolean   isError,
                             final ImageIcon imageIcon, 
                             final File      localFile)
    {
        imgDisp.setImage(imageIcon);
        imageFile = localFile;
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.images.ImageLoaderListener#imageStopped(java.lang.String)
     */
    @Override
    public void imageStopped(final String imageName, final boolean doLoadFullImage)
    {
        imgDisp.setImage((ImageIcon)null);
        repaint();
    }    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        return super.aboutToShutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
     */
    @Override
    public void shutdown()
    {
        imgDisp.setImage((ImageIcon)null);
        super.shutdown();
    }
    
    
}

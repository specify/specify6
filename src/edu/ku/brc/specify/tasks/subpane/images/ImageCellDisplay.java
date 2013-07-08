/* Copyright (C) 2012, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.Vector;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.tasks.AttachmentsTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.ImageLoaderExector;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 3, 2012
 *
 */
public class ImageCellDisplay extends ImageDisplay implements ImageLoaderListener
{
    public static final int INFO_BTN        = -1;
    public static final int METADATA_BTN    = -2;
    public static final int SELECTION_WIDTH = 3;
    private final int   margin              = 6;
    
    private static final Color lightBorderColor = new Color(248, 248, 248);

    private Border      nonSelBorder    = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    private BasicStroke stdLineStroke   = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private Color       selectColor     = UIManager.getColor("Table.selectionBackground");
    private RoundRectangle2D.Double rr  = new RoundRectangle2D.Double(0, 0, 0, 0, 10, 10); // Selection Rect
    
    private ImageIcon   infoIcon16      = IconManager.getIcon("InfoIcon", IconManager.STD_ICON_SIZE.Std16);
    private ImageIcon   dataObjIcon     = null;
    
    private Dimension   defaultImgSize  = Thumbnailer.getInstance().getMaxSize();
    private Dimension   imgSize         = new Dimension();
    private Rectangle   infoHitRect     = new Rectangle();
    private Rectangle   dataHitRect     = new Rectangle();
    private Rectangle   mdHitRect       = new Rectangle(); // metadat icon
    
    private Vector<GalleryGridListener> listeners = new Vector<GalleryGridListener>();
    
    private ImageDataItem       imgDataItem = null;
    
    /**
     * @param imgWidth
     * @param imgHeight
     * @param listener
     */
    public ImageCellDisplay(final int imgWidth, 
                            final int imgHeight)
    {
        super(imgWidth, imgHeight, false, false);
        
        
        setBackground(Color.WHITE);
        setOpaque(true);
        
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (infoHitRect != null && infoHitRect.contains(e.getPoint()))
                {
                    for (GalleryGridListener l : listeners)
                    {
                        l.infoSelected(ImageCellDisplay.this, Integer.MIN_VALUE, true, INFO_BTN);
                    }
                } else if (dataHitRect != null && dataHitRect.contains(e.getPoint()))
                {
                    for (GalleryGridListener l : listeners)
                    {
                        l.dataSelected(ImageCellDisplay.this, Integer.MIN_VALUE, true, INFO_BTN);
                    }
                } else if (mdHitRect != null && mdHitRect.contains(e.getPoint()))
                {
                    for (GalleryGridListener l : listeners)
                    {
                        l.infoSelected(ImageCellDisplay.this, Integer.MIN_VALUE, true, METADATA_BTN);
                    }
                } else
                {
                    for (GalleryGridListener l : listeners)
                    {
                        l.itemSelected(ImageCellDisplay.this, -1, true, e.getClickCount());
                    }
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageDisplay#createUI()
     */
    @Override
    protected void createUI()
    {
        super.createUI();
        setBorder(nonSelBorder);
        //setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        
        MouseListener mouseListener = new MouseAdapter() 
        {
              private boolean showIfPopupTrigger(MouseEvent mouseEvent) 
              {
                  if (mouseEvent.isPopupTrigger())
                  {
                      JPopupMenu popupMenu = createContextMenu();
                      if (popupMenu != null && popupMenu.getComponentCount() > 0) 
                      {
                          popupMenu.show(mouseEvent.getComponent(),
                                  mouseEvent.getX(),
                                  mouseEvent.getY());
                          return true;
                      }
                  }
                  return false;
              }
              @Override
              public void mousePressed(MouseEvent mouseEvent) 
              {
                  showIfPopupTrigger(mouseEvent);
              }
              @Override
              public void mouseReleased(MouseEvent mouseEvent) 
              {
                  showIfPopupTrigger(mouseEvent);
              }
        };
        addMouseListener(mouseListener);
    }
    
    /**
     * @param pane
     */
    protected JPopupMenu createContextMenu()
    {
        JPopupMenu popupMenu = new JPopupMenu();
        
        String ttl = getResourceString("ATTCH.EXPORT_ATTACHMENT"); 
        String mnu = getResourceString("ATTCH.EXPORT_ATTACHMENT_MNEU"); 
        UIHelper.createLocalizedMenuItem(popupMenu, ttl, mnu, ttl, true, new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        CommandDispatcher.dispatch(new CommandAction(AttachmentsTask.ATTACHMENTS, AttachmentsTask.EXPORT_CMD, imgDataItem));
                    }
                });
            }
        });
        return popupMenu;
    }


    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(final Graphics gr)
    {
        super.paintComponent(gr);
        
        Graphics2D g2 = (Graphics2D)gr;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int         x = 1;
        int         y = 1;
        int         w = 0;
        int         h = 0;
        
        if (image == null)
        {
            imgSize.setSize(defaultImgSize);
        } else
        {
            imgSize.setSize(image.getWidth(null), image.getHeight(null));
        }
        
        Dimension s    = getSize();
        int       imgW = imgSize.width;
        h = s.height - (margin*2);
        if (imgW < h)
        {
            imgW = h;
        }
        Stroke cacheStroke = g2.getStroke();
        
        w = getSize().width  - SELECTION_WIDTH;
        h = getSize().height - SELECTION_WIDTH;
        
        g2.setColor(imgDataItem != null && imgDataItem.isSelected() ? selectColor : lightBorderColor);
        g2.setStroke(stdLineStroke);
        rr.setRoundRect(x, y, w, h, 10, 10);
        g2.draw(rr);
        
        int imgX = x + w - infoIcon16.getIconWidth();
        int imgY = y + h - infoIcon16.getIconHeight();
        g2.drawImage(infoIcon16.getImage(), imgX, imgY, null);
        
        infoHitRect.x      = imgX;
        infoHitRect.y      = imgY;
        infoHitRect.width  = infoIcon16.getIconWidth();
        infoHitRect.height = infoIcon16.getIconHeight();
        
        if (dataObjIcon != null)
        {
            imgX = x;
            imgY = y + h - dataObjIcon.getIconHeight();
            g2.drawImage(dataObjIcon.getImage(), imgX, imgY, null);

            dataHitRect.x      = imgX;
            dataHitRect.y      = imgY;
            dataHitRect.width  = infoIcon16.getIconWidth();
            dataHitRect.height = infoIcon16.getIconHeight();
        }
        g2.setStroke(cacheStroke);
        
        // Disabling display any text below the image thumbnail
//        if (imgDataItem != null)
//        {
//            g2.setFont(g2.getFont().deriveFont(10.0f));
//            FontMetrics fm = g2.getFontMetrics();
//            
//            int    txtW      =  w - infoIcon16.getIconWidth();
//            String shortName = imgDataItem.getShortName();
//            shortName = GraphicsUtils.clipString(fm, shortName, txtW);
//            
//            int txtY = y + h-fm.getDescent()-2;
//            int rrY  = y + h-fm.getHeight()-2;
//            //if (rr2 == null)
//            {
//                rr2 = new RoundRectangle2D.Double(5, rrY, txtW-4, fm.getHeight(), 10, 10);
//            }
//            g2.setColor(new Color(255, 255, 255, 196));
//            g2.fill(rr2);
//            g2.setColor(Color.BLACK);
//            g2.drawString(shortName, 5, txtY);
//            //GraphicsUtils.drawCenteredString(shortName, g2, (w/2) - infoIcon16.getIconWidth(), y + h-fm.getDescent()-4);
//        }
    }

    /**
     * 
     */
    private void schedRepaint()
    {
        if (!stopLoading)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    //ImageCellDisplay.this.revalidate();
                    ImageCellDisplay.this.repaint();
                    RepaintManager rpm = RepaintManager.currentManager(ImageCellDisplay.this);
                    Rectangle r = getBounds();
                    rpm.addDirtyRegion(ImageCellDisplay.this, r.x, r.y, r.width, r.height);
                    //ImageCellDisplay.this.update();
                    UIRegistry.forceTopFrameRepaint();
                }
            });
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageDisplay#setImage(java.awt.Image)
     */
    @Override
    public synchronized void setImage(final Image newImage)
    {
        super.setImage(newImage);
        schedRepaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageDisplay#setImage(javax.swing.ImageIcon)
     */
    @Override
    public synchronized void setImage(final ImageIcon newImageIcon)
    {
        super.setImage(newImageIcon);
        schedRepaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageDisplay#setNoImage(boolean)
     */
    @Override
    public void setNoImage(final boolean isNoImage)
    {
        super.setNoImage(isNoImage);
        schedRepaint();
    }
    
    /**
     * 
     */
    public void startLoad()
    {
        if (!stopLoading)
        {
            int loadSize = ImageDataItem.STD_ICON_SIZE - (4 * SELECTION_WIDTH);
            ImageLoader imageLoader = new ImageLoader(imgDataItem.getImgName(), imgDataItem.getMimeType(), false, loadSize, this);
            setLoading(true);
            ImageLoaderExector.getInstance().loadImage(imageLoader);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.images.ImageLoaderListener#imagedLoaded(java.lang.String, java.lang.String, boolean, int, boolean, javax.swing.ImageIcon, java.io.File)
     */
    @Override
    public void imageLoaded(final String    imageName,
                             final String    mimeType,
                             final boolean   doLoadFullImage,
                             final int       scale,
                             final boolean   isError,
                             final ImageIcon imageIcon, 
                             final File      localFile)
    {
        setLoading(false);

        //System.out.println(imageName+" -> "+isError);
        if (!isError)
        {
            setImage(imageIcon);

        } else
        {
            setImage((ImageIcon)null);
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.images.ImageLoaderListener#imageStopped(java.lang.String)
     */
    @Override
    public void imageStopped(String imageName, final boolean doLoadFullImage)
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageDisplay#setLoading(boolean)
     */
    @Override
    public synchronized void setLoading(boolean isLoading)
    {
        super.setLoading(isLoading);
//        if (!isLoading && imageLoader != null)
//        {
//            imageLoader.stopLoading();
//        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#stopLoading()
     */
    @Override
    public void stopLoading()
    {
        super.stopLoading();
//        if (imageLoader != null)
//        {
//            imageLoader.stopLoading();
//        }
    }


    /**
     * @return the isSelected
     */
    public boolean isSelected()
    {
        return imgDataItem != null && imgDataItem.isSelected();
    }

    /**
     * @param isSelected the isSelected to set
     */
    public void setSelected(boolean isSelected)
    {
        if (imgDataItem != null)
        {
            imgDataItem.setSelected(isSelected);
        }
    }
    
    /**
     * @return the imgDataItem
     */
    public ImageDataItem getImageDataItem()
    {
        return imgDataItem;
    }

    /**
     * @param imgDataItem the imgDataItem to set
     */
    public void setImageDataItem(final ImageDataItem imgDataItem)
    {
        this.imgDataItem = imgDataItem;
        
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(imgDataItem.getTableId());
        if (ti != null)
        {
            dataObjIcon = ti.getIcon(IconManager.STD_ICON_SIZE.Std16);
        }
    }

    /**
     * @param lsl
     */
    public void addListener(final GalleryGridListener lsl)
    {
        if (lsl != null)
        {
            listeners.add(lsl);
        }
    }
    
    /**
     * @param lsl
     */
    public void removeListener(final GalleryGridListener lsl)
    {
        if (lsl != null)
        {
            listeners.remove(lsl);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#cleanup()
     */
    @Override
    public void cleanup()
    {
        super.cleanup();
        
        image          = null;
        changeListener = null;
        paintComponent = null;
        
        if (listeners != null)
        {
            listeners.clear();
            listeners = null;
        }
        
        if (imgDataItem != null) 
        {
            imgDataItem.cleanup();
            imgDataItem = null;
        }

    }


}

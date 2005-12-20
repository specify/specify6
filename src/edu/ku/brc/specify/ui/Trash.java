/* Filename:    $RCSfile: Trash.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.ui;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import edu.ku.brc.specify.ui.dnd.DndDeletable;
import edu.ku.brc.specify.ui.dnd.GhostActionable;
import edu.ku.brc.specify.ui.dnd.GhostMotionAdapter;
import edu.ku.brc.specify.ui.dnd.*;

/**
 * Implements a "trash can" for deleting and recovering RecordSets, labels etc.
 * 
 * @author rods
 *
 */
public class Trash extends JComponent implements GhostActionable
{
    // These used for the Ghosting
    protected static final int SHADOW_SIZE = 10;
    
    protected ImageIcon              imgIcon;
    protected ImageIcon              trashIcon;
    protected ImageIcon              trashFullIcon;
    protected ImageIcon              paperIcon;
    
    protected GhostMouseDropAdapter  mouseDropAdapter    = null;
    protected Object                 data                = null;
    protected RenderingHints         hints               = null;
    protected BufferedImage          shadowBuffer        = null;
    protected BufferedImage          buffer              = null;;
    protected Dimension              prefferedRenderSize = new Dimension(0,0);
    protected boolean                verticalLayout      = false;
    protected Vector<Object>         items               = new Vector<Object>();
    
    protected Color                  textColor           = new Color(0,0,0, 90);
    protected Font                   textFont            = null;
    protected JPopupMenu             popupMenu           = null;
    protected JMenuItem              emptyMenuItem       = null;
    
    public Trash()
    {
        trashIcon     = new ImageIcon(IconManager.getImagePath("trash.png"));
        trashFullIcon = new ImageIcon(IconManager.getImagePath("trash_full.png"));
        paperIcon     = trashFullIcon;//new ImageIcon(IconManager.getImagePath("trash_paper.gif"));
        imgIcon       = trashIcon;
        
        popupMenu = new JPopupMenu();
        emptyMenuItem = new JMenuItem(getResourceString("EmptyTrash"));
        emptyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                emptyTrash();
            }
          });
        popupMenu.add(emptyMenuItem);
        MouseListener mouseListener = new MouseAdapter() {
              private void showIfPopupTrigger(MouseEvent mouseEvent) 
              {
                  emptyMenuItem.setEnabled(items.size() > 0);
                  
                  if (mouseEvent.isPopupTrigger() && popupMenu.getComponentCount() > 0) 
                  {
                      popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                  }
              }
              public void mousePressed(MouseEvent mouseEvent) {
                showIfPopupTrigger(mouseEvent);
              }
              public void mouseReleased(MouseEvent mouseEvent) {
                showIfPopupTrigger(mouseEvent);
              }
            };
            //iconLabel.addMouseListener (mouseListener);            
        addMouseListener (mouseListener);        

    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        g.drawImage(imgIcon.getImage(), 0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
        /*
        if (items.size() > 0)
        {
            g.setColor(textColor);
            if (textFont == null)
            {
                textFont = new Font(getParent().getFont().getName(), Font.BOLD, 14);
            }
            g.setFont(textFont);
            FontMetrics fm = g.getFontMetrics();
            Dimension size = getSize();
            String text = Integer.toString(items.size());
            g.drawString(text, (size.width-fm.stringWidth(text))/2, size.height-(size.height - fm.getAscent())/2);
        }*/
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        return new Dimension(imgIcon.getIconWidth(), imgIcon.getIconHeight());
    }
    
    /**
     * Returns a buffered image of the paper in the trash
     * @return Returns a buffered image of the paper in the trash
     */
    protected BufferedImage getPaper()
    {
        createRenderingHints();
        
        BufferedImage buffer = new BufferedImage(imgIcon.getIconWidth(),imgIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        
        Graphics2D g2 = (Graphics2D) buffer.createGraphics();
        g2.setRenderingHints(hints);

        g2.drawImage(paperIcon.getImage(), 0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
        
        return buffer;
    }
    
    /**
     * Empties trash
     */
    protected void emptyTrash()
    {
        GhostGlassPane glassPane = UICacheManager.getGlassPane();
        
        Component parent = getParent();
        
        Point p = (Point)getLocation().clone();
        
        Rectangle rr = SwingUtilities.convertRectangle(parent, parent.getBounds(), glassPane);
        
        // I was having no luck translating the point of the image
        // so do the brute forace
        p.x = (rr.width - imgIcon.getIconWidth())/2;
        p.y = rr.y + parent.getSize().height - imgIcon.getIconHeight() - 1;
        
        glassPane.setImage(getPaper());
        glassPane.setPoint(p);
        glassPane.setVisible(true);
        
        glassPane.startAnimation(rr);
        
        items.clear();
        imgIcon = trashIcon;
        repaint();
    }
    
    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#doAction(edu.ku.brc.specify.ui.dnd.GhostActionable)
     */
    public void doAction(GhostActionable src)
    {
        if (src != null)
        {
            
            Object data = src.getData();
            
            if (src instanceof DndDeletable && data != null)
            {
                if (((DndDeletable)src).deleteRequest())
                {
                    items.add(data);
                    imgIcon = trashFullIcon;
                    repaint();
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    public void setData(final Object data)
    {
        this.data = data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#getData()
     */
    public Object getData()
    {
        return data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#createMouseDropAdapter()
     */
    public void createMouseDropAdapter()
    {
        mouseDropAdapter = new GhostMouseDropAdapter(UICacheManager.getGlassPane(), "action", this);
        addMouseListener(mouseDropAdapter);
        addMouseMotionListener(new GhostMotionAdapter(UICacheManager.getGlassPane()));
    }
    
    /**
     * Returns the adaptor for tracking mouse drop gestures
     * @return Returns the adaptor for tracking mouse drop gestures
     */
    public GhostMouseDropAdapter getMouseDropAdapter()
    {
        return mouseDropAdapter;
    }
    
    /**
     * 
     *
     */
    private void createRenderingHints() 
    {
        if (hints == null)
        {
            hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                                       RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            Object value = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            try {
                Field declaredField = RenderingHints.class.getDeclaredField("VALUE_TEXT_ANTIALIAS_LCD_HRGB");
                value = declaredField.get(null);
            } catch (Exception e) {
            }
            hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, value);
        }
    }
    
    /**
     * 
     */
    private void renderOffscreen() 
    {
        createRenderingHints();
        
        buffer = new BufferedImage(imgIcon.getIconWidth(), imgIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = (Graphics2D) buffer.createGraphics();
        g2.setRenderingHints(hints);

        g2.fillRect(0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight());
                
        g2.setClip(0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight());
        g2.drawImage(imgIcon.getImage(), 0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
        
        g2.dispose();
    }
    
    /**
     * 
     * @return
     */
    public BufferedImage getBufferedImage() 
    {
        if (buffer == null) 
        {
            renderOffscreen();
        }
        return buffer;
    }

}

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

package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import edu.ku.brc.ui.dnd.DndDeletable;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;

/**
 * Implements a "trash can" for deleting and recovering RecordSets, labels etc.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class Trash extends JComponent implements GhostActionable
{
    // Static Data Members
    public static final DataFlavor TRASH_FLAVOR = new DataFlavor(Trash.class, "Trash");
    
    private static final Trash instance = new Trash();
    
    // These used for the Ghosting
    protected static final int SHADOW_SIZE = 10;
    
    protected ImageIcon              imgIcon;
    protected ImageIcon              trashIcon;
    protected ImageIcon              trashFullIcon;
    protected ImageIcon              paperIcon;
    
    protected GhostMouseInputAdapter  mouseInputAdapter    = null;
    protected RenderingHints         hints               = null;
    protected BufferedImage          shadowBuffer        = null;
    protected BufferedImage          buffer              = null;
    protected Dimension              prefferedRenderSize = new Dimension(0,0);
    protected boolean                verticalLayout      = false;
    protected Vector<DndDeletable>   items               = new Vector<DndDeletable>();
    
    protected Color                  textColor           = new Color(0,0,0, 90);
    protected Font                   textFont            = null;
    protected JPopupMenu             popupMenu           = null;
    protected JMenuItem              emptyMenuItem       = null;
    protected JMenuItem              openMenuItem        = null;
    
    protected Object                 data                = null;
    
    // DnD
    protected List<DataFlavor>       dropFlavors         = new ArrayList<DataFlavor>(); 

    /**
     * Constructor
     */
    protected Trash()
    {
        dropFlavors.add(TRASH_FLAVOR);
        
        trashIcon     = new ImageIcon(IconManager.getImagePath("trash.png"));
        trashFullIcon = new ImageIcon(IconManager.getImagePath("trash_full.png"));
        paperIcon     = trashFullIcon;//new ImageIcon(IconManager.getImagePath("trash_paper.gif"));
        imgIcon       = trashIcon;
        
        // XXX RELEASE - Disabled for stand-alone Workbench
        if (false)
        {
            popupMenu = new JPopupMenu();
            
            openMenuItem = new JMenuItem(getResourceString("Open"));
            openMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    openTrashCan();
                }
              });
            popupMenu.add(openMenuItem);
            
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
                      openMenuItem.setEnabled(items.size() > 0);
                      
                      if (mouseEvent.isPopupTrigger() && popupMenu.getComponentCount() > 0) 
                      {
                          popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                      }
                  }
                  @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    showIfPopupTrigger(mouseEvent);
                  }
                  @Override
                public void mouseReleased(MouseEvent mouseEvent) {
                    showIfPopupTrigger(mouseEvent);
                  }
                };
                //iconLabel.addMouseListener (mouseListener);            
            addMouseListener (mouseListener);       
        }

    }
    
    /**
     * Opens the trash can for viewing
     */
    protected void openTrashCan()
    {
        TrashCanDlg dlg = new TrashCanDlg((Frame)UIRegistry.get(UIRegistry.TOPFRAME));
        dlg.setVisible(true);        
    }
    
    /**
     * A singleton to the trash can
     * @return A singleton to the trash can
     */
    public static Trash getInstance()
    {
        return instance;
    }
    
    /**
     * the list of trash items
     * @return the list of trash items
     */
    public Vector<DndDeletable> getItems()
    {
        return items;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
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
    @Override
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
        
        BufferedImage buf = new BufferedImage(imgIcon.getIconWidth(),imgIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D    g2  = buf.createGraphics();
        g2.setRenderingHints(hints);

        g2.drawImage(paperIcon.getImage(), 0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
        
        return buf;
    }
    
    /**
     * Empties trash
     */
    protected void emptyTrash()
    {
        GhostGlassPane glassPane = UIRegistry.getGlassPane();
        
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
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    public void doAction(GhostActionable src)
    {
        if (src != null)
        {
            
            Object dataObj = src.getData();
            
            if (src instanceof DndDeletable && dataObj != null)
            {
                if (((DndDeletable)src).deleteRequest())
                {
                    items.add((DndDeletable)src);
                    //imgIcon = trashFullIcon;
                    repaint();
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    public void setData(final Object data)
    {
        this.data = data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    public Object getData()
    {
        return data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
     */
    public Object getDataForClass(Class<?> classObj)
    {
        return UIHelper.getDataForClass(data, classObj);
    }
   
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseInputAdapter()
     */
    public void createMouseInputAdapter()
    {
        mouseInputAdapter = new GhostMouseInputAdapter(UIRegistry.getGlassPane(), "action", this);
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setActive(boolean)
     */
    public void setActive(boolean isActive)
    {
        // Auto-generated method stub
    }
    
    /**
     * Returns the adaptor for tracking mouse drop gestures
     * @return Returns the adaptor for tracking mouse drop gestures
     */
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return mouseInputAdapter;
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
            } catch (Exception e)
            {
                // do nothing
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

        Graphics2D g2 = buffer.createGraphics();
        g2.setRenderingHints(hints);

        g2.fillRect(0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight());
                
        g2.setClip(0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight());
        g2.drawImage(imgIcon.getImage(), 0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
        
        g2.dispose();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getBufferedImage()
     */
    public BufferedImage getBufferedImage() 
    {
        if (buffer == null) 
        {
            renderOffscreen();
        }
        return buffer;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataFlavor()
     */
    public List<DataFlavor> getDropDataFlavors()
    {
        return dropFlavors;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDragDataFlavors()
     */
    public List<DataFlavor> getDragDataFlavors()
    {
        return null; // this is not draggable
    }
    


}

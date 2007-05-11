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

package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.dnd.ShadowFactory;

/**
 * This class implements the "Glass Pane" that goes over the entire form. NOTE: it isn't a "real" glass pane
 * but we are doing essentially the same thing.<BR><BR>
 * 
 * First, we have the form render into an offscreen buffer, then we find all the fields and their rects. Next we
 * reduce the transparency of each of the rects.<BR><BR>
 * 
 * Then we enable the user to click anywhere on a field and have it added as a field for Carry Forward. This gets 
 * all the CF info from the FormViewObj so it can tell it whether a field has been added or removed form the 
 * Carry Forward info.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class CarryForwardSetUp extends JComponent
{
    protected final ImageIcon checkIcon = new ImageIcon(IconManager.getImagePath("check.gif"));  // Move to icons.xml
    
    protected MultiView          root;
    protected BufferedImage      bufImg;
    protected List<CarryFwdItem> items = new ArrayList<CarryFwdItem>(20);
    
    protected ControlDialog      dlg;
    protected boolean            doingDrag  = true; 
    protected Point              dragPnt    = new Point();
    protected Point              offsetPnt  = new Point();
    
    /**
     * Create the JComponent that will act like a glass pane. 
     * Then traverse all the forms and collect the location of all the fields
     * @param root The root MultiView that will be rendered and traversed.
     */
    public CarryForwardSetUp(final MultiView root)
    {
        this.root = root;
        
        dlg = new ControlDialog(this);
        
        Dimension size = root.getSize();
        bufImg = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        setSize(size);
        setPreferredSize(size);
        
        discoverCompRects(root, 0, 0);
        Graphics2D g2 = (Graphics2D)bufImg.getGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, size.width, size.height);
        
        root.paint(g2);
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.70f));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        

        g2.setColor(Color.WHITE);
        //g2.setColor(new Color(255, 255, 255, 100));
        for (CarryFwdItem item : items)
        { 
            g2.fillRoundRect(item.rect.x, item.rect.y, item.rect.width, item.rect.height, 5, 5);
            //blur(r, g2);
        }
        g2.dispose();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Point p = e.getPoint();
                p.x -= dlg.getLocation().x;
                p.y -= dlg.getLocation().y;
                if (!dlg.processMouseClicked(p))
                {
                    doSelect(e.getPoint());
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) 
            {
                if (dlg.getTitleBounds().contains(e.getPoint()))
                {
                    dragPnt   = e.getPoint();
                    doingDrag = true;
                    offsetPnt.setLocation(dragPnt.x - dlg.getLocation().x, dragPnt.y - dlg.getLocation().y);
                    
                } else
                {
                    doingDrag = false;
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                if (doingDrag)
                {
                    Point p = e.getPoint();
                    dlg.setLocation(p.x-offsetPnt.x, p.y-offsetPnt.y);
                    repaint();
                }
            }
        });
        
        //add(dlg);
        dlg.setLocation((size.width-dlg.getSize().width)/2, (size.height-dlg.getSize().height)/2);
    }
    
    /**
     * Selects all the fields 
     */
    protected void selectAll()
    {
        for (CarryFwdItem item : items)
        {
            if (!item.isChecked())
            {
                item.doCheck();
            }
        }
        root.repaint();
    }
    
    
    /**
     * Delselects all the fields 
     */
    protected void selectNone()
    {
        for (CarryFwdItem item : items)
        {
            if (item.isChecked())
            {
                item.doCheck();
            }
        }
        root.repaint();
    }
    
    /**
     * Closes the Setup dialog and returns the form to edit state
     */
    protected void accept()
    {
        root.acceptCarryForwardSetup();
        root.repaint();
    }

    
    /**
     * Selects a single (clicked on) field
     * @param pnt the pnt that was clicked
     */
    protected void doSelect(final Point pnt)
    {
        for (CarryFwdItem item : items)
        {
            if (item.rect.contains(pnt))
            {
                item.doCheck();
            }
        }
        repaint();
    }


    
    /**
     * Walks the MultiView tree and calculates all the bounding rectangles for each field
     * and adjusts their X,Y to be offset form the root MultiView
     * @param parent the top level MultiView
     */
    protected void discoverCompRects(final MultiView parent, final int xOffset, final int yOffset)
    {
        for (Viewable viewable : parent.getViewables())
        {
            if (viewable instanceof FormViewObj)
            {
                Map<String, Component> map = viewable.getControlMapping();
                for (String id : viewable.getControlMapping().keySet())
                {
                    Component comp = map.get(id);
                    
                    // This is cheesy and lame (assume sobly these two type of controls
                    if (comp instanceof JTextArea || comp instanceof JList)
                    {
                        Component p = comp.getParent();
                        while (p != null && !(p instanceof JScrollPane))
                        {
                            p = p.getParent();
                        }
                        if (p != null)
                        {
                            comp = p;
                        }
                    }
                    if (!(comp instanceof MultiView))
                    {
                        Rectangle r = comp.getBounds();
                        r.translate(xOffset, yOffset);
                        items.add(new CarryFwdItem(id, ((FormViewObj)viewable).getCarryForwardInfo(), r));
                    }
                }
            }
        }
        for (MultiView mv : parent.getKids())
        {
            Point pnt = mv.getLocation(); 
            discoverCompRects(mv, xOffset+pnt.x, yOffset+pnt.y);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawImage(bufImg, 0,0, bufImg.getWidth(), bufImg.getHeight(), null);
        
        int w = checkIcon.getIconWidth();
        int h = checkIcon.getIconHeight();
        
        for (CarryFwdItem item : items)
        {
            if (item.isChecked())
            {
                Rectangle r = item.rect;
                g.drawImage(checkIcon.getImage(), r.x+((r.width-w)/2), r.y+((r.height-h)/2), w, h, null);
            }
        }
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        dlg.paint(g);
    }
    
    
    //-----------------------------------------------------------------------------
    // This JComponent represent the floating dialog that enables the user to 
    // select all, select none, and close the set up session
    // NOte the JComponent is not atually added to the parent.
    // It is manually drawn into the parent
    //-----------------------------------------------------------------------------
    class ControlDialog extends JComponent
    {
        protected BufferedImage bufferedImg;
        protected String        title      = "Carry Forward Setup";                    // XXX I18N
        protected String[]      buttonStrs = {getResourceString("SelectAll"), getResourceString("SelectNone"), getResourceString("Close")};
        protected int[]         widths     = new int[buttonStrs.length];
        protected int           maxWidth   = 0;
        protected int           vertGap    = 5;
        protected int           horzGap    = 10;
        protected int           strHeight  = 0;
        protected Rectangle[]   rects      = new Rectangle[buttonStrs.length];
        protected int           titleBarHeight;
        protected int           titleBarGap = 2;
        
        protected CarryForwardSetUp cfsu;
        
        /**
         *  Create an Image buffer with all the UI drawn in. 
         */
        public ControlDialog(final CarryForwardSetUp cfsu)
        {
            this.cfsu = cfsu;
            
            bufferedImg = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufferedImg.createGraphics();
            strHeight = g2.getFontMetrics().getHeight();
            
            titleBarHeight = strHeight + titleBarGap*2;
            
            maxWidth = g2.getFontMetrics().stringWidth(title);
            int titleWidth = maxWidth;
            
            for (int i=0;i<buttonStrs.length;i++)
            {
                widths[i] = g2.getFontMetrics().stringWidth(buttonStrs[i]);
                maxWidth = Math.max(maxWidth, widths[i]);
            }
            
            int width = maxWidth + (2 * horzGap);
            int y = titleBarHeight + vertGap;
            for (int i=0;i<buttonStrs.length;i++)
            {
                rects[i] = new Rectangle(0, y, width, strHeight);
                y  += strHeight + vertGap;
            }
            
            int height = ((buttonStrs.length+1)*vertGap) + (strHeight * buttonStrs.length) + titleBarHeight;
            setSize(width, height);
            g2.dispose();
            
            bufferedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            g2 = bufferedImg.createGraphics();
             g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, width-1, height-1, 10, 10);
            
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(0, 0, width-1, height-1, 10, 10);
            
            // Draw title bar
            g2.setColor(new Color(220, 220, 255, 175));
            g2.fillRoundRect(0, 0, width, titleBarHeight, 10, 10);
            
            g2.setColor(Color.BLACK);
            g2.drawString(title, (width-titleWidth)/2, ((titleBarHeight-strHeight)/2)+strHeight-3); // XXX minus 5 kludge


            g2.setColor(Color.BLACK);
            y = titleBarHeight + vertGap + strHeight;
            for (int i=0;i<buttonStrs.length;i++)
            {
                g2.drawString(buttonStrs[i], ((width - widths[i]) / 2), y);
                y  += strHeight + vertGap;
            }
            
            g2.dispose();
            
            int shadowWidth = 10;
            ShadowFactory factory = new ShadowFactory(shadowWidth, 0.17f, Color.BLACK);
            BufferedImage shadowImage = factory.createShadow(bufferedImg);
            g2 = shadowImage.createGraphics();
            int left   = (int)(shadowWidth * 0.5);
            int top    = (int)(shadowWidth* 0.4);
            g2.drawImage(bufferedImg, left, top, bufferedImg.getWidth(), bufferedImg.getHeight(), null);
            g2.dispose();
            bufferedImg = shadowImage;
            
            for (int i=0;i<rects.length;i++)
            {
                rects[i].translate(left, top);
            }

        }
        
        /**
         * LOcate which "button" the user clicked on
         * @param pnt the point the user clicked on already adjusted
         * @return true - was consumed, false it wasn't consumed
         */
        public boolean processMouseClicked(final Point pnt)
        {
            for (int i=0;i<rects.length;i++)
            {
                if (rects[i].contains(pnt))
                {
                    switch (i) 
                    {
                        case 0 : 
                            cfsu.selectAll();
                            return true;
                            
                        case 1 : 
                            cfsu.selectNone();
                            return true;
                            
                        case 2 : 
                            cfsu.accept();
                            return true;                            
                    }
                }
            }
            return false;
        }
        
        /**
         * Returns the rectange for the title bar translated to the location of the dialog
         * @return the rectange for the title bar translated to the location of the dialog
         */
        public Rectangle getTitleBounds()
        {
            Point p = getLocation();
            return new Rectangle(p.x, p.y, getSize().width, titleBarHeight);
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Point p = getLocation();
            g.drawImage(bufferedImg, p.x, p.y, bufferedImg.getWidth(), bufferedImg.getHeight(), null);
        }
    }
    
    /**
     * This class represents each field on the screen and maintains information about where it is
     * and whether it has been checked
     *
     */
    class CarryFwdItem 
    {
        private String           id;
        private boolean          isChecked = false;
        protected Rectangle        rect;
        private CarryForwardInfo cfi;
        
        public CarryFwdItem(final String id, final CarryForwardInfo cfi, final Rectangle rect)
        {
            this.id = id;
            this.rect = rect;
            this.cfi  = cfi;
            
            this.isChecked  = cfi.contains(id);
         }
        
        public void doCheck()
        {
            isChecked = !isChecked;
            if (isChecked)
            {
                cfi.add(id);
            } else
            {
                cfi.remove(id);
            }
        }
        public boolean isChecked()
        {
            return isChecked;
        }

        public Rectangle getRect()
        {
            return rect;
        }
        
        
    }
    

}

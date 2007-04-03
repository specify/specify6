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
package edu.ku.brc.ui.dnd;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.dnd.GhostGlassPane.ImagePaintMode;

/**
 * Class responsible for doing all the work for the Drag portion of Drag and Drop.
 
 * @code_status Beta
 **
 * @author rods
 *
 */
public class GhostMouseInputAdapter extends MouseInputAdapter
{
    private static final Logger log = Logger.getLogger(GhostMouseInputAdapter.class);
    
    // Ghost MotionAdaptor
    protected static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor DEF_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    protected static final int    dragButtonIndex;
    
    protected GhostGlassPane          glassPane;
    protected Cursor                  currCursor        = null;
    protected Point                   firstPosition     = new Point();
    protected ImagePaintMode          paintPositionMode = ImagePaintMode.DRAG;

    protected String                  action;
    protected List<GhostDropListener> listeners;
    protected GhostActionable         ghostActionable;
    
    protected Point                   offsetFromStartPnt = new Point();
    protected boolean                 doAnimationOnDrop  = true;
    protected Component               dropCanvas         = null;
    
    static {
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            dragButtonIndex = 1;
        } else
        {
            dragButtonIndex = 0;
        }
    }

    /**
     * Constructor
     * @param glassPane the glass pane
     * @param action the action command
     * @param ghostActionable the actionable for the adapter
     */
    public GhostMouseInputAdapter(GhostGlassPane  glassPane,
                                  String          action,
                                  GhostActionable ghostActionable)
    {
        this.glassPane       = glassPane;
        this.action          = action;
        this.ghostActionable = ghostActionable;
        this.listeners       = new ArrayList<GhostDropListener>();
    }
    
    public void registerWithGlassPane()
    {
        glassPane.add(ghostActionable);
    }
    
    public void unregisterWithGlassPane()
    {
        glassPane.remove(ghostActionable);
    }
    

    public ImagePaintMode getPaintPositionMode()
    {
        return paintPositionMode;
    }

    public void setPaintPositionMode(ImagePaintMode paintPositionMode)
    {
        this.paintPositionMode = paintPositionMode;
    }

    public Point getOffsetFromStartPnt()
    {
        return offsetFromStartPnt;
    }

    public void setDoAnimationOnDrop(boolean doAnimationOnDrop)
    {
        this.doAnimationOnDrop = doAnimationOnDrop;
    }

    /**
     * @param dropCanvas the dropCanvas to set
     */
    public void setDropCanvas(Component dropCanvas)
    {
        this.dropCanvas = dropCanvas;
    }
    
    // From GhostMouseDropAdapter


    /**
     * Starts the Drag process
     * @param c the component
     * @param button the button
     * @param pnt the starting point
     */
    protected void startDrag(Component c, int button, Point pnt)
    {
        Point pos = c.getLocation();
        pos.x = pnt.x - pos.x;
        pos.y = pnt.y - pos.y;
        //System.out.println(button+" startDrag "+DragAndDropLock.isDragAndDropStarted()+" "+DragAndDropLock.isLocked()+"  dragButtonIndex "+dragButtonIndex);

        if (DragAndDropLock.isLocked() || button != dragButtonIndex)
        {
            DragAndDropLock.setDragAndDropStarted(false);
            //System.out.println("startDrag bailing");
            return;
        }

        DragAndDropLock.setLocked(true);
        DragAndDropLock.setDragAndDropStarted(true);

        glassPane.startDrag(ghostActionable);
        
        glassPane.setVisible(true);
        
        //log.error("StartDrag: "+c.getClass().getName()+" "+c.getBounds());

        Point p = (Point) pnt.clone();
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, glassPane);

        BufferedImage bi = ghostActionable.getBufferedImage();
        glassPane.setPoint(p, paintPositionMode);
        glassPane.setOffset(pos);
        glassPane.setImage(bi, bi.getWidth());
        glassPane.repaint();

    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        //System.out.println("mousePressed "+e.getPoint());
        firstPosition.setLocation(e.getPoint());
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
        //System.out.println(e.getButton()+" mouseReleased "+DragAndDropLock.isDragAndDropStarted()+" "+DragAndDropLock.isLocked()+"  dragButtonIndex "+dragButtonIndex);
        //if (e.getButton() != dragButtonIndex || !DragAndDropLock.isDragAndDropStarted())
        if (!DragAndDropLock.isDragAndDropStarted())
        {
            return;
        }
        DragAndDropLock.setDragAndDropStarted(false);
        
        glassPane.stopDrag();

        Component srcOfDropComp = e.getComponent();
        if (!(srcOfDropComp instanceof GhostActionable))
        {
            srcOfDropComp = srcOfDropComp.getParent();
        }
        Point p = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(p, srcOfDropComp);

        Point eventPoint = (Point) p.clone();
        SwingUtilities.convertPointFromScreen(p, glassPane);

        glassPane.setPoint(p, paintPositionMode);

        JComponent rootPane = (JComponent)UICacheManager.get(UICacheManager.MAINPANE);

        // find the component that under this point
        Component dropComponent;
        if (dropCanvas != null)
        {
            //System.out.println("++++++++++++++++++++++++++++++++++++++++");
            Point pp = (Point) e.getPoint().clone();
            //System.out.println(pp);
            SwingUtilities.convertPointToScreen(pp, srcOfDropComp);
            //System.out.println(pp);
            SwingUtilities.convertPointFromScreen(pp, dropCanvas);
            //System.out.println(pp +" "+rootPane);
            dropComponent = SwingUtilities.getDeepestComponentAt(dropCanvas, pp.x, pp.y);
            
            if (dropComponent == null)
            {
                log.error("Drop Component is NULL!");
            }

            Component parent = dropComponent;
            while (parent != null && parent != dropCanvas)
            {
                parent = parent.getParent();
            }
            if (parent == dropCanvas)
            {
                dropComponent = dropCanvas;
            }
            //System.out.println("++++++++++++++++++++++++++++++++++++++++\n");
            
        } else
        {
            Point pp = (Point) e.getPoint().clone();
            //System.out.println(pp);
            SwingUtilities.convertPointToScreen(pp, srcOfDropComp);
            //System.out.println(pp);
            SwingUtilities.convertPointFromScreen(pp, rootPane);
            //System.out.println(pp +" "+rootPane);
            dropComponent = SwingUtilities.getDeepestComponentAt(rootPane, pp.x, pp.y);
        }

        offsetFromStartPnt.setLocation(e.getPoint());

        boolean clearIt = true;
        if (dropComponent == ghostActionable)
        {
            clearIt = true;
            
        } else if (dropComponent instanceof GhostActionable && dropComponent instanceof JComponent)
        {
            BufferedImage bi = ghostActionable.getBufferedImage();
            glassPane.setImage(bi, bi.getWidth());
            
            boolean isDropOK = glassPane.isDropOK(ghostActionable, (GhostActionable)dropComponent);
            
            if (doAnimationOnDrop && isDropOK)
            {
                glassPane.startAnimation(SwingUtilities.convertRectangle(dropComponent,
                                                                        ((JComponent)dropComponent).getVisibleRect(),
                                                                        glassPane));
            } else
            {
                glassPane.finishedWithDragAndDrop();
                glassPane.repaint();
            }
            
            if (srcOfDropComp instanceof GhostActionable) // this check really needed?
            {
                if (isDropOK)
                {
                    doCommandAction((GhostActionable)dropComponent, (GhostActionable)srcOfDropComp);
                }
                clearIt = false;
            }

        } else
        {
            if (hasListeners())
            {
                GhostDropEvent gde = new GhostDropEvent(srcOfDropComp, action, eventPoint);
                fireGhostDropEvent(gde);
                if (gde.isConsumed())
                {
                    clearIt = false;
                }
            }
        }

        if (clearIt)
        {
            glassPane.setImage(null);
            glassPane.setVisible(false);
            DragAndDropLock.setLocked(false);
        }
    }
        
    /**
     * Perform the drop action.
     * @param drop the GhostActionable to perform it on 
     * @param src the source of the drop
     */
    protected void doCommandAction(final GhostActionable drop, final GhostActionable src)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                drop.doAction(src);
            }
        });
    }

    /**
     * Returns whether the flavors match
     * @param srcGA the source
     * @param dstGA the destination
     * @return Returns whether the flavors match
     */
    protected boolean isFlavorOK(final GhostActionable  srcGA, final GhostActionable  dstGA)
    {
        for (DataFlavor srcDF : srcGA.getDragDataFlavors())
        {
            for (DataFlavor dstDF : dstGA.getDropDataFlavors())
            {
                if (srcDF.equals(dstDF))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Updates (draws) the glass pane as the image is dragged
     */
    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (!ghostActionable.isEnabled())
        {
            return;
        }
        
        //System.out.println("mouseDragged "+DragAndDropLock.isDragAndDropStarted()+" btn "+e.getButton());
        Point pnt = e.getPoint();
        if (!DragAndDropLock.isDragAndDropStarted())
        {

            if (Math.abs(firstPosition.x - pnt.x) > 3 || Math.abs(firstPosition.y - pnt.y) > 3)
            {
                 startDrag(e.getComponent(), e.getButton(), firstPosition);

            } else
            {
                return;
            }
        }

        Component c = e.getComponent();
        if (!(c instanceof GhostActionable))
        {
            c = c.getParent();
        }

        // Translate the Point from current component to the Main pane
        // which is the same size as the ghost pane
        JComponent rootPane = (JComponent)UICacheManager.get(UICacheManager.MAINPANE);
        Point      pp       = (Point) pnt.clone();
        SwingUtilities.convertPointToScreen(pp, c);
        SwingUtilities.convertPointFromScreen(pp, rootPane);

        // Find the component that under this point on the main pane
        Component dropComponent = SwingUtilities.getDeepestComponentAt(rootPane, pp.x, pp.y);

        boolean flavorOK = false;
        if (dropComponent instanceof GhostActionable && c != dropComponent && c instanceof GhostActionable)
        {
            flavorOK = isFlavorOK((GhostActionable)c, (GhostActionable)dropComponent);
        }

        if (flavorOK)
        {
            if (currCursor != HAND_CURSOR) // a little optimiztion
            {
                glassPane.setAlpha(1.0f);
                glassPane.setCursor(HAND_CURSOR);
                currCursor = HAND_CURSOR;
            }
        } else
        {
            if (currCursor != DEF_CURSOR) // a little optimiztion
            {
                glassPane.resetAlpha();
                glassPane.setCursor(DEF_CURSOR);
                currCursor = DEF_CURSOR;
            }
        }

        // Now translate the point to the glass pane
        Point p = (Point) pnt.clone();
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, glassPane);
        glassPane.setPoint(p, paintPositionMode);

        glassPane.repaint(glassPane.getRepaintRect());
    }


    public void addGhostDropListener(GhostDropListener listener)
    {
        if (listener != null)
            listeners.add(listener);
    }

    public void removeGhostDropListener(GhostDropListener listener)
    {
        if (listener != null)
            listeners.remove(listener);
    }

    protected void fireGhostDropEvent(GhostDropEvent evt)
    {
        Iterator<GhostDropListener> it = listeners.iterator();
        while (it.hasNext())
        {
            it.next().ghostDropped(evt);
        }
    }

    protected boolean hasListeners()
    {
        return listeners.size() > 0;
    }




}

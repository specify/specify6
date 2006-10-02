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

import edu.ku.brc.ui.UICacheManager;

/**
 * Class responsible for doing all the work for the Drag portion of Drag and Drop.
 
 * @code_status Beta
 **
 * @author rods
 *
 */
public class GhostMouseInputAdapter extends MouseInputAdapter
{
    // Ghost MotionAdaptor
    protected static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor DEF_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    protected static final int    dragButtonIndex;
    
    protected GhostGlassPane          glassPane;
    protected Cursor                  currCursor    = null;
    protected Point                   firstPosition = new Point();

    protected String                  action;
    protected List<GhostDropListener> listeners;
    protected GhostActionable         ghostActionable;
    
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
        this.listeners = new ArrayList<GhostDropListener>();
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

        //System.out.println(button+" startDrag "+DragAndDropLock.isDragAndDropStarted()+" "+DragAndDropLock.isLocked()+"  dragButtonIndex "+dragButtonIndex);

        if (DragAndDropLock.isLocked() || button != dragButtonIndex)
        {
            DragAndDropLock.setDragAndDropStarted(false);
            //System.out.println("startDrag bailing");
            return;
        }

        DragAndDropLock.setLocked(true);
        DragAndDropLock.setDragAndDropStarted(true);

        glassPane.setVisible(true);

        Point p = (Point) pnt.clone();
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, glassPane);

        BufferedImage bi = ghostActionable.getBufferedImage();
        glassPane.setPoint(p);
        glassPane.setImage(bi, bi.getWidth());
        glassPane.repaint();

    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        //System.out.println("mousePressed");
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

        Component c = e.getComponent();

        Point p = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(p, c);

        Point eventPoint = (Point) p.clone();
        SwingUtilities.convertPointFromScreen(p, glassPane);

        glassPane.setPoint(p);

        //System.out.println(NavBoxMgr.getInstance().getBounds());
        //JComponent rootPane = NavBoxMgr.getInstance();
        JComponent rootPane = (JComponent)UICacheManager.get(UICacheManager.MAINPANE);
        //System.out.println(rootPane.getBounds());

        Point pp = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(pp, c);
        SwingUtilities.convertPointFromScreen(pp, rootPane);

        // find the component that under this point
        Component dropComponent = SwingUtilities.getDeepestComponentAt(rootPane, pp.x, pp.y);

        //System.out.println(component);

        boolean clearIt = true;
        if (dropComponent instanceof GhostActionable && dropComponent instanceof JComponent)
        {
            BufferedImage bi = ghostActionable.getBufferedImage();
            glassPane.setImage(bi, bi.getWidth());
            glassPane.startAnimation(SwingUtilities.convertRectangle(dropComponent,
                                                                    ((JComponent)dropComponent).getVisibleRect(),
                                                                    glassPane));
            Component source = e.getComponent();

            if (source instanceof GhostActionable)
            {
                ((GhostActionable)dropComponent).doAction((GhostActionable)source);
                clearIt = false;
            }

        } else
        {
            if (hasListeners())
            {
                GhostDropEvent gde = new GhostDropEvent(c, action, eventPoint);
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
        //System.out.println("mouseDragged "+DragAndDropLock.isDragAndDropStarted()+" btn "+e.getButton());
        Point pnt = e.getPoint();
        if (!DragAndDropLock.isDragAndDropStarted())
        {

            if (Math.abs(firstPosition.x - pnt.x) > 3 || Math.abs(firstPosition.y - pnt.y) > 3)
            {
                 startDrag(e.getComponent(), e.getButton(), pnt);

            } else
            {
                return;
            }
        }

        Component c = e.getComponent();

        // Translate the Point from current component to the Main pane
        // which is the same size as the ghost pane
        JComponent rootPane = (JComponent)UICacheManager.get(UICacheManager.MAINPANE);
        Point      pp       = (Point) pnt.clone();
        SwingUtilities.convertPointToScreen(pp, c);
        SwingUtilities.convertPointFromScreen(pp, rootPane);

        // Find the component that under this point on the main pane
        Component dropComponent = SwingUtilities.getDeepestComponentAt(rootPane, pp.x, pp.y);

        boolean flavorOK = false;
        if (dropComponent instanceof GhostActionable && c != dropComponent)
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

        // NOw translate the point ot the glass pane
        Point p = (Point) pnt.clone();
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, glassPane);
        glassPane.setPoint(p);

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

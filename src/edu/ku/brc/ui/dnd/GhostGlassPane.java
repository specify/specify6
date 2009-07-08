/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.ui.dnd;

import java.awt.AlphaComposite;
import java.awt.Dimension;
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
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import javax.swing.Timer;

import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.ProgressGlassPane;
import edu.ku.brc.ui.RolloverCommand;

/**
 * Implements a transparent glass pane for the app so images can be dragged across
 *
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 
 * @code_status Beta
 **
 * @author rods
 * @author Romain Guy
 * @author S�bastien Petrucci <sebastien_petrucci@yahoo.fr>*
 *
 */
@SuppressWarnings("serial")
public class GhostGlassPane extends ProgressGlassPane
{
    public enum ImagePaintMode {CENTERED, DRAG, ABSOLUTE}
    private static final boolean DEBUG = false;
    
    private static GhostGlassPane instance = new GhostGlassPane();

    private final int   ANIMATION_DELAY = 500;
    private final float STD_ALPHA       = 0.7f;
    
    protected FadeOutAnimation fadeOutAnimation = null;
    protected Timer            fadeOutTimer     = null;

    protected BufferedImage dragged     = null;
    protected Point         location    = new Point(0, 0);
    protected Point         oldLocation = new Point(0, 0);
    protected MouseAdapter  mouseAdapter;

    protected int           width;
    protected int           height;
    protected Rectangle     visibleRect = null;

    protected float         zoom        = 1.0f;
    protected float         alpha       = STD_ALPHA;
    
    protected Point         newPnt      = new Point();
    protected Point         oldPnt      = new Point();
    protected Point         offset      = new Point();

    protected ImagePaintMode paintPositionMode = ImagePaintMode.DRAG;
    
    protected Vector<GhostActionable> actionables = new Vector<GhostActionable>();
    protected Vector<GhostActionable> enumList    = new Vector<GhostActionable>();
    
    /**
     * Default Constructor
     */
    protected GhostGlassPane()
    {
        setOpaque(false);
        
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                e.consume();
            }
            @Override
            public void mouseDragged(MouseEvent e)
            {
                e.consume();
            }
            @Override
            public void mouseEntered(MouseEvent e)
            {
                e.consume();
            }
            @Override
            public void mouseExited(MouseEvent e)
            {
                e.consume();
            }
            @Override
            public void mouseMoved(MouseEvent e)
            {
                e.consume();
            }
            @Override
            public void mousePressed(MouseEvent e)
            {
                e.consume();
            }
            @Override
            public void mouseReleased(MouseEvent e)
            {
                e.consume();
            }
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                e.consume();
            }
        };

    }
    
    /**
     * @return the singleton
     */
    public static GhostGlassPane getInstance()
    {
        return instance;
    }

    /**
     * Sets the image that will be draged on the glass pane
     * @param dragged the buffered image
     */
    public void setImage(final BufferedImage dragged)
    {
        setImage(dragged, dragged == null ? 0 : dragged.getWidth());
    }

    /**
     * Sets image with a specific width used for animation zooming
     * @param dragged the image
     * @param width the rect
     */
    public void setImage(BufferedImage dragged, int width)
    {
        if (dragged != null)
        {
            float ratio = (float) dragged.getWidth() / (float) dragged.getHeight();
            this.width = width;
            height = (int) (width / ratio);
        }

        this.dragged = dragged;
    }
    
    public void setOffset(final Point offset)
    {
        this.offset.setLocation(offset);
    }

    /**
     * @return the offset
     */
    public Point getOffset()
    {
        return offset;
    }

    /**
     * Sets the point for drawing
     * @param location the location
     */
    public void setPoint(Point location)
    {
        this.oldLocation  = this.location;
        this.location     = location;
        this.paintPositionMode = ImagePaintMode.DRAG;
    }

    /**
     * Sets the point for drawing and set the doingDragPainting to false so
     * it pains directly at the location
     * @param location the location
     * @param doingDragPainting set whether to paint at the location or paint centered at the location (false paints at the location)
     */
    public void setPoint(Point location, ImagePaintMode paintPositionMode)
    {
        this.oldLocation  = this.location;
        this.location     = location;
        this.paintPositionMode = paintPositionMode;
    }

    /**
     * Returns the point on the glass pane of the object being dragged.
     * @return the point on the glass pane of the object being dragged.
     */
    public Point getPoint()
    {
        return location;
    }

    /**
     * Return the rect for painting
     * @return Return the rect for painting
     */
    protected Rectangle getRepaintRect()
    {
        calcPoints();
        
        //int x = (int) (location.getX() - (width * zoom / 2)) - 5;
        //int y = (int) (location.getY() - (height * zoom / 2)) - 5;
        int x = newPnt.x - 5;
        int y = newPnt.y - 5;

        int x2 = oldPnt.x - 5;
        int y2 = oldPnt.y - 5;

        int w  = (int) (this.width * zoom + 10.0);
        int h  = (int) (this.height * zoom + 10.0);

        return new Rectangle(x, y, w, h).union(new Rectangle(x2, y2, w, h));
    }
    
    protected void calcPoints()
    {
        double widthZoom  = width * zoom;
        double heightZoom = height * zoom;
        
        newPnt.x = 0;
        newPnt.y = 0;
        if (paintPositionMode == ImagePaintMode.DRAG)
        {
            newPnt.x = (int) (location.getX() - (widthZoom / 2));
            newPnt.y = (int) (location.getY() - (heightZoom / 2));
            
            oldPnt.x = (int) (oldLocation.getX() - (widthZoom / 2));
            oldPnt.y = (int) (oldLocation.getY() - (heightZoom / 2));

        } else if (paintPositionMode == ImagePaintMode.CENTERED)
        {
            Dimension size = getSize();
            int w = size.width  - dragged.getWidth();
            int h = size.height - dragged.getHeight();
            
            newPnt.x = (w / 2) + (int)location.getX();
            newPnt.y = (h / 2) + (int)location.getY();

            oldPnt.x = (w / 2) + (int)oldLocation.getX();
            oldPnt.y = (h / 2) + (int)oldLocation.getY();

        }  else if (paintPositionMode == ImagePaintMode.ABSOLUTE)
        {
            newPnt.x = (int)location.getX() - offset.x;
            newPnt.y = (int)location.getY() - offset.y;

            oldPnt.x = (int)oldLocation.getX() - offset.x;
            oldPnt.y = (int)oldLocation.getY() - offset.y;
        }

    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        if (dragged == null || !isVisible())
        {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        double widthZoom  = width * zoom;
        double heightZoom = height * zoom;
        
        calcPoints();
        
        if (visibleRect != null)
        {
            g2.setClip(visibleRect);
        }

        RoundRectangle2D rectangle = new RoundRectangle2D.Double(newPnt.x - 1.0, newPnt.y - 1.0, widthZoom + 1.0, heightZoom + 1.0, 8.0, 8.0);
        
        if (visibleRect != null) 
        {
            Area clip = new Area(visibleRect);
            g2.setClip(clip);
            
        } else 
        {
            g2.setClip(rectangle);
        }

        g2.drawImage(dragged, newPnt.x, newPnt.y, (int)widthZoom, (int)heightZoom, null);
        g2.dispose();
    }

    /**
     * Set the alpha to a custmo value
     * @param alpha the new alpha value used to paint the dragged object
     */
    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
    }

    /**
     * Resets the alpha channel back to it's "standard" value
     */
    public void resetAlpha()
    {
        this.alpha = STD_ALPHA;
    }

    /**
     * Start animation where painting will occur for the given rect
     * @param visibleRectArg the rect to be painted
     */
    public void startAnimation(Rectangle visibleRectArg)
    {
        this.visibleRect = visibleRectArg;
        
        fadeOutAnimation = new FadeOutAnimation();
        fadeOutTimer     = new Timer(1000 / 30, fadeOutAnimation);
        fadeOutTimer.start();
    }
    
    /**
     * Cleanup after drag and drop 
     */
    public void finishedWithDragAndDrop()
    {
        setVisible(false);
        zoom        = 1.0f;
        alpha       = 0.6f;
        visibleRect = null;
        dragged     = null;
        DragAndDropLock.setLocked(false);  
        fadeOutAnimation = null;
        fadeOutTimer     = null;
    }
    
    /**
     * Sets active state for all items that match one of the drag flavors.
     * @param dragActionable the item
     */
    protected boolean isDropOK(final GhostActionable dragActionable, final GhostActionable dropActionable)
    {
        List<DataFlavor> dragList = dragActionable.getDragDataFlavors();
        if (DEBUG)
        {
            String dragTitle = dragActionable instanceof RolloverCommand ? ((RolloverCommand)dragActionable).getTitle() : dragActionable.getClass().getSimpleName();
            String dropTitle = dropActionable instanceof RolloverCommand ? (((RolloverCommand)dropActionable).getTitle()) : dropActionable.getClass().getSimpleName();
            System.out.println("\n\n*********** Drag["+dragTitle+"]  Drop["+dropTitle+"] ******** Num Drag Fl["+dragList.size()+"]");
            for (DataFlavor dragFlavor : dragList)
            {
                System.out.print("DragFlavor["+dragFlavor.getHumanPresentableName()+"] iof DataFlavorTableExt: "+(dragFlavor instanceof DataFlavorTableExt)+" ");
                if (dragFlavor instanceof DataFlavorTableExt)
                {
                    for (Integer id : ((DataFlavorTableExt)dragFlavor).getTableIds())
                    {
                        System.out.print(id+" ");
                    }
                }
                System.out.println();
            }
            System.out.println("***********\n\ndropActionable "+dropActionable);
        }
        if (dragList != null)
        {
            for (DataFlavor dragFlavor : dragList)
            {
                if (DEBUG) System.out.println("------ dragFlavor "+dragFlavor.getHumanPresentableName()+" ---------- Num Drop Flavs: "+dropActionable.getDropDataFlavors().size());
                for (DataFlavor dropFlavor : dropActionable.getDropDataFlavors())
                {
                    if (DEBUG) 
                    {
                        System.out.println("Drag["+dragFlavor.getHumanPresentableName()+"] drop["+dropFlavor.getHumanPresentableName()+"] Can DROP["+dragFlavor.equals(dropFlavor)+"]");
                        System.out.print("Drag iof DFTE ["+(dragFlavor instanceof DataFlavorTableExt)+"] drop iof DFTE ["+(dropFlavor instanceof DataFlavorTableExt)+"] ");
                        if (dropFlavor instanceof DataFlavorTableExt)
                        {
                            for (Integer id : ((DataFlavorTableExt)dropFlavor).getTableIds())
                            {
                                System.out.print(id+" ");
                            }
                        }
                        System.out.println();
                    }
                    /*if (!(dragFlavor instanceof DataFlavorTableExt) && !(dropFlavor instanceof DataFlavorTableExt))
                    {
                        System.out.println(dragFlavor.equals(dropFlavor));
                        if (dragFlavor.getHumanPresentableName().equals("InfoRequest") || dropFlavor.getHumanPresentableName().equals("InfoRequest"))
                        {
                            int x = 0;
                            x++;
                        }
                    }*/

                    if (dragFlavor.equals(dropFlavor))
                    {
                        if (DEBUG) 
                        {
                            System.out.println("Drag iof DFTE ["+(dragFlavor instanceof DataFlavorTableExt)+"] drop iof DFTE ["+(dropFlavor instanceof DataFlavorTableExt)+"] ");
                            System.out.println("Drag["+dragFlavor.getHumanPresentableName()+"] drop["+dropFlavor.getHumanPresentableName()+"]");
                            System.out.println("!!! Drop is OK");
                        }
                        return true;
                    }
                }
                if (DEBUG) System.out.println("------ ------------------------ ----------");
            }
        }
        if (DEBUG) System.out.println("!!! Drop NOT OK");
        return false;
    }
    
    public synchronized void finishDnD()
    {
        if (fadeOutAnimation != null)
        {
            fadeOutTimer.stop();
            fadeOutAnimation.setStopNow(true);
            finishedWithDragAndDrop();
        }
    }
    
    /**
     * Sets active state for all items that match one of the drag flavors.
     * @param dragActionable the item
     */
    public void startDrag(final GhostActionable dragActionable)
    {
        //System.out.println("actionables "+actionables.size());
        for (GhostActionable dropActionable : actionables)
        {
            if (dragActionable != dropActionable)
            {
                boolean isActive = isDropOK(dragActionable, dropActionable);
                dropActionable.setActive(isActive);
                if (dropActionable instanceof RolloverCommand)
                {
                    RolloverCommand rc = (RolloverCommand)dropActionable;
                    //System.out.println(">>>>> "+rc.getTitle()+"  "+isActive);
                    rc.repaint();
                } else
                {
                    int x = 0;
                    x++;
                }
            }
        }
    }
    
    /**
     * Resets active state after drag.
     */
    public void stopDrag()
    {
        for (GhostActionable dropActionable : actionables)
        {
            dropActionable.setActive(false);
        }
    }
    
    /**
     * Adds the a actionable to be tracked.
     * @param actionable the actionable
     */
    public void add(final GhostActionable actionable)
    {
        //System.out.println("adding ["+(actionable instanceof RolloverCommand ? (((RolloverCommand)actionable).getTitle()) : actionable.getClass().getSimpleName())+"]");

        actionables.add(actionable);
    }

    /**
     * Removes the a actionable from being tracked.
     * @param actionable the actionable
     */
    public void remove(final GhostActionable actionable)
    {
        actionables.remove(actionable);
    }
    
    /**
     * Clears all the actionables. 
     */
    public void clearActionableList()
    {
        actionables.clear();
    }

    /**
     * @param isMaskingEvents the isMaskingEvents to set
     */
    public void setMaskingEvents(boolean isMaskingEvents)
    {
        if (isMaskingEvents)
        {
            addMouseListener(mouseAdapter);
        } else
        {
            removeMouseListener(mouseAdapter);
        }
    }

    //------------------------------------------------------------
    // Inner Class
    //------------------------------------------------------------
    private class FadeOutAnimation implements ActionListener
    {
        private long    start;
        private boolean stopNow = false;

        FadeOutAnimation()
        {
            this.start = System.currentTimeMillis();
            oldLocation = location;
        }

        /**
         * @param stopNow the stopNow to set
         */
        public synchronized void setStopNow(boolean stopNow)
        {
            this.stopNow = stopNow;
        }

        public void actionPerformed(ActionEvent e)
        {

            long elapsed = System.currentTimeMillis() - start;
            if (stopNow || elapsed > ANIMATION_DELAY)
            {
                ((Timer) e.getSource()).stop();
                finishedWithDragAndDrop();
                
            } else
            {
                alpha = 0.6f - (0.6f * elapsed / ANIMATION_DELAY);
                zoom = 1.0f + 3.0f * (elapsed / (float) ANIMATION_DELAY);
            }

            repaint(getRepaintRect());
        }
    }
}

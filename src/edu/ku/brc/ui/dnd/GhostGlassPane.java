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
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.Timer;

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
public class GhostGlassPane extends JPanel
{
    public enum ImagePaintMode {CENTERED, DRAG, ABSOLUTE}

    private final int   ANIMATION_DELAY = 500;
    private final float STD_ALPHA       = 0.7f;

    protected BufferedImage dragged     = null;
    protected Point         location    = new Point(0, 0);
    protected Point         oldLocation = new Point(0, 0);

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
    public GhostGlassPane()
    {
        setOpaque(false);
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
        new Timer(1000 / 30, new FadeOutAnimation()).start();
    }
    
    /**
     * Cleanup after drag and drop 
     */
    public void finishedWithDragAndDrop()
    {
        setVisible(false);
        zoom = 1.0f;
        alpha = 0.6f;
        visibleRect = null;
        dragged = null;
        DragAndDropLock.setLocked(false);  
    }
    
    /**
     * Sets active state for all items that match one of the drag flavors.
     * @param dragActionable the item
     */
    public boolean isDropOK(final GhostActionable dragActionable, final GhostActionable dropActionable)
    {
        List<DataFlavor> dragList = dragActionable.getDragDataFlavors();
        //for (DataFlavor dragFlavor : dragList)
        //{
        //    System.out.println("["+dragFlavor.getHumanPresentableName()+"]");
        //}
        
        //System.out.println("\n\ndropActionable "+dropActionable);
        for (DataFlavor dragFlavor : dragList)
        {
            //System.out.println("------ dragFlavor "+dragFlavor.getHumanPresentableName()+" ----------");
            for (DataFlavor dropFlavor : dropActionable.getDropDataFlavors())
            {
                //System.out.println("Drag["+dragFlavor.getHumanPresentableName()+"] drop["+dropFlavor.getHumanPresentableName()+"]");
                if (dragFlavor.getHumanPresentableName().equals(dropFlavor.getHumanPresentableName()))
                {
                    return true;
                }
            }
        }

        return false;
    }
    
    /**
     * Sets active state for all items that match one of the drag flavors.
     * @param dragActionable the item
     */
    public void startDrag(final GhostActionable dragActionable)
    {
        for (GhostActionable dropActionable : actionables)
        {
            dropActionable.setActive(isDropOK(dragActionable, dropActionable)); 
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

    //------------------------------------------------------------
    // Inner Class
    //------------------------------------------------------------
    private class FadeOutAnimation implements ActionListener
    {
        private long start;

        FadeOutAnimation()
        {
            this.start = System.currentTimeMillis();
            oldLocation = location;
        }

        public void actionPerformed(ActionEvent e)
        {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > ANIMATION_DELAY)
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

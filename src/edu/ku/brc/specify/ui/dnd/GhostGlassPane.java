package edu.ku.brc.specify.ui.dnd;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Implements a transparent glass pane for the app so images can be dragged across
 *
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 *
 * @author rods
 * @author Romain Guy <romain.guy@mac.com>
 * @author S�bastien Petrucci <sebastien_petrucci@yahoo.fr>*
 *
 */
@SuppressWarnings("serial")
public class GhostGlassPane extends JPanel
{
    public enum ImagePaintMode {CENTERED, DRAG, ABSOLUTE};

    private final int   ANIMATION_DELAY = 500;
    private final float STD_ALPHA       = 0.7f;

    private BufferedImage dragged = null;
    private Point location = new Point(0, 0);
    private Point oldLocation = new Point(0, 0);

    private int width;
    private int height;
    private Rectangle visibleRect = null;

    private float zoom  = 1.0f;
    private float alpha = STD_ALPHA;

    private ImagePaintMode paintPositionMode = ImagePaintMode.DRAG;

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
    public void setImage(BufferedImage dragged)
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
     * Return the rect for painting
     * @return Return the rect for painting
     */
    protected Rectangle getRepaintRect()
    {
        int x = (int) (location.getX() - (width * zoom / 2)) - 5;
        int y = (int) (location.getY() - (height * zoom / 2)) - 5;

        int x2 = (int) (oldLocation.getX() - (width * zoom / 2)) - 5;
        int y2 = (int) (oldLocation.getY() - (height * zoom / 2)) - 5;

        int width = (int) (this.width * zoom + 10.0);
        int height = (int) (this.height * zoom + 10.0);

        return new Rectangle(x, y, width, height).union(new Rectangle(x2, y2, width, height));
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

        int x = 0;
        int y = 0;
        if (paintPositionMode == ImagePaintMode.DRAG)
        {
            x = (int) (location.getX() - (width * zoom / 2));
            y = (int) (location.getY() - (height * zoom / 2));

        } else if (paintPositionMode == ImagePaintMode.CENTERED)
        {
            Dimension size = getSize();
            x = ((size.width - dragged.getWidth()) / 2) + (int)location.getX();
            y = ((size.height - dragged.getHeight()) / 2) + (int)location.getY();

        }  else if (paintPositionMode == ImagePaintMode.ABSOLUTE)
        {
            x = (int)location.getX();
            y = (int)location.getY();

        }

        if (visibleRect != null)
        {
            g2.setClip(visibleRect);
        }

        RoundRectangle2D rectangle = new RoundRectangle2D.Double(x - 1.0, y - 1.0,
                                                                 (double) width * zoom + 1.0,
                                                                 (double) height * zoom + 1.0,
                                                                 8.0, 8.0);
        if (visibleRect != null) {
            Area clip = new Area(visibleRect);
            g2.setClip(clip);
        } else {
            g2.setClip(rectangle);
        }

        g2.drawImage(dragged, x, y, (int) (width * zoom), (int) (height * zoom), null);
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
     * @param visibleRect the rect to be painted
     */
    public void startAnimation(Rectangle visibleRect)
    {
        this.visibleRect = visibleRect;
        new Timer(1000 / 30, new FadeOutAnimation()).start();
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
                setVisible(false);
                zoom = 1.0f;
                alpha = 0.6f;
                visibleRect = null;
                dragged = null;
                DragAndDropLock.setLocked(false);
            } else
            {
                alpha = 0.6f - (0.6f * (float) elapsed / (float) ANIMATION_DELAY);
                zoom = 1.0f + 3.0f * ((float) elapsed / (float) ANIMATION_DELAY);
            }
            repaint(getRepaintRect());
        }
    }

}
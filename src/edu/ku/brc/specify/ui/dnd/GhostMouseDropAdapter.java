package edu.ku.brc.specify.ui.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

/**
 * Implements the mousePressed and release methods to start and stop the drag
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 * 
 * @author rods
 * @author Romain Guy <romain.guy@mac.com>
 * @author Sébastien Petrucci <sebastien_petrucci@yahoo.fr>*
 */
public class GhostMouseDropAdapter extends GhostDropAdapter 
{
    private GhostActionable ghostActionable;

    /**
     * 
     * @param glassPane the glass pane 
     * @param action the action command
     * @param image the image to be drawn on the glassPane
     */
    public GhostMouseDropAdapter(GhostGlassPane glassPane,
                               String           action,
                               GhostActionable  ghostActionable) 
    {
        super(glassPane, action);
        this.ghostActionable = ghostActionable;
    }


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) 
    {
        if (DragAndDropLock.isLocked() || e.getButton() != 1) 
        {
            DragAndDropLock.setDragAndDropStarted(false);
            return;
        }
        DragAndDropLock.setLocked(true);
        DragAndDropLock.setDragAndDropStarted(true);

        Component c = e.getComponent();

        glassPane.setVisible(true);

        Point p = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, glassPane);

        BufferedImage bi = ghostActionable.getBufferedImage();
        glassPane.setPoint(p);
        glassPane.setImage(bi, bi.getWidth());
        glassPane.repaint();
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) 
    {
        if (e.getButton() != 1 || !DragAndDropLock.isDragAndDropStarted()) 
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

        boolean clearIt = true;
        if (hasListeners())
        {
            GhostDropEvent gde = new GhostDropEvent(c, action, eventPoint);
            fireGhostDropEvent(gde);
            if (gde.isConsumed())
            {
                clearIt = false;
            }
        }
        
        if (clearIt)
        {
            glassPane.setImage(null); 
            glassPane.setVisible(false);
            DragAndDropLock.setLocked(false);
        }
    }
}
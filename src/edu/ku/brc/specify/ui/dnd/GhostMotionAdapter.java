package edu.ku.brc.specify.ui.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.SwingUtilities;

/**
 * Class for making sure the image gets drawn on the glass pane while the image gets dragged on the glass pane
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 * 
 * @author rods
 * @author Romain Guy <romain.guy@mac.com>
 * @author Sébastien Petrucci <sebastien_petrucci@yahoo.fr>*
 *
 */
public class GhostMotionAdapter extends MouseMotionAdapter 
{
    private GhostGlassPane glassPane;

    /**
     * Contrsucts with the glass pane needed for drawing during the drag
     * @param glassPane
     */
    public GhostMotionAdapter(GhostGlassPane glassPane) 
    {
        this.glassPane = glassPane;
    }

    /**
     * Updates (draws) the glass pane as the image is dragged
     */
    public void mouseDragged(MouseEvent e) 
    {
        if (!DragAndDropLock.isDragAndDropStarted()) 
        {
            return;
        }
        
        Component c = e.getComponent();

        Point p = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, glassPane);
        glassPane.setPoint(p);

        glassPane.repaint(glassPane.getRepaintRect());
    }
}
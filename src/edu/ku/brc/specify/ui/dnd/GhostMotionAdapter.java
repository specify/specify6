package edu.ku.brc.specify.ui.dnd;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import edu.ku.brc.specify.ui.UICacheManager;

/**
 * Class for making sure the image gets drawn on the glass pane while the image gets dragged on the glass pane
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 * 
 * @author rods
 * @author Romain Guy <romain.guy@mac.com>
 * @author S�bastien Petrucci <sebastien_petrucci@yahoo.fr>*
 *
 */
public class GhostMotionAdapter extends MouseMotionAdapter 
{
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    private static final Cursor DEF_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    
    private GhostGlassPane glassPane;
    private Cursor         currCursor = null; 

    /**
     * Contrsucts with the glass pane needed for drawing during the drag
     * @param glassPane
     */
    public GhostMotionAdapter(GhostGlassPane glassPane) 
    {
        this.glassPane = glassPane;
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
    public void mouseDragged(MouseEvent e) 
    {
        if (!DragAndDropLock.isDragAndDropStarted()) 
        {
            return;
        }
        
        Component c = e.getComponent();
        
        JComponent rootPane = (JComponent)UICacheManager.get(UICacheManager.MAINPANE);
        Point pp = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(pp, c);
        SwingUtilities.convertPointFromScreen(pp, rootPane);

        boolean flavorOK = false;
        // find the component that under this point
        Component dropComponent = SwingUtilities.getDeepestComponentAt(rootPane, pp.x, pp.y);
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
        
        Point p = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, glassPane);
        glassPane.setPoint(p);

        glassPane.repaint(glassPane.getRepaintRect());
    }
}
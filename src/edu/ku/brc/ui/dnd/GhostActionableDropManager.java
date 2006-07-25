package edu.ku.brc.ui.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Watches for the ghost image to be dropped and then negoiates between the source and the destination calls
 * doAction on the setination GhostActionale
 * 
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 * @author Romain Guy
 * @author S�bastien Petrucci <sebastien_petrucci@yahoo.fr>*
 * @author rods
 *
 */
public class GhostActionableDropManager extends AbstractGhostDropManager 
{
    private GhostActionable ghostActionable;
    private GhostGlassPane  glassPane;

    /**
     * Constructs a drop zone
     * @param glassPane the glass pane to animate on drop
     * @param target the target JComponent 
     * @param ghostActionable the destination actionable
     */
    public GhostActionableDropManager(GhostGlassPane glassPane, JComponent target, GhostActionable ghostActionable) 
    {
        super(target);
        this.glassPane       = glassPane;
        this.ghostActionable = ghostActionable;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostDropListener#ghostDropped(edu.ku.brc.ui.dnd.GhostDropEvent)
     */
    public void ghostDropped(GhostDropEvent e) 
    {
        Point p = getTranslatedPoint(e.getDropLocation());

        if (isInTarget(p)) 
        {
            BufferedImage bi = ghostActionable.getBufferedImage();
            glassPane.setImage(bi, bi.getWidth());
            glassPane.startAnimation(SwingUtilities.convertRectangle(component,
                                                                    component.getVisibleRect(),
                                                                    glassPane));
            Component source = e.getComponent();
           
            if (component instanceof GhostActionable && source instanceof GhostActionable)
            {
                ((GhostActionable)component).doAction((GhostActionable)source);
            }
            e.setConsumed(true);
           
        }  else if (component.isShowing()) 
        {
            // xxx ?? Not sure what to do with this now ??
            
           //glassPane.setImage(null);
           //glassPane.setVisible(false);
           //DragAndDropLock.setLocked(false);
        }
    }
}

package edu.ku.brc.ui.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import edu.ku.brc.ui.UICacheManager;

/**
 * Implements the mousePressed and release methods to start and stop the drag
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 * @author Romain Guy <romain.guy@mac.com>
 * @author S�bastien Petrucci <sebastien_petrucci@yahoo.fr>*
 */
public class GhostMouseDropAdapter extends GhostDropAdapter 
{
    private GhostActionable    ghostActionable;

    /**
     * Constructor
     * @param glassPane the glass pane 
     * @param action the action command
     * @param ghostActionable the actionable
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
}

package edu.ku.brc.specify.ui.dnd;

import java.awt.Point;
import java.awt.Component;

/**
 * 
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 * 
 * @author rods
 * @author Romain Guy <romain.guy@mac.com>
 * @author Sébastien Petrucci <sebastien_petrucci@yahoo.fr>*
 *
 */
public class GhostDropEvent 
{
    private Point point;
    private String action;
    private Component component;
    private boolean   isConsumed = false;

    public GhostDropEvent(Component component, String action, Point point) 
    {
        this.action = action;
        this.point = point;
        this.component = component;
    }

    public String getAction() 
    {
        return action;
    }

    public Point getDropLocation() 
    {
        return point;
    }

    public Component getComponent()
    {
        return component;
    }

    public boolean isConsumed()
    {
        return isConsumed;
    }

    public void setConsumed(boolean isConsumed)
    {
        this.isConsumed = isConsumed;
    }
    
}

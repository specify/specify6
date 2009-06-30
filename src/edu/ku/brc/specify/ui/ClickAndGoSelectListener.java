/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package edu.ku.brc.specify.ui;

import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.view.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.Logging;

/**
 * Handles view 'fly to' on left clicked picked objects with a position.
 *
 * @author Patrick Murris
 * @version $Id: ClickAndGoSelectListener.java 5897 2008-08-09 05:20:21Z tgaskins $
 */
public class ClickAndGoSelectListener  implements SelectListener
{

    private final WorldWindow wwd;
    private final Class<?>    pickedObjClass;    // Which picked object class do we handle
    private final double      elevationOffset;  // Meters above the target position

    public ClickAndGoSelectListener(WorldWindow wwd, Class<?> pickedObjClass)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.pickedObjClass = pickedObjClass;
        this.elevationOffset = 0d;
    }

    public ClickAndGoSelectListener(WorldWindow wwd, Class<?> pickedObjClass, double elevationOffset)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (pickedObjClass == null)
        {
            String msg = Logging.getMessage("nullValue.ClassIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.pickedObjClass = pickedObjClass;
        this.elevationOffset = elevationOffset;
    }

    /**
     * Select Listener implementation.
     *
     * @param event the SelectEvent
     */
    public void selected(SelectEvent event)
    {
        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
        {
            // This is a left click
            if (event.hasObjects() && event.getTopPickedObject().hasPosition())
            {
                System.err.println(event.getTopObject().getClass());
                // There is a picked object with a position
                if (event.getTopObject().getClass().equals(pickedObjClass)
                        && this.wwd.getView() instanceof OrbitView)
                {
                    // This object class we handle and we have an orbit view
                    Position targetPos = event.getTopPickedObject().getPosition();
                    OrbitView view = (OrbitView)this.wwd.getView();
                    Globe globe = this.wwd.getModel().getGlobe();
                    if(globe != null && view != null)
                    {
                        // Use a PanToIterator to iterate view to target position
                        view.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                            // The elevation component of 'targetPos' here is not the surface elevation,
                            // so we ignore it when specifying the view center position.
                            view, globe, new Position(targetPos, 0),
                            Angle.ZERO, Angle.ZERO, targetPos.getElevation() + this.elevationOffset));
                    }
                }
            }
        }
    }

}

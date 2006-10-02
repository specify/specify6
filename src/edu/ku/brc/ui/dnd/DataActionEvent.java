package edu.ku.brc.ui.dnd;

import java.awt.event.ActionEvent;
/**
 * 
 * An DataAction Event that knows the source and destination and can carry a data object with it.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class DataActionEvent extends ActionEvent
{
    protected GhostActionable sourceObj = null;
    protected GhostActionable destObj   = null;
    protected Object          data      = null;

    /**
     * Constructor.
     * @param source the source object, the object that initiated the drag-n-drop.
     * @param destination the destination object, the object it was dropped on.
     * @param data the data of the drag-n-drop
     */
    public DataActionEvent(final GhostActionable sourceObj, final GhostActionable destObj, final Object data)
    {
        super(sourceObj, 1, null);
        
        this.sourceObj = sourceObj;
        this.destObj   = destObj;
        this.data      = data;
    }
    
    /**
     * Returns the data object.
     * @return the data object.
     */
    public Object getData()
    {
        return data;
    }

    /**
     * Returns the destination object (this is typically the object that the object was dropped on).
     * @return the destination object
     */
    public GhostActionable getDestObj()
    {
        return destObj;
    }

    /**
     * Sets the destination object (this is usually called after the drop).
     * @param destObj the destination object.
     */
    public void setDestObj(final GhostActionable destObj)
    {
        this.destObj = destObj;
    }

    /**
     * Returns the source object (this is typically the object that the object was dropped on).
     * @return the source object
     */
    public GhostActionable getSourceObj()
    {
        return sourceObj;
    }

}

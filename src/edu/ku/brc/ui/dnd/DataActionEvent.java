package edu.ku.brc.ui.dnd;

import java.awt.event.ActionEvent;
/**
 * 
 * An ActionEvent that can carry a data object with it
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class DataActionEvent extends ActionEvent
{
    protected Object data = null;

    public DataActionEvent(Object arg0, Object data)
    {
        super(arg0, 1, null);
        this.data = data;
    }
    
    public Object getData()
    {
        return data;
    }

}

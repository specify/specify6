package edu.ku.brc.specify.ui.dnd;

import java.awt.event.ActionEvent;
/**
 * 
 * @author rods
 *
 */
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

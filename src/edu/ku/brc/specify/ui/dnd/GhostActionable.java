package edu.ku.brc.specify.ui.dnd;

import java.awt.image.BufferedImage;

/**
 * 
 * @author rods
 *
 */
public interface GhostActionable
{

    /**
     * Asks the object to perform its action
     * @param data a generic data object to be passed
     */
    public void doAction(Object data);
    
    /**
     * 
     * @param data
     */
    public void setData(final Object data);
    
    /**
     * 
     * @return
     */
    public Object getData();
    
    /**
     * 
     *
     */
    public void createMouseDropAdapter();
    
    /**
     * Returns the adaptor for tracking mouse drop gestures
     * @return Returns the adaptor for tracking mouse drop gestures
     */
    public GhostMouseDropAdapter getMouseDropAdapter();


    /**
     * Returns a BufferedImage representing a "snapshot" of what the UI looks like before a Drag
     * @return Returns a BufferedImage representing a "snapshot" of what the UI looks like before a Drag
     */
    public BufferedImage getBufferedImage();
}

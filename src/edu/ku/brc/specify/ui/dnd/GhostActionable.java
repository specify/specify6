package edu.ku.brc.specify.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 
 * @author rods
 *
 */
public interface GhostActionable
{

    /**
     * Asks the destination to perform its action amd it is given the source
     * @param source the source object that caused the action to happen
     */
    public void doAction(GhostActionable source);
    
    /**
     * Set the data into the objet
     * @param data
     */
    public void setData(final Object data);
    
    /**
     * Return the data
     * @return Return the data
     */
    public Object getData();
    
    /**
     * Creates a adpator for the DnD action 
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
    
    
    /**
     * Returns the DataFlavor that this accept as a Drop 
     * @return Returns the DataFlavor that this accept as a Drag or Drop 
     */
    public List<DataFlavor> getDropDataFlavors();
    
    /**
     * Returns the Drag flavor
     * @return Returns the Drag flavor
     */
    public List<DataFlavor> getDragDataFlavors();
    
}

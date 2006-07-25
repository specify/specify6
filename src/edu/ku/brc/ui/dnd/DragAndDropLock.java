package edu.ku.brc.ui.dnd;

import java.util.concurrent.atomic.AtomicBoolean;
/**
 * 
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 * @author Romain Guy
 * @author Sebastien Petrucci <sebastien_petrucci@yahoo.fr>*
 *
 */
public class DragAndDropLock {
    private static AtomicBoolean locked = new AtomicBoolean(false);
    private static AtomicBoolean startedDnD = new AtomicBoolean(false);
    
    public static boolean isLocked() {
        return locked.get();
    }
    
    public static void setLocked(boolean isLocked) {
        locked.set(isLocked);
    }
    
    public static boolean isDragAndDropStarted() {
        return startedDnD.get();
    }
    
    public static void setDragAndDropStarted(boolean isLocked) {
        startedDnD.set(isLocked);
    }
}

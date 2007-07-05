package edu.ku.brc.ui;

/**
 * Provides callbacks for drop events on JLists.
 *
 * @code_status Complete
 * @author jstewart
 */
public interface DragDropCallback
{
    /**
     * Performs all tasks required by the event of <code>dragged</code>
     * being dropped on <code>droppedOn</code>.
     *
     * @param draggedObj the dragged object
     * @param dropLocObj the object that <code>dragged</code> was dropped on
     * @param dropAction the type of drop action
     * @return true if the drop was successfully handled
     */
    public boolean dropOccurred(Object dragged, Object droppedOn, int dropAction);
    
    /**
     * Determines if a drop of <code>dragged</code> being dropped on <code>droppedOn</code>
     * is acceptable.
     * 
     * @param dragged the dragged object
     * @param droppedOn the object that <code>dragged</code> is being dropped on
     * @param dropAction the type of drop action
     * @return true if the drop is acceptable
     */
    public boolean dropAcceptable(Object dragged, Object droppedOn, int dropAction);
}

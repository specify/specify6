package edu.ku.brc.ui;

/**
 * Provides callbacks for drop events on JLists.
 *
 * @author jstewart
 * @version %I% %G%
 */
public interface DragDropCallback
{
	/**
	 * Performs all tasks required by the event of <code>dragged</code>
	 * being dropped on <code>droppedOn</code>.
	 *
	 * @param draggedObj the dragged object
	 * @param dropLocObj the object that <code>dragged</code> was dropped on
	 */
	public void dropOccurred(Object dragged, Object droppedOn );
}

/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import javax.swing.ListModel;

import edu.ku.brc.ui.DragDropCallback;
import edu.ku.brc.ui.DragDropJList;

/**
 * A custom JList that is capable of ignoring clicks on list items in order to
 * modify item selection behavior from the mouse.  An "outside" MouseListener
 * would determine if the click should trigger a change in selection or not.  If not,
 * the listener should call {@link #setClickOnText(boolean)} with a value of <code>false</code>.
 *
 * NOTE: the workings of this class <i>MIGHT</i> be dependent on the order of MouseListeners
 * that are being processed.  If the "outside" MouseListener is registered on a parent component,
 * it should fire before the internal MouseListener used by the JList to notify the
 * ListSelectionModel of user selection changes.
 * 
 * @code_status Beta
 * 
 * @author jstewart
 */
@SuppressWarnings("serial")
public class TreeDataGhostDropJList extends DragDropJList
{
	protected boolean clickOnText;
	
	/**
	 * @param model
	 * @param dragDropCallback
	 * @param isDraggable
	 */
	public TreeDataGhostDropJList(final ListModel model, final DragDropCallback dragDropCallback, final boolean isDraggable)
	{
		super(model, dragDropCallback, isDraggable);
		clickOnText = false;
	}
	
	public void setClickOnText(boolean onText)
	{
		clickOnText = onText;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JList#setSelectedIndices(int[])
	 */
	@Override
	public void setSelectedIndices(int[] indices)
	{
		if( clickOnText )
		{
            super.setSelectedIndices(indices);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JList#setSelectedValue(java.lang.Object, boolean)
	 */
	@Override
	public void setSelectedValue(Object anObject, boolean shouldScroll)
	{
		if( clickOnText )
		{
			super.setSelectedValue(anObject,shouldScroll);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JList#setSelectionInterval(int, int)
	 */
	@Override
	public void setSelectionInterval(int anchor, int lead)
	{
		if( clickOnText )
		{
			super.setSelectionInterval(anchor,lead);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JList#setSelectedIndex(int)
	 */
	@Override
	public void setSelectedIndex(int index)
	{
		if( clickOnText )
		{
			super.setSelectedIndex(index);
		}
	}
}

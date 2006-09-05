/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 * @status CodeFreez
 * @code_status Unknown (auto-generated)
 *
 * @author jstewart
 * @version %I% %G%
 */
@SuppressWarnings("serial")
public class TreeDataGhostDropJList extends DragDropJList
{
	protected boolean clickOnText;
	
	/**
	 *
	 *
	 * @param model
	 * @param dragDropCallback
	 */
	public TreeDataGhostDropJList(ListModel model, DragDropCallback dragDropCallback)
	{
		super(model,dragDropCallback);
		clickOnText = false;
	}
	
	public void setClickOnText(boolean onText)
	{
		clickOnText = onText;
	}
	
	/**
	 *
	 *
	 * @see javax.swing.JList#setSelectedIndices(int[])
	 * @param indices
	 */
	@Override
	public void setSelectedIndices(int[] indices)
	{
		if( clickOnText )
		{
			return;
		}
		clickOnText = true;
	}

	/**
	 *
	 *
	 * @see javax.swing.JList#setSelectedValue(java.lang.Object, boolean)
	 * @param anObject
	 * @param shouldScroll
	 */
	@Override
	public void setSelectedValue(Object anObject, boolean shouldScroll)
	{
		if( clickOnText )
		{
			super.setSelectedValue(anObject,shouldScroll);
		}
		clickOnText = true;
	}

	/**
	 *
	 *
	 * @see javax.swing.JList#setSelectionInterval(int, int)
	 * @param anchor
	 * @param lead
	 */
	@Override
	public void setSelectionInterval(int anchor, int lead)
	{
		if( clickOnText )
		{
			super.setSelectionInterval(anchor,lead);
		}
		clickOnText = true;
	}

	/**
	 *
	 *
	 * @see javax.swing.JList#setSelectedIndex(int)
	 * @param index
	 */
	@Override
	public void setSelectedIndex(int index)
	{
		if( clickOnText )
		{
			super.setSelectedIndex(index);
		}
		clickOnText = true;
	}
}

/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.ui.treetables;

import java.awt.Dimension;
import java.awt.Graphics;

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
	public TreeDataGhostDropJList(final ListModel<?> model, final DragDropCallback dragDropCallback, final boolean isDraggable)
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

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        int       count = getModel().getSize();
        Dimension size  = getSize();
        TreeViewerNodeRenderer ren = (TreeViewerNodeRenderer)getCellRenderer();
        int       currPaintHeight  = getFixedCellHeight() * count;
        ren.getNodeUI().drawBackgroundColors(g, currPaintHeight, size.height-currPaintHeight);
    }
	
}

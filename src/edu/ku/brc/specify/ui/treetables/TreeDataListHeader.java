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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.util.Pair;

/**
 * A {@link JLabel} for displaying the names of columns of lists displaying data in
 * {@link TreeDataListModel}s.
 
 * @code_status Unknown (auto-generated)
 **
 * @author jstewart
 * @version %I% %G%
 */
@SuppressWarnings("serial")
public class TreeDataListHeader extends JPanel implements ListDataListener
{
	/** The associated JList. */
	protected JList list;
	/** The underlying data model for the list. */
	protected TreeDataListModel model;
	
	protected TreeDataListCellRenderer listCellRenderer;
	/** The label's text color. */
	protected Color textColor;
	
	/**
	 * Creates a header appropriate for labelling the columns of the given
	 * list (which represents the given data model).
	 *
	 * @param list the list
	 * @param tdlm the list's underlying data model
	 */
	public TreeDataListHeader( JList list, TreeDataListModel tdlm, TreeDataListCellRenderer listCellRenderer )
	{
		this.list = list;
		this.model = tdlm;
		this.listCellRenderer = listCellRenderer;
		
		model.addListDataListener(this);
		
		if( list.getFont() != null )
		{
			this.setFont(list.getFont());
		}
		this.setTextColor(list.getForeground());
		this.setBackground(list.getBackground());
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.getBackground());
        
        int w = this.getWidth();
        int h = this.getHeight();
        g2.fillRect(0,0,w,h);

        for( Integer rank: model.getVisibleRanks() )
		{
			TreeDefinitionItemIface defItem = model.getTreeDef().getDefItemByRank(rank);
			g.setColor(textColor);
			TreeDataListCellRenderer rend = (TreeDataListCellRenderer)list.getCellRenderer();
			Pair<Integer,Integer> textBounds = rend.getTextBoundsForRank(rank); 
			g.drawString(defItem.getName(),textBounds.first,getHeight()/2);
		}
	}

	/**
	 * Returns the text color.
	 *
	 * @see #setTextColor(Color)
	 * @return the text color
	 */
	public Color getTextColor()
	{
		return textColor;
	}

	/**
	 * Sets the text color.
	 *
	 * @see #getTextColor()
	 * @param textColor the text color
	 */
	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}

	/**
	 * Returns the width of the header.
	 *
	 * @see javax.swing.Icon#getIconWidth()
	 * @return the width of the header
	 */
	public int getWidth()
	{
		return list.getWidth();
	}

	/**
	 * Returns the height of the header.
	 *
	 * @see javax.swing.Icon#getIconHeight()
	 * @return the height of the header
	 */
	public int getHeight()
	{
		return list.getGraphics().getFontMetrics().getHeight()+20;
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(getWidth(),getHeight());
	}

	/**
	 * Repaints the header.
	 *
	 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
	 * @param e the triggering list data event
	 */
	public void contentsChanged(ListDataEvent e)
	{
		repaint();
	}

	/**
	 * Repaints the header.
	 *
	 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
	 * @param e the triggering list data event
	 */
	public void intervalAdded(ListDataEvent e)
	{
		repaint();
	}

	/**
	 * Repaints the header.
	 *
	 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
	 * @param e the triggering list data event
	 */
	public void intervalRemoved(ListDataEvent e)
	{
		repaint();
	}
}

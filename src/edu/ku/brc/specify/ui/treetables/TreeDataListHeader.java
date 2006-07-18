package edu.ku.brc.specify.ui.treetables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.treeutils.TreeTableUtils;
import edu.ku.brc.ui.TreeDataJList;
import edu.ku.brc.util.Pair;

/**
 * A {@link JLabel} for displaying the names of columns of {@link TreeDataJList}s.
 *
 * @author jstewart
 * @version %I% %G%
 */
@SuppressWarnings("serial")
public class TreeDataListHeader extends JLabel implements Icon, ListDataListener
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
		
		this.setIcon(this);
		
		if( list.getFont() != null )
		{
			this.setFont(list.getFont());
		}
		this.setTextColor(list.getForeground());
		this.setBackground(list.getBackground());
	}

	/**
	 * Paints the column headers as an {@link Icon}.
	 *
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 * @param c the component containing the icon
	 * @param g the graphics context
	 * @param x the x-coord to paint the icon at
	 * @param y the y-coord to paint the icon at
	 */
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.getBackground());
        
        int w = this.getWidth();
        int h = this.getHeight();
        g2.fillRect(0,0,w,h);

        for( Integer rank: model.getVisibleRanks() )
		{
			TreeDefinitionItemIface defItem = TreeTableUtils.getDefItemByRank(model.getTreeDef(), rank);
			g.setColor(textColor);
			TreeDataListCellRenderer rend = (TreeDataListCellRenderer)list.getCellRenderer();
			Pair<Integer,Integer> textBounds = rend.getTextBoundsForRank(rank); 
			g.drawString(defItem.getName(),x+textBounds.first,y+getIconHeight()/2);
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
	public int getIconWidth()
	{
		return list.getWidth();
	}

	/**
	 * Returns the height of the header.
	 *
	 * @see javax.swing.Icon#getIconHeight()
	 * @return the height of the header
	 */
	public int getIconHeight()
	{
		return list.getGraphics().getFontMetrics().getHeight()+50;
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(list.getWidth(),list.getGraphics().getFontMetrics().getHeight()+50);
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
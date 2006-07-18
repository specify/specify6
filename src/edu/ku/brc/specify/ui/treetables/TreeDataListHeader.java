package edu.ku.brc.specify.ui.treetables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.treeutils.TreeTableUtils;
import edu.ku.brc.ui.TreeDataJList;

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
	/** The width of the header label. */
	protected int width;
	/** The height of the header label. */
	protected int height;
	/** A mapping from the rank of the column to the number of leading pixels in front of its text label. */
	protected SortedMap<Integer, Integer> rankToIconWidthMap;
	/** Indicates if the most recent values in {@link #rankToIconWidthMap} are still valid. */
	protected boolean sizeValid;
	/** The label's text color. */
	protected Color textColor;
	
	/**
	 * Creates a header appropriate for labelling the columns of the given
	 * list (which represents the given data model).
	 *
	 * @param list the list
	 * @param tdlm the list's underlying data model
	 */
	public TreeDataListHeader( JList list, TreeDataListModel tdlm )
	{
		this.list = list;
		if( list.getFont() != null )
		{
			this.setFont(list.getFont());
		}
		this.model = tdlm;
		this.rankToIconWidthMap = new TreeMap<Integer, Integer>();
		model.addListDataListener(this);
		recalculateSize();
		this.setIcon(this);
		
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
		if( !sizeValid )
		{
			recalculateSize();
		}

		Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.getBackground());
        
        int w = this.getWidth();
        int h = this.getHeight();
        g2.fillRect(0,0,w,h);

        for( Integer rank: model.getVisibleRanks() )
		{
			TreeDefinitionItemIface defItem = TreeTableUtils.getDefItemByRank(model.getTreeDef(), rank);
			int xOffset = rankToIconWidthMap.get(rank);
			g.setColor(textColor);
			g.drawString(defItem.getName(), x+xOffset+5, y+getIconHeight()/2);
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
		if( !sizeValid )
		{
			recalculateSize();
		}
		return width;
	}

	/**
	 * Returns the height of the header.
	 *
	 * @see javax.swing.Icon#getIconHeight()
	 * @return the height of the header
	 */
	public int getIconHeight()
	{
		if( !sizeValid )
		{
			recalculateSize();
		}
		return height;
	}

	/**
	 * Recalculates all size information from the data in the list's underlying model.
	 */
	protected void recalculateSize()
	{
		Graphics g = list.getGraphics();
		if( g == null )
		{
			sizeValid = false;
			return;
		}
		
		FontMetrics fm = g.getFontMetrics();
		if( fm == null )
		{
			sizeValid = false;
			return;
		}
		int prevRanksWidths = 0;
		for( Integer rank: model.getVisibleRanks() )
		{
			// the icon size should be equal to the sum of the lengths of the longest strings
			// from each of the lower ranks
			
			rankToIconWidthMap.put(rank, prevRanksWidths);
			
			Integer longestStringLength = model.getLongestNamePixelLengthByRank(rank,fm,true);
			if( longestStringLength != null )
			{
				int spacerWidth = g.getFontMetrics().stringWidth("XXX");
				prevRanksWidths += longestStringLength.intValue() + spacerWidth;
			}
		}
		
		width = prevRanksWidths;
		height = fm.getHeight()+50;
		sizeValid = true;
	}

	/**
	 * Invalidates the sizes and requests a recalculation of the size information.
	 *
	 * @see #invalidateRecalculateAndUpdate()
	 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
	 * @param e the triggering list date event
	 */
	public void intervalAdded(ListDataEvent e)
	{
		invalidateRecalculateAndUpdate();
	}

	/**
	 * Invalidates the sizes and requests a recalculation of the size information.
	 *
	 * @see #invalidateRecalculateAndUpdate()
	 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
	 * @param e the triggering list date event
	 */
	public void intervalRemoved(ListDataEvent e)
	{
		invalidateRecalculateAndUpdate();
	}

	/**
	 * Invalidates the sizes and requests a recalculation of the size information.
	 *
	 * @see #invalidateRecalculateAndUpdate()
	 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
	 * @param e the triggering list date event
	 */
	public void contentsChanged(ListDataEvent e)
	{
		invalidateRecalculateAndUpdate();
	}
	
	/**
	 * Invalidates the sizes and requests a recalculation of the size information.
	 * A call to {@link #repaint()} is then made.
	 */
	protected void invalidateRecalculateAndUpdate()
	{
		sizeValid=false;
		recalculateSize();
		this.repaint();
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(list.getWidth(),height);
	}
}
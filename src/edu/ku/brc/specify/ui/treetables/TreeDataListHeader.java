/**
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.Color;
import java.awt.Component;
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
import edu.ku.brc.specify.helpers.TreeTableUtils;

@SuppressWarnings("serial")
public class TreeDataListHeader extends JLabel implements Icon, ListDataListener
{
	protected JList list;
	protected TreeDataListModel model;
	protected int width;
	protected int height;
	protected SortedMap<Integer, Integer> rankToIconWidthMap;
	protected boolean sizeValid;
	protected Color textColor;
	
	public TreeDataListHeader( JList list, TreeDataListModel tdlm )
	{
		this.list = list;
		this.model = tdlm;
		this.rankToIconWidthMap = new TreeMap<Integer, Integer>();
		model.addListDataListener(this);
		recalculateSize();
		this.setIcon(this);
		
		this.setTextColor(list.getForeground());
		this.setBackground(list.getBackground());
	}

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
        g2.fillRect(0,0, w,h);

        for( Integer rank: model.getVisibleRanks() )
		{
			TreeDefinitionItemIface defItem = TreeTableUtils.getDefItemByRank(model.getTreeDef(), rank);
			int xOffset = rankToIconWidthMap.get(rank);
			g.setColor(textColor);
			g.drawString(defItem.getName(), x+xOffset, y+getIconHeight()-g.getFontMetrics().getDescent());
		}
	}

	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}
	
	public Color getTextColor()
	{
		return textColor;
	}

	public int getIconWidth()
	{
		if( !sizeValid )
		{
			recalculateSize();
		}
		return width;
	}

	public int getIconHeight()
	{
		if( !sizeValid )
		{
			recalculateSize();
		}
		return height;
	}

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
				prevRanksWidths += longestStringLength.intValue() + 32;
			}
		}
		
		width = prevRanksWidths;
		height = fm.getHeight()+10;
		sizeValid = true;
	}

	public void intervalAdded(ListDataEvent e)
	{
		sizeValid=false;
		recalculateSize();
		this.repaint();
	}

	public void intervalRemoved(ListDataEvent e)
	{
		sizeValid=false;
		recalculateSize();
		this.repaint();
	}

	public void contentsChanged(ListDataEvent e)
	{
		sizeValid=false;
		recalculateSize();
		this.repaint();
	}
}
package edu.ku.brc.specify.ui.treetables;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.IconManager;

@SuppressWarnings("serial")
public class TreeDataListCellRenderer extends DefaultListCellRenderer implements ListDataListener
{
	protected TreeDataListModel model;
	protected JList list;
	protected boolean lengthsValid;
	protected SortedMap<Integer, Integer> rankWidthsMap;
	
	// amount of space between tree-lines and name of node
	protected int whitespace;
	
	protected Icon open;
	protected Icon closed;
	
	protected Treeable currentTreeable;
	
	public TreeDataListCellRenderer( JList list, TreeDataListModel listModel )
	{
		this.whitespace = 5;
		this.list = list;
		setModel(listModel);
		lengthsValid = false;
		
		rankWidthsMap = new TreeMap<Integer, Integer>();

		open   = IconManager.getIcon("Down",    IconManager.IconSize.Std16);
		closed = IconManager.getIcon("Forward", IconManager.IconSize.Std16);
		
		if(open==null)
		{
			open = new Icon()
			{
				public void paintIcon(Component c, Graphics g, int x, int y)
				{
					// draw a big minus sign
					g.drawLine(x+2, y+7, x+14, y+7);
					g.drawLine(x+2, y+8, x+14, y+8);
				}

				public int getIconWidth()
				{
					return 16;
				}

				public int getIconHeight()
				{
					return 16;
				}
			};
		}
		if(closed==null)
		{
			closed = new Icon()
			{
				public void paintIcon(Component c, Graphics g, int x, int y)
				{
					// draw a big plus sign
					g.drawLine(x+2, y+7, x+14, y+7);
					g.drawLine(x+2, y+8, x+14, y+8);
					
					g.drawLine(x+7, y+2, x+7, y+14);
					g.drawLine(x+8, y+2, x+8, y+14);
				}

				public int getIconWidth()
				{
					return 16;
				}

				public int getIconHeight()
				{
					return 16;
				}
			};
		}
	}
	
	public void setList( JList list )
	{
		this.list = list;
	}
	
	public JList getList()
	{
		return list;
	}
	
	public void setModel( TreeDataListModel tdlm )
	{
		if( model != null )
		{
			model.removeListDataListener(this);
		}
		
		model = tdlm;
		if( model != null )
		{
			model.addListDataListener(this);
		}
	}
	
	/**
	 * @return Returns the closed.
	 */
	public Icon getClosed()
	{
		return closed;
	}

	/**
	 * @param closed The closed to set.
	 */
	public void setClosed(Icon closed)
	{
		this.closed = closed;
	}

	/**
	 * @return Returns the open.
	 */
	public Icon getOpen()
	{
		return open;
	}

	/**
	 * @param open The open to set.
	 */
	public void setOpen(Icon open)
	{
		this.open = open;
	}

	public int getWhitespace()
	{
		return whitespace;
	}

	public void setWhitespace(int whitespace)
	{
		this.whitespace = whitespace;
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		TreeDataJList treeList = (TreeDataJList)list;
		if( lengthsValid == false )
		{
			recomputeLengthPerLevel(list.getGraphics());
		}

		JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		Treeable st = (Treeable)value;
		l.setText(st.getName());
		
//		TreeLookIcon tli = new TreeLookIcon(treeList,model,st,index);
//		l.setIcon(tli);
		//return l;

		TreeNodeUI node = new TreeNodeUI(treeList,model,st,index,isSelected);
		node.setOpaque(false);
		node.setSize(list.getWidth(),list.getFixedCellHeight());
		node.setForeground(l.getForeground());
		node.setBackground(l.getBackground());
		return node;
	}
	
	protected void recomputeLengthPerLevel( Graphics g )
	{
//		for( int i = 0; i < model.getSize(); ++i )
//		{
//			String s = model.getElementAt(i).toString();
//			int stringWidth = g.getFontMetrics().stringWidth(s);
//			if( stringWidth > lengthPerLevel )
//			{
//				lengthPerLevel = stringWidth;
//			}
//		}
//		lengthsValid = true;
		
		int prevRanksWidths = 0;
		for( Integer rank: model.getVisibleRanks() )
		{
			// the icon size should be equal to the sum of the lengths of the longest strings
			// from each of the lower ranks
			
			rankWidthsMap.put(rank, prevRanksWidths);
			
			Integer longestStringLength = model.getLongestNamePixelLengthByRank(rank,g.getFontMetrics(),true);
			if( longestStringLength != null )
			{
				prevRanksWidths += longestStringLength.intValue() + 32;
			}
		}
		
		lengthsValid = true;
	}

	public class TreeNodeUI extends JPanel
	{
		protected TreeDataJList list;
		protected TreeDataListModel model;
		protected Treeable treeable;
		protected int index;
		protected boolean selected;
		
		public TreeNodeUI(TreeDataJList list, TreeDataListModel model, Treeable treeable, int index, boolean selected)
		{
			this.list = list;
			this.model = model;
			this.treeable = treeable;
			this.index = index;
			this.selected = selected;
		}
		
		@Override
		protected void paintComponent(Graphics g)
		{
			//super.paintComponent(g);
			
			// ensure that the lengths are valid
			if( !lengthsValid )
			{
				recomputeLengthPerLevel(g);
			}
			
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(list.getForeground());
			
			int cellHeight = list.getFixedCellHeight();
			FontMetrics fm = g.getFontMetrics();
			int baselineAdj = (int)(1.0/2.0*fm.getAscent() + 1.0/2.0*cellHeight);
			int midCell = cellHeight/2;
			
			// determine if this node has more peer nodes below it
			// if not, draw an L-shape
			// if so, draw a T-shape
			Treeable child = treeable;
			Treeable parent = treeable.getParentNode();
			if( parent != null )
			{
				int parentWidth = rankWidthsMap.get(parent.getRankId());
				int childWidth = rankWidthsMap.get(child.getRankId());

				if( !model.parentHasChildrenAfterNode(parent, child) )
				{
					// draw an L-shape
					g.drawLine(parentWidth+2*whitespace, 0, parentWidth+2*whitespace, midCell);
					g.drawLine(parentWidth+2*whitespace, midCell, childWidth, midCell);
				}
				else
				{
					// draw a T-shape
					g.drawLine(parentWidth+2*whitespace, 0, parentWidth+2*whitespace, cellHeight);
					g.drawLine(parentWidth+2*whitespace, midCell, childWidth, midCell);
				}
			}
			
			// draw the downward lines from ancestors to descendants renderered below this node
			while( parent != null )
			{
				if( model.parentHasChildrenAfterNode(parent, child) )
				{
					// draw the vertical line for under this parent
					int width = rankWidthsMap.get(parent.getRankId());
					g.drawLine(width+2*whitespace, 0, width+2*whitespace, cellHeight);
				}
				
				child = parent;
				parent = child.getParentNode();
			}
			
			// paint the open/close icon
//			Icon icon = open ? getOpen() : getClosed();
//			icon.paintIcon(c, g, x+width-icon.getIconWidth(), y+cellHeight);
			
			//draw the string name of the node
			String name = treeable.getName();
			int stringX = rankWidthsMap.get(treeable.getRankId()) + whitespace;
			int stringY = baselineAdj;
			int stringWidth = fm.stringWidth(name);
			if( selected )
			{
				g2d.setColor(list.getSelectionBackground());
				g2d.fillRoundRect(stringX-2, 1, stringWidth+4, cellHeight-2, 8, 8);
				g2d.setColor(list.getSelectionForeground());
			}
			g.drawString(name, stringX, stringY);
		}
	}
	
	public void intervalAdded(ListDataEvent e)
	{
		lengthsValid = false;
	}

	public void intervalRemoved(ListDataEvent e)
	{
		lengthsValid = false;
	}

	public void contentsChanged(ListDataEvent e)
	{
		lengthsValid = false;
	}
}

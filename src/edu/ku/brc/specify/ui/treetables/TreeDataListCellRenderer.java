package edu.ku.brc.specify.ui.treetables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.GraphicsUtils;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.ui.TreeDataJList;

@SuppressWarnings("serial")
public class TreeDataListCellRenderer implements ListCellRenderer, ListDataListener
{
	protected TreeDataListModel model;
	protected JList list;
	protected TreeNodeUI nodeUI;
	protected boolean lengthsValid;
	protected SortedMap<Integer, Integer> rankWidthsMap;
	
	// amount of space between tree-lines and name of node
	protected int whitespace;
	
	protected Icon open;
	protected Icon closed;
	
	protected Treeable currentTreeable;
	
	protected Color bgs[];
	
	public TreeDataListCellRenderer(JList list, TreeDataListModel listModel)
	{
		bgs = new Color[2];
		bgs[0] = new Color(202,238,255);
		bgs[1] = new Color(151,221,255);
		
		nodeUI = new TreeNodeUI(list,listModel);
		
		this.whitespace = 5;
		this.list = list;
		model = listModel;
		model.addListDataListener(this);
		lengthsValid = false;
		
		rankWidthsMap = new TreeMap<Integer, Integer>();

		open   = IconManager.getIcon("Down",    IconManager.IconSize.Std16);
		closed = IconManager.getIcon("Forward", IconManager.IconSize.Std16);
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		nodeUI.setTreeable((Treeable)value);
		nodeUI.setSelected(isSelected);
		nodeUI.setIndex(index);
		nodeUI.setHasFocus(cellHasFocus);
		return nodeUI;
	}
	
	protected void recomputeLengthPerLevel( Graphics g )
	{
		rankWidthsMap.clear();
		
		int prevRanksWidths = 0;
		SortedSet<Integer> visibleRanks = model.getVisibleRanks();
		for( Integer rank: visibleRanks )
		{
			rankWidthsMap.put(rank, prevRanksWidths);
			
			Integer longestStringLength = model.getLongestNamePixelLengthByRank(rank,g.getFontMetrics(),true);
			if( longestStringLength != null )
			{
				int spacerWidth = g.getFontMetrics().stringWidth("XXX");
				prevRanksWidths += longestStringLength.intValue() + spacerWidth;
			}
		}
		
		lengthsValid = true;
	}

	public class TreeNodeUI extends JPanel
	{
		protected JList list;
		protected TreeDataListModel model;
		protected Treeable treeable;
		protected int index;
		protected boolean selected;
		protected boolean hasFocus;

		public TreeNodeUI(JList list, TreeDataListModel model)
		{
			this.list = list;
			this.model = model;
			
			if( list.getFont() != null )
			{
				this.setFont(list.getFont());
			}
			
			setForeground(list.getForeground());
			setBackground(list.getBackground());
			setSize(list.getWidth(),list.getFixedCellHeight());
			setOpaque(true);
		}
		
		@Override
		public Dimension getPreferredSize()
		{
			// ensure that the lengths are valid
			if( !lengthsValid )
			{
				recomputeLengthPerLevel(list.getGraphics());
			}

			Graphics2D g2d = (Graphics2D)getGraphics();
			String name = treeable.getName();
			int stringX = rankWidthsMap.get(treeable.getRankId()) + whitespace;
			int stringWidth = g2d.getFontMetrics().stringWidth(name);
			
			return new Dimension(stringX+stringWidth,list.getFixedCellHeight());
		}
		
		/**
		 * Sets the index.
		 *
		 * @param index the index
		 */
		public void setIndex(int index)
		{
			this.index = index;
		}

		/**
		 * Sets the list.
		 *
		 * @param list the list
		 */
		public void setList(TreeDataJList list)
		{
			this.list = list;
		}

		/**
		 * Sets the selected.
		 *
		 * @param selected the value
		 */
		public void setSelected(boolean selected)
		{
			this.selected = selected;
		}

		/**
		 * Sets the treeable.
		 *
		 * @param treeable the treeable
		 */
		public void setTreeable(Treeable treeable)
		{
			this.treeable = treeable;
		}

		/**
		 * Sets the hasFocus.
		 *
		 * @param hasFocus the value
		 */
		public void setHasFocus(boolean hasFocus)
		{
			this.hasFocus = hasFocus;
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			// ensure that the lengths are valid
			if( !lengthsValid )
			{
				recomputeLengthPerLevel(list.getGraphics());
			}
			
			GraphicsUtils.turnOnAntialiasedDrawing(g);
			g.setColor(list.getForeground());
			
			// draw the alternating color background
			drawBackgroundColors(g);
			
			drawNodeAnchors(g);
			
			// draw the downward lines from ancestors to descendants renderered below this node
			drawTreeLinesToLowerNodes(g);
			
			// draw the string name of the node
			drawNodeString(g);
		}
		
		private void drawBackgroundColors(Graphics g)
		{
			Color orig = g.getColor();
			int cellHeight = list.getFixedCellHeight();

			SortedSet<Integer> visibleRanks = model.getVisibleRanks();
			int i = 0;
			Integer prevRank = null;
			for( Integer rank: visibleRanks )
			{
				if( prevRank == null )
				{
					prevRank = rank;
					continue;
				}
				
				int startX = rankWidthsMap.get(prevRank);
				int endX = rankWidthsMap.get(rank);
				
				g.setColor(bgs[i]);
				g.fillRect(startX,0,endX,cellHeight);
				++i;
				i%=2;
				prevRank = rank;
			}
			
			g.setColor(bgs[i]);
			int startX = rankWidthsMap.get(prevRank);
			g.fillRect(startX,0,list.getWidth(),cellHeight);
			
			g.setColor(orig);
		}
		
		private void drawNodeAnchors(Graphics g)
		{
			Treeable child = treeable;
			Treeable parent = treeable.getParentNode();
			int cellHeight = list.getFixedCellHeight();
			int midCell = cellHeight/2;

			if( child != model.getVisibleRoot() && parent != null )
			{
				Integer parentRankId = parent.getRankId();
				Integer childRankId  = child.getRankId();
				Integer parentWidth = rankWidthsMap.get(parentRankId);
				Integer childWidth = rankWidthsMap.get(childRankId);
				if( parentWidth == null || childWidth == null )
				{
					System.out.println("Unable to compute visual node location");
					System.out.println("   Parent: " + parent.getName());
					System.out.println("   Child:  " + child.getName());
				}
				
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
		}
		
		private void drawTreeLinesToLowerNodes(Graphics g)
		{
			// determine if this node has more peer nodes below it
			// if not, draw an L-shape
			// if so, draw a T-shape

			Treeable child = treeable;
			Treeable parent = treeable.getParentNode();
			int cellHeight = list.getFixedCellHeight();

			while( child != model.getVisibleRoot() && parent != null )
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
		}
		
		private void drawNodeString(Graphics g)
		{
			Graphics2D g2d = (Graphics2D)g;
			FontMetrics fm = g.getFontMetrics();
			int cellHeight = list.getFixedCellHeight();
			String name = treeable.getName();
			int baselineAdj = (int)(1.0/2.0*fm.getAscent() + 1.0/2.0*cellHeight);
			int stringX = rankWidthsMap.get(treeable.getRankId()) + whitespace;
			int stringY = baselineAdj;
			int stringWidth = fm.stringWidth(name);
			if( selected )
			{
				g2d.setColor(list.getSelectionBackground());
				g2d.fillRoundRect(stringX-2, 1, stringWidth+4, cellHeight-2, 8, 8);
				g2d.setColor(list.getSelectionForeground());
			}
			
			// TODO: replace this with something much more visually appealling
			if( !treeable.getChildNodes().isEmpty() && !model.allChildrenAreVisible(treeable) )
			{
				g.drawString("+", stringX, stringY);
			}
			stringX += g.getFontMetrics().stringWidth("+");
			
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

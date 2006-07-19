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
import edu.ku.brc.specify.treeutils.TreeTableUtils;
import edu.ku.brc.specify.ui.GraphicsUtils;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.ui.TreeDataJList;
import edu.ku.brc.util.Pair;

@SuppressWarnings("serial")
public class TreeDataListCellRenderer implements ListCellRenderer, ListDataListener
{
	protected TreeDataListModel model;
	protected JList list;
	protected TreeNodeUI nodeUI;
	protected boolean lengthsValid;
	protected SortedMap<Integer,Pair<Integer,Integer>> rankBoundsMap;
	protected int leadTextOffset;
	protected int tailTextOffset;
	
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
		
		leadTextOffset = 24;
		tailTextOffset = 8;
		
		nodeUI = new TreeNodeUI(list,listModel);
		
		this.whitespace = 5;
		this.list = list;
		model = listModel;
		model.addListDataListener(this);
		lengthsValid = false;
		
		rankBoundsMap = new TreeMap<Integer, Pair<Integer,Integer>>();

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
	
	public Pair<Integer,Integer> getTextBoundsForRank(Integer rank)
	{
		if( rank == null )
		{
			return null;
		}
		
		Pair<Integer,Integer> textBounds = new Pair<Integer,Integer>();
		Pair<Integer,Integer> bounds = rankBoundsMap.get(rank);
		if( bounds == null )
		{
			return null;
		}
		
		textBounds.first = bounds.first + leadTextOffset;
		textBounds.second = bounds.second - tailTextOffset;
		
		return textBounds;
	}
	
	public Pair<Integer,Integer> getAnchorBoundsForRank(Integer rank)
	{
		if( rank == null )
		{
			return null;
		}
		
		Pair<Integer,Integer> anchorBounds = new Pair<Integer,Integer>();
		Pair<Integer,Integer> bounds = rankBoundsMap.get(rank);
		if( bounds == null )
		{
			return null;
		}
		
		anchorBounds.first = bounds.first;
		anchorBounds.second = bounds.first + leadTextOffset;
		
		return anchorBounds;
	}
	
	protected void recomputeLengthPerLevel( Graphics g )
	{
		rankBoundsMap.clear();
		
		int prevRankEnd = 0;
		SortedSet<Integer> visibleRanks = model.getVisibleRanks();
		for( Integer rank: visibleRanks )
		{
			Pair<Integer,Integer> bounds = new Pair<Integer, Integer>();
			bounds.setFirst(prevRankEnd);

			Integer longestStringLength = model.getLongestNamePixelLengthByRank(rank,g.getFontMetrics(),true);
			if( longestStringLength != null )
			{
				bounds.setSecond(prevRankEnd + longestStringLength + leadTextOffset + tailTextOffset);
			}
			rankBoundsMap.put(rank,bounds);
			prevRankEnd = bounds.second;
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

			int width = rankBoundsMap.get(treeable.getRankId()).second;
			return new Dimension(width,list.getFixedCellHeight());
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
			this.setToolTipText(treeable.getFullName());
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
			
			// draw the open/close icon
			drawOpenClosedIcon(g);
			
			// draw the string name of the node
			drawNodeString(g);
		}
		
		private void drawBackgroundColors(Graphics g)
		{
			Color orig = g.getColor();
			int cellHeight = list.getFixedCellHeight();

			int i = 0;
			for( Integer rank: rankBoundsMap.keySet() )
			{
				Pair<Integer,Integer> startEnd = rankBoundsMap.get(rank);
				g.setColor(bgs[i%2]);
				g.fillRect(startEnd.first,0,startEnd.second,cellHeight);
				++i;
			}
			
			g.setColor(orig);
		}
		
		private void drawNodeAnchors(Graphics g)
		{
			Treeable node = treeable;
			Treeable parent = treeable.getParentNode();
			int cellHeight = list.getFixedCellHeight();
			int midCell = cellHeight/2;

			if( node != model.getVisibleRoot() && parent != null )
			{
				Integer parentRank = parent.getRankId();
				Integer rank  = node.getRankId();
				Pair<Integer,Integer> parentAnchorBounds = getAnchorBoundsForRank(parentRank);
				Pair<Integer,Integer> nodeAnchorBounds = getAnchorBoundsForRank(rank);
				
				if( !model.parentHasChildrenAfterNode(parent, node) )
				{
					// draw an L-line
					g.drawLine(parentAnchorBounds.second,0,parentAnchorBounds.second,midCell);
					g.drawLine(parentAnchorBounds.second,midCell,nodeAnchorBounds.first,midCell);
				}
				else
				{
					// draw a T-shape
					g.drawLine(parentAnchorBounds.second,0,parentAnchorBounds.second,cellHeight);
					g.drawLine(parentAnchorBounds.second,midCell,nodeAnchorBounds.first,midCell);
				}
			}
		}
		
		private void drawTreeLinesToLowerNodes(Graphics g)
		{
			// determine if this node has more peer nodes below it
			// if not, draw an L-shape
			// if so, draw a T-shape

			Treeable node = treeable;
			Treeable parent = treeable.getParentNode();
			int cellHeight = list.getFixedCellHeight();

			while( node != model.getVisibleRoot() && parent != null )
			{
				if( model.parentHasChildrenAfterNode(parent, node) )
				{
					// draw the vertical line for under this parent
					int width = getAnchorBoundsForRank(parent.getRankId()).second;
					g.drawLine(width, 0, width, cellHeight);
				}
				
				node = parent;
				parent = node.getParentNode();
			}
		}
		
		private void drawOpenClosedIcon(Graphics g)
		{
			int cellHeight = list.getFixedCellHeight();
			Pair<Integer,Integer> anchorBounds = getAnchorBoundsForRank(treeable.getRankId());
			int anchorStartX = anchorBounds.getFirst();

			// don't do anything for leaf nodes
			if( TreeTableUtils.getChildNodes(treeable).isEmpty() )
			{
				return;
			}
			
			Icon openClose = null;
			if( !model.allChildrenAreVisible(treeable) )
			{
				openClose = closed;
			}
			else
			{
				openClose = open;
			}

			// calculate offsets for icon
			int iconWidth = openClose.getIconWidth();
			int iconHeight = openClose.getIconHeight();
			int widthDiff = anchorBounds.second - anchorBounds.first - iconWidth;
			int heightDiff = cellHeight - iconHeight;
			openClose.paintIcon(list,g,anchorStartX+(int)(.5*widthDiff),0+(int)(.5*heightDiff));
		}
		
		private void drawNodeString(Graphics g)
		{
			Graphics2D g2d = (Graphics2D)g;
			FontMetrics fm = g.getFontMetrics();
			int cellHeight = list.getFixedCellHeight();
			String name = treeable.getName();
			int baselineAdj = (int)(1.0/2.0*fm.getAscent() + 1.0/2.0*cellHeight);
			Pair<Integer,Integer> stringBounds = getTextBoundsForRank(treeable.getRankId());
			int stringStartX = stringBounds.getFirst();
			int stringEndX = stringBounds.getSecond();
			int stringLength = stringEndX - stringStartX;
			int stringY = baselineAdj;
			if( selected )
			{
				g2d.setColor(list.getSelectionBackground());
				g2d.fillRoundRect(stringStartX-2, 1, stringLength+4, cellHeight-2, 8, 8);
				g2d.setColor(list.getSelectionForeground());
			}
			
			g.drawString(name, stringStartX, stringY);
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

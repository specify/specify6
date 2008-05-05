/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.util.Pair;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class TreeViewerNodeRenderer implements ListCellRenderer, ListDataListener
{
    /** Logger for all messages emitted. */
    //private static final Logger log = Logger.getLogger(TreeViewerNodeRenderer.class);
            
    // open/close handle icons
    protected Icon open;
    protected Icon closed;
    
    protected TreeViewerListModel model;
    protected JList list;
    
    protected SortedMap<Integer, Integer> columnWidths;
    protected boolean widthsValid;

    protected Color bgs[];
    
    protected int leadTextOffset;
    protected int tailTextOffset;

    protected TreeNodeUI nodeUI;
    
    protected boolean firstTime = true;
    
    protected Stroke lineStroke;
    protected Color  lineColor;
    
    protected TreeTableViewer<?,?,?> treeViewer;
    
    protected boolean renderTooltip = true;
    
    /**
     * @param ttv
     * @param model
     * @param bgColors
     * @param lineColor
     */
    public TreeViewerNodeRenderer(final TreeTableViewer<?,?,?> ttv, 
                                  final TreeViewerListModel model, 
                                  final Color[] bgColors, 
                                  final Color lineColor)
    {
        this.model = model;
        this.treeViewer = ttv;
        
        bgs = new Color[2];
        bgs[0] = bgColors[0];
        bgs[1] = bgColors[1];
    
        open   = IconManager.getIcon("Down",    IconManager.IconSize.NonStd);
        closed = IconManager.getIcon("Forward", IconManager.IconSize.NonStd);
        
        leadTextOffset = 24;
        tailTextOffset = 8;
        
        columnWidths = Collections.synchronizedSortedMap(new TreeMap<Integer, Integer>());
        widthsValid = false;
        
        model.addListDataListener(this);
        
        lineStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        this.lineColor = lineColor;
        
        nodeUI = new TreeNodeUI();
    }
    
    /**
     * @param renderTooltip
     */
    public void setRenderTooltip(boolean renderTooltip)
    {
        this.renderTooltip = renderTooltip;
    }

    /**
     * @return
     */
    public Color[] getBackgroundColors()
    {
        return this.bgs;
    }

    /* (non-Javadoc)
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(final JList   l, 
                                                  final Object  value, 
                                                  final int     index, 
                                                  final boolean isSelected, 
                                                  final boolean cellHasFocus)
    {
        //log.debug("getListCellRendererComponent( " + value + " )");
        this.list = l;
        
        if ( !(value instanceof TreeNode))
        {
            return createLabel("Item must be an instance of TreeNode");
        }
        
        if (firstTime)
        {
            firstTime = false;
            computeMissingColumnWidths(l.getGraphics());
        }

        TreeNode node = (TreeNode)value;
        
        nodeUI.setNode(node);
        nodeUI.setSelected(isSelected);
        nodeUI.setHasFocus(cellHasFocus);
        
        if (!renderTooltip)
        {
            nodeUI.setToolTipText(null);
            return nodeUI;
        }
        
        // build up the tooltip from the synonym information
        StringBuilder tooltipBuilder = new StringBuilder("<html><div style=\"font-family: sans-serif; font-size: 12pt\">");
        tooltipBuilder.append(node.getFullName());
        
        Set<Pair<Integer,String>> idsAndNames = node.getSynonymIdsAndNames();
        if (idsAndNames.size() > 0)
        {
            tooltipBuilder.append("</div><br><div style=\"font-family: sans-serif; font-size: 10pt\">");
            tooltipBuilder.append(getResourceString("TTV_SYNONYMS"));
            tooltipBuilder.append("<ul>");
            List<String> justNames = new ArrayList<String>();
            for (Pair<Integer,String> idAndName: idsAndNames)
            {
                justNames.add(idAndName.second);
            }
            Collections.sort(justNames);
            for (String name: justNames)
            {
                tooltipBuilder.append("<li>");
                tooltipBuilder.append(name);
                tooltipBuilder.append("</li>");
            }
            tooltipBuilder.append("</ul>");
        }
        if (node.getAcceptedParentFullName() != null)
        {
            tooltipBuilder.append("</div><br><div style=\"font-family: sans-serif; font-size: 10pt\">");
            tooltipBuilder.append(getResourceString("TTV_PREFERRED_NAME"));
            tooltipBuilder.append(": ");
            tooltipBuilder.append(node.getAcceptedParentFullName());
        }
        tooltipBuilder.append("</div></html>");
        nodeUI.setToolTipText(tooltipBuilder.toString());
        
        return nodeUI;
    }
    
    public synchronized Pair<Integer,Integer> getTextBoundsForRank(Integer rank)
    {
        if( rank == null )
        {
            return null;
        }
        
        Pair<Integer,Integer> textBounds = new Pair<Integer,Integer>();
        Pair<Integer,Integer> bounds = getColumnBoundsForRank(rank);
        if( bounds == null )
        {
            return null;
        }
        
        textBounds.first = bounds.first + leadTextOffset;
        textBounds.second = bounds.second - tailTextOffset;
        
        return textBounds;
    }
    
    public synchronized Pair<Integer,Integer> getAnchorBoundsForRank(Integer rank)
    {
        if( rank == null )
        {
            return null;
        }
        
        Pair<Integer,Integer> anchorBounds = new Pair<Integer,Integer>();
        Pair<Integer,Integer> bounds = getColumnBoundsForRank(rank);
        if( bounds == null )
        {
            return null;
        }
        
        anchorBounds.first = bounds.first;
        anchorBounds.second = bounds.first + leadTextOffset;
        
        return anchorBounds;
    }
    
    public synchronized Pair<Integer,Integer> getColumnBoundsForRank(Integer rank)
    {
        if( rank == null )
        {
            return null;
        }
        
        List<Integer> visibleRanks = model.getVisibleRanks();
        if (!visibleRanks.contains(rank))
        {
            return null;
        }
        
        Pair<Integer,Integer> colBounds = new Pair<Integer,Integer>();
        
        int widthsOfLowerColumns = 0;
        for (Integer r: visibleRanks)
        {
            if (r < rank)
            {
                Integer colWidth = columnWidths.get(r);
                widthsOfLowerColumns += colWidth;
            }
            else
            {
                break;
            }
        }
        
        colBounds.first = widthsOfLowerColumns;
        Integer colWidth = columnWidths.get(rank);
        if (colWidth == null)
        {
            return null;
        }
        
        colBounds.second = widthsOfLowerColumns + colWidth;
        return colBounds;
    }        
    
    public synchronized void computeMissingColumnWidths( Graphics g )
    {
        //log.debug("Computing missing column widths");
        int defaultColumnWidth =  g.getFontMetrics().stringWidth("WWWWWWWWWWWWW");
        for (Integer rank: model.getVisibleRanks())
        {
            if (!columnWidths.containsKey(rank))
            {
                columnWidths.put(rank,defaultColumnWidth);
            }
        }
        widthsValid = true;
    }
    
    public synchronized void removeUnusedColumnWidths()
    {
        Set<Integer> ranksWithWidths = new HashSet<Integer>();
        ranksWithWidths.addAll(columnWidths.keySet());
        
        List<Integer> visibleRanks = model.getVisibleRanks();
        for (Integer rankWithWidth: ranksWithWidths)
        {
            if (!visibleRanks.contains(rankWithWidth))
            {
                columnWidths.remove(rankWithWidth);
            }
        }
    }
    
    public synchronized int getColumnWidth(int rank)
    {
        return columnWidths.get(rank);
    }
    
    public synchronized void changeColumnWidth(int rank, int change)
    {
        int width = columnWidths.get(rank);
        columnWidths.put(rank, width+change);
        
        treeViewer.updateAllUI();
    }
    
    protected synchronized int[] getRanksSurroundingPoint(int x)
    {
        int[] ranks = new int[] {-1,-1};
        boolean leftSet = false;
        boolean rightSet = false;
        for (int rank: model.getVisibleRanks())
        {
            Pair<Integer,Integer> rankBounds = getColumnBoundsForRank(rank);
            if (Math.abs(rankBounds.first -x) < 4)
            {
                ranks[1] = rank;
                rightSet = true;
            }
            else if (Math.abs(rankBounds.second -x) < 4)
            {
                ranks[0] = rank;
                leftSet = true;
            }
        }
        
        if (leftSet || rightSet)
        {
            return ranks;
        }
        return null;
    }
    
    public synchronized int getSumOfColumnWidths()
    {
        int width = 0;
        for (Integer w: columnWidths.values())
        {
            width += w;
        }
        return width;
    }

    public void intervalAdded(ListDataEvent e)
    {
        widthsValid = false;
        if (list.getGraphics() != null)
        {
            computeMissingColumnWidths(list.getGraphics());
        }
    }

    public void intervalRemoved(ListDataEvent e)
    {
        widthsValid = false;
        removeUnusedColumnWidths();
    }

    public void contentsChanged(ListDataEvent e)
    {
        widthsValid = false;
        if (list != null && list.getGraphics() != null)
        {
            computeMissingColumnWidths(list.getGraphics());
        }
    }
    
    public class TreeNodeUI extends JPanel
    {
        protected TreeNode treeNode;
        protected boolean hasFocus;
        protected boolean selected;
        
        public TreeNodeUI()
        {
            super();
        }
        
        public boolean isHasFocus()
        {
            return hasFocus;
        }

        public void setHasFocus(boolean hasFocus)
        {
            this.hasFocus = hasFocus;
        }

        public TreeNode getTreeNode()
        {
            return treeNode;
        }

        public void setNode(TreeNode node)
        {
            this.treeNode = node;
        }

        public boolean isSelected()
        {
            return selected;
        }

        public void setSelected(boolean selected)
        {
            this.selected = selected;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public Dimension getPreferredSize()
        {
            //log.debug("TreeViewerNodeRenderer.getPreferredSize() called.  " + treeNode);
            // ensure that the lengths are valid
            if( !widthsValid )
            {
                computeMissingColumnWidths(list.getGraphics());
            }
            
            Dimension prefSize = new Dimension(getSumOfColumnWidths(),list.getFixedCellHeight());
            //log.debug("getPreferredSize() = " + prefSize);
            return prefSize;
        }
        
        @Override
        protected void paintComponent(Graphics g)
        {
            if( list.getFont() != null )
            {
                this.setFont(list.getFont());
            }

            // ensure that the lengths are valid
            if( !widthsValid )
            {
                computeMissingColumnWidths(list.getGraphics());
                widthsValid = true;
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

            // TODO: if there is time, get group opinions on how to make this look much better
            // then add this call back into the code
//            if (selected)
//            {
//                drawParentageStrings(g);
//            }
        }
        
        private void drawBackgroundColors(Graphics g)
        {
            Color orig = g.getColor();
            int cellHeight = list.getFixedCellHeight();

            int i = 0;
            for( Integer rank: model.getVisibleRanks() )
            {
                Pair<Integer,Integer> startEnd = getColumnBoundsForRank(rank);
                g.setColor(bgs[i%2]);
                g.fillRect(startEnd.first,0,startEnd.second-startEnd.first,cellHeight);
                ++i;
            }
            
            g.setColor(orig);
        }
        
        private void drawNodeAnchors(Graphics g)
        {
            // setup the color and stroke
            Graphics2D g2d = (Graphics2D)g;
            Stroke origStroke = g2d.getStroke();
            g2d.setStroke(lineStroke);
            Color origColor = g.getColor();
            g.setColor(lineColor);
            
            TreeNode node = treeNode;
            TreeNode parent = model.getNodeById(treeNode.getParentId());
            int cellHeight = list.getFixedCellHeight();
            int midCell = cellHeight/2;

            if( node != model.getVisibleRoot() && parent != null )
            {
                int parentRank = parent.getRank();
                int rank  = node.getRank();
                Pair<Integer,Integer> parentAnchorBounds = getAnchorBoundsForRank(parentRank);
                Pair<Integer,Integer> nodeAnchorBounds = getAnchorBoundsForRank(rank);
                
                if( !model.parentHasChildrenAfterNode(parent, node) )
                {
                    // draw an L-line
                    g.drawLine(parentAnchorBounds.second,0,parentAnchorBounds.second,midCell);
                    g.drawLine(parentAnchorBounds.second+1,midCell,nodeAnchorBounds.first,midCell);
                }
                else
                {
                    // draw a T-shape
                    g.drawLine(parentAnchorBounds.second,0,parentAnchorBounds.second,cellHeight);
                    g.drawLine(parentAnchorBounds.second+1,midCell,nodeAnchorBounds.first,midCell);
                }
            }
            
            // reset the color and stroke to original values
            g2d.setStroke(origStroke);
            g.setColor(origColor);
        }
        
        private void drawTreeLinesToLowerNodes(Graphics g)
        {
            // setup the color and stroke
            Graphics2D g2d = (Graphics2D)g;
            Stroke origStroke = g2d.getStroke();
            g2d.setStroke(lineStroke);
            Color origColor = g.getColor();
            g.setColor(lineColor);
            
            // determine if this node has more peer nodes below it
            // if not, draw an L-shape
            // if so, draw a T-shape

            TreeNode node = treeNode;
            TreeNode parent = model.getNodeById(treeNode.getParentId());
            int cellHeight = list.getFixedCellHeight();

            while( node != model.getVisibleRoot() && parent != null )
            {
                if( model.parentHasChildrenAfterNode(parent, node) )
                {
                    // draw the vertical line for under this parent
                    int width = getAnchorBoundsForRank(parent.getRank()).second;
                    
                    if (parent.getRank() != treeNode.getParentRank())
                    {
                        g.drawLine(width, 0, width, cellHeight);
                    }
                }
                
                node = parent;
                parent = model.getNodeById(node.getParentId());
            }
            
            // reset the color and stroke to original values
            g2d.setStroke(origStroke);
            g.setColor(origColor);
        }
        
        private void drawOpenClosedIcon(Graphics g)
        {
            int cellHeight = list.getFixedCellHeight();
            Pair<Integer,Integer> anchorBounds = getAnchorBoundsForRank(treeNode.getRank());
            int anchorStartX = anchorBounds.getFirst();

            // don't do anything for leaf nodes
            if( !treeNode.isHasChildren() )
            {
                return;
            }
            
            Icon openClose = null;
            if( !model.showingChildrenOf(treeNode) )
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
            Color startingColor = g.getColor();
            if (treeNode.getAcceptedParentFullName() != null)
            {
                UIDefaults uid = UIManager.getLookAndFeelDefaults();
                Color disabledTextColor = uid.getColor("textInactiveText");
                g.setColor(disabledTextColor);
            }
            Graphics2D    g2d         = (Graphics2D)g;
            FontMetrics   fm          = g.getFontMetrics();
            int           cellHeight  = list.getFixedCellHeight();
            StringBuilder name        = new StringBuilder(treeNode.getName());
            int           baselineAdj = (int)(1.0/2.0*fm.getAscent() + 1.0/2.0*cellHeight);
            Pair<Integer,Integer> stringBounds = getTextBoundsForRank(treeNode.getRank());
            int stringStartX = stringBounds.getFirst();
            int stringEndX   = stringBounds.getSecond();
            int stringLength = stringEndX - stringStartX;
            int stringY = baselineAdj;
            if( selected )
            {
                g2d.setColor(list.getSelectionBackground());
                g2d.fillRoundRect(stringStartX-2, 1, stringLength+4, cellHeight-2, 8, 8);
                g2d.setColor(list.getSelectionForeground());
            }
            if (treeNode == model.getDropLocationNode())
            {
                Color selBG = list.getSelectionBackground();
                Color secondarySelectionColor = new Color(selBG.getRed(), selBG.getGreen(), selBG.getBlue(), 127);
                g2d.setColor(secondarySelectionColor);
                g2d.fillRoundRect(stringStartX-2, 1, stringLength+4, cellHeight-2, 8, 8);
                g2d.setColor(list.getSelectionForeground());
            }
            
            if (treeNode.getAssociatedRecordCount() > 0)
            {
                name.append(" (");
                name.append(treeNode.getAssociatedRecordCount());
            }
            
            // Draw the Line from the start of the column to the text.
            if (!treeNode.isHasChildren())
            {
                Pair<Integer,Integer> colBnds = getColumnBoundsForRank(treeNode.getRank());
                
                Stroke origStroke = g2d.getStroke();
                g2d.setStroke(lineStroke);
                Color origColor = g.getColor();
                g.setColor(lineColor);
                
                g.drawLine(colBnds.first+1, cellHeight/2, stringStartX-4, cellHeight/2);
                
                // reset the color and stroke to original values
                g2d.setStroke(origStroke);
                g.setColor(origColor);
            }
            
            if (treeNode.getAssociatedRecordCount2() > 0 && treeNode.isHasChildren())
            {
                if (treeNode.getAssociatedRecordCount() > 0)
                {
                    name.append(", ");
                } else
                {
                    name.append(" (0, ");
                }
                name.append(treeNode.getAssociatedRecordCount2());
                name.append(")");
                
            } else if (treeNode.getAssociatedRecordCount() > 0)
            {
                name.append(")");
            }
            
            String clippedName = GraphicsUtils.clipString(fm, name.toString(), stringLength);
            g.drawString(clippedName, stringStartX, stringY);
            
            g.setColor(startingColor);
        }
        
        @SuppressWarnings("unused")
        private void drawParentageStrings(Graphics g)
        {
            TreeNode node = treeNode;
            FontMetrics fm = g.getFontMetrics();
            int cellHeight = list.getFixedCellHeight();
            int baselineAdj = (int)(1.0/2.0*fm.getAscent() + 1.0/2.0*cellHeight);
            
            int parentId = node.getParentId();
            TreeNode parent = model.getNodeById(parentId);
            while (parent != null && node != parent)
            {
                String name = parent.getName();
                Pair<Integer,Integer> stringBounds = getTextBoundsForRank(parent.getRank());
                if (stringBounds != null)
                {
                    int stringStartX = stringBounds.getFirst();
                    int stringEndX = stringBounds.getSecond();
                    int stringLength = stringEndX - stringStartX;
                    int stringY = baselineAdj;
                    String clippedName = GraphicsUtils.clipString(fm, name, stringLength);
                    g.drawString(clippedName, stringStartX, stringY);
                }
                
                node = parent;
                parentId = node.getParentId();
                parent = model.getNodeById(parentId);
            }
        }
    }
}

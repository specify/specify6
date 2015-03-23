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

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
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
    protected boolean    widthsValid;

    protected Color      bgs[];
    
    protected int        leadTextOffset;
    protected int        tailTextOffset;

    protected TreeNodeUI nodeUI;
    
    protected boolean    firstTime = true;
    
    protected Stroke     lineStroke;
    protected Color      lineColor;
    protected Color      synonymyColor;
    
    protected TreeTableViewer<?,?,?> treeViewer;
    
    protected boolean renderTooltip = true;
    
    protected Stack<Pair<Integer,Integer>> intPairRecycler = new Stack<Pair<Integer,Integer>>();
    protected StringBuilder                nameStrBldr     = new StringBuilder();
    protected boolean                      recalc          = true;
    
    protected Hashtable<Integer, Pair<Integer, Integer>> anchorRankHash = new Hashtable<Integer, Pair<Integer, Integer>>();
    protected Hashtable<Integer, Pair<Integer, Integer>> textRankHash   = new Hashtable<Integer, Pair<Integer, Integer>>();
    protected Hashtable<Integer, Pair<Integer, Integer>> colRankHash    = new Hashtable<Integer, Pair<Integer, Integer>>();
    protected BufferedImage                              bgImg          = null;
    protected int                                        numVisRanks    = 0;
    
    /**
     * @param ttv
     * @param model
     * @param bgColors
     * @param lineColor
     * @param synonymyColor
     */
    public TreeViewerNodeRenderer(final TreeTableViewer<?,?,?> ttv, 
                                  final TreeViewerListModel model, 
                                  final Color[] bgColors, 
                                  final Color lineColor,
                                  final Color synonymyColor)
    {
        this.model      = model;
        this.treeViewer = ttv;
        
        bgs = new Color[2];
        bgs[0] = bgColors[0];
        bgs[1] = bgColors[1];
        this.synonymyColor = synonymyColor;
    
        open   = IconManager.getIcon("Down",    IconManager.IconSize.NonStd);
        closed = IconManager.getIcon("Forward", IconManager.IconSize.NonStd);
        
        leadTextOffset = 24;
        tailTextOffset = 8;
        
        columnWidths = Collections.synchronizedSortedMap(new TreeMap<Integer, Integer>());
        widthsValid = false;
        
        model.addListDataListener(this);
        
        this.lineStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        this.lineColor = lineColor;
        
        nodeUI = new TreeNodeUI();
    }
    
    /**
     * @return the nodeUI
     */
    public TreeNodeUI getNodeUI()
    {
        return nodeUI;
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
        
        if (!(value instanceof TreeNode))
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
        
        if (node.getTooltipText() == null)
        {
            // build up the tooltip from the synonym information
            nameStrBldr.setLength(0);
            nameStrBldr.append("<html><div style=\"font-family: sans-serif; font-size: 12pt\">");
            nameStrBldr.append(node.getFullName());
            
            Set<Pair<Integer,String>> idsAndNames = node.getSynonymIdsAndNames();
            if (idsAndNames.size() > 0)
            {
                nameStrBldr.append("</div><<div style=\"font-family: sans-serif; font-size: 10pt\">");
                //tooltipBuilder.append("</div><br><div>");
            	nameStrBldr.append("<br>");
            	if (idsAndNames.size() > 1)
            	{
            		nameStrBldr.append(UIRegistry.getResourceString("TTV_SYNONYMS" )+ ":");
            	}
            	else
            	{
            		nameStrBldr.append(UIRegistry.getResourceString("TTV_SYNONYM" )+ ":");
            	}
                //tooltipBuilder.append("<ul>");
                List<String> justNames = new ArrayList<String>();
                for (Pair<Integer,String> idAndName: idsAndNames)
                {
                    justNames.add(idAndName.second);
                }
                Collections.sort(justNames);
                for (String name: justNames)
                {
                    nameStrBldr.append("<br>");
                	//tooltipBuilder.append("<li>");
                    nameStrBldr.append(name);
                    //tooltipBuilder.append("</li>");
                }
                nameStrBldr.append("</ul>");
            }
            if (node.getAcceptedParentFullName() != null)
            {
                nameStrBldr.append("</div><br><div style=\"font-family: sans-serif; font-size: 10pt\">");
                nameStrBldr.append(UIRegistry.getResourceString("TTV_PREFERRED_NAME"));
                nameStrBldr.append(": ");
                nameStrBldr.append(node.getAcceptedParentFullName());
            }
            nameStrBldr.append("</div></html>");
            node.setTooltipText(nameStrBldr.toString());
        }
        nodeUI.setToolTipText(node.getTooltipText());
        
        return nodeUI;
    }
    
    /**
     * @return
     */
    public Pair<Integer,Integer> getRecycledIntPair()
    {
        return intPairRecycler.size() == 0 ? new Pair<Integer,Integer>() : intPairRecycler.pop();
    }
    
    /**
     * @param item
     */
    public void recycle(final Pair<Integer,Integer> item)
    {
        if (item != null)
        {
            intPairRecycler.push(item);
        }
    }
    
    /**
     * @param rank
     * @return
     */
    public synchronized Pair<Integer,Integer> getTextBoundsForRank(final Integer rank)
    {
        if (rank == null)
        {
            return null;
        }
        
        Pair<Integer,Integer> bounds = getColumnBoundsForRank(rank);
        if (bounds == null)
        {
            return null;
        }
        
        Pair<Integer,Integer> textBounds = textRankHash.get(rank.intValue());
        if (textBounds == null)
        {
            textBounds        = getRecycledIntPair();
            textBounds.first  = bounds.first + leadTextOffset;
            textBounds.second = bounds.second - tailTextOffset;
            textRankHash.put(rank, textBounds);
        }
        return textBounds;
    }
    
    /**
     * @param rank
     * @return
     */
    public synchronized Pair<Integer,Integer> getAnchorBoundsForRank(final Integer rank)
    {
        if (rank == null)
        {
            return null;
        }
        
        //hash.clear();
        Pair<Integer,Integer> anchorBnds = anchorRankHash.get(rank);
        if (anchorBnds == null)
        {
            anchorBnds = getRecycledIntPair();
            
            Pair<Integer,Integer> bounds = getColumnBoundsForRank(rank);
            if (bounds == null)
            {
                return null;
            }
            
            anchorBnds.first  = bounds.first;
            anchorBnds.second = bounds.first + leadTextOffset;
            anchorRankHash.put(rank, anchorBnds);
        }
        
        return anchorBnds;
    }
    
    /**
     * @param rank
     * @return
     */
    public synchronized Pair<Integer,Integer> getColumnBoundsForRank(final Integer rank)
    {
        if (rank == null)
        {
            return null;
        }
        
        List<Integer> visibleRanks = model.getVisibleRanks();
        if (!visibleRanks.contains(rank))
        {
            return null;
        }
        
        Pair<Integer,Integer> colBounds = colRankHash.get(rank);
        if (colBounds == null)
        {
            colBounds = getRecycledIntPair();
        
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
            
            Integer colWidth = columnWidths.get(rank);
            if (colWidth == null)
            {
                recycle(colBounds);
                return null;
            }
            
            colBounds.first  = widthsOfLowerColumns;
            colBounds.second = widthsOfLowerColumns + colWidth;
            colRankHash.put(rank, colBounds);
        }
        return colBounds;
    }        
    
    /**
     * @param g
     */
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
    
    /**
     * 
     */
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
    
    /**
     * @param rank
     * @return
     */
    public synchronized int getColumnWidth(int rank)
    {
        return columnWidths.get(rank);
    }
    
    /**
     * 
     */
    public void reset()
    {
        bgImg = null;
        
        intPairRecycler.addAll(anchorRankHash.values());
        intPairRecycler.addAll(textRankHash.values());
        intPairRecycler.addAll(colRankHash.values());
        
        anchorRankHash.clear();
        textRankHash.clear();
        colRankHash.clear();
        
    }
    
    /**
     * @param rank
     * @param change
     */
    public synchronized void changeColumnWidth(int rank, int change)
    {
        int width = columnWidths.get(rank);
        columnWidths.put(rank, width+change);
        
        reset();
        
        treeViewer.updateAllUI();
    }
    
    /**
     * @param x
     * @return
     */
    protected synchronized int[] getRanksSurroundingPoint(int x)
    {
        int[]    ranks   = new int[] {-1,-1};
        boolean leftSet  = false;
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
    
    /**
     * @return
     */
    public synchronized int getSumOfColumnWidths()
    {
        int width = 0;
        for (Integer w: columnWidths.values())
        {
            width += w;
        }
        return width;
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
     */
    public void intervalAdded(ListDataEvent e)
    {
        widthsValid = false;
        if (list != null && list.getGraphics() != null)
        {
            computeMissingColumnWidths(list.getGraphics());
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
     */
    public void intervalRemoved(ListDataEvent e)
    {
        widthsValid = false;
        removeUnusedColumnWidths();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
     */
    public void contentsChanged(final ListDataEvent e)
    {
        widthsValid = false;
        if (list != null && list.getGraphics() != null)
        {
            computeMissingColumnWidths(list.getGraphics());
        }
    }
    
    /**
     *
     */
    @SuppressWarnings("serial")
    public class TreeNodeUI extends JPanel
    {
        protected TreeNode treeNode;
        protected boolean hasFocus;
        protected boolean selected;
        
        protected boolean isOpen = false;
        
        /**
         * 
         */
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

        /* (non-Javadoc)
         * @see javax.swing.JComponent#getPreferredSize()
         */
        @Override
        public Dimension getPreferredSize()
        {
            //log.debug("TreeViewerNodeRenderer.getPreferredSize() called.  " + treeNode);
            // ensure that the lengths are valid
            if (!widthsValid )
            {
                computeMissingColumnWidths(list.getGraphics());
            }
            
            Dimension prefSize = new Dimension(getSumOfColumnWidths(),list.getFixedCellHeight());
            //log.debug("getPreferredSize() = " + prefSize);
            return prefSize;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(Graphics g)
        {
            if (list.getFont() != null)
            {
                this.setFont(list.getFont());
            }

            // ensure that the lengths are valid
            if (!widthsValid )
            {
                computeMissingColumnWidths(list.getGraphics());
                widthsValid = true;
            }
            
            GraphicsUtils.turnOnAntialiasedDrawing(g);
            g.setColor(list.getForeground());
            
            // draw the alternating color background
            drawBackgroundColors(g);
            
            // draw the downward lines from ancestors to descendants renderered below this node
            drawTreeLinesToLowerNodes(g);
            
            // draw the open/close icon
            drawOpenClosedIcon(g);
            
            drawNodeAnchors(g);
            
            // draw the string name of the node
            drawNodeString(g);

            // TODO: if there is time, get group opinions on how to make this look much better
            // then add this call back into the code
//            if (selected)
//            {
//                drawParentageStrings(g);
//            }
        }
        
        /**
         * @param graphics
         */
        public void drawBackgroundColors(Graphics graphics)
        {
            Dimension size = getSize();
            boolean sizeChanged = bgImg == null || size.width > bgImg.getWidth() || size.height > bgImg.getHeight();
            if (sizeChanged || model.getVisibleRanks().size() != numVisRanks)
            {
                //if (sizeChanged)
                {
                    if (bgImg != null)
                    {
                        reset();
                    }
                    bgImg = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
                }
                
                Graphics g = bgImg.createGraphics();

                g.setColor(Color.WHITE);
                g.fillRect(0, 0, size.width, size.height);
                
                List<Integer> visRanksList = model.getVisibleRanks();
                int cellHeight = list.getFixedCellHeight();
                
                int i = 0;
                for( Integer rank : visRanksList)
                {
                    Pair<Integer,Integer> startEnd = getColumnBoundsForRank(rank);
                    g.setColor(bgs[i%2]);
                    g.fillRect(startEnd.first,0,startEnd.second-startEnd.first,cellHeight);
                    ++i;
                }
                numVisRanks = visRanksList.size();
                
                g.dispose();
            }
            
            graphics.drawImage(bgImg, 0, 0, null);
        }
        
        /**
         * Draw in background color tiled and alternating. Pass in the start and end height.
         * @param g the graphics to be drawn into
         * @param yStart the starting height 
         * @param yEnd the ending height
         */
        public void drawBackgroundColors(Graphics g, int yStart, int yEnd)
        {
            Color orig = g.getColor();
            int i = 0;
            for( Integer rank: model.getVisibleRanks() )
            {
                Pair<Integer,Integer> startEnd = getColumnBoundsForRank(rank);
                g.setColor(bgs[i%2]);
                g.fillRect(startEnd.first, yStart, startEnd.second-startEnd.first, yEnd);
                ++i;
            }
            
            g.setColor(orig);
        }
        
        /**
         * @param g
         */
        private void drawNodeAnchors(final Graphics g)
        {
            TreeNode node   = treeNode;
            TreeNode parent = model.getNodeById(treeNode.getParentId());
            
            // setup the color and stroke
            Graphics2D g2d        = (Graphics2D)g;
            Stroke     origStroke = g2d.getStroke();
            Color      origColor  = g.getColor();
            
            g2d.setStroke(lineStroke);
            g.setColor(lineColor);
            
            int cellHeight = list.getFixedCellHeight();
            int midCell    = cellHeight / 2;

            if (node != model.getVisibleRoot() && parent != null)
            {
                Pair<Integer,Integer> parentAnchorBounds = getAnchorBoundsForRank(parent.getRank());
                Pair<Integer,Integer> nodeAnchorBounds   = getAnchorBoundsForRank(node.getRank());
                
                // This can be optimized also
                boolean isLastKid = model.parentHasChildrenAfterNode(parent, node);
                
                if (!isLastKid)
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
        
        /**
         * @param g
         */
        private void drawTreeLinesToLowerNodes(final Graphics g)
        {
            // setup the color and stroke
            Graphics2D g2d        = (Graphics2D)g;
            Stroke     origStroke = g2d.getStroke();
            Color      origColor  = g.getColor();
            
            g2d.setStroke(lineStroke);
            g.setColor(lineColor);
            
            // determine if this node has more peer nodes below it
            // if not, draw an L-shape
            // if so, draw a T-shape

            TreeNode node   = treeNode;
            TreeNode parent = model.getNodeById(treeNode.getParentId());
            int cellHeight  = list.getFixedCellHeight();

            while (node != model.getVisibleRoot() && parent != null)
            {
                if (model.parentHasChildrenAfterNode(parent, node))
                {
                    // draw the vertical line for under this parent
                    Pair<Integer,Integer> anchorBnds = getAnchorBoundsForRank(parent.getRank());
                    
                    if (parent.getRank() != treeNode.getParentRank())
                    {
                        g.drawLine(anchorBnds.second, 0, anchorBnds.second, cellHeight);
                    }
                }
                
                node = parent;
                parent = model.getNodeById(node.getParentId());
            }
            
            // reset the color and stroke to original values
            g2d.setStroke(origStroke);
            g.setColor(origColor);
        }
        
        /**
         * @param g
         */
        private void drawOpenClosedIcon(final Graphics g)
        {
            // don't do anything for leaf nodes
            if (!treeNode.isHasChildren())
            {
                return;
            }
            
            Boolean hasVisKids = treeNode.hasVisualChildren();
            if (hasVisKids == null)
            {
            	hasVisKids = model.showingChildrenOf(treeNode);
                treeNode.setHasVisualChildren(hasVisKids);
            }
            
            Icon openCloseIcon = hasVisKids ? open : closed;
            
            int                   cellHeight   = list.getFixedCellHeight();
            Pair<Integer,Integer> anchorBounds = getAnchorBoundsForRank(treeNode.getRank());
            int                   anchorStartX = anchorBounds.getFirst();

            // calculate offsets for icon
            int widthDiff  = anchorBounds.second - anchorBounds.first - openCloseIcon.getIconWidth();
            int heightDiff = cellHeight - openCloseIcon.getIconHeight();
            
            openCloseIcon.paintIcon(list,g,anchorStartX+(int)(.5*widthDiff),0+(int)(.5*heightDiff));
        }
        
        /**
         * @param g
         */
        private void drawNodeString(final Graphics g)
        {
            Color startingColor = g.getColor();
            if (treeNode.getAcceptedParentFullName() != null)
            {
                g.setColor(synonymyColor);
            }
            
            nameStrBldr.setLength(0);
            nameStrBldr.append(treeNode.getName());
            
            Graphics2D    g2d         = (Graphics2D)g;
            FontMetrics   fm          = g.getFontMetrics();
            int           cellHeight  = list.getFixedCellHeight();
            int           baselineAdj = (int)(1.0/2.0*fm.getAscent() + 1.0/2.0*cellHeight);
            
            Pair<Integer,Integer> stringBounds = getTextBoundsForRank(treeNode.getRank());
            
            int stringStartX = stringBounds.getFirst();
            int stringEndX   = stringBounds.getSecond();
            int stringLength = stringEndX - stringStartX;
            int stringY      = baselineAdj;
            
            if (selected)
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
                nameStrBldr.append(" (");
                nameStrBldr.append(treeNode.getAssociatedRecordCount());
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
                    nameStrBldr.append(", ");
                } else
                {
                    nameStrBldr.append(" (0, ");
                }
                nameStrBldr.append(treeNode.getAssociatedRecordCount2());
                nameStrBldr.append(")");
                
            } else if (treeNode.getAssociatedRecordCount() > 0)
            {
                nameStrBldr.append(")");
            }
            
            String clippedName = GraphicsUtils.clipString(fm, nameStrBldr.toString(), stringLength);
            g.drawString(clippedName, stringStartX, stringY);
            
            g.setColor(startingColor);
        }
        
        /**
         * @param g
         */
        @SuppressWarnings("unused")
        private void drawParentageStrings(final Graphics g)
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
                    int stringEndX   = stringBounds.getSecond();
                    int stringLength = stringEndX - stringStartX;
                    int stringY      = baselineAdj;
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

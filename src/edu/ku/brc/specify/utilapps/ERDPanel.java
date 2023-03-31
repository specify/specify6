/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.utilapps;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 26, 2007
 *
 */
class ERDPanel extends JPanel
{
    
    protected Color[] colors = {Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLUE.darker(), Color.MAGENTA, Color.MAGENTA.darker(), Color.ORANGE, Color.RED, Color.CYAN, Color.PINK};
    protected int     colorInx     = 0;
    protected boolean doDebugBoxes = false;

    protected TableTracker tableTracker;
    
    protected Vector<ERDTable>             tables   = new Vector<ERDTable>();
    protected Hashtable<String, ERDTable>  tblHash  = new Hashtable<String, ERDTable>();
    protected Hashtable<ERDTable, Boolean> usedHash = new Hashtable<ERDTable, Boolean>();
    
    protected  Vector<DBRelationshipInfo> orderedList;
    
    
    protected  ERDTable         mainTable = null;
    protected  Vector<ERDTable> relTables = new Vector<ERDTable>();
    
    protected  PanelBuilder outer;
    protected  PanelBuilder leftPB;
    protected  PanelBuilder rightPB;
    
    protected boolean   ignorePaint = false;
    protected boolean   firstPaint  = true;
    protected int       aboveCnt = 0;
    protected int       belowCnt = 0;
    
    protected Color     mainColor  = new Color(239, 251, 214);
    protected Color     relColor   = new Color(219, 239, 176);
    protected Color     lineColor1 = new Color(113, 38, 0);
    protected Color     lineColor2 = Color.BLACK;//(179, 60, 0);
    
    protected ERDTable  root = null;
    protected Dimension preferredSize = null;
    
    /**
     * @param tableOrg
     */
    public ERDPanel(final TableTracker tableOrg)
    {
        super(null);
        
        this.tableTracker = tableOrg;
    }
    
    /**
     * @return the mainTable
     */
    public ERDTable getMainTable()
    {
        return mainTable != null ? mainTable : root;
    }

    /**
     * @return the relTables
     */
    public Vector<ERDTable> getRelTables()
    {
        return relTables;
    }
    
    /**
     * @return
     */
    public boolean isRoot()
    {
        return root != null;
    }

    /**
     * 
     */
    public void clear()
    {
        for (ERDTable t : tblHash.values())
        {
            t.clear();
        }
        setLayout(null);
        
        removeAll();
        
        mainTable = null;
        relTables.clear();
        tblHash.clear();
        
        firstPaint  = true;
        
        if (orderedList != null)
        {
            orderedList.clear();
        }
    }
    
    /**
     * @param ignorePaint the ignorePaint to set
     */
    public synchronized void setIgnorePaint(boolean ignorePaint)
    {
        this.ignorePaint = ignorePaint;
    }

    /**
     * @param numTables
     */
    public void setup(int numTables)
    {
        int gapWidth = (numTables * 6) + 20;
        outer   = new PanelBuilder(new FormLayout("p,"+gapWidth+"px,p", "t:p:g"), this);
        leftPB  = new PanelBuilder(new FormLayout("p", "t:p"));
        rightPB = new PanelBuilder(new FormLayout("p", UIHelper.createDuplicateJGoodiesDef("p:g", "10px", numTables)));
        
        CellConstraints cc = new CellConstraints();
        outer.add(leftPB.getPanel(), cc.xy(1, 1));
        outer.add(rightPB.getPanel(), cc.xy(3, 1));
        
        setBackground(Color.WHITE);
        leftPB.getPanel().setBackground(Color.WHITE);
        rightPB.getPanel().setBackground(Color.WHITE);
    }
    
    /**
     * @param table
     */
    public synchronized void addTable(final ERDTable table)
    {
        if (table == null)
        {
            return;
        }
        tables.add(table);
        
        if (mainTable == null)
        {
            mainTable = table;
            table.setBackground(mainColor);
            orderedList = new Vector<DBRelationshipInfo>(mainTable.getTable().getRelationships());
            //Collections.sort(orderedList);
            Collections.sort(orderedList, new Comparator<DBRelationshipInfo>() {
                public int compare(DBRelationshipInfo o1, DBRelationshipInfo o2)
                {
                    String name1 = ((DBRelationshipInfo)o1).getClassName();
                    if (name1.startsWith("Sp"))
                    {
                        name1 = name1.substring(2, name1.length());
                    }
                    String name2 = ((DBRelationshipInfo)o2).getClassName();
                    if (name2.startsWith("Sp"))
                    {
                        name2 = name2.substring(2, name2.length());
                    }
                    return name1.compareTo(name2);
                }
            });
            
        } else
        {
            relTables.add(table);
            table.setBackground(relColor);
        }
        tblHash.put(table.getTable().getClassName(), table);
        System.out.println("Adding ["+table.getTable().getName()+"] "+tblHash.size());

    }
    
    /**
     * 
     */
    public void initTables()
    {
        relTables.clear();
        
        int cnt = 0;
        for (DBRelationshipInfo t : orderedList)
        {
            ERDTable table = tblHash.get(t.getClassName());
            if (table != null && table != mainTable && !relTables.contains(table))// && !table.getTable().getClassName().toLowerCase().endsWith("iface"))
            {
                cnt++;
            }
        }

        setup(cnt);
        
        CellConstraints cc = new CellConstraints();
        
        leftPB.add(mainTable, cc.xy(1, 1));
        
        for (DBRelationshipInfo t : orderedList)
        {
            System.out.println(t.getName()+"  "+t.getClassName());
            
            ERDTable table = tblHash.get(t.getClassName());
            if (table != null)
            {
                if (table != mainTable && !relTables.contains(table))
                {
                    relTables.add(table);
                    rightPB.add(table, cc.xy(1, (relTables.size()*2)-1));
                }
            } else
            {
                System.out.println("Couldn't find ["+t.getClassName()+"]");
            }
        }
    }
    
    /**
     * @param pNode
     * @param g
     */
    protected void drawTreeLines(final ERDTable pNode, Graphics g)
    {
        Rectangle r = pNode.getBounds();
        int x = r.x + ((r.width-20) / 2);
        int y = r.y + r.height - 20;
        for (ERDTable k : pNode.getKids())
        {
            Rectangle kr = k.getBounds();
            int kx = kr.x + ((kr.width-20) / 2);
            int ky = kr.y;
            g.setColor(lineColor1);
            g.drawLine(x, y, kx, ky-1);
            //System.out.println(x+" "+y+" "+kx+" "+ky+" ");
            drawTreeLines(k, g);
        }
        
        if (doDebugBoxes)
        {
            g.setColor(colors[colorInx % colors.length]);
            g.drawRect(pNode.getShiftX(), r.y, pNode.getSpace().width, pNode.getSpace().height);
            g.drawString(pNode.getTable().getName(), x, y+20);
            colorInx++;
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (root != null)
        {
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(lineColor1);
            g2d.setStroke(new BasicStroke(2));
            drawTreeLines(root, g);
            return;
        }
        
        if (!ignorePaint && mainTable != null)
        {
            if (firstPaint)
            {
                firstPaint = false;
                aboveCnt = 0;
                belowCnt = 0;
                for (DBRelationshipInfo rel : orderedList)
                {
                    String rName = rel.getClassName();
                    String mName = mainTable.getTable().getClassName();
                    if (rName.equals(mName))
                    {
                        continue;
                    }
                    
                    JComponent src  = mainTable.getRelUIHash().get(rel);
                        
                    Rectangle  srcB  = src.getBounds();
                    Rectangle  srcBP = src.getParent().getBounds();
                    
                    int        offsetY = srcB.y + srcBP.y;
                    
                    JPanel dst = tblHash.get(rel.getClassName());
                    if (dst == null)
                    {
                        System.out.println("Couldn't find ["+rel.getClassName()+"] "+tblHash.size());
                        continue;
                    }
                    
                    Rectangle rDst  = dst.getBounds();
                    Rectangle rDstP = dst.getParent().getBounds();
                    
                    int       y     = offsetY + (srcB.height / 2);
                    
                    int dy = rDst.y + rDstP.y;
                    if (y > dy)
                    {
                        aboveCnt++;
                    } else
                    {
                        belowCnt++;
                    }
                }
            }
            
            Integer aboveGap = null;
            Integer belowGap = null;
            int xPosAbove = 0;
            int xPosBelow = 0;
            int cnt       = 1;
            for (DBRelationshipInfo rel : orderedList)
            {
                String rName = rel.getClassName();
                String mName = mainTable.getTable().getClassName();
                if (rName.equals(mName))
                {
                    continue;
                }
                
                JComponent src  = mainTable.getRelUIHash().get(rel);
                    
                Rectangle  srcB  = src.getBounds();
                Rectangle  srcBP = src.getParent().getBounds();
                
                int        offsetX = srcB.x + srcBP.x;
                int        offsetY = srcB.y + srcBP.y;
                
                JPanel dst = tblHash.get(rel.getClassName());
                if (dst == null)
                {
                    System.out.println("Couldn't find ["+rel.getClassName()+"] "+tblHash.size());
                    continue;
                }
                
                Rectangle rDst  = dst.getBounds();
                Rectangle rDstP = dst.getParent().getBounds();
                
                int       x     = offsetX + srcB.width+5;
                int       y     = offsetY + (srcB.height / 2);
                
                int dx = rDst.x + rDstP.x;
                int dy = rDst.y + rDstP.y;
                
                if (aboveGap == null)
                {
                    aboveGap = (dx - x) / (aboveCnt + 1);
                    belowGap = (dx - x) / (belowCnt + 1);
                    xPosBelow = belowGap * (belowCnt+1);
                }
                
                FontMetrics fm = g.getFontMetrics();
                
                dy += fm.getAscent() / 2;
                
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(cnt % 2 == 0 ? lineColor2 : lineColor1);
                g2d.setStroke(new BasicStroke(2));
                
                int pos;
                if (y > dy)
                {
                    xPosAbove += aboveGap;
                    pos = xPosAbove;
                    
                } else
                {
                    xPosBelow -= belowGap;
                    pos = xPosBelow;
                }
                
                
                if (rel.getType() == DBRelationshipInfo.RelationshipType.ManyToMany || rel.getType() == DBRelationshipInfo.RelationshipType.ManyToOne)
                {
                    // Left
                    g2d.fill(new Arc2D.Double(x+1-5, y-5,
                            10,
                            10,
                            270, 
                            180,
                            Arc2D.OPEN));
                }
                
                if (rel.getType() == DBRelationshipInfo.RelationshipType.OneToMany || rel.getType() == DBRelationshipInfo.RelationshipType.ManyToMany)
                {
                    // Right
                    g2d.fill(new Arc2D.Double(dx-5, dy-5,
                            10,
                            10,
                            90, 
                            180,
                            Arc2D.OPEN));
                }

                g.drawLine(x+2, y, x+pos-1, y);
                //g.drawString(ends[0], x+2,y+(fm.getAscent()/2));
                
                int ey = dy + (dy < y ? 1 : -1);
                
                g.drawLine(x+pos, y, x+pos, ey);
                g.drawLine(x+pos, dy, dx-1, dy);
                
                //g.drawString(ends[1], dx-fm.stringWidth(ends[1])-2, dy+(fm.getAscent()/2));
                cnt++;
            }
        }
        
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        if (preferredSize != null)
        {
            System.out.println("ERDPanel getPreferredSize "+preferredSize);
            return preferredSize;
        }
        Dimension d = super.getPreferredSize();
        System.out.println("ERDPanel getPreferredSize "+d);
        return d;
    }

    /**
     * @param pNode
     * @param xPos
     * @param yPos
     * @param level
     * @return
     */
    public int addNode(ERDTable pNode, final int xPos, final int yPos, final int level)
    {
        NodeInfo ni = tableTracker.getNodeInfo(pNode);
        //System.out.println(pNode.getTable().getTableName());
        
        if (usedHash.get(pNode) != null)
        {
            if (!ni.isOkToDuplicate())
            {
                return -1;
            }
        }
        
        add(pNode);
        usedHash.put(pNode, true);
        
        Dimension space = pNode.getSpace();
        Dimension pSize = pNode.getPreferredSize();
        Dimension size  = getSize();
        
        if (space.width > size.width || space.height > size.height)
        {
            int width  = Math.max(size.width, space.width);
            int height = Math.max(size.height, space.height);
            setSize(width, height);
        }
        
        int x = xPos + (space.width - pSize.width) / 2;
        int y = yPos;
        pNode.setLocation(x, y);
        pNode.setSize(pSize);
        pNode.setShiftX(xPos);
        
        //System.out.println(level+"  Pos: "+x+", "+y+" "+pSize);
        
        y = yPos + pSize.height + 10;
        
        int shiftedX = xPos;
        if (pNode.getKids().size() == 1)
        {
            shiftedX += (pNode.getSpace().width - pNode.getKids().get(0).getSpace().width) / 2;
        }
        
        for (ERDTable kid : pNode.getKids())
        {
            int w = addNode(kid, shiftedX, y+pSize.height+10, level+1);
            if (w > 0)
            {
                shiftedX += kid.getSpace().width + 10;
            }
        }
        return pSize.width;
    }

    /**
     * @param root
     */
    public void addTree(ERDTable root)
    {
        setLayout(null);
        addNode(root, 10, 10, 0);
        
        this.root = root;
        
        preferredSize = getSize();
        System.out.println(preferredSize+"\n"+super.getPreferredSize());

        // This is Need for the Trees for some reason.
        if (ERDTable.getDisplayType() == ERDTable.DisplayType.Title)
        {
            preferredSize.width += 200;
        }

    }
}


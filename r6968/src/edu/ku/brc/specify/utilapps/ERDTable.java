/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.utilapps;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.dnd.ShadowFactory;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 26, 2007
 *
 */
public class ERDTable extends JPanel implements Comparable<ERDTable>
{
    protected static int BRD_GAP = 6;
    
    public enum DisplayType { All, MainFields, Title, TitleAndRel }
    
    protected static DisplayType displayType = DisplayType.All;
    
    protected Hashtable<DBRelationshipInfo, JComponent> relUIHash   = new Hashtable<DBRelationshipInfo, JComponent>();
    
    protected DBTableInfo  table;
    
    protected JPanel                 inner;
    
    protected static final int       SHADOW_SIZE  = 10;
    protected BufferedImage          shadowBuffer = null;
    protected BufferedImage          buffer       = null;
    protected double                 ratio        = 0.0;
    protected Dimension              preferredRenderSize = new Dimension(0,0);
    
    protected Vector<ERDTable>       kids         = new Vector<ERDTable>();
    protected Dimension              space        = new Dimension(0, 0);
    protected int                    shiftX       = 0;
    protected String                 yesStr       = getResourceString("ERD_YES");
    
    public ERDTable(final DBTableInfo table)
    {
        super(new BorderLayout());
        this.table = table;
        
        if (ERDVisualizer.isDoShadow())
        {
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }
    }
    
    /**
     * @return the table
     */
    public DBTableInfo getTable()
    {
        return table;
    }
    
    public String getClassName()
    {
        return table.getClassName();
    }

    /**
     * @return the relUIHash
     */
    public Hashtable<DBRelationshipInfo, JComponent> getRelUIHash()
    {
        return relUIHash;
    }

    public void build(final PanelBuilder p, final DBFieldInfo f, final Font font, final int y, boolean all)
    {
        String typ = StringUtils.substringAfterLast(f.getType(), ".");
        if (StringUtils.isEmpty(typ))
        {
            typ = f.getType();
        }
        
        String lenStr = f.getLength() > 0  && f.getLength() < 65000 ? Integer.toString(f.getLength()) : "";
        
        CellConstraints cc = new CellConstraints();
        p.add(ERDVisualizer.mkLabel(font, f.getTitle(), SwingConstants.LEFT),   cc.xy(1,y));
        p.add(ERDVisualizer.mkLabel(font, typ, SwingConstants.CENTER),           cc.xy(3,y));
        p.add(ERDVisualizer.mkLabel(font, lenStr, SwingConstants.CENTER),        cc.xy(5,y));
        
        if (all)
        {
            p.add(ERDVisualizer.mkLabel(font, f.isRequired() ? yesStr : "", SwingConstants.CENTER), cc.xy(7,y));
            p.add(ERDVisualizer.mkLabel(font, f.isRequired() ? yesStr : "",  SwingConstants.CENTER), cc.xy(9,y));
        }

    }
    
    public void build(final PanelBuilder p, final DBTableInfo tbl, final Font font, final int y, boolean all)
    {
        String typ = StringUtils.substringAfterLast(tbl.getIdType(), ".");
        if (StringUtils.isEmpty(typ))
        {
            typ = tbl.getIdType();
        }
        CellConstraints cc = new CellConstraints();
        p.add(ERDVisualizer.mkLabel(font, tbl.getIdColumnName(), SwingConstants.LEFT),   cc.xy(1,y));
        p.add(ERDVisualizer.mkLabel(font, typ, SwingConstants.CENTER),                   cc.xy(3,y));
        p.add(ERDVisualizer.mkLabel(font, "", SwingConstants.CENTER),                    cc.xy(5,y));
        if (all)
        {
            p.add(ERDVisualizer.mkLabel(font, yesStr, SwingConstants.CENTER), cc.xy(7,y));
            p.add(ERDVisualizer.mkLabel(font, yesStr, SwingConstants.CENTER), cc.xy(9,y));
        }

    }
    
    public JComponent build(final PanelBuilder p, final DBRelationshipInfo r, final Font font, final int y, boolean all)
    {
        CellConstraints cc = new CellConstraints();
        p.add(ERDVisualizer.mkLabel(font, StringUtils.substringAfterLast(r.getClassName(), "."), SwingConstants.LEFT), cc.xy(1,y));
        p.add(ERDVisualizer.mkLabel(font, StringUtils.capitalize(r.getTitle()), SwingConstants.LEFT), cc.xy(3,y));
        JComponent comp = ERDVisualizer.mkLabel(font, r.getType().toString(), SwingConstants.CENTER);
        p.add(comp, cc.xy(5,y));
        if (all)
        {
            comp = ERDVisualizer.mkLabel(font, r.isRequired() ? yesStr : "", SwingConstants.CENTER);
            p.add(comp, cc.xy(7,y));
        }
        return comp;
    }
    
    /**
     * @param font
     */
    public void build(final Font font)
    {
        int numRows = 7;
        switch (displayType)
        {
            case All         : numRows = 7; break;
            case MainFields  : numRows = 7; break;
            case Title       : numRows = 1; break;
            case TitleAndRel : numRows = 4; break;
            
        }
        Font            bold   = new Font(font.getFamily(), Font.BOLD, font.getSize());
        Font            italic = new Font(font.getFamily(), Font.ITALIC, font.getSize());
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("f:p:g", UIHelper.createDuplicateJGoodiesDef("p", "2px", numRows)));
        CellConstraints cc     = new CellConstraints();
        
        String      className = StringUtils.substringAfterLast(table.getClassName(), ".");
        DBTableInfo tblInfo   = DBTableIdMgr.getInstance().getByShortClassName(className);
        if (tblInfo == null)
        {
            throw new RuntimeException("Couldn't find table for className["+className+"]");
        }
        String tblName = tblInfo.getTitle();
        int y = 1;
        pb.add(ERDVisualizer.mkLabel(bold, tblName, SwingConstants.CENTER), cc.xy(1,y)); y += 2;
        
        boolean doingAll = displayType == DisplayType.All;
        if (displayType == DisplayType.All || displayType == DisplayType.MainFields)
        {
            pb.addSeparator("", cc.xy(1,y)); y += 2;
            
            pb.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_FIELDS"), SwingConstants.CENTER), cc.xy(1,y)); y += 2;
            
            String       colsDef = "p:g,4px,p:g,4px" + (doingAll ? ",p:g,4px,p:g,4px" : "") + ",f:p:g";
            PanelBuilder fieldsPB = new PanelBuilder(new FormLayout(colsDef, 
                                                     UIHelper.createDuplicateJGoodiesDef("p", "2px", table.getFields().size()+2)));
            int yy = 1;
    
            fieldsPB.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_FIELD"), SwingConstants.LEFT), cc.xy(1,yy));
            fieldsPB.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_TYPE"),  SwingConstants.CENTER), cc.xy(3,yy));
            fieldsPB.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_LENGTH"), SwingConstants.CENTER), cc.xy(5,yy));
            if (doingAll)
            {
                fieldsPB.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_REQUIRED"), SwingConstants.CENTER), cc.xy(7,yy));
                fieldsPB.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_UNIQUE"),  SwingConstants.CENTER), cc.xy(9,yy));
            }
            yy += 2;
            
            build(fieldsPB, table, font, yy, doingAll); // does ID
            yy += 2;
            
            for (DBFieldInfo f : table.getFields())
            {
                build(fieldsPB, f, font, yy, doingAll);
                yy += 2;
            }
            pb.add(fieldsPB.getPanel(), cc.xy(1,y)); y += 2;
            
        }
        
        if (displayType == DisplayType.All || displayType == DisplayType.TitleAndRel)
        {
            pb.addSeparator("", cc.xy(1,y)); y += 2;
            
            pb.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_RELATIONSHIPS"), SwingConstants.CENTER), cc.xy(1,y)); y += 2;
            
            String       colsDef = "p:g,4px,p:g,4px" + (doingAll ? ",p:g,4px" : "") + ",f:p:g";
            PanelBuilder relsPB = new PanelBuilder(new FormLayout(colsDef, UIHelper.createDuplicateJGoodiesDef("p", "2px", table.getRelationships().size()+1)));
            int yy = 1;
            
            relsPB.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_TABLE"), SwingConstants.LEFT), cc.xy(1,yy));
            relsPB.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_NAME"),  SwingConstants.CENTER), cc.xy(3,yy));
            relsPB.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_TYPE"), SwingConstants.CENTER), cc.xy(5,yy));
            if (doingAll)
            {
                relsPB.add(ERDVisualizer.mkLabel(italic, getResourceString("ERD_REQUIRED"), SwingConstants.CENTER), cc.xy(7,yy));
            }
            yy += 2;
            
            Vector<DBRelationshipInfo> orderedList = new Vector<DBRelationshipInfo>(table.getRelationships());
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
            
            for (DBRelationshipInfo r : orderedList)
            {
                //System.out.println(r.getName()+" "+r.getType());
                if (!r.getName().toLowerCase().endsWith("iface"))
                {
                    JComponent p = build(relsPB, r, font, yy, doingAll);
                    relUIHash.put(r, p);
                    yy += 2;
                }
            }
            pb.add(relsPB.getPanel(), cc.xy(1,y)); y += 2;
            
            //fieldsPB.getPanel().setBackground(Color.GREEN);
            //relsPB.getPanel().setBackground(Color.BLUE);
        }
        
        inner = pb.getPanel();
        //inner.setBorder(BorderFactory.createEmptyBorder(BRD_GAP, BRD_GAP, BRD_GAP, BRD_GAP));
        
        inner.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(BRD_GAP, BRD_GAP, BRD_GAP, BRD_GAP)));
            
        setBackground(Color.WHITE);
        add(inner, BorderLayout.CENTER);
    }
    
    /**
     * Returns the BufferedImage of a background shadow. I creates a large rectangle than the orignal image.
     * @return Returns the BufferedImage of a background shadow. I creates a large rectangle than the orignal image.
     */
    private BufferedImage getBackgroundImageBuffer()
    {
        if (shadowBuffer == null)
        {
            ShadowFactory factory = new ShadowFactory(SHADOW_SIZE, 0.17f, Color.BLACK);

            Dimension     size  = inner.getSize();
            BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2.dispose();

            shadowBuffer = factory.createShadow(image);
        }
        return shadowBuffer;
    }
    
    public void clear()
    {
        shadowBuffer = null;
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setBackground(java.awt.Color)
     */
    @Override
    public void setBackground(Color bg)
    {
        if (inner != null)
        {
            inner.setBackground(bg);
            for (int i=0;i<inner.getComponentCount();i++)
            {
                inner.getComponent(i).setBackground(bg);
            }
        }
    }

    public void paint(Graphics g)
    {
        if (!ERDVisualizer.isDoShadow())
        {
            super.paint(g);
            
        } else
        {
            if (shadowBuffer == null)
            {
                getBackgroundImageBuffer();
                Graphics gg = shadowBuffer.createGraphics();
                //gg.translate(5, 5);
                inner.paint(gg);
                gg.dispose();
            }
    
            g.drawImage(shadowBuffer, 0,0, null);
        }
        
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ERDTable arg0)
    {
        return table.getTitle().compareTo(arg0.getTable().getTitle());
    }

    /**
     * @return the displayType
     */
    public static DisplayType getDisplayType()
    {
        return displayType;
    }

    /**
     * @param displayType the displayType to set
     */
    public static void setDisplayType(DisplayType displayType)
    {
        ERDTable.displayType = displayType;
    }
    
    /**
     * @param kid
     */
    public boolean addKid(ERDTable kid)
    {
        if (!kids.contains(kid))
        {
            kids.add(kid);
            return true;
        }
        return false;
    }

    /**
     * @return the kids
     */
    public Vector<ERDTable> getKids()
    {
        return kids;
    }

    /**
     * @param kids the kids to set
     */
    public void setKids(Vector<ERDTable> kids)
    {
        this.kids = kids;
    }

    /**
     * @return the space
     */
    public Dimension getSpace()
    {
        return space;
    }
    
    protected ERDTable treeParent = null;
    protected Position pos        = new Position();
    public class Position 
    {
        public int x = 0;
        public int y = 0;
        public int width;
        public int ascent = 0;
        public int descent = 0;
        public int shift = 0;
        public Rectangle2D bounds;// = new Rectangle2D(0,0,0,0);
    }
    /**
     * @return the pos
     */
    public Position getPos()
    {
        return pos;
    }

    /**
     * @param pos the pos to set
     */
    public void setPos(Position pos)
    {
        this.pos = pos;
    }

    /**
     * @return the treeParent
     */
    public ERDTable getTreeParent()
    {
        return treeParent;
    }

    /**
     * @param treeParent the treeParent to set
     */
    public void setTreeParent(ERDTable treeParent)
    {
        this.treeParent = treeParent;
    }

    public boolean isRoot()
    {
        return treeParent == null;
    }

    /**
     * @return the shiftX
     */
    public int getShiftX()
    {
        return shiftX;
    }

    /**
     * @param shiftX the shiftX to set
     */
    public void setShiftX(int shiftX)
    {
        this.shiftX = shiftX;
    }
    
    /**
     * I didn't want to use clone, because it isn't clone.
     * @return
     */
    protected ERDTable duplicate(final Font font)
    {
        
        ERDTable newTable = new ERDTable(table);
        ERDTable.displayType = DisplayType.Title;
        newTable.build(font);
        
        newTable.relUIHash   = new Hashtable<DBRelationshipInfo, JComponent>(relUIHash);
        
        newTable.shadowBuffer = shadowBuffer;
        newTable.buffer       = buffer;
        newTable.ratio        = ratio;
        newTable.preferredRenderSize = new Dimension(preferredRenderSize);
        
        return newTable;
    }

}

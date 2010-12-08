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
package edu.ku.brc.specify.ui.containers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.ui.containers.ContainerTreePanel.ParentNodeInfo;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 19, 2010
 *
 */
public class ContainerTreeRenderer extends DefaultTreeCellRenderer 
{
    protected final static String DASH = " - ";
    
    protected Color bgColor = Color.WHITE;//new Color(245, 245, 245, 255);
    
    protected HashMap<Class<?>, ImageIcon> iconHash     = new HashMap<Class<?>, ImageIcon>();
    protected HashMap<Short, ImageIcon>    typeIconHash = new HashMap<Short, ImageIcon>();
    protected UIFieldFormatterIFace        catNumFmt;
    protected Object                       userObj      = null;
    
    protected Component                    layoutComp;
    
    protected ImageIcon                    img1 = null;
    protected ImageIcon                    img2 = null;
    
    protected String                       txt1 = null;
    protected String                       txt2 = null;
    
    protected boolean                      isEditable  = false;
    protected boolean                      isViewMode  = false;  // indicates whether the form is in View or Edit mode
    protected boolean                      isSelected  = false;
    protected boolean                      isContainer = false;
    protected boolean                      hasColObj   = false;
    protected int                          iconSep     = 8;
    protected Point                        transPoint  = null;
    
    protected Rectangle[]                  hitRects = new Rectangle[2];                        
    
    protected ImageIcon                    addImgIcon  = IconManager.getIcon("AddRecord", IconManager.STD_ICON_SIZE.Std16);
    protected ImageIcon                    delImgIcon  = IconManager.getIcon("DelRecord", IconManager.STD_ICON_SIZE.Std16);
    protected ImageIcon                    schImgIcon  = IconManager.getIcon("Search", IconManager.STD_ICON_SIZE.Std16);
    protected ImageIcon                    edtImgIcon  = IconManager.getIcon("EditIcon", IconManager.STD_ICON_SIZE.NonStd);
    protected ImageIcon                    viewImgIcon = IconManager.getIcon("ViewForm", IconManager.STD_ICON_SIZE.Std16);
    
    /**
     * 
     */
    protected RenderingHints renderingHints = UIHelper.createTextRenderingHints();
    
    /**
     * @param layoutComp
     * @param isEditable
     * @param isViewMode
     */
    public ContainerTreeRenderer(final Component layoutComp, final boolean isEditable, final boolean isViewMode) 
    {
        this.isEditable = isEditable;
        this.isViewMode = isViewMode;
        
        Class<?>[] cls = {Container.class, CollectionObject.class};
        for (Class<?> c : cls)
        {
            iconHash.put(c, IconManager.getIcon(c.getSimpleName(), IconManager.IconSize.Std24));
        }
        setIcon(iconHash.get(cls[0]));
        
        String[] fNames = {"Container", "Folder", "Sheet", "PlantSpecimen"};
        for (short i=0;i<fNames.length;i++)
        {
            typeIconHash.put((short)i, IconManager.getIcon(fNames[i], IconManager.IconSize.Std24));
        }
        
        catNumFmt = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "CatalogNumber");
        
        this.layoutComp = layoutComp;
        
        for (int i=0;i<hitRects.length;i++)
        {
            hitRects[i] = new Rectangle();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) 
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        //System.err.println(value+" sel:"+sel+"  exp:"+expanded+"  foc:"+hasFocus);
        /*if (!sel)
        {
            setOpaque(true);
            setBackground(bgColor);
        } else
        {
            System.out.println(getBackgroundSelectionColor());
            setBackground(getBackgroundSelectionColor());
        }*/
        
        isSelected = sel;
        
        img1 = null;
        img2 = null;
        txt1 = null;
        txt2 = null;
        setIcon(null);
        setText(null);
        
        if (value instanceof DefaultMutableTreeNode)
        {
            isContainer = false;
            hasColObj   = false;
            
            userObj = ((DefaultMutableTreeNode)value).getUserObject();
            if (userObj instanceof Container)
                System.err.println(value.hashCode()+"  "+userObj.hashCode()+"  "+((Container)userObj).getName()+"  "+((Container)userObj).getId());
            if (userObj instanceof Container)
            {
                isContainer = true;
                hasColObj   = ((Container)userObj).getCollectionObject() != null;
            }
            
            if (userObj instanceof String)
            {
                txt1 = userObj.toString();
                
            } else if (userObj instanceof ParentNodeInfo)
            {
                ParentNodeInfo pni = (ParentNodeInfo)userObj;
                txt1 = pni.getTitle();
                if (pni.type != null)
                {
                    img1 = typeIconHash.get(pni.type.shortValue());
                }
                
            } else
            {
                Class<?> cls = userObj.getClass();
                img1 = iconHash.get(cls);
                if (cls == Container.class)
                {
                    Container c = (Container)userObj;
                    txt1 = c.getName();
                    if (c.getType() != null && c.getType().shortValue() > 0)
                    {
                        img1 = typeIconHash.get(c.getType());
                    }
                    CollectionObject colObj = c.getCollectionObject();
                    if (colObj != null)
                    {
                        String catNum = StringUtils.isNotEmpty(colObj.getCatalogNumber()) ? (String)catNumFmt.formatToUI(colObj.getCatalogNumber()) : null;
                        txt2 = catNum != null ? catNum : " ";
                        img2 = IconManager.getIcon("CollectionObject", IconManager.IconSize.Std16);//iconHash.get(CollectionObject.class);
                    } else
                    {
                        txt2 = " (Not Cataloged) ";
                    }
                        
                } else if (userObj instanceof CollectionObject)
                {
                    CollectionObject colObj = (CollectionObject)userObj;
                    String catNum = StringUtils.isNotEmpty(colObj.getCatalogNumber()) ? (String)catNumFmt.formatToUI(colObj.getCatalogNumber()) : null;
                    txt1 = catNum != null ? catNum : " ";
                    img1 = iconHash.get(CollectionObject.class);
                    
                } else
                {
                    txt1 = userObj.toString();
                }
            }
        }
        return this;
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        Dimension   dim = super.getPreferredSize();
        FontMetrics fm  = this.getFontMetrics(getFont());
        
        dim.height = IconManager.STD_ICON_SIZE.Std24.size() + 2;
        //int half   = layoutComp.getX();
        
        int width = 10;
        if (img1 != null)
        {
            width += img1.getIconWidth();
        }
        if (img2 != null)
        {
            width += img2.getIconWidth() + 1;
        }
        if (txt1 != null)
        {
            width += getIconTextGap();
            width += fm.stringWidth(txt1);
        }
        if (txt2 != null)
        {
            //width = half;
            width += fm.stringWidth(DASH);
            width += fm.stringWidth(txt2);
        }
        dim.width  = width;
        
        // Add Space for Edit Icons
        int numIcons = 0;
        if (isViewMode)
        {
            numIcons = 2;
            
        } else if (isEditable)
        {
            numIcons = 2;
        }
        dim.width += (IconManager.IconSize.Std16.size() * numIcons) + (iconSep * numIcons);

        return dim;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.DefaultTreeCellRenderer#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Dimension d = getSize();
        
        Graphics2D  g2d  = (Graphics2D)g.create();
        g2d.setRenderingHints(renderingHints);
        
        FontMetrics fm    = g2d.getFontMetrics();
        int         imgY  = img1 != null ? (d.height - 24) / 2 : 0;
        int         imgY2 = img1 != null ? (d.height - 16) / 2 : 0;
        int         txtY  = ((d.height - fm.getHeight()) / 2) + fm.getAscent();
        int         x     = 0;
        
        if (img1 != null)
        {
            g2d.drawImage(img1.getImage(), x, imgY, null);
            x += img1.getIconWidth();
        }
        
        if (img2 != null)
        {
            x += 1;
            g2d.drawImage(img2.getImage(), x, imgY2, null);
            x += img2.getIconWidth() + getIconTextGap();
        } else
        {
            x += getIconTextGap();
        }
        
        g2d.setColor(getForeground());
        
        if (txt1 != null)
        {
            g.drawString(txt1, x, txtY);
            x += fm.stringWidth(txt1);
        }
        
        if (txt2 != null)
        {
            g.drawString(DASH, x, txtY);
            x += fm.stringWidth(DASH);
            g.drawString(txt2, x, txtY);
            x += fm.stringWidth(txt2);
        }
        
        if (isSelected)
        {
            if (isViewMode)
            {
                x += iconSep;
                x += drawIcon(g2d, x, imgY2, viewImgIcon, 0);
                
                if (isContainer && hasColObj)
                {
                    x += iconSep;
                    x += drawIcon(g2d, x, imgY2, viewImgIcon, 1);
                }
                
            } else if (isEditable && isContainer)
            {
                ImageIcon imgIcn1 = null;
                ImageIcon imgIcn2 = null;
                
                if (hasColObj)
                {
                    imgIcn1 = edtImgIcon;
                    imgIcn2 = delImgIcon;
                } else
                {
                    imgIcn1 = schImgIcon;
                    imgIcn2 = addImgIcon;
                }
                
                x += iconSep;
                x += drawIcon(g2d, x, imgY2, imgIcn1, 0);
                x += drawIcon(g2d, x, imgY2, imgIcn2, 1);
            }
        }

        g2d.dispose();
    }
    
    /**
     * @param g2d
     * @param xc
     * @param yc
     * @param imgIcon
     * @param hitsInx
     * @return
     */
    private int drawIcon(final Graphics2D g2d, 
                         final int xc, 
                         final int yc, 
                         final ImageIcon imgIcon, 
                         final int hitsInx)
    {
        g2d.drawImage(imgIcon.getImage(), xc, yc, null);
        hitRects[hitsInx].setBounds(xc, yc, imgIcon.getIconWidth(), imgIcon.getIconHeight());
        Point p = hitRects[hitsInx].getLocation();
        SwingUtilities.convertPointToScreen(p, this);
        hitRects[hitsInx].setLocation(p);
        return imgIcon.getIconWidth() + iconSep;
    }
    
    /**
     * @param bkgdColor
     */
    public void setBGColor(final Color bkgdColor)
    {
        this.bgColor = bkgdColor;
    }

    /**
     * @return the isEditable
     */
    public boolean isEditable()
    {
        return isEditable;
    }

    /**
     * @param isEditable the isEditable to set
     */
    public void setEditable(boolean isEditable)
    {
        this.isEditable = isEditable;
    }

    /**
     * @return the hitRects
     */
    public Rectangle[] getHitRects()
    {
        Rectangle[] rects = new Rectangle[hitRects.length];
        for (int i=0;i<rects.length;i++)
        {
            rects[i] = new Rectangle(hitRects[i]);
        }
        return rects;
    }
    
    
}

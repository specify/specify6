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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
 * Oct 21, 2010
 *
 */
public class ContainerTreeCellRenderer extends JPanel implements TreeCellRenderer
{
    protected final static Color      bgColor = new Color(245, 245, 245);
    
    protected HashMap<Class<?>, ImageIcon> iconHash     = new HashMap<Class<?>, ImageIcon>();
    protected HashMap<Short, ImageIcon>    typeIconHash = new HashMap<Short, ImageIcon>();
    protected UIFieldFormatterIFace        catNumFmt;
    
    protected JLabel                       cnIconLbl;
    protected JLabel                       coIconLbl;
    
    protected JLabel                       sepLbl;

    protected JLabel                       cnTextLbl;
    protected JLabel                       coTextLbl;
    
    protected boolean                      isSelected = false;
    protected boolean                      isFocused  = false;

    /**
     * 
     */
    public ContainerTreeCellRenderer() 
    {
        Class<?>[] cls = {Container.class, CollectionObject.class};
        for (Class<?> c : cls)
        {
            iconHash.put(c, IconManager.getIcon(c.getSimpleName(), IconManager.IconSize.Std24));
        }
        
        cnIconLbl = new JLabel(iconHash.get(cls[0]));
        coIconLbl = new JLabel(iconHash.get(cls[1]));
        
        sepLbl    = UIHelper.createLabel("-");
        
        cnTextLbl = UIHelper.createLabel(" ");
        coTextLbl = UIHelper.createLabel(" ");
        
        cnTextLbl.setOpaque(false);
        coTextLbl.setOpaque(false);
        sepLbl.setOpaque(false);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("5px,p,1px,p,8px,p:g,4px,p,4px,p:g,15px", "c:p"), this);
        pb.add(cnIconLbl, cc.xy(2, 1));
        pb.add(coIconLbl, cc.xy(4, 1));
        pb.add(cnTextLbl, cc.xy(6, 1));
        pb.add(sepLbl,    cc.xy(8, 1));
        pb.add(coTextLbl, cc.xy(10, 1));
        
        String[] fNames = {"Folder", "Sheet", "PlantSpecimen"};
        for (short i=0;i<fNames.length;i++)
        {
            typeIconHash.put((short)(i+1), IconManager.getIcon(fNames[i], IconManager.IconSize.Std24));
        }
        
        catNumFmt = DBTableIdMgr.getInstance().getFieldFormatterFor(CollectionObject.class, "CatalogNumber");
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus)
    {
        //super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        
        isSelected = selected;
        isFocused  = hasFocus;
        
        //System.err.println(value+" sel:"+sel+"  exp:"+expanded+"  foc:"+hasFocus);
        if (!selected)
        {
            setOpaque(true);
            cnTextLbl.setForeground(Color.BLACK);
            sepLbl.setForeground(Color.BLACK);
            coTextLbl.setForeground(Color.BLACK);            
        } else
        {
            cnTextLbl.setForeground(Color.WHITE);
            sepLbl.setForeground(Color.WHITE);
            coTextLbl.setForeground(Color.WHITE);
        }
        
        if (value instanceof DefaultMutableTreeNode)
        {
            Object userObj = ((DefaultMutableTreeNode)value).getUserObject();
            if (userObj instanceof String)
            {
                coIconLbl.setIcon(iconHash.get(Container.class));
                cnTextLbl.setText(userObj.toString());
                sepLbl.setText(" ");
                coIconLbl.setIcon(null);
                coTextLbl.setText(" ");
                
            } else if (userObj instanceof ParentNodeInfo)
            {
                ParentNodeInfo pni = (ParentNodeInfo)userObj;
                coIconLbl.setIcon(iconHash.get(Container.class));
                cnTextLbl.setText(pni.getTitle());
                sepLbl.setText(" ");
                coIconLbl.setIcon(null);
                coTextLbl.setText(" ");

                if (pni.type != null && pni.type > 0)
                {
                    coIconLbl.setIcon(typeIconHash.get(pni.type));
                    cnTextLbl.setText(pni.getTitle());
                    sepLbl.setText(" ");
                    coIconLbl.setIcon(null);
                    coTextLbl.setText(" ");
                }
                
            } else
            {
                Class<?> cls = userObj.getClass();
                if (cls == Container.class)
                {
                    Container c = (Container)userObj;
                    if (c.getType() != null && c.getType().shortValue() > 0)
                    {
                        cnIconLbl.setIcon(typeIconHash.get(c.getType()));
                    } else
                    {
                        cnIconLbl.setIcon(iconHash.get(Container.class));
                    }
                    String  cnName  = c.getName();
                    boolean isEmpty = StringUtils.isEmpty(cnName);
                    
                    cnTextLbl.setText(cnName);
                    
                    CollectionObject colObj = c.getCollectionObject();
                    if (colObj != null)
                    {
                        
                        sepLbl.setText("-");
                        coIconLbl.setIcon(iconHash.get(CollectionObject.class));
                        coTextLbl.setText((String)catNumFmt.formatToUI(colObj.getCatalogNumber())+"   ");
                    } else
                    {
                        sepLbl.setText(isEmpty ? "" : "-");
                        coIconLbl.setIcon(null);
                        coTextLbl.setText( isEmpty ? " "  : "(Not Cataloged) ");
                    }
                        
                } else
                {
                    cnIconLbl.setIcon(iconHash.get(cls));
                    cnTextLbl.setText(userObj.toString());
                    sepLbl.setText(" ");
                    coIconLbl.setIcon(null);
                    coTextLbl.setText(" ");
                }
            }
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
     */
    @Override
    protected void paintBorder(Graphics g)
    {
        super.paintBorder(g);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        //super.paintComponent(g);
        
        Dimension d = getSize();
        
        if (isSelected)
        {
            Graphics2D g2d = (Graphics2D)((Graphics2D)g).create();
            g2d.setColor(bgColor);
            g2d.fillRect(0, 0, d.width, d.height);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(UIHelper.getStdLineStroke());
            g2d.setColor(Color.BLUE);
            g2d.fillRoundRect(0, 0, d.width-1, d.height-1, 24, 24);
            g2d.dispose();
            
        } else
        {
            g.setColor(bgColor);
            g.fillRect(0, 0, d.width, d.height);
        }
    }
    
    
}

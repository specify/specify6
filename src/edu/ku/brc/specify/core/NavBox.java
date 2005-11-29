/* Filename:    $RCSfile: NavBox.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.core;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;

import edu.ku.brc.specify.core.NavBoxIFace.Scope;
import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.ui.IconManager.IconSize;

public class NavBox extends JPanel implements NavBoxIFace
{
    private String             name;
    private NavBoxIFace.Scope  scope;
    private NavBoxMgr          mgr;
    
    /**
     * 
     *
     */
    public NavBox(String name)
    {
        super();
        this.name = name;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        setBorder(BorderFactory.createEmptyBorder(15, 2, 2, 2));
        setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
    }
    
    /**
     * Returns the scope of the tab
     * @return returns the scope of the tab
     */
    public Scope getScope()
    {
        return scope;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.NavBoxIFace#setScope(edu.ku.brc.specify.core.NavBoxIFace.Scope)
     */
    public void setScope(final NavBoxIFace.Scope scope)
    {
        this.scope = scope;
    }


    /* (non-Javadoc)
     * @see java.awt.Component#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setName(java.lang.String)
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.NavBoxIFace#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /**
     * Adds a NavBoxItemIFace item to the box and returns the UI component for that item
     * @param item the NavBoxItemIFace item to be added
     * @param notify whether to have it relayout or not (true -> does layout)
     * @return the UI component for this item
     */
    public Component add(final NavBoxItemIFace item, boolean notify)
    {
        super.add(item.getUIComponent());
        if (notify && mgr != null)
        {
            mgr.invalidate();
            mgr.doLayout();
        }
        return item.getUIComponent();
    }
       
    /**
     * Adds a NavBoxItemIFace item to the box and returns the UI component for that item and does not perform a doLayout of the box
     * @param item NavBoxItemIFace to be added
     * @return the ui component for the item
     */
    public Component add(final NavBoxItemIFace item)
    {
        return add(item, false);
    }
        

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        Dimension dim = getSize();
        
        FontMetrics fm = g.getFontMetrics();
        int strW = fm.stringWidth(name);
        int strH = fm.getHeight();
        
        int x = (dim.width - strW) / 2;
        Insets ins = getBorder().getBorderInsets(this);
        int y = 2 + fm.getAscent();
        
        int lineW = dim.width - ins.left - ins.right;
        //g.setColor(Color.BLUE.darker().darker());
        //g.drawString(name, x+1, y+1);
        g.setColor(Color.BLUE.darker());
        g.drawString(name, x, y);
        x = ins.left;
        y += fm.getDescent() + fm.getLeading();

        g.setColor(Color.LIGHT_GRAY.brighter());
        g.drawLine(x, y,   x+lineW, y);
        y++;
        x++;
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(x, y,   x+lineW, y);
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.NavBoxIFace#setMgr(edu.ku.brc.specify.core.NavBoxMgr)
     */
    public void setMgr(NavBoxMgr mgr)
    {
        this.mgr = mgr;
    }

    /**
     * Returns a NavBoxItemIFace item built from a RolloverCommand
     * @param label the text label
     * @param iconName the icon name, not the image filename, but the IconManager name for the icon
     * @param fileName the file name for the icon
     * @param iconSize the size  to use
     * @return Returns a NavBoxItemIFace item built from a RolloverCommand
     */
    public static NavBoxItemIFace createBtn(final String label, 
                                            final String iconName, 
                                            final String fileName, 
                                            final IconManager.IconSize iconSize, 
                                            final ActionListener al)
    {
        Icon icon = fileName != null ? IconManager.getInstance().register(iconName, fileName, iconSize) :
                                       IconManager.getInstance().getIcon(iconName, iconSize);
        
        RolloverCommand btn = new RolloverCommand(label, icon);
        if (al != null)
        {
            btn.addActionListener(al);
        }
        
        return btn; 
    }
    
    /**
     * Returns a NavBoxItemIFace item built from a RolloverCommand
     * @param label the text label
     * @param iconName the icon name, not the image filename, but the IconManager name for the icon
     * @param iconSize the size  to use
     * @return Returns a NavBoxItemIFace item built from a RolloverCommand
     */
    public static NavBoxItemIFace createBtn(final String label, 
                                            final String iconName, 
                                            final IconManager.IconSize iconSize)
    {
        return createBtn(label, iconName, null, iconSize, null);
    }
    
    /**
     * Returns a NavBoxItemIFace item built from a RolloverCommand
     * @param label the text label
     * @param iconName the icon name, not the image filename, but the IconManager name for the icon
     * @param iconSize the size  to use
     * @param al the action listener that will be added the item 
     * @return Returns a NavBoxItemIFace item built from a RolloverCommand
     */
    public static NavBoxItemIFace createBtn(final String label,
                                            final String iconName, 
                                            final IconManager.IconSize iconSize,
                                            final ActionListener al)
    {
        return createBtn(label, iconName, null, iconSize, al);
    }


}

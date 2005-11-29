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
import edu.ku.brc.specify.ui.RolloverCmdButton;
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
        
        //setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(15, 2, 2, 2));
        //add(Box.createHorizontalGlue());
        //setBorder(BorderFactory.createCompoundBorder(
        //        BorderFactory.createLineBorder(new Color(160,160,160)), getBorder()));
        //setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(160,160,160), 3, true), getBorder()));
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
    
    /**
     * 
     * @param scope
     */
    public void setScope(final NavBoxIFace.Scope scope)
    {
        this.scope = scope;
    }
    
    
    /**
     * Returns the name of the tab (localized)
     * @return the localized name of the tab
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * 
     * @param name
     */
    public void setName(final String name)
    {
        this.name = name;
    }
    
    /**
     * Returns the UI component for this tab
     * @return returns the UI component for this tab
     */
    public JComponent getUIComponent()
    {
        return this;
    }
    
    /**
     * 
     * @param item
     * @return
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
     * 
     * @param item
     * @return
     */
    public Component add(final NavBoxItemIFace item)
    {
        return add(item, false);
    }
        
    /**
     * 
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
    
    public void setMgr(NavBoxMgr mgr)
    {
        this.mgr = mgr;
    }

    /**
     * Herlp method for creating Nav Buttons
     * @param label The 
     * @param iconName
     * @param fileName
     * @param iconSize
     * @param addGap
     * @return
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
    
    public static NavBoxItemIFace createBtn(final String label, 
                                            final String iconName, 
                                            final IconManager.IconSize iconSize)
    {
        return createBtn(label, iconName, null, iconSize, null);
    }
    
    public static NavBoxItemIFace createBtn(final String label,
                                            final String iconName, 
                                            final IconManager.IconSize iconSize,
                                            final ActionListener al)
    {
        return createBtn(label, iconName, null, iconSize, al);
    }


}

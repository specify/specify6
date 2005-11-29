/* Filename:    $RCSfile: StatGroup.java,v $
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
package edu.ku.brc.specify.stats;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.ku.brc.specify.ui.CurvedBorder;

/**
 * This class represents a logical 'group' of statistics. The statistic items do not effect each other.
 * 
 * @author rods
 *
 */
public class StatGroup extends JPanel
{

    public String name;
    
    /**
     * Constructor with the localized name of the Group
     * @param name name of the group (already been localized)
     */
    public StatGroup(final String name)
    {
        this.name = name;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(15, 2, 2, 2));
        setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
    }
    
    /**
     * Enables Threaded objects to ask for a relayout of the UI
     *
     */
    public synchronized void relayout()
    {
        invalidate();
        doLayout();
        repaint();
    }
    
    /**
     * Adds StatItem to group
     * @param item the item to be added
     */
    public void addItem(StatItem item)
    {
        add(item);
    }
    
    /**
     * Overrides paint to draw in name at top with separator
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        Dimension dim = getSize();
        
        FontMetrics fm = g.getFontMetrics();
        int strW = fm.stringWidth(name);
        
        int x = (dim.width - strW) / 2;
        Insets ins = getBorder().getBorderInsets(this);
        int y = 2 + fm.getAscent();
        
        int lineW = dim.width - ins.left - ins.right;
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

}

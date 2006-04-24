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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.CurvedBorder;

/**
 * Class to manage an entire group of StatItems where each StatItem gets its own data from a unique query.
 * This class represents a logical 'group' of statistics. The statistic items do not effect each other.
 * This is a simple class that is mostly used for layoing out the group in a vertial fashion.
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class StatGroup extends JPanel
{

    protected String       name;
    protected PanelBuilder builder    = new PanelBuilder(new FormLayout("p:g", "p,p"));
    protected JPanel       content    = null;
    protected JScrollPane  scrollPane = null;
    
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
        setBackground(Color.WHITE);
    } 
    
    /**
     * Constructor with the localized name of the Group
     * @param name name of the group (already been localized)
     * @param useSeparator use non-border separator titles
     */
    public StatGroup(final String name, boolean useSeparator)
    {
        this.name = name;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        if (useSeparator)
        {
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            CellConstraints cc         = new CellConstraints();
            
            builder.addSeparator(name, cc.xy(1,1));
            
            builder.add(content, cc.xy(1,2));
            builder.getPanel().setOpaque(false);
            add(builder.getPanel());
            
        } else
        {
            setBorder(BorderFactory.createEmptyBorder(15, 2, 2, 2));
            setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
            add(content, BorderLayout.CENTER);
         }
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
        if (content.getComponentCount() > 10 && scrollPane == null)
        {
            Dimension size = content.getPreferredSize();
            remove(content);
            //content.setPreferredSize(size);
            scrollPane = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            Dimension spSize = scrollPane.getPreferredSize();
            scrollPane.setPreferredSize(new Dimension(spSize.width, size.height));
            scrollPane.setBackground(Color.WHITE);//getParent().getBackground());
            content.setBackground(Color.WHITE);
            content.setOpaque(true);
            builder.add(scrollPane, (new CellConstraints()).xy(1,2));
        }
        content.add(item);
    }
    
    /**
     * Overrides paint to draw in name at top with separator
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (content == null)
        {
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

}

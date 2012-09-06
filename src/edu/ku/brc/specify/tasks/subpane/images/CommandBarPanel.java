/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.images;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 2, 2012
 *
 */
public class CommandBarPanel extends JPanel
{
    /**
     * 
     */
    public CommandBarPanel(final ResultSetController rs, final JButton...btns)
    {
        super();
        
        setOpaque(true);
        
        rs.getPanel().setOpaque(false);
        
        CellConstraints cc = new CellConstraints();
        String       colDef = UIHelper.createDuplicateJGoodiesDef("p", "4px", btns.length);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "f:p:g,p,f:p:g"));
        pb.add(rs.getPanel(), cc.xy(2, 2));
        pb.getPanel().setOpaque(false);
        
        PanelBuilder tb = new PanelBuilder(new FormLayout("f:p:g,"+colDef+", 20px", "1px,p,3px"), this);
        tb.add(pb.getPanel(), cc.xy(1, 2));
        
        int x = 2;
        for (JButton btn : btns)
        {
            tb.add(btn, cc.xy(x, 2));
            x += 2;
        }
    }
    
    @Override
    protected void paintComponent(final Graphics g)
    {
        super.paintComponent(g);
        
        Color     base = getBackground();
        Dimension size = getSize();
        
        Color grad_top = base;
        Color grad_bot = UIHelper.makeDarker(base, UIHelper.isMacOS() ? 0.15 : 0.1);     
        GradientPaint bg = new GradientPaint(new Point(0,0), grad_top,
                                             new Point(0, size.height), grad_bot);
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(bg);
        g2.fillRect(0, 0, size.width, size.height);
        
        g.setColor(UIHelper.makeDarker(base, 0.5));
        g.drawLine(0, size.height-1, size.width, size.height-1);
    }

}

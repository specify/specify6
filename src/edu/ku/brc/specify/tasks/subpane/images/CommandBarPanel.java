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
    private ResultSetController rs;
    private JButton[]           leftBtns;
    private JButton[]           rightBtns;
    
    /**
     * 
     */
    public CommandBarPanel(final ResultSetController rs)
    {
        super();
        
        setOpaque(true);
        
        this.rs = rs;
        rs.getPanel().setOpaque(false);
    }

    /**
     * @param btns
     */
    public void setLeftBtns(final JButton...btns)
    {
        leftBtns = btns;
    }
    
    /**
     * @param btns
     */
    public void setRightBtns(final JButton...btns)
    {
        rightBtns = btns;
    }
    
    /**
     * 
     */
    public void createUI()
    {
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder leftPB = null;
        if (leftBtns != null)
        {
            String colDef = UIHelper.createDuplicateJGoodiesDef("p", "4px", leftBtns.length);
            leftPB = new PanelBuilder(new FormLayout("20px," + colDef + ",f:p:g", "p"));
            leftPB.getPanel().setOpaque(false);
            int x = 2;
            for (JButton btn : leftBtns)
            {
                leftPB.add(btn, cc.xy(x, 1));
                x += 2;
            }
        }
        
        PanelBuilder rightPB = null;
        if (rightBtns != null)
        {
            String colDef = UIHelper.createDuplicateJGoodiesDef("p", "4px", rightBtns.length);
            rightPB = new PanelBuilder(new FormLayout("f:p:g, "+colDef + ", 20px", "p"));
            rightPB.getPanel().setOpaque(false);
            int x = 2;
            for (JButton btn : rightBtns)
            {
                rightPB.add(btn, cc.xy(x, 1));
                x += 2;
            }
        }
        
        String leftP  = leftPB != null ? "p" : "f:p:g";
        String rightP = rightPB != null ? "p" : "f:p:g";
        PanelBuilder pb = new PanelBuilder(new FormLayout(leftP+", f:p:g, p, f:p:g, "+rightP, "f:p:g,p,f:p:g"), this);
        
        if (leftPB != null) pb.add(leftPB.getPanel(), cc.xy(1, 2));
        pb.add(rs.getPanel(), cc.xy(3, 2));
        if (rightPB != null) pb.add(rightPB.getPanel(), cc.xy(5, 2));
        pb.getPanel().setOpaque(false);
        
        // No longer needed.
        this.rs        = null;
        this.leftBtns  = null;
        this.rightBtns = null;
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

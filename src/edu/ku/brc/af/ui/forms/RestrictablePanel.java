/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Aug 15, 2008
 *
 */
public class RestrictablePanel extends JPanel implements RestrictableUIIFace
{
    private boolean isRestricted = false;
    private Font    newFont      = null;
    private Color   shadeColor   = new Color(255,255,255,170);
    
    /**
     * 
     */
    public RestrictablePanel()
    {
        
    }
    
    /**
     * @param layout
     */
    public RestrictablePanel(LayoutManager layout)
    {
        super(layout);
    }

    public boolean isRestricted()
    {
        return isRestricted;
    }

    public void setRestricted(boolean isRestricted)
    {
        this.isRestricted = isRestricted;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (isRestricted)
        {
            String restrictedStr = "Restricted";
            Dimension   size = getSize();
            if (newFont == null)
            {
                newFont = g.getFont().deriveFont((float)24.0);
            }
            
            ((Graphics2D)g).setRenderingHints(UIHelper.createTextRenderingHints());
            
            g.setFont(newFont);
            
            FontMetrics fm   = g.getFontMetrics();
            int         sw   = fm.stringWidth(restrictedStr);
            g.setColor(shadeColor);
            g.fillRect(0,0,size.width-1,size.height-1);
            g.setColor(Color.BLACK);
            g.drawString(restrictedStr, (size.width-sw)/2, (size.height+fm.getHeight())/2);

        }
    }
}

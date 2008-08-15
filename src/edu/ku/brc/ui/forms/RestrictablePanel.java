/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.ui.forms;

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
            g.drawString(restrictedStr, (size.width-sw)/2, (size.height+fm.getHeight())/2);
            //g.drawRect(0,0,size.width,size.height);
        }
    }
}

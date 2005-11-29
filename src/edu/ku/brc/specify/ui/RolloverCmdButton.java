/* Filename:    $RCSfile: RolloverCmdButton.java,v $
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
package edu.ku.brc.specify.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import javax.swing.event.*;
import  com.jgoodies.looks.plastic.*;

import edu.ku.brc.specify.core.NavBoxItemIFace;

/**
 * @author Rod Spears
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RolloverCmdButton extends JButton implements NavBoxItemIFace
{
    protected boolean      isOver     = false;
    protected static Color focusColor = Color.BLUE;
    
    
    /**
     * 
     * @param aLabel
     * @param aImgIcon
     */
    public RolloverCmdButton(String aLabel, Icon aImgIcon)
    {
        super(aLabel, aImgIcon);
        
        //UIManager.getLookAndFeel().
        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
            public void mouseEntered(MouseEvent e) 
            {
                isOver = true;
                repaint();
            }
            public void mouseExited(MouseEvent e) 
            {
                isOver = false;
                repaint();
            }
          };
          addMouseListener(mouseInputAdapter);
          addMouseMotionListener(mouseInputAdapter);
    }
    
    /**
     * paints the component
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        if (isOver && !this.hasFocus())
        {
            Dimension dim = getSize();
            Insets insets = getInsets();
            
            g.setColor(UIManager.getLookAndFeel() instanceof PlasticLookAndFeel ? PlasticLookAndFeel.getFocusColor() : Color.BLUE);
            g.drawRect(insets.left, insets.top, dim.width-insets.right-insets.left, dim.height-insets.bottom-insets.top);
        }
    }
    
    // NavBoxItemIFace
    public Component getUIComponent()
    {
        return this;
    }
}

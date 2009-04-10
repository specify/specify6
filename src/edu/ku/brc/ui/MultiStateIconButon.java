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
package edu.ku.brc.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 23, 2007
 *
 */
public class MultiStateIconButon extends JButton
{
    protected ImageIcon[] icons;
    protected int       state = 0;

    public MultiStateIconButon(final ImageIcon[] icons)
    {
        super();
        
        this.icons = icons;
        setIcon(icons[0]);
        setFocusable(true);
        this.setFocusPainted(true);
        
        /*super.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                grabFocus();
                state = state < icons.length-1 ? state + 1 : 0;
                updateVisualState();
            }
        });*/
        
        addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae)
            {
                state = state < icons.length-1 ? state + 1 : 0;
                updateVisualState();
            }
        });
        //setMargin(new Insets(0,0,0,0));
        //setBorder(BorderFactory.createEmptyBorder());
    }
    
    protected void updateVisualState()
    {
        setIcon(icons[state]);
    }

    /**
     * @return the state
     */
    public int getState()
    {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(int state)
    {
        this.state = (state >= icons.length || state < 0) ? 0 : state;
        updateVisualState();
    }
    
}

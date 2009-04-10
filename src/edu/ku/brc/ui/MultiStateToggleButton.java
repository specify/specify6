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
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import edu.ku.brc.util.Pair;

public class MultiStateToggleButton extends JButton
{
    protected List<String> stateNames   = new Vector<String>();
    protected List<Icon>   icons        = new Vector<Icon>();
    protected int          currentIndex = 0;
    
    public MultiStateToggleButton(Pair<Icon,String>... iconsAndNames)
    {
        for (Pair<Icon,String> iconNamePair: iconsAndNames)
        {
            stateNames.add(iconNamePair.second);
            icons.add(iconNamePair.first);
        }
        setupInternalActionListener();
    }
    
    public MultiStateToggleButton(Icon... icons)
    {
        for (Icon icon: icons)
        {
            this.icons.add(icon);
            stateNames.add(null);
        }
        setupInternalActionListener();
    }
    
    protected void setupInternalActionListener()
    {
        addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                clicked();
            }
        });
    }
    
    protected void clicked()
    {
        currentIndex++;
        currentIndex %= stateNames.size();
        updateState();
    }
    
    protected void updateState()
    {
        setIcon(icons.get(currentIndex));
        
        String name = stateNames.get(currentIndex);
        if (name != null)
        {
            setText(name);
        }
    }
    
    public int getStateIndex()
    {
        return currentIndex;
    }
    
    public void setStateIndex(int index)
    {
        this.currentIndex = index;
        updateState();
    }
    
    public static void main(String[] args)
    {
        Icon i1 = new ImageIcon("/home/jstewart/Desktop/RightSideUp.png");
        Icon i2 = new ImageIcon("/home/jstewart/Desktop/LeftSideDown.png");
        Icon i3 = new ImageIcon("/home/jstewart/Desktop/UpAndDown.png");
        
//        Pair<Icon,String> p1 = new Pair<Icon,String>(i1,null);
//        Pair<Icon,String> p2 = new Pair<Icon,String>(i2,null);
//        Pair<Icon,String> p3 = new Pair<Icon,String>(i3,null);
        
        JFrame f = new JFrame();
//        MultiStateToggleButton btn = new MultiStateToggleButton(p1, p2, p3);
        MultiStateToggleButton btn = new MultiStateToggleButton(i1, i2, i3);
        f.add(btn);
        f.pack();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        btn.setStateIndex(0);
        btn.setText("Where");
        
        f.setVisible(true);
    }
}

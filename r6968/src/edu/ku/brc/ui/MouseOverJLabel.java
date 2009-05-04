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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;

/**
 * @author jstewart
 * @code_status Alpha
 *
 */
public class MouseOverJLabel extends JLabel
{
    protected EventListenerList actionListeners;
    protected int actionId;
    protected Color activatedTextColor;
    protected Color normalForeground;
    protected boolean mouseIsOver;
    
    public MouseOverJLabel()
    {
        init();
    }

    public MouseOverJLabel(String text)
    {
        super(text);
        init();
    }

    public MouseOverJLabel(Icon image)
    {
        super(image);
        init();
    }

    public MouseOverJLabel(String text, int horizontalAlignment)
    {
        super(text, horizontalAlignment);
        init();
    }

    public MouseOverJLabel(Icon image, int horizontalAlignment)
    {
        super(image, horizontalAlignment);
        init();
    }

    public MouseOverJLabel(String text, Icon icon, int horizontalAlignment)
    {
        super(text, icon, horizontalAlignment);
        init();
    }
    
    public synchronized void addActionListener(ActionListener listener)
    {
        actionListeners.add(ActionListener.class, listener);
    }
    
    public synchronized void removeActionListener(ActionListener listener)
    {
        actionListeners.remove(ActionListener.class, listener);
    }
    
    public Color getActivatedTextColor()
    {
        return activatedTextColor;
    }

    public void setActivatedTextColor(Color activatedTextColor)
    {
        this.activatedTextColor = activatedTextColor;
    }

    protected void fireActionPerformed()
    {
        ActionEvent ae = new ActionEvent(this,actionId++,"double-click");
        for (ActionListener listener: actionListeners.getListeners(ActionListener.class))
        {
            listener.actionPerformed(ae);
        }
    }
    
    protected void init()
    {
        actionId = 0;
        actionListeners = new EventListenerList();
        activatedTextColor = Color.RED;
        normalForeground = this.getForeground();
        
        this.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                fireActionPerformed();
                super.mouseClicked(e);
            }

            @SuppressWarnings("synthetic-access")
            @Override
            public void mouseEntered(MouseEvent e)
            {
                mouseIsOver = true;
                super.mouseEntered(e);
                MouseOverJLabel.super.setForeground(activatedTextColor);
                MouseOverJLabel.this.repaint();
            }

            @SuppressWarnings("synthetic-access")
            @Override
            public void mouseExited(MouseEvent e)
            {
                mouseIsOver = false;
                super.mouseExited(e);
                MouseOverJLabel.super.setForeground(normalForeground);
                MouseOverJLabel.this.repaint();
            }
            
        });
    }

    @Override
    public void setForeground(Color fg)
    {
        this.normalForeground = fg;
        if (!mouseIsOver)
        {
            super.setForeground(fg);
        }
    }
    
    @Override
    public Color getForeground()
    {
        if (mouseIsOver)
        {
            return this.activatedTextColor;
        }
        // else
        return this.normalForeground;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        MouseOverJLabel l = new MouseOverJLabel("Some text");
        l.setForeground(Color.WHITE);
        l.setActivatedTextColor(Color.RED);
        l.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                System.out.println("click");
            }
        });
        f.add(l);
        f.setSize(200,200);
        f.setVisible(true);
    }

}

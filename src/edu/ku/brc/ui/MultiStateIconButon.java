/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
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

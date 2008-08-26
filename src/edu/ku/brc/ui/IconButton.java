/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 26, 2008
 *
 */
public class IconButton extends JButton
{
    protected static Border emptyBorder;
    protected static Border focusBorder;

    static
    {
        emptyBorder = UIHelper.isMacOS() ? BorderFactory.createEmptyBorder(3, 3, 3, 3) : BorderFactory.createEmptyBorder(1, 1, 1, 1);
        focusBorder = UIHelper.isMacOS() ? new LineBorder(UIManager.getColor("Button.focus") != null ?  UIManager.getColor("Button.focus") : Color.GRAY, 1, true) : new MacBtnBorder();
    }
    
    /**
     * @param icon
     */
    public IconButton(final Icon    icon,
                      final boolean withEmptyBorder)
    {
        super(icon);
        init(withEmptyBorder);
        
    }

    /**
     * @param text
     * @param icon
     */
    public IconButton(final String text, 
                      final Icon icon,
                      final boolean withEmptyBorder)
    {
        super(text, icon);
        init(withEmptyBorder);
    }

    /**
     * @param withEmptyBorder
     */
    protected void init(final boolean withEmptyBorder)
    {
        setOpaque(false);
        
        if (!withEmptyBorder)
        {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    if (((JButton)e.getSource()).isEnabled())
                    {
                        ((JButton)e.getSource()).setBorder(focusBorder);
                    }
                    super.mouseEntered(e);
                }
                @Override
                public void mouseExited(MouseEvent e)
                {
                    if (((JButton)e.getSource()).isEnabled())
                    {               
                        ((JButton)e.getSource()).setBorder(emptyBorder);
                    }
                    super.mouseExited(e);
                }
                
            });
            addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e)
                {
                    if (((JButton)e.getSource()).isEnabled())
                    {
                        ((JButton)e.getSource()).setBorder(focusBorder);
                    }
                }
                public void focusLost(FocusEvent e)
                {
                    if (((JButton)e.getSource()).isEnabled())
                    {               
                        ((JButton)e.getSource()).setBorder(emptyBorder);
                    }
                }
                
            });
            setBorder(emptyBorder);
        }
    }
    

    /* (non-Javadoc)
     * @see javax.swing.AbstractButton#setEnabled(boolean)
     */
    public void setEnabled(final boolean enable)
    {
        super.setEnabled(enable);
        
        if (!enable && isEnabled())
        {
            setBorder(emptyBorder);
            repaint();
        }
    }

}

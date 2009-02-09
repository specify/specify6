/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

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
    protected static BasicStroke   lineStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    protected static Color         focusColor = null;
    protected static Color         hoverColor = new Color(0, 0, 150, 100);
    protected static Border        emptyBorder;
    protected static Border        focusBorder = null;
    
    protected boolean              isHovering = false;

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
        focusBorder = null;
        if (focusBorder == null)
        {
            if (UIHelper.isMacOS())
            {
                focusBorder = new MacBtnBorder();
                Insets fbInsets = focusBorder.getBorderInsets(this);
                emptyBorder = new EmptyBorder(fbInsets);
                
            } else
            {
                if (UIManager.getLookAndFeel() instanceof PlasticLookAndFeel)
                {
                    focusColor = PlasticLookAndFeel.getFocusColor();
                } else
                {
                    focusColor = UIManager.getColor("Button.focus");
                }
                if (focusColor == null)
                {
                    focusColor = Color.DARK_GRAY;
                }
                focusBorder = new LineBorder(focusColor, 1, true);
                emptyBorder = new EmptyBorder(focusBorder.getBorderInsets(this));
            }
        }

        setOpaque(false);
        
        if (!withEmptyBorder)
        {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    isHovering = true;
                    repaint();
                    super.mouseEntered(e);
                }
                @Override
                public void mouseExited(MouseEvent e)
                {
                    isHovering = false;
                    repaint();
                    super.mouseExited(e);
                }
                /* (non-Javadoc)
                 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
                 */
                @Override
                public void mousePressed(MouseEvent e)
                {
                    super.mousePressed(e);
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
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) 
    {
        super.paint(g);
        
        if (isHovering && !hasFocus() && isEnabled())
        {
            g.setColor(hoverColor);
            
            Insets    insets = getInsets();
            Dimension size   = getSize();
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D.Double rr = new RoundRectangle2D.Double(insets.left, insets.top, size.width-insets.right-insets.left, size.height-insets.bottom-insets.top, 10, 10);
            g2d.setStroke(lineStroke);
            g2d.draw(rr);
            rr = new RoundRectangle2D.Double(insets.left+1, insets.top+1, size.width-insets.right-insets.left-2, size.height-insets.bottom-insets.top-2, 10, 10);
            g2d.draw(rr);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.AbstractButton#setEnabled(boolean)
     */
    public void setEnabled(final boolean enable)
    {
        super.setEnabled(enable);
        
        if (!enable)
        {
            setBorder(emptyBorder);
            repaint();
        }
    }

}

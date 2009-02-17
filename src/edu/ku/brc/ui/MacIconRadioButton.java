/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;

import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 15, 2009
 *
 */
public class MacIconRadioButton extends JToggleButton
{
    protected Icon selectedIcon   = null;
    protected Icon unselectedIcon = null;
    
    /**
     * @param selectedIcon
     * @param unselectedIcon
     */
    public MacIconRadioButton(final Icon selectedIcon, final Icon unselectedIcon)
    {
        super(selectedIcon);
        
        this.selectedIcon   = selectedIcon;
        this.unselectedIcon = unselectedIcon;
        
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                setIcon(isSelected() ? selectedIcon : unselectedIcon);
            }
        });
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        Dimension size = super.getPreferredSize();
        /*if (getBorder() != null)
        {
            Insets ins = getBorder().getBorderInsets(this);
            size.width  += ins.left + ins.right;
            size.height += ins.top + ins.bottom;
        }*/
        return size;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g)
    {
        Insets    ins    = getBorder().getBorderInsets(this);
        Dimension size   = getSize();
        int       xc     = ins.left/2;
        int       yc     = ins.top/2;
        int       width  = size.width - ins.left;// - ins.right;
        int       height = size.height - ins.top;// - ins.bottom;
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        BasicStroke    stdLineStroke    = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(stdLineStroke);
        
        if (!isSelected())
        {
            Color color = UIManager.getColor("CheckBox.background");
            Color grad_top = UIHelper.changeColorBrightness(color, isSelected() ? 0.75 : 1.20);
            Color grad_bot = UIHelper.changeColorBrightness(color, isSelected() ? 1.20 : 0.75);
            
            GradientPaint bg = new GradientPaint(new Point(0,0), grad_top,
                                                 new Point(0,height/2), grad_bot);
            g2d.setPaint(bg);
            g2d.fillArc(xc, yc, width-1, height-1, 0, 360);
            Shape clip = g2d.getClip();
            
            g2d.setClip(0,height/2,width,height);
            
            bg = new GradientPaint(new Point(0,height/2), grad_bot,
                                   new Point(0,height),   grad_top);
            
            g2d.setPaint(bg);
            g2d.fillArc(xc, yc, width-1, height-1, 0, 360);
            
            g2d.setClip(clip);
        } else
        {
            g2d.setColor(Color.WHITE);
            g2d.fillArc(xc, yc, width-1, height-1, 0, 360);
        }
        
        g2d.setPaint(null);
        g2d.setColor(new Color(90,90,90));
        g2d.drawArc(xc, yc, width-1, height-1, 0, 360);
        
        Icon icon = getIcon();
        int x = (size.width - icon.getIconWidth()) / 2; 
        int y = (size.height - icon.getIconHeight()) / 2; 
        icon.paintIcon(this, g, x, y);
    }

}

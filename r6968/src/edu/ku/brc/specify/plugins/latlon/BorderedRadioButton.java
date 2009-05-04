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
package edu.ku.brc.specify.plugins.latlon;import java.awt.Color;
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

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Triple;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 10, 2007
 *
 */
public class BorderedRadioButton extends JToggleButton
{
    protected static Border selectedBorder   = null;
    protected static Border unselectedBorder = null;
    
    protected Color         hoverColor = UIHelper.getHoverColor();
    protected Color         focusColor = null;
    protected Border        emptyBorder;
    protected Border        focusBorder = null;
    
    protected boolean       hasFocus   = false;
    protected boolean       isHovering = false;
    
    /**
     * 
     */
    public BorderedRadioButton()
    {
        init();
    }

    /**
     * @param icon
     */
    public BorderedRadioButton(Icon icon)
    {
        super(icon);
        init();
    }

    /**
     * @param a
     */
    public BorderedRadioButton(Action a)
    {
        super(a);
        init();
    }

    /**
     * @param text
     */
    public BorderedRadioButton(String text)
    {
        super(text);
        init();
    }

    /**
     * @param arg0
     * @param arg1
     */
    public BorderedRadioButton(Icon arg0, boolean arg1)
    {
        super(arg0, arg1);
        init();
    }

    /**
     * @param text
     * @param b
     */
    public BorderedRadioButton(String text, boolean b)
    {
        super(text, b);
        init();
    }

    /**
     * @param text
     * @param icon
     */
    public BorderedRadioButton(String text, Icon icon)
    {
        super(text, icon);
        init();
    }

    /**
     * @param text
     * @param icon
     * @param b
     */
    public BorderedRadioButton(String text, Icon icon, boolean b)
    {
        super(text, icon, b);
        init();
    }
    
    /**
     * 
     */
    protected void init()
    {
        if (unselectedBorder != null)
        {
            //unselectedBorder = new CurvedBorder(4, Color.DARK_GRAY, Color.DARK_GRAY);
            //selectedBorder   = new CurvedBorder(4, Color.DARK_GRAY, Color.LIGHT_GRAY);
            
            /*unselectedBorder = new DropShadowBorder(Color.LIGHT_GRAY, 4, 0.8f, 6, true, true, true, true);
            selectedBorder   = new DropShadowBorder(Color.DARK_GRAY, 4, 0.8f, 6, true, true, true, true);
            
            unselectedBorder = new edu.ku.brc.ui.DropShadowBorder(Color.LIGHT_GRAY, 4, 4, 0.8f, 6, true, true, true, true);
            selectedBorder   = new edu.ku.brc.ui.DropShadowBorder(Color.DARK_GRAY, 4, 4, 0.8f, 6, true, true, true, true);
            */
            
            setBorder(unselectedBorder);
            setBorderPainted(true);
        }
        
        Triple<Border, Border, Color> focusInfo = UIHelper.getFocusBorders(this);
        focusBorder = focusInfo.first;
        emptyBorder = focusInfo.second;
        focusColor  = focusInfo.third;
        
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setIconTextGap(1); 
        setMargin(new Insets(0,0,0,0));
        
        this.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent ce)
            {
                JToggleButton rb = (JToggleButton)ce.getSource();
                rb.setBorder(rb.isSelected() ? selectedBorder : unselectedBorder);
            }
        });
        
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
                hasFocus = true;
                if (((BorderedRadioButton)e.getSource()).isEnabled())
                {
                    ((BorderedRadioButton)e.getSource()).setBorder(focusBorder);
                }
            }
            public void focusLost(FocusEvent e)
            {
                hasFocus = false;
                if (((BorderedRadioButton)e.getSource()).isEnabled())
                {               
                    ((BorderedRadioButton)e.getSource()).setBorder(isSelected() ? selectedBorder : unselectedBorder);
                }
            }
            
        });
        setBorder(emptyBorder);

    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(final Graphics g)
    {
        super.paint(g);
        
        if (isHovering && !hasFocus && isEnabled())
        {
            g.setColor(hoverColor);
            
            Insets    insets = getInsets();
            Dimension size   = getSize();
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D.Double rr = new RoundRectangle2D.Double(insets.left, insets.top, size.width-insets.right-insets.left, size.height-insets.bottom-insets.top, 10, 10);
            g2d.setStroke(UIHelper.getStdLineStroke());
            g2d.draw(rr);
            rr = new RoundRectangle2D.Double(insets.left+1, insets.top+1, size.width-insets.right-insets.left-2, size.height-insets.bottom-insets.top-2, 10, 10);
            g2d.draw(rr);
        }
    }
    
    /**
     * 
     */
    public void makeSquare()
    {
        Dimension size = getMinimumSize();
        size.width = size.height;
        setMinimumSize(size);
        setMaximumSize(size);
        setPreferredSize(size);  
    }

    public static void setSelectedBorder(Border selectedBorder)
    {
        BorderedRadioButton.selectedBorder = selectedBorder;
    }

    public static void setUnselectedBorder(Border unselectedBorder)
    {
        BorderedRadioButton.unselectedBorder = unselectedBorder;
    }
}

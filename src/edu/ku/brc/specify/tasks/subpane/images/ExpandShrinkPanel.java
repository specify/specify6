/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.images;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 13, 2012
 *
 */
public class ExpandShrinkPanel extends JPanel implements TimingTarget
{

    public static final int EXPANDED   = 1;
    public static final int CONTRACTED = -1;
    
    protected boolean    animationInProgress = false;
    protected boolean    shrinking           = false;
    protected boolean    expanding           = false;
    protected int        mode;  
    protected Dimension  prefSize;
    protected Dimension  contractedSize;
    
    protected boolean    isVertical;

    /**
     * @param startingMode
     * @param isVertical
     * @param contractedSize
     */
    public ExpandShrinkPanel(final int startingMode, final boolean isVertical)
    {
        super();
        
        this.mode           = startingMode;
        this.isVertical     = isVertical;
        //contractedSize = this.contractedSize;
    }
    
    /**
     * 
     */
    public void createUI()
    {
        
    }
    
    /**
     * 
     */
    protected void doneBuilding()
    {
        prefSize = super.getPreferredSize();
        contractedSize = isVertical ? new Dimension(prefSize.width,0) : new Dimension(0, prefSize.height);
    }
    
    /* (non-Javadoc)
     * @see org.jdesktop.animation.timing.TimingTarget#begin()
     */
    @Override
    public void begin() 
    {
        animationInProgress = true;
    }

    /* (non-Javadoc)
     * @see org.jdesktop.animation.timing.TimingTarget#end()
     */
    @Override
    public void end() 
    {
        animationInProgress = false;
        
        if (expanding)
        {
            mode = EXPANDED;
            expanding = false;
        }
        if (shrinking)
        {
            mode = CONTRACTED;
            shrinking = false;
        }
        
        Component c = getParent();
        c.invalidate();
        c.doLayout();
        c.validate();
        c.repaint();
    }

    /* (non-Javadoc)
     * @see org.jdesktop.animation.timing.TimingTarget#repeat()
     */
    @Override
    public void repeat() 
    {
        // never gets called
    }

    /* (non-Javadoc)
     * @see org.jdesktop.animation.timing.TimingTarget#timingEvent(float)
     */
    @Override
    public void timingEvent(float fraction) 
    {
        float sizeFrac = fraction;
        
        if (shrinking)
        {
            sizeFrac = 1 - fraction;
        }
        
        if (isVertical)
        {
            prefSize.height = (int)(super.getPreferredSize().height * sizeFrac);
        } else
        {
            prefSize.width = (int)(super.getPreferredSize().width * sizeFrac);
        }
        
        //System.out.println(shrinking+"  "+sizeFrac+"  "+prefSize);
        
        Component c = getParent().getParent();
        c.invalidate();
        c.doLayout();
        c.validate();
        c.repaint();
        
        this.invalidate();
        this.repaint();
        this.validate();
    }

    /**
     * 
     */
    public void expand()
    {
        if (mode == EXPANDED || expanding || shrinking)
        {
            return;
        }
        
        if (isVertical)
        {
            prefSize.height = 0;
        } else
        {
            prefSize.width = 0;
        }
        expanding = true;
        
        // start animation to expand the panel
        Animator expander = new Animator(450, this);
        expander.start();
    }

    /**
     * 
     */
    public void contract()
    {
        if (mode == CONTRACTED || shrinking || expanding)
        {
            return;
        }
        
        shrinking = true;
        // start animation to shrink the panel
        Animator expander = new Animator(300, this);
        expander.start();
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        if (shrinking || expanding)
        {
            return prefSize;
        }
        
        if (mode == CONTRACTED)
        {
            return contractedSize;
        }
        
        return super.getPreferredSize();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getMaximumSize()
     */
    @Override
    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }
    
    /**
     * @return true if panel is expanded.
     */
    public boolean isExpanded()
    {
        return mode == EXPANDED;
    }
    
    /**
     * @param pSize
     */
    public void setSpecialPrefSize(final Dimension pSize)
    {
        prefSize.setSize(pSize);
    }
}

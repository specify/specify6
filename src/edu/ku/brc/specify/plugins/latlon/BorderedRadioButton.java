/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
    
    /**
     * 
     */
    public BorderedRadioButton()
    {
        init();
    }

    /**
     * @param arg0
     */
    public BorderedRadioButton(Icon arg0)
    {
        super(arg0);
        init();
    }

    /**
     * @param arg0
     */
    public BorderedRadioButton(Action arg0)
    {
        super(arg0);
        init();
    }

    /**
     * @param arg0
     */
    public BorderedRadioButton(String arg0)
    {
        super(arg0);
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
     * @param arg0
     * @param arg1
     */
    public BorderedRadioButton(String arg0, boolean arg1)
    {
        super(arg0, arg1);
        init();
    }

    /**
     * @param arg0
     * @param arg1
     */
    public BorderedRadioButton(String arg0, Icon arg1)
    {
        super(arg0, arg1);
        init();
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     */
    public BorderedRadioButton(String arg0, Icon arg1, boolean arg2)
    {
        super(arg0, arg1, arg2);
        init();
    }
    
    protected void init()
    {
        if (unselectedBorder != null)
        {
            setBorder(unselectedBorder);
            setBorderPainted(true);

        }
        
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        //setMargin(new Insets(1,1,1,1));
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
    }
    
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

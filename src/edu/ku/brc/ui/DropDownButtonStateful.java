/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

/**
 *  
 * Creates a panel containing an icon and button with a focus "ring" when the mouse is hovering; and it is "stateful"
 * because the buttons toggles between 2 or more states by clicking on the main button. Or you can switch states by selecting
 * a "state" from the list.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class DropDownButtonStateful extends DropDownButton
{
    protected ImageIcon[]            imgIcons      = null;
    protected String[]               labels        = null;
    protected String[]               toolTips      = null;
    protected String                 currLabel     = null;
    protected int                    currInx       = 0;
    protected Dimension              preferredSize = null;

    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action.
     * @param labels the text labels for the UI
     * @param imgIcons the icons for the UI
     */
    public DropDownButtonStateful(final String[]    labels, 
                                  final ImageIcon[] imgIcons)
    {
        this(labels, imgIcons, null);
    }
    
    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action.
     * @param labels the text labels for the UI
     * @param imgIcons the icons for the UI
     * @param toolTips toolTip text
     */
    public DropDownButtonStateful(final String[]    labels, 
                                  final ImageIcon[] imgIcons, 
                                  final String[]    toolTips)
    {
       super(labels[0], imgIcons[0], toolTips != null ? toolTips[0] : null, SwingConstants.CENTER);
       
        setBorder(null);//new EmptyBorder(new Insets(1,1,1,1)));
        setLayout(new BorderLayout());
       
        this.imgIcons = imgIcons;
        this.labels   = labels;
        this.toolTips = toolTips;
        
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                itemSelected(actionEvent.getSource());
                for (ActionListener al : listeners)
                {
                    al.actionPerformed(actionEvent);
                }
            }
        };
          
        // menus need to be set up before the the init
        menus = new ArrayList<JComponent>();
        for (int i=0;i<imgIcons.length;i++)
        {
            JMenuItem menuItem = new JMenuItem(labels[i], imgIcons[i]);
            menuItem.addActionListener(actionListener);
            menus.add(menuItem);
        }
        
        init(labels[0], imgIcons[0], toolTips != null ? toolTips[0] : null);
        
        mainBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                currInx++;
                if (currInx >= imgIcons.length)
                {
                    currInx = 0;
                }
                setCurrentIndex(currInx);
            }
        });
        setCurrentIndex(0);

    }
    
    /**
     * Returns the next index in the stateful button which means we wrap around to zero
     * @return he next index in the stateful button which means we wrap around to zero
     */
    protected int getNextIndex()
    {
        return (currInx+ 1) % labels.length;
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    /*public Dimension getPreferredSize()
    {
        if (preferredSize == null)
        {
            this.validate();
            preferredSize = super.getPreferredSize();
            for (int i=0;i<labels.length;i++)
            {
                mainBtn.setIcon(imgIcons[i]);
                mainBtn.setText(labels[i]);
                this.validate();
                doLayout();
    
                Dimension s = super.getPreferredSize();
                System.out.println(s);
                preferredSize.width = Math.max(s.width, preferredSize.width);
                preferredSize.height = Math.max(s.height, preferredSize.height);
            }
        }
        return preferredSize;
    }*/
    
    /**
     * Sets Current Index.
     * @param index the new index
     */
    public void setCurrentIndex(final int index)
    {
        currInx = index;
        int nxtInx = getNextIndex();
        mainBtn.setIcon(imgIcons[nxtInx]);
        mainBtn.setText(labels[nxtInx]);
        
        if (toolTips != null)
        {
            mainBtn.setToolTipText(toolTips[nxtInx]);
        }
    }
    
    /**
     * Returns the current state index.
     * @return the current state index
     */
    public int getCurrentIndex()
    {
        return currInx;
    }
    
    /**
     * Tells the class that an items was selected from the list.
     * @param obj the object that made the state change (a JMenuItem)
     */
    protected void itemSelected(final Object obj)
    {
        if (obj instanceof JMenuItem)
        {
            JMenuItem mi = (JMenuItem)obj;
            for (int i=0;i<imgIcons.length;i++)
            {
                if (labels[i] != null && labels[i].equals(mi.getText()))
                {
                    setCurrentIndex(i);
                    return;
                }
            }
        }
    }  
}

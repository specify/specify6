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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
    protected List<DropDownMenuInfo>   menuInfoItems;
    protected String                   currLabel     = null;
    protected int                      currInx       = 0;
    protected Dimension                preferredSize = null;

    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action.
     * @param labels the text labels for the UI
     * @param imgIcons the icons for the UI
     */
    public DropDownButtonStateful(final List<DropDownMenuInfo> items)
    {
        super(items.get(0).getLabel(), items.get(0).getImageIcon(), items.get(0).getTooltip(), SwingConstants.RIGHT);
        
        menuInfoItems = items;
        
        init();
    }
    
    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action.
     * @param labels the text labels for the UI
     * @param imgIcons the icons for the UI
     */
    public DropDownButtonStateful(final List<String>    labels, 
                                  final List<ImageIcon> imgIcons)
    {
        this(labels, imgIcons, null);
    }
    
    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action.
     * @param labels the text labels for the UI
     * @param imgIcons the icons for the UI
     * @param toolTips toolTip text
     */
    public DropDownButtonStateful(final List<String>    labels, 
                                  final List<ImageIcon> imgIcons, 
                                  final List<String>    toolTips)
    {
        super(labels != null && labels.size() > 0 ? labels.get(0) : null,
              imgIcons != null && imgIcons.size() > 0 ? imgIcons.get(0) : null, 
              toolTips != null && toolTips.size() > 0 ? toolTips.get(0) : null, SwingConstants.CENTER);

        
        int length = 0;
        if (imgIcons != null)
        {
            length = imgIcons.size();
            
        } else if (labels != null)
        {
            length = labels.size();
        }
        

        
        menuInfoItems = new Vector<DropDownMenuInfo>();
        for (int i=0;i<length;i++)
        {
            menuInfoItems.add(new DropDownMenuInfo(labels != null && labels.size() > 0 ? labels.get(0) : null,
                                                   imgIcons != null && imgIcons.size() > 0 ? imgIcons.get(0) : null, 
                                                   toolTips != null && toolTips.size() > 0 ? toolTips.get(0) : null));
        }

        init();

    }
    
    protected void init()
    {
        //setBorder(null);
        //setLayout(new BorderLayout());
        
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
        for (DropDownMenuInfo mi : menuInfoItems)
        {
            
            JMenuItem menuItem = UICacheManager.createMenuItem(mi.getLabel(), mi.getImageIcon());
            menuItem.addActionListener(actionListener);
            menus.add(menuItem);
        }
        
        //DropDownMenuInfo mi = menuInfoItems.get(0);
        //super.init(mi.getLabel(), mi.getImageIcon(), mi.getTooltip());
        
        mainBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                currInx++;
                if (currInx >= menuInfoItems.size())
                {
                    currInx = 0;
                }
                setCurrentIndex(currInx);
            }
        });
        
        arrowBtn.setVisible(getPopMenuSize() > 0);

        setCurrentIndex(0);
    }

    
    /**
     * Returns the next index in the stateful button which means we wrap around to zero
     * @return he next index in the stateful button which means we wrap around to zero
     */
    protected int getNextIndex()
    {
        return (currInx+ 1) % menuInfoItems.size();
    }
    
    /**
     * Sets Current Index.
     * @param index the new index
     */
    public void setCurrentIndex(final int index)
    {
        currInx = index;
        int nxtInx = getNextIndex();
        DropDownMenuInfo mi = menuInfoItems.get(nxtInx);
        mainBtn.setIcon(mi.getImageIcon());
        mainBtn.setText(mi.getLabel());
        
        if (mi.getTooltip() != null)
        {
            mainBtn.setToolTipText(mi.getTooltip());
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
            JMenuItem item = (JMenuItem)obj;
            int i = 0;
            for (DropDownMenuInfo mi : menuInfoItems)
            {
                if (mi.getLabel() != null && mi.getLabel().equals(item.getText()))
                {
                    setCurrentIndex(i);
                    return;
                }
                i++;
            }
        }
    }
    
}

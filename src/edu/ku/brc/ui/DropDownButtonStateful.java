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

import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
    protected int                      nxtInx        = 0;
    protected Dimension                preferredSize = null;
    protected boolean                  doAdvance     = true;
    protected boolean                  doShowCurrent = true;
    
    protected List<JButton>            btns          = new Vector<JButton>();
    protected CardLayout               cardLayout    = new CardLayout();
    protected JPanel                   cardPanel;


    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action.
     * @param labels the text labels for the UI
     * @param imgIcons the icons for the UI
     */
    public DropDownButtonStateful(final List<DropDownMenuInfo> items)
    {
        this(items, true);
    }
    
    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action.
     * @param labels the text labels for the UI
     * @param imgIcons the icons for the UI
     */
    public DropDownButtonStateful(final List<DropDownMenuInfo> items, final boolean doAdvance)
    {
        super(items.get(0).getLabel(), items.get(0).getImageIcon(), items.get(0).getTooltip(), SwingConstants.RIGHT, items.size() > 0);
        
        menuInfoItems = items;
        
        overrideButtonBorder = true;
        
        init(doAdvance);
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
        super();
        
        int length = 0;
        if (imgIcons != null)
        {
            length = imgIcons.size();
            
        } else if (labels != null)
        {
            length = labels.size();
        }
        
        init(labels != null && labels.size() > 0 ? labels.get(0) : null, 
             imgIcons != null && imgIcons.size() > 0 ? imgIcons.get(0) : null, 
             toolTips != null && toolTips.size() > 0 ? toolTips.get(0) : null, length > 0);
        
        mainBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        mainBtn.setVerticalTextPosition(SwingConstants.CENTER);
        
        menuInfoItems = new Vector<DropDownMenuInfo>();
        for (int i=0;i<length;i++)
        {
            menuInfoItems.add(new DropDownMenuInfo(labels != null && i < labels.size()? labels.get(i) : null,
                                                   imgIcons != null && i < imgIcons.size() ? imgIcons.get(i) : null, 
                                                   toolTips != null && i < toolTips.size() ? toolTips.get(i) : null));
        }

        init(true);
    }

    /**
     * Use this to set to false to the Next item instead of the current item.
     * @param doShowCurrent the doShowCurrent to set
     */
    public void setDoShowCurrent(boolean doShowCurrent)
    {
        this.doShowCurrent = doShowCurrent;
    }

    /**
     * @param doAdvance
     */
    protected void init(final boolean doAdvanceArg)
    {
        this.doAdvance = doAdvanceArg;
        
        ActionListener menuAL = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) 
            {
                int prevInx = currInx;
                itemSelected(actionEvent.getSource());
                
                cardLayout.show(cardPanel, Integer.toString(currInx));
                
                if (currInx != prevInx)
                {
                    for (ActionListener al : listeners)
                    {
                        al.actionPerformed(actionEvent);
                    }
                }
            }
        };
        
        ActionListener btnAL = new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                if (doAdvance)
                {
                    currInx++;
                    if (currInx >= menuInfoItems.size())
                    {
                        currInx = 0;
                    }
                }
                //System.out.println("New Index: "+currInx);
                setCurrentIndex(currInx);
                cardLayout.show(cardPanel, Integer.toString(currInx));
                
                for (ActionListener al : listeners)
                {
                    al.actionPerformed(ae);
                }
            }
        };
        
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        FocusListener     focusListener     = createFocusListener();
        MouseInputAdapter mouseInputAdapter = createMouseInputAdapter();
        
        CellConstraints cc = new CellConstraints();
        
        // menus need to be set up before the the init
        menus = new ArrayList<JComponent>();
        int i = 0;
        for (DropDownMenuInfo mi : menuInfoItems)
        {
            JMenuItem menuItem = new JMenuItem(mi.getLabel(), mi.getImageIcon());
            setControlSize(menuItem);
            menuItem.addActionListener(menuAL);
            menus.add(menuItem);
            
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p", "p"));

            JButton btn = createLabelBtn(mi.getLabel(), mi.getImageIcon(), mi.getTooltip(), this, focusListener, 
                                         mouseInputAdapter, btnAL, this, false);
            btns.add(btn);
            btn.setOpaque(false);
            pb.getPanel().setOpaque(false);
            pb.add(btn, cc.xy(2,1));
            cardPanel.add(pb.getPanel(), Integer.toString(i));
            i++;
        }
        
        setLayout(null);
        removeAll();
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,p", "f:p:g"), this);
        
        pb.add(cardPanel, cc.xy(1, 1));
        pb.add(arrowBtn, cc.xy(2, 1));
        
        popupAnchorComponent = cardPanel;
        

        arrowBtn.setVisible(getPopMenuSize() > 0);

        setCurrentIndex(0);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (isHovering)
        {
            JButton   btn = btns.get(nxtInx);
            Rectangle r   = btn.getBounds();
            Rectangle pr  = getBounds();
            
            pr.x = r.x;
            g.setColor(SystemColor.controlLtHighlight);
            g.drawLine(pr.x, pr.y, pr.width-1, pr.y);
            g.drawLine(pr.x, pr.y, pr.x, pr.height-1);
            
            g.setColor(SystemColor.controlShadow);
            g.drawLine(pr.x, pr.height-1, pr.width-1, pr.height-1);
            g.drawLine(pr.width-1, pr.y, pr.width-1, pr.height-1);
        }
        
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
        nxtInx = doAdvance && !doShowCurrent ? getNextIndex() : index;
        cardLayout.show(cardPanel, Integer.toString(nxtInx));

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
            int i = 0;
            for (JComponent mi : menus)
            {
                if (mi == obj)
                {
                    setCurrentIndex(i);
                    return;                    
                }
                i++;
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.DropDownButton#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        menuInfoItems.clear();
        super.finalize();
    }

    /**
     * @return the doAdvance
     */
    public boolean isDoAdvance()
    {
        return doAdvance;
    }

    /**
     * @param doAdvance the doAdvance to set
     */
    public void setDoAdvance(boolean doAdvance)
    {
        this.doAdvance = doAdvance;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean value)
    {
        super.setEnabled(value);
        
        for (JButton btn : btns)
        {
            btn.setEnabled(value);
        }
        repaint();
    }
    
}

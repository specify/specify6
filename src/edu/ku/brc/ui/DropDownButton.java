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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * 
 * XXX NOTE: This only drops menu down and doesn't check for where it is on the screen
 * 
 * (Adpated from an example by santhosh kumar - santhosh@in.fiorano.com)
 *
 * @code_status Complete
 * 
 * @author rods  
 *
 */
@SuppressWarnings("serial")
public abstract class DropDownButton extends JPanel implements ChangeListener, PopupMenuListener,
                                                                ActionListener, PropertyChangeListener
{
    
    protected EmptyBorder     emptyBorder;
    protected SoftBevelBorder raisedBorder;

    protected JButton       mainBtn;
    protected JButton       arrowBtn          = null;
    protected boolean       popupVisible      = false;
    protected String        statusBarHintText = null;
    
    protected List<JComponent>     menus    = null;
    protected List<ActionListener> listeners = new ArrayList<ActionListener>();
    
    protected static Icon dropDownArrow;
    
    static {
        dropDownArrow = IconManager.getImage("DropDownArrow");
    }
    
    // this class needs a mouse tracker to pop down the menu when the mouse isn't over it or the button

    /**
     * Default Constructor.
     */
    public DropDownButton()
    {
        super();
        init(null, null, null);
    }

    /**
     * Creates a toolbar item with label and icon and their positions.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param toolTip the tooltip text that has already been localized
     * @param textPosition the position of the text as related to the icon
     */
    public DropDownButton(String label, Icon icon, String toolTip, int textPosition)
    {
        init(label, icon, toolTip);
        
        mainBtn.setVerticalTextPosition(textPosition);
        mainBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    /**
     * Constructor with only an icon.
     * @param icon the icon
     */
    public DropDownButton(Icon icon)
    {
        init(null, icon, null);
    }

    /**
     * Creates a toolbar item with label and icon and their positions and menu items to be added.
     * The Items MUST be of class JSeparator or JMenuItem.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param textPosition the position of the text as related to the icon
     * @param menus the list of menu items and separators
     */
    public DropDownButton(final String label, final Icon icon, final int textPosition, final List<JComponent> menus)
    {
        this.menus = menus;
        init(label, icon, null);
        
        mainBtn.setVerticalTextPosition(textPosition);
        mainBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    /**
     * Initializes the internal UI.
     */
    /**
     * @param label
     * @param icon
     * @param toolTip
     */
    protected void init(final String label, final Icon icon, String toolTip)
    {
        mainBtn   = new JButton(label, icon);
        arrowBtn  = new JButton(dropDownArrow);
        
        mainBtn.setBorder(new EmptyBorder(1,4,1,4));
        mainBtn.setIconTextGap(1); 
        mainBtn.setMargin(new Insets(0,0,0,0));
        mainBtn.getModel().addChangeListener(this);
        mainBtn.addPropertyChangeListener("enabled", this);
        if (toolTip != null)
        {
            mainBtn.setToolTipText(toolTip);
        }

        arrowBtn.setBorder(new EmptyBorder(6,4,6,4));
        arrowBtn.getModel().addChangeListener(this);
        arrowBtn.addActionListener(this);
        arrowBtn.setMargin(new Insets(3, 3, 3, 3));
        arrowBtn.setFocusPainted(false); 
        arrowBtn.setFocusable(false);            
        arrowBtn.setVisible(getPopMenuSize() > 0);
        
        
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", "c:p:g"), this);
        CellConstraints cc  = new CellConstraints();
        
        builder.add(mainBtn, cc.xy(1,1));
        builder.add(arrowBtn, cc.xy(3,1));
        
//        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
//        this.add(mainBtn);
//        this.add(Box.createRigidArea(new Dimension(2,2)));
//        this.add(arrowBtn);
        
        raisedBorder = new SoftBevelBorder(BevelBorder.RAISED);
        emptyBorder  = new EmptyBorder(raisedBorder.getBorderInsets(this));
        setBorder(emptyBorder);
        
        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() 
        {
            @Override
            public void mouseEntered(MouseEvent e) 
            {
                setBorder(raisedBorder);
                if (statusBarHintText != null)
                    UICacheManager.displayStatusBarText(statusBarHintText);
                
                arrowBtn.setEnabled(getPopMenuSize() > 0);
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) 
            {
                setBorder(emptyBorder);
                UICacheManager.displayStatusBarText(null);
                repaint();
                
                if (popupVisible)
                {
                    popupVisible = false;

                    mainBtn.getModel().setRollover(false);
                    arrowBtn.getModel().setSelected(false);
                }

                
            }
          };
          addMouseListener(mouseInputAdapter);
          addMouseMotionListener(mouseInputAdapter);
          
          mainBtn.addMouseListener(mouseInputAdapter);
          mainBtn.addMouseMotionListener(mouseInputAdapter);
          arrowBtn.addMouseListener(mouseInputAdapter);
          arrowBtn.addMouseMotionListener(mouseInputAdapter);

          mainBtn.addActionListener(this);
    }
    
    /**
     * Adds listener.
     * @param al the action listener
     */
    public void addActionListener(ActionListener al)
    {
        listeners.add(al);
    }
    
    /**
     * Removes listener.
     * @param al the action listener
     */
    public void removeActionListener(ActionListener al)
    {
        listeners.remove(al);
    }


    /*------------------------------[ PropertyChangeListener ]---------------------------------------------------*/

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
    	Object source = evt.getSource();
    	if(source == mainBtn.getModel() || source == arrowBtn.getModel())
    	{
    		arrowBtn.setEnabled(mainBtn.isEnabled());
	        if (!arrowBtn.isVisible())
	        {
	            arrowBtn.setVisible(getPopMenuSize() > 0);
	            invalidate();
	            doLayout();
	            repaint();
	        }
    	}
    }

    /*------------------------------[ ChangeListener ]---------------------------------------------------*/

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == mainBtn.getModel())
        {
            if (popupVisible && !mainBtn.getModel().isRollover())
            {
                mainBtn.getModel().setRollover(true);
                return;
            }
            arrowBtn.getModel().setRollover(mainBtn.getModel().isRollover());
            arrowBtn.setSelected(mainBtn.getModel().isArmed() && mainBtn.getModel().isPressed());
        } else
        {
            if (popupVisible && !arrowBtn.getModel().isSelected())
            {
                arrowBtn.getModel().setSelected(true);
                return;
            }
            mainBtn.getModel().setRollover(arrowBtn.getModel().isRollover());
        }
        arrowBtn.setVisible(getPopMenuSize() > 0);
    }

    /*------------------------------[ ActionListener ]---------------------------------------------------*/

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae)
    {
        if (ae.getSource() == arrowBtn)
        {
            JPopupMenu popup = getPopupMenu();
            popup.addPopupMenuListener(this);
            popup.show(mainBtn, 0, mainBtn.getHeight());
            
        } else
        {
            for (ActionListener al : listeners)
            {
                al.actionPerformed(ae);
            }
        }
    }

    /*------------------------------[ PopupMenuListener ]---------------------------------------------------*/

    /* (non-Javadoc)
     * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
     */
    public void popupMenuWillBecomeVisible(PopupMenuEvent e)
    {
        popupVisible = true;
        mainBtn.getModel().setRollover(true);
        arrowBtn.getModel().setSelected(true);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
     */
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
    {
        popupVisible = false;

        mainBtn.getModel().setRollover(false);
        arrowBtn.getModel().setSelected(false);
        ((JPopupMenu) e.getSource()).removePopupMenuListener(this); // act as
                                                                    // good
                                                                    // programmer
                                                                    // :)
    }

    /* (non-Javadoc)
     * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
     */
    public void popupMenuCanceled(PopupMenuEvent e)
    {
        popupVisible = false;
    }

    /*------------------------------[ Other Methods ]---------------------------------------------------*/


    /**
     * Returns a new JPopMenu each time it is called, the poopup menu is created from the internal list.
     * @return Returns a new JPopMenu each time it is called, the poopup menu is created from the internal list
     */
    protected JPopupMenu getPopupMenu()
    {
        if (menus != null)
        {
            JPopupMenu popupMenu = new JPopupMenu();
            for (JComponent comp : menus)
            {
                if (comp instanceof JMenuItem)
                {
                    popupMenu.add((JMenuItem)comp);
                    
                } else if (comp instanceof JSeparator)
                {
                    popupMenu.add(comp);                    
                }
            }           
            return popupMenu;
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.DropDownButton#getPopMenuSize()
     */
    protected int getPopMenuSize()
    {
        return menus == null ? 0 : menus.size();
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) 
    {
        //mainBtn.setMargin(new Insets(0,0,0,0));
        
        super.paint(g);
        
        if (getBorder() == raisedBorder && arrowBtn.isVisible())
        {

            Color highlight = raisedBorder.getHighlightInnerColor(mainBtn);
            Color shadow    = raisedBorder.getShadowInnerColor(mainBtn);
            
            g.setColor(shadow);
            Rectangle r = mainBtn.getBounds();

            int x = r.x + r.width;
            int shrink = 0;
            int y1 = r.y+shrink;
            int y2 = r.y+r.height-(shrink*2);
            g.drawLine(x-1, y1,   x,   y1+1);
            g.drawLine(x,   y1+1, x,   y2-1);
            g.drawLine(x,   y2-1, x-1, y2);
            x++;
            g.setColor(highlight);
            g.drawLine(x-1, y1,   x,   y1+1);
            g.drawLine(x,   y1+1, x,   y2-1);
            g.drawLine(x,   y2-1, x-1, y2);
         }
    }
    
    /**
     * Returns the statusBarHintText.
     * @return the statusBarHintText.
     */
    public String getStatusBarHintText()
    {
        return statusBarHintText;
    }

    /**
     * Sets the hint.
     * @param statusBarHintText The statusBarHintText to set.
     */
    public void setStatusBarHintText(String statusBarHintText)
    {
        this.statusBarHintText = statusBarHintText;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean value)
    {
    	mainBtn.setEnabled(value);
    	arrowBtn.setEnabled(value);
    }
}

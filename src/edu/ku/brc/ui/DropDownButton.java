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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
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
public class DropDownButton extends JPanel implements ChangeListener, PopupMenuListener,
                                                      ActionListener, PropertyChangeListener
{
    protected EmptyBorder          emptyBorder;
    protected Border               hoverBorder;

    protected JButton              mainBtn;
    protected JButton              arrowBtn             = null;
    protected boolean              popupVisible         = false;
    protected String               statusBarHintText    = null;
    protected boolean              overrideButtonBorder = false;
    protected List<JComponent>     menus                = null;
    protected List<ActionListener> listeners            = new ArrayList<ActionListener>();
    
    protected boolean              isHovering           = false;
    protected boolean              hasFocus             = false; 
    protected JComponent           popupAnchorComponent = null;
    
    protected static ImageIcon     dropDownArrow;
    
    static 
    {
        dropDownArrow = IconManager.getIcon("DropDownArrow");
    }
    
    // this class needs a mouse tracker to pop down the menu when the mouse isn't over it or the button

    /**
     * Default Constructor.
     */
    public DropDownButton()
    {
        super();
        
        init(null, null, null, false);
    }

    public DropDownButton(final boolean addArrow)
    {
        super();
        
        init(null, null, null, addArrow);
    }

    /**
     * Constructor with only an icon.
     * @param icon the icon
     */
    public DropDownButton(final ImageIcon icon, final boolean addArrow)
    {
        init(null, icon, null, addArrow);
    }
    
    public void setFont(final Font font)
    {
        if (mainBtn != null)
        {
            mainBtn.setFont(font);
        }
    }

    /**
     * Creates a toolbar item with label and icon and their positions.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param toolTip the tooltip text that has already been localized
     * @param textPosition the position of the text as related to the icon
     * @param indicates whether an arrow btn should be added
     */
    public DropDownButton(String label, ImageIcon icon, String toolTip, int horzTextPosition, final boolean addArrow)
    {
        this(label, icon, toolTip, horzTextPosition, SwingConstants.CENTER, addArrow);
    }

    /**
     * Creates a toolbar item with label and icon and their positions.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param toolTip the tooltip text that has already been localized
     * @param horzTextPosition the horizontal position of the text as related to the icon
     * @param vertTextPosition the vertical position of the text as related to the icon
     * @param indicates whether an arrow btn should be added
     */
    public DropDownButton(final String label, ImageIcon icon, 
                          final String toolTip, 
                          final int horzTextPosition, 
                          final int vertTextPosition, 
                          final boolean addArrow)
    {
        init(label, icon, toolTip, addArrow);
        
        mainBtn.setHorizontalTextPosition(horzTextPosition);
        mainBtn.setVerticalTextPosition(vertTextPosition);
    }

    /**
     * Creates a toolbar item with label and icon and their positions and menu items to be added.
     * The Items MUST be of class JSeparator or JMenuItem.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param vertTextPosition the position of the text as related to the icon
     * @param menus the list of menu items and separators
     */
    public DropDownButton(final String label, 
                          final ImageIcon icon, 
                          final int vertTextPosition, 
                          final List<JComponent> menus)
    {
        this.menus = menus;
        
        init(label, icon, null, menus != null && menus.size() > 0);
        
        mainBtn.setVerticalTextPosition(vertTextPosition);
        
        if (vertTextPosition == SwingConstants.BOTTOM || vertTextPosition == SwingConstants.TOP)
        {
            mainBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        }
    }

    /**
     * Initializes the internal UI.
     */
    /**
     * @param label
     * @param icon
     * @param toolTip
     */
    protected void init(final String label, 
                        final ImageIcon icon, 
                        final String toolTip,
                        final boolean addArrowBtn)
    {
        setOpaque(false);
        
        FocusListener     focusListener     = createFocusListener();
        MouseInputAdapter mouseInputAdapter = createMouseInputAdapter();

        mainBtn  = createLabelBtn(label, icon, toolTip, this, focusListener, mouseInputAdapter, this, this, overrideButtonBorder);
        arrowBtn = createArrowBtn(mouseInputAdapter);
        mainBtn.setOpaque(false);
        
        popupAnchorComponent = mainBtn;

        PanelBuilder pb = new PanelBuilder(new FormLayout("p" + (addArrowBtn ? ",p" : ""), "f:p:g"), this);
        CellConstraints cc = new CellConstraints();
        
        pb.add(mainBtn, cc.xy(1, 1));
        if (addArrowBtn)
        {
            pb.add(arrowBtn, cc.xy(2, 1));
        }
        
        hoverBorder = new SoftBevelBorder(BevelBorder.RAISED);
        emptyBorder = new EmptyBorder(hoverBorder.getBorderInsets(this));
        if (!overrideButtonBorder)
        {
            setBorder(emptyBorder);
        }
        
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
    }
    
    /**
     * @return
     */
    public FocusListener createFocusListener()
    {
        return new FocusListener() {
            public void focusGained(FocusEvent arg0)
            {
                hasFocus = true;
                repaint();
            }
            public void focusLost(FocusEvent arg0)
            {
                hasFocus = false;
                repaint();
            }
            
        };
    }

    /**
     * @return
     */
    public MouseInputAdapter createMouseInputAdapter()
    {
        return new MouseInputAdapter() 
        {
            @Override
            public void mouseEntered(MouseEvent e) 
            {
                if (DropDownButton.this.isEnabled())
                {
                    isHovering = true;
                    if (!overrideButtonBorder)
                    {
                        setBorder(hoverBorder);
                    }
                    if (statusBarHintText != null)
                    {
                        UIRegistry.displayStatusBarText(statusBarHintText);
                    }
                    
                    arrowBtn.setEnabled(getPopMenuSize() > 0 && isEnabled());
                    repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) 
            {
                isHovering = false;
                if (DropDownButton.this.isEnabled())
                {
                    if (!overrideButtonBorder)
                    {
                        setBorder(emptyBorder);
                    }
                    UIRegistry.displayStatusBarText(null);
                     
                    if (popupVisible)
                    {
                        popupVisible = false;

                        mainBtn.getModel().setRollover(false);
                        arrowBtn.getModel().setSelected(false);
                    }
                }
                repaint();
            }
          };
    }
    
    /**
     * @param label
     * @param icon
     * @param toolTip
     * @param changeListener
     * @param focusListener
     * @param mouseInputAdapter
     * @param al
     * @param pcl
     * @param overRideButtonBorder
     * @return
     */
    public static JButton createLabelBtn(final String            label, 
                                         final ImageIcon         icon, 
                                         final String            toolTip,
                                         final ChangeListener    changeListener,
                                         final FocusListener     focusListener,
                                         final MouseInputAdapter mouseInputAdapter,
                                         final ActionListener    al,
                                         final PropertyChangeListener pcl,
                                         final boolean           overRideButtonBorder)
    {
        JButton btn   = UIHelper.createButton(label, icon);
        btn.setOpaque(false);
        btn.addFocusListener(focusListener);

        if (!overRideButtonBorder)
        {
            btn.setBorder(new EmptyBorder(3,6,3,4));
        }
        
        btn.setIconTextGap(1); 
        btn.setMargin(new Insets(0,0,0,0));
        btn.getModel().addChangeListener(changeListener);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.addPropertyChangeListener("enabled", pcl);
        
        if (toolTip != null)
        {
            btn.setToolTipText(toolTip);
        }
        
        btn.addMouseListener(mouseInputAdapter);
        btn.addMouseMotionListener(mouseInputAdapter);
        btn.addActionListener(al);

        return btn;
    }
    
    /**
     * @param mouseInputAdapter
     * @return
     */
    public JButton createArrowBtn(final MouseInputAdapter mouseInputAdapter)
    {
        JButton arrowBtn = new JButton(dropDownArrow);
        arrowBtn.setOpaque(false);
        arrowBtn.setBorder(new EmptyBorder(4,4,4,4)); // T,L,B,R
        arrowBtn.getModel().addChangeListener(this);
        arrowBtn.addActionListener(this);
        arrowBtn.setMargin(new Insets(3, 3, 3, 3));
        arrowBtn.setFocusPainted(false); 
        arrowBtn.setFocusable(false);            
        arrowBtn.setVisible(getPopMenuSize() > 0);
        arrowBtn.addMouseListener(mouseInputAdapter);
        arrowBtn.addMouseMotionListener(mouseInputAdapter);
        return arrowBtn;
    }
    
    /**
     * Sets a new hover border
     * @param hoverBorder the hover border
     */
    public void setHoverBorder(final Border raisedBorder)
    {
        this.hoverBorder = raisedBorder;
    }

    /**
     * @param val
     * @param border
     */
    public void setOverrideBorder(boolean val, Border border)
    {
        overrideButtonBorder = val;
        if (val)
        {
            setBorder(border);
        }
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
    
    /**
     * Returns the text of the button.
     * @return the text of the button.
     */
    public String getText()
    {
        return mainBtn.getText();
    }


    /*------------------------------[ PropertyChangeListener ]---------------------------------------------------*/

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
    	Object source = evt.getSource();
    	if (source == mainBtn.getModel() || source == arrowBtn.getModel())
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
            popup.show(popupAnchorComponent, 0, popupAnchorComponent.getHeight());
            
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
        
        if (getBorder() == hoverBorder && arrowBtn.isVisible())
        {

            boolean isSoftBevel = hoverBorder instanceof SoftBevelBorder;
            Color highlight = isSoftBevel ? ((SoftBevelBorder)hoverBorder).getHighlightInnerColor(mainBtn) : Color.LIGHT_GRAY;
            Color shadow    = isSoftBevel ? ((SoftBevelBorder)hoverBorder).getShadowInnerColor(mainBtn) : Color.DARK_GRAY;
            
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
        
        /* Experiment
        if (hasFocus)
        {
            Color focusColor = UIManager.getColor("Button.focus");
            System.out.println(focusColor);
            Dimension size = getSize();
            g.setColor(new Color(217,236,234));
            g.drawRect(0,0,size.width-1, size.height-1);
            g.setColor(new Color(194,213,230));
            g.drawRect(1,1,size.width-3, size.height-3);
            g.setColor(new Color(169,199,225));
            g.drawRect(2,2,size.width-5, size.height-5);
            g.setColor(new Color(134,155,175));
            g.drawRect(3,3,size.width-7, size.height-7);
        }*/
        
    }
    
    /**
     * Returns the menus for the DropDown.
     * @return the menus for the DropDown.
     */
    public List<JComponent> getMenus()
    {
        return menus;
    }

    /**
     * Sets the menus.
     * @param menus the menus.
     */
    public void setMenus(List<JComponent> menus)
    {
        this.menus = menus;
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
        super.setEnabled(value);
        
    	mainBtn.setEnabled(value);
    	arrowBtn.setEnabled(value);
    	if (!value && isHovering)
    	{
        	isHovering = false;
        	repaint();
    	}
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        listeners.clear();
        menus.clear();
        UIHelper.removeMouseListeners(this);
        super.finalize();
    }
    
}

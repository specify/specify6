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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
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
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;


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
    protected static BasicStroke   lineStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    protected static Color         focusColor = null;
    protected static Color         hoverColor = new Color(0, 0, 150, 100);
    
    protected EmptyBorder          emptyBorder;
    protected Border               focusBorder;

    protected JButton              mainBtn;
    protected JButton              arrowBtn             = null;
    protected boolean              popupVisible         = false;
    protected String               statusBarHintText    = null;
    protected boolean              overrideButtonBorder = false;
    protected List<JComponent>     menus                = null;
    protected List<ActionListener> listeners            = new ArrayList<ActionListener>();
    
    protected boolean              isHovering           = false;
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
        
        if (UIHelper.isMacOS())
        {
            focusBorder = new MacBtnBorder();
            emptyBorder = new EmptyBorder(focusBorder.getBorderInsets(this));
            
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
        
        if (!overrideButtonBorder)
        {
            setBorder(emptyBorder);
        }
        
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
    }
     
    /**
     * @param hoverColor the hoverColor to set
     */
    public static void setHoverColor(Color hoverColor)
    {
        DropDownButton.hoverColor = hoverColor;
    }

    /**
     * @return
     */
    public FocusListener createFocusListener()
    {
        return new FocusListener() {
            public void focusGained(FocusEvent e)
            {
                setBorder(focusBorder);
                repaint();
            }
            public void focusLost(FocusEvent e)
            {
                setBorder(emptyBorder);
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
        JButton arwBtn = new JButton(dropDownArrow);
        arwBtn.setOpaque(false);
        arwBtn.setBorder(new EmptyBorder(4,4,4,4)); // T,L,B,R
        arwBtn.getModel().addChangeListener(this);
        arwBtn.addActionListener(this);
        arwBtn.setMargin(new Insets(3, 3, 3, 3));
        arwBtn.setFocusPainted(false); 
        arwBtn.setFocusable(false);            
        arwBtn.setVisible(getPopMenuSize() > 0);
        arwBtn.addMouseListener(mouseInputAdapter);
        arwBtn.addMouseMotionListener(mouseInputAdapter);
        return arwBtn;
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

    /**
     * @param icon
     */
    public void setIcon(final ImageIcon icon)
    {
        mainBtn.setIcon(icon);
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
    
    /**
     * @return
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
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        
        if (!enabled)
        {
            setBorder(emptyBorder); 
        }
        
    	mainBtn.setEnabled(enabled);
    	arrowBtn.setEnabled(enabled);
    	if (!enabled && isHovering)
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

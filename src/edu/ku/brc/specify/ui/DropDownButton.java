/* Filename:    $RCSfile: DropDownButton.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import edu.ku.brc.specify.helpers.XMLHelper;


/**
 * 
 * XXX NOTE: This only drops menu down and doesn't check for where it is on the screen
 * 
 * (Adpated from an example by santhosh kumar)
 * 
 * @author rods
 * @author  santhosh kumar - santhosh@in.fiorano.com 
 *
 */
@SuppressWarnings("serial")
public abstract class DropDownButton extends JButton implements ChangeListener, PopupMenuListener,
                                                                ActionListener, PropertyChangeListener
{
    protected final JButton mainBtn           = this;
    protected JButton       arrowBtn          = null;
    protected boolean       popupVisible      = false;
    protected String        statusBarHintText = null;
    
    protected List<JComponent> menus = null;
    
    protected static ImageIcon dropDownArrow;
    
    static {
        dropDownArrow = new ImageIcon(XMLHelper.getConfigDirPath("dropdownarrow.gif"));
    }
    
    // this class needs a mouse tracker to pop down the menu when the mouse isn't over it or the button

    /**
     * Default Constructor  
     */
    public DropDownButton()
    {
        super();
        init();
    }

    /**
     * Creates a toolbar item with label and icon and their positions.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param textPosition the position of the text as related to the icon
     */
    public DropDownButton(String label, Icon icon, int textPosition)
    {
        super(label, icon);
        init();
        setVerticalTextPosition(textPosition);
        setHorizontalTextPosition(JButton.CENTER);
    }

    /**
     * Constructor with only an icon
     * @param icon the icon
     */
    public DropDownButton(Icon icon)
    {
        super(icon);
        init();
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
        super(label, icon);      
        this.menus = menus;
        init();
        setVerticalTextPosition(textPosition);
        setHorizontalTextPosition(JButton.CENTER);
    }

    /**
     * INitializes the internal UI
     */
    protected void init()
    {

        arrowBtn  = new JButton(dropDownArrow);
        
        Insets insets = new Insets(4,4,4,4);
        mainBtn.setBorder(new EmptyBorder(insets));
        arrowBtn.setBorder(new EmptyBorder(4,4,4,4));
        mainBtn.setIconTextGap(1); 
        mainBtn.setMargin(new Insets(0,0,0,0));

        
        mainBtn.getModel().addChangeListener(this);
        mainBtn.addPropertyChangeListener("enabled", this); // NO I18N
        
        arrowBtn.getModel().addChangeListener(this);
        arrowBtn.addActionListener(this);
        arrowBtn.setMargin(new Insets(3, 3, 3, 3));
        arrowBtn.setFocusPainted(false); 
        arrowBtn.setFocusable(false);            
        //arrowBtn.setEnabled(false);     
        arrowBtn.setVisible(getPopMenuSize() > 0);

    }
    

    /*------------------------------[ PropertyChangeListener ]---------------------------------------------------*/

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
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
        JPopupMenu popup = getPopupMenu();
        popup.addPopupMenuListener(this);
        popup.show(mainBtn, 0, mainBtn.getHeight());
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
     * Returns a new JPopMenu each time it is called, the poopup menu is created from the internal list
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
                    popupMenu.add((JSeparator)comp);                    
                }
            }           
            return popupMenu;
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.DropDownButton#getPopMenuSize()
     */
    protected int getPopMenuSize()
    {
        return menus == null ? 0 : menus.size();
    }

    /**
     * Returns the "complete" component  
     * @return Returns the "complete" component  
     */
    public JPanel getCompleteComp()
    {
        GridBagLayout      gridbag = new GridBagLayout();
        GridBagConstraints c       = new GridBagConstraints();
        
        JPanel panel = new BtnPanel(gridbag, mainBtn, arrowBtn);
        c.fill = GridBagConstraints.VERTICAL;
        gridbag.setConstraints(mainBtn, c);
        panel.add(mainBtn);
        
        gridbag.setConstraints(arrowBtn, c);
        panel.add(arrowBtn);
        return panel;       
    }
    
    /**
     * Herlp so this can add itself to the toolbar properly
     * @param toolbar
     * @return the main button of the control 
     */
    public JButton addToToolBar(JToolBar toolbar)
    {   
        toolbar.add(getCompleteComp());
        return mainBtn;
    }
    
    /**
     * 
     * @author rods
     *
     */
    class BtnPanel extends JPanel
    {
        protected EmptyBorder     emptyBorder;
        protected SoftBevelBorder raisedBorder;
        
        protected JButton mainBtn;
        protected JButton arrowBtn;
        
        public BtnPanel(LayoutManager aLM, JButton aMainBtn, JButton aArrowBtn)
        {
            super(aLM);
            
            mainBtn     = aMainBtn;
            arrowBtn = aArrowBtn;
            
            raisedBorder = new SoftBevelBorder(SoftBevelBorder.RAISED);
            emptyBorder  = new EmptyBorder(raisedBorder.getBorderInsets(this));
            setBorder(emptyBorder);
            
            MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() 
            {
                public void mouseEntered(MouseEvent e) 
                {
                    setBorder(raisedBorder);
                    if (statusBarHintText != null)
                        UICacheManager.displayStatusBarText(statusBarHintText);
                    
                    arrowBtn.setEnabled(getPopMenuSize() > 0);
                    repaint();
                }
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
              
              aMainBtn.addMouseListener(mouseInputAdapter);
              aMainBtn.addMouseMotionListener(mouseInputAdapter);
              aArrowBtn.addMouseListener(mouseInputAdapter);
              aArrowBtn.addMouseMotionListener(mouseInputAdapter);
        }
                
        public int getPreferredWidth()
        {
            return getPreferredSize().width;
        }
        
        public void setSize(Dimension aDim)
        {
            aDim.width = getPreferredWidth();
            super.setSize(aDim);
        }
        
        public void setSize(int aX, int aY)
        {
            aX = getPreferredWidth();
            super.setSize(aX, aY);
        }
        
        public void setBounds(int x, int y, int width, int height)
        {
            width = getPreferredWidth();
            super.setBounds(x, y, width, height);
        }
        
        public void setBounds(Rectangle r)       
        {
            r.width = getPreferredWidth();
            super.setBounds(r);
        }
        

        public void paint(Graphics g) 
        {
            mainBtn.setMargin(new Insets(0,0,0,0));
            
            super.paint(g);
            
            if (getBorder() == raisedBorder && arrowBtn.isVisible())
            {

                Color highlight = raisedBorder.getHighlightInnerColor(mainBtn);
                Color shadow    = raisedBorder.getShadowInnerColor(mainBtn);
                
                g.setColor(shadow);
                Rectangle r = mainBtn.getBounds();

                int x = r.x + r.width ;
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
    }

    /**
     * @return Returns the statusBarHintText.
     */
    public String getStatusBarHintText()
    {
        return statusBarHintText;
    }

    /**
     * @param statusBarHintText The statusBarHintText to set.
     */
    public void setStatusBarHintText(String statusBarHintText)
    {
        this.statusBarHintText = statusBarHintText;
    }
}
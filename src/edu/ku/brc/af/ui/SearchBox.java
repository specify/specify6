/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;

import edu.ku.brc.af.core.expresssearch.SearchConfigService;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.SearchBorder;
import edu.ku.brc.ui.SearchBorderMac;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Sep 9, 2007
 *
 */
public class SearchBox extends JPanel implements ActionListener, PropertyChangeListener
{
    protected static ImageIcon   searchIcon   = getSearchIcon();
    
    protected JTextField         searchText;
    protected ImageIcon          triangleIcon = null;
    protected ImageIcon          icon         = null;
    protected List<JComponent>   menus        = new ArrayList<JComponent>();
    
    protected int                iconWidth;
    protected JButton            clearIconBtn = null;
    
    protected int                heightAdjust;
    protected int                widthAdjust;
    protected int                popupHitWidth;
    
    protected MenuCreator        menuCreator = null;
    
    
    /**
     * @param textField
     * @param menuCreator
     */
    public SearchBox(final JTextField textField, 
                     final MenuCreator menuCreator) 
    {
        this(textField, menuCreator, false);
    }
    
    /**
     * @param textField
     * @param menuCreator
     * @param includeClearIcon
     */
    public SearchBox(final JTextField textField, 
                     final MenuCreator menuCreator,
                     final boolean includeClearIcon) 
    {
        super(null);
        
        this.menuCreator = menuCreator;
        
        SearchConfigService.getInstance().addPropertyChangeListener(this);
        
        // We must be non-opaque since we won't fill all pixels.
        // This will also stop the UI from filling our background.
        setOpaque(false);
        
        triangleIcon = menuCreator != null ? IconManager.getIcon("SearchTriangle") : null;
        icon         = searchIcon;
        int triWidth = triangleIcon != null ? triangleIcon.getIconWidth() : 0;
        
        iconWidth = Math.max(16, iconWidth);
        
        
        if (includeClearIcon)
        {
            Action action = new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            searchText.setText("");
                        }
                    });
                }
            };
            clearIconBtn = UIHelper.createIconBtn("MacSearchClose", null, true, action);
            clearIconBtn.setEnabled(true);
            clearIconBtn.setFocusable(false);
            clearIconBtn.setVisible(false);
        }
        
        if (clearIconBtn != null)
        {
            textField.getDocument().addDocumentListener(new DocumentAdaptor() {
                @Override
                protected void changed(DocumentEvent e)
                {
                    clearIconBtn.setVisible(!textField.getText().isEmpty());
                }
            });
        }

        setBorder(UIHelper.isMacOS() ? new SearchBorderMac(iconWidth+triWidth) : new SearchBorder(iconWidth+triWidth));
        
        this.searchText = textField;
        this.searchText.setBorder(null);
        add(this.searchText);
        if (clearIconBtn != null)
        {
            add(clearIconBtn);
        }
        
        //System.out.println(clearIconBtn.getBounds());
        
        popupHitWidth = 7+iconWidth+triWidth + 2;
        this.searchText.setLocation(popupHitWidth, 4);
        
        Dimension size = textField.getPreferredSize();
        textField.setSize(size);
        
        widthAdjust  = 16 + iconWidth + triWidth;
        heightAdjust = 8;
        
        final SearchBox p = this;
        
        if (menuCreator != null)
        {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    if (searchText.isEnabled())
                    {
                        super.mouseReleased(e);
                        
                        if (e.getPoint().x <= popupHitWidth)
                        {
                            JPopupMenu popup = getPopupMenu(p);
                            popup.show(p, 0, p.getHeight());
                        }
                    }
                }
            });
        }
        
        searchText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e)
            {
                super.focusGained(e);
                repaint();
            }
            @Override
            public void focusLost(FocusEvent e)
            {
                super.focusLost(e);
                repaint();
            }
            
        });
    }
    
    /**
     * @param l
     */
    public void addClearListener(final ActionListener l)
    {
        if (clearIconBtn != null)
        {
            clearIconBtn.addActionListener(l);
        }
    }
    
    
    /**
     * @return the searchText
     */
    public JTextField getSearchText()
    {
        return searchText;
    }


    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        searchText.setEnabled(enabled);
        if (clearIconBtn != null)
        {
            clearIconBtn.setEnabled(enabled);
        }
    }
    
    /**
     * @return the menuCreator
     */
    public MenuCreator getMenuCreator()
    {
        return menuCreator;
    }

    /**
     * @return the "All" icon from the search box.
     */
    public static ImageIcon getSearchIcon()
    {
        if (searchIcon == null)
        {
            if (UIHelper.isMacOS())
            {
                searchIcon = IconManager.getIcon("SearchBoxMac");
            } else
            {
                searchIcon = IconManager.getIcon("SearchBoxLinux", IconManager.IconSize.Std16);
            }
        }
        return searchIcon;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() instanceof JMenuItem)
        {
            JMenuItem mi = (JMenuItem)e.getSource();
            
            if (mi.getIcon() != null && !mi.getText().equals(getResourceString("ESConfig")))
            {
                icon = (ImageIcon)mi.getIcon();
                repaint();
            }
        }
    }
    
    /**
     * Changes the left Icon to it's default state.
     */
    public void resetSearchIcon()
    {
        icon = searchIcon;
        repaint();
    }

    /**
     * Creates the pop up menu and instals its own listener.
     * @param l the listern
     * @return the poop up menu
     */
    protected JPopupMenu getPopupMenu(final ActionListener l)
    {
        if (menuCreator != null)
        {
            JPopupMenu popupMenu = new JPopupMenu();
            for (JComponent comp : menuCreator.createPopupMenus())
            {
                if (comp instanceof JMenuItem)
                {
                    popupMenu.add((JMenuItem)comp);
                    ((JMenuItem)comp).addActionListener(l);
                    
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
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) 
    {
        int height = getHeight();
        int width  = getWidth();
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, width-1, height-1, height-1, height-1);
        
        int ih = icon.getIconHeight();
        
        g.drawImage(icon.getImage(), 6, ((height-ih)/2)+1, null);
        if (triangleIcon != null)
        {
            g.drawImage(triangleIcon.getImage(), 6+iconWidth, ((height-triangleIcon.getIconWidth())/2)+2, null);
        }
        
        // Now call the superclass behavior to paint the foreground.
        super.paintComponent(g);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        Dimension size = searchText.getPreferredSize();
        size.width  += widthAdjust;
        size.height += heightAdjust;
        return size;
    }
    
    /**
     * @param width
     * @param height
     */
    protected void resizeSearchText(final int width, final int height)
    {
        Dimension d = searchText.getSize();
        d.width     = width - widthAdjust - 5 - (clearIconBtn != null ? clearIconBtn.getWidth() : 0);
        d.height    = height - heightAdjust;
        searchText.setSize(d);
        
        if (clearIconBtn != null)
        {
            Rectangle r = clearIconBtn.getBounds();
            r.x = searchText.getX() + d.width;
            r.y = (getSize().height - r.height) / 2;
            r.width  = clearIconBtn.getWidth();
            r.height = clearIconBtn.getHeight();
            clearIconBtn.setBounds(r);
        }
        
    }

    /* (non-Javadoc)
     * @see java.awt.Component#resize(java.awt.Dimension)
     */
    @Override
    public void resize(Dimension d)
    {
        super.setSize(d);
        resizeSearchText(d.width, d.height);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#resize(int, int)
     */
    @Override
    public void resize(int width, int height)
    {
        super.setSize(width, height);
        resizeSearchText(width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
        resizeSearchText(width, height);
    }


    /* (non-Javadoc)
     * @see java.awt.Component#setBounds(java.awt.Rectangle)
     */
    @Override
    public void setBounds(Rectangle r)
    {
        super.setBounds(r);
        resizeSearchText(r.width, r.height);
    }


    /* (non-Javadoc)
     * @see java.awt.Component#setSize(java.awt.Dimension)
     */
    @Override
    public void setSize(Dimension d)
    {
        super.setSize(d.width, d.height);
        resizeSearchText(d.width, d.height);
    }


    /* (non-Javadoc)
     * @see java.awt.Component#setSize(int, int)
     */
    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);
        resizeSearchText(width, height);
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (menuCreator != null)
        {
            if (evt.getPropertyName().equals("contentsChanged"))
            {
                menuCreator.reset();
                
            } else if (evt.getPropertyName().equals("noContext"))
            {
                icon = searchIcon;
                repaint();
            }
        }
    }

    //------------------------------------------------------
    //-- Interface for Create=ing the menus in the search popup
    //------------------------------------------------------
    public interface MenuCreator 
    {
        /**
         * @return the list of menus to be displayed
         */
        public abstract List<JComponent> createPopupMenus();
        
        /**
         * Tells the creator to reset itself because of changes to the SearchConfig.
         */
        public abstract void reset();
    }
}

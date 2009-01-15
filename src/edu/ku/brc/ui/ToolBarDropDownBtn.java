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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import edu.ku.brc.af.prefs.AppPreferences;

/**
 * Toolbar button derived from DropDownBtn, this provides a way to set menu items.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ToolBarDropDownBtn extends DropDownButton implements CommandListener
{
    protected static Color hoverColor  = new Color(0, 0, 150, 100);
    protected static final String PREFS = "Preferences";

    /**
     * Creates a toolbar item with label and icon and their positions.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param textPosition the position of the text as related to the icon
     */
    public ToolBarDropDownBtn(final String    label, 
                              final ImageIcon icon, 
                              final int       textPosition, 
                              final boolean   addArrowBtn)
    {
        super(label, icon, null, textPosition, addArrowBtn);
        CommandDispatcher.register(PREFS, this);
    }

    /**
     * Creates a toolbar item with label and icon and their positions.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param toolTip the toolTip
     * @param textPosition the position of the text as related to the icon
     */
    public ToolBarDropDownBtn(final String    label, 
                              final ImageIcon icon, 
                              final String    toolTip, 
                              final int       textPosition, 
                              final boolean   addArrowBtn)
    {
        super(label, icon, toolTip, textPosition, addArrowBtn);
        hoverBorder = emptyBorder;
        CommandDispatcher.register(PREFS, this);
    }

    /**
     * Creates a toolbar item with label and icon and their positions and menu items to be added.
     * The Items MUST be of class JSeparator or JMenuItem.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param vertTextPosition the position of the text as related to the icon
     * @param menus the list of menu items and separators
     */
    public ToolBarDropDownBtn(final String           label, 
                              final ImageIcon        icon, 
                              final int              vertTextPosition, 
                              final List<JComponent> menus)
    {
        super(label, icon, vertTextPosition, menus);      
        hoverBorder = emptyBorder;
        CommandDispatcher.register(PREFS, this);
    }

    /**
     * Creates a toolbar item with icon.
     * @param icon the icon for the button.
     */
    public ToolBarDropDownBtn(final ImageIcon icon)
    {
        super(icon, false);
        hoverBorder = emptyBorder;
        CommandDispatcher.register(PREFS, this);
    }
    
    /**
     * @param hoverColor the hoverColor to set
     */
    public static void setHoverColor(final Color hoverColor)
    {
        ToolBarDropDownBtn.hoverColor = hoverColor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.DropDownButton#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (isHovering && !this.hasFocus())
        {
            Color color = (this.hasFocus() && UIManager.getLookAndFeel() instanceof PlasticLookAndFeel) ? PlasticLookAndFeel.getFocusColor() : hoverColor;
            g.setColor(color);
            
            Insets    insets = getInsets();
            Dimension size   = getSize();
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D.Double rr = new RoundRectangle2D.Double(insets.left, insets.top, size.width-insets.right-insets.left, size.height-insets.bottom-insets.top, 10, 10);
            g2d.draw(rr);
            rr = new RoundRectangle2D.Double(insets.left+1, insets.top+1, size.width-insets.right-insets.left-2, size.height-insets.bottom-insets.top-2, 10, 10);
            g2d.draw(rr);
        }
    }
    
    /**
     * 
     */
    public void shutdown()
    {
        CommandDispatcher.unregister(PREFS, this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.DropDownButton#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        shutdown();
    }

    /**
     * @param appPrefs
     */
    protected void prefsChanged(final AppPreferences appPrefs)
    {
        if (appPrefs == AppPreferences.getRemote())
        {
            AppPreferences ap = AppPreferences.getLocalPrefs();
            String key      = "ui.formatting.controlSizes"; //$NON-NLS-1$
            String fontName = ap.get(key+".FN", UIHelper.getSysBaseFont().getFamily());//$NON-NLS-1$
            int    size     = ap.getInt(key+".SZ", UIHelper.getSysBaseFont().getSize());//$NON-NLS-1$
            
            mainBtn.setFont(UIHelper.adjustFont(new Font(fontName, Font.PLAIN, size)));
            mainBtn.validate();
            mainBtn.repaint();
            
            Container parent = getParent();
            while (!(parent instanceof JToolBar) && parent != null)
            {
                parent = parent.getParent();
            }
            
            if (parent != null)
            {
                parent.invalidate();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(PREFS))
        {
            prefsChanged((AppPreferences)cmdAction.getData());
        }
    }
    
 }

/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 27, 2008
 *
 */
@SuppressWarnings("serial")
public class ExtendedTabPanel extends JPanel
{
    protected JLabel textLabel;
    protected Icon   icon;
    protected Component comp;
    
    
    public ExtendedTabPanel(final Component comp,
                            final String    title,
                            final Icon      icon)
    {
        super(new BorderLayout());
        this.comp = comp;
        this.icon = icon;
        
        JLabel closeBtn = new JLabel(IconManager.getIcon("Close"));
        closeBtn.setBorder(null);
        closeBtn.setOpaque(false);
        closeBtn.addMouseListener(new TabMouseAdapter(comp, closeBtn));
        
        add(textLabel = new JLabel(title, icon, SwingConstants.RIGHT), BorderLayout.WEST);
        add(new JLabel(" "), BorderLayout.CENTER);
        add(closeBtn, BorderLayout.EAST);
        
        if (UIHelper.isMacOS())
        {
            setOpaque(false);
            
            if (UIHelper.isMacOS_10_7_X())
            {
                textLabel.setForeground(Color.WHITE);
            }
        }
        
        addFocusListener(new FocusListener()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                textLabel.setForeground(Color.BLACK);
            }
            
            @Override
            public void focusGained(FocusEvent arg0)
            {
                textLabel.setForeground(Color.WHITE);
            }
        });
    }
    
    public JLabel getTextLabel()
    {
        return textLabel;
    }

    public Icon getIcon()
    {
        return icon;
    }

    public Component getComp()
    {
        return comp;
    }

    /**
     * @param titleStr
     */
    public void setTitle(final String titleStr)
    {
        textLabel.setText(titleStr);
        validate();
    }
    
    class TabMouseAdapter extends MouseAdapter
    {
        protected Component tabComp;
        protected JLabel    closeBtn;
        
        public TabMouseAdapter(final Component tabComp, final JLabel closeBtn) 
        {
            this.tabComp  = tabComp;
            this.closeBtn = closeBtn;
        }
        
        @Override
        public void mouseClicked(MouseEvent e)
        {
            SubPaneIFace subPane = (SubPaneIFace)tabComp;
            SubPaneMgr.getInstance().showPane(subPane);
            SubPaneMgr.getInstance().closeCurrent();
        }
        @Override
        public void mouseEntered(MouseEvent e)
        {
            closeBtn.setIcon(IconManager.getIcon("CloseHover"));
            closeBtn.repaint();
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            closeBtn.setIcon(IconManager.getIcon("Close"));
            closeBtn.repaint();
        }
    }
}

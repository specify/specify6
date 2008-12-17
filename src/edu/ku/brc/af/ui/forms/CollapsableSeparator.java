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
package edu.ku.brc.af.ui.forms;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;

/**
 * A collapsable panel where there is a "more" button that indicates whether the pane
 * is collapsed or not. Then a component to its right that is a separator and then the panel 
 * below it.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Dec 13, 2006
 *
 */
public class CollapsableSeparator extends JPanel
{
    protected Component    innerComp        = null;
    protected JCheckBox    moreBtn;
    protected ImageIcon    forwardImgIcon;
    protected ImageIcon    downImgIcon;
    
    protected JPanel       subPanel         = null;
    protected CardLayout   cardLayout       = new CardLayout();
    protected PanelBuilder panelBldr;
    protected boolean      includeMore;
    
    protected Hashtable<String, Component> compsHash = new Hashtable<String, Component>();

    /**
     * Create a collapsable panel where there is a "more" button that indicates whether the pane
     * is collapsed or not. Then a component to its right that is a separator and then the panel 
     * below it.
     * @param separator this can be any component but usually it is a separator.
     */
    public CollapsableSeparator(final Component separator, final boolean includeMore)
    {
        this.includeMore = includeMore;
        
        init();
        
        panelBldr = new PanelBuilder(new FormLayout((includeMore ? "p," : "") + "p,f:p:g", "b:p"), this);
        CellConstraints cc        = new CellConstraints();
        
        int x = 1;
        if (includeMore)
        {
            panelBldr.add(moreBtn, cc.xy(x,1));
            x++;
        }
        panelBldr.add(separator, cc.xy(x,1));
    }
    
    /**
     * @param title
     */
    public CollapsableSeparator(final String title, final boolean includeMore)
    {
        this.includeMore = includeMore;
        init();
        
        panelBldr = new PanelBuilder(new FormLayout((includeMore ? "p," : "") +"p,p,f:p:g", "b:p"), this);
        CellConstraints cc        = new CellConstraints();
        
        subPanel = new JPanel(cardLayout);
        subPanel.setBorder(null);
        subPanel.setOpaque(false);

        int x = 1;
        if (includeMore)
        {
            panelBldr.add(moreBtn, cc.xy(x,1));
            x++;
        }
        JLabel titleLbl = UIHelper.createLabel(title);
        titleLbl.setFont(UIManager.getFont("TitledBorder.font"));
        titleLbl.setForeground(UIManager.getColor("TitledBorder.titleColor"));
        panelBldr.add(titleLbl, cc.xy(x, 1));
        x += 2;
        panelBldr.addSeparator(" ",                cc.xy(x, 1));
    }
    
    /**
     * 
     */
    protected void init()
    {
        if (includeMore)
        {
            forwardImgIcon = IconManager.getIcon("Forward");
            downImgIcon    = IconManager.getIcon("Down");
            moreBtn        = new JCheckBox("", forwardImgIcon); // I18N
            moreBtn.setOpaque(false);
            moreBtn.setFocusable(false);
        }
        UIHelper.setControlSize(moreBtn);
        setOpaque(false);
    }

    /**
     * @param comp
     * @param key
     */
    public void addToSubPanel(final JComponent comp, final String key)
    {
        if (subPanel != null && subPanel.getComponentCount() == 0)
        {
            CellConstraints cc = new CellConstraints();
            panelBldr.add(subPanel, cc.xy(includeMore ? 3 : 2, 1));
            
            
            if (((Container)comp).getComponentCount() > 0)
            {
                subPanel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
            }
        }
        subPanel.add(comp, key);
        compsHash.put(key, comp);
    }
    
    /**
     * @param key
     */
    public void showSubPanel(final String key)
    {
        cardLayout.show(subPanel, key);
        
        subPanel.setVisible(((Container)compsHash.get(key)).getComponentCount() > 0);
    }
    
    /**
     * Sets the component that will be hidden or collapsed.
     * @param innerComp the component
     */
    public void setInnerComp(final Component innerComp)
    {
        if (innerComp != null && this.innerComp == null)
        {
            this.innerComp = innerComp;
            
            if (moreBtn != null)
            {
                moreBtn.setIcon(innerComp.isVisible() ? downImgIcon : forwardImgIcon);
                
                moreBtn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (innerComp.isVisible())
                        {
                            innerComp.setVisible(false);
                            moreBtn.setIcon(forwardImgIcon);
                        } else
                        {
                            innerComp.setVisible(true);
                            moreBtn.setIcon(downImgIcon);
                        }
                        invalidate();
                        doLayout();
                        repaint();
                    }
                 });
            }
        }
    }    
}

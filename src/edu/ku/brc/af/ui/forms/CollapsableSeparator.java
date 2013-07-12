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
    protected JComponent   extraComp;
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
     * @param includeMore
     * @param extraComp
     */
    public CollapsableSeparator(final Component separator, 
                                final boolean includeMore,
                                final JComponent extraComp)
    {
        this.includeMore = includeMore;
        this.extraComp = extraComp;
        
        init();
        
        String rowDef = (includeMore ? "p," : "") + "p,f:p:g" + (extraComp != null ? ",p" : "");
        panelBldr = new PanelBuilder(new FormLayout(rowDef, "b:p"), this);
        CellConstraints cc        = new CellConstraints();
        
        int x = 1;
        if (includeMore)
        {
            panelBldr.add(moreBtn, cc.xy(x,1));
            x++;
        }
        
        panelBldr.add(separator, cc.xy(x,1));
        
        if (extraComp != null)
        {
            panelBldr.add(extraComp, cc.xy(x,1));
            x++;
        }
    }
    
    /**
     * @param title
     * @param includeMore
     * @param extraComp
     */
    public CollapsableSeparator(final String title, 
                                final boolean includeMore,
                                final JComponent extraComp)
    {
        this.includeMore = includeMore;
        this.extraComp = extraComp;
        
        init();
        
        String rowDef = (includeMore ? "p," : "") +"p,p,f:p:g" + (extraComp != null ? ",p" : "");
        panelBldr = new PanelBuilder(new FormLayout(rowDef, "c:p"), this);
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
        if (!UIHelper.isMacOS())
        {
            titleLbl.setFont(UIManager.getFont("TitledBorder.font"));
            titleLbl.setForeground(UIManager.getColor("TitledBorder.titleColor"));
        }
        panelBldr.add(titleLbl, cc.xy(x, 1));
        x += 2;
        panelBldr.addSeparator(" ",                cc.xy(x, 1));
        x++;
        
        if (extraComp != null)
        {
            panelBldr.add(extraComp, cc.xy(x,1));
            x++;
        }

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

    /**
     * @return the moreBtn
     */
    public JCheckBox getMoreBtn()
    {
        return moreBtn;
    }   
}

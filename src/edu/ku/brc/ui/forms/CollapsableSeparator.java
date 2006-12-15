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
package edu.ku.brc.ui.forms;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;

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
    protected Component innerComp        = null;
    protected JCheckBox moreBtn;
    protected ImageIcon forwardImgIcon;
    protected ImageIcon downImgIcon;

    /**
     * Create a collapsable panel where there is a "more" button that indicates whether the pane
     * is collapsed or not. Then a component to its right that is a separator and then the panel 
     * below it.
     * @param separator this can be any component but usually it is a separator.
     */
    public CollapsableSeparator(final Component separator)
    {
        PanelBuilder    panelBldr = new PanelBuilder(new FormLayout("p,2px,f:p:g", "f:p:g"), this);
        CellConstraints cc        = new CellConstraints();
        
        forwardImgIcon = IconManager.getIcon("Forward");
        downImgIcon    = IconManager.getIcon("Down");
        moreBtn        = new JCheckBox("", forwardImgIcon); // I18N
        
        panelBldr.add(moreBtn, cc.xy(1,1));
        panelBldr.add(separator, cc.xy(3,1));
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

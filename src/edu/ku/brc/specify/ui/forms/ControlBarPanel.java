/* Filename:    $RCSfile: ControlBarPanel.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/03/29 0:0:0 $
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
package edu.ku.brc.specify.ui.forms;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.helpers.UIHelper;

/**
 * A panel at the bottom of a form that is divided into 3 sections, 
 * the middle section is reserved for the record controller UI
 * 
 * @author rods
 *
 */
public class ControlBarPanel extends JPanel
{
    protected PanelBuilder builder;
    protected JPanel       leftSidePanel  = null;
    protected JPanel       rightSidePanel = null;
    
    /**
     * Constructor
     * @param rsc the recordset cntroller
     */
    public ControlBarPanel(final ResultSetController rsc)
    {
        builder = new PanelBuilder(new FormLayout("p,2px,c:p:g,2px,p", "p"), this);
        CellConstraints cc = new CellConstraints();
        builder.add(rsc.getPanel(), cc.xy(3,1));
    }
    
    /**
     * Adds a row of buttons to the left or right side of the recordset controller
     * @param btns the array of buttons to be added
     * @param onLeftSide true for the left side, false for the right side
     */
    public void addButtons(final JButton[] btns, final boolean onLeftSide)
    {
        CellConstraints cc      = new CellConstraints();
        JPanel panel = ButtonBarFactory.buildGrowingBar(btns);
        if (onLeftSide)
        {
            leftSidePanel = panel;
            builder.add(leftSidePanel, cc.xy(1,1));
        } else
        {
            rightSidePanel = panel;
            builder.add(rightSidePanel, cc.xy(5,1));
        }
    }
    
    /**
     * Adds an array of components to the left or right side of the recordset controller
     * @param comps the array of controls
     * @param onLeftSide true for the left side, false for the right side
     */
    public void addComponents(final JComponent[] comps, final boolean onLeftSide)
    {
        CellConstraints cc       = new CellConstraints();
        String          colsDef  = comps.length == 1 ? "p" : UIHelper.createDuplicateJGoodiesDef("p", "2px", comps.length);
        PanelBuilder    pBuilder = new PanelBuilder(new FormLayout(colsDef, "p"));
        
        for (int i=0;i<comps.length;i++)
        {
            pBuilder.add(comps[i], cc.xy((i*2)+1, 1));
        }
        if (onLeftSide)
        {
            leftSidePanel = pBuilder.getPanel();
            builder.add(leftSidePanel, cc.xy(1,1));
        } else
        {
            rightSidePanel = pBuilder.getPanel();
            builder.add(rightSidePanel, cc.xy(5,1));
        }
    }
    
    

}

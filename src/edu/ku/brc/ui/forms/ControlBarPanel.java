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

import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.UIHelper;

/**
 * A panel at the bottom of a form that is divided into 3 sections,
 * the middle section is reserved for the record controller UI
 
 * @code_status Beta
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ControlBarPanel extends JPanel
{
    protected PanelBuilder        builder;
    protected JPanel              leftSidePanel  = null;
    protected JPanel              rightSidePanel = null;
    protected ResultSetController recordSetController;

    /**
     * Constructor
     */
    public ControlBarPanel()
    {
        builder = new PanelBuilder(new FormLayout("p,2px,c:p:g,2px,p", "p"), this);
    }


    /**
     * Adds a ResultSetController to the center pane
     * @param recordSetController the recordset cntroller
     */
    public void add(final ResultSetController _recordSetController)
    {
        CellConstraints cc = new CellConstraints();
        builder.add(_recordSetController.getPanel(), cc.xy(3,1));
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
     * @param compsList the List of controls
     * @param onLeftSide true for the left side, false for the right side
     */
    public void addComponents(final List<JComponent> compsList, final boolean onLeftSide)
    {
        if (compsList != null && compsList.size() > 0)
        {
            CellConstraints cc       = new CellConstraints();
            String          colsDef  = compsList.size() == 1 ? "p" : UIHelper.createDuplicateJGoodiesDef("p", "2px", compsList.size());
            PanelBuilder    pBuilder = new PanelBuilder(new FormLayout(colsDef, "p"));
    
            for (int i=0;i<compsList.size();i++)
            {
                pBuilder.add(compsList.get(i), cc.xy((i*2)+1, 1));
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

    /**
     * Sets the ResultSetController's visibility
     * @param vis true visible, false hidden
     */
    public void setRSCVisibility(final boolean vis)
    {
        if (recordSetController != null)
        {
            recordSetController.getPanel().setVisible(vis);
        }
    }

}

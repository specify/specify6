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
package edu.ku.brc.af.ui.forms;

import java.awt.Color;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
 *
 * @code_status Beta
 *
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
    public ControlBarPanel(final Color bgColor)
    {
        builder = new PanelBuilder(new FormLayout("p,2px,f:p:g,2px,p", "p"), this);
        setBackground(bgColor);
        setOpaque(false);
    }

    /**
     * @param recordSetController the recordSetController to set
     */
    public void setRecordSetController(ResultSetController recordSetController)
    {
        this.recordSetController = recordSetController;
    }

    /**
     * Adds a Component that controls the index and/or the creation and deletions of objects, to the center pane
     * @param comp the recordset cntroller
     */
    public void addController(final JComponent comp)
    {
        CellConstraints cc = new CellConstraints();
        builder.add(comp, cc.xy(3,1));
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
        panel.setBackground(getBackground());
        panel.setOpaque(false);
        
        if (onLeftSide)
        {
            if (leftSidePanel != null)
            {
                remove(leftSidePanel);
            }
            leftSidePanel = panel;
            builder.add(leftSidePanel, cc.xy(1,1));
            
            if (rightSidePanel == null)
            {
                rightSidePanel = new JPanel();
                rightSidePanel. setLayout(new BoxLayout(rightSidePanel, BoxLayout.X_AXIS));
                rightSidePanel.add(Box.createRigidArea(leftSidePanel.getPreferredSize()));
                builder.add(rightSidePanel, cc.xy(5,1));
                rightSidePanel.setOpaque(false);
            }
            
        } else
        {
            if (rightSidePanel != null)
            {
                remove(rightSidePanel);
            }
            rightSidePanel = panel;
            builder.add(rightSidePanel, cc.xy(5,1));
            
            if (leftSidePanel == null)
            {
                leftSidePanel = new JPanel();
                leftSidePanel. setLayout(new BoxLayout(leftSidePanel, BoxLayout.X_AXIS));
                leftSidePanel.add(Box.createRigidArea(rightSidePanel.getPreferredSize()));
                builder.add(leftSidePanel, cc.xy(1,1));
                leftSidePanel.setOpaque(false);

            }

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
                if (leftSidePanel != null)
                {
                    remove(leftSidePanel);
                }
                
                leftSidePanel = pBuilder.getPanel();
                builder.add(leftSidePanel, cc.xy(1,1));
                
                if (rightSidePanel == null)
                {
                    rightSidePanel = new JPanel();
                    rightSidePanel. setLayout(new BoxLayout(rightSidePanel, BoxLayout.X_AXIS));
                    rightSidePanel.add(Box.createRigidArea(leftSidePanel.getPreferredSize()));  
                    builder.add(rightSidePanel, cc.xy(5,1));
                }
            } else
            {
                if (rightSidePanel != null)
                {
                    remove(rightSidePanel);
                }
                
                rightSidePanel = pBuilder.getPanel();
                rightSidePanel.validate();
                rightSidePanel.doLayout();
                
                builder.add(rightSidePanel, cc.xy(5,1));
                
                if (leftSidePanel == null)
                {
                    leftSidePanel = new JPanel();
                    leftSidePanel. setLayout(new BoxLayout(leftSidePanel, BoxLayout.X_AXIS));
                    leftSidePanel.add(Box.createRigidArea(rightSidePanel.getPreferredSize()));
                    builder.add(leftSidePanel, cc.xy(1,1));
                }
            }
        }
        
        if (leftSidePanel != null)
        {
            leftSidePanel.setBackground(getBackground());
        }
        if (rightSidePanel != null)
        {
            rightSidePanel.setBackground(getBackground());
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

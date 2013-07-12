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
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.PermissionPanelContainerIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * A class that manages several panels where each panel is a Permissions Editor.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Oct 21, 2008
 *
 */
@SuppressWarnings("serial")
public class PermissionPanelEditor extends JPanel
{
    protected JPanel     cardPanel    = new JPanel();
    protected CardLayout cardLayout   = new CardLayout();
    protected JComboBox  switcherCBX;
    
    protected JButton    selectAllBtn;
    protected JButton    deselectAllBtn;
    
    protected Vector<PermissionPanelContainerIFace> panels = new Vector<PermissionPanelContainerIFace>();
    
    /**
     * @param selectAllBtn
     * @param deselectAllBtn
     */
    public PermissionPanelEditor(final JButton selectAllBtn, 
                                 final JButton deselectAllBtn)
    {
        super();
        
        this.selectAllBtn   = selectAllBtn;
        this.deselectAllBtn = deselectAllBtn;
        
        cardPanel   = new JPanel(cardLayout);
        
        switcherCBX = new JComboBox(new DefaultComboBoxModel());

        // to let the panel shrink correctly (bug 6409)
        cardPanel.setPreferredSize(new Dimension(200, 50));

        CellConstraints cc = new CellConstraints();
        PanelBuilder topPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        topPB.add(switcherCBX, cc.xy(2, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "p,10px,f:p:g"), this);
        pb.add(topPB.getPanel(), cc.xy(1, 1));
        pb.add(cardPanel,        cc.xy(1, 3));
        
        switcherCBX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                cardLayout.show(cardPanel, switcherCBX.getSelectedItem().toString());
                boolean supportsSelectAll = doesSupportSelectAll();
                if (selectAllBtn != null)
                {
                    selectAllBtn.setEnabled(supportsSelectAll);   
                    deselectAllBtn.setEnabled(supportsSelectAll);   
                }
            }
        });
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    public void setVisible(final boolean vis)
    {
       super.setVisible(vis);
       
       if (vis)
       {
           boolean supportsSelectAll = doesSupportSelectAll();
           if (selectAllBtn != null)
           {
               selectAllBtn.setEnabled(supportsSelectAll);   
               deselectAllBtn.setEnabled(supportsSelectAll);   
           }
       }
    }
    
    /**
     * @param panel
     */
    public void addPanel(final PermissionPanelContainerIFace panel)
    {
        cardPanel.add(panel.getPanelName(), panel.getUIComponent());
        panels.add(panel);
        ((DefaultComboBoxModel)switcherCBX.getModel()).addElement(panel.getPanelName());
    }
    
    /**
     * @param session
     * @throws Exception
     */
    public void savePermissions(final DataProviderSessionIFace session) throws Exception
    {
        for (PermissionPanelContainerIFace panel : panels)
        {
            panel.savePermissions(session);
        }
    }
    
    /**
     * @return whether the current editor supports Select All / Deselect All.
     */
    public boolean doesSupportSelectAll()
    {
        int inx = switcherCBX.getSelectedIndex();
        if (inx > -1)
        {
            return panels.get(inx).doesSupportSelectAll();
        }
        return false;
    }

    /**
     * 
     */
    public void selectAll()
    {
        int inx = switcherCBX.getSelectedIndex();
        if (inx > -1)
        {
            panels.get(inx).selectAll();
        }
    }
    
    /**
     * 
     */
    public void deselectAll()
    {
        int inx = switcherCBX.getSelectedIndex();
        if (inx > -1)
        {
            panels.get(inx).deselectAll();
        }  
    }
    
    /**
     * @param principal
     * @param overrulingPrincipal
     * @param existingPerms
     * @param overrulingPerms
     * @param userType
     */
    public void updateData(final SpPrincipal                     principal, 
                           final SpPrincipal                     overrulingPrincipal, 
                           final Hashtable<String, SpPermission> existingPerms,
                           final Hashtable<String, SpPermission> overrulingPerms,
                           final String                          userType)
    {
        for (PermissionPanelContainerIFace panel : panels)
        {
            panel.updateData(principal, overrulingPrincipal, existingPerms, overrulingPerms, userType);
        }
    }

    /**
     * @return the panels
     */
    public Vector<PermissionPanelContainerIFace> getPanels()
    {
        return panels;
    }
}

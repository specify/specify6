/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
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
 * @code_status Alpha
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
    
    protected Vector<PermissionPanelContainerIFace> panels = new Vector<PermissionPanelContainerIFace>();
    
    /**
     * 
     */
    public PermissionPanelEditor()
    {
        super();
        
        cardPanel   = new JPanel(cardLayout);
        
        switcherCBX = new JComboBox(new DefaultComboBoxModel());
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder topPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        topPB.add(switcherCBX, cc.xy(2, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "p,10px,p:g"), this);
        pb.add(topPB.getPanel(), cc.xy(1, 1));
        pb.add(cardPanel,        cc.xy(1, 3));
        
        switcherCBX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                cardLayout.show(cardPanel, switcherCBX.getSelectedItem().toString());
            }
        });
        
        if (SecurityAdminPane.isDoDebug())
        {
            cardPanel.setBackground(Color.CYAN);
            setBackground(Color.GREEN);
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
     * @param principal
     * @param overrulingPrincipal
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

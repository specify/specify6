/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.PermissionPanelContainerIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.ComparatorByStringRepresentation;

/**
 * This editor enables each object to offer it's own panel editing permissions.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 21, 2008
 *
 */
public class IndvPanelPermEditor extends JPanel implements PermissionPanelContainerIFace
{
    //private static final Logger log = Logger.getLogger(IndvPanelPermEditor.class);
    
    protected String                panelName;
    protected PermissionEnumerator  enumerator;
    protected SpPrincipal           principal;
    protected ChangeListener        listener;
    protected boolean               readOnly;
    
    protected JList                 list;
    protected DefaultListModel      model = new DefaultListModel();
    protected JPanel                mainPanel;
    protected ImageIcon             icon;
      
    protected Vector<PermissionEditorRowIFace> rowDataList = new Vector<PermissionEditorRowIFace>();
    protected PermissionEditorIFace            editor      = null;
    protected PermissionEditorRowIFace         prevRowData = null;
    protected BasicPermisionPanel              basicEditor = new BasicPermisionPanel();
    
    
    
    /**
     * @param permissionTable
     * @param enumerator
     */
    public IndvPanelPermEditor(final String panelName,
                               final PermissionEnumerator enumerator,
                               final ChangeListener       listener)
    {
        this(panelName, enumerator, listener, false);
    }

    /**
     * @param panelName
     * @param enumerator
     * @param listener
     * @param readOnly
     */
    public IndvPanelPermEditor(final String               panelName,
                               final PermissionEnumerator enumerator,
                               final ChangeListener       listener, 
                               final boolean              readOnly)
    {
        super(new BorderLayout());
        
        this.panelName          = panelName;
        this.enumerator         = enumerator;
        this.principal          = null;
        this.listener           = listener;
        this.readOnly           = readOnly;
        
        list = new JList(model);
        JScrollPane sp = UIHelper.createScrollPane(list);
        list.setCellRenderer(new PermWrapperRenderer());
        
        mainPanel = new JPanel(new BorderLayout());
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,10px,f:p:g", "f:p:g"), this);
        CellConstraints cc = new CellConstraints();
        
        pb.add(sp,        cc.xy(1, 1));
        pb.add(mainPanel, cc.xy(3, 1));
        
        //basicEditor.addChangeListener(listener);
        
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (editor != null)
                    {
                        editor.removeChangeListener(listener);
                        mainPanel.remove(editor.getUIComponent());
                    
                        if (editor.hasChanged() && prevRowData != null)
                        {
                            prevRowData.setPermissions(editor.getPermissions());
                            editor.setChanged(false);
                        }
                    }
                    
                    PermissionEditorRowIFace rowData = (PermissionEditorRowIFace)list.getSelectedValue();
                    if (rowData != null)
                    {
                        editor = rowData.getEditorPanel();
                        if (editor == null)
                        {
                            editor = basicEditor;
                        }
                        
                        editor.setPermissions(rowData.getPermissions());
                        mainPanel.add(editor.getUIComponent(), BorderLayout.CENTER);
                        mainPanel.invalidate();
                        mainPanel.validate();
                        mainPanel.repaint();
                    }
                    prevRowData = rowData;
                    
                    editor.addChangeListener(listener);
                }
            }
        });
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelIFace#getPanelName()
     */
    @Override
    public String getPanelName()
    {
        return panelName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelIFace#getUIComponent()
     */
    @Override
    public Component getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelContainerIFace#getPermissionEnumerator()
     */
    @Override
    public PermissionEnumerator getPermissionEnumerator()
    {
        return enumerator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelMgrIFace#savePermissions(edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void savePermissions(final DataProviderSessionIFace session) throws Exception
    {
        // nothing to save if we didn't specify a principal yet
        if (principal == null)
        {
            return;
        }
        
        // Make sure the last panel being changed get it values saved.
        if (editor != null && editor.hasChanged() && prevRowData != null)
        {
            prevRowData.setPermissions(editor.getPermissions());
            editor.setChanged(false);
            editor.removeChangeListener(listener);
        }
        
        for (PermissionEditorRowIFace rowData : rowDataList)
        {
            for (SpPermission perm : rowData.getPermissionList())
            {
                if ( !(perm.canView() || perm.canAdd() || perm.canModify() || perm.canDelete()))
                {
                    // no flag is set, so delete the permission
                    if (perm.getId() != null)
                    {
                        perm.setActions("");
                        session.saveOrUpdate(session.merge(perm));
                    }
                }
                else if (perm.hasSameFlags(perm.canView(), perm.canAdd(), perm.canModify(), perm.canDelete()))
                {
                    // permission has changed: save it
                    if (perm.getId() == null)
                    {
                        // permission doesn't yet exist in database: attach it to its principal
                        principal.getPermissions().add(perm);
                        perm.getPrincipals().add(principal);
                    }
                    
                    session.saveOrUpdate(session.merge(perm));
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelContainerIFace#updateData(edu.ku.brc.specify.datamodel.SpPrincipal, edu.ku.brc.specify.datamodel.SpPrincipal, boolean)
     */
    @Override
    public void updateData(final SpPrincipal                     principalArg, 
                           final SpPrincipal                     overrulingPrincipal, 
                           final Hashtable<String, SpPermission> existingPerms,
                           final Hashtable<String, SpPermission> overrulingPerms,
                           final String                          userType)
    {
        // save principal used when saving permissions later
        this.principal = principalArg;
        
        rowDataList.clear();
        
        List<PermissionEditorRowIFace> perms = enumerator.getPermissions(principalArg, existingPerms, overrulingPerms, userType);
        Collections.sort(perms, new ComparatorByStringRepresentation<PermissionEditorRowIFace>(true));
        for (PermissionEditorRowIFace perm : perms) 
        {
            rowDataList.add(perm);
        }
        
        if (model == null) 
        {
            return;
        }
        
        model.clear();
        
        IconManager.IconSize iconSize = IconManager.IconSize.Std20;
        icon = IconManager.getIcon("SystemSetup", iconSize);
        
        JLabel label = UIHelper.createLabel("XXXX");
        label.setIcon(icon);
        
        for (PermissionEditorRowIFace permWrapper : rowDataList) 
        {
            model.addElement(permWrapper);
        }
    }
    
    //---------------------------------------------------------------
    public class PermWrapperRenderer extends DefaultListCellRenderer 
    {
        protected ImageIcon blankIcon;
        
        public PermWrapperRenderer() 
        {
            this.setOpaque(false);
            this.blankIcon = null;
        }

        public Component getListCellRendererComponent(JList listArg,
                                                      Object value,   // value to display
                                                      int index,      // cell index
                                                      boolean iss,    // is the cell selected
                                                      boolean chf)    // the list and the cell have the focus
        {
            super.getListCellRendererComponent(listArg, value, index, iss, chf);

            PermissionEditorRowIFace rowData = (PermissionEditorRowIFace)value;
            setIcon(rowData.getIcon() != null ? rowData.getIcon() : blankIcon);
            
            if (iss) 
            {
                setOpaque(true);
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                list.setSelectedIndex(index);

            } else 
            {
                this.setOpaque(false);
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setText(rowData.getTitle());
            return this;
        }
    }

}

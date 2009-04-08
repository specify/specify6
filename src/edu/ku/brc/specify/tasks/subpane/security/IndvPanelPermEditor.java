/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.PermissionPanelContainerIFace;
import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.VerticalSeparator;
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
@SuppressWarnings("serial")
public class IndvPanelPermEditor extends JPanel implements PermissionPanelContainerIFace
{
    //private static final Logger log = Logger.getLogger(IndvPanelPermEditor.class);
    
    protected String                panelName;
    protected PermissionEnumerator  enumerator;
    protected SpPrincipal           principal                 = null;
    protected SpPrincipal           overrulingPrincipal; 
    protected Hashtable<String, SpPermission> existingPerms;
    protected Hashtable<String, SpPermission> overrulingPerms;
    protected String                userType;
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
    
    protected PermissionEditor                 tableEditor;
    protected TablePermissionEnumerator        tblEnumerator    = null;
    protected boolean                          doAddTableEditor = false;
    
    /**
     * @param permissionTable
     * @param enumerator
     */
    public IndvPanelPermEditor(final String               panelNameKey,
                               final String               descKey,
                               final PermissionEnumerator enumerator,
                               final ChangeListener       listener)
    {
        this(panelNameKey, descKey, enumerator, listener, false);
    }

    /**
     * @param panelNameKey
     * @param descKey
     * @param enumerator
     * @param listener
     * @param readOnly
     */
    public IndvPanelPermEditor(final String               panelNameKey,
                               final String               descKey,
                               final PermissionEnumerator enumerator,
                               final ChangeListener       listener, 
                               final boolean              readOnly)
    {
        super(new BorderLayout());
        
        this.panelName          = UIRegistry.getResourceString(panelNameKey);
        this.enumerator         = enumerator;
        this.principal          = null;
        this.listener           = listener;
        this.readOnly           = readOnly;
        
        if (doAddTableEditor)
        {
            tblEnumerator = new TablePermissionEnumerator();
            tblEnumerator.setTableIds(new int[] {});
            tableEditor = new PermissionEditor("SEC_TABLES", tblEnumerator, listener);
            tableEditor.setVisible(false);
        }
        
        list = new JList(model);
        JScrollPane sp = UIHelper.createScrollPane(list);
        list.setCellRenderer(new PermWrapperRenderer());
        
        mainPanel = new JPanel(new BorderLayout());
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,10px,p:g,5px,p,5px,f:p:g", "p,2px,f:p:g"), this);
        CellConstraints cc = new CellConstraints();
        
        pb.add(UIHelper.createI18NLabel(descKey, SwingConstants.CENTER),  cc.xy(1, 1));
        pb.add(sp,          cc.xy(1, 3));
        pb.add(mainPanel,   cc.xywh(3, 1, 1, 3));
        
        // This needs a little work for saving and refreshing
        // I am disabling it for now
        if (doAddTableEditor)
        {
            pb.add(new VerticalSeparator(new Color(224, 224, 224), new Color(124, 124, 124)), cc.xywh(5, 1, 1, 3));
            pb.add(tableEditor, cc.xy(7, 3));
        }
        
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
                        
                        for (int i=1;i<9;i *= 2)
                        {
                            editor.setOverrideText(i, rowData.getOverrideText(i), readOnly);
                        }
                        editor.setTitle(rowData.getTitle());
                        editor.setPermissions(rowData.getPermissions());
                        
                        if (doAddTableEditor)
                        {
                            int[] tableIds = editor.getAssociatedTableIds();
                            if (tableIds != null && tableIds.length > 0)
                            {
                                tblEnumerator.setTableIds(tableIds);
                                tableEditor.setVisible(true);
                                
                                if (tableEditor != null && principal != null)
                                {
                                    tableEditor.updateData(principal, overrulingPrincipal, existingPerms, overrulingPerms, userType);
                                }
    
                            } else
                            {
                                tableEditor.setVisible(false);
                            }
                        }
                        
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
        
        final int selectedIndex = list.getSelectedIndex();
        
        for (PermissionEditorRowIFace rowData : rowDataList)
        {
            for (SpPermission perm : rowData.getPermissionList())
            {
                SpPermission newPerm = perm;
                if ( !(perm.canView() || perm.canAdd() || perm.canModify() || perm.canDelete()))
                {
                    // no flag is set, so delete the permission
                    if (perm.getId() != null)
                    {
                        perm.setActions("");
                        newPerm = session.merge(perm);
                        session.saveOrUpdate(newPerm);
                    }
                }
                else if (perm.hasSameFlags(perm.canView(), perm.canAdd(), perm.canModify(), perm.canDelete()))
                {
                    // permission has changed: save it
                    if (perm.getId() == null)
                    {
                        // permission doesn't yet exist in database: attach it to its principal
                        perm.getPrincipals().add(principal);
                    }
                    newPerm = perm.getId() == null ? perm : session.merge(perm);
                    session.saveOrUpdate(newPerm);
                    //session.saveOrUpdate(session.merge(principal));
                }
                rowData.updatePerm(perm, newPerm);
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                list.setSelectedIndex(selectedIndex);
            }
        });
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelContainerIFace#updateData(edu.ku.brc.specify.datamodel.SpPrincipal, edu.ku.brc.specify.datamodel.SpPrincipal, boolean)
     */
    @Override
    public void updateData(final SpPrincipal                     principalArg, 
                           final SpPrincipal                     overrulingPrincipalArg, 
                           final Hashtable<String, SpPermission> existingPermsArg,
                           final Hashtable<String, SpPermission> overrulingPermsArg,
                           final String                          userTypeArg)
    {
        final int selectedIndex = list.getSelectedIndex();
        
        // save principal used when saving permissions later
        this.principal           = principalArg;
        this.overrulingPrincipal = overrulingPrincipalArg;
        this.existingPerms       = existingPermsArg;
        this.overrulingPerms     = overrulingPermsArg;
        this.userType            = userTypeArg;
        
        rowDataList.clear();
        
        List<PermissionEditorRowIFace> perms = enumerator.getPermissions(principalArg, existingPermsArg, overrulingPermsArg, userTypeArg);
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
            //permWrapper.addListRow(model, permWrapper.getIcon());
        }
        
        if (doAddTableEditor)
        {
            if (tableEditor != null && principal != null)
            {
                tableEditor.updateData(principal, overrulingPrincipal, existingPerms, overrulingPerms, userType);
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                list.setSelectedIndex(selectedIndex == -1 ? 0 : selectedIndex);
            }
        });
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    public void setVisible(final boolean vis)
    {
       super.setVisible(vis); 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelContainerIFace#doesSupportSelectAll()
     */
    @Override
    public boolean doesSupportSelectAll()
    {
        return false;//!UIRegistry.isRelease();
    }
    
    /**
     * @param options
     */
    private void setAllRows(final int options)
    {
        for (PermissionEditorRowIFace rowData : rowDataList) 
        {
            List<PermissionIFace> permList = rowData.getPermissions();
            for (PermissionIFace item : permList)
            {
                item.setOptions(options);
            }
            rowData.setPermissions(permList);
            if (rowData.getEditorPanel() != null)
            {
                rowData.getEditorPanel().setPermissions(permList);
                rowData.getEditorPanel().setChanged(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelContainerIFace#deselectAll()
     */
    @Override
    public void deselectAll()
    {
        setAllRows(PermissionSettings.NO_PERM);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelContainerIFace#selectAll()
     */
    @Override
    public void selectAll()
    {
        setAllRows(PermissionSettings.ALL_PERM);
    }

    //---------------------------------------------------------------
    //-- 
    //---------------------------------------------------------------
    public class PermWrapperRenderer extends DefaultListCellRenderer 
    {
        protected ImageIcon blankIcon;
        
        /**
         * 
         */
        public PermWrapperRenderer() 
        {
            this.setOpaque(false);
            this.blankIcon = null;
        }

        /* (non-Javadoc)
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
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

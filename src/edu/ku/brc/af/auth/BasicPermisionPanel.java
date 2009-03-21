/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.auth;

import static edu.ku.brc.ui.UIHelper.*;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 21, 2008
 *
 */
public class BasicPermisionPanel extends JPanel implements PermissionEditorIFace
{
    protected PermissionIFace permissions = null;
    protected JCheckBox       viewChk;
    protected JCheckBox       addChk;
    protected JCheckBox       modifyChk;
    protected JCheckBox       delChk;
    
    protected JLabel          label = new JLabel();
    
    protected String[]        originalLabels = new String[4];
    
    protected boolean hasChanged = false; 
    
    /**
     * @param changeListener
     */
    public BasicPermisionPanel()
    {
        this(null, "SEC_PERM", "SEC_VIEW_TITLE", "SEC_ADD_TITLE", "SEC_MOD_TITLE", "SEC_DEL_TITLE");
    }
    
    /**
     * @param changeListener
     */
    public BasicPermisionPanel(final ChangeListener changeListener)
    {
        this(changeListener, "SEC_PERM", "SEC_VIEW_TITLE", "SEC_MOD_TITLE", "SEC_ADD_TITLE", "SEC_DEL_TITLE");
    }
    
    /**
     * @param titleKey
     * @param viewKey
     */
    public BasicPermisionPanel(final String titleKey,
                               final String viewKey)
    {
        this(null, titleKey, viewKey, null, null, null);
    }
    
    /**
     * @param titleKey
     * @param viewKey
     * @param modKey
     */
    public BasicPermisionPanel(final String titleKey,
                               final String viewKey,
                               final String modKey)
    {
        this(null, titleKey, viewKey, modKey, null, null);
    }
    

    /**
     * @param titleKey
     * @param viewKey
     * @param modKey
     * @param addKey
     * @param delKey
     */
    public BasicPermisionPanel(final String titleKey,
                               final String viewKey,
                               final String modKey,
                               final String addKey,
                               final String delKey)
    {
        this(null, titleKey, viewKey, modKey, addKey, delKey);
    }
    
    /**
     * @param changeListener
     * @param titleKey
     * @param viewKey
     * @param modKey
     * @param addKey
     * @param delKey
     */
    public BasicPermisionPanel(final ChangeListener changeListener,
                               final String titleKey,
                               final String viewKey,
                               final String modKey,
                               final String addKey,
                               final String delKey)
    {
        super();
        
        CellConstraints cc = new CellConstraints();
        int numRows = addKey != null ? 1 : 0;
        numRows += delKey != null ? 1 : 0;
        numRows += modKey != null ? 1 : 0;
        numRows += viewKey != null ? 1 : 0;
        numRows++;
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,f:p:g", UIHelper.createDuplicateJGoodiesDef("p", "4px", numRows)), this);
        
        originalLabels[0] = viewKey != null ? UIRegistry.getResourceString(viewKey) : null;
        originalLabels[1] = modKey  != null ? UIRegistry.getResourceString(modKey) : null;
        originalLabels[2] = addKey  != null ? UIRegistry.getResourceString(addKey) : null;
        originalLabels[3] = delKey  != null ? UIRegistry.getResourceString(delKey) : null;
        
        int y = 1;
        PanelBuilder sepPB = new PanelBuilder(new FormLayout("p,0px,f:p:g", "p"));
        sepPB.add(label, cc.xy(1,1));
        sepPB.addSeparator(" ", cc.xy(3, 1));
        
        pb.add(sepPB.getPanel(),   cc.xyw(1, y, 2)); y+= 2;
        if (viewKey != null) pb.add(viewChk  = createCheckBox(originalLabels[0]), cc.xy(1, y)); y += 2;
        if (modKey != null) pb.add(modifyChk = createCheckBox(originalLabels[1]),   cc.xy(1, y)); y += 2;
        if (addKey != null) pb.add(addChk    = createCheckBox(originalLabels[2]),   cc.xy(1, y)); y += 2;
        if (delKey != null) pb.add(delChk    = createCheckBox(originalLabels[3]),   cc.xy(1, y)); y += 2;
        
        addChangeListener(changeListener);
        
        addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                hasChanged = true;
            }
        });
    }
    
    /**
     * @param title
     */
    public void setTitle(final String title)
    {
        label.setText(title);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#setOverrideText(int, java.lang.String, boolean)
     */
    @Override
    public void setOverrideText(int option, String text, final boolean isReadOnly)
    {
        boolean hasText = text != null;
        
        switch (option)
        {
            case PermissionSettings.CAN_VIEW :
                if (viewChk != null) 
                {
                    viewChk.setText(originalLabels[0] + (hasText ? " ("+ text +")" : ""));
                    viewChk.setEnabled(!hasText && !isReadOnly);
                }
                break;
                
            case PermissionSettings.CAN_MODIFY :
                if (modifyChk != null)
                {
                    modifyChk.setText(originalLabels[1] + (hasText ? " ("+ text +")" : ""));
                    modifyChk.setEnabled(!hasText && !isReadOnly);
                }
                break;
                
            case PermissionSettings.CAN_ADD :
                if (addChk != null) 
                {
                    addChk.setText(originalLabels[2] + (hasText ? " ("+ text +")" : ""));
                    addChk.setEnabled(!hasText && !isReadOnly);
                }
                
                break;
                
            case PermissionSettings.CAN_DELETE :
                if (delChk != null)
                {
                    delChk.setText(originalLabels[3] + (hasText ? " ("+ text +")" : ""));
                    delChk.setEnabled(!hasText && !isReadOnly);
                }
                
                break;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener(ChangeListener changeListener)
    {
        if (changeListener != null)
        {
            if (viewChk != null)   viewChk.addChangeListener(changeListener);
            if (addChk != null)    addChk.addChangeListener(changeListener);
            if (modifyChk != null) modifyChk.addChangeListener(changeListener);
            if (delChk != null)    delChk.addChangeListener(changeListener);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener(ChangeListener changeListener)
    {
        if (changeListener != null)
        {
            if (viewChk != null) viewChk.removeChangeListener(changeListener);
            if (addChk != null) addChk.removeChangeListener(changeListener);
            if (modifyChk != null) modifyChk.removeChangeListener(changeListener);
            if (delChk != null) delChk.removeChangeListener(changeListener);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#getUIComponent()
     */
    @Override
    public Component getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#getPermissions()
     */
    @Override
    public List<PermissionIFace> getPermissions()
    {
        ArrayList<PermissionIFace> list = new ArrayList<PermissionIFace>(1);
        permissions.clear();
        
        if (addChk != null) permissions.setCanAdd(addChk.isSelected());
        if (delChk != null) permissions.setCanDelete(delChk.isSelected());
        if (modifyChk != null) permissions.setCanModify(modifyChk.isSelected());
        if (viewChk != null) permissions.setCanView(viewChk.isSelected());
        
        list.add(permissions);
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#setPermissions(edu.ku.brc.af.auth.PermissionSettings)
     */
    @Override
    public void setPermissions(final List<PermissionIFace> permissionsArg)
    {
        this.permissions = permissionsArg.get(0);
        
        if (viewChk != null)   viewChk.setSelected(permissions.canView());
        if (addChk != null)    addChk.setSelected(permissions.canAdd());
        if (modifyChk != null) modifyChk.setSelected(permissions.canModify());
        if (delChk != null)    delChk.setSelected(permissions.canDelete());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#hasChanged()
     */
    @Override
    public boolean hasChanged()
    {
        return hasChanged;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionEditorIFace#setChanged(boolean)
     */
    @Override
    public void setChanged(boolean changed)
    {
        hasChanged = changed;
    }
    
}

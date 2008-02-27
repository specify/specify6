/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 27, 2007
 *
 */
public class AddRemoveEditPanel extends JPanel
{
    protected JButton addBtn;
    protected JButton delBtn;
    protected JButton editBtn;
    
    public AddRemoveEditPanel(final ActionListener addAL,
                              final ActionListener delAL,
                              final ActionListener editAL)
    {
        createUI(addAL, delAL, editAL, "", "", "");
    }
    
    public AddRemoveEditPanel(final ActionListener addAL,
                              final ActionListener delAL,
                              final ActionListener editAL,
                              final String addTTKey,
                              final String delTTKey,
                              final String editTTKey)
    {
        createUI(addAL, delAL, editAL, addTTKey, delTTKey, editTTKey);
    }
    
    protected void createUI(final ActionListener addAL,
                            final ActionListener delAL,
                            final ActionListener editAL,
                            final String addTTKey,
                            final String delTTKey,
                            final String editTTKey)
    {
        if (editAL != null)
        {
            editBtn = UIHelper.createIconBtn("EditIcon", addTTKey, editAL);
        }
        
        if (delAL != null)
        {
            delBtn = UIHelper.createIconBtn("MinusSign", delTTKey, delAL);
        }
        
        if (addAL != null)
        {
            addBtn = UIHelper.createIconBtn("PlusSign", editTTKey, addAL);
        }
        
        int numBtns = (addAL != null ? 1 : 0) + (delAL != null ? 1 : 0) + (addAL != null ? 1 : 0);
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder btnPb = new PanelBuilder(new FormLayout("f:p:g," + UIHelper.createDuplicateJGoodiesDef("P", "2px", numBtns), "p"), this);
        int x = 2;
        if (editAL != null)
        {
            btnPb.add(editBtn, cc.xy(x,1));
            x += 2;
        }
        if (delAL != null)
        {
            btnPb.add(delBtn, cc.xy(x,1));
            x += 2;
        }
        if (addAL != null)
        {
            btnPb.add(addBtn, cc.xy(x,1));
            x += 2;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        if (editBtn != null)
        {
            editBtn.setEnabled(enabled);
        }
        
        if (delBtn != null)
        {
            delBtn.setEnabled(enabled);
        }
        
        if (addBtn != null)
        {
            addBtn.setEnabled(enabled);
        }
    }
    
    /**
     * @return the addBtn
     */
    public JButton getAddBtn()
    {
        return addBtn;
    }

    /**
     * @return the delBtn
     */
    public JButton getDelBtn()
    {
        return delBtn;
    }

    /**
     * @return the editBtn
     */
    public JButton getEditBtn()
    {
        return editBtn;
    }

}

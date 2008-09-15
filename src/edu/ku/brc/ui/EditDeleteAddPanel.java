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
 * Panel with Edit, Delete and Add buttons (optionally).
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 27, 2007
 *
 */
public class EditDeleteAddPanel extends JPanel
{
    protected JButton addBtn;
    protected JButton delBtn;
    protected JButton editBtn;
    
    /**
     * 
     */
    protected EditDeleteAddPanel()
    {
        
    }
    
    /**
     * @param editAL
     * @param delAL
     * @param addAL
     */
    public EditDeleteAddPanel(final ActionListener editAL,
                              final ActionListener delAL,
                              final ActionListener addAL)
    {
        createUI(editAL, delAL, addAL, "", "", "");
    }
    
    /**
     * @param editAL
     * @param delAL
     * @param addAL
     * @param editTTKey
     * @param delTTKey
     * @param addTTKey
     */
    public EditDeleteAddPanel(final ActionListener editAL,
                              final ActionListener delAL,
                              final ActionListener addAL,
                              final String editTTKey,
                              final String delTTKey,
                              final String addTTKey)
    {
        createUI(editAL, delAL, addAL, editTTKey, delTTKey, addTTKey);
    }
    
    /**
     * @param delAL
     * @param editAL
     * @param addAL
     * @param delTTKey
     * @param editTTKey
     * @param addTTKey
     * @return
     */
    protected PanelBuilder createUI(final ActionListener editAL,
                                    final ActionListener delAL,
                                    final ActionListener addAL,
                                    final String editTTKey,
                                    final String delTTKey,
                                    final String addTTKey)
    {
        
        if (editAL != null)
        {
            editBtn = UIHelper.createIconBtn("EditIcon", editTTKey, editAL);
        }
        
        if (delAL != null)
        {
            delBtn = UIHelper.createIconBtn("MinusSign", delTTKey, delAL);
        }
        
        if (addAL != null)
        {
            addBtn = UIHelper.createIconBtn("PlusSign", addTTKey, addAL);
        }
        
        int numBtns = (addAL != null ? 1 : 0) + (delAL != null ? 1 : 0) + (editAL != null ? 1 : 0);
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder btnPb = new PanelBuilder(createFormLayout(numBtns), this);
        int x = getStartingIndex();
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
        return btnPb;
    }
    
    /**
     * @param numBtns the number of btns in the row
     * @return the FormLayout to use to build the button row
     */
    protected FormLayout createFormLayout(final int numBtns)
    {
        return new FormLayout("f:p:g," + UIHelper.createDuplicateJGoodiesDef("P", "2px", numBtns), "p");
    }
    
    /**
     * @return  the start of index of where the btuttons are layed out
     */
    protected int getStartingIndex()
    {
        return 2;
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

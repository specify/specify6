/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.forms.formatters;

import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;

/**
 * Creates a panel with Default, Add Remove, and Edit buttons.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 6, 2008
 *
 */
public class DefEditDeleteAddPanel extends EditDeleteAddPanel
{
    protected JButton defBtn = null;
    
    /**
     * @param addAL
     * @param delAL
     * @param editAL
     * @param defAL
     */
    public DefEditDeleteAddPanel(final ActionListener addAL,
                                 final ActionListener delAL,
                                 final ActionListener editAL,
                                 final ActionListener defAL)
    {
        
        this(addAL, delAL, editAL, defAL, "", "", "", "");

    }
    
    public DefEditDeleteAddPanel(final ActionListener addAL,
                                 final ActionListener delAL,
                                 final ActionListener editAL,
                                 final ActionListener defAL,
                                 final String addTTKey,
                                 final String delTTKey,
                                 final String editTTKey,
                                 final String defTTKey)
    {
        createUI(addAL, delAL, editAL, defAL, addTTKey, delTTKey, editTTKey, defTTKey);
    }

    /**
     * @param addAL
     * @param delAL
     * @param editAL
     * @param defAL
     * @param addTTKey
     * @param delTTKey
     * @param editTTKey
     * @param defTTKey
     * @return
     */
    protected PanelBuilder createUI(final ActionListener addAL,
                                    final ActionListener delAL,
                                    final ActionListener editAL,
                                    final ActionListener defAL,
                                    final String addTTKey,
                                    final String delTTKey,
                                    final String editTTKey,
                                    final String defTTKey)
    {
        if (defAL != null)
        {
            defBtn = UIHelper.createIconBtn("Checkmark", IconManager.IconSize.Std16, defTTKey, defAL);
        }

        PanelBuilder    pb = super.createUI(addAL, delAL, editAL, addTTKey, delTTKey, editTTKey);
        CellConstraints cc = new CellConstraints();
        pb.add(defBtn, cc.xy(2, 1));
        return pb;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.AddRemoveEditPanel#createFormLayout(int)
     */
    @Override
    protected FormLayout createFormLayout(int numBtns)
    {
        return super.createFormLayout(numBtns + (defBtn != null ? 1 : 0));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.AddRemoveEditPanel#getStartingIndex()
     */
    @Override
    protected int getStartingIndex()
    {
        return super.getStartingIndex() + (defBtn != null ? 2 : 0);
    }

    /**
     * @return the default button
     */
    public JButton getDefBtn()
    {
        return defBtn;
    }

}

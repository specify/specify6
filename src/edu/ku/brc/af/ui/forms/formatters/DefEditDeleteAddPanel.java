/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms.formatters;

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
    public DefEditDeleteAddPanel(final ActionListener defAL,
                                 final ActionListener editAL,
                                 final ActionListener delAL,
                                 final ActionListener addAL)
    {
        
        this(defAL, editAL, delAL, addAL, "", "", "", "");

    }
    
    public DefEditDeleteAddPanel(final ActionListener defAL,
                                 final ActionListener editAL,
                                 final ActionListener delAL,
                                 final ActionListener addAL,
                                 final String defTTKey,
                                 final String editTTKey,
                                 final String delTTKey,
                                 final String addTTKey)
    {
        createUI(defAL, editAL, delAL, addAL, defTTKey, editTTKey, delTTKey, addTTKey);
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
    protected PanelBuilder createUI(final ActionListener defAL,
                                    final ActionListener editAL,
                                    final ActionListener delAL,
                                    final ActionListener addAL,
                                    final String defTTKey,
                                    final String editTTKey,
                                    final String delTTKey,
                                    final String addTTKey)
    {
        if (defAL != null)
        {
            defBtn = UIHelper.createIconBtn("Checkmark", IconManager.IconSize.Std16, defTTKey, defAL);
        }

        PanelBuilder    pb = super.createUI(editAL, delAL, addAL, defTTKey, editTTKey, addTTKey);
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

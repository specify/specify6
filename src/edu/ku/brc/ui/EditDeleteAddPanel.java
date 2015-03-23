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
        
        return doBuildLayout();
    }
    
    /**
     * @return
     */
    protected PanelBuilder doBuildLayout()
    {
        int numBtns = (addBtn != null ? 1 : 0) + (delBtn != null ? 1 : 0) + (editBtn != null ? 1 : 0);
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder btnPb = new PanelBuilder(createFormLayout(numBtns), this);
        int x = getStartingIndex();
        if (editBtn != null)
        {
            btnPb.add(editBtn, cc.xy(x,1));
            x += 2;
        }
        if (delBtn != null)
        {
            btnPb.add(delBtn, cc.xy(x,1));
            x += 2;
        }
        if (addBtn != null)
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

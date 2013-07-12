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
package edu.ku.brc.ui;

import java.awt.event.ActionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Oct 3, 2010
 *
 */
public class EditDeleteAddVertPanel extends EditDeleteAddPanel
{

    /**
     * @param editAL
     * @param delAL
     * @param addAL
     */
    public EditDeleteAddVertPanel(final ActionListener editAL, 
                                  final ActionListener delAL, 
                                  final ActionListener addAL)
    {
        super(editAL, delAL, addAL);
    }

    /**
     * @param editAL
     * @param delAL
     * @param addAL
     * @param editTTKey
     * @param delTTKey
     * @param addTTKey
     */
    public EditDeleteAddVertPanel(final ActionListener editAL, 
                                  final ActionListener delAL,
                                  final ActionListener addAL, 
                                  final String editTTKey, 
                                  final String delTTKey, 
                                  final String addTTKey)
    {
        super(editAL, delAL, addAL, editTTKey, delTTKey, addTTKey);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.EditDeleteAddPanel#createFormLayout(int)
     */
    @Override
    protected FormLayout createFormLayout(int numBtns)
    {
        return new FormLayout("c:p:g", UIHelper.createDuplicateJGoodiesDef("P", "2px", numBtns) + ",f:p:g");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.EditDeleteAddPanel#doBuildLayout()
     */
    @Override
    protected PanelBuilder doBuildLayout()
    {
        int numBtns = (addBtn != null ? 1 : 0) + (delBtn != null ? 1 : 0) + (editBtn != null ? 1 : 0);
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder btnPb = new PanelBuilder(createFormLayout(numBtns), this);
        int y = getStartingIndex();
        if (editBtn != null)
        {
            btnPb.add(editBtn, cc.xy(1,y));
            y += 2;
        }
        if (delBtn != null)
        {
            btnPb.add(delBtn, cc.xy(1,y));
            y += 2;
        }
        if (addBtn != null)
        {
            btnPb.add(addBtn, cc.xy(1,y));
            y += 2;
        }
        return btnPb; 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.EditDeleteAddPanel#getStartingIndex()
     */
    @Override
    protected int getStartingIndex()
    {
        return 1;
    }

}

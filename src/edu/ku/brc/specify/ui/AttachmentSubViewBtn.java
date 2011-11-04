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
package edu.ku.brc.specify.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.SubViewBtn;
import edu.ku.brc.af.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 22, 2009
 *
 */
public class AttachmentSubViewBtn extends SubViewBtn
{

    /**
     * @param mvParent
     * @param subviewDef
     * @param view
     * @param dataType
     * @param options
     * @param props
     * @param classToCreate
     * @param mode
     */
    public AttachmentSubViewBtn(MultiView mvParent, FormCellSubViewIFace subviewDef,
            ViewIFace view, DATA_TYPE dataType, int options, Properties props,
            Class<?> classToCreate, CreationMode mode)
    {
        super(mvParent, subviewDef, view, dataType, options, props, classToCreate, mode);
        
        for (ActionListener al: subViewBtn.getActionListeners())
        {
            subViewBtn.removeActionListener(al);
        }
        
        subViewBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (AttachmentUtils.isAvailable())
                {
                    showForm();
                } else
                {
                    UIRegistry.showLocalizedError(UIRegistry.getResourceString("AttachmentUtils." + (AttachmentUtils.isConfigForPath() ? "LOC_BAD" : "URL_BAD")));
                }
            }
        });
    }
}

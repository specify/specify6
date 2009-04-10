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
package edu.ku.brc.specify.config;

import java.util.Properties;

import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.SubViewBtn;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.SubViewBtn.DATA_TYPE;
import edu.ku.brc.af.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.specify.ui.AttachmentSubViewBtn;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 22, 2009
 *
 */
public class SpecifyViewFactory extends ViewFactory
{

    /**
     * 
     */
    public SpecifyViewFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ViewFactory#createSubViewBtn(edu.ku.brc.af.ui.forms.MultiView, edu.ku.brc.af.ui.forms.persist.FormCellSubViewIFace, edu.ku.brc.af.ui.forms.persist.ViewIFace, edu.ku.brc.af.ui.forms.SubViewBtn.DATA_TYPE, int, java.util.Properties, java.lang.Class, edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode)
     */
    @Override
    protected SubViewBtn createSubViewBtn(final MultiView mvParent,
                                          final FormCellSubViewIFace subviewDef,
                                          final ViewIFace view,
                                          final DATA_TYPE dataType,
                                          final int options,
                                          final Properties props,
                                          final Class<?> classToCreate,
                                          final CreationMode mode)
    {
        return new AttachmentSubViewBtn(mvParent, subviewDef, view, dataType, options, props, classToCreate, mode);
    }

}

/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.specify.ui;

import java.util.Properties;

import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.SubViewBtn;
import edu.ku.brc.af.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode;
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
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.SubViewBtn#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled && AttachmentUtils.isAvailable());
    }
}

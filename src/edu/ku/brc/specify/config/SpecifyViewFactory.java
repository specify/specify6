/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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

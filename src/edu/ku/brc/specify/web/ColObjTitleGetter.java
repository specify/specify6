/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.web;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 24, 2008
 *
 */
public class ColObjTitleGetter implements TitleGetterIFace
{

    public ColObjTitleGetter()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.web.TitleGetterIFace#getTitle(edu.ku.brc.ui.forms.FormDataObjIFace)
     */
    public String getTitle(final FormDataObjIFace dataObj)
    {
        if (dataObj instanceof CollectionObject)
        {
            CollectionObject colObj = (CollectionObject)dataObj;
            for (Determination det : colObj.getDeterminations())
            {
                if (det.getStatus().getType().equals(DeterminationStatus.CURRENT))
                {
                    return colObj.getCatalogNumber() + " - " + det.getTaxon().getFullName();
                }
            }
        }
        return null;
    }

   

}

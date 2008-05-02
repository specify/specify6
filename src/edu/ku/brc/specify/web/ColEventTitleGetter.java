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

import java.text.SimpleDateFormat;

import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2008
 *
 */
public class ColEventTitleGetter implements TitleGetterIFace
{
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.web.TitleGetterIFace#getTitle(edu.ku.brc.ui.forms.FormDataObjIFace)
     */
    public String getTitle(FormDataObjIFace dataObj)
    {
        if (dataObj instanceof CollectingEvent)
        {
            CollectingEvent colEvent = (CollectingEvent)dataObj;
            if (colEvent != null)
            {
                String date = sdf.format(colEvent.getStartDate());
                Locality loc = colEvent.getLocality();
                if (loc != null)
                {
                    return date + " - " +loc.getLocalityName();
                }
                return date;
            }
        }
        return null;
    }

}

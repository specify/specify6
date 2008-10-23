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

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;

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

    protected DBFieldInfo field;
    
    public ColObjTitleGetter()
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
        field = ti.getFieldByColumnName("CatalogNumber");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.web.TitleGetterIFace#getTitle(edu.ku.brc.ui.forms.FormDataObjIFace)
     */
    public String getTitle(final FormDataObjIFace dataObj)
    {
        if (dataObj instanceof CollectionObject)
        {
            CollectionObject colObj = (CollectionObject)dataObj;
            if (colObj != null)
            {
                //System.out.print("Det: "+colObj.getDeterminations());
                for (Determination det : colObj.getDeterminations())
                {
                    if (DeterminationStatus.isCurrentType(det.getStatus().getType()))
                    {
                        UIFieldFormatterIFace fmt = field.getFormatter();
                        return fmt.formatToUI(colObj.getCatalogNumber()) + " - " + det.getTaxon().getFullName();
                    }
                }
            }
        }
        return null;
    }

   

}

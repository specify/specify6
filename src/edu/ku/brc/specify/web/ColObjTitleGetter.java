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
package edu.ku.brc.specify.web;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;

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
                    if (det.isCurrentDet())
                    {
                        UIFieldFormatterIFace fmt = field.getFormatter();
                        return fmt.formatToUI(colObj.getCatalogNumber()) + " - " + det.getPreferredTaxon().getFullName();
                    }
                }
            }
        }
        return null;
    }

   

}

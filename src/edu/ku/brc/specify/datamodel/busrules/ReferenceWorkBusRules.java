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
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author Administrator
 *
 *Added in case of need to manage ContainingReferenceWork relationship.
 */
public class ReferenceWorkBusRules extends BaseBusRules
{

	/**
	 * 
	 */
	public ReferenceWorkBusRules()
	{
	    super(ReferenceWork.class);
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(Object dataObj,
                           DataProviderSessionIFace session,
                           BusinessRulesOkDeleteIFace deletable)
    {
        ReferenceWork rw = (ReferenceWork)dataObj;
        
        if (rw.getId() != null)
        {
            String sql = "SELECT count(*) FROM referencework WHERE JournalID is NULL AND ReferenceWorkID = " + rw.getId();
            Integer cnt = BasicSQLUtils.getCount(sql);
            if (cnt == 0)
            {
                UIRegistry.showLocalizedError("RW_NO_DEL");
                return;
            }
        }
        super.okToDelete(dataObj, session, deletable);
    }
	

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        setLSID((FormDataObjIFace)dataObj);

        return super.afterSaveCommit(dataObj, session);
    }
}

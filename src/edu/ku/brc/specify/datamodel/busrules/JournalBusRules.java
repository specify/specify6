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
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 4, 2009
 *
 */
public class JournalBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public JournalBusRules()
    {
        super(Journal.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(Object dataObj,
                           DataProviderSessionIFace session,
                           BusinessRulesOkDeleteIFace deletable)
    {
        Journal journal = (Journal)dataObj;
        
        if (journal.getId() != null)
        {
            String sql = "SELECT count(*) FROM referencework r WHERE JournalID = " + journal.getId();
            Integer cnt = BasicSQLUtils.getCount(sql);
            if (cnt != null && cnt > 0)
            {
                UIRegistry.showLocalizedError("JN_NO_DEL");
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
        return super.afterSaveCommit(dataObj, session);
    }
}

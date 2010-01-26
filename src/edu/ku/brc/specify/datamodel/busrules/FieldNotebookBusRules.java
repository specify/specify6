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

import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.FieldNotebook;
import edu.ku.brc.specify.datamodel.FieldNotebookAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPage;
import edu.ku.brc.specify.datamodel.FieldNotebookPageAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSet;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSetAttachment;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 11, 2008
 *
 */
public class FieldNotebookBusRules extends AttachmentOwnerBaseBusRules
{
    public FieldNotebookBusRules()
    {
        super(FieldNotebook.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        
        if (viewable != null && viewable.getMVParent() != null && viewable.getMVParent().getTopLevel() != null)
        {
            MultiView topMV = viewable.getMVParent().getTopLevel();
            
            FieldNotebook fieldNotebook = (FieldNotebook)dataObj;
            
            for (FieldNotebookAttachment fnba : fieldNotebook.getAttachmentReferences())
            {
                topMV.addBusRuleItem(fnba);
            }
            
            for (FieldNotebookPageSet pageSet : fieldNotebook.getPageSets())
            {
                topMV.addBusRuleItem(pageSet);
                
                for (FieldNotebookPageSetAttachment psa : pageSet.getAttachmentReferences())
                {
                    topMV.addBusRuleItem(psa);
                }
                
                for (FieldNotebookPage page : pageSet.getPages())
                {
                    topMV.addBusRuleItem(page);
                    
                    for (FieldNotebookPageAttachment pa : page.getAttachmentReferences())
                    {
                        topMV.addBusRuleItem(pa);
                    }
                }
            }
        }
    }

}

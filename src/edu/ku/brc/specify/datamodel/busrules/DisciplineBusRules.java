/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.specify.utilapps.DataBuilder.createTaxonTreeDef;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.ui.forms.BaseBusRules;
import edu.ku.brc.ui.forms.FormDataObjIFace;

public class DisciplineBusRules extends BaseBusRules
{   
    //private final Logger         log      = Logger.getLogger(DisciplineBusRules.class);
    
    /**
     * 
     */
    public DisciplineBusRules()
    {
        super(Discipline.class);    
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        if (!okToDelete("collection", "DisciplineID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        
        if (!okToDelete("attributedef", "DisciplineID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        
        if (!okToDelete("appresourcedefault", "DisciplineID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj,session);
        
        Discipline ct = (Discipline)dataObj;
        if (ct.getTaxonTreeDef() == null)
        {
            TaxonTreeDef taxonTreeDef = createTaxonTreeDef("Sample Taxon Tree Def");
            ct.setTaxonTreeDef(taxonTreeDef);
        }
        
        if (ct.getSpAppResourceDirs() == null)
        {
            //ct.setTaxonTreeDef(taxonTreeDef);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Discipline)
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(Discipline.getClassTableId());
            return getLocalizedMessage("DISCIPLINE_DELETED", ti.getTitle());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }
}

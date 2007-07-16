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

import static edu.ku.brc.specify.tests.DataBuilder.createTaxonTreeDef;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.ui.forms.FormDataObjIFace;

public class CollectionObjDefBusRules extends BaseBusRules
{   
    //private final Logger         log      = Logger.getLogger(CollectionObjDefBusRules.class);
    
    /**
     * 
     */
    public CollectionObjDefBusRules()
    {
        super(CollectionObjDef.class);    
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        /*
        CollectionObjDef cod = (CollectionObjDef)dataObj;
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            // load workbenches so they aren't lazy
            // this is needed later on when the new WB is added to the template 
            session.attach(cod);

            if (cod.getLocalities().size() > 0)
            {
                return false;
            }
            if (cod.getAttributeDefs().size() > 0)
            {
                return false;
            }
            if (cod.getCollection().size() > 0)
            {
                return false;
            }
            if (cod.getAppResourceDefaults().size() > 0)
            {
                return false;
            }
            return true;
            
        } catch (Exception ex)
        {
            log.error(ex);
            
        } finally 
        {
            session.close();
        }
*/
        
        if (!okToDelete("colobjdef_locality", "CollectionObjDefID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        
        if (!okToDelete("collection", "CollectionObjDefID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        
        if (!okToDelete("attributedef", "CollectionObjDefID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        
        if (!okToDelete("appresourcedefault", "CollectionObjDefID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj)
    {
        super.beforeSave(dataObj);
        
        CollectionObjDef cod = (CollectionObjDef)dataObj;
        if (cod.getSpecifyUser() == null)
        {
            cod.setSpecifyUser(SpecifyUser.getCurrentUser());
        }
        
        if (cod.getTaxonTreeDef() == null)
        {
            TaxonTreeDef taxonTreeDef = createTaxonTreeDef("Sample Taxon Tree Def");
            cod.setTaxonTreeDef(taxonTreeDef);
        }
        
        if (cod.getAppResourceDefaults() == null)
        {
            //cod.setTaxonTreeDef(taxonTreeDef);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof CollectionObjDef)
        {
            return getLocalizedMessage("COLLECTIONOBJDEF_DELETED", ((CollectionObjDef)dataObj).getName());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }
}

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

import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.ui.forms.FormDataObjIFace;

public class CollectionObjDefBusRules extends SimpleBusRules
{   
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
        if (!okToDelete("colobjdef_locality", "CollectionObjDefID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        
        if (!okToDelete("catseries_colobjdef", "CollectionObjDefID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        
        if (!okToDelete("attributedef", "CollectionObjDefID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        
        if (!okToDelete("appresourcefefault", "CollectionObjDefID", ((FormDataObjIFace)dataObj).getId()))
        {
            return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof DataType)
        {
            return "Collection Object Definition "+((CollectionObjDef)dataObj).getName() + " was deleted."; // I18N
        }
        return null;
    }
}

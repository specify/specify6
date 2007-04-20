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
/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Permit;

/**
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class PermitBusRules extends BaseBusRules
{  
    /**
     * Constructor.
     */
    public PermitBusRules()
    {
        super(Permit.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(Object dataObj)
    {
        errorList.clear();
        
        if (!(dataObj instanceof Permit))
        {
            return STATUS.Error;
        }
        
        Permit permit = (Permit)dataObj;
        
        String permitNum = permit.getPermitNumber();
        if (StringUtils.isNotEmpty(permitNum))
        {
            // Start by checking to see if the permit number has changed
            boolean checkPermitNumberForDuplicates = true;
            Long id = permit.getPermitId();
            if (id != null)
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                List<?>                  permits = session.getDataList(Permit.class, "permitId", id);
                if (permits.size() == 1)
                {
                    Permit oldPermit = (Permit)permits.get(0);
                    String oldPermitNumber = oldPermit.getPermitNumber();
                    if (oldPermitNumber.equals(permit.getPermitNumber()))
                    {
                        checkPermitNumberForDuplicates = false;
                    }
                }
            }
            
            // If the Id is null then it is a new permit, if not then we are editting the permit
            //
            // If the permit has not changed then we shouldn't check for duplicates
            if (checkPermitNumberForDuplicates)
            {
                DataProviderSessionIFace session       = DataProviderFactory.getInstance().createSession();
                List<?>                  permitNumbers = session.getDataList(Permit.class, "permitNumber", permitNum);
                if (permitNumbers.size() > 0)
                {
                    errorList.add(getLocalizedMessage("PERMIT_NUM_IN_USE", permitNum));
                } else
                {
                    return STATUS.OK;
                }
                
            } else
            {
                return STATUS.OK;
            }
            
        } else
        {
            errorList.add(getLocalizedMessage("PERMIT_NUM_MISSING"));
        }

        return STATUS.Error;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj instanceof Permit)
        {
            Permit permit = (Permit)dataObj;
            if (permit.getPermitId() != null && permit.getAccessionAuthorizations().size() == 0)
            {
                return true;
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Permit)
        {
            return getLocalizedMessage("PERMIT_DELETED", ((Permit)dataObj).getPermitNumber());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }
}

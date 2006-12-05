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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.forms.BusinessRulesDataItem;
import edu.ku.brc.ui.forms.BusinessRulesIFace;
import edu.ku.brc.ui.forms.DraggableRecordIdentifier;

/**
 * Business rules for validating a Loan.
 * 
 * @code_status Complete
 *
 * @author rods
 *
 */
public class LoanBusRules implements BusinessRulesIFace
{
    //private static final Logger  log      = Logger.getLogger(LoanBusRule.class);
    
    private List<String> errorList = new Vector<String>();

   
    /**
     * Constructor.
     */
    public LoanBusRules()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getWarningsAndErrors()
     */
    public List<String> getWarningsAndErrors()
    {
        return errorList;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(Object dataObj)
    {
        errorList.clear();
        
        if (dataObj == null || !(dataObj instanceof Loan))
        {
            return STATUS.Error;
        }
        
        /*
        Loan loan = (Loan)dataObj;
        
        for (LoanAgents loanAgent : loan.getLoanAgents())
        {
            Agent agent = loanAgent.getAgent();
            if (agent != null)
            {
                Set<Address> addr = agent.getAddresses();
                if (addr.size() == 0)
                {
                    errorList.add("The select agent you loaning to,\nneeds to have at least one address."); // Thsi shouldn't eveer happen
                    return STATUS.Error;
                }
                
            } else
            {
                errorList.add("Loan Agent is missing an Agent"); // Thsi shouldn't eveer happen
                return STATUS.Error;
            }     
        }
        */
        
        return STATUS.OK;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getStandAloneDataItems(java.lang.Object)
     */
    public List<BusinessRulesDataItem> getStandAloneDataItems(Object dataObj)
    {
        return new ArrayList<BusinessRulesDataItem>();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#saveStandAloneData(java.lang.Object, java.util.List)
     */
    public void saveStandAloneData(final Object dataObj, final List<BusinessRulesDataItem> list)
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public boolean okToDelete(Object dataObj)
    {
        return true;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Accession)
        {
            return "Loan "+((Loan)dataObj).getLoanNumber() + " was deleted."; // I18N
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#setObjectIdentity(java.lang.Object, edu.ku.brc.ui.DraggableIcon)
     */
    public void setObjectIdentity(final Object dataObj, final DraggableRecordIdentifier draggableIcon)
    {
        if (dataObj == null)
        {
            draggableIcon.setLabel("");
        }
        
        if (dataObj instanceof Loan)
        {
            Loan loan = (Loan)dataObj;
            
            draggableIcon.setLabel(loan.getLoanNumber());
            
            Object data = draggableIcon.getData();
            if (data == null)
            {
                RecordSet rs = new RecordSet();
                rs.initialize();
                rs.addItem(loan.getLoanId());
                data = rs;
                draggableIcon.setData(data);
                
            } else if (data instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)data;
                rs.getItems().clear();
                rs.addItem(loan.getLoanId());
            }
        }
     }
}

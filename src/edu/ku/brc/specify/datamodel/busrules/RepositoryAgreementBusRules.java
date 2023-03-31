/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.util.Hashtable;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.ui.UIRegistry;

/**
 * Business Rules for validating a RepositoryAgreement.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class RepositoryAgreementBusRules extends AttachmentOwnerBaseBusRules
{
    //private static final Logger  log      = Logger.getLogger(RepositoryAgreementBusRules.class);
   
    /**
     * Constructor.
     */
    public RepositoryAgreementBusRules()
    {
        super(RepositoryAgreement.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        ((RepositoryAgreement)newDataObj).setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#shouldCreateSubViewData(java.lang.String)
     */
    @Override
    public boolean shouldCreateSubViewData(String fieldName)
    {
        //return fieldName.equals("repositoryAgreementAgents") ||
        //       fieldName.equals("repositoryAgreementAuthorizations");
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (!(viewable instanceof FormViewObj))
        {
            return;
        }
        
        FormViewObj fvo = (FormViewObj)viewable;
        if (fvo.isFieldAutoNumberedByName("repositoryAgreementNumber"))
        {
            
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof RepositoryAgreement))
        {
            return STATUS.Error;
        }
        
        RepositoryAgreement repositoryAgreement = (RepositoryAgreement)dataObj;
        
        // Check for AcccessionAgent and their Roles (for duplicates)
        Hashtable<String, Boolean> agentRoleHash = new Hashtable<String, Boolean>();
        for (AccessionAgent aa : repositoryAgreement.getRepositoryAgreementAgents())
        {
            Agent agent = aa.getAgent();
            if (agent != null)
            {
                String key = agent.getId() + " _ " + aa.getRole();
                if (agentRoleHash.get(key) == null)
                {
                    agentRoleHash.put(key, true);
                } else
                {
                    reasonList.add(UIRegistry.getLocalizedMessage("REPAGR_DUP_AGENTROLE", agent.toString(), aa.getRole()));
                    return STATUS.Error;
                }
            }
        }
        
        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("repositoryAgreementNumber", 
                (FormDataObjIFace)dataObj, 
                RepositoryAgreement.class, 
                "repositoryAgreementId");
        
        return duplicateNumberStatus;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public boolean okToEnableDelete(Object dataObj)
    {
        reasonList.clear();
        
        if (dataObj != null)
        {
            if (dataObj instanceof RepositoryAgreement)
            {
                RepositoryAgreement repositoryAgreement = (RepositoryAgreement)dataObj;
                if (repositoryAgreement.getRepositoryAgreementId() != null)
                {
                    
                    // Doing "repositoryAgreement.getCollectionObjects().size() == 0"
                    // potentially is REALLY slow if a lot of CollectionObjects are attached 
                    // to an RepositoryAgreements
                    // So instead we will use straight SQL
                    int count = BasicSQLUtils.getCount("select count(*) from accession where RepositoryAgreementID = "+repositoryAgreement.getRepositoryAgreementId());
                    return count == 0;
                    
                } else
                {
                    return true;
                }
            }
        } else
        {
            return false;
        }
        throw new RuntimeException("Data Obj is not an RepositoryAgreement ["+dataObj.getClass().getSimpleName()+"]");
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof RepositoryAgreement)
        {
            return getLocalizedMessage("REPAGR_DELETED", ((RepositoryAgreement)dataObj).getRepositoryAgreementNumber());
        }
        // else
        return super.getDeleteMsg(dataObj);
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
        
        if (dataObj instanceof RepositoryAgreement)
        {
            RepositoryAgreement repositoryAgreement = (RepositoryAgreement)dataObj;
            
            draggableIcon.setLabel(repositoryAgreement.getRepositoryAgreementNumber());
            
            Object data = draggableIcon.getData();
            if (data == null)
            {
                RecordSet rs = new RecordSet();
                rs.initialize();
                rs.addItem(repositoryAgreement.getRepositoryAgreementId());
                data = rs;
                draggableIcon.setData(data);
                
            } else if (data instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)data;
                rs.clearItems();
                rs.addItem(repositoryAgreement.getRepositoryAgreementId());
            }
        }
     }
}

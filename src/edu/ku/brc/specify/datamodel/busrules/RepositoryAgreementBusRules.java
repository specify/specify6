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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.Viewable;

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
        
        Collection collection = Collection.getCurrentCollection();
        if (collection != null)
        {
            Division division = collection.getDiscipline().getDivision();
            if (division != null)
            {
                RepositoryAgreement repositoryAgreement = (RepositoryAgreement) newDataObj;
                repositoryAgreement.setDivision(division);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#shouldCreateSubViewData(java.lang.String)
     */
    @Override
    public boolean shouldCreateSubViewData(String fieldName)
    {
        return fieldName.equals("repositoryAgreementAgents") ||
               fieldName.equals("repositoryAgreementAuthorizations");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void afterFillForm(final Object dataObj, final Viewable viewableArg)
    {
        super.afterFillForm(dataObj, viewable);
        
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
        
        // Let's check RepositoryAgreement for duplicates 
        String repositoryAgreementNumber = repositoryAgreement.getRepositoryAgreementNumber();
        if (StringUtils.isNotEmpty(repositoryAgreementNumber))
        {
            // Start by checking to see if the permit number has changed
            boolean checkRepositoryAgreementNumberForDuplicates = true;
            Integer id = repositoryAgreement.getRepositoryAgreementId();
            if (id != null)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    List<?> accessions = session.getDataList(RepositoryAgreement.class, "repositoryAgreementId", id);
                    if (accessions.size() == 1)
                    {
                        RepositoryAgreement oldRepositoryAgreement       = (RepositoryAgreement)accessions.get(0);
                        String    oldRepositoryAgreementNumber = oldRepositoryAgreement.getRepositoryAgreementNumber();
                        if (oldRepositoryAgreementNumber.equals(repositoryAgreement.getRepositoryAgreementNumber()))
                        {
                            checkRepositoryAgreementNumberForDuplicates = false;
                        }
                    }
                } catch (Exception ex)
                {
                    log.error(ex);
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            }
            
            // If the Id is null then it is a new permit, if not then we are editting the repositoryAgreement
            //
            // If the repositoryAgreement has not changed then we shouldn't check for duplicates
            if (checkRepositoryAgreementNumberForDuplicates)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    List <?> accessionNumbers        = session.getDataList(RepositoryAgreement.class, "repositoryAgreementNumber", repositoryAgreementNumber);
                    if (accessionNumbers.size() > 0)
                    {
                        reasonList.add(UIRegistry.getResourceString("REPAGR_IN_USE"));
                    } else
                    {
                        return STATUS.OK;
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
                
            } else
            {
                return STATUS.OK;
            }
            
        } else
        {
            reasonList.add(UIRegistry.getResourceString("REPAGR_NUM_MISSING"));
        }

        return STATUS.Error;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public boolean okToEnableDelete(Object dataObj)
    {
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
                    try
                    {
                        Statement stmt = DBConnection.getInstance().getConnection().createStatement();
                        ResultSet rs   = stmt.executeQuery("select count(*) from collectionobject where RepositoryAgreementID = "+repositoryAgreement.getRepositoryAgreementId());
                        if (rs.next())
                        {
                            return rs.getInt(1) == 0;
                        }
                        rs.close();
                        stmt.close();
                        
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        throw new RuntimeException(ex);
                    }
                } else
                {
                    return false;
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

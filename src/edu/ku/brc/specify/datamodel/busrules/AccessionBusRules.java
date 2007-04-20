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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.ui.forms.Viewable;

/**
 *Business rules for validating a Accession.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class AccessionBusRules extends BaseBusRules
{
    private static final Logger  log      = Logger.getLogger(AccessionBusRules.class);
   
    /**
     * Constructor.
     */
    public AccessionBusRules()
    {
        super(Accession.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#fillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    public void fillForm(Object dataObj, Viewable viewable)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(Object dataObj)
    {
        errorList.clear();
        
        if (!(dataObj instanceof Accession))
        {
            return STATUS.Error;
        }
        
        Accession accession       = (Accession)dataObj;
        String    accessionNumber = accession.getNumber();
        if (StringUtils.isNotEmpty(accessionNumber))
        {
            // Start by checking to see if the permit number has changed
            boolean checkAccessionNumberForDuplicates = true;
            Long id = accession.getAccessionId();
            if (id != null)
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                List<?> accessions = session.getDataList(Accession.class, "accessionId", id);
                if (accessions.size() == 1)
                {
                    Accession oldAccession       = (Accession)accessions.get(0);
                    String    oldAccessionNumber = oldAccession.getNumber();
                    if (oldAccessionNumber.equals(accession.getNumber()))
                    {
                        checkAccessionNumberForDuplicates = false;
                    }
                }
                session.close();
            }
            
            // If the Id is null then it is a new permit, if not then we are editting the accession
            //
            // If the accession has not changed then we shouldn't check for duplicates
            if (checkAccessionNumberForDuplicates)
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                List <?> accessionNumbers        = session.getDataList(Accession.class, "number", accessionNumber);
                if (accessionNumbers.size() > 0)
                {
                    errorList.add("ACCESSION_IN_USE");
                } else
                {
                    return STATUS.OK;
                }
                session.close();
                
            } else
            {
                return STATUS.OK;
            }
            
        } else
        {
            errorList.add("ACCESSION_NUM_MISSING");
        }

        return STATUS.Error;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj != null)
        {
            if (dataObj instanceof Accession)
            {
                Accession accession = (Accession)dataObj;
                if (accession.getAccessionId() != null)
                {
                    
                    // Doing "accession.getCollectionObjects().size() == 0"
                    // potentially is REALLY slow if a lot of CollectionObjects are attached 
                    // to an Accessions
                    // So instead we will use straight SQL
                    try
                    {
                        Statement stmt = DBConnection.getInstance().getConnection().createStatement();
                        ResultSet rs   = stmt.executeQuery("select count(*) from collectionobject where AccessionID = "+accession.getAccessionId());
                        if (rs.first())
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
        throw new RuntimeException("Data Obj is not an Accession ["+dataObj.getClass().getSimpleName()+"]");
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Accession)
        {
            return getLocalizedMessage("ACCESSION_DELETED", ((Accession)dataObj).getNumber());
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
        
        if (dataObj instanceof Accession)
        {
            Accession accession = (Accession)dataObj;
            
            draggableIcon.setLabel(accession.getNumber());
            
            Object data = draggableIcon.getData();
            if (data == null)
            {
                RecordSet rs = new RecordSet();
                rs.initialize();
                rs.addItem(accession.getAccessionId());
                data = rs;
                draggableIcon.setData(data);
                
            } else if (data instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)data;
                rs.getItems().clear();
                rs.addItem(accession.getAccessionId());
            }
        }
     }
}

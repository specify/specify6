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

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DeaccessionPreparation;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 24, 2007
 *
 */
public class CollectionObjectBusRules extends AttachmentOwnerBaseBusRules
{
    //private static final Logger  log = Logger.getLogger(CollectionObjectBusRules.class);
    
    /**
     * Constructor.
     */
    public CollectionObjectBusRules()
    {
        super(CollectionObject.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(final Object dataObj)
    {
        if (!(dataObj instanceof CollectionObject))
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * @param disciplineType
     * @return
     */
    protected PrepType getDefaultPrepType()
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(PrepType.getClassTableId());
        if (tableInfo != null)
        {
            String sqlStr = QueryAdjusterForDomain.getInstance().adjustSQL("FROM PrepType WHERE collectionId = COLLID");
            log.debug(sqlStr);
            if (StringUtils.isNotEmpty(sqlStr))
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    List<?> dataList = session.getDataList(sqlStr);
                    if (dataList != null && !dataList.isEmpty())
                    {
                        // XXX for now we just get the First one
                        return (PrepType)dataList.iterator().next();
                    }
                    // No Data Error
        
                } catch (Exception ex)
                {
                    log.error(ex);
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
                log.error("Query String is empty for tableId["+tableInfo.getTableId()+"]");
            }
        } else
        {
            throw new RuntimeException("Error looking up PickLIst's Table Name PrepType");
        }
        return null;
    }
    
    /**
     * @return the default preparer
     */
    protected Agent getDefaultPreparedByAgent()
    {
        return Agent.getUserAgent();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        CollectionObject colObj = (CollectionObject)newDataObj;
        if (Collection.getCurrentCollection().getIsEmbeddedCollectingEvent())
        {
            // Carry Forward may have already added 
            // some values so we need to check to make sure
            // before adding new ones automatically.
            if (colObj.getCollectingEvent() == null)
            {
                CollectingEvent ce = new CollectingEvent();
                ce.initialize();
                colObj.addReference(ce, "collectingEvent");
            }
            
            if (colObj.getPreparations().size() == 0)
            {
                Preparation prep = new Preparation();
                prep.initialize();
                prep.setCount(1);
                prep.setPrepType(getDefaultPrepType());
                prep.setPreparedDate(Calendar.getInstance());
                colObj.addReference(prep, "preparations");
                prep.setPreparedByAgent(getDefaultPreparedByAgent());
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object dataObj,
                           final DataProviderSessionIFace sessionArg,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        boolean isOK = true;
        if (deletable != null)
        {
            DataProviderSessionIFace session = sessionArg != null ? sessionArg : DataProviderFactory.getInstance().createSession();
            try
            {
                session.attach(dataObj);
    
                CollectionObject colObj = (CollectionObject)dataObj;
                for (Preparation prep : colObj.getPreparations())
                {
                    if (!prep.getLoanPreparations().isEmpty())
                    {
                        isOK = false;
                        addDeleteReason(LoanPreparation.getClassTableId());
                    }
                    
                    if (!prep.getDeaccessionPreparations().isEmpty())
                    {
                        isOK = false;
                        addDeleteReason(DeaccessionPreparation.getClassTableId());
                    }
                    
                    if (colObj.getAccession() != null)
                    {
                        isOK = false;
                        addDeleteReason(Accession.getClassTableId());
                    }
                    
                    if (!isOK)
                    {
                        break;
                    }
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                // Error Dialog
                
            } finally
            {
                if (sessionArg == null && session != null)
                {
                    session.close();
                }
            }
            
            deletable.doDeleteDataObj(dataObj, session, isOK);
            
        } else
        {
            super.okToDelete(dataObj, sessionArg, deletable);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#shouldCloneField(java.lang.String)
     */
    @Override
    public boolean shouldCloneField(String fieldName)
    {
        // Depending on the Type of Discipline the Collecting Events can be shared 
        // a ManyToOne from CollectionObject to Collecting (Fish).
        //
        // Or it acts like a OneToOne where each CE acts as if it is "embedded" or is
        // a part of the CO.
        //System.err.println(fieldName);
        if (fieldName.equals("collectingEvent"))
        {
            // So we need to clone it make a full copy when it is embedded.
            return Collection.getCurrentCollection().getIsEmbeddedCollectingEvent();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object, boolean)
     */
    @Override
    public STATUS processBusinessRules(Object parentDataObj, Object dataObj, boolean isEdit)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof CollectionObject))
        {
            return STATUS.Error;
        }
        
        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("catalogNumber", 
                                                                (FormDataObjIFace)dataObj, 
                                                                Accession.class, 
                                                                "collectionObjectId");
        
        return duplicateNumberStatus;
    }

}

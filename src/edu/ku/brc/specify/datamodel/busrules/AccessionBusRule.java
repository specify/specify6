/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.datamodel.busrules;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgents;
import edu.ku.brc.specify.datamodel.AccessionAuthorizations;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.forms.BusinessRulesDataItem;
import edu.ku.brc.ui.forms.BusinessRulesIFace;
import edu.ku.brc.ui.forms.DataObjFieldFormatMgr;
import edu.ku.brc.ui.forms.DraggableRecordIdentifier;

/**
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class AccessionBusRule implements BusinessRulesIFace
{
    private static final Logger  log      = Logger.getLogger(AccessionBusRule.class);
    
    private List<String> errorList = new Vector<String>();
   
    /**
     * Constructor.
     */
    public AccessionBusRule()
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
                Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class);
                criteria.add(Expression.eq("accessionId", id));
                List accessions = criteria.list();
                if (accessions.size() == 1)
                {
                    Accession oldAccession       = (Accession)accessions.get(0);
                    String    oldAccessionNumber = oldAccession.getNumber();
                    if (oldAccessionNumber.equals(accession.getNumber()))
                    {
                        checkAccessionNumberForDuplicates = false;
                    }
                }
            }
            
            // If the Id is null then it is a new permit, if not then we are editting the accession
            //
            // If the accession has not changed then we shouldn't check for duplicates
            if (checkAccessionNumberForDuplicates)
            {
                Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class);
                criteria.add(Expression.eq("number", accessionNumber));
                List accessionNumbers = criteria.list();
                if (accessionNumbers.size() > 0)
                {
                    errorList.add("Accession Number is already in use."); // I18N
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
            errorList.add("Accession Number is missing!"); // I18N
        }

        return STATUS.Error;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getStandAloneDataItems(java.lang.Object)
     */
    public List<BusinessRulesDataItem> getStandAloneDataItems(Object dataObj)
    {
        
        List<BusinessRulesDataItem> list = new ArrayList<BusinessRulesDataItem>();
        Accession accession = (Accession)dataObj;
        
        for (AccessionAgents accAgent : accession.getAccessionAgents())
        {
            Agent agent = accAgent.getAgent();
            if (agent != null && agent.getAgentId() == null)
            {
                list.add(new AccessionBRS(agent));
            }
        }
        
        for (AccessionAuthorizations auth : accession.getAccessionAuthorizations())
        {
            Permit permit = auth.getPermit();
            if (permit != null && permit.getPermitId() == null)
            {
                list.add(new AccessionBRS(permit));
            }
        }
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#saveStandAloneData(java.lang.Object, java.util.List)
     */
    public void saveStandAloneData(final Object dataObj, final List<BusinessRulesDataItem> list)
    {
        if (!(dataObj instanceof Accession))
        {
            return;
        }
        
        try
        {
            HibernateUtil.beginTransaction();
            Session session = HibernateUtil.getCurrentSession();
            
            for (BusinessRulesDataItem item : list)
            {
                if (item.isChecked())
                {
                    session.save(item.getData());
                }
            }
            HibernateUtil.commitTransaction();
            
        } catch (Exception ex)
        {
            log.error(ex);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public boolean okToDelete(Object dataObj)
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
                    Statement stmt = DBConnection.getConnection().createStatement();
                    ResultSet rs = stmt.executeQuery("select count(*) from collectionobject where AccessionID = "+accession.getAccessionId());
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
        throw new RuntimeException("DataObj is not an Accession ["+dataObj.getClass().getSimpleName()+"]");
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Accession)
        {
            return "Accession "+((Accession)dataObj).getNumber() + " was deleted."; // I18N
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
                
            } else
            {
                RecordSet rs = (RecordSet)data;
                rs.getItems().clear();
                rs.addItem(accession.getAccessionId());
            }
        }
     }
    
    //-----------------------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------------------
    class AccessionBRS extends BusinessRulesDataItem
    {
        public AccessionBRS(final Object data)
        {
            super(data);
        }
        
        public String toString()
        {
            if (data instanceof Agent)
            {
                return DataObjFieldFormatMgr.format(data, "Agent"); // NOTE: This assumes we definitely have an "Agent" format

            } else if (data instanceof Permit)
            {
                return ((Permit)data).getPermitNumber();
            }
            return null;
        }
    }

}

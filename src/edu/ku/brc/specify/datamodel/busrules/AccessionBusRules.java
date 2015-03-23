/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.awt.Component;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.UIRegistry;

/**
 * Business Rules for validating a Accession.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class AccessionBusRules extends AttachmentOwnerBaseBusRules
{
    //private static final Logger  log      = Logger.getLogger(AccessionBusRules.class);
   
    /**
     * Constructor.
     */
    public AccessionBusRules()
    {
        super(Accession.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        if (collection != null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                // Just in case the Discipline and Division aren't loaded
                // that should happen.
                session.attach(collection); 
                
                Division division = collection.getDiscipline().getDivision();
                if (division != null)
                {
                    Accession accession = (Accession)newDataObj;
                    accession.setDivision(division);
                }
                
            } catch (Exception ex)
            {
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AccessionBusRules.class, ex);
                ex.printStackTrace();
                UsageTracker.incrNetworkUsageCount();
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#shouldCreateSubViewData(java.lang.String)
     */
    @Override
    public boolean shouldCreateSubViewData(String fieldName)
    {
        //return fieldName.equals("accessionAgents") ||
        //       fieldName.equals("accessionAuthorizations");
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (!(viewable instanceof FormViewObj) || dataObj == null)
        {
            return;
        }
        
        Accession accession = (Accession)dataObj;
        
        DBTableInfo divisionTI = DBTableIdMgr.getInstance().getInfoById(Division.getClassTableId());
        FormViewObj fvo = (FormViewObj)viewable;
        JLabel label = (JLabel)fvo.getLabelById("divLabel");
        if (label != null)
        {
            label.setText(divisionTI.getTitle()+":");
        }
        
        Component divComp = fvo.getControlById("4");
        if (divComp instanceof ValComboBox)
        {
            ValComboBox cbx = (ValComboBox)divComp;
            DefaultComboBoxModel model = (DefaultComboBoxModel)cbx.getModel();
            model.removeAllElements();
            model.addElement(accession.getDivision());
            cbx.getComboBox().setSelectedIndex(0);
            
        } else
        {
            JTextField tf = (JTextField)divComp;
            tf.setText(accession.getDivision().getName());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof Accession))
        {
            return STATUS.Error;
        }
        
        Accession accession = (Accession)dataObj;
        
        // Check for AcccessionAgent and their Roles (for duplicates)
        Hashtable<String, Boolean> agentRoleHash = new Hashtable<String, Boolean>();
        for (AccessionAgent aa : accession.getAccessionAgents())
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
                    reasonList.add(UIRegistry.getLocalizedMessage("ACCESSION_DUP_AGENTROLE", agent.toString(), aa.getRole()));
                    return STATUS.Error;
                }
            }
        }
        
        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("accessionNumber", 
                (FormDataObjIFace)dataObj, 
                Accession.class, 
                "accessionId");
        
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
                        if (rs.next())
                        {
                            return rs.getInt(1) == 0;
                        }
                        rs.close();
                        stmt.close();
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AccessionBusRules.class, ex);
                        log.error(ex);
                        throw new RuntimeException(ex);
                    }
                } else
                {
                    return true;
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
            return getLocalizedMessage("ACCESSION_DELETED", ((Accession)dataObj).getAccessionNumber());
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
            
            draggableIcon.setLabel(accession.getAccessionNumber());
            
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
                rs.clearItems();
                rs.addItem(accession.getAccessionId());
            }
        }
     }
}

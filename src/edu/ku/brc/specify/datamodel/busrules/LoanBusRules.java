/* Copyright (C) 2009, University of Kansas Center for Research
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIRegistry;

/**
 * Business rules for validating a Loan.
 * 
 * @code_status Complete
 *
 * @author rods
 *
 */
public class LoanBusRules extends AttachmentOwnerBaseBusRules
{  
    public static final String CMDTYPE     = "Interactions";
    public static final String DE_CMDS     = "Data_Entry";
    public static final String ADD_TO_LOAN = "AddToLoan";
    public static final String NEW_LOAN    = "NEW_LOAN";
    
    /**
     * Constructor.
     */
    public LoanBusRules()
    {
        super(Loan.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        formViewObj.setSkippingAttach(true);

        if (isEditMode())
        {
            Component closedComp = formViewObj.getControlByName("isClosed");
            if (closedComp instanceof JCheckBox)
            {
                ((JCheckBox)closedComp).addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (((JCheckBox)e.getSource()).isSelected())
                        {
                            Component dateComp = formViewObj.getControlByName("dateClosed");
                            if (dateComp != null && dateComp instanceof ValFormattedTextFieldSingle)
                            {
                                ValFormattedTextFieldSingle loanDateComp = (ValFormattedTextFieldSingle)dateComp;
                                //System.out.println("["+loanDateComp.getText()+"]");
                                if (StringUtils.isEmpty(loanDateComp.getText()))
                                {
                                    DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
                                    loanDateComp.setText(scrDateFormat.format(Calendar.getInstance()));
                                }
                            }
                        }
                    }
                });
            }
        }
        
        /*if (formViewObj.getRsController() != null)
        {
            JButton newBtn = formViewObj.getRsController().getNewRecBtn();
            if (newBtn != null)
            {
                // Remove all ActionListeners, there should only be one
                for (ActionListener al : newBtn.getActionListeners())
                {
                    newBtn.removeActionListener(al);
                }
                
                newBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        CommandAction cmdAction = new CommandAction(CMDTYPE, NEW_LOAN, null);
                        cmdAction.setData(cmdAction); // simulating a click
                        CommandDispatcher.dispatch(cmdAction);
                    }
                });
            }
        }*/
    }
    
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#canCreateNewDataObject()
     */
    @Override
    public boolean canCreateNewDataObject()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#createNewObj(boolean, java.lang.Object)
     */
    @Override
    public void createNewObj(boolean doSetIntoAndValidateArg, Object oldDataObj)
    {
        CommandAction cmdAction = new CommandAction(CMDTYPE, NEW_LOAN, viewable);
        CommandDispatcher.dispatch(cmdAction);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeFormFill()
     */
    @Override
    public void beforeFormFill()
    {
        Loan loan = (Loan)formViewObj.getDataObj();
        
        if (formViewObj != null && loan != null)
        {
            if (formViewObj.getSession() == null && loan.getId() != null)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();    
                    session.attach(loan);
                    loan.forceLoad();
                }
                catch (Exception ex)
                {
                    //UsageTracker.incrHandledUsageCount();
                    //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataEntryTask.class, ex);
                    //log.error(ex);
                    ex.printStackTrace();
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        if (formViewObj != null && formViewObj.getDataObj() instanceof Loan)
        {
            formViewObj.setSkippingAttach(true);

            MultiView mvParent = formViewObj.getMVParent();
            Loan      loan     = (Loan)formViewObj.getDataObj();
            boolean   isNewObj = loan.getId() == null;//MultiView.isOptionOn(mvParent.getOptions(), MultiView.IS_NEW_OBJECT);
            boolean   isEdit   = mvParent.isEditable();

            Component comp     = formViewObj.getControlByName("generateInvoice");
            if (comp instanceof JCheckBox)
            {
                ((JCheckBox)comp).setVisible(isEdit);
            }
            
            boolean allResolved = true;
            for (LoanPreparation loanPrep : loan.getLoanPreparations())
            {
                Boolean isResolved = loanPrep.getIsResolved();
                if (isResolved == null || (isResolved != null && !isResolved))
                {
                    allResolved = false;
                    break;
                }
            }
            
            comp = formViewObj.getControlByName("ReturnLoan");
            if (comp instanceof JButton)
            {
                comp.setVisible(isEdit);
                Boolean isClosed = loan.getIsClosed();
                comp.setEnabled(!isNewObj && (isClosed != null ? !loan.getIsClosed() : false) && !allResolved);
                
                if (allResolved)
                {
                    ((JButton)comp).setText(UIRegistry.getResourceString("LOAN_ALL_PREPS_RETURNED"));
                }
            }
            
            if (isNewObj)
            {
                Component shipComp = formViewObj.getControlByName("shipmentNumber");
                comp = formViewObj.getControlByName("loanNumber");
                if (comp instanceof JTextField && shipComp instanceof JTextField)
                {
                    JTextField loanTxt = (JTextField)comp;
                    if (shipComp instanceof GetSetValueIFace)
                    {
                        GetSetValueIFace gsv = (GetSetValueIFace)shipComp;
                        gsv.setValue(loanTxt.getText(), loanTxt.getText());
                        
                    } else if (shipComp instanceof JTextField)
                    {
                        ((JTextField)shipComp).setText(loanTxt.getText());
                    }
                    
                    if (shipComp instanceof UIValidatable)
                    {
                        UIValidatable uiv = (UIValidatable)shipComp;
                        uiv.setChanged(true);
                    }
                }
            }
        }
    }
    
    /**
     * Calculated from the database the amount of available Preparations.
     * @param prep the preparation in question
     * @return the count available
     */
    /*public static int getUsedPrepCount(final Preparation prep)
    {
        String sql = "SELECT CountAmt FROM preparation WHERE PreparationID = " + prep.getId();
        int prepQty = getInt(BasicSQLUtils.getCount(sql));
        
        String lpSQL = "SELECT %s FROM loanpreparation AS lp INNER JOIN preparation AS p ON lp.PreparationID = p.PreparationID WHERE p.PreparationID = %d";
        
        int prepQtyLoaned   = getInt(BasicSQLUtils.getCount(String.format(lpSQL, "lp.Quantity", prep.getId())));
        int prepQtyResolved = getInt(BasicSQLUtils.getCount(String.format(lpSQL, "lp.QuantityResolved", prep.getId())));
        
        sql = "SELECT gp.Quantity " +
              "FROM giftpreparation AS gp INNER JOIN preparation AS p ON gp.PreparationID = p.PreparationID " +
              "WHERE p.PreparationID = " + prep.getId();
        int prepQtyGifted = getInt(BasicSQLUtils.getCount(sql));

        return prepQty - prepQtyLoaned + prepQtyResolved - prepQtyGifted;
    }*/
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean isOkToSave(Object dataObj, DataProviderSessionIFace session)
    {
        /*reasonList.clear();
        
        Loan loan = (Loan)dataObj;
        
        for (LoanPreparation loanPrep : loan.getLoanPreparations())
        {
            int availCnt = getUsedPrepCount(loanPrep.getPreparation());
            if (availCnt < 1)
            {
                reasonList.add("Not enough Preps to loan "+availCnt); // I18N
                return false;
            }
        }*/
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
         Loan loan = (Loan)dataObj;
        
        //System.out.println("beforeSaveCommit loanNum: "+loan.getLoanNumber());
        
        for (Shipment shipment : loan.getShipments())
        {
            if (shipment.getShipmentId() == null)
            {
                //shipment.setShipmentNumber(loan.getLoanNumber());
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        /*Loan loan = (Loan)dataObj;
        
        System.out.println("beforeSaveCommit loanNum: "+loan.getLoanNumber());
        
        for (Shipment shipment : loan.getShipments())
        {
            //if (shipment.getShipmentId() == null)
            //{
            String shipmentNum = shipment.getShipmentNumber();
            if (StringUtils.isEmpty(shipmentNum))
            {
                shipmentNum = loan.getLoanNumber();
                
            } else if (StringUtils.contains(shipmentNum, UIFieldFormatterMgr.getAutoNumberPatternChar())) // XXX Need to check the formatter!
            {
                shipment.setShipmentNumber(loan.getLoanNumber());
            }
        }*/
        return super.beforeSaveCommit(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        
        if (dataObj == null || !(dataObj instanceof Loan))
        {
            return STATUS.Error;
        }
        Loan loan = (Loan)dataObj;
        
        /*
        for (LoanAgent loanAgent : loan.getLoanAgents())
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
        if (loan.getId() == null)
        {
            STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("loanNumber", 
                                                                    (FormDataObjIFace)dataObj, 
                                                                    Loan.class, 
                                                                    "loanId");
            
            return duplicateNumberStatus;
        }
        return STATUS.OK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Accession)
        {
            return getLocalizedMessage("LOAN_DELETED", ((Loan)dataObj).getLoanNumber());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#setObjectIdentity(java.lang.Object, edu.ku.brc.ui.DraggableIcon)
     */
    public void setObjectIdentity(final Object dataObj, 
                                  final DraggableRecordIdentifier draggableIcon)
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
                rs.clearItems();
                rs.addItem(loan.getLoanId());
            }
        }
     }
}

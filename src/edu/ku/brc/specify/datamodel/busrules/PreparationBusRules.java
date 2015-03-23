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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 11, 2008
 *
 */
public class PreparationBusRules extends AttachmentOwnerBaseBusRules
{
    public PreparationBusRules()
    {
        super(Preparation.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(final Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            JButton btn = formViewObj.getCompById("ShowLoansBtn");
            if (btn != null)
            {
                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        showLoans();
                    }
                });
            }
        }
    }
    
    /**
     * 
     */
    private void showLoans()
    {
        if (formViewObj != null)
        {
            Preparation prep = (Preparation)formViewObj.getDataObj();
            if (prep != null)
            {
                /*
                 * final Dialog  parentDialog,
                                  final String  viewSetName,
                                  final String  viewName,
                                  final String  displayName,
                                  final String  title,
                                  final String  closeBtnTitle,
                                  final String  className,
                                  final String  idFieldName,
                                  final boolean isEdit,
                                  final int     options)
                 */
                ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Dialog)null,
                        null,
                        "Loan",
                        null,
                        "Loans", // I18N ?
                        UIRegistry.getResourceString("CLOSE"),
                        Loan.class.getName(),
                        "loanId",
                        false,
                        MultiView.HIDE_SAVE_BTN |
                        MultiView.RESULTSET_CONTROLLER);
                
                Vector<Loan> loans = new Vector<Loan>();
                
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    String sql = " SELECT DISTINCT loan.LoanID FROM loan Inner Join loanpreparation AS lp ON loan.LoanID = lp.LoanID WHERE loan.IsClosed <> 1 AND lp.PreparationID ="+prep.getId();
                    //log.debug(sql);
                    for (Integer id : BasicSQLUtils.queryForInts(sql))
                    {
                        Loan loan = session.get(Loan.class, id);
                        if (loan != null)
                        {
                            loans.add(loan);
                            loan.getLoanAgents().size();
                            loan.getLoanPreparations().size();
                            loan.getLoanAttachments().size();
                        }
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
                
                dlg.setData(loans);
                UIHelper.centerAndShow(dlg);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object dataObj,
                           final DataProviderSessionIFace session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        boolean isOK = false;
        if (deletable != null)
        {
            FormDataObjIFace dbObj = (FormDataObjIFace)dataObj;
            
            Integer id = dbObj.getId();
            if (id == null)
            {
                isOK = true;
                
            } else
            {
                DBTableInfo tableInfo      = DBTableIdMgr.getInstance().getInfoById(Preparation.getClassTableId());
                String[]    tableFieldList = gatherTableFieldsForDelete(new String[] {"preparation", "preparationattachment"}, tableInfo);
                isOK = okToDelete(1, tableFieldList, dbObj.getId());
            }
            deletable.doDeleteDataObj(dataObj, session, isOK);
            
        } else
        {
            super.okToDelete(dataObj, session, deletable);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#shouldCloneField(java.lang.String)
     */
    @Override
    public boolean shouldCloneField(String fieldName)
    {
        if (fieldName.equals("preparationAttribute"))
        {
            return true;
        }
        
        return false;
    }
}

/* Copyright (C) 2017, University of Kansas Center for Research
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
import edu.ku.brc.sgr.datamodel.DataModel;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.Gift;
import edu.ku.brc.specify.datamodel.Deaccession;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.ExchangeOut;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

import javax.persistence.Basic;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

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
                        showInteractions();
                    }
                });
            }
        }
    }


    private void showInteractions() {
        if (formViewObj != null) {
            Preparation prep = (Preparation)formViewObj.getDataObj();
            int loanCnt = BasicSQLUtils.getCountAsInt("select count(distinct l.loanid) from loan l inner join loanpreparation lp on lp.loanid = l.loanid where not lp.isresolved and lp.preparationid = " + prep.getId());
            int giftCnt = BasicSQLUtils.getCountAsInt("select count(distinct l.giftid) from gift l inner join giftpreparation lp on lp.giftid = l.giftid where lp.preparationid = " + prep.getId());
            int deaccCnt = BasicSQLUtils.getCountAsInt("select count(distinct l.deaccessionid) from deaccession l inner join deaccessionpreparation lp on lp.deaccessionid = l.deaccessionid where lp.preparationid = " + prep.getId());
            int exchCnt = BasicSQLUtils.getCountAsInt("select count(distinct l.exchangeoutid) from exchangeout l inner join exchangeoutprep lp on lp.exchangeoutid = l.exchangeoutid where lp.preparationid = " + prep.getId());
            if (loanCnt > 0) {
                showLoans();
            }
            if (giftCnt > 0) {
                showGifts();
            }
            if (deaccCnt > 0) {
                showDeaccessions();
            }
            if (exchCnt > 0) {
                showExchanges();
            }
            if (loanCnt + giftCnt + deaccCnt + exchCnt == 0) {
                UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "PreparationBusRules.NoAssociatedInteractionsTitle","PreparationBusRules.NoAssociatedInteractions");
            }
        }
    }
    /**
     * 
     */
    private void showLoans() {
        this.showInteraction(DBTableIdMgr.getInstance().getInfoById(Loan.getClassTableId()));
    }

    private void showGifts() {
        this.showInteraction(DBTableIdMgr.getInstance().getInfoById(Gift.getClassTableId()));
    }

    private void showDeaccessions() {
        this.showInteraction(DBTableIdMgr.getInstance().getInfoById(Deaccession.getClassTableId()));
    }

    private void showExchanges() {
        this.showInteraction(DBTableIdMgr.getInstance().getInfoById(ExchangeOut.getClassTableId()));
    }

    private void showInteraction(DBTableInfo tbl) {
        if (formViewObj != null) {
            Preparation prep = (Preparation)formViewObj.getDataObj();
            if (prep != null) {
                ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((java.awt.Dialog)null,
                        null,
                        tbl.getTitle(),
                        null,
                        tbl.getTitle() + "s", // I18N ?
                        UIRegistry.getResourceString("CLOSE"),
                        tbl.getClassName(),
                        tbl.getIdFieldName(),
                        false,
                        MultiView.HIDE_SAVE_BTN |
                                MultiView.RESULTSET_CONTROLLER);

                List<Object> iActions = new ArrayList<>();

                DataProviderSessionIFace session = null;
                try {
                    String prepTblName = tbl.getName();
                    prepTblName += "exchangeout".equalsIgnoreCase(prepTblName) ? "prep" : "preparation";
                    session = DataProviderFactory.getInstance().createSession();
                    String sql = " SELECT DISTINCT " + tbl.getName() + "." + tbl.getIdColumnName() + " FROM "
                    + tbl.getName() +  " Inner Join " + prepTblName + " AS lp ON " + tbl.getName() + "."
                    + tbl.getIdColumnName() + " = lp." + tbl.getIdColumnName() + " WHERE ";
                    if (tbl.getFieldByName("IsClosed") != null) {
                        sql += "not " + tbl.getName() + ".IsClosed AND ";
                    }
                    sql += "lp.PreparationID =" +prep.getId();
                    for (Integer id : BasicSQLUtils.queryForInts(sql)) {
                        Object iAction = session.get(tbl.getClassObj(), id);
                        if (iAction != null) {
                            iActions.add(iAction);
                            ((DataModelObjBase)iAction).forceLoad();
                        }
                    }

                } catch (Exception ex) {
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AccessionBusRules.class, ex);
                    ex.printStackTrace();
                    UsageTracker.incrNetworkUsageCount();

                } finally {
                    if (session != null) {
                        session.close();
                    }
                }

                dlg.setData(iActions);
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

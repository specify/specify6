/* Copyright (C) 2021, Specify Collections Consortium
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JButton;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.TableViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Gift;
import edu.ku.brc.specify.datamodel.GiftPreparation;
import edu.ku.brc.specify.tasks.InteractionsProcessor;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import org.apache.log4j.Logger;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 29, 2007
 *
 */
public class GiftPreparationBusRules extends BaseBusRules implements CommandListener
{
    private static final Logger log = Logger.getLogger(GiftPreparationBusRules.class);

    /**
     * 
     */
    public GiftPreparationBusRules()
    {
        super(GiftPreparation.class);
        
        CommandDispatcher.register(GiftBusRules.CMDTYPE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        
        JButton newBtn = getNewBtn();
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
                    MultiView giftMV = null;
                    if (viewable instanceof FormViewObj)
                    {
                        giftMV = formViewObj.getMVParent().getMultiViewParent();
                        formViewObj.getDataFromUI();
                        
                    } else if (viewable instanceof TableViewObj)
                    {
                        TableViewObj tblViewObj = (TableViewObj)viewable; 
                        giftMV = tblViewObj.getMVParent().getMultiViewParent();
                    }
                    
                    if (giftMV != null)
                    {
                        if (formViewObj != null)
                        {
                            formViewObj.getDataFromUI();
                        }
                        CommandDispatcher.dispatch(new CommandAction(GiftBusRules.CMDTYPE, "AddToGift", giftMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
                    }
                }
            });
        }
    }
    
    @Override
    public void afterFillForm(Object dataObj)
    {
        Component comp = formViewObj.getControlByName("quantity");
        if (comp instanceof ValSpinner && dataObj != null)
        {
            GiftPreparation  giftPrep   = (GiftPreparation)dataObj;
            
            ValSpinner quantity         = (ValSpinner)comp;
            
            if (quantity != null)
            {
                int qMax = 5000;
                if (giftPrep.getPreparation() != null && giftPrep.getPreparation().getId() != null) {
                    boolean[] settings = {true, true, true, true}; //the false means stuff on loan will be available to gift???
                    String sql = InteractionsProcessor.getAdjustedCountForPrepSQL("p.preparationid = " + giftPrep.getPreparation().getId(), settings);
                    Connection conn = InteractionsProcessor.getConnForAvailableCounts();
                    Object[] amt = BasicSQLUtils.queryForRow(conn, sql);
                    qMax = amt != null ? Integer.valueOf(amt[1].toString()).intValue() : qMax;
                    try {
                        conn.close();
                    } catch (SQLException x) {
                        log.warn(x);
                    }
                    if (giftPrep.getId() != null) {
                        qMax += giftPrep.getQuantity();
                    }
                }
                quantity.setRange(0, qMax, giftPrep.getQuantity());
            }
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        CommandDispatcher.unregister(GiftBusRules.CMDTYPE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(GiftBusRules.CMDTYPE) && cmdAction.isAction("REFRESH_GIFT_PREPS"))
        {
            if (formViewObj != null)
            {
                MultiView giftMV = formViewObj.getMVParent().getMultiViewParent();
                if (giftMV != null)
                {
                    if (formViewObj.getValidator() != null)
                    {
                        Gift gift = (Gift)giftMV.getData();
                        formViewObj.setDataObj(gift.getGiftPreparations());
                        formViewObj.getValidator().setHasChanged(true);
                        formViewObj.getValidator().validateRoot();
                    }
                }

            } else if (viewable instanceof TableViewObj)
            {
                TableViewObj tvo = (TableViewObj)viewable;
                // Make sure the Loan form knows there is a change
                MultiView giftMV = tvo.getMVParent().getMultiViewParent();
                giftMV.getCurrentValidator().setHasChanged(true);
                giftMV.getCurrentValidator().validateRoot();
                
                // Refresh list in the grid
                tvo.refreshDataList();
            }
        }
    }
}

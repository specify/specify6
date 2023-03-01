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

import edu.ku.brc.af.ui.forms.*;
import edu.ku.brc.specify.datamodel.ExchangeOut;
import edu.ku.brc.specify.datamodel.ExchangeOutPrep;
import edu.ku.brc.specify.tasks.InteractionsTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExchangeOutPrepBusRules extends BaseBusRules implements CommandListener {

    /**
     *
     */
    public ExchangeOutPrepBusRules()
    {
        super(ExchangeOutPrep.class);

        CommandDispatcher.register("Interactions", this);
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
                    MultiView exchOutMV = null;
                    if (viewable instanceof FormViewObj)
                    {
                        exchOutMV = formViewObj.getMVParent().getMultiViewParent();
                        formViewObj.getDataFromUI();

                    } else if (viewable instanceof TableViewObj)
                    {
                        TableViewObj tblViewObj = (TableViewObj)viewable;
                        exchOutMV = tblViewObj.getMVParent().getMultiViewParent();
                    }

                    if (exchOutMV != null)
                    {
                        if (formViewObj != null)
                        {
                            formViewObj.getDataFromUI();
                        }
                        CommandDispatcher.dispatch(new CommandAction("Interactions", InteractionsTask.ADD_TO_EXCHANGE,
                                exchOutMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
                    }
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType("Interactions") && cmdAction.isAction("REFRESH_EXCHANGE_PREPS"))
        {
            if (formViewObj != null)
            {
                MultiView exchMV = formViewObj.getMVParent().getMultiViewParent();
                if (exchMV != null)
                {
                    if (formViewObj.getValidator() != null)
                    {
                        ExchangeOut exchange = (ExchangeOut)exchMV.getData();
                        formViewObj.setDataObj(exchange.getExchangeOutPreps());
                        formViewObj.getValidator().setHasChanged(true);
                        formViewObj.getValidator().validateRoot();
                    }
                }

            } else if (viewable instanceof TableViewObj)
            {
                TableViewObj tvo = (TableViewObj)viewable;
                // Make sure the Loan form knows there is a change
                MultiView exchOut = tvo.getMVParent().getMultiViewParent();
                exchOut.getCurrentValidator().setHasChanged(true);
                exchOut.getCurrentValidator().validateRoot();

                // Refresh list in the grid
                tvo.refreshDataList();
            }
        }
    }

}

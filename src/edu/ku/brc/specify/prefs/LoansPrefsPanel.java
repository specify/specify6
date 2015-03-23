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
package edu.ku.brc.specify.prefs;

import static edu.ku.brc.specify.datamodel.busrules.LoanBusRules.DUEINMONTHS;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.busrules.LoanGiftShipmentBusRules;

/**
 * Preference Panel for setting EMail Preferences.
 *
 * This also includes a method that kicks off a dialog on a thread to check to make sure all the email settings are correct.
 *
 * @code_status Alpha
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class LoansPrefsPanel extends GenericPrefsPanel implements PrefsSavable, PrefsPanelIFace
{

    /**
     * Constructor of the EMail setting panel.
     */
    public LoansPrefsPanel()
    {
        super();
        createUI();
    }

    /**
     * Create the UI for the panel
     */
    protected void createUI()
    {
        createForm("Preferences", "LoansPrefs");
        AppPreferences prefs = AppPreferences.getRemote();
        
        Integer dueInMonths      = prefs.getInt(DUEINMONTHS, 6);
        String  shippingMethod   = prefs.get(LoanGiftShipmentBusRules.SHIPMETHOD, null);
        Integer shippedByAgentId = prefs.getInt(LoanGiftShipmentBusRules.SHIPPEDBY, null);
        
        FormViewObj fvo = (FormViewObj)form;
        
        ValSpinner dueSpinner = fvo.getCompById("OVERDUETIME");
        if (dueSpinner != null)
        {
            dueSpinner.setValue(dueInMonths);
        }
        
        ValComboBox shipMeth = fvo.getCompById("SHIPMETH");
        if (shipMeth != null)
        {
            shipMeth.setValue(shippingMethod, null);
        }
        
        ValComboBoxFromQuery shippedBy = fvo.getCompById("SHIPPEDBY");
        if (shippedBy != null && shippedByAgentId != null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                Agent agent = session.get(Agent.class, shippedByAgentId);
                shippedBy.setValue(agent, null);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ValComboBoxFromQuery.class, ex);
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

    //--------------------------------------------------------------------
    // PrefsSavable Interface
    //--------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        AppPreferences prefs = AppPreferences.getRemote();
        
        FormViewObj fvo = (FormViewObj)form;
        
        ValSpinner dueSpinner = fvo.getCompById("OVERDUETIME");
        if (dueSpinner != null)
        {
            Integer val = (Integer)dueSpinner.getValue();
            if (val != null)
            {
                prefs.putInt(DUEINMONTHS, val);
            }
        }
        
        ValComboBox shipMeth = fvo.getCompById("SHIPMETH");
        if (shipMeth != null)
        {
            String method = (String)shipMeth.getValue();
            if (method != null)
            {
                prefs.put(LoanGiftShipmentBusRules.SHIPMETHOD, method);
            }
        }
        
        ValComboBoxFromQuery shippedBy = fvo.getCompById("SHIPPEDBY");
        if (shippedBy != null)
        {
            Agent agent = (Agent)shippedBy.getValue();
            if (agent != null)
            {
                prefs.putInt(LoanGiftShipmentBusRules.SHIPPEDBY, agent.getAgentId());
            } else
            {
                prefs.remove(LoanGiftShipmentBusRules.SHIPPEDBY);
            }
        }
    }
}

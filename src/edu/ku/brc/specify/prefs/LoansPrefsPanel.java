/* Copyright (C) 2020, Specify Collections Consortium
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
package edu.ku.brc.specify.prefs;

import static edu.ku.brc.specify.datamodel.busrules.LoanBusRules.DUEINMONTHS;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
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
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.busrules.LoanGiftShipmentBusRules;
import edu.ku.brc.specify.tasks.InteractionsProcessor;
import org.apache.xml.utils.StringComparable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    private ValComboBox coFld = null;
    private ValComboBox prepFld = null;
    private ValComboBox defSrc = null;

    /**
     * Constructor of the Loans setting panel.
     */
    public LoansPrefsPanel()
    {
        super();
        createUI();
    }

    public LoansPrefsPanel(boolean createUI) {
        super();
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
        Integer defSrcTblId = prefs.getInt(InteractionsProcessor.DEFAULT_SRC_TBL_ID, null);
        String defCOItemIdFld = prefs.get(InteractionsProcessor.getInteractionItemLookupFieldPref(CollectionObject.getClassTableId()),
                InteractionsProcessor.getDefaultInteractionLookupField(CollectionObject.getClassTableId()));
        String defPrepItemIdFld = prefs.get(InteractionsProcessor.getInteractionItemLookupFieldPref(Preparation.getClassTableId()),
                InteractionsProcessor.getDefaultInteractionLookupField(Preparation.getClassTableId()));

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
        defSrc = fvo.getCompById("lnDefSrcCBX");
        ((DefaultComboBoxModel)defSrc.getModel()).addElement(getResourceString("LoanPrefsPanel.AskDefSrc"));
        ((DefaultComboBoxModel)defSrc.getModel()).addElement(DBTableIdMgr.getInstance().getTitleForId(CollectionObject.getClassTableId()));
        ((DefaultComboBoxModel)defSrc.getModel()).addElement(DBTableIdMgr.getInstance().getTitleForId(Preparation.getClassTableId()));


        if (defSrcTblId == null || defSrcTblId == 0) {
            defSrc.getComboBox().setSelectedIndex(0);
        } else if (defSrcTblId == 1) {
            defSrc.getComboBox().setSelectedIndex(1);
        } else if (defSrcTblId == 63) {
            defSrc.getComboBox().setSelectedIndex(2);
        }

        ((JLabel)fvo.getLabelFor("lnIdFldCoCBX")).setText(DBTableIdMgr.getInstance().getTitleForId(CollectionObject.getClassTableId()) + ":");
        coFld = fvo.getCompById("lnIdFldCoCBX");
        addIdFldChoices(coFld, CollectionObject.getClassTableId(), defCOItemIdFld);
        ((JLabel)fvo.getLabelFor("lnIdFldPrepCBX")).setText(DBTableIdMgr.getInstance().getTitleForId(Preparation.getClassTableId()) + ":");
        prepFld = fvo.getCompById("lnIdFldPrepCBX");
        addIdFldChoices(prepFld, Preparation.getClassTableId(), defPrepItemIdFld);
    }

    private void addIdFldChoices(ValComboBox cb, int tblId, String defIdFld) {
        List<DBFieldInfo> flds = new ArrayList<DBFieldInfo>();
        for (DBFieldInfo fi : DBTableIdMgr.getInstance().getInfoById(tblId).getFields()) {
            if (isValidIdFld(fi)) {
                flds.add(fi);
            }
        }
        Collections.sort(flds, (fieldInfo, t1) -> {
            String title1 = fieldInfo.getTitle() == null ? fieldInfo.getName() : fieldInfo.getTitle();
            String title2 = t1.getTitle() == null ? t1.getName() : t1.getTitle();
            return title1.compareTo(title2);
        });
        int idx = 0;
        for (DBFieldInfo fi : flds) {
            if (fi.getName().equalsIgnoreCase(defIdFld)) {
                idx =  cb.getModel().getSize();
            }
            ((DefaultComboBoxModel)cb.getModel()).addElement(fi);
        }
        cb.getComboBox().setSelectedIndex(idx);
    }

    private static String theValidCoItemIdFlds = ",catalogNumber,altCatalogNumber,barCode,fieldNumber,GUID,modifier,name,projectNumber,Text1,Text2,Text3,";
    private static String theValidPrepItemIdFlds = ",BarCode,GUID,SampleNumber,Text1,Text2,Text3,Text4,Text5,Text6,Text7,Text8,Text9,";
    private boolean isValidIdFld(DBFieldInfo fieldInfo) {
        int tableId = fieldInfo.getTableInfo().getTableId();
        if (!fieldInfo.isHidden() && (tableId == 1 || tableId == 63)) {
            String validFlds = tableId == 1 ? theValidCoItemIdFlds : theValidPrepItemIdFlds;
            return validFlds.toLowerCase().contains("," + fieldInfo.getName().toLowerCase() + ",");
        }
        return false;
    }

    private Integer getSrcTblIdForSelection() {
        if (defSrc.getComboBox().getSelectedIndex() == 0) {
            return  0;
        } else if (defSrc.getComboBox().getSelectedIndex() == 1) {
            return 1;
        }  else if (defSrc.getComboBox().getSelectedIndex() == 2) {
            return 63;
        } else {
            return 0;
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

        prefs.putInt(InteractionsProcessor.DEFAULT_SRC_TBL_ID, getSrcTblIdForSelection());
        prefs.put(InteractionsProcessor.getInteractionItemLookupFieldPref(CollectionObject.getClassTableId()),
                ((DBFieldInfo)coFld.getComboBox().getModel().getSelectedItem()).getName());
        prefs.put(InteractionsProcessor.getInteractionItemLookupFieldPref(Preparation.getClassTableId()),
                ((DBFieldInfo)prepFld.getComboBox().getModel().getSelectedItem()).getName());
    }
}

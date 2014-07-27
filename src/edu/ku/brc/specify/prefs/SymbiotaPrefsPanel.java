/**
 * 
 */
package edu.ku.brc.specify.prefs;

import static edu.ku.brc.specify.datamodel.busrules.LoanBusRules.DUEINMONTHS;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.tasks.SymbiotaTask;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class SymbiotaPrefsPanel extends GenericPrefsPanel {

	/**
	 * 
	 */
	public SymbiotaPrefsPanel() {
		super();
		createUI();
	}
	
	/**
	 * 
	 */
	protected void createUI() {
		createForm("Preferences", "Symbiota");
		
        AppPreferences prefs = AppPreferences.getRemote();
        
        String  baseUrl   = prefs.get(SymbiotaTask.BASE_URL_PREF, SymbiotaTask.BASE_URL_DEFAULT);
         
        FormViewObj fvo = (FormViewObj)form;
        
        ValTextField comp = fvo.getCompById("1");
        if (comp != null)
        {
        	comp.setText(baseUrl);
        }
        
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
	 */
	@Override
	public void savePrefs() {
        AppPreferences prefs = AppPreferences.getRemote();
        
        FormViewObj fvo = (FormViewObj)form;
        
        ValTextField comp = fvo.getCompById("1");
        if (comp != null)
        {
            String val = comp.getText();
            if (val != null)
            {
                prefs.put(SymbiotaTask.BASE_URL_PREF, val);
            }
        }
	}
	
	
}

/**
 * 
 */
package edu.ku.brc.specify.prefs;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
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
        
//        Boolean showTask = AppPreferences.getLocalPrefs().getBoolean(SymbiotaTask.SHOW_TASK_PREF, false);
//        ValCheckBox chk = fvo.getCompById("showtask");
//        if (chk != null) {
//        	chk.setValue(showTask, null);
//        }
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
	 */
	@Override
	public void savePrefs() {
        AppPreferences prefs = AppPreferences.getRemote();
        
        FormViewObj fvo = (FormViewObj)form;
        
        ValTextField comp = fvo.getCompById("1");
        if (comp != null) {
            String val = comp.getText();
            if (val != null) {
                prefs.put(SymbiotaTask.BASE_URL_PREF, val);
            }
        }
//        ValCheckBox chk = fvo.getCompById("showtask");
//        if (chk != null) {
//        	Object value = chk.getValue();
//        	if (value instanceof Boolean) {
//        		AppPreferences.getLocalPrefs().putBoolean(SymbiotaTask.SHOW_TASK_PREF, Boolean.class.cast(value));
//        	}
//        }
        		
	}
	
	
}

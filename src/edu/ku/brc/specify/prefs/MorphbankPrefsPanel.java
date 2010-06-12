/**
 * 
 */
package edu.ku.brc.specify.prefs;

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.plugins.morphbank.MorphBankTest;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class MorphbankPrefsPanel extends GenericPrefsPanel
{
	
	/**
	 * 
	 */
	public MorphbankPrefsPanel()
	{
		super();
        createForm("Preferences", "Morphbank");
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.prefs.GenericPrefsPanel#createForm(java.lang.String, java.lang.String)
	 */
	@Override
	public void createForm(String viewSetName, String viewName)
	{
		super.createForm(viewSetName, viewName);
		JComboBox maps = ((ValComboBox )form.getCompById("7")).getComboBox();
		maps.setModel(new DefaultComboBoxModel(getExistingExportMappings()));
		String pref = AppPreferences.getRemote().get("morphbank.dwcmapping", null);
		if (maps.getModel().getSize() > 1 && pref != null)
		{
			maps.setSelectedItem(pref);
		} else if (maps.getModel().getSize() == 1 && pref == null)
		{
			form.getValidator().setHasChanged(true); //trying to enable the OK button???
		}
		pref = AppPreferences.getRemote().get("morphbank.baseurl", null);
		if (pref == null)
		{
			ValTextField comp = (ValTextField )form.getCompById("10");
			if (comp != null)
			{
				comp.setText(MorphBankTest.MORPHBANK_URL);
				comp.setChanged(true);
				form.getValidator().setHasChanged(true); //trying to enable the OK button???
			}
		}
		pref = AppPreferences.getRemote().get("morphbank.imageposturl", null);
		if (pref == null)
		{
			ValTextField comp = (ValTextField )form.getCompById("11");
			if (comp != null)
			{
				comp.setText(MorphBankTest.MORPHBANK_IM_POST_URL);
				comp.setChanged(true);
				form.getValidator().setHasChanged(true); //trying to enable the OK button???
			}
		}
		
	}

	/**
	 * @return
	 */
	private Vector<Object> getExistingExportMappings()
	{
		String sql = "select distinct mappingname from spexportschemamapping esm inner join "
			+ "spexportschemaitemmapping esim on esim.spexportschemamappingid = "
			+ "esm.spexportschemamappingid inner join spexportschemaitem esi on "
			+ "esi.spexportschemaitemid = esim.exportschemaitemid inner join spexportschema es "
			+ "on es.spexportschemaid = esi.spexportschemaid where disciplineid = ";
		sql += AppContextMgr.getInstance().getClassObject(Discipline.class).getId();
		return BasicSQLUtils.querySingleCol(sql);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
	 */
	@Override
	public void savePrefs()
	{
		ValComboBox dwc = (ValComboBox )form.getCompById("7");
		if (dwc != null)
		{
			dwc.setChanged(true);
		}
		super.savePrefs();
	}
	
	
}

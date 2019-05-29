/* Copyright (C) 2019, University of Kansas Center for Research
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

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.tasks.AuditLogCleanupTask;
import edu.ku.brc.ui.UIRegistry;

import javax.swing.*;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 8, 2011
 *
 */
public class AuditPrefsPanel extends GenericPrefsPanel implements PrefsSavable, PrefsPanelIFace {
    protected JCheckBox         doAuditsChk;
    protected JCheckBox         doAuditFieldValsChk;
    protected JLabel            lifeSpanLbl;
    protected ValTextField      lifeSpanTxt;
    protected boolean isInitialized = false;

    /**
     *
     */
    public AuditPrefsPanel() {
        super();

        createForm("Preferences", "Audits");

        doAuditsChk = form.getCompById(Specify.hiddenDoAuditPrefName);
        doAuditFieldValsChk = form.getCompById(Specify.hiddenAuditFldUpdatePrefName);
        lifeSpanTxt    = form.getCompById(AuditLogCleanupTask.AUDIT_LIFESPAN_MONTHS_PREF);

        isInitialized = doAuditsChk != null && doAuditFieldValsChk != null && lifeSpanTxt != null;
        if (!isInitialized) {
            UIRegistry.showError("The form is not setup correctly.");
            return;
        }
        setDataIntoUI();
    }

    /**
     * @param prefs
     * @return
     */
    private boolean setDataIntoUI() {
        AppPreferences prefs = AppPreferences.getRemote();
        boolean isUsingPath = true;
        doAuditsChk.setSelected(prefs.getBoolean(Specify.hiddenDoAuditPrefName, true));
        doAuditFieldValsChk.setSelected(prefs.getBoolean(Specify.hiddenAuditFldUpdatePrefName, true));
        lifeSpanTxt.setText(prefs.get(AuditLogCleanupTask.AUDIT_LIFESPAN_MONTHS_PREF, "0"));
        return true;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#isOKToSave()
     */
    @Override
    public boolean isOKToSave() {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs() {
        if (!isInitialized) return;

        AppPreferences prefs = AppPreferences.getRemote();
        super.savePrefs(); // Gets data from form
        prefs.putBoolean(Specify.hiddenDoAuditPrefName, doAuditsChk.isSelected());
        prefs.putBoolean(Specify.hiddenAuditFldUpdatePrefName, doAuditFieldValsChk.isSelected());
        prefs.put(AuditLogCleanupTask.AUDIT_LIFESPAN_MONTHS_PREF, lifeSpanTxt.getText());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getHelpContext()
     */
    @Override
    public String getHelpContext()
    {
        return "PrefsAudits";
    }

}

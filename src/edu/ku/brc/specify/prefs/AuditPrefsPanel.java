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
package edu.ku.brc.specify.prefs;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
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
    protected ValSpinner lifeSpanSpin;
    protected boolean isInitialized = false;

    /**
     *
     */
    public AuditPrefsPanel() {
        super();

        createForm("Preferences", "Audits");

        doAuditsChk = form.getCompById(Specify.hiddenDoAuditPrefName);
        doAuditFieldValsChk = form.getCompById(Specify.hiddenAuditFldUpdatePrefName);
        lifeSpanSpin = form.getCompById(AuditLogCleanupTask.AUDIT_LIFESPAN_MONTHS_PREF);

        isInitialized = doAuditsChk != null && doAuditFieldValsChk != null && lifeSpanSpin != null;
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
        AppPreferences gPrefs = AppPreferences.getGlobalPrefs();
        AppPreferences rPrefs = AppPreferences.getRemote();
        boolean isUsingPath = true;
        doAuditsChk.setSelected(rPrefs.getBoolean(Specify.hiddenDoAuditPrefName, true));
        doAuditFieldValsChk.setSelected(rPrefs.getBoolean(Specify.hiddenAuditFldUpdatePrefName, true));
        lifeSpanSpin.setValue(gPrefs.getInt(AuditLogCleanupTask.AUDIT_LIFESPAN_MONTHS_PREF,0));
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

        AppPreferences gPrefs = AppPreferences.getGlobalPrefs();
        AppPreferences rPrefs = AppPreferences.getRemote();
        boolean doAuditChanged = false;
        Boolean oldDoAudit = rPrefs.getBoolean(Specify.hiddenDoAuditPrefName, true);
        doAuditChanged = oldDoAudit != doAuditsChk.isSelected();
        boolean oldDoFields = rPrefs.getBoolean(Specify.hiddenAuditFldUpdatePrefName, true);
        boolean doFieldsChanged = oldDoFields != doAuditFieldValsChk.isSelected();
        int oldDuration = gPrefs.getInt(AuditLogCleanupTask.AUDIT_LIFESPAN_MONTHS_PREF, 0);
        int duration = lifeSpanSpin.getValue() == null ? 0 : (Integer)lifeSpanSpin.getValue();
        boolean durationChanged = oldDuration != duration;
        super.savePrefs(); // Gets data from form and saves it to prefs.
        //but savePrefs() doesn't work for global prefs??
        if (durationChanged) {
            gPrefs.putInt(AuditLogCleanupTask.AUDIT_LIFESPAN_MONTHS_PREF, duration);
        }
        if (doAuditChanged || doFieldsChanged || durationChanged) {
            UIRegistry.displayInfoMsgDlg(UIRegistry.getResourceString("MiscPrefsPanel.RestartRequired"));
        }
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

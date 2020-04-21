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

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.specify.tools.webportal.BuildSearchIndex2;
import edu.ku.brc.ui.UIRegistry;

import javax.swing.*;

public class WebPortalPrefsPanel extends GenericPrefsPanel implements PrefsSavable, PrefsPanelIFace {
    protected JCheckBox         doProcessWebLinks;
    protected boolean isInitialized = false;

    /**
     *
     */
    public WebPortalPrefsPanel() {
        super();

        createForm("Preferences", "WebPortal");

        doProcessWebLinks = form.getCompById(BuildSearchIndex2.PROCESS_WEBLINKS_PREF_NAME);

        isInitialized = doProcessWebLinks != null;
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
        AppPreferences rPrefs = AppPreferences.getRemote();
        boolean isUsingPath = true;
        doProcessWebLinks.setSelected(rPrefs.getBoolean(BuildSearchIndex2.PROCESS_WEBLINKS_PREF_NAME, BuildSearchIndex2.PROCESS_WEBLINKS_PREF_DEFAULT));
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

        AppPreferences rPrefs = AppPreferences.getRemote();
        super.savePrefs(); // Gets data from form
        rPrefs.putBoolean(BuildSearchIndex2.PROCESS_WEBLINKS_PREF_NAME, doProcessWebLinks.isSelected());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getHelpContext()
     */
    @Override
    public String getHelpContext()
    {
        return "PrefsWebPortal";
    }

}

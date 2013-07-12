/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 24, 2008
 *
 */
public class LoginPrefs extends GenericPrefsPanel
{
    /**
     * 
     */
    public LoginPrefs()
    {
        super();
        
        createUI();
        
        this.hContext = "PrefsLogin";
    }

    /**
     * Create the UI for the panel.
     */
    protected void createUI()
    {
        createForm("Preferences", "LoginPrefs");

        if (formView != null & form != null && form.getUIComponent() != null)
        {
            JCheckBox chkbx = form.getCompById("autologin");
            if (chkbx != null)
            {
                chkbx.setSelected(AppPreferences.getLocalPrefs().getBoolean("login.autologin", false));
            }
            
            JButton btn = form.getCompById("usernames");
            if (btn != null)
            {
                btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    AppPreferences.getLocalPrefs().put("login.username", "");
                    form.getValidator().setHasChanged(true);
                    form.getValidator().dataChanged(null, null, null);
                }
               });
            }
            btn = form.getCompById("databases");
            if (btn != null)
            {
                btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    AppPreferences.getLocalPrefs().put("login.databases_selected", "");
                    AppPreferences.getLocalPrefs().put("login.databases", "");
                    form.getValidator().setHasChanged(true);
                    form.getValidator().dataChanged(null, null, null);
                }
               });
            }
            btn = form.getCompById("servers");
            if (btn != null)
            {
                btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    AppPreferences.getLocalPrefs().put("login.servers", "");
                    form.getValidator().setHasChanged(true);
                    form.getValidator().dataChanged(null, null, null);
                }
               });
            }
        }
    }
}

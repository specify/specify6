/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getChangedFields(java.util.Properties)
     */
    @Override
    public void getChangedFields(Properties changeHash)
    {
        // not nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        super.savePrefs(); // gets data from form
        
        if (formView != null & form != null && form.getUIComponent() != null)
        {
            JCheckBox chkbx = form.getCompById("autologin");
            if (chkbx != null)
            {
                AppPreferences.getLocalPrefs().putBoolean("login.autologin", chkbx.isSelected());
            }
        }
    }
    
    
}

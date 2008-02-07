/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.specify.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.validation.FormValidator;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2007
 *
 */
public class CachePrefs extends GenericPrefsPanel
{
    /**
     * Constructor.
     */
    public CachePrefs()
    {
        createForm("Preferences", "System");
        
        JButton clearCache = (JButton)form.getCompById("clearcache");
        
        clearCache.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                Specify.getCacheManager().clearAll();
                
                // Tell the OK btn a change has occurred and update the OK btn
                FormValidator validator = ((FormViewObj)form).getValidator();
                if (validator != null)
                {
                    validator.setHasChanged(true);
                    validator.wasValidated(null);
                    validator.dataChanged(null, null, null);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getHelpContext()
     */
    @Override
    public String getHelpContext()
    {
        return "PrefsCache";
    }
    
    
}

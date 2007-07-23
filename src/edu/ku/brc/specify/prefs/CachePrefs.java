/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.specify.prefs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ViewFactory;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.validation.FormValidator;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2007
 *
 */
public class CachePrefs extends JPanel implements PrefsSavable, PrefsPanelIFace
{
    private static final Logger log  = Logger.getLogger(CachePrefs.class);

    protected View         formView  = null;
    protected Viewable     form      = null;
    
    /**
     * Constructor.
     */
    public CachePrefs()
    {
        super(new BorderLayout());
        
        String viewName = "System";
        String name     = "Preferences";

        formView = AppContextMgr.getInstance().getView(name, viewName);

        if (formView != null)
        {
            form = ViewFactory.createFormView(null, formView, null, AppPreferences.getRemote(), MultiView.NO_OPTIONS);
            add(form.getUIComponent(), BorderLayout.CENTER);

        } else
        {
            log.error("Couldn't load form with name ["+name+"] Id ["+viewName+"]");
        }  
        
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
     * @see edu.ku.brc.af.prefs.PrefsSavable#savePrefs()
     */
    public void savePrefs()
    {
        if (form.getValidator() == null || form.getValidator().hasChanged())
        {
            form.getDataFromUI();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getValidator()
     */
    public FormValidator getValidator()
    {
        return form.getValidator();
    }

}

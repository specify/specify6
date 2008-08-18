/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.specify.prefs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Apr 29, 2007
 *
 */
public class SystemPrefs extends GenericPrefsPanel
{
    /**
     * Constructor.
     */
    public SystemPrefs()
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
        
        final ValComboBox localeCBX = (ValComboBox)form.getCompById("5");
        localeCBX.getComboBox().setRenderer(new LocaleRenderer());
        localeCBX.setEnabled(false);
        
        SwingWorker workerThread = new SwingWorker()
        {
            protected int inx = -1;
            
            @Override
            public Object construct()
            {
                
                Vector<Locale> locales = new Vector<Locale>();
                Collections.addAll(locales, Locale.getAvailableLocales());
                Collections.sort(locales, new Comparator<Locale>() {
                    public int compare(Locale o1, Locale o2)
                    {
                        return o1.getDisplayName().compareTo(o2.getDisplayName());
                    }
                });
                
                int i = 0;
                String language = AppPreferences.getLocalPrefs().get("locale.lang", Locale.getDefault().getLanguage());
                String country  = AppPreferences.getLocalPrefs().get("locale.country", Locale.getDefault().getCountry());
                String variant  = AppPreferences.getLocalPrefs().get("locale.var", Locale.getDefault().getVariant());
                
                Locale prefLocale = new Locale(language, country, variant);
                
                Locale cachedLocale = Locale.getDefault();
                for (Locale l : locales)
                {
                    try
                    {
                        Locale.setDefault(l);
                        ResourceBundle rb = ResourceBundle.getBundle("resources", l);
                        if (rb.getKeys().hasMoreElements())
                        {
                            if (l.equals(prefLocale))
                            {
                                inx = i;
                            }
                            localeCBX.getComboBox().addItem(l);
                            i++;
                        }
                        
                    } catch (MissingResourceException ex)
                    {
                    }
                }
                Locale.setDefault(cachedLocale);
                
                return null;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public void finished()
            {
                localeCBX.setEnabled(true);
                localeCBX.getComboBox().setSelectedIndex(inx);
                JTextField loadingLabel = (JTextField)form.getCompById("6");
                if (loadingLabel != null)
                {
                    loadingLabel.setText(UIRegistry.getResourceString("LOCALE_RESTART_REQUIRED"));
                }
            }
        };
        
        // start the background task
        workerThread.start();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getHelpContext()
     */
    @Override
    public String getHelpContext()
    {
        return "PrefsSystem";
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        if (form.getValidator() == null || form.getValidator().hasChanged())
        {
            super.savePrefs();
            
            ValComboBox localeCBX = (ValComboBox)form.getCompById("5");
            Locale item = (Locale)localeCBX.getComboBox().getSelectedItem();
            if (item != null)
            {
                if (item.equals(UIRegistry.getPlatformLocale()))
                {
                    AppPreferences.getLocalPrefs().remove("locale.lang");
                    AppPreferences.getLocalPrefs().remove("locale.country");
                    AppPreferences.getLocalPrefs().remove("locale.var");
                    
                    //System.out.println("["+AppPreferences.getLocalPrefs().get("locale.lang", null)+"]");
                    Locale.setDefault(UIRegistry.getPlatformLocale());
                    
                } else
                {
                    if (item.getLanguage() == null)
                    {
                        AppPreferences.getLocalPrefs().remove("locale.lang");
                    } else
                    {
                        AppPreferences.getLocalPrefs().put("locale.lang", item.getLanguage());
                    }
                    
                    if (item.getCountry() == null)
                    {
                        AppPreferences.getLocalPrefs().remove("locale.country");
                    } else
                    {
                        AppPreferences.getLocalPrefs().put("locale.country", item.getCountry());
                    }
                    
                    if (item.getVariant() == null)
                    {
                        AppPreferences.getLocalPrefs().remove("locale.var");
                    } else
                    {
                        AppPreferences.getLocalPrefs().put("locale.var", item.getVariant());
                    }
                }
                try
                {
                    AppPreferences.getLocalPrefs().flush();
                } catch (BackingStoreException ex) {}
            }
        }
    }


    //-----------------------------------------------------------------
    //--
    //-----------------------------------------------------------------
    public class LocaleRenderer extends DefaultListCellRenderer 
    {
        
        public LocaleRenderer() 
        {
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,   // value to display
                                                      int index,      // cell index
                                                      boolean iss,    // is the cell selected
                                                      boolean chf)    // the list and the cell have the focus
        {
            super.getListCellRendererComponent(list, value, index, iss, chf);

            if (value != null)
            {
                setText(((Locale)value).getDisplayName());
            }
            return this;
        }
    }
}

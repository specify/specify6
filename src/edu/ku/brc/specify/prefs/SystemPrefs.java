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

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.prefs.BackingStoreException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JTextField;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValBrowseBtnPanel;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.tasks.subpane.JasperReportsCache;
import edu.ku.brc.ui.IconEntry;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
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
    protected static final String SPECIFY_BG_IMG_PATH = "specify.bg.image";
    protected static final String VERSION_CHECK       = "version_check.auto";
    
    protected static final String USE_WORLDWIND       = "USE.WORLDWIND";
    protected static final String SYSTEM_HasOpenGL    = "SYSTEM.HasOpenGL";
    protected static final String ALWAYS_ASK_COLL     = "ALWAYS.ASK.COLL";
    
    protected AppPreferences remotePrefs = AppPreferences.getRemote();
    protected AppPreferences localPrefs  = AppPreferences.getLocalPrefs();
    
    protected String oldSplashPath       = null;
    
    /**
     * Constructor.
     */
    public SystemPrefs()
    {
        createForm("Preferences", "System");
        
        JButton clearCache = form.getCompById("clearcache");
        
        clearCache.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                clearCache();
            }
        });
        
        ValBrowseBtnPanel browse = form.getCompById("7");
        if (browse != null)
        {
            oldSplashPath = localPrefs.get(SPECIFY_BG_IMG_PATH, null);
            browse.setValue(oldSplashPath, null);
        }
        
        final ValComboBox localeCBX = form.getCompById("5");
        localeCBX.getComboBox().setRenderer(new LocaleRenderer());
        localeCBX.setEnabled(false);
        
        SwingWorker workerThread = new SwingWorker()
        {
            protected int inx = -1;

            @Override
            public Object construct()
            {
                
                List<String> sysLocales = new ArrayList<String>();
                for (Locale l : Locale.getAvailableLocales()) {
                    sysLocales.add(l.toString());
                }
                List<Locale> spLocales = SchemaI18NService.getInstance().getLocalesFromData(SpLocaleContainer.CORE_SCHEMA,
                        AppContextMgr.getInstance().getClassObject(edu.ku.brc.specify.datamodel.Discipline.class).getId());
                Collections.sort(spLocales, new Comparator<Locale>(){
                    public int compare(Locale l1, Locale l2) {
                        return l1.getDisplayName().compareTo(l2.getDisplayName());
                    }
                });

                String language = AppPreferences.getLocalPrefs().get("locale.lang", Locale.getDefault().getLanguage());
                String country  = AppPreferences.getLocalPrefs().get("locale.country", Locale.getDefault().getCountry());
                String variant  = AppPreferences.getLocalPrefs().get("locale.var", Locale.getDefault().getVariant());
                
                Locale prefLocale = new Locale(language, country, variant);
                
                int justLangIndex = -1;
                Locale cachedLocale = Locale.getDefault();
                for (Locale l : spLocales) {
                    try  {
                        boolean isOK = sysLocales.indexOf(l.toString()) != -1;
                        if (isOK) {
                            Locale.setDefault(l);
                            ResourceBundle rb = ResourceBundle.getBundle("resources", l);
                            if (rb.getKeys().hasMoreElements()) {
                                localeCBX.getComboBox().addItem(l);
                                if (l.getLanguage().equals(prefLocale.getLanguage())) {
                                    justLangIndex = localeCBX.getComboBox().getItemCount()-1;
                                }
                                if (l.equals(prefLocale)) {
                                    inx = localeCBX.getComboBox().getItemCount()-1;
                                }
                            }
                        }
                    } catch (MissingResourceException ex)
                    {
                    }
                }

                if (inx == -1 && justLangIndex > -1) {
                    inx = justLangIndex;
                }
                Locale.setDefault(cachedLocale);
                
                return null;
            }
            
            @Override
            public void finished()
            {
                UIValidator.setIgnoreAllValidation("SystemPrefs", true);
                localeCBX.setEnabled(true);
                localeCBX.getComboBox().setSelectedIndex(inx);
                JTextField loadingLabel = form.getCompById("6");
                if (loadingLabel != null)
                {
                    loadingLabel.setText(UIRegistry.getResourceString("LOCALE_RESTART_REQUIRED"));
                }
                UIValidator.setIgnoreAllValidation("SystemPrefs", false);
            }
        };
        
        // start the background task
        workerThread.start();
        
        ValCheckBox chk = form.getCompById("2");
        chk.setValue(localPrefs.getBoolean(VERSION_CHECK, true), "true");
        
    }
    
    /**
     * 
     */
    protected void clearCache()
    {
        final String CLEAR_CACHE = "CLEAR_CACHE";
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setIndeterminate(CLEAR_CACHE, true);
        
        Component dlg = getParent();
        while (dlg != null && !(dlg instanceof JDialog))
        {
            dlg = dlg.getParent();
        }
        Point loc = null;
        if (dlg != null)
        {
            loc = dlg.getLocation();
        }
        
        final JDialog parentDlg = (JDialog)dlg;
        
        Rectangle screenRect = dlg.getGraphicsConfiguration().getBounds();
        parentDlg.setLocation(loc.x, screenRect.height);
        
        javax.swing.SwingWorker<Integer, Integer> backupWorker = new javax.swing.SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                try
                {
                    long startTm = System.currentTimeMillis();
                    
                    Specify.getCacheManager().clearAll();
                    
                    // Tell the OK btn a change has occurred and update the OK btn
                    FormValidator validator = ((FormViewObj)form).getValidator();
                    if (validator != null)
                    {
                        validator.setHasChanged(true);
                        validator.wasValidated(null);
                        validator.dataChanged(null, null, null);
                    }
                    
                    Thread.sleep(Math.max(0, 2000 - (System.currentTimeMillis() -startTm)));
                    
                } catch (Exception ex) {}
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                statusBar.setProgressDone(CLEAR_CACHE);
                
                UIRegistry.clearSimpleGlassPaneMsg();
                
                UIRegistry.displayLocalizedStatusBarText("SystemPrefs.CACHE_CLEARED");
                
                UIHelper.centerWindow(parentDlg);
            }
        };
        
        UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("SystemPrefs.CLEARING_CACHE"), 24);
        backupWorker.execute();
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
            
            ValCheckBox chk = form.getCompById("2");
            localPrefs.putBoolean(VERSION_CHECK, (Boolean)chk.getValue());
            
            ValComboBox localeCBX = form.getCompById("5");
            Locale item = (Locale)localeCBX.getComboBox().getSelectedItem();
            if (item != null)
            {
                if (item.equals(UIRegistry.getPlatformLocale()))
                {
                    localPrefs.remove("locale.lang");
                    localPrefs.remove("locale.country");
                    localPrefs.remove("locale.var");
                    
                    //System.out.println("["+localPrefs.get("locale.lang", null)+"]");
                    Locale.setDefault(UIRegistry.getPlatformLocale());
                    
                } else
                {
                    if (item.getLanguage() == null)
                    {
                        localPrefs.remove("locale.lang");
                    } else
                    {
                        localPrefs.put("locale.lang", item.getLanguage());
                    }
                    
                    if (item.getCountry() == null)
                    {
                        localPrefs.remove("locale.country");
                    } else
                    {
                        localPrefs.put("locale.country", item.getCountry());
                    }
                    
                    if (item.getVariant() == null)
                    {
                        localPrefs.remove("locale.var");
                    } else
                    {
                        localPrefs.put("locale.var", item.getVariant());
                    }
                }
                
                ValBrowseBtnPanel browse = form.getCompById("7");
                if (browse != null)
                {
                    String newSplashPath = browse.getValue().toString();
                    if (newSplashPath != null && (oldSplashPath == null || !oldSplashPath.equals(newSplashPath)))
                    {
                        if (newSplashPath.isEmpty())
                        {
                            resetSplashImage();
                            localPrefs.remove(SPECIFY_BG_IMG_PATH);
                            
                        } else
                        {
                            localPrefs.put(SPECIFY_BG_IMG_PATH, newSplashPath);
                            changeSplashImage();
                        }
                    }
                }
                File cp = JasperReportsCache.getImagePath();
                
                try
                {
                    localPrefs.flush();
                    
                } catch (BackingStoreException ex) {}
            }
        }
    }
    
    /**
     * 
     */
    public static void resetSplashImage()
    {
        IconEntry entry      = IconManager.getIconEntryByName("SpecifySmallSplash");
        IconEntry entryCache = IconManager.getIconEntryByName("SpecifySplashCache");
        entry.setIcon(entryCache.getIcon());
    }
    
    /**
     * 
     */
    public static void changeSplashImage()
    {
        String userSplashIconPath = AppPreferences.getLocalPrefs().get(SPECIFY_BG_IMG_PATH, null);
        if (StringUtils.isNotEmpty(userSplashIconPath))
        {
            IconEntry entry = IconManager.getIconEntryByName("SpecifySmallSplash");
            if (entry != null)
            {
                try
                {   
                    entry.setIcon(new ImageIcon(userSplashIconPath));
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemPrefs.class, ex);
                    ex.printStackTrace();
                }
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

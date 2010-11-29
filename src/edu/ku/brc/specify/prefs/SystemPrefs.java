/* Copyright (C) 2009, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JTextField;

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
import edu.ku.brc.ui.IconEntry;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;

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
    protected static final String ATTCH_PATH          = "attachment.path";
    protected static final String VERSION_CHECK       = "version_check.auto";
    
    protected static final String SEND_STATS          = "usage_tracking.send_stats";
    protected static final String SEND_ISA_STATS      = "usage_tracking.send_isa_stats";
    
    protected static final String USE_WORLDWIND       = "USE.WORLDWIND";
    protected static final String SYSTEM_HasOpenGL    = "SYSTEM.HasOpenGL";
    protected static final String ALWAYS_ASK_COLL     = "ALWAYS.ASK.COLL";

    
    protected AppPreferences remotePrefs = AppPreferences.getRemote();
    protected AppPreferences localPrefs  = AppPreferences.getLocalPrefs();
    
    protected String oldAttachmentPath   = null;
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
        
        browse = form.getCompById("8");
        if (browse != null)
        {
            oldAttachmentPath = localPrefs.get(ATTCH_PATH, null);
            browse.setValue(oldAttachmentPath, null);
            
            browse.getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e)
                {
                    super.focusLost(e);
                    verifyAttachmentPath();
                }
            });
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
                
                int justLangIndex = -1;
                Locale cachedLocale = Locale.getDefault();
                for (Locale l : locales)
                {
                    try
                    {
                        Locale.setDefault(l);
                        ResourceBundle rb = ResourceBundle.getBundle("resources", l);
                        
                        boolean isOK = (l.getLanguage().equals("en") && StringUtils.isEmpty(l.getCountry())) ||
                                       (l.getLanguage().equals("pt") && l.getCountry().equals("PT"));

                        if (isOK && rb.getKeys().hasMoreElements())
                        {
                            if (l.getLanguage().equals(prefLocale.getLanguage()))
                            {
                                justLangIndex = i;
                            }
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
                
                if (inx == -1 && justLangIndex > -1)
                {
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
        
        chk = form.getCompById("3");
        chk.setValue(remotePrefs.getBoolean(SEND_STATS, true), "true");
        
        chk = form.getCompById("9");
        chk.setValue(remotePrefs.getBoolean(SEND_ISA_STATS, true), "true");
        chk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
                if (collection != null)
                {
                    String isaNumber = collection.getIsaNumber();
                    if (StringUtils.isNotEmpty(isaNumber) && !((JCheckBox)e.getSource()).isSelected()){
                        UIRegistry.showLocalizedMsg("ISA_STATS_WARNING");
                    }
                }
            }
         });
        
        // Not sure why the form isn't picking up the pref automatically
        ValCheckBox useWWChk  = form.getCompById(USE_WORLDWIND);
        ValCheckBox hasOGLChk = form.getCompById(SYSTEM_HasOpenGL);
        
        useWWChk.setValue(localPrefs.getBoolean(USE_WORLDWIND, false), null);
        hasOGLChk.setValue(localPrefs.getBoolean(SYSTEM_HasOpenGL, false), null);
        hasOGLChk.setEnabled(false);
        
        //ValCheckBox askCollChk = form.getCompById(ALWAYS_ASK_COLL);
        //askCollChk.setValue(localPrefs.getBoolean(ALWAYS_ASK_COLL, false), null);
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
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
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
    
    /**
     * 
     */
    private void verifyAttachmentPath()
    {
        ValBrowseBtnPanel browse = form.getCompById("8");
        if (browse != null)
        {
            String newAttachmentPath = browse.getValue().toString();
            if (newAttachmentPath != null && 
                StringUtils.isNotEmpty(oldAttachmentPath) && 
                !oldAttachmentPath.equals(newAttachmentPath))
            {
                if (newAttachmentPath.isEmpty())
                {
                    UIRegistry.showLocalizedError("SystemPrefs.NOEMPTY_ATTCH");
                    browse.setValue(oldAttachmentPath, oldAttachmentPath);
                    
                } else if (okChangeAttachmentPath(oldAttachmentPath, newAttachmentPath))
                {
                    if (!oldAttachmentPath.equals(newAttachmentPath))
                    {
                        localPrefs.put(ATTCH_PATH, newAttachmentPath);
                        try
                        {
                            if (AttachmentUtils.getAttachmentManager() == null)
                            {
                                AttachmentUtils.setAttachmentManager(new FileStoreAttachmentManager(new File(newAttachmentPath)));
                            } else
                            {
                                AttachmentUtils.getAttachmentManager().setDirectory(new File(newAttachmentPath));
                            }
                            
                        } catch (IOException ex)
                        {
                            UIRegistry.showLocalizedError("SystemPrefs.ESA");
                        }
                    }
                    
                } else
                {
                    UIRegistry.showLocalizedError("SystemPrefs.DOESNT_EXIST", newAttachmentPath);
                    browse.setValue(oldAttachmentPath, oldAttachmentPath);
                }
            }
        }
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
            
            chk = form.getCompById("3");
            remotePrefs.putBoolean(SEND_STATS, (Boolean)chk.getValue());
            
            chk = form.getCompById("9");
            remotePrefs.putBoolean(SEND_ISA_STATS, (Boolean)chk.getValue());
            
            chk = form.getCompById(USE_WORLDWIND);
            localPrefs.putBoolean(USE_WORLDWIND, (Boolean)chk.getValue());
            
            chk = form.getCompById(SYSTEM_HasOpenGL);
            localPrefs.putBoolean(SYSTEM_HasOpenGL, (Boolean)chk.getValue());
            
            chk = form.getCompById(USE_WORLDWIND);
            localPrefs.putBoolean(USE_WORLDWIND, (Boolean)chk.getValue());  
            
            //chk = form.getCompById(ALWAYS_ASK_COLL);
            //localPrefs.putBoolean(ALWAYS_ASK_COLL, (Boolean)chk.getValue());
            
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
                
                verifyAttachmentPath();
                
                try
                {
                    localPrefs.flush();
                    
                } catch (BackingStoreException ex) {}
            }
        }
    }
    
    /**
     * @param oldPath
     * @param newPath
     * @return
     */
    protected boolean okChangeAttachmentPath(final String oldPath, final String newPath)
    {
        /*if (false)
        {
            File  oldDir = new File(oldPath);
            if (oldDir.exists())
            {
                File origDir = new File(oldPath + File.separator + "originals");
                
                boolean doMoveFiles = false;
                int numFiles = origDir.listFiles().length;
                if (numFiles > 0)
                {
                    Object[] options = { getResourceString("SystemPrefs.MV_FILES"),  //$NON-NLS-1$
                                         getResourceString("CANCEL")  //$NON-NLS-1$
                                       };
                    int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                getLocalizedMessage("SystemPrefs.MV_FILES_MSG"),  //$NON-NLS-1$
                                                                getResourceString("SystemPrefs.ATTCH_TITLE"),  //$NON-NLS-1$
                                                                JOptionPane.YES_NO_OPTION,
                                                                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (userChoice == JOptionPane.YES_OPTION)
                    {
                        doMoveFiles = true;
                    } else
                    {
                        return false;
                    }
                }
                
                if (doMoveFiles)
                {
                    File newDir = new File(newPath);
                    try
                    {
                        AttachmentUtils.getAttachmentManager().setDirectory(newDir);
                        
                        //File newParentDir = new File(newDir.getParent());
                        for (File file : oldDir.listFiles())
                        {
                            if (file.isDirectory())
                            {
                                FileUtils.copyDirectoryToDirectory(file, newDir);
                                
                            } else if (!file.getName().equals(".") && !file.getName().equals(".."))
                            {
                                FileUtils.copyFileToDirectory(file, newDir);
                            }
                        }
                        //FileUtils.copyDirectoryToDirectory(oldDir, newParentDir);
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemPrefs.class, ex);
                        ex.printStackTrace();
                        try
                        {
                            AttachmentUtils.getAttachmentManager().setDirectory(oldDir);
                            
                        } catch (Exception ex2)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemPrefs.class, ex);
                            ex2.printStackTrace();
                        }
                        UIRegistry.showLocalizedError("SystemPrefs.NO_MOVE_ATTCH", newDir.getAbsoluteFile());
                        return false;
                    }
                }
            }
        } else
        {*/
            return AttachmentUtils.isAttachmentDirMounted(new File(newPath));
        //}
        
        //return true;
    }
    
    /**
     * 
     */
    public static void resetSplashImage()
    {
        IconEntry entry      = IconManager.getIconEntryByName("SpecifySplash");
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
            IconEntry entry = IconManager.getIconEntryByName("SpecifySplash");
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

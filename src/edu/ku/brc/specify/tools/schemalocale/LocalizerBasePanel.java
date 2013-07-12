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
package edu.ku.brc.specify.tools.schemalocale;

import java.util.Collections;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.swing.JTextComponentSpellChecker;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 23, 2007
 *
 */
public abstract class LocalizerBasePanel extends JPanel
{
    protected static LocalizableStrFactory       localizableStrFactory;
    
    // Used for Caching the lists
    protected static Vector<LocalizableStrIFace> namesList = new Vector<LocalizableStrIFace>();
    protected static Vector<LocalizableStrIFace> descsList = new Vector<LocalizableStrIFace>();

    // Spell Check Members
    protected boolean          doAutoSpellCheck   = false;
    
    protected SpellDictionary  dictionary         = null;
    protected String           phoneticFileName   = "phonet.en";
    protected String           dictionaryFileName = "english.0.zip";
    protected String           userFileName       = "user.dict";
    protected JTextComponentSpellChecker checker  = null;
    protected SpellDictionary  userDict;
    protected boolean          spellCheckLoaded   = false;
    
    // UI Helpers
    protected boolean          hasChanged         = false;
    private   boolean          ignoreChanges      = false;   // Yes, private on purpose, I want callers to use the set method
    protected JButton          saveBtn            = null;
    protected JMenuItem        saveMenuItem       = null;
    
    /**
     * 
     */
    public LocalizerBasePanel()
    {
        SchemaI18NService.initializeLocales(); // this should have already been done, but the cost is minimal
    }
    
    /**
     * @return the localizableStrFactory
     */
    public static LocalizableStrFactory getLocalizableStrFactory()
    {
        return localizableStrFactory;
    }

    /**
     * @param localizableStrFactory the localizableStrFactory to set
     */
    public static void setLocalizableStrFactory(final LocalizableStrFactory localizableStrFactory)
    {
        LocalizerBasePanel.localizableStrFactory = localizableStrFactory;
    }

    /**
     * 
     */
    public void init()
    {
        /*if (false) // turn off loading the spellchecker
        {
            SwingWorker workerThread = new SwingWorker()
            {
                @Override
                public Object construct()
                {
                    try
                    {
                        File           phoneticFile = XMLHelper.getConfigDir(phoneticFileName);
                        File           file         = XMLHelper.getConfigDir(dictionaryFileName);
                        ZipInputStream zip          = null;
    
                        try
                        {
                            zip = new ZipInputStream(new FileInputStream(file));
    
                        } catch (NullPointerException e)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LocalizerBasePanel.class, e);
                            FileInputStream fin = new FileInputStream(file);
                            zip = new ZipInputStream(fin);
                        }
    
                        zip.getNextEntry();
                        
                        dictionary = new SpellDictionaryHashMap(new BufferedReader(new InputStreamReader(zip)), new FileReader(phoneticFile));
                        File userDictFile = new File(userFileName);
                        if (!userDictFile.exists())
                        {
                            userDictFile.createNewFile();
                        }
                        checker  = new JTextComponentSpellChecker(dictionary);
                        userDict = new SpellDictionaryHashMap(userDictFile, phoneticFile);
                        checker.setUserDictionary(userDict);
                        
                    } catch (MalformedURLException e)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LocalizerBasePanel.class, e);
                        e.printStackTrace();
    
                    } catch (FileNotFoundException e)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LocalizerBasePanel.class, e);
                        e.printStackTrace();
    
                    } catch (IOException e)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LocalizerBasePanel.class, e);
                        e.printStackTrace();
                    }
                    return null;
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public void finished()
                {
                    spellCheckLoaded = true;
                    enableSpellCheck();
                }
            };
            
            // start the background task
            workerThread.start();
        }*/
    }
    
    /**
     * 
     */
    protected void enableSpellCheck()
    {
        // to be overridden
    }

    
    /**
     * @param displayName
     * @return
     */
    protected Locale getLocaleByName(final String displayName)
    {
        for (Locale locale : SchemaI18NService.getInstance().getLocales())
        {
            if (locale.getDisplayName().equals(displayName))
            {
                return locale;
            }
        }
        return null;
    }
    
    /**
     * @return
     */
    protected Locale chooseNewLocale(final Vector<Locale> inUseLocales)
    {
        Vector<DisplayLocale> list = new Vector<DisplayLocale>();
        for (Locale l : inUseLocales)
        {
            list.add(new DisplayLocale(l));
        }
        Collections.sort(list);
        
        ToggleButtonChooserDlg<DisplayLocale> dlg = new ToggleButtonChooserDlg<DisplayLocale>((java.awt.Dialog)null, 
                                                   "CHOOSE_LOCALE", list, ToggleButtonChooserPanel.Type.RadioButton);
        dlg.setUseScrollPane(true);
        dlg.setVisible(true);
        
        if (!dlg.isCancelled())
        {
            return dlg.getSelectedObject().getLocale();
        }
        return null;
    }
    
    /**
     * @param lndi
     * @param text
     */
    public static boolean setDescStrForCurrLocale(final LocalizableItemIFace lndi, final String text)
    {
        LocalizableStrIFace desc = getDescForCurrLocale(lndi);
        if (desc == null)
        {
            if (StringUtils.isNotEmpty(text))
            {
                desc =  localizableStrFactory.create(text, SchemaI18NService.getCurrentLocale());
                lndi.addDesc(desc);
                return true;
            }
        } else
        {
            if (!text.equals(desc.getText()))
            {
                desc.setText(text);
                //System.out.println("Setting Desc text ["+text+"] "+((SpLocaleItemStr)desc).getId());
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param lndi
     * @return
     */
    public static String getDescStrForCurrLocale(final LocalizableItemIFace lndi)
    {
        LocalizableStrIFace desc = getDescForCurrLocale(lndi);
        if (desc == null)
        {
            //desc = localizableStrFactory.create("", currLocale);
            //lndi.addDesc(desc);
            //return desc.getText();
            return "";
        }
        //System.out.println("Getting Desc text ["+desc.getText()+"] "+((SpLocaleItemStr)desc).getId());
        return desc.getText();
    }
    
    /**
     * @param lndi
     * @return
     */
    public static LocalizableStrIFace getDescForCurrLocale(final LocalizableItemIFace lndi)
    {
        lndi.fillDescs(descsList);
        for (LocalizableStrIFace d : descsList)
        {
            if (d.isLocale(SchemaI18NService.getCurrentLocale()))
            {
                return d;
            }
        }
        return null;
    }
    
    /**
     * @param lndi
     * @param text
     */
    public static boolean setNameDescStrForCurrLocale(final LocalizableItemIFace lndi, final String text)
    {
        LocalizableStrIFace nameDesc = getNameDescForCurrLocale(lndi);
        if (nameDesc == null)
        {
            if (StringUtils.isNotEmpty(text))
            {
                nameDesc = localizableStrFactory.create(text, SchemaI18NService.getCurrentLocale());
                lndi.addName(nameDesc);
                return true;
            }
        }  else
        {
            if (!text.equals(nameDesc.getText()))
            {
                nameDesc.setText(text);
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param lndi
     * @return
     */
    public static String getNameDescStrForCurrLocale(final LocalizableItemIFace lndi)
    {
        LocalizableStrIFace nameDesc = getNameDescForCurrLocale(lndi);
        if (nameDesc == null)
        {
            //nameDesc = localizableStrFactory.create("", SchemaI18NService.getCurrentLocale());
            //lndi.addName(nameDesc);
            return "";
            
        }
        //System.out.println("Getting Name text ["+nameDesc.getText()+"] "+((SpLocaleItemStr)nameDesc).getId());
        return nameDesc.getText();
    }
    
    /**
     * @param lndi
     * @return
     */
    public static LocalizableStrIFace getNameDescForCurrLocale(final LocalizableItemIFace lndi)
    {
        lndi.fillNames(namesList);
        for (LocalizableStrIFace n : namesList)
        {
            if (n.isLocale(SchemaI18NService.getCurrentLocale()))
            {
                return n;
            }
        }
        return null;
    }
    
    /**
     * @return
     */
    public boolean isIgnoreChanges()
    {
        return ignoreChanges;
    }

    /**
     * @param ignoreChanges
     */
    public void setIgnoreChanges(boolean ignoreChanges)
    {
        this.ignoreChanges = ignoreChanges;
    }

    /**
     * @param changed
     */
    protected void setHasChanged(final boolean changed)
    {
        if (!ignoreChanges)
        {
            hasChanged = changed;
            if (saveBtn != null)
            {
                saveBtn.setEnabled(changed);
            }
            if (saveMenuItem != null)
            {
                saveMenuItem.setEnabled(changed);
            }
        }
    }
    
    /**
     * @return
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }
    
    /**
     * @param saveBtn the saveBtn to set
     */
    public void setSaveBtn(JButton saveBtn)
    {
        this.saveBtn = saveBtn;
    }

    /**
     * @param saveMenuItem the saveMenuItem to set
     */
    public void setSaveMenuItem(JMenuItem saveMenuItem)
    {
        this.saveMenuItem = saveMenuItem;
    }

    /**
     * @return
     */
    public JMenuItem getSaveMenuItem()
    {
        return saveMenuItem;
    }

    /**
     * @param newLocale
     */
    public abstract void localeChanged(final Locale newLocale);
    
}

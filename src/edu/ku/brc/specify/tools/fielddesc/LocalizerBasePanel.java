/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.fielddesc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Vector;
import java.util.zip.ZipInputStream;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.swing.JTextComponentSpellChecker;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIRegistry;

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
    protected static Locale    currLocale;
    protected Vector<Locale>   locales            = new Vector<Locale>();
    
    protected Vector<JCheckBoxMenuItem> localeMenuItems   = new Vector<JCheckBoxMenuItem>();
    
    protected boolean          doAutoSpellCheck   = false;
    
    protected SpellDictionary  dictionary         = null;
    protected String           phoneticFileName   = "phonet.en";
    protected String           dictionaryFileName = "english.0.zip";
    protected String           userFileName       = "user.dict";
    protected JTextComponentSpellChecker checker  = null;
    protected SpellDictionary  userDict;
    
    protected boolean          hasChanged         = false;
    protected JButton          saveBtn            = null;
    protected JMenuItem        saveMenuItem       = null;
    
    static
    {
        Locale defLocale = Locale.getDefault();
        currLocale       = new Locale(defLocale.getLanguage(), "", "");
    }
    
    /**
     * 
     */
    public LocalizerBasePanel()
    {
        for (Locale locale : Locale.getAvailableLocales())
        {
            if (StringUtils.isEmpty(locale.getCountry()))
            {
                locales.add(locale);
            }
        }
    }
    
    /**
     * 
     */
    public void init()
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
            e.printStackTrace();

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    
    /**
     * @param displayName
     * @return
     */
    protected Locale getLocaleByName(final String displayName)
    {
        for (Locale locale : locales)
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
        ToggleButtonChooserDlg<DisplayLocale> dlg = new ToggleButtonChooserDlg<DisplayLocale>(null, 
                                                   "Choose Locale", list, ToggleButtonChooserPanel.Type.RadioButton);
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
    public static boolean setDescStrForCurrLocale(final LocalizableNameDescIFace lndi, final String text)
    {
        Desc  desc = getDescForCurrLocale(lndi);
        if (desc == null)
        {
            desc = new Desc(text, currLocale);
            lndi.getDescs().add(desc);
        } else
        {
            if (!text.equals(desc.getText()))
            {
                desc.setText(text);
            }
        }
        return false;
    }
    
    /**
     * @param lndi
     * @return
     */
    public static String getDescStrForCurrLocale(final LocalizableNameDescIFace lndi)
    {
        Desc desc = getDescForCurrLocale(lndi);
        if (desc == null)
        {
            desc = new Desc("", currLocale);
            lndi.getDescs().add(desc);
        }
        return desc.getText();
    }
    
    /**
     * @param lndi
     * @return
     */
    public static Desc getDescForCurrLocale(final LocalizableNameDescIFace lndi)
    {
        for (Desc d : lndi.getDescs())
        {
            if (d.isLocale(currLocale))
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
    public static boolean setNameDescStrForCurrLocale(final LocalizableNameDescIFace lndi, final String text)
    {
        Name nameDesc = getNameDescForCurrLocale(lndi);
        if (nameDesc == null)
        {
            nameDesc = new Name(text, currLocale);
            lndi.getNames().add(nameDesc);
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
    public static String getNameDescStrForCurrLocale(final LocalizableNameDescIFace lndi)
    {
        Name nameDesc = getNameDescForCurrLocale(lndi);
        if (nameDesc == null)
        {
            nameDesc = new Name("", currLocale);
            lndi.getNames().add(nameDesc);
        }
        return nameDesc.getText();
    }
    
    /**
     * @param lndi
     * @return
     */
    public static Name getNameDescForCurrLocale(final LocalizableNameDescIFace lndi)
    {
        for (Name n : lndi.getNames())
        {
            if (n.isLocale(currLocale))
            {
                return n;
            }
        }
        return null;
    }
    
    /**
     * @param lang
     * @param country
     * @param variant
     * @return
     */
    public static String makeLocaleKey(final String lang, final String country, final String variant)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(lang);
        sb.append(StringUtils.isNotEmpty(country) ? ("_" + country) : "");
        sb.append(StringUtils.isNotEmpty(variant) ? ("_" + variant) : "");
        return sb.toString();
    }
    
    /**
     * @param locale
     * @return
     */
    protected static String makeLocaleKey(final Locale locale)
    {
        return makeLocaleKey(locale.getLanguage(), locale.getCountry(), locale.getVariant());
    }
    
    /**
     * @param changed
     */
    protected void setHasChanged(final boolean changed)
    {
        hasChanged = changed;
        if (saveBtn != null)
        {
            saveBtn.setEnabled(true);
        }
        if (saveMenuItem != null)
        {
            saveMenuItem.setEnabled(true);
        }
    }
    
    public boolean hasChanged()
    {
        return hasChanged;
    }
    
    /**
     * @return the currLocale
     */
    public static Locale getCurrLocale()
    {
        return currLocale;
    }

    /**
     * @param currLocale the currLocale to set
     */
    public static void setCurrLocale(Locale currLocale)
    {
        LocalizerBasePanel.currLocale = currLocale;
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
    protected JMenu getLocaleMenu(final ActionListener al)
    {
        JMenu menu = new JMenu("Locale");
        for (Locale locale : locales)
        {
            if (locale.getLanguage().equals("en") || locale.getLanguage().equals("de"))
            {
                JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(locale.getDisplayName());
                localeMenuItems.add(cbmi);
                menu.add(cbmi);
                cbmi.addActionListener(al);
            }
        }
        menu.addSeparator();
        for (Locale locale : locales)
        {
            if (!locale.getLanguage().equals("en") && !locale.getLanguage().equals("de"))
            {
                JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(locale.getDisplayName());
                localeMenuItems.add(cbmi);
                menu.add(cbmi);
                cbmi.addActionListener(al);
            }
        }
        return menu;
    }
    
    /**
     * @param newLocale
     */
    public void checkLocaleMenu(final Locale newLocale)
    {
        for (JCheckBoxMenuItem cbmi : localeMenuItems)
        {
            if (cbmi.getText().equals(newLocale.getDisplayName()))
            {
                cbmi.setSelected(true);
            } else
            {
                cbmi.setSelected(false);     
            }
        }
    }
    
    /**
     * @param frame
     * @return
     */
    public JMenu getLocaleMenu(final JFrame frame)
    {
        JMenu localeMenu = getLocaleMenu(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                
                currLocale = getLocaleByName(((JCheckBoxMenuItem)e.getSource()).getText());
                checkLocaleMenu(currLocale);

                UIRegistry.pushWindow(frame);
                localeChanged(currLocale.getDisplayName());
                UIRegistry.popWindow(frame);
            }  
        });
        checkLocaleMenu(currLocale);
        return localeMenu;
    }
    
    public abstract void localeChanged(final String newLocaleName);
    
    //-------------------------------------------------------------
    //-- Inner Classes
    //-------------------------------------------------------------
    class DisplayLocale
    {
        protected Locale locale;

        /**
         * @param locale
         */
        public DisplayLocale(final Locale locale)
        {
            super();
            this.locale = locale;
        }

        /**
         * @return the locale
         */
        public Locale getLocale()
        {
            return locale;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return locale.getDisplayName();
        }
    }
}

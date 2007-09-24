/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.fielddesc;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.swing.JTextComponentSpellChecker;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 23, 2007
 *
 */
public class LocalizableBaseApp extends JFrame
{
    protected Locale           currLocale         = Locale.getDefault();
    protected Vector<Locale>   locales            = new Vector<Locale>();
    
    protected Vector<JCheckBoxMenuItem> localeMenuItems   = new Vector<JCheckBoxMenuItem>();
    
    protected boolean          doAutoSpellCheck   = true;
    
    protected SpellDictionary  dictionary         = null;
    protected String           phoneticFileName   = "phonet.en";
    protected String           dictionaryFileName = "english.0.zip";
    protected String           userFileName       = "user.dict";
    protected JTextComponentSpellChecker checker  = null;
    protected SpellDictionary  userDict;
    
    protected boolean          hasChanged         = false;
    protected JMenuItem        saveMenuItem       = null;
    
    protected String           appName             = "";
    protected String           appVersion          = "";
    protected String           appBuildVersion     = "";
    

    /**
     * 
     */
    public LocalizableBaseApp()
    {
        Locale defLocale = Locale.getDefault();
        currLocale       = new Locale(defLocale.getLanguage(), "", "");
        
        for (Locale locale : Locale.getAvailableLocales())
        {
            if (StringUtils.isEmpty(locale.getCountry()))
            {
                locales.add(locale);
            }
        }
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
    protected void checkLocaleMenu(final Locale newLocale)
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
        ToggleButtonChooserDlg<DisplayLocale> dlg = new ToggleButtonChooserDlg<DisplayLocale>(this, "Choose Locale", list, ToggleButtonChooserPanel.Type.RadioButton);
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
    protected void setDescStrForCurrLocale(final LocalizableNameDescIFace lndi, final String text)
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
                setHasChanged(true);
            }
        }
    }
    
    /**
     * @param lndi
     * @return
     */
    protected String getDescStrForCurrLocale(final LocalizableNameDescIFace lndi)
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
    protected Desc getDescForCurrLocale(final LocalizableNameDescIFace lndi)
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
    protected void setNameDescStrForCurrLocale(final LocalizableNameDescIFace lndi, final String text)
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
                setHasChanged(true);
            }
        }
    }
    
    /**
     * @param lndi
     * @return
     */
    protected String getNameDescStrForCurrLocale(final LocalizableNameDescIFace lndi)
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
    protected Name getNameDescForCurrLocale(final LocalizableNameDescIFace lndi)
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
    protected String makeLocaleKey(final String lang, final String country, final String variant)
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
    protected String makeLocaleKey(final Locale locale)
    {
        return makeLocaleKey(locale.getLanguage(), locale.getCountry(), locale.getVariant());
    }
    
    /**
     * @param changed
     */
    protected void setHasChanged(final boolean changed)
    {
        hasChanged = changed;
        
        if (saveMenuItem != null)
        {
            saveMenuItem.setEnabled(hasChanged);
        }
    }
    
    /**
     * @return the currLocale
     */
    public Locale getCurrLocale()
    {
        return currLocale;
    }

    /**
     * @param currLocale the currLocale to set
     */
    public void setCurrLocale(Locale currLocale)
    {
        this.currLocale = currLocale;
    }

    /**
     * Checks to see if cache has changed before exiting.
     */
    protected void doAbout()
    {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel iconLabel = new JLabel(IconManager.getIcon("SpecifyLargeIcon"));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 8));
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(new JLabel("<html>"+appName+" " + appVersion + 
                "<br><br>Biodiversity Research Center<br>University of Kansas<br>Lawrence, KS  USA 66045<br><br>" + 
                "www.specifysoftware.org<br>specify@ku.edu<br><br>" + 
                "<p>The Specify Software Project is<br>"+
                "funded by the Biological Databases<br>"+
                "and Informatics Program of the<br>"+
                "U.S. National Science Foundation <br>(Award DBI-0446544)</P><br>" +
                "Build: " + appBuildVersion + 
                "</html>"), BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(6,6,0,6));
        CustomDialog aboutDlg = new CustomDialog(this, getResourceString("About") + " " +appName, true, CustomDialog.OK_BTN, panel);
        aboutDlg.setOkLabel(getResourceString("Close"));
        UIHelper.centerAndShow(aboutDlg);
    }
    
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

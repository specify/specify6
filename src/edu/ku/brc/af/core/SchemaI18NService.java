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
package edu.ku.brc.af.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 *
 * @code_status Beta
 *
 * Created Date: Oct 3, 2007
 *
 */
public abstract class SchemaI18NService
{
    public static final String factoryName = "edu.ku.brc.af.core.SchemaI18NService"; //$NON-NLS-1$
    
    protected static SchemaI18NService instance        = null;
    protected static Locale            currentLocale;
    protected static String[]          priorityLocales = {"en", "US", null,
                                                          "da", "DK", null,
                                                          "de", "DE", null,
                                                          "pt", "PT", null,
                                                          "pt", "BR", null,
                                                          "sv", "SE", null,
                                                          }; 
    
    
    // Locale Data Members
    protected Vector<Locale>            locales           = new Vector<Locale>();
    protected Vector<JCheckBoxMenuItem> localeMenuItems   = new Vector<JCheckBoxMenuItem>();

    static
    {
        Locale defLocale = Locale.getDefault();
        currentLocale    = new Locale(defLocale.getLanguage(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    
    /**
     * Returns the instance of the AppContextMgr.
     * @return the instance of the AppContextMgr.
     */
    public static SchemaI18NService getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryNameStr = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (factoryNameStr != null) 
        {
            try 
            {
                instance = (SchemaI18NService)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaI18NService.class, e);
                InternalError error = new InternalError("Can't instantiate SchemaI18NService factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        throw new RuntimeException("The System porpoerty ["+factoryName+"] has not been set up!"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return the currentLocale
     */
    public static Locale getCurrentLocale()
    {
        return currentLocale;
    }

    /**
     * @param currentLocale the currentLocale to set
     */
    public static void setCurrentLocale(Locale currentLocale)
    {
        SchemaI18NService.currentLocale = currentLocale;
    }
    
    
    /**
     * Only Loads the Locales with empty Country and Variant
     */
    public static void initializeLocales()
    {
        SchemaI18NService srv = getInstance();
        if (srv != null)
        {
            Vector<Locale> locs = srv.getLocales();
            if (locs.size() == 0)
            {
                for (Locale locale : Locale.getAvailableLocales())
                {
                    if (StringUtils.isNotEmpty(locale.getCountry()))
                    {
                        locs.add(locale);
                    }
                }
                Collections.sort(locs, new Comparator<Locale>() {
                    public int compare(Locale o1, Locale o2)
                    {
                        return o1.getDisplayName().compareTo(o2.getDisplayName());
                    }
                });
            }
        }
    }
    
    /**
     * @return the list of locales that have been localized or at least created and store in
     * the database or XML.
     * @param schemaType the type (Core or WB)
     * @param disciplineId the Discipline
     * @return a list of available already Locales
     */
    public abstract List<Locale> getLocalesFromData(Byte schemaType, int disciplineId);
    
    /**
     * Method for loading the Locale String from a persistent store into the DBTableInfo classes.
     * @param schemaType the type of schema 'core' or 'workbench'
     * @param disciplineId the discipline of the schema
     * @param mgr the DBTableIDMgr to use (could be the main one or the workbench)
     * @param locale the Locale to load
     */
    public abstract void loadWithLocale(final Byte schemaType, 
                                        final int  disciplineId,
                                        final DBTableIdMgr mgr, 
                                        final Locale locale);

    //----------------------------------------------------------------------
    //-- Non-Static Methods
    //----------------------------------------------------------------------

    /**
     * @param lang
     * @param country
     * @param variant
     * @return
     */
    public Locale getLocaleByLangCountry(final String lang, 
                                         final String country,
                                         final String variant)
    {
        if (lang != null)
        {
            for (Locale l : locales)
            {
                if (l.getLanguage().equals(lang))
                {
                    if (country == null && variant == null)
                    {
                        return l;
                    }
                    
                    if (l.getCountry() != null && l.getCountry().equals(country))
                    {
                        if (variant == null)
                        {
                            return l;
                        }
                        
                        if (l.getVariant() != null && l.getVariant().equals(variant))
                        {
                            return l;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * @param includeSepLocale
     * @return
     */
    public Vector<Locale> getStdLocaleList(final boolean includeSepLocale)
    {
        /*for (Locale l : locales)
        {
            System.out.println(String.format("%s - %s, %s, %s", l.getDisplayName(), l.getLanguage(), l.getCountry(), l.getVariant()));
        }*/
        
        Vector<Locale> freqLocales = new Vector<Locale>();
        int i = 0;
        while (i < priorityLocales.length)
        {
            String lang = priorityLocales[i++];
            String ctry = priorityLocales[i++];
            String vari = priorityLocales[i++];
            Locale l = getLocaleByLangCountry(lang, ctry, vari);
            if (l != null)
            {
                freqLocales.add(l);
            }
        }
        HashSet<String> freqSet = new HashSet<String>();
        for (Locale l : freqLocales)
        {
            freqSet.add(l.getDisplayName());
        }
        Vector<Locale>  stdLocaleList = new Vector<Locale>();
        for (Locale l : locales)
        {
            if (!freqSet.contains(l.getDisplayName()))
            {
                stdLocaleList.add(l);
            }
        }
        
        if (includeSepLocale)
        {
            stdLocaleList.insertElementAt(new Locale("-", "-", "-"), 0);
        }
        
        for (i=freqLocales.size()-1;i>-1;i--)
        {
            stdLocaleList.insertElementAt(freqLocales.get(i), 0);
        }
        return stdLocaleList;
    }
    
    /**
     * @return the locales the vector of available language locales
     */
    public Vector<Locale> getLocales()
    {
        return locales;
    }

    /**
     * Create a Locale menu with all the language locales (no Country or variant).
     * @return
     */
    public JMenu createLocaleMenu(final ActionListener al)
    {
        JMenu menu = new JMenu("Locale"); //$NON-NLS-1$
        JMenuItem m = new JMenuItem("Choose Locale"); //$NON-NLS-1$
        m.addActionListener(al);
        menu.add(m);
        return menu;
    }
    
    
    /**
     * Checks (selects) the MenuItem of the current locale.
     */
    public void checkCurrentLocaleMenu()
    {
        for (JCheckBoxMenuItem cbmi : localeMenuItems)
        {
            if (cbmi.getText().equals(currentLocale.getDisplayName()))
            {
                cbmi.setSelected(true);
            } else
            {
                cbmi.setSelected(false);     
            }
        }
    }
    
    
    /**
     * Return a locale by name.
     * @param displayName the name in the menu's display
     * @return the locale or null
     */
    public Locale getLocaleByName(final String displayName)
    {
        // Need to use binarySearch
        for (Locale locale : locales)
        {
            if (locale.getDisplayName().equals(displayName))
            {
                return locale;
            }
        }
        return null;
    }
    
    private class LocaleDlgItem  {
    	private final Locale locale;
    	public LocaleDlgItem(Locale locale) {
    		this.locale = locale;
    	}
    	
    	/**
    	 * @return
    	 */
    	public Locale getLocale() {
    		return locale;
    	}

		@Override
		public String toString() {
			return locale.getDisplayName();
		}
    	
    	
    }
    /**
     * Creates a menu with the locate and a property listener. A property event is sent with the property
     * name set to "locale" with the current locale passed as the old value and the new locale as the new value.
     * @param frame the frame is passed in so the property listner can push and pop the frame before and after the listener is called.
     * @return the menu
     */
    public JMenu createLocaleMenu(final JFrame frame, 
                                  final PropertyChangeListener pcl)
    {
        JMenu localeMenu = createLocaleMenu(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                
            	List<LocaleDlgItem> dlgItems = new ArrayList<LocaleDlgItem>();
            	for (Locale l: getStdLocaleList(true)) {
            		if (!l.getLanguage().equals("-")) {           		
            			dlgItems.add(new LocaleDlgItem(l));
            		}
            	}
            	ChooseFromListDlg<LocaleDlgItem> localeDlg = new ChooseFromListDlg<LocaleDlgItem>(frame, "Choose a Locale", dlgItems);
        		UIHelper.centerAndShow(localeDlg);
        		LocaleDlgItem selected = localeDlg.getSelectedObject();
        		if (!localeDlg.isCancelled() && selected != null) {
        			Locale newLocale = selected.getLocale();
        			checkCurrentLocaleMenu();
        			UIRegistry.pushWindow(frame);
        			pcl.propertyChange(new PropertyChangeEvent(this, "locale", currentLocale, newLocale)); //$NON-NLS-1$
        			UIRegistry.popWindow(frame);
        		}
            }  
        });
        checkCurrentLocaleMenu();
        return localeMenu;
    }
}

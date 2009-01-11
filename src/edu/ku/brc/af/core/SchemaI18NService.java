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

package edu.ku.brc.af.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.specify.tools.schemalocale.DisplayLocale;
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
    protected static String[]          priorityLocales = {"en", "de", "sv", "pt", "da"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    
    
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
                    if (StringUtils.isEmpty(locale.getCountry()))
                    {
                        locs.add(locale);
                    }
                }
            }
        }
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
        
        Hashtable<String, Boolean> priorityHash = new Hashtable<String, Boolean>();
        for (String ps : priorityLocales)
        {
            priorityHash.put(ps, true);
        }
        
        Vector<DisplayLocale> dispLocales = new Vector<DisplayLocale>();
        
        for (Locale locale : locales)
        {
            dispLocales.add(new DisplayLocale(locale));
        }
        Collections.sort(dispLocales);
        
        for (DisplayLocale dspLocale : dispLocales)
        {
            Locale locale = dspLocale.getLocale();
            if (priorityHash.get(locale.getLanguage()) != null)
            {
                JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(locale.getDisplayName());
                localeMenuItems.add(cbmi);
                menu.add(cbmi);
                cbmi.addActionListener(al);
            }
        }
        menu.addSeparator();
        for (DisplayLocale dspLocale : dispLocales)
        {
            Locale locale = dspLocale.getLocale();
            if (priorityHash.get(locale.getLanguage()) == null)
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
     * Return a lcoale by name.
     * @param displayName the name in the menu's display
     * @return the locale or null
     */
    public Locale getLocaleByName(final String displayName)
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
                Locale newLocale = getLocaleByName(((JCheckBoxMenuItem)e.getSource()).getText());
                checkCurrentLocaleMenu();

                UIRegistry.pushWindow(frame);
                pcl.propertyChange(new PropertyChangeEvent(this, "locale", currentLocale, newLocale)); //$NON-NLS-1$
                UIRegistry.popWindow(frame);
            }  
        });
        checkCurrentLocaleMenu();
        return localeMenu;
    }
}

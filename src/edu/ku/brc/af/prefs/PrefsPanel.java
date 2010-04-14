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
package edu.ku.brc.af.prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Apr 9, 2010
 *
 */
public class PrefsPanel extends GenericPrefsPanel
{
    public enum CompType {eSep, eCheckbox, eComboBox};
    
    protected AppPreferences              appPrefs;
    protected boolean                     doLocalPrefs;
    protected HashMap<String, JComponent> fieldHash = new HashMap<String, JComponent>();
    protected ArrayList<ItemInfo>         items     = new ArrayList<ItemInfo>();
    
    protected FormValidator               formValidator = new FormValidator(null);
    
    /**
     * 
     */
    public PrefsPanel(final boolean doLocalPrefs)
    {
        super();
        appPrefs = doLocalPrefs ? AppPreferences.getLocalPrefs() : AppPreferences.getRemote();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#createForm(java.lang.String, java.lang.String)
     */
    @Override
    public void createForm(final String viewSetName, 
                           final String viewName)
    {
        CellConstraints cc     = new CellConstraints();
        String          rowDef = UIHelper.createDuplicateJGoodiesDef("p", "4px", items.size());
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("p,2px,f:p:g", rowDef), this);
        
        int y = 1;
        for (ItemInfo item : items)
        {
            if (item.getCompType() == CompType.eSep)
            {
                pb.addSeparator(item.getTitle(), cc.xyw(1, y, 3));
                y += 2;
                
            } else if (item.getCompType() == CompType.eCheckbox)
            {
                JCheckBox cb = UIHelper.createCheckBox(item.getTitle());
                item.setComp(cb);
                pb.add(UIHelper.createLabel(" "), cc.xy(1, y));
                pb.add(cb, cc.xy(3, y));
                y += 2;
                
                cb.setSelected(appPrefs.getBoolean(item.getPrefName(), (Boolean)item.getDefaultVal()));
                
                formValidator.createValidator(cb, UIValidator.Type.Changed);
                
            } else if (item.getCompType() == CompType.eComboBox)
            {
                JComboBox cb = UIHelper.createComboBox((Object[])item.getItemsTitles());
                item.setComp(cb);
                pb.add(UIHelper.createFormLabel(item.getTitle()), cc.xy(1, y));
                pb.add(cb, cc.xy(3, y));
                y += 2;
                
                cb.setSelectedIndex(appPrefs.getInt(item.getPrefName(), (Integer)item.getDefaultVal()));
                
                formValidator.createValidator(cb, UIValidator.Type.Changed);
            }
        }
        
        pb.setDefaultDialogBorder();
    }
    
    /**
     * @param title
     * @param prefName
     * @param cls
     */
    public void add(final CompType compType, final String title, final String prefName, final Class<?> cls, final Object defVal)
    {
        items.add(new ItemInfo(compType, title, prefName, cls, defVal));
    }
    
    /**
     * @param title
     * @param prefName
     * @param cls
     */
    public void add(final CompType compType, final String title, final String prefName, final Class<?> cls, final String[] titles, final Object defVal)
    {
        items.add(new ItemInfo(compType, title, prefName, cls, titles, defVal));
    }
    
    /**
     * @param title
     * @param prefName
     * @param cls
     */
    public void add(final String title)
    {
        items.add(new ItemInfo(CompType.eSep, title));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getValidator()
     */
    @Override
    public FormValidator getValidator()
    {
        return formValidator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#isFormValid()
     */
    @Override
    public boolean isFormValid()
    {
        return getValidator() != null ? getValidator().getState() == UIValidatable.ErrorType.Valid : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#isOKToLoad()
     */
    @Override
    public boolean isOKToLoad()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsSavable#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        for (ItemInfo item : items)
        {
            if (item.getCompType() == CompType.eCheckbox)
            {
                JCheckBox cb = (JCheckBox)item.getComp();
                appPrefs.putBoolean(item.getPrefName(), cb.isSelected());
                
            } else if (item.getCompType() == CompType.eComboBox)
            {
                JComboBox cb = (JComboBox)item.getComp();
                appPrefs.putInt(item.getPrefName(), cb.getSelectedIndex());
            }
        }
        
        try
        {
            appPrefs.flush();
        } catch (BackingStoreException e)
        {
            e.printStackTrace();
        }
    }
    
    //-------------------------------------------------------------------
    //
    //-------------------------------------------------------------------
    class ItemInfo 
    {
        CompType compType;
        String   title;
        String   prefName;
        Class<?> clazz;
        boolean  isSep = false;
        Object   defaultVal;
        
        String[] itemTitles = null;
        
        JComponent comp = null;
        
        /**
         * @param title
         * @param prefName
         * @param clazz
         */
        public ItemInfo(CompType compType,
                        String title, 
                        String prefName, 
                        Class<?> clazz,
                        Object defaultVal)
        {
            super();
            this.compType = compType;
            this.title = title;
            this.prefName = prefName;
            this.clazz = clazz;
            this.defaultVal = defaultVal;
        }
        
        /**
         * @param title
         * @param prefName
         * @param clazz
         */
        public ItemInfo(CompType compType,
                        String title, 
                        String prefName, 
                        Class<?> clazz,
                        String[] itemTitles,
                        Object defaultVal)
        {
            super();
            this.compType   = compType;
            this.title      = title;
            this.prefName   = prefName;
            this.clazz      = clazz;
            this.itemTitles = itemTitles;
            this.defaultVal = defaultVal;
        }
        
        /**
         * @param title
         */
        public ItemInfo(CompType compType,
                        String title)
        {
            super();
            this.compType = compType;
            this.title    = title;
            this.prefName = null;
            this.clazz    = null;
            this.isSep    = true;
        }
        
        /**
         * @return the title
         */
        public String getTitle()
        {
            return title;
        }
        
        /**
         * @return the prefName
         */
        public String getPrefName()
        {
            return prefName;
        }
        
        /**
         * @return the clazz
         */
        public Class<?> getClazz()
        {
            return clazz;
        }
        /**
         * @return the comp
         */
        public JComponent getComp()
        {
            return comp;
        }
        /**
         * @param comp the comp to set
         */
        public void setComp(JComponent comp)
        {
            this.comp = comp;
        }
        /**
         * @return the isSep
         */
        public boolean isSep()
        {
            return isSep;
        }
        
        /**
         * @return the compType
         */
        public CompType getCompType()
        {
            return compType;
        }

        /**
         * @return the itemTitles
         */
        public String[] getItemsTitles()
        {
            return itemTitles;
        }

        /**
         * @return the defaultVal
         */
        public Object getDefaultVal()
        {
            return defaultVal;
        }
    }
}

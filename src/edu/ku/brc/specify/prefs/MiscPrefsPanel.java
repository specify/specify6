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
package edu.ku.brc.specify.prefs;

import java.awt.Component;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.ui.forms.validation.ValCheckBox;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Dec 14, 2006
 *
 */
public class MiscPrefsPanel extends GenericPrefsPanel implements PrefsSavable, PrefsPanelIFace
{
    
    /**
     * 
     */
    public MiscPrefsPanel()
    {
        createForm("Preferences", "Misc");
        
        setCheckbox("1", "Using.Interactions", true);
        setCheckbox("2", "Doing.Gifts", true);
        setCheckbox("3", "Doing.Exchanges", Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.botany));

    }
    
    
    /**
     * @param id
     * @param prefName
     * @param defVal
     */
    protected void setCheckbox(final String id, final String prefName, final boolean defVal)
    {
        Component comp = form.getCompById(id);
        if (comp instanceof ValCheckBox)
        {
            String ds = Discipline.getCurrentDiscipline().getDiscipline();
            AppPreferences remotePrefs = AppPreferences.getRemote();
            boolean value = remotePrefs.getBoolean("Interactions."+prefName+"."+ds, defVal);
            ((ValCheckBox)comp).setSelected(value);
        }
    }
    
    /**
     * @param id
     * @param prefName
     */
    protected void getCheckbox(final String id, final String prefName)
    {
        Component comp = form.getCompById(id);
        if (comp instanceof ValCheckBox)
        {
            String ds = Discipline.getCurrentDiscipline().getDiscipline();
            AppPreferences remotePrefs = AppPreferences.getRemote();
            remotePrefs.putBoolean("Interactions."+prefName+"."+ds, ((ValCheckBox)comp).isSelected());
        } 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getHelpContext()
     */
    @Override
    public String getHelpContext()
    {
        return "PrefsMisc";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getChangedFields(java.util.Hashtable)
     */
    /*public void getChangedFields(final Properties changeHash)
    {
        super.getChangedFields(changeHash);
        
        FormViewObj fvo = (FormViewObj)form;
        Hashtable<String, String> idToNameHash = fvo.getIdToNameHash();
        
        String[] ids {};
        for (String id : ids)
        {
            Component comp = fvo.getCompById(id);
            if (comp instanceof UIValidatable && ((UIValidatable)comp).isChanged())
            {
                changeHash.put(idToNameHash.get(id), comp instanceof GetSetValueIFace ? ((GetSetValueIFace)comp).getValue() : "");
            }
        }
    }*/
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        if (form.getValidator() == null || form.getValidator().hasChanged())
        {
            super.savePrefs();
        
            getCheckbox("1", "Using.Interactions");
            getCheckbox("2", "Doing.Gifts");
            getCheckbox("3", "Doing.Exchanges");
        }
    }
    
}

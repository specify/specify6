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

import java.awt.Component;
import java.util.Map;
import java.util.HashMap;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.tasks.SymbiotaTask;
import edu.ku.brc.ui.UIRegistry;

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
    Map<String, Boolean> originals = new HashMap<>();
    /**
     * 
     */
    public MiscPrefsPanel()
    {
        createForm("Preferences", "Misc");
        
        originals.put("1", setCheckbox("1", "Interactions.Using.Interactions", true));
        originals.put("2", setCheckbox("2", "ExportTask.OnTaskbar", false));
        originals.put("3", setCheckbox("3", "StartupTask.OnTaskbar", true));
        originals.put("4", setCheckbox("4", "AttachmentsTask.OnTaskbar", true, "ATTACHMENTS"));
        //setCheckbox("5", "CleanupToolsTask.OnTaskbar", false, "CLEANUP");
        originals.put("6", setCheckbox("6", SymbiotaTask.IS_USING_SYMBIOTA_PREFNAME, false));
        //setCheckbox("7", SGRTask.IS_USING_SGR_PREFNAME, true);
    }
    
    /**
     * @param id
     * @param prefName
     * @param defVal
     */
    protected boolean setCheckbox(final String id,
                               final String prefName, 
                               final boolean defVal,
                               final String taskName)
    {
        Taskable task = TaskMgr.getTask(taskName);
        boolean enable = !AppContextMgr.isSecurityOn() || (task != null && task.getPermissions().canView());
        return setCheckbox(id, prefName, defVal, enable);
    }
    
    /**
     * @param id
     * @param prefName
     * @param defVal
     */
    protected boolean setCheckbox(final String id,
                               final String prefName, 
                               final boolean defVal)
    {
        return setCheckbox(id, prefName, defVal, true);
    }
    
    
    /**
     * @param id
     * @param prefName
     * @param defVal
     */
    protected boolean setCheckbox(final String id,
                               final String prefName, 
                               final boolean defVal,
                               final boolean enable)
    {
        Component comp = form.getCompById(id);
        if (comp instanceof ValCheckBox)
        {
            String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
            AppPreferences remotePrefs = AppPreferences.getRemote();
            boolean value = remotePrefs.getBoolean(prefName+"."+ds, defVal);
            ((ValCheckBox)comp).setSelected(value);
            comp.setEnabled(enable);
            return value;
        }
        return defVal;
    }
    
    /**
     * @param id
     * @param prefName
     * return true if pref was changed.
     */
    protected boolean getCheckbox(final String id, final String prefName)
    {
        Component comp = form.getCompById(id);
        boolean result = false;
        if (comp instanceof ValCheckBox)
        {
            String         ds          = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
            AppPreferences remotePrefs = AppPreferences.getRemote();
            Boolean v = remotePrefs.getBoolean(prefName+"."+ds, null);
            boolean isSelected = ((ValCheckBox)comp).isSelected();
            if ((v == null && isSelected != originals.get(id)) || (v != null && !v.equals(isSelected))) {
            	remotePrefs.putBoolean(prefName+"."+ds, isSelected);
            	result = true;
            }
        } 
        return result;
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
            boolean changed = false; 
            changed |= getCheckbox("1", "Interactions.Using.Interactions");
            changed |= getCheckbox("2", "ExportTask.OnTaskbar");
            changed |= getCheckbox("3", "StartupTask.OnTaskbar");
            changed |= getCheckbox("4", "AttachmentsTask.OnTaskbar");
            changed |= getCheckbox("5", "CleanupToolsTask.OnTaskbar");
            changed |= getCheckbox("6", SymbiotaTask.IS_USING_SYMBIOTA_PREFNAME);
            //changed |= getCheckbox("7", SGRTask.IS_USING_SGR_PREFNAME);
            
            if (changed) {
            	UIRegistry.displayInfoMsgDlg(UIRegistry.getResourceString("MiscPrefsPanel.RestartRequired"));
            }
        }
    }
    
}

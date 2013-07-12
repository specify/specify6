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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 18, 2008
 *
 */
public class InstitutionBusRules extends BaseBusRules
{
    private static final String RELEASES = "RELEASES";
    
    private ValCheckBox checkbox    = null;
    private ValComboBox relCombobox = null;

    /**
     * @param dataClasses
     */
    public InstitutionBusRules()
    {
        super(Institution.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(final Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null && formViewObj.getMVParent().isTopLevel())
        {
            ResultSetController rsc = formViewObj.getRsController();
            if (rsc != null)
            {
                if (rsc.getNewRecBtn() != null) rsc.getNewRecBtn().setVisible(false);
                if (rsc.getDelRecBtn() != null) rsc.getDelRecBtn().setVisible(false);
            }
            
            ValSpinner minPwdLenSpinner = (ValSpinner)formViewObj.getControlById("minimumPwdLength");
            if (minPwdLenSpinner != null)
            {
                
                minPwdLenSpinner.setRange(Institution.MIN_PASSWORD_LEN,  // min
                                          30,                            // max
                                          Institution.MIN_PASSWORD_LEN); // val
            }
            
            if (!AppPreferences.getLocalPrefs().getBoolean("RELEASE_MANAGER", false))
            {
                Component comp = formViewObj.getControlById("relmgrsep");
                if (comp != null) comp.setVisible(false);
                comp = formViewObj.getControlById("relmgrlabel");
                if (comp != null) comp.setVisible(false);
                comp = formViewObj.getControlById("relmgrpanel");
                if (comp != null) comp.setVisible(false);
            }
        }
    }
    
    /**
     * @param name
     * @return
     */
    private int getNameCount(final String name)
    {
        return BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM institution WHERE Name = '%s'", name));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean isOkToSave(final Object dataObj, final DataProviderSessionIFace session)
    {
        if (formViewObj != null)
        {
            Component comp = formViewObj.getControlByName("name");
            if (comp instanceof ValTextField)
            {
                Institution inst   = (Institution)dataObj;
                Integer     instId = inst.getId();
                String      name   = ((ValTextField)comp).getText();
                int cnt = getNameCount(name);
                if (cnt == 0 || (cnt == 1 && instId != null))
                {
                    return true;
                }
               reasonList.add(UIRegistry.getLocalizedMessage("DIVNAME_DUP", name));
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (checkbox == null && dataObj != null)
        {
            Component comp = formViewObj.getControlById("curMgrRelVer");
            if (comp instanceof ValComboBox)
            {
                relCombobox = (ValComboBox)comp;
            }
            
            comp = formViewObj.getControlById("isRelMgrGlb");
            if (relCombobox != null && comp instanceof ValCheckBox)
            {
                checkbox = (ValCheckBox)comp;
                
                checkbox.addChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        relCombobox.setEnabled(checkbox.isSelected());
                        ((JLabel)formViewObj.getLabelById("curMgrRelVerLbl")).setEnabled(checkbox.isSelected());
                    }
                });
                
                final Institution inst = (Institution)viewable.getDataObj();
                if (inst != null)
                {
                    Vector<String> releases   = new Vector<String>();
                    String         curRelease = AppPreferences.getGlobalPrefs().get(RELEASES, null);
                    
                    if (curRelease == null)
                    {
                        curRelease = UIHelper.getInstall4JInstallString();
                        AppPreferences.getGlobalPrefs().put(RELEASES, curRelease);
                    }
                    releases.add(curRelease);
                    
                    String managedRelease = inst.getCurrentManagedRelVersion();
                    if (managedRelease == null)
                    {
                        managedRelease = curRelease;
                        inst.setCurrentManagedRelVersion(managedRelease);
                    }
                    
                    boolean releaseNumMismatch = !managedRelease.equals(curRelease);
                    if (releaseNumMismatch)
                    {
                        releases.insertElementAt(managedRelease, 0);
                    }
                    
                    relCombobox.getComboBox().setModel(new DefaultComboBoxModel(releases));
                    relCombobox.getComboBox().setSelectedIndex(releaseNumMismatch ? 1 : 0);
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            UIValidator.setIgnoreAllValidation(this, true);
                            try
                            {
                                checkbox.setSelected(!inst.getIsReleaseManagedGlobally()); // make sure the ChangeListener gets activated
                                checkbox.setSelected(inst.getIsReleaseManagedGlobally());
                            } catch (Exception ex) {}
                            finally 
                            {
                                UIValidator.setIgnoreAllValidation(this, false);
                            }
                        }
                    });
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(Object dataObj, DataProviderSessionIFace session)
    {
        AppContextMgr.getInstance().setClassObject(Institution.class, dataObj);
        
        Institution inst = (Institution)dataObj;
        
        AppPreferences.getLocalPrefs().putBoolean("MANAGED_RELEASES", inst.getIsReleaseManagedGlobally());
        String managedRel = inst.getCurrentManagedRelVersion();
        if (StringUtils.isNotEmpty(managedRel))
        {
            AppPreferences.getGlobalPrefs().put(RELEASES, managedRel);
        } else
        {
            AppPreferences.getGlobalPrefs().remove(RELEASES);
        }

        return super.afterSaveCommit(dataObj, session);
    }
    
    
}

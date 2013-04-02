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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getFormattedResStr;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.PasswordStrengthUI;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValPasswordField;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.subpane.security.SecurityAdminPane;
import edu.ku.brc.specify.tasks.subpane.security.SecuritySummaryDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * 
 * @author megkumin & Ricardo
 *
 */
public class SecurityAdminTask extends BaseTask
{
    private static final Logger log            = Logger.getLogger(SecurityAdminTask.class);
    public static final  String SECURITY_ADMIN = "SecurityAdmin"; //$NON-NLS-1$
    protected SubPaneIFace      starterPane    = null;

    public SecurityAdminTask()
    {
    	super(SECURITY_ADMIN, getResourceString(SECURITY_ADMIN));
    	CommandDispatcher.register(SECURITY_ADMIN, this);
    	iconName = "Security";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        if (starterPane == null)
        {
            if (SubPaneMgr.getInstance().aboutToShutdown())
            {
            	SecurityAdminPane userGroupAdminPane = new SecurityAdminPane(title, this);
            	userGroupAdminPane.createMainControlUI();
                starterPane = userGroupAdminPane;
                
                TaskMgr.disableAllEnabledTasks();
            }
        }
        return starterPane;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneMgrListener#subPaneRemoved(edu.ku.brc.af.ui.SubPaneIFace)
     */
    @Override
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        
        if (starterPane != null && (starterPane == subPane || subPanes.size() == 0))
        {
            starterPane.shutdown();
            starterPane = null;
            TaskMgr.reenableAllDisabledTasks();
            TaskMgr.getTask("Startup").requestContext();
        }
    }
    
    /**
     * Enables the User to change password.
     */
    public static void changePassword(final boolean isStartingEmpty)
    {
        final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                "SystemSetup",
                "ChangePassword",
                null,
                getResourceString(getKey("CHG_PWD_TITLE")),
                "OK",
                null,
                null,
                true,
                MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE |
                MultiView.IS_EDITTING);
        dlg.setWhichBtns(CustomDialog.OK_BTN | CustomDialog.CANCEL_BTN);
        
        dlg.setFormAdjuster(new FormPane.FormPaneAdjusterIFace() {
            @Override
            public void adjustForm(final FormViewObj fvo)
            {
                final ValPasswordField   oldPwdVTF    = fvo.getCompById("1");
                final ValPasswordField   newPwdVTF    = fvo.getCompById("2");
                final ValPasswordField   verPwdVTF    = fvo.getCompById("3");
                final PasswordStrengthUI pwdStrenthUI = fvo.getCompById("4");
                
                if (isStartingEmpty && pwdStrenthUI != null)
                {
                    pwdStrenthUI.setDoPainting(true);
                }
                
                Institution institution = AppContextMgr.getInstance().getClassObject(Institution.class);
                int minPwdLen = (int)institution.getMinimumPwdLength();
                newPwdVTF.setMinLen(minPwdLen);
                verPwdVTF.setMinLen(minPwdLen);
                pwdStrenthUI.setMinPwdLen(minPwdLen);
                
                DocumentAdaptor da = new DocumentAdaptor() {
                    @Override
                    protected void changed(final DocumentEvent e)
                    {
                        super.changed(e);
                        
                        // Need to invoke later so the da gets to set the enabled state last.
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run()
                            {
                                
                                String  pwdStr  = new String(newPwdVTF.getPassword());
                                String  verStr  = new String(verPwdVTF.getPassword());
                                boolean pwdOK   = pwdStrenthUI.checkStrength(pwdStr) &&
                                                  pwdStr.equals(verStr) &&
                                                  newPwdVTF.getState() == UIValidatable.ErrorType.Valid;
                                dlg.getOkBtn().setEnabled(pwdOK);
                                pwdStrenthUI.repaint();
                            }
                        });
                    }
                };
                
                oldPwdVTF.getDocument().addDocumentListener(da);
                verPwdVTF.getDocument().addDocumentListener(da);
                newPwdVTF.getDocument().addDocumentListener(da);
            }
        });
        
        Hashtable<String, String> valuesHash = new Hashtable<String, String>();
        dlg.setData(valuesHash);
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            int pwdLen = 6;
            
            String oldPwd  = valuesHash.get("OldPwd");
            String newPwd1 = valuesHash.get("NewPwd1");
            String newPwd2 = valuesHash.get("NewPwd2");
            
            if (newPwd1.equals(newPwd2))
            {
                if (newPwd1.length() >= pwdLen)
                {
                    SpecifyUser spUser    = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
                    //String      username  = spUser.getName();
                    String      spuOldPwd = spUser.getPassword();
                    
                    String newEncryptedPwd = null;
                    String oldDecryptedPwd = Encryption.decrypt(spuOldPwd, oldPwd);
                    if (oldDecryptedPwd != null && oldDecryptedPwd.equals(oldPwd))
                    {
                        newEncryptedPwd = Encryption.encrypt(newPwd2, newPwd2);
                        spUser.setPassword(newEncryptedPwd);
                        if (!DataModelObjBase.save(spUser))
                        {
                            UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(getKey("PWD_ERR_SAVE")), Color.RED);
                        }
                        
                    } else
                    {
                        UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(getKey("PWD_ERR_BAD")), Color.RED);
                    }
                    
                    if (newEncryptedPwd != null)
                    {
                        Pair<String, String> masterPwd = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
                        
                        String encryptedMasterUP = UserAndMasterPasswordMgr.encrypt(masterPwd.first, masterPwd.second, newPwd2);
                        if (StringUtils.isNotEmpty(encryptedMasterUP))
                        {
                            AppPreferences.getLocalPrefs().put(UserAndMasterPasswordMgr.getInstance().getMasterPrefPath(true), encryptedMasterUP);
                            UIHelper.setTextToClipboard(encryptedMasterUP);
                            UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "INFORMATION", "SPUSR_NEWPWD");
                            
                        } else
                        {
                            UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(getKey("PWD_ERR_RTRV")), Color.RED);
                        }
                        
                    } else
                    {
                        UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(getKey("PWD_ERR_BAD")), Color.RED);
                    }
                } else
                {
                    UIRegistry.writeTimedSimpleGlassPaneMsg(getFormattedResStr(getKey("PWD_ERR_LEN"), pwdLen), Color.RED);
                }
            } else
            {
                UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(getKey("PWD_ERR_NOTSAME")), Color.RED);
            }
        }
    }
    
    /**
     * @param key
     * @return
     */
    private static String getKey(final String key)
    {
        return "SecurityAdminTask." + key;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        menuItems = new Vector<MenuItemDesc>();
        
        // show security summary menu item
    	// no need to check security as everyone can see their own summary... besides it's read only
        String menuTitle = getKey("SHOW_SECURITY_SUMMARY_MENU"); //$NON-NLS-1$
        String mneu      = getKey("SHOW_SECURITY_SUMMARY_MNEU"); //$NON-NLS-1$
        String desc      = getKey("SHOW_SECURITY_SUMMARY_DESC"); //$NON-NLS-1$
        JMenuItem mi     = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null); // I18N
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
            	SecuritySummaryDlg dlg = new SecuritySummaryDlg(null);
            	dlg.setVisible(true);
            }
        });
        String menuDesc = "Specify.SYSTEM_MENU/Specify.COLSETUP_MENU";
        
        MenuItemDesc showSummaryMenuDesc = new MenuItemDesc(mi, menuDesc);
        showSummaryMenuDesc.setPosition(MenuItemDesc.Position.After, getResourceString(getKey("SECURITY_TOOLS_MENU")));

        // check whether user can see the security admin panel
        // other permissions will be checked when the panel is created 
        // XXX RELEASE
        String securityName = buildTaskPermissionName(SECURITY_ADMIN);
        if (!AppContextMgr.isSecurityOn() || SecurityMgr.getInstance().checkPermission(securityName, BasicSpPermission.view)) //$NON-NLS-1$
        {
            // security tools menu item
            menuTitle = getKey("SECURITY_TOOLS_MENU"); //$NON-NLS-1$
            mneu      = getKey("SECURITY_TOOLS_MNEU"); //$NON-NLS-1$
            desc      = getKey("SECURITY_TOOLS_DESC"); //$NON-NLS-1$
            mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    if (SubPaneMgr.getInstance().aboutToShutdown())
                    {
                        SecurityAdminTask.this.requestContext();
                    }
                }
            });
            MenuItemDesc mid = new MenuItemDesc(mi, menuDesc);
            mid.setPosition(MenuItemDesc.Position.After, getResourceString("SystemSetupTask.COLL_CONFIG"));
            //mid.setSepPosition(MenuItemDesc.Position.After);
            
            menuItems.add(mid);
            menuItems.add(showSummaryMenuDesc);
        }
        
        menuTitle = getKey("MASTER_PWD_MENU"); //$NON-NLS-1$
        mneu      = getKey("MASTER_PWD_MNEU"); //$NON-NLS-1$
        desc      = getKey("MASTER_PWD_DESC"); //$NON-NLS-1$
        mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                SpecifyUser spUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
                UserAndMasterPasswordMgr.getInstance().editMasterInfo(spUser.getName(), DBConnection.getInstance().getDatabaseName(), true);
            }
        });
        MenuItemDesc mid = new MenuItemDesc(mi, UIHelper.isMacOS() ? "HELP" : "HELP/ABOUT", UIHelper.isMacOS() ? MenuItemDesc.Position.Bottom : MenuItemDesc.Position.Before); //$NON-NLS-1$ $NON-NLS-2$
        menuItems.add(mid);

        menuTitle = getKey("CHANGE_PWD_MENU"); //$NON-NLS-1$
        mneu      = getKey("CHANGE_PWD_MNEU"); //$NON-NLS-1$
        desc      = getKey("CHANGE_PWD_DESC"); //$NON-NLS-1$
        mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                changePassword(false);
            }
        });
        
        mid = new MenuItemDesc(mi, mid.getMenuPath(), UIHelper.isMacOS() ? MenuItemDesc.Position.Bottom : MenuItemDesc.Position.Before); //$NON-NLS-1$ $NON-NLS-2$
        menuItems.add(mid);
        
        return menuItems;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        String securityName = buildTaskPermissionName(SECURITY_ADMIN);
        if (!AppContextMgr.isSecurityOn() || SecurityMgr.getInstance().checkPermission(securityName, BasicSpPermission.view)) //$NON-NLS-1$
        {
            if (AppPreferences.getLocalPrefs().getBoolean("SEC_TOOLBAR", false))
            {
                toolbarItems = new Vector<ToolBarItemDesc>();
                toolbarItems.add(new ToolBarItemDesc(createToolbarButton("Security", iconName, "")));
            }
        }
        return toolbarItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(SECURITY_ADMIN))
        {
            processAdminCommands(cmdAction);     
        } 
    }
   
    /**
     * @param cmdAction
     */
    private void processAdminCommands(@SuppressWarnings("unused") CommandAction cmdAction)
    {
        log.error("not implemented");         //$NON-NLS-1$
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(SECURITY_ADMIN, "ENABLE", null, null, null);
    }
    
    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {false, false, false, false},
                                {false, false, false, false},
                                {false, false, false, false}};
    }
    
    /*class PwdDocAdapter extends DocumentAdaptor
    {
        private CustomDialog       dlg;
        private ValPasswordField   pwdField;
        private boolean            doCheckLen;
        private PasswordStrengthUI pwdStrenthUI;
        
        public PwdDocAdapter(final CustomDialog dlg, 
                             final ValPasswordField pwdField, 
                             final PasswordStrengthUI pwdStrenthUI,
                             final boolean doCheckLen)
        {
            super();
            this.dlg          = dlg;
            this.pwdField     = pwdField;
            this.pwdStrenthUI = pwdStrenthUI;
            this.doCheckLen   = doCheckLen;
        }
                
        @Override
        protected void changed(DocumentEvent e)
        {
            super.changed(e);
            
            // Need to invoke later so the da gets to set the enabled state last.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    //boolean enabled = dlg.getOkBtn().isEnabled();
                    //String  pwdStr  = new String(newPwdVTF.getPassword());
                    //boolean pwdOK   = pwdStrenthUI.checkStrength(pwdStr);
                    
                    //dlg.getOkBtn().setEnabled(enabled && pwdOK);
                    //pwdStrenthUI.repaint();
                }
            });
        }
    }*/
}

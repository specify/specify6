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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.persist.FormDevHelper;
import edu.ku.brc.af.ui.forms.persist.ViewLoader;
import edu.ku.brc.specify.extras.ViewToSchemaReview;
import edu.ku.brc.specify.tools.FormDisplayer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 7, 2009
 *
 */
public class FormEditTask extends BaseTask
{
    //private static final Logger log = Logger.getLogger(FormEditTask.class);
    
    // Static Data Members
    public static final String FORMS  = "FORMS";

    /**
     * 
     */
    public FormEditTask()
    {
        super(FORMS, "Forms");
        
        iconName = "Form";
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
        
        //
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
     */
    @Override
    protected void doProcessAppCommands(CommandAction cmdAction)
    {
        super.doProcessAppCommands(cmdAction);
        
        if (cmdAction.isAction(APP_RESTART_ACT) ||
            cmdAction.isAction(APP_START_ACT))
        {
            JMenu formMenu = (JMenu)UIRegistry.get("Specify.FORM_MENU");
            if (formMenu != null)
            {
                boolean isOKToEdit = SecurityMgr.getInstance().checkPermission("Task." + FORMS, BasicSpPermission.view);
                formMenu.setEnabled(isOKToEdit);
            }
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        final String FORM_SECURITY    = "Task.FORMS";
        final String FORM_MENU        = "Specify.FORMS_MENU";
        final String SYSTEM_MENU      = "Specify.SYSTEM_MENU";
        final String FULL_SYSTEM_MENU = SYSTEM_MENU + "/" + FORM_MENU;
        SecurityMgr secMgr = SecurityMgr.getInstance();
        
        menuItems = new Vector<MenuItemDesc>();
        
        MenuItemDesc mid;
        
        String    mneu; 
        JMenuItem mi;
        
        String securityName = buildTaskPermissionName(FORM_SECURITY);
        if (!AppContextMgr.isSecurityOn() || 
            (secMgr.getPermission(securityName) != null && 
             !secMgr.getPermission(securityName).hasNoPerm()))
        {
            final String reloadViews = "reload_views"; //$NON-NLS-1$
            JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(getResourceString("Specify.RELOAD_VIEWS")); //$NON-NLS-1$
            cbMenuItem.setSelected(AppPreferences.getLocalPrefs().getBoolean(reloadViews, false));
            cbMenuItem.addActionListener(new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean isReload = !AppPreferences.getLocalPrefs().getBoolean(reloadViews, false);                       
                    AppPreferences.getLocalPrefs().putBoolean(reloadViews, isReload);
                    ((JMenuItem)ae.getSource()).setSelected(isReload);
                    
                    ((JMenuItem)ae.getSource()).setSelected(isReload);
            }});
            mid = new MenuItemDesc(cbMenuItem, FULL_SYSTEM_MENU);
            menuItems.add(mid);
    
            final String verifyFields = "verify_field_names"; //$NON-NLS-1$
            cbMenuItem = new JCheckBoxMenuItem(getResourceString("Specify.VERIFY_FIELDS")); //$NON-NLS-1$
            cbMenuItem.setSelected(AppPreferences.getLocalPrefs().getBoolean(verifyFields, false));
            cbMenuItem.addActionListener(new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean isVerify = !AppPreferences.getLocalPrefs().getBoolean(verifyFields, false);                       
                    AppPreferences.getLocalPrefs().putBoolean(verifyFields, isVerify);
                    ((JMenuItem)ae.getSource()).setSelected(isVerify);
                    ViewLoader.setDoFieldVerification(isVerify);
                }});
            mid = new MenuItemDesc(cbMenuItem, FULL_SYSTEM_MENU);
            menuItems.add(mid);
            
            cbMenuItem = new JCheckBoxMenuItem(getResourceString("Specify.SHOW_FORM_DEBUG")); //$NON-NLS-1$
            cbMenuItem.setSelected(FormViewObj.isUseDebugForm());
            cbMenuItem.addActionListener(new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean useDebugForm = !FormViewObj.isUseDebugForm();
                    FormViewObj.setUseDebugForm(useDebugForm);
                    ((JMenuItem)ae.getSource()).setSelected(useDebugForm);
                }});
            mid = new MenuItemDesc(cbMenuItem, FULL_SYSTEM_MENU);
            mid.setSepPosition(MenuItemDesc.Position.After);
            menuItems.add(mid);
            
            String ttle = "Specify.CREATE_FORM_IMAGES";//$NON-NLS-1$ 
            mneu        = "Specify.CREATE_FORM_IMAGES_MNEU";//$NON-NLS-1$ 
            String desc = "Specify.CREATE_FORM_IMAGES";//$NON-NLS-1$ 
            mi = UIHelper.createLocalizedMenuItem(ttle , mneu, desc, true, null);  
            mi.addActionListener(new ActionListener()
            {
               @Override
                public void actionPerformed(ActionEvent ae)
                {
                    FormDisplayer fd = new FormDisplayer();
                    fd.generateFormImages();
                }
            });
            mid = new MenuItemDesc(mi, FULL_SYSTEM_MENU);
            menuItems.add(mid);
            
            ttle = "Specify.CREATE_FORM_LIST";//$NON-NLS-1$ 
            mneu = "Specify.CREATE_FORM_LIST_MNEU";//$NON-NLS-1$ 
            desc = "Specify.CREATE_FORM_LIST";//$NON-NLS-1$ 
            mi = UIHelper.createLocalizedMenuItem(ttle , mneu, desc, true, null);  
            mi.addActionListener(new ActionListener()
            {
               @Override
                public void actionPerformed(ActionEvent ae)
                {
                    FormDisplayer fd = new FormDisplayer();
                    fd.createViewListing(UIRegistry.getUserHomeDir(), true);
                }
            });
            mid = new MenuItemDesc(mi, FULL_SYSTEM_MENU);
            menuItems.add(mid);
            
            ttle = "Specify.FORM_FIELD_LIST";//$NON-NLS-1$ 
            mneu = "Specify.FORM_FIELD_LIST_MNEU";//$NON-NLS-1$ 
            desc = "Specify.FORM_FIELD_LIST";//$NON-NLS-1$ 
            mi = UIHelper.createLocalizedMenuItem(ttle , mneu, desc, true, null);  
            mi.addActionListener(new ActionListener()
            {
               @Override
                public void actionPerformed(ActionEvent ae)
                {
                    ViewToSchemaReview.dumpFormFieldList(true);
                }
            });
            mid = new MenuItemDesc(mi, FULL_SYSTEM_MENU);
            menuItems.add(mid);

            ttle = "Specify.VIEW_REVIEW_LIST";//$NON-NLS-1$ 
            mneu = "Specify.VIEW_REVIEW_LIST_MNEU";//$NON-NLS-1$ 
            desc = "Specify.VIEW_REVIEW_LIST";//$NON-NLS-1$ 
            mi = UIHelper.createLocalizedMenuItem(ttle , mneu, desc, true, null);  
            mi.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    (new ViewToSchemaReview()).checkSchemaAndViews();
                }
            });
            mid = new MenuItemDesc(mi, FULL_SYSTEM_MENU);
            menuItems.add(mid);
            
            if (FormDevHelper.isFormDevMode())
            {
                ttle = "Specify.SHOW_DEV_WIN";//$NON-NLS-1$ 
                mneu = "Specify.SHOW_DEV_WIN_MNEU";//$NON-NLS-1$ 
                desc = "Specify.SHOW_DEV_WIN";//$NON-NLS-1$ 
                mi = UIHelper.createLocalizedMenuItem(ttle , mneu, desc, true, null);  
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        FormDevHelper.getLogFrame().setVisible(true);
                    }
                });
                mid = new MenuItemDesc(mi, FULL_SYSTEM_MENU);
                menuItems.add(mid);
            }
        }
        
        return menuItems;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return starterPane = StartUpTask.createFullImageSplashPanel(title, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTask#getAndSetDefPerms()
     */
    @Override
    protected Hashtable<String, PermissionOptionPersist> getAndSetDefPerms()
    {
        return super.getAndSetDefPerms();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTask#getDefaultPermissions(java.lang.String)
     */
    @Override
    public PermissionIFace getDefaultPermissions(String userType)
    {
        return super.getDefaultPermissions(userType);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermsArray()
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {false, false, false, false},
                                {false, false, false, false},
                                {false, false, false, false}};
    }
}

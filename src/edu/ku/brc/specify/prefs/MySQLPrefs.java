/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.prefs;

import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.isSecurityOn;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.af.core.db.MySQLBackupService;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsPanelMgrIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.af.tasks.BackupTask;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.ValBrowseBtnPanel;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 3, 2008
 *
 */
public class MySQLPrefs extends JPanel implements PrefsSavable, PrefsPanelIFace
{
    final static String MYSQL_PREF_NAME = "Task.BackupTask";
    
    private static final String  securityPrefix    = "Prefs."; //$NON-NLS-1$
    
    private final String MYSQL_LOC     = "mysql.location";
    private final String MYSQLDUMP_LOC = "mysqldump.location";
    private final String MYSQLBCK_LOC  = "backup.location";
    
    private ValBrowseBtnPanel mysqlLocBP     = new ValBrowseBtnPanel(new ValTextField(), true, true);
    private ValBrowseBtnPanel mysqlDumpLocBP = new ValBrowseBtnPanel(new ValTextField(), true, true);
    private ValBrowseBtnPanel backupLocBP    = new ValBrowseBtnPanel(new ValTextField(), true, true);
    
    private JButton           backupBtn;
    private JButton           restoreBtn;
    private boolean           doShowRestore;

    private FormValidator     validator     = new FormValidator(null);
    protected PrefsPanelMgrIFace mgr = null;
    protected String             name;
    protected String             title;
    protected String             hContext  = null;
    protected Color              shadeColor = null;

    // Security
    protected PermissionIFace permissions = null;

    /**
     * @param isOnlyPanel
     */
    public MySQLPrefs()
    {
        this(false);
    }
    
    /**
     * @param isOnlyPanel
     */
    public MySQLPrefs(final boolean doShowRestore)
    {
        super();
        
        this.doShowRestore = doShowRestore;
        
        UIRegistry.loadAndPushResourceBundle("preferences");
        
        createUI();
        
        UIRegistry.popResourceBundle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getValidator()
     */
    public FormValidator getValidator()
    {
        return validator;
    }

    /**
     * Create the UI for the panel
     */
    protected void createUI()
    {
        AppPreferences prefs = AppPreferences.getLocalPrefs();
        
        CellConstraints cc = new CellConstraints();
        
        backupBtn  = createI18NButton("MYS_BACKUP");
        restoreBtn = createI18NButton("MYS_RESTORE");

        
        PanelBuilder btnPB         = new PanelBuilder(new FormLayout("f:p:g,2px,p,5px,p,f:p:g", "p"));
        
        btnPB.add(backupBtn, cc.xy(3, 1));
        if (doShowRestore)
        {
            btnPB.add(restoreBtn, cc.xy(5, 1));
        }

        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,p,p:g,f:p:g", "p,2dlu,p,2dlu,p,2dlu,p,14px,p"), this);
        
        pb.addSeparator(UIRegistry.getResourceString("BCKRSTR_OPTIONS"), cc.xyw(1, 1, 5));
        
        pb.add(createI18NFormLabel("MYS_DUMP_PATH"), cc.xy(1, 3));
        pb.add(mysqlDumpLocBP,                   cc.xyw(3, 3, 3));
        
        pb.add(createI18NFormLabel("MYS_RESTR_PATH"), cc.xy(1, 5));
        pb.add(mysqlLocBP,                        cc.xyw(3, 5, 3));
        
        pb.add(createI18NFormLabel("MYS_BCK_PATH"), cc.xy(1, 7));
        pb.add(backupLocBP,                     cc.xyw(3, 7, 3));
        
        pb.add(btnPB.getPanel(), cc.xyw(1, 9, 5));
        
        pb.setDefaultDialogBorder();
        
        String mysqlLoc     = prefs.get(MYSQL_LOC,     null);
        String mysqlDumpLoc = prefs.get(MYSQLDUMP_LOC, null);
        String backupLoc    = prefs.get(MYSQLBCK_LOC,  null);
        
        if (StringUtils.isEmpty(mysqlLoc))
        {
            mysqlLoc = MySQLBackupService.getDefaultMySQLLoc();
        }
        
        if (StringUtils.isEmpty(backupLoc))
        {
            backupLoc = MySQLBackupService.getDefaultBackupLoc();
        }
        
        mysqlLocBP.setValue(mysqlLoc, mysqlLoc);
        mysqlDumpLocBP.setValue(mysqlDumpLoc, mysqlDumpLoc);
        backupLocBP.setValue(backupLoc, backupLoc);
        
        DocumentListener dl = new DocumentListener()
        {
            protected void changed()
            {
                validator.setHasChanged(true);
                updateEnableUI();
            }
            @Override
            public void changedUpdate(DocumentEvent e) { changed(); }
            @Override
            public void insertUpdate(DocumentEvent e) { changed(); }
            @Override
            public void removeUpdate(DocumentEvent e) { changed(); }
        };
        mysqlLocBP.getTextField().getDocument().addDocumentListener(dl);
        mysqlDumpLocBP.getTextField().getDocument().addDocumentListener(dl);
        backupLocBP.getTextField().getDocument().addDocumentListener(dl);

        
        backupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (mgr == null || mgr.closePrefs())
                {
                    BackupServiceFactory.getInstance().doBackUp();
                }
            }
        });
        
        restoreBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doRestore();
            }
        });
        
        if (isSecurityOn())
        {
            PermissionSettings perm = SecurityMgr.getInstance().getPermission(MYSQL_PREF_NAME);
            restoreBtn.setVisible(perm.canModify()); // this means Enabled
            backupBtn.setVisible(perm.canView()); // this means Enabled
        }
    }
    
    /**
     * 
     */
    private void updateEnableUI()
    {
        backupBtn.setEnabled(!backupLocBP.getTextField().getText().isEmpty() && !mysqlDumpLocBP.getTextField().getText().isEmpty());
        restoreBtn.setEnabled(!mysqlLocBP.getTextField().getText().isEmpty());
    }
    
    /**
     * Asks for the prefs to close and all the SubPanes so it can do a restore.
     */
    protected void doRestore()
    {
        if (mgr == null || mgr.closePrefs())
        {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    if (SubPaneMgr.getInstance().aboutToShutdown())
                    {
                        Taskable task = TaskMgr.getTask("BackupTask");
                        if (task != null)
                        {
                            SubPaneIFace splash = ((BackupTask)TaskMgr.getTask("BackupTask")).getSplashPane();
                            SubPaneMgr.getInstance().addPane(splash);
                            SubPaneMgr.getInstance().showPane(splash);
                        }
                        BackupServiceFactory.getInstance().doRestore();
                    }
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        AppPreferences prefs = AppPreferences.getLocalPrefs();
        
        prefs.put(MYSQL_LOC, (String)mysqlLocBP.getValue());
        prefs.put(MYSQLDUMP_LOC, (String)mysqlDumpLocBP.getValue());
        prefs.put(MYSQLBCK_LOC, (String)backupLocBP.getValue());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#isOKToLoad()
     */
    @Override
    public boolean isOKToLoad()
    {
        Institution institution = AppContextMgr.getInstance().getClassObject(Institution.class);
        return !institution.getIsServerBased();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getChangedFields(java.util.Properties)
     */
    @Override
    public void getChangedFields(Properties changeHash)
    {
        
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getPermissions()
     */
    public PermissionIFace getPermissions()
    {
        if (permissions == null)
        {
            permissions = SecurityMgr.getInstance().getPermission(securityPrefix + getPermissionName());
        }
        return permissions;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return name to be used when getting permissions from SecurityMgr.
     */
    protected String getPermissionName()
    {
        return name;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setPermissions(edu.ku.brc.af.core.PermissionIFace)
     */
    public void setPermissions(PermissionIFace permissions)
    {
        this.permissions = permissions;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setPrefsPanelMgr(edu.ku.brc.af.prefs.PrefsPanelMgrIFace)
     */
    @Override
    public void setPrefsPanelMgr(PrefsPanelMgrIFace mgrArg)
    {
        this.mgr = mgrArg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getHelpContext()
     */
    public String getHelpContext()
    {
        return hContext;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setHelpContext(java.lang.String)
     */
    public void setHelpContext(String context)
    {
        hContext = context;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setShadeColor(java.awt.Color)
     */
    @Override
    public void setShadeColor(Color color)
    {
        shadeColor = color;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#isFormValid()
     */
    @Override
    public boolean isFormValid()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String titleArg)
    {
        this.title = titleArg;
    }

    
    
}

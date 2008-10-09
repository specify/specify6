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

import java.awt.Component;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.MySQLBackupService;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.validation.ValBrowseBtnPanel;
import edu.ku.brc.specify.datamodel.Institution;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 3, 2008
 *
 */
public class MySQLPrefs extends GenericPrefsPanel
{

    private final String MYSQLDUMP_LOC = "mysqldump.location";
    private final String MYSQL_LOC     = "mysql.location";
    private final String MYSQLBCK_LOC  = "backup.location";
    
    /**
     * 
     */
    public MySQLPrefs()
    {
        super();
        
        createUI();
    }

    /**
     * Create the UI for the panel
     */
    protected void createUI()
    {
        createForm("Preferences", "MySQLBckRstrOptions");
        
        AppPreferences prefs = AppPreferences.getLocalPrefs();
        
        String mysqldumpLoc = prefs.get(MYSQLDUMP_LOC, null);
        String mysqlLoc     = prefs.get(MYSQL_LOC,     null);
        String backupLoc    = prefs.get(MYSQLBCK_LOC,  null);
        
        if (StringUtils.isEmpty(mysqldumpLoc))
        {
            mysqldumpLoc = MySQLBackupService.getDefaultMySQLDumpLoc();
        }
        
        if (StringUtils.isEmpty(mysqlLoc))
        {
            mysqlLoc = MySQLBackupService.getDefaultMySQLLoc();
        }
        
        if (StringUtils.isEmpty(backupLoc))
        {
            backupLoc = MySQLBackupService.getDefaultBackupLoc();
        }
        
        Component comp = form.getCompById("1");
        if (comp instanceof ValBrowseBtnPanel)
        {
            ((ValBrowseBtnPanel)comp).setValue(mysqldumpLoc, mysqldumpLoc);
        }
        
        comp = form.getCompById("2");
        if (comp instanceof ValBrowseBtnPanel)
        {
            ((ValBrowseBtnPanel)comp).setValue(mysqlLoc, mysqlLoc);
        }
        
        comp = form.getCompById("3");
        if (comp instanceof ValBrowseBtnPanel)
        {
            ((ValBrowseBtnPanel)comp).setValue(backupLoc, backupLoc);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        super.savePrefs();
        
        AppPreferences prefs = AppPreferences.getLocalPrefs();
        
        Component comp = form.getCompById("1");
        if (comp instanceof ValBrowseBtnPanel)
        {
            prefs.put(MYSQLDUMP_LOC, (String)((ValBrowseBtnPanel)comp).getValue());
        }
        
        comp = form.getCompById("2");
        if (comp instanceof ValBrowseBtnPanel)
        {
            prefs.put(MYSQL_LOC, (String)((ValBrowseBtnPanel)comp).getValue());
        }
        
        comp = form.getCompById("3");
        if (comp instanceof ValBrowseBtnPanel)
        {
            prefs.put(MYSQLBCK_LOC, (String)((ValBrowseBtnPanel)comp).getValue());
        }
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
}

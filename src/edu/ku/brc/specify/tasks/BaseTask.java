/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Oct 29, 2008
 *
 */
public abstract class BaseTask extends edu.ku.brc.af.tasks.BaseTask
{
    protected static final Logger  log = Logger.getLogger(BaseTask.class);
    
    protected static SoftReference<Hashtable<String, PermissionOptionPersist>> taskPermsListSR = null;
    
    /**
     * @param name the name of the task and should have an icon of the same name
     * @param title the already localized title of the task.
     */
    public BaseTask(final String name, final String title)
    {
        super(name, title);
    }
    
    /**
     * @param fileName xml file to be read in
     * @return a double hashtable first hashed by Permissions name and then hased by User Type
     */
    @SuppressWarnings("unchecked")
    public static Hashtable<String, Hashtable<String, PermissionOptionPersist>> readDefaultPermsFromXML(final String fileName)
    {
        Hashtable<String, Hashtable<String, PermissionOptionPersist>> hash = new Hashtable<String, Hashtable<String, PermissionOptionPersist>>();
        
        XStream xstream = new XStream();
        PermissionOptionPersist.config(xstream);
        
        String xmlStr = null;
        try
        {
            File permFile = new File(XMLHelper.getConfigDirPath("defaultperms" + File.separator + fileName)); //$NON-NLS-1$
            log.debug(permFile.getAbsoluteFile());
            if (permFile.exists())
            {
                xmlStr = FileUtils.readFileToString(permFile);
            }
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTask.class, ex);
            ex.printStackTrace();
        }
        
        if (xmlStr != null)
        {
            hash = (Hashtable<String, Hashtable<String, PermissionOptionPersist>>)xstream.fromXML(xmlStr);
            /*for (TaskPermPersist tp : list)
            {
                System.err.println("TP: "+tp.getTaskName()+"  "+tp.getUserType()+"  "+tp.isCanAdd());
            }*/
        }
        return hash;
    }
    
    /**
     * @return the Hashtable from the SoftReference
     */
    protected Hashtable<String, PermissionOptionPersist> getAndSetDefPerms()
    {
        if (taskPermsListSR == null)
        {
            taskPermsListSR = new SoftReference<Hashtable<String, PermissionOptionPersist>>(null);
        }
        
        Hashtable<String, PermissionOptionPersist> hash = taskPermsListSR.get();
        
        if (hash == null)
        {
            Hashtable<String, Hashtable<String, PermissionOptionPersist>> taskHash = readDefaultPermsFromXML("tasks.xml");
            if (taskHash != null)
            {
                hash = taskHash.get(name);
                if (hash != null)
                {
                    taskPermsListSR = new SoftReference<Hashtable<String, PermissionOptionPersist>>(hash);
                }
            }
        }
        
        return hash;
    }
    
    /**
     * Display a message dialog if not all the tabs are closed (except 'Welcome') and ten returns false.
     * @return whether any tabs are open except the 'Welcome' tab. 
     */
    public boolean isTabsClosed()
    {
        Collection<SubPaneIFace> allSubPanes = SubPaneMgr.getInstance().getSubPanes();
        if (allSubPanes.size() > 0)
        {
            for (SubPaneIFace sp : allSubPanes)
            {
                if (sp.getTask().getClass() != StartUpTask.class)
                {
                    UIRegistry.displayInfoMsgDlgLocalized("BaseTask.PL_CLOSE_TABS");
                    return false;
                }
                
                if (sp.getTask() == this)
                {
                    break;
                }
            }
        }
        return true;
    }
    
    /**
     * Checks to see if anyone else is logged into the Discipline and displays a dialog list the Agent names
     * of those who need to log off to perform the function.
     * @return true if no else is logged into the Discipline, false if someone else is logged in
     */
    public boolean isAnyOtherUsersOn()
    {
        return !((SpecifyAppContextMgr)AppContextMgr.getInstance()).displayAgentsLoggedInDlg("SystemSetupTask.CFG_SETUP");
    }


    //---------------------------------------------------------------------------
    //-- edu.ku.brc.af.tasks.BaseTask
    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getDefaultPermissions(java.lang.String)
     */
    @Override
    public PermissionIFace getDefaultPermissions(final String userType)
    {
        Hashtable<String, PermissionOptionPersist> hash = getAndSetDefPerms();
        //System.err.println(name+"  "+userType+"  "+(defaultPermissionsHash != null ? defaultPermissionsHash.get(userType) : null));
        if (hash != null)
        {
            PermissionOptionPersist permOpt = hash.get(userType);
            if (permOpt != null)
            {
                return permOpt.getDefaultPerms();
            }
            log.error("No permissions from hash for user type["+userType+"]");
        } else
        {
            log.error("No hashtable from getAndSetDefPerms!");
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
     */
    @Override
    public abstract SubPaneIFace getStarterPane();
}

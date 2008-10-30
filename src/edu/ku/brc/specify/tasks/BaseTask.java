/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.helpers.XMLHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 29, 2008
 *
 */
public abstract class BaseTask extends edu.ku.brc.af.tasks.BaseTask
{
    protected static final String COLLECTION_MANAGER = "CollectionManager";
    protected static final String GUEST              = "Guest";
    protected static final String DataENTRY          = "DataEntry";
    
    protected static WeakReference<Vector<TaskPermPersist>> taskPermsListWR       = null;
    
    // Data Members
    protected Hashtable<String, PermissionIFace> defaultPermissionsHash = null;
    
    public BaseTask(final String name, final String title)
    {
        super(name, title);
    }

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected void readAndSetDefPerms()
    {
        if (taskPermsListWR == null)
        {
            taskPermsListWR = new WeakReference<Vector<TaskPermPersist>>(null);
        }
        
        Vector<TaskPermPersist> list = taskPermsListWR.get();
        
        if (list == null)
        {
            XStream xstream = new XStream();
            TaskPermPersist.config(xstream);
            
            String xmlStr = null;
            try
            {
                File permFile = new File(XMLHelper.getConfigDirPath("taskperms.xml")); //$NON-NLS-1$
                if (permFile.exists())
                {
                    xmlStr = FileUtils.readFileToString(permFile);
                }
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
            
            if (xmlStr != null)
            {
                list = (Vector<TaskPermPersist>)xstream.fromXML(xmlStr);
                /*for (TaskPermPersist tp : list)
                {
                    System.err.println("TP: "+tp.getTaskName()+"  "+tp.getUserType()+"  "+tp.isCanAdd());
                }*/
                taskPermsListWR = new WeakReference<Vector<TaskPermPersist>>(list);
            }
        }
        
        if (list != null)
        {
            defaultPermissionsHash = new Hashtable<String, PermissionIFace>();
            for (TaskPermPersist tp : list)
            {
                System.out.println(name+"  "+tp.getTaskName());
                if (tp.getTaskName().equals(name))
                {
                    PermissionIFace defPerm = tp.getDefaultPerms();
                    defaultPermissionsHash.put(tp.getUserType(), defPerm);
                    
                    break;
                }
            }
        }

        if (false)
        {
            XStream xstream = new XStream();
            TaskPermPersist.config(xstream);
            
            try
            {
                Vector<TaskPermPersist> permList = new Vector<TaskPermPersist>();
                permList.add(new TaskPermPersist(name, "CollectionManager", true, true, true,true));
                permList.add(new TaskPermPersist(name, "Guest", true, true, true,true));
                permList.add(new TaskPermPersist(name+"XX", "CollectionManager", true, true, true,true));
                
                System.out.println("***** : "+name);
                FileUtils.writeStringToFile(new File("taskperms.xml"), xstream.toXML(permList)); //$NON-NLS-1$
                //System.out.println(xstream.toXML(config));
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getDefaultPermissions(java.lang.String)
     */
    @Override
    public PermissionIFace getDefaultPermissions(String userType)
    {
        defaultPermissionsHash = null;
        if (defaultPermissionsHash == null)
        {
            readAndSetDefPerms();
        }
        
        //System.err.println(name+"  "+userType+"  "+(defaultPermissionsHash != null ? defaultPermissionsHash.get(userType) : null));
        
        return defaultPermissionsHash != null ? defaultPermissionsHash.get(userType) : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
     */
    @Override
    public abstract SubPaneIFace getStarterPane();
    
    

    
    //---------------------------------------------------------------
    //--
    //---------------------------------------------------------------
    


}

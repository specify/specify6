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

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.helpers.XMLHelper;

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
    protected static final String COLLECTION_MANAGER = "CollectionManager";
    protected static final String GUEST              = "Guest";
    protected static final String DataENTRY          = "DataEntry";
    
    protected static WeakReference<Hashtable<String, PermissionOptionPersist>> taskPermsListWR = null;
    
    /**
     * @param name
     * @param title
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
    public static Hashtable<String, Hashtable<String, PermissionOptionPersist>> readDefaultPrefsFromXML(final String fileName)
    {
        Hashtable<String, Hashtable<String, PermissionOptionPersist>> hash = new Hashtable<String, Hashtable<String, PermissionOptionPersist>>();
        
        XStream xstream = new XStream();
        PermissionOptionPersist.config(xstream);
        
        String xmlStr = null;
        try
        {
            File permFile = new File(XMLHelper.getConfigDirPath("defaultperms" + File.separator + fileName)); //$NON-NLS-1$
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
            hash = (Hashtable<String, Hashtable<String, PermissionOptionPersist>>)xstream.fromXML(xmlStr);
            /*for (TaskPermPersist tp : list)
            {
                System.err.println("TP: "+tp.getTaskName()+"  "+tp.getUserType()+"  "+tp.isCanAdd());
            }*/
        }
        return hash;
    }
    
    /**
     * @return the Hashtable from the WeakReference
     */
    protected Hashtable<String, PermissionOptionPersist> getAndSetDefPerms()
    {
        if (taskPermsListWR == null)
        {
            taskPermsListWR = new WeakReference<Hashtable<String, PermissionOptionPersist>>(null);
        }
        
        Hashtable<String, PermissionOptionPersist> hash = taskPermsListWR.get();
        
        if (hash == null)
        {
            Hashtable<String, Hashtable<String, PermissionOptionPersist>> taskHash = readDefaultPrefsFromXML("task.xml");
            if (taskHash != null)
            {
                hash = taskHash.get(name);
                if (hash != null)
                {
                    taskPermsListWR = new WeakReference<Hashtable<String, PermissionOptionPersist>>(hash);
                }
            }
        }
        
        if (false)
        {
            XStream xstream = new XStream();
            PermissionOptionPersist.config(xstream);
            
            try
            {
                Hashtable<String, PermissionOptionPersist> hashItem = new Hashtable<String, PermissionOptionPersist>();
                hashItem.put("CollectionManager", new PermissionOptionPersist(name, "CollectionManager", true, true, true,true));
                hashItem.put("Guest",             new PermissionOptionPersist(name, "Guest", true, true, true,true));
                hashItem.put("DataEntry",        new PermissionOptionPersist(name, "DataEntry", true, true, true,true));
                
                Hashtable<String, Hashtable<String, PermissionOptionPersist>> mainHash = new Hashtable<String, Hashtable<String, PermissionOptionPersist>>();
                mainHash.put(name, hashItem);

                System.out.println("***** : "+name);
                FileUtils.writeStringToFile(new File("taskperms.xml"), xstream.toXML(mainHash)); //$NON-NLS-1$
                //System.out.println(xstream.toXML(config));
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return hash;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getDefaultPermissions(java.lang.String)
     */
    @Override
    public PermissionIFace getDefaultPermissions(String userType)
    {
        Hashtable<String, PermissionOptionPersist> hash = getAndSetDefPerms();
        
        //System.err.println(name+"  "+userType+"  "+(defaultPermissionsHash != null ? defaultPermissionsHash.get(userType) : null));
        
        return hash != null ? hash.get(userType).getDefaultPerms() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
     */
    @Override
    public abstract SubPaneIFace getStarterPane();
}

/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.af.auth.SecurityOption;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.CollectionRelationship;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.tasks.BaseTask;
import edu.ku.brc.specify.tasks.PermissionOptionPersist;

/**
 * This class enumerates Data Objects permissions associated with a principal in a given scope
 * 
 * @code_status Beta
 *
 * Aug 15, 2008
 *
 */
public class DataObjPermissionEnumerator extends PermissionEnumerator 
{
    protected List<SecurityOptionIFace> tableOptions = null;
    
	/**
     * 
     */
    public DataObjPermissionEnumerator()
    {
        super("DO", "ADMININFO_DESC");
    }
    
    /**
     * @param tblInfo
     * @return
     */
    protected boolean isTableOK(final DBTableInfo tblInfo)
    {
        if (tblInfo.getTableId() < 500)
        {
            String shortName = tblInfo.getShortClassName();
            if ((!shortName.startsWith("Workbench") || shortName.equals("Workbench")) &&
                 !shortName.equals("SpecifyUser"))
            {
                if (!(TreeDefItemIface.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      AttributeDef.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      CollectionObjectAttr.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      CollectingEventAttr.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      PreparationAttr.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      AutoNumberingScheme.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      CollectionRelationship.class.isAssignableFrom(tblInfo.getClassObj()) ||
                      CollectionRelType.class.isAssignableFrom(tblInfo.getClassObj())))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator#getSecurityOptions()
     */
    @Override
    protected List<SecurityOptionIFace> getSecurityOptions()
    {
        if (tableOptions == null)
        {
            tableOptions = new ArrayList<SecurityOptionIFace>();
            
            Hashtable<String, Hashtable<String, PermissionOptionPersist>> mainHash = BaseTask.readDefaultPrefsFromXML("prefsperms.xml");
            
            for (DBTableInfo tblInfo : DBTableIdMgr.getInstance().getTables())
            {
                if (isTableOK(tblInfo))
                {
                    SecurityOption securityOption = new SecurityOption(tblInfo.getName(), 
                                                                       tblInfo.getTitle(), 
                                                                       "DO",
                                                                       tblInfo.getName());
                    tableOptions.add(securityOption);
                    
                    if (mainHash != null)
                    {
                        Hashtable<String, PermissionOptionPersist> hash = mainHash.get(tblInfo.getName());
                        if (hash != null)
                        {
                            for (PermissionOptionPersist tp : hash.values())
                            {
                                PermissionIFace defPerm = tp.getDefaultPerms();
                                securityOption.addDefaultPerm(tp.getUserType(), defPerm);
                            }
                        }
                    }

                }
            }
        }
        return tableOptions;
    }
}

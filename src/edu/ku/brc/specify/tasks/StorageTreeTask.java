/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import javax.persistence.Transient;

import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
import edu.ku.brc.specify.datamodel.busrules.StorageBusRules;
import edu.ku.brc.ui.IconManager;

/**
 * Task that handles the UI for viewing storage data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class StorageTreeTask extends BaseTreeTask<Storage, StorageTreeDef, StorageTreeDefItem>
{
	public static final String STORAGE = "StorageTree";
	
	/**
	 * Constructor.
	 */
	public StorageTreeTask()
	{
        super(STORAGE, getResourceString(STORAGE));
        
        treeClass         = Storage.class;
        treeDefClass      = StorageTreeDef.class;
        icon              = IconManager.getIcon(STORAGE,IconManager.IconSize.Std16);
        
        menuItemText      = getResourceString("StorageMenu");
        menuItemMnemonic  = getResourceString("StorageMnemonic");
        starterPaneText   = getResourceString("StorageStarterPaneText");
        commandTypeString = STORAGE;
        
        businessRules     = new StorageBusRules();
        
        initialize();
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#getCurrentTreeDef()
     */
    @Transient
    @Override
    protected StorageTreeDef getCurrentTreeDef()
    {
        return Discipline.getCurrentDiscipline().getStorageTreeDef();
    }
}

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
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.forms.FormViewObj;

/**
 * Task that handles the UI for viewing storage data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class StorageTreeTask extends BaseTreeTask<Storage,StorageTreeDef,StorageTreeDefItem>
{
	public static final String LOCATION = "StorageTree";
	
	/**
	 * Constructor.
	 */
	public StorageTreeTask()
	{
        super(LOCATION, getResourceString(LOCATION));
        treeDefClass = StorageTreeDef.class;
        icon = IconManager.getIcon(LOCATION,IconManager.IconSize.Std16);
        
        menuItemText      = getResourceString("StorageMenu");
        menuItemMnemonic  = getResourceString("StorageMnemonic");
        starterPaneText   = getResourceString("StorageStarterPaneText");
        commandTypeString = LOCATION;
        
        initialize();
	}
    
    @Transient
    @Override
    protected StorageTreeDef getCurrentTreeDef()
    {
        return Discipline.getCurrentDiscipline().getStorageTreeDef();
    }

//    protected void adjustTreeDefForm(FormViewObj form)
//    {
//    }
//    
//    protected void adjustTreeDefItemForm(FormViewObj form)
//    {
//    }
    
    @Override
    public void adjustForm(FormViewObj form)
    {
        if (form.getDataObj() instanceof Storage || form.getViewDef().getClassName().equals(Storage.class.getName()))
        {
            adjustNodeForm(form);
        }
//        else if (form.getDataObj() instanceof StorageTreeDef)
//        {
//            adjustTreeDefForm(form);
//        }
//        else if (form.getDataObj() instanceof StorageTreeDefItem)
//        {
//            adjustTreeDefItemForm(form);
//        }
    }
}

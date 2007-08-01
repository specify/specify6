/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.forms.FormViewObj;

/**
 * Task that handles the UI for viewing location data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class LocationTreeTask extends BaseTreeTask<Location,LocationTreeDef,LocationTreeDefItem>
{
	public static final String LOCATION = "LocationTree";
	
	/**
	 * Constructor.
	 */
	public LocationTreeTask()
	{
        super(LOCATION, getResourceString(LOCATION));
        treeDefClass = LocationTreeDef.class;
        icon = IconManager.getIcon(LOCATION,IconManager.IconSize.Std24);
        
        menuItemText      = getResourceString("LocationMenu");
        menuItemMnemonic  = getResourceString("LocationMnemonic");
        starterPaneText   = getResourceString("LocationStarterPaneText");
        commandTypeString = LOCATION;
        
        initialize();
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
        if (form.getDataObj() instanceof Location)
        {
            adjustNodeForm(form);
        }
//        else if (form.getDataObj() instanceof LocationTreeDef)
//        {
//            adjustTreeDefForm(form);
//        }
//        else if (form.getDataObj() instanceof LocationTreeDefItem)
//        {
//            adjustTreeDefItemForm(form);
//        }
    }
}

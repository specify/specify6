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
        
        menuItemText     = getResourceString("LocationMenu");
        menuItemMnemonic = getResourceString("LocationMnemonic");
        starterPaneText  = getResourceString("LocationStarterPaneText");
        
        initialize();
	}
}

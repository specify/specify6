/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.ui.IconManager;

/**
 * Task that handles the UI for viewing geography data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class GeographyTreeTask extends BaseTreeTask<Geography,GeographyTreeDef,GeographyTreeDefItem>
{
	public static final String GEOGRAPHY = "GeographyTree";
	
	/**
	 * Constructor.
	 */
	public GeographyTreeTask()
	{
        super(GEOGRAPHY, getResourceString(GEOGRAPHY));
        treeDefClass = GeographyTreeDef.class;
        icon = IconManager.getIcon(GEOGRAPHY,IconManager.IconSize.Std24);
        
        menuItemText     = getResourceString("GeographyMenu");
        menuItemMnemonic = getResourceString("GeographyMnemonic");
        starterPaneText  = getResourceString("GeographyStarterPaneText");
        commandTypeString = GEOGRAPHY;
        
        initialize();
	}
}

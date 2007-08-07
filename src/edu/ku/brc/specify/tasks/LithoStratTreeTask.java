/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.ui.IconManager;

/**
 * Task that handles the UI for viewing litho stratigraphy data.
 * 
 * @code_status Beta
 * @author rods
 */
public class LithoStratTreeTask extends BaseTreeTask<LithoStrat,LithoStratTreeDef,LithoStratTreeDefItem>
{
	public static final String LITHO = "LithoStratTree";
	
	/**
	 * Constructor.
	 */
	public LithoStratTreeTask()
	{
        super(LITHO, getResourceString(LITHO));
        treeDefClass = LithoStratTreeDef.class;
        icon = IconManager.getIcon(LITHO, IconManager.IconSize.Std24);
        
        menuItemText     = getResourceString("LithoStratMenu");
        menuItemMnemonic = getResourceString("LithoStratMnemonic");
        starterPaneText  = getResourceString("LithoStratStarterPaneText");
        commandTypeString = LITHO;
        
        initialize();
	}
}

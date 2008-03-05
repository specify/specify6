/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import javax.persistence.Transient;

import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.busrules.LithoStratBusRules;
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
        icon         = IconManager.getIcon(LITHO, IconManager.IconSize.Std24);
        
        menuItemText     = getResourceString("LithoStratMenu");
        menuItemMnemonic = getResourceString("LithoStratMnemonic");
        starterPaneText  = getResourceString("LithoStratStarterPaneText");
        commandTypeString = LITHO;
        
        businessRules = new LithoStratBusRules();

        
        initialize();
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#getCurrentTreeDef()
     */
    @Transient
    @Override
    protected LithoStratTreeDef getCurrentTreeDef()
    {
        return Discipline.getCurrentDiscipline().getLithoStratTreeDef();
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#isTreeOnByDefault()
     */
    @Override
    protected boolean isTreeOnByDefault()
    {
        return Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.paleobotany) ||
               Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.invertpaleo) ||
               Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.vertpaleo);
    }
}

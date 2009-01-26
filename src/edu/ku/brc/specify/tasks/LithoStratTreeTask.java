/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import javax.persistence.Transient;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.busrules.LithoStratBusRules;

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
        
        treeClass        = LithoStrat.class;
        treeDefClass     = LithoStratTreeDef.class;
        
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
        return AppContextMgr.getInstance().getClassObject(Discipline.class).getLithoStratTreeDef();
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#isTreeOnByDefault()
     */
    @Override
    public boolean isTreeOnByDefault()
    {
        return Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.paleobotany) ||
               Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.invertpaleo) ||
               Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.vertpaleo);
    }
}

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
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.busrules.GeologicTimePeriodBusRules;
import edu.ku.brc.ui.IconManager;

/**
 * Task that handles the UI for viewing geologic time period data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class GtpTreeTask extends BaseTreeTask<GeologicTimePeriod,GeologicTimePeriodTreeDef,GeologicTimePeriodTreeDefItem>
{
	public static final String GTP = "GeoTimePeriodTree";
	
	/**
	 * Constructor.
	 */
	public GtpTreeTask()
	{
        super(GTP, getResourceString(GTP));
        
        treeDefClass = GeologicTimePeriodTreeDef.class;
        icon         = IconManager.getIcon(GTP,IconManager.IconSize.Std16);
        
        menuItemText     = getResourceString("GeoTimePeriodMenu");
        menuItemMnemonic = getResourceString("GeoTimePeriodMnemonic");
        starterPaneText  = getResourceString("GeoTimePeriodStarterPaneText");
        commandTypeString = GTP;
        
        businessRules = new GeologicTimePeriodBusRules();
        
        initialize();
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#getCurrentTreeDef()
     */
    @Transient
    @Override
    protected GeologicTimePeriodTreeDef getCurrentTreeDef()
    {
        return Discipline.getCurrentDiscipline().getGeologicTimePeriodTreeDef();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#isTreeOnByDefault()
     */
    @Override
    protected boolean isTreeOnByDefault()
    {
        return Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.paleo);
    }
}

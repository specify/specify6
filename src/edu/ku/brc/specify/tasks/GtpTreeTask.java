/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import javax.persistence.Transient;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.busrules.GeologicTimePeriodBusRules;

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
        
        treeClass        = GeologicTimePeriod.class;
        treeDefClass     = GeologicTimePeriodTreeDef.class;
        
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
        return (GeologicTimePeriodTreeDef )((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass(GeologicTimePeriod.class);
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

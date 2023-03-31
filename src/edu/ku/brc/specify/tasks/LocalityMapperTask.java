/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.tasks.subpane.LocalityMapperSubPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;

/**
 * The LocalityMapperTask show a special screen for examining Locality Infor with Collection info.
 *
 * @code_status Alpha
 *
 * @author rods
 *
 */
public class LocalityMapperTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(LocalityMapperTask.class);
    
    // Static Data Members
    public static final String LOCALITYMAPPER = "LocalityMapper";
    //private static final Logger log = Logger.getLogger(LocalityMapperTask.class);
    
    // Data Members
    protected java.sql.ResultSet    resultSet;

    // Data Members
    /**
     * Creates a Statistics Tasks
     *
     */
    public LocalityMapperTask()
    {
        super(LOCALITYMAPPER, getResourceString(LOCALITYMAPPER));
        
        CommandDispatcher.register(LOCALITYMAPPER, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return starterPane = new SimpleDescPane(name, this, "Map");
    }

    /**
     * @param recordSet
     */
    @SuppressWarnings("unchecked")
    public void createMappingInfoFromRecordSet(final RecordSetIFace recordSet)
    {
        
        
        String sqlStr = DBTableIdMgr.getInstance().getQueryForTable(recordSet);
        if (StringUtils.isNotEmpty(sqlStr))
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            List<CollectingEvent> list = (List<CollectingEvent>)session.getDataList(sqlStr);
            session.close();
            
            LocalityMapperSubPane panel = new LocalityMapperSubPane(name, this, list);
            addSubPaneToMgr(panel);
            
        } else
        {
            log.warn("Query String was empty.");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(LOCALITYMAPPER))
        {
            if (cmdAction.isAction("DoLocalityMap"))
            {
                if (cmdAction.getData() instanceof RecordSetIFace)
                {
                    RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
                    createMappingInfoFromRecordSet(recordSet);
                }
            }
        }
    }

    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------



}

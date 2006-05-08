/* Filename:    $RCSfile: ReportsTask.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;

import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.dbsupport.SQLExecutionListener;
import edu.ku.brc.specify.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.tasks.subpane.LocalityMapperSubPane;
import edu.ku.brc.specify.tasks.subpane.SimpleDescPane;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.SubPaneIFace;
/**
 * The LocalityMapperTask is responsible gettng and displaying all various idfferent kinds of stats
 * 
 * @author rods
 *
 */
public class LocalityMapperTask extends BaseTask
{
    // Static Data Members
    public static final String LOCALITYMAPPER = "LocalityMapper";
    
    private static Log log = LogFactory.getLog(LocalityMapperTask.class);
    
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
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(name, this, "Map");
    }
    
    /**
     * @param recordSet
     */
    public void createMappingInfoFromRecordSet(final RecordSet recordSet)
    {
        Query query = DBTableIdMgr.getQueryForTable(recordSet);
        
        List list = query.list();
        
        LocalityMapperSubPane panel = new LocalityMapperSubPane(name, this, list);
        addSubPaneToMgr(panel);

    }

    
    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        //ToolBarDropDownBtn      btn  = createToolbarButton(name, "locality.gif", "stats_hint");
        //list.add(new ToolBarItemDesc(btn));
        return list;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        
        return list;
        
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getAction().equals("DoLocalityMap"))
        {
            if (cmdAction.getData() instanceof RecordSet)
            {
                RecordSet recordSet = (RecordSet)cmdAction.getData();
                createMappingInfoFromRecordSet(recordSet);
            }
        }
    }
    
    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------

   

}

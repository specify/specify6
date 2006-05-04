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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.SQLExecutionListener;
import edu.ku.brc.specify.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.LocalityMapperSubPane;
import edu.ku.brc.specify.tasks.subpane.SimpleDescPane;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.ResultSetTableModelDM;
/**
 * The LocalityMapperTask is responsible gettng and displaying all various idfferent kinds of stats
 * 
 * @author rods
 *
 */
public class LocalityMapperTask extends BaseTask implements SQLExecutionListener
{
    // Static Data Members
    public static final String LOCALITYMAPPER = "LocalityMapper";
    
    private static Log log = LogFactory.getLog(LocalityMapperTask.class);
    
    // Data Members
    protected SQLExecutionProcessor sqlExecutor;
    protected java.sql.ResultSet resultSet;
    
    // Data Members
    /**
     * Creates a Statistics Tasks
     *
     */
    public LocalityMapperTask()
    {
        super(LOCALITYMAPPER, getResourceString(LOCALITYMAPPER));
  
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(name, this, "Map");
    }
    
    /**
     * Looks up statName and creates the appropriate SubPane 
     * @param statName the name of the stat to be displayed
     */
    public void createMapperPane(final List<Locality> localityList)
    {
        // Create stat pane return a non-null panel for charts and null for non-charts
        // Of coarse, it could pass back nul if a chart was missed named
        // but error would be shown inside the StatsMgr for that case
        LocalityMapperSubPane panel = new LocalityMapperSubPane(name, this, localityList);
        addSubPaneToMgr(panel);

    }
    
    public void doMapperDemo()
    {
        String sql = "Select collectingevent.StartDate, collectingevent.EndDate, locality.Latitude1, locality.Longitude1 From collectingevent Inner Join locality ON collectingevent.LocalityID = locality.LocalityID Where collectingevent.StartDate Is Not Null AND collectingevent.StartDate >= '19510701' AND collectingevent.StartDate <= '19510731' AND locality.Latitude1 Is Not Null AND locality.Longitude1 Is Not Null Order By collectingevent.StartDate Asc";
        sqlExecutor = new SQLExecutionProcessor(this, sql);
        sqlExecutor.setAutoCloseConnection(false);
        sqlExecutor.start();
        
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
        ToolBarDropDownBtn      btn  = createToolbarButton(name, "locality.gif", "stats_hint");      

        
        list.add(new ToolBarItemDesc(btn));
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
    
    
    //-----------------------------------------------------
    //-- SQLExecutionListener
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet resultSet)
    {
        this.resultSet = resultSet;
        
        try
        {
            StringBuilder strBuf = new StringBuilder();
            if (resultSet.first())
            {
                while (resultSet.next())
                {
                    
                }
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        ResultSetTableModelDM rsm = new ResultSetTableModelDM(resultSet);

        sqlExecutor = null;


    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public void executionError(final SQLExecutionProcessor process, final Exception ex)
    {
        sqlExecutor = null;
    }


    
    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------

   

}

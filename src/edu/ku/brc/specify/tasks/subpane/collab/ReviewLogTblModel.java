/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.collab;

import java.util.Random;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.services.geolocate.prototype.Locality;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.tasks.SGRTask;
import edu.ku.brc.ui.IconManager;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 29, 2011
 *
 */
public class ReviewLogTblModel extends DefaultTableModel
{
    protected String[] instTitles = {"KU", "AMNH", "UFL", "ILL", "WISC", 
                                        "UTK", "LSU", "FIELD", "DUKE", "MOBOT", 
                                        "UMICH", "BYU", "UAFB", "OSU", "ANSP", 
                                        "UWASH", "MSU", "BERK", "UMASS", "UCR", 
    };
    
    protected Class<?>[] classes = {String.class, String.class, Boolean.class, Integer.class, String.class};
    protected String[]   titles  = {"Description", "Action", "Is Batch", "Num of Records", "Source"};
    protected Class<?>[] dataCls = {CollectionObject.class, CollectingEvent.class, Locality.class, 
                                    Geography.class, Determination.class, Taxon.class, Workbench.class, 
                                    SGRTask.class};

    protected String[][] actionData = {
            {"Added Records", "Updated Record"}, // CO
            {"Updated Date", "Updated Field Number", "UPdated Collector Number"}, // CE
            {"New GeoReferenced", "Updated GeoReferenced"}, // LOC
            {"New Geography", "Updated Geography"}, // Geo
            {"New Determination", "Updated Determination", "Synominized"}, // Det
            {"Added Taxon", "Moved Taxon", "Synominized Taxon"}, // Tax
            {"Uploaded Collection Objects", "Collection Objects", "Added Images"}, // WB
            {"Updated Collection Object", "Updated Latitude/Longitude", "Updated Taxon"}, // SGR
    };
    
    private Vector<NotiLogInfo> data = new Vector<NotiLogInfo>();
    
    /**
     * 
     */
    public ReviewLogTblModel()
    {
        super();
        Random rand = new Random(System.currentTimeMillis());
        
        // initialize
        int num = rand.nextInt(15) + 15;
        for (int i=0;i<num;i++)
        {
            int clsIndex = rand.nextInt(dataCls.length);
            Class<?> cls = dataCls[clsIndex];
            String desc;
            Icon   icon;
            if (cls == SGRTask.class)
            {
              icon = IconManager.getIcon("SGR", IconManager.STD_ICON_SIZE.Std20);
              desc = "SGR";
            } else
            {
              icon = IconManager.getIcon(cls.getSimpleName(), IconManager.STD_ICON_SIZE.Std20);
              DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(cls.getSimpleName());
              desc = (ti != null ? ti.getTitle() : "X");
            }
            
            boolean isNewData = rand.nextDouble() > 0.5;
            boolean isBatch   = rand.nextDouble() > 0.5;
            
            String[] actionArray = actionData[clsIndex];
            int      actionIndex = rand.nextInt(actionArray.length);
            String   action      = actionArray[actionIndex];
            String   source      = instTitles[rand.nextInt(instTitles.length)];

            int numRecords = 0;
            if (isBatch)
            {
                numRecords = rand.nextInt(233) + 177;
                
            } else if (clsIndex == 6 && actionIndex == 2)
            {
                numRecords = rand.nextInt(5) + 1;
            }
            
            NotiLogInfo nli = new NotiLogInfo(icon, desc, isNewData, action, isBatch, numRecords, source);
            data.add(nli);
        }
    }
    
    /**
     * @return the data
     */
    public Vector<NotiLogInfo> getData()
    {
        return data;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getColumnCount()
     */
    @Override
    public int getColumnCount()
    {
        return titles != null ? titles.length : 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column)
    {
        return titles != null ? titles[column] : "";
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getRowCount()
     */
    @Override
    public int getRowCount()
    {
        return data != null ? data.size() : 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int column)
    {
        if (row > -1 && row < data.size())
        {
            NotiLogInfo ni = data.get(row);
            switch (column)
            {
                case 0: return ni.getDesc();
                //case 1: return ni.isNewData();
                case 1: return ni.getAction();
                case 2: return ni.isBatch();
                case 3: return ni.getNumRecords();
                case 4: return ni.getSource();
            }
        }
        //System
        return null;//super.getValueAt(row, column);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int col)
    {
        return col > 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int inx)
    {
        return classes != null ? classes[inx] : String.class;
    }
}

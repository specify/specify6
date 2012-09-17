/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.images;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 14, 2012
 *
 */
public class MetaDataPanel extends ExpandShrinkPanel
{
    private Integer attachmentId = null;
    
    //private ArrayList<JTable>        tables = new ArrayList<JTable>();
    //private ArrayList<MetaDataModel> models = new ArrayList<MetaDataModel>();
    
    private HashMap<String, HashMap<String, Object>> sectionMap = new HashMap<String, HashMap<String, Object>>();
    
    private JTabbedPane tabbedPane = null;
    
    /**
     * 
     */
    public MetaDataPanel()
    {
        super(CONTRACTED, true);
    }

    /**
     * 
     */
    public void createUI()
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "300px"), this);
        tabbedPane = new JTabbedPane();
        pb.add(tabbedPane, cc.xy(1, 1));
        
        super.doneBuilding();
    }
    
    /**
     * 
     */
    private void loadMetaData()
    {
        sectionMap.clear();
        AttachmentManagerIface attachmentMgr = AttachmentUtils.getAttachmentManager();

        String jsonStr = attachmentMgr.getMetaDataAsJSON(attachmentId);
        if (jsonStr != null)
        {
            JSONArray sections = JSONArray.fromObject(jsonStr);
            for (Object section : sections)
            {
                JSONObject obj = (JSONObject)section;
                for (Object key : obj.keySet())
                {
                    Object     name      = obj.get("Name");
                    JSONObject fieldsObj = (JSONObject)obj.get("Fields");
                    HashMap<String, Object> subMap = new HashMap<String, Object>();
                    
                    sectionMap.put(name.toString(), subMap);
                    
                    for (Object subKey : fieldsObj.keySet())
                    {
                        Object value = fieldsObj.get(subKey);
                        if (value != null)
                        {
                            subMap.put(subKey.toString(), value);
                        }
                    }
                }
            }
        }
        if (sectionMap != null && sectionMap.size() > 0)
        {
            updateMetaDataUI();
        }
    }
    
    /**
     * @param valuesMap
     */
    private void updateMetaDataUI()
    {
        tabbedPane.removeAll();
        
        for (String key : sectionMap.keySet())
        {
            HashMap<String, Object> map = sectionMap.get(key);
            if (map.size() > 0)
            {
                MetaDataModel model = new MetaDataModel(map);
                JTable table = new JTable(model);
                JScrollPane sp = UIHelper.createScrollPane(table);
                tabbedPane.addTab(key, sp);
            }
        }
    }
    
    /**
     * @return the attachmentId
     */
    public Integer getAttachmentId()
    {
        return attachmentId;
    }

    /**
     * @param attachmentId the attachmentId to set
     */
    public void setAttachmentId(Integer attachmentId)
    {
        this.attachmentId = attachmentId;
        
        tabbedPane.removeAll();
        if (this.attachmentId != null)
        {
            loadMetaData();
        }
    }

    class MetaDataModel extends DefaultTableModel
    {
        private ArrayList<Pair<String, Object>> values = new ArrayList<Pair<String, Object>>();
        /**
         * 
         */
        public MetaDataModel(final HashMap<String, Object> valuesMap)
        {
            super();
            for (String key : valuesMap.keySet())
            {
                Object obj = valuesMap.get(key);
                System.out.println(key);
                values.add(new Pair<String, Object>(key, obj));
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return values != null ? values.size() : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return 2;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return column == 0 ? "Name" : "Value";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            Pair<String, Object> p = values.get(row);
            return column == 0 ? p.first : (p.second != null ? p.second : "");
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        /*@Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return super.getColumnClass(columnIndex);
        }*/
        
    }
}

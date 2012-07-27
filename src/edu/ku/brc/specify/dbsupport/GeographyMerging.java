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
package edu.ku.brc.specify.dbsupport;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 26, 2012
 *
 */
public class GeographyMerging
{
    private Vector<Pair<String, Integer>> items = new Vector<Pair<String, Integer>>();
    private int               currIndex = 0;
    private CustomDialog      dlg;
    private GeoItemsModel     model;
    private JTable            table;
    private PreparedStatement pStmt;
    private Vector<Object[]>  modelList = new Vector<Object[]>();
    private String[]          levelNames = new String[3];
    
    /**
     * 
     */
    public GeographyMerging()
    {
        super();
        
    }
    
    /**
     * 
     */
    public void start()
    {
        for (int i=1;i<4;i++)
        {
            String sql = "SELECT Name FROM geographytreedefitem WHERE GeographyTreeDefID = GEOTREEDEFID AND RankID = "+(i*100);
            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            levelNames[i-1] = BasicSQLUtils.querySingleObj(sql);
        }
        
        String sql = "SELECT g1.Name, g2.Name, g3.Name, g1.GeographyID FROM geography g1 LEFT OUTER JOIN geography g2 ON g1.ParentID = g2.GeographyID " +
                     "INNER JOIN geography g3 ON g2.ParentID = g3.GeographyID WHERE g1.Name = ? AND g1.RankID = ?";
        
        try
        {
            pStmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        findDuplicates();
        
        model = new GeoItemsModel();
        table = new JTable(model);
        JScrollPane sp = UIHelper.createScrollPane(table, true);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        pb.add(sp, cc.xy(1,1));
        dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), getResourceString("GEO_MERGING"), true, CustomDialog.OKCANCELAPPLYHELP, pb.getPanel())
        {
            @Override
            protected void cancelButtonPressed() // Skip Button
            {
                next();
            }

            @Override
            protected void applyButtonPressed() // Accept Button
            {
                doAccept();
                next();
            }
        };
        dlg.setCancelLabel(getResourceString("GeoLocateResultsChooser.SKIP")); //$NON-NLS-1$
        dlg.setApplyLabel(getResourceString("GeoLocateResultsChooser.ACCEPT")); //$NON-NLS-1$
        dlg.setOkLabel(getResourceString("GeoLocateResultsChooser.QUIT")); //$NON-NLS-1$

        UIHelper.centerAndShow(dlg);
    }
    
    /**
     * 
     */
    private void next()
    {
        currIndex++;
        fillModel();
    }

    /**
     * 
     */
    private void doAccept()
    {
        
    }
    
    /**
     * 
     */
    private void findDuplicates()
    {
        String sql = "SELECT NM, CNT FROM (select NM, COUNT(NM) CNT from (SELECT CONCAT(Name, '|', RankID) NM FROM geography " +
        		     "WHERE RankID < 400) T0 GROUP BY NM) T1 WHERE CNT > 1 ORDER BY CNT DESC";
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            String[] parts = StringUtils.split((String)row[0], "|");
            Pair<String, Integer> item = new Pair<String, Integer>(parts[0], Integer.parseInt(parts[1]));
            items.add(item);
        }
    }
    
    /**
     * 
     */
    private void fillModel()
    {
        modelList.clear();
        
        try
        {
            Pair<String, Integer> item = items.get(currIndex);
            pStmt.setString(1,item.first);
            pStmt.setInt(2, item.second);
            ResultSet rs = pStmt.executeQuery();
            while (rs.next())
            {
                Object[] row = new Object[5];
                row[0] = Boolean.FALSE;
                for (int i=1;i<5;i++)
                {
                    row[i] = i < 4 ? rs.getString(i) : rs.getInt(i);
                }
                modelList.add(row);
            }
            rs.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    class GeoItemsModel extends DefaultTableModel
    {
        @Override
        public int getColumnCount()
        {
            return levelNames.length;
        }

        @Override
        public String getColumnName(int inx)
        {
            return levelNames[inx];
        }

        @Override
        public int getRowCount()
        {
            return modelList.size();
        }

        @Override
        public Object getValueAt(int row, int col)
        {
            Object[] rowData = modelList.get(row);
            return rowData[col];
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int col)
        {
            return col == 0 ? Boolean.class : String.class;
        }

        @Override
        public void setValueAt(Object value, int row, int column)
        {
            boolean hasValue = false;
            if (value != null && (Boolean)value)
            {
                for (int i=0;i<modelList.size();i++)
                {
                    modelList.get(i)[0] = Boolean.FALSE;
                }
                modelList.get(row)[0] = Boolean.TRUE;
                hasValue = true;
            }
            dlg.getApplyBtn().setEnabled(hasValue);
        }
    }
}

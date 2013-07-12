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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.ui.UIRegistry.displayErrorDlg;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.treeutils.TreeMergeException;
import edu.ku.brc.specify.treeutils.TreeMerger;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
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
    protected static final Logger log = Logger.getLogger(GeographyMerging.class);
    
    private Vector<Pair<String, Integer>> items = new Vector<Pair<String, Integer>>();
    private int               currRankId = 200;
    private int               currIndex  = 0;
    private CustomDialog      dlg;
    private GeoItemsModel     model;
    private JTable            table;
    private PreparedStatement pStmt;
    private Vector<ModelItem>  modelList = new Vector<ModelItem>();
    private String[]          levelNames = new String[4];
    private String[]          headers    = new String[5];
    private GeographyTreeDef  geoTreeDef;
    
    private Integer           primaryId      = null;
    private int               mergeIndex     = 0;
    private String            rankTitleKey   = "CLNUP_GEO_COUNTRIES"; 

    
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
    public boolean start()
    {
        DataProviderSessionIFace session = null;
        try
        {
            session    = DataProviderFactory.getInstance().createSession();
            GeographyTreeDef gtd = AppContextMgr.getInstance().getClassObject(Discipline.class).getGeographyTreeDef();
            geoTreeDef = session.get(GeographyTreeDef.class, gtd.getId());
            geoTreeDef.forceLoad();
            
        } catch (Exception ex)
        {
            return false;
            
        } finally
        {
            if (session != null) session.close();
        }
        
        headers[0] = getResourceString("CLNUP_PRIMARY");
        headers[1] = getResourceString("CLNUP_INCLUDE");
        for (int i=0;i<4;i++)
        {
            String sql = "SELECT Name FROM geographytreedefitem WHERE GeographyTreeDefID = GEOTREEDEFID AND RankID = "+(i*100);
            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            levelNames[3-i] = BasicSQLUtils.querySingleObj(sql);
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
        
        if (!findDuplicates())
        {
            return false;
        }
        
        model = new GeoItemsModel();
        table = new JTable(model);
        UIHelper.makeTableHeadersCentered(table, true);
        
        JScrollPane sp = UIHelper.createScrollPane(table, true);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        pb.add(sp, cc.xy(1,1));
        pb.setDefaultDialogBorder();
        dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), " ", true, CustomDialog.OKCANCELAPPLYHELP, pb.getPanel()) // Title is set later
        {
            @Override
            protected void cancelButtonPressed() // Skip Button
            {
                doNextGeographyInList();
            }

            @Override
            protected void applyButtonPressed() // Accept Button
            {
                doAccept();
            }
        };
        dlg.setCancelLabel(getResourceString("GeoLocateResultsChooser.SKIP")); //$NON-NLS-1$
        dlg.setApplyLabel(getResourceString("GeoLocateResultsChooser.ACCEPT")); //$NON-NLS-1$
        dlg.setOkLabel(getResourceString("GeoLocateResultsChooser.QUIT")); //$NON-NLS-1$
        
        dlg.createUI();
        
        // Start the process.
        currIndex = -1;
        doNextGeographyInList();

        dlg.pack();
        UIHelper.centerAndShow(dlg, 800, dlg.getSize().height);
        
        return true;
    }
    
    /**
     * 
     */
    private void doAccept()
    {
        primaryId = null;
        for (ModelItem mi: modelList)
        {
            if (mi.isPrimary)
            {
                primaryId = mi.geoId;
            }
        }
        
        if (primaryId != null)
        {
            mergeIndex = 0;
            mergeNext();
        }
    }

    /**
     * 
     */
    private void mergeNext()
    {
        final Integer fromId = modelList.get(mergeIndex).geoId;
        log.debug(String.format("Merging From %d to %d  Index: %d  Size: %d", fromId, primaryId, mergeIndex, modelList.size()));
        mergeIndex++;
        
        if (fromId.equals(primaryId))
        {
            if (mergeIndex < modelList.size())
            {
                mergeNext();
                return;
                
            } else if (currIndex < items.size())
            {
                doNextGeographyInList();
                return;
            }
        }
        
        final TreeMerger<Geography,GeographyTreeDef,GeographyTreeDefItem> merger = new TreeMerger<Geography,GeographyTreeDef,GeographyTreeDefItem>(geoTreeDef);
        new javax.swing.SwingWorker<Object, Object>() 
        {
            Boolean   result = false;
            Exception killer = null;

            @Override
            protected Object doInBackground() throws Exception
            {
                try
                {
                    Integer parentId = BasicSQLUtils.getCount("SELECT ParentID FROM geography WHERE GeographyID = "+primaryId);
                    merger.mergeTrees(fromId, parentId);
                    result = true;
                    
                } catch (Exception ex)
                {
                    log.error(ex);
                    result = false;
                    killer = ex;
                }
                return result;
            }

            @Override
            protected void done() 
            {
                if (result)
                {
                    try
                    {
                        geoTreeDef.updateAllNodes(null, true, true);
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeTableViewer.class, ex);
                    }
                }
                
                if (!result && killer != null)
                {
                    if (killer instanceof TreeMergeException)
                    {
                        displayErrorDlg(killer.getMessage());
                    }
                    else 
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture( TreeTableViewer.class, killer);
                    }
                } else
                {
                    if (mergeIndex < modelList.size())
                    {
                        SwingUtilities.invokeLater(new Runnable() 
                        {
                            @Override
                            public void run()
                            {
                                mergeNext();
                            }
                        });
                    } else
                    {
                        doNextGeographyInList();
                    }
                }
            }
            
        }.execute();
    }
    
    /**
     * 
     */
    private void doStates()
    {
        if (currRankId == 200)
        {
            rankTitleKey = "CLNUP_GEO_STATES"; 
            currRankId   = 300;

            new javax.swing.SwingWorker<Boolean, Boolean>() 
            {
                boolean hasDups = false;
    
                @Override
                protected Boolean doInBackground() throws Exception
                {
                    return hasDups = findDuplicates();
                }
                
                @Override
                protected void done()
                {
                    if (hasDups)
                    {
                        currIndex = -1;
                        doNextGeographyInList();
                    }
                }
            }.execute();
        } else
        {
            dlg.setVisible(false);
        }
    }
    
    /**
     * 
     */
    private boolean findDuplicates()
    {
        items.clear();
        String sql = String.format("SELECT NM, CNT FROM (select NM, COUNT(NM) CNT from (SELECT CONCAT(Name, '|', RankID) NM FROM geography " +
        		                   "WHERE RankID = %d) T0 GROUP BY NM) T1 WHERE CNT > 1 ORDER BY CNT DESC", currRankId);
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            String[] parts = StringUtils.split((String)row[0], "|");
            Pair<String, Integer> item = new Pair<String, Integer>(parts[0], Integer.parseInt(parts[1]));
            System.out.println(String.format("Added [%s] %d", item.first, item.second));
            items.add(item);
        }
        
        if (items.size() == 0)
        {
           // Display Message here
           return false; 
        }
        return true;
    }
    
    /**
     * 
     */
    private void doNextGeographyInList()
    {
        dlg.getApplyBtn().setEnabled(false);
        currIndex++;
        if (currIndex < items.size())
        {
            modelList.clear();
            
            try
            {
                Pair<String, Integer> item = items.get(currIndex);
                int rankId = item.second;
                int inx    = rankId == 200 ? 1 : 0;
                for (int i=2;i<5;i++)
                {
                    headers[i] = levelNames[inx++];
                }
                pStmt.setString(1,item.first);
                pStmt.setInt(2, rankId);
                ResultSet rs = pStmt.executeQuery();
                while (rs.next())
                {
                    ModelItem modelItem = new ModelItem(false, true, rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4));
                    modelList.add(modelItem);
                }
                rs.close();
                
                dlg.setTitle(UIRegistry.getFormattedResStr("CLNUP_GEO_MERGING", getResourceString(rankTitleKey), (currIndex+1), items.size()));
                model.fireTableStructureChanged();
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }            
        } else
        {
            doStates();
        }
    }
    
    //---------------------------------------------------------------------
    class ModelItem
    {
        boolean isPrimary;
        boolean isIncluded;
        String geoName1;
        String geoName2;
        String geoName3;
        int    geoId;
        
        /**
         * @param isPrimary
         * @param isIncluded
         * @param geoName1
         * @param geoName2
         * @param geoName3
         * @param geoId
         */
        public ModelItem(boolean isPrimary, boolean isIncluded, String geoName1, String geoName2,
                String geoName3, int geoId)
        {
            super();
            this.isPrimary = isPrimary;
            this.isIncluded = isIncluded;
            this.geoName1 = geoName1;
            this.geoName2 = geoName2;
            this.geoName3 = geoName3;
            this.geoId = geoId;
        }
    }

  
    //---------------------------------------------------------------------
    class GeoItemsModel extends DefaultTableModel
    {
        @Override
        public int getColumnCount()
        {
            return headers.length;
        }

        @Override
        public String getColumnName(int inx)
        {
            return headers[inx];
        }

        @Override
        public int getRowCount()
        {
            return modelList.size();
        }

        @Override
        public Object getValueAt(int row, int col)
        {
            ModelItem item = modelList.get(row);
            switch (col)
            {
                case 0: return item.isPrimary;
                case 1: return item.isIncluded;
                case 2: return item.geoName1;
                case 3: return item.geoName2;
                case 4: return item.geoName3;
                case 5: return item.geoId;
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column < 2;
        }

        @Override
        public Class<?> getColumnClass(int col)
        {
            return col < 2 ? Boolean.class : String.class;
        }

        @Override
        public void setValueAt(Object value, int row, int column)
        {
            int primaryIndex = -1;
            if (column == 0)
            {
                if (value != null && (Boolean)value)
                {
                    for (int i=0;i<modelList.size();i++)
                    {
                        modelList.get(i).isPrimary = false;
                    }
                    modelList.get(row).isPrimary = true;
                    primaryIndex = row;
                    
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            fireTableDataChanged();
                        }
                    });
                }
            } else
            {
                for (int i=0;i<modelList.size();i++)
                {
                    if (modelList.get(i).isPrimary)
                    {
                        primaryIndex = i;
                        break;
                    }
                }
            }
            
            if (column == 1)
            {
                modelList.get(row).isIncluded = (Boolean)value;
            }
            
            boolean hasInclude = false;
            for (int i=0;i<modelList.size();i++)
            {
                if (i == primaryIndex)
                {
                    modelList.get(i).isIncluded = true;
                } else if (modelList.get(i).isIncluded)
                {
                    hasInclude = true;
                }
            }
            dlg.getApplyBtn().setEnabled(hasInclude && primaryIndex > -1);
        }
    }
    
}

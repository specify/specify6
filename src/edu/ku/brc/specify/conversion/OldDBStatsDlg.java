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
package edu.ku.brc.specify.conversion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Triple;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 26, 2009
 *
 */
public class OldDBStatsDlg extends CustomDialog
{
    protected Connection oldDBConn;
    protected Vector<Triple<String, Integer, Boolean>> statsData = new Vector<Triple<String,Integer,Boolean>>();
    protected JCheckBox  doFixAgentsChkbx;
    /**
     * @param oldDBConn
     */
    public OldDBStatsDlg(final Connection oldDBConn)
    {
        super((Frame)UIRegistry.getMostRecentWindow(), "Source DB Statistic", true, null);
        this.oldDBConn = oldDBConn;
    }
    

    @Override
    public void createUI()
    {
        setOkLabel("Continue");
        
        super.createUI();
        
        String[] queries = {"SELECT COUNT(*) FROM collectionobject co1 LEFT JOIN collectionobject co2 ON co1.DerivedFromID = co2.CollectionObjectID WHERE co1.DerivedFromID is not NULL AND co2.CollectionObjectID is NULL",
                            "SELECT COUNT(*) FROM taxonname WHERE RankID iS NULL",
                            "SELECT COUNT(*) FROM collectionobject",
                            "SELECT COUNT(*) FROM collectionobjectcatalog",
                            "SELECT COUNT(*) FROM taxonname",
                            "SELECT COUNT(*) FROM determination",
                            "SELECT COUNT(*) FROM agent",
                            "SELECT COUNT(*) FROM agent WHERE LENGTH(Name) > 50",
                            "SELECT COUNT(*) FROM agent WHERE LENGTH(LastName) > 50",
                            "SELECT COUNT(*) FROM agent WHERE LastName LIKE '%;%'",
                            "SELECT COUNT(c.AgentID) FROM collectingevent ce INNER JOIN collectors c ON ce.CollectingEventID = c.CollectingEventID " + 
                                "INNER JOIN agent a ON c.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'",
                            "SELECT COUNT(*) FROM authors au INNER JOIN agent a ON au.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'",
                            "SELECT COUNT(*) FROM accessionagents aagt INNER JOIN agentaddress aa ON aagt.AgentAddressID = aa.AgentAddressID " +
                                "INNER JOIN agent a ON aa.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'", 
                            "SELECT COUNT(*) FROM borrowagents ba INNER JOIN agentaddress aa ON ba.AgentAddressID = aa.AgentAddressID INNER JOIN agent a ON aa.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'",
                            "SELECT COUNT(*) FROM loanagents la INNER JOIN agentaddress aa ON la.AgentAddressID = aa.AgentAddressID INNER JOIN agent a ON aa.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'",
                            "SELECT COUNT(ID) FROM (SELECT DISTINCT ce.LocalityID as ID FROM collectingevent ce LEFT JOIN locality loc ON loc.localityid = ce.LocalityID  WHERE loc.LocalityID IS NULL) AS T1", 
                            "SELECT COUNT(*) FROM shipment",
                            null,
                            };
        
        String[] descs = {"Stranded Preparations",
                          "Number of Taxon with a NULL RankId",
                          "CollectionObjects",
                          "Collection Object Catalogs",
                          "Taxon",
                          "Determinations",
                          "Agents",
                          "Agent Names Truncated",
                          "Agent Last Names Truncated",
                          "Agent Last Names with ';'",
                          "Collector Last Names with ';'",
                          "Author Last Names with ';'",
                          "Accession Agents Last Names with ';'",
                          "Borrow Agent Last Names with ';'",
                          "Loan Agent Last Names with ';'",
                          "Collecting Events with NULL Localities",
                          "Number of Shipments",
                          "Stranded Agents Last Names with ';'",
                          };
        
        Integer[] errors = {0, 
                            Integer.MAX_VALUE, 
                            Integer.MAX_VALUE, 
                            Integer.MAX_VALUE, 
                            Integer.MAX_VALUE, 
                            Integer.MAX_VALUE, 
                            Integer.MAX_VALUE, 
                            0, 
                            0, 
                            0, 
                            0,
                            0, // Authors
                            0, // Accession Agents
                            0, // Stranded
                            0, // Borrow
                            0, // Loan
                            0, // CE no Locality
                            0, // # Shipments
                            };
        
        for (int i=0;i<queries.length;i++)
        {
            
            int count = queries[i] != null ? BasicSQLUtils.getCountAsInt(oldDBConn, queries[i]) : 0;
            
            statsData.add(new Triple<String, Integer, Boolean>(descs[i], count, errors[i] != null && count > errors[i]));
        }
        
        Triple<String, Integer, Boolean> stranded = statsData.get(errors.length-1);
        stranded.second = getStrandedAgentsCount();
        stranded.third  = stranded.second > errors[errors.length-1];
        
        JTable table = new JTable(new StatsModel());
        table.setPreferredScrollableViewportSize(new Dimension( table.getPreferredScrollableViewportSize().width, 10*table.getRowHeight()));
        table.getColumnModel().getColumn(0).setCellRenderer(new StatTblCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new StatTblCellRenderer());

        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "f:p:g,10px,p,2px,f:p:g,4px,p"));
        pb.add(UIHelper.createScrollPane(table, true), cc.xyw(1, 1, 3));
        
        String sql = "SELECT DISTINCT(CNAME), L, F, M FROM (SELECT a.LastName AS L, a.FirstName AS F, a.MiddleInitial AS M, " +
                     "CONCAT(a.LastName, CONCAT(IFNULL(a.FirstName, ''), IFNULL(a.MiddleInitial, ''))) AS CNAME FROM " + 
                     "collectingevent ce INNER JOIN collectors c ON ce.CollectingEventID = c.CollectingEventID " + 
                     "INNER JOIN agent a ON c.AgentID = a.AgentID " + 
                     "WHERE a.LastName LIKE '%;%' OR a.FirstName LIKE '%,%') T1";
        
        Vector<Object[]> collRows = BasicSQLUtils.query(oldDBConn, sql);
        
        Object[][] rows = new Object[collRows.size()][4];
        int i = 0;
        for (Object[] row : collRows)
        {
            rows[i][0] = row[0];
            rows[i][1] = row[1];
            rows[i][2] = row[2];
            rows[i][3] = row[3];
            i++;
        }
        
        pb.addSeparator("Collectors with Semicolons", cc.xyw(1, 3, 3));
        JTable collTable = new JTable(rows, new Object[] {"Combined", "Last Name", "First Name", "Middle"});
        pb.add(UIHelper.createScrollPane(collTable, true), cc.xyw(1, 5, 3));
        
        collTable.setPreferredScrollableViewportSize(new Dimension( collTable.getPreferredScrollableViewportSize().width, 10*collTable.getRowHeight()));
        
        doFixAgentsChkbx = UIHelper.createCheckBox("Fix Agents");
        pb.add(doFixAgentsChkbx, cc.xyw(3, 7, 1));
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
    }
    
    /**
     * @return
     */
    private int getStrandedAgentsCount()
    {
        
        String[] queries = {
                "SELECT AgentID FROM agent WHERE LastName LIKE '%;%'",
                "SELECT c.AgentID FROM collectingevent ce INNER JOIN collectors c ON ce.CollectingEventID = c.CollectingEventID " + 
                    "INNER JOIN agent a ON c.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'",
                "SELECT au.AgentID FROM authors au INNER JOIN agent a ON au.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'",
                "SELECT a.AgentID FROM accessionagents aagt INNER JOIN agentaddress aa ON aagt.AgentAddressID = aa.AgentAddressID " +
                    "INNER JOIN agent a ON aa.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'", 
                "SELECT a.AgentID FROM borrowagents ba INNER JOIN agentaddress aa ON ba.AgentAddressID = aa.AgentAddressID INNER JOIN agent a ON aa.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'",
                "SELECT a.AgentID FROM loanagents la INNER JOIN agentaddress aa ON la.AgentAddressID = aa.AgentAddressID INNER JOIN agent a ON aa.AgentID = a.AgentID WHERE a.LastName LIKE '%;%'",
                };
        
        Hashtable<Integer, Boolean> hash = new Hashtable<Integer, Boolean>();
        int cnt = 0;
        for (String sql : queries)
        {
            ArrayList<Integer> ids = getIds(sql);
            if (cnt == 0)
            {
                for (Integer id : ids)
                {
                    hash.put(id, true);
                }
            } else
            {
                for (Integer id : ids)
                {
                    if (hash.get(id) != null)
                    {
                        hash.remove(id);
                    }
                }
            }
            cnt++;
        }
        return hash.size();
    }
    
    /**
     * @param sql
     * @return
     */
    private ArrayList<Integer> getIds(final String sql)
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        
        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs   = stmt.executeQuery(sql);
    
            while (rs.next())
            {
                list.add(rs.getInt(1));
            }
            rs.close();
            stmt.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return list;
    }
    
    /**
     * @return whether to fix the agents
     */
    public boolean doFixAgents()
    {
        return doFixAgentsChkbx.isSelected();
    }

    
    class StatsModel extends AbstractTableModel
    {
        String[] titles = new String[] {"Description", "Count"};
        
        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnIndex == 0 ? String.class : Integer.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return titles[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return titles != null ? titles.length : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return statsData.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Triple<String, Integer, Boolean> triple = statsData.get(rowIndex);
            return columnIndex == 0 ? triple.first : triple.second;
        }
    }
    
    class StatTblCellRenderer extends DefaultTableCellRenderer
    {
        protected Color red = new Color(255, 192,192);
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Triple<String, Integer, Boolean> triple = statsData.get(row);
            String text = column == 0 ? triple.first : triple.second.toString();
            lbl.setText(text);
            lbl.setBackground(triple.third ? red : Color.WHITE);
            //lbl.setForeground(triple.third ? Color.YELLOW : Color.BLACK);
            return lbl;
        }
        
    }
}

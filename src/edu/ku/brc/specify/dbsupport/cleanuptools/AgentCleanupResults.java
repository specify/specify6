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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Dialog;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel.RowInfo;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 29, 2010
 *
 */
public class AgentCleanupResults extends BaseCleanupResults
{
    protected String selectColNames = null;
    /**
     * 
     */
    public AgentCleanupResults(final BaseFindCleanupItems.ItemInfo itemInfo)
    {
        super("Agent Cleanup", itemInfo);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.cleanuptools.BaseCleanup#createAndFillModels()
     */
    @Override
    protected void createAndFillModels()
    {
        model = new DataObjTableModel(Agent.getClassTableId(), itemInfo.value.toString(), false)
        {
            /* (non-Javadoc)
             * @see edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel#buildSQL()
             */
            @Override
            protected String buildSQL()
            {
                DBTableInfo addrTableInfo = DBTableIdMgr.getInstance().getInfoById(Address.getClassTableId());
                
                String agentKey = tableInfo.getIdColumnName();
                String addrKey  = addrTableInfo.getIdColumnName();

                String[]        skipFields = {"Ordinal", "TimestampModified", "TimestampCreated", "Version", agentKey, addrKey};
                HashSet<String> skipHash   = new HashSet<String>();
                for (String s : skipFields)
                {
                    skipHash.add(s);
                }
                
                numColumns = 0;
                StringBuilder fsb = new StringBuilder("ag."+agentKey);
                colDefItems.add(new DBFieldInfo(tableInfo, agentKey, Integer.class));
                
                for (DBFieldInfo fi : tableInfo.getFields())
                {
                    if (skipHash.contains(fi.getColumn())) continue;
                    
                    if (fsb.length() > 0) fsb.append(',');
                    fsb.append("ag."+fi.getColumn());
                    colDefItems.add(fi);
                    numColumns++;
                }
                fsb.append(",ad."+addrKey);
                colDefItems.add(new DBFieldInfo(addrTableInfo, addrKey, Integer.class));
                
                for (DBFieldInfo fi : addrTableInfo.getFields())
                {
                    if (skipHash.contains(fi.getColumn())) continue;
                    
                    if (fsb.length() > 0) fsb.append(',');
                    fsb.append("ad."+fi.getColumn());
                    colDefItems.add(fi);
                    numColumns++;
                }
                selectColNames = fsb.toString();
                
                return String.format("SELECT %s FROM agent ag LEFT JOIN address ad on ag.AgentID = ad.AgentID WHERE LastName = ?", selectColNames);
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel#addAdditionalRows(java.util.ArrayList)
             */
            @Override
            protected void addAdditionalRows(final ArrayList<DBInfoBase> colDefItems,
                                             final ArrayList<RowInfo> rowInfoList)
            {
                HashSet<Integer> existingIdsHash = new HashSet<Integer>();
                for (RowInfo ri : rowInfoList)
                {
                    existingIdsHash.add(ri.getId());
                }
                
                Connection conn = DBConnection.getInstance().getConnection();
                String sqlFmt = String.format("SELECT %s FROM agent ag LEFT JOIN address ad on ag.AgentID = ad.AgentID WHERE ag.LastName LIKE ? AND ag.LastName IS NOT NULL ORDER BY ag.LastName, ag.TimestampCreated ASC", selectColNames);

                PreparedStatement pStmt = null;
                try
                {
                    int len = 4;
                    String lastName = (String)itemInfo.value.toString();
                    if (lastName.length() > len)
                    {
                        String partialLastName = lastName.substring(0, len-1) +"%";
                        pStmt = conn.prepareStatement(sqlFmt);
                        pStmt.setString(1, partialLastName);
                        System.out.println(sqlFmt+" ["+partialLastName+"]");
                        ResultSet rs = pStmt.executeQuery();
                        while (rs.next())
                        {
                            int id = rs.getInt(1);
                            if (!existingIdsHash.contains(id))
                            {
                                Object[] row = new Object[numColumns];
                                for (int i=0;i<numColumns;i++)
                                {
                                    row[i] = rs.getObject(i+1);
                                }
                                values.add(row);
                                rowInfoList.add(new RowInfo(rs.getInt(1), false, false));
                            }
                        }
                        rs.close();
                        pStmt.close();
                    }
                        
                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                }
                
                int agentTypeInx = -1;
                int i =0;
                altClasses = new ArrayList<Class<?>>();
                for (DBInfoBase bi : colDefItems)
                {
                    DBFieldInfo fi = (DBFieldInfo)bi;
                    if (fi.getColumn().equals("AgentType"))
                    {
                        altClasses.add(String.class);
                        agentTypeInx = i;
                    } else
                    {
                        altClasses.add(fi.getDataClass());
                    }
                    i++;
                }
                
                if (agentTypeInx > -1)
                {
                    String[] types = {"ORG", "PERSON", "OTHER", "GROUP"};
                    for (int j=0;j<types.length;j++)
                    {
                        types[j] = UIRegistry.getResourceString("Agent_"+types[j]); 
                    }
                    for (int ii=0;ii<values.size();ii++)
                    {
                        Object[] cols      = values.get(ii);
                        Integer  agentType = (Integer)cols[agentTypeInx];
                        DualValue p        = new DualValue(types[agentType], agentType);
                        cols[agentTypeInx] = p;
                    }
                }
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel#adjustHasDataColumns()
             */
            @Override
            protected void adjustHasDataColumns()
            {
                int inx = tableInfo.getFields().size() - 2;
                if (hasDataList.get(inx))
                {
                    hasDataList.set(inx, false);
                    hasDataCols--;
                }
            }
            
        };
        
        newModel = new DataObjTableModel(Agent.getClassTableId(), model.getItems(), model.getHasDataList(), model.getSameValues(), model.getMapInx(), model.getIndexHash());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.cleanuptools.BaseCleanupResults#doFixDuplicates()
     */
    @Override
    protected void doFixDuplicates()
    {
        Integer          mainId      = null;
        HashSet<Integer> idsToChange = new HashSet<Integer>();
        for (RowInfo ri : model.getRowInfoList())
        {
            if (ri.isMainRecord)
            {
                mainId = ri.getId();
                
            } else
            {
                idsToChange.add(ri.getId());
            }
        }
        
        for (Integer id : new HashSet<Integer>(idsToChange))
        {
            if (id.equals(mainId))
            {
                idsToChange.remove(id);
            }
        }
        if (idsToChange.size() == 0)
        {
            return;
        }
        StringBuilder idsStrSB = new StringBuilder();
        for (Integer id : idsToChange)
        {
            if (idsStrSB.length() > 0) idsStrSB.append(',');
            idsStrSB.append(id);
        }
        String idsStr = '(' + idsStrSB.toString() + ')';
        System.out.println("Main: "+mainId+"  ids: "+idsStrSB.toString());
        
        // check for valid deletions
        if (!checkForValidDeletions(mainId, idsToChange))
        {
            return;
        }
        
        HashMap<DBTableInfo, Vector<DBRelationshipInfo>> tablesToBeFixed = new HashMap<DBTableInfo, Vector<DBRelationshipInfo>>();
        //HashMap<DBRelationshipInfo, HashSet<Integer>>    idsInRels       = new HashMap<DBRelationshipInfo, HashSet<Integer>>();
        
        int totalCnt = 0;
        ArrayList<Pair<String, Integer>> cntPairs = new ArrayList<Pair<String, Integer>>();
        
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            Vector<DBRelationshipInfo> rels = null;
            int totalForTable = 0;
            for (DBRelationshipInfo ri : ti.getRelationships())
            {
                if (ri.getDataClass() == Agent.class)
                {
                    String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s IN %s", ti.getName(), ri.getColName(), idsStr);
                    int cnt = BasicSQLUtils.getCountAsInt(sql);
                    if (cnt > 0)
                    {
                        System.out.println(String.format("Found %d in %s.%s", cnt, ti.getName(), ri.getColName()));
                        totalForTable += cnt;
                        totalCnt      += cnt;
                        
                        if (rels == null)
                        {
                            rels = new Vector<DBRelationshipInfo>();
                        }
                        rels.add(ri);
                    }
                }
            }
            
            if (totalForTable > 0)
            {
                cntPairs.add(new Pair<String, Integer>(ti.getTitle(), totalForTable));
                tablesToBeFixed.put(ti, rels);
            }
        }
        
        if (totalCnt > 0)
        {
            displayTotals(cntPairs, totalCnt);
            
            // Fix
            for (DBTableInfo ti : tablesToBeFixed.keySet())
            {
                Vector<DBRelationshipInfo> rels = tablesToBeFixed.get(ti);
                for (DBRelationshipInfo ri : rels)
                {
                    for (Integer id : idsToChange)
                    {
                        String sql = String.format("UPDATE %s SET %s=%d WHERE %s = %d", ti.getName(), ri.getColName(), mainId, ri.getColName(), id);
                        System.out.println(sql);
                    }
                }
            }
        }

        // Remove the unneeded Agents
        
        for (Integer id : idsToChange)
        {
            String sql = String.format("DELETE FROM agent WHERE AgentID = %d", id);
            System.out.println(sql);
        }
    }
    
    /**
     * @param errMsgs
     * @param msg
     * @return
     */
    private StringBuilder addErrMsg(final StringBuilder errMsgs, final String msg)
    {
        StringBuilder msgs = errMsgs;
        if (msgs == null)
        {
            msgs = new StringBuilder();
        }
        msgs.append(msg);
        msgs.append("<BR>");
        return msgs;
    }
    
    /**
     * @param mainId
     * @param idsToChange
     * @return
     */
    protected boolean checkForValidDeletions(final Integer mainId, final HashSet<Integer> idsToChange)
    {
        HashSet<String> idsInErrorHash = new HashSet<String>();
        
        StringBuilder errMsgs = null;
        String sqlFmt = "SELECT id, cnt, num  FROM (SELECT l.LoanID as id, COUNT(l.LoanID) as cnt, l.LoanNumber as num " +
                        "FROM loanagent la INNER JOIN loan l ON la.LoanID = l.LoanID WHERE la.AgentID in (%d,%d) GROUP BY l.LoanID) T1 WHERE cnt > 1";
        for (Integer id : idsToChange)
        {
            String sql = String.format(sqlFmt, mainId, id);
            for (Object[] row : BasicSQLUtils.query(sql))
            {
                String str = row[2].toString();
                if (!idsInErrorHash.contains(str))
                {
                    errMsgs = addErrMsg(errMsgs, String.format("For Loan %s it is using both Agents.", str));
                    idsInErrorHash.add(str);
                }
            }
        }
        
        sqlFmt = "SELECT id, cnt, num  FROM (SELECT ce.CollectingEventID as id, COUNT(ce.CollectingEventID) as cnt, ce.StartDate as num " +
                 "FROM collector c INNER JOIN collectingevent ce ON c.CollectingEventID = ce.CollectingEventID WHERE c.AgentID in (%d,%d) GROUP BY ce.CollectingEventID) T1 WHERE cnt > 1";
        for (Integer id : idsToChange)
        {
            String sql = String.format(sqlFmt, mainId, id);
            for (Object[] row : BasicSQLUtils.query(sql))
            {
                String str = row[2].toString();
                if (!idsInErrorHash.contains(str))
                {
                    errMsgs = addErrMsg(errMsgs, String.format("For CollectingEvent %s it is using both Agents.", str));
                    idsInErrorHash.add(str);
                }
            }
        }
        
        sqlFmt = "SELECT id, cnt, num  FROM (SELECT a.AccessionID as id, COUNT(a.AccessionID) as cnt, a.AccessionNumber as num " +
                 "FROM accessionagent aa INNER JOIN accession a ON aa.AccessionID = a.AccessionID WHERE aa.AgentID in (%d,%d) GROUP BY a.AccessionID) T1 WHERE cnt > 1";
        for (Integer id : idsToChange)
        {
            String sql = String.format(sqlFmt, mainId, id);
            for (Object[] row : BasicSQLUtils.query(sql))
            {
                String str = row[2].toString();
                if (!idsInErrorHash.contains(str))
                {
                    errMsgs = addErrMsg(errMsgs, String.format("For Accession %s it is using both Agents.", str));
                    idsInErrorHash.add(str);
                }
           }
        }
        
        sqlFmt = "SELECT id, cnt, num  FROM (SELECT r.RepositoryAgreementID as id, COUNT(r.RepositoryAgreementID) as cnt, r.RepositoryAgreementNumber as num " +
                 "FROM accessionagent a INNER JOIN repositoryagreement r ON a.RepositoryAgreementID = r.RepositoryAgreementID WHERE a.AgentID in (%d,%d) GROUP BY r.RepositoryAgreementID) T1 WHERE cnt > 1";
        for (Integer id : idsToChange)
        {
            String sql = String.format(sqlFmt, mainId, id);
            for (Object[] row : BasicSQLUtils.query(sql))
            {
                String str = row[2].toString();
                if (!idsInErrorHash.contains(str))
                {
                    errMsgs = addErrMsg(errMsgs, String.format("For RepositoryAgreement %s it is using both Agents.", str));
                    idsInErrorHash.add(str);
                }
            }
        }
        
        if (errMsgs != null)
        {
            JEditorPane pane = new JEditorPane("text/html", getResourceString("UNHDL_EXCP"));
            pane.setEditable(false);
            pane.setOpaque(false);
            pane.setText(errMsgs.toString());
            
            JScrollPane  sp = new JScrollPane(pane,  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
            pb.add(UIHelper.createScrollPane(sp), (new CellConstraints()).xy(1, 1));
            pb.setDefaultDialogBorder();
            
            CustomDialog dlg;
            if (UIRegistry.getMostRecentWindow() instanceof Frame)
            {
                dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), "Errors", true, CustomDialog.OK_BTN, pb.getPanel());
            } else
            {
                dlg = new CustomDialog((Dialog)UIRegistry.getMostRecentWindow(), "Errors", true, CustomDialog.OK_BTN, pb.getPanel());
            }
            dlg.createUI();
            dlg.pack();
            dlg.setSize(Math.max(dlg.getPreferredSize().width, 350), Math.max(dlg.getPreferredSize().height, 350));
            UIHelper.centerAndShow(dlg);
            return false;
        }
        return true;
    }
    
    /**
     * @param cntPairs
     * @param totalCnt
     */
    protected void displayTotals(final ArrayList<Pair<String, Integer>> cntPairs, final int totalCnt)
    {
        Vector<String> colNames = new Vector<String>();
        colNames.add("Table");
        colNames.add("Count");
        Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
        for (Pair<String, Integer> p : cntPairs)
        {
            Vector<Object> row = new Vector<Object>();
            row.add(p.first);
            row.add(p.second);
            rows.add(row);
        }
        Vector<Object> row = new Vector<Object>();
        row.add("Total");
        row.add(totalCnt);
        rows.add(row);
        
        DefaultTableModel mdl = new DefaultTableModel(rows, colNames);
        JTable tbl = new JTable(mdl);
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        pb.add(UIHelper.createScrollPane(tbl), (new CellConstraints()).xy(1, 1));
        pb.setDefaultDialogBorder();
        CustomDialog dlg = new CustomDialog((Dialog)UIRegistry.getMostRecentWindow(), "Summary", true, CustomDialog.OK_BTN, pb.getPanel());
        dlg.setVisible(true);
    }

    //------------------------------------------------------------------------
    //-- Override toString
    //------------------------------------------------------------------------
    class DualValue extends Pair<Object, Object>
    {
        /**
         * 
         */
        public DualValue()
        {
            super();
        }

        /**
         * @param first
         * @param second
         */
        public DualValue(Object first, Object second)
        {
            super(first, second);
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.util.Pair#toString()
         */
        @Override
        public String toString()
        {
            return first != null ? first.toString() : "";
        }
    }

}

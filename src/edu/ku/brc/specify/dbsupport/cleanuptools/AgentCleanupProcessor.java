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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.ui.UIRegistry.displayInfoMsgDlg;
import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.showLocalizedMsg;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.config.Scriptlet;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentGeography;
import edu.ku.brc.specify.datamodel.AgentSpecialty;
import edu.ku.brc.specify.datamodel.AgentVariant;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 28, 2012
 *
 */
public class AgentCleanupProcessor
{
    private static final Logger  log = Logger.getLogger(AgentCleanupProcessor.class);
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static boolean     doUpdates       = true;
    static Integer[]           agentIdTable    = {12, 86, 19, 30, 35, 146, 49, 53, 133};
    static String[]            roleTableIds    = {"AccessionID", "BorrowID", "DeaccessionID", "LoanID", "GiftID", };

    static HashSet<Integer>    tableIdsSet     = new HashSet<Integer>();

    protected boolean          hasGroups;
    protected boolean          hasAgentSpecialty;
    protected boolean          isContinuing    = true;
    protected boolean          isSkipping      = false;
    protected ProgressDialog   prgDlg;
    protected int[]            indexes         = null;
    protected int              currIndex       = 0;
    
    protected HashSet<Integer> usedIds         = null;
    protected HashSet<Integer> skipJoinTables  = null;
    
    protected Vector<FindItemInfo> itemsList   = new Vector<FindItemInfo>();
    protected TableWriter      tblWriter;
    protected StringBuilder[]  outputRows      = new StringBuilder[5];
    protected int              updCnt          = 0;
    protected boolean          isForExactMatches;
    
    protected int              totalUpdated    = 0;
    protected int              totalDeleted    = 0;
    
    protected AgentCleanupIndexer cleanupIndexer = null;
    
    static {
        Collections.addAll(tableIdsSet, agentIdTable);
    }
    
    enum JoinTableDupStatus {eOK, eError, eNeedUpdating};

    /**
     * @param cleanupIndexer
     * @param isForExactMatches
     */
    public AgentCleanupProcessor(final AgentCleanupIndexer cleanupIndexer, 
                                 final boolean isForExactMatches)
    {
        super();
        this.cleanupIndexer     = cleanupIndexer;
        this.isForExactMatches = isForExactMatches;
        
        initialize();
    }
    
    /**
     * 
     */
    private void initialize()
    {
        String sql = QueryAdjusterForDomain.getInstance().adjustSQL("SELECT COUNT(*) FROM groupperson WHERE DivisionID = DIVID");
        hasGroups  = BasicSQLUtils.getCountAsInt(sql) > 0;
        
        sql = QueryAdjusterForDomain.getInstance().adjustSQL("SELECT COUNT(*) FROM agent a INNER JOIN agentspecialty asp ON a.AgentID = asp.AgentID WHERE a.DivisionID = DIVID");
        hasAgentSpecialty  = BasicSQLUtils.getCountAsInt(sql) > 0;
        
        prgDlg = new ProgressDialog(getResourceString("CLNUP_AG_PRG_TITLE"), true, false);
        prgDlg.getProcessProgress().setIndeterminate(true);
        prgDlg.setDesc(getResourceString("CLNUP_AG_INIT_MSG"));
        UIHelper.centerAndShow(prgDlg);
        
        cleanupIndexer.setProgressDlg(prgDlg);
        
        if (cleanupIndexer != null)
        {
            //doBuildLuceneIndex();
            
            initReport();
        }
    }

    /**
     * 
     */
    public void doBuildLuceneIndex()
    {
        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
            @Override
            protected Object doInBackground() throws Exception
            {
                int cnt = cleanupIndexer.buildIndex();
                prgDlg.setOverall(0, cnt);
                return null;
            }

            @Override
            protected void done()
            {
                currIndex = 0;
                nextAgent();
            }
        };
        worker.execute();
    }

    /**
     * 
     */
    public void loadExactMatchAgents()
    {
        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
            @Override
            protected Object doInBackground() throws Exception
            {
                buildAgentList();
                return null;
            }

            @Override
            protected void done()
            {
                showAgentListDialog();
            }
        };
        worker.execute();
    }
    
    /**
     * 
     */
    protected void buildAgentList()
    {
        Connection conn = DBConnection.getInstance().getConnection();
        
        itemsList = new Vector<FindItemInfo>();
        
        Scriptlet scriptlet = new Scriptlet();
       
        PreparedStatement pStmt = null;
        try
        {
            String sql = "SELECT AgentID, LastName, FirstName, MiddleInitial FROM agent WHERE LOWER(CONCAT(IFNULL(LastName, ''), ' ', IFNULL(FirstName, ''),  ' ', IFNULL(MiddleInitial, ''))) = ? AND DivisionID = DIVID";
            sql   =  QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            System.out.println(sql);
            pStmt = conn.prepareStatement(sql);

            // First get LastNames that are duplicates
            sql = "SELECT NM, CNT FROM (SELECT NM, COUNT(NM) CNT FROM (SELECT LOWER(CONCAT(IFNULL(LastName, ''), ' ', IFNULL(FirstName, ''),  ' ', IFNULL(MiddleInitial, ''))) NM FROM agent " +
            		"WHERE DivisionID = DIVID AND SpecifyUserID IS NULL) T0 GROUP BY NM) T1 WHERE CNT > 1 ORDER BY CNT DESC, NM ASC";
            sql =  QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            System.out.println(sql);
            
            int i = 0;
            for (Object[] row : BasicSQLUtils.query(sql))
            {
                if (i % 100 == 0) System.out.println("Loaded: "+i);
                i++;
                
                String       fullName = (String)row[0];
                FindItemInfo fii      = null;
                pStmt.setString(1, fullName);
                ResultSet rs = pStmt.executeQuery();
                while (rs.next())
                {
                    int id = rs.getInt(1);
                    if (fii == null)
                    {
                        String lastName   = rs.getString(2);
                        String firstName  = rs.getString(3);
                        String midInitial = rs.getString(4);
                        fii = new FindItemInfo(id, scriptlet.buildNameString(firstName, lastName, midInitial));
                        itemsList.add(fii);
                    } else
                    {
                        fii.addDuplicate(id);
                    }
                }
                rs.close();
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    private void initReport()
    {
        try
        {
            String fullPath = getAppDataDir() + File.separator + "agent_report.html";
            tblWriter       = new TableWriter(fullPath, "Agent Merge Report");
            tblWriter.startTable();
            tblWriter.logHdr("Final Agent", "To Be Merged Agents", "Addresses", "Tables Updated", "Errors");
            for (int i=0;i<outputRows.length;i++)
            {
                outputRows[i] = new StringBuilder();
            }
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    protected void showAgentListDialog()
    {
        if (itemsList.size() > 0)
        {
            prgDlg.toBack();
            AgentCleanupListDlg dlg = new AgentCleanupListDlg("Agent", "Exact Match Duplicates", itemsList);
            UIHelper.centerAndShow(dlg);
            prgDlg.toFront();
            
            if (!dlg.isCancelled())
            {
                indexes = dlg.getSelectedIndexes();
                currIndex = 0;
                prgDlg.setDesc("Merging Agents...");
                prgDlg.setOverall(0, indexes.length);
                UIHelper.centerAndShow(prgDlg);

                initReport();
                
                nextAgent();
                
            } else
            {
                prgDlg.setVisible(false);
                prgDlg.dispose();
            }
        } else
        {
            prgDlg.setVisible(false);
            prgDlg.dispose();
            showLocalizedMsg("There are no 'exact match' duplicate Agents.");
        }
    }
    
    /**
     * @param addrId
     * @return
     */
    /*private String getAddrStr(final int addrId)
    {
        Object[] row = BasicSQLUtils.queryForRow("SELECT Address, Address2, City, State, PostalCode, " +
                                                 "Country  FROM address WHERE AddressID = "+addrId);
        StringBuilder sb = new StringBuilder();
        for (Object r : row)
        {
            if (sb.length() > 0) sb.append(";");
            sb.append(r != null ? r.toString() : "");
        }
        return sb.toString();
    }*/
    
    /**
     * @param fii
     * @param addrIdList
     * @return
     */
    /*private boolean cleanupAddresses(final FindItemInfo fii, final List<AddrInfo> addrIdList)
    {
        for (AddrInfo ai :addrIdList)
        {
            if (!ai.isIncluded) // NOT Included
            {
                String addrStr = getAddrStr(ai.id);
                String sql     = String.format("DELETE FROM address WHERE AddressID = %d", ai.id);
                logSQL(sql);
                if (BasicSQLUtils.update(sql) != 1)
                {
                    return false;
                }
                outputRows[2].append("Removed Address: "+addrStr+"<BR>");
            }
        }
        
        int i = 0;
        for (AddrInfo ai : new ArrayList<AddrInfo>(addrIdList))
        {
            if (ai.isIncluded) // Is Included
            {
                String sql = String.format("UPDATE address SET AgentID = %d, IsPrimary=%d, IsCurrent=%d, Ordinal=%d WHERE AddressID = %d", 
                                            fii.getId(), ai.isPrimary ? 1 : 0, ai.isCurrent ? 1 :0, i, ai.id);
                logSQL(sql);
                if (BasicSQLUtils.update(sql) != 1)
                {
                    return false;
                }
                outputRows[2].append("Updated Address: "+getAddrStr(ai.id)+"<BR>");
            }
            i++;
        }

        return true;
    }*/
    
    /**
     * @param fii
     */
    private void checkForAddrs(final FindItemInfo fii)
    {
        System.out.println("\ncheckForAddrs (async)");

        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
            boolean hasAddrs = false;
            @Override
            protected Object doInBackground() throws Exception
            {
                String inClause = fii.getInClause(true);
                String sql      = String.format("SELECT COUNT(*) FROM address WHERE AgentID IN %s", inClause);
                hasAddrs        = BasicSQLUtils.getCountAsInt(sql) > 0;
                return null;
            }

            @Override
            protected void done()
            {
               if (hasAddrs)
               {
                   /*AddressCleanupDlg addrCleanup = new AddressCleanupDlg(fii); // NOTE: SQL is done on UI thread
                   addrCleanup.fillModel();
                   
                   if (!addrCleanup.isSingle())
                   {
                       addrCleanup.pack();
                       addrCleanup.setSize(800, 500);
                       UIHelper.centerWindow(addrCleanup);
                       addrCleanup.setVisible(true);
                       isContinuing = !addrCleanup.isCancelled();
                   }
                   
                   List<AddrInfo> addrIdList = addrCleanup.getAddrIds();
                   
                   if (doUpdates)
                   {
                       if (addrIdList != null && addrIdList.size() > 0)
                       {
                           if (!cleanupAddresses(fii, addrIdList))
                           {
                               showError(String.format("There was an error cleaning up addresses for agent'%s'", fii.getValue().toString()));
                               isContinuing = false;
                           }
                       }
                   }*/
                   
                   MultipleRecordCleanupDlg mrcDlg = null;
                   try
                   {
                       isSkipping = false;
                       MultipleRecordComparer mrc = new MultipleRecordComparer(fii, 
                                                                               Agent.getClassTableId(), 
                                                                               Address.getClassTableId(), 
                                                                               AgentVariant.getClassTableId()
                                                                               //AccessionAgent.getClassTableId()
                                                                               //AgentSpecialty.getClassTableId(),
                                                                               //AgentGeography.getClassTableId()
                                                                               );
                       mrc.setSingleRowIncluded(false, false, false, false);
                       mrc.addDisplayColumn("Agent's Name");
                       
                       mrc.loadData();
                       
                       if (mrc.hasColmnsOfDataThatsDiff() || mrc.hasKidsDataThatsDiff())
                       {
                           mrcDlg = new MultipleRecordCleanupDlg(mrc, "Agent Cleanup");
                           mrcDlg.createUI();
                           if (!mrcDlg.isSingle())
                           {
                               mrcDlg.pack();
                               mrcDlg.setSize(800, 500);
                               UIHelper.centerWindow(mrcDlg);
                               mrcDlg.toFront();
                               prgDlg.toBack();
                               mrcDlg.setVisible(true);
                               prgDlg.toFront();
                               isContinuing = !mrcDlg.isCancelled();
                               isSkipping   = mrcDlg.getBtnPressed() == CustomDialog.APPLY_BTN;
                           }
                           
                           if (isContinuing)
                           {
                               MergeInfo       mainItem = mrcDlg.getMainMergedInfo();
                               List<MergeInfo> kidItems = mrcDlg.getKidsMergedInfo();
                               if (!cleanupMerges(Agent.getClassTableId(), mainItem, kidItems))
                               {
                                   String msg = String.format("There was an error cleaning up addresses for agent '%s'", fii.getValue().toString());
                                   showProcessingMessage(msg);
                                   log.error(msg);
                                   isContinuing = false;
                               }
                           }
                       } else if (mrc.hasRecords())
                       {
                           log.debug("Here");
                       } else
                       {
                           isContinuing = true;
                       }
                   } catch (Exception ex)
                   {
                       ex.printStackTrace();
                   }
                   
                   if (isContinuing)
                   {
                       prgDlg.toFront();
                       doMergeOfAgents(fii);
                   } else
                   {
                       prgDlg.setVisible(false);
                       prgDlg.dispose();
                   }
               } else
               {
                   doMergeOfAgents(fii);
               }
            }
        };
        worker.execute();
    }
    
    /**
     * @param parentTblId
     * @param mainItem
     * @param kidItems
     * @return
     */
    private boolean cleanupMerges(final int             parentTblId,
                                  final MergeInfo       mainItem,
                                  final List<MergeInfo> kidItems)
    {
        /*boolean cont = false;
        if (cont)
        {
            return false;
        }*/
        
        DBTableInfo parentTI = DBTableIdMgr.getInstance().getInfoById(parentTblId);
        System.out.println("ParentTbl: "+parentTI.getTitle());
        
        //------------------------------------------
        // Find Merged into (Primary) record (Master)
        //------------------------------------------
        MergeInfoItem       intoRec = mainItem.getMergeInto();
        List<MergeInfoItem> fromRec = mainItem.getMergeFrom();
        if (intoRec == null && fromRec.size() > 0)
        {
            String msg = String.format("No 'Primary' record for %s", parentTI.getTitle());
            showProcessingMessage(msg);
            log.error(msg);
            return false;
        }
        
        //--------------------------------------------------------------------
        // Merge Kid's Information before removing unwanted 'parent' records.
        //--------------------------------------------------------------------
        System.out.println("Kid Tables to be merged:");
        for (MergeInfo kidMergeInfo : kidItems)
        {
            DBTableInfo ti = kidMergeInfo.getTblInfo();
            System.out.println("KidTbl: "+ti.getTitle());
        }
        
        for (MergeInfo kidMergeInfo : kidItems)
        {
            DBTableInfo ti = kidMergeInfo.getTblInfo();
            System.out.println("KidTbl: "+ti.getTitle());
            
            for (MergeInfoItem mi : kidMergeInfo.getMergeNotIncluded())
            {
                String sql = String.format("DELETE FROM %s WHERE %s = %d", ti.getName(), ti.getIdColumnName(), mi.getId());
                logSQL(sql);
                if (BasicSQLUtils.update(sql) != 1)
                {
                    String msg = String.format("Error deleting child record for %s (record id %d)", ti.getTitle(), mi.getId());
                    showProcessingMessage(msg);
                    log.error(msg);
                    return false;
                }
                totalDeleted++;
            }
            
            for (MergeInfoItem mi : kidMergeInfo.getMergeIncluded())
            {
                String sql = String.format("UPDATE %s SET %s=%d WHERE %s = %d", ti.getName(), parentTI.getIdColumnName(), intoRec.getId(), ti.getIdColumnName(), mi.getId());
                logSQL(sql);
                if (BasicSQLUtils.update(sql) != 1)
                {
                    String msg = String.format("Error updating child record for %s (record id %d)", ti.getTitle(), mi.getId());
                    showProcessingMessage(msg);
                    log.error(msg);
                    return false;
                }
                totalUpdated++;
            }
        }
        
        // ZZZ 
        
        DBTableInfo ti = mainItem.getTblInfo();
        for (MergeInfoItem mi : mainItem.getMergeFrom())
        {
            String sql = String.format("DELETE FROM %s WHERE %s = %d", ti.getName(), ti.getIdColumnName(), mi.getId());
            logSQL(sql);
            if (BasicSQLUtils.update(sql) != 1)
            {
                String msg = String.format("Error deleting 'merge from' record for %s (record id %d)", parentTI.getTitle(), mi.getId());
                showProcessingMessage(msg);
                log.error(msg);
                return false;
            }
        }
        
        //outputRows[2].append("Removed Address: "+addrStr+"<BR>");
        //outputRows[2].append("Updated Address: "+getAddrStr(ai.id)+"<BR>");

        return true;
    }

    
    /**
     * @param fieldName
     * @param fii
     * @param inClause
     * @return
     */
    private JoinTableDupStatus fixGroupPersons(final FindItemInfo fii)
    {
        boolean isError = false;
        
        // Get Highest Order Number for the Destination AgentID
        String  sql      = String.format("SELECT OrderNumber FROM grouperson WHERE GroupID = %d ORDER BY OrderNumber DESC", fii.getId());
        Integer orderNum = BasicSQLUtils.getCountAsInt(sql);
        if (orderNum == null)
        {
            orderNum = 0; // shouldn't happen
        }
        
        // Get all the groups that the Agents to be changed belong to
        sql = "SELECT GroupID, MemberID FROM groupperson WHERE MemberID IN "+ fii.getInClause(false);
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            Integer groupId  = (Integer)row[0];
            Integer memberId = (Integer)row[0];
            
            // Check to make sure the 'new' agent isn't already in the group.
            sql = String.format("SELECT COUNT(*) FROM groupperson WHERE GroupID = %d AND MemberID = %d", groupId, fii.getId());
            int cnt = BasicSQLUtils.getCountAsInt(sql);
            if (cnt > 0)
            {
                sql = String.format("DELETE FROM groupperson WHERE GroupID = %d AND MemberID = %d", groupId, memberId);
                
            } else
            {
                sql = String.format("UPDATE groupperson SET MemberID = %d WHERE GroupID = %d AND MemberID = %d", fii.getId(), groupId, memberId);
            }
            if (BasicSQLUtils.update(sql) != 1)
            {
                showProcessingMessage(String.format("Error deleting/updating table groupperson"));
                isError = true;
                break;
            }
            if (cnt > 0)
            {
                totalDeleted++;
            } else
            {
                totalUpdated++;
            }
        }
        return isError ? JoinTableDupStatus.eError : JoinTableDupStatus.eOK;
    } 
    
    /**
     * @param ti
     * @param fii
     * @return
     */
    private JoinTableDupStatus fixAgentSpecialty(final DBTableInfo ti, 
                                                 final FindItemInfo fii)
    {
        boolean isError = false;
        
        // Get Highest Order Number for the Destination AgentID
        String  sql      = String.format("SELECT OrderNumber FROM agentspecialty WHERE AgentID = %d ORDER BY OrderNumber DESC", fii.getId());
        logSQL(sql);
        Integer orderNum = BasicSQLUtils.getCountAsInt(sql);
        orderNum = orderNum == null ? 0 : (orderNum + 1);
        
        final String fmt = "SELECT AgentSpecialtyId FROM agentspecialty WHERE AgentID = %d";
        
        for (Integer agentId : fii.getDuplicateIds())
        {
            sql = String.format(fmt, agentId);
            logSQL(sql);
            Integer otherId = BasicSQLUtils.getCountAsInt(sql); // Get the ID to be changed
            if (otherId != null && otherId != 0)
            {
                // Move it over
                sql = String.format("UPDATE agentspecialty SET AgentID = %d, OrderNumber=%d WHERE AgentID = %d AND AgentSpecialtyId = %d", fii.getId(), orderNum, agentId, otherId);
                logSQL(sql);
                int updtCnt = BasicSQLUtils.update(sql);
                if (updtCnt != 1)
                {
                    showProcessingMessage(String.format("Error deleting/updating table %s", ti.getName()));
                    isError = true;
                    break;
                }
                totalUpdated++;
                orderNum++;
            }
        }
        return isError ? JoinTableDupStatus.eError : JoinTableDupStatus.eOK;
    }
    
    /**
     * @param fii
     * @return
     */
    public boolean isFundingAgentOK(final FindItemInfo fii)
    {    
        //     SELECT ct.CollectingTripID, fa.AgentID FROM collectingtrip ct INNER JOIN fundingagent fa ON ct.CollectingTripID = fa.CollectingTripID

        String sql = String.format("SELECT CTID FROM (SELECT ct.CollectingTripID CTID, COUNT(ct.CollectingTripID) CNT " +
        		                   "FROM collectingtrip ct INNER JOIN fundingagent fa ON ct.CollectingTripID = fa.CollectingTripID " +
        		                   "WHERE fa.AgentID IN %s GROUP BY ct.CollectingTripID) T1 WHERE CNT > 1", fii.getInClause(true));
        logSQL(sql);
        StringBuilder strBldr = new StringBuilder("The Collecting Trips are:<BR>");
        Vector<Integer> ids = BasicSQLUtils.queryForInts(sql);
        if (ids.size() > 0)
        {
            int cnt = 0;
            for (Integer ctId : ids)
            {
                sql = "SELECT CollectingTripName, StartDate FROM collectingtrip WHERE CollectingTripID = " + ctId;
                Object[] row = BasicSQLUtils.queryForRow(sql);
                StringBuilder sb = new StringBuilder();
                if (row[0] !=  null)
                {
                    sb.append((String)row[0]);
                }
                if (row[1] !=  null)
                {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(sdf.format((Date)row[1]));
                }
                strBldr.append(sb);
                strBldr.append("<BR>");
                cnt++;
            }
            String suffix = strBldr.toString()+"<BR>";
            String title  = String.format("Agent '%s' cannot be merged because it would create a duplicate funding agent.<BR>", getAgentStr(fii.getId()));
            String msg    = title + (cnt < 5 ? suffix : " (See report).");
            showProcessingMessage(msg);
            outputRows[4].append(msg);
            return false;
        }
        return true;
    }
    
    /**
     * @param fii
     * @return
     */
    public boolean isCollectorsOK(final FindItemInfo fii)
    {    
        String sql = String.format("SELECT CEID FROM (SELECT ce.CollectingEventID CEID, COUNT(ce.CollectingEventID) CNT FROM collectingevent ce " +
                "INNER JOIN collector c ON ce.CollectingEventID = c.CollectingEventID WHERE c.AgentID IN %s GROUP BY ce.CollectingEventID) T1 WHERE CNT > 1", fii.getInClause(true));
        logSQL(sql);
        StringBuilder strBldr = new StringBuilder("The Collecting Events are:<BR>");
        Vector<Integer> ids = BasicSQLUtils.queryForInts(sql);
        if (ids.size() > 0)
        {
            int cnt = 0;
            for (Integer ceId : ids)
            {
                sql = "SELECT StationFieldNumber, StartDate FROM collectingevent WHERE CollectingEventID = " + ceId;
                Object[] row = BasicSQLUtils.queryForRow(sql);
                StringBuilder sb = new StringBuilder();
                if (row[0] !=  null)
                {
                    sb.append((String)row[0]);
                }
                if (row[1] !=  null)
                {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(sdf.format((Date)row[1]));
                }
                strBldr.append(sb);
                strBldr.append("<BR>");
                cnt++;
            }
            String suffix = strBldr.toString()+"<BR>";
            String title  = String.format("Agent '%s' cannot be merged because it would create a duplicate collector.<BR>", getAgentStr(fii.getId()));
            String msg    = title + (cnt < 5 ? suffix : " (See report).");
            showProcessingMessage(msg);
            outputRows[4].append(msg);
            return false;
        }
        return true;
    }
    
    
    /**
     * @param fii
     * @return
     */
    public boolean isGroupOK(final FindItemInfo fii)
    {    
        String sql = "SELECT AGID FROM (SELECT a.AgentID AGID, COUNT(a.AgentID) CNT FROM agent a " +
                     "INNER JOIN groupperson gp ON a.AgentID = gp.GroupID " +
                     "WHERE gp.MemberID IN %s GROUP BY a.AgentID) T1 WHERE CNT > 1";
        return isRelationshipOK(fii, sql, "Groups", "group", "SELECT LastName FROM agent WHERE AgentID = ");
    }
    
    /**
     * @param fii
     * @return
     */
    public boolean isRelationshipOK(final FindItemInfo fii, 
                                    final String sqlStr, 
                                    final String objTitle, 
                                    final String objTitle2, 
                                    final String sqlLookUp)
    {    
        String sql = String.format(sqlStr, fii.getInClause(true));
        logSQL(sql);
        StringBuilder strBldr = new StringBuilder("The "+objTitle+" are:<BR>");
        Vector<Integer> ids = BasicSQLUtils.queryForInts(sql);
        if (ids.size() > 0)
        {
            int cnt = 0;
            for (Integer agId : ids)
            {
                String sql2 = sqlLookUp + agId;
                Object[] row = BasicSQLUtils.queryForRow(sql2);
                StringBuilder sb = new StringBuilder();
                if (row[0] !=  null)
                {
                    sb.append((String)row[0]);
                }
                if (row.length > 1 && row[1] !=  null)
                {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(sdf.format((Date)row[1]));
                }
                strBldr.append(sb);
                strBldr.append("<BR>");
                cnt++;
            }
            String suffix = strBldr.toString()+"<BR>";
            String title  = String.format("Agent '%s' cannot be merged because it would create a duplicate %s.<BR>", getAgentStr(fii.getId()), objTitle2);
            String msg    = title + (cnt < 5 ? suffix : " (See report).");
            showProcessingMessage(msg);
            outputRows[4].append(msg);
            return false;
        }
        return true;

    }
    
    /**
     * @param fii
     * @return
     */
    public boolean isRolesOK(final FindItemInfo fii)
    {
        StringBuilder sb    = new StringBuilder();
        
        boolean hasBadRole = false;
        try
        {
            String inClause = fii.getInClause(true);
            int i = 0;
            for (int tblId : new int[] {12, 19, 35, 53, 133})
            {
                DBTableInfo ti  =  DBTableIdMgr.getInstance().getInfoById(tblId);
                String      sql = String.format("SELECT %s, Role FROM %s WHERE AgentID = %d GROUP BY %s", roleTableIds[i], ti.getName(), fii.getId(), roleTableIds[i]);
                logSQL(sql);
                boolean tblIsInError = false;
                for (Object[] row : BasicSQLUtils.query(sql))
                {
                    Integer assocTblId   = (Integer)row[0]; // (e.g. AccessionID)
                    sql = String.format("SELECT COUNT(*) FROM (SELECT Role,COUNT(Role) CNT FROM %s WHERE %s = %d AND AgentID IN %s GROUP BY Role) T1 WHERE CNT > 1", ti.getName(), roleTableIds[i], assocTblId, inClause);
                    logSQL(sql);
                    if (BasicSQLUtils.getCountAsInt(sql) > 0)
                    {
                        if (!hasBadRole)
                        {
                            hasBadRole = true;
                        } 
                        if (!tblIsInError)
                        {
                            sb.append("\n");
                            sb.append(ti.getTitle());
                            tblIsInError = true;
                        }
                    }
                }
                i++;
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        if (hasBadRole)
        {
            String title = String.format("Agent '%s' cannot be merged because duplicate Roles for the\nsame person will be created during the merge for the following tables:", getAgentStr(fii.getId()));
            String msg   = title + sb.toString();
            showProcessingMessage(msg);
            outputRows[4].append(msg+"<BR>");
            return false;
        }
        return true;
    }
    
    /**
     * @param fii
     * @return
     */
    public boolean isVariantOK(final FindItemInfo fii)
    {
        boolean hasBadVariant = false;
        try
        {
            String  inClause = fii.getInClause(true);
            String  sql      = String.format("SELECT COUNT(*) FROM (SELECT VarType,COUNT(VarType) CNT FROM agentvariant WHERE AgentID IN %s GROUP BY VarType) T1 WHERE CNT > 1", inClause);
            logSQL(sql);
            if (BasicSQLUtils.getCountAsInt(sql) > 0)
            {
                if (!hasBadVariant)
                {
                    hasBadVariant = true;
                } 
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        if (hasBadVariant)
        {
            String msg = String.format("Agent '%s' cannot be merged because duplicate Variant Types for the\nsame person will be created during the merge of the Agent Variant table:");
            showProcessingMessage(msg);
            outputRows[4].append(msg+"<BR>");
            return false;
        }
        return true;
    }
    
    /**
     * @param message
     */
    private void showProcessingMessage(final String message)
    {
        String msg = message;
        if (StringUtils.contains(msg.toLowerCase(), "<br>"))
        {
            msg = StringUtils.replace(msg, "<BR>", "\n");
        }
        cleanupIndexer.getPrgDlg().toBack();
        UIRegistry.displayInfoMsgDlg(msg);
        cleanupIndexer.getPrgDlg().toFront();
    }
    
    /**
     * @param ti
     * @param fii
     * @param inClause
     * @return
     */
    private JoinTableDupStatus fixRelationships(final DBTableInfo ti, 
                                                final FindItemInfo fii, 
                                                final String inClause)
    {
        /*
        12 - AccessionAgent.java:   @UniqueConstraint(columnNames = { "Role", "AgentID", "AccessionID" }) })
        19 - BorrowAgent.java:      @UniqueConstraint(columnNames = { "Role", "AgentID", "BorrowID" }) 
        35 - DeaccessionAgent.java: @UniqueConstraint(columnNames = { "Role", "AgentID", "DeaccessionID" }) })
        53 - LoanAgent.java:        @UniqueConstraint(columnNames = { "Role", "AgentID", "LoanID" })
       133 - GiftAgent.java         @UniqueConstraint(columnNames = { "Role", "AgentID", "GiftID" }) 
        
        30 - Collector.java:        @UniqueConstraint(columnNames = {"AgentID", "CollectingEventID"}) })
       146 - FundingAgent.java:     @UniqueConstraint(columnNames = {"AgentID", "CollectingTripID"}) })
        
         // Ordering
        86 - AgentSpecialty.java:   @UniqueConstraint(columnNames = {"AgentID", "OrderNumber"}) })
        49 - GroupPerson.java:      @UniqueConstraint(columnNames = { "OrderNumber", "GroupID" }) 
      */
        
        int tblId = ti.getTableId();
        
        if (tblId == 86)
        {
            if (hasAgentSpecialty)
            {
                return fixAgentSpecialty(ti, fii);
            }
            return JoinTableDupStatus.eOK;
        }
        
        if (tblId == 49)
        {
            if (hasGroups)
            {
                return fixGroupPersons(fii);
            }
            return JoinTableDupStatus.eOK;
        }
        
        String tblName = ti.getName();
        
        String otherFld = null;
        switch (tblId)
        {
            // Has Role
            case  12: otherFld  = "AccessionID";   break;
            case  19: otherFld  = "BorrowID";      break;
            case  35: otherFld  = "DeaccessionID"; break;
            case  53: otherFld  = "LoanID";        break;
            case  133: otherFld = "GiftID";        break;
            
            // No Role
            case  30: otherFld = "CollectingEventID";break;
            case 146: otherFld = "CollectingTripID";break;
        }
        
        String sql = String.format("SELECT COUNT(*) FROM (SELECT %s,COUNT(%s) CNT FROM %s WHERE AgentID IN %s GROUP BY %s) T1 WHERE CNT > 1", 
                                     otherFld, otherFld, tblName, inClause, otherFld);
        logSQL(sql);
        int numDups = BasicSQLUtils.getCountAsInt(sql);
        if (numDups == 0)
        {
            return JoinTableDupStatus.eNeedUpdating;
        }
        
        final String srchStr = "SELECT %s FROM %s WHERE AgentID = %d";
        
        boolean          isError = false;
        HashSet<Integer> usedSet = new HashSet<Integer>();
        sql = String.format(srchStr, otherFld, tblName, fii.getId());
        Integer otherId = BasicSQLUtils.getCountAsInt(sql);
        if (otherId != null)
        {
            usedSet.add(otherId);
        }
        
        for (Integer agentId : fii.getDuplicateIds())
        {
            sql     = String.format(srchStr, otherFld, tblName, agentId);
            logSQL(sql);
            otherId = BasicSQLUtils.getCountAsInt(sql);
            System.out.println(String.format("%d %s - %s", otherId, usedSet.contains(otherId)?"Contains":"no", sql));
            
            int cnt = BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM %s WHERE %s = %d AND AgentID = %d", tblName, otherFld, otherId, fii.getId()));
            logSQL(sql);
            boolean isDelete;
            if (cnt > 0 || usedSet.contains(otherId))
            {
                sql = String.format("DELETE FROM %s WHERE AgentID = %d AND %s = %d", tblName, agentId, otherFld, otherId);
                isDelete = true;
            } else
            {
                sql = String.format("UPDATE %s SET AgentID = %d WHERE AgentID = %d AND %s = %d", tblName, fii.getId(), agentId, otherFld, otherId);
                usedSet.add(otherId);
                isDelete = false;
            }
            logSQL(sql);
            if (BasicSQLUtils.update(sql) != 1)
            {
                showProcessingMessage(String.format("Error deleting/updating table %s", tblName));
                isError = true;
                break;
            }
            if (isDelete)
            {
                totalDeleted++;
            } else
            {
                totalUpdated++;
            }
        }
        
        if (!isError)
        {
            if (updCnt > 0) outputRows[3].append(", ");
            outputRows[3].append(ti.getTitle());
        }
        return isError ? JoinTableDupStatus.eError : JoinTableDupStatus.eNeedUpdating;
    }
    
    /**
     * @param sql
     */
    private void logSQL(final String sql)
    {
        System.out.println(sql);
    }
    
    /**
     * @param fii
     */
    private void doProcessMerge(final FindItemInfo fii)
    {
        System.out.println("\ndoProcessMerge");
        
        boolean isError  = false;
        int     cnt      = 1;
        String  inClause = fii.getInClause(false);
        
        final int numTables = DBTableIdMgr.getInstance().getTables().size();
        final int twentyPercent = numTables / 20;
        
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            boolean wasUpdated = false;
            
            if (cnt % twentyPercent == 0)
            {
                final int count = (cnt * 100) / numTables;
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        prgDlg.setProcess(count);
                    }
                });
            }
            cnt++;
            
            JoinTableDupStatus status = JoinTableDupStatus.eNeedUpdating;
            if (tableIdsSet.contains(ti.getTableId()))
            {
                status = fixRelationships(ti, fii, inClause); 
            }

            //HashSet<Integer> skipTables = new HashSet<Integer>();
            /*
                AccessionAgent
                AgentSpecialty
                BorrowAgent 
                Collector
                DeaccessionAgent
                FundingAgent
                GiftAgent 
                LoanAgent 
             */
            
            if (status == JoinTableDupStatus.eNeedUpdating)
            {
                for (DBRelationshipInfo ri : ti.getRelationships())
                {
                    if (ri.getDataClass() == Agent.class)
                    {
                        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s IN %s", ti.getName(), ri.getColName(), inClause);
                        logSQL(sql);
                        int totalCount = BasicSQLUtils.getCountAsInt(sql);
                        logSQL("TotCnt: "+totalCount+"\n"+sql);
                        if (totalCount > 0)
                        {
                            sql = String.format("UPDATE %s SET %s=%d WHERE %s IN %s", ti.getName(), ri.getColName(), fii.getId(), ri.getColName(), inClause);
                            logSQL(sql);
                            if (doUpdates)
                            {
                                int numChanged = BasicSQLUtils.update(sql);
                                if (numChanged != totalCount)
                                {
                                    String msg = String.format("Error updating AgentIds - Should have updated %d, only updated %d", totalCount, numChanged);
                                    showProcessingMessage(msg);
                                    isContinuing = false;
                                    isError      = true;
                                } else
                                {
                                    wasUpdated = true;
                                    totalUpdated += numChanged;
                                }
                            }
                            
                            // Now Add them to the Set of already fixed AgentIds
                            if (usedIds != null)
                            {
                                usedIds.add(fii.getId());
                                usedIds.addAll(fii.getDuplicateIds());
                            }
                        }
                    }
                }
            }
            
            if (!isError && wasUpdated)
            {
                if (updCnt > 0) outputRows[3].append(", ");
                outputRows[3].append(ti.getTitle());
                updCnt++;
            }
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                prgDlg.setProcess(100);
            }
        });

        if (!isError)
        {
            String sql = String.format("DELETE FROM agent WHERE AgentID IN %s", inClause);
            logSQL(sql);
            if (doUpdates)
            {
                int totalCount = fii.getDuplicateIds().size();
                int numChanged = BasicSQLUtils.update(sql);
                if (numChanged != totalCount)
                {
                    String msg = String.format("Error deleting AgentIds - Should have updated %d, only updated %d", totalCount, numChanged);
                    showProcessingMessage(msg);
                    isContinuing = false;
                    isError      = true;
                } else
                {
                    totalDeleted += numChanged;
                }
            }
        }
    }
    
    /**
     * @param fii
     */
    private void doMergeOfAgents(final FindItemInfo fii)
    {
        prgDlg.setProcess(0, 100);
        
        System.out.println(String.format("%d : %s - %d", fii.getId(), fii.getValue(), fii.getCount()));
        
        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
            @Override
            protected Object doInBackground() throws Exception
            {
                try
                {
                    doProcessMerge(fii);
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done()
            {
                if (hasMoreAgents())
                {
                    nextAgent();
                } else
                {
                    doComplete();
                }
            }
        };
        worker.execute();
    }
    
    /**
     * @param isForExactMatches the isForExactMatches to set
     */
    public void setForExactMatches(boolean isForExactMatches)
    {
        this.isForExactMatches = isForExactMatches;
    }

    /**
     * @param id
     * @return
     */
    private String getAgentStr(final int id)
    {
        return BasicSQLUtils.querySingleObj("SELECT CONCAT(IFNULL(LastName, ''), ' ', IFNULL(FirstName, ''),  ' ', IFNULL(MiddleInitial, '')) FROM agent WHERE AgentID = "+id);
    }
    
    /**
     * @param fii
     * @return
     */
//    private boolean isGroupPersonsOK(final FindItemInfo fii)
//    {
//        String sql = "SELECT COUNT(*) FROM groupperson WHERE GroupID IN " + fii.getInClause(true);
//        int cnt = BasicSQLUtils.getCountAsInt(sql);
//        if (cnt > 0)
//        {
//            String msg = String.format("One of the agents being merged as part of agent '%s' is a Group and cannot be merged.", getAgentStr(fii.getId()));
//            outputRows[4].append(msg+"<BR>");
//            showError(msg);
//            return false;
//        }
//        return true;
//    }
    
    /**
     * @return
     */
    private boolean hasMoreAgents()
    {
        if (cleanupIndexer != null && !isForExactMatches)
        {
            return cleanupIndexer.hasMoreAgents();
        }
        
        return currIndex < indexes.length;    
    }
    
    /**
     * 
     */
    private void nextAgent()
    {
        FindItemInfo fii;
        if (cleanupIndexer != null && !isForExactMatches)
        {
            fii = cleanupIndexer.getNextAgent();
            if (fii == null)
            {
                if (cleanupIndexer.isQuitting() || !cleanupIndexer.hasMoreAgents())
                {
                    doComplete();
                    return;
                }
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        nextAgent();
                    }
                });
            }
        } else
        {
            fii = itemsList.get(indexes[currIndex]);
            System.out.println(String.format("%s - %d", fii.getValue(), fii.getCount()));
        }
        processNextAgent(fii);
    }
    
    /**
     * 
     */
    private void processNextAgent(final FindItemInfo fii)
    {
        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
            private boolean doSkip = false;
            
            @Override
            protected Object doInBackground() throws Exception
            {
                if (outputRows[0].length() > 0)
                {
                    tblWriter.logObjRowVAlign(outputRows, "top");
                }
                for (StringBuilder sb : outputRows)
                {
                    sb.setLength(0);
                }
                updCnt = 0;

                outputRows[0].append(getAgentStr(fii.getId()));
                for (Integer agentID : fii.getDuplicateIds())
                {
                    if (outputRows[1].length() > 0) outputRows[1].append("<BR>");
                    outputRows[1].append(getAgentStr(agentID));
                }
                
                if (isGroupOK(fii) && isRolesOK(fii) && 
                    isCollectorsOK(fii) && isFundingAgentOK(fii) && isVariantOK(fii))
                {
                    if (usedIds == null || !usedIds.contains(fii.getId()))
                    {
                        int numLeft = fii.cleanDuplicateIds(usedIds);
                        if (numLeft > 0)
                        {
                            checkForAddrs(fii); // this calls doMergeOfAgents
                        } else
                        {
                            doMergeOfAgents(fii);
                        }
                    } else
                    {
                        doMergeOfAgents(fii);
                    }
                } else
                {
                    doSkip = true;
                }
                currIndex++;
                return null;
            }

            @Override
            protected void done()
            {
                prgDlg.setOverall(currIndex);
                if (doSkip)
                {
                    if (hasMoreAgents())
                    {
                        nextAgent();
                    } else
                    {
                        doComplete();
                    }
                }
            }
        };
        worker.execute();
    }
    
    /**
     * 
     */
    private void doComplete()
    {
        prgDlg.setVisible(false);
        prgDlg.dispose();
        
        if (outputRows[0].length() > 0)
        {
            tblWriter.logObjRowVAlign(outputRows, "top");
        }
        tblWriter.close();
        
        String msg = "";
        if (totalDeleted > 0)
        {
            msg += String.format("Records updated: %d", totalUpdated);
        }
        if (totalDeleted > 0)
        {
            if (!msg.isEmpty()) msg += "\n";
            msg += String.format("Records deleted: %d", totalDeleted);
        }
        displayInfoMsgDlg(msg.isEmpty() ? "Done" : msg);
        
        if (tblWriter.hasLines())
        {
            try
            {
                AttachmentUtils.openFile(new File(tblWriter.getFullFilePath()));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @return
     */
    public static int getExactMatchCount()
    {
        String sql = "SELECT COUNT(*) FROM (SELECT COUNT(NM) AS CNT FROM (SELECT LOWER(CONCAT(IFNULL(LastName, ''), ' ', IFNULL(FirstName, ''),  ' ', IFNULL(MiddleInitial, ''))) NM FROM agent) T1 GROUP BY NM) T2 WHERE CNT > 1";
        return BasicSQLUtils.getCountAsInt(sql);
    }
    

    /**
     * @return the doUpdates
     */
    public static boolean isDoUpdates()
    {
        return doUpdates;
    }
}

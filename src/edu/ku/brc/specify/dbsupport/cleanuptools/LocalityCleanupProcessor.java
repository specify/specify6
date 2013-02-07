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
import static edu.ku.brc.ui.UIRegistry.showError;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.GeoCoordDetail;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityDetail;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 28, 2012
 *
 */
public class LocalityCleanupProcessor
{
    private static final Logger  log = Logger.getLogger(LocalityCleanupProcessor.class);
    
    protected boolean          isBuiltByLatLon;
    protected boolean          isContinuing    = true;
    protected ProgressDialog   prgDlg;
    protected int              currIndex       = 0;
    
    protected HashSet<Integer> usedIds         = null;
    
    protected Vector<FindItemInfo> itemsList   = new Vector<FindItemInfo>();
    protected TableWriter      tblWriter;
    protected StringBuilder[]  outputRows      = new StringBuilder[5];
    protected int              updCnt          = 0;
    
    protected LocalityCleanupIndexer cleanupIndexer = null;
    
    enum JoinTableDupStatus {eOK, eError, eNeedUpdating};

    /**
     * 
     */
    public LocalityCleanupProcessor(final boolean isBuiltByLatLon)
    {
        this(null);
        
        this.isBuiltByLatLon = isBuiltByLatLon;
    }

    /**
     * 
     */
    public LocalityCleanupProcessor(final LocalityCleanupIndexer cleanupIndexer)
    {
        super();
        this.cleanupIndexer  = cleanupIndexer;
        this.isBuiltByLatLon = false;
        
        initialize();
    }
    
    /**
     * 
     */
    private void initialize()
    {
        prgDlg = new ProgressDialog(getResourceString("CLNUP_LC_PRG_TITLE"), true, false);
        prgDlg.getProcessProgress().setIndeterminate(true);
        prgDlg.setDesc(getResourceString("CLNUP_LC_INIT_MSG"));
        
        if (cleanupIndexer != null)
        {
            cleanupIndexer.setProgressDlg(prgDlg);
        }
        UIHelper.centerAndShow(prgDlg);
        
        //if (cleanupIndexer != null)
        {
            initReport();
        }
    }

    /**
     * 
     */
    public void performLuceneMatching()
    {
        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
            @Override
            protected Object doInBackground() throws Exception
            {
                try
                {
                    int cnt = cleanupIndexer.buildIndex();
                    prgDlg.setOverall(0, cnt);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done()
            {
                currIndex = 0;
                nextLocality();
            }
        };
        worker.execute();
    }

    /**
     * 
     */
    public void performExactMatching()
    {
        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
            @Override
            protected Object doInBackground() throws Exception
            {
                buildLocalityList();
                return null;
            }

            @Override
            protected void done()
            {
                if (hasMoreLocalties())
                {
                    nextLocality();
                } else
                {
                    doComplete();
                }
            }
        };
        worker.execute();
    }
    
    /**
     * 
     */
    protected void buildLocalityList()
    {
        Connection conn = DBConnection.getInstance().getConnection();
        
        itemsList = new Vector<FindItemInfo>();
        
        PreparedStatement pStmt = null;
        try
        {
            String fieldStr;
            String whereStr;
            if (isBuiltByLatLon)
            {
                fieldStr = "CONCAT(Latitude1, ' ', Longitude1)";
                whereStr = "Latitude1 IS NOT NULL AND Longitude1 IS NOT NULL";
            } else
            {
                fieldStr = "CONCAT(LocalityName, ' ',IFNULL(FullName, ''))";
                whereStr = "LocalityName IS NOT NULL";
            }
            
            String pstmtSQL = String.format("SELECT LocalityID, %s FROM locality l LEFT JOIN geography g ON l.GeographyID = g.GeographyID " +
            		                   "WHERE %s AND LOWER(%s) = ? AND DisciplineID = DSPLNID", fieldStr, whereStr, fieldStr);
            pstmtSQL   =  QueryAdjusterForDomain.getInstance().adjustSQL(pstmtSQL);
            logSQL(pstmtSQL);
            
            pStmt = conn.prepareStatement(pstmtSQL);

            String cntSQL = String.format("SELECT COUNT(*) FROM (SELECT NM,COUNT(NM) CNT FROM (SELECT LOWER(%s) NM, l.LocalityID FROM locality l " +
                            		      "LEFT JOIN geography g ON l.GeographyID = g.GeographyID " +
                            	          "WHERE DisciplineID = DSPLNID AND %s) T1 GROUP BY NM) T2 WHERE CNT > 1", fieldStr, whereStr);
            cntSQL = QueryAdjusterForDomain.getInstance().adjustSQL(cntSQL);
            logSQL(cntSQL);
            
            final int totCnt = BasicSQLUtils.getCountAsInt(conn, cntSQL);
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    prgDlg.setProcess(1, 100);
                }
            });
            int inc = totCnt / 20;

            String dupsSQL = String.format("SELECT NM, CNT FROM (SELECT NM,COUNT(NM) CNT FROM (SELECT LOWER(%s) NM, l.LocalityID FROM locality l " +
            		                       "LEFT JOIN geography g ON l.GeographyID = g.GeographyID WHERE DisciplineID = DSPLNID AND %s) T1 GROUP BY NM) T2 WHERE CNT > 1 LIMIT 0,250", fieldStr, whereStr);
            dupsSQL = QueryAdjusterForDomain.getInstance().adjustSQL(dupsSQL);
            logSQL(dupsSQL);
            
            int i = 0;
            for (Object[] row : BasicSQLUtils.query(dupsSQL))
            {
                String       locName = (String)row[0];
                FindItemInfo fii     = null;
                pStmt.setString(1, locName);
                
                ResultSet rs = pStmt.executeQuery();
                while (rs.next())
                {
                    int id = rs.getInt(1);
                    if (fii == null)
                    {
                        String title = rs.getString(2);
                        fii = new FindItemInfo(id, title);
                        itemsList.add(fii);
                    } else
                    {
                        fii.addDuplicate(id);
                    }
                } // while
                rs.close();
                
                i++;
                if (i % inc == 0)
                {
                    final int recCnt = (int)Math.round((float)i * 100.0 / (float)totCnt);
                    System.out.println("Loaded: "+i+" inc: "+inc+"  r:"+recCnt+"  tot: "+totCnt);
                    
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            prgDlg.setProcess(recCnt);
                        }
                    });
                }
            } // for
            pStmt.close();
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
            String fullPath = getAppDataDir() + File.separator + "locality_report.html";
            tblWriter       = new TableWriter(fullPath, "Locality Merge Report");
            tblWriter.startTable();
            tblWriter.logHdr("Final Locality", "To Be Merged Localities", "xxxx", "Tables Updated", "Errors");
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
     * @param sql
     */
    private void logSQL(final String sql)
    {
        System.out.println(sql);
    }
    
    /**
     * @param fii
     */
    private void checkForAddrs(final FindItemInfo fii)
    {
        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
            boolean hasLocDetails = false;
            boolean hasGeoCoords  = false;
            @Override
            protected Object doInBackground() throws Exception
            {
                String inClause = fii.getInClause(true);
                String sql      = String.format("SELECT COUNT(*) FROM localitydetail WHERE LocalityID IN %s", inClause);
                System.out.println(sql+"  "+BasicSQLUtils.getCountAsInt(sql));
                hasLocDetails   = BasicSQLUtils.getCountAsInt(sql) > 0;
                
                sql      = String.format("SELECT COUNT(*) FROM geocoorddetail WHERE LocalityID IN %s", inClause);
                System.out.println(sql+"  "+BasicSQLUtils.getCountAsInt(sql));
                hasGeoCoords    = BasicSQLUtils.getCountAsInt(sql) > 0;
                return null;
            }

            @Override
            protected void done()
            {
               if (hasGeoCoords || hasLocDetails)
               {
                   MultipleRecordCleanupDlg mrcDlg = null;
                   try
                   {
                       MultipleRecordComparer mrc = new MultipleRecordComparer(fii, 
                                                                               Locality.getClassTableId(), 
                                                                               LocalityDetail.getClassTableId(), 
                                                                               GeoCoordDetail.getClassTableId());
                       mrc.setSingleRowIncluded(true, true);
                       //mrc.addDisplayColumn("FullName", "Full Name", "CONCAT(LocalityName, '-') AS FullName");
                       mrc.addDisplayColumn("LocalityName");
                       
                       mrc.loadData();
                       
                       mrcDlg = new MultipleRecordCleanupDlg(mrc, "Locality Cleanup");
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
                       }
                       
                   } catch (Exception ex)
                   {
                       ex.printStackTrace();
                   }
                   
                   if (isContinuing)
                   {
                       MergeInfo       mainItem = mrcDlg.getMainMergedInfo();
                       List<MergeInfo> kidItems = mrcDlg.getKidsMergedInfo();
                       if (!cleanupMerges(Locality.getClassTableId(), mainItem, kidItems))
                       {
                           String msg = String.format("There was an error cleaning up addresses for agent'%s'", fii.getValue().toString());
                           showError(msg);
                           log.error(msg);
                           isContinuing = false;
                       }
                   }
                       
                   if (isContinuing)
                   {
                       prgDlg.toFront();
                       doMergeOfLocalities(fii);
                   } else
                   {
                       prgDlg.setVisible(false);
                       prgDlg.dispose();
                   }
               } else
               {
                   doMergeOfLocalities(fii);
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
        DBTableInfo parentTI = DBTableIdMgr.getInstance().getInfoById(parentTblId);
        
        // Find Merged into record (Master)
        MergeInfoItem intoRec = mainItem.getMergeInto();
        if (intoRec == null)
        {
            String msg = String.format("No 'Primary' record for %s", parentTI.getTitle());
            showError(msg);
            log.error(msg);
            return false;
        }
        
        // Merge Kid's Information before removing unwanted 'parent' records.
        for (MergeInfo kidMergeInfo : kidItems)
        {
            DBTableInfo ti = kidMergeInfo.getTblInfo();
            
            for (MergeInfoItem mi : kidMergeInfo.getMergeNotIncluded())
            {
                String sql = String.format("DELETE FROM %s WHERE %s = %d", ti.getName(), ti.getIdColumnName(), mi.getId());
                logSQL(sql);
                if (BasicSQLUtils.update(sql) != 1)
                {
                    String msg = String.format("Error deleting child record for %s (record id %d)", ti.getTitle(), mi.getId());
                    showError(msg);
                    log.error(msg);
                    return false;
                }
            }
            
            for (MergeInfoItem mi : kidMergeInfo.getMergeIncluded())
            {
                String sql = String.format("UPDATE %s SET %s=%d WHERE %s = %d", 
                        ti.getName(), parentTI.getIdColumnName(), intoRec.getId(), ti.getIdColumnName(), mi.getId());
                logSQL(sql);
                if (BasicSQLUtils.update(sql) != 1)
                {
                    String msg = String.format("Error updating child record for %s (record id %d)", ti.getTitle(), mi.getId());
                    showError(msg);
                    log.error(msg);
                    return false;
                }
            }
        }
        
        DBTableInfo ti = mainItem.getTblInfo();
        for (MergeInfoItem mi : mainItem.getMergeFrom())
        {
            String sql = String.format("DELETE FROM %s WHERE %s = %d", ti.getName(), ti.getIdColumnName(), mi.getId());
            logSQL(sql);
            if (BasicSQLUtils.update(sql) != 1)
            {
                String msg = String.format("Error deleting 'merge from' record for %s (record id %d)", parentTI.getTitle(), mi.getId());
                showError(msg);
                log.error(msg);
                return false;
            }
        }
        
        //outputRows[2].append("Removed Address: "+addrStr+"<BR>");
        //outputRows[2].append("Updated Address: "+getAddrStr(ai.id)+"<BR>");

        return true;
    }

    
    /**
     * @param fii
     */
    private void doProcessMerge(final FindItemInfo fii)
    {
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
            
            String sql = String.format("SELECT COUNT(*) FROM collectingevent WHERE LocalityID IN %s", inClause);
            logSQL(sql);
            int totalCount = BasicSQLUtils.getCountAsInt(sql);
            logSQL("TotCnt: "+totalCount+"\n"+sql);
            if (totalCount > 0)
            {
                sql = String.format("UPDATE collectingevent SET LocalityID=%d WHERE LocalityID IN %s", fii.getId(), inClause);
                logSQL(sql);
                int numChanged = BasicSQLUtils.update(sql);
                if (numChanged != totalCount)
                {
                    String msg = String.format("Error updating AgentIds - Should have updated %d, only updated %d", totalCount, numChanged);
                    showError(msg);
                    isContinuing = false;
                    isError      = true;
                } else
                {
                    wasUpdated = true;
                }
                
                // Now Add them to the Set of already fixed AgentIds
                if (usedIds != null)
                {
                    usedIds.add(fii.getId());
                    usedIds.addAll(fii.getDuplicateIds());
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
            String sql = String.format("DELETE FROM locality WHERE LocalityID IN %s", inClause);
            logSQL(sql);
            int totalCount = fii.getDuplicateIds().size();
            int numChanged = BasicSQLUtils.update(sql);
            if (numChanged != totalCount)
            {
                String msg = String.format("Error deleting LocalityIDs - Should have updated %d, only updated %d", totalCount, numChanged);
                showError(msg);
                isContinuing = false;
                isError      = true;
            }
        }
    }
    
    /**
     * @param fii
     */
    private void doMergeOfLocalities(final FindItemInfo fii)
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
                if (hasMoreLocalties())
                {
                    nextLocality();
                } else
                {
                    doComplete();
                }
            }
        };
        worker.execute();
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
     * @return
     */
    private boolean hasMoreLocalties()
    {
        if (cleanupIndexer != null)
        {
            return cleanupIndexer.hasMoreLocalities();
        }
        
        return currIndex < itemsList.size();    
    }
    
    /**
     * 
     */
    private void nextLocality()
    {
        FindItemInfo fii;
        if (cleanupIndexer != null)
        {
            fii = cleanupIndexer.getNextLocality();
            if (fii == null)
            {
                if (cleanupIndexer.isQuitting() || !cleanupIndexer.hasMoreLocalities())
                {
                    doComplete();
                    return;
                }
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        nextLocality();
                    }
                });
            }
        } else
        {
            fii = itemsList.get(currIndex);
            System.out.println(String.format("%s - %d", fii.getValue(), fii.getCount()));
        }
        processNextLocality(fii);
    }
    
    /**
     * @param fii
     */
    private void processNextLocality(final FindItemInfo fii)
    {
        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
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
                
                //doMergeOfLocalities(fii);
                checkForAddrs(fii);
                currIndex++;
                return null;
            }

            @Override
            protected void done()
            {
                prgDlg.setOverall(currIndex);
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
        
        if (cleanupIndexer != null && cleanupIndexer.hasFoundNothing())
        {
            displayInfoMsgDlg(getResourceString("CLNUP_NO_MATCHES"));
            
        } else if (cleanupIndexer == null || !cleanupIndexer.isQuitting())
        {
            displayInfoMsgDlg(getResourceString("DONE"));
        }
        
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
}

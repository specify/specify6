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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.ProgressFrame;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jan 15, 2010
 *
 */
public class DisciplineDuplicator
{
    protected static final Logger            log         = Logger.getLogger(DisciplineDuplicator.class);

    protected Connection    newDBConn; 
    protected TableWriter   tblWriter;
    protected ProgressFrame prgFrame;
    
    
    /**
     * @param newDBConn
     * @param tblWriter
     * @param prgFrame
     */
    public DisciplineDuplicator(final Connection newDBConn, final TableWriter tblWriter, final ProgressFrame prgFrame)
    {
        super();
        this.newDBConn = newDBConn;
        this.tblWriter = tblWriter;
        this.prgFrame = prgFrame;
    }


    /**
     * 
     */
    public void duplicateCollectingEvents()
    {
        Statement stmt  = null;
        Statement stmt2  = null;
        Statement uStmt = null;
    
        try
        {
            PreparedStatement pStmt = newDBConn.prepareStatement("UPDATE collectingevent SET DisciplineID=? WHERE CollectingEventID=?");
            stmt  = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt2 = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            uStmt = newDBConn.createStatement();
            
            String sql;
            int cnt = 0;
            
            StringBuilder fieldNames = new StringBuilder();
            DBTableInfo ceTbl = DBTableIdMgr.getInstance().getByShortClassName("CollectingEvent");
            cnt = 0;
            for (DBFieldInfo fi : ceTbl.getFields())
            {
                if (!fi.getColumn().equals(ceTbl.getIdColumnName()))
                {
                    if (cnt > 0) fieldNames.append(',');
                    fieldNames.append(fi.getColumn());
                }
                cnt++;
            }
            
            Vector<Integer> changedCEIds = new Vector<Integer>();
            
            cnt = 0;
            sql = " FROM (SELECT ce.CollectingEventID, COUNT(ce.CollectingEventID) as cnt FROM collectionobject co " +
                  "INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID GROUP BY ce.CollectingEventID) T1 WHERE cnt > 1";
            int total = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) " + sql);
            
            if (prgFrame != null) prgFrame.setProcess(0, total);
            ResultSet rs = stmt.executeQuery("SELECT T1.CollectingEventID" + sql);
            while (rs.next())
            {
                /*
                     "CollectingEventID","DisciplineID","CollectionObjectID","CollectionMemberID", "Collection.DisciplineID"
                         2058,               7,               14930,                 4                       3
                         2058,               7,               7894,                  6                       7
                 */
                sql = String.format(" FROM (SELECT ce.CollectingEventID, ce.DisciplineID, co.CollectionObjectID, CollectionMemberID FROM collectionobject co " +
                                   "INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID WHERE ce.CollectingEventID = %d) T1 " +
                                   "INNER JOIN collection c ON CollectionMemberID = c.collectionId " +
                                   "WHERE T1.DisciplineID != c.DisciplineID GROUP BY CollectionMemberID", rs.getInt(1));
                //log.debug("SELECT COUNT(*)" + sql);
                //int count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*)" + sql);
                
                sql = String.format("SELECT CollectingEventID, T1.DisciplineID, CollectionObjectID, CollectionMemberID, c.DisciplineID %s", sql);
                //log.debug(sql);

                int ceCnt = 0;
                ResultSet rs2 = stmt2.executeQuery(sql);
                while (rs2.next())
                {
                    Integer newCEId = null;
                    if (ceCnt == 0)
                    {
                        newCEId = rs2.getInt(1);
                    } else
                    {
                        sql = String.format("INSERT INTO collectingevent (%s) (SELECT %s FROM collection WHERE CollectingEventID = %d)", fieldNames.toString(), fieldNames.toString(), rs2.getInt(1));
                        //log.debug(sql);
                        uStmt.executeUpdate(sql);
                        newCEId = BasicSQLUtils.getInsertedId(uStmt);
                    }
                    
                    // Update the existing or new CE with the correct Discipline. 
                    pStmt.setInt(1, rs2.getInt(5));
                    pStmt.setInt(2, newCEId);
                    pStmt.execute();

                    //System.out.println("    " + rs2.getInt(2) + " -> " + rs2.getInt(5));
                    changedCEIds.add(newCEId);
                    ceCnt++;
                }
                rs2.close();

                cnt++;
                if (cnt % 500 == 0)
                {
                    log.debug("Processed: "+cnt);
                    if (prgFrame != null) prgFrame.setProcess(cnt);
                }
            }
            log.debug("Processed: "+cnt);
            rs.close(); 
            pStmt.close();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                stmt2.close();
                uStmt.close();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
    

    /**
     * 
     */
    public void duplicateLocalities()
    {
        Statement stmt  = null;
        Statement stmt2  = null;
        Statement uStmt = null;
    
        try
        {
            PreparedStatement pStmt = newDBConn.prepareStatement("UPDATE locality SET DisciplineID=? WHERE LocalityID=?");
            PreparedStatement pStmtCE = newDBConn.prepareStatement("UPDATE collectingevent SET LocalityID=? WHERE CollectingEventID=?");
            
            stmt  = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt2 = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            uStmt = newDBConn.createStatement();
            
            String sql;
            int cnt = 0;
            
            StringBuilder fieldNames = new StringBuilder();
            DBTableInfo ceTbl = DBTableIdMgr.getInstance().getByShortClassName("Locality");
            cnt = 0;
            for (DBFieldInfo fi : ceTbl.getFields())
            {
                if (!fi.getColumn().equals(ceTbl.getIdColumnName()))
                {
                    if (cnt > 0) fieldNames.append(',');
                    fieldNames.append(fi.getColumn());
                }
                cnt++;
            }
            
            cnt = 0;
            String fromSQL = " FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID WHERE l.DisciplineID != ce.DisciplineID";
            int total = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) " + fromSQL);
            
            System.out.println("Total: "+total);
            
            if (prgFrame != null) prgFrame.setProcess(0, total);
            sql = String.format("SELECT l.LocalityID %s GROUP BY l.LocalityID", fromSQL);
            log.debug(sql);
            
            boolean debug = false;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                int localityId = rs.getInt(1);
                if (localityId == 326)
                {
                    debug = true;
                }
                
                // This returns how many different CollectingEvent Disciplines there are.
                // If it returns a '1' then all the CEs using this Locality come from the same Discipline
                // If it returns more than '1' then we need to duplicate the localities
                sql = String.format("SELECT COUNT(*) FROM (SELECT CEDSPID, COUNT(CEDSPID) FROM " +
                		"(SELECT l.LocalityID, l.DisciplineID as LOCDSPID, ce.CollectingEventID, ce.DisciplineID as CEDSPID " +
                		"FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
                		"WHERE l.DisciplineID != ce.DisciplineID AND l.LocalityID = %d) T1 GROUP BY CEDSPID) T2", localityId);
                
                if (debug) log.debug(sql);
                if (BasicSQLUtils.getCountAsInt(sql) == 1)
                {
                    // Get the CE's Discipline
                    sql = String.format("SELECT ce.DisciplineID FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
                    	                "WHERE l.DisciplineID != ce.DisciplineID AND l.LocalityID = %d LIMIT 0,1", localityId);
                    int ceDiscipline = BasicSQLUtils.getCountAsInt(sql);
                    
                    // Update the existing Locality with the correct Discipline. 
                    // This is a shortcut
                    pStmt.setInt(1, ceDiscipline);
                    pStmt.setInt(2, localityId);
                    pStmt.execute();
                    
                } else
                {
                    sql = String.format("SELECT CEDSPID, COUNT(CEDSPID) FROM " +
                                		"(SELECT l.LocalityID, l.DisciplineID as LOCDSPID, ce.CollectingEventID, ce.DisciplineID as CEDSPID " +
                                		"FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
                                		"WHERE l.DisciplineID != ce.DisciplineID AND l.LocalityID = %d) T1 GROUP BY CEDSPID", localityId);
                    if (debug) log.debug(sql);
                    
                    int locCnt = 0;
                    ResultSet rs2 = stmt2.executeQuery(sql);
                    while (rs2.next())
                    {
                        // Don't duplicate the first one
                        // Just update the Discipline
                        if (locCnt == 0)
                        {
                            pStmt.setInt(1, rs2.getInt(1));
                            pStmt.setInt(2, localityId);
                            pStmt.execute();
                            
                        } else
                        {
                            int ceDspId = rs2.getInt(1);
                            sql = String.format("SELECT l.LocalityID, l.DisciplineID, ce.CollectingEventID, ce.DisciplineID " + 
                                                "FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " + 
                                                "WHERE l.DisciplineID != ce.DisciplineID AND l.LocalityID = %d AND ce.DisciplineID = %d ORDER BY CEDSPID", localityId, ceDspId);
                            log.debug(sql);
                            
                            int       ceLocCnt = 0;
                            Integer   newLocID = null;
                            ResultSet rs3      = stmt2.executeQuery(sql);
                            while (rs3.next())
                            {
                                if (ceLocCnt == 0)
                                {
                                    sql = String.format("INSERT INTO locality (%s) (SELECT %s FROM locality WHERE LocalityID = %d)", fieldNames.toString(), fieldNames.toString(), localityId);
                                    uStmt.executeUpdate(sql);
                                    newLocID = BasicSQLUtils.getInsertedId(uStmt);
                                    
                                    pStmt.setInt(1, rs2.getInt(4));
                                    pStmt.setInt(2, newLocID);
                                    pStmt.execute();
                                }
                                
                                pStmtCE.setInt(1, newLocID);
                                pStmtCE.setInt(2, rs2.getInt(3));
                                pStmtCE.execute();
                                
                                ceLocCnt++;
                            }
                            rs3.close();
                        }
                        locCnt++;
                    }
                    rs2.close();
                }
                
                cnt++;
                if (cnt % 500 == 0)
                {
                    log.debug("Processed: "+cnt);
                    if (prgFrame != null) prgFrame.setProcess(cnt);
                }
            }
            log.debug("Processed: "+cnt);
            rs.close(); 
            
            pStmt.close();
            pStmtCE.close();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                stmt2.close();
                uStmt.close();
                
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
    
}

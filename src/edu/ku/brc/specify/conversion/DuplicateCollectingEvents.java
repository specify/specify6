/**
 * 
 */
package edu.ku.brc.specify.conversion;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo.RelationshipType;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttribute;
import edu.ku.brc.ui.CustomFrame;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;

/**
 * @author timo
 * @author rods
 *
 * duplicateCollectingEvents() ensures that for 'embedded collecting event' collections, the effective relationship between
 * CollectingEvent AND CollectionObject is one-to-one.
 * 
 * Any CollectingEvents that have N > 1 related CollectionObjects are cloned N times to create a distinct CollectingEvent 
 * for each related CollectionObject.
 * 
 */
public class DuplicateCollectingEvents
{
    protected static final Logger  log = Logger.getLogger(DuplicateCollectingEvents.class);
    
    protected Connection    connection;
    protected ProgressFrame progressFrame;
    protected int           createdByAgentId;
    protected int           disciplineId;
    
    protected Vector<Integer> cesNoCOList = new Vector<Integer>(1000);
    
    /**
     * 
     */
    public DuplicateCollectingEvents(final Connection connection, 
                                     final ProgressFrame progressFrame,
                                     final int createdByAgentId,
                                     final int disciplineId)
    {
        this.connection       = connection;
        this.progressFrame    = progressFrame;
        this.createdByAgentId = createdByAgentId;
        this.disciplineId     = disciplineId;
    }        
    
    /**
     * @param collectionId
     * @return
     */
    private Vector<Object> getCollectingEventsWithManyCollectionObjects()
    {
        String sql = "SELECT * FROM (SELECT CollectingEventID, count(*) AS cnt FROM collectionobject c WHERE " +
                     "CollectingEventID IS NOT NULL GROUP BY CollectingEventID) T1 WHERE cnt > 1";
        return BasicSQLUtils.querySingleCol(connection, sql);
        
    }
    
    /**
     * @param collectionId
     * @return
     */
    private int getCollectingEventsWithManyCollectionObjectsCount()
    {
        String sql = "SELECT SUM(CNT) FROM (SELECT count(*) AS cnt FROM collectionobject c WHERE " +
                     "CollectingEventID IS NOT NULL GROUP BY CollectingEventID) T1 WHERE cnt > 1";
        return BasicSQLUtils.getCountAsInt(connection, sql);
        
    }
    
    /**
     * @param collectionId id of an 'embedded collecting event' collection.
     * @throws Exception
     */
    protected int duplicateCollectingEvents()
    {
        // CollectingEvent
        PreparedStatement prepCEStmt = null;
        Statement         stmtCE     = null;
        
        // CollectingEvent Attributes
        PreparedStatement prepCEAStmt = null;
        Statement         stmtCEA     = null;

        try
        {
            String selectCESQL  = createSelectStmt(CollectingEvent.getClassTableId(), "CollectingEventID = %d");
            prepCEStmt          = createPreparedStmt(CollectingEvent.getClassTableId());
            stmtCE              = connection.createStatement();
            
            String selectCEASQL = createSelectStmt(CollectingEventAttribute.getClassTableId(), "CollectingEventAttributeID = %d");
            prepCEAStmt         = createPreparedStmt(CollectingEventAttribute.getClassTableId());
            stmtCEA             = connection.createStatement();
            
            log.debug(selectCESQL);
            log.debug(selectCEASQL);
            
            int totalCnt = getCollectingEventsWithManyCollectionObjectsCount();
            log.debug(String.format("%d CEs with more than one CO for all Collections", totalCnt));
            
            progressFrame.setProcess(0, totalCnt);
            
            Vector<Object> list = getCollectingEventsWithManyCollectionObjects();
            int cnt      = 0;
            int percent  = 0;
            for (Object ceID : list)
            {
                cnt += duplicateCollectingEvent((Integer)ceID, String.format(selectCESQL, ceID), stmtCE, prepCEStmt, 
                                                               selectCEASQL, stmtCEA, prepCEAStmt);
                int p = (int)(cnt * 100.0 / (double)totalCnt);
                if (p != percent)
                {
                    progressFrame.setProcess(cnt);
                    percent = p;
                }
                //System.out.println((int)((cnt*100.0) / (double)totalCnt) + " "+cnt + "  "+totalCnt);
            }
            
            if (cnt != totalCnt)
            {
                log.debug(String.format("%d processed doesn't match %d", cnt, totalCnt));
            }
            
            progressFrame.setProcess(cnt);
            
            return cnt;
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (prepCEStmt != null) prepCEStmt.close();
                if (stmtCE != null) stmtCE.close();
            } catch (Exception ex) {}
        }
        return 0;
    }
    
    /**
     * @param rsmd
     * @param columnName
     * @return the index of the colun by name, zero means not found
     * @throws SQLException
     */
    private int getColumnIndex(final ResultSetMetaData rsmd, final String columnName) throws SQLException
    {
        for (int i=1;i<=rsmd.getColumnCount();i++)
        {
            if (rsmd.getColumnName(i).equals(columnName))
            {
                return i;
            }
        }
        return 0;
    }
    
    /**
     * @param ceid id for a collecting event with many (> 1) collection objects
     * @throws Exception
     */
    protected int duplicateCollectingEvent(final int       ceID,
                                           final String    selectCEStr, 
                                           final Statement stmtCE,
                                           final PreparedStatement prepCEStmt,
                                           final String    selectCEAStr, 
                                           final Statement stmtCEA,
                                           final PreparedStatement prepCEAStmt) throws SQLException
    {
        Statement stmt2 = null;
        try
        {
            stmt2 = connection.createStatement();
            
            int cnt = 0;
            
            ResultSet         rs   = stmtCE.executeQuery(selectCEStr);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            int dispInx = getColumnIndex(rsmd, "DisciplineID");
            int ceaInx  = getColumnIndex(rsmd, "CollectingEventAttributeID");
            
            while (rs.next())
            {
                Integer ceaId = rs.getObject(ceaInx) != null ? rs.getInt(ceaInx) : null; // get the CollectingEventAttribute
                
                String    sql  = "SELECT CollectionObjectID FROM collectionobject WHERE CollectingEventID = " + ceID;
                ResultSet coRS = stmt2.executeQuery(sql);
                if (coRS.next()) // skip the first one, that one is already hooked up.
                {
                     int dispId = BasicSQLUtils.getCountAsInt("SELECT DisciplineID FROM collectingevent WHERE CollectingEventID = " + ceID);
                    
                    while (coRS.next())
                    {
                        int coId   = coRS.getInt(1);
                        
                        for (int i=1;i<=rs.getMetaData().getColumnCount();i++)
                        {
                            prepCEStmt.setObject(i, rs.getObject(i));
                        }
                        prepCEStmt.setInt(dispInx, dispId);
                        
                        if (prepCEStmt.executeUpdate() != 1)
                        {
                            throw new RuntimeException("Couldn't insert CE row.");
                        }
                        
                        int newCEID = BasicSQLUtils.getInsertedId(prepCEStmt);
                        cnt++;
                        
                        sql = String.format("UPDATE collectionobject SET CollectingEventID=%d WHERE CollectionObjectID = %d", newCEID, coId);
                        if (BasicSQLUtils.update(sql) != 1)
                        {
                            throw new RuntimeException(sql+" didn't update CO correctly.");
                        }
                        
                        // Now duplicate CollectingEventAttribute
                        if (ceaId != null)
                        {
                            ResultSet ceaRS      = stmtCEA.executeQuery(String.format(selectCEAStr, ceaId));
                            int       dispCEAInx = getColumnIndex(ceaRS.getMetaData(), "DisciplineID");
                            if (ceaRS.next())
                            {
                                for (int i=1;i<=ceaRS.getMetaData().getColumnCount();i++)
                                {
                                    prepCEAStmt.setObject(i, ceaRS.getObject(i));
                                }
                                prepCEAStmt.setInt(dispCEAInx, dispId);
                                
                                if (prepCEAStmt.executeUpdate() != 1)
                                {
                                    throw new RuntimeException("Couldn't insert CEA row.");
                                }
                                
                                int newCEAID = BasicSQLUtils.getInsertedId(prepCEAStmt);
                                
                                sql = String.format("UPDATE collectingevent SET CollectingEventAttributeID=%d WHERE CollectingEventID = %d", newCEAID, newCEID);
                                if (BasicSQLUtils.update(sql) != 1)
                                {
                                    String msg = "["+sql+"] didn't update CE correctly.";
                                    log.error("************************ "+msg);
                                    //throw new RuntimeException("["+sql+"] didn't update CE correctly.");
                                }
                            }
                            ceaRS.close();
                        }
                        
                    } 
                }
                coRS.close();
            }
            rs.close();
            
            return cnt;
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            log.error(ex);
            
        } finally
        {
            try
            {
                if (stmt2 != null) stmt2.close();
            } catch (SQLException ex){}
        }
        return 0;
    }
    
    @SuppressWarnings("unused")
    private void showStats(final boolean isStart)
    {
        String noCOSQL       = " FROM collectingevent ce LEFT JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID WHERE co.CollectionObjectID IS NULL";
        int totalWithCOs     = BasicSQLUtils.getCountAsInt(connection, "SELECT COUNT(ce.CollectingEventID) FROM collectingevent ce INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID");
        int totalCEWithoutCO = BasicSQLUtils.getCountAsInt(connection, "SELECT COUNT(ce.CollectingEventID)" + noCOSQL);
        
        int totalCE          = BasicSQLUtils.getCountAsInt(connection, "SELECT COUNT(*) FROM collectingevent");
        int totalCO          = BasicSQLUtils.getCountAsInt(connection, "SELECT COUNT(*) FROM collectionobject");
        
        int coWithoutCE      = BasicSQLUtils.getCountAsInt(connection, "SELECT COUNT(*) FROM collectionobject WHERE CollectingEventID is NULL");
        
        String msg = "<HTML><table border=\"1\">" + 
        "<tr><td >Total CO w/o CE</td><td>" + coWithoutCE + "</td></tr>" +
        "<tr><td>Total CE w/  CO</td><td>" + totalWithCOs + "</td></tr>" +
        "<tr><td>Total CE w/o CO</td><td>" + totalCEWithoutCO + "</td></tr>" +
        "<tr><td>Dif          </td><td>" + (totalCEWithoutCO - totalWithCOs) + "</td></tr>" +
        "<tr><td>Total CE     </td><td>" + totalCE + "</td></tr>" +
        "<tr><td>Total CO     </td><td>" + totalCO + "</td></tr>" +
        "<tr><td>Dif          </td><td>" + (totalCE - totalCO) + "</td></tr>" +
        "</table>";
        
        Vector<Object> list = BasicSQLUtils.querySingleCol(connection, "SELECT ce.CollectingEventID " + noCOSQL);
        if (isStart)
        {
            int i = 0;
            for (Object obj : list)
            {
                Integer id = (Integer)obj;
                cesNoCOList.add(id);
                System.out.print(id+", ");
                i++;
                if (i % 10 == 0)
                {
                    System.out.println("");
                }
            }
            System.out.println("");
            
        } else
        {
            HashSet<Integer> hashSet = new HashSet<Integer>();
            for (Integer id : cesNoCOList)
            {
                hashSet.add(id);
            }
            Vector<Integer> diffList = new Vector<Integer>();
            for (Object obj : list)
            {
                Integer id = (Integer)obj;
                if (!hashSet.contains(id))
                {
                    diffList.add(id);
                }
            }
            
            System.out.println("-------- CE with no COs -------- ");
            int i = 0;
            for (Integer id : diffList)
            {
                System.out.print(id+", ");
                i++;
                if (i % 10 == 0)
                {
                    System.out.println();
                }
            }
            System.out.println("");
            
            //------------------------------------------
            //-- 
            //------------------------------------------
            hashSet.clear();
            Hashtable<Integer, Boolean> hash = new Hashtable<Integer, Boolean>();
            for (Object idObj : list)
            {
                hash.put((Integer)idObj, Boolean.TRUE);
            }
            diffList.clear();
            for (Integer id : cesNoCOList)
            {
                if (hash.get(id) == null)
                {
                    diffList.add(id);
                }
            }
            
            System.out.println("=========== CE with no COs =========== ");
            for (Integer id : diffList)
            {
                System.out.print(id+", ");
                i++;
                if (i % 10 == 0)
                {
                    System.out.println();
                }
            }
            System.out.println("");

        }
        
        log.info(msg);
        JPanel panel = new JPanel(new BorderLayout());
        JLabel lbl   = UIHelper.createLabel(msg);
        panel.add(lbl, BorderLayout.CENTER);
        //UIRegistry.showLocalizedMsg(msg);
        CustomFrame statsFrame = new CustomFrame((isStart ? "Before" : "After" ) + " Duplication", CustomFrame.OK_BTN, panel);
        statsFrame.pack();
        statsFrame.setVisible(true);
    }
    
    /**
     * @throws Exception
     * 
     * Duplicates collecting events for all 'embedded collecting event' collections in the database. 
     */
    public void performMaint()
    {
        //showStats(true);
        
        Vector<Integer> collectionsIds = BasicSQLUtils.queryForInts("SELECT CollectionID FROM collection WHERE IsEmbeddedCollectingEvent <> 0");
        if (collectionsIds == null || collectionsIds.size() == 0)
        {
            return;
        }
        
        progressFrame.setDesc("Fixing Collecting Events...");
        
        int count = duplicateCollectingEvents();
        
        addCEsForCOWithNone();
        
        log.debug("*** Total for Collections: " + count);
        
        //showStats(false);
    }
    
    private void addCEsForCOWithNone()
    {
        try
        {
            String sql = "INSERT INTO collectingevent (TimestampCreated, TimestampModified, Version, CreatedByAgentID, ModifiedByAgentID, DisciplineID) VALUES(?,?,?,?,?,?)";
            PreparedStatement pStmt = connection.prepareStatement(sql);
            
            Calendar now = Calendar.getInstance();
            
            for (Object idObj : BasicSQLUtils.querySingleCol(connection, "SELECT CollectionObjectID FROM collectionobject WHERE CollectingEventID is NULL"))
            {
                Integer coID = (Integer)idObj;
                
                pStmt.setTimestamp(1, new Timestamp(now.getTime().getTime()));
                pStmt.setTimestamp(2, new Timestamp(now.getTime().getTime()));
                pStmt.setInt(3, 0);
                pStmt.setInt(4, createdByAgentId);
                pStmt.setInt(5, createdByAgentId);
                pStmt.setInt(6, disciplineId);
                
                if (pStmt.executeUpdate() != 1)
                {
                    throw new RuntimeException(sql+" didn't update correctly.");
                }
                
                int newCEID = BasicSQLUtils.getInsertedId(pStmt);
                
                sql = String.format("UPDATE collectionobject SET CollectingEventID=%d WHERE CollectionObjectID = %d", newCEID, coID);
                if (BasicSQLUtils.update(sql) != 1)
                {
                    throw new RuntimeException(sql+" didn't update correctly.");
                }
            }
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }

    }
    
    /**
     * @param tableID
     * @return
     * @throws SQLException
     */
    private PreparedStatement createPreparedStmt(final int tableID) throws SQLException
    {
        DBTableInfo   tblInfo = DBTableIdMgr.getInstance().getInfoById(tableID);
        StringBuilder sb      = new StringBuilder("INSERT INTO " + tblInfo.getName() + " (");
        
        int fldCnt = 0;
        String keyName = tblInfo.getIdFieldName();
        for (DBFieldInfo fi : tblInfo.getFields())
        {
            if (!fi.getColumn().equals(keyName))
            {
                sb.append(fi.getColumn());
                sb.append(',');
                fldCnt++;
            }
        }
        
        for (DBRelationshipInfo ri : tblInfo.getRelationships())
        {
            if (ri.getType() == RelationshipType.ManyToOne)
            {
                sb.append(ri.getColName());
                sb.append(',');
                fldCnt++;
            }
        }
        
        sb.setLength(sb.length()-1); // chomp unneeded comma
        sb.append(") VALUES(");
        for (int i=0;i<fldCnt;i++)
        {
            sb.append("?,");
        }
        sb.setLength(sb.length()-1); // chomp unneeded comma
        sb.append(")");
        log.debug(sb.toString());
        
        return connection.prepareStatement(sb.toString());
    }
    
    /**
     * @param tableID
     * @param whereClauseStr
     * @return
     */
    private String createSelectStmt(final int tableID, final String whereClauseStr)
    {
        DBTableInfo   tblInfo = DBTableIdMgr.getInstance().getInfoById(tableID);
        StringBuilder sb = new StringBuilder("SELECT ");
        
        for (DBFieldInfo fi : tblInfo.getFields())
        {
            sb.append(fi.getColumn());
            sb.append(',');
        }
        for (DBRelationshipInfo ri : tblInfo.getRelationships())
        {
            if (ri.getType() == RelationshipType.ManyToOne)
            {
                sb.append(ri.getColName());
                sb.append(',');
            }
        }
        sb.setLength(sb.length()-1); // chomp unneeded comma
        sb.append(" FROM ");
        sb.append(tblInfo.getName());
        
        if (StringUtils.isNotEmpty(whereClauseStr))
        {
            sb.append(" WHERE ");
            sb.append(whereClauseStr);
        }
        log.debug(sb.toString());
        return sb.toString();
    }
}

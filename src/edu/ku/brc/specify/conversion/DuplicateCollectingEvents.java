/**
 * 
 */
package edu.ku.brc.specify.conversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo.RelationshipType;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIRegistry;

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
    private Vector<Object> getCollectingEventsWithManyCollectionObjects(final int collectionId)
    {
        String sql = "SELECT * FROM (SELECT CollectingEventID, count(*) AS cnt FROM collectionobject c WHERE " +
                     "CollectingEventID IS NOT NULL AND CollectionMemberID = " + collectionId + " GROUP BY CollectingEventID) T1 WHERE cnt > 1";
        return BasicSQLUtils.querySingleCol(connection, sql);
        
    }
    
    /**
     * @param collectionId
     * @return
     */
    private int getCollectingEventsWithManyCollectionObjectsCount(final int collectionId)
    {
        String sql = "SELECT SUM(CNT) FROM (SELECT CollectingEventID, count(*) AS cnt FROM collectionobject c WHERE " +
                     "CollectingEventID IS NOT NULL AND CollectionMemberID = " + collectionId + " GROUP BY CollectingEventID) T1 WHERE cnt > 1";
        return BasicSQLUtils.getCountAsInt(connection, sql);
        
    }
    
    /**
     * @param collectionId id of an 'embedded collecting event' collection.
     * @throws Exception
     */
    protected int duplicateCollectingEvents(final int collectionId)
    {
        try
        {
            PreparedStatement prepStmt   = createPreparedStmt(CollectingEvent.getClassTableId());
            String            selectSQL  = createSelectStmt(CollectingEvent.getClassTableId(), "CollectingEventID = %d");
            Statement         stmt       = connection.createStatement();
            
            log.debug(selectSQL);
            
            int totalCnt = getCollectingEventsWithManyCollectionObjectsCount(collectionId);
            
            progressFrame.setProcess(0, totalCnt);
            
            Vector<Object> list = getCollectingEventsWithManyCollectionObjects(collectionId);
            int cnt      = 0;
            int percent  = 0;
            for (Object ceID : list)
            {
                cnt += duplicateCollectingEvent(String.format(selectSQL, ceID), (Integer)ceID, stmt, prepStmt);
                int p = (int)(cnt * 100.0 / (double)totalCnt);
                if (p != percent)
                {
                    progressFrame.setProcess(cnt);
                    percent = p;
                }
                //System.out.println((int)((cnt*100.0) / (double)totalCnt) + " "+cnt + "  "+totalCnt);
            }
            progressFrame.setProcess(cnt);
            return cnt;
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return 0;
    }
    
    /**
     * @param ceid id for a collecting event with many (> 1) collection objects
     * @throws Exception
     */
    protected int duplicateCollectingEvent(final String    selectStr, 
                                           final int       ceID,
                                           final Statement stmt,
                                           final PreparedStatement prepStmt) throws SQLException
    {
        try
        {
            Statement stmt2 = connection.createStatement();
            
            int cnt = 0;
            
            ResultSet rs = stmt.executeQuery(selectStr);
            while (rs.next())
            {
                String sql = "SELECT CollectionObjectID FROM collectionobject WHERE CollectingEventID = " + ceID;
                ResultSet coRS = stmt2.executeQuery(sql);
                if (coRS.next()) // skip the first one, that one is already hooked up.
                {
                    cnt++;
                    
                    while (coRS.next())
                    {
                        for (int i=1;i<=rs.getMetaData().getColumnCount();i++)
                        {
                            prepStmt.setObject(i, rs.getObject(i));
                        }
                        if (prepStmt.executeUpdate() != 1)
                        {
                            throw new RuntimeException("Couldn't insert row.");
                        }
                        
                        int newCEID = BasicSQLUtils.getInsertedId(prepStmt);
                        cnt++;
                        
                        sql = String.format("UPDATE collectionobject SET CollectingEventID=%d WHERE CollectionObjectID = %d", newCEID, coRS.getInt(1));
                        if (BasicSQLUtils.update(sql) != 1)
                        {
                            throw new RuntimeException(sql+" didn't update correctly.");
                        }
                    }
                }
            }
            
            return cnt;
            
        } catch (SQLException ex)
        {
            log.error(ex);
        }
        return 0;
    }
    
    @SuppressWarnings("unused")
    private void showStats(final boolean isStart)
    {
        int totalCEWithoutCO = BasicSQLUtils.getCountAsInt(connection, "SELECT COUNT(ce.CollectingEventID) FROM collectingevent ce LEFT JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID");
        int totalWithCOs     = BasicSQLUtils.getCountAsInt(connection, "SELECT COUNT(ce.CollectingEventID) FROM collectingevent ce INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID");
        
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
        
        Vector<Object> list = BasicSQLUtils.querySingleCol(connection, "SELECT ce.CollectingEventID FROM collectingevent ce LEFT JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID WHERE co.CollectionObjectID is NULL");
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
        UIRegistry.showLocalizedMsg(msg);
    }
    
    /**
     * @throws Exception
     * 
     * Duplicates collecting events for all 'embedded collecting event' collections in the database. 
     */
    public void performMaint()
    {
        showStats(true);
        
        final ArrayList<Integer> collectionsIds = new ArrayList<Integer>(16);
        for (Object[] row : BasicSQLUtils.query("SELECT CollectionID FROM collection WHERE IsEmbeddedCollectingEvent = TRUE"))
        {
            collectionsIds.add((Integer)row[0]);
        }
        
        if (collectionsIds.size() == 0)
        {
            return;
        }
        
        int count = 0;
        for (Integer id : collectionsIds)
        {
            progressFrame.setDesc("Fixing Collecting Events...");
            
            count += duplicateCollectingEvents(id);
        }
        
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

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
import java.util.HashMap;
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
import edu.ku.brc.specify.datamodel.Collector;
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
    
    protected Connection    oldDBConn;
    protected Connection    newDBConn;
    protected ProgressFrame progressFrame;
    protected int           createdByAgentId;
    protected Integer       disciplineId;
    
    protected Vector<Integer> cesNoCOList = new Vector<Integer>(1000);
    
    /**
     * 
     */
    public DuplicateCollectingEvents(final Connection oldDBConn,
                                     final Connection newDBConn, 
                                     final ProgressFrame progressFrame,
                                     final int createdByAgentId,
                                     final Integer disciplineId)
    {
        this.oldDBConn        = oldDBConn;
        this.newDBConn        = newDBConn;
        this.progressFrame    = progressFrame;
        this.createdByAgentId = createdByAgentId;
        this.disciplineId     = disciplineId;
    }        
    
    /**
     * 
     */
    public DuplicateCollectingEvents(final Connection oldDBConn,
                                     final Connection newDBConn)
    {
        this(oldDBConn, newDBConn, null, 0, null);
        
        this.disciplineId     = BasicSQLUtils.getCount("SELECT DisciplineID FROM discipline ORDER BY DisciplineID LIMIT 0,1");
        this.createdByAgentId = BasicSQLUtils.getCount("SELECT AgentID FROM agent ORDER BY AgentID LIMIT 0,1");
    }
    
    /**
     * @return
     */
    private String getQueryPostfix()
    {
        return " FROM (SELECT ce.CollectingEventID, COUNT(ce.CollectingEventID) AS cnt FROM collectingevent ce " +
        "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID " +
        "INNER JOIN collectionobjectcatalog cc ON co.CollectionObjectID = cc.CollectionObjectCatalogID " +    
        "WHERE co.CollectionObjectTypeID > 8 AND co.CollectionObjectTypeID < 20 AND cc.SubNumber >= 0 " +
        "GROUP BY ce.CollectingEventID) T1 WHERE cnt > 1 ";
    }

    /**
     * @param collectionId
     * @return
     */
    private Vector<Integer> getCollectingEventsWithManyCollectionObjects(final Connection oldDBConn)
    {
        String sql = "SELECT CollectingEventID" + getQueryPostfix();
        return BasicSQLUtils.queryForInts(oldDBConn, sql);
        
    }
    
    /**
     * @param collectionId
     * @return
     */
    private int getCollectingEventsWithManyCollectionObjectsCount()
    {
        String sql = "SELECT SUM(cnt)" + getQueryPostfix();
        
        return BasicSQLUtils.getCountAsInt(oldDBConn, sql);
        
    }
    
    /**
     * @param collectionId id of an 'embedded collecting event' collection.
     * @throws Exception
     */
    protected int duplicateCollectingEvents()
    {
        IdTableMapper ceMapper = (IdTableMapper)IdMapperMgr.getInstance().get("collectingevent", "CollectingEventID");

        // CollectingEvent
        PreparedStatement prepCEStmt = null;
        Statement         stmtCE     = null;
        
        // CollectingEvent Collector
        PreparedStatement prepCECStmt = null;
        Statement         stmtCEC     = null;

        // CollectingEvent Attributes
        PreparedStatement prepCEAStmt = null;
        Statement         stmtCEA     = null;

        try
        {
            String selectCESQL  = createSelectStmt(CollectingEvent.getClassTableId(), "CollectingEventID = %d");
            prepCEStmt          = createPreparedStmt(newDBConn, CollectingEvent.getClassTableId());
            stmtCE              = newDBConn.createStatement();
            
            String selectCEASQL = createSelectStmt(CollectingEventAttribute.getClassTableId(), "CollectingEventAttributeID = %d");
            prepCEAStmt         = createPreparedStmt(newDBConn, CollectingEventAttribute.getClassTableId());
            stmtCEA             = newDBConn.createStatement();
            
            String selectCECSQL = createSelectStmt(Collector.getClassTableId(), "CollectingEventID = %d");
            prepCECStmt         = createPreparedStmt(newDBConn, Collector.getClassTableId());
            stmtCEC             = newDBConn.createStatement();
            
            log.debug(selectCESQL);
            log.debug(selectCEASQL);
            log.debug(selectCECSQL);
            
            int totalCnt = getCollectingEventsWithManyCollectionObjectsCount();
            log.debug(String.format("%d CEs with more than one CO for all Collections", totalCnt));
            
            if (progressFrame != null) progressFrame.setProcess(0, totalCnt);
            
            Vector<Integer> list = getCollectingEventsWithManyCollectionObjects(oldDBConn);
            int cnt      = 0;
            int percent  = 0;
            for (Integer ceID : list)
            {
                
                Integer newCEID = ceMapper.get(ceID);
                if (newCEID != null)
                {
                    cnt += duplicateCollectingEvent(newCEID, String.format(selectCESQL, newCEID), stmtCE, prepCEStmt, 
                                                    selectCEASQL, stmtCEA, prepCEAStmt, 
                                                    selectCECSQL, stmtCEC, prepCECStmt);
                    int p = (int)(cnt * 100.0 / (double)totalCnt);
                    if (p != percent)
                    {
                        if (progressFrame != null) progressFrame.setProcess(cnt);
                        percent = p;
                    }
                } else
                {
                    log.error("Unable to map old CEId ["+ceID+"] to new CE Id.");
                }
                //System.out.println((int)((cnt*100.0) / (double)totalCnt) + " "+cnt + "  "+totalCnt);
            }
            
            if (cnt != totalCnt)
            {
                log.debug(String.format("%d processed doesn't match %d", cnt, totalCnt));
            }
            
            if (progressFrame != null) progressFrame.setProcess(cnt);
            
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
                
                if (prepCEAStmt != null) prepCEAStmt.close();
                if (stmtCEA != null) stmtCEA.close();
                
                if (prepCECStmt != null) prepCECStmt.close();
                if (stmtCEC != null) stmtCE.close();
                
            } catch (Exception ex) {}
        }
        return 0;
    }
    
    /**
     * @param rsmd
     * @param columnName
     * @return the index of the column by name, zero means not found
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
                                           final PreparedStatement prepCEAStmt,
                                           final String    selectCECStr, 
                                           final Statement stmtCEC,
                                           final PreparedStatement prepCECStmt) throws SQLException
    {
        boolean doCollectors = true; // temp

        int dispId = BasicSQLUtils.getCountAsInt("SELECT DisciplineID FROM collectingevent WHERE CollectingEventID = " + ceID);
        int divId  = BasicSQLUtils.getCountAsInt("SELECT DivisionID FROM discipline WHERE DisciplineID = " + dispId);

        Statement stmt2 = null;
        try
        {
            stmt2 = newDBConn.createStatement();
            
            int cnt = 0;
            
            //log.debug(selectCEStr);
            ResultSet         rs   = stmtCE.executeQuery(selectCEStr);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            int dispInx = getColumnIndex(rsmd, "DisciplineID");
            int ceaInx  = getColumnIndex(rsmd, "CollectingEventAttributeID");
            
            Integer newCEID = null;
            
            while (rs.next())
            {
                Integer ceaId = rs.getObject(ceaInx) != null ? rs.getInt(ceaInx) : null; // get the CollectingEventAttribute
                
                String    sql  = "SELECT CollectionObjectID FROM collectionobject WHERE CollectingEventID = " + ceID;
                //log.debug(sql);
                ResultSet coRS = stmt2.executeQuery(sql);
                if (coRS.next()) // skip the first one, that one is already hooked up.
                {
                    while (coRS.next())
                    {
                        int coId = coRS.getInt(1);
                        
                        for (int i=1;i<=rs.getMetaData().getColumnCount();i++)
                        {
                            prepCEStmt.setObject(i, rs.getObject(i));
                        }
                        prepCEStmt.setInt(dispInx, dispId);
                        
                        if (prepCEStmt.executeUpdate() != 1)
                        {
                            throw new RuntimeException("Couldn't insert CE row.");
                        }
                        
                        newCEID = BasicSQLUtils.getInsertedId(prepCEStmt);
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
                        
                        if (doCollectors)
                        {
                            // Duplicate Collectors for (CE)
                            sql = String.format(selectCECStr, ceID);
                            ResultSet rsCEC = stmtCEC.executeQuery(sql);
                            while (rsCEC.next()) 
                            {
                                int divCECInx  = getColumnIndex(rsCEC.getMetaData(), "DivisionID");
                                int newCEIDInx = getColumnIndex(rsCEC.getMetaData(), "CollectingEventID");
    
                                for (int i=1;i<=rsCEC.getMetaData().getColumnCount();i++)
                                {
                                    //System.out.println(i+"  "+rsCEC.getMetaData().getColumnName(i)+"  "+rsCEC.getObject(i));
                                    prepCECStmt.setObject(i, rsCEC.getObject(i));
                                }
                                prepCECStmt.setInt(divCECInx, divId);
                                prepCECStmt.setInt(newCEIDInx, newCEID);
                                
                                if (prepCECStmt.executeUpdate() != 1)
                                {
                                    throw new RuntimeException("Couldn't insert CECollector row.");
                                }
                            }
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
                                           
    /**
     * @param oldDBConn
     * @param newDBConn
     */
    public void removeUnneededCEs()
    {
        log.debug("Deleting extra CollectingEvents.");
        try
        {
            PreparedStatement pStmt = newDBConn.prepareStatement("DELETE FROM collectingevent WHERE CollectingEventID = ?");
            String sql = "SELECT ce.CollectingEventID FROM collectingevent ce " +
                         "LEFT JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID WHERE co.CollectionObjectID IS NULL";
            log.debug(sql);
            Vector<Integer> ids = BasicSQLUtils.queryForInts(newDBConn, sql);
            int cnt   = 0;
            int rmCnt = 0;
            for (Integer id : ids)
            {
                pStmt.setInt(1, id);
                int rv = pStmt.executeUpdate();
                if (rv == 1)
                {
                    rmCnt++; 
                } else
                {
                    log.error("Couldn't remove CE Id: "+id);
                }
                cnt++;
                if (cnt % 1000 == 0)
                {
                    log.debug(String.format("%d / %d", cnt, ids.size()));
                }
            }
            pStmt.close();
            log.debug(String.format("Done deleting CollectingEvents. %d removed out of %d", rmCnt, cnt));
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    private boolean compareStr(final String oldStr, final String newStr)
    {
        if (oldStr == null && newStr == null)
        {
            return true;
        }
        
        if (oldStr == null || newStr == null)
        {
            return false;
        }
        
        return oldStr.equals(newStr);
    }
    
    /**
     * @param oldDBConn
     * @param newDBConn
     */
    public void fixCollectorsForCollectingEvents2()
    {
        IdTableMapper agentMapper = IdMapperMgr.getInstance().addTableMapper("agent", "AgentID", false);
        IdTableMapper colMapper   = IdMapperMgr.getInstance().addTableMapper("collectors", "CollectorsID", false);
        IdTableMapper ceMapper    = IdMapperMgr.getInstance().addTableMapper("collectingevent", "CollectingEventID", false);
        IdTableMapper coMapper    = IdMapperMgr.getInstance().addTableMapper("collectionobjectcatalog", "CollectionObjectCatalogID", false);
        
        // CollectingEvent
        PreparedStatement prepCEStmt = null;
        Statement         stmtCE     = null;
        
        try
        {
            
            String            selectCECSQL = createSelectStmt(Collector.getClassTableId(), "CollectingEventID = %d");
            PreparedStatement prepCECStmt  = createPreparedStmt(newDBConn, Collector.getClassTableId());
            Statement         stmtCEC      = newDBConn.createStatement();
            
            PreparedStatement pStmt  = newDBConn.prepareStatement("UPDATE agent SET LastName=?,FirstName=?,Initials=? WHERE AgentID=?");

            
            //log.debug(selectCECSQL);
            
            int totalCnt = getCollectingEventsWithManyCollectionObjectsCount();
            log.debug(String.format("%d CEs with more than one CO for all Collections", totalCnt));
            
            
            //String    oSQL     = "SELECT CatalogNumber FROM collectionobjectcatalog WHERE CollectionObjectTypeID > 8 && CollectionObjectTypeID < 20 AND SubNumber >= 0 ORDER BY CatalogNumber ASC";
            String    oSQL     = "SELECT CollectingEventID FROM collectingevent ORDER BY CollectingEventID ASC";
            Statement oStmt    = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Statement stmtOld  = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Statement stmtNew  = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            HashMap<Integer, Integer> fixedAgentIdsHash = new HashMap<Integer, Integer>();
            
            int cnt    = 0;
            int okCnt  = 0;
            int fixCnt = 0;
            ResultSet rs = oStmt.executeQuery(oSQL);
            while (rs.next())
            {
                int oldCEID = rs.getInt(1);
                int newCEID = ceMapper.get(oldCEID);
                
                String newSQL = "SELECT ce.CollectingEventID, c.CollectorID, a.AgentID, a.LastName, a.FirstName, a.Initials " +
                                "FROM collectingevent AS ce " +
                                "LEFT Join collector AS c ON ce.CollectingEventID = c.CollectingEventID " +
                                "LEFT Join agent AS a ON c.AgentID = a.AgentID WHERE ce.CollectingEventID = " + newCEID + " ORDER BY OrderNumber";

                String oldSQL = "SELECT ce.CollectingEventID, c.CollectorsID, a.AgentID, IF(a.Name IS NULL, LastName, Name), a.FirstName, a.MiddleInitial " +
                                "FROM collectingevent AS ce " +
                                "INNER Join collectors AS c ON ce.CollectingEventID = c.CollectingEventID " +
                                "INNER Join agent AS a ON c.AgentID = a.AgentID WHERE ce.CollectingEventID = " + oldCEID+ " ORDER BY `Order`";
                
                if (oldCEID == -2132584997)
                {
                    System.out.println("\n"+oldSQL);
                    System.out.println(newSQL);
                }
                
                ResultSet rsOld = stmtOld.executeQuery(oldSQL);
                ResultSet rsNew = stmtNew.executeQuery(newSQL);
                boolean   oldOK = rsOld.next();
                boolean   newOK = rsNew.next();
                if (oldOK && newOK)
                {
                    Integer newCollectorID = rsNew.getObject(2) != null ? rsNew.getInt(2) : null;
                    Integer oldCollectorID = rsOld.getObject(2) != null ? rsOld.getInt(2) : null;
                    
                    Integer newAgentId     = rsNew.getObject(3) != null ? rsNew.getInt(3) : null;
                    Integer oldAgentId     = rsOld.getObject(3) != null ? rsOld.getInt(3) : null;
                    
                    String oldLast  = rsOld.getString(4);
                    String oldFirst = rsOld.getString(5);
                    String oldMid   = rsOld.getString(6);
                    
                    String newLast  = rsNew.getString(4);
                    String newFirst = rsNew.getString(5);
                    String newMid   = rsNew.getString(6);
                    
                    if (!compareStr(oldLast, newLast) || !compareStr(oldFirst, newFirst) || !compareStr(oldMid, newMid))
                    {
                        Integer hits = fixedAgentIdsHash.get(newAgentId);
                        if (hits == null)
                        {
                            fixedAgentIdsHash.put(newAgentId, 1);
                            
                            pStmt.setString(1, oldLast);
                            pStmt.setString(2, oldFirst);
                            pStmt.setString(3, oldMid);
                            pStmt.setInt(4, newAgentId);
                            pStmt.executeUpdate();
                            
                        } else
                        {
                            //fixedAgentIdsHash.put(newAgentId, ++hits);
                            log.error("Agent Id "+newAgentId+" has already been fixed.");
                        }
                        continue;
                    }
                    
                    //System.out.println("OldCE: "+ceID+"  New CEID: "+rsNew.getObject(1)+"  New ColID: "+rsNew.getObject(2));
                    if (newCollectorID == null || newAgentId == null)
                    {
                        Integer mappedAgentID  = agentMapper.get(oldAgentId);
                        Integer mappedColID    = colMapper.get(oldCollectorID);
                        
                        if (mappedColID != null && !mappedColID.equals(newCollectorID))
                        {
                            log.error(String.format("Old Collector ID %d doesn't map to New Id %d - it was mapped to %d", oldCollectorID, newCollectorID, mappedColID));
                            continue;
                        }
                        
                        if (mappedAgentID != null && !mappedAgentID.equals(newAgentId))
                        {
                            log.error(String.format("Old Agent ID %d doesn't map to New Id %d - it was mapped to %d", oldAgentId, newAgentId, mappedColID));
                            continue;
                        }

                        int dispId = BasicSQLUtils.getCountAsInt("SELECT DisciplineID FROM collectingevent WHERE CollectingEventID = " + rsNew.getObject(1));
                        int divId  = BasicSQLUtils.getCountAsInt("SELECT DivisionID FROM discipline WHERE DisciplineID = " + dispId);
                        
                        // Duplicate Collector Record
                        String    sql   = String.format(selectCECSQL, oldCEID);
                        ResultSet rsCEC = stmtCEC.executeQuery(sql);
                        while (rsCEC.next()) 
                        {
                            int divCECInx = getColumnIndex(rsCEC.getMetaData(), "DivisionID");
                            int ceIDInx   = getColumnIndex(rsCEC.getMetaData(), "CollectingEventID");
                            int agtInx    = getColumnIndex(rsCEC.getMetaData(), "AgentID");
    
                            for (int i=1;i<=rsCEC.getMetaData().getColumnCount();i++)
                            {
                                //System.out.println(i+"  "+rsCEC.getMetaData().getColumnName(i)+"  "+rsCEC.getObject(i));
                                if (i == agtInx)
                                {
                                    Integer newAgtID = agentMapper.get(rsCEC.getInt(i));
                                    if (newAgtID != null)
                                    {
                                        prepCECStmt.setObject(i, newAgtID);
                                    } else
                                    {
                                        System.err.println(String.format("Error couldn't find old Agent Id["+rsCEC.getInt(i)+"] in the agent mapper. For %d / %d", oldCEID, rsOld.getInt(2)));
                                        prepCECStmt.setObject(i, null);
                                    }
                                   
                                } else
                                {
                                    prepCECStmt.setObject(i, rsCEC.getObject(i));
                                }
                            }
                            prepCECStmt.setInt(divCECInx, divId);
                            prepCECStmt.setInt(ceIDInx, newCEID);
                            
                            if (prepCECStmt.executeUpdate() != 1)
                            {
                                throw new RuntimeException("Couldn't insert CE Collector row.");
                            }
                            fixCnt++;
                        }
                    } else
                    {
                        okCnt++;
                        //System.out.println("oldOK: "+oldOK+"  newOK: "+ newOK+"  newCEID: "+newCEID+"  oldCEID: "+oldCEID+"  collector: "+newCollectorID);
                        //System.out.println(oldSQL);
                        //System.out.println(newSQL);
                        //int x = 0;
                        //x++;
                    }
                }
                rsOld.close();
                
                cnt++;
                if (cnt % 1000 == 0)
                {
                    System.out.println(cnt+" - "+oldCEID +" "+newCEID);
                }
            }
            rs.close();
            
            log.debug("okCnt: "+okCnt+"  fixCnt: "+fixCnt+"  cnt: "+cnt);
            
            oStmt.close();
            stmtOld.close();
            stmtNew.close();
            
            pStmt.close();
            
            for (Integer id : fixedAgentIdsHash.keySet())
            {
                System.out.println("Id: "+id+" - "+fixedAgentIdsHash.get(id));
            }
            
            
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

    }

    /**
     * @param oldDBConn
     * @param newDBConn
     */
    public void fixCollectorsForCollectingEvents()
    {
        IdTableMapper ceMapper    = IdMapperMgr.getInstance().addTableMapper("collectingevent", "CollectingEventID", false);
        IdTableMapper coMapper    = IdMapperMgr.getInstance().addTableMapper("collectionobjectcatalog", "CollectionObjectCatalogID", false);
        
        // CollectingEvent
        PreparedStatement prepCEStmt = null;
        Statement         stmtCE     = null;
        
        try
        {
            
            Statement         stmt  = oldDBConn.createStatement();
            PreparedStatement pStmt = newDBConn.prepareStatement("UPDATE collectionobject SET CollectingEventID=? WHERE CollectionObjectID=?");
            
            // First hook up the original Collecting Events back up to the Collection Objects and
            // at the same time set the CE Id back to NULL if it didn't have one
            
            String postfix = " FROM collectionobject co " +
                             "INNER JOIN collectionobjectcatalog cc ON co.CollectionObjectID = cc.CollectionObjectCatalogID " +
                             "WHERE co.CollectionObjectTypeID > 8 && co.CollectionObjectTypeID < 20  AND cc.SubNumber >= 0 " +
                             "ORDER BY co.CollectionObjectID";
            String sql = "SELECT co.CollectionObjectID, co.CollectingEventID" + postfix;
            log.debug(sql);
            
            int totCnt = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*)"+postfix);
            
            Integer lastCEId = null;
            
            ceMapper.setShowLogErrors(false);
            
            int cnt   = 0;
            int coCnt = 0;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                int     oldCOId = rs.getInt(1);
                int     oldCEId = rs.getInt(2);
                Integer newCOId = coMapper.get(oldCOId);
                Integer newCEId = ceMapper.get(oldCEId);
                
                if (newCOId != null)
                {
                    if (newCEId != null)
                    {
                        pStmt.setInt(1, newCEId);
                    } else
                    {
                        pStmt.setObject(1, null);
                    }
                    
                    try
                    {
                        pStmt.setInt(2, newCOId);
                        if (pStmt.executeUpdate() != 1)
                        {
                            throw new RuntimeException(String.format("Couldn't update CO with correct CEId; oldCOId: %d   newCOId: %d   oldCEId: %d  newCEId: %d", oldCOId, newCOId, oldCEId, newCEId != null ? newCEId : -1));
                        }
                    } catch (SQLException ex)
                    {
                        log.error(ex.toString());
                        log.error(String.format("oldCOId: %d   newCOId: %d   oldCEId: %d  newCEId: %d", oldCOId, newCOId, oldCEId, newCEId != null ? newCEId : -1));
                    }
                    lastCEId = newCEId;
                    coCnt++;
                        
                } else
                {
                    log.error(String.format("Error oldCOId: %d is mapped to null", oldCOId));
                }
                cnt++;
                if (cnt % 1000 == 0)
                {
                    System.out.println(cnt + " / "+ totCnt);
                }
            }
            rs.close();
            pStmt.close();
            
            log.debug(cnt + " / "+ totCnt);
            log.debug("The last new CE Changed was "+lastCEId);
            log.debug(String.format("%d COs were updated out of %d", coCnt, cnt));
            
            int lastCEAttrID = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT NewID FROM habitat_HabitatID ORDER BY NewID DESC LIMIT 0,1");
            
            sql = "SELECT COUNT(*) FROM collectionobject WHERE TimestampCreated < '2010-01-01'";// AND CollectingEventID > "+lastCEId;
            log.debug(sql);
            int badCnt = BasicSQLUtils.getCountAsInt(sql);
            log.debug(String.format("COs with CE Id %d and there shouldn't be any.", badCnt));
            int c = 0;
            for (Integer id : BasicSQLUtils.queryForInts("SELECT CollectingEventID FROM collectionobject WHERE CollectingEventID > "+lastCEId))
            {
                System.out.print(id);
                System.out.print(',');
                c++;
                if (c > 20) System.out.println();
            }
            System.out.println();
            
            sql = "UPDATE collectionobject SET CollectingEventID=NULL WHERE CollectingEventID > "+lastCEId;
            log.debug(sql);
            int fixCOCnt = BasicSQLUtils.update(sql);
            log.debug(String.format("Set %d CO->CEID to NULL.", fixCOCnt));
            
            int lastCollectorID = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT NewID FROM collectors_CollectorsID ORDER BY NewID DESC LIMIT 0,1");
            sql = "DELETE FROM collector WHERE CollectorID > "+lastCollectorID;
            log.debug(sql);
            int updateColCnt = BasicSQLUtils.update(sql);
            log.debug(String.format("Deleted %d extra Collectors", updateColCnt));

            
            sql = "DELETE FROM collectingevent WHERE CollectingEventID > "+lastCEId;
            log.debug(sql);
            int updateCECnt = BasicSQLUtils.update(sql);
            log.debug(String.format("Deleted %d extra CollectingEvents", updateCECnt));
            
            sql = "DELETE FROM collectingeventattribute WHERE CollectingEventAttributeID > "+lastCEAttrID;
            log.debug(sql);
            int updateCEACnt = BasicSQLUtils.update(sql);
            log.debug(String.format("Deleted %d extra CollectingEventAttributes", updateCEACnt));

            //String selectCECSQL = createSelectStmt(Collector.getClassTableId(), "CollectingEventID = %d");
            //prepCECStmt         = createPreparedStmt(Collector.getClassTableId());
            //stmtCEC             = newDBConn.createStatement();
            
            //log.debug(selectCECSQL);
            
            int totalCnt = getCollectingEventsWithManyCollectionObjectsCount();
            log.debug(String.format("%d CEs with more than one CO for all Collections", totalCnt));
            
            performMaint(false);
            
            /*
            //String    oSQL     = "SELECT CatalogNumber FROM collectionobjectcatalog WHERE CollectionObjectTypeID > 8 && CollectionObjectTypeID < 20 AND SubNumber >= 0 ORDER BY CatalogNumber ASC";
            String    oSQL     = "SELECT CollectingEventID FROM collectingevent ORDER BY CollectingEventID ASC";
            Statement oStmt    = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Statement stmtOld  = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Statement stmtNew  = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            cnt        = 0;
            int okCnt  = 0;
            int fixCnt = 0;
            rs = oStmt.executeQuery(oSQL);
            while (rs.next())
            {
                int oldCEID = rs.getInt(1);
                int newCEID = rs.getInt(2);
                
                String newSQL = "SELECT ce.CollectingEventID, c.CollectorID, a.AgentID " +
                                "FROM collectingevent AS ce " +
                                "LEFT Join collector AS c ON ce.CollectingEventID = c.CollectingEventID " +
                                "LEFT Join agent AS a ON c.AgentID = a.AgentID WHERE ce.CollectingEventID = " + newCEID + " ORDER BY OrderNumber";

                String oldSQL = "SELECT ce.CollectingEventID, c.CollectorsID, a.AgentID " +
                                "FROM collectingevent AS ce " +
                                "INNER Join collectors AS c ON ce.CollectingEventID = c.CollectingEventID " +
                                "INNER Join agent AS a ON c.AgentID = a.AgentID WHERE ce.CollectingEventID = " + oldCEID+ " ORDER BY `Order`";
                //System.out.println(oldSQL);
                //System.out.println(newSQL);
                
                ResultSet rsOld = stmtOld.executeQuery(oldSQL);
                ResultSet rsNew = stmtNew.executeQuery(newSQL);
                boolean   oldOK = rsOld.next();
                boolean   newOK = rsNew.next();
                if (oldOK && newOK)
                {
                    Integer ceID = rsOld.getObject(1) != null ? rsOld.getInt(1) : null;
                    if (ceID != null)
                    {
                        int collector = rsNew.getInt(2);
                        //System.out.println("OldCE: "+ceID+"  New CEID: "+rsNew.getObject(1)+"  New ColID: "+rsNew.getObject(2));
                        if (rsNew.wasNull())
                        {
                            int dispId = BasicSQLUtils.getCountAsInt("SELECT DisciplineID FROM collectingevent WHERE CollectingEventID = " + rsNew.getObject(1));
                            int divId  = BasicSQLUtils.getCountAsInt("SELECT DivisionID FROM discipline WHERE DisciplineID = " + dispId);
                            
                            // Duplicate Collector Record
                            sql = String.format(selectCECSQL, ceID);
                            ResultSet rsCEC = stmtCEC.executeQuery(sql);
                            while (rsCEC.next()) 
                            {
                                int divCECInx = getColumnIndex(rsCEC.getMetaData(), "DivisionID");
                                int ceIDInx   = getColumnIndex(rsCEC.getMetaData(), "CollectingEventID");
                                int agtInx    = getColumnIndex(rsCEC.getMetaData(), "AgentID");
        
                                for (int i=1;i<=rsCEC.getMetaData().getColumnCount();i++)
                                {
                                    //System.out.println(i+"  "+rsCEC.getMetaData().getColumnName(i)+"  "+rsCEC.getObject(i));
                                    if (i == agtInx)
                                    {
                                        Integer newAgtID = agentMapper.get(rsCEC.getInt(i));
                                        if (newAgtID != null)
                                        {
                                            prepCECStmt.setObject(i, newAgtID);
                                        } else
                                        {
                                            System.err.println(String.format("Error couldn't find old Agent Id["+rsCEC.getInt(i)+"] in the agent mapper. For %d / %d", ceID, rsOld.getInt(2)));
                                            prepCECStmt.setObject(i, null);
                                        }
                                       
                                    } else
                                    {
                                        prepCECStmt.setObject(i, rsCEC.getObject(i));
                                    }
                                }
                                prepCECStmt.setInt(divCECInx, divId);
                                prepCECStmt.setInt(ceIDInx, ceID);
                                
                                if (prepCECStmt.executeUpdate() != 1)
                                {
                                    throw new RuntimeException("Couldn't insert CE Collector row.");
                                }
                                fixCnt++;
                            }
                        } else
                        {
                            okCnt++;
                            System.out.println("oldOK: "+oldOK+"  newOK: "+ newOK+"  newCEID: "+newCEID+"  oldCEID: "+oldCEID+"  collector: "+collector);
                            //System.out.println(oldSQL);
                            //System.out.println(newSQL);
                            //int x = 0;
                            //x++;
                        }
                    }
                }
                rsOld.close();
                
                cnt++;
                if (cnt % 1000 == 0)
                {
                    System.out.println(cnt+" - "+oldCEID +" "+newCEID);
                }
            }
            rs.close();
            
            log.debug("okCnt: "+okCnt+"  fixCnt: "+fixCnt+"  cnt: "+cnt);
            
            oStmt.close();
            stmtOld.close();
            stmtNew.close();
            */
            
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
    }
    
    
    /**
     * @param isStart
     */
    @SuppressWarnings("unused")
    private void showStats(final boolean isStart)
    {
        String noCOSQL       = " FROM collectingevent ce LEFT JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID WHERE co.CollectionObjectID IS NULL";
        int totalWithCOs     = BasicSQLUtils.getCountAsInt(newDBConn, "SELECT COUNT(ce.CollectingEventID) FROM collectingevent ce INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID");
        int totalCEWithoutCO = BasicSQLUtils.getCountAsInt(newDBConn, "SELECT COUNT(ce.CollectingEventID)" + noCOSQL);
        
        int totalCE          = BasicSQLUtils.getCountAsInt(newDBConn, "SELECT COUNT(*) FROM collectingevent");
        int totalCO          = BasicSQLUtils.getCountAsInt(newDBConn, "SELECT COUNT(*) FROM collectionobject");
        
        int coWithoutCE      = BasicSQLUtils.getCountAsInt(newDBConn, "SELECT COUNT(*) FROM collectionobject WHERE CollectingEventID is NULL");
        
        String msg = "<HTML><table border=\"1\">" + 
        "<tr><td >Total CO w/o CE</td><td>" + coWithoutCE + "</td></tr>" +
        "<tr><td>Total CE w/  CO</td><td>" + totalWithCOs + "</td></tr>" +
        "<tr><td>Total CE w/o CO</td><td>" + totalCEWithoutCO + "</td></tr>" +
        "<tr><td>Dif          </td><td>" + (totalCEWithoutCO - totalWithCOs) + "</td></tr>" +
        "<tr><td>Total CE     </td><td>" + totalCE + "</td></tr>" +
        "<tr><td>Total CO     </td><td>" + totalCO + "</td></tr>" +
        "<tr><td>Dif          </td><td>" + (totalCE - totalCO) + "</td></tr>" +
        "</table>";
        
        Vector<Object> list = BasicSQLUtils.querySingleCol(newDBConn, "SELECT ce.CollectingEventID " + noCOSQL);
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
    public void performMaint(final boolean doAddCEMapper)
    {
        //showStats(true);
        
        if (doAddCEMapper)
        {
            IdMapperMgr.getInstance().addTableMapper("collectingevent", "CollectingEventID", false);
        }
        
        Vector<Integer> collectionsIds = BasicSQLUtils.queryForInts("SELECT CollectionID FROM collection WHERE IsEmbeddedCollectingEvent <> 0");
        if (collectionsIds == null || collectionsIds.size() == 0)
        {
            return;
        }
        
        if (progressFrame != null) progressFrame.setDesc("Fixing Collecting Events...");
        
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
            PreparedStatement pStmt = newDBConn.prepareStatement(sql);
            
            Calendar now = Calendar.getInstance();
            
            for (Object idObj : BasicSQLUtils.querySingleCol(newDBConn, "SELECT CollectionObjectID FROM collectionobject WHERE CollectingEventID is NULL"))
            {
                Integer coID = (Integer)idObj;
                
                pStmt.setTimestamp(1, new Timestamp(now.getTime().getTime()));
                pStmt.setTimestamp(2, new Timestamp(now.getTime().getTime()));
                pStmt.setInt(3, 0);
                pStmt.setInt(4, createdByAgentId);
                pStmt.setInt(5, createdByAgentId);
                pStmt.setInt(6, disciplineId);
                
                try
                {
                    if (pStmt.executeUpdate() != 1)
                    {
                        throw new RuntimeException(sql+" didn't update correctly.");
                    }
                } catch (SQLException ex)
                {
                    log.debug(ex.toString());
                    log.error(String.format("%d %d %d", coID, createdByAgentId, disciplineId));
                    continue;
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
    public static  PreparedStatement createPreparedStmt(final Connection conn, final int tableID) throws SQLException
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
        
        return conn.prepareStatement(sb.toString());
    }
    
    /**
     * @param tableID
     * @param whereClauseStr
     * @return
     */
    public static String createSelectStmt(final int tableID, final String whereClauseStr)
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

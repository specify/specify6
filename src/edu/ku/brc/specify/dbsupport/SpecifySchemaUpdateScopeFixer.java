/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import java.sql.Connection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 14, 2009
 *
 */
public class SpecifySchemaUpdateScopeFixer
{
    protected static final Logger log = Logger.getLogger(SpecifySchemaUpdateScopeFixer.class);
    protected HashMap<Integer, Integer> colToDspHash = new HashMap<Integer, Integer>();
    protected HashMap<Integer, Integer> colToDivHash = new HashMap<Integer, Integer>();
    protected final String databaseName;
    /**
     * 
     */
    public SpecifySchemaUpdateScopeFixer(final String databaseName)
    {
        super();
        this.databaseName = databaseName;
    }

    
    /**
     * @param conn
     * @return
     */
    public boolean fix(final Connection conn)
    {
        
        String sql = "SELECT c.UserGroupScopeId, d.UserGroupScopeId FROM collection c Inner Join discipline d ON c.DisciplineID = d.UserGroupScopeId";
        for (Object[] row : BasicSQLUtils.query(conn, sql))
        {
            colToDspHash.put((Integer)row[0], (Integer)row[1]);
        }
        
        sql = "SELECT c.UserGroupScopeId, d.DivisionId FROM collection c Inner Join discipline d ON c.DisciplineID = d.UserGroupScopeId";
        for (Object[] row : BasicSQLUtils.query(conn, sql))
        {
            colToDivHash.put((Integer)row[0], (Integer)row[1]);
        }
        
        String[] memberTableNames = new String[] {
                "GiftAgent",  
                "GiftPreparation", 
                "LoanAgent", 
                "LoanPreparation", 
                "LoanReturnPreparation",
                "LocalityCitation",
                "Shipment", 
                };
        
        String[] memberIndexNames = new String[] {
                "GiftAgColMemIDX",  
                "GiftPrepColMemIDX", 
                "LoanAgColMemIDX", 
                "LoanPrepColMemIDX", 
                "LoanRetPrepColMemIDX",
                "LocCitColMemIDX",
                "ShipmentColMemIDX", 
                };
        
        for (int i=0;i<memberTableNames.length;i++)
        {
            if (!fixCollectionMember(conn, memberTableNames[i], memberIndexNames[i]))
            {
                return false;
            }
        }
        
        return fixGroupPerson(conn);
        
        /*
        String[] allMemberTableNames = new String[] {
        "BorrowAgent", 
        "Borrow", 
        "BorrowMaterial",
        "BorrowReturnMaterial",
        "CollectingEventAttachment", 
        "CollectingEventAttribute", 
        "CollectingEventAttr", 
        "CollectionObjectAttachment",
        "CollectionObjectAttribute", 
        "CollectionObjectAttr", 
        "CollectionObjectCitation", 
        "CollectionObject", 
        "Collector", 
        "Container", 
        "DeterminationCitation", 
        "Determination", 
        "DNASequence", 
        "DNASequencingRun", 
        //"GiftAgent",  
        //"GiftPreparation", 
        "GroupPerson", 
        "InfoRequest", 
        //"LoanAgent", 
        //"LoanPreparation", 
        //"LoanReturnPreparation",
        //"LocalityCitation",
        "OtherIdentifier", 
        "PaleoContext", 
        "PreparationAttachment", 
        "PreparationAttribute",
        "PreparationAttr", 
        "Preparation",
        "Project", 
        "RecordSet", 
        //"Shipment", 
        "SpFieldValueDefault", 
        };*/
    }
    
    /**
     * @param conn
     * @param tableName
     * @param fieldName
     * @return true if there is a field named fieldName in the table named tableName.
     */
    protected boolean fieldExists(final Connection conn,  final String tableName, final String fieldName)
    {
        DBMSUserMgr dbUserMgr = DBMSUserMgr.getInstance();
        Connection connCache = dbUserMgr.getConnection();
        dbUserMgr.setConnection(conn);
        
        boolean fieldExists = dbUserMgr.doesFieldExistInTable(tableName, fieldName);
        
        dbUserMgr.setConnection(connCache);
        
        return fieldExists;
    }
    
    /**
     * @param conn
     * @param tableName
     * @param oldIndexName
     * @return
     */
    protected boolean fixCollectionMember(final Connection conn, 
                                          final String tableName,
                                          final String oldIndexName)
    {
        log.info(tableName + " - " + oldIndexName);
        
        //check to see if the fix has already been done
        if (!fieldExists(conn, tableName, "CollectionMemberID")) 
        {
            return true; //successfully did nothing
        }
        
        boolean hasDisciplineID = fieldExists(conn, tableName, "DisciplineID");

        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(tableName);
        if (tblInfo != null && tblInfo.getTableIndexMap() != null)
        {
            String newIndexName = null;
            for (String key : tblInfo.getTableIndexMap().keySet())
            {
                if (tblInfo.getTableIndexMap().get(key).equals("DisciplineID"))
                {
                    newIndexName = key;
                    break;
                }
            }
            
            if (newIndexName != null)
            {
                String tblName      = tableName.toLowerCase();
                
                int cnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM " + tblName);
                log.debug(String.format("Fixing %d %s records", cnt, tblName));

                HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
                String sql = "SELECT " + tblInfo.getIdFieldName() + ", CollectionMemberID FROM " + tblName;
                log.debug(sql);
                for (Object[] row : BasicSQLUtils.query(conn, sql))
                {
                    hash.put((Integer)row[0], (Integer)row[1]);
                }
                
                String dropOldInx   = String.format("DROP INDEX %s on %s", oldIndexName, tblName);
                String dropOldCol   = String.format("ALTER TABLE %s DROP COLUMN CollectionMemberID", tblName);
                String createNewCol = String.format("ALTER TABLE %s Add COLUMN DisciplineID int(11) ", tblName);
                //String createNewInx = String.format("CREATE INDEX %s ON %s (DisciplineID)", newIndexName, tblName);
                
                @SuppressWarnings("unused")
                int rv = BasicSQLUtils.update(conn, dropOldInx);
/*                if (rv != 0)
                {
                    log.error("Error on ["+dropOldInx+"] for table["+tblName+"]");
                    return false;
                }*/
                
                rv = BasicSQLUtils.update(conn, dropOldCol);
/*                if (rv != 0)
                {
                    log.error("Error on ["+dropOldCol+"] for table["+tblName+"]");
                    return false; 
                }*/
                
                if (!hasDisciplineID)
                {
                    rv = BasicSQLUtils.update(conn, createNewCol);
                }

/*                if (rv != 0)
                {
                    log.error("Error on ["+createNewCol+"] for table["+tblName+"]");
                    return false;
                }
                */
/*                rv = BasicSQLUtils.update(conn, createNewInx);
                {
                    log.error("Error on ["+createNewInx+"] for table["+tblName+"]");
                    return false;
                }*/
                
                sql = "SELECT " + tblInfo.getIdFieldName() + " FROM " + tblName;
                for (Object idObj : BasicSQLUtils.querySingleCol(conn, sql))
                {
                    Integer id          = (Integer)idObj;
                    Integer oldColMemId = hash.get(id);
                    if (oldColMemId != null)
                    {
                        sql = "UPDATE " + tblName + " SET DisciplineID=" + colToDspHash.get(oldColMemId) + " WHERE " + tblInfo.getIdFieldName() + " = " + id;
                        BasicSQLUtils.update(sql);
                    }
                }
                return true;
                
            } else
            {
                log.error("Couldn't find new Index name for table["+tableName+"]["+oldIndexName+"]");
            }
        } else
        {
            log.error("Couldn't find new DBTableInfo or the TableIndex HashMap was null table["+tableName+"]["+oldIndexName+"]");
        }
        return false;
    }

    

    public boolean fixGroupPerson(final Connection conn)
    {
        if (!fieldExists(conn, "groupperson", "CollectionMemberID"))
        {
            return true; //already fixed
        }
        
        String dropOldInx   = "DROP INDEX GPColMemIDX on groupperson";
        String dropOldCol   = "ALTER TABLE groupperson DROP COLUMN CollectionMemberID";
        String createNewCol = "ALTER TABLE groupperson Add COLUMN DivisionID int(11)";
        String createNewInx = "CREATE INDEX GPDivMemIDX ON groupperson (DivisionID)";
        String createNewFK  = "ALTER TABLE groupperson ADD CONSTRAINT `FK5DEB769497C961D8` FOREIGN KEY (`DivisionID`) REFERENCES `division` (`UserGroupScopeId`)";
        
        int cnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM groupperson");
        log.debug(String.format("Fixing %d groupperson records", cnt));
        
        HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
        String sql = "SELECT GroupPersonID, CollectionMemberID FROM groupperson";
        for (Object[] row : BasicSQLUtils.query(conn, sql))
        {
            hash.put((Integer)row[0], (Integer)row[1]);
        }
        
        @SuppressWarnings("unused")
        int rv = BasicSQLUtils.update(conn, dropOldInx);
/*            {
                log.error("Error on ["+dropOldInx+"] for table[groupperson]");
                return false;
            }*/
        
        rv = BasicSQLUtils.update(conn, dropOldCol);
/*            {
                log.error("Error on ["+dropOldCol+"] for table[groupperson]");
                return false; 
            }*/
        
        rv = BasicSQLUtils.update(conn, createNewCol);
/*            {
                log.error("Error on ["+createNewCol+"] for table[groupperson]");
                return false;
            }*/
        
        rv = BasicSQLUtils.update(conn, createNewInx);
/*            {
                log.error("Error on ["+createNewInx+"] for table[groupperson]");
                return false;
            }*/
        
        rv = BasicSQLUtils.update(conn, createNewFK);
/*            {
                log.error("Error on ["+createNewFK+"] for table[groupperson]");
                return false;
            }*/
        
        sql = "SELECT GroupPersonID FROM groupperson";
        for (Object idObj : BasicSQLUtils.querySingleCol(conn, sql))
        {
            Integer id          = (Integer)idObj;
            Integer oldColMemId = hash.get(id);
            if (oldColMemId != null)
            {
                sql = "UPDATE groupperson SET DivisionID=" + colToDivHash.get(oldColMemId) + " WHERE GroupPersonID = " + id;
                BasicSQLUtils.update(sql);
            }
        }
        return true;
            
    }

}

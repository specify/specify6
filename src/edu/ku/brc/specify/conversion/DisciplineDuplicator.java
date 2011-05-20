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
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIRegistry;

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

    protected Connection    oldDBConn; 
    protected Connection    newDBConn; 
    protected TableWriter   tblWriter;
    protected ProgressFrame prgFrame;
    protected GenericDBConversion conversion;
    
    
    /**
     * @param newDBConn
     * @param tblWriter
     * @param prgFrame
     */
    public DisciplineDuplicator(final Connection oldDBConn, 
                                final Connection newDBConn, 
                                final TableWriter tblWriter, 
                                final ProgressFrame prgFrame,
                                final GenericDBConversion conversion)
    {
        super();
        this.oldDBConn  = oldDBConn;
        this.newDBConn  = newDBConn;
        this.tblWriter  = tblWriter;
        this.prgFrame   = prgFrame;
        this.conversion = conversion;
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void doShowFieldsForDiscipline()
    {
    
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            for (Discipline discipline : (List<Discipline>)session.createQuery("FROM Discipline", false).list())
            {
                BuildSampleDatabase.doShowHideTablesAndFields(null, discipline);
                BuildSampleDatabase.doShowHideTablesAndFields(discipline.getType(), discipline);
            }
                
        } catch(Exception ex)
        {
            log.error("Error while  show fields in Discipline.");
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    
    /**
     * @param conn
     * @param catalog
     * @param tableName
     * @param skipFirst
     * @return
     */
    public static List<String> getColumnNames(final Connection conn, final String catalog, final String tableName, final boolean skipFirst)
    {
        Vector<String> list = new Vector<String>();
        Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            String    sql = String.format("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'", catalog, tableName);
            ResultSet rs  = stmt.executeQuery(sql);
            while (rs.next())
            {
                list.add(rs.getString(1));
            }
            rs.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                stmt.close();
                
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        
        if (skipFirst)
        {
            list.remove(0);
        }
        
        return list;
    }
    
    /**
     * @param tblName
     * @return
     * @throws SQLException
     */
    public static String getFieldNameList(final Connection conn, final String tblName) throws SQLException
    {
        StringBuilder fieldNames = new StringBuilder();
        int cnt = 0;
        for (String colName : getColumnNames(conn, conn.getCatalog(), tblName, true))
        {
            if (cnt > 0) fieldNames.append(',');
            fieldNames.append(colName);
            cnt++;
        }
        return fieldNames.toString();
    }


    /**
     * 
     */
    public void duplicateCollectingEvents()
    {
        Statement stmt   = null;
        Statement stmt2  = null;
        Statement stmt3  = null;
        Statement uStmt  = null;
        
        int changeCOCnt  = 0;
        int changeCECnt  = 0;
        int insertCECnt    = 0;
        int insertCEACnt = 0;
        
        try
        {
            PreparedStatement pStmt  = newDBConn.prepareStatement("UPDATE collectingevent SET DisciplineID=? WHERE CollectingEventID=?");
            PreparedStatement pStmt2 = newDBConn.prepareStatement("UPDATE collectionobject SET CollectingEventID=? WHERE CollectionObjectID=?");
            PreparedStatement pCECEA = newDBConn.prepareStatement("UPDATE collectingevent SET CollectingEventAttributeID=? WHERE CollectingEventID=?");
            
            stmt  = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt2 = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt3 = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            uStmt = newDBConn.createStatement();
            
            String sql;
            
            String ceFldNames  = getFieldNameList(newDBConn, "collectingevent");
            String ceaFldNames = getFieldNameList(newDBConn, "collectingeventattribute");
            
            sql = " FROM collectionobject " +
                    "Inner Join collection ON collectionobject.CollectionID = collection.UserGroupScopeId " +
                    "Inner Join collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID WHERE collection.DisciplineID <> collectingevent.DisciplineID";
            
            int total = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) " + sql);
            
            if (prgFrame != null) prgFrame.setProcess(0, total);
            
            
            String selectPrefix = "SELECT DISTINCT collectingevent.CollectingEventID  ";
            log.debug(selectPrefix + sql);
            
            int cnt = 0;
            ResultSet rs = stmt.executeQuery(selectPrefix + sql);
            while (rs.next())
            {
                int     ceID  = rs.getInt(1);
                boolean debug = false;//ceID == 49;
                /*
                     "CollectingEventID","DisciplineID","CollectionObjectID","CollectionMemberID", "Collection.DisciplineID"
                         2058,               7,               14930,                 4                       3
                         2058,               7,               7894,                  6                       7
                 */
                String sqlStr = String.format("SELECT collectingevent.DisciplineID, collection.DisciplineID, collectionobject.CollectionID, collectingevent.CollectingEventAttributeID" + 
                                               sql + " AND collectingevent.CollectingEventID = %d", ceID);
                //if (debug) log.debug("SELECT COUNT(*)" + sql);
                //int count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) " + sql + " GROUP BY CollectionMemberID");
                
                //String sqlStr = String.format("SELECT T1.DisciplineID, collection.DisciplineID, CollectionMemberID, CollectingEventAttributeID %s GROUP BY CollectionMemberID", sql);
                if (debug) log.debug(sqlStr);
                
                int ceCnt = 0;
                ResultSet rs2 = stmt2.executeQuery(sqlStr);
                while (rs2.next())
                {
                    int ceDspID     = rs2.getInt(1);
                    int colDspID    = rs2.getInt(2);                    
                    int colMemID    = rs2.getInt(3);
                    
                    Integer ceAttrID = rs2.getObject(4) != null ? rs2.getInt(4) : null;
                    
                    if (debug) log.debug(String.format("ceDspID %d,  colDspID %d,  colMemID %d,  ", ceDspID, colDspID, colMemID));
                   
                    Integer newCEId = null;
                    if (ceCnt == 0)
                    {
                        newCEId = ceID;
                        changeCOCnt++;
                        
                    } else
                    {
                        String insertSQL = String.format("INSERT INTO collectingevent (%s) (SELECT %s FROM collectingevent WHERE CollectingEventID = %d)", ceFldNames.toString(), ceFldNames.toString(), ceID);
                        if (debug) log.debug(insertSQL);
                        
                        uStmt.executeUpdate(insertSQL);
                        newCEId = BasicSQLUtils.getInsertedId(uStmt);
                        insertCECnt++;
                        tblWriter.log(String.format("Duplicated collectingevent Old %d to New %d", ceID, newCEId));
                        
                        if (ceAttrID != null)
                        {
                            int newCEAttrsId = dupRecord(uStmt, "collectingeventattribute", "CollectingEventAttributeID", ceaFldNames, ceAttrID);
                            pCECEA.setInt(1, newCEAttrsId);
                            pCECEA.setInt(1, newCEId);
                            pCECEA.executeUpdate();
                            insertCEACnt++;
                        }
                        
                        sqlStr = String.format("SELECT CollectionObjectID FROM collectionobject WHERE  CollectingEventID = %d AND CollectionMemberID = %d", ceID, colMemID);
                        if (debug) log.debug(sqlStr);
                        
                        ResultSet rs3 = stmt3.executeQuery(sqlStr);
                        while (rs3.next())
                        {
                            // Update the ColObj's CE Id
                            int colObjId = rs3.getInt(1);
                            pStmt2.setInt(1, newCEId);
                            pStmt2.setInt(2, colObjId);
                            pStmt2.execute();
                            tblWriter.log(String.format("Updated ColObj Id: %d to new CE Id %d", colObjId, newCEId));
                            changeCOCnt++;
                        }
                        rs3.close();
                    }
                    
                    // Update the existing or new CE with the correct Discipline. 
                    pStmt.setInt(1, colDspID);
                    pStmt.setInt(2, newCEId);
                    pStmt.execute();
                    changeCECnt++;

                    if (debug) log.debug(String.format("UPDATE collectingevent SET DisciplineID=%d WHERE CollectingEventID=%d",colDspID, newCEId));
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
            pStmt2.close();
            pCECEA.close();
            
            tblWriter.log(String.format("There were %d CollectingEvent records changed.", changeCECnt));
            tblWriter.log(String.format("There were %d CollectionObject records changed.", changeCOCnt));
            tblWriter.log(String.format("There were %d records CollectingEvent copied and inserted.", insertCECnt));
            tblWriter.log(String.format("There were %d records CollectingEvent copied and inserted.", insertCEACnt));
            
            tblWriter.log("<BR>");

        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                stmt2.close();
                stmt3.close();
                uStmt.close();
                
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @param uStmt
     * @param tblName
     * @param idName
     * @param fieldNames
     * @param oldId
     * @return
     * @throws SQLException
     */
    private int dupRecord(final Statement uStmt, 
                          final String    tblName,
                          final String    idName,
                          final String    fieldNames, 
                          final int       oldId) throws SQLException
    {
        String insertSQL = String.format("INSERT INTO %s (%s) (SELECT %s FROM %s WHERE CollectingEventID = %d)", 
                                          tblName, fieldNames, fieldNames, tblName, oldId);
        //if (debug) log.debug(insertSQL);
        
        uStmt.executeUpdate(insertSQL);
        int newId = BasicSQLUtils.getInsertedId(uStmt);
        tblWriter.log(String.format("Duplicated %s Old %d to New %d", tblName, oldId, newId));
        return newId;
    }
    

    /**
     * 
     */
    public void duplicateLocalities()
    {
        Statement stmt    = null;
        Statement stmt2   = null;
        Statement stmt3   = null;
        Statement uStmt   = null;
    
        int changeCECnt   = 0;
        int changeLCCnt   = 0;
        int insertLocCnt  = 0;
        int insertGCDCnt  = 0;
        int insertLDCnt   = 0;
        
        try
        {
            PreparedStatement pStmt   = newDBConn.prepareStatement("UPDATE locality SET DisciplineID=? WHERE LocalityID=?");
            PreparedStatement pStmtCE = newDBConn.prepareStatement("UPDATE collectingevent SET LocalityID=? WHERE CollectingEventID=?");
            PreparedStatement pGCDLoc = newDBConn.prepareStatement("UPDATE geocoorddetail SET LocalityID=? WHERE GeoCoordDetailID=?");
            //PreparedStatement pLDLoc  = newDBConn.prepareStatement("UPDATE localitydetail SET LocalityID=? WHERE LocalityDetailID=?");
            
            stmt  = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt2 = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt3 = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            uStmt = newDBConn.createStatement();
            
            String sql;
            int cnt = 0;
            
            String locFldNames = getFieldNameList(newDBConn, "locality");
            String gcdFldNames = getFieldNameList(newDBConn, "geocoorddetail");
            String ldFldNames  = getFieldNameList(newDBConn, "localitydetail");
            
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
                
                debug = localityId == 1116;
                
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
                    changeLCCnt++;
                    
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
                            changeLCCnt++;
                            
                        } else
                        {
                            int ceDspId = rs2.getInt(1);
                            sql = String.format("SELECT l.LocalityID, l.DisciplineID, ce.CollectingEventID " + 
                                                "FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " + 
                                                "WHERE l.DisciplineID != ce.DisciplineID AND l.LocalityID = %d AND ce.DisciplineID = %d ORDER BY ce.DisciplineID", localityId, ceDspId);
                            log.debug(sql);
                            
                            int       ceLocCnt = 0;
                            Integer   newLocID = null;
                            ResultSet rs3      = stmt3.executeQuery(sql);
                            while (rs3.next())
                            {
                                int ceID = rs3.getInt(3);
                                if (ceLocCnt == 0)
                                {
                                    sql = String.format("INSERT INTO locality (%s) (SELECT %s FROM locality WHERE LocalityID = %d)", locFldNames, locFldNames, localityId);
                                    uStmt.executeUpdate(sql);
                                    newLocID = BasicSQLUtils.getInsertedId(uStmt);
                                    
                                    log.debug("New Locality["+ newLocID + "] from Old Loc["+localityId+"] for CE["+ceID+"]");
                                    
                                    pStmt.setInt(1, ceDspId);
                                    pStmt.setInt(2, newLocID);
                                    pStmt.execute();
                                    changeLCCnt++;
                                    
                                    tblWriter.log(String.format("Duplicated Locality Old %d to New %d", localityId, newLocID));
                                    
                                    Integer gcdId = BasicSQLUtils.getCount("SELECT GeoCoordDetailID FROM geocoorddetail WHERE LocalityID = " + localityId);
                                    if (gcdId != null)
                                    {
                                        int newGCDId = dupRecord(uStmt, "geocoorddetail", "GeoCoordDetailID", gcdFldNames, gcdId);
                                        pGCDLoc.setInt(1, newGCDId);
                                        pGCDLoc.setInt(1, newLocID);
                                        pGCDLoc.executeUpdate();
                                        insertGCDCnt++;
                                    }
                                    
                                    Integer ldId = BasicSQLUtils.getCount("SELECT LocalityDetailID FROM localitydetail WHERE LocalityID = " + localityId);
                                    if (ldId != null)
                                    {
                                        int newLDId = dupRecord(uStmt, "localitydetail", "LocalityDetailID", ldFldNames, ldId);
                                        pGCDLoc.setInt(1, newLDId);
                                        pGCDLoc.setInt(1, newLocID);
                                        pGCDLoc.executeUpdate();
                                        insertLDCnt++;
                                    }
                                }
                                
                                pStmtCE.setInt(1, newLocID);
                                pStmtCE.setInt(2, ceID);
                                pStmtCE.execute();
                                changeCECnt++;
                                
                                tblWriter.log(String.format("Updated collectingevent's %d  Locality ID: %d to new CE Id %d", ceID, localityId, newLocID));
                                
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
            log.debug("Done - Processed: "+cnt);
            rs.close(); 
            
            pStmt.close();
            pStmtCE.close();
            
            tblWriter.log(String.format("There were %d Locality records changed.", changeLCCnt));
            tblWriter.log(String.format("There were %d Locality records inserted.", insertLocCnt));
            tblWriter.log(String.format("There were %d Collecting Events records changed.", changeCECnt));
            tblWriter.log("<BR>");


        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                stmt2.close();
                stmt3.close();
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
    public void duplicateGeography()
    {
        String sql = " SELECT collectionobjectcatalog.CollectionObjectTypeID, Count(collectionobjectcatalog.CollectionObjectTypeID), collectionobjecttype.CollectionObjectTypeName FROM collectionobjectcatalog " +
                        "Inner Join collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID " +
                        "Inner Join collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "Inner Join locality ON collectingevent.LocalityID = locality.LocalityID " +
                        "Inner Join geography ON locality.GeographyID = geography.GeographyID " +
                        "Inner Join collectionobjecttype ON collectionobjectcatalog.CollectionObjectTypeID = collectionobjecttype.CollectionObjectTypeID  " +
                        "WHERE collectionobject.DerivedFromID IS NULL " +
                        "GROUP BY collectionobjectcatalog.CollectionObjectTypeID";
        tblWriter.startTable();
        tblWriter.logHdr("Discipline ID", "Number of ColObjs", "Discipline");
        for (Object[] row : BasicSQLUtils.query(oldDBConn, sql))
        {
            tblWriter.logObjRow(row);
        }
        tblWriter.endTable();

        if (BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM discipline") > 1)
        {
            try
            {
                Integer mainGeoDiscipline = null;
                
                sql = "SELECT DISTINCT d.UserGroupScopeId FROM discipline d Inner Join geographytreedef gtd ON d.GeographyTreeDefID = gtd.GeographyTreeDefID Inner Join geography g ON gtd.GeographyTreeDefID = g.GeographyTreeDefID";
                Vector<Integer> dispsInGeo = BasicSQLUtils.queryForInts(sql);
                if (dispsInGeo.size() == 0)
                {
                    UIRegistry.showError("There are no disciplines in the Geography tree and there should only be one.");
                    return;
                    
                } else
                {
                    mainGeoDiscipline = dispsInGeo.get(0);
                }
                
                IdTableMapper geoIdMapper     = (IdTableMapper)IdMapperMgr.getInstance().get("geography", "GeographyID");
                IdTableMapper origGeoIdMapper = new IdTableMapper("geography", "orig", false, false);
                IdMapperMgr.getInstance().addMapper(origGeoIdMapper);

                origGeoIdMapper.clearRecords();
                geoIdMapper.copy(origGeoIdMapper);
                geoIdMapper.clearRecords();
                
                int fixCnt = 0;
                for (Object[] row : BasicSQLUtils.query("SELECT DisciplineID, Name FROM discipline ORDER BY DisciplineID"))
                {
                    Integer dspId   = (Integer)row[0];
                    String  dspName = (String)row[1];
                    
                    if (mainGeoDiscipline.equals(dspId)) continue;
                    
                    GeographyTreeDef geoTreeDef = conversion.createStandardGeographyDefinitionAndItems(false);
                    BasicSQLUtils.update(String.format("UPDATE discipline SET GeographyTreeDefID=%d WHERE DisciplineID = %d",geoTreeDef.getId(), dspId));
                    conversion.convertGeography(geoTreeDef, dspName, false);
                    
                    PreparedStatement pStmt = newDBConn.prepareStatement("UPDATE locality SET GeographyID=? WHERE LocalityID=?");
                    Statement         stmt  = newDBConn.createStatement();
                    ResultSet         rs    = stmt.executeQuery("SELECT LocalityID, GeographyID FROM locality WHERE DisciplineID = " + dspId);
                    while (rs.next())
                    {
                        int locId = rs.getInt(1);
                        int geoId = rs.getInt(2);
                        
                        Integer oldId    = origGeoIdMapper.reverseGet(geoId);
                        Integer newGeoId = geoIdMapper.get(oldId);
                        
                        pStmt.setInt(1, newGeoId);
                        pStmt.setInt(2, locId);
                        pStmt.execute();
                        
                        fixCnt++;
                    }
                    
                    rs.close();
                }
                
                tblWriter.log(String.format("There were %d Locality records changed.", fixCnt));
                tblWriter.log("<BR>");
                
                sql = " SELECT discipline.UserGroupScopeId, COUNT(discipline.UserGroupScopeId), discipline.Name FROM discipline " +
                        "Inner Join collection ON discipline.UserGroupScopeId = collection.DisciplineID " +
                        "Inner Join collectionobject ON collection.UserGroupScopeId = collectionobject.CollectionMemberID " +
                        "Inner Join collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "Inner Join locality ON collectingevent.LocalityID = locality.LocalityID " +
                        "Inner Join geography ON locality.GeographyID = geography.GeographyID " +
                        "GROUP BY discipline.UserGroupScopeId ";
                tblWriter.startTable();
                tblWriter.logHdr("Discipline ID", "Number of ColObjs", "Discipline");
                for (Object[] row : BasicSQLUtils.query(sql))
                {
                    tblWriter.logObjRow(row);
                }
                tblWriter.endTable();
                tblWriter.log("<BR>");
                
                /*sql = " SELECT discipline.disciplineId, Count(discipline.disciplineId), discipline.Name FROM discipline " +
                		"Inner Join geographytreedef ON discipline.GeographyTreeDefID = geographytreedef.GeographyTreeDefID " +
                		"Inner Join geography ON geographytreedef.GeographyTreeDefID = geography.GeographyTreeDefID " +
                		"Inner Join locality ON geography.GeographyID = locality.GeographyID " +
                		"Inner Join collectingevent ON locality.LocalityID = collectingevent.LocalityID " +
                		"Inner Join collectionobject ON collectingevent.CollectingEventID = collectionobject.CollectingEventID " +
                		"GROUP BY discipline.disciplineId";
                tblWriter.startTable();
                tblWriter.logHdr("Discipline ID", "Number of ColObjs", "Discipline");
                for (Object[] row : BasicSQLUtils.query(sql))
                {
                    tblWriter.logObjRow(row);
                }
                tblWriter.endTable();
                tblWriter.log("<BR>");*/

            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }
}

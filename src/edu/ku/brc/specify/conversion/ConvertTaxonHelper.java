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
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.copyTable;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCount;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setFieldsToIgnoreWhenMappingNames;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setIdentityInsertONCommandForSQLServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

public class ConvertTaxonHelper
{
    protected static final Logger            log         = Logger.getLogger(ConvertTaxonHelper.class);
    
    protected static SimpleDateFormat        dateTimeFormatter      = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static SimpleDateFormat        dateFormatter          = new SimpleDateFormat("yyyy-MM-dd");
    protected static Timestamp               now                    = new Timestamp(System .currentTimeMillis());
    protected static String                  nowStr                 = dateTimeFormatter.format(now);
    
    protected Connection                     oldDBConn;
    protected Connection                     newDBConn;
    protected String                         oldDBName;
    protected TableWriter                    tblWriter;
    protected IdMapperIndexIncrementerIFace  indexIncremeter;
    protected GenericDBConversion            conversion;
    
    protected String                         taxonomyTypeIdInClause = null;
    protected Vector<CollectionInfo>         collectionInfoList;
    protected HashMap<Integer, Vector<CollectionInfo>> collDispHash;
    
    protected HashMap<Integer, TaxonTreeDef> newTaxonInfoHash = new HashMap<Integer, TaxonTreeDef>();
    
    protected HashSet<Integer>               taxonTypesInUse  = new HashSet<Integer>();
    protected HashMap<Integer, TaxonTreeDef> taxonTreeDefHash = new HashMap<Integer, TaxonTreeDef>(); // Key is old TaxonTreeTypeID
    protected HashMap<Integer, Taxon>        taxonTreeHash    = new HashMap<Integer, Taxon>();        // Key is old TaxonTreeTypeID
    
    /**
     * @param oldDBConn
     * @param newDBConn
     * @param oldDBName
     * @param tblWriter
     */
    public ConvertTaxonHelper(final Connection oldDBConn, 
                              final Connection newDBConn,
                              final String     oldDBName,
                              final TableWriter tblWriter,
                              final GenericDBConversion conversion,
                              final IdMapperIndexIncrementerIFace indexIncremeter)
    {
        super();
        this.oldDBConn = oldDBConn;
        this.newDBConn = newDBConn;
        this.oldDBName = oldDBName;
        this.tblWriter = tblWriter;
        this.indexIncremeter = indexIncremeter;
        this.conversion = conversion;
        
        
        CollectionInfo.getCollectionInfoList(oldDBConn);
        
        collectionInfoList = CollectionInfo.getFilteredCollectionInfoList();
        
        // Create a Hashed List of CollectionInfo for each unique TaxonomyTypeId
        // where the TaxonomyTypeId is a Discipline
        collDispHash = new HashMap<Integer, Vector<CollectionInfo>>();
        for (CollectionInfo info : collectionInfoList)
        {
            if (info.isTaxonomicUnitTypeInUse())
            {
                Vector<CollectionInfo> colInfoList = collDispHash.get(info.getTaxonomyTypeId());
                if (colInfoList == null)
                {
                    colInfoList = new Vector<CollectionInfo>();
                    collDispHash.put(info.getTaxonomyTypeId(), colInfoList);
                }
                colInfoList.add(info);
            }
        }
    }
    
    /**
     * 
     */
    public void doForeignKeyMappings()
    {
        
        // When you run in to this table1.field, go to that table2 and look up the id
        String[] mappings = {

                "Determination",
                "TaxonNameID",
                "TaxonName",
                "TaxonNameID",
                
                "Preparation",
                "ParasiteTaxonNameID",
                "TaxonName",
                "TaxonNameID",
                
                "Habitat",
                "HostTaxonID",
                "TaxonName",
                "TaxonNameID",
                
                "TaxonCitation",
                "ReferenceWorkID",
                "ReferenceWork",
                "ReferenceWorkID",
                
                "TaxonCitation",
                "TaxonNameID",
                "TaxonName",
                "TaxonNameID",
                
                // taxonname ID mappings
                "TaxonName", "ParentTaxonNameID", "TaxonName", "TaxonNameID", "TaxonName",
                "TaxonomicUnitTypeID", "TaxonomicUnitType", "TaxonomicUnitTypeID", "TaxonName",
                "TaxonomyTypeID", "TaxonomyType", "TaxonomyTypeID", "TaxonName", "AcceptedID",
                "TaxonName", "TaxonNameID",

                // taxonomytype ID mappings
                // NONE

                // taxonomicunittype ID mappings
                "TaxonomicUnitType", "TaxonomyTypeID", "TaxonomyType", "TaxonomyTypeID" };

        for (int i = 0; i < mappings.length; i += 4)
        {
            IdMapperMgr.getInstance().mapForeignKey(mappings[i], mappings[i + 1], mappings[i + 2], mappings[i + 3]);
        }
    }
    
    /**
     * 
     */
    public void createTaxonIdMappings()
    {
        IdMapperMgr idMapperMgr = IdMapperMgr.getInstance();
        
        // These are the names as they occur in the old datamodel
        String[] tableNames = {
                "Habitat", 
                "TaxonCitation", 
                "TaxonomicUnitType",  // Added Only 
        };
        
        int i = 0;
        IdTableMapper idMapper = null;
        for (String tableName : tableNames)
        {
            idMapper = idMapperMgr.addTableMapper(tableName, tableName + "ID");
            log.debug("mapIds() for table" + tableName);
            
            if (i < tableNames.length - 1)
            {
                idMapper.mapAllIds();
            }
            i++;
        }
        
        //---------------------------------
        // TaxonomyType
        //---------------------------------
        
        HashSet<Integer> hashSet = new HashSet<Integer>();
        StringBuilder    inSB    = new StringBuilder();
        for (CollectionInfo ci : CollectionInfo.getCollectionInfoList(oldDBConn))
        {
            log.debug("For Collection["+ci.getCatSeriesName()+"]  TaxonomyTypeId: "+ci.getTaxonomyTypeId() +"  "+ (hashSet.contains(ci.getTaxonomyTypeId()) ? "Done" : "not Done."));
            if (!hashSet.contains(ci.getTaxonomyTypeId()))
            {
                log.debug("Mapping TaxonomyTypeId ["+ci.getTaxonomyTypeId()+"]  For Collection["+ci.getCatSeriesName()+"]");
                if (inSB.length() > 0) inSB.append(',');
                inSB.append(ci.getTaxonomyTypeId());
                hashSet.add(ci.getTaxonomyTypeId());
            }
        }
        
        taxonomyTypeIdInClause = " in (" + inSB.toString() + ")";
        
        idMapperMgr.addTableMapper("TaxonomyType", "TaxonomyTypeID", true);
        
        //---------------------------------
        // TaxonName
        //---------------------------------
        
        int    count = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM taxonname WHERE taxonname.TaxonomyTypeId "+taxonomyTypeIdInClause);
        String sql   = "SELECT TaxonNameID FROM taxonname WHERE RankID IS NOT NULL AND taxonname.TaxonomyTypeId " + taxonomyTypeIdInClause;
        log.debug(count+" - " + sql);
        
        //sb.setLength(0);
        //sb.append("SELECT TaxonNameID FROM taxonname");
        
        // This mapping is used by Discipline
        idMapper = idMapperMgr.addTableMapper("TaxonName", "TaxonNameID", sql, false);
        idMapper.mapAllIdsWithSQL();
    }
    
    /**
     * 
     */
    protected void getTaxonTreesTypesInUse()
    {
        for (CollectionInfo colInfo : collectionInfoList)
        {
            //log.debug("TaxonType in use ["+taxonTypeId+"]");
            taxonTypesInUse.add(colInfo.getTaxonNameId());
        }
    }

    /**
     * 
     */
    public void convertAllTaxonTreeDefs()
    {
        for (CollectionInfo colInfo : collectionInfoList)
        {
            convertTaxonTreeDefinition(colInfo);
        }
    }

    /**
     * Converts the taxonomy tree definition from the old taxonomicunittype table to the new table
     * pair: TaxonTreeDef & TaxonTreeDefItems.
     * 
     * @param taxonomyTypeId the tree def id in taxonomicunittype
     * @return the TaxonTreeDef object
     * @throws SQLException
     */
    public void convertTaxonTreeDefinition(final CollectionInfo colInfo)
    {
        if (!colInfo.isInUse())
        {
            return;
        }
        
        TaxonTreeDef taxonTreeDef = newTaxonInfoHash.get(colInfo.getTaxonNameId());
        if (taxonTreeDef != null)
        {
            colInfo.setTaxonTreeDef(taxonTreeDef);
            return;
        }
        
        Integer oldTaxonRootId = colInfo.getTaxonNameId();
        Integer taxonomyTypeId = colInfo.getTaxonomyTypeId();
        
        try
        {
            Statement st = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    
            taxonTreeDef = new TaxonTreeDef();
            taxonTreeDef.initialize();
    
            String sql = "SELECT TaxonomyTypeName FROM taxonomytype WHERE TaxonomyTypeID = " + taxonomyTypeId;
            log.debug(sql);
            ResultSet rs = st.executeQuery(sql);
            rs.next();
            String taxonomyTypeName = rs.getString(1);
            rs.close();
    
            taxonTreeDef.setName(taxonomyTypeName + " taxonomy tree");
            taxonTreeDef.setRemarks("Tree converted from " + oldDBName);
            taxonTreeDef.setFullNameDirection(TreeDefIface.FORWARD);
    
            sql = "SELECT RankID, RankName, RequiredParentRankID, TaxonomicUnitTypeID FROM taxonomicunittype WHERE TaxonomyTypeID = " + taxonomyTypeId + " ORDER BY RankID";
            log.debug(sql);
            rs = st.executeQuery(sql);
    
            Hashtable<Integer, Integer> rankId2TxnUntTypId = new Hashtable<Integer, Integer>();
            int    rank;
            String name;
            int    requiredRank;
    
            Vector<TaxonTreeDefItem> items = new Vector<TaxonTreeDefItem>();
            Vector<Integer> enforcedRanks = new Vector<Integer>();
    
            while (rs.next())
            {
                rank         = rs.getInt(1);
                name         = rs.getString(2);
                requiredRank = rs.getInt(3);
                
                int taxUnitTypeId = rs.getInt(4);
                
                if (StringUtils.isEmpty(name) || (rank > 0 && requiredRank == 0))
                {
                    continue;
                }
                
                rankId2TxnUntTypId.put(rank, taxUnitTypeId);
                
                log.debug(rank + "  " + name+"  TaxonomicUnitTypeID: "+taxUnitTypeId);
                
                TaxonTreeDefItem ttdi = new TaxonTreeDefItem();
                ttdi.initialize();
                ttdi.setName(name);
                ttdi.setFullNameSeparator(" ");
                ttdi.setRankId(rank);
                ttdi.setTreeDef(taxonTreeDef);
                taxonTreeDef.getTreeDefItems().add(ttdi);
    
                // setup the parent/child relationship
                if (items.isEmpty())
                {
                    ttdi.setParent(null);
                } else
                {
                    ttdi.setParent(items.lastElement());
                }
                items.add(ttdi);
                enforcedRanks.add(requiredRank);
            }
            rs.close();
            
            for (TaxonTreeDefItem i : items)
            {
                i.setIsEnforced(enforcedRanks.contains(i.getRankId()));
            }
            
            try
            {
                Session     session = HibernateUtil.getNewSession();
                Transaction trans = session.beginTransaction();
                session.save(taxonTreeDef);
                trans.commit();
                session.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
            
            IdMapperMgr   idMapperMgr        = IdMapperMgr.getInstance();
            IdMapperIFace tutMapper          = idMapperMgr.get("TaxonomicUnitType", "TaxonomicUnitTypeID");
            IdMapperIFace taxonomyTypeMapper = idMapperMgr.get("TaxonomyType",      "TaxonomyTypeID");
            
            taxonomyTypeMapper.put(taxonomyTypeId, taxonTreeDef.getId());
            
            for (TaxonTreeDefItem ttdi : taxonTreeDef.getTreeDefItems())
            {
                int ttdiId = rankId2TxnUntTypId.get(ttdi.getRankId());
                tutMapper.put(ttdiId, ttdi.getId());
                log.debug("Mapping "+ttdiId+" -> "+ttdi.getId());
            }
            
            newTaxonInfoHash.put(oldTaxonRootId, taxonTreeDef);

            CollectionInfo ci = getCIByTaxonTypeId(taxonomyTypeId);
            ci.setTaxonTreeDef(taxonTreeDef);
            
            taxonTreeDefHash.put(taxonomyTypeId, taxonTreeDef);
            log.debug("Hashing taxonomyTypeId: "+taxonomyTypeId+" ->  taxonTreeDefId:"+taxonTreeDef.getId());
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param taxonomyTypeId
     * @return
     */
    protected CollectionInfo getCIByTaxonTypeId(final int taxonomyTypeId)
    {
        for (CollectionInfo ci : CollectionInfo.getCollectionInfoList(oldDBConn))
        {
            if (ci.getTaxonomyTypeId() == taxonomyTypeId)
            {
                return ci;
            }
        } 
        log.error("Couldn't find ["+taxonomyTypeId+"] in CollectionInfo list");
        return null;
    }

    /**
     * 
     */
    private void convertTaxonRecords()
    {
        
        IdMapperIFace txMapper        = IdMapperMgr.getInstance().get("taxonname", "TaxonNameID");
        IdMapperIFace txTypMapper     = IdMapperMgr.getInstance().get("TaxonomyType", "TaxonomyTypeID");
        IdMapperIFace txUnitTypMapper = IdMapperMgr.getInstance().get("TaxonomicUnitType", "TaxonomicUnitTypeID");
        IdMapperIFace[] mappers       = {txMapper, txMapper, txTypMapper, txMapper, txUnitTypMapper};
        
        String[] oldCols = {"TaxonNameID", "ParentTaxonNameID", "TaxonomyTypeID", "AcceptedID", "TaxonomicUnitTypeID", "TaxonomicSerialNumber", "TaxonName", "UnitInd1", "UnitName1", 
                            "UnitInd2", "UnitName2", "UnitInd3", "UnitName3", "UnitInd4", "UnitName4", "FullTaxonName", "CommonName", "Author", "Source", "GroupPermittedToView", 
                            "EnvironmentalProtectionStatus", "Remarks", "NodeNumber", "HighestChildNodeNumber", "LastEditedBy", "Accepted", 
                            "RankID", "GroupNumber", "TimestampCreated", "TimestampModified"};
        
        String[] cols = {"TaxonID", "Author", "CitesStatus", "COLStatus", "CommonName", "CultivarName", "EnvironmentalProtectionStatus",
                         "EsaStatus", "FullName", "GroupNumber", "GUID", "HighestChildNodeNumber", "IsAccepted", "IsHybrid", "IsisNumber", "LabelFormat", "Name", "NcbiTaxonNumber", "NodeNumber", "Number1", "Number2",
                         "RankID", "Remarks", "Source", "TaxonomicSerialNumber", "Text1", "Text2", "UnitInd1", "UnitInd2", "UnitInd3", "UnitInd4", "UnitName1", "UnitName2", "UnitName3", "UnitName4", "UsfwsCode", "Visibility",
                         "ParentID", "AcceptedID", "ModifiedByAgentID", "TaxonTreeDefItemID", "VisibilitySetByID", "CreatedByAgentID", "HybridParent1ID", "TaxonTreeDefID", "HybridParent2ID",
                         "TimestampCreated", "TimestampModified", "Version"};


        Hashtable<String, String> newToOldColMap = new Hashtable<String, String>();
        newToOldColMap.put("TaxonID",            "TaxonNameID");
        newToOldColMap.put("ParentID",           "ParentTaxonNameID");
        newToOldColMap.put("TaxonTreeDefID",     "TaxonomyTypeID");
        newToOldColMap.put("TaxonTreeDefItemID", "TaxonomicUnitTypeID");
        newToOldColMap.put("Name",               "TaxonName");
        newToOldColMap.put("FullName",           "FullTaxonName");
        newToOldColMap.put("IsAccepted",         "Accepted");
        
        Hashtable<String, String> oldToNewColMap = new Hashtable<String, String>();
        oldToNewColMap.put("TaxonNameID",         "TaxonID");
        oldToNewColMap.put("ParentTaxonNameID",   "ParentID");
        oldToNewColMap.put("TaxonomyTypeID",      "TaxonTreeDefID");
        oldToNewColMap.put("TaxonomicUnitTypeID", "TaxonTreeDefItemID");
        oldToNewColMap.put("TaxonName",           "Name");
        oldToNewColMap.put("FullTaxonName",       "FullName");
        oldToNewColMap.put("Accepted",            "IsAccepted");
        
        

        // Ignore new fields
        // These were added for supporting the new security model and hybrids
        /*String[] ignoredFields = { "GUID", "Visibility", "VisibilitySetBy", "IsHybrid",
                                    "HybridParent1ID", "HybridParent2ID", "EsaStatus", "CitesStatus", "UsfwsCode",
                                    "IsisNumber", "Text1", "Text2", "NcbiTaxonNumber", "Number1", "Number2",
                                    "CreatedByAgentID", "ModifiedByAgentID", "Version", "CultivarName", "LabelFormat", 
                                    "COLStatus", "VisibilitySetByID"};
        */
        
        StringBuilder sb = new StringBuilder();
        StringBuilder vl = new StringBuilder();
        HashMap<String, Integer> fieldToColHash = new HashMap<String, Integer>();
        HashMap<Integer, String> colToFieldHash = new HashMap<Integer, String>();
        for (int i=0;i<cols.length;i++)
        {
            fieldToColHash.put(cols[i], i+1);
            colToFieldHash.put(i+1, cols[i]);
            
            if (sb.length() > 0) sb.append(", ");
            sb.append(cols[i]);
            
            if (vl.length() > 0) vl.append(',');
            vl.append('?');
        }
        
        HashMap<String, Integer> oldFieldToColHash = new HashMap<String, Integer>();
        StringBuilder oldSB = new StringBuilder();
        for (int i=0;i<oldCols.length;i++)
        {
            oldFieldToColHash.put(oldCols[i], i+1);
            if (oldSB.length() > 0) oldSB.append(", ");
            oldSB.append(oldCols[i]);
        }
        
        String sqlStr = String.format("SELECT %s FROM taxon", sb.toString());
        
        String sql = String.format("SELECT %s FROM taxonname WHERE RankID IS NOT NULL AND TaxonomyTypeID %s", oldSB.toString(), taxonomyTypeIdInClause);
        log.debug(sql);
        
        //String cntSQL = "SELECT COUNT(*) FROM taxonname WHERE TaxonomyTypeID " + taxonomyTypeIdInClause;

        String pStr = String.format("INSERT INTO taxon (%s) VALUES (%s)", sb.toString(), vl.toString());
        log.debug(pStr);
        
        PreparedStatement pStmt = null;
        Statement         stmt  = null;
        try
        {
            int cnt = 0;
            stmt  = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs1 = stmt.executeQuery(sqlStr);
            ResultSetMetaData rsmd1 = rs1.getMetaData();
            int[] colTypes = new int[rsmd1.getColumnCount()];
            for (int i=0;i<colTypes.length;i++)
            {
                colTypes[i] = rsmd1.getColumnType(i+1); 
            }
            rs1.close();
            stmt.close();
            
            int missingParentTaxonCount = 0;
            
            int lastEditedByInx = oldFieldToColHash.get("LastEditedBy");
            
            stmt  = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pStmt = newDBConn.prepareStatement(pStr);
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next())
            {
                for (int colInx=1;colInx<=cols.length;colInx++)
                {
                    pStmt.setNull(colInx, colTypes[colInx-1]);
                }
                
                boolean skip = false;
                for (int colInx=1;colInx<=oldCols.length && !skip;colInx++)
                {
                    String  oldName = oldCols[colInx-1];
                    Integer newInx  = fieldToColHash.get(oldName);
                    if (newInx == null)
                    {
                        String newName = oldToNewColMap.get(oldName);
                        if (newName != null)
                        {
                            newInx = fieldToColHash.get(newName);
                            if (newInx == -1)
                            {
                                String msg = "Couldn't find column index for New Name["+newName+"]";
                                log.error(msg);
                                tblWriter.logError(msg);
                            }
                        } else if (colInx == lastEditedByInx)
                        {
                            Integer agtId = conversion.getModifiedByAgentId(rs.getString(colInx));
                            if (agtId != null)
                            {
                                pStmt.setInt(colInx, agtId);
                                continue;
                            }
                            
                        } else if (colInx != 20)
                        {
                            String msg = "Couldn't find Old Name["+oldName+"]";
                            log.error(msg);
                            tblWriter.logError(msg);
                        } else
                        {
                            continue; // GroupToView
                        }
                    }
                    
                    if (colInx < 6)
                    {
                        Integer oldID = rs.getInt(colInx);
                        if (!rs.wasNull())
                        {
                            boolean skipError = false; 
                            Integer newID = mappers[colInx-1].get(oldID);
                            if (newID == null)
                            {
                                if (colInx == 3 || colInx == 5)
                                {
                                    skip = true;
                                } else
                                {
                                    boolean wasInOldTaxonTable = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM taxonname WHERE TaxonNameID = " + oldID) != 1;
                                    boolean isDetPointToTaxon  = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM determination WHERE TaxonNameID = " + oldID) != 1;
                                    String msg = String.format("***** Couldn't get NewID [%s] from mapper for column [%s]  In Old taxonname table: %s  WasParentID: %s  Det Using: %s", 
                                            oldID, colToFieldHash.get(colInx), (wasInOldTaxonTable ? "YES" : "no"), (colInx == 2 ? "YES" : "no"), (isDetPointToTaxon ? "YES" : "no"));
                                    log.error(msg);
                                    tblWriter.logError(msg);
                                    skipError = true;
                                    missingParentTaxonCount++;
                                }
                            }
                            
                            if (!skip)
                            {
                                if (newID != null)
                                {
                                    pStmt.setInt(newInx, newID);
                                    
                                } else if (!skipError)
                                {
                                    String msg = "Unable to map old TaxonNameID["+oldID+"]";
                                    log.error(msg);
                                    tblWriter.logError(msg);
                                }
                            }
                        } else
                        {
                            //log.error("***** Old ID Col ["+colInx+"] was null");
                            //skip = true;
                        }
                        continue;
                    }
                    
                    
                    
                    switch (colTypes[newInx-1])
                    {
                        case java.sql.Types.BIT:
                        {
                            boolean val = rs.getBoolean(colInx);
                            if (!rs.wasNull()) pStmt.setBoolean(newInx, val);
                            break;
                        }
                        case java.sql.Types.INTEGER:
                        {
                            int val = rs.getInt(colInx);
                            if (!rs.wasNull()) pStmt.setInt(newInx, val);
                            break;
                        }
                        case java.sql.Types.SMALLINT:
                        {
                            short val = rs.getShort(colInx);
                            if (!rs.wasNull()) pStmt.setShort(newInx, val);
                            break;
                        }
                        case java.sql.Types.TIMESTAMP:
                        {
                            Timestamp val = rs.getTimestamp(colInx);
                            if (!rs.wasNull()) pStmt.setTimestamp(newInx, val);
                            break;
                        }
                        case java.sql.Types.LONGVARCHAR:
                        case java.sql.Types.VARCHAR:
                        {
                            String val = rs.getString(colInx);
                            if (!rs.wasNull()) 
                            {
                                pStmt.setString(newInx, val);
                                
                            } else if (colInx == 7)
                            {
                                pStmt.setString(newInx, "Empty");
                            }
                            break;
                        }
                        default:
                            log.error("Didn't support SQL Type: "+rsmd.getColumnType(colInx));
                            break;
                    }
                    
                }
                
                if (!skip)
                {
                    pStmt.setInt(fieldToColHash.get("Version"), 0);
                    pStmt.execute();
                }
                
                cnt++;
                if (cnt % 1000 == 0)
                {
                    log.debug(cnt);
                }
            }
            rs.close();
            
            // select COUNT(*) FROM taxonname t1 LEFT JOIN taxonname t2 ON t1.ParentTaxonNameID = t2.TaxonNameID WHERE t2.TaxonNameID IS NULL AND t1.RankID > 0
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                stmt.close();
                pStmt.close();
            } catch (Exception ex) {}
        }
    }

    
    /**
     * @param disciplineId
     * @param taxonRootId
     * @return
     */
    public Pair<TaxonTreeDef, Discipline> doTaxonForCollection(final int disciplineId, final int taxonRootId)
    {
        Pair<TaxonTreeDef, Discipline> dataForColInfo = null;
        
        IdMapperIFace txMapper       = IdMapperMgr.getInstance().get("taxonname", "TaxonNameID");
        int           newTaxonRootID = txMapper.get(taxonRootId);
        int taxonTreeDefId = BasicSQLUtils.getCountAsInt(newDBConn, "SELECT TaxonTreeDefID FROM taxon WHERE TaxonID = " + newTaxonRootID);
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            TaxonTreeDef ttd        = session.get(TaxonTreeDef.class, taxonTreeDefId);
            Discipline   discipline = (Discipline)session.getData("FROM Discipline WHERE id = " + disciplineId);
            session.beginTransaction();
            discipline.setTaxonTreeDef(ttd);
            session.saveOrUpdate(discipline);
            session.commit();
            
            dataForColInfo = new Pair<TaxonTreeDef, Discipline>(ttd, discipline);
                
        } catch(Exception ex)
        {
            session.rollback();
            
            log.error("Error while setting TaxonTreeDef into the Discipline.");
            ex.printStackTrace();
            throw new RuntimeException(ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        tblWriter.append("<H3>Taxon with null RankIDs</H3>");
        tblWriter.startTable();
        String missingRankSQL = "SELECT * FROM taxonname WHERE RankID IS NULL";
        Vector<Object[]> rows = query(oldDBConn, missingRankSQL);
        for (Object[] row : rows)
        {
            tblWriter.append("<TR>");
            for (Object obj : row)
            {
                tblWriter.append("<TD>");
                tblWriter.append(obj != null ? obj.toString() : "null");
                tblWriter.append("</TD>");
            }
            tblWriter.append("</TR>");
        }
        tblWriter.endTable();
        tblWriter.append("<BR>");
        
        if (txMapper instanceof IdHashMapper)
        {
            for (Integer oldId : ((IdHashMapper)txMapper).getOldIdNullList())
            {
                tblWriter.println(ConvertVerifier.dumpSQL(oldDBConn, "SELECT * FROM taxonname WHERE ParentTaxonNameId = "+oldId));
            }
        }

        setFieldsToIgnoreWhenMappingNames(null);
        setIdentityInsertOFFCommandForSQLServer(newDBConn, "taxon", BasicSQLUtils.myDestinationServerType);
        
        IdHashMapper.setTblWriter(null);
        
        return dataForColInfo;
    }

    
    /**
     * 
     */
    private void convertTaxonTreeDefSeparators()
    {
        // fix the fullNameDirection field in each of the converted tree defs
        Session session = HibernateUtil.getCurrentSession();
        Query q = session.createQuery("FROM TaxonTreeDef");
        List<?> allTTDs = q.list();
        HibernateUtil.beginTransaction();
        for(Object o: allTTDs)
        {
            TaxonTreeDef ttd = (TaxonTreeDef)o;
            ttd.setFullNameDirection(TreeDefIface.FORWARD);
            session.saveOrUpdate(ttd);
        }
        try
        {
            HibernateUtil.commitTransaction();
        }
        catch(Exception ex)
        {
            log.error("Error while setting the fullname direction of taxonomy tree definitions.");
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        
        // fix the fullNameSeparator field in each of the converted tree def items
        session = HibernateUtil.getCurrentSession();
        q = session.createQuery("FROM TaxonTreeDefItem");
        List<?> allTTDIs = q.list();
        HibernateUtil.beginTransaction();
        for(Object o : allTTDIs)
        {
            TaxonTreeDefItem ttdi = (TaxonTreeDefItem)o;
            ttdi.setFullNameSeparator(" ");
            session.saveOrUpdate(ttdi);
        }
        try
        {
            HibernateUtil.commitTransaction();
        }
        catch (Exception ex)
        {
            log.error("Error while setting the fullname separator of taxonomy tree definition items.");
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * 
     */
//    private void assignTreeDefToDiscipline()
//    {
//        DataProviderSessionIFace session = null;
//        try
//        {
//            session = DataProviderFactory.getInstance().createSession();
//            
//            for (Integer txTypeId : collDispHash.keySet())
//            {
//                Vector<CollectionInfo> collInfoList = collDispHash.get(txTypeId);
//                Integer                disciplineId = collInfoList.get(0).getDisciplineId();
//                if (disciplineId != null)
//                {
//                    TaxonTreeDef txnTreeDef = taxonTreeDefHash.get(txTypeId);
//                    String sql = "UPDATE discipline SET TaxonTreeDefID=" + txnTreeDef.getTaxonTreeDefId() + " WHERE DisciplineID = " + disciplineId;
//                    if (BasicSQLUtils.update(newDBConn, sql) != 1)
//                    {
//                        log.error("Error updating discipline["+disciplineId+"] with TaxonTreeDefID "+ txnTreeDef.getTaxonTreeDefId());
//                    } else
//                    {
//                        /*Discipline discipline = collInfoList.get(0).getDiscipline();
//                        if (discipline == null)
//                        {
//                            log.error("Error updating discipline["+collInfoList.get(0).getDisciplineId()+"] with TaxonTreeDefID "+ collInfoList.get(0).getTaxonTreeDef().getTaxonTreeDefId());
//                            continue;
//                        }
//                        
//                        discipline = session.load(Discipline.class, discipline.getId());
//                        for (CollectionInfo ci : collInfoList)
//                        {
//                            ci.setDiscipline(discipline);
//                        }*/
//                    }
//                } else
//                {
//                    log.error("Missing Discipline #");
//                }
//            }
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        } finally
//        {
//            if (session != null)
//            {
//                session.close();
//            }
//        }
//    }
    
    /**
     * 
     */
    public void doConvert()
    {
        getTaxonTreesTypesInUse();
        
        convertAllTaxonTreeDefs();
        convertTaxonTreeDefSeparators();
        
        convertTaxonRecords(); // converts all the taxon records
        
        HashMap<Integer, Integer> dispTxnRootHash = new HashMap<Integer, Integer>();
        for (CollectionInfo colInfo : collectionInfoList)
        {
            Integer txnRootId = dispTxnRootHash.get(colInfo.getDisciplineId());
            if (txnRootId == null)
            {
                dispTxnRootHash.put(colInfo.getDisciplineId(), colInfo.getTaxonNameId());
                
            } else if (!txnRootId.equals(colInfo.getTaxonNameId()))
            {
                UIRegistry.showError("Two (or more) Disciplines have different Taxon Root Id records.  Dsp["+colInfo.getDisciplineId()+"]  Prev RootId["+txnRootId+"] New RootId["+colInfo.getTaxonNameId()+"]");
            }
        }
        
        for (Integer dispId : dispTxnRootHash.keySet())
        {
            Pair<TaxonTreeDef, Discipline> dispTxn = doTaxonForCollection(dispId, dispTxnRootHash.get(dispId));
            if (dispTxn != null)
            {
                for (CollectionInfo colInfo : collectionInfoList)
                {
                    if (colInfo.getDisciplineId().equals(dispTxn.second.getId()))
                    {
                        colInfo.setTaxonTreeDef(dispTxn.first);
                    }
                }
            }
        }
        
        //assignTreeDefToDiscipline();
    }
    
}

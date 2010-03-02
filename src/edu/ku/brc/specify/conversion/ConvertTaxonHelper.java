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
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setShowErrors;

import java.sql.Connection;
import java.sql.ResultSet;
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
    protected TableWriter   tblWriter;
    protected IdMapperIndexIncrementerIFace  indexIncremeter;
    
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
                              final IdMapperIndexIncrementerIFace indexIncremeter)
    {
        super();
        this.oldDBConn = oldDBConn;
        this.newDBConn = newDBConn;
        this.oldDBName = oldDBName;
        this.tblWriter = tblWriter;
        this.indexIncremeter = indexIncremeter;
        
        
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
            if (!hashSet.contains(ci.getTaxonomyTypeId()))
            {
                if (inSB.length() > 0) inSB.append(',');
                inSB.append(ci.getTaxonomyTypeId());
                hashSet.add(ci.getTaxonomyTypeId());
            }
        }
        
        idMapperMgr.addTableMapper("TaxonomyType", "TaxonomyTypeID", true);
        
        //---------------------------------
        // TaxonName
        //---------------------------------
        StringBuilder sb;
        sb = new StringBuilder("SELECT TaxonNameID FROM taxonname WHERE TaxonName IS NOT NULL AND taxonname.TaxonomyTypeId in (");
        sb.append(inSB);
        sb.append(')');
        log.debug(sb.toString());
        
        // This mapping is used by Discipline
        idMapper = idMapperMgr.addTableMapper("TaxonName", "TaxonNameID", sb.toString(), false);
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
    public void convertTaxonRecords(final CollectionInfo colInfo)
    {
        //deleteAllRecordsFromTable(newDBConn, "taxon", BasicSQLUtils.myDestinationServerType);
        
        setIdentityInsertONCommandForSQLServer(newDBConn, "taxon", BasicSQLUtils.myDestinationServerType);
        
        String sql    = "SELECT *        FROM taxonname WHERE TaxonName IS NOT NULL AND TaxonomyTypeID = ";
        String cntSQL = "SELECT COUNT(*) FROM taxonname WHERE TaxonName IS NOT NULL AND TaxonomyTypeID = ";

        Hashtable<String, String> newToOldColMap = new Hashtable<String, String>();
        newToOldColMap.put("TaxonID",            "TaxonNameID");
        newToOldColMap.put("ParentID",           "ParentTaxonNameID");
        newToOldColMap.put("TaxonTreeDefID",     "TaxonomyTypeID");
        newToOldColMap.put("TaxonTreeDefItemID", "TaxonomicUnitTypeID");
        newToOldColMap.put("Name",               "TaxonName");
        newToOldColMap.put("FullName",           "FullTaxonName");
        newToOldColMap.put("IsAccepted",         "Accepted");

        // Ignore new fields
        // These were added for supporting the new security model and hybrids
        String[] ignoredFields = { "GUID", "Visibility", "VisibilitySetBy", "IsHybrid",
                                    "HybridParent1ID", "HybridParent2ID", "EsaStatus", "CitesStatus", "UsfwsCode",
                                    "IsisNumber", "Text1", "Text2", "NcbiTaxonNumber", "Number1", "Number2",
                                    "CreatedByAgentID", "ModifiedByAgentID", "Version", "CultivarName", "LabelFormat", 
                                    "COLStatus", "VisibilitySetByID"};
            
        setFieldsToIgnoreWhenMappingNames(ignoredFields);
        
        // AcceptedID is typically NULL unless they are using synonymies
        Integer cnt               = getCount(oldDBConn, "SELECT count(AcceptedID) FROM taxonname where AcceptedID IS NOT null");
        boolean showMappingErrors = cnt != null && cnt > 0;

        int errorsToShow = (BasicSQLUtils.SHOW_NAME_MAPPING_ERROR | BasicSQLUtils.SHOW_VAL_MAPPING_ERROR);
        if (showMappingErrors)
        {
            errorsToShow = errorsToShow | BasicSQLUtils.SHOW_PM_LOOKUP | BasicSQLUtils.SHOW_NULL_PM;// | BasicSQLUtils.SHOW_COPY_TABLE;
        }
        setShowErrors(errorsToShow);
        
        IdHashMapper.setTblWriter(tblWriter);

        log.info("Copying taxon records from 'taxonname' table");
        log.info("SQL: "+ (sql + colInfo.getTaxonomyTypeId()));
        if (!copyTable(oldDBConn, newDBConn, 
                       sql + colInfo.getTaxonomyTypeId(), cntSQL + colInfo.getTaxonomyTypeId(), 
                       "taxonname", "taxon", 
                       newToOldColMap, null, null,
                       BasicSQLUtils.mySourceServerType, BasicSQLUtils.myDestinationServerType))
        {
            String msg = "Table 'taxonname' didn't copy correctly";
            log.error(msg);
            tblWriter.logError(msg);
        }
        
        //sql = "SELECT TaxonNameID FROM taxonname WHERE TaxonName IS NOT NULL AND RankID = 0 AND TaxonomicUnitTypeID = " + colInfo.getTaxonomyTypeId();
        
        IdMapperIFace txMapper       = IdMapperMgr.getInstance().get("taxonname", "TaxonNameID");
        int           newTaxonRootID = txMapper.get(colInfo.getTaxonNameId());
        int taxonTreeDefId = BasicSQLUtils.getCountAsInt(newDBConn, "SELECT TaxonTreeDefID FROM taxon WHERE TaxonID = " + newTaxonRootID);
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            TaxonTreeDef ttd        = session.get(TaxonTreeDef.class, taxonTreeDefId);
            Discipline   discipline = (Discipline)session.getData("FROM Discipline WHERE id = " + colInfo.getDisciplineId());
            session.beginTransaction();
            discipline.setTaxonTreeDef(ttd);
            session.saveOrUpdate(discipline);
            session.commit();
            
            colInfo.setDiscipline(discipline);
            colInfo.setTaxonTreeDef(ttd);
                
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
    private void assignTreeDefToDiscipline()
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            for (Integer txTypeId : collDispHash.keySet())
            {
                Vector<CollectionInfo> collInfoList = collDispHash.get(txTypeId);
                Integer                disciplineId = collInfoList.get(0).getDisciplineId();
                if (disciplineId != null)
                {
                    TaxonTreeDef txnTreeDef = taxonTreeDefHash.get(txTypeId);
                    String sql = "UPDATE discipline SET TaxonTreeDefID=" + txnTreeDef.getTaxonTreeDefId() + " WHERE DisciplineID = " + disciplineId;
                    if (BasicSQLUtils.update(newDBConn, sql) != 1)
                    {
                        log.error("Error updating discipline["+disciplineId+"] with TaxonTreeDefID "+ txnTreeDef.getTaxonTreeDefId());
                    } else
                    {
                        /*Discipline discipline = collInfoList.get(0).getDiscipline();
                        if (discipline == null)
                        {
                            log.error("Error updating discipline["+collInfoList.get(0).getDisciplineId()+"] with TaxonTreeDefID "+ collInfoList.get(0).getTaxonTreeDef().getTaxonTreeDefId());
                            continue;
                        }
                        
                        discipline = session.load(Discipline.class, discipline.getId());
                        for (CollectionInfo ci : collInfoList)
                        {
                            ci.setDiscipline(discipline);
                        }*/
                    }
                } else
                {
                    log.error("Missing Discipline #");
                }
            }
        } catch (Exception ex)
        {
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
     * 
     */
    public void doConvert()
    {
        getTaxonTreesTypesInUse();
        
        convertAllTaxonTreeDefs();
        convertTaxonTreeDefSeparators();
        
        for (CollectionInfo colInfo : collectionInfoList)
        {
            convertTaxonRecords(colInfo);
        }
        
        assignTreeDefToDiscipline();
    }
    
}

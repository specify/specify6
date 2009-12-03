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
import static edu.ku.brc.specify.conversion.BasicSQLUtils.deleteAllRecordsFromTable;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.dbsupport.HibernateUtil;
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
    protected ConversionLogger.TableWriter   tblWriter;
    protected IdMapperIndexIncrementerIFace  indexIncremeter;
    
    protected Vector<CollectionInfo>         collectionInfoList;
    HashMap<Integer, Vector<CollectionInfo>> collDispHash;
    
    protected HashMap<Integer, Integer>      taxonTypesInUse = new HashMap<Integer, Integer>();
    protected HashMap<Integer, TaxonTreeDef> taxonTreeDefHash = new HashMap<Integer, TaxonTreeDef>(); // Key is old TaxonTreeTypeID
    
    /**
     * @param oldDBConn
     * @param newDBConn
     * @param oldDBName
     * @param tblWriter
     */
    public ConvertTaxonHelper(final Connection oldDBConn, 
                              final Connection newDBConn,
                              final String     oldDBName,
                              final ConversionLogger.TableWriter tblWriter,
                              final IdMapperIndexIncrementerIFace indexIncremeter)
    {
        super();
        this.oldDBConn = oldDBConn;
        this.newDBConn = newDBConn;
        this.oldDBName = oldDBName;
        this.tblWriter = tblWriter;
        this.indexIncremeter = indexIncremeter;
        
        
        collectionInfoList = CollectionInfo.getCollectionInfoList(oldDBConn);
        
        // Create a Hashed List of CollectionInfo for each unique TaxonomyTypeId
        // where the TaxonomyTypeId is a Discipline
        collDispHash = new HashMap<Integer, Vector<CollectionInfo>>();
        for (CollectionInfo info : collectionInfoList)
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
    
    /**
     * 
     */
    public void doForeignKeyMappings()
    {
        
        // When you run in to this table1.field, go to that table2 and look up the id
        String[] mappings = {

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
                "TaxonCitation", 
                "TaxonomicUnitType", 
                "TaxonomyType" 
        };
        
        IdTableMapper idMapper = null;
        for (String tableName : tableNames)
        {
            idMapper = idMapperMgr.addTableMapper(tableName, tableName + "ID");
            log.debug("mapIds() for table" + tableName);
            idMapper.mapAllIds();
        }
        
        //---------------------------------
        // TaxonomyType
        //---------------------------------
        StringBuilder sb = new StringBuilder("SELECT TaxonomyTypeID FROM taxonomytype WHERE TaxonomyTypeId in (");
        for (Integer txTypeId : collDispHash.keySet())
        {
            sb.append(txTypeId);
            sb.append(',');
        }
        sb.setLength(sb.length()-1); // Chomp
        sb.append(')');
        log.debug(sb.toString());
        
        // This mapping is used by Discipline
        IdTableMapper taxonomyTypeMapper = idMapperMgr.addTableMapper("TaxonomyType", "TaxonomyTypeID", false);
        for (Object txTypIdObj : BasicSQLUtils.querySingleCol(oldDBConn, sb.toString()))
        {
            Integer txTypId = (Integer)txTypIdObj;
            taxonomyTypeMapper.put(txTypId, indexIncremeter.getNextIndex());
        }
        
        //---------------------------------
        // TaxonName
        //---------------------------------
        sb = new StringBuilder("SELECT TaxonNameID FROM taxonname WHERE TaxonName IS NOT NULL AND taxonname.TaxonomyTypeId in (");
        for (CollectionInfo ci : CollectionInfo.getCollectionInfoList(oldDBConn))
        {
            sb.append(ci.getTaxonomyTypeId());
            sb.append(',');
        }
        sb.setLength(sb.length()-1); // Chomp
        sb.append(')');
        log.debug(sb.toString());
        
        // This mapping is used by Discipline
        idMapper = idMapperMgr.addTableMapper("TaxonName", "TaxonNameID", sb.toString(), false);
        idMapper.setIndexIncremeter(indexIncremeter);
        idMapper.mapAllIdsWithSQL();
        idMapper.setIndexIncremeter(null);
    }
    
    /**
     * 
     */
    protected void getTaxonTreesTypesInUse()
    {
        /*String sql = "SELECT DISTINCT taxonomytype.TaxonomyTypeID FROM  determination "+
                     "Inner Join taxonname ON determination.TaxonNameID = taxonname.TaxonNameID "+
                     "Inner Join taxonomytype ON taxonname.TaxonomyTypeID = taxonomytype.TaxonomyTypeID ";
                     */
        String sql = "SELECT DISTINCT taxonomytype.TaxonomyTypeID FROM taxonname " +
                     "Inner Join taxonomytype ON taxonname.TaxonomyTypeID = taxonomytype.TaxonomyTypeID " +
                     "WHERE taxonname.RankID >  0";
        log.debug(sql);
        for (Object obj : BasicSQLUtils.querySingleCol(oldDBConn, sql))
        {
            taxonTypesInUse.put((Integer)obj, (Integer)obj);
        }
    }

    /**
     * @throws SQLException
     */
    public void convertAllTaxonTreeDefs()
    {
        for (Integer id : taxonTypesInUse.keySet())
        {
            convertTaxonTreeDefinition(id);
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
    public void convertTaxonTreeDefinition(final int taxonomyTypeId)
    {
        try
        {
            Statement st = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    
            TaxonTreeDef ttd = new TaxonTreeDef();
            ttd.initialize();
    
            ResultSet rs = st.executeQuery("SELECT TaxonomyTypeName FROM taxonomytype WHERE TaxonomyTypeID = " + taxonomyTypeId);
            rs.next();
            String taxonomyTypeName = rs.getString(1);
    
            ttd.setName(taxonomyTypeName + " taxonomy tree");
            ttd.setRemarks("Tree converted from " + oldDBName);
            ttd.setFullNameDirection(TreeDefIface.FORWARD);
    
            rs = st.executeQuery("SELECT DISTINCT RankID, RankName, RequiredParentRankID FROM taxonomicunittype WHERE TaxonomyTypeID = " + taxonomyTypeId + " ORDER BY RankID");
    
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
                
                log.debug(rank + "  " + name);
                
                TaxonTreeDefItem i = new TaxonTreeDefItem();
                i.initialize();
                i.setName(name);
                i.setFullNameSeparator(" ");
                i.setRankId(rank);
                i.setTreeDef(ttd);
                ttd.getTreeDefItems().add(i);
    
                // setup the parent/child relationship
                if (items.isEmpty())
                {
                    i.setParent(null);
                } else
                {
                    i.setParent(items.lastElement());
                }
                items.add(i);
                enforcedRanks.add(requiredRank);
            }
    
            for (TaxonTreeDefItem i : items)
            {
                i.setIsEnforced(enforcedRanks.contains(i.getRankId()));
            }
            
            try
            {
                Session     session = HibernateUtil.getNewSession();
                Transaction trans = session.beginTransaction();
                session.save(ttd);
                trans.commit();
                session.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
            
            CollectionInfo ci = getCIByTaxonTypeId(taxonomyTypeId);
            ci.setTaxonTreeDef(ttd);
            
            taxonTreeDefHash.put(taxonomyTypeId, ttd);
            
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
    public void convertTaxonRecords()
    {
        deleteAllRecordsFromTable(newDBConn, "taxon", BasicSQLUtils.myDestinationServerType);
        
        setIdentityInsertONCommandForSQLServer(newDBConn, "taxon", BasicSQLUtils.myDestinationServerType);
        
        String sql    = "SELECT * FROM taxonname WHERE TaxonName IS NOT NULL AND taxonname.TaxonomyTypeId = ";
        String cntSQL = "SELECT COUNT(*) FROM taxonname WHERE TaxonName IS NOT NULL AND taxonname.TaxonomyTypeId = ";


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

        for (Integer txTypeId : collDispHash.keySet())
        {
            Vector<CollectionInfo> collInfoList = collDispHash.get(txTypeId);
            
            CollectionInfo ci = collInfoList.get(0);
            
            setFieldsToIgnoreWhenMappingNames(ignoredFields);
            
            // AcceptedID is typically NULL unless they are using synonimies
            Integer cnt               = getCount(oldDBConn, "SELECT count(AcceptedID) FROM taxonname where AcceptedID IS NOT null");
            boolean showMappingErrors = cnt != null && cnt > 0;
    
            int errorsToShow = (BasicSQLUtils.SHOW_NAME_MAPPING_ERROR | BasicSQLUtils.SHOW_VAL_MAPPING_ERROR);
            if (showMappingErrors)
            {
                errorsToShow = errorsToShow | BasicSQLUtils.SHOW_PM_LOOKUP | BasicSQLUtils.SHOW_NULL_PM | BasicSQLUtils.SHOW_COPY_TABLE;
            }
            setShowErrors(errorsToShow);
            
            IdHashMapper.setTblWriter(tblWriter);
    
            log.info("Copying taxon records from 'taxonname' table");
            log.info("SQL: "+ (sql + ci.getTaxonomyTypeId()));
            if (!copyTable(oldDBConn, newDBConn, 
                           sql + ci.getTaxonomyTypeId(), cntSQL + ci.getTaxonomyTypeId(), 
                           "taxonname", "taxon", 
                           newToOldColMap, null, null,
                           BasicSQLUtils.mySourceServerType, BasicSQLUtils.myDestinationServerType))
            {
                String msg = "Table 'taxonname' didn't copy correctly";
                log.error(msg);
                tblWriter.logError(msg);
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
        
        IdMapperIFace txMapper = IdMapperMgr.getInstance().get("taxonname", "TaxonNameID");
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
    public void doConvert()
    {
        getTaxonTreesTypesInUse();
        
        convertAllTaxonTreeDefs();
        convertTaxonTreeDefSeparators();
        convertTaxonRecords();
    }
    
}

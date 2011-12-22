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
package edu.ku.brc.specify.conversion;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import com.mysql.jdbc.PreparedStatement;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 9, 2009
 *
 */
public class CollectionInfo implements Comparable<CollectionInfo>
{
    protected static final Logger           log         = Logger.getLogger(CollectionInfo.class);
    
    protected static Vector<CollectionInfo>   collectionInfoList                = new Vector<CollectionInfo>();
    protected static HashMap<String, Integer> kingdomTaxTypToTaxUnitRootIDHash  = new HashMap<String, Integer>();
    protected static HashMap<String, Pair<Integer, Integer>> kingdomTaxTypToTaxUnitRootIDHash2 = new HashMap<String, Pair<Integer, Integer>>();
    protected static Vector<Integer>          kingdomTaxTypToTaxUnitRootIDList  = new Vector<Integer>();

    protected static boolean                  askForFix = false;
    protected static PrintWriter              pw        = null;
    protected static int                      pwPassCnt = 1;
    
    public static boolean        DOING_ACCESSSION = false;


    protected boolean            isIncluded    = true;
    protected Integer            colObjTypeId;
    protected String             colObjTypeName;
    
    protected int                colObjCnt = 0;
    
    protected Integer            catSeriesDefId;
    protected Integer            catSeriesId;
    protected String             catSeriesName;
    protected String             catSeriesPrefix;
    protected String             catSeriesRemarks;
    protected String             catSeriesLastEditedBy;
    
    protected String             combinedCatSeries;
    
    protected Integer            taxonomyTypeId;
    protected String             taxonomyTypeName;
    protected Integer            taxonomicUnitTypeID;
    protected int                kingdom;
    
    protected TaxonTreeDef       taxonTreeDef = null;
    
    protected Integer            taxonNameId;  // root node of the tree
    protected String             taxonName;
    
    protected Integer            disciplineId;
    protected Discipline         discipline = null;
    
    protected Integer            collectionId;
    protected Collection         collection = null;
    
    protected int                taxonNameCnt;
    protected int                colObjDetTaxCnt;
    protected long               srcHostTaxonCnt;
    protected DisciplineType     disciplineTypeObj;
    
    protected String             determinationTaxonType = null;
    protected ArrayList<Integer> detTaxonTypeIdList     = new ArrayList<Integer>();
    
    protected HashMap<Integer, Taxon>            placeHolderTreeHash = new HashMap<Integer, Taxon>();
    protected List<TaxonTreeDefItem>             treeDefItems        = null;
    protected HashMap<Integer, TaxonTreeDefItem> treeDefItemHash     = new HashMap<Integer, TaxonTreeDefItem>();
    protected HashMap<Integer, Integer>          rankParentHash      = new HashMap<Integer, Integer>();
    protected Integer                            taxonRootId         = null;
    
    protected Connection   oldDBConn;
    
    /**
     * 
     */
    public CollectionInfo(final Connection oldDBConn)
    {
        super();
        this.oldDBConn = oldDBConn;
    }

    
    /**
     * @return the placeHolderTreeHash
     */
    public HashMap<Integer, Taxon> getPlaceHolderTreeHash()
    {
        return placeHolderTreeHash;
    }

    /**
     * @return the treeDefItems
     */
    public List<TaxonTreeDefItem> getTreeDefItems()
    {
        buildPlaceHolderInfo();
        
        return treeDefItems;
    }
    
    /**
     * @return the taxonRootId
     */
    public Integer getTaxonRootId()
    {
        return taxonRootId;
    }

    /**
     * @param taxonRootId the taxonRootId to set
     */
    public void setTaxonRootId(Integer taxonRootId)
    {
        this.taxonRootId = taxonRootId;
    }

    /**
     * 
     */
    private void buildPlaceHolderInfo()
    {
        if (treeDefItems == null)
        {
            try
            {
                treeDefItems = new Vector<TaxonTreeDefItem>(taxonTreeDef.getTreeDefItems());
                Collections.sort(treeDefItems);
                
                int i = 0;
                for (TaxonTreeDefItem item : treeDefItems)
                {
                    if (i > 0)
                    {
                        rankParentHash.put(item.getRankId(), treeDefItems.get(i-1).getRankId());
                    }
                    i++;
                    treeDefItemHash.put(item.getRankId(), item);
                }
                buildPlaceHolders();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * 
     */
    private void buildPlaceHolders()
    {
        if (placeHolderTreeHash.size() == 0)
        {
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                taxonTreeDef = session.get(TaxonTreeDef.class, taxonTreeDef.getId());
                
                String msg = "SELECT TaxonID FROM taxon WHERE RankID = 0 AND TaxonTreeDefID = " + taxonTreeDef.getId();
                log.debug(msg);
                Integer taxonId = BasicSQLUtils.getCount(msg);
                if (taxonId != null)
                {
                    Taxon  txRoot = (Taxon)session.getData("FROM Taxon WHERE id = " + taxonId);
                    Taxon  parent = txRoot;
                    
                    for (TaxonTreeDefItem item : treeDefItems)
                    {
                        if (item.getRankId() > 0)
                        {
                            Taxon taxon = new Taxon();
                            taxon.initialize();
                            taxon.setRankId(item.getRankId());
                            taxon.setName("Placeholder");
                            taxon.setFullName(taxon.getName());
                            
                            taxon.setDefinition(taxonTreeDef);
                            taxon.setDefinitionItem(item);
                            taxon.setParent(parent);
                            
                            parent = taxon;
                            
                            Transaction trans = null;
                            try
                            {
                                session.beginTransaction();
                                session.save(taxon);
                                session.commit();
                                
                                placeHolderTreeHash.put(item.getRankId(), taxon);
                                
                            } catch (Exception ex)
                            {
                                if (trans != null) trans.rollback();
                            }
                        }
                    }
                } else
                {
                    log.error("Couldn't find the Root Taxon Node");
                }
                
            } catch(Exception ex)
            {
                //session.rollback();
                ex.printStackTrace();
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }

        }
    }
    
    
    /**
     * @return the treeDefItemHash
     */
    public HashMap<Integer, TaxonTreeDefItem> getTreeDefItemHash()
    {
        buildPlaceHolderInfo();
        return treeDefItemHash;
    }

    /**
     * @return the rankParentHash
     */
    public HashMap<Integer, Integer> getRankParentHash()
    {
        buildPlaceHolderInfo();
        return rankParentHash;
    }

    /**
     * @param oldDBConn
     * @return
     */
    public static Pair<CollectionInfo, DisciplineType> getDisciplineType(final Connection oldDBConn)
    {
        int            max     = 0;
        CollectionInfo colInfo = null;
        int inx = 0;
        for (CollectionInfo ci : getCollectionInfoList(oldDBConn, true))
        {
            if (ci.getColObjCnt() > max || DOING_ACCESSSION)
            {
                max = ci.getColObjCnt();
                colInfo = ci;
            }
            inx++;
        }
        
        if (colInfo != null)
        {
            return new Pair<CollectionInfo, DisciplineType>(colInfo, DisciplineType.getDiscipline(colInfo.getColObjTypeName().toLowerCase()));
        }
        return null;
    }
    
    /**
     * @return the askForFix
     */
    public static boolean isAskForFix()
    {
        return askForFix;
    }

    /**
     * @param oldDBConn
     * @return
     */
    public static Vector<CollectionInfo> getCollectionInfoList(final Connection oldDBConn)
    {
        return getCollectionInfoList(oldDBConn, false);
    }
    
    /**
     * @param desc
     * @param sql
     */
    private static void logSQL(final String desc, final String sql)
    {
        pw.println(String.format("----------- %s -----------", desc));
        pw.println(sql);
        pw.println();
    }
    
    /**
     * @param kingdom
     * @param taxType
     * @return
     */
    private static String createHashKey(final Integer kingdom, final Integer taxType)
    {
        return String.format("%d_%d", kingdom, taxType);
    }
    
    /**
     * A list if Ids of the Root TaxonomicUnitTypeID that are actually being used.
     * @param oldDBConn connection
     */
    private static void retrieveTaxonomyUnitTypeRoots(final Connection oldDBConn)
    {
        String sql = "SELECT DISTINCT t.TaxonomicUnitTypeID FROM collectionobjectcatalog AS cc " +
                     "Inner Join collectionobject AS co ON cc.CollectionObjectCatalogID = co.CollectionObjectID " +
                     "Inner Join determination AS d ON co.CollectionObjectID = d.BiologicalObjectID " +
                     "Inner Join taxonname AS t ON d.TaxonNameID = t.TaxonNameID";
        log.debug(sql);
        
        for (Integer tutId : BasicSQLUtils.queryForInts(oldDBConn, sql))
        {
            sql = "SELECT RankID, Kingdom, TaxonomyTypeID FROM taxonomicunittype WHERE TaxonomicUnitTypeID = " + tutId;
            log.debug(sql);
            Object[] row = BasicSQLUtils.queryForRow(oldDBConn, sql);
            Integer rankID         = (Integer)row[0];
            Integer kingdom        = (Integer)row[1];
            Integer taxonomyTypeID = (Integer)row[2];
            
            Integer taxUnitTypeID;
            if (rankID > 0)
            {
                sql = String.format("SELECT TaxonomicUnitTypeID FROM taxonomicunittype WHERE TaxonomyTypeID = %d AND Kingdom = %d ORDER BY RankID ASC", taxonomyTypeID, kingdom);
                log.debug(sql);
                taxUnitTypeID = BasicSQLUtils.querySingleObj(oldDBConn, sql);
            } else
            {
                taxUnitTypeID = tutId;
            }
            
            if (taxUnitTypeID == null)
            {
                String msg = String.format("Unable to find the lowest RankID for kingdom[%d] taxonomyTypeID[%d]", kingdom, taxonomyTypeID);
                log.error(msg);
                UIRegistry.showError(msg);
            }
            
            Integer taxUnitId = kingdomTaxTypToTaxUnitRootIDHash.get(createHashKey(kingdom, taxonomyTypeID));
            if (taxUnitId == null)
            {
                log.debug(String.format("Adding - Kingdom: %d, TaxonType: %d -> TaxonomicUnitTypeID: %d", kingdom, taxonomyTypeID, taxUnitTypeID));
                
                kingdomTaxTypToTaxUnitRootIDHash.put(createHashKey(kingdom, taxonomyTypeID), taxUnitTypeID);
                kingdomTaxTypToTaxUnitRootIDHash2.put(createHashKey(kingdom, taxonomyTypeID), new Pair<Integer, Integer>(kingdom, taxonomyTypeID));
                kingdomTaxTypToTaxUnitRootIDList.add(taxUnitTypeID);
                
            } else if (!taxUnitId.equals(taxUnitTypeID))
            {
                String msg = String.format("Multiple kingdom [%d], taxonomyTypeID [%d] have different taxUnitTypeID [%d] [%d]", kingdom, taxonomyTypeID, taxUnitId, taxUnitTypeID);
                log.error(msg);
                UIRegistry.showError(msg);
            }
        }
    }

    /**
     * @param oldDBConn
     * @return
     */
    public static Vector<CollectionInfo> getCollectionInfoList(final Connection oldDBConn, final boolean doSkipCheck)
    {
        //collectionInfoList.clear();
        if (collectionInfoList.isEmpty())
        {        
            retrieveTaxonomyUnitTypeRoots(oldDBConn);
            
            String hostTaxonID = "SELECT Count(tn.TaxonomicUnitTypeID) FROM habitat h " + 
                                 "INNER JOIN taxonname tn ON h.HostTaxonID = tn.TaxonNameID WHERE tn.TaxonomyTypeId = ";
            
            /*String sql = "SELECT cot.CollectionObjectTypeID, cot.CollectionObjectTypeName, csd.CatalogSeriesDefinitionID, csd.CatalogSeriesID FROM collectionobjecttype cot " +
                         "INNER JOIN catalogseriesdefinition csd on " + 
                         "csd.ObjectTypeId = cot.CollectionObjectTypeId WHERE cot.Category = 'Biological' ORDER BY cot.CollectionObjectTypeID, csd.CatalogSeriesID";
            */
            
            String catSeriesSQL = "SELECT SeriesName, CatalogSeriesPrefix, Remarks, LastEditedBy FROM catalogseries WHERE CatalogSeriesID = ";
            

            String cntTaxonName = "SELECT COUNT(TaxonNameID) FROM taxonname WHERE TaxonName IS NOT NULL AND TaxonomyTypeId = ";
            

            String colObjCountPerCatSeriesSQL = "SELECT COUNT(cc.CatalogSeriesID) " + //, cc.CatalogSeriesID, cs.SeriesName " +
                                                "FROM collectionobjectcatalog cc INNER JOIN catalogseries cs ON cc.CatalogSeriesID = cs.CatalogSeriesID " +
                                                "WHERE cs.CatalogSeriesID = %d GROUP BY cs.CatalogSeriesID";
            
            String colObjDetCountPerCatSeriesSQL = "SELECT COUNT(cc.CatalogSeriesID) " +
                                                    "FROM determination d INNER JOIN collectionobject co ON d.BiologicalObjectID = co.CollectionObjectID " +
                                                    "INNER JOIN collectionobjectcatalog cc ON co.CollectionObjectID = cc.CollectionObjectCatalogID " +
                                                    "WHERE cc.CatalogSeriesID = %d AND d.TaxonNameID IS NOT NULL GROUP BY cc.CatalogSeriesID";
            
            String colObjCatSeriesSQL = "SELECT cot.CollectionObjectTypeID, cot.CollectionObjectTypeName, csd.CatalogSeriesDefinitionID, csd.CatalogSeriesID FROM collectionobjecttype cot " +
                                        "INNER JOIN catalogseriesdefinition csd on csd.ObjectTypeId = cot.CollectionObjectTypeId " +
                                        "WHERE cot.Category = 'Biological' ORDER BY cot.CollectionObjectTypeID, csd.CatalogSeriesID";
            
            Statement stmt = null;
            
            try
            {
                pw = new PrintWriter(String.format("sql_%d.log", pwPassCnt++));
                
                log.debug(colObjCatSeriesSQL);
                logSQL("ColObj Cat Series", colObjCatSeriesSQL);
                
                HashSet<Integer> taxonTypeIdHash = new HashSet<Integer>();
                
                stmt         = oldDBConn.createStatement();
                ResultSet rs = stmt.executeQuery(colObjCatSeriesSQL);
                while (rs.next())
                {
                    CollectionInfo info = new CollectionInfo(oldDBConn);
                    
                    Integer colObjTypeID = rs.getInt(1);
                    pw.println(String.format("%d ================================================================================", colObjTypeID));
                    
                    System.err.println("ColObjType: " + colObjTypeID);
                    
                    info.setColObjTypeId(colObjTypeID);
                    info.setColObjTypeName(rs.getString(2));
                    info.setCatSeriesDefId(rs.getInt(3));
                    info.setCatSeriesId(rs.getInt(4));
                    
                    String sql = String.format(colObjCountPerCatSeriesSQL, info.getCatSeriesId());
                    log.debug(sql);
                    logSQL("ColObj Count Per Cat Series", sql);
                    
                    int colObjCnt = BasicSQLUtils.getCountAsInt(oldDBConn, sql);
                    info.setColObjCnt(colObjCnt);
                    
                    sql = String.format(colObjDetCountPerCatSeriesSQL, info.getCatSeriesId());
                    log.debug(sql);
                    logSQL("ColObj Count Determinations Per Cat Series", sql);

                    info.setColObjDetTaxCnt(BasicSQLUtils.getCountAsInt(oldDBConn, sql));
                    
                    sql = catSeriesSQL + info.getCatSeriesId();
                    log.debug(sql);
                    logSQL("Cat Series", sql);

                    Vector<Object[]> rows = BasicSQLUtils.query(oldDBConn, sql);
                    if (rows != null && rows.size() == 1)
                    {
                        Object[] row = rows.get(0);
                        
                        info.setCatSeriesName((String)row[0]);
                        info.setCatSeriesPrefix((String)row[1]);
                        info.setCatSeriesRemarks((String)row[2]);
                        info.setCatSeriesLastEditedBy((String)row[3]);
                        
                    } else
                    {
                        log.error("Error getting CollectionInfo for CollectionObjectTypeID: " + rs.getInt(1)+" number of CatlogSeries: " + rows.size());
                    }
                    
                    if (!doSkipCheck)
                    {
                        String detSQLStr = "SELECT ct.TaxonomyTypeID, (select distinct relatedsubtypevalues FROM usysmetacontrol c " +
                                           "LEFT JOIN usysmetafieldsetsubtype fst ON fst.fieldsetsubtypeid = c.fieldsetsubtypeid " +
                                           "WHERE objectid = 10290 AND ct.taxonomytypeid = c.relatedsubtypevalues) AS DeterminationTaxonType " +
                                           "FROM collectiontaxonomytypes ct WHERE ct.biologicalobjecttypeid = " + info.getColObjTypeId();
                        log.debug(detSQLStr);
                        logSQL("Checking USYS data", detSQLStr);


                        Vector<Object[]> detRows = BasicSQLUtils.query(oldDBConn, detSQLStr);
                        for (Object[] row : detRows)
                        {
                            Integer txnTypeId    = (Integer)row[0];
                            String  detTxnTypes  = (String)row[1];
                            if (detTxnTypes == null)
                            {
                                detTxnTypes = Integer.toString(txnTypeId);
                            }
                            
                            if (StringUtils.isNotEmpty(detTxnTypes))
                            {
                                String txNameSQL = "SELECT TaxonomyTypeName FROM taxonomytype WHERE TaxonomyTypeID = ";
                                logSQL("Getting Taxon Type Name", txNameSQL+txnTypeId);


                                if (StringUtils.contains(detTxnTypes, ','))
                                {
                                    StringBuilder sb = new StringBuilder();
                                    String[] toks = StringUtils.split(detTxnTypes, ',');
                                    
                                    String dtName = BasicSQLUtils.querySingleObj(oldDBConn, txNameSQL + txnTypeId);
                                    sb.append(String.format("Warning - There are %d DeterminationTaxonTypes for TaxonObjectType %d (%s) they are:\n",  toks.length, txnTypeId, dtName));
                                    for (String id : toks)
                                    {
                                        logSQL("Getting Taxon Type Name", txNameSQL + id);
                                        String name = BasicSQLUtils.querySingleObj(oldDBConn, txNameSQL + id);
                                        sb.append(id);
                                        sb.append(" - ");
                                        sb.append(name);
                                        sb.append("\n");
                                    }
                                    sb.append("\nThis database will need to be fixed by hand before it can be converted.");
                                    UIRegistry.showError(sb.toString());
                                    
                                    askForFix = true;
                                    return null;
                                    
                                } else if (StringUtils.isNumeric(detTxnTypes.trim()))
                                {
                                    Integer txnType = Integer.parseInt(detTxnTypes);
                                    if (!txnType.equals(txnTypeId))
                                    {
                                        String tName  = BasicSQLUtils.querySingleObj(oldDBConn, txNameSQL + txnType);
                                        String dtName = BasicSQLUtils.querySingleObj(oldDBConn, txNameSQL + txnTypeId);
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(String.format("Warning - The TaxonObjectType %d (%s) in the DeterminationTaxonTypes field\ndoesn't match the actual TaxonObjectType %d (%s)",  txnType, tName, txnTypeId, dtName));
                                        UIRegistry.showError(sb.toString());
                                        askForFix = true;
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                    
                    /*info.setDeterminationTaxonType(detTxnTypeStr);
                    for (Integer id : info.getDetTaxonTypeIdList())
                    {
                        log.debug("ID: "+id);
                    }*/
                    
                    // This represents a mapping from what would be the Discipline (Biological Object Type) to the Taxonomic Root
                    sql = String.format("SELECT tt.TaxonomyTypeID, tt.TaxonomyTypeName, tu.Kingdom, tn.TaxonNameID, tn.TaxonName, tu.TaxonomicUnitTypeID FROM taxonomytype AS tt " +
                                        "INNER JOIN taxonomicunittype AS tu ON tt.TaxonomyTypeID = tu.TaxonomyTypeID " +
                                        "INNER JOIN taxonname AS tn ON tu.TaxonomyTypeID = tn.TaxonomyTypeID " +
                                        "INNER JOIN collectiontaxonomytypes AS ct ON tn.TaxonomyTypeID = ct.TaxonomyTypeID " +
                                        "WHERE tu.RankID =  0 AND tn.RankID =  0 AND ct.BiologicalObjectTypeID = %d " +
                                        "ORDER BY ct.BiologicalObjectTypeID ASC", info.getColObjTypeId());
                    log.debug(sql);
                    logSQL("Taxon -> Coll Taxon Types", sql);

                    rows = BasicSQLUtils.query(oldDBConn, sql);
                    if (rows != null)
                    {
                        Object[] row = rows.get(0);
                        
                        int taxonomyTypeID = (Integer)row[0];
                        
                        info.setTaxonomyTypeId(taxonomyTypeID);
                        info.setTaxonomyTypeName((String)row[1]);
                        info.setKingdom((Integer)row[2]);
                        info.setTaxonNameId((Integer)row[3]);
                        info.setTaxonName((String)row[4]);
                        info.setTaxonomicUnitTypeID((Integer)row[5]);
                        
                        info.setTaxonNameCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntTaxonName + taxonomyTypeID));
                        
                        log.debug("TaxonomyTypeName: "+ info.getTaxonomyTypeName()+
                                  "  TaxonName: "+info.getTaxonName()+
                                  "  TaxonomyTypeId: "+info.getTaxonomyTypeId());
                        
                        sql = hostTaxonID + taxonomyTypeID;
                        log.debug(sql);
                        Vector<Object> ttNames = BasicSQLUtils.querySingleCol(oldDBConn, sql);
                        if (ttNames != null && ttNames.size() > 0 && ((Long)ttNames.get(0)) > 0)
                        {
                            info.setSrcHostTaxonCnt((Long)ttNames.get(0));
                        } else
                        {
                            info.setSrcHostTaxonCnt(0);
                        }
                        
                        taxonTypeIdHash.add(taxonomyTypeID);
                        
                    } else
                    {
                        log.error("Error getting CollectionInfo for CollectionObjectTypeID: " + rs.getInt(1));
                    }
                    
                    collectionInfoList.add(info);
                    //System.out.println(info.toString());
                }
                rs.close();
                
                // Here we figure out whether a Taxon Tree that is used by HostTaxonID is associated with a Collection.
                String sql = "SELECT DISTINCT tt.TaxonomyTypeID FROM habitat AS h " +
                             "INNER JOIN taxonname AS tn ON h.HostTaxonID = tn.TaxonNameID " +
                             "INNER JOIN taxonomytype AS tt ON tn.TaxonomyTypeID = tt.TaxonomyTypeID";
                logSQL("Check for HostID", sql);

                Vector<Integer> txTypeIds = BasicSQLUtils.queryForInts(oldDBConn, sql);
                
                HashSet<Integer> txnTypeIdHashSet = new HashSet<Integer>();
                for (Integer txTypId : txTypeIds)
                {
                    Boolean hasColInfo = false;
                    for (CollectionInfo colInfo : collectionInfoList)
                    {
                        if (colInfo.getTaxonomyTypeId().equals(txTypId))
                        {
                            hasColInfo = true;
                        }
                    }
                    
                    if (!hasColInfo)
                    {
                        txnTypeIdHashSet.add(txTypId);
                    }
                }
                
                // These TaxonTypeIds do not have CollectionInfo
                for (Iterator<Integer> iter = txnTypeIdHashSet.iterator(); iter.hasNext();)
                {
                    Integer taxonomyTypeID = iter.next();
                    System.out.println(taxonomyTypeID);
                    
                    sql = "SELECT tt.TaxonomyTypeName, tn.TaxonName, tut.Kingdom, tn.TaxonNameID, tn.TaxonomicUnitTypeID FROM taxonomytype AS tt " +
                          "INNER JOIN taxonomicunittype AS tut ON tt.TaxonomyTypeID = tut.TaxonomyTypeID " +
                          "INNER JOIN taxonname AS tn ON tt.TaxonomyTypeID = tn.TaxonomyTypeID AND tut.TaxonomicUnitTypeID = tn.TaxonomicUnitTypeID " +    
                          "WHERE tt.TaxonomyTypeID =  "+taxonomyTypeID+" AND tn.RankID =  0";
                    log.debug(sql);
                    logSQL("Get TaxonTypeName etc from TaxonomyTypeID and RankID = 0", sql);
                    
                    Vector<Object[]> rows = BasicSQLUtils.query(oldDBConn, sql);
                    if (rows.size() != 1)
                    {
                        String msg = "There should only be '1' TaxonTypeName for  TaxonomyTypeID:"+taxonomyTypeID;
                        log.error(msg);
                        UIRegistry.showError(msg);
                        continue;
                    }
                    
                    CollectionInfo colInfo = new CollectionInfo(oldDBConn);
                    
                    String taxonTypeName = (String)rows.get(0)[0];
                    String taxonRootName = (String)rows.get(0)[1];
                    if (StringUtils.isEmpty(taxonRootName))
                    {
                        taxonRootName = taxonTypeName;
                    }
                    
                    //colInfo.setColObjTypeId();
                    colInfo.setColObjTypeName(taxonRootName);
                    //colInfo.setCatSeriesDefId(rs.getInt(3));
                    //colInfo.setCatSeriesId(rs.getInt(4));
                    
                    colInfo.setCatSeriesName(taxonRootName);
                    colInfo.setCatSeriesPrefix("");
                    colInfo.setCatSeriesRemarks("");
                    colInfo.setCatSeriesLastEditedBy("");
                    
                    colInfo.setColObjCnt(1);
                    colInfo.setColObjDetTaxCnt(1);
                    
                    colInfo.setTaxonomyTypeId(taxonomyTypeID);
                    colInfo.setTaxonomyTypeName(taxonTypeName);
                    colInfo.setKingdom((Integer)rows.get(0)[2]);
                    colInfo.setTaxonNameId((Integer)rows.get(0)[3]);
                    colInfo.setTaxonName(taxonRootName);
                    colInfo.setTaxonomicUnitTypeID((Integer)rows.get(0)[4]);
                    
                    colInfo.setTaxonNameCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntTaxonName + taxonomyTypeID));
                    
                    colInfo.setSrcHostTaxonCnt(0);
                    
                    collectionInfoList.add(colInfo);
                }
                
                int cnt = getFilteredCollectionInfoList().size();
                if (kingdomTaxTypToTaxUnitRootIDList.size() > cnt)
                {
                    String msg = String.format("There are %d TaxonomicUnitType Roots being used and there are only %d Collection(s) with ColObjs.",  kingdomTaxTypToTaxUnitRootIDList.size(), cnt);
                    log.error(msg);
                    //UIRegistry.showError(msg);
                    
                    HashMap<Integer, Integer> checkHash = new HashMap<Integer, Integer>();
                    for (Pair<Integer, Integer> p : kingdomTaxTypToTaxUnitRootIDHash2.values())
                    {
                        Integer ttCnt = checkHash.get(p.second);
                        if (ttCnt == null) ttCnt = 0;
                        ttCnt++;
                        checkHash.put(p.second, ttCnt);
                    }
                    
                    if (checkHash.size() == 1)
                    {
                        int ttCnt = checkHash.values().iterator().next();
                        UIRegistry.showError(JOptionPane.INFORMATION_MESSAGE, String.format("There are %d trees, they will be merged into a single tree.", ttCnt));
                        
                    } else
                    {
                        msg = String.format("There are %d different types of trees and the converter cannot handle that right now.", checkHash.size());
                        log.error(msg);
                        UIRegistry.showError(msg);
                        return null;
                    }
                    
                    HashMap<String, Integer> hash  = new HashMap<String, Integer>(kingdomTaxTypToTaxUnitRootIDHash);
                    for (CollectionInfo ci : getFilteredCollectionInfoList())
                    {
                        hash.remove(createHashKey(ci.getKingdom(),ci.getTaxonomyTypeId()));
                    }
                    
                    /*for (String key : hash.keySet())
                    {
                        Pair<Integer, Integer> p = kingdomTaxTypToTaxUnitRootIDHash2.get(key);
                        if (p != null)
                        {
                            makeNewCollection(oldDBConn, p.first, p.second, kingdomTaxTypToTaxUnitRootIDHash.get(key));
                        }
                    }*/
                }
                
                for (CollectionInfo ci : getFilteredCollectionInfoList())
                {
                    Integer tutId = kingdomTaxTypToTaxUnitRootIDHash.get(createHashKey(ci.getKingdom(),ci.getTaxonomyTypeId()));
                    if (tutId == null)
                    {
                        String msg = String.format("No TaxonomicUnitTypeID for Kingdom [%d] and TaxonomyTypeId [%d]", ci.getKingdom(),ci.getTaxonomyTypeId());
                        log.error(msg);
                        UIRegistry.showError(msg);
                        
                    } else if (!tutId.equals(ci.getTaxonomicUnitTypeID()))
                    {
                        String msg = String.format("Bad match between CI's TaxonomicUnitTypeID [%d] and the the independent look up [%d]", ci.getTaxonomicUnitTypeID(), tutId);
                        log.error(msg);
                        UIRegistry.showError(msg);
                    }
                }

                dump();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                try
                {
                    if (stmt != null)
                    {
                        stmt.close();
                    }
                } catch (Exception e) {}
                
            } finally
            {
                if (pw != null) pw.close();
            }
        }
        
        Collections.sort(collectionInfoList);
        
        return collectionInfoList;
    }
    
    private static Vector<Integer> getTaxonLevels(final int taxType, final int kingdom)
    {
        Vector<Integer> items = new Vector<Integer>();
        
        return items;
    }
    
    private static void mergeTrees(final Connection oldDBConn)
    {
        CollectionInfo collInfo = getFilteredCollectionInfoList().get(0);
        
        String sql = "SELECT RankID, TaxonomicUnitTypeID FROM taxonomicunittype WHERE TaxonomyTypeID = %s AND Kingdom = %d"; 
        //Vector<Integer> mainTreeLevels = BasicSQLUtils.queryForInts(String.format(sql, collInfo.getTaxonomyTypeId(), collInfo.getKingdom());
        
        
    }
    
    /**
     * @return
     */
    /*private static CollectionInfo getCollInfoToCopy(final Connection oldDBConn, final Integer kingdom, final Integer taxonTypeID)
    {
        Vector<CollectionInfo> ciList = getFilteredCollectionInfoList();
        if (ciList.size() == 1)
        {
            return ciList.get(0);
        }
        
        String sql = "SELECT tut.TaxonomicUnitTypeID, tut.Kingdom, tut.TaxonomyTypeID, tut.RankID, tt.TaxonomyTypeName, ctt.CollectionTaxonomyTypesID, `cot`.CollectionObjectTypeID, " +
        		     "`cot`.CollectionObjectTypeName, csd.CatalogSeriesDefinitionID, csd.CatalogSeriesID, cs.SeriesName FROM taxonomicunittype AS tut " +
                     "Inner Join taxonomytype AS tt ON tut.TaxonomyTypeID = tt.TaxonomyTypeID " +
                     "Inner Join collectiontaxonomytypes AS ctt ON tt.TaxonomyTypeID = ctt.TaxonomyTypeID " +
                     "Inner Join collectionobjecttype AS `cot` ON ctt.BiologicalObjectTypeID = `cot`.CollectionObjectTypeID " +
                     "Inner Join catalogseriesdefinition AS csd ON `cot`.CollectionObjectTypeID = csd.ObjectTypeID " +
                     "Inner Join catalogseries AS cs ON csd.CatalogSeriesID = cs.CatalogSeriesID " +    " WHERE tut.RankID <  20 " +
                     "ORDER BY tut.Kingdom ASC, tut.TaxonomyTypeID ASC, tut.RankID ASC";
        UIRegistry.showError("Not implemented yet!");
        throw new RuntimeException("Not implemented yet!");
    }*/
    
    /**
     * @param kingdom
     * @param taxonTypeID
     * @param taxUnitTypeID
     */
    /*public static int getNewIdFromTable(final Connection oldDBConn, final String tableName, final String primaryKeyName)
    {
       int id = 1;
        do
        {
            String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = %d", tableName, primaryKeyName, id);
            if (BasicSQLUtils.getCountAsInt(oldDBConn, sql) == 0)
            {
                return id;
            }
            id++;
        } while (true);
    }
    
    private Integer updateSQL(final Connection oldDBConn, final String updateSQL) throws SQLException
    {
        Statement stmt = null;
        try
        {
            stmt = oldDBConn.createStatement();
            stmt.executeUpdate(updateSQL);
            return BasicSQLUtils.getInsertedId(stmt);
            
        } finally
        {
            if (stmt != null) stmt.close();
        }
    }
    
    public static void makeNewCollection(final Connection oldDBConn, final Integer kingdom, final Integer taxonTypeID, final Integer taxUnitTypeID)
    {
        CollectionInfo collInfo = getCollInfoToCopy(oldDBConn, kingdom, taxonTypeID); // Find a simalr one
        if (collInfo == null) return;
        
        try
        {
            String dateStr = "2011-12-15 00:00:00";
            
            String insSQL1 = "INSERT INTO catalogseries (CatalogSeriesID, CollectionID, SeriesName, CatalogSeriesPrefix, Remarks, TimestampModified, TimestampCreated, LastEditedBy) VALUES (%d,%d,'%s','%s','%s','%s','%s','%s')";
            String insSQL2 = "INSERT INTO catalogseriesdefinition (CatalogSeriesDefinitionID, CatalogSeriesID, ObjectTypeID, Remarks,TimestampModified,TimestampCreated,LastEditedBy) VALUES (%d,%d,'%s','%s','%s','%s','%s')";
            String insSQL3 = "INSERT INTO collectiontaxonomytypes (CollectionTaxonomyTypesID, CollectionID, BiologicalObjectTypeID, TaxonomyTypeID, TimestampModified, TimestampCreated, LastEditedBy, DisplaySubSpecificTaxaLevelIndicators) " +
            		         "VALUES (%d,%d,%d,%d,'%s','%s','%s',1)";
            
            int newCatSeriesID    = getNewIdFromTable(oldDBConn, "catalogseries", "CatalogSeriesID");
            int newCatSeriesDefID = getNewIdFromTable(oldDBConn, "catalogseriesdefinition", "CatalogSeriesDefinitionID");
            int newCollTaxObjID   = getNewIdFromTable(oldDBConn, "collectiontaxonomytypes", "CollectionTaxonomyTypesID");
            
            String insertSQL1 = String.format(insSQL1, newCatSeriesID, 0, collInfo.getCatSeriesName(), collInfo.getCatSeriesPrefix(), "", dateStr, dateStr, "conv");
            BasicSQLUtils.update(oldDBConn, insertSQL1);
            
            String insertSQL2 = String.format(insSQL2, newCatSeriesDefID, newCatSeriesID, collInfo.getCatSeriesName(), collInfo.getCatSeriesPrefix(), "", dateStr, dateStr, "conv");
            BasicSQLUtils.update(oldDBConn, insertSQL2);
            
            String insertSQL3 = String.format(insSQL3, newCatSeriesID, 0, collInfo.getCatSeriesName(), collInfo.getCatSeriesPrefix(), "", dateStr, dateStr, "conv");
            BasicSQLUtils.update(oldDBConn, insertSQL3);
            
            //String sql3 = "UPDATE collectionobjectcatalog SET CatalogSeriesID=%d WHERE CatalogSeriesID=%d AND CollectionObjectTypeID=%d";
            //String sql4 = "SELECT COUNT(*) FROM collectionobjectcatalog WHERE CatalogSeriesID=%d AND CollectionObjectTypeID=%d";
            

            
            
            
                        
            CollectionInfo.getCollectionInfoList().clear();
            collectionInfoList = CollectionInfo.getCollectionInfoList(oldDBConn, false);

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }*/

    /**
     * @return the determinationTaxonType
     */
    public String getDeterminationTaxonType()
    {
        return determinationTaxonType;
    }


    /**
     * @param determinationTaxonType the determinationTaxonType to set
     */
    public void setDeterminationTaxonType(String determinationTaxonType)
    {
        this.determinationTaxonType = determinationTaxonType;
        
        if (StringUtils.isNotEmpty(determinationTaxonType))
        {
            for (String str : StringUtils.split(determinationTaxonType, ','))
            {
                if (StringUtils.isNumeric(str))
                {
                    Integer id = Integer.parseInt(str);
                    detTaxonTypeIdList.add(id);
                }
            }
        } else
        {
            determinationTaxonType = null;
        }
    }


    /**
     * @return the detTaxonTypeIdList
     */
    public List<Integer> getDetTaxonTypeIdList()
    {
        return detTaxonTypeIdList;
    }


    /**
     * @return
     */
    public boolean isTaxonomicUnitTypeInUse()
    {
        String sql = "SELECT COUNT(*) FROM taxonname WHERE TaxonomicUnitTypeID = "+taxonomicUnitTypeID;
        log.debug(sql);
        
        int count = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM taxonname WHERE TaxonomicUnitTypeID = "+taxonomicUnitTypeID);
        log.debug("Count: "+count);
        return count > 0;
    }
    
    /**
     * 
     */
    public static void dump()
    {
        for (CollectionInfo ci : collectionInfoList)
        {
            System.out.println("-----------------\n"+ci.toString());
        }
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(CollectionInfo o)
    {
        return taxonNameId.compareTo(o.taxonNameId);
    }


    /**
     * @return
     */
    public static Vector<CollectionInfo> getCollectionInfoList()
    {
        return collectionInfoList;
    }
    
    /**
     * @return
     */
    public static Vector<CollectionInfo> getFilteredCollectionInfoList()
    {
        if (DOING_ACCESSSION)
        {
            return new Vector<CollectionInfo>(collectionInfoList);
        }
        
        Vector<CollectionInfo> colList = new Vector<CollectionInfo>();
        for (CollectionInfo ci : collectionInfoList)
        {
            if (ci.getColObjCnt() > 0 || ci.getSrcHostTaxonCnt() > 0)
            {
                colList.add(ci);
            }
        }
        return colList;
    }


    /**
     * @return932413666
     */
    public static DefaultTableModel getCollectionInfoTableModel(final boolean doFilter)
    {
        CollectionInfoModel model;
        if (doFilter)
        {
            model = new CollectionInfoModel(getFilteredCollectionInfoList());
        } else
        {
            model = new CollectionInfoModel(collectionInfoList);    
        }
        
        return model;
    }
    
    /**
     * @param collection
     * @return
     */
    public static CollectionInfo getCollectionObjectTypeForNewCollection(final Collection collection)
    {
        for (CollectionInfo ci : getFilteredCollectionInfoList())
        {
            log.debug(ci.toString()+"  "+collection.getId());
        }
        for (CollectionInfo ci : getFilteredCollectionInfoList())
        {
            log.debug(ci.getCatSeriesName()+" "+ ci.getCollectionId()+"  "+collection.getId());
            if (ci.getCollectionId().equals(collection.getId()))
            {
                return ci;
            }
        }
        return null;
    }
    
     //----- Getters and Setters ----------------------------
    
    
    /**
     * @return the taxonNameCnt
     */
    public int getTaxonNameCnt()
    {
        return taxonNameCnt;
    }

    /**
     * @return the taxonomicUnitTypeID
     */
    public Integer getTaxonomicUnitTypeID()
    {
        return taxonomicUnitTypeID;
    }


    /**
     * @param taxonomicUnitTypeID the taxonomicUnitTypeID to set
     */
    public void setTaxonomicUnitTypeID(final Integer taxonomicUnitTypeID)
    {
        this.taxonomicUnitTypeID = taxonomicUnitTypeID;
    }


    /**
     * @return the taxonNameId
     */
    public Integer getTaxonNameId()
    {
        return taxonNameId;
    }


    /**
     * @return the colObjCnt
     */
    public int getColObjCnt()
    {
        return colObjCnt;
    }


    /**
     * @param colObjCnt the colObjCnt to set
     */
    public void setColObjCnt(int colObjCnt)
    {
        this.colObjCnt = colObjCnt;
    }


    /**
     * @return the srcHostTaxonCnt
     */
    public long getSrcHostTaxonCnt()
    {
        return srcHostTaxonCnt;
    }


    /**
     * @param srcHostTaxonCnt the srcHostTaxonCnt to set
     */
    public void setSrcHostTaxonCnt(long srcHostTaxonCnt)
    {
        this.srcHostTaxonCnt = srcHostTaxonCnt;
    }

    /**
     * @return the isIncluded
     */
    public boolean isIncluded()
    {
        return isIncluded;
    }


    /**
     * @param isIncluded the isIncluded to set
     */
    public void setIncluded(boolean isIncluded)
    {
        this.isIncluded = isIncluded;
    }


    /**
     * @param taxonNameCnt the taxonNameCnt to set
     */
    public void setTaxonNameCnt(int taxonNameCnt)
    {
        this.taxonNameCnt = taxonNameCnt;
    }


    /**
     * @return the colObjDetTaxCnt
     */
    public int getColObjDetTaxCnt()
    {
        return colObjDetTaxCnt;
    }


    /**
     * @param colObjDetTaxCnt the colObjDetTaxCnt to set
     */
    public void setColObjDetTaxCnt(int colObjDetTaxCnt)
    {
        this.colObjDetTaxCnt = colObjDetTaxCnt;
    }

    /**
     * @return the taxonTreeDef
     */
    public TaxonTreeDef getTaxonTreeDef()
    {
        return taxonTreeDef;
    }

    /**
     * @return the catSeriesLastEditedBy
     */
    public String getCatSeriesLastEditedBy()
    {
        return catSeriesLastEditedBy;
    }


    /**
     * @param catSeriesLastEditedBy the catSeriesLastEditedBy to set
     */
    public void setCatSeriesLastEditedBy(String catSeriesLastEditedBy)
    {
        this.catSeriesLastEditedBy = catSeriesLastEditedBy;
    }


    /**
     * @return the catSeriesRemarks
     */
    public String getCatSeriesRemarks()
    {
        return catSeriesRemarks;
    }


    /**
     * @param catSeriesRemarks the catSeriesRemarks to set
     */
    public void setCatSeriesRemarks(String catSeriesRemarks)
    {
        this.catSeriesRemarks = catSeriesRemarks;
    }


    /**
     * @return the catSeriesPrefix
     */
    public String getCatSeriesPrefix()
    {
        return catSeriesPrefix;
    }


    /**
     * @param catSeriesPrefix the catSeriesPrefix to set
     */
    public void setCatSeriesPrefix(String catSeriesPrefix)
    {
        this.catSeriesPrefix = catSeriesPrefix;
    }


    /**
     * @return the catSeriesName
     */
    public String getCatSeriesName()
    {
        return catSeriesName;
    }


    /**
     * @param catSeriesName the catSeriesName to set
     */
    public void setCatSeriesName(String catSeriesName)
    {
        this.catSeriesName = catSeriesName;
    }


    /**
     * @return the colObjTypeId
     */
    public Integer getColObjTypeId()
    {
        return colObjTypeId;
    }


    /**
     * @param colObjTypeId the colObjTypeId to set
     */
    public void setColObjTypeId(Integer colObjTypeId)
    {
        this.colObjTypeId = colObjTypeId;
    }


    /**
     * @return the colObjTypeName
     */
    public String getColObjTypeName()
    {
        return colObjTypeName;
    }


    /**
     * @param colObjTypeName the colObjTypeName to set
     */
    public void setColObjTypeName(String colObjTypeName)
    {
        this.colObjTypeName = colObjTypeName;
    }


    /**
     * @return the catSeriesDefId
     */
    public Integer getCatSeriesDefId()
    {
        return catSeriesDefId;
    }


    /**
     * @param catSeriesDefId the catSeriesDefId to set
     */
    public void setCatSeriesDefId(Integer catSeriesDefId)
    {
        this.catSeriesDefId = catSeriesDefId;
    }


    /**
     * @return the catSeriesId
     */
    public Integer getCatSeriesId()
    {
        return catSeriesId;
    }


    /**
     * @param catSeriesId the catSeriesId to set
     */
    public void setCatSeriesId(Integer catSeriesId)
    {
        this.catSeriesId = catSeriesId;
    }


    /**
     * @return the taxonomyTypeId
     */
    public Integer getTaxonomyTypeId()
    {
        return taxonomyTypeId;
    }


    /**
     * @param taxonomyTypeId the taxonomyTypeId to set
     */
    public void setTaxonomyTypeId(Integer taxonomyTypeId)
    {
        this.taxonomyTypeId = taxonomyTypeId;
    }


    /**
     * @return the taxonomyTypeName
     */
    public String getTaxonomyTypeName()
    {
        return taxonomyTypeName;
    }


    /**
     * @param taxonomyTypeName the taxonomyTypeName to set
     */
    public void setTaxonomyTypeName(final String taxonomyTypeName)
    {
        this.taxonomyTypeName = taxonomyTypeName;
    }

    /**
     * @return the kingdomId
     */
    public int getKingdom()
    {
        return kingdom;
    }

    public boolean isInUse()
    {
        return taxonNameCnt > 0;
    }
    
    /**
     * @param taxonTreeDef the taxonTreeDef to set
     */
    public void setTaxonTreeDef(TaxonTreeDef taxonTreeDef)
    {
        this.taxonTreeDef = taxonTreeDef;
    }


    /**
     * @param kingdom the kingdomId to set
     */
    public void setKingdom(int kingdom)
    {
        this.kingdom = kingdom;
    }

    /**
     * @param taxonNameId the taxonNameId to set
     */
    public void setTaxonNameId(int taxonNameId)
    {
        this.taxonNameId = taxonNameId;
    }

    /**
     * @return the taxonName
     */
    public String getTaxonName()
    {
        return taxonName;
    }

    /**
     * @param taxonName the taxonName to set
     */
    public void setTaxonName(String taxonName)
    {
        this.taxonName = taxonName;
    }

    /**
     * @return the disciplineId
     */
    public Integer getDisciplineId()
    {
        return disciplineId;
    }

    /**
     * @param disciplineId the disciplineId to set
     */
    public void setDisciplineId(Integer disciplineId)
    {
        this.disciplineId = disciplineId;
    }

    /**
     * @return the discipline
     */
    public Discipline getDiscipline()
    {
        return discipline;
    }


    /**
     * @param discipline the discipline to set
     */
    public void setDiscipline(Discipline discipline)
    {
        this.discipline = discipline;
    }


    /**
     * @return the collectionId
     */
    public Integer getCollectionId()
    {
        return collectionId;
    }


    /**
     * @param collectionId the collectionId to set
     */
    public void setCollectionId(Integer collectionId)
    {
        this.collectionId = collectionId;
    }


    /**
     * @return the collection
     */
    public Collection getCollection()
    {
        return collection;
    }


    /**
     * @return the disciplineTypeObj
     */
    public DisciplineType getDisciplineTypeObj()
    {
        return disciplineTypeObj;
    }


    /**
     * @param disciplineTypeObj the disciplineTypeObj to set
     */
    public void setDisciplineTypeObj(DisciplineType disciplineTypeObj)
    {
        this.disciplineTypeObj = disciplineTypeObj;
    }


    /**
     * @param collection the collection to set
     */
    public void setCollection(Collection collection)
    {
        this.collection = collection;
    }

    /**
     * @return the combinedCatSeries
     */
    public String getCombinedCatSeries()
    {
        return combinedCatSeries;
    }


    /**
     * @param combinedCatSeries the combinedCatSeries to set
     */
    public void setCombinedCatSeries(String combinedCatSeries)
    {
        this.combinedCatSeries = combinedCatSeries;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        // Taken from a web example
        try
        {
            String   result = "";
            Class<?> cls = this.getClass();
            Field fieldlist[] = cls.getDeclaredFields();
            for (int i = 0; i < fieldlist.length; i++)
            {
                Field fld = fieldlist[i];
                if (!Modifier.isStatic(fld.getModifiers()))
                {
                    result +=  fld.getName() + " = " + fld.get(this) + "\n";
                }
            }
            result += "\n";
            
            return result;

        } catch (Throwable e)
        {
            e.printStackTrace();
        }
        return super.toString();
    }

}


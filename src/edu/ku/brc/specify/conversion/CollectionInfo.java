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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;

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
    
    protected static Vector<CollectionInfo> collectionInfoList = new Vector<CollectionInfo>();
    protected static boolean                askForFix = false;
    protected static PrintWriter            pw        = null;
    protected static int                    pwPassCnt = 1;
    
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
    protected int                kingdomId;
    
    //protected TaxonTreeDef       taxonTreeDef = null;
    
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
    
    protected Integer            taxonRootId            = null;
    protected PlaceholderHelper  placeholderHelper      = null;
    
    protected Connection         oldDBConn;
    
    
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
        assert(placeholderHelper == null);
        return placeholderHelper.getPlaceHolderTreeHash();
    }

    /**
     * @return the treeDefItems
     */
    public List<TaxonTreeDefItem> getTreeDefItems()
    {
        assert(placeholderHelper == null);
        return placeholderHelper.getTreeDefItems();
    }
    
    /**
     * @return the placeholderHelper
     */
    public PlaceholderHelper getPlaceholderHelper()
    {
        return placeholderHelper;
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
     * @return the treeDefItemHash
     */
    public HashMap<Integer, TaxonTreeDefItem> getTreeDefItemHash()
    {
        assert(placeholderHelper == null);
        return placeholderHelper.getTreeDefItemHash();
    }

    /**
     * @return the rankParentHash
     */
    public HashMap<Integer, Integer> getRankParentHash()
    {
        assert(placeholderHelper == null);
        return placeholderHelper.getRankParentHash();
    }

    /**
     * @param oldDBConn
     * @return
     */
    public static Pair<CollectionInfo, DisciplineType> getDisciplineType(final Connection oldDBConn)
    {
        int            max     = 0;
        CollectionInfo colInfo = null;
        for (CollectionInfo ci : getCollectionInfoList(oldDBConn, true))
        {
            if (ci.getColObjCnt() > max || DOING_ACCESSSION)
            {
                max = ci.getColObjCnt();
                colInfo = ci;
            }
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
     * @param oldDBConn
     * @return
     */
    public static Vector<CollectionInfo> getCollectionInfoList(final Connection oldDBConn, final boolean doSkipCheck)
    {
        //collectionInfoList.clear();
        if (collectionInfoList.isEmpty())
        {        
            
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
                    sql = String.format("SELECT tt.TaxonomyTypeID, tt.TaxonomyTypeName, tt.KingdomID, tn.TaxonNameID, tn.TaxonName, tu.TaxonomicUnitTypeID FROM taxonomytype AS tt " +
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
                        info.setKingdomId((Integer)row[2]);
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
                String sql = "SELECT DISTINCT tt.TaxonomyTypeID, tt.TaxonomyTypeName FROM habitat AS h " +
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
                    
                    sql = "SELECT tt.TaxonomyTypeName, tn.TaxonName, tt.KingdomID, tn.TaxonNameID, tn.TaxonomicUnitTypeID FROM taxonomytype AS tt " +
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
                    colInfo.setKingdomId((Integer)rows.get(0)[2]);
                    colInfo.setTaxonNameId((Integer)rows.get(0)[3]);
                    colInfo.setTaxonName(taxonRootName);
                    colInfo.setTaxonomicUnitTypeID((Integer)rows.get(0)[4]);
                    
                    colInfo.setTaxonNameCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntTaxonName + taxonomyTypeID));
                    
                    colInfo.setSrcHostTaxonCnt(0);
                    
                    collectionInfoList.add(colInfo);
                }
                
                // Do All
                /*String sqlAllTx = "SELECT cot.CollectionObjectTypeID, cot.CollectionObjectTypeName, tt.TaxonomyTypeID, tt.TaxonomyTypeName, tt.KingdomID, tn.TaxonNameID, tn.TaxonName, tn.TaxonomicUnitTypeID " + 
                                  "FROM collectionobjecttype AS cot " +
                                  "INNER JOIN collectiontaxonomytypes as ctt ON cot.CollectionObjectTypeID = ctt.BiologicalObjectTypeID " + 
                                  "INNER JOIN taxonomytype as tt ON ctt.TaxonomyTypeID = tt.TaxonomyTypeID " + 
                                  "INNER JOIN taxonname as tn ON tt.TaxonomyTypeID = tn.TaxonomyTypeID " + 
                                  "WHERE  cot.Category = 'Biological' AND tn.ParentTaxonNameID IS NULL";
                
                log.debug(sqlAllTx);
                Vector<Object[]> rows = BasicSQLUtils.query(oldDBConn, sqlAllTx);
                for (Object[] row : rows)
                {
                    int taxonomyTypeID = (Integer)row[2];
                    if (taxonTypeIdHash.get(taxonomyTypeID) == null)
                    {
                        CollectionInfo info = new CollectionInfo(oldDBConn);
                        
                        info.setColObjTypeId((Integer)row[0]);
                        info.setColObjTypeName((String)row[1]);
                        info.setCatSeriesDefId(null);
                        info.setCatSeriesId(null);
                        info.setCatSeriesName("");
                        info.setCatSeriesPrefix("");
                        info.setCatSeriesRemarks("");
                        info.setCatSeriesLastEditedBy("");
                        
                        info.setTaxonomyTypeId(taxonomyTypeID);
                        info.setTaxonomyTypeName((String)row[3]);
                        info.setKingdomId((Integer)row[4]);
                        info.setTaxonNameId((Integer)row[5]);
                        info.setTaxonName((String)row[6]);
                        
                        info.setTaxonomicUnitTypeID((Integer)row[7]);
                        
                        info.setTaxonNameCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntTaxonName + taxonomyTypeID));
                        
                        Vector<Object> ttNames = BasicSQLUtils.querySingleCol(oldDBConn, hostTaxonID + taxonomyTypeID);
                        if (ttNames != null && ttNames.size() > 0 && ((Long)ttNames.get(0)) > 0)
                        {
                            info.setSrcHostTaxonCnt((Long)ttNames.get(0));
                        } else
                        {
                            info.setSrcHostTaxonCnt(0);
                        }
                        
                        taxonTypeIdHash.put(taxonomyTypeID, true);
                        
                        collectionInfoList.add(info);
                    }
                }*/

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
        assert(placeholderHelper == null);
        return placeholderHelper.getTaxonTreeDef();
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
    public int getKingdomId()
    {
        return kingdomId;
    }

    public boolean isInUse()
    {
        return taxonNameCnt > 0;
    }
    
    /**
     * @param taxonTreeDef the taxonTreeDef to set
     */
    public void setTaxonTreeDef(final TaxonTreeDef taxonTreeDef)
    {
        if (placeholderHelper != null)
        {
            if (taxonTreeDef != null)
            {
                if (!placeholderHelper.getTaxonTreeDef().getId().equals(taxonTreeDef.getId()))
                {
                    assert(placeholderHelper != null);
                }
            }
        } else
        {
            placeholderHelper = new PlaceholderHelper(true, taxonTreeDef);
        }
    }


    /**
     * @param kingdomId the kingdomId to set
     */
    public void setKingdomId(int kingdomId)
    {
        this.kingdomId = kingdomId;
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


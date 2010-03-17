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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
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

    protected boolean      isIncluded    = true;
    protected Integer      colObjTypeId;
    protected String       colObjTypeName;
    
    protected int          colObjCnt = 0;
    
    protected Integer      catSeriesDefId;
    protected Integer      catSeriesId;
    protected String       catSeriesName;
    protected String       catSeriesPrefix;
    protected String       catSeriesRemarks;
    protected String       catSeriesLastEditedBy;
    
    protected Integer      taxonomyTypeId;
    protected String       taxonomyTypeName;
    protected Integer      taxonomicUnitTypeID;
    protected int          kingdomId;
    
    protected TaxonTreeDef taxonTreeDef = null;
    
    protected Integer      taxonNameId;  // root node of the tree
    protected String       taxonName;
    
    protected Integer      disciplineId;
    protected Discipline   discipline = null;
    
    protected Integer      collectionId;
    protected Collection   collection = null;
    
    protected int          taxonNameCnt;
    protected int          colObjDetTaxCnt;
    protected long         srcHostTaxonCnt;
    protected DisciplineType disciplineTypeObj;
    
    protected String             determinationTaxonType = null;
    protected ArrayList<Integer> detTaxonTypeIdList     = new ArrayList<Integer>();
    
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
     * @param oldDBConn
     * @return
     */
    public static Pair<CollectionInfo, DisciplineType> getDisciplineType(final Connection oldDBConn)
    {
        int            max     = 0;
        CollectionInfo colInfo = null;
        int inx = 0;
        for (CollectionInfo ci : getCollectionInfoList(oldDBConn))
        {
            if (ci.getColObjCnt() > max)
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
     * @param oldDBConn
     * @return
     */
    public static Vector<CollectionInfo> getCollectionInfoList(final Connection oldDBConn)
    {
        //collectionInfoList.clear();
        if (collectionInfoList.isEmpty())
        {        
            
            String hostTaxonID = "SELECT Count(taxonname.TaxonomicUnitTypeID) FROM habitat " + 
                                 "Inner Join taxonname ON habitat.HostTaxonID = taxonname.TaxonNameID WHERE taxonname.TaxonomyTypeId = ";
            
            String sql = "SELECT cot.CollectionObjectTypeID, cot.CollectionObjectTypeName, csd.CatalogSeriesDefinitionID, csd.CatalogSeriesID FROM collectionobjecttype cot " +
                         "INNER JOIN catalogseriesdefinition csd on " + 
                         "csd.ObjectTypeId = cot.CollectionObjectTypeId WHERE cot.Category = 'Biological' ORDER BY cot.CollectionObjectTypeID, csd.CatalogSeriesID";
            
            String catSeriesSQL = "SELECT SeriesName, CatalogSeriesPrefix, Remarks, LastEditedBy FROM catalogseries WHERE CatalogSeriesID = ";
            
            
            String sqlTx = "SELECT tt.TaxonomyTypeID, tt.TaxonomyTypeName, tt.KingdomID, tn.TaxonNameID, tn.TaxonName, tn.TaxonomicUnitTypeID " + 
                           "FROM collectionobjecttype AS cot " +
                           "Inner Join collectiontaxonomytypes as ctt ON cot.CollectionObjectTypeID = ctt.BiologicalObjectTypeID " + 
                           "Inner Join taxonomytype as tt ON ctt.TaxonomyTypeID = tt.TaxonomyTypeID " + 
                           "Inner Join taxonname as tn ON tt.TaxonomyTypeID = tn.TaxonomyTypeID " + 
                           "WHERE  cot.Category = 'Biological' AND tn.ParentTaxonNameID IS NULL AND RankID = 0 AND cot.CollectionObjectTypeID = ";
            
            String cntTaxonName = "SELECT COUNT(TaxonNameID) FROM taxonname WHERE TaxonName IS NOT NULL AND taxonname.TaxonomyTypeId = ";
            
            /*String cntColObjForTaxon = "SELECT COUNT(taxonomytype.TaxonomyTypeID) FROM determination "+
                                        "Inner Join taxonname ON determination.TaxonNameID = taxonname.TaxonNameID "+
                                        "Inner Join taxonomytype ON taxonname.TaxonomyTypeID = taxonomytype.TaxonomyTypeID WHERE taxonomytype.TaxonomyTypeID = ";
            */
            //String cntColObj = "SELECT COUNT(CollectionObjectID) FROM collectionobject WHERE CollectionObjectTypeID = ";

            /*sql = " SELECT collectionobject.CollectionObjectTypeID, COUNT(collectionobject.CollectionObjectTypeID), collectionobjecttype.CollectionObjectTypeName " +
                    "FROM collectionobject INNER JOIN collectionobjecttype ON collectionobject.CollectionObjectTypeID = collectionobjecttype.CollectionObjectTypeID " +
                    "GROUP BY collectionobject.CollectionObjectTypeID";
            */
            
            /*String colObjCountPerCatSeriesSQL = "SELECT COUNT(cs.CatalogSeriesID) FROM catalogseries cs " +
            "INNER JOIN collectionobjectcatalog cc ON cs.CatalogSeriesID = cc.CatalogSeriesID " +
            "INNER JOIN collectionobjecttype ct ON cc.CollectionObjectTypeID = ct.CollectionObjectTypeID " +
            "WHERE cc.CollectionObjectTypeID > 9 AND cc.CollectionObjectTypeID < 20 AND cs.CatalogSeriesID = %d " +
            "GROUP BY cs.CatalogSeriesID, cs.SeriesName, ct.CollectionObjectTypeID, ct.CollectionObjectTypeName";
            */
            String colObjCountPerCatSeriesSQL = "SELECT COUNT(cc.CatalogSeriesID) " + //, cc.CatalogSeriesID, cs.SeriesName " +
                                                "FROM collectionobjectcatalog cc INNER JOIN catalogseries cs ON cc.CatalogSeriesID = cs.CatalogSeriesID " +
                                                "WHERE cs.CatalogSeriesID = %d GROUP BY cs.CatalogSeriesID";
            
            String colObjDetCountPerCatSeriesSQL = "SELECT COUNT(cc.CatalogSeriesID) " +
                                                    "FROM determination d INNER JOIN collectionobject co ON d.BiologicalObjectID = co.CollectionObjectID " +
                                                    "INNER JOIN collectionobjectcatalog cc ON co.CollectionObjectID = cc.CollectionObjectCatalogID " +
                                                    "WHERE cc.CatalogSeriesID = %d AND d.TaxonNameID IS NOT NULL GROUP BY cc.CatalogSeriesID";
            
            sql = "SELECT cot.CollectionObjectTypeID, cot.CollectionObjectTypeName, csd.CatalogSeriesDefinitionID, csd.CatalogSeriesID FROM collectionobjecttype cot " +
                  "INNER JOIN catalogseriesdefinition csd on csd.ObjectTypeId = cot.CollectionObjectTypeId " +
                  "WHERE cot.Category = 'Biological' ORDER BY cot.CollectionObjectTypeID, csd.CatalogSeriesID";
            
            Statement stmt = null;
            
            try
            {
                log.debug(sql);
                
                HashMap<Integer, Boolean> taxonTypeIdHash = new HashMap<Integer, Boolean>();
                
                stmt         = oldDBConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    CollectionInfo info = new CollectionInfo(oldDBConn);
                    
                    System.err.println("CI: " + rs.getInt(1));
                    
                    info.setColObjTypeId(rs.getInt(1));
                    info.setColObjTypeName(rs.getString(2));
                    info.setCatSeriesDefId(rs.getInt(3));
                    info.setCatSeriesId(rs.getInt(4));
                    
                    sql = String.format(colObjCountPerCatSeriesSQL, info.getCatSeriesId());
                    log.debug(sql);
                    int colObjCnt = BasicSQLUtils.getCountAsInt(oldDBConn, sql);
                    info.setColObjCnt(colObjCnt);
                    
                    sql = String.format(colObjDetCountPerCatSeriesSQL, info.getCatSeriesId());
                    log.debug(sql);
                    info.setColObjDetTaxCnt(BasicSQLUtils.getCountAsInt(oldDBConn, sql));
                    
                    String s = catSeriesSQL + info.getCatSeriesId();
                    log.debug(s);
                    Vector<Object[]> rows = BasicSQLUtils.query(oldDBConn, s);
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
                    
                    // This represents a mapping from what would be the Discipline (Biological Object Type) to the Taxonomic Root
                    sql = String.format("SELECT tt.TaxonomyTypeID, tt.TaxonomyTypeName, tt.KingdomID, tn.TaxonNameID, tn.TaxonName, tu.TaxonomicUnitTypeID FROM taxonomytype AS tt " +
                                        "Inner Join taxonomicunittype AS tu ON tt.TaxonomyTypeID = tu.TaxonomyTypeID " +
                                        "Inner Join taxonname AS tn ON tu.TaxonomyTypeID = tn.TaxonomyTypeID " +
                                        "Inner Join collectiontaxonomytypes AS ct ON tn.TaxonomyTypeID = ct.TaxonomyTypeID " +
                                        "WHERE tu.RankID =  0 AND tn.RankID =  0 AND ct.BiologicalObjectTypeID = %d " +
                                        "ORDER BY ct.BiologicalObjectTypeID ASC", info.getColObjTypeId());
                    
                    String detSQLStr = "SELECT ct.TaxonomyTypeID, (select distinct relatedsubtypevalues FROM usysmetacontrol c " +
                    	               "LEFT JOIN usysmetafieldsetsubtype fst ON fst.fieldsetsubtypeid = c.fieldsetsubtypeid " +
                    	               "WHERE objectid = 10290 AND ct.taxonomytypeid = c.relatedsubtypevalues) AS DeterminationTaxonType " +
                    	               "FROM collectiontaxonomytypes ct WHERE ct.biologicalobjecttypeid = " + info.getColObjTypeId();
                    
                    String txNameSQL = "SELECT TaxonomyTypeName FROM taxonomytype WHERE TaxonomyTypeID = ";
                    
                    log.debug(detSQLStr);
                    Vector<Object[]> detRows = BasicSQLUtils.query(oldDBConn, detSQLStr);
                    
                    
                    for (Object[] row : detRows)
                    {
                        Integer txnTypeId    = (Integer)row[0];
                        String  detTxnTypes  = (String)row[1];
                        
                        if (StringUtils.isNotEmpty(detTxnTypes))
                        {
                            if (StringUtils.contains(detTxnTypes, ','))
                            {
                                StringBuilder sb = new StringBuilder();
                                String[] toks = StringUtils.split(detTxnTypes, ',');
                                
                                String dtName = BasicSQLUtils.querySingleObj(oldDBConn, txNameSQL + txnTypeId);
                                sb.append(String.format("Warning - There are %d DeterminationTaxonTypes for TaxonObjectType %d (%s) they are:\n",  toks.length, txnTypeId, dtName));
                                for (String id : toks)
                                {
                                    String name = BasicSQLUtils.querySingleObj(oldDBConn, txNameSQL + id);
                                    sb.append(id);
                                    sb.append(" - ");
                                    sb.append(name);
                                    sb.append("\n");
                                }
                                sb.append("\nThis database will need to be fixed by hand before it can be converted.");
                                UIRegistry.showError(sb.toString());
                                
                                oldDBConn.close();
                                
                                System.exit(0);
                                
                            } else if (StringUtils.isNumeric(detTxnTypes))
                            {
                                Integer txnType = Integer.parseInt(detTxnTypes);
                                if (!txnType.equals(txnTypeId))
                                {
                                    String tName  = BasicSQLUtils.querySingleObj(oldDBConn, txNameSQL + txnType);
                                    String dtName = BasicSQLUtils.querySingleObj(oldDBConn, txNameSQL + txnTypeId);
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(String.format("Warning - The TaxonObjectType %d (%s) in the DeterminationTaxonTypes field\ndoesn't match the actual TaxonObjectType %d (%s)",  txnType, tName, txnTypeId, dtName));
                                    UIRegistry.showError(sb.toString());
                                    System.exit(0);
                                }
                            }
                        }
                    }
                    
                    /*info.setDeterminationTaxonType(detTxnTypeStr);
                    for (Integer id : info.getDetTaxonTypeIdList())
                    {
                        log.debug("ID: "+id);
                    }*/
                    
                    log.debug(sql);
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
                        
                        log.debug("TaxonomyTypeName: "+ info.getTaxonomyTypeName()+"  TaxonName: "+info.getTaxonName()+"  TaxonomyTypeId: "+info.getTaxonomyTypeId());
                        
                        s = hostTaxonID + taxonomyTypeID;
                        log.debug(s);
                        Vector<Object> ttNames = BasicSQLUtils.querySingleCol(oldDBConn, s);
                        if (ttNames != null && ttNames.size() > 0 && ((Long)ttNames.get(0)) > 0)
                        {
                            info.setSrcHostTaxonCnt((Long)ttNames.get(0));
                        } else
                        {
                            info.setSrcHostTaxonCnt(0);
                        }
                        
                        taxonTypeIdHash.put(taxonomyTypeID, true);
                        
                    } else
                    {
                        log.error("Error getting CollectionInfo for CollectionObjectTypeID: " + rs.getInt(1));
                    }
                    
                    collectionInfoList.add(info);
                    System.out.println(info.toString());
                }
                rs.close();
                
                // Do All
                /*String sqlAllTx = "SELECT cot.CollectionObjectTypeID, cot.CollectionObjectTypeName, tt.TaxonomyTypeID, tt.TaxonomyTypeName, tt.KingdomID, tn.TaxonNameID, tn.TaxonName, tn.TaxonomicUnitTypeID " + 
                                  "FROM collectionobjecttype AS cot " +
                                  "Inner Join collectiontaxonomytypes as ctt ON cot.CollectionObjectTypeID = ctt.BiologicalObjectTypeID " + 
                                  "Inner Join taxonomytype as tt ON ctt.TaxonomyTypeID = tt.TaxonomyTypeID " + 
                                  "Inner Join taxonname as tn ON tt.TaxonomyTypeID = tn.TaxonomyTypeID " + 
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
     * @return932413666
     */
    public static Vector<CollectionInfo> getFilteredCollectionInfoList()
    {
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
    public void setTaxonTreeDef(TaxonTreeDef taxonTreeDef)
    {
        this.taxonTreeDef = taxonTreeDef;
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


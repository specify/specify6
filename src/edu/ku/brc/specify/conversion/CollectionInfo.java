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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 9, 2009
 *
 */
public class CollectionInfo
{
    protected static final Logger           log         = Logger.getLogger(CollectionInfo.class);
    
    protected static Vector<CollectionInfo> collectionInfoList = new Vector<CollectionInfo>();
    protected static String[]               headers            = null;

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
    
    protected int          taxonomyTypeId;
    protected String       taxonomyTypeName;
    protected int          kingdomId;
    protected TaxonTreeDef taxonTreeDef = null;
    protected Taxon        taxonRoot    = null;
    
    protected int          taxonNameId;  // root node of the tree
    protected String       taxonName;
    
    protected Integer      disciplineId;
    protected Discipline   discipline = null;
    
    protected Integer      collectionId;
    protected Collection   collection = null;
    
    protected int          taxonNameCnt;
    protected int          colObjDetTaxCnt;
    protected long         srcHostTaxonCnt;
    
    
    
    /**
     * @param taxonomyTypeId
     * @param kingdomId
     * @param colObjTypeName
     * @param taxonNameId
     */
    public CollectionInfo()
    {
        super();
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
            
            String hostTaxonID = "SELECT Count(taxonname.TaxonomicUnitTypeID), taxonname.TaxonName FROM habitat " + 
                                 "Inner Join taxonname ON habitat.HostTaxonID = taxonname.TaxonNameID WHERE taxonname.TaxonomyTypeId = ";
            
            String sql = "SELECT cot.CollectionObjectTypeID, cot.CollectionObjectTypeName, csd.CatalogSeriesDefinitionID, csd.CatalogSeriesID " + 
                         "FROM collectionobjecttype cot INNER JOIN catalogseriesdefinition csd on " + 
                         "csd.ObjectTypeId = cot.CollectionObjectTypeId WHERE cot.Category =  'Biological'";
            
            String catSeriesSQL = "SELECT SeriesName, CatalogSeriesPrefix, Remarks, LastEditedBy FROM catalogseries WHERE CatalogSeriesID = ";
            
            
            String sqlTx = "SELECT tt.TaxonomyTypeID, tt.TaxonomyTypeName, tt.KingdomID, tn.TaxonNameID, tn.TaxonName " + 
                           "FROM collectionobjecttype AS cot " +
                           "Inner Join collectiontaxonomytypes as ctt ON cot.CollectionObjectTypeID = ctt.BiologicalObjectTypeID " + 
                           "Inner Join taxonomytype as tt ON ctt.TaxonomyTypeID = tt.TaxonomyTypeID " + 
                           "Inner Join taxonname as tn ON tt.TaxonomyTypeID = tn.TaxonomyTypeID " + 
                           "WHERE  cot.Category = 'Biological' AND tn.ParentTaxonNameID IS NULL AND RankID = 0 AND cot.CollectionObjectTypeID = ";
            
            String cntTaxonName = "SELECT COUNT(TaxonNameID) FROM taxonname WHERE TaxonName IS NOT NULL AND taxonname.TaxonomyTypeId = ";
            
            String cntColObjForTaxon = "SELECT COUNT(taxonomytype.TaxonomyTypeID) FROM  determination "+
            "Inner Join taxonname ON determination.TaxonNameID = taxonname.TaxonNameID "+
            "Inner Join taxonomytype ON taxonname.TaxonomyTypeID = taxonomytype.TaxonomyTypeID WHERE taxonomytype.TaxonomyTypeID = ";

            String cntColObj = "SELECT COUNT(CollectionObjectID) FROM collectionobject WHERE CollectionObjectTypeID = ";

            
            Statement stmt = null;
            
            try
            {
                log.debug(sql);
                
                HashMap<Integer, Boolean> taxonTypeIdHash = new HashMap<Integer, Boolean>();
                
                stmt         = oldDBConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    CollectionInfo info = new CollectionInfo();
                    
                    info.setColObjTypeId(rs.getInt(1));
                    info.setColObjTypeName(rs.getString(2));
                    info.setCatSeriesDefId(rs.getInt(3));
                    info.setCatSeriesId(rs.getInt(4));
                    
                    String s = catSeriesSQL + info.getCatSeriesId();
                    log.debug(s);
                    Vector<Object[]> rows = BasicSQLUtils.query(oldDBConn, s);
                    if (rows != null)
                    {
                        Object[] row = rows.get(0);
                        
                        info.setCatSeriesName((String)row[0]);
                        info.setCatSeriesPrefix((String)row[1]);
                        info.setCatSeriesRemarks((String)row[2]);
                        info.setCatSeriesLastEditedBy((String)row[3]);
                        
                    } else
                    {
                        log.error("Error getting CollectionInfo for CollectionObjectTypeID: " + rs.getInt(1));
                    }
                    
                    s = sqlTx + rs.getInt(1);
                    log.debug(s);
                    rows = BasicSQLUtils.query(oldDBConn, s);
                    if (rows != null)
                    {
                        Object[] row = rows.get(0);
                        
                        int taxonomyTypeID = (Integer)row[0];
                        
                        info.setTaxonomyTypeId(taxonomyTypeID);
                        info.setTaxonomyTypeName((String)row[1]);
                        info.setKingdomId((Integer)row[2]);
                        info.setTaxonNameId((Integer)row[3]);
                        info.setTaxonName((String)row[4]);
                        
                        info.setTaxonNameCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntTaxonName + taxonomyTypeID));
                        info.setColObjDetTaxCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntColObjForTaxon + taxonomyTypeID));
                        info.setColObjCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntColObj + info.getColObjTypeId()));
                        
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
                }
                rs.close();
                
                // Do All
                String sqlAllTx = "SELECT cot.CollectionObjectTypeID, cot.CollectionObjectTypeName, tt.TaxonomyTypeID, tt.TaxonomyTypeName, tt.KingdomID, tn.TaxonNameID, tn.TaxonName " + 
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
                        CollectionInfo info = new CollectionInfo();
                        
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
                        
                        info.setTaxonNameCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntTaxonName + taxonomyTypeID));
                        info.setColObjDetTaxCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntColObjForTaxon + taxonomyTypeID));
                        info.setColObjCnt(BasicSQLUtils.getCountAsInt(oldDBConn, cntColObj + info.getColObjTypeId()));
                        
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
                }
                
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
        
        return collectionInfoList;
    }
    
    /**
     * @param col
     * @return
     */
    public boolean canEdit(final int col)
    {
        switch (col)
        {
            case  0 : return true; // isIncluded;
            case  1 : return false; // colObjTypeName;
            case  2 : return true; // catSeriesName;
            case  3 : return true; // catSeriesPrefix;
            case  4 : return true; // catSeriesRemarks;
            case  5 : return false; // catSeriesLastEditedBy;
            case  6 : return false; // taxonomyTypeName;
            case  7 : return false; // kingdomId;
            case  8 : return false; // taxonName;
            case  9 : return false; // taxonNameCnt;
            case 10 : return false; // colObjDetTaxCnt;
            case 11 : return false; // srcHostTaxonCnt;
            case 12 : return false; // colObjCnt;
        }
        return false; 
    }
    
    /**
     * @param col
     * @return
     */
    public Object getValueAt(final int col)
    {
        switch (col)
        {
            case  0 : return isIncluded;
            case  1 : return colObjTypeName;
            case  2 : return catSeriesName;
            case  3 : return catSeriesPrefix;
            case  4 : return catSeriesRemarks;
            case  5 : return catSeriesLastEditedBy;
            case  6 : return taxonomyTypeName;
            case  7 : return kingdomId;
            case  8 : return taxonName;
            case  9 : return taxonNameCnt;
            case 10 : return colObjDetTaxCnt;
            case 11 : return srcHostTaxonCnt;
            case 12 : return colObjCnt;
        }
        return ""; 
    }
    
    /**
     * @return
     */
    public static String[] getHeaders()
    {
        if (headers == null)
        {
            headers = new String[] {
                    "Is Included",
		            "Coll Obj Type Name",
                    "Cat Series Name", 
                    "Cat Series Prefix", 
                    "Cat Series Remarks", 
                    "Last Edited By", 
                    "Taxonomy Type Name", 
                    "Kingdom Id", 
                    "Taxon Name (Root)", 
                    "# of Taxon", 
                    "# of Coll Objs",
                    "Src Host Taxon",
                    "Col Obj Count"};
        }
        return headers;
    }
    
    /**
     * @param columnIndex
     * @return
     */
    private static Class<?> getColumnClassForModel(int columnIndex)
    {
        switch (columnIndex)
        {
            case  0 : return Boolean.class;
            case  1 : return String.class;
            case  2 : return String.class;
            case  3 : return String.class;
            case  4 : return String.class;
            case  5 : return String.class;
            case  6 : return String.class;
            case  7 : return Integer.class;
            case  8 : return String.class;
            case  9 : return Integer.class;
            case 10 : return Integer.class;
            case 11 : return Long.class;
            case 12 : return Integer.class;
        }
        return String.class;
    }

    /**
     * @return
     */
    public static DefaultTableModel getCollectionInfoTableModel()
    {
        ColInfoTableModel model = (new CollectionInfo()).new ColInfoTableModel();
        
        return model;
    }
    
    //----------------------------------------------------------------
    class ColInfoTableModel extends DefaultTableModel
    {
        /**
         * 
         */
        public ColInfoTableModel()
        {
            super();
            getHeaders();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return headers == null ? 0 : headers.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return headers[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return collectionInfoList != null ? collectionInfoList.size() : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return getColumnClassForModel(columnIndex);
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            CollectionInfo ci = collectionInfoList.get(row);
            return ci.getValueAt(column);
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int row, int column)
        {
            
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            CollectionInfo ci = collectionInfoList.get(row);
            return ci.canEdit(column);
        }
        
    };
    
    //----- Getters and Setters ----------------------------
    
    
    /**
     * @return the taxonNameCnt
     */
    public int getTaxonNameCnt()
    {
        return taxonNameCnt;
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
     * @return the taxonRoot
     */
    public Taxon getTaxonRoot()
    {
        return taxonRoot;
    }


    /**
     * @param taxonRoot the taxonRoot to set
     */
    public void setTaxonRoot(Taxon taxonRoot)
    {
        this.taxonRoot = taxonRoot;
    }


    /**
     * @return the taxonTreeDef
     */
    public TaxonTreeDef getTaxonTreeDef()
    {
        return taxonTreeDef;
    }


    /**
     * @param taxonTreeDef the taxonTreeDef to set
     */
    public void setTaxonTreeDef(TaxonTreeDef taxonTreeDef)
    {
        this.taxonTreeDef = taxonTreeDef;
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
    public int getTaxonomyTypeId()
    {
        return taxonomyTypeId;
    }


    /**
     * @param taxonomyTypeId the taxonomyTypeId to set
     */
    public void setTaxonomyTypeId(int taxonomyTypeId)
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
    public void setTaxonomyTypeName(String taxonomyTypeName)
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


    /**
     * @param kingdomId the kingdomId to set
     */
    public void setKingdomId(int kingdomId)
    {
        this.kingdomId = kingdomId;
    }


    /**
     * @return the taxonNameId
     */
    public int getTaxonNameId()
    {
        return taxonNameId;
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
     * @param collection the collection to set
     */
    public void setCollection(Collection collection)
    {
        this.collection = collection;
    }


}


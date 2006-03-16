/*
 * Filename:    $RCSfile: GenericDBConversion.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.3 $
 * Date:        $Date: 2005/10/20 12:53:02 $
 *
 * This library is free software; you can redistribute it and/or
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

import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.buildSelectFieldList;
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.copyTable;
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.createFieldNameMap;
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.deleteAllRecordsFromTable;
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.getFieldMetaDataFromSchema;
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.getFieldNamesFromSchema;
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.getStrValue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AttributeIFace;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.PrepType;

import edu.ku.brc.specify.datamodel.TaxonTreeDef;

import edu.ku.brc.specify.datamodel.User;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.Encryption;
import edu.ku.brc.specify.helpers.UIHelper;

/**
 * This class is used for copying over the and creating all the tables that are not specify to any one collection. 
 * This assumes that the "static" data members of DBConnection have been set up with the new Database's 
 * driver, name, user and password. This is created with the old Database's driver, name, user and password.
 */
public class GenericDBConversion 
{
    protected static Log log = LogFactory.getLog(GenericDBConversion.class);

    protected static StringBuilder strBuf   = new StringBuilder("");
    protected static Calendar     calendar  = Calendar.getInstance();
    
    private static final int GEO_ROOT_RANK  = 0;
    private static final int CONTINENT_RANK = 100;
    private static final int COUNTRY_RANK   = 200;
    private static final int STATE_RANK     = 300;
    private static final int COUNTY_RANK    = 400;
    
    protected String oldDriver   = "";
    protected String oldDBName   = "";
    protected String oldUserName = "";
    protected String oldPassword = "";
        

    /**
     * Default Constructor
     *
     */
    public GenericDBConversion()
    {

    }

    /**
     * "Old" means the database you want to copy "from"
     * @param oldDriver old driver
     * @param oldDBName old database name
     * @param oldUserName old user name
     * @param oldPassword old password
     */
    public GenericDBConversion(final String oldDriver, 
                               final String oldDBName,
                               final String oldUserName, 
                               final String oldPassword)
    {
        this.oldDriver    = oldDriver;
        this.oldDBName    = oldDBName;
        this.oldUserName  = oldUserName;
        this.oldPassword  = oldPassword;
    }

    /**
     * Removes all the records from every table in the new database and then copies over 
     * all the tables that have few if any changes to their schema
     */
    public void copyTables()
    {
        
        //cleanAllTables(); // from DBCOnnection which is the new DB
       
        DBConnection oldDB = DBConnection.createInstance(oldDriver, oldDBName, oldUserName, oldPassword);
       
        String[] tablesToMoveOver = {
                                    "accession",
                                    "accessionagents",
                                    "accessionauthorizations",
                                    "address",
                                    "agent",
                                    "agentaddress",
                                    "authors",
                                    "borrow",
                                    "borrowagents",
                                    "borrowmaterial",
                                    "borrowreturnmaterial",
                                    "borrowshipments",
                                    "catalogseries",
                                    "collectingevent",
                                    "collectionobjectcitation",
                                    "collectors",
                                    "deaccession",
                                    "deaccessionagents",
                                    "deaccessioncollectionobject",
                                    "determination",
                                    "determinationcitation",
                                    "exchangein",
                                    "exchangeout",
                                    "grouppersons",
                                    "journal",
                                    "loan",
                                    "loanagents",
                                    "loanphysicalobject",
                                    "loanreturnphysicalobject",
                                    //"locality",
                                    "localitycitation",
                                    "observation",
                                    "otheridentifier",
                                    "permit",
                                    "project",
                                    "projectcollectionobjects",
                                    "referencework",
                                    "shipment",
                                    "stratigraphy",
                                    "taxoncitation",
       };
       
       Map<String, Map<String, String>> tableMaps = new Hashtable<String, Map<String, String>>();
       tableMaps.put("authors", createFieldNameMap(new String[] {"OrderNumber", "Order1"}));
       tableMaps.put("borrowreturnmaterial", createFieldNameMap(new String[] {"ReturnedDate", "Date1"}));
       tableMaps.put("collectors", createFieldNameMap(new String[] {"OrderNumber", "Order1"}));
       tableMaps.put("determination", createFieldNameMap(new String[] {"CollectionObjectID", "BiologicalObjectID", "IsCurrent", "Current1", "DeterminationDate", "Date1", "TaxonID", "TaxonNameID"}));
       tableMaps.put("loanreturnphysicalobject", createFieldNameMap(new String[] {"DateField", "Date1"}));
       tableMaps.put("referencework", createFieldNameMap(new String[] {"WorkDate", "Date1"}));
       tableMaps.put("stratigraphy", createFieldNameMap(new String[] {"LithoGroup", "Group1"}));
       tableMaps.put("taxoncitation", createFieldNameMap(new String[] {"TaxonID", "TaxonNameID"}));
       
       Map<String, Map<String, String>> tableDateMaps = new Hashtable<String, Map<String, String>>();
       tableDateMaps.put("collectingevent", createFieldNameMap(new String[] {"TaxonID", "TaxonNameID"}));
      
       //tableMaps.put("locality", createFieldNameMap(new String[] {"NationalParkName", "", "ParentID", "TaxonParentID"}));
      
       BasicSQLUtils.setShowMappingError(false);
       for (String tableName : tablesToMoveOver)
       {
           if (!copyTable(oldDB.getConnectionToDB(), DBConnection.getConnection(), tableName, tableMaps.get(tableName), null))
           {
               log.error("Table ["+tableName+"] didn't copy correctly.");
               break;
           }
       }
       BasicSQLUtils.setShowMappingError(true);
    } 
    
    /**
     * Creates a map from a String Preparation Type to its ID in the table
     * @return map of name to PrepType
     */
    public Map<String, PrepType> createPreparationTypesFromUSys()
    {
        deleteAllRecordsFromTable("preptype");
        
        Hashtable<String, PrepType> prepTypeMapper = new Hashtable<String, PrepType>();
        
        DBConnection oldDB     = DBConnection.createInstance(oldDriver, oldDBName, oldUserName, oldPassword);
        Connection   oldDBConn = oldDB.getConnectionToDB();
        try 
        {
            /*
            +-----------------------+-------------+------+-----+---------+-------+
            | Field                 | Type        | Null | Key | Default | Extra |
            +-----------------------+-------------+------+-----+---------+-------+
            | USYSCollObjPrepMethID | int(11)     |      | PRI | 0       |       |
            | InterfaceID           | int(11)     | YES  |     | NULL    |       |
            | FieldSetSubTypeID     | int(11)     | YES  |     | NULL    |       |
            | PreparationMethod     | varchar(50) | YES  |     | NULL    |       |
            +-----------------------+-------------+------+-----+---------+-------+
             */
            Statement stmt   = oldDBConn.createStatement();
            String    sqlStr = "select USYSCollObjPrepMethID, InterfaceID, FieldSetSubTypeID, PreparationMethod from usyscollobjprepmeth";
            
            log.info(sqlStr);
            
            boolean foundMisc = false;
            
            boolean doDebug   = false;
            ResultSet rs      = stmt.executeQuery(sqlStr);
            int       count   = 0;
            while (rs.next()) 
            {
                if (rs.getObject(2) != null && rs.getObject(3) != null)
                {
                    String name = rs.getString(4);
                    PrepType prepType = AttrUtils.loadPrepType(name);
                    prepTypeMapper.put(name.toLowerCase(), prepType);
                    if (name.equalsIgnoreCase("misc"))
                    {
                        foundMisc = true;
                    }
                }
                count++;
            }
            
            if (!foundMisc)
            {
                String name = "Misc";
                PrepType prepType = AttrUtils.loadPrepType(name);
                prepTypeMapper.put(name.toLowerCase(), prepType);
                count++;
            }
            log.info("Processed PrepType "+count+" records.");

            
        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            return prepTypeMapper;
        } 
 
        return prepTypeMapper;
   } 
    
    /**
     * @param name
     * @return
     */
    protected String convertColumnName(final String name)
    {
        StringBuilder nameStr = new StringBuilder();
        int cnt = 0;
        for (char c : name.toCharArray())
        {
            if (cnt == 0)
            {
                nameStr.append(name.toUpperCase().charAt(0));
                cnt++;
                
            } else if (c < 'a')
            {
                nameStr.append(' ');
                nameStr.append(c);

            } else
            {
                nameStr.append(c);
            }
        }
        return nameStr.toString();
    } 
    
    /**
     * Returns the proper value depending on the type
     * @param value the data value from the database object
     * @param type the defined type
     * @param attr the data value from the database object
     * @return the data object for the value
     */
    protected Object getData(final AttributeIFace.FieldType type, AttributeIFace attr)
    {
        if (type == AttributeIFace.FieldType.BooleanType)
        { 
            return attr.getDblValue() != 0.0;
            
        } else if (type == AttributeIFace.FieldType.FloatType)
        {
            return attr.getDblValue().floatValue();
            
        } else if (type == AttributeIFace.FieldType.DoubleType)
        {
            return attr.getDblValue();
            
        } else if (type == AttributeIFace.FieldType.IntegerType)
        {
            return attr.getDblValue().intValue();
            
        } else
        {
            return attr.getStrValue();  
        }  
    } 
    
    /**
     * Returns a converted value from the old schema to the new schema
     * @param rs the resultset
     * @param index the index of the column in the resultset
     * @param type the defined type for the new schema
     * @param metaData the metat data describing the old schema column
     * @return the new data object
     */
    protected Object getData(final ResultSet                   rs,
                             final int                         index,
                             final AttributeIFace.FieldType    type, 
                             final BasicSQLUtils.FieldMetaData metaData)
    {
        // Note: we need to check the old schema once again because the "type" may have been mapped
        // so now we must map the actual value
        
        AttributeIFace.FieldType oldType = getDataType(metaData.getName(), metaData.getType());
        
        try
        {
            Object value = rs.getObject(index);
            
            if (type == AttributeIFace.FieldType.BooleanType)
            { 
                if (value == null)
                {
                   return false;
                   
                } else if (oldType == AttributeIFace.FieldType.IntegerType)
                {
                    return rs.getInt(index) != 0;
                    
                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    return rs.getFloat(index) != 0.0f;
                    
                } else if (oldType == AttributeIFace.FieldType.DoubleType)
                {
                    return rs.getDouble(index) != 0.0;
                    
                } else if (oldType == AttributeIFace.FieldType.StringType)
                {
                    return rs.getString(index).equalsIgnoreCase("true");
                }
                log.error("Error maping from schema["+metaData.getType()+"] to ["+type.toString()+"]");
                return false;
                
            } else if (type == AttributeIFace.FieldType.FloatType)
            {
                if (value == null)
                {
                   return 0.0f;
                   
                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    return rs.getFloat(index);
                    
                } else if (oldType == AttributeIFace.FieldType.DoubleType)
                {
                    return rs.getFloat(index);
                }
                log.error("Error maping from schema["+metaData.getType()+"] to ["+type.toString()+"]");
                return 0.0f;
                
            } else if (type == AttributeIFace.FieldType.DoubleType)
            {
                if (value == null)
                {
                   return 0.0;
                   
                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    return rs.getDouble(index);
                    
                } else if (oldType == AttributeIFace.FieldType.DoubleType)
                {
                    return rs.getDouble(index);
                }
                log.error("Error maping from schema["+metaData.getType()+"] to ["+type.toString()+"]");
                return 0.0; 
                
            } else if (type == AttributeIFace.FieldType.IntegerType)
            {
                if (value == null)
                {
                   return 0;
                   
                } else if (oldType == AttributeIFace.FieldType.IntegerType)
                {
                    return rs.getInt(index) != 0;
                }
                log.error("Error maping from schema["+metaData.getType()+"] to ["+type.toString()+"]");
                return 0; 
                
            } else
            {
                return rs.getString(index); 
            }
        }
        catch (SQLException ex)
        {
            log.error("Error maping from schema["+metaData.getType()+"] to ["+type.toString()+"]");
            log.error(ex);
        }
        return "";
    } 

    /**
     * Sets a converted value from the old schema to the new schema into the CollectionObjectAttr object
     * @param rs the resultset
     * @param index the index of the column in the resultset
     * @param type the defined type for the new schema
     * @param metaData the metat data describing the old schema column
     * @param colObjAttr the object the data is set into
     * @return the new data object
     */
    protected void setData(final ResultSet                   rs,
                           final int                         index,
                           final AttributeIFace.FieldType    type, 
                           final BasicSQLUtils.FieldMetaData metaData,
                           final CollectionObjectAttr        colObjAttr)
    {
        // Note: we need to check the old schema once again because the "type" may have been mapped
        // so now we must map the actual value
        
        AttributeIFace.FieldType oldType = getDataType(metaData.getName(), metaData.getType());
        
        try
        {
            Object value = rs.getObject(index);
            
            if (type == AttributeIFace.FieldType.BooleanType)
            { 
                if (value == null)
                {
                    colObjAttr.setDblValue(0.0); //false
                   
                } else if (oldType == AttributeIFace.FieldType.IntegerType)
                {
                    colObjAttr.setDblValue(rs.getInt(index) != 0 ? 1.0 : 0.0);
                    
                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    colObjAttr.setDblValue(rs.getFloat(index) != 0.0f ? 1.0 : 0.0);
                    
                } else if (oldType == AttributeIFace.FieldType.DoubleType)
                {
                    colObjAttr.setDblValue(rs.getDouble(index) != 0.0 ? 1.0 : 0.0);
                    
                } else if (oldType == AttributeIFace.FieldType.StringType)
                {
                    colObjAttr.setDblValue(rs.getString(index).equalsIgnoreCase("true") ? 1.0 : 0.0);
                } else
                {
                    log.error("Error maping from schema["+metaData.getType()+"] to ["+type.toString()+"]");
                }
                
            } else if (type == AttributeIFace.FieldType.IntegerType || 
                       type == AttributeIFace.FieldType.DoubleType ||
                       type == AttributeIFace.FieldType.FloatType)
            {
                if (value == null)
                {
                    colObjAttr.setDblValue(0.0);
                   
                } else if (oldType == AttributeIFace.FieldType.IntegerType)
                {
                    colObjAttr.setDblValue((double)rs.getInt(index));
                    
                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    colObjAttr.setDblValue((double)rs.getFloat(index));
                    
                } else if (oldType == AttributeIFace.FieldType.DoubleType)
                {
                    colObjAttr.setDblValue(rs.getDouble(index));
                    
                } else
                {
                    log.error("Error maping from schema["+metaData.getType()+"] to ["+type.toString()+"]");
                }
                
            } else
            {
                colObjAttr.setStrValue(rs.getString(index));
            }
        }
        catch (SQLException ex)
        {
            log.error("Error maping from schema["+metaData.getType()+"] to ["+type.toString()+"]");
            log.error(ex);
        }
    } 
    

    /**
     * Figure out the data type given the database column's field's name and data type
     * @param name the column name
     * @param type the database schema type for the column
     * @return the AttributeIFace.Type for the column
     */
    protected AttributeIFace.FieldType getDataType(final String name, final String type)
    {
        if (name.startsWith("YesNo"))
        {
            return AttributeIFace.FieldType.BooleanType;
            
        } else if (name.equalsIgnoreCase("remarks"))
        {
            return AttributeIFace.FieldType.MemoType;
            
        } else if (type.equalsIgnoreCase("float"))
        {
            return AttributeIFace.FieldType.FloatType;
            
        } else if (type.equalsIgnoreCase("double"))
        {
            return AttributeIFace.FieldType.DoubleType;
            
        } else if (type.startsWith("varchar") || type.startsWith("text") || type.startsWith("longtext"))
        {
            return AttributeIFace.FieldType.StringType;
            
        } else
        {
            return AttributeIFace.FieldType.IntegerType;
        }
    }
    
    /**
     * Convert all the biological attributes to Collection Object Attributes. 
     * Each old record may end up being multiple records in the new schema. This will first figure out 
     * which columns in the old schema were used and olnly map those columns to the new database.<br><br>
     * It also will use the old name if there is not mapping for it. The old name is converted from lower/upper case to
     * be space separated where each part of the name starts with a capital letter.
     * 
     * @param colObjDef the Collection Object Definition
     * @param colToNameMap a mape for old names to new names
     * @param typeMap a map for changing the type of the data (meaning an old value may be a boolean stored in a float)
     * @return true for success
     */
    public boolean convertBiologicalAttrs(CollectionObjDef colObjDef, final Map<String, String> colToNameMap, final Map<String, Short> typeMap)
    {
        AttributeIFace.FieldType[] attrTypes = {AttributeIFace.FieldType.IntegerType, AttributeIFace.FieldType.FloatType,
                                                AttributeIFace.FieldType.DoubleType, AttributeIFace.FieldType.BooleanType,AttributeIFace.FieldType.StringType,
                                                AttributeIFace.FieldType.MemoType};
        
        Session session = HibernateUtil.getCurrentSession();
        
        Connection newDBConn = DBConnection.getConnection();
        deleteAllRecordsFromTable(newDBConn, "collectionobjectattr");
        deleteAllRecordsFromTable(newDBConn, "attributedef");
        
        DBConnection oldDB     = DBConnection.createInstance(oldDriver, oldDBName, oldUserName, oldPassword);
        Connection   oldDBConn = oldDB.getConnectionToDB();
        try 
        {
            Statement stmt = oldDBConn.createStatement();
            
            // grab the field and their type from the old schema
            List<BasicSQLUtils.FieldMetaData>        oldFieldMetaData    = new ArrayList<BasicSQLUtils.FieldMetaData>();
            Map<String, BasicSQLUtils.FieldMetaData> oldFieldMetaDataMap = new Hashtable<String, BasicSQLUtils.FieldMetaData>();
            getFieldMetaDataFromSchema(oldDBConn, "biologicalobjectattributes", oldFieldMetaData);
             
            // create maps to figure which columns where used
            List<String>              columnsInUse = new ArrayList<String>();
            Map<String, AttributeDef> attrDefs     = new Hashtable<String, AttributeDef>();
            
            List<Integer>             counts       = new ArrayList<Integer>();
            
            int totalCount = 0;
            
            for (BasicSQLUtils.FieldMetaData md : oldFieldMetaData)
            {
                // Skip these fields
                if (md.getName().indexOf("ID") == -1 && md.getName().indexOf("Timestamp") == -1&& md.getName().indexOf("LastEditedBy") == -1)
                {
                    oldFieldMetaDataMap.put(md.getName(), md); // add to map for later
                    
                    //log.info(convertColumnName(md.getName())+"  "+ md.getType());
                    String sqlStr = "select count("+md.getName()+") from biologicalobjectattributes where "+md.getName()+" is not null";
                    ResultSet rs  = stmt.executeQuery(sqlStr);
                    if (rs.first() && rs.getInt(1) > 0)
                    {
                        int rowCount = rs.getInt(1);
                        totalCount += rowCount;
                        counts.add(rowCount);
                        
                        log.info(md.getName() + " has " + rowCount + " rows of values");
                        
                        columnsInUse.add(md.getName());
                        AttributeDef attrDef = new AttributeDef();
                        
                        String newName = convertColumnName(md.getName());
                        attrDef.setFieldName(newName);
                        System.out.println("mapping["+newName+"]["+md.getName()+"]");
                        
                        //newNameToOldNameMap.put(newName, md.getName());
                        
                        short dataType = -1;
                        if (typeMap != null)
                        {
                            Short type = typeMap.get(md.getName());
                            if (type == null)
                            {
                                dataType = type;
                            }
                        }
                        
                        if (dataType == -1)
                        {
                            dataType = getDataType(md.getName(), md.getType()).getType();
                        }
                        
                        attrDef.setDataType(dataType);
                        attrDef.setCollectionObjDef(colObjDef);
                        attrDef.setTableType(AttributeIFace.TableType.CollectionObject.getType());
                        
                        attrDefs.put(md.getName(), attrDef);
                        //attrDefs.setTimestampCreated(new Date());
                        //attrDefs.setTimestampModified(new Date());

                        try
                        {
                            HibernateUtil.beginTransaction();
                            session.save(attrDef);
                            HibernateUtil.commitTransaction();
                            
                        } catch (Exception e)
                        {
                            log.error("******* " + e);
                            HibernateUtil.rollbackTransaction();  
                        }

                    }
                    rs.close();
                }
            } // for
            log.info("Total Number of Attrs: " + totalCount);
            
            // Now that we know which columns are being used we can start the conversion process
            
            log.info("biologicalobjectattributes columns in use: "+columnsInUse.size());
            if (columnsInUse.size() > 0)
            {
                int inx = 0;
                StringBuilder str = new StringBuilder("select BiologicalObjectAttributesID");
                for (String name : columnsInUse)
                {
                    str.append(", ");
                    str.append(name);
                    inx++;
                }
                
                str.append(" from biologicalobjectattributes");
                log.info("sql: "+str.toString());
                ResultSet rs = stmt.executeQuery(str.toString());
                
                int[]         countVerify = new int[counts.size()];
                for (int i=0;i<countVerify.length;i++)
                {
                    countVerify[i] = 0;
                }
                boolean       useHibernate = false;
                StringBuilder strBuf       = new StringBuilder();
                int           recordCount  = 0;
                while (rs.next()) 
                {
                    
                    if (useHibernate)
                    {
                        Criteria criteria = session.createCriteria(CollectionObject.class);
                        criteria.add(Expression.eq("collectionObjectId", rs.getInt(1)));
                        List list = criteria.list();
                        if (list.size() == 0)
                        {
                            log.error("**** Can't find the CollectionObject "+rs.getInt(1));
                        } else
                        {
                            CollectionObject colObj = (CollectionObject)list.get(0);
                            
                            inx = 2; // skip the first column (the ID)
                            for (String name : columnsInUse)
                            {
                                AttributeDef                attrDef = attrDefs.get(name); // the needed AttributeDef by name
                                BasicSQLUtils.FieldMetaData md      = oldFieldMetaDataMap.get(name);
                                
                                // Create the new Collection Object Attribute
                                CollectionObjectAttr colObjAttr = new CollectionObjectAttr();
                                colObjAttr.setCollectionObject(colObj);
                                colObjAttr.setDefinition(attrDef);
                                colObjAttr.setTimestampCreated(new Date());
                                colObjAttr.setTimestampModified(new Date());
                                
                                //String oldName = newNameToOldNameMap.get(attrDef.getFieldName());
                                //System.out.println("["+attrDef.getFieldName()+"]["+oldName+"]");
                                
                                
                                //System.out.println(inx+"  "+attrTypes[attrDef.getDataType()]+"  "+md.getName()+"  "+md.getType());
                                setData(rs, inx, attrTypes[attrDef.getDataType()], md, colObjAttr);
                                
                                HibernateUtil.beginTransaction();
                                session.save(colObjAttr);
                                HibernateUtil.commitTransaction();
                                
                                inx++;
                                if (recordCount % 1000 == 0)
                                {
                                    log.info("CollectionObjectAttr Records Processed: "+recordCount);
                                }
                                recordCount++;
                            } // for
                            //log.info("Done - CollectionObjectAttr Records Processed: "+recordCount);
                        }
                    } else
                    {
                        inx = 2; // skip the first column (the ID)
                        for (String name : columnsInUse)
                        {
                            AttributeDef                attrDef = attrDefs.get(name); // the needed AttributeDef by name
                            BasicSQLUtils.FieldMetaData md      = oldFieldMetaDataMap.get(name);

                            
                            if (rs.getObject(inx) != null)
                            {
                                Object  data  = getData(rs, inx, attrTypes[attrDef.getDataType()], md);
                                boolean isStr = data instanceof String;
                                
                                countVerify[inx - 2]++;
                                
                                strBuf.setLength(0);
                                Date date = new Date();
                                strBuf.append("INSERT INTO collectionobjectattr VALUES (");
                                strBuf.append("NULL");//Integer.toString(recordCount));
                                strBuf.append(",");
                                strBuf.append(getStrValue(isStr ? data : null));
                                strBuf.append(",");
                                strBuf.append(getStrValue(isStr ? null : data));
                                strBuf.append(",");
                                strBuf.append(getStrValue(date));
                                strBuf.append(",");
                                strBuf.append(getStrValue(date));
                                strBuf.append(",");
                                strBuf.append(getStrValue(rs.getInt(1)));
                                strBuf.append(",");
                                strBuf.append(getStrValue(attrDef.getAttributeDefId()));
                                strBuf.append(")");
                                
                                try
                                {
                                    Statement updateStatement = newDBConn.createStatement();
                                    updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                                    if (false)
                                    {
                                        System.out.println(strBuf.toString());
                                    }
                                    updateStatement.executeUpdate(strBuf.toString());
                                    updateStatement.clearBatch();
                                    updateStatement.close();
                                    updateStatement = null;
                                    
                                } catch (SQLException e)
                                {
                                    log.error(strBuf.toString());
                                    log.error("Count: "+recordCount);
                                    e.printStackTrace();
                                    log.error(e);
                                    return false;
                                }
                                
                                if (recordCount % 1000 == 0)
                                {
                                    log.info("CollectionObjectAttr Records Processed: "+recordCount);
                                }
                                recordCount++;
                            }
                            inx++;
                        } // for
                    } // if
                } // while
                rs.close();
                stmt.close();
                
                log.info("Count Verification:");
                for (int i=0;i<counts.size();i++)
                {
                    log.info(columnsInUse.get(i)+" ["+counts.get(i)+"]["+countVerify[i]+"] "+(counts.get(i) - countVerify[i]));
                            
                }
            }
            
        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            return false;
        } 
        return true;
    } 
    
    /**
     * Converts all the CollectionObject Physical records and CollectionObjectCatalog Records into the new schema Preparation table.
     * @return true if no errors
     */
    public boolean createPreparationRecords(final Map<String, PrepType> prepTypeMap)
    {
        Connection newDBConn = DBConnection.getConnection();
        deleteAllRecordsFromTable(newDBConn, "preparation");
        
        DBConnection oldDB     = DBConnection.createInstance(oldDriver, oldDBName, oldUserName, oldPassword);
        Connection   oldDBConn = oldDB.getConnectionToDB();
        try 
        {
            Statement    stmt = oldDBConn.createStatement();
            StringBuilder str  = new StringBuilder();
            
            List<String> oldFieldNames = new ArrayList<String>();
            
            StringBuilder sql = new StringBuilder("select ");
            List<String> names = new ArrayList<String>();
            getFieldNamesFromSchema(oldDBConn, "collectionobject", names);
            
            sql.append(buildSelectFieldList(names, "collectionobject"));
            sql.append(", ");
            oldFieldNames.addAll(names);
            
            names.clear();
            getFieldNamesFromSchema(oldDBConn, "collectionobjectcatalog", names);
            sql.append(buildSelectFieldList(names, "collectionobjectcatalog"));
            oldFieldNames.addAll(names);
            
            sql.append(" From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID Where not (collectionobject.DerivedFromID Is Null)");
            
            log.info(sql);
            
            List<BasicSQLUtils.FieldMetaData> newFieldMetaData = new ArrayList<BasicSQLUtils.FieldMetaData>();
            getFieldMetaDataFromSchema(newDBConn, "preparation", newFieldMetaData);

            
            log.info("Number of Fields in Preparation "+newFieldMetaData.size());
            String sqlStr = sql.toString();
            
            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 0;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
                System.out.println(name+" "+(inx-1));
            }
            Hashtable<String, String> newToOld = new Hashtable<String, String>();
            newToOld.put("PreparationID", "CollectionObjectID");
            newToOld.put("CollectionObjectID", "DerivedFromID");
            newToOld.put("StorageLocation", "Location");
            
            boolean doDebug   = false;
            ResultSet rs      = stmt.executeQuery(sqlStr);
            Integer   idIndex = oldNameIndex.get("CollectionObjectID");
            int       count   = 0;
            while (rs.next()) 
            {
                Integer   preparedById = null;
                Date      preparedDate = null;
                
                boolean   checkForPreps = false;
                if (checkForPreps)
                {
                    Statement subStmt      = oldDBConn.createStatement();
                    String    subQueryStr  = "select PreparedByID,PreparedDate from preparation where PreparationID = "+rs.getInt(idIndex+1);
                    ResultSet subQueryRS   = subStmt.executeQuery(subQueryStr);
                    if (subQueryRS.first())
                    {
                        preparedById = subQueryRS.getInt(1);
                        preparedDate = UIHelper.convertIntToDate(subQueryRS.getInt(2));
                    }
                    subQueryRS.close();
                    subStmt.close();
                }
                
                int catNum =  rs.getInt(oldNameIndex.get("CatalogNumber")+1);
                doDebug = catNum == 30972;
                
                if (doDebug)
                {
                    System.out.println("CatalogNumber      "+catNum);
                    System.out.println("CollectionObjectID "+rs.getInt(oldNameIndex.get("CollectionObjectID")+1));
                    System.out.println("DerivedFromID      "+rs.getInt(oldNameIndex.get("DerivedFromID")+1));
                }
                
                str.setLength(0);
                str.append("INSERT INTO preparation VALUES (");
                for (int i=0;i<newFieldMetaData.size();i++)
                {
                    if (i > 0) str.append(", ");
                    
                    String newFieldName = newFieldMetaData.get(i).getName();
                    String mappedName   = newToOld.get(newFieldName);

                    if (mappedName != null)
                    {
                        newFieldName = mappedName;
                    }
                    
                    if (newFieldName.equals("PreparedByID"))
                    {
                        str.append(getStrValue(preparedById));
                        
                    } else if (newFieldName.equals("PreparedDate"))
                    {
                        str.append(getStrValue(preparedDate));
                        
                    } else if (newFieldName.equals("PrepTypeID"))
                    {
                        String value = rs.getString(oldNameIndex.get("PreparationMethod")+1);
                        if (value == null || value.length() == 0)
                        {
                            value = "n/a";
                        }
                        Integer prepTypeId = prepTypeMap.get(value.toLowerCase()).getPrepTypeId();
                        if (prepTypeId != null)
                        {
                            str.append(getStrValue(prepTypeId));
                            
                        } else
                        {
                            str.append("NULL");
                            log.error("***************** Couldn't find PreparationMethod["+value+"] in PrepTypeMap");
                            /*stmt.close();
                            oldDBConn.close();
                            newDBConn.close();
                            return false;*/
                        }
                        
                    } else if (newFieldName.equals("LocationID"))
                    {
                        str.append("NULL");
                        
                    } else
                    {
                        
                        Integer index = oldNameIndex.get(newFieldName);
                        if (index != null)
                        {
                            str.append(getStrValue(rs.getObject(index+1), newFieldMetaData.get(i).getType()));
                        } else
                        {
                            log.error("Couldn't find new field name["+newFieldName+"] in old field name in index Map");
                            stmt.close();
                            oldDBConn.close();
                            newDBConn.close();
                            return false;
                        }
                    }

                }
                str.append(")");
                //log.info("\n"+str.toString());
                if (count % 1000 == 0) log.info("Preparation Records: "+count);
                
                try
                {
                    Statement updateStatement = newDBConn.createStatement();
                    updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                    if (doDebug)
                    {
                        System.out.println(str.toString());
                    }
                    updateStatement.executeUpdate(str.toString());
                    updateStatement.clearBatch();
                    updateStatement.close();
                    updateStatement = null;
                    
                } catch (SQLException e)
                {
                    log.error("Count: "+count);
                    e.printStackTrace();
                    log.error(e);
                    return false;
                }
                
                count++;
                //if (count == 1) break;
            }
            log.info("Processed CollectionObject "+count+" records.");

            
        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            return false;
        } 

        return true;
        
    } 
    
    /**
     * Converts all the CollectionObject and CollectionObjectCatalog Records into the new schema CollectionObject table.
     * All "logical" records are moved to the CollectionObject table and all "physical" records are moved to the Preparation table.
     * @return true if no errors
     */
    public boolean createCollectionRecords()
    {
        
        Connection newDBConn = DBConnection.getConnection();
        deleteAllRecordsFromTable(newDBConn, "collectionobject");
        
        DBConnection oldDB     = DBConnection.createInstance(oldDriver, oldDBName, oldUserName, oldPassword);
        Connection   oldDBConn = oldDB.getConnectionToDB();
        try 
        {
            Statement    stmt = oldDBConn.createStatement();
            StringBuilder str  = new StringBuilder();
            
            List<String> oldFieldNames = new ArrayList<String>();
            
            StringBuilder sql = new StringBuilder("select ");
            List<String> names = new ArrayList<String>();
            getFieldNamesFromSchema(oldDBConn, "collectionobject", names);
            
            sql.append(buildSelectFieldList(names, "collectionobject"));
            sql.append(", ");
            oldFieldNames.addAll(names);
            
            names.clear();
            getFieldNamesFromSchema(oldDBConn, "collectionobjectcatalog", names);
            sql.append(buildSelectFieldList(names, "collectionobjectcatalog"));
            oldFieldNames.addAll(names);
            
            sql.append(" From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID Where collectionobject.DerivedFromID Is Null");
            
            log.info(sql);
            
            //List<String> newFieldNames = new ArrayList<String>();
            //getFieldNamesFromSchema(newDBConn, "collectionobject", newFieldNames);
            
            List<BasicSQLUtils.FieldMetaData> newFieldMetaData = new ArrayList<BasicSQLUtils.FieldMetaData>();
            getFieldMetaDataFromSchema(newDBConn, "collectionobject", newFieldMetaData);

            
            log.info("Number of Fields in New CollectionObject "+newFieldMetaData.size());
            String sqlStr = sql.toString();
            
            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 0;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
            }
            
            ResultSet rs = stmt.executeQuery(sqlStr);
            
            int count = 0;
            while (rs.next()) 
            {                    
                str.setLength(0);
                str.append("INSERT INTO collectionobject VALUES (");
                for (int i=0;i<newFieldMetaData.size();i++)
                {
                    if (i > 0) str.append(", ");
                    
                    String newFieldName = newFieldMetaData.get(i).getName();
                    
                    if (newFieldName.equals("CatalogedDateVerbatim") || newFieldName.equals("ContainerID") || newFieldName.equals("GUID"))
                    {
                        str.append("NULL");
                        
                    } else 
                    {
                        Integer index = oldNameIndex.get(newFieldName);
                        if (index != null)
                        {
                            str.append(getStrValue(rs.getObject(index+1), newFieldMetaData.get(i).getType()));
                        } else
                        {
                            log.error("Couldn't find new field name["+newFieldName+"] in old field name Map");
                            stmt.close();
                            oldDBConn.close();
                            newDBConn.close();
                            return false;
                        }
                    }

                }
                str.append(")");
                //log.info("\n"+str.toString());
                if (count % 1000 == 0) log.info("CollectionObject Records: "+count);
                
                try
                {
                    Statement updateStatement = newDBConn.createStatement();
                    updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                    updateStatement.executeUpdate(str.toString());
                    updateStatement.clearBatch();
                    updateStatement.close();
                    updateStatement = null;
                    
                } catch (SQLException e)
                {
                    log.error("Count: "+count);
                    e.printStackTrace();
                    log.error(e);
                    return false;
                }
                
                count++;
                //if (count == 1) break;
            }
            log.info("Processed CollectionObject "+count+" records.");

            
        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            return false;
        } 


        return true;
    }
    

    /**
     * Creates a User Group
     * @param groupName the name of the group
     * @return the group
     */
    public UserGroup createUserGroup(final String groupName)
    {
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            UserGroup userGroup = new UserGroup();
            userGroup.setName(groupName);
            userGroup.setUsers(new HashSet());
            
            session.save(userGroup);
            
            HibernateUtil.commitTransaction();

            return userGroup;
            
        } catch (Exception e)
        {
            log.error("******* " + e);
            HibernateUtil.rollbackTransaction();
        }

        return null;
    }
    
    /**
     * Creates a new User a new User 
     * @param username the user name of the user
     * @param password the password (not emcrypted)
     * @param privLevel the privLevel
     * @return the user object
     */
    public User createNewUser(final UserGroup userGroup, final String username, final String password, final short privLevel)
    {
        
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            User user = new User();
            user.setName(username);
            user.setPassword(Encryption.encrypt(password));
            user.setPrivLevel(privLevel);
            user.setUserGroup(userGroup);
            
            session.save(user);
            
            userGroup.getUsers().add(user);
            session.saveOrUpdate(userGroup);
            
            HibernateUtil.commitTransaction();


            return user;
            
        } catch (Exception e)
        {
            log.error("******* " + e);
            HibernateUtil.rollbackTransaction();
        }

        return null;
    }
    
    /**
     * Creates a Standard set of DataTypes for Collections
     * @param returnName the name of a DataType to return (ok if null)
     * @return the DataType requested
     */
    public DataType createDataTypes(final String returnName)
    {
        String[] dataTypeNames = {"Animal", "Plant", "Fungi", "Mineral", "Other"};
        
        DataType retDataType = null;
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            for (String name : dataTypeNames)
            {
                DataType dataType = new DataType();
                dataType.setName(name);
                dataType.setCollectionObjDef(null);
                session.save(dataType);
                
                if (returnName != null && name.equals(returnName))
                {
                    retDataType = dataType;
                }
            }
            
            HibernateUtil.commitTransaction();
            
        } catch (Exception e)
        {
            log.error("******* " + e);
            HibernateUtil.rollbackTransaction();
        }
        return retDataType;
    }
    
    /**
     * @param name
     * @param dataType
     * @param user
     * @param taxaTreeDef
     * @param catalogSeries
     * @return
     */
    public Set<Object> createCollectionObjDef(final String          name, 
                                              final DataType        dataType, 
                                              final User            user, 
                                              final TaxonTreeDef taxaTreeDef,
                                              final CatalogSeries   catalogSeries)
    {
        try
        {
            Set<Object> catalogSeriesSet = new HashSet<Object>();
            if (catalogSeries != null)
            {
                catalogSeriesSet.add(catalogSeries);
            }
            
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            
            Set<Object> set = new HashSet<Object>();
            set.add(taxaTreeDef);

            CollectionObjDef colObjDef = new CollectionObjDef();
            colObjDef.setName(name);
            colObjDef.setDataType(dataType);
            colObjDef.setUser(user);

            colObjDef.setTaxonTreeDef(set);

            colObjDef.setCatalogSeries(catalogSeriesSet);
            colObjDef.setAttributeDefs(new HashSet<Object>());
            
            session.save(colObjDef);
            
            set.clear();
            set.add(colObjDef);
            user.setCollectionObjDef(set);
            session.saveOrUpdate(user);
            
            HibernateUtil.commitTransaction();
           
            
           return set;
            
        } catch (Exception e)
        {
            log.error("******* " + e);
            e.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
        return null;
    }
    
    /**
     * @brief Parses a tab-delimited file containing geographical location data
     *        and fills a db table with the appropriate data.
     * 
     * The input file must format the data in the following order: id, current
     * id, continent or ocean, country, state, county, island group, island,
     * water body, drainage, full geographical name. <b>IT IS ASSUMED THAT THE
     * INPUT DATA HAS BEEN SORTED ALPHABETICALLY BY CONTINENT, THEN COUNTRY,
     * THEN STATE, AND FINALLY COUNTY.<b>
     * 
     * @param filename
     *            full pathname of a tab-delimited file containing the geography
     *            data
     * @throws IOException
     *             if filename doesn't refer to a valid file path or there is an
     *             error while reading the file. In either situation, the
     *             resulting database table should not be considered usable.
     * @throws SQLException
     */
    public void loadSpecifyGeographicNames( final String tablename,
                                            final String filename,
                                            final int    geographyTreeDefId )
        throws IOException, SQLException
    {   
        BufferedReader inFile = new BufferedReader(new FileReader(filename));
        
        // StringBuilder updateString = new StringBuilder("insert into " +
        // tablename + " values ");
        // Statement st = dbConn.createStatement();
        
        Vector<GeoFileLine>     oldStyleItems = new Vector<GeoFileLine>();
        Vector<Integer>         usedIds       = new Vector<Integer>();
        Vector<Sp6GeoTableItem> newTableRows  = new Vector<Sp6GeoTableItem>();
        String line = null;
        int cnt = 0;
        while( (line = inFile.readLine()) != null )
        {
            String fields[] = line.split("\t");
            int geoId = Integer.parseInt(fields[0]);
            // int curId = Integer.parseInt(fields[1]);
            String contOrOcean = fields[2].equals("") ? null : fields[2];
            String country = fields[3].equals("") ? null : fields[3];
            String state = fields[4].equals("") ? null : fields[4];
            String county = fields[5].equals("") ? null : fields[5];
            String islandGrp = fields[6].equals("") ? null : fields[6];
            String island = fields[7].equals("") ? null : fields[7];
            String waterBody = fields[8].equals("") ? null : fields[8];
            String drainage = fields[9].equals("") ? null : fields[9];
            String full = fields[10].equals("") ? null : fields[10];
        
            GeoFileLine row = new GeoFileLine(geoId,0,0,contOrOcean,country,state,county,islandGrp,island,waterBody,drainage,full);
            oldStyleItems.add(row);
            usedIds.add(geoId);
            
            if (cnt % 1000 == 0)
            {
                log.info("Geography: " + cnt);
            }
            cnt++;
        }
        
        // setup the root node (Earth) of the geo tree
        int geoRootId = findUnusedId(usedIds);
        usedIds.add(geoRootId);
        int nextNodeNumber = 1;
        Sp6GeoTableItem geoRoot = new Sp6GeoTableItem(geoRootId,"Earth",GEO_ROOT_RANK,nextNodeNumber++,0,geoRootId);
        newTableRows.add(geoRoot);

        
        String prevCont = null;
        String prevCountry = null;
        String prevState = null;
        String prevCounty = null;
        int prevContGeoId = 0;
        int prevCountryGeoId = 0;
        int prevStateGeoId = 0;
        int prevCountyGeoId = 0;
        
        // process them all into the new tree structure
        // on the first pass, we're simply going to create all of the nodes and
        // setup the parent pointers
        for( GeoFileLine geo: oldStyleItems )
        {
            boolean hasCont = !(geo.getContOrOcean() == null);
            boolean hasCountry = !(geo.getCountry() == null);
            boolean hasState = !(geo.getState() == null);
            boolean hasCounty = !(geo.getCounty() == null);
            
            if( !hasCont && !hasCountry && !hasState && !hasCounty )
            {
                // this one has no geo information that we need
                // it's probably just water bodies
                
                // we could probably reclaim the geographyId if we wanted to
                continue;
            }
            
            int countyGeoId;
            int stateGeoId;
            int countryGeoId;
            int contGeoId;
            String geoName;
            
            if( geo.getContOrOcean() != null && !geo.getContOrOcean().equals(prevCont) )
            {
                // the continent is new (and country, state, and county, if
                // non-empty)
                
                // find geographyIds for each node
                if( hasCounty )
                {
                    // the county keeps the existing id
                    // the other levels get new ones
                    
                    contGeoId = findUnusedId(usedIds);
                    usedIds.add(contGeoId);
                    geoName = geo.getContOrOcean();
                    Sp6GeoTableItem newCont = new Sp6GeoTableItem(contGeoId,geoName,CONTINENT_RANK,nextNodeNumber++,-1,geoRootId);
                    prevCont = geoName;
                    prevContGeoId = contGeoId;

                    countryGeoId = findUnusedId(usedIds);
                    usedIds.add(countryGeoId);
                    geoName = geo.getCountry();
                    Sp6GeoTableItem newCountry = new Sp6GeoTableItem(countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,-1,prevContGeoId);
                    prevCountry = geoName;
                    prevCountryGeoId = countryGeoId;
                    
                    stateGeoId = findUnusedId(usedIds);
                    usedIds.add(stateGeoId);
                    geoName = geo.getState();
                    Sp6GeoTableItem newState = new Sp6GeoTableItem(stateGeoId,geoName,STATE_RANK,nextNodeNumber++,-1,prevCountryGeoId);
                    prevState = geoName;
                    prevStateGeoId = stateGeoId;
                    
                    // county keeps existing id
                    countyGeoId = geo.getId();
                    geoName = geo.getCounty();
                    Sp6GeoTableItem newCounty = new Sp6GeoTableItem(countyGeoId,geoName,COUNTY_RANK,nextNodeNumber++,-1,prevStateGeoId);
                    prevCounty = geoName;
                    prevCountyGeoId = countyGeoId;
                    
                    newTableRows.add(newCont);
                    newTableRows.add(newCountry);
                    newTableRows.add(newState);
                    newTableRows.add(newCounty);
                }
                else if( hasState )
                {
                    // state keeps the existing id
                    // cont and country get new ones
                    // this item has no county

                    contGeoId = findUnusedId(usedIds);
                    usedIds.add(contGeoId);
                    geoName = geo.getContOrOcean();
                    Sp6GeoTableItem newCont = new Sp6GeoTableItem(contGeoId,geoName,CONTINENT_RANK,nextNodeNumber++,-1,geoRootId);
                    prevCont = geoName;
                    prevContGeoId = contGeoId;

                    countryGeoId = findUnusedId(usedIds);
                    usedIds.add(countryGeoId);
                    geoName = geo.getCountry();
                    Sp6GeoTableItem newCountry = new Sp6GeoTableItem(countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,-1,prevContGeoId);
                    prevCountry = geoName;
                    prevCountryGeoId = countryGeoId;
                    
                    // state keeps existing id
                    stateGeoId = geo.getId();
                    geoName = geo.getState();
                    Sp6GeoTableItem newState = new Sp6GeoTableItem(stateGeoId,geoName,STATE_RANK,nextNodeNumber++,-1,prevCountryGeoId);
                    prevState = geoName;
                    prevStateGeoId = stateGeoId;
                                        
                    newTableRows.add(newCont);
                    newTableRows.add(newCountry);
                    newTableRows.add(newState);
                }
                else if( hasCountry )
                {
                    // country keeps the existing id
                    // cont gets a new one
                    // this item has no state or county

                    contGeoId = findUnusedId(usedIds);
                    usedIds.add(contGeoId);
                    geoName = geo.getContOrOcean();
                    Sp6GeoTableItem newCont = new Sp6GeoTableItem(contGeoId,geoName,CONTINENT_RANK,nextNodeNumber++,-1,geoRootId);
                    prevCont = geoName;
                    prevContGeoId = contGeoId;

                    // country keeps existing id
                    countryGeoId = geo.getId();
                    geoName = geo.getCountry();
                    Sp6GeoTableItem newCountry = new Sp6GeoTableItem(countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,-1,prevContGeoId);
                    prevCountry = geoName;
                    prevCountryGeoId = countryGeoId;
                    
                    newTableRows.add(newCont);
                    newTableRows.add(newCountry);
                }
                else if( hasCont )
                {
                    // cont keeps the existing id
                    // this item has no country, state, or county

                    contGeoId = geo.getId();
                    geoName = geo.getContOrOcean();
                    Sp6GeoTableItem newCont = new Sp6GeoTableItem(contGeoId,geoName,CONTINENT_RANK,nextNodeNumber++,-1,geoRootId);
                    prevCont = geoName;
                    prevContGeoId = contGeoId;

                    newTableRows.add(newCont);
                }
            }
            
            else if( geo.getCountry() != null && !geo.getCountry().equals(prevCountry) )
            {
                // the country is new (and the state and county, if non-empty)
                
                // find geographyIds for each node
                if( hasCounty )
                {
                    // the county keeps the existing id
                    // the other levels get new ones
                    
                    countryGeoId = findUnusedId(usedIds);
                    usedIds.add(countryGeoId);
                    geoName = geo.getCountry();
                    Sp6GeoTableItem newCountry = new Sp6GeoTableItem(countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,-1,prevContGeoId);
                    prevCountry = geoName;
                    prevCountryGeoId = countryGeoId;
                    
                    stateGeoId = findUnusedId(usedIds);
                    usedIds.add(stateGeoId);
                    geoName = geo.getState();
                    Sp6GeoTableItem newState = new Sp6GeoTableItem(stateGeoId,geoName,STATE_RANK,nextNodeNumber++,-1,prevCountryGeoId);
                    prevState = geoName;
                    prevStateGeoId = stateGeoId;
                    
                    // county keeps existing id
                    countyGeoId = geo.getId();
                    geoName = geo.getCounty();
                    Sp6GeoTableItem newCounty = new Sp6GeoTableItem(countyGeoId,geoName,COUNTY_RANK,nextNodeNumber++,-1,prevStateGeoId);
                    prevCounty = geoName;
                    prevCountyGeoId = countyGeoId;
                    
                    newTableRows.add(newCountry);
                    newTableRows.add(newState);
                    newTableRows.add(newCounty);
                }
                else if( hasState )
                {
                    // state keeps the existing id
                    // cont and country get new ones
                    // this item has no county

                    countryGeoId = findUnusedId(usedIds);
                    usedIds.add(countryGeoId);
                    geoName = geo.getCountry();
                    Sp6GeoTableItem newCountry = new Sp6GeoTableItem(countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,-1,prevContGeoId);
                    prevCountry = geoName;
                    prevCountryGeoId = countryGeoId;
                    
                    // state keeps existing id
                    stateGeoId = geo.getId();
                    geoName = geo.getState();
                    Sp6GeoTableItem newState = new Sp6GeoTableItem(stateGeoId,geoName,STATE_RANK,nextNodeNumber++,-1,prevCountryGeoId);
                    prevState = geoName;
                    prevStateGeoId = stateGeoId;
                                        
                    newTableRows.add(newCountry);
                    newTableRows.add(newState);
                }
                else if( hasCountry )
                {
                    // country keeps the existing id
                    // cont gets a new one
                    // this item has no state or county

                    // country keeps existing id
                    countryGeoId = geo.getId();
                    geoName = geo.getCountry();
                    Sp6GeoTableItem newCountry = new Sp6GeoTableItem(countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,-1,prevContGeoId);
                    prevCountry = geoName;
                    prevCountryGeoId = countryGeoId;
                    
                    newTableRows.add(newCountry);
                }
            }
            
            else if( geo.getState() != null && !geo.getState().equals(prevState) )
            {
                // the state is new (and the county, if non-empty)
                
                // find geographyIds for each node
                if( hasCounty )
                {
                    // the county keeps the existing id
                    // the other levels get new ones
                                    
                    stateGeoId = findUnusedId(usedIds);
                    usedIds.add(stateGeoId);
                    geoName = geo.getState();
                    Sp6GeoTableItem newState = new Sp6GeoTableItem(stateGeoId,geoName,STATE_RANK,nextNodeNumber++,-1,prevCountryGeoId);
                    prevState = geoName;
                    prevStateGeoId = stateGeoId;
                    
                    // county keeps existing id
                    countyGeoId = geo.getId();
                    geoName = geo.getCounty();
                    Sp6GeoTableItem newCounty = new Sp6GeoTableItem(countyGeoId,geoName,COUNTY_RANK,nextNodeNumber++,-1,prevStateGeoId);
                    prevCounty = geoName;
                    prevCountyGeoId = countyGeoId;
                    
                    newTableRows.add(newState);
                    newTableRows.add(newCounty);
                }
                else if( hasState )
                {
                    // state keeps the existing id
                    // cont and country get new ones
                    // this item has no county
                    stateGeoId = geo.getId();
                    geoName = geo.getState();
                    Sp6GeoTableItem newState = new Sp6GeoTableItem(stateGeoId,geoName,STATE_RANK,nextNodeNumber++,-1,prevCountryGeoId);
                    prevState = geoName;
                    prevStateGeoId = stateGeoId;
                                        
                    newTableRows.add(newState);
                }
            }

            else if( geo.getCounty() != null && !geo.getCounty().equals(prevCounty) )
            {
                // only the county is new (and the county, if non-empty)
                
                // find geographyIds for each node
                if( hasCounty )
                {
                    // the county keeps the existing id
                    // the other levels get new ones
                    countyGeoId = geo.getId();
                    geoName = geo.getCounty();
                    Sp6GeoTableItem newCounty = new Sp6GeoTableItem(countyGeoId,geoName,COUNTY_RANK,nextNodeNumber++,-1,prevStateGeoId);
                    prevCounty = geoName;
                    prevCountyGeoId = countyGeoId;
                    
                    newTableRows.add(newCounty);
                }
            }
        }
        
        // now we have a Vector of Sp6GeoTableItems that contains all the data
        // we simply need to fixup all the highChildNodeNumber fields
        
        ListIterator<Sp6GeoTableItem> revIter = newTableRows.listIterator(newTableRows.size());
        while(revIter.hasPrevious())
        {
            Sp6GeoTableItem newRow = revIter.previous();
            int nodeNum = newRow.getNodeNumber();
            if( nodeNum > newRow.getHighChildNodeNumber() )
            {
                newRow.setHighChildNodeNumber(nodeNum);
            }
            Sp6GeoTableItem parent = newRow;
            
            // adjust all the parent nodes (all the way up)
            while( true )
            {
                int parentId = parent.getParentId();
                parent = findNodeById(newTableRows, parentId);
                
                if( parent.getHighChildNodeNumber() < nodeNum )
                {
                    parent.setHighChildNodeNumber(nodeNum);
                }
                if( parent.getGeographyId() == parent.getParentId() ) // indicates
                                                                        // the
                                                                        // geo
                                                                        // root
                                                                        // node
                                                                        // (Earth)
                {
                    break;
                }
            }
        }
        
        Connection conn = DBConnection.getConnection();
        Statement st = conn.createStatement();
        st.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
        
        // put together a huge 'insert' statement, starting with the 'values
        // (...)' portion
        int rowsInserted = 0;
        StringBuilder insertStatement = new StringBuilder();
        for( Sp6GeoTableItem item: newTableRows )
        {
            insertStatement.setLength(0);
            insertStatement.append( "INSERT INTO geography (Name,GeographyId,ParentId,NodeNumber,HighestChildNodeNumber,RankId,GeographyTreeDefId) values ");
            insertStatement.append("(\"");
            insertStatement.append(item.getName());
            insertStatement.append("\",");
            insertStatement.append(item.getGeographyId());
            insertStatement.append(",");
            insertStatement.append(item.getParentId());
            insertStatement.append(",");
            insertStatement.append(item.getNodeNumber());
            insertStatement.append(",");
            insertStatement.append(item.getHighChildNodeNumber());
            insertStatement.append(",");
            insertStatement.append(item.getRankId());
            insertStatement.append(",");
            insertStatement.append(geographyTreeDefId);
            insertStatement.append(")");
            
            int row = st.executeUpdate(insertStatement.toString());
            if (rowsInserted % 1000 == 0)
            {
                log.info("Geography: " + rowsInserted);
            }
            rowsInserted++;
        }
        log.info("Rows inserted: " + rowsInserted);
    }
    
    /**
     * @param nodes
     * @param id
     * @return
     */
    private static Sp6GeoTableItem findNodeById(final Vector<Sp6GeoTableItem> nodes, int id )
    {
        for( Sp6GeoTableItem node: nodes )
        {
            if( node.getGeographyId() == id )
            {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Finds the smallest <code>int</code> not in the <code>Collection</code>
     * 
     * @param usedIds
     *            the <code>Collection</code> of used values
     * @return the smallest unused value
     */
    public static int findUnusedId(final Collection<Integer> usedIds )
    {
        for(int i=1;;++i)
        {
            if( !usedIds.contains(i) )
            {
                return i;
            }
        }
    }
        
    
    /**
     * 
     */
    public void convertTaxon()
    {
        boolean showMappingErrors = BasicSQLUtils.isShowMappingError();
        BasicSQLUtils.setShowMappingError(false); // turn off notification because of errors with TaxonTreeDefID
            
        DBConnection oldDB     = DBConnection.createInstance(oldDriver, oldDBName, oldUserName, oldPassword);
        String sql = "select * from taxonname";
        
        if (copyTable(oldDB.getConnectionToDB(), DBConnection.getConnection(), sql, "taxonname", "taxon", 
                      createFieldNameMap(new String[] {"TaxonID", "TaxonNameID", "ParentID", "ParentTaxonNameID", "Name", "TaxonName", "FullName", "FullTaxonName"}), null))
        {
            log.info("TaxonName copied ok.");
        } else
        {
            log.error("Copying TaxonName (fields) to new Taxon");
        }
       BasicSQLUtils.setShowMappingError(showMappingErrors);
    }
    
    /**
     * 
     */
    public void convertLocality()
    {
        boolean showMappingErrors = BasicSQLUtils.isShowMappingError();
        BasicSQLUtils.setShowMappingError(false); // turn off notification because of errors with National Parks
            
        DBConnection oldDB     = DBConnection.createInstance(oldDriver, oldDBName, oldUserName, oldPassword);
        String sql = "select locality.*, geography.* from locality,geography where locality.GeographyID = geography.GeographyID";
        
        if (copyTable(oldDB.getConnectionToDB(), DBConnection.getConnection(), sql, "geography", "locality", null, null))
        {
            log.info("Locality/Geography copied ok.");
        } else
        {
            log.error("Copying locality/geography (fields) to new Locality");
        }
        BasicSQLUtils.setShowMappingError(showMappingErrors);
    }
    
}

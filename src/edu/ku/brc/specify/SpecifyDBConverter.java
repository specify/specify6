package edu.ku.brc.specify;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.HashSet;

import org.hibernate.*;
import org.hibernate.SessionFactory;

import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Expression;

import  edu.ku.brc.specify.datamodel.*;
import  edu.ku.brc.specify.dbsupport.*;

import java.text.SimpleDateFormat;

/**
 * Create more sample data, letting Hibernate persist it for us.
 */
public class SpecifyDBConverter 
{
    protected static Hashtable<String, Integer> prepTypeMapper    = new Hashtable<String, Integer>();
    protected static int                        attrsId           = 0;
    protected static SimpleDateFormat           dateFormatter     = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static StringBuffer               strBuf            = new StringBuffer("");
    protected static Calendar                   calendar          = Calendar.getInstance();
    
    public SpecifyDBConverter()
    {

    }
    
     /**
     * @param oldNames
     * @param newName
     * @return
     */
    public static int getIndex(String[] oldNames, String newName)
    {
        for (int i=0;i<oldNames.length;i++)
        {
            String fieldName = oldNames[i].substring(oldNames[i].indexOf(".")+1, oldNames[i].length());
            //System.out.println(fieldName
            if (newName.equals(fieldName))
            {
                return i;
            }
        }
        return -1;
    }
    
   
    /**
     * @param id
     * @param name
     * @return
     */
    public static PrepTypes loadPrepType(final int id, final String name)
    {
        try 
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            

            PrepTypes prepType = new PrepTypes(new Integer(id+1));
            prepType.setName(name);
            prepType.setPrepsObjs(null);
            
            session.save(prepType);
            
            HibernateUtil.commitTransaction();
            
            return prepType;
            
        } catch (Exception e) 
        {
            System.err.println("******* " + e);
            HibernateUtil.rollbackTransaction();
        } 
        return null;
    }
    
    /**
     * 
     * @param aSession
     * @param aNames
     * @param aTypes
     * @param aDiscipline
     * @param aTableType
     */
    public static List loadAttrDefs(final short    discipline, 
                                    final short    tableType, 
                                    final int      subType, 
                                    final String[] attrNames, 
                                    final short[]  dataTypes)
    {
        if (attrNames.length == dataTypes.length)
        {
            Vector<AttrsDef> list = new Vector<AttrsDef>();
            try
            {
                Session session = HibernateUtil.getCurrentSession();
                HibernateUtil.beginTransaction();

                for (int i = 0; i < attrNames.length; i++)
                {
                    attrsId++;
                    AttrsDef attrsDef = new AttrsDef(attrsId);
                    attrsDef.setDisciplineType(discipline);
                    attrsDef.setTableType(tableType);
                    attrsDef.setSubType((short)subType);
                    attrsDef.setFieldName(attrNames[i]);
                    attrsDef.setDataType(dataTypes[i]);

                    list.addElement(attrsDef);
                }
                HibernateUtil.commitTransaction();

                return list;

            } catch (Exception e)
            {
                System.err.println("******* " + e);
                HibernateUtil.rollbackTransaction();
            }

        } else
        {
            System.out.println("Names length: " + attrNames.length + " doesn't match Types length "
                    + dataTypes.length);
        }
        return null;
    }
    
    /**
     * 
     * @param aSession
     * @param aNames
     * @param aTypes
     * @param aDiscipline
     * @param aTableType
     */
    /*public static void loadPrepAttrDefs(Session aSession, int aPrepTypeInx, String[] aPrepTypes, String[] aAttrNames, short[] aTypes, short aDiscipline)
    {
      if (aAttrNames.length == aTypes.length)
      {
          Transaction tx = null;
          try 
          {
              tx = aSession.beginTransaction();
              PrepTypes prepType = new PrepTypes(new Integer(aPrepTypeInx+1), aPrepTypes[aPrepTypeInx], null, null);
              aSession.save(prepType);
              
              for (int i=0;i<aAttrNames.length;i++)
              {
                  //BioAttrs bioAttr = new BioAttrs(new Integer(i+1), aNames[i], "", new Integer(aTypes[i]), 
                  //                                Integer unit, new Integer(aTypes[i]), null, null)
                  PrepAttrsDef prepAttrsDef = new PrepAttrsDef(new Integer(i+1), new Integer(aDiscipline), aAttrNames[i], new Short(aTypes[i]), prepType);
                  
                  aSession.save(prepAttrsDef);
              }
              
              // We're done; make our changes permanent
              aSession.flush();
              tx.commit();

          } catch (Exception e) {
              System.err.println("******* " + e);
              if (tx != null) 
              {
                   // Something went wrong; discard all partial changes
                    //tx.rollback();
              }
          } finally {
              System.err.println("Closing Session.");
              // No matter what, close the session
              //aSession.close();
          }
            
      } else
      {
          System.out.println("Names length: "+aAttrNames.length+" doesn't match Types length "+aTypes.length);
      }
    }*/
    
    protected static void deleteAllRecordsFromTable(final String tableName)
    {
        try
        {
            Connection connect = DBConnection.getConnection();
            
            Statement updateStatement = connect.createStatement();
           
            exeUpdateCmd(updateStatement, "delete from "+tableName);
            
            updateStatement.clearBatch();
            updateStatement.close();
            
            System.out.println("Deleted all records from "+tableName);
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /*
     * 
     */
    public static void loadAttrs()
    {
        try
        {

            boolean doDeleteAll = true;
            if (doDeleteAll) 
            {
                deleteAllRecordsFromTable("attrsdef");
                deleteAllRecordsFromTable("preptypes");
                deleteAllRecordsFromTable("prepattrs");
            }

            //------------------------------
            // Load PrepTypes and Prep Attrs
            //------------------------------
            String[] fishPrepTypes = {"EtOH", "Skeleton", "Tissue", "Clear & Stained", "X-Ray", "Misc"};
            String[] fishPrepStr   = {"EtOH", "Skel",     "Tissue", "C&S",             "X-Ray", "Misc"};
            for (int i=0;i<fishPrepTypes.length;i++)
            {
                loadPrepType(i+1, fishPrepTypes[i]);
                prepTypeMapper.put(fishPrepStr[i], i);
            }
            
            String[] fishEtOHAttrs = {
                    "size",
                    "sex",
                    "url",
                    };
            short[] fishEtOHTypes = {
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.MEMO_TYPE,
            };
            String[] fishSkelAttrs = {
                    "size",
                    };
            short[] fishSkelTypes = {
                    AttrsMgr.VARC_TYPE,
            };
            
            String[] fishTissueAttrs = {
                    "dna",
                    };
            short[] fishTissueTypes = {
                    AttrsMgr.VARC_TYPE,
            };
            
            String[] fishClearStainAttrs = {
                    "stain_color",
                    };
            short[] fishClearStainTypes = {
                    AttrsMgr.VARC_TYPE,
            };
            
            String[] fishXRayAttrs = {
                    "film_no",
                    };
            short[] fishXRayTypes = {
                    AttrsMgr.VARC_TYPE,
            };
            
            String[] fishMiscAttrs = {
                    "misc",
                    };
            short[] fishMiscTypes = {
                    AttrsMgr.VARC_TYPE,
            };
            
            loadAttrDefs(AttrsMgr.FISH_DISCIPLINE, AttrsMgr.PREP_TABLE_TYPE, 0, fishEtOHAttrs, fishEtOHTypes);
            loadAttrDefs(AttrsMgr.FISH_DISCIPLINE, AttrsMgr.PREP_TABLE_TYPE, 1, fishSkelAttrs, fishSkelTypes);
            loadAttrDefs(AttrsMgr.FISH_DISCIPLINE, AttrsMgr.PREP_TABLE_TYPE, 2, fishClearStainAttrs, fishClearStainTypes);
            loadAttrDefs(AttrsMgr.FISH_DISCIPLINE, AttrsMgr.PREP_TABLE_TYPE, 3, fishTissueAttrs, fishTissueTypes);
            loadAttrDefs(AttrsMgr.FISH_DISCIPLINE, AttrsMgr.PREP_TABLE_TYPE, 4, fishXRayAttrs, fishXRayTypes);
            loadAttrDefs(AttrsMgr.FISH_DISCIPLINE, AttrsMgr.PREP_TABLE_TYPE, 5, fishMiscAttrs, fishMiscTypes);
            
            String[] birdPrepAttrs = {
                    "preparedDate",
                    "size",
                    "url",
                    "identifier",
                    "nestLining",
                    "nestMaterial",
                    "nestLocation",
                    "setMark",
                    "collectedEggCount",
                    "collectedParasiteEggCount",
                    "fieldEggCount",
                    "fieldParasiteEggCount",
                    "eggIncubationStage",
                    "eggDescription",
                    "nestCollected"
                    };
            
            short[] bird_types = {
                    AttrsMgr.DATE_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.MEMO_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.INT_TYPE,
                    AttrsMgr.INT_TYPE,
                    AttrsMgr.INT_TYPE,
                    AttrsMgr.INT_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.SHRT_TYPE};
            
            
            String[] prepAttrs = {
                    "preparationId",
                    "preparedDate",
                    "medium",
                    "mediumId",
                    "partInformation",
                    "startBoxNumber",
                    "endBoxNumber",
                    "startSlideNumber",
                    "endSlideNumber",
                    "sectionOrientation",
                    "sectionWidth",
                    "size",
                    "url",
                    "identifier",
                    "nestLining",
                    "nestMaterial",
                    "nestLocation",
                    "setMark",
                    "collectedEggCount",
                    "collectedParasiteEggCount",
                    "fieldEggCount",
                    "fieldParasiteEggCount",
                    "eggIncubationStage",
                    "eggDescription",
                    "format",
                    "storageInfo",
                    "preparationType",
                    "preparationTypeId",
                    "containerType",
                    "containerTypeId",
                    "dnaconcentration",
                    "volume",
                    "nestCollected",
                    "yesNo1",
                    "yesNo2",
                    "collectionObject",
                    "collectionObjectType",
                    "taxonName",
                    "agent",
                    "location" // from the Physical Record
                    };
            
            String[] bioAttrs = {
                    "sex", 
                    "age", 
                    "stage", 
                    "weight", 
                    "length", 
                    "gosnerStage", 
                    "snoutVentLength", 
                    "activity", 
                    "lengthTail", 
                    "reproductiveCondition", 
                    "condition", 
                    "lengthTarsus", 
                    "lengthWing", 
                    "lengthHead", 
                    "lengthBody", 
                    "lengthMiddleToe", 
                    "lengthBill", 
                    "totalExposedCulmen", 
                    "maxLength", 
                    "minLength", 
                    "lengthHindFoot", 
                    "lengthForeArm", 
                    "lengthTragus", 
                    "lengthEar", 
                    "earFromNotch", 
                    "wingspan", 
                    "lengthGonad", 
                    "widthGonad", 
                    "lengthHeadBody", 
                    "width", 
                    "heightFinalWhorl", 
                    "insideHeightAperture", 
                    "insideWidthAperture", 
                    "numberWhorls", 
                    "outerLipThickness", 
                    "mantle", 
                    "height", 
                    "diameter", 
                    "branchingAt", 
                    "remarks", 
                    "timestampModified", 
                    "timestampCreated", 
                    "lastEditedBy", 
                    "sexId", 
                    "stageId", 
            };
            short[] bioTypes = {
                    AttrsMgr.FLT_TYPE,
            };
            
            String[] fishBioAttrs = {
                    "sex", 
                    "weight", 
                    "length", 
                    "remarks", 
            };
            short[] fishBioTypes = {
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.MEMO_TYPE,
            };
             loadAttrDefs(AttrsMgr.FISH_DISCIPLINE, AttrsMgr.BIO_TABLE_TYPE, 0, fishBioAttrs, fishBioTypes);

            //------------------------------
            // Load Habtitat Attrs
            //------------------------------

            /*
            String[] habitatAttrs = {
                    "airTempC",
                    "waterTempC",
                    "waterpH",
                    "turbidity",
                    "clarity",
                    "salinity",
                    "soilType",
                    "soilPh",
                    "soilTempC",
                    "soilMoisture",
                    "slope",
                    "vegetation",
                    "habitatType",
                    "current",
                    "substrate",
                    "substrateMoisture",
                    "heightAboveGround",
                    "nearestNeighbor",
                    "remarks",
                    "minDepth",
                    "maxDepth",
                    "hostTaxonName"
                };
            short[] habitatTypes = {
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.MEMO_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.TAXON_TYPE};
            */
            String[] fishHabitatAttrs = {
                    "waterpH",
                    "turbidity",
                    "clarity",
                    "salinity",
                    "current",
                    "substrate",
                    "remarks",
                    "minDepth",
                    "maxDepth",
                };
            short[] fishHabitatTypes = {
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.VARC_TYPE,
                    AttrsMgr.MEMO_TYPE,
                    AttrsMgr.FLT_TYPE,
                    AttrsMgr.FLT_TYPE};
            
             loadAttrDefs(AttrsMgr.FISH_DISCIPLINE, AttrsMgr.HABITAT_TABLE_TYPE, 0, fishHabitatAttrs, fishHabitatTypes);
            
             
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
    /**
     * @param stmt
     * @param cmdStr
     * @return
     */
    public static int exeUpdateCmd(Statement stmt, String cmdStr)
    {
        try 
        {
            //System.out.println(cmdStr);
            return stmt.executeUpdate(cmdStr); 
            
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 
     */
    public static void createDataRecordsForNewSchema()
    {
        
        Connection connect = DBConnection.getConnection();
        
        boolean doCollectionObj            = true;   // Controls (parent of) doCollectionObjCopy && doingAndysFishDB
        boolean doCollectionObjCopy        = true;   // Combines and Converts the Old CollectionObject and CollectionObjectCatalog to the new table CollectionObj 
        boolean doingAndysFishDB           = false;  // Converts Info in the Determination Records over to PrepAttrs in Andy's Database
        boolean doPreps                    = false;  // Converts Physical CollectionObject Records to Preparation Records
        boolean loadingPrepAttrsForTesting = true;   // Creates test PrepAttrs for each Preparation record in the database
        

        
        if (doCollectionObj) 
        {
            try 
            {
 
                
                Statement stmt = connect.createStatement();
                StringBuffer str = new StringBuffer();
                
                if (doingAndysFishDB)
                {
                    System.out.println("***** Doing PrepAttrs for Andy");
                    
                    String[] fieldNames = {
                            "collectionobject.CollectionObjectID", 
                            "collectionobject.DerivedFromID", 
                            "collectionobject.ContainerID", 
                            "collectionobject.CollectionObjectTypeID", 
                            "collectionobject.FieldNumber", 
                            "collectionobject.CollectingEventID", 
                            "collectionobject.Description", 
                            "collectionobject.PreparationMethod", 
                            "collectionobject.ContainerType", 
                            "collectionobject.ContainerTypeID", 
                            "collectionobject.Text1", 
                            "collectionobject.Text2", 
                            "collectionobject.Number1", 
                            "collectionobject.Number2", 
                            "collectionobject.TimestampCreated", 
                            "collectionobject.TimestampModified", 
                            "collectionobject.LastEditedBy", 
                            "collectionobject.PreparationMethodID", 
                            "collectionobject.YesNo1", 
                            "collectionobject.YesNo2", 
                            "collectionobject.Count", 
                            "collectionobject.Remarks", 
                            "collectionobjectcatalog.CollectionObjectCatalogID", 
                            "collectionobjectcatalog.CollectionObjectTypeID", 
                            "collectionobjectcatalog.CatalogSeriesID", 
                            "collectionobjectcatalog.SubNumber", 
                            "collectionobjectcatalog.Name", 
                            "collectionobjectcatalog.Modifier", 
                            "collectionobjectcatalog.AccessionID", 
                            "collectionobjectcatalog.CatalogerID", 
                            "collectionobjectcatalog.CatalogedDate", 
                            "collectionobjectcatalog.Location", 
                            "collectionobjectcatalog.TimestampCreated", 
                            "collectionobjectcatalog.TimestampModified", 
                            "collectionobjectcatalog.LastEditedBy", 
                            "collectionobjectcatalog.Deaccessioned", 
                            "collectionobjectcatalog.CatalogNumber",
                            "determination.Confidence", 
                            "determination.Method", 
                            "determination.Text1", 
                            "determination.Number1" };
                    

                    // Searching for Logical Records
                    StringBuffer sql = new StringBuffer("Select ");
                    for (int ii=0;ii<fieldNames.length;ii++)
                    {
                        if (ii > 0) sql.append(", ");
                        sql.append(fieldNames[ii]);
                    }
                    sql.append(" From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID Inner Join determination ON determination.BiologicalObjectID = collectionobjectcatalog.CollectionObjectCatalogID Where collectionobject.DerivedFromID Is Null");
                    
                    String[] newNames = {"CollectionObjectID", "FieldNumber", "Description", "PreparationMethod", "ContainerType", "ContainerTypeID", 
                                         "Text1", "Text2", "Number1", "Number2", 
                                         "PreparationMethodID", "YesNo1", "YesNo2", "Count", "Remarks", "SubNumber", "Name", "Modifier", "CatalogedDate", 
                                         "TimestampCreated", "TimestampModified", "LastEditedBy", "Deaccessioned", "CatalogNumber", "CollectingEventID", 
                                         "CatalogSeriesID", "AccessionID", "CatalogerID", "ContainerID"};
                    
                    System.out.println("Number of Fields in CollectioObj "+newNames.length);
                    String sqlStr = sql.toString();
                    
                    
                    ResultSet rs = stmt.executeQuery(sqlStr);
                    
                    int prepAttrsID = 1;
                    
                    int count = 0;
                    while (rs.next()) 
                    {
                        String sexAttr  = "";
                        float  sizeAttr = 0.0F;
                        
                        Statement updateStatement = connect.createStatement();
                        
                        Date timeStamp = new Date();
                        Date modifiedDate  = new Date();

                        String confidence = rs.getString(fieldNames.length-3);
                        //Object method     = rs.getObject(fieldNames.length-2); // ??
                        String   text1      = rs.getString(fieldNames.length-1); // size
                        //float  number1    = rs.getFloat(fieldNames.length);    // ??
                        
                        if (doingAndysFishDB)
                        {
                            if (text1 != null)
                            {
                                exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "size", text1, (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modifiedDate, null, rs.getInt(1), null));
                                prepAttrsID++;
                            }
                            if (confidence != null)
                            {
                                exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "sex", confidence, (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modifiedDate, null, rs.getInt(1), null));                                
                                prepAttrsID++;
                            }
                        }
                        count++;
                        if (count % 1000 == 0) System.out.println(count);
                        
                        updateStatement.clearBatch();
                        updateStatement.close();
                    } // while
                    System.out.println("Processed "+count+" records.");
                    stmt.close();
                    
                    System.out.println("Added "+prepAttrsID+" PrepAttr Records");
               }
                
                
               if (doCollectionObjCopy)
               {
                   deleteAllRecordsFromTable("collectionobj");
                   

                   String[] fieldNames = {
                            "collectionobject.CollectionObjectID", 
                            "collectionobject.DerivedFromID", 
                            "collectionobject.ContainerID", 
                            "collectionobject.CollectionObjectTypeID", 
                            "collectionobject.FieldNumber", 
                            "collectionobject.CollectingEventID", 
                            "collectionobject.Description", 
                            "collectionobject.PreparationMethod", 
                            "collectionobject.ContainerType", 
                            "collectionobject.ContainerTypeID", 
                            "collectionobject.Text1", 
                            "collectionobject.Text2", 
                            "collectionobject.Number1", 
                            "collectionobject.Number2", 
                            "collectionobject.TimestampCreated", 
                            "collectionobject.TimestampModified", 
                            "collectionobject.LastEditedBy", 
                            "collectionobject.PreparationMethodID", 
                            "collectionobject.YesNo1", 
                            "collectionobject.YesNo2", 
                            "collectionobject.Count", 
                            "collectionobject.Remarks", 
                            "collectionobjectcatalog.CollectionObjectCatalogID", 
                            "collectionobjectcatalog.CollectionObjectTypeID", 
                            "collectionobjectcatalog.CatalogSeriesID", 
                            "collectionobjectcatalog.SubNumber", 
                            "collectionobjectcatalog.Name", 
                            "collectionobjectcatalog.Modifier", 
                            "collectionobjectcatalog.AccessionID", 
                            "collectionobjectcatalog.CatalogerID", 
                            "collectionobjectcatalog.CatalogedDate", 
                            "collectionobjectcatalog.Location", 
                            "collectionobjectcatalog.TimestampCreated", 
                            "collectionobjectcatalog.TimestampModified", 
                            "collectionobjectcatalog.LastEditedBy", 
                            "collectionobjectcatalog.Deaccessioned", 
                            "collectionobjectcatalog.CatalogNumber"};
    
                    // Searching for Logical Records
                    StringBuffer sql = new StringBuffer("");
                    sql.append("Select collectionobject.CollectionObjectID, collectionobject.DerivedFromID, collectionobject.ContainerID, collectionobject.CollectionObjectTypeID, collectionobject.FieldNumber, collectionobject.CollectingEventID, collectionobject.Description, collectionobject.PreparationMethod, collectionobject.ContainerType, collectionobject.ContainerTypeID, collectionobject.Text1, collectionobject.Text2, collectionobject.Number1, collectionobject.Number2, collectionobject.TimestampCreated, collectionobject.TimestampModified, collectionobject.LastEditedBy, collectionobject.PreparationMethodID, collectionobject.YesNo1, collectionobject.YesNo2, collectionobject.`Count`, collectionobject.Remarks, collectionobjectcatalog.CollectionObjectCatalogID, collectionobjectcatalog.CollectionObjectTypeID, collectionobjectcatalog.CatalogSeriesID, collectionobjectcatalog.SubNumber, collectionobjectcatalog.Name, collectionobjectcatalog.Modifier, collectionobjectcatalog.AccessionID, collectionobjectcatalog.CatalogerID, collectionobjectcatalog.CatalogedDate, collectionobjectcatalog.Location, collectionobjectcatalog.TimestampCreated, collectionobjectcatalog.TimestampModified, collectionobjectcatalog.LastEditedBy, collectionobjectcatalog.Deaccessioned, collectionobjectcatalog.CatalogNumber From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID Where collectionobject.DerivedFromID Is Null");
                    
                    
                    String[] newNames = {"CollectionObjectID",  "FieldNumber",   "Description",       "ContainerType",     "ContainerTypeID", 
                                         "Text1",               "Text2",         "Number1",           "Number2",           "YesNo1", 
                                         "YesNo2",              "Count",         "Remarks",           "SubNumber",         "Name",                
                                         "Modifier",            "CatalogedDate", "TimestampCreated",  "TimestampModified", "LastEditedBy", 
                                         "Deaccessioned",       "CatalogNumber", "CollectionObjectTypeID", "CollectingEventID", "CatalogSeriesID",   
                                         "AccessionID",         "CatalogerID",   "ContainerID"};

                    System.out.println("Number of Fields in CollectioObj "+newNames.length);
                    String sqlStr = sql.toString();
                    
                    
                    ResultSet rs = stmt.executeQuery(sqlStr);
                    
                    int prepAttrsID = 1;
                    
                    int count = 0;
                    while (rs.next()) 
                    {                    

                        
                        str.setLength(0);
                        str.append("INSERT INTO collectionobj VALUES (");
                        for (int i=0;i<newNames.length;i++)
                        {
                            if (i > 0) str.append(", ");
                            
                            int inx = getIndex(fieldNames, newNames[i]);
                            if (inx > -1)
                            {
                                Object dataObj = rs.getString(inx+1);
                                if (dataObj == null)
                                {
                                    str.append("NULL");
                                    
                                } else
                                {
                                    str.append("'");
                                    str.append(dataObj);
                                    str.append("'");
                                }
                            } else
                            {
                                System.out.println("Couldn't find ["+newNames[i]+"]");
                                stmt.close();
                                connect.close();
                                return;
                            }

                        }
                        str.append(")");
                        //System.out.println("\n"+str.toString());
                        if (count % 1000 == 0) System.out.println(count);
                        
                        try
                        {
                            Statement updateStatement = connect.createStatement();
                            updateStatement.executeUpdate(str.toString());
                            updateStatement.clearBatch();
                            
                        } catch (Exception e)
                        {
                            System.out.println("Count: "+count);
                            e.printStackTrace();
                        }
                        
                        count++;
                        //if (count == 1) break;
                    }
                    System.out.println("Processed "+count+" records.");
               }
                
            } catch (Exception e)
            {
                e.printStackTrace();
            }  
        }
            
        if (doPreps) 
        {
            System.out.println("****** Doing Preparations");
            int inx = -1;
            try 
            {
                Statement stmt = connect.createStatement();
              
                //------------------------------------------
                // Physical Records to PrepObjs
                //------------------------------------------
                /* create table PrepsObj (
                            PrepsObjID integer not null,
                            disciplineType integer,
                            tableType smallint,
                            fieldName varchar(32),
                            dataType smallint,
                            PreparationMethod varchar(50),
                            SubNumber integer,
                            PreparedDate integer,
                            TimestampCreated datetime,
                            TimestampModified datetime,
                            LastEditedBy varchar(50),
                            PhysicalObjectTypeID integer,
                            ParasiteTaxonNameID integer,
                            PreparedByID integer,
                            primary key (PrepsObjID)
                         )
                         */
                
                StringBuffer sql = new StringBuffer();
                sql.setLength(0);
                sql.append("Select collectionobject.CollectionObjectID, collectionobject.Count, collectionobject.Remarks, collectionobjectcatalog.Location, collectionobject.TimestampCreated, collectionobject.TimestampModified, collectionobject.LastEditedBy, collectionobject.FieldNumber, collectionobject.Description, collectionobject.PreparationMethod, collectionobject.DerivedFromID, collectionobjectcatalog.CatalogNumber, collectionobjectcatalog.Deaccessioned, collectionobjectcatalog.Location, collectionobjectcatalog.Name, collectionobjectcatalog.SubNumber, collectionobjectcatalog.CatalogSeriesID, collectionobjectcatalog.CollectionObjectTypeID, collectionobject.Remarks, collectionobject.PreparationMethodID,collectionobject.PreparationMethod From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID Where collectionobject.DerivedFromID Is Not Null Order By collectionobject.DerivedFromID Asc, collectionobjectcatalog.SubNumber Asc");
                ResultSet rs = stmt.executeQuery(sql.toString());
                
                String[] oldPrepFieldNames = {"collectionobject.CollectionObjID", 
                                              "collectionobject.Count", 
                                              "collectionobject.Remarks", 
                                              "collectionobjectcatalog.Location", 
                                              "collectionobject.TimestampCreated", 
                                              "collectionobject.TimestampModified", 
                                              "collectionobject.LastEditedBy", 
                                              "collectionobject.FieldNumber", 
                                              "collectionobject.Description", 
                                              "collectionobject.PreparationMethod", 
                                              "collectionobject.DerivedFromID", 
                                              "collectionobjectcatalog.CatalogNumber", 
                                              "collectionobjectcatalog.Deaccessioned", 
                                              "collectionobjectcatalog.Location", 
                                              "collectionobjectcatalog.Name", 
                                              "collectionobjectcatalog.SubNumber", 
                                              "collectionobjectcatalog.CatalogSeriesID", 
                                              "collectionobjectcatalog.CollectionObjectTypeID", 
                                              "collectionobject.Remarks", 
                                              "collectionobject.PreparationMethodID",
                                              "collectionobject.PreparationMethod"};
                String[] prepFieldNames = {
                "PrepsObjID",
                "PreparationMethod",
                "SubNumber",
                "Count",
                "Location",
                "Url",
                "Remarks",
                "PreparedDate",
                "TimestampCreated",
                "TimestampModified",
                "LastEditedBy",
                "PrepTypeID",
                "CollectionObjID",
                "PreparedByID"};

                int collectionObjID = 0;
                StringBuffer str = new StringBuffer();
                int count = 0;
                while (rs.next()) 
                {
                    int subNum = Integer.parseInt(rs.getString(getIndex(oldPrepFieldNames, "SubNumber")+1));
                    if (subNum == 1)
                    {
                        collectionObjID = Integer.parseInt(rs.getString(getIndex(oldPrepFieldNames, "DerivedFromID")+1));
                    }
                    
                    String method = rs.getString(getIndex(oldPrepFieldNames, "PreparationMethod")+1);
                    int prepTypeID = -1;
                    if (method != null)
                    {
                        Integer prepTypeInt = (Integer)prepTypeMapper.get(method);
                        if (prepTypeInt == null)
                        {
                            System.err.println("Couldn't Map PrepType["+method+"]");
                        } else
                        {
                            prepTypeID = prepTypeInt.intValue();
                        }
                    } else
                    {
                        System.err.println("Couldn't Map PrepType is null");
                        continue;
                    }
                    
                    str.setLength(0);
                    str.append("INSERT INTO PrepsObj VALUES (");
                    for (int i=0;i<prepFieldNames.length;i++)
                    {
                        if (i > 0) str.append(", ");
                        if (i == 0)
                        {
                            str.append("'");
                            str.append(rs.getString(1));
                            str.append("'");
                            
                        } else if (i == 5)
                        {
                            str.append("NULL");
                        } else if (i == 11)
                        {
                            if (prepTypeID != -1)
                            {
                                str.append("'");
                                str.append(prepTypeID);
                                str.append("'");
                            } else 
                            {
                                str.append("NULL");
                            }

                            
                        } else if (i == 12)
                        {
                            str.append("'");
                            str.append(Integer.toString(collectionObjID));
                            str.append("'");

                            
                        } else if (i == 7 || i == 13)
                        {
                            str.append("NULL");
                            
                        } else 
                        {
                            inx = getIndex(oldPrepFieldNames, prepFieldNames[i]);
                            if (inx > -1)
                            {
                                Object dataObj = rs.getString(inx+1);
                                if (dataObj == null)
                                {
                                    str.append("NULL");
                                    
                                } else
                                {
                                    str.append("'");
                                    str.append(dataObj);
                                    str.append("'");
                                }
                            } else
                            {
                                System.out.println("Couldn't find ["+prepFieldNames[i]+"]["+i+"]");
                                stmt.close();
                                connect.close();
                                return;
                            }
                        }
                    }
                    str.append(")");
                    //System.out.println("\n"+str.toString());
                    
                    try
                    {
                        Statement updateStatement = connect.createStatement();
                        updateStatement.executeUpdate(str.toString());
                        updateStatement.clearBatch();
                        updateStatement.close();
                        
                    } catch (Exception e)
                    {
                        System.out.println("Count: "+count);
                        e.printStackTrace();
                    }
                    
                    
                    if (count % 1000 == 0) System.out.println(count);
                    count++;
                    //if (count == 100) break;
                }
                
                stmt.close();
                
                
            } catch (Exception e)
            {
                System.out.println("inx: "+inx);
                e.printStackTrace();
            }
        }
        
        // For Testing
        // it fill all the attrs
        if (loadingPrepAttrsForTesting)
        {
            try 
            {
                 System.out.println("***** Doing PrepAttrs for Testing");
                 
                 Statement st = connect.createStatement();
                 ResultSet prepRS = st.executeQuery("Select PrepsObjID, SubNumber From prepsobj");
                 
                 
                 int prepAttrsID = 1;
                 
                 Random rand = new Random(133033);
                 
                 int count = 0;
                 while (prepRS.next()) 
                 {
                     int rsVal1 = prepRS.getInt(1);
                     int rsVal2 = prepRS.getInt(2);
                     
                     Statement updateStatement = connect.createStatement();
                     
                     Date timeStamp = new Date();
                     Date modified  = new Date();
                     
                     /*
                     +---------------------+--------------+------+-----+---------+-------+
                     | Field               | Type         | Null | Key | Default | Extra |
                     +---------------------+--------------+------+-----+---------+-------+
                     | PrepAttrsID         | int(11)      |      | PRI | 0       |       |
                     | name                | varchar(50)  | YES  |     | NULL    |       |
                     | strValue            | varchar(128) | YES  |     | NULL    |       |
                     | intValue            | int(11)      | YES  |     | NULL    |       |
                     | fieldType           | int(11)      | YES  |     | NULL    |       |
                     | unit                | int(11)      | YES  |     | NULL    |       |
                     | TimestampCreated    | datetime     | YES  |     | NULL    |       |
                     | TimestampModified   | datetime     | YES  |     | NULL    |       |
                     | Remarks             | longtext     | YES  |     | NULL    |       |
                     | PrepsObjID          | int(11)      | YES  | MUL | NULL    |       |
                     | ParasiteTaxonNameID | int(11)      | YES  | MUL | NULL    |       |
                     +---------------------+--------------+------+-----+---------+-------+
                     */
                     try
                     {
                         switch (rsVal2)
                         {
                                case 1 : // Ethonol
                                 {
                                     String randomSize = Integer.toString(rand.nextInt(99) + 1) + " " + (rand.nextInt() % 2 == 0 ? "mm" : "cm");
                                     exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "size", randomSize, (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modified, null, rsVal1, null));                                
                                     prepAttrsID++;
                                     exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "sex", (rand.nextInt() % 2 == 0 ? "Male" : "Female"), (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modified, null, rsVal1, null));                                
                                     prepAttrsID++;
                                     
                                 } break;
                                 case 2 : // Skeleton
                                 {
                                     String randomSize = Integer.toString(rand.nextInt(99) + 1) + " " + (rand.nextInt() % 2 == 0 ? "mm" : "cm");
                                     exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "size", randomSize, (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modified, null, rsVal1, null));                                
                                     prepAttrsID++;
                                     exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "sex", (rand.nextInt() % 2 == 0 ? "Male" : "Female"), (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modified, null, rsVal1, null));                                
                                     prepAttrsID++;
                                     
                                 } break;
                                 case 3 : // Tissue
                                 {
                                     exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "dna", "xx-xxx-xxx-xx-xx", (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modified, null, rsVal1, null));                                
                                     prepAttrsID++;
                                 } break;
                                 case 4 : // Clear and Stain
                                 {
                                     exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "stain_color", "blue", (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modified, null, rsVal1, null));                                
                                     prepAttrsID++;
                                 } break;
                                 case 5 : // X-Ray
                                 {
                                     exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "film_no", "112-32", (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modified, null, rsVal1, null));                                
                                     prepAttrsID++;
                                 } break;
                                 case 6 : // Misc
                                 {
                                     exeUpdateCmd(updateStatement, createPrepsInsert(prepAttrsID, "misc", "no data", (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modified, null, rsVal1, null));                                
                                     prepAttrsID++;
                                 } break;
                         } //switch 
                     
                     } catch (Exception e)
                     {
                         System.out.println("Count: "+count);
                         e.printStackTrace();
                     }
                     
                     prepAttrsID++;
                     count++;
                     if (count % 1000 == 0) System.out.println(count);
                     
                     updateStatement.clearBatch();
                     updateStatement.close();

                 } // while
                 System.out.println("Processed "+count+" records.");
                 st.close();
                 
                 System.out.println("Added "+prepAttrsID+" PrepAttr Records");
             
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        
        try
        {
            connect.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }        
    }
    /*
    +---------------------+--------------+------+-----+---------+-------+
    | Field               | Type         | Null | Key | Default | Extra |
    +---------------------+--------------+------+-----+---------+-------+
    | PrepAttrsID         | int(11)      |      | PRI | 0       |       |
    | name                | varchar(50)  | YES  |     | NULL    |       |
    | strValue            | varchar(128) | YES  |     | NULL    |       |
    | intValue            | int(11)      | YES  |     | NULL    |       |
    | fieldType           | int(11)      | YES  |     | NULL    |       |
    | unit                | int(11)      | YES  |     | NULL    |       |
    | TimestampCreated    | datetime     | YES  |     | NULL    |       |
    | TimestampModified   | datetime     | YES  |     | NULL    |       |
    | Remarks             | longtext     | YES  |     | NULL    |       |
    | PrepsObjID          | int(11)      | YES  | MUL | NULL    |       |
    | ParasiteTaxonNameID | int(11)      | YES  | MUL | NULL    |       |
    +---------------------+--------------+------+-----+---------+-------+
    */
    protected static String getStrValue(Object obj)
    {
        if (obj instanceof String)
        {
            return obj == null ? "null" : "'"+((String)obj)+"'";
            
        } else if (obj instanceof Integer)
        {
            return obj == null ? "null" : ((Integer)obj).toString();
            
        } else if (obj instanceof Date)
        {
            
            return obj == null ? "null" : "'"+dateFormatter.format((Date)obj) + "'";
            
        } else
        {
            return obj == null ? "null" : obj.toString();
        }
    }
    
    
    
    protected static String createPrepsInsert(int     prepAttrsID, 
                                       String  name,
                                       String  strValue, 
                                       Integer intValue, 
                                       Short   fieldType, 
                                       Short   unit, 
                                       Date    timeStamp, 
                                       Date    modifiedDate, 
                                       String  remarks, 
                                       int     prepsObjID,
                                       Integer parasiteTaxonNameID)
    {
        strBuf.setLength(0);
        strBuf.append("INSERT INTO prepattrs VALUES (");
        strBuf.append(prepAttrsID);
        strBuf.append(',');
        strBuf.append(getStrValue(name));
        strBuf.append(',');
        strBuf.append(getStrValue(strValue));
        strBuf.append(',');
        strBuf.append(getStrValue(intValue));
        strBuf.append(',');
        strBuf.append(getStrValue(fieldType));
        strBuf.append(',');
        strBuf.append(getStrValue(unit));
        strBuf.append(',');
        strBuf.append(getStrValue(timeStamp));
        strBuf.append(',');
        strBuf.append(getStrValue(modifiedDate));
        strBuf.append(',');
        strBuf.append(getStrValue(remarks));
        strBuf.append(',');
        strBuf.append(prepsObjID);
        strBuf.append(',');
        strBuf.append(parasiteTaxonNameID);
        strBuf.append(')');

        return strBuf.toString();
    }

    
    public static void loadData() throws Exception
    {
        Session session = HibernateUtil.getCurrentSession();
        try 
        {
            HibernateUtil.beginTransaction();
            
            /*Criteria criteria1 = session.createCriteria(CollectionObject.class).setFetchMode(CollectionObject.class.getName(), FetchMode.EAGER);
            java.util.List data1 = criteria1.list();
            System.out.println("Items to Delete: "+data1.size());
            
            for (Iterator iter=data1.iterator();iter.hasNext();)
            {
                CollectionObj newColObj = new CollectionObj();
                session.delete(newColObj);
            }
            session.flush();
            tx.commit();
            */
            

            Criteria criteria = session.createCriteria(CollectionObject.class).setMaxResults(300);
            criteria.add(Expression.isNull("derivedFromId"));
            
            
            java.util.List data = criteria.list();
            System.out.println("Items Returned: "+data.size());
            
            for (Iterator iter=data.iterator();iter.hasNext();)
            {
                CollectionObject        colObj    = (CollectionObject)iter.next();
                CollectionObjectCatalog colObjCat = colObj.getCollectionObjectCatalog();
                System.out.println(colObj.getCollectionObjectId());
                
                CollectionObj newColObj = new CollectionObj();
                
                
                /*newColObj.setAccession(colObjCat.getAccession());
                newColObj.setAgent(colObjCat.getAgent());
                newColObj.setBiologicalObjectAttribute(colObj.getBiologicalObjectAttribute());
                
                //Set set = colObj.getBiologicalObjectRelationsByRelatedBiologicalObjectId();
                newColObj.setBiologicalObjectRelationsByBiologicalObjectId(colObj.getBiologicalObjectRelationsByRelatedBiologicalObjectId());
                
                newColObj.setCatalogedDate(colObjCat.getCatalogedDate());
                newColObj.setCatalogNumber(colObjCat.getCatalogNumber());
                newColObj.setCatalogSery(colObjCat.getCatalogSery());
                newColObj.setCollectingEvent(colObj.getCollectingEvent());
                
                // Error newColObj.setCollectionObjectCitations(colObj.getCollectionObjectCitations());
                
                newColObj.setCollectionObjectId(colObj.getCollectionObjectId());
                //newColObj.setCollectionObjects()
                newColObj.setCollectionObjectType(colObj.getCollectionObjectType());
                newColObj.setContainerType(colObj.getContainerType());
                newColObj.setContainerTypeId(colObj.getContainerTypeId());
                newColObj.setCount(colObj.getCount());
                newColObj.setDeaccessionCollectionObjects(colObjCat.getDeaccessionCollectionObjects());
                newColObj.setDeaccessioned(colObjCat.getDeaccessioned());
                newColObj.setDerivedFromId(colObj.getDerivedFromId());
                newColObj.setDescription(colObj.getDescription());
                
                newColObj.setDeterminationsByBiologicalObjectId(colObj.getDeterminationsByBiologicalObjectId());
                newColObj.setDeterminationsByPreparationId(colObj.getDeterminationsByPreparationId());
                newColObj.setFieldNumber(colObj.getFieldNumber());
                newColObj.setImage(colObj.getImage());
                
                // Error newColObj.setImageCollectionObjects(colObj.getImageCollectionObjects());
                
                newColObj.setLastEditedBy(colObj.getLastEditedBy());
                newColObj.setLoanPhysicalObjects(colObjCat.getLoanPhysicalObjects());
                newColObj.setLocation(colObjCat.getLocation());
                newColObj.setModifier(colObjCat.getModifier());
                newColObj.setName(colObjCat.getName());
                newColObj.setNumber1(colObj.getNumber1());
                newColObj.setNumber2(colObj.getNumber2());
                
                // Error newColObj.setObservations(colObj.getObservations());
                
                newColObj.setOtherIdentifiers(colObjCat.getOtherIdentifiers());
                newColObj.setPreparation(colObj.getPreparation());
                newColObj.setPreparationMethod(colObj.getPreparationMethod());
                newColObj.setPreparationMethodId(colObj.getPreparationMethodId());
                newColObj.setProjectCollectionObjects(colObjCat.getProjectCollectionObjects());
                newColObj.setRemarks(colObj.getRemarks());
                newColObj.setSound(colObj.getSound());
                
                // Error newColObj.setSoundEventStorages(colObj.getSoundEventStorages());
                
                newColObj.setSubNumber(colObjCat.getSubNumber());
                newColObj.setText1(colObj.getText1());
                newColObj.setText2(colObj.getText2());
                newColObj.setTimestampCreated(colObj.getTimestampCreated());
                newColObj.setTimestampModified(colObj.getTimestampModified());
                newColObj.setYesNo1(colObj.getYesNo1());
                newColObj.setYesNo2(colObj.getYesNo2());
                */
                session.save(newColObj);
            }
            // We're done; make our changes permanent
            HibernateUtil.commitTransaction();

        } catch (Exception e) {
            System.err.println("******* " + e);
            HibernateUtil.rollbackTransaction();
            throw e;
            
        } finally {
            System.err.println("Closing Session.");
            // No matter what, close the session
            session.close();
        }
        
    }
    
    public static void doLoadAttrsMgr() throws Exception 
    {
        
        AttrsMgr attrMgr = new AttrsMgr();  
        
        System.out.println("BIO_TABLE_TYPE");
        attrMgr.getAttrDefs(AttrsMgr.FISH_DISCIPLINE, (short)0, AttrsMgr.BIO_TABLE_TYPE);
        
        System.out.println("HABITAT_TABLE_TYPE");
        attrMgr.getAttrDefs(AttrsMgr.FISH_DISCIPLINE, (short)0, AttrsMgr.HABITAT_TABLE_TYPE);
        
        System.out.println("PREP_TABLE_TYPE 0");
        attrMgr.getAttrDefs(AttrsMgr.FISH_DISCIPLINE, (short)0, AttrsMgr.PREP_TABLE_TYPE);
        
        System.out.println("PREP_TABLE_TYPE 1");
        attrMgr.getAttrDefs(AttrsMgr.FISH_DISCIPLINE, (short)1, AttrsMgr.PREP_TABLE_TYPE);
        
        System.out.println("PREP_TABLE_TYPE 2");
        attrMgr.getAttrDefs(AttrsMgr.FISH_DISCIPLINE, (short)2, AttrsMgr.PREP_TABLE_TYPE);

    }
    
    /**
     * Utility method to associate an artist with a catObj
     */
    //private static void addCatalogObjCollectionEvent(CatalogObj catObj, CollectionEvent artist) {
    //    catObj.getCollectionEvent().add(artist);
    //}

    public static void main(String args[]) throws Exception 
    {
        DBConnection.setUsernamePassword("rods", "rods");
        DBConnection.setDriver("com.mysql.jdbc.Driver");
        DBConnection.setDBName("jdbc:mysql://localhost/demo_fish2");
        
        boolean doingHibernate = false;
        if (doingHibernate) 
        {
           
            loadData();

            System.out.println("Done.");
        } else
        {
            loadAttrs();
            createDataRecordsForNewSchema();
            //doLoadAttrsMgr();
        }
    }
}

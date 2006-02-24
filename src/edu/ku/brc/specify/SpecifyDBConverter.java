package edu.ku.brc.specify;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import edu.ku.brc.specify.conversion.FishConversion;
import edu.ku.brc.specify.conversion.GenericDBConversion;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.User;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.dbsupport.ResultsPager;

/**
 * Create more sample data, letting Hibernate persist it for us.
 */
public class SpecifyDBConverter 
{
    protected static Log log = LogFactory.getLog(SpecifyDBConverter.class);
    
    protected static Hashtable<String, Integer> prepTypeMapper    = new Hashtable<String, Integer>();
    protected static int                        attrsId           = 0;
    protected static SimpleDateFormat           dateFormatter     = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static StringBuffer               strBuf            = new StringBuffer("");
    protected static Calendar                   calendar          = Calendar.getInstance();
    
    public SpecifyDBConverter()
    {

    }
    
     /**
     * @param oldNames xxxxx
     * @param newName xxxx
     * @return xxxx
     */
    /*public static int getIndex(String[] oldNames, String newName)
    {
        for (int i=0;i<oldNames.length;i++)
        {
            String fieldName = oldNames[i].substring(oldNames[i].indexOf(".")+1, oldNames[i].length());
            //log.info(fieldName
            if (newName.equals(fieldName))
            {
                return i;
            }
        }
        return -1;
    }*/
    
   
    /**
     * @param id  xxxx
     * @param name xxxx
     * @return xxxx
     */
    /*public static PrepTypes loadPrepType(final int id, final String name)
    {
        try 
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            

            PrepTypes prepType = new PrepTypes(new Integer(id+1));
            prepType.setName(name);
            prepType.setPreparation(null);
            
            session.save(prepType);
            
            HibernateUtil.commitTransaction();
            
            return prepType;
            
        } catch (Exception e) 
        {
            System.err.println("******* " + e);
            HibernateUtil.rollbackTransaction();
        } 
        return null;
    }*/
    
    /**
     * @param discipline xxxx
     * @param tableType xxxx
     * @param subType xxxx
     * @param attrNames xxxx
     * @param dataTypes xxxx
     * @return xxxx
     */
    /*public static List loadAttrDefs(final short    discipline, 
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
            log.info("Names length: " + attrNames.length + " doesn't match Types length "
                    + dataTypes.length);
        }
        return null;
    }*/
    
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
          log.info("Names length: "+aAttrNames.length+" doesn't match Types length "+aTypes.length);
      }
    }*/
    
    
    /*
     * 
     */
    /*public static void loadAttrs()
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
        
    }*/
    
    /**
     * 
     */
    /*public static void createDataRecordsForNewSchema()
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
                    log.info("***** Doing PrepAttrs for Andy");
                    
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
                    
                    log.info("Number of Fields in CollectioObj "+newNames.length);
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
                        if (count % 1000 == 0) log.info(count);
                        
                        updateStatement.clearBatch();
                        updateStatement.close();
                    } // while
                    log.info("Processed "+count+" records.");
                    stmt.close();
                    
                    log.info("Added "+prepAttrsID+" PrepAttr Records");
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

                    log.info("Number of Fields in CollectioObj "+newNames.length);
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
                                log.info("Couldn't find ["+newNames[i]+"]");
                                stmt.close();
                                connect.close();
                                return;
                            }

                        }
                        str.append(")");
                        //log.info("\n"+str.toString());
                        if (count % 1000 == 0) log.info(count);
                        
                        try
                        {
                            Statement updateStatement = connect.createStatement();
                            updateStatement.executeUpdate(str.toString());
                            updateStatement.clearBatch();
                            
                        } catch (Exception e)
                        {
                            log.info("Count: "+count);
                            e.printStackTrace();
                        }
                        
                        count++;
                        //if (count == 1) break;
                    }
                    log.info("Processed "+count+" records.");
               }
                
            } catch (Exception e)
            {
                e.printStackTrace();
            }  
        }
            
        if (doPreps) 
        {
            log.info("****** Doing Preparations");
            int inx = -1;
            try 
            {
                Statement stmt = connect.createStatement();
              
                //------------------------------------------
                // Physical Records to PrepObjs
                //------------------------------------------
                
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
                                log.info("Couldn't find ["+prepFieldNames[i]+"]["+i+"]");
                                stmt.close();
                                connect.close();
                                return;
                            }
                        }
                    }
                    str.append(")");
                    //log.info("\n"+str.toString());
                    
                    try
                    {
                        Statement updateStatement = connect.createStatement();
                        updateStatement.executeUpdate(str.toString());
                        updateStatement.clearBatch();
                        updateStatement.close();
                        
                    } catch (Exception e)
                    {
                        log.info("Count: "+count);
                        e.printStackTrace();
                    }
                    
                    
                    if (count % 1000 == 0) log.info(count);
                    count++;
                    //if (count == 100) break;
                }
                
                stmt.close();
                
                
            } catch (Exception e)
            {
                log.info("inx: "+inx);
                e.printStackTrace();
            }
        }
        
        // For Testing
        // it fill all the attrs
        if (loadingPrepAttrsForTesting)
        {
            try 
            {
                 log.info("***** Doing PrepAttrs for Testing");
                 
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
                         log.info("Count: "+count);
                         e.printStackTrace();
                     }
                     
                     prepAttrsID++;
                     count++;
                     if (count % 1000 == 0) log.info(count);
                     
                     updateStatement.clearBatch();
                     updateStatement.close();

                 } // while
                 log.info("Processed "+count+" records.");
                 st.close();
                 
                 log.info("Added "+prepAttrsID+" PrepAttr Records");
             
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
    }*/

    
    
    /*
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
    }*/

    
    public static void loadData() throws Exception
    {
        Session session = HibernateUtil.getCurrentSession();
        try 
        {
            HibernateUtil.beginTransaction();
            
            /*Criteria criteria1 = session.createCriteria(CollectionObject.class).setFetchMode(CollectionObject.class.getName(), FetchMode.EAGER);
            java.util.List data1 = criteria1.list();
            log.info("Items to Delete: "+data1.size());
            
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
            log.info("Items Returned: "+data.size());
            
            for (Iterator iter=data.iterator();iter.hasNext();)
            {
                //CollectionObject        colObj    = (CollectionObject)iter.next();
                /*
                CollectionObjectCatalog colObjCat = colObj.getCollectionObjectCatalog();
                log.info(colObj.getCollectionObjectId());
                
                CollectionObj newColObj = new CollectionObj();
                */
                
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
               
                session.save(newColObj);
                 */
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
        DBConnection.setDBName("jdbc:mysql://localhost/demo_fish3");
        
        boolean doingHibernate = false;
        if (doingHibernate) 
        {
           
            //loadData();
            
            BasicSQLUtils.cleanAllTables();

            
        } else
        {
            //loadAttrs();
            //createDataRecordsForNewSchema();
            //doLoadAttrsMgr();
            
            BasicSQLUtils.cleanAllTables();
            
            boolean doConvert = true;
            if (doConvert)
            {
                GenericDBConversion conversion = new GenericDBConversion("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/demo_fish2", "rods", "rods");
                
                boolean copyTables = true;
                if (copyTables)
                {              
                    conversion.copyTables();
                    conversion.createCollectionRecords();
                    conversion.convertTaxon(); 
                    conversion.convertLocality();
                }
                
                BasicSQLUtils.deleteAllRecordsFromTable("datatype");
                BasicSQLUtils.deleteAllRecordsFromTable("usysusers");
                BasicSQLUtils.deleteAllRecordsFromTable("collectionobjdef");
                
                DataType          dataType  = conversion.createDataTypes("Animal");        
                User              user      = conversion.createNewUser("rods", "rods", 0);
                
                Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CatalogSeries.class);
                criteria.add(Expression.eq("catalogSeriesId", new Integer(0)));
                java.util.List catalogSeriesList = criteria.list();
                if (catalogSeriesList.size() > 0)
                {
                    
                    Set<Object>  colObjDefSet = conversion.createCollectionObjDef("Fish", dataType, user, null, (CatalogSeries)catalogSeriesList.get(0));
                    
        
                    Object obj = colObjDefSet.iterator().next();
                    CollectionObjDef colObjDef = (CollectionObjDef)obj;
                    FishConversion fishConversion = new FishConversion(colObjDef);
                    fishConversion.loadAttrs(true);
                    
                    
                    DBConnection oldDB     = DBConnection.createInstance("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/demo_fish2", "rods", "rods");
                    Connection   oldDBConn = oldDB.getConnectionToDB();
                    fishConversion.loadPrepAttrs(oldDBConn, DBConnection.getConnection());
                    oldDBConn.close();
                } else
                {
                    log.error("Error: No Catalog Series!");
                }
            }
            
            boolean testPaging = false;
            if (testPaging)
            {
                /*
                HibernatePage.setDriverName("com.mysql.jdbc.Driver");
                
                int pageNo = 1;
                Pagable page = HibernatePage.getHibernatePageInstance(HibernateUtil.getCurrentSession().createQuery("from collectionobject in class CollectionObject"), 0, 100);
                log.info("Number Pages: "+page.getLastPageNumber());
                int cnt = 0;
                for (Object list : page.getThisPageElements()) 
                {
                    //cnt += list.size();
                    
                    log.info("******************** Page "+pageNo++);
                }
                */
                ResultsPager pager = new ResultsPager(HibernateUtil.getCurrentSession().createQuery("from collectionobject in class CollectionObject"), 0, 10);
                //ResultsPager pager = new ResultsPager(HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class), 0, 100);
                int pageNo = 1;
                do
                {
                    long start = System.currentTimeMillis();
                    List list = pager.getList();
                    log.info("******************** Page "+pageNo+" "+(System.currentTimeMillis() - start));
                    pageNo++;
                    
                    for (Object co : list)
                    {
                        if (pageNo % 100 == 0)
                        {
                            log.info(((CollectionObject)co).getCatalogNumber());
                        }
                    }
                } while (pager.isNextPage());
                
            }

            
        }
        log.info("Done.");
    }
}

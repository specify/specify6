/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.deleteAllRecordsFromTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.PrepType;

/**
 * Used for Converting a Fish Database
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class FishConversion 
{
    protected static final Logger log = Logger.getLogger(FishConversion.class);
    
    protected static Hashtable<String, Integer> prepTypeMapper    = new Hashtable<String, Integer>();
    protected static int                        attrsId           = 0;
    protected static SimpleDateFormat           dateFormatter     = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static StringBuilder               strBuf            = new StringBuilder("");
    protected static Calendar                   calendar          = Calendar.getInstance();
    
    protected Discipline                  discipline;
    
    /**
     * 
     */
    public FishConversion(final Discipline discipline)
    {
        this.discipline = discipline;
    }
    
    /*
     * Cleans all the Attribute tables and recreates them
     */
    public void loadAttrs(final boolean cleanTables, Map<String, PrepType> prepTypesMap)
    {
        
        if (cleanTables) 
        {
            deleteAllRecordsFromTable("attrsdef", BasicSQLUtils.myDestinationServerType);
            deleteAllRecordsFromTable("prepattr", BasicSQLUtils.myDestinationServerType);
        }

        //------------------------------
        // Load PrepTypes and Prep Attrs
        //------------------------------

        
        String[] fishEtOHAttrs = {
                "size",
                "sex",
                };
        short[] fishEtOHTypes = {
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
        };
        String[] fishSkelAttrs = {
                "size",
                };
        short[] fishSkelTypes = {
                AttributeIFace.FieldType.StringType.getType(),
        };
        
        String[] fishTissueAttrs = {
                "dna",
                };
        short[] fishTissueTypes = {
                AttributeIFace.FieldType.StringType.getType(),
        };
        
        String[] fishClearStainAttrs = {
                "stain_color",
                };
        short[] fishClearStainTypes = {
                AttributeIFace.FieldType.StringType.getType(),
        };
        
        String[] fishXRayAttrs = {
                "film_no",
                };
        short[] fishXRayTypes = {
                AttributeIFace.FieldType.StringType.getType(),
        };
        
        String[] fishMiscAttrs = {
                "misc",
                };
        short[] fishMiscTypes = {
                AttributeIFace.FieldType.StringType.getType(),
        };
        
        AttrUtils.loadAttrDefs(discipline, GenericDBConversion.TableType.Preparation, null, fishEtOHAttrs, fishEtOHTypes);
        AttrUtils.loadAttrDefs(discipline, GenericDBConversion.TableType.Preparation, null, fishSkelAttrs, fishSkelTypes);
        AttrUtils.loadAttrDefs(discipline, GenericDBConversion.TableType.Preparation, null, fishClearStainAttrs, fishClearStainTypes);
        AttrUtils.loadAttrDefs(discipline, GenericDBConversion.TableType.Preparation, null, fishTissueAttrs, fishTissueTypes);
        AttrUtils.loadAttrDefs(discipline, GenericDBConversion.TableType.Preparation, null, fishXRayAttrs, fishXRayTypes);
        AttrUtils.loadAttrDefs(discipline, GenericDBConversion.TableType.Preparation, null, fishMiscAttrs, fishMiscTypes);
        
        /*
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
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.MemoType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.IntegerType.getType(),
                AttributeIFace.FieldType.IntegerType.getType(),
                AttributeIFace.FieldType.IntegerType.getType(),
                AttributeIFace.FieldType.IntegerType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
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
                "storage" // from the Physical Record
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
                AttributeIFace.FieldType.FloatType.getType(),
        };
        */
        String[] fishBioAttrs = {
                "sex", 
                "weight", 
                "length", 
                "remarks", 
        };
        short[] fishBioTypes = {
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.FloatType.getType()//,
                //AttributeIFace.FieldType.MemoType.getType(),
        };
        AttrUtils.loadAttrDefs(discipline, GenericDBConversion.TableType.CollectionObject, null, fishBioAttrs, fishBioTypes);

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
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.MemoType,
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.FloatType.getType(),
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
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                AttributeIFace.FieldType.StringType.getType(),
                //AttributeIFace.FieldType.MemoType.getType(),
                AttributeIFace.FieldType.FloatType.getType(),
                AttributeIFace.FieldType.FloatType.getType()};
        
        AttrUtils.loadAttrDefs(discipline, GenericDBConversion.TableType.CollectingEvent, null, fishHabitatAttrs, fishHabitatTypes);
    }
    
    /**
     * 
     */
    public void loadPrepAttrs(final Connection oldDBConn, final Connection newDBConn)
    {        
        try 
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            
            StringBuilder sqlStr = new StringBuilder("Select collectionobject.CollectionObjectID, determination.Confidence, determination.Text1");
            sqlStr.append(" From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID Inner Join determination ON determination.BiologicalObjectID = collectionobjectcatalog.CollectionObjectCatalogID Where collectionobject.DerivedFromID Is Null");
        
            
            ResultSet rs = stmt.executeQuery(sqlStr.toString());
            
            int  prepAttrsID  = 1;
            Date timeStamp    = new Date();
            Date modifiedDate = new Date();
            
            int count = 0;
            while (rs.next()) 
            {
               
                Statement updateStatement = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                //BasicSQLUtils.exeUpdateCmd(updateStatement, "SET FOREIGN_KEY_CHECKS = 0");
                if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
                {
                    BasicSQLUtils.removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);
                }
                String sex  = rs.getString(2); // sex
                String size = rs.getString(3); // size
                
                /*PrepAttrs prepsType = AttrUtils.createPrepsInsert("sex",
                        sex,  // String value
                        null, // Integer value
                        null, // fieldType,
                        AttributeIFace.FieldType.StringType.getType(), 
                        null,
                        null);*/
                
                if (size != null)
                {
                    BasicSQLUtils.exeUpdateCmd(updateStatement, AttrUtils.createPrepsInsert(prepAttrsID, "size", size, (Integer)null, AttributeIFace.FieldType.StringType.getType(), null, timeStamp, modifiedDate, null, rs.getInt(1), null));
                    prepAttrsID++;
                }
                
                if (sex != null)
                {
                    BasicSQLUtils.exeUpdateCmd(updateStatement, AttrUtils.createPrepsInsert(prepAttrsID, "sex", sex, (Integer)null, AttributeIFace.FieldType.StringType.getType(), null, timeStamp, modifiedDate, null, rs.getInt(1), null));                                
                    prepAttrsID++;
                }
                
                count++;
                if (count % 1000 == 0) log.info("Processed "+count);
                
                updateStatement.clearBatch();
                updateStatement.close();
                
            } // while
            System.out.println("Processed "+count+" records.");
            stmt.close();
            
        } catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FishConversion.class, e);
            e.printStackTrace();
            log.error(e);
        } 

       
    }

}

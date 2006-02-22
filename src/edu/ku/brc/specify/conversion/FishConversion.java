/*
 * Filename:    $RCSfile: FishConversion.java,v $
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

import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.deleteAllRecordsFromTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.dbsupport.AttrsMgr;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils;

/**
 * Used for Converting a Fish Database
 */
public class FishConversion 
{
    protected static Log log = LogFactory.getLog(FishConversion.class);
    
    protected static Hashtable<String, Integer> prepTypeMapper    = new Hashtable<String, Integer>();
    protected static int                        attrsId           = 0;
    protected static SimpleDateFormat           dateFormatter     = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static StringBuilder               strBuf            = new StringBuilder("");
    protected static Calendar                   calendar          = Calendar.getInstance();
    
    protected CollectionObjDef                  collectionObjDef;
    
    /**
     * 
     */
    public FishConversion(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDef = collectionObjDef;
    }
    
  
   
    /*
     * Cleans all the Attribute tables and recreates them
     */
    public void loadAttrs(final boolean cleanTables)
    {
        
        if (cleanTables) 
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
            AttrUtils.loadPrepType(i+1, fishPrepTypes[i]);
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
        
        AttrUtils.loadAttrDefs(collectionObjDef, AttrsMgr.PREP_TABLE_TYPE, 0, fishEtOHAttrs, fishEtOHTypes);
        AttrUtils.loadAttrDefs(collectionObjDef, AttrsMgr.PREP_TABLE_TYPE, 1, fishSkelAttrs, fishSkelTypes);
        AttrUtils.loadAttrDefs(collectionObjDef, AttrsMgr.PREP_TABLE_TYPE, 2, fishClearStainAttrs, fishClearStainTypes);
        AttrUtils.loadAttrDefs(collectionObjDef, AttrsMgr.PREP_TABLE_TYPE, 3, fishTissueAttrs, fishTissueTypes);
        AttrUtils.loadAttrDefs(collectionObjDef, AttrsMgr.PREP_TABLE_TYPE, 4, fishXRayAttrs, fishXRayTypes);
        AttrUtils.loadAttrDefs(collectionObjDef, AttrsMgr.PREP_TABLE_TYPE, 5, fishMiscAttrs, fishMiscTypes);
        
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
        */
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
        AttrUtils.loadAttrDefs(collectionObjDef, AttrsMgr.BIO_TABLE_TYPE, 0, fishBioAttrs, fishBioTypes);

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
        
        AttrUtils.loadAttrDefs(collectionObjDef, AttrsMgr.HABITAT_TABLE_TYPE, 0, fishHabitatAttrs, fishHabitatTypes);
    }
    
    /**
     * 
     */
    public void loadPrepAttrs(final Connection oldDBConn, final Connection newDBConn)
    {        
        try 
        {
            Statement stmt = oldDBConn.createStatement();
            
            List<String> oldFieldNames = new ArrayList<String>();
            
            StringBuilder sqlStr = new StringBuilder("Select collectionobject.CollectionObjectID, determination.Confidence, determination.Text1");
            sqlStr.append(" From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID Inner Join determination ON determination.BiologicalObjectID = collectionobjectcatalog.CollectionObjectCatalogID Where collectionobject.DerivedFromID Is Null");
        
            
            ResultSet rs = stmt.executeQuery(sqlStr.toString());
            
            int  prepAttrsID  = 1;
            Date timeStamp    = new Date();
            Date modifiedDate = new Date();
            
            Date date = new Date();
            int count = 0;
            while (rs.next()) 
            {
               
                Statement updateStatement = newDBConn.createStatement();
                
                String sex  = rs.getString(2); // sex
                String size = rs.getString(3); // size
                
                /*PrepAttrs prepsType = AttrUtils.createPrepsInsert("sex",
                        sex,  // String value
                        null, // Integer value
                        null, // fieldType,
                        AttrsMgr.VARC_TYPE, 
                        null,
                        null);*/
                
                if (size != null)
                {
                    BasicSQLUtils.exeUpdateCmd(updateStatement, AttrUtils.createPrepsInsert(prepAttrsID, "size", size, (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modifiedDate, null, rs.getInt(1), null));
                    prepAttrsID++;
                }
                
                if (sex != null)
                {
                    BasicSQLUtils.exeUpdateCmd(updateStatement, AttrUtils.createPrepsInsert(prepAttrsID, "sex", sex, (Integer)null, AttrsMgr.VARC_TYPE, null, timeStamp, modifiedDate, null, rs.getInt(1), null));                                
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
            e.printStackTrace();
            log.error(e);
        } 

       
    }

}

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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
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
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils.FieldMetaData;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.tests.ObjCreatorHelper;
import edu.ku.brc.specify.ui.db.PickList;
import edu.ku.brc.specify.ui.db.PickListItem;
import edu.ku.brc.util.Pair;

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

    protected IdMapperMgr  idMapperMgr;
    protected DBConnection oldDB;


    // Helps during debuggin
    protected static boolean shouldCreateMapTables = true;
    protected static boolean shouldDeleteMapTables = false;

    /**
     * Default Constructor
     *
     */
    public GenericDBConversion()
    {
        idMapperMgr = IdMapperMgr.getInstance();
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
        this.idMapperMgr  = IdMapperMgr.getInstance();
        this.oldDB        = DBConnection.createInstance(oldDriver, oldDBName, oldUserName, oldPassword);
    }

    /**
     *
     */
    public void mapIds()
    {
        String[] tableNamesX =
        {
                "Agent"
        };

        String[] tableNames =
        {
        "Accession",
        "AccessionAgents",
        "AccessionAuthorizations",
        //"Address",
        //"Agent",
        //"AgentAddress",
        "Authors",
        "BiologicalObjectAttributes",
        "BiologicalObjectRelation",
        "BiologicalObjectRelationType",
        "Borrow",
        "BorrowAgents",
        "BorrowMaterial",
        "BorrowReturnMaterial",
        "BorrowShipments",
        "CatalogSeries",
        "CatalogSeriesDefinition",
        "CollectingEvent",
        //"Collection",
        "CollectionObject",
        //"CollectionObjectCatalog",
        "CollectionObjectCitation",
        "CollectionObjectType",
        "CollectionTaxonomyTypes",
        "Collectors",
        "Deaccession",
        "DeaccessionAgents",
        "DeaccessionCollectionObject",
        "Determination",
        "DeterminationCitation",
        "ExchangeIn",
        "ExchangeOut",
        //"Geography",
        "GeologicTimeBoundary",
        "GeologicTimePeriod",
        "GroupPersons",
        "Habitat",
        "ImageAgents",
        "ImageCollectionObjects",
        "ImageLocalities",
        "Journal",
        "Loan",
        "LoanAgents",
        "LoanPhysicalObject",
        "LoanReturnPhysicalObject",
        "Locality",
        "LocalityCitation",
        "Observation",
        "OtherIdentifier",
        "Permit",
        //"Preparation",
        "Project",
        "ProjectCollectionObjects",
        "ReferenceWork",
        "Shipment",
        "Sound",
        "SoundEventStorage",
        "Stratigraphy",
        "TaxonCitation",
        "TaxonName",
        "TaxonomicUnitType",
        "TaxonomyType"
        };

        for (String tableName : tableNames)
        {
            IdMapper idMapper = idMapperMgr.addMapper(tableName, tableName+"ID");
            if (shouldCreateMapTables)
                idMapper.mapAllIds();
        }
        
        // Create the mappers here, but fill them in during the AgentAddress Process
        IdMapper agentIDMapper     = idMapperMgr.addMapper("agent", "AgentID");
        IdMapper addrIDMapper      = idMapperMgr.addMapper("address", "AddressID");
        IdMapper agentAddrIDMapper = idMapperMgr.addMapper("agentaddress", "AgentAddressID");

        // Map all the Logical IDs
        IdMapper idMapper  = idMapperMgr.addMapper("collectionobject", "CollectionObjectID");
        if (shouldCreateMapTables)
        {
            idMapper.mapAllIds("select CollectionObjectID from collectionobject Where collectionobject.DerivedFromID Is Null order by CollectionObjectID");
        }

        // Map all the Physical IDs
        idMapper = idMapperMgr.addMapper("preparation", "PreparationID");
        if (shouldCreateMapTables)
        {
            idMapper.mapAllIds("select CollectionObjectID from collectionobject Where not (collectionobject.DerivedFromID Is Null) order by CollectionObjectID");
        }

        // Map all the Physical IDs
        idMapper = idMapperMgr.addMapper("geography", "GeographyID");
        if (shouldCreateMapTables)
        {
            idMapper.mapAllIds("SELECT DISTINCT GeographyID,ContinentOrOcean,Country,State,County,IslandGroup,Island,WaterBody,Drainage,FullGeographicName from " +
                                "geography ORDER BY ContinentOrOcean,Country,State,County");
        }

        String[] mappings = {
            "BorrowReturnMaterial", "BorrowMaterialID", "BorrowMaterial", "BorrowMaterialID",
            "BorrowReturnMaterial", "ReturnedByID", "Agent", "AgentID",

            //"Preparation", "PhysicalObjectTypeID", "PhysicalObjectType", "PhysicalObjectTypeID",
            "Preparation", "PreparedByID", "Agent", "AgentID",
            "Preparation", "ParasiteTaxonNameID", "TaxonName", "TaxonNameID",
            //"Preparation", "PreparationTypeID", "PreparationType", "PreparationTypeID",
            //"Preparation", "ContainerTypeID", "ContainerType", "ContainerTypeID",

            "LoanPhysicalObject", "PhysicalObjectID", "CollectionObject", "CollectionObjectID",
            "LoanPhysicalObject", "LoanID", "Loan", "LoanID",

            // ??? "ExchangeIn", "CollectionID", "Collection", "CollectionID",
            "ExchangeIn", "ReceivedFromOrganizationID", "Agent", "AgentID",
            "ExchangeIn", "CatalogedByID", "Agent", "AgentID",

            // ??? "Geography", "CurrentID", "Current", "CurrentID",

            "Collection", "OrganizationID", "Agent", "AgentID",

            "GroupPersons", "GroupID", "Agent", "AgentID",
            "GroupPersons", "MemberID", "Agent", "AgentID",

            // ??? "ExchangeOut", "CollectionID", "Collection", "CollectionID",
            "ExchangeOut", "SentToOrganizationID", "Agent", "AgentID",
            "ExchangeOut", "CatalogedByID", "Agent", "AgentID",
            "ExchangeOut", "ShipmentID", "Shipment", "ShipmentID",

            //"ImageLocalities", "ImageID", "Image", "ImageID",
            //"ImageLocalities", "LocalityID", "Locality", "LocalityID",

            //"ImageCollectionObjects", "ImageID", "Image", "ImageID",
            //"ImageCollectionObjects", "CollectionlObjectID", "CollectionObject", "CollectionObjectID",

            "ReferenceWork", "JournalID", "Journal", "JournalID",
            "ReferenceWork", "ContainingReferenceWorkID", "ReferenceWork", "ReferenceWorkID",

            //"ImageAgents", "ImageID", "Image", "ImageID",
           // "ImageAgents", "AgentID", "Agent", "AgentID",

            "BiologicalObjectRelation", "BiologicalObjectID",             "CollectionObject", "CollectionObjectID",
            "BiologicalObjectRelation", "RelatedBiologicalObjectID",      "CollectionObject", "CollectionObjectID",
            "BiologicalObjectRelation", "BiologicalObjectRelationTypeID", "BiologicalObjectRelationType", "BiologicalObjectRelationTypeID",

            //"SoundEventStorage", "SoundEventID", "SoundEvent", "SoundEventID",
            //"SoundEventStorage", "SoundRecordingID", "SoundRecording", "SoundRecordingID",

            "Shipment", "ShipperID", "AgentAddress", "AgentAddressID",
            "Shipment", "ShippedToID", "AgentAddress", "AgentAddressID",
            "Shipment", "ShippedByID", "Agent", "AgentID",
            //"Shipment", "ShipmentMethodID", "ShipmentMethod", "ShipmentMethodID",

            // ??? "Habitat", "BiologicalObjectTypeCollectedID", "BiologicalObjectTypeCollected", "BiologicalObjectTypeCollectedID",
            "Habitat", "HostTaxonID", "TaxonName", "TaxonNameID",
            //"Habitat", "HabitatTypeID", "HabitatType", "HabitatTypeID",

            "Authors", "AgentID", "Agent", "AgentID",
            "Authors", "ReferenceWorkID", "ReferenceWork", "ReferenceWorkID",

            "BorrowMaterial",  "BorrowID", "Borrow", "BorrowID",

            "BorrowShipments", "BorrowID", "Borrow", "BorrowID",
            "BorrowShipments", "ShipmentID", "Shipment", "ShipmentID",

            "BorrowAgents",    "BorrowID", "Borrow", "BorrowID",
            "BorrowAgents",    "AgentAddressID", "AgentAddress", "AgentAddressID",
            //"BorrowAgents",    "RoleID", "Role", "RoleID",

            "DeaccessionCollectionObject", "DeaccessionID", "Deaccession", "DeaccessionID",
            "DeaccessionCollectionObject", "CollectionObjectID", "CollectionObject", "CollectionObjectID",

            "CollectionObjectCitation", "ReferenceWorkID", "ReferenceWork", "ReferenceWorkID",
            "CollectionObjectCitation", "BiologicalObjectID", "CollectionObject", "CollectionObjectID",

            "Stratigraphy", "GeologicTimePeriodID", "GeologicTimePeriod", "GeologicTimePeriodID",

            //"Deaccession", "CollectionID", "Collection", "CollectionID",
            //"Deaccession", "TypeID", "Type", "TypeID",

            //"CollectingEvent", "BiologicalObjectTypeCollectedID", "BiologicalObjectTypeCollected", "BiologicalObjectTypeCollectedID",
            "CollectingEvent", "LocalityID", "Locality", "LocalityID",
            //"CollectingEvent", "MethodID", "Method", "MethodID",

            "Collectors", "CollectingEventID", "CollectingEvent", "CollectingEventID",
            "Collectors", "AgentID", "Agent", "AgentID",

            "Permit", "IssuerID", "AgentAddress", "AgentAddressID",
            "Permit", "IssueeID", "AgentAddress", "AgentAddressID",
            //"Permit", "TypeID", "Type", "TypeID",

            "Sound", "RecordedByID", "Agent", "AgentID",

            "TaxonCitation", "ReferenceWorkID", "ReferenceWork", "ReferenceWorkID",
            "TaxonCitation", "TaxonNameID", "TaxonName", "TaxonNameID",

            "Determination", "DeterminerID",           "Agent", "AgentID",
            "Determination", "TaxonNameID",            "TaxonName", "TaxonNameID",
            "Determination", "BiologicalObjectID",     "CollectionObject", "CollectionObjectID",
            //"Determination", "PreparationID",          "Preparation", "PreparationID",
            //"Determination", "BiologicalObjectTypeID", "BiologicalObjectType", "BiologicalObjectTypeID",
            //"Determination", "TypeStatusNameID",       "TypeStatusName", "TypeStatusNameID",
            //"Determination", "ConfidenceID",           "Confidence", "ConfidenceID",
            //"Determination", "MethodID",               "Method", "MethodID",

            // ??? "GeologicTimePeriod", "UpperBoundaryID", "UpperBoundary", "UpperBoundaryID",
            // ??? "GeologicTimePeriod", "LowerBoundaryID", "LowerBoundary", "LowerBoundaryID",

            //"CatalogSeriesDefinition", "CatalogSeriesID", "CatalogSeries", "CatalogSeriesID",
            //"CatalogSeriesDefinition", "ObjectTypeID", "ObjectType", "ObjectTypeID",

            // (not needed) "CollectionObject", "DerivedFromID", "DerivedFrom", "DerivedFromID",
            //"CollectionObject", "ContainerID", "Container", "ContainerID",
            //"CollectionObject", "CollectionObjectTypeID", "CollectionObjectType", "CollectionObjectTypeID",
            "CollectionObject", "CollectingEventID", "CollectingEvent", "CollectingEventID",
            //"CollectionObject", "ContainerTypeID", "ContainerType", "ContainerTypeID",
            //"CollectionObject", "PreparationMethodID", "PreparationMethod", "PreparationMethodID",

            //"CollectionObjectCatalog", "CollectionObjectTypeID", "CollectionObjectType", "CollectionObjectTypeID",
            "CollectionObject", "CatalogSeriesID",        "CatalogSeries", "CatalogSeriesID",
            "CollectionObject", "AccessionID",            "Accession", "AccessionID",
            "CollectionObject", "CatalogerID",            "Agent", "AgentID",

            //"Observation", "BiologicalObjectID", "BiologicalObject", "BiologicalObjectID",
            //"Observation", "ObservationMethodID", "ObservationMethod", "ObservationMethodID",

            "Loan", "ShipmentID", "Shipment", "ShipmentID",
            //"Loan", "CollectionID", "Collection", "CollectionID",

            "AccessionAuthorizations", "AccessionID", "Accession", "AccessionID",
            "AccessionAuthorizations", "PermitID", "Permit", "PermitID",

            "AccessionAgents", "AccessionID", "Accession", "AccessionID",
            "AccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
            //"AccessionAgents", "RoleID", "Role", "RoleID",

            "DeterminationCitation", "ReferenceWorkID", "ReferenceWork", "ReferenceWorkID",
            "DeterminationCitation", "DeterminationID", "Determination", "DeterminationID",

            //"CatalogSeries", "CollectionID", "Collection", "CollectionID",

            "OtherIdentifier", "CollectionObjectID", "CollectionObject", "CollectionObjectID",

            "Agent", "ParentOrganizationID", "Agent", "AgentID",

            //"CollectionTaxonomyTypes", "CollectionID", "Collection", "CollectionID",
            //"CollectionTaxonomyTypes", "BiologicalObjectTypeID", "BiologicalObjectType", "BiologicalObjectTypeID",
            //"CollectionTaxonomyTypes", "TaxonomyTypeID", "TaxonomyType", "TaxonomyTypeID",

            "AgentAddress", "AddressID", "Address", "AddressID",
            "AgentAddress", "AgentID", "Agent", "AgentID",
            "AgentAddress", "OrganizationID", "Agent", "AgentID",

            "LocalityCitation", "ReferenceWorkID", "ReferenceWork", "ReferenceWorkID",
            "LocalityCitation", "LocalityID", "Locality", "LocalityID",

            //"BiologicalObjectAttributes", "BiologicalObjectTypeID", "BiologicalObjectType", "BiologicalObjectTypeID",
            //"BiologicalObjectAttributes", "SexID", "Sex", "SexID",
            //"BiologicalObjectAttributes", "StageID", "Stage", "StageID",

            "LoanReturnPhysicalObject", "LoanPhysicalObjectID",        "LoanPhysicalObject", "LoanPhysicalObjectID",
            "LoanReturnPhysicalObject", "ReceivedByID",                "Agent",  "AgentID",
            "LoanReturnPhysicalObject", "DeaccessionPhysicalObjectID", "CollectionObject", "CollectionObjectID",

            //"Borrow", "CollectionID", "Collection", "CollectionID",

            "Locality", "GeographyID", "Geography", "GeographyID",
            //"Locality", "ElevationMethodID", "ElevationMethod", "ElevationMethodID",
            //"Locality", "LatLongTypeID", "LatLongType", "LatLongTypeID",
            //"Locality", "LatLongMethodID", "LatLongMethod", "LatLongMethodID",

            "DeaccessionAgents", "DeaccessionID", "Deaccession", "DeaccessionID",
            "DeaccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
            //"DeaccessionAgents", "RoleID", "Role", "RoleID",

            "ProjectCollectionObjects", "ProjectID", "Project", "ProjectID",
            "ProjectCollectionObjects", "CollectionObjectID", "CollectionObject", "CollectionObjectID",

            "Project", "ProjectAgentID", "Agent", "AgentID",

            "LoanAgents", "LoanID", "Loan", "LoanID",
            "LoanAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
            //"LoanAgents", "RoleID", "Role", "RoleID",

            //"Accession", "CollectionID", "Collection", "CollectionID",
            //"Accession", "StatusID", "Status", "StatusID",
            //"Accession", "TypeID", "Type", "TypeID",

        	// taxonname ID mappings
            "TaxonName", "ParentTaxonNameID", "TaxonName", "TaxonNameID",
            "TaxonName", "TaxonomicUnitTypeID", "TaxonomicUnitType", "TaxonomicUnitTypeID",
            "TaxonName", "TaxonomyTypeID", "TaxonomyType", "TaxonomyTypeID",
            "TaxonName", "AcceptedID", "TaxonName", "TaxonNameID",

            // taxonomytype ID mappings
            // NONE
            
            // taxonomicunittype ID mappings
            "TaxonomicUnitType", "TaxonomyTypeID", "TaxonomyType", "TaxonomyTypeID"
        };

        for (int i=0;i<mappings.length;i += 4)
        {
            idMapperMgr.mapForeignKey(mappings[i], mappings[i+1], mappings[i+2], mappings[i+3]);
        }
    }

    /**
     * Removes all the records from every table in the new database and then copies over
     * all the tables that have few if any changes to their schema
     */
    public void copyTables()
    {

        //cleanAllTables(); // from DBCOnnection which is the new DB

        String[] tablesToMoveOverX = {
                "Permit"
        };


        String[] tablesToMoveOver = {
                                    "AccessionAgents",
                                    "Accession",
                                    "AccessionAuthorizations",
                                    //"Address",
                                    //"Agent",
                                    //"AgentAddress",
                                    "Authors",
                                    "Borrow",
                                    "BorrowAgents",
                                    "BorrowMaterial",
                                    "BorrowReturnMaterial",
                                    "BorrowShipments",
                                    "CatalogSeries",
                                    "CollectingEvent",
                                    "CollectionObjectCitation",
                                    "Collectors",
                                    "Deaccession",
                                    "DeaccessionAgents",
                                    "DeaccessionCollectionObject",
                                    "Determination",
                                    "DeterminationCitation",
                                    "ExchangeIn",
                                    "ExchangeOut",
                                    "GroupPersons",
                                    "Journal",
                                    "Loan",
                                    "LoanAgents",
                                    "LoanPhysicalObject",
                                    "LoanReturnPhysicalObject",
                                    //"locality",
                                    "LocalityCitation",
                                    "OtherIdentifier",
                                    "Permit",
                                    "Project",
                                    "ProjectCollectionObjects",
                                    "ReferenceWork",
                                    "Shipment",
                                    "Stratigraphy",
                                    "TaxonCitation",
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
       
       tableMaps.put("accessionagents", createFieldNameMap(new String[] {"AgentID", "AgentAddressID"}));
       tableMaps.put("borrowagent", createFieldNameMap(new String[] {"AgentID", "AgentAddressID"}));
       tableMaps.put("deaccessionagent", createFieldNameMap(new String[] {"AgentID", "AgentAddressID"}));
       tableMaps.put("loanagent", createFieldNameMap(new String[] {"AgentID", "AgentAddressID"}));


       Map<String, Map<String, String>> tableDateMaps = new Hashtable<String, Map<String, String>>();
       tableDateMaps.put("collectingevent", createFieldNameMap(new String[] {"TaxonID", "TaxonNameID"}));

       //tableMaps.put("locality", createFieldNameMap(new String[] {"NationalParkName", "", "ParentID", "TaxonParentID"}));


       BasicSQLUtils.setShowMappingError(false);
       for (String tableName : tablesToMoveOver)
       {

           String lowerCaseName = tableName.toLowerCase();

           deleteAllRecordsFromTable(lowerCaseName);

           if (!copyTable(oldDB.getConnectionToDB(), DBConnection.getConnection(), lowerCaseName, tableMaps.get(lowerCaseName), null))
           {
               log.error("Table ["+tableName+"] didn't copy correctly.");
               break;
           }
       }
       BasicSQLUtils.setShowMappingError(true);
    }

    /**
     * Converts an old USYS table to a PickList
     * @param usysTableName old table name
     * @param pickListName new pciklist name
     * @return true on success, false on failure
     */
    public boolean convertUSYSToPicklist(final String usysTableName, final String pickListName)
    {
        Connection   oldDBConn = oldDB.getConnectionToDB();

        List<BasicSQLUtils.FieldMetaData> fieldMetaData = new ArrayList<BasicSQLUtils.FieldMetaData>();
        getFieldMetaDataFromSchema(oldDBConn, usysTableName, fieldMetaData);

        int ifaceInx    = -1;
        int dataInx     = -1;
        int fieldSetInx = -1;
        int i= 0;
        for (BasicSQLUtils.FieldMetaData md : fieldMetaData)
        {
            if (ifaceInx == -1 && md.getName().equals("InterfaceID"))
            {
                ifaceInx = i+1;

            } else if (fieldSetInx == -1 && md.getName().equals("FieldSetSubTypeID"))
            {
                fieldSetInx = i+1;

            } else if (dataInx  == -1 && md.getType().toLowerCase().indexOf("varchar") > -1)
            {
                dataInx = i+1;
            }
            i++;
        }

        if (ifaceInx == -1 || dataInx == -1 || fieldSetInx == -1)
        {
            throw new RuntimeException("Couldn't decypher USYS table ifaceInx["+ifaceInx+"] dataInx["+dataInx+"] fieldSetInx["+fieldSetInx+"]");
        }

        Session session = HibernateUtil.getCurrentSession();
        PickList pl     = new PickList();
        Set      items  = new HashSet<Object>();

        try
        {
            pl.setName(pickListName);
            pl.setCreated(new Date());
            pl.setItems(items);
            pl.setReadOnly(shouldDeleteMapTables);
            pl.setSizeLimit(-1);


            HibernateUtil.beginTransaction();
            session.saveOrUpdate(pl);
            HibernateUtil.commitTransaction();

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException("Couldn't create PickList for ["+usysTableName+"]");
        }

        try
        {
            Statement stmt   = oldDBConn.createStatement();
            String    sqlStr = "select * from "+usysTableName+" where InterfaceID is not null";

            log.info(sqlStr);

            boolean   useField = false;
            ResultSet rs       = stmt.executeQuery(sqlStr);

            // check for no records which is OK
            if (!rs.first())
            {
                oldDBConn.close();
                return true;
            }

            do
            {
                Object fieldObj = rs.getObject(fieldSetInx);
                if (fieldObj != null)
                {
                    useField = true;
                    break;
                }
            } while (rs.next());

            Hashtable<String, String> values = new Hashtable<String, String>();

            log.info("Using FieldSetSubTypeID "+useField);
            rs.first();
            int       count   = 0;
            do
            {
                if (!useField || rs.getObject(fieldSetInx) != null)
                {
                    String val = rs.getString(dataInx);
                    if (values.get(val) == null)
                    {
                        log.info("["+val+"]");
                        PickListItem pli = new PickListItem();
                        pli.setTitle(val);
                        pli.setValue(val);
                        pli.setCreatedDate(new Date());
                        items.add(pli);
                        values.put(val, val);
                        count++;
                    } else
                    {
                        log.error("Discarding duplicate picklist value["+val+"]");
                    }
                }
            } while (rs.next());

            log.info("Processed "+usysTableName+"  "+count+" records.");

            HibernateUtil.beginTransaction();

            session.saveOrUpdate(pl);

            HibernateUtil.commitTransaction();

            oldDBConn.close();

            return true;

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
        }
        return false;
    }

    /**
     * Converts all the USYS tables to PickLists
     * @return true on success, false on failure
     */
    public boolean convertUSYSTables()
    {
        log.info("Converting USYS Tables.");

        BasicSQLUtils.deleteAllRecordsFromTable("picklist");
        BasicSQLUtils.deleteAllRecordsFromTable("picklist_items");

        String[] tables = {
                "usysaccessionstatus",            "AccessionStatus",
                "usysaccessiontype",              "AccessionType",
                "usysborrowagenrole",             "BorrowAgentRole",
                "usysaccessionarole",             "AccessionRole",
                "usysdeaccessiorole",             "DeaccessionaRole",
                "usysloanagentsrole",             "LoanAgentsRole",
                "usysbiologicalsex",              "BiologicalSex",
                "usysbiologicalstage",            "BiologicalStage",
                "usyscollectingmethod",           "CollectingMethod",
                "usyscollobjprepmeth",            "CollObjPrepMeth",
                "usysdeaccessiotype",             "DeaccessionType",
                "usysdeterminatconfidence",       "DeterminationConfidence",
                "usysdeterminatmethod",           "DeterminationMethod",
                "usysdeterminattypestatusname",   "DeterminationTypeStatus",
                "usyshabitathabitattype",         "HabitatTtype",
                "usyslocalityelevationmethod",    "LocalityElevationMethod",
                "usysobservatioobservationmetho", "ObservationMethod",
                "usyspermittype",                 "PermitType",
                "usyspreparatiocontainertype",    "PrepContainertype",
                "usyspreparatiomedium",           "PreparatioMedium",
                "usyspreparatiopreparationtype",  "PreparationType",
                "usysshipmentshipmentmethod",     "ShipmentMethod"
                };

        for (int i=0;i<tables.length;i++)
        {
            boolean status = convertUSYSToPicklist(tables[i], tables[i+1]);
            if (!status)
            {
                log.error(tables[i]+" failed to convert.");
                return false;
            }
            i++;
        }
        return true;
    }


    /**
     * Creates a map from a String Preparation Type to its ID in the table
     * @return map of name to PrepType
     */
    public Map<String, PrepType> createPreparationTypesFromUSys()
    {
        deleteAllRecordsFromTable("preptype");

        Hashtable<String, PrepType> prepTypeMapper = new Hashtable<String, PrepType>();

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
     * @param data
     * @param fromTableName
     * @param oldColName
     * @return
     */
    protected Object getMappedId(final Object data, final String fromTableName, final String oldColName)
    {
        if (idMapperMgr != null && oldColName.endsWith("ID"))
        {

            IdMapper idMapper =  idMapperMgr.get(fromTableName, oldColName);
            if (idMapper != null)
            {
                return idMapper.getNewIdFromOldId((Integer)data);
            } else
            {
                //throw new RuntimeException("No Map for ["+fromTableName+"]["+oldMappedColName+"]");
                if (!oldColName.equals("MethodID") &&
                        !oldColName.equals("RoleID") &&
                        !oldColName.equals("CollectionID") &&
                        !oldColName.equals("ConfidenceID") &&
                        !oldColName.equals("TypeStatusNameID") &&
                        !oldColName.equals("ObservationMethodID"))
                {
                    System.out.println("No Map for ["+fromTableName+"]["+oldColName+"]");
                }
            }
        }
        return data;
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

                str.append(" from biologicalobjectattributes order by BiologicalObjectAttributesID");
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
                                Integer newRecId = (Integer)getMappedId(rs.getInt(1), "biologicalobjectattributes", "BiologicalObjectAttributesID");

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
                                strBuf.append(newRecId.intValue());
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

            sql.append(" From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID ");
            sql.append("Where not (collectionobject.DerivedFromID Is Null) order by collectionobjectcatalog.CollectionObjectCatalogID");

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
                //System.out.println(name+" "+(inx-1));
            }
            Hashtable<String, String> newToOld = new Hashtable<String, String>();
            newToOld.put("PreparationID", "CollectionObjectID");
            newToOld.put("CollectionObjectID", "DerivedFromID");
            newToOld.put("StorageLocation", "Location");

            IdMapper agentIdMapper = idMapperMgr.get("agent", "AgentID");
            IdMapper prepIdMapper =  idMapperMgr.get("preparation",  "PreparationID");

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
                    Integer   recordId     = rs.getInt(idIndex+1);
                    Statement subStmt      = oldDBConn.createStatement();
                    String    subQueryStr  = "select PreparedByID,PreparedDate from preparation where PreparationID = "+recordId;
                    ResultSet subQueryRS   = subStmt.executeQuery(subQueryStr);
                    if (subQueryRS.first())
                    {
                        preparedById = subQueryRS.getInt(1);
                        preparedDate = UIHelper.convertIntToDate(subQueryRS.getInt(2));
                    }
                    subQueryRS.close();
                    subStmt.close();
                }

                /*int catNum =  rs.getInt(oldNameIndex.get("CatalogNumber")+1);
                doDebug = catNum == 30972;

                if (doDebug)
                {
                    System.out.println("CatalogNumber      "+catNum);
                    System.out.println("CollectionObjectID "+rs.getInt(oldNameIndex.get("CollectionObjectID")+1));
                    System.out.println("DerivedFromID      "+rs.getInt(oldNameIndex.get("DerivedFromID")+1));
                }*/

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
                    } else
                    {
                        mappedName = newFieldName;
                    }

                    if (i == 0)
                    {
                        //Integer  recId  = count+1;
                        str.append("NULL");//getStrValue(recId));

                    } else if (newFieldName.equals("PreparedByID"))
                    {
                        if (agentIdMapper != null)
                        {
                            str.append(getStrValue(agentIdMapper.getNewIdFromOldId(preparedById)));
                        } else
                        {
                            log.error("No Map for PreparedByID["+preparedById+"]");
                        }

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
                        if (index == null)
                        {
                            log.error("Couldn't find new field name["+newFieldName+"] in old field name in index Map");
                            stmt.close();
                            oldDBConn.close();
                            newDBConn.close();
                            return false;
                        }
                        Object  data  = rs.getObject(index+1);

                        if (idMapperMgr != null && mappedName.endsWith("ID") && !mappedName.equals("DerivedFromID"))
                        {
                            IdMapper idMapper =  idMapperMgr.get("collectionobject", mappedName);
                            if (idMapper != null)
                            {
                                data = idMapper.getNewIdFromOldId(rs.getInt(index));
                            } else
                            {
                                System.out.println("No Map for [collectionobject]["+mappedName+"]");
                            }
                        }
                        str.append(getStrValue(data, newFieldMetaData.get(i).getType()));

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
        deleteAllRecordsFromTable(newDBConn, "collectionobject"); // automatically closes the connection

        newDBConn = DBConnection.getConnection();

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

            String tableName = "collectionobject";

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

                    if (i == 0)
                    {
                        Integer  recId  = count+1;
                        str.append(getStrValue(recId));

                    } else if (newFieldName.equals("CatalogedDateVerbatim") ||
                            newFieldName.equals("ContainerID") ||
                            newFieldName.equals("ContainerItemID") ||
                            newFieldName.equals("AltCatalogNumber") ||
                            newFieldName.equals("GUID") ||
                            newFieldName.equals("CollectionObjectID"))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("CountAmt"))
                    {
                        Integer index = oldNameIndex.get("Count1");
                        str.append(getStrValue(rs.getObject(index+1), newFieldMetaData.get(i).getType()));

                    } else
                    {

                        Integer index = oldNameIndex.get(newFieldName);
                        if (index == null)
                        {
                            log.error("Couldn't find new field name["+newFieldName+"] in old field name in index Map");
                            stmt.close();
                            oldDBConn.close();
                            newDBConn.close();
                            return false;
                        }
                        Object data  = rs.getObject(index+1);

                        if (data != null)
                        {
                            int idInx = newFieldName.lastIndexOf("ID");
                            if (idMapperMgr != null && idInx > -1)
                            {
                                //System.out.println(newFieldName+" "+(index.intValue()+1)+"  "+rs.getInt(index+1));

                                IdMapper idMapper =  idMapperMgr.get(tableName, newFieldName);
                                if (idMapper != null)
                                {
                                    data = idMapper.getNewIdFromOldId(rs.getInt(index+1));
                                } else
                                {
                                    log.error("No Map for ["+tableName+"]["+newFieldName+"]");
                                }
                            }
                        }
                        str.append(getStrValue(data, newFieldMetaData.get(i).getType()));

                        /*Integer index = oldNameIndex.get(newFieldName);
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
                        }*/
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
                    rs.close();
                    stmt.close();
                    oldDBConn.close();
                    newDBConn.close();
                    return false;
                }

                count++;
                //if (count > 10) break;
            }
            log.info("Processed CollectionObject "+count+" records.");

            rs.close();
            stmt.close();
            oldDBConn.close();
            newDBConn.close();

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            return false;
        }


        return true;
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
     * @param name name
     * @param dataType dataType
     * @param user user
     * @param taxaTreeDef taxaTreeDef
     * @param  catalogSeries catalogSeries
     * @return set of objects
     */
    public Set<CollectionObjDef> createCollectionObjDef(final String          name,
                                                        final DataType        dataType,
                                                        final SpecifyUser     user,
                                                        final TaxonTreeDef taxaTreeDef,
                                                        final CatalogSeries   catalogSeries)
    {
        try
        {
            Set<CatalogSeries> catalogSeriesSet = new HashSet<CatalogSeries>();
            if (catalogSeries != null)
            {
                catalogSeriesSet.add(catalogSeries);
            }

            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            CollectionObjDef colObjDef = new CollectionObjDef();
            colObjDef.initialize();
            colObjDef.setName(name);
            colObjDef.setDataType(dataType);
            colObjDef.setSpecifyUser(user);

            colObjDef.setTaxonTreeDef(taxaTreeDef);

            colObjDef.setCatalogSeries(catalogSeriesSet);

            session.save(colObjDef);

            HashSet<CollectionObjDef> set = new HashSet<CollectionObjDef>();
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
    
    public void convertAllTaxonTreeDefs() throws SQLException
    {
    	Connection conn = oldDB.getConnectionToDB();
    	Statement  st   = conn.createStatement();

    	TaxonTreeDef ttd = new TaxonTreeDef();
    	ttd.initialize();
    	
    	ResultSet rs = st.executeQuery("SELECT TaxonomyTypeID FROM taxonomytype");
    	Vector<Integer> ttIds = new Vector<Integer>();
    	while( rs.next() )
    	{
    		ttIds.add(rs.getInt(1));
    	}
    	
    	for( Integer id: ttIds )
    	{
    		convertTaxonTreeDefinition(id);
    	}
    }

    /**
     * Converts the taxonomy tree definition from the old taxonomicunittype
     * table to the new table pair: TaxonTreeDef & TaxonTreeDefItems.
     * 
     * @param taxonomyTypeId the tree def id in taxonomicunittype
     * @return the TaxonTreeDef object
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
	public TaxonTreeDef convertTaxonTreeDefinition( int taxonomyTypeId ) throws SQLException
    {
    	Connection conn = oldDB.getConnectionToDB();
    	Statement  st   = conn.createStatement();

    	TaxonTreeDef ttd = new TaxonTreeDef();
    	ttd.initialize();
    	
    	ResultSet rs = st.executeQuery("SELECT TaxonomyTypeName FROM taxonomytype WHERE TaxonomyTypeID="+taxonomyTypeId);
    	rs.next();
    	String taxonomyTypeName = rs.getString(1);
    	
    	ttd.setName(taxonomyTypeName + " taxonomy tree");
    	ttd.setRemarks("Tree converted from " + oldDBName);

    	rs = st.executeQuery("SELECT DISTINCT RankID,RankName,RequiredParentRankID FROM taxonomicunittype WHERE TaxonomyTypeID="
    			+ taxonomyTypeId + " ORDER BY RankID");

    	int rank;
    	String name;
    	int requiredRank;
    	
    	Vector<TaxonTreeDefItem> items = new Vector<TaxonTreeDefItem>();
    	Vector<Integer> enforcedRanks = new Vector<Integer>();
    	
    	while( rs.next() )
    	{
    		rank = rs.getInt(1);
    		name = rs.getString(2);
    		requiredRank = rs.getInt(3);
    		System.out.println( rank + "  " + name );
    		TaxonTreeDefItem i = new TaxonTreeDefItem();
    		i.initialize();
    		i.setName(name);
    		i.setRankId(rank);
    		i.setTreeDef(ttd);
    		ttd.getTreeDefItems().add(i);

    		// setup the parent/child relationship
    		if( items.isEmpty() )
    		{
    			i.setParent(null);
    		}
    		else
    		{
    			i.setParent(items.lastElement());
    		}
    		items.add(i);
    		
    		enforcedRanks.add(requiredRank);
    	}
    	
    	for( TaxonTreeDefItem i: items )
    	{
    		if( enforcedRanks.contains(i.getRankId()) )
    		{
    			i.setIsEnforced(true);
    		}
    		else
    		{
    			i.setIsEnforced(false);
    		}
    	}
    	
    	return ttd;
    }
    
    public static TaxonTreeDef createStdTaxonTreeDef()
    {
    	Object[][] stdItems = {
    			{  0,"Taxonomy Root",true},
    			{ 10,"Kingdom",true},
    			{ 20,"Subkingdom",false},
    			{ 30,"Phylum",true},
    		//	{ 30,"Division",true}, // botanical collections
    			{ 40,"Subphylum",false},
    		//	{ 40,"Subdivision",false}, // botanical collections
    			{ 50,"Superclass",false},
    			{ 60,"Class",true},
    			{ 70,"Subclass",false},
    			{ 80,"Infraclass",false},
    			{ 90,"Superorder",false},
    			{100,"Order",true},
    			{110,"Suborder",false},
    			{120,"Infraorder",false},
    			{130,"Superfamily",false},
    			{140,"Tribe",false},
    			{150,"Subtribe",false},
    			{160,"Genus",true},
    			{170,"Subgenus",false},
    			{180,"Section",false},
    			{190,"Subsection",false},
    			{200,"Species",false},
    			{210,"Subspecies",false},
    			{220,"Variety",false},
    			{230,"Subvariety",false},
    			{240,"Forma",false},
    			{250,"Subforma",false}
    	};
    	
    	TaxonTreeDef ttd = new TaxonTreeDef();
    	ttd.initialize();
    	ttd.setName("Standard Taxonomy Tree Definition");
    	
    	TaxonTreeDefItem[] items = new TaxonTreeDefItem[stdItems.length];
    	
    	TaxonTreeDefItem parent = null;
    	for( int i = 0; i < stdItems.length; ++i )
    	{
    		if( i > 0 )
    		{
    			parent = items[i-1];
    		}
    		int rank = (Integer)stdItems[i][0];
    		String name = (String)stdItems[i][1];
    		boolean enforced = (Boolean)stdItems[i][2];
    		
    		items[i] = ObjCreatorHelper.createTaxonTreeDefItem(parent, ttd, name, rank);
   			items[i].setIsEnforced(enforced);
    	}
    	
    	return ttd;
    }
    
    public void copyTaxonTreeDefs()
    {
    	String sql = "SELECT * FROM taxonomytype";
    	
    	Hashtable<String,String> newToOldColMap = new Hashtable<String,String>();
    	newToOldColMap.put("TreeDefID", "TaxonomyTypeID");
    	newToOldColMap.put("Name", "TaxonomyTypeName");
    	
    	String[] ignoredFields = {"Remarks"};
    	BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
    	
    	log.info("Copying taxonomy tree definitions from 'taxonomytype' table");
    	if( !copyTable(oldDB.getConnectionToDB(),
    			DBConnection.getConnection(),
    			sql,
    			"taxonomytype",
    			"taxontreedef",
    			newToOldColMap,
    			null) )
    	{
    		log.error("Table 'taxonomytype' didn't copy correctly");
    	}
    	
    	BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);
    }
    
    /**
     * Converts the old taxonomy records to the new schema.  In general,
     * the process is...
     * 	1. Copy all columns that don't require any modification (other than col. name)
     *  2. Set the proper values in the IsEnforced column
     *  3. Set the proper values in the ParentItemID column
     * 
     * @throws SQLException
     */
    public void convertTaxonTreeDefItems() throws SQLException
    {
    	String sqlStr = "SELECT * FROM taxonomicunittype";
    	
    	Hashtable<String,String> newToOldColMap = new Hashtable<String,String>();
    	newToOldColMap.put("TreeDefItemID", "TaxonomicUnitTypeID");
    	newToOldColMap.put("Name", "RankName");
    	newToOldColMap.put("TreeDefID", "TaxonomyTypeID");
    	
    	String[] ignoredFields = {"IsEnforced", "ParentItemID"};
    	BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
    	
    	// Copy over most of the columns in the old table to the new one
    	log.info("Copying taxonomy tree definition items from 'taxonomicunittype' table");
    	if( !copyTable(oldDB.getConnectionToDB(),
    			DBConnection.getConnection(),
    			sqlStr,
    			"taxonomicunittype",
    			"taxontreedefitem",
    			newToOldColMap,
    			null) )
    	{
    		log.error("Table 'taxonomicunittype' didn't copy correctly");
    	}

    	BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);

    	// JDBC Statments for use throughout process
        Statement oldDbStmt = oldDB.getConnectionToDB().createStatement();
		Statement newDbStmt = DBConnection.getConnection().createStatement();

    	// get each individual TaxonomyTypeID value
    	sqlStr = "SELECT DISTINCT TaxonomyTypeID from taxonomicunittype";
		ResultSet rs = oldDbStmt.executeQuery(sqlStr);

    	Vector<Integer> typeIds = new Vector<Integer>();
    	while(rs.next())
    	{
    		Integer typeId = rs.getInt(1);
    		if( !rs.wasNull() )
    		{
        		typeIds.add(typeId);
    		}
    	}

    	// will be used to map old TaxonomyTypeID values to TreeDefID values
		IdMapperMgr idMapperMgr = IdMapperMgr.getInstance();
		IdMapper typeIdMapper = idMapperMgr.get("taxonomytype", "TaxonomyTypeID");
    	
    	// for each value of TaxonomyType...
    	for( Integer typeId: typeIds )
    	{
    		// get all of the values of RequiredParentRankID (the enforced ranks)
    		sqlStr = "SELECT DISTINCT RequiredParentRankID from taxonomicunittype WHERE TaxonomyTypeID=" + typeId;
    		rs = oldDbStmt.executeQuery(sqlStr);
    		
    		Vector<Integer> enforcedIds = new Vector<Integer>();
    		while( rs.next() )
    		{
    			Integer reqId = rs.getInt(1);
    			if( !rs.wasNull() )
    			{
    				enforcedIds.add(reqId);
    			}
    		}
    		
    		// now we have a vector of the required/enforced rank IDs
    		// fix the new DB values accordingly
    		
    		// what is the corresponding TreeDefID?
    		int treeDefId = typeIdMapper.getNewIdFromOldId(typeId);
    		
    		StringBuilder sqlUpdate = new StringBuilder("UPDATE taxontreedefitem SET IsEnforced=TRUE WHERE TreeDefID="+treeDefId+" AND RankID IN (");
    		// add all the enforced ranks
    		for( int i = 0; i < enforcedIds.size(); ++i )
    		{
    			sqlUpdate.append(enforcedIds.get(i));
    			if( i < enforcedIds.size()-1 )
    			{
    				sqlUpdate.append(",");
    			}
    		}
    		sqlUpdate.append(")");
    		
    		log.info(sqlUpdate);
    		
    		int rowsUpdated = newDbStmt.executeUpdate(sqlUpdate.toString());
    		log.info(rowsUpdated + " rows updated");
    	}
    	
    	// at this point, we've set all the IsEnforced fields that need to be TRUE
    	// now we need to set the others to FALSE
    	String setToFalse = "UPDATE TaxonTreeDefItem SET IsEnforced=FALSE WHERE IsEnforced IS NULL";
    	int rowsUpdated = newDbStmt.executeUpdate(setToFalse);
    	log.info("IsEnforced set to FALSE in " + rowsUpdated + " rows");
    	
    	// we still need to fix the ParentItemID values to point at each row's parent

    	// we'll work with the items in sets as determined by the TreeDefID
    	for( Integer typeId: typeIds )
    	{
    		int treeDefId = typeIdMapper.getNewIdFromOldId(typeId);
        	sqlStr = "SELECT TreeDefItemID FROM TaxonTreeDefItem WHERE TreeDefID="+treeDefId+" ORDER BY RankID";
        	rs = newDbStmt.executeQuery(sqlStr);
        	
        	boolean atLeastOneRecord = rs.next();
        	if( !atLeastOneRecord )
        	{
        		continue;
        	}
        	int prevTreeDefItemId = rs.getInt(1);
        	Vector<Pair<Integer,Integer>> idAndParentIdPairs = new Vector<Pair<Integer,Integer>>();
        	while( rs.next() )
        	{
        		int treeDefItemId = rs.getInt(1);
        		idAndParentIdPairs.add(new Pair<Integer,Integer>(treeDefItemId,prevTreeDefItemId));
        		prevTreeDefItemId = treeDefItemId;
        	}
        	
        	// now we have all the pairs (ID,ParentID) in a Vector of Pair objects
        	rowsUpdated = 0;
        	for( Pair<Integer,Integer> idPair: idAndParentIdPairs )
        	{
        		sqlStr = "UPDATE TaxonTreeDefItem SET ParentItemID=" + idPair.second + " WHERE TreeDefItemID=" + idPair.first;
        		rowsUpdated += newDbStmt.executeUpdate(sqlStr);
        	}
        	
        	log.info("Fixed parent pointers on " + rowsUpdated + " rows");
    	}
    }
    
    public void copyTaxonRecords()
    {
    	String sql = "SELECT * FROM taxonname";
    	
    	Hashtable<String,String> newToOldColMap = new Hashtable<String,String>();
    	newToOldColMap.put("TreeID", "TaxonNameID");
    	newToOldColMap.put("ParentID", "ParentTaxonNameID");
    	newToOldColMap.put("TreeDefID", "TaxonomyTypeID");
    	newToOldColMap.put("TreeDefItemID", "TaxonomicUnitTypeID");
    	newToOldColMap.put("Name", "TaxonName");
    	newToOldColMap.put("FullTaxon", "FullTaxonName");
    	
    	String[] ignoredFields = {"GUID"};
    	BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
    	
    	log.info("Copying taxon records from 'taxonname' table");
    	if( !copyTable(oldDB.getConnectionToDB(),
    			DBConnection.getConnection(),
    			sql,
    			"taxonname",
    			"taxon",
    			newToOldColMap,
    			null) )
    	{
    		log.error("Table 'taxonname' didn't copy correctly");
    	}
    	
    	BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);
    }
    
    public GeographyTreeDef createStandardGeographyDefinitionAndItems()
    {
    	Session session = HibernateUtil.getCurrentSession();
    	HibernateUtil.beginTransaction();
    	GeographyTreeDef def = new GeographyTreeDef();
    	session.save(def);

    	def.setName("Default Geography Definition");
    	def.setRemarks("A simple continent/country/state/county geography tree");

		GeographyTreeDefItem planet = new GeographyTreeDefItem();
		session.save(planet);
		planet.setName("Planet");
		planet.setRankId(0);

		GeographyTreeDefItem cont = new GeographyTreeDefItem();
		session.save(cont);
		cont.setName("Continent");
		cont.setRankId(100);

		GeographyTreeDefItem country = new GeographyTreeDefItem();
		session.save(country);
		country.setName("Country");
		country.setRankId(200);

		GeographyTreeDefItem state = new GeographyTreeDefItem();
		session.save(state);
		state.setName("State");
		state.setRankId(300);

		GeographyTreeDefItem county = new GeographyTreeDefItem();
		session.save(county);
		county.setName("County");
		county.setRankId(400);

		// setup parents
		county.setParent(state);
		state.setParent(country);
		country.setParent(cont);
		cont.setParent(planet);

		// set the tree def for each tree def item
		planet.setTreeDef(def);
		cont.setTreeDef(def);
		country.setTreeDef(def);
		state.setTreeDef(def);
		county.setTreeDef(def);

		HibernateUtil.commitTransaction();
		HibernateUtil.closeSession();

		return def;
    }

    /**
     * @param filename the input file
     * @return the collection of <code>GeoFileLine</code>s representing the input file lines
     * @throws IOException if the input file cannot be found
     */
    public Vector<GeoFileLine> parseGeographyFile( final String filename ) throws IOException
    {
        BufferedReader inFile = new BufferedReader(new FileReader(filename));
        Vector<GeoFileLine>     geoFileLines = new Vector<GeoFileLine>();
        String line = null;
        int cnt = 0;

        while( (line = inFile.readLine()) != null )
        {
        	cnt++;
            String fields[] = line.split("\t");

            // verify that the proper number of fields are present
            if( fields.length < 11 )
            {
            	log.error("Ignoring invalid line in geography file ("+filename+":"+cnt);
            	continue;
            }

            // watch out for non-numbers in the ID field
            int geoId;
            try
            {
            	geoId = Integer.parseInt(fields[0]);
            }
            catch( NumberFormatException nfe )
            {
            	log.error("Ignoring invalid line in geography file ("+filename+":"+cnt);
            	continue;
            }
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
           	if (cnt % 1000 == 0)
           	{
           		log.debug("Geography: " + cnt);
           	}
            geoFileLines.add(row);
        }

        return geoFileLines;
    }

    /**
     * Create list of geo rows
     * @param oldTableName x
     * @return x
     * @throws SQLException x
     */
    public Vector<GeoFileLine> extractGeographyFromOldDb( final String oldTableName ) throws SQLException
    {
    	Vector<GeoFileLine> oldStyleLines = new Vector<GeoFileLine>();

    	Connection conn = oldDB.getConnectionToDB();
    	Statement  st   = conn.createStatement();

    	ResultSet rs = st.executeQuery(
    			"SELECT DISTINCT GeographyID,ContinentOrOcean,Country,State,County FROM "
    			+ oldTableName +
    			" WHERE (ContinentOrOcean IS NOT NULL) " +
    			"OR (Country IS NOT NULL) " +
    			"OR (State IS NOT NULL) " +
    			"OR (County IS NOT NULL) " +
    			"ORDER BY ContinentOrOcean,Country,State,County");

        IdMapper idMapper =  idMapperMgr.get("geography", "GeographyID");
    	while( rs.next() )
    	{
    		int geoId = rs.getInt(1);
            if (idMapper != null)
            {
                geoId = idMapper.getNewIdFromOldId(geoId);
            }

    		String cont = rs.getString(2);
    		String country = rs.getString(3);
    		String state = rs.getString(4);
    		String county = rs.getString(5);
    		String islandGrp = null;
    		String island = null;
    		String waterBody = null;
    		String drainage = null;
    		String fullname = null;

    		GeoFileLine gfl = new GeoFileLine(geoId,0,0,cont,country,state,county,islandGrp,island,waterBody,drainage,fullname);
    		oldStyleLines.add(gfl);
    	}

    	return oldStyleLines;
    }

    /**
     * Create Geography Object
     * @param def x
     * @param id x
     * @param name x
     * @param rank x
     * @param nodeNum x
     * @param parent x
     * @return x
     */
    private Geography buildGeography(GeographyTreeDef def, int id, String name, int rank, int nodeNum, Geography parent )
    {
    	Geography geo = new Geography(id);
    	geo.setDefinition(def);
    	geo.setName(name);
    	geo.setRankId(rank);
    	geo.setNodeNumber(nodeNum);
    	geo.setParent(parent);
    	return geo;
    }

    /**
     * Parses a tab-delimited file containing geographical location data
     *        and fills a db table with the appropriate data.
     *
     * The input file must format the data in the following order: id, current
     * id, continent or ocean, country, state, county, island group, island,
     * water body, drainage, full geographical name. <b>IT IS ASSUMED THAT THE
     * INPUT DATA HAS BEEN SORTED ALPHABETICALLY BY CONTINENT, THEN COUNTRY,
     * THEN STATE, AND FINALLY COUNTY.<b>
     *
     * @param tablename
     *            Table name (FIX ME COMMENT
     * @throws IOException
     *             if filename doesn't refer to a valid file path or there is an
     *             error while reading the file. In either situation, the
     *             resulting database table should not be considered usable.
     * @throws SQLException
     */
    public void loadSpecifyGeographicNames( final String tablename,
                                            final Vector<GeoFileLine> oldGeoRecords,
                                            final GeographyTreeDef treeDef )
        throws Exception
    {
        Vector<Integer>         usedIds       = new Vector<Integer>();
        //Vector<Geography> newTableRows  = new Vector<Geography>();
        Vector<Geography> newTableRows  = new Vector<Geography>();

        for( GeoFileLine gfl: oldGeoRecords )
        {
        	usedIds.add(gfl.getId());
        	// we also have to find all the IDs currently used in the DB
        }

        // get the GeographyTreeDef from the DB
		Session session = HibernateUtil.getCurrentSession();

		GeographyTreeDef def = treeDef;

//		Query query = session.createQuery("from "+GeographyTreeDef.class.getCanonicalName()+" as def where def.treeDefId = :treeid");
//		query.setParameter("treeid",geographyTreeDefId);
//		GeographyTreeDef def = (GeographyTreeDef)query.list().get(0);
//		if( def == null )
//		{
//			throw new Exception("No GeographyTreeDef found with ID="+geographyTreeDefId);
//		}

        // setup the root node (Earth) of the geo tree
        int geoRootId = findUnusedId(usedIds);
        usedIds.add(geoRootId);
        int nextNodeNumber = 1;
        Geography geoRoot = buildGeography(def,geoRootId,"Earth",GEO_ROOT_RANK,nextNodeNumber++,null);
        newTableRows.add(geoRoot);

        { // new code block for the sake of getting these vars out of scope to make debugging easier
        	// this should not remain in final versions
        String prevCont = null;
        String prevCountry = null;
        String prevState = null;
        String prevCounty = null;
        Geography prevContGeo = null;
        Geography prevCountryGeo = null;
        Geography prevStateGeo = null;
        Geography prevCountyGeo = null;

        // process them all into the new tree structure
        // on the first pass, we're simply going to create all of the nodes and
        // setup the parent pointers
        for( GeoFileLine geo: oldGeoRecords )
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
                    Geography newCont = buildGeography(def,contGeoId,geoName,CONTINENT_RANK,nextNodeNumber++,geoRoot);
                    prevCont = geoName;
                    prevContGeo = newCont;

                    countryGeoId = findUnusedId(usedIds);
                    usedIds.add(countryGeoId);
                    geoName = geo.getCountry();
                    Geography newCountry = buildGeography(def,countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,newCont);
                    prevCountry = geoName;
                    prevCountryGeo = newCountry;

                    stateGeoId = findUnusedId(usedIds);
                    usedIds.add(stateGeoId);
                    geoName = geo.getState();
                    Geography newState = buildGeography(def,stateGeoId,geoName,STATE_RANK,nextNodeNumber++,newCountry);
                    prevState = geoName;
                    prevStateGeo = newState;

                    // county keeps existing id
                    countyGeoId = geo.getId();
                    geoName = geo.getCounty();
                    Geography newCounty = buildGeography(def,countyGeoId,geoName,COUNTY_RANK,nextNodeNumber++,newState);
                    prevCounty = geoName;
                    prevCountyGeo = newCounty;

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
                    Geography newCont = buildGeography(def,contGeoId,geoName,CONTINENT_RANK,nextNodeNumber++,geoRoot);
                    prevCont = geoName;
                    prevContGeo = newCont;

                    countryGeoId = findUnusedId(usedIds);
                    usedIds.add(countryGeoId);
                    geoName = geo.getCountry();
                    Geography newCountry = buildGeography(def,countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,newCont);
                    prevCountry = geoName;
                    prevCountryGeo = newCountry;

                    // state keeps existing id
                    stateGeoId = geo.getId();
                    geoName = geo.getState();
                    Geography newState = buildGeography(def,stateGeoId,geoName,STATE_RANK,nextNodeNumber++,newCountry);
                    prevState = geoName;
                    prevStateGeo = newState;

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
                    Geography newCont = buildGeography(def,contGeoId,geoName,CONTINENT_RANK,nextNodeNumber++,geoRoot);
                    prevCont = geoName;
                    prevContGeo = newCont;

                    // country keeps existing id
                    countryGeoId = geo.getId();
                    geoName = geo.getCountry();
                    Geography newCountry = buildGeography(def,countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,newCont);
                    prevCountry = geoName;
                    prevCountryGeo = newCountry;

                    newTableRows.add(newCont);
                    newTableRows.add(newCountry);
                }
                else if( hasCont )
                {
                    // cont keeps the existing id
                    // this item has no country, state, or county

                    contGeoId = geo.getId();
                    geoName = geo.getContOrOcean();
                    Geography newCont = buildGeography(def,contGeoId,geoName,CONTINENT_RANK,nextNodeNumber++,geoRoot);
                    prevCont = geoName;
                    prevContGeo = newCont;

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
                    Geography newCountry = buildGeography(def,countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,prevContGeo);
                    prevCountry = geoName;
                    prevCountryGeo = newCountry;

                    stateGeoId = findUnusedId(usedIds);
                    usedIds.add(stateGeoId);
                    geoName = geo.getState();
                    Geography newState = buildGeography(def,stateGeoId,geoName,STATE_RANK,nextNodeNumber++,prevCountryGeo);
                    prevState = geoName;
                    prevStateGeo = newState;

                    // county keeps existing id
                    countyGeoId = geo.getId();
                    geoName = geo.getCounty();
                    Geography newCounty = buildGeography(def,countyGeoId,geoName,COUNTY_RANK,nextNodeNumber++,prevStateGeo);
                    prevCounty = geoName;
                    prevCountyGeo = newCounty;

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
                    Geography newCountry = buildGeography(def,countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,prevContGeo);
                    prevCountry = geoName;
                    prevCountryGeo = newCountry;

                    // state keeps existing id
                    stateGeoId = geo.getId();
                    geoName = geo.getState();
                    Geography newState = buildGeography(def,stateGeoId,geoName,STATE_RANK,nextNodeNumber++,prevCountryGeo);
                    prevState = geoName;
                    prevStateGeo = newState;

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
                    Geography newCountry = buildGeography(def,countryGeoId,geoName,COUNTRY_RANK,nextNodeNumber++,prevContGeo);
                    prevCountry = geoName;
                    prevCountryGeo = newCountry;

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
                    Geography newState = buildGeography(def,stateGeoId,geoName,STATE_RANK,nextNodeNumber++,prevCountryGeo);
                    prevState = geoName;
                    prevStateGeo = newState;

                    // county keeps existing id
                    countyGeoId = geo.getId();
                    geoName = geo.getCounty();
                    Geography newCounty = buildGeography(def,countyGeoId,geoName,COUNTY_RANK,nextNodeNumber++,prevStateGeo);
                    prevCounty = geoName;
                    prevCountyGeo = newCounty;

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
                    Geography newState = buildGeography(def,stateGeoId,geoName,STATE_RANK,nextNodeNumber++,prevCountryGeo);
                    prevState = geoName;
                    prevStateGeo = newState;

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
                    Geography newCounty = buildGeography(def,countyGeoId,geoName,COUNTY_RANK,nextNodeNumber++,prevStateGeo);
                    prevCounty = geoName;
                    prevCountyGeo = newCounty;

                    newTableRows.add(newCounty);
                }
            }
        }// end of "for( GeoFileLine geo: oldGeoRecords )"
    	} //end of weird code block inserted for debugging purposes

        // now we have a Vector of Geography's that contains all the data
        // we simply need to fixup all the highChildNodeNumber fields

        ListIterator<Geography> revIter = newTableRows.listIterator(newTableRows.size());
        while(revIter.hasPrevious())
        {
            Geography newRow = revIter.previous();
            int nodeNum = newRow.getNodeNumber();
            if( newRow.getHighestChildNodeNumber() == null || nodeNum > newRow.getHighestChildNodeNumber() )
            {
                newRow.setHighestChildNodeNumber(nodeNum);
            }
            Geography parent = newRow.getParent();

            // adjust all the parent nodes (all the way up)
            while( parent != null )
            {
                if( parent.getHighestChildNodeNumber() == null || parent.getHighestChildNodeNumber() < nodeNum )
                {
                    parent.setHighestChildNodeNumber(nodeNum);
                }
                parent = parent.getParent();
            }
        }

        HibernateUtil.beginTransaction();
        for( Geography geo: newTableRows )
        {
        	session.save(geo);
        }
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();
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
    public void convertLocality()
    {
        // Ignore these field names from new table schema when mapping IDs
        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(new String[] {"NationalParkName", "GUID"});

        BasicSQLUtils.deleteAllRecordsFromTable("locality");

        boolean showMappingErrors = BasicSQLUtils.isShowMappingError();
        BasicSQLUtils.setShowMappingError(false); // turn off notification because of errors with National Parks

        String sql = "select locality.*, geography.* from locality,geography where locality.GeographyID = geography.GeographyID";

        if (copyTable(oldDB.getConnectionToDB(), DBConnection.getConnection(), sql, "locality", "locality", null, null))
        {
            log.info("Locality/Geography copied ok.");
        } else
        {
            log.error("Copying locality/geography (fields) to new Locality");
        }
        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);
        //BasicSQLUtils.setShowMappingError(showMappingErrors);
    }
   
   /**
   *
   */
  public boolean convertAgents()
  {
       Connection connection = oldDB.getConnectionToDB();
       Connection newDBConn = DBConnection.getConnection();
       
       BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "agent");
       BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "address");
       
       
       String sql = "Select agentaddress.*, agent.*, address.* From agent Inner Join agentaddress ON agentaddress.AgentID = agent.AgentID Inner Join address ON agentaddress.AddressID = address.AddressID Order By agentaddress.AgentAddressID Asc";
       
       String[] agentColumns = {"agent.AgentID",
               "agent.AgentType",
               "agentaddress.JobTitle",
               "agent.FirstName",
               "agent.LastName",
               "agent.MiddleInitial",
               "agent.Title",
               "agent.Interests",
               "agent.Abbreviation",
               "agent.Name",
               "agentaddress.Email",
               "agentaddress.URL",
               "agent.Remarks",
               "agent.TimestampModified",
               "agent.TimestampCreated",
               "agent.LastEditedBy",
               "agent.ParentOrganizationID"};
       
       String[] addressColumns = {"address.AddressID",
                                    "address.Address",
                                    "address.Address2",
                                    "address.City",
                                    "address.State",
                                    "address.Country",
                                    "address.Postalcode",
                                    "address.Remarks",
                                    "address.TimestampModified",
                                    "address.TimestampCreated",
                                    "address.LastEditedBy",
                                    "agentaddress.IsCurrent",
                                    "agentaddress.Phone1",
                                    "agentaddress.Phone2",
                                    "agentaddress.Fax",
                                    "agentaddress.RoomOrBuilding",
                                    "address.AgentID"};
       
       Hashtable<Integer, Integer> agentTracker = new Hashtable<Integer, Integer>();
       Hashtable<Integer, Integer> addressTracker = new Hashtable<Integer, Integer>();
       Hashtable<Integer, Integer> oldAddrIds = new Hashtable<Integer, Integer>();
       Hashtable<Integer, Integer> oldAgentIds = new Hashtable<Integer, Integer>();
       
       try
       {
           Statement stmtX = connection.createStatement();
           ResultSet rsX   = stmtX.executeQuery("select AddressID from address order by AddressID");
           while (rsX.next())
           {
               int addrId = rsX.getInt(1);
               oldAddrIds.put(addrId, 0);
           }
           rsX.close();
           stmtX.close();
           
           stmtX = connection.createStatement();
           rsX   = stmtX.executeQuery("select AgentID from agent order by AgentID");
           while (rsX.next())
           {
               int agentId = rsX.getInt(1);
               oldAgentIds.put(agentId, 0);
           }
           rsX.close();
           stmtX.close();
           
           //////////////////////////////////////////////////////////////////////////////////
           // This does the part of AgentAddress where it has both an Address AND an Agent
           //////////////////////////////////////////////////////////////////////////////////
           
           Statement         stmt = connection.createStatement();
           ResultSet         rs   = stmt.executeQuery(sql);
           ResultSetMetaData rsmd = rs.getMetaData();
           List<FieldMetaData> fieldList = new ArrayList<FieldMetaData>();
           Hashtable<String, Integer> indexFromNameMap = new Hashtable<String, Integer>();
           
           BasicSQLUtils.getFieldMetaDataFromSchema(rsmd, fieldList);
           int inx = 1;
           for (FieldMetaData fmd : fieldList)
           {
               //System.out.println("["+fmd.getName()+"]  "+fmd.getType());
               indexFromNameMap.put(fmd.getName(), inx++);
           }
           
           IdMapper agentIDMapper     = idMapperMgr.get("agent", "AgentID");
           IdMapper addrIDMapper      = idMapperMgr.get("address", "AddressID");
           IdMapper agentAddrIDMapper = idMapperMgr.get("agentaddress", "AgentAddressID");
           
           int agentIdInx = indexFromNameMap.get("agent.AgentID");
           int addrIdInx  = indexFromNameMap.get("address.AddressID");
           
           int newAgentId = 1;
           int newAddrId  = 1;
           
           int recordCnt = 0;
           while (rs.next())
           {
               int agentAddressId = rs.getInt(1);
               int agentId = rs.getInt(agentIdInx);
               int addrId  = rs.getInt(addrIdInx);
               if (addrId == -1505739717)
               {
                   int x = 0;
                   x++;
               }
  
               recordCnt++;

               boolean alreadyInserted = agentTracker.get(agentId) != null;
               if (!alreadyInserted)
               {
                   // Create Agent
                    StringBuilder strBuf = new StringBuilder("INSERT INTO agent VALUES (");
                    for (int i=0;i<agentColumns.length;i++)
                    {
                        if (i > 0) strBuf.append(",");
                        //System.out.println(agentColumns[i]);
                        if (i == 0)
                        {
                            strBuf.append(newAgentId);
                            
                        } else
                        {
                            inx = indexFromNameMap.get(agentColumns[i]);
                            strBuf.append(BasicSQLUtils.getStrValue(rs.getObject(inx)));
                        }
                    }
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
                        
                        agentTracker.put(agentId, newAgentId);
                        oldAgentIds.put(agentId, 1);
                        agentIDMapper.addIndex(newAgentId, agentId);
                        
                        agentAddrIDMapper.addIndex(newAgentId, agentAddressId);
                        
                        newAgentId++;
                        
                    } catch (SQLException e)
                    {
                        log.error(strBuf.toString());
                        log.error("Count: "+recordCnt);
                        e.printStackTrace();
                        log.error(e);
                        connection.close();
                        newDBConn.close();

                        return false;
                    }
                    
               } else
               {
                   log.info("Agent already Used ["+BasicSQLUtils.getStrValue(rs.getObject(indexFromNameMap.get("agent.LastName")))+"]");
                   
                   int newAID = agentTracker.get(agentId);
                   agentAddrIDMapper.addIndex(newAID, agentAddressId);
               }
               
               // Create Address
               boolean alreadyInsertedAddr = addressTracker.get(addrId) != null;
               if (!alreadyInsertedAddr)
               {
                   StringBuilder strBuf = new StringBuilder("INSERT INTO address VALUES (");
                   for (int i=0;i<addressColumns.length;i++)
                   {
                       if (i > 0) strBuf.append(",");
                       if (i == addressColumns.length-1)
                       {
                           strBuf.append(newAgentId);
                           
                       } else
                       {
                           
                           Integer inxInt = indexFromNameMap.get(addressColumns[i]);
                           String value;
                           if (i == 0)
                           {
                               value = Integer.toString(newAddrId);
                               
                           } else if (inxInt == null && addressColumns[i].equals("address.Address2"))
                           {
                               //System.out.println(addressColumns[i]);
                               value = "''";
                           } else if (addressColumns[i].equals("address.Address"))
                           {
                               value = BasicSQLUtils.getStrValue(StringEscapeUtils.escapeJava(rs.getString(inxInt)));
                               
                           } else
                           {
                               //System.out.println(addressColumns[i]);
                               value = BasicSQLUtils.getStrValue(rs.getObject(inxInt));
                           }
                           strBuf.append(value);
                       }
                   }
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
                       
                       addressTracker.put(addrId, newAddrId);
                       oldAddrIds.put(addrId, 1);
                       
                       addrIDMapper.addIndex(newAddrId, addrId);
                       newAddrId++;
                       
                   } catch (SQLException e)
                   {
                       log.error(strBuf.toString());
                       log.error("Count: "+recordCnt);
                       e.printStackTrace();
                       log.error(e);
                       connection.close();
                       newDBConn.close();
    
                       return false;
                   }
               }

               if (recordCnt % 250 == 0)
               {
                   log.info("AgentAddress Records: "+ recordCnt);
               }
           } // while 
           log.info("AgentAddress Records: "+ recordCnt);
           rs.close();
           stmt.close();

           
           //////////////////////////////////////////////////////////////////////////////////
           // This does the part of AgentAddress where it has JUST an Address
           //////////////////////////////////////////////////////////////////////////////////
           log.info("******** Doing AgentAddress JUST Address");
           sql = "Select agentaddress.*, address.* From address Inner Join agentaddress ON agentaddress.AddressID = address.AddressID Order By agentaddress.AgentAddressID Asc";

           stmt = connection.createStatement();
           rs   = stmt.executeQuery(sql);
           rsmd = rs.getMetaData();
           fieldList.clear();
           indexFromNameMap.clear();
           
           BasicSQLUtils.getFieldMetaDataFromSchema(rsmd, fieldList);
            inx = 1;
           for (FieldMetaData fmd : fieldList)
           {
               indexFromNameMap.put(fmd.getName(), inx++);
           }
           
           addrIdInx  = indexFromNameMap.get("address.AddressID");
           
           int newRecordsAdded = 0;
           recordCnt = 0;
           while (rs.next())
           {
               //int agentAddressId = rs.getInt(1);
               int addrId         = rs.getInt(addrIdInx);
 
               recordCnt++;

               // Create Address
               boolean alreadyInsertedAddr = addressTracker.get(addrId) != null;
               if (!alreadyInsertedAddr)
               {
                   StringBuilder strBuf = new StringBuilder("INSERT INTO address VALUES (");
                   for (int i=0;i<addressColumns.length;i++)
                   {
                       if (i > 0) strBuf.append(",");
                       if (i == addressColumns.length-1)
                       {
                           strBuf.append("NULL");
                           
                       } else
                       {
                           
                           Integer inxInt = indexFromNameMap.get(addressColumns[i]);
                           String value;
                           if (i == 0)
                           {
                               value = Integer.toString(newAddrId);
                               
                           } else if (inxInt == null && addressColumns[i].equals("address.Address2"))
                           {
                               value = "''";
                               
                           } else if (addressColumns[i].equals("address.Address"))
                           {
                               value = BasicSQLUtils.getStrValue(StringEscapeUtils.escapeJava(rs.getString(inxInt)));
                                                              
                           } else
                           {
                               //System.out.println(addressColumns[i]);
                               value = BasicSQLUtils.getStrValue(rs.getObject(inxInt));
                           }
                           strBuf.append(value);
                       }
                   }
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
                       
                       addressTracker.put(addrId, newAddrId);
                       oldAddrIds.put(addrId, 1);
                       
                       addrIDMapper.addIndex(newAddrId, addrId);
                       newAddrId++;
                       
                       newRecordsAdded++;
                       
                   } catch (SQLException e)
                   {
                       log.error(strBuf.toString());
                       log.error("Count: "+recordCnt);
                       e.printStackTrace();
                       log.error(e);
                       connection.close();
                       newDBConn.close();
    
                       return false;
                   }
               }

               if (recordCnt % 250 == 0)
               {
                   log.info("AgentAddress (Address Only) Records: "+ recordCnt+"  newRecordsAdded "+newRecordsAdded);
               }
           } // while 
           log.info("AgentAddress (Address Only) Records: "+ recordCnt);
           rs.close();
           stmt.close();
          
           //////////////////////////////////////////////////////////////////////////////////
           // This does the part of AgentAddress where it has JUST Agent
           //////////////////////////////////////////////////////////////////////////////////
           log.info("******** Doing AgentAddress JUST Agent");

           newRecordsAdded = 0;
           
           sql = "Select agentaddress.*, agent.* From agent Inner Join agentaddress ON agentaddress.AgentID = agent.AgentID Order By agentaddress.AgentAddressID Asc";
           
           stmt = connection.createStatement();
           rs   = stmt.executeQuery(sql);
           rsmd = rs.getMetaData();
           fieldList.clear();
           indexFromNameMap.clear();
           
           BasicSQLUtils.getFieldMetaDataFromSchema(rsmd, fieldList);
           inx = 1;
           for (FieldMetaData fmd : fieldList)
           {
               indexFromNameMap.put(fmd.getName(), inx++);
           }
           
           agentIdInx = indexFromNameMap.get("agent.AgentID");
           
           recordCnt = 0;
           while (rs.next())
           {
               int agentAddressId = rs.getInt(1);
               int agentId = rs.getInt(agentIdInx);
  
               recordCnt++;

               boolean alreadyInserted = agentTracker.get(agentId) != null;
               if (!alreadyInserted)
               {
                   // Create Agent
                    StringBuilder strBuf = new StringBuilder("INSERT INTO agent VALUES (");
                    for (int i=0;i<agentColumns.length;i++)
                    {
                        if (i > 0) strBuf.append(",");
                        if (i == 0)
                        {
                            strBuf.append(newAgentId);
                            
                            
                        } else
                        {
                            inx = indexFromNameMap.get(agentColumns[i]);
                            strBuf.append(BasicSQLUtils.getStrValue(rs.getObject(inx)));
                        }
                    }
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
                        
                        agentTracker.put(agentId, newAgentId);
                        oldAgentIds.put(agentId, 1);
                        agentIDMapper.addIndex(newAgentId, agentId);
                        
                        agentAddrIDMapper.addIndex(newAgentId, agentAddressId);
                        
                        newAgentId++;
                        
                        newRecordsAdded++;
                        
                    } catch (SQLException e)
                    {
                        log.error(strBuf.toString());
                        log.error("Count: "+recordCnt);
                        e.printStackTrace();
                        log.error(e);
                        connection.close();
                        newDBConn.close();

                        return false;
                    }
                    
               }
               
               if (recordCnt % 250 == 0)
               {
                   log.info("AgentAddress (Agent Only) Records: "+ recordCnt);
               }
           } // while 
           log.info("AgentAddress (Agent Only) Records: "+ recordCnt+"  newRecordsAdded "+newRecordsAdded);

           rs.close();
           stmt.close();

           
           if (oldAddrIds.size() > 0)
           {
               //System.out.println("Address Record IDs not used by AgentAddress:");
               
               StringBuilder sqlStr = new StringBuilder("select ");
               List<String> names = new ArrayList<String>();
               getFieldNamesFromSchema(connection, "address", names);
               sqlStr.append(buildSelectFieldList(names, "address"));
               sqlStr.append(" from address where AddressId in (");
               
               int cnt = 0;
               for (Enumeration<Integer> e=oldAddrIds.keys();e.hasMoreElements();)
               {
                   
                   Integer id = e.nextElement();
                   Integer val = oldAddrIds.get(id);
                   if (val == 0)
                   {
                       addrIDMapper.addIndex(newAddrId, id);
                       newAddrId++;

                       if (cnt > 0) sqlStr.append(",");
                       sqlStr.append(id);
                       cnt++;
                   }
               }
               sqlStr.append(")");
               
               copyTable(connection, newDBConn, sqlStr.toString(), "address", "address", null, null); // closes the connection automatically
           }
           
           if (oldAgentIds.size() > 0)
           {
               connection = oldDB.getConnectionToDB();
               newDBConn  = DBConnection.getConnection();

               StringBuilder sqlStr = new StringBuilder("select ");
               List<String> names = new ArrayList<String>();
               getFieldNamesFromSchema(connection, "agent", names);
               sqlStr.append(buildSelectFieldList(names, "agent"));
               sqlStr.append(" from agent where AgentId in (");
               
               int cnt = 0;
               for (Enumeration<Integer> e=oldAgentIds.keys();e.hasMoreElements();)
               {
                   
                   Integer id = e.nextElement();
                   Integer val = oldAgentIds.get(id);
                   if (val == 0)
                   {
                       agentIDMapper.addIndex(newAgentId, id);
                       newAgentId++;

                       if (cnt > 0) sqlStr.append(",");
                       sqlStr.append(id);
                       cnt++;
                   }
               }
               sqlStr.append(")");
               
               copyTable(connection, newDBConn, sqlStr.toString(), "agent", "agent", null, null);
               

           }
           
           
           log.info("Agent Address SQL recordCnt "+recordCnt);
           
           connection.close();
           newDBConn.close();
           
           return true;

       } catch (SQLException ex)
       {
           log.error(ex);
       }
       
       return false;

   }



    //--------------------------------------------------------------------
    //-- Static Methods
    //--------------------------------------------------------------------

    /**
     * @return wehether it should create the Map Tables, if false it assumes they have already been created
     */
    public static boolean shouldCreateMapTables()
    {
        return shouldCreateMapTables;
    }

    /**
     * @return whether the map tables should be removed
     */
    public static boolean shouldDeleteMapTables()
    {
        return shouldDeleteMapTables;
    }

    /**
     * Sets whether to create all the mapping tables
     * @param shouldCreateMapTables true to create, false to do nothing
     */
    public static void setShouldCreateMapTables(boolean shouldCreateMapTables)
    {
        GenericDBConversion.shouldCreateMapTables = shouldCreateMapTables;
    }

    /**
     * Sets whether the mapping tables should be deleted after the conversion process completes
     * @param shouldDeleteMapTables true to delete tables, false means do nothing
     */
    public static void setShouldDeleteMapTables(boolean shouldDeleteMapTables)
    {
        GenericDBConversion.shouldDeleteMapTables = shouldDeleteMapTables;
    }
}

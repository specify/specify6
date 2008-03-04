/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.buildSelectFieldList;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.copyTable;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.createFieldNameMap;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.deleteAllRecordsFromTable;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getFieldMetaDataFromSchema;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getFieldNamesFromSchema;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getStrValue;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDivision;
import static edu.ku.brc.specify.utilapps.DataBuilder.createInstitution;
import static edu.ku.brc.specify.utilapps.DataBuilder.createPickList;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.CatalogNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.HibernateDataProviderSession;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.treeutils.TreeHelper;
import edu.ku.brc.specify.utilapps.BldrPickList;
import edu.ku.brc.specify.utilapps.BldrPickListItem;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.specify.utilapps.DataBuilder;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.db.PickListItemIFace;
import edu.ku.brc.util.Pair;

/**
 * This class is used for copying over the and creating all the tables that are not specify to any
 * one collection. This assumes that the "static" data members of DBConnection have been set up with
 * the new Database's driver, name, user and password. This is created with the old Database's
 * driver, name, user and password.
 * 
 * @author rods
 * 
 */
public class GenericDBConversion
{
    public enum TableType {
        CollectingEvent(0), CollectionObject(1), ExternalResource(2), Preparation(3);

        TableType(final int ord)
        {
            this.ord = (short)ord;
        }

        private short ord;

        public short getType()
        {
            return ord;
        }
    }

    // public enum VISIBILITY_LEVEL {All, Institution}
    public static int                                       defaultVisibilityLevel = 0;                                                   // User/Security
                                                                                                                                            // changes

    protected static final int                              D_STATUS_CURRENT       = 1;
    protected static final int                              D_STATUS_UNKNOWN       = 2;
    protected static final int                              D_STATUS_OLD           = 3;

    protected static final Logger                           log                    = Logger
                                                                                           .getLogger(GenericDBConversion.class);

    protected static StringBuilder                          strBuf                 = new StringBuilder(
                                                                                           "");

    protected static SimpleDateFormat                       dateFormatter          = new SimpleDateFormat(
                                                                                           "yyyy-MM-dd hh:mm:ss");
    protected static Timestamp                              now                    = new Timestamp(
                                                                                           System
                                                                                                   .currentTimeMillis());
    protected static String                                 nowStr                 = dateFormatter
                                                                                           .format(now);

    protected String                                        oldDriver              = "";
    protected String                                        oldDBName              = "";
    protected String                                        oldUserName            = "";
    protected String                                        oldPassword            = "";

    protected IdMapperMgr                                   idMapperMgr;

    protected Connection                                    oldDBConn;
    protected Connection                                    newDBConn;

    protected String[]                                      standardDataTypes      = { "Plant",
            "Animal", "Mineral", "Fungi", "Anthropology"                          };
    protected Hashtable<String, Integer>                    dataTypeNameIndexes    = new Hashtable<String, Integer>();                   // Name
                                                                                                                                            // to
                                                                                                                                            // Index
                                                                                                                                            // in
                                                                                                                                            // Array
    protected Hashtable<String, Integer>                    dataTypeNameToIds      = new Hashtable<String, Integer>();                   // name
                                                                                                                                            // to
                                                                                                                                            // Record
                                                                                                                                            // ID

    protected Hashtable<String, TableStats>                 tableStatHash          = new Hashtable<String, TableStats>();

    // Helps during debugging
    protected static boolean                                shouldCreateMapTables  = true;
    protected static boolean                                shouldDeleteMapTables  = true;

    protected SpecifyAppContextMgr                          appContextMgr          = new SpecifyAppContextMgr();

    protected ProgressFrame                                 frame                  = null;
    protected boolean                                       hasFrame               = false;

    protected Hashtable<String, Integer>                    collectionHash         = new Hashtable<String, Integer>();
    protected Hashtable<String, String>                     prefixHash             = new Hashtable<String, String>();
    protected Hashtable<String, Integer>                    catNumSchemeHash       = new Hashtable<String, Integer>();

    protected Session                                       session;

    // Temp
    protected Agent                                         creatorAgent;
    protected Agent                                         modifierAgent;
    protected Hashtable<String, BasicSQLUtilsMapValueIFace> columnValueMapper      = new Hashtable<String, BasicSQLUtilsMapValueIFace>();
    protected Division                                      division = null;
    protected int                                           disciplineId           = 0;

    public GenericDBConversion()
    {
        // no op
    }

    /**
     * "Old" means the database you want to copy "from"
     * @param oldDriver old driver
     * @param oldDBName old database name
     * @param connectionStr old server name
     * @param oldUserName old user name
     * @param oldPassword old password
     */
    public GenericDBConversion(final String oldDriver, final String oldDBName,
            final String connectionStr, final String oldUserName, final String oldPassword)
    {
        this.oldDriver = oldDriver;
        this.oldDBName = oldDBName;
        this.oldUserName = oldUserName;
        this.oldPassword = oldPassword;
        this.idMapperMgr = IdMapperMgr.getInstance();

        DBConnection oldDB = DBConnection.createInstance(oldDriver, null, oldDBName, connectionStr,
                oldUserName, oldPassword);
        oldDBConn = oldDB.createConnection();
        newDBConn = DBConnection.getInstance().createConnection();

    }

    /**
     * @param newDBConn the newDBConn to set
     */
    public void setNewDBConn(Connection newDBConn)
    {
        this.newDBConn = newDBConn;
    }

    public void shutdown()
    {
        try
        {
            oldDBConn.close();
            newDBConn.close();

        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Sets a UI feedback frame.
     * @param frame the frame
     */
    public void setFrame(final ProgressFrame frame)
    {
        this.frame = frame;
        hasFrame = frame != null;

        BasicSQLUtils.setFrame(frame);

        if (idMapperMgr != null)
        {
            idMapperMgr.setFrame(frame);
        }
    }

    public void setOverall(final int min, final int max)
    {
        if (hasFrame)
        {
            frame.setOverall(min, max);
        }
    }

    public void setOverall(final int value)
    {
        if (hasFrame)
        {
            frame.setOverall(value);
        }
    }

    public void setProcess(final int min, final int max)
    {
        if (hasFrame)
        {
            frame.setProcess(min, max);
        }
    }

    public void setProcess(final int value)
    {
        if (hasFrame)
        {
            frame.setProcess(value);
        }
    }

    public void setDesc(final String text)
    {
        if (hasFrame)
        {
            frame.setDesc(text);
        }
    }

    /**
     * Return the SQL Connection to the Old Database
     * @return the SQL Connection to the Old Database
     */
    public Connection getOldDBConnection()
    {
        return oldDBConn;
    }

    /**
     * Return the SQL Connection to the New Database
     * @return the SQL Connection to the New Database
     */
    public Connection getNewDBConnection()
    {
        return newDBConn;
    }

    public void showStats()
    {
        for (Enumeration<TableStats> ts = tableStatHash.elements(); ts.hasMoreElements();)
        {
            ts.nextElement().compareStats();
        }
    }

    /**
     * 
     */
    public void doInitialize()
    {

        BasicSQLUtilsMapValueIFace collectionMemberIDValueMapper = getCollectionMemberIDValueMapper();
        BasicSQLUtilsMapValueIFace agentCreatorValueMapper       = getAgentCreatorValueMapper();
        BasicSQLUtilsMapValueIFace agentModiferValueMapper       = getAgentModiferValueMapper();
        BasicSQLUtilsMapValueIFace versionValueMapper            = getVersionValueMapper();
        BasicSQLUtilsMapValueIFace divisionValueMapper           = getDivisionValueMapper();

        columnValueMapper.put("CollectionMemberID", collectionMemberIDValueMapper);
        columnValueMapper.put("CreatedByAgentID",   agentCreatorValueMapper);
        columnValueMapper.put("ModifiedByAgentID",  agentModiferValueMapper);
        columnValueMapper.put("Version",            versionValueMapper);
        columnValueMapper.put("DivisionID",         divisionValueMapper);

        String[] tableNames = { "locality", "accession", "accessionagents",
                "accessionauthorizations", "address", "agent", "agentaddress", "authors",
                "biologicalobjectattributes", "biologicalobjectrelation",
                "biologicalobjectrelationtype", "borrow", "borrowagents", "borrowmaterial",
                "borrowreturnmaterial", "borrowshipments", "catalogseries",
                "catalogseriesdefinition", "collectingevent", "collection", "collectionobject",
                "collectionobjectcatalog", "collectionobjectcitation", "collectionobjecttype",
                "collectiontaxonomytypes", "collectors", "collevent_verbdate", "deaccession",
                "deaccessionagents", "deaccessioncollectionobject", "determination",
                "determinationcitation", "exchangein", "exchangeout", "geography",
                "geologictimeboundary", "geologictimeperiod", "grouppersons", "habitat", "image",
                "imageagents", "imagecollectionobjects", "imagelocalities", "journal", "loan",
                "loanagents", "loanphysicalobject", "loanreturnphysicalobject", "locality",
                "localitycitation", "observation", "otheridentifier", "permit", "preparation",
                "project", "projectcollectionobjects", "raveproject", "referencework", "reports",
                "shipment", "sound", "soundeventstorage", "stratigraphy", "taxoncitation",
                "taxonname", "taxonomicunittype", "taxonomytype", "usysaccessionarole",
                "usysaccessionstatus", "usysaccessiontype", "usysactionhotkey", "usysautoreports",
                "usysbiologicalsex", "usysbiologicalstage", "usysborrowagenrole",
                "usyscatalognumber", "usyscollectinggrouppermittedto", "usyscollectingmethod",
                "usyscollectioncontainertype", "usyscollobjprepmeth", "usysdatadefinition",
                "usysdatemasktypes", "usysdeaccessiorole", "usysdeaccessiotype",
                "usysdefaultquerydef", "usysdeterminatconfidence", "usysdeterminatmethod",
                "usysdeterminattypestatusname", "usysdigir", "usysdigirconcept", "usysfieldtypes",
                "usysfulltextcatalog", "usysfulltextfield", "usysfulltextupdates",
                "usyshabitathabitattype", "usyslanguages", "usyslatlongmasktypes",
                "usyslatlongtype", "usysloanagentsrole", "usyslocalityelevationmethod",
                "usyslocalitygrouppermittedtovi", "usyslocalitylatlongmethod", "usysmetacontrol",
                "usysmetadefaultcontrol", "usysmetafieldset", "usysmetafieldsetsubtype",
                "usysmetainterface", "usysmetaobject", "usysmetarelationshipsubtyperule",
                "usysmetasubtypecontents", "usysmetasubtypefield",
                "usysobservatioobservationmetho", "usyspermittype", "usyspreparatiocontainertype",
                "usyspreparatiomedium", "usyspreparatiopreparationtype", "usysquery",
                "usysqueryinterfaces", "usyssecuritytypes", "usysshipmentshipmentmethod",
                "usysspecifyadmin", "usysspecifyfullaccessuser", "usysspecifyguest",
                "usysspecifylimitedaccessuser", "usysspecifymanager", "usysstatistics",
                "usystaxonnamegrouppermittedtov", "usystemprequired", "usysuserpreferences",
                "usysversion", "usyswebqueryform", "usyswebquerylog", "usyswebquerytemplate",
                "webadmin" };
    }

    /**
     * Creates backstop creator and modifer agents.
     */
    public void initializeAgentInfo(final boolean startFromScratch)
    {
        if (startFromScratch)
        {
            Hashtable<String, Agent> agentMap = new Hashtable<String, Agent>();
            creatorAgent = new Agent();
            creatorAgent.initialize();
            creatorAgent.setAgentType(Agent.PERSON);
            creatorAgent.setFirstName("DB");
            creatorAgent.setLastName("Creator");
            agentMap.put("Creator", creatorAgent);

            modifierAgent = new Agent();
            modifierAgent.initialize();
            modifierAgent.setAgentType(Agent.PERSON);
            modifierAgent.setFirstName("DB");
            modifierAgent.setLastName("Modifier");
            agentMap.put("Modifier", modifierAgent);

            setSession(HibernateUtil.getCurrentSession());
            startTx();
            persist(creatorAgent);
            persist(modifierAgent);
            commitTx();
            setSession(null);

        } else
        {
            DataProviderSessionIFace s = DataProviderFactory.getInstance().createSession();
            creatorAgent = s.getData(Agent.class, "lastName", "Creator",
                    DataProviderSessionIFace.CompareType.Equals);
            modifierAgent = s.getData(Agent.class, "lastName", "Modifier",
                    DataProviderSessionIFace.CompareType.Equals);
            s.close();
        }

        /*
         * for (String tableName : tableNames) { try { Statement stmt = oldDBConn.createStatement();
         * ResultSet rs = stmt.executeQuery("select unique LastEditedBy from "+tableName);
         * 
         * while (rs.next()) { String modifierAgent = rs.getString(1); if
         * (StringUtils.isNotEmpty(editedBy)) { Agent agent = agentMap.get(editedBy); } }
         *  } catch (Exception ex) {
         *  } }
         */
    }

    /**
     * 
     */
    public void mapIds() throws SQLException
    {
        log.debug("mapIds()");

        // These are the names as they occur in the old datamodel
        String[] tableNames = {
                "Accession",
                // XXX "AccessionAgents",
                "AccessionAuthorizations",
                // "Address",
                // "Agent",
                // "AgentAddress",
                // XXX "Authors",
                "BiologicalObjectAttributes", // Turn back on when datamodel checked in
                "BiologicalObjectRelation",
                "BiologicalObjectRelationType",
                "Borrow",
                // XXX "BorrowAgents",
                "BorrowMaterial",
                "BorrowReturnMaterial",
                // "BorrowShipments",
                // "Collection",
                "CatalogSeriesDefinition",
                "CollectingEvent",
                // "Collection",
                "CollectionObject",
                "CollectionObjectCatalog",
                "CollectionObjectCitation",
                "CollectionObjectType",
                "CollectionTaxonomyTypes",
                "Collectors",
                "Deaccession",
                // XXX "DeaccessionAgents",
                "DeaccessionCollectionObject",
                "Determination",
                "DeterminationCitation",
                "ExchangeIn",
                "ExchangeOut",
                // "Geography",
                "GeologicTimeBoundary",
                // "GeologicTimePeriod",
                "GroupPersons",
                "Habitat", // Turn back on when datamodel checked in
                // XXX "ImageAgents",
                "ImageCollectionObjects", "ImageLocalities",
                "Journal",
                "Loan",
                // XXX "LoanAgents",
                "LoanPhysicalObject", "LoanReturnPhysicalObject", "Locality", "LocalityCitation",
                "Observation", "OtherIdentifier",
                "Permit",
                "Preparation", // Turn back on when datamodel checked in
                "Project", "ProjectCollectionObjects", "ReferenceWork", "Shipment", "Sound",
                "SoundEventStorage", "Stratigraphy", "TaxonCitation", "TaxonName",
                "TaxonomicUnitType", "TaxonomyType" };

        // shouldCreateMapTables = false;

        IdTableMapper idMapper = null;
        for (String tableName : tableNames)
        {
            idMapper = idMapperMgr.addTableMapper(tableName, tableName + "ID");
            log.debug("mapIds() for table" + tableName);
            if (shouldCreateMapTables)
            {
                idMapper.mapAllIds();
            }
        }

        idMapper = idMapperMgr
                .addTableMapper(
                        "TaxonomyType",
                        "TaxonomyTypeID",
                        "select TaxonomyTypeID, TaxonomyTypeName from taxonomytype where TaxonomyTypeID in (SELECT distinct TaxonomyTypeID from taxonname where RankId <> 0)");
        if (shouldCreateMapTables)
        {
            idMapper.mapAllIdsWithSQL();
        }

        // Map all the Logical IDs
        idMapper = idMapperMgr.addTableMapper("collectionobject", "CollectionObjectID");
        if (shouldCreateMapTables)
        {
            idMapper
                    .mapAllIds("select CollectionObjectID from collectionobject Where collectionobject.DerivedFromID Is Null order by CollectionObjectID");
        }

        // meg commented out because it was blowing away map table created above
        // // Map all the Physical IDs
        // idMapper = idMapperMgr.addTableMapper("preparation", "PreparationID");
        // if (shouldCreateMapTables)
        // {
        // idMapper.mapAllIds("select CollectionObjectID from collectionobject Where not
        // (collectionobject.DerivedFromID Is Null) order by CollectionObjectID");
        // }

        // Map all the Physical IDs
        // idMapper = idMapperMgr.addTableMapper("geography", "GeographyID");
        // if (shouldCreateMapTables)
        // {
        // idMapper.mapAllIds("SELECT DISTINCT GeographyID,ContinentOrOcean,Country,State,County" +
        // "FROM demo_fish2.geography" +
        // "WHERE( (ContinentOrOcean IS NOT NULL) OR (Country IS NOT NULL) OR (State IS NOT NULL) OR
        // (County IS NOT NULL) )" +
        // "AND ( (IslandGroup IS NULL) AND (Island IS NULL) AND (WaterBody IS NULL) AND (Drainage
        // IS NULL) ) " +
        // "GROUP BY ContinentOrOcean,Country,State,County" );
        // }

        // Meg had to added conditional for Current field
        String oldDetermination_Current = "[Current]";
        String oldDetermination_CurrentValue = "1";

        if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
        {
            oldDetermination_Current = "IsCurrent";
            oldDetermination_CurrentValue = "-1";
        }
        // Map all the CollectionObject to its TaxonomyType
        // Meg had to add taxonname.TaxonomyTypeID to the GroupBy clause for SQL Server, ugh.
        // IdHashMapper idHashMapper = idMapperMgr.addHashMapper("ColObjCatToTaxonType", "Select
        // collectionobjectcatalog.CollectionObjectCatalogID, taxonname.TaxonomyTypeID From
        // collectionobjectcatalog Inner Join determination ON determination.BiologicalObjectID =
        // collectionobjectcatalog.CollectionObjectCatalogID Inner Join taxonname ON
        // taxonname.TaxonNameID = determination.TaxonNameID Where determination.IsCurrent = '-1'
        // group by collectionobjectcatalog.CollectionObjectCatalogID, taxonname.TaxonomyTypeID");
        IdHashMapper idHashMapper = idMapperMgr
                .addHashMapper(
                        "ColObjCatToTaxonType",
                        "Select collectionobjectcatalog.CollectionObjectCatalogID, taxonname.TaxonomyTypeID From collectionobjectcatalog Inner Join determination ON determination.BiologicalObjectID = collectionobjectcatalog.CollectionObjectCatalogID Inner Join taxonname ON taxonname.TaxonNameID = determination.TaxonNameID Where determination."
                                + oldDetermination_Current
                                + " = '"
                                + oldDetermination_CurrentValue
                                + "'  group by collectionobjectcatalog.CollectionObjectCatalogID, taxonname.TaxonomyTypeID");

        if (shouldCreateMapTables)
        {
            idHashMapper.mapAllIds();
            log.info("colObjTaxonMapper: " + idHashMapper.size());

        }

        // When you run in to this table1.field, go to that table2 and look up the id
        String[] mappings = {
                // "DeaccessionCollectionObject", "DeaccessionCollectionObjectID",
                // "DeaccessionPreparation", "DeaccessionPreparationID",
                // "LoanReturnPhysicalObject", "LoanReturnPhysicalObjectID",
                // "LoanReturnPreparation", "LoanReturnPreparationID",

                "BorrowReturnMaterial",
                "BorrowMaterialID",
                "BorrowMaterial",
                "BorrowMaterialID",
                // XXX "BorrowReturnMaterial", "ReturnedByID", "Agent", "AgentID",
                // XXX "Preparation", "PreparedByID", "Agent", "AgentID",
                "Preparation",
                "ParasiteTaxonNameID",
                "TaxonName",
                "TaxonNameID",

                "LoanPhysicalObject",
                "PhysicalObjectID",
                "preparation",
                "PreparationID",
                "LoanPhysicalObject",
                "LoanID",
                "Loan",
                "LoanID",

                // XXX "ExchangeIn", "ReceivedFromOrganizationID", "Agent", "AgentID",
                // XXX "ExchangeIn", "CatalogedByID", "Agent", "AgentID",
                // XXX "Collection", "OrganizationID", "Agent", "AgentID",
                // XXX "GroupPersons", "GroupID", "Agent", "AgentID",
                // XXX "GroupPersons", "MemberID", "Agent", "AgentID",

                // ??? "ExchangeOut", "CollectionID", "Collection", "CollectionID",
                // XXX "ExchangeOut", "SentToOrganizationID", "Agent", "AgentID",
                // XXX "ExchangeOut", "CatalogedByID", "Agent", "AgentID",
                "ExchangeOut",
                "ShipmentID",
                "Shipment",
                "ShipmentID",

                "ReferenceWork",
                "JournalID",
                "Journal",
                "JournalID",
                "ReferenceWork",
                "ContainingReferenceWorkID",
                "ReferenceWork",
                "ReferenceWorkID",

                // "ImageAgents", "ImageID", "Image", "ImageID",
                // "ImageAgents", "AgentID", "Agent", "AgentID",

                "BiologicalObjectRelation",
                "BiologicalObjectID",
                "CollectionObject",
                "CollectionObjectID",
                "BiologicalObjectRelation",
                "RelatedBiologicalObjectID",
                "CollectionObject",
                "CollectionObjectID",
                "BiologicalObjectRelation",
                "BiologicalObjectRelationTypeID",
                "BiologicalObjectRelationType",
                "BiologicalObjectRelationTypeID",

                // XXX "Shipment", "ShipperID", "AgentAddress", "AgentAddressID",
                // XXX "Shipment", "ShippedToID", "AgentAddress", "AgentAddressID",
                // XXX "Shipment", "ShippedByID", "Agent", "AgentID",
                // "Shipment", "ShipmentMethodID", "ShipmentMethod", "ShipmentMethodID",

                "Habitat",
                "HostTaxonID",
                "TaxonName",
                "TaxonNameID",

                // XXX "Authors", "AgentID", "Agent", "AgentID",
                "Authors",
                "ReferenceWorkID",
                "ReferenceWork",
                "ReferenceWorkID",
                "BorrowMaterial",
                "BorrowID",
                "Borrow",
                "BorrowID",
                "BorrowAgents",
                "BorrowID",
                "Borrow",
                "BorrowID",
                // XXX "BorrowAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",

                "DeaccessionCollectionObject",
                "DeaccessionID",
                "Deaccession",
                "DeaccessionID",
                "DeaccessionCollectionObject",
                "CollectionObjectID",
                "Preparation",
                "PreparationID",

                // "DeaccessionCollectionObject", "DeaccessionID", "Deaccession", "DeaccessionID",
                // // not sure this is needed
                // "DeaccessionCollectionObject", "CollectionObjectID",
                // "DeaccessionCollectionObject", "CollectionObjectID", // not sure this is needed

                "CollectionObjectCitation",
                "ReferenceWorkID",
                "ReferenceWork",
                "ReferenceWorkID",
                "CollectionObjectCitation",
                "BiologicalObjectID",
                "CollectionObject",
                "CollectionObjectID",
                "CollectingEvent",
                "HabitatAttributesID",
                "Habitat",
                "HabitatID",
                "CollectingEvent",
                "LocalityID",
                "Locality",
                "LocalityID",
                "CollectingEvent",
                "StratigraphyID",
                "Stratigraphy",
                "StratigraphyID",

                "Collectors",
                "CollectingEventID",
                "CollectingEvent",
                "CollectingEventID",
                // XXX "Collectors", "AgentID", "Agent", "AgentID",

                // XXX "Permit", "IssuerID", "AgentAddress", "AgentAddressID",
                // XXX "Permit", "IssueeID", "AgentAddress", "AgentAddressID",
                // XXX "Sound", "RecordedByID", "Agent", "AgentID",
                "TaxonCitation",
                "ReferenceWorkID",
                "ReferenceWork",
                "ReferenceWorkID",
                "TaxonCitation",
                "TaxonNameID",
                "TaxonName",
                "TaxonNameID",
                // XXX "Determination", "DeterminerID", "Agent", "AgentID",
                "Determination",
                "TaxonNameID",
                "TaxonName",
                "TaxonNameID",
                "Determination",
                "BiologicalObjectID",
                "CollectionObject",
                "CollectionObjectID",

                // ??? "GeologicTimePeriod", "UpperBoundaryID", "UpperBoundary", "UpperBoundaryID",
                // ??? "GeologicTimePeriod", "LowerBoundaryID", "LowerBoundary", "LowerBoundaryID",

                // ************************************************************************************
                // NOTE: Since we are mapping CollectionObjectType to CatalogSeriesDefinition
                // then we might as well map CollectionObjectTypeID to CatalogSeriesDefinitionID
                //
                // The Combination of the CatalogSeriesDefinition and CollectionObjectType become
                // the
                // new Discipline
                // As you might expect the CatalogSeriesDefinitionID is mapped to the new
                // Discipline
                // ************************************************************************************
                "CollectionObjectType",
                "CollectionObjectTypeID",
                "CatalogSeriesDefinition",
                "CatalogSeriesDefinitionID",
                "CollectionObject",
                "CollectionObjectTypeID",
                "CatalogSeriesDefinition",
                "CatalogSeriesDefinitionID",

                "CollectionObject",
                "CollectingEventID",
                "CollectingEvent",
                "CollectingEventID",

                "CollectionObject",
                "AccessionID",
                "Accession",
                "AccessionID",
                // XXX "CollectionObject", "CatalogerID", "Agent", "AgentID",
                "Loan",
                "ShipmentID",
                "Shipment",
                "ShipmentID",
                "AccessionAuthorizations",
                "AccessionID",
                "Accession",
                "AccessionID",
                "AccessionAuthorizations",
                "PermitID",
                "Permit",
                "PermitID",
                "AccessionAgents",
                "AccessionID",
                "Accession",
                "AccessionID",
                // XXX "AccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
                "DeterminationCitation",
                "ReferenceWorkID",
                "ReferenceWork",
                "ReferenceWorkID",
                "DeterminationCitation",
                "DeterminationID",
                "Determination",
                "DeterminationID",
                "OtherIdentifier",
                "CollectionObjectID",
                "CollectionObject",
                "CollectionObjectID",

                // XXX "Agent", "ParentOrganizationID", "Agent", "AgentID",
                // XXX "AgentAddress", "AddressID", "Address", "AddressID",
                // XXX "AgentAddress", "AgentID", "Agent", "AgentID",
                // XXX "AgentAddress", "OrganizationID", "Agent", "AgentID",

                "LocalityCitation", "ReferenceWorkID", "ReferenceWork",
                "ReferenceWorkID",
                "LocalityCitation",
                "LocalityID",
                "Locality",
                "LocalityID",
                "LoanReturnPhysicalObject",
                "LoanPhysicalObjectID",
                "LoanPhysicalObject",
                "LoanPhysicalObjectID",
                // XXX "LoanReturnPhysicalObject", "ReceivedByID", "Agent", "AgentID",
                "LoanReturnPhysicalObject", "DeaccessionPhysicalObjectID",
                "CollectionObject",
                "CollectionObjectID",
                "DeaccessionAgents",
                "DeaccessionID",
                "Deaccession",
                "DeaccessionID",
                // XXX "DeaccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
                "ProjectCollectionObjects", "ProjectID",
                "Project",
                "ProjectID",
                "ProjectCollectionObjects",
                "CollectionObjectID",
                "CollectionObject",
                "CollectionObjectID",
                // XXX "Project", "ProjectAgentID", "Agent", "AgentID",
                "LoanAgents",
                "LoanID",
                "Loan",
                "LoanID",
                // XXX "LoanAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",

                // taxonname ID mappings
                "TaxonName", "ParentTaxonNameID", "TaxonName", "TaxonNameID", "TaxonName",
                "TaxonomicUnitTypeID", "TaxonomicUnitType", "TaxonomicUnitTypeID", "TaxonName",
                "TaxonomyTypeID", "TaxonomyType", "TaxonomyTypeID", "TaxonName", "AcceptedID",
                "TaxonName", "TaxonNameID",

                // taxonomytype ID mappings
                // NONE

                // taxonomicunittype ID mappings
                "TaxonomicUnitType", "TaxonomyTypeID", "TaxonomyType", "TaxonomyTypeID" };

        for (int i = 0; i < mappings.length; i += 4)
        {
            idMapperMgr.mapForeignKey(mappings[i], mappings[i + 1], mappings[i + 2],
                    mappings[i + 3]);
        }
    }

    /**
     * @throws SQLException
     */
    public void mapAgentRelatedIds() throws SQLException
    {
        log.debug("mapAgentRelatedIds()");

        // These are the names as they occur in the old datamodel
        String[] tableNames = { "AccessionAgents",
        // "Address",
                // "Agent",
                // "AgentAddress",
                "Authors", "BorrowAgents", "DeaccessionAgents", "ImageAgents", "LoanAgents", };

        // shouldCreateMapTables = false;

        IdTableMapper idMapper = null;
        for (String tableName : tableNames)
        {
            idMapper = idMapperMgr.addTableMapper(tableName, tableName + "ID");
            log.debug("mapIds() for table" + tableName);
            if (shouldCreateMapTables)
            {
                idMapper.mapAllIds();
            }
        }

        // When you run in to this table1.field, go to that table2 and look up the id
        String[] mappings = { "BorrowReturnMaterial", "ReturnedByID", "Agent", "AgentID",
                "Preparation", "PreparedByID", "Agent", "AgentID", "ExchangeIn",
                "ReceivedFromOrganizationID", "Agent", "AgentID", "ExchangeIn", "CatalogedByID",
                "Agent", "AgentID", "Collection", "OrganizationID", "Agent", "AgentID",
                "GroupPersons", "GroupID", "Agent", "AgentID", "GroupPersons", "MemberID", "Agent",
                "AgentID", "ExchangeOut", "SentToOrganizationID", "Agent", "AgentID",
                "ExchangeOut", "CatalogedByID", "Agent", "AgentID", "ExchangeOut", "ShipmentID",
                "Shipment", "ShipmentID", "Shipment", "ShipperID", "AgentAddress",
                "AgentAddressID", "Shipment", "ShippedToID", "AgentAddress", "AgentAddressID",
                "Shipment", "ShippedByID", "Agent", "AgentID", "Authors", "AgentID", "Agent",
                "AgentID", "BorrowAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
                "Collectors", "AgentID", "Agent", "AgentID", "Permit", "IssuerID", "AgentAddress",
                "AgentAddressID", "Permit", "IssueeID", "AgentAddress", "AgentAddressID", "Sound",
                "RecordedByID", "Agent", "AgentID", "Determination", "DeterminerID", "Agent",
                "AgentID", "CollectionObject", "CatalogerID", "Agent", "AgentID",
                "AccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID", "Agent",
                "ParentOrganizationID", "Agent", "AgentID", "AgentAddress", "AddressID", "Address",
                "AddressID", "AgentAddress", "AgentID", "Agent", "AgentID", "AgentAddress",
                "OrganizationID", "Agent", "AgentID", "LoanReturnPhysicalObject", "ReceivedByID",
                "Agent", "AgentID", "DeaccessionAgents", "AgentAddressID", "AgentAddress",
                "AgentAddressID", "Project", "ProjectAgentID", "Agent", "AgentID", "LoanAgents",
                "AgentAddressID", "AgentAddress", "AgentAddressID",

        // XXX "BorrowReturnMaterial", "ReturnedByID", "Agent", "AgentID",
        // XXX "Preparation", "PreparedByID", "Agent", "AgentID",
        // XXX "ExchangeIn", "ReceivedFromOrganizationID", "Agent", "AgentID",
        // XXX "ExchangeIn", "CatalogedByID", "Agent", "AgentID",
        // XXX "Collection", "OrganizationID", "Agent", "AgentID",
        // XXX "GroupPersons", "GroupID", "Agent", "AgentID",
        // XXX "GroupPersons", "MemberID", "Agent", "AgentID",
        // XXX "ExchangeOut", "SentToOrganizationID", "Agent", "AgentID",
        // XXX "ExchangeOut", "CatalogedByID", "Agent", "AgentID",
        // XXX "Shipment", "ShipperID", "AgentAddress", "AgentAddressID",
        // XXX "Shipment", "ShippedToID", "AgentAddress", "AgentAddressID",
        // XXX "Shipment", "ShippedByID", "Agent", "AgentID",
        // XXX "Authors", "AgentID", "Agent", "AgentID",
        // XXX "BorrowAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
        // XXX "Collectors", "AgentID", "Agent", "AgentID",
        // XXX "Permit", "IssuerID", "AgentAddress", "AgentAddressID",
        // XXX "Permit", "IssueeID", "AgentAddress", "AgentAddressID",
        // XXX "Sound", "RecordedByID", "Agent", "AgentID",
        // XXX "Determination", "DeterminerID", "Agent", "AgentID",
        // XXX "Determination", "TaxonNameID", "TaxonName", "TaxonNameID",
        // XXX "Determination", "BiologicalObjectID", "CollectionObject", "CollectionObjectID",
        // XXX "AccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
        // XXX "Agent", "ParentOrganizationID", "Agent", "AgentID",
        // XXX "AgentAddress", "AddressID", "Address", "AddressID",
        // XXX "AgentAddress", "AgentID", "Agent", "AgentID",
        // XXX "AgentAddress", "OrganizationID", "Agent", "AgentID",
        // XXX "LoanReturnPhysicalObject", "ReceivedByID", "Agent", "AgentID",
        // XXX "DeaccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
        // XXX "Project", "ProjectAgentID", "Agent", "AgentID",
        // XXX "LoanAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",

        };

        for (int i = 0; i < mappings.length; i += 4)
        {
            idMapperMgr.mapForeignKey(mappings[i], mappings[i + 1], mappings[i + 2],
                    mappings[i + 3]);
        }
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getCollectionMemberIDValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            // @Override
            public String mapValue(Object oldValue)
            {
                return "1";
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getAgentCreatorValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            // @Override
            public String mapValue(Object oldValue)
            {
                return "100";
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getAgentModiferValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            // @Override
            public String mapValue(Object oldValue)
            {
                return "100";
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getVersionValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            // @Override
            public String mapValue(Object oldValue)
            {
                return "0";
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getDivisionValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            // @Override
            public String mapValue(Object oldValue)
            {
                return division.getDivisionId().toString();
            }
        };
    }

    /**
     * Removes all the records from every table in the new database and then copies over all the
     * tables that have few if any changes to their schema
     */
    public void copyTables()
    {
        log.debug("copyTables()");
        // cleanAllTables(); // from DBCOnnection which is the new DB

        // String[] tablesToMoveOver2 = {
        // "LoanPhysicalObject"
        // };

        BasicSQLUtilsMapValueIFace collectionMemberIDValueMapper = getCollectionMemberIDValueMapper();
        BasicSQLUtilsMapValueIFace agentCreatorValueMapper = getAgentCreatorValueMapper();
        BasicSQLUtilsMapValueIFace agentModiferValueMapper = getAgentModiferValueMapper();
        BasicSQLUtilsMapValueIFace versionValueMapper      = getVersionValueMapper();
        BasicSQLUtilsMapValueIFace divisionValueMapper     = getDivisionValueMapper();

        String[] tablesToMoveOver = { "AccessionAgent", "Accession", "AccessionAuthorization",
                "Author", "BiologicalObjectAttributes", "Borrow", "BorrowAgent", "BorrowMaterial",
                "BorrowReturnMaterial", "CollectingEvent", "CollectionObjectCitation", "Collector",
                "Deaccession", "DeaccessionAgent", "DeterminationCitation", "ExchangeIn",
                "ExchangeOut", "GroupPerson", "Journal", "Loan", "LoanAgent",
                "DeaccessionCollectionObject", "LoanReturnPhysicalObject", "LocalityCitation",
                "OtherIdentifier", "Permit", "Preparation", // this is really an Attributes Table
                                                            // (PreparationAttributes)
                "Project",
                // "ProjectCollectionObjects", // I think we got rid of this!
                "ReferenceWork",
                // "Shipment", // needs it's own conversion
                "TaxonCitation", };

        String oldLoanReturnPhysicalObj_Date_FieldName = "Date";
        String oldRefWork_Date_FieldName = "Date";
        String oldDeaccession_Date_FieldName = "Date";
        String oldHabitat_Current_FieldName = "Current";
        String oldAuthors_Order_FieldName = "Order";
        String oldCollectors_Order_FieldName = "Order";
        String oldGroupPersons_Order_FieldName = "Order";
        String oldStratigraphy_Group_FieldName = "Group";
        String oldBorrowReturnMaterial_Date_FieldName = "Date";

        if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
        {
            oldDeaccession_Date_FieldName = "Date1";
            oldRefWork_Date_FieldName = "Date1";
            oldLoanReturnPhysicalObj_Date_FieldName = "Date1";
            oldBorrowReturnMaterial_Date_FieldName = "Date1";
            oldHabitat_Current_FieldName = "IsCurrent";
            oldAuthors_Order_FieldName = "Order1";
            oldCollectors_Order_FieldName = "Order1";
            oldGroupPersons_Order_FieldName = "Order1";
            oldStratigraphy_Group_FieldName = "Group1";
        }

        // This maps old column names to new column names, they must be in pairs
        // Ths Table Name is the "TO Table" name
        Map<String, Map<String, String>> tableMaps = new Hashtable<String, Map<String, String>>();
        // ----------------------------------------------------------------------------------------------------------------------
        // NEW TABLE NAME NEW FIELD NAME, OLD FIELD NAME
        // ----------------------------------------------------------------------------------------------------------------------
        tableMaps.put("accession", createFieldNameMap(new String[] { "AccessionNumber", "Number" }));
        tableMaps.put("accessionagent", createFieldNameMap(new String[] { "AgentID",
                "AgentAddressID", "AccessionAgentID", "AccessionAgentsID" }));
        tableMaps.put("accessionauthorization", createFieldNameMap(new String[] {
                "AccessionAuthorizationID", "AccessionAuthorizationsID" }));
        tableMaps.put("author", createFieldNameMap(new String[] { "OrderNumber",
                oldAuthors_Order_FieldName, "AuthorID", "AuthorsID" }));
        tableMaps.put("borrow", createFieldNameMap(new String[] { "IsClosed", "Closed" }));
        tableMaps.put("borrowagent", createFieldNameMap(new String[] { "AgentID", "AgentAddressID",
                "BorrowAgentID", "BorrowAgentsID" }));
        tableMaps.put("borrowreturnmaterial", createFieldNameMap(new String[] { "ReturnedDate",
                oldBorrowReturnMaterial_Date_FieldName }));
        tableMaps.put("borrowshipment", createFieldNameMap(new String[] { "BorrowShipmentID",
                "BorrowShipmentsID" }));
        tableMaps.put("collectingevent",
                createFieldNameMap(new String[] { "TaxonID", "TaxonNameID" }));
        tableMaps.put("collectionobjectcitation", createFieldNameMap(new String[] {
                "CollectionObjectID", "BiologicalObjectID" }));
        tableMaps.put("collector", createFieldNameMap(new String[] { "OrderNumber",
                oldCollectors_Order_FieldName, "CollectorID", "CollectorsID" }));
        tableMaps.put("deaccession", createFieldNameMap(new String[] { "DeaccessionDate",
                oldDeaccession_Date_FieldName }));
        tableMaps.put("deaccessionagent", createFieldNameMap(new String[] { "AgentID",
                "AgentAddressID", "DeaccessionAgentID", "DeaccessionAgentsID" }));
        tableMaps.put("deaccessionpreparation", createFieldNameMap(new String[] {
                "DeaccessionPreparationID", "DeaccessionCollectionObjectID", "PreparationID",
                "CollectionObjectID", }));
        // tableMaps.put("determination", createFieldNameMap(new String[] {"CollectionObjectID",
        // "BiologicalObjectID", "IsCurrent", "Current1", "DeterminationDate", "Date1", "TaxonID",
        // "TaxonNameID"}));
        tableMaps.put("groupperson", createFieldNameMap(new String[] { "GroupPersonID",
                "GroupPersonsID", "OrderNumber", oldGroupPersons_Order_FieldName }));
        tableMaps.put("loan", createFieldNameMap(new String[] { "IsGift", "Category", "IsClosed",
                "Closed" }));
        tableMaps.put("loanagent", createFieldNameMap(new String[] { "AgentID", "AgentAddressID",
                "LoanAgentID", "LoanAgentsID" }));
        tableMaps.put("loanpreparation", createFieldNameMap(new String[] { "PreparationID",
                "PhysicalObjectID" }));
        tableMaps.put("loanreturnpreparation", createFieldNameMap(new String[] {
                "DeaccessionPreparationID", "DeaccessionPhysicalObjectID", "LoanPreparationID",
                "LoanPhysicalObjectID", "LoanReturnPreparationID", "LoanReturnPhysicalObjectID",
                "ReturnedDate", oldLoanReturnPhysicalObj_Date_FieldName }));
        tableMaps.put("permit", createFieldNameMap(new String[] { "IssuedByID", "IssuerID",
                "IssuedToID", "IssueeID" }));
        tableMaps.put("projectcollectionobjects", createFieldNameMap(new String[] {
                "ProjectCollectionObjectID", "ProjectCollectionObjectsID" }));
        tableMaps.put("referencework", createFieldNameMap(new String[] { "WorkDate",
                oldRefWork_Date_FieldName, "IsPublished", "Published" }));
        tableMaps.put("stratigraphy", createFieldNameMap(new String[] { "LithoGroup",
                oldStratigraphy_Group_FieldName }));
        tableMaps.put("taxoncitation",
                createFieldNameMap(new String[] { "TaxonID", "TaxonNameID" }));

        // Turn back on when datamodel checked in
        tableMaps.put("collectionobjectattributes",
                createFieldNameMap(getCollectionObjectAttributeMappings()));
        tableMaps.put("preparationattributes", createFieldNameMap(getPrepAttributeMappings()));
        tableMaps.put("habitatattributes", createFieldNameMap(getHabitatAttributeMappings()));

        // Map<String, Map<String, String>> tableDateMaps = new Hashtable<String, Map<String,
        // String>>();
        // tableDateMaps.put("collectingevent", createFieldNameMap(new String[] {"TaxonID",
        // "TaxonNameID"}));
        // tableMaps.put("locality", createFieldNameMap(new String[] {"NationalParkName", "",
        // "ParentID", "TaxonParentID"}));

        BasicSQLUtils.clearValueMapper();
        BasicSQLUtils.addToValueMapper("CollectionMemberID", collectionMemberIDValueMapper);
        BasicSQLUtils.addToValueMapper("CreatedByAgentID",   agentCreatorValueMapper);
        BasicSQLUtils.addToValueMapper("ModifiedByAgentID",  agentModiferValueMapper);
        BasicSQLUtils.addToValueMapper("Version",            versionValueMapper);
        BasicSQLUtils.addToValueMapper("DivisionID",         divisionValueMapper);

        for (String tableName : tablesToMoveOver)
        {
            String fromTableName = tableName.toLowerCase();
            String toTableName = fromTableName;

            BasicSQLUtils.setOneToOneIDHash(null);

            int errorsToShow = BasicSQLUtils.SHOW_ALL;

            TableStats tblStats = new TableStats(oldDBConn, fromTableName, newDBConn, toTableName);
            tableStatHash.put(fromTableName, tblStats);
            log.info("Getting ready to copy table: " + fromTableName);
            // if (tableName.equals("LoanPhysicalObject"))
            // {
            // String[] ignoredFields = {"IsResolved"};
            // BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            //               
            // } else
            //               
            BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);

            if (tableName.equals("Accession") || tableName.equals("AccessionAuthorization"))
            {
                String[] ignoredFields = { "RepositoryAgreementID", "Version", "CreatedByAgentID",
                        "ModifiedByAgentID", "DateAcknowledged",
                        "AddressOfRecordID", "AppraisalID", "AccessionCondition",
                        "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);

            } else if (fromTableName.equals("accession"))
            {
                String[] ignoredFields = { "RepositoryAgreementID", "Version", "CreatedByAgentID",
                                           "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            } else if (fromTableName.equals("accessionagent"))
            {
                String[] ignoredFields = { "RepositoryAgreementID", "Version", "CreatedByAgentID",
                                           "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            } else if (fromTableName.equals("attachment"))
            {
                String[] ignoredFields = { "Visibility", "VisibilitySetBy", "Version",
                                           "CreatedByAgentID", "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            } else if (fromTableName.equals("biologicalobjectattributes"))
            {
                toTableName = "collectionobjectattributes";
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(getCollectionObjectAttributeToIgnore());
                
            } else if (fromTableName.equals("borrow"))
            {
                String[] ignoredFields = { "IsFinancialResonsibility", "AddressOfRecordID",
                                           "Version", "CreatedByAgentID", "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("borrowreturnmaterial"))
            {
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for
                                                                // ReturnedByID
            } else if (fromTableName.equals("collectionobject"))
            {
                // The First name is the name of the new column that is an ID of a One-to-One
                // releationship
                // NOTE: We have mapped as a Many-to-One for Hibernate, because we really want it to
                // be
                // a Zero-or-One
                BasicSQLUtils.setOneToOneIDHash(createFieldNameMap(new String[] {
                        "PreparationAttributesID", "PreparationAttributesID",
                        "CollectionObjectAttributesID", "CollectionObjectAttributesID" }));
            } else if (fromTableName.equals("collectingevent"))
            {
                String[] ignoredFields = { "Visibility", "VisibilitySetBy", "CollectingTripID",
                        "EndDateVerbatim", "EndDatePrecision", "StartDateVerbatim",
                        "StartDatePrecision", "HabitatAttributesID", "Version", "CreatedByAgentID",
                        "ModifiedByAgentID", "CollectionMemberID", "CollectingEventAttributesID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
                BasicSQLUtils.setOneToOneIDHash(createFieldNameMap(new String[] {
                        "HabitatAttributesID", "HabitatAttributesID" }));

                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for
                                                                // LocalityID
                errorsToShow &= ~BasicSQLUtils.SHOW_VAL_MAPPING_ERROR; // Turn off this error for
                                                                        // Habitat

            } else if (fromTableName.equals("deaccessioncollectionobject"))
            {
                toTableName = "deaccessionpreparation";
            } else if (fromTableName.equals("determination"))
            {
                String[] ignoredFields = { "DeterminationStatusID", "Version", "CreatedByAgentID",
                        "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            } else if (fromTableName.equals("habitat"))
            {
                toTableName = "habitatattributes";
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(getHabitatAttributeToIgnore());
            } else if (fromTableName.equals("loan"))
            {
                String[] ignoredFields = { "SpecialConditions", "AddressOfRecordID", 
                        "DateReceived", "ReceivedComments", "PurposeOfLoan", "OverdueNotiSetDate",
                        "FinancialResponsibility", "Version", "CreatedByAgentID",
                        "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            } else if (fromTableName.equals("loancollectionobject"))
            {
                toTableName = "loanreturnpreparation";
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for
                                                                // DeaccessionPhysicalObjectID
            } else if (fromTableName.equals("loanreturnphysicalobject"))
            {
                toTableName = "loanreturnpreparation";
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for
                                                                // DeaccessionPhysicalObjectID
            } else if (fromTableName.equals("otheridentifier"))
            {
                String[] ignoredFields = { "Institution", "Version", "CreatedByAgentID",
                        "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            } else if (fromTableName.equals("permit"))
            {
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for IssueeID,
                                                                // IssuerID
            } else if (fromTableName.equals("preparation"))
            {
                toTableName = "preparationattributes";
                BasicSQLUtils
                        .setFieldsToIgnoreWhenMappingNames(getPrepAttributeAttributeToIgnore());
                BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(new String[] { "MediumID" });
            } else if (fromTableName.equals("referencework"))
            {
                String[] ignoredFields = { "GUID", "Version", "CreatedByAgentID",
                        "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for
                                                                // ContainingReferenceWorkID
            } else if (fromTableName.equals("shipment"))
            {
                String[] ignoredFields = { "GUID", "Version", "CreatedByAgentID",
                        "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
                BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(new String[] { "ShipmentMethodID" });

                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for
                                                                // ShippedByID
            } else if (fromTableName.equals("stratigraphy"))
            {
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for
                                                                // GeologicTimePeriodID
            } else
            {
                String[] ignoredFields = { "GUID", "Version", "CreatedByAgentID",
                        "ModifiedByAgentID", "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            }

            if (fromTableName.equals("accessionagent")
                    || fromTableName.equals("accessionauthorization")
                    || fromTableName.equals("author") || fromTableName.equals("borrowshipment")
                    || fromTableName.equals("borrowagent") || fromTableName.equals("collector")
                    || fromTableName.equals("deaccessionagent")
                    || fromTableName.equals("groupperson") || fromTableName.equals("loanagent"))
            {
                fromTableName = fromTableName + "s";
            }

            if (!BasicSQLUtils.hasIgnoreFields())
            {
                String[] ignoredFields = { "Version", "CreatedByAgentID", "ModifiedByAgentID",
                        "CollectionMemberID" };
                BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            }

            deleteAllRecordsFromTable(toTableName, BasicSQLUtils.myDestinationServerType);

            BasicSQLUtils.setShowErrors(errorsToShow);
            BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, toTableName,
                    BasicSQLUtils.myDestinationServerType);
            if (!copyTable(oldDBConn, newDBConn, fromTableName, toTableName, tableMaps
                    .get(toTableName), null, BasicSQLUtils.mySourceServerType,
                    BasicSQLUtils.myDestinationServerType))
            {
                log.error("Table [" + tableName + "] didn't copy correctly.");
                break;

            }
            BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, toTableName,
                    BasicSQLUtils.myDestinationServerType);
            BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(null);
            tblStats.collectStats();
        }
        BasicSQLUtils.setShowErrors(BasicSQLUtils.SHOW_ALL);
    }

    /**
     * @return
     */
    protected String[] getCollectionObjectAttributeToIgnore()
    {
        return new String[] { "BiologicalObjectTypeId", "BiologicalObjectAttributesID", "SexId",
                "StageId", "Text8", "Number34", "Number35", "Number36", "Text8", "Version",
                "CreatedByAgentID", "ModifiedByAgentID", "CollectionMemberID", "Text10", "Text11",
                "Text12", "Text13", "Text14", };
    }

    protected String[] getCollectionObjectAttributeMappings()
    {
        String oldBiologicalObjectAttribute_Condition_FieldName = "Condition";
        if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
        {
            oldBiologicalObjectAttribute_Condition_FieldName = "Condition1";// TODO change when get
                                                                            // new MySQL dumps, Meg
        }

        return new String[] {
                "CollectionObjectAttributesID",
                "BiologicalObjectAttributesID",
                // "biologicalObjectTypeId", ??? "Number36"
                "Number10", "Sex", "Number11", "Age", "Number12", "Stage", "Number37", "Weight",
                "Number38", "Length", "Number8", "GosnerStage", "Number9", "SnoutVentLength",
                "Text8", "Sctivity", "Number10", "LengthTail", "Text13", "ReproductiveCondition",
                "Text14", "ObjCondition", "Number11", "LengthTarsus", "Number12", "LengthWing",
                "Number13", "LengthHead", "Number14", "LengthBody", "Number15", "LengthMiddleToe",
                "Number16", "LengthBill", "Number17", "TotalExposedCulmen", "Number39",
                "MaxLength", "Number40", "MinLength", "Number18", "LengthHindFoot", "Number19",
                "LengthForeArm", "Number20", "LengthTragus", "Number21", "LengthEar", "Number22",
                "EarFromNotch", "Number23", "Wingspan", "Number24", "LengthGonad", "Number25",
                "WidthGonad", "Number26", "LengthHeadBody", "Number41", "Width", "Number27",
                "HeightFinalWhorl", "Number28", "InsideHeightAperture", "Number29",
                "InsideWidthAperture", "Number30", "NumberWhorls", "Number31", "OuterLipThickness",
                "Number32", "Mantle", "Number42", "Height", "Number33", "Diameter", "Text9",
                "BranchingAt",
        // "Text1",
        // "Text2",
        // "Text3",
        // "Text4",
        // "Text5",
        // "remarks",
        // "sexId", "Number34", ?????
        // "stageId", "Number35", ????
        // "yesNo1",
        // "yesNo2",
        // "yesNo3",
        // "Number1",
        // "Number2",
        // "Number3",
        // "Number4",
        // "Number5",
        // "Number6",
        // "Number7",
        // "Text6",
        // "Text7",
        // "yesNo4",
        // "yesNo5",
        // "yesNo6",
        // "yesNo7",
        };
    }

    protected String[] getPrepAttributeAttributeToIgnore()
    {
        return new String[] { "MediumId", "PreparationTypeId", "ContainerTypeId", "Number3",
                "Number8",
                "Text16", // ??? JUST FOR NOW!
                "Version", "CreatedByAgentID", "ModifiedByAgentID", "CollectionMemberID", "Text22",
                "Text23", "Text24", "Text25", "Text26", "YesNo3", "YesNo4",

        };
    }

    protected String[] getPrepAttributeMappings()
    {
        return new String[] { "PreparationAttributesID", "PreparationID", "AttrDate",
                "PreparedDate", "Number3", "MediumID", "Text3", "PartInformation", "Text4",
                "StartBoxNumber", "Text5", "EndBoxNumber", "Text6", "StartSlideNumber", "Text7",
                "EndSlideNumber", "Text8", "SectionOrientation", "Text9", "SectionWidth", "Text26",
                "size", "Text10", "URL", "Text11", "Identifier", "Text12", "NestLining", "Text13",
                "NestMaterial", "Text14", "NestLocation", "Text15", "SetMark", "Number4",
                "CollectedEggCount", "Number5", "CollectedParasiteEggCount", "Number6",
                "FieldEggCount", "Number7", "FieldParasiteEggCount", "Text17",
                "EggIncubationStage", "Text18", "EggDescription", "Text19", "Format", "Text25",
                "storageInfo", "Text22", "preparationType",
                // "preparationTypeId", "Number8", ?????
                "Text23", "containerType", "Text24", "medium",
                // "containerTypeId",????????
                "Text20", "DNAConcentration", "Text21", "Volume",
                // "Text1",
                // "Text2",
                // "Number1",
                // "Number2",
                // "remarks",
                "Number9", "NestCollected",
        // "yesNo1",
        // "yesNo2",
        };
    }

    protected String[] getHabitatAttributeToIgnore()
    {
        return new String[] { "HabitatTypeId", };
    }

    protected String[] getHabitatAttributeMappings()
    {
        String oldHabitatAttribute_Current_FieldName = "Current";
        if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
        {
            oldHabitatAttribute_Current_FieldName = "IsCurrent";// TODO change when get new MySQL
                                                                // dumps, Meg
        }
        return new String[] { "HabitatAttributesID", "HabitatID", "Number9", "airTempC",
                "Number10", "waterTempC", "Number11", "WaterpH", "Text12", "turbidity", "Text16",
                "clarity", "Text14", "salinity", "Text6", "SoilType", "Number6", "SoilPh",
                "Number7", "SoilTempC", "Text7", "SoilMoisture", "Text15", "slope", "Text13",
                "vegetation", "Text17", "habitatType", "Text8",
                oldHabitatAttribute_Current_FieldName, "Text9", "Substrate", "Text10",
                "SubstrateMoisture", "Number8", "HeightAboveGround", "Text11", "NearestNeighbor",
                // "remarks",
                "Number13", "minDepth", "Number12", "maxDepth",
        // "text1",
        // "text2",
        // "number1",
        // "number2",
        // "habitatTypeId", ???????
        // "yesNo1",
        // "yesNo2",
        // "number3",
        // "number4",
        // "number5",
        // "text3",
        // "text4",
        // "text5",
        // "yesNo3",
        // "yesNo4",
        // "yesNo5",
        };
    }

    /**
     * @param createdBy
     * @return
     */
    protected int getCreatorAgentId(final String createdByName)
    {
        return creatorAgent == null ? -1 : creatorAgent.getAgentId();
    }

    /**
     * @param modifierAgent
     * @return
     */
    protected int getModifiedByAgentId(final String modifierAgentName)
    {
        return modifierAgent == null ? -2 : modifierAgent.getAgentId();
    }

    protected int getCollectionMemberId()
    {
        return 1;
    }

    protected int getDisciplineId()
    {
        return disciplineId;
    }

    /**
     * Checks to see if any of the names in the array are in passed in name
     * @param referenceNames array of reference names
     * @param name the name to be figured out
     * @return true if there is a match
     */
    protected boolean checkName(String[] referenceNames, final String name)
    {
        for (String rn : referenceNames)
        {
            if (name.toLowerCase().indexOf(rn.toLowerCase()) > -1) { return true; }
        }
        return false;
    }

    /**
     * Convert a CollectionObjectTypeName to a DataType
     * @param collectionObjTypeName the name
     * @return the Standard DataType
     */
    public String getStandardDataTypeName(final String collectionObjTypeName)
    {
        if (checkName(new String[] { "Plant", "Herb" }, collectionObjTypeName)) { return "Plant"; }

        if (checkName(new String[] { "Fish", "Bird", "Frog", "Insect", "Fossil", "Icth", "Orn",
                "Herp", "Entom", "Paleo", "Mammal", "Invertebrate", "Animal" },
                collectionObjTypeName)) { return "Animal"; }

        if (checkName(new String[] { "Mineral", "Rock" }, collectionObjTypeName)) { return "Mineral"; }

        if (checkName(new String[] { "Anthro" }, collectionObjTypeName)) { return "Anthropology"; }

        if (checkName(new String[] { "Fungi" }, collectionObjTypeName)) { return "Fungi"; }
        log.error("****** Unable to Map [" + collectionObjTypeName + "] to a standard DataType.");

        return null;
    }

    /**
     * Convert a CollectionObjectTypeName to a DataType
     * @param name the name
     * @return the Standard DataType
     */
    public String getStandardDisciplineName(final String name)
    {
        DisciplineType disciplineType = DisciplineType.getDiscipline(name.toLowerCase());
        if (disciplineType != null) { return disciplineType.getName(); }

        if (checkName(new String[] { "Plant", "Herb" }, name)) { return "plant"; }
        // TO DO: Rod needs to look at this at make sure that things are geting mapped properly
        // for disciplineType types, disciplines, and datatypes. Meg ran into problems when
        // converting the
        // uvgherps database, we decided to make note of the issue and address later. For the time
        // being
        // I added "Herps" to this listing so that the converter would pass without failing.
        if (checkName(new String[] { "FishHerps", "Herps" }, name)) { return "animal"; }

        if (checkName(new String[] { "Mineral", "Rock" }, name)) { return "mineral"; }

        if (checkName(new String[] { "Anthro" }, name)) { return "anthropology"; }

        if (checkName(new String[] { "Fungi" }, name)) { return "fungi"; }
        log.error("****** Unable to Map Name[" + name + "] to a DisciplineType type.");

        return null;
    }

    /**
     * Create the two default records that should be in the new 'determinationstatus' table. The
     * first record is an 'current' record. The second record is a 'unknown' record which
     * corresponds to the old isCurrent=true records.
     */
    public void createDefaultDeterminationStatusRecords()
    {
        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "determinationstatus", BasicSQLUtils.myDestinationServerType);

        StringBuilder insert = new StringBuilder();
        StringBuilder insert2 = new StringBuilder();
        StringBuilder insert3 = new StringBuilder();
        try
        {
            BasicSQLUtils.deleteAllRecordsFromTable("determinationstatus", BasicSQLUtils.myDestinationServerType);

            // setup the insert statement
            insert.append("INSERT INTO determinationstatus ");
            // Meg had to drop explicit insert of CURRENT_DATE, not suported by SQL Server
            // insert.append("(DeterminationStatusID,Name,Remarks,TimestampCreated,TimestampModified,
            // IsCurrent) ");
            insert.append("(DeterminationStatusID, Name, Remarks, TimestampCreated, TimestampModified, Type, CreatedByAgentID, ModifiedByAgentID, DisciplineID) ");
            insert.append("VALUES ");
            // followed by the 'current status' record
            insert.append("(");
            insert.append(D_STATUS_CURRENT);
            // Meg had to drop explicit insert of CURRENT_DATE, not suported by SQL Server
            // Meg had to drop explicit insert of true, not suported by SQL Server
            // insert.append(",'Current','mirror of the old schema isCurrent
            // field',CURRENT_DATE,CURRENT_DATE,true)");
            insert.append(",'Current','mirror of the old schema isCurrent field', '" + nowStr
                    + "','" + nowStr + "', "+DeterminationStatus.CURRENT+"," + getCreatorAgentId(null) + ","
                    + getModifiedByAgentId(null) + ","
                    + getDisciplineId() + ")");

            // Meg had to split single insert statement into two.
            insert2.append("INSERT INTO determinationstatus ");
            // Meg had to drop explicit insert of CURRENT_DATE, not suported by SQL Server
            // insert.append("(DeterminationStatusID,Name,Remarks,TimestampCreated,TimestampModified,
            // IsCurrent) ");
            insert2.append("(DeterminationStatusID, Name, Remarks, TimestampCreated, TimestampModified, Type, CreatedByAgentID, ModifiedByAgentID, DisciplineID) ");
            insert2.append("values ");
            // the 'unknown status' record
            insert2.append("(");
            insert2.append(D_STATUS_UNKNOWN);
            // Meg had to drop explicit insert of CURRENT_DATE, not suported by SQL Server
            // insert.append(",'Unknown','',CURRENT_DATE,CURRENT_DATE, false)");
            // Meg had to drop explicit insert of false, not suported by SQL Server
            insert2.append(",'Unknown','', '" + nowStr + "','" + nowStr + "',"+DeterminationStatus.NOTCURRENT+","
                    + getCreatorAgentId(null) + "," + getModifiedByAgentId(null) + ","
                    + getDisciplineId() + ")");

            insert2.append("INSERT INTO determinationstatus ");
            insert2.append("(DeterminationStatusID, Name, Remarks, TimestampCreated, TimestampModified, Type, CreatedByAgentID, ModifiedByAgentID, DisciplineID) ");
            insert2.append("values ");
            insert2.append("(");
            insert2.append(D_STATUS_OLD);
            insert2.append(",'Old Determination','', '" + nowStr + "','" + nowStr + "',"+DeterminationStatus.OLDDETERMINATION+","
                    + getCreatorAgentId(null) + "," + getModifiedByAgentId(null) + ","
                    + getDisciplineId() + ")");


            Statement st = newDBConn.createStatement();
            
            log.debug(insert.toString());
            st.executeUpdate(insert.toString());
            
            log.debug(insert2.toString());
            st.executeUpdate(insert2.toString());
            
            log.debug(insert3.toString());
            st.executeUpdate(insert3.toString());
            
            st.close();

        } catch (SQLException e)
        {
            log.error(insert.toString());
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }
        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "determinationstatus", BasicSQLUtils.myDestinationServerType);

    }

    /**
     * Create a data type.
     * @param taxonomyTypeName the name
     * @return the ID (record id) of the data type
     */
    public int createDataType(final String taxonomyTypeName)
    {
        int dataTypeId = -1;
        String dataTypeName = getStandardDataTypeName(taxonomyTypeName);
        if (dataTypeName == null) { return dataTypeId; }

        try
        {
            if (dataTypeNameToIds.get(dataTypeName) == null)
            {
                /*
                 * describe datatype;
                 * +------------+-------------+------+-----+---------+----------------+ | Field |
                 * Type | Null | Key | Default | Extra |
                 * +------------+-------------+------+-----+---------+----------------+ | DataTypeID |
                 * int(11) | NO | PRI | | auto_increment | | Name | varchar(50) | YES | | | |
                 * +------------+-------------+------+-----+---------+----------------+
                 */

                Statement updateStatement = newDBConn.createStatement();
                // String updateStr = "INSERT INTO datatype (DataTypeID, TimestampCreated,
                // TimestampModified, LastEditedBy, Name) VALUES
                // (null,'"+nowStr+"','"+nowStr+"',NULL,'"+dataTypeName+"')";
                // Meg removed explicit insert of null value into DataTypeID, it was failing on SQL
                // Server
                // String updateStr = "INSERT INTO datatype ( TimestampCreated, TimestampModified,
                // Name, CreatedByAgentID, ModifiedByAgentID) VALUES
                // ('"+nowStr+"','"+nowStr+"','"+dataTypeName+"',"+getCreatorAgentId(null)+","+getModifiedByAgentId(null)+")";
                String updateStr = "INSERT INTO datatype ( TimestampCreated, TimestampModified, Name, Version, CreatedByAgentID, ModifiedByAgentID) "
                        + "VALUES ('"
                        + nowStr
                        + "','"
                        + nowStr
                        + "','"
                        + dataTypeName
                        + "', 0, "
                        + getCreatorAgentId(null) + "," + getModifiedByAgentId(null) + ")";
                updateStatement.executeUpdate(updateStr);
                updateStatement.clearBatch();
                updateStatement.close();
                updateStatement = null;

                dataTypeId = BasicSQLUtils.getHighestId(newDBConn, "DataTypeID", "datatype");
                log.info("Created new datatype[" + dataTypeName + "]");

                dataTypeNameToIds.put(dataTypeName, dataTypeId);

            } else
            {
                dataTypeId = dataTypeNameToIds.get(dataTypeName);
                log.info("Reusing new datatype[" + dataTypeName + "]");
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }
        return dataTypeId;
    }

    /**
     * 
     */
    public void doLocalizeSchema()
    {

        Session localSession = HibernateUtil.getNewSession();
        Discipline discipline = null;
        Transaction trans = localSession.beginTransaction();

        try
        {
            Criteria criteria = localSession.createCriteria(Discipline.class);
            List<?> disciplineeList = criteria.list();
            discipline = (Discipline)disciplineeList.iterator().next();

            BuildSampleDatabase.loadSchemaLocalization(discipline, SpLocaleContainer.CORE_SCHEMA,
                    DBTableIdMgr.getInstance());
            localSession.save(discipline);
            trans.commit();

            DBTableIdMgr schema = new DBTableIdMgr(false);
            schema.initialize(new File(XMLHelper
                    .getConfigDirPath("specify_workbench_datamodel.xml")));
            BuildSampleDatabase.loadSchemaLocalization(discipline,
                    SpLocaleContainer.WORKBENCH_SCHEMA, schema);

            trans = localSession.beginTransaction();
            localSession.save(discipline);
            trans.commit();

        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.err.println(ex);

            trans.rollback();

        } finally
        {
            localSession.close();
        }
    }

    /**
     * 
     */
    public void convertDivision()
    {
        DisciplineType disciplineType = DisciplineType.getDiscipline("fish");

        Session cacheSession = DataBuilder.getSession();
        DataBuilder.setSession(null);

        Session     localSession = HibernateUtil.getNewSession();
        Transaction trans        = localSession.beginTransaction();
        Institution institution  = createInstitution("Natural History Museum");
        division = createDivision(institution, disciplineType.getName(), "Icthyology", "IT", "Icthyology");
        localSession.persist(institution);
        localSession.persist(division);
        // persist(discipline);
        trans.commit();
        localSession.close();

        DataBuilder.setSession(cacheSession);
        // return true;
    }
    
    public void persist(Object o)
    {
        if (session != null)
        {
            session.saveOrUpdate(o);
        }
    }

    public void setSession(Session s)
    {
        session = s;
    }

    public void startTx()
    {
        HibernateUtil.beginTransaction();
    }

    public void commitTx()
    {
        HibernateUtil.commitTransaction();
    }

    /**
     * Converts Object Defs.
     * @param specifyUserId
     * @return true on success, false on failure
     */
    public boolean convertCollectionObjectDefs(final int specifyUserId)
    {
        try
        {
            // The Old Table catalogseriesdefinition is being converted to Discipline
            IdMapperIFace taxonomyTypeMapper = idMapperMgr.get("TaxonomyType", "TaxonomyTypeID");

            // Create a Hashtable to track which IDs have been handled during the conversion process
            BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "datatype",       BasicSQLUtils.myDestinationServerType);
            BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "discipline",     BasicSQLUtils.myDestinationServerType);
            BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "collection",     BasicSQLUtils.myDestinationServerType);
            // BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "collection_colobjdef");

            Hashtable<Integer, Integer> newColObjIDTotaxonomyTypeID = new Hashtable<Integer, Integer>();
            Hashtable<Integer, String> taxonomyTypeIDToTaxonomyName = new Hashtable<Integer, String>();

            // First, create a Discipline for TaxonomyType record
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            log.info(taxonomyTypeMapper.getSql());
            ResultSet rs = stmt.executeQuery(taxonomyTypeMapper.getSql());
            int recordCnt = 0;
            while (rs.next())
            {
                int taxonomyTypeID = rs.getInt(1);
                String taxonomyTypeName = rs.getString(2);
                String lastEditedBy = null;

                String disciplineName = getStandardDisciplineName(taxonomyTypeName);
                if (disciplineName == null)
                {
                    log.error("**** Had to Skip record because taxonomyTypeName couldn't be found in our DisciplineType lookup in SpecifyAppContextMgr["+ taxonomyTypeName + "]");
                    continue;
                }

                DisciplineType disciplineType = DisciplineType.getDiscipline(disciplineName);
                if (disciplineType == null)
                {
                    log.error("**** disciplineType couldn't be found in our DisciplineType lookup in SpecifyAppContextMgr["+ disciplineName + "]");
                    continue;
                }
                log.info("Creating a new Discipline for taxonomyTypeName[" + taxonomyTypeName
                        + "] disciplineType[" + disciplineName + "]");

                taxonomyTypeName = disciplineName;

                // Figure out what type of standard adat type this is from the
                // CollectionObjectTypeName
                BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "datatype", BasicSQLUtils.myDestinationServerType);

                int dataTypeId = createDataType(taxonomyTypeName);
                if (dataTypeId == -1)
                {
                    log.error("**** Had to Skip record because of DataType mapping error[" + taxonomyTypeName + "]");
                    continue;
                }

                taxonomyTypeIDToTaxonomyName.put(taxonomyTypeID, taxonomyTypeName);

                /*
                 * Discipline
                 * +-----------------------------+-------------+------+-----+---------+----------------+ |
                 * Field | Type | Null | Key | Default | Extra |
                 * +-----------------------------+-------------+------+-----+---------+----------------+ |
                 * DisciplineID | bigint(20) | NO | PRI | NULL | auto_increment | |
                 * TimestampCreated | datetime | NO | | | | | TimestampModified | datetime | NO | | | | |
                 * LastEditedBy | varchar(50) | YES | | NULL | | | Name | varchar(64) | YES | | NULL | | |
                 * DisciplineType | varchar(64) | YES | | NULL | | | GeologicTimePeriodTreeDefID |
                 * bigint(20) | NO | UNI | | | | TaxonTreeDefID | bigint(20) | NO | MUL | | | |
                 * SpecifyUserID | bigint(20) | NO | MUL | | | | StorageTreeDefID | bigint(20) | NO |
                 * MUL | | | | GeographyTreeDefID | bigint(20) | NO | MUL | | | | DataTypeID |
                 * DivisionID | bigint(20) | NO | MUL | | |
                 * +-----------------------------+-------------+------+-----+---------+----------------+
                 */

                // use the old CollectionObjectTypeName as the new Discipline name
                BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "discipline", BasicSQLUtils.myDestinationServerType);
                Statement     updateStatement = newDBConn.createStatement();
                StringBuilder strBuf2         = new StringBuilder();
                // adding DivisioniID
                strBuf2.append("INSERT INTO discipline (DisciplineID, TimestampModified, DisciplineType, Name, TimestampCreated, ");
                strBuf2.append("DataTypeID, GeographyTreeDefID, GeologicTimePeriodTreeDefID, StorageTreeDefID, TaxonTreeDefID, DivisionID, ");
                strBuf2.append("CreatedByAgentID, ModifiedByAgentID, Version) VALUES (");
                strBuf2.append(taxonomyTypeMapper.get(taxonomyTypeID) + ",");
                strBuf2.append("'" + dateFormatter.format(now) + "',"); // TimestampModified
                strBuf2.append("'" + disciplineType.getName() + "',");
                strBuf2.append("'" + disciplineType.getTitle() + "',");
                strBuf2.append("'" + dateFormatter.format(now) + "',"); // TimestampCreated
                strBuf2.append(dataTypeId + ",");
                strBuf2.append("1,"); // GeographyTreeDefID
                strBuf2.append("1,"); // GeologicTimePeriodTreeDefID
                strBuf2.append("1,"); // StorageTreeDefID
                strBuf2.append("1,");// TaxonTreeDefID
                strBuf2.append(division.getDivisionId() + ","); // DivisionID
                strBuf2.append(getCreatorAgentId(null) + "," + getModifiedByAgentId(lastEditedBy) + ",0");
                strBuf2.append(")");
                // strBuf2.append("NULL)");// UserPermissionID//User/Security changes
                log.info(strBuf2.toString());

                updateStatement.executeUpdate(strBuf2.toString());
                updateStatement.clearBatch();
                updateStatement.close();
                updateStatement = null;
                recordCnt++;
                BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "discipline", BasicSQLUtils.myDestinationServerType);
                Integer disciplineID = BasicSQLUtils.getHighestId(newDBConn, "DisciplineID", "discipline");

                newColObjIDTotaxonomyTypeID.put(disciplineID, taxonomyTypeID);

                log.info("Created new discipline[" + taxonomyTypeName + "] is dataType ["  + dataTypeId + "]");
            }

            rs.close();
            stmt.close();
            log.info("Discipline Records: " + recordCnt);

            if (taxonomyTypeMapper.size() > 0)
            {
                // Now convert over all Collection

                String sql = "SELECT catalogseries.CatalogSeriesID, catalogseries.SeriesName, taxonomytype.TaxonomyTypeName, "
                        + "catalogseries.CatalogSeriesPrefix, catalogseries.Remarks, catalogseries.LastEditedBy, catalogseriesdefinition.CatalogSeriesDefinitionID, "
                        + "taxonomytype.TaxonomyTypeID From catalogseries Inner Join catalogseriesdefinition ON "
                        + "catalogseries.CatalogSeriesID = catalogseriesdefinition.CatalogSeriesID Inner Join collectiontaxonomytypes ON "
                        + "catalogseriesdefinition.ObjectTypeID = collectiontaxonomytypes.BiologicalObjectTypeID Inner Join taxonomytype ON "
                        + "collectiontaxonomytypes.TaxonomyTypeID = taxonomytype.TaxonomyTypeID order by catalogseries.CatalogSeriesID";
                log.info(sql);

                stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery(sql.toString());

                Hashtable<String, Boolean> seriesNameHash = new Hashtable<String, Boolean>();

                recordCnt = 0;
                while (rs.next())
                {
                    int catalogSeriesID = rs.getInt(1);
                    String seriesName   = rs.getString(2);
                    String taxTypeName  = rs.getString(3);
                    String prefix       = rs.getString(4);
                    String remarks      = rs.getString(5);
                    String lastEditedBy = rs.getString(6);
                    // int catSeriesDefID = rs.getInt(7);
                    int taxonomyTypeID = rs.getInt(8);

                    String newSeriesName = seriesName + " " + taxTypeName;
                    if (seriesNameHash.get(newSeriesName) != null)
                    {
                        log.error("Rebuilt Collection Name [" + newSeriesName + "] is not unique.");
                    } else
                    {
                        seriesNameHash.put(seriesName, true);
                    }

                    Session localSession = HibernateUtil.getNewSession();
                    CatalogNumberingScheme cns = new CatalogNumberingScheme();
                    cns.initialize();
                    cns.setIsNumericOnly(true);
                    cns.setSchemeClassName("");
                    cns.setSchemeName(newSeriesName);
                    Transaction trans = localSession.beginTransaction();
                    localSession.save(cns);
                    trans.commit();

                    Integer catNumSchemeId = cns.getCatalogNumberingSchemeId();
                    // catNumSchemeHash.put(hashKey, catNumSchemeId);
                    // localSession.close();

                    // Now craete the proper record in the Join Table

                    int newColObjdefID = taxonomyTypeMapper.get(taxonomyTypeID);

                    Statement updateStatement = newDBConn.createStatement();
                    strBuf.setLength(0);
                    strBuf.append("INSERT INTO collection (DisciplineID, CollectionName, CollectionPrefix, Remarks, CatalogNumberingSchemeID, ");
                    strBuf.append("TimestampCreated, TimestampModified, CreatedByAgentID, ModifiedByAgentID, Version) VALUES (");
                    strBuf.append(newColObjdefID + ",");
                    strBuf.append(getStrValue(newSeriesName) + ",");
                    strBuf.append(getStrValue(prefix) + ",");
                    strBuf.append(getStrValue(remarks) + ",");
                    strBuf.append(catNumSchemeId.toString() + ",");
                    strBuf.append("'" + dateFormatter.format(now) + "',"); // TimestampModified
                    strBuf.append("'" + dateFormatter.format(now) + "',"); // TimestampCreated
                    strBuf.append(getCreatorAgentId(null) + ","
                            + getModifiedByAgentId(lastEditedBy) + ", 0");
                    strBuf.append(")");

                    System.out.println(strBuf.toString());

                    updateStatement.executeUpdate(strBuf.toString());
                    updateStatement.clearBatch();
                    updateStatement.close();
                    updateStatement = null;

                    String hashKey = catalogSeriesID + "_" + taxonomyTypeID;

                    Integer newCatSeriesID = BasicSQLUtils.getHighestId(newDBConn, "CollectionID", "collection");
                    collectionHash.put(hashKey, newCatSeriesID);
                    if (StringUtils.isNotEmpty(prefix))
                    {
                        prefixHash.put(hashKey, prefix);
                    }

                    log.info("Collection New[" + newCatSeriesID + "] [" + seriesName + "] ["
                            + prefix + "] [" + newColObjdefID + "]");

                    recordCnt++;

                } // while

                log.info("Collection Join Records: " + recordCnt);
                rs.close();
                stmt.close();
            } else
            {
                log.warn("taxonomyTypeMapper is empty.");
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }
        
        return false;
    }

    /**
     * Looks up all the current Permit Types and uses them, instead of the usystable
     */
    @SuppressWarnings("unchecked")
    public void createPermitTypePickList()
    {
        /*
         * try { Statement stmt = oldDBConn.createStatement(); String sqlStr = "select count(Type)
         * from (select distinct Type from permit where Type is not null) as t";
         * 
         * log.info(sqlStr);
         * 
         * boolean useField = false; ResultSet rs = stmt.executeQuery(sqlStr); } catch (SQLException
         * e) { e.printStackTrace(); log.error(e); }
         */

        Session localSession = HibernateUtil.getCurrentSession();
        PickList pl = new PickList();
        pl.initialize();
        Set<PickListItemIFace> items = pl.getItems();

        try
        {
            pl.setName("Permit");
            pl.setSizeLimit(-1);

            HibernateUtil.beginTransaction();
            localSession.saveOrUpdate(pl);
            HibernateUtil.commitTransaction();

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException("Couldn't create PickList for [Permit]");
        }

        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            String sqlStr = "select distinct Type from permit where Type is not null";

            log.info(sqlStr);

            ResultSet rs = stmt.executeQuery(sqlStr);

            // check for no records which is OK
            if (!rs.first()) { return; }

            int count = 0;
            do
            {
                String typeStr = rs.getString(1);
                if (typeStr != null)
                {
                    log.info("Permit Type[" + typeStr + "]");
                    PickListItem pli = new PickListItem();
                    pli.initialize();
                    pli.setTitle(typeStr);
                    pli.setValue(typeStr);
                    pli.setTimestampCreated(now);
                    items.add(pli);
                    pli.setPickList(pl);
                    count++;

                }
            } while (rs.next());

            log.info("Processed Permit Types " + count + " records.");

            HibernateUtil.beginTransaction();

            localSession.saveOrUpdate(pl);

            HibernateUtil.commitTransaction();

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }

    }

    /**
     * Converts an old USYS table to a PickList.
     * @param usysTableName old table name
     * @param pickListName new pciklist name
     * @return true on success, false on failure
     */
    @SuppressWarnings("unchecked")
    public boolean convertUSYSToPicklist(final String usysTableName, final String pickListName)
    {
        List<BasicSQLUtils.FieldMetaData> fieldMetaData = new ArrayList<BasicSQLUtils.FieldMetaData>();
        getFieldMetaDataFromSchema(oldDBConn, usysTableName, fieldMetaData,
                BasicSQLUtils.mySourceServerType);

        int ifaceInx = -1;
        int dataInx = -1;
        int fieldSetInx = -1;
        int i = 0;
        for (BasicSQLUtils.FieldMetaData md : fieldMetaData)
        {
            if (ifaceInx == -1 && md.getName().equals("InterfaceID"))
            {
                ifaceInx = i + 1;

            } else if (fieldSetInx == -1 && md.getName().equals("FieldSetSubTypeID"))
            {
                fieldSetInx = i + 1;

            } else if (dataInx == -1 && md.getType().toLowerCase().indexOf("varchar") > -1)
            {
                dataInx = i + 1;
            }
            i++;
        }

        if (ifaceInx == -1 || dataInx == -1 || fieldSetInx == -1) { throw new RuntimeException(
                "Couldn't decypher USYS table ifaceInx[" + ifaceInx + "] dataInx[" + dataInx
                        + "] fieldSetInx[" + fieldSetInx + "]"); }

        Session localSession = HibernateUtil.getCurrentSession();
        PickList pl = new PickList();
        pl.initialize();

        try
        {
            pl.setName(pickListName);
            pl.setReadOnly(false);
            pl.setSizeLimit(-1);

            HibernateUtil.beginTransaction();
            localSession.saveOrUpdate(pl);
            HibernateUtil.commitTransaction();

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException("Couldn't create PickList for [" + usysTableName + "]");
        }

        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            String sqlStr = "select * from " + usysTableName + " where InterfaceID is not null";

            log.info(sqlStr);

            boolean useField = false;
            ResultSet rs = stmt.executeQuery(sqlStr);

            // check for no records which is OK
            if (!rs.first()) { return true; }

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

            log.info("Using FieldSetSubTypeID " + useField);
            rs.first();
            int count = 0;
            do
            {
                if (!useField || rs.getObject(fieldSetInx) != null)
                {
                    String val = rs.getString(dataInx);
                    String lowerStr = val.toLowerCase();
                    if (values.get(lowerStr) == null)
                    {
                        log.info("[" + val + "]");
                        pl.addItem(val, val);
                        values.put(lowerStr, val);
                        count++;
                    } else
                    {
                        log.info("Discarding duplicate picklist value[" + val + "]");
                    }
                }
            } while (rs.next());

            log.info("Processed " + usysTableName + "  " + count + " records.");

            HibernateUtil.beginTransaction();

            localSession.saveOrUpdate(pl);

            HibernateUtil.commitTransaction();

            return true;

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     */
    public void createPickListsFromXML(final String discipline)
    {
        Session cachedSession = DataBuilder.getSession();
        DataBuilder.setSession(null);

        HibernateDataProviderSession localSession = new HibernateDataProviderSession();
        try
        {

            List<BldrPickList> pickLists = DataBuilder.getBldrPickLists(discipline != null ? discipline : "common");
            // DataBuilder.buildPickListFromXML(pickLists);

            localSession.beginTransaction();

            for (BldrPickList pl : pickLists)
            {

                PickList dbPL = localSession.getData(PickList.class, "name", pl.getName(),
                        DataProviderSessionIFace.CompareType.Equals);
                if (dbPL == null)
                {
                    PickList pickList = createPickList(pl.getName(), pl.getType(), pl
                            .getTableName(), pl.getFieldName(), pl.getFormatter(),
                            pl.getReadOnly(), pl.getSizeLimit(), pl.getIsSystem());
                    for (BldrPickListItem item : pl.getItems())
                    {
                        pickList.addItem(item.getTitle(), item.getValue());
                    }
                    localSession.saveOrUpdate(pickList);
                } else
                {
                    log.debug("Skipping PickList [" + pl.getName() + "] it is already in use.");
                }
            }
            localSession.commit();

        } catch (Exception ex)
        {
            ex.printStackTrace();

        } finally
        {
            localSession.close();
            DataBuilder.setSession(cachedSession);
        }
    }

    /**
     * Converts all the USYS tables to PickLists.
     * @return true on success, false on failure
     */
    public boolean convertUSYSTables()
    {
        log.info("Creating picklists");

        int tableType = PickListDBAdapterIFace.Type.Table.ordinal();
        // int tableFieldType = PickListDBAdapterIFace.Type.TableField.ordinal();

        // Name Type Table Name Field Formatter R/O Size IsSys
        createPickList("DeterminationStatus", tableType, "determinationstatus", null,
                "DeterminationStatus", true, -1, true);
        createPickList("DataType", tableType, "datatype", null, "DataType", true, -1, true);

        log.info("Converting USYS Tables.");

        BasicSQLUtils.deleteAllRecordsFromTable("picklist", BasicSQLUtils.myDestinationServerType);
        BasicSQLUtils.deleteAllRecordsFromTable("picklistitem",
                BasicSQLUtils.myDestinationServerType);

        String[] tables = { "usysaccessionstatus", "AccessionStatus", "usysaccessiontype",
                "AccessionType", "usysborrowagenrole", "BorrowAgentRole", "usysaccessionarole",
                "AccessionRole", "usysdeaccessiorole", "DeaccessionaRole", "usysloanagentsrole",
                "LoanAgentRole", "usysbiologicalsex", "BiologicalSex", "usysbiologicalstage",
                "BiologicalStage", "usyscollectingmethod", "CollectingMethod",
                "usyscollobjprepmeth", "CollObjPrepMeth", "usysdeaccessiotype", "DeaccessionType",
                "usysdeterminatconfidence", "DeterminationConfidence", "usysdeterminatmethod",
                "DeterminationMethod", "usysdeterminattypestatusname", "DeterminationTypeStatus",
                "usyshabitathabitattype", "HabitatTtype", "usyslocalityelevationmethod",
                "LocalityElevationMethod", "usysobservatioobservationmetho", "ObservationMethod",
                "usyspermittype", "PermitType", "usyspreparatiocontainertype", "PrepContainertype",
                "usyspreparatiomedium", "PreparatioMedium", "usyspreparatiopreparationtype",
                "PreparationType", "usysshipmentshipmentmethod", "ShipmentMethod" };

        setProcess(0, tables.length);

        for (int i = 0; i < tables.length; i++)
        {
            setDesc("Converting " + tables[i]);

            setProcess(i);

            boolean status = convertUSYSToPicklist(tables[i], tables[i + 1]);
            if (!status)
            {
                log.error(tables[i] + " failed to convert.");
                return false;
            }
            i++;
        }
        setProcess(tables.length);
        return true;
    }

    /**
     * Creates a map from a String Preparation Type to its ID in the table.
     * @return map of name to PrepType
     */
    public Map<String, PrepType> createPreparationTypesFromUSys(final Collection collection)
    {
        deleteAllRecordsFromTable("preptype", BasicSQLUtils.myDestinationServerType);

        Hashtable<String, PrepType> prepTypeMapper = new Hashtable<String, PrepType>();

        try
        {
            /*
             * +-----------------------+-------------+------+-----+---------+-------+ | Field | Type |
             * Null | Key | Default | Extra |
             * +-----------------------+-------------+------+-----+---------+-------+ |
             * USYSCollObjPrepMethID | int(11) | | PRI | 0 | | | InterfaceID | int(11) | YES | |
             * NULL | | | FieldSetSubTypeID | int(11) | YES | | NULL | | | PreparationMethod |
             * varchar(50) | YES | | NULL | |
             * +-----------------------+-------------+------+-----+---------+-------+
             */
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sqlStr = "select USYSCollObjPrepMethID, InterfaceID, FieldSetSubTypeID, PreparationMethod from usyscollobjprepmeth";

            log.info(sqlStr);

            boolean foundMisc = false;
            ResultSet rs = stmt.executeQuery(sqlStr);
            int count = 0;
            while (rs.next())
            {
                if (rs.getObject(2) != null && rs.getObject(3) != null)
                {
                    String name = rs.getString(4);
                    PrepType prepType = AttrUtils.loadPrepType(name, collection);
                    if (shouldCreateMapTables)
                    {
                        prepTypeMapper.put(name.toLowerCase(), prepType);
                    }
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
                PrepType prepType = AttrUtils.loadPrepType(name, collection);
                // if (shouldCreateMapTables)
                {
                    prepTypeMapper.put(name.toLowerCase(), prepType);
                }
                count++;
            }
            log.info("Processed PrepType " + count + " records.");

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            return prepTypeMapper;
        }

        return prepTypeMapper;
    }

    /**
     * Convert the column name
     * @param name the name
     * @return the converted name
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
    protected Object getData(final ResultSet rs,
                             final int index,
                             final AttributeIFace.FieldType type,
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

                } else if (oldType == AttributeIFace.FieldType.StringType) { return rs.getString(
                        index).equalsIgnoreCase("true"); }
                log.error("Error maping from schema[" + metaData.getType() + "] to ["
                        + type.toString() + "]");
                return false;

            } else if (type == AttributeIFace.FieldType.FloatType)
            {
                if (value == null)
                {
                    return 0.0f;

                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    return rs.getFloat(index);

                } else if (oldType == AttributeIFace.FieldType.DoubleType) { return rs
                        .getFloat(index); }
                log.error("Error maping from schema[" + metaData.getType() + "] to ["
                        + type.toString() + "]");
                return 0.0f;

            } else if (type == AttributeIFace.FieldType.DoubleType)
            {
                if (value == null)
                {
                    return 0.0;

                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    return rs.getDouble(index);

                } else if (oldType == AttributeIFace.FieldType.DoubleType) { return rs
                        .getDouble(index); }
                log.error("Error maping from schema[" + metaData.getType() + "] to ["
                        + type.toString() + "]");
                return 0.0;

            } else if (type == AttributeIFace.FieldType.IntegerType)
            {
                if (value == null)
                {
                    return 0;

                } else if (oldType == AttributeIFace.FieldType.IntegerType) { return rs
                        .getInt(index) != 0; }
                log.error("Error maping from schema[" + metaData.getType() + "] to ["
                        + type.toString() + "]");
                return 0;

            } else
            {
                return rs.getString(index);
            }
        } catch (SQLException ex)
        {
            log.error("Error maping from schema[" + metaData.getType() + "] to [" + type.toString()
                    + "]");
            log.error(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets a converted value from the old schema to the new schema into the CollectionObjectAttr
     * object
     * @param rs the resultset
     * @param index the index of the column in the resultset
     * @param type the defined type for the new schema
     * @param metaData the metat data describing the old schema column
     * @param colObjAttr the object the data is set into
     * @return the new data object
     */
    protected void setData(final ResultSet rs,
                           final int index,
                           final AttributeIFace.FieldType type,
                           final BasicSQLUtils.FieldMetaData metaData,
                           final CollectionObjectAttr colObjAttr)
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
                    colObjAttr.setDblValue(0.0); // false

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
                    colObjAttr
                            .setDblValue(rs.getString(index).equalsIgnoreCase("true") ? 1.0 : 0.0);
                } else
                {
                    log.error("Error maping from schema[" + metaData.getType() + "] to ["
                            + type.toString() + "]");
                }

            } else if (type == AttributeIFace.FieldType.IntegerType
                    || type == AttributeIFace.FieldType.DoubleType
                    || type == AttributeIFace.FieldType.FloatType)
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
                    log.error("Error maping from schema[" + metaData.getType() + "] to ["
                            + type.toString() + "]");
                }

            } else
            {
                colObjAttr.setStrValue(rs.getString(index));
            }
        } catch (SQLException ex)
        {
            log.error("Error maping from schema[" + metaData.getType() + "] to [" + type.toString()
                    + "]");
            log.error(ex);
            throw new RuntimeException(ex);
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

            // } else if (name.equalsIgnoreCase("remarks"))
            // {
            // return AttributeIFace.FieldType.MemoType;

        } else if (type.equalsIgnoreCase("float"))
        {
            return AttributeIFace.FieldType.FloatType;

        } else if (type.equalsIgnoreCase("double"))
        {
            return AttributeIFace.FieldType.DoubleType;

        } else if (type.startsWith("varchar") || type.startsWith("text")
                || type.startsWith("longtext"))
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
    protected Object getMappedId(final Object data,
                                 final String fromTableName,
                                 final String oldColName)
    {
        if (idMapperMgr != null && oldColName.endsWith("ID"))
        {

            IdMapperIFace idMapper = idMapperMgr.get(fromTableName, oldColName);
            if (idMapper != null) { return idMapper.get((Integer)data); }
            // else

            // throw new RuntimeException("No Map for ["+fromTableName+"]["+oldMappedColName+"]");
            if (!oldColName.equals("MethodID") && !oldColName.equals("RoleID")
                    && !oldColName.equals("CollectionID") && !oldColName.equals("ConfidenceID")
                    && !oldColName.equals("TypeStatusNameID")
                    && !oldColName.equals("ObservationMethodID"))
            {
                System.out.println("No Map for [" + fromTableName + "][" + oldColName + "]");
            }
        }
        return data;
    }

    /**
     * Convert all the biological attributes to Collection Object Attributes. Each old record may
     * end up being multiple records in the new schema. This will first figure out which columns in
     * the old schema were used and olnly map those columns to the new database.<br>
     * <br>
     * It also will use the old name if there is not mapping for it. The old name is converted from
     * lower/upper case to be space separated where each part of the name starts with a capital
     * letter.
     * 
     * @param discipline the Discipline
     * @param colToNameMap a mape for old names to new names
     * @param typeMap a map for changing the type of the data (meaning an old value may be a boolean
     *            stored in a float)
     * @return true for success
     */
    public boolean convertBiologicalAttrs(Discipline discipline, @SuppressWarnings("unused")
    final Map<String, String> colToNameMap, final Map<String, Short> typeMap)
    {
        AttributeIFace.FieldType[] attrTypes = { AttributeIFace.FieldType.IntegerType,
                AttributeIFace.FieldType.FloatType, AttributeIFace.FieldType.DoubleType,
                AttributeIFace.FieldType.BooleanType, AttributeIFace.FieldType.StringType,
        // AttributeIFace.FieldType.MemoType
        };

        Session localSession = HibernateUtil.getCurrentSession();

        deleteAllRecordsFromTable(newDBConn, "collectionobjectattr",
                BasicSQLUtils.myDestinationServerType);
        deleteAllRecordsFromTable(newDBConn, "attributedef", BasicSQLUtils.myDestinationServerType);

        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            // grab the field and their type from the old schema
            List<BasicSQLUtils.FieldMetaData> oldFieldMetaData = new ArrayList<BasicSQLUtils.FieldMetaData>();
            Map<String, BasicSQLUtils.FieldMetaData> oldFieldMetaDataMap = new Hashtable<String, BasicSQLUtils.FieldMetaData>();
            getFieldMetaDataFromSchema(oldDBConn, "biologicalobjectattributes", oldFieldMetaData,
                    BasicSQLUtils.mySourceServerType);

            // create maps to figure which columns where used
            List<String> columnsInUse = new ArrayList<String>();
            Map<String, AttributeDef> attrDefs = new Hashtable<String, AttributeDef>();

            List<Integer> counts = new ArrayList<Integer>();

            int totalCount = 0;

            for (BasicSQLUtils.FieldMetaData md : oldFieldMetaData)
            {
                // Skip these fields
                if (md.getName().indexOf("ID") == -1 && md.getName().indexOf("Timestamp") == -1
                        && md.getName().indexOf("LastEditedBy") == -1)
                {
                    oldFieldMetaDataMap.put(md.getName(), md); // add to map for later

                    // log.info(convertColumnName(md.getName())+" "+ md.getType());
                    String sqlStr = "select count(" + md.getName()
                            + ") from biologicalobjectattributes where " + md.getName()
                            + " is not null";
                    ResultSet rs = stmt.executeQuery(sqlStr);
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
                        System.out.println("mapping[" + newName + "][" + md.getName() + "]");

                        // newNameToOldNameMap.put(newName, md.getName());

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
                        attrDef.setDiscipline(discipline);
                        attrDef.setTableType(GenericDBConversion.TableType.CollectionObject
                                .getType());
                        attrDef.setTimestampCreated(now);

                        attrDefs.put(md.getName(), attrDef);

                        try
                        {
                            HibernateUtil.beginTransaction();
                            localSession.save(attrDef);
                            HibernateUtil.commitTransaction();

                        } catch (Exception e)
                        {
                            log.error("******* " + e);
                            HibernateUtil.rollbackTransaction();
                            throw new RuntimeException(e);
                        }

                    }
                    rs.close();
                }
            } // for
            log.info("Total Number of Attrs: " + totalCount);

            // Now that we know which columns are being used we can start the conversion process

            log.info("biologicalobjectattributes columns in use: " + columnsInUse.size());
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

                str
                        .append(" from biologicalobjectattributes order by BiologicalObjectAttributesID");
                log.info("sql: " + str.toString());
                ResultSet rs = stmt.executeQuery(str.toString());

                int[] countVerify = new int[counts.size()];
                for (int i = 0; i < countVerify.length; i++)
                {
                    countVerify[i] = 0;
                }
                boolean useHibernate = false;
                StringBuilder strBufInner = new StringBuilder();
                int recordCount = 0;
                while (rs.next())
                {

                    if (useHibernate)
                    {
                        Criteria criteria = localSession.createCriteria(CollectionObject.class);
                        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
                        criteria.add(Restrictions.eq("collectionObjectId", rs.getInt(1)));
                        List<?> list = criteria.list();
                        if (list.size() == 0)
                        {
                            log.error("**** Can't find the CollectionObject " + rs.getInt(1));
                        } else
                        {
                            CollectionObject colObj = (CollectionObject)list.get(0);

                            inx = 2; // skip the first column (the ID)
                            for (String name : columnsInUse)
                            {
                                AttributeDef attrDef = attrDefs.get(name); // the needed
                                                                            // AttributeDef by name
                                BasicSQLUtils.FieldMetaData md = oldFieldMetaDataMap.get(name);

                                // Create the new Collection Object Attribute
                                CollectionObjectAttr colObjAttr = new CollectionObjectAttr();
                                colObjAttr.setCollectionObject(colObj);
                                colObjAttr.setDefinition(attrDef);
                                colObjAttr.setTimestampCreated(now);

                                // String oldName = newNameToOldNameMap.get(attrDef.getFieldName());
                                // System.out.println("["+attrDef.getFieldName()+"]["+oldName+"]");

                                // System.out.println(inx+" "+attrTypes[attrDef.getDataType()]+"
                                // "+md.getName()+" "+md.getType());
                                setData(rs, inx, attrTypes[attrDef.getDataType()], md, colObjAttr);

                                HibernateUtil.beginTransaction();
                                localSession.save(colObjAttr);
                                HibernateUtil.commitTransaction();

                                inx++;
                                if (recordCount % 2000 == 0)
                                {
                                    log.info("CollectionObjectAttr Records Processed: "
                                            + recordCount);
                                }
                                recordCount++;
                            } // for
                            // log.info("Done - CollectionObjectAttr Records Processed:
                            // "+recordCount);
                        }
                    } else
                    {
                        inx = 2; // skip the first column (the ID)
                        for (String name : columnsInUse)
                        {
                            AttributeDef attrDef = attrDefs.get(name); // the needed AttributeDef
                                                                        // by name
                            BasicSQLUtils.FieldMetaData md = oldFieldMetaDataMap.get(name);

                            if (rs.getObject(inx) != null)
                            {
                                Integer newRecId = (Integer)getMappedId(rs.getInt(1),
                                        "biologicalobjectattributes",
                                        "BiologicalObjectAttributesID");

                                Object data = getData(rs, inx, attrTypes[attrDef.getDataType()], md);
                                boolean isStr = data instanceof String;

                                countVerify[inx - 2]++;

                                strBufInner.setLength(0);
                                strBufInner.append("INSERT INTO collectionobjectattr VALUES (");
                                strBufInner.append("NULL");// Integer.toString(recordCount));
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(isStr ? data : null));
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(isStr ? null : data));
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(now));
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(now));
                                strBufInner.append(",");
                                strBufInner.append(newRecId.intValue());
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(attrDef.getAttributeDefId()));
                                strBufInner.append(")");

                                try
                                {
                                    Statement updateStatement = newDBConn.createStatement();
                                    // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                                    BasicSQLUtils.removeForeignKeyConstraints(newDBConn,
                                            BasicSQLUtils.myDestinationServerType);
                                    if (false)
                                    {
                                        System.out.println(strBufInner.toString());
                                    }
                                    updateStatement.executeUpdate(strBufInner.toString());
                                    updateStatement.clearBatch();
                                    updateStatement.close();
                                    updateStatement = null;

                                } catch (SQLException e)
                                {
                                    log.error(strBufInner.toString());
                                    log.error("Count: " + recordCount);
                                    e.printStackTrace();
                                    log.error(e);
                                    throw new RuntimeException(e);
                                }

                                if (recordCount % 2000 == 0)
                                {
                                    log.info("CollectionObjectAttr Records Processed: "
                                            + recordCount);
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
                for (int i = 0; i < counts.size(); i++)
                {
                    log.info(columnsInUse.get(i) + " [" + counts.get(i) + "][" + countVerify[i]
                            + "] " + (counts.get(i) - countVerify[i]));

                }
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Converts all the CollectionObject Physical records and CollectionObjectCatalog Records into
     * the new schema Preparation table.
     * @return true if no errors
     */
    public boolean convertPreparationRecords(final Hashtable<Integer, Map<String, PrepType>> collToPrepTypeHash)
    {
        deleteAllRecordsFromTable(newDBConn, "preparation", BasicSQLUtils.myDestinationServerType);
        // BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "preparation",
        // BasicSQLUtils.myDestinationServerType);
        try
        {
            Statement     stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            StringBuilder str  = new StringBuilder();

            List<String> oldFieldNames = new ArrayList<String>();

            StringBuilder sql = new StringBuilder("SELECT ");
            List<String> names = new ArrayList<String>();
            getFieldNamesFromSchema(oldDBConn, "collectionobject", names, BasicSQLUtils.mySourceServerType);

            sql.append(buildSelectFieldList(names, "collectionobject"));
            sql.append(", ");
            oldFieldNames.addAll(names);

            names.clear();
            getFieldNamesFromSchema(oldDBConn,     "collectionobjectcatalog", names, BasicSQLUtils.mySourceServerType);
            sql.append(buildSelectFieldList(names, "collectionobjectcatalog"));
            oldFieldNames.addAll(names);

            sql.append(" From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID ");
            sql.append("Where not (collectionobject.DerivedFromID Is Null) order by collectionobjectcatalog.CollectionObjectCatalogID");

            log.info(sql);

            List<BasicSQLUtils.FieldMetaData> newFieldMetaData = new ArrayList<BasicSQLUtils.FieldMetaData>();
            getFieldMetaDataFromSchema(newDBConn, "preparation", newFieldMetaData, BasicSQLUtils.myDestinationServerType);

            log.info("Number of Fields in Preparation " + newFieldMetaData.size());
            String sqlStr = sql.toString();

            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 0;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
                System.out.println(name + " " + (inx - 1));
            }
            Hashtable<String, String> newToOld = new Hashtable<String, String>();
            newToOld.put("PreparationID",      "CollectionObjectID");
            newToOld.put("CollectionObjectID", "DerivedFromID");
            newToOld.put("StorageLocation",    "Storage");

            IdMapperIFace agentIdMapper = idMapperMgr.get("agent", "AgentID");
            // IdMapperIFace prepIdMapper = idMapperMgr.get("preparation", "PreparationID");

            boolean doDebug = false;
            ResultSet rs = stmt.executeQuery(sqlStr);

            if (rs.last())
            {
                setProcess(0, rs.getRow());
                rs.first();

            } else
            {
                rs.close();
                stmt.close();
                setProcess(0, 0);
                return true;
            }

            Statement prepStmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            // Meg added
            // Map all the Physical IDs
            IdTableMapper idMapper2 = idMapperMgr.addTableMapper("preparation", "PreparationID");
            if (shouldCreateMapTables)
            {
                idMapper2.mapAllIds("select CollectionObjectID from collectionobject Where not (collectionobject.DerivedFromID Is Null) order by CollectionObjectID");
            }

            IdMapperIFace colObjIdMapper = idMapperMgr.get("preparation", "PreparationID");
            colObjIdMapper.setShowLogErrors(false);

            int     lastEditedByInx = oldNameIndex.get("LastEditedBy") + 1;
            Integer idIndex         = oldNameIndex.get("CollectionObjectID");
            int     count           = 0;
            do
            {
                Integer preparedById = null;
                Date preparedDate = null;

                boolean checkForPreps = false;
                if (checkForPreps)
                {
                    Integer   recordId    = rs.getInt(idIndex + 1);
                    Statement subStmt     = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    String    subQueryStr = "select PreparedByID, PreparedDate from preparation where PreparationID = " + recordId;
                    ResultSet subQueryRS  = subStmt.executeQuery(subQueryStr);
                    
                    if (subQueryRS.first())
                    {
                        preparedById = subQueryRS.getInt(1);
                        preparedDate = UIHelper.convertIntToDate(subQueryRS.getInt(2));
                    }
                    subQueryRS.close();
                    subStmt.close();
                }

                String lastEditedBy = rs.getString(lastEditedByInx);

                /*
                 * int catNum = rs.getInt(oldNameIndex.get("CatalogNumber")+1); doDebug = catNum ==
                 * 30972;
                 * 
                 * if (doDebug) { System.out.println("CatalogNumber "+catNum);
                 * System.out.println("CollectionObjectID
                 * "+rs.getInt(oldNameIndex.get("CollectionObjectID")+1));
                 * System.out.println("DerivedFromID
                 * "+rs.getInt(oldNameIndex.get("DerivedFromID")+1)); }
                 */

                str.setLength(0);

                StringBuffer fieldList = new StringBuffer();
                fieldList.append("( ");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if ((i > 1) && (i < newFieldMetaData.size()))
                    {
                        fieldList.append(", ");
                    }
                    if (i > 0)
                    {
                        String newFieldName = newFieldMetaData.get(i).getName();
                        fieldList.append(newFieldName + " ");
                    }
                }
                fieldList.append(")");

                str.append("INSERT INTO preparation " + fieldList + " VALUES (");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if (i > 1)
                        str.append(", ");

                    String newFieldName = newFieldMetaData.get(i).getName();
                    String mappedName = newToOld.get(newFieldName);

                    if (mappedName != null)
                    {
                        newFieldName = mappedName;
                    } else
                    {
                        mappedName = newFieldName;
                    }

                    if (i == 0)
                    {
                        // need to skip over inserting a value for the PreparationID
                        // Integer recId = count+1;
                        // str.append("NULL");//getStrValue(recId));

                    } else if (newFieldName.equals("PreparedByID"))
                    {
                        if (agentIdMapper != null)
                        {
                            str.append(getStrValue(agentIdMapper.get(preparedById)));
                        } else
                        {
                            log.error("No Map for PreparedByID[" + preparedById + "]");
                        }

                    } else if (newFieldName.equals("PreparedDate"))
                    {
                        str.append(getStrValue(preparedDate));

                    } else if (newFieldName.equals("DerivedFromIDX"))
                    {
                        // skip

                    } else if (newFieldName.equals("PreparationAttributesID"))
                    {
                        Integer id = rs.getInt(idIndex + 1);
                        Object data = colObjIdMapper.get(id);
                        if (data == null)
                        {
                            // throw new RuntimeException("Couldn't map ID for new
                            // PreparationAttributesID [CollectionObjectID]["+id+"]");
                            str.append("NULL");

                        } else
                        {
                            ResultSet prepRS = 
                                prepStmt.executeQuery("select PreparationID from preparation where PreparationID = " + id);
                            if (prepRS.first())
                            {
                                str.append(getStrValue(data));
                            } else
                            {
                                str.append("NULL");
                            }
                            prepRS.close();
                        }

                    } else if (newFieldName.equals("Count"))
                    {
                        Integer value = rs.getInt("Count");
                        if (rs.wasNull())
                        {
                            value = null;
                        }
                        str.append(getStrValue(value));

                    } else if (newFieldName.equalsIgnoreCase("SampleNumber")
                            || newFieldName.equalsIgnoreCase("Status")
                            || newFieldName.equalsIgnoreCase("YesNo3"))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equalsIgnoreCase("Version"))
                    {
                        str.append("0");

                    } else if (newFieldName.equalsIgnoreCase("CollectionMemberID"))
                    {
                        str.append(getCollectionMemberId());

                    } else if (newFieldName.equalsIgnoreCase("ModifiedByAgentID"))
                    {
                        str.append(getModifiedByAgentId(lastEditedBy));

                    } else if (newFieldName.equalsIgnoreCase("CreatedByAgentID"))
                    {
                        str.append(getCreatorAgentId(null));

                    } else if (newFieldName.equals("PrepTypeID"))
                    {
                        String value = rs.getString(oldNameIndex.get("PreparationMethod") + 1);
                        if (value == null || value.length() == 0)
                        {
                            value = "n/a";
                        }

                        PrepType prepType = null;// ZZZ prepTypeMap.get(value.toLowerCase());
                        if (prepType != null)
                        {
                            Integer prepTypeId = prepType.getPrepTypeId();
                            if (prepTypeId != null)
                            {
                                str.append(getStrValue(prepTypeId));

                            } else
                            {
                                str.append("NULL");
                                log.error("***************** Couldn't find PreparationMethod["
                                        + value + "] in PrepTypeMap");
                            }
                        } else
                        {
                            log.info("Couldn't find PrepType[" + value + "] creating it.");
                            prepType = new PrepType();
                            prepType.initialize();
                            prepType.setName(value);
                            try
                            {
                                Session tmpSession = HibernateUtil.getCurrentSession();
                                Transaction trans = tmpSession.beginTransaction();
                                trans.begin();
                                tmpSession.save(prepType);
                                trans.commit();

                                str.append(getStrValue(prepType.getPrepTypeId()));

                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                                throw new RuntimeException(ex);
                            }
                        }

                    } else if (newFieldName.equals("StorageID"))
                    {
                        str.append("NULL");

                    } else
                    {
                        Integer index = oldNameIndex.get(newFieldName);
                        if (index == null)
                        {
                            String msg = "Couldn't find new field name[" + newFieldName
                                    + "] in old field name in index Map";
                            log.error(msg);
                            stmt.close();
                            throw new RuntimeException(msg);
                        }
                        Object data = rs.getObject(index + 1);

                        if (idMapperMgr != null && mappedName.endsWith("ID"))
                        {
                            IdMapperIFace idMapper;
                            if (mappedName.equals("DerivedFromID"))
                            {
                                idMapper = idMapperMgr.get("preparation", "PreparationID");

                            } else
                            {
                                idMapper = idMapperMgr.get("collectionobject", mappedName);

                            }
                            if (idMapper != null)
                            {
                                data = idMapper.get(rs.getInt(index));
                            } else
                            {
                                throw new RuntimeException("No Map for [collectionobject]["
                                        + mappedName + "]");
                            }
                        }
                        str.append(getStrValue(data, newFieldMetaData.get(i).getType()));

                    }

                }
                str.append(")");
                // log.info("\n"+str.toString());
                if (hasFrame)
                {
                    if (count % 5000 == 0)
                    {
                        setProcess(count);
                        log.info("Preparation Records: " + count);
                    }

                } else
                {
                    if (count % 2000 == 0)
                    {
                        log.info("Preparation Records: " + count);
                    }
                }

                try
                {
                    Statement updateStatement = newDBConn.createStatement();
                    // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                    if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
                    {
                        BasicSQLUtils.removeForeignKeyConstraints(newDBConn, "preparation",
                                BasicSQLUtils.myDestinationServerType);
                    }
                    if (doDebug)
                    {
                        System.out.println(str.toString());
                    }
                    // log.debug(str.toString());
                    updateStatement.executeUpdate(str.toString());
                    updateStatement.clearBatch();
                    updateStatement.close();
                    updateStatement = null;

                } catch (SQLException e)
                {
                    log.error("Error trying to execute: " + str.toString());
                    log.error("Count: " + count);
                    e.printStackTrace();
                    log.error(e);
                    throw new RuntimeException(e);
                }

                count++;
                // if (count == 1) break;
            } while (rs.next());

            prepStmt.close();

            if (hasFrame)
            {
                setProcess(count);
            } else
            {
                if (count % 2000 == 0)
                {
                    log.info("Processed CollectionObject " + count + " records.");
                }
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Converts all the Determinations.
     * @return true if no errors
     */
    public boolean convertDeterminationRecords()
    {
        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "determination",
                BasicSQLUtils.myDestinationServerType);

        deleteAllRecordsFromTable(newDBConn, "determination", BasicSQLUtils.myDestinationServerType); // automatically
                                                                                                        // closes
                                                                                                        // the
                                                                                                        // connection

        if (BasicSQLUtils.getNumRecords(oldDBConn, "determination") == 0) { return true; }

        String oldDetermination_Current = "Current";
        String oldDetermination_Date = "Date";

        if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
        {
            oldDetermination_Date = "Date1";
            oldDetermination_Current = "IsCurrent";
        }

        Map<String, String> colNewToOldMap = createFieldNameMap(new String[] {
                "CollectionObjectID", "BiologicalObjectID", // meg is this right?
                "IsCurrent", oldDetermination_Current, "DeterminedDate", oldDetermination_Date, // want
                                                                                                // to
                                                                                                // change
                                                                                                // over
                                                                                                // to
                                                                                                // DateField
                                                                                                // TODO
                                                                                                // Meg!!!
                "TaxonID", "TaxonNameID" });

        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            StringBuilder str = new StringBuilder();

            List<String> oldFieldNames = new ArrayList<String>();

            StringBuilder sql = new StringBuilder("SELECT ");
            List<String> names = new ArrayList<String>();
            getFieldNamesFromSchema(oldDBConn, "determination", names,
                    BasicSQLUtils.mySourceServerType);

            sql.append(buildSelectFieldList(names, "determination"));
            oldFieldNames.addAll(names);

            sql.append(" FROM determination");

            log.info(sql);

            if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MS_SQLServer)
            {
                log.debug("FIXING select statement to run against SQL Server.......");
                log.debug("old string: " + sql.toString());
                String currentSQL = sql.toString();
                currentSQL = currentSQL.replaceAll("Current", "[" + "Current" + "]");
                log.debug("new string: " + currentSQL);
                sql = new StringBuilder(currentSQL);

            }
            log.info(sql);
            List<BasicSQLUtils.FieldMetaData> newFieldMetaData = new ArrayList<BasicSQLUtils.FieldMetaData>();
            getFieldMetaDataFromSchema(newDBConn, "determination", newFieldMetaData,
                    BasicSQLUtils.myDestinationServerType);

            log.info("Number of Fields in New Determination " + newFieldMetaData.size());
            String sqlStr = sql.toString();

            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 0;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
            }

            String tableName = "determination";

            int isCurrentInx = oldNameIndex.get(oldDetermination_Current) + 1;

            // Get Current and Unknow Record Ids
            int currentDetStatusID = 0;
            Statement newStmt = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs2 = newStmt
                    .executeQuery("select DeterminationStatusID from determinationstatus where Name = 'Current'");
            if (rs2.first())
            {
                currentDetStatusID = rs2.getInt(1);
                rs2.close();

            } else
            {
                throw new RuntimeException("Couldn't find Current DeterminationStatus record!");
            }

            int unknownDetStatusID = 0;
            rs2 = newStmt
                    .executeQuery("select DeterminationStatusID from determinationstatus where Name = 'Unknown'");
            if (rs2.first())
            {
                unknownDetStatusID = rs2.getInt(1);
                rs2.close();
                newStmt.close();

            } else
            {
                throw new RuntimeException("Couldn't find Current DeterminationStatus record!");
            }

            log.info(sqlStr);
            ResultSet rs = stmt.executeQuery(sqlStr);

            if (hasFrame)
            {
                if (rs.last())
                {
                    setProcess(0, rs.getRow());
                    rs.first();

                } else
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            } else
            {
                if (!rs.first())
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            }

            int lastEditedByInx = oldNameIndex.get("LastEditedBy") + 1;

            int count = 0;
            do
            {
                String lastEditedBy = rs.getString(lastEditedByInx);

                str.setLength(0);
                StringBuffer fieldList = new StringBuffer();
                fieldList.append("( ");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if ((i > 0) && (i < newFieldMetaData.size()))
                    {
                        fieldList.append(", ");
                    }
                    String newFieldName = newFieldMetaData.get(i).getName();
                    fieldList.append(newFieldName + " ");
                }
                fieldList.append(")");
                str.append("INSERT INTO determination " + fieldList + " VALUES (");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if (i > 0)
                        str.append(", ");

                    String newFieldName = newFieldMetaData.get(i).getName();

                    if (i == 0)
                    {
                        Integer recId = count + 1;
                        str.append(getStrValue(recId));

                    } else if (newFieldName.equals("DeterminationStatusID"))
                    {
                        str.append(Integer
                                .toString(rs.getShort(isCurrentInx) != 0 ? currentDetStatusID
                                        : unknownDetStatusID));

                    } else if (newFieldName.equals("Version")) // User/Security changes
                    {
                        str.append("0");

                    } else if (newFieldName.equals("CreatedByAgentID")) // User/Security changes
                    {
                        str.append(getCreatorAgentId(null));

                    } else if (newFieldName.equals("ModifiedByAgentID")) // User/Security changes
                    {
                        str.append(getModifiedByAgentId(lastEditedBy));

                    } else if (newFieldName.equals("CollectionMemberID")) // User/Security changes
                    {
                        str.append(getCollectionMemberId());

                    } else
                    {
                        Integer index = null;
                        String oldMappedColName = colNewToOldMap.get(newFieldName);
                        if (oldMappedColName != null)
                        {
                            index = oldNameIndex.get(oldMappedColName);

                        } else
                        {
                            index = oldNameIndex.get(newFieldName);
                            oldMappedColName = newFieldName;
                        }

                        if (index == null)
                        {
                            String msg = "Couldn't find new field name[" + newFieldName
                                    + "] in old field name in index Map";
                            log.error(msg);
                            stmt.close();
                            throw new RuntimeException(msg);
                        }

                        Object data = rs.getObject(index + 1);

                        if (data != null)
                        {
                            int idInx = newFieldName.lastIndexOf("ID");
                            if (idMapperMgr != null && idInx > -1)
                            {
                                IdMapperIFace idMapper = idMapperMgr.get(tableName,
                                        oldMappedColName);
                                if (idMapper != null)
                                {
                                    data = idMapper.get(rs.getInt(index + 1));
                                } else
                                {
                                    log.error("No Map for [" + tableName + "][" + oldMappedColName
                                            + "]");
                                }
                            }
                        }

                        // hack for ??bug?? found in Sp5 that inserted null values in
                        // timestampmodified field of determination table?
                        if (newFieldName.equals("TimestampModified"))
                        {
                            // log.error("******TimestampModified***************" +
                            // getStrValue(data, newFieldMetaData.get(i).getType()));
                            if (getStrValue(data, newFieldMetaData.get(i).getType()).toString()
                                    .toLowerCase().equals("null"))
                            {
                                // log.error("******TimestampModified***************" + "found null
                                // value, appending string: " + "'"+nowStr+"'");
                                str.append("'" + nowStr + "'");
                                // log.error("new string: " +str);
                            } else
                            {
                                str.append(getStrValue(data, newFieldMetaData.get(i).getType()));
                            }
                        } else
                        {
                            // log.error("my debgings - ##########################" +
                            // getStrValue(data, newFieldMetaData.get(i).getType()).toString());
                            str.append(getStrValue(data, newFieldMetaData.get(i).getType()));
                            // log.error("new string: " +str);
                        }
                    }
                }
                str.append(")");

                if (hasFrame)
                {
                    if (count % 100 == 0)
                    {
                        setProcess(count);
                    }

                } else
                {
                    if (count % 2000 == 0)
                    {
                        log.info("Determination Records: " + count);
                    }
                }

                try
                {
                    Statement updateStatement = newDBConn.createStatement();
                    // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                    updateStatement.executeUpdate(str.toString());
                    updateStatement.clearBatch();
                    updateStatement.close();
                    updateStatement = null;

                } catch (SQLException e)
                {
                    log.error("Count: " + count);
                    log.error("Exception on insert: " + str.toString());
                    e.printStackTrace();
                    log.error(e);
                    rs.close();
                    stmt.close();
                    throw new RuntimeException(e);
                }

                count++;
                // if (count > 10) break;
            } while (rs.next());

            if (hasFrame)
            {
                setProcess(count);
            } else
            {
                log.info("Processed Determination " + count + " records.");
            }
            rs.close();

            stmt.close();

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }

        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "determination",
                BasicSQLUtils.myDestinationServerType);

        return true;

    }

    /**
     * Converts all the CollectionObject and CollectionObjectCatalog Records into the new schema
     * CollectionObject table. All "logical" records are moved to the CollectionObject table and all
     * "physical" records are moved to the Preparation table.
     * @return true if no errors
     */
    public boolean convertCollectionObjects(final boolean useNumericCatNumbers,
                                            final boolean usePrefix)
    {
        idMapperMgr.dumpKeys();
        IdHashMapper colObjTaxonMapper = (IdHashMapper)idMapperMgr.get("ColObjCatToTaxonType"
                .toLowerCase());
        IdHashMapper colObjAttrMapper = (IdHashMapper)idMapperMgr
                .get("biologicalobjectattributes_BiologicalObjectAttributesID");
        colObjTaxonMapper.setShowLogErrors(false); // NOTE: TURN THIS ON FOR DEBUGGING or running
                                                    // new Databases through it
        colObjAttrMapper.setShowLogErrors(false);

        IdHashMapper stratMapper = (IdHashMapper)idMapperMgr.get("stratigraphy_StratigraphyID");
        IdHashMapper stratGTPMapper = (IdHashMapper)idMapperMgr
                .get("stratigraphy_GeologicTimePeriodID");

        String[] fieldsToSkip = { "CatalogedDateVerbatim", "ContainerID", "ContainerItemID",
                "AltCatalogNumber",
                "GUID",
                "ContainerOwnerID",
                "RepositoryAgreementID",
                "GroupPermittedToView", // this may change when converting Specify 5.x
                "CollectionObjectID", "VisibilitySetBy", "ContainerOwnerID", "InventoryDate",
                "ObjectCondition", "Notifications", "ProjectNumber", "Restrictions", "YesNo3",
                "YesNo4", "YesNo5", "YesNo6", "FieldNotebookPageID", "ColObjAttributesID",
                "DNASequenceID", "AppraisalID", "TotalValue" };

        Hashtable<String, String> fieldsToSkipHash = new Hashtable<String, String>();
        for (String fName : fieldsToSkip)
        {
            fieldsToSkipHash.put(fName, "X");
        }

        log.info("colObjTaxonMapper: " + colObjTaxonMapper.size());
        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "collectionobject",
                BasicSQLUtils.myDestinationServerType);

        deleteAllRecordsFromTable(newDBConn, "collectionobject",
                BasicSQLUtils.myDestinationServerType); // automatically closes the connection
        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            StringBuilder str = new StringBuilder();

            List<String> oldFieldNames = new ArrayList<String>();

            StringBuilder sql = new StringBuilder("select ");
            List<String> names = new ArrayList<String>();
            getFieldNamesFromSchema(oldDBConn, "collectionobject", names,
                    BasicSQLUtils.mySourceServerType);

            sql.append(buildSelectFieldList(names, "collectionobject"));
            sql.append(", ");
            oldFieldNames.addAll(names);

            names.clear();
            getFieldNamesFromSchema(oldDBConn, "collectionobjectcatalog", names,
                    BasicSQLUtils.mySourceServerType);
            sql.append(buildSelectFieldList(names, "collectionobjectcatalog"));
            oldFieldNames.addAll(names);

            sql
                    .append(" From collectionobject Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID Where collectionobject.CollectionObjectTypeID = 10");

            log.info(sql);

            // List<String> newFieldNames = new ArrayList<String>();
            // getFieldNamesFromSchema(newDBConn, "collectionobject", newFieldNames);

            List<BasicSQLUtils.FieldMetaData> newFieldMetaData = new ArrayList<BasicSQLUtils.FieldMetaData>();
            getFieldMetaDataFromSchema(newDBConn, "collectionobject", newFieldMetaData,
                    BasicSQLUtils.myDestinationServerType);

            log.info("Number of Fields in New CollectionObject " + newFieldMetaData.size());
            String sqlStr = sql.toString();

            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 0;
            log.info("---- Old Names ----");
            for (String name : oldFieldNames)
            {
                log.info("[" + name + "][" + inx + "]");
                oldNameIndex.put(name, inx++);
            }

            log.info("---- New Names ----");
            for (BasicSQLUtils.FieldMetaData fmd : newFieldMetaData)
            {
                log.info("[" + fmd.getName() + "]");
            }
            String tableName = "collectionobject";

            log.info(sqlStr);
            ResultSet rs = stmt.executeQuery(sqlStr);

            if (hasFrame)
            {
                if (rs.last())
                {
                    setProcess(0, rs.getRow());
                    rs.first();

                } else
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            } else
            {
                if (!rs.first())
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            }

            Statement stmt2 = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            int catNumInx = oldNameIndex.get("CatalogNumber");

            int colObjAttrsNotMapped = 0;
            int count = 0;
            boolean skipRecord = false;
            do
            {
                skipRecord = false;
                String catIdTaxIdStr = "SELECT collectionobjectcatalog.CollectionObjectCatalogID,collectionobjectcatalog.CatalogSeriesID, collectiontaxonomytypes.TaxonomyTypeID "
                        + "FROM collectionobjectcatalog "
                        + "Inner Join collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID "
                        + "Inner Join collectiontaxonomytypes ON collectionobject.CollectionObjectTypeID = collectiontaxonomytypes.BiologicalObjectTypeID "
                        + "where collectionobjectcatalog.CollectionObjectCatalogID = "
                        + rs.getInt(1);
                // log.info(catIdTaxIdStr);
                ResultSet rs2 = stmt2.executeQuery(catIdTaxIdStr);
                rs2.first();
                Integer catalogSeriesID = rs2.getInt(2);
                Integer taxonomyTypeID = rs2.getInt(3);
                Integer newCatSeriesId = collectionHash.get(catalogSeriesID + "_" + taxonomyTypeID);
                String prefix = prefixHash.get(catalogSeriesID + "_" + taxonomyTypeID);
                rs2.close();

                if (newCatSeriesId == null)
                {
                    log.error("Can't find " + catalogSeriesID + "_" + taxonomyTypeID);
                }

                String stratGTPIdStr = "SELECT collectionobject.CollectionObjectID, "
                        + "collectingevent.CollectingEventID, "
                        + "stratigraphy.StratigraphyID, "
                        + "geologictimeperiod.GeologicTimePeriodID "
                        + "FROM collectionobject INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID "
                        + "INNER JOIN stratigraphy ON collectingevent.CollectingEventID = stratigraphy.StratigraphyID "
                        + "INNER JOIN geologictimeperiod ON stratigraphy.GeologicTimePeriodID = geologictimeperiod.GeologicTimePeriodID "
                        + "where collectionobject.CollectionObjectID = " + rs.getInt(1);
                rs2 = stmt2.executeQuery(stratGTPIdStr);

                Integer coId = null;
                Integer ceId = null;
                Integer stId = null;
                Integer gtpId = null;
                if (rs2.next())
                {
                    coId = rs2.getInt(1);
                    ceId = rs2.getInt(2);
                    stId = rs2.getInt(3);
                    gtpId = rs2.getInt(4);
                }
                rs2.close();

                String catalogNumber = null;
                String colObjId = null;

                str.setLength(0);

                StringBuffer fieldList = new StringBuffer();
                fieldList.append("( ");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if ((i > 0) && (i < newFieldMetaData.size()))
                    {
                        fieldList.append(", ");
                    }
                    String newFieldName = newFieldMetaData.get(i).getName();
                    fieldList.append(newFieldName + " ");
                }
                fieldList.append(")");
                str.append("INSERT INTO collectionobject " + fieldList + "  VALUES (");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if (i > 0)
                        str.append(", ");

                    String newFieldName = newFieldMetaData.get(i).getName();

                    if (i == 0)
                    {
                        Integer recId = count + 1;

                        str.append(getStrValue(recId));

                        colObjId = getStrValue(recId);

                        if (useNumericCatNumbers)
                        {
                            catalogNumber = String.format("%09d", rs.getInt(catNumInx + 1));

                        } else
                        {
                            float catNum = rs.getFloat(catNumInx + 1);
                            catalogNumber = (usePrefix && StringUtils.isNotEmpty(prefix) ? (prefix + "-")
                                    : "")
                                    + String.format("%9.0f", catNum).trim();
                        }

                        int subNumber = rs.getInt(oldNameIndex.get("SubNumber") + 1);
                        if (subNumber < 0)
                        {
                            skipRecord = true;
                            log
                                    .error("Collection Object is being skipped because SubNumber is less than zero CatalogNumber["
                                            + catalogNumber + "]");
                            break;
                        }

                    } else if (fieldsToSkipHash.get(newFieldName) != null)
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("CollectionID")) // User/Security changes
                    {
                        str.append(newCatSeriesId);

                    } else if (newFieldName.equals("Version")) // User/Security changes
                    {
                        str.append("0");

                    } else if (newFieldName.equals("CreatedByAgentID")) // User/Security changes
                    {
                        str.append(getCreatorAgentId(null));

                    } else if (newFieldName.equals("ModifiedByAgentID")) // User/Security changes
                    {
                        str.append(getModifiedByAgentId(null));

                    } else if (newFieldName.equals("CollectionMemberID")) // User/Security changes
                    {
                        str.append(getCollectionMemberId());

                    } else if (newFieldName.equals("PaleoContextID"))
                    {
                        str.append("NULL");// newCatSeriesId);

                    } else if (newFieldName.equals("CollectionObjectAttributesID")) // User/Security
                                                                                    // changes
                    {
                        Object idObj = rs.getObject(1);
                        if (idObj != null)
                        {
                            Integer newId = colObjAttrMapper.get(rs.getInt(1));
                            if (newId != null)
                            {
                                str.append(getStrValue(newId));
                            } else
                            {
                                colObjAttrsNotMapped++;
                                str.append("NULL");
                            }
                        } else
                        {
                            str.append("NULL");
                        }

                    } else if (newFieldName.equals("CatalogedDatePrecision"))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("Availability"))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("CatalogNumber"))
                    {
                        str.append(catalogNumber);

                    } else if (newFieldName.equals("Visibility")) // User/Security changes
                    {
                        str.append(defaultVisibilityLevel);

                    } else if (newFieldName.equals("CountAmt"))
                    {
                        Integer index = oldNameIndex.get("Count1");
                        if (index == null)
                        {
                            index = oldNameIndex.get("Count");
                        }
                        Object countObj = rs.getObject(index + 1);
                        if (countObj != null)
                        {
                            str.append(getStrValue(countObj, newFieldMetaData.get(i).getType()));
                        } else
                        {
                            str.append("NULL");
                        }

                    } else
                    {

                        Integer index = oldNameIndex.get(newFieldName);
                        if (index == null)
                        {
                            String msg = "Couldn't find new field name[" + newFieldName
                                    + "] in old field name in index Map";
                            log.error(msg);
                            // for (String key : oldNameIndex.keySet())
                            // {
                            // log.info("["+key+"]["+oldNameIndex.get(key)+"]");
                            // }
                            stmt.close();
                            throw new RuntimeException(msg);
                        }
                        Object data = rs.getObject(index + 1);

                        if (data != null)
                        {
                            int idInx = newFieldName.lastIndexOf("ID");
                            if (idMapperMgr != null && idInx > -1)
                            {
                                // System.out.println(newFieldName+" "+(index.intValue()+1)+"
                                // "+rs.getInt(index+1));

                                IdMapperIFace idMapper = idMapperMgr.get(tableName, newFieldName);
                                if (idMapper != null)
                                {
                                    data = idMapper.get(rs.getInt(index + 1));
                                } else
                                {
                                    log.error("No Map for [" + tableName + "][" + newFieldName
                                            + "]");
                                }
                            }
                        }
                        str.append(getStrValue(data, newFieldMetaData.get(i).getType()));

                    }

                }

                if (!skipRecord)
                {
                    str.append(")");
                    // log.info("\n"+str.toString());
                    if (hasFrame)
                    {
                        if (count % 100 == 0)
                        {
                            setProcess(count);
                        }
                        if (count % 5000 == 0)
                        {
                            log.info("CollectionObject Records: " + count);
                        }

                    } else
                    {
                        if (count % 2000 == 0)
                        {
                            log.info("CollectionObject Records: " + count);
                        }
                    }

                    try
                    {
                        Statement updateStatement = newDBConn.createStatement();
                        if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
                        {
                            BasicSQLUtils.removeForeignKeyConstraints(newDBConn,
                                    BasicSQLUtils.myDestinationServerType);
                        }
                        // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                        updateStatement.executeUpdate(str.toString());
                        updateStatement.clearBatch();
                        updateStatement.close();
                        updateStatement = null;

                    } catch (SQLException e)
                    {
                        log.error("Count: " + count);
                        log.error("Key: [" + colObjId + "][" + catalogNumber + "]");
                        log.error("SQL: " + str.toString());
                        e.printStackTrace();
                        log.error(e);
                        rs.close();
                        stmt.close();
                        throw new RuntimeException(e);
                    }

                    count++;
                }
                // if (count > 10) break;
            } while (rs.next());

            log.info("CollectionObjectAttributes not mapped: " + colObjAttrsNotMapped);

            stmt2.close();

            if (hasFrame)
            {
                setProcess(count);
            } else
            {
                log.info("Processed CollectionObject " + count + " records.");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e)
        {
            BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "collectionobject",
                    BasicSQLUtils.myDestinationServerType);
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }
        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "collectionobject",
                BasicSQLUtils.myDestinationServerType);
        return true;
    }

    /**
     * @param rs
     * @param columnIndex
     * @return
     */
    protected int getIntValue(final ResultSet rs, final int columnIndex)
    {
        try
        {
            int val = rs.getInt(columnIndex);
            return rs.wasNull() ? 0 : val;

        } catch (Exception ex)
        {
            // TODO: what now?
        }
        return 0;
    }

    /**
     * Converts all the LoanPhysicalObjects.
     * @return true if no errors
     */
    public boolean convertLoanPreparations()
    {
        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "determination",
                BasicSQLUtils.myDestinationServerType);
        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "loanpreparation",
                BasicSQLUtils.myDestinationServerType);
        deleteAllRecordsFromTable(newDBConn, "loanpreparation",
                BasicSQLUtils.myDestinationServerType); // automatically closes the connection

        if (BasicSQLUtils.getNumRecords(oldDBConn, "loanphysicalobject") == 0)
        {
            BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "loanpreparation",
                    BasicSQLUtils.myDestinationServerType);
            return true;
        }

        try
        {
            // MEG ADDED????
            // Map all the Physical IDs
            IdTableMapper idMapper2 = idMapperMgr.addTableMapper("preparation", "PreparationID");
            if (shouldCreateMapTables)
            {
                idMapper2
                        .mapAllIds("select CollectionObjectID from collectionobject Where not (collectionobject.DerivedFromID Is Null) order by CollectionObjectID");
            }

            Map<String, String> colNewToOldMap = createFieldNameMap(new String[] { "PreparationID",
                    "PhysicalObjectID" });

            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            StringBuilder str = new StringBuilder();

            List<String> oldFieldNames = new ArrayList<String>();

            StringBuilder sql = new StringBuilder("SELECT ");
            List<String> names = new ArrayList<String>();
            getFieldNamesFromSchema(oldDBConn, "loanphysicalobject", names,
                    BasicSQLUtils.mySourceServerType);

            sql.append(buildSelectFieldList(names, "loanphysicalobject"));
            oldFieldNames.addAll(names);

            sql.append(" FROM loanphysicalobject");

            log.info(sql);

            List<BasicSQLUtils.FieldMetaData> newFieldMetaData = new ArrayList<BasicSQLUtils.FieldMetaData>();
            getFieldMetaDataFromSchema(newDBConn, "loanpreparation", newFieldMetaData,
                    BasicSQLUtils.myDestinationServerType);

            log.info("Number of Fields in New loanpreparation " + newFieldMetaData.size());
            String sqlStr = sql.toString();

            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 0;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
            }

            String tableName = "loanphysicalobject";

            int quantityIndex = oldNameIndex.get("Quantity") + 1;
            int quantityReturnedIndex = oldNameIndex.get("QuantityReturned") + 1;
            int lastEditedByInx = oldNameIndex.get("LastEditedBy") + 1;
            // int quantityResolvedIndex = oldNameIndex.get("QuantityResolved") + 1;

            log.info(sqlStr);
            ResultSet rs = stmt.executeQuery(sqlStr);

            if (hasFrame)
            {
                if (rs.last())
                {
                    setProcess(0, rs.getRow());
                    rs.first();

                } else
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            } else
            {
                if (!rs.first())
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            }

            int count = 0;
            do
            {
                int quantity = getIntValue(rs, quantityIndex);
                int quantityReturned = getIntValue(rs, quantityReturnedIndex);
                // int quantityResolved = getIntValue(rs, quantityResolvedIndex);
                Boolean isResolved = quantityReturned == quantity;
                String lastEditedBy = rs.getString(lastEditedByInx);

                str.setLength(0);
                StringBuffer fieldList = new StringBuffer();
                fieldList.append("( ");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if ((i > 0) && (i < newFieldMetaData.size()))
                    {
                        fieldList.append(", ");
                    }
                    String newFieldName = newFieldMetaData.get(i).getName();
                    fieldList.append(newFieldName + " ");
                }
                fieldList.append(")");
                str.append("INSERT INTO loanpreparation " + fieldList + " VALUES (");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if (i > 0)
                        str.append(", ");

                    String newFieldName = newFieldMetaData.get(i).getName();

                    if (i == 0)
                    {
                        Integer recId = count + 1;
                        str.append(getStrValue(recId));

                    } else if (newFieldName.equals("ReceivedComments"))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("IsResolved"))
                    {
                        str.append(BasicSQLUtils.getStrValue(isResolved));

                    } else if (newFieldName.equalsIgnoreCase("Version"))
                    {
                        str.append("0");

                    } else if (newFieldName.equalsIgnoreCase("CollectionMemberID"))
                    {
                        str.append(getCollectionMemberId());

                    } else if (newFieldName.equalsIgnoreCase("ModifiedByAgentID"))
                    {
                        str.append(getModifiedByAgentId(lastEditedBy));

                    } else if (newFieldName.equalsIgnoreCase("CreatedByAgentID"))
                    {
                        str.append(getCreatorAgentId(null));

                    } else
                    {
                        Integer index = null;
                        String oldMappedColName = colNewToOldMap.get(newFieldName);
                        if (oldMappedColName != null)
                        {
                            index = oldNameIndex.get(oldMappedColName);

                        } else
                        {
                            index = oldNameIndex.get(newFieldName);
                            oldMappedColName = newFieldName;
                        }

                        if (index == null)
                        {
                            String msg = "Couldn't find new field name[" + newFieldName
                                    + "] in old field name in index Map";
                            log.error(msg);
                            stmt.close();
                            throw new RuntimeException(msg);
                        }
                        Object data = rs.getObject(index + 1);

                        if (data != null)
                        {
                            int idInx = newFieldName.lastIndexOf("ID");
                            if (idMapperMgr != null && idInx > -1)
                            {
                                IdMapperIFace idMapper = idMapperMgr.get(tableName,
                                        oldMappedColName);
                                if (idMapper != null)
                                {
                                    data = idMapper.get(rs.getInt(index + 1));
                                } else
                                {
                                    log.error("No Map for [" + tableName + "][" + oldMappedColName
                                            + "]");
                                }
                            }
                        }
                        str.append(getStrValue(data, newFieldMetaData.get(i).getType()));
                    }
                }
                str.append(")");

                if (hasFrame)
                {
                    if (count % 100 == 0)
                    {
                        setProcess(count);
                    }

                } else
                {
                    if (count % 2000 == 0)
                    {
                        log.info("LoanPreparation Records: " + count);
                    }
                }

                try
                {
                    Statement updateStatement = newDBConn.createStatement();
                    if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
                    {
                        BasicSQLUtils.removeForeignKeyConstraints(newDBConn,
                                BasicSQLUtils.myDestinationServerType);
                    }
                    // log.debug("executring: " + str.toString());
                    // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                    updateStatement.executeUpdate(str.toString());
                    updateStatement.clearBatch();
                    updateStatement.close();
                    updateStatement = null;

                } catch (SQLException e)
                {
                    log.error("Count: " + count);
                    e.printStackTrace();
                    log.error(e);
                    rs.close();
                    stmt.close();
                    throw new RuntimeException(e);
                }

                count++;
                // if (count > 10) break;
            } while (rs.next());

            if (hasFrame)
            {
                setProcess(count);
                log.info("Processed LoanPreparation " + count + " records.");
            } else
            {
                log.info("Processed LoanPreparation " + count + " records.");
            }
            rs.close();

            stmt.close();

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "LoanPreparation",
                    BasicSQLUtils.myDestinationServerType);
            throw new RuntimeException(e);
        }
        log.info("Done processing LoanPhysicalObject");
        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "LoanPreparation",
                BasicSQLUtils.myDestinationServerType);
        return true;

    }

    /**
     * Creates a Standard set of DataTypes for Collections
     * @param returnName the name of a DataType to return (ok if null)
     * @return the DataType requested
     */
    public DataType createDataTypes(final String returnName)
    {
        String[] dataTypeNames = { "Animal", "Plant", "Fungi", "Mineral", "Other" };

        DataType retDataType = null;
        try
        {
            Session localSession = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            for (String name : dataTypeNames)
            {
                DataType dataType = new DataType();
                dataType.setName(name);
                dataType.setDiscipline(null);
                localSession.save(dataType);

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
            throw new RuntimeException(e);
        }
        return retDataType;
    }

    /**
     * @param name name
     * @param dataType dataType
     * @param user user
     * @param taxaTreeDef taxaTreeDef
     * @param collection collection
     * @return set of objects
     */
    public Discipline createDiscipline(final String       name,
                                               final DataType     dataType,
                                               final Agent        userAgent,
                                               final TaxonTreeDef taxaTreeDef,
                                               final Collection   collection)
    {
        try
        {
            Set<Collection> collectionSet = new HashSet<Collection>();
            if (collection != null)
            {
                collectionSet.add(collection);
            }

            Session localSession = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            Discipline discipline = new Discipline();
            discipline.initialize();
            discipline.setName(name);
            discipline.setDataType(dataType);

            discipline.setTaxonTreeDef(taxaTreeDef);

            discipline.setCollections(collectionSet);

            localSession.save(discipline);

            discipline.addReference(userAgent, "agents");

            HibernateUtil.commitTransaction();

            return discipline;

        } catch (Exception e)
        {
            log.error("******* " + e);
            e.printStackTrace();
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException(e);
        }
    }

    /**
     * @throws SQLException
     */
    public void convertAllTaxonTreeDefs() throws SQLException
    {
        Statement st = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);

        TaxonTreeDef ttd = new TaxonTreeDef();
        ttd.initialize();

        ResultSet rs = st.executeQuery("SELECT TaxonomyTypeID FROM taxonomytype");
        Vector<Integer> ttIds = new Vector<Integer>();
        while (rs.next())
        {
            ttIds.add(rs.getInt(1));
        }

        for (Integer id : ttIds)
        {
            convertTaxonTreeDefinition(id);
        }
    }

    /**
     * Converts the taxonomy tree definition from the old taxonomicunittype table to the new table
     * pair: TaxonTreeDef & TaxonTreeDefItems.
     * 
     * @param taxonomyTypeId the tree def id in taxonomicunittype
     * @return the TaxonTreeDef object
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public TaxonTreeDef convertTaxonTreeDefinition(int taxonomyTypeId) throws SQLException
    {
        Statement st = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);

        TaxonTreeDef ttd = new TaxonTreeDef();
        ttd.initialize();

        ResultSet rs = st
                .executeQuery("SELECT TaxonomyTypeName FROM taxonomytype WHERE TaxonomyTypeID="
                        + taxonomyTypeId);
        rs.next();
        String taxonomyTypeName = rs.getString(1);

        ttd.setName(taxonomyTypeName + " taxonomy tree");
        ttd.setRemarks("Tree converted from " + oldDBName);
        ttd.setFullNameDirection(TreeDefIface.FORWARD);

        rs = st
                .executeQuery("SELECT DISTINCT RankID,RankName,RequiredParentRankID FROM taxonomicunittype WHERE TaxonomyTypeID="
                        + taxonomyTypeId + " ORDER BY RankID");

        int rank;
        String name;
        int requiredRank;

        Vector<TaxonTreeDefItem> items = new Vector<TaxonTreeDefItem>();
        Vector<Integer> enforcedRanks = new Vector<Integer>();

        while (rs.next())
        {
            rank = rs.getInt(1);
            name = rs.getString(2);
            requiredRank = rs.getInt(3);
            System.out.println(rank + "  " + name);
            TaxonTreeDefItem i = new TaxonTreeDefItem();
            i.initialize();
            i.setName(name);
            i.setFullNameSeparator(" ");
            i.setRankId(rank);
            i.setTreeDef(ttd);
            ttd.getTreeDefItems().add(i);

            // setup the parent/child relationship
            if (items.isEmpty())
            {
                i.setParent(null);
            } else
            {
                i.setParent(items.lastElement());
            }
            items.add(i);

            enforcedRanks.add(requiredRank);
        }

        for (TaxonTreeDefItem i : items)
        {
            if (enforcedRanks.contains(i.getRankId()))
            {
                i.setIsEnforced(true);
            } else
            {
                i.setIsEnforced(false);
            }
        }

        return ttd;
    }

    /**
     * 
     */
    public void copyTaxonTreeDefs()
    {
        log.debug("copyTaxonTreeDefs");
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "taxontreedef",
                BasicSQLUtils.myDestinationServerType);
        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "taxontreedef",
                BasicSQLUtils.myDestinationServerType);
        // Meg had to removed the inner order by statement, becuase SQL Server does not allow order
        // bys in subqueries. it is assumed that this is okay because we are calling an
        // "in" function on the result of subquery so the order should not matter
        // String sql = "select * from taxonomytype where taxonomytype.TaxonomyTypeId in (SELECT
        // DISTINCT t.TaxonomyTypeId FROM taxonname t WHERE t.RankId<> 0 ORDER BY TaxonomyTypeId)";
        String sql = "select * from taxonomytype where taxonomytype.TaxonomyTypeId in (SELECT DISTINCT t.TaxonomyTypeId FROM taxonname t WHERE t.RankId<> 0)";
        log.debug("convertTaxonTreeDefItems - created sql string: " + sql);
        Hashtable<String, String> newToOldColMap = new Hashtable<String, String>();
        newToOldColMap.put("TaxonTreeDefID", "TaxonomyTypeID");
        newToOldColMap.put("Name", "TaxonomyTypeName");

        // since these columns don't exist in the old DB, setup some default values for them
        Map<String, String> timestampValues = new Hashtable<String, String>();
        timestampValues.put("TimestampCreated", "'" + nowStr + "'");
        timestampValues.put("TimestampModified", "'" + nowStr + "'");

        String[] ignoredFields = { "Remarks", "FullNameDirection", "LastEditedBy",
                "TimestampCreated", "TimestampModified", "CreatedByAgentID", "ModifiedByAgentID",
                "Version", "CollectionMemberID" };
        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);

        log.info("Copying taxonomy tree definitions from 'taxonomytype' table:" + sql);
        if (!copyTable(oldDBConn, newDBConn, sql, "taxonomytype", "taxontreedef", newToOldColMap,
                null, timestampValues, BasicSQLUtils.mySourceServerType,
                BasicSQLUtils.myDestinationServerType))
        {
            log.error("Table 'taxonomytype' didn't copy correctly");
        }

        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);
        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "taxontreedef",
                BasicSQLUtils.myDestinationServerType);
    }

    /**
     * Converts the old taxonomy records to the new schema. In general, the process is... 1. Copy
     * all columns that don't require any modification (other than col. name) 2. Set the proper
     * values in the IsEnforced column 3. Set the proper values in the ParentItemID column
     * 
     * @throws SQLException
     */
    public void convertTaxonTreeDefItems() throws SQLException
    {
        log.debug("convertTaxonTreeDefItems");
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "taxontreedefitem",
                BasicSQLUtils.myDestinationServerType);
        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "taxontreedefitem",
                BasicSQLUtils.myDestinationServerType);
        // Meg had to removed the inner order by statement, becuase SQL Server does not allow order
        // bys in subqueries. it is assumed that this is okay because we are calling an
        // "in" function on the result of subquery so the order should not matter
        // String sqlStr = "SELECT * FROM taxonomicunittype where taxonomicunittype.TaxonomyTypeID
        // in (SELECT DISTINCT t.TaxonomyTypeId FROM taxonname t WHERE t.RankId<> 0 ORDER BY
        // TaxonomyTypeId)";
        String sqlStr = "SELECT * FROM taxonomicunittype where taxonomicunittype.TaxonomyTypeID in (SELECT DISTINCT t.TaxonomyTypeId FROM taxonname t WHERE t.RankId<> 0)";
        log.debug("convertTaxonTreeDefItems - created sql string: " + sqlStr);
        Hashtable<String, String> newToOldColMap = new Hashtable<String, String>();
        newToOldColMap.put("TaxonTreeDefItemID", "TaxonomicUnitTypeID");
        newToOldColMap.put("Name", "RankName");
        newToOldColMap.put("TaxonTreeDefID", "TaxonomyTypeID");

        // since these columns don't exist in the old DB, setup some default values for them
        Map<String, String> timestampValues = new Hashtable<String, String>();
        timestampValues.put("TimestampCreated", "'" + nowStr + "'");
        timestampValues.put("TimestampModified", "'" + nowStr + "'");

        String[] ignoredFields = { "IsEnforced", "ParentItemID", "Remarks", "IsInFullName",
                "FullNameSeparator", "TextBefore", "TextAfter", "TimestampCreated",
                "TimestampModified", "LastEditedBy", "FormatToken", "CreatedByAgentID",
                "ModifiedByAgentID", "Version", "CollectionMemberID" };
        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);

        // Copy over most of the columns in the old table to the new one
        log.info("Copying taxonomy tree definition items from 'taxonomicunittype' table");
        if (!copyTable(oldDBConn, newDBConn, sqlStr, "taxonomicunittype", "taxontreedefitem",
                newToOldColMap, null, timestampValues, BasicSQLUtils.mySourceServerType,
                BasicSQLUtils.myDestinationServerType))
        {
            log.error("Table 'taxonomicunittype' didn't copy correctly");
        }

        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);

        // JDBC Statments for use throughout process
        Statement oldDbStmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        Statement newDbStmt = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);

        // Meg had to removed the inner order by statement, becuase SQL Server does not allow order
        // bys in subqueries. it is assumed that this is okay because we are calling an
        // "in" function on the result of subquery so the order should not matter
        // sqlStr = "SELECT DISTINCT TaxonomyTypeID from taxonomicunittype where
        // taxonomicunittype.TaxonomyTypeId in (SELECT DISTINCT t.TaxonomyTypeId FROM taxonname t
        // WHERE t.RankId<> 0 ORDER BY TaxonomyTypeId)";

        // get each individual TaxonomyTypeID value
        sqlStr = "SELECT DISTINCT TaxonomyTypeID from taxonomicunittype where taxonomicunittype.TaxonomyTypeId in (SELECT DISTINCT t.TaxonomyTypeId FROM taxonname t WHERE t.RankId<> 0)";

        ResultSet rs = oldDbStmt.executeQuery(sqlStr);

        Vector<Integer> typeIds = new Vector<Integer>();
        while (rs.next())
        {
            Integer typeId = rs.getInt(1);
            if (!rs.wasNull())
            {
                typeIds.add(typeId);
            }
        }

        // will be used to map old TaxonomyTypeID values to TreeDefID values
        IdMapperIFace typeIdMapper = idMapperMgr.get("taxonomytype", "TaxonomyTypeID");

        // for each value of TaxonomyType...
        for (Integer typeId : typeIds)
        {
            // get all of the values of RequiredParentRankID (the enforced ranks)
            sqlStr = "SELECT DISTINCT RequiredParentRankID from taxonomicunittype WHERE TaxonomyTypeID="
                    + typeId;
            rs = oldDbStmt.executeQuery(sqlStr);

            Vector<Integer> enforcedIds = new Vector<Integer>();
            while (rs.next())
            {
                Integer reqId = rs.getInt(1);
                if (!rs.wasNull())
                {
                    enforcedIds.add(reqId);
                }
            }
            // make sure the root item is always enforced
            if (!enforcedIds.contains(0))
            {
                enforcedIds.add(0);
            }
            // now we have a vector of the required/enforced rank IDs
            // fix the new DB values accordingly

            // what is the corresponding TreeDefID?
            int treeDefId = typeIdMapper.get(typeId);

            StringBuilder sqlUpdate = new StringBuilder(
                    "UPDATE taxontreedefitem SET IsEnforced=1 WHERE TaxonTreeDefID=" + treeDefId
                            + " AND RankID IN (");
            // add all the enforced ranks
            for (int i = 0; i < enforcedIds.size(); ++i)
            {
                sqlUpdate.append(enforcedIds.get(i));
                if (i < enforcedIds.size() - 1)
                {
                    sqlUpdate.append(",");
                }
            }
            sqlUpdate.append(")");

            log.info(sqlUpdate);

            int rowsUpdated = newDbStmt.executeUpdate(sqlUpdate.toString());
            log.info(rowsUpdated + " rows updated");

            StringBuilder fullNameUpdate = new StringBuilder(
                    "UPDATE taxontreedefitem SET IsInFullName=1 WHERE TaxonTreeDefID="
                            + treeDefId
                            + " AND Name IN ('Genus','Species','Subspecies','Variety','Subvariety','Forma','Subforma')");
            log.info(fullNameUpdate);

            rowsUpdated = newDbStmt.executeUpdate(fullNameUpdate.toString());
            log.info(fullNameUpdate);
        }

        // at this point, we've set all the IsEnforced fields that need to be TRUE
        // now we need to set the others to FALSE
        String setToFalse = "UPDATE taxontreedefitem SET IsEnforced=0 WHERE IsEnforced IS NULL";
        int rowsUpdated = newDbStmt.executeUpdate(setToFalse);
        log.info("IsEnforced set to FALSE in " + rowsUpdated + " rows");

        // we still need to fix the ParentItemID values to point at each row's parent

        // we'll work with the items in sets as determined by the TreeDefID
        for (Integer typeId : typeIds)
        {
            int treeDefId = typeIdMapper.get(typeId);
            sqlStr = "SELECT TaxonTreeDefItemID FROM taxontreedefitem WHERE TaxonTreeDefID="
                    + treeDefId + " ORDER BY RankID";
            rs = newDbStmt.executeQuery(sqlStr);

            boolean atLeastOneRecord = rs.next();
            if (!atLeastOneRecord)
            {
                continue;
            }
            int prevTreeDefItemId = rs.getInt(1);
            Vector<Pair<Integer, Integer>> idAndParentIdPairs = new Vector<Pair<Integer, Integer>>();
            while (rs.next())
            {
                int treeDefItemId = rs.getInt(1);
                idAndParentIdPairs
                        .add(new Pair<Integer, Integer>(treeDefItemId, prevTreeDefItemId));
                prevTreeDefItemId = treeDefItemId;
            }

            // now we have all the pairs (ID,ParentID) in a Vector of Pair objects
            rowsUpdated = 0;
            for (Pair<Integer, Integer> idPair : idAndParentIdPairs)
            {
                sqlStr = "UPDATE taxontreedefitem SET ParentItemID=" + idPair.second
                        + " WHERE TaxonTreeDefItemID=" + idPair.first;
                rowsUpdated += newDbStmt.executeUpdate(sqlStr);
            }

            log.info("Fixed parent pointers on " + rowsUpdated + " rows");
        }
        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "taxontreedefitem",
                BasicSQLUtils.myDestinationServerType);
    }

    /**
     * 
     */
    public void copyTaxonRecords()
    {
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "taxon",
                BasicSQLUtils.myDestinationServerType);
        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "taxon",
                BasicSQLUtils.myDestinationServerType);
        // Meg had to removed the inner order by statement, becuase SQL Server does not allow order
        // bys in subqueries. it is assumed that this is okay because we are calling an
        // "in" function on the result of subquery so the order should not matter
        // String sql = "SELECT * FROM taxonname where taxonname.TaxonomyTypeId in (SELECT DISTINCT
        // t.TaxonomyTypeId FROM taxonname t WHERE t.RankId <> 0 ORDER BY TaxonomyTypeId)";
        String sql = "SELECT * FROM taxonname where taxonname.TaxonomyTypeId in (SELECT DISTINCT t.TaxonomyTypeId FROM taxonname t WHERE t.RankId <> 0 )";

        Hashtable<String, String> newToOldColMap = new Hashtable<String, String>();
        newToOldColMap.put("TaxonID", "TaxonNameID");
        newToOldColMap.put("ParentID", "ParentTaxonNameID");
        newToOldColMap.put("TaxonTreeDefID", "TaxonomyTypeID");
        newToOldColMap.put("TaxonTreeDefItemID", "TaxonomicUnitTypeID");
        newToOldColMap.put("Name", "TaxonName");
        newToOldColMap.put("FullName", "FullTaxonName");
        newToOldColMap.put("IsAccepted", "Accepted");

        // Ignore new fields
        // These were added for supporting the new security model and hybrids
        String[] ignoredFields = { "GUID", "Visibility", "VisibilitySetBy", "IsHybrid",
                "HybridParent1ID", "HybridParent2ID", "EsaStatus", "CitesStatus", "UsfwsCode",
                "IsisNumber", "Text1", "Text2", "NcbiTaxonNumber", "Number1", "Number2",
                "CreatedByAgentID", "ModifiedByAgentID", "Version", };

        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);

        // AcceptedID is typically NULL unless they are using synonimies
        boolean showMappingErrors = false;
        try
        {

            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt
                    .executeQuery("SELECT count(AcceptedID) FROM taxonname where AcceptedID <> null");
            if (rs.first())
            {
                showMappingErrors = rs.getInt(1) > 0;
            }
            rs.close();
            stmt.close();

        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        int errorsToShow = (BasicSQLUtils.SHOW_NAME_MAPPING_ERROR | BasicSQLUtils.SHOW_VAL_MAPPING_ERROR);
        if (showMappingErrors)
        {
            errorsToShow = errorsToShow
                    | (BasicSQLUtils.SHOW_FK_LOOKUP | BasicSQLUtils.SHOW_NULL_FK);
        }
        BasicSQLUtils.setShowErrors(errorsToShow);

        log.info("Copying taxon records from 'taxonname' table");
        if (!copyTable(oldDBConn, newDBConn, sql, "taxonname", "taxon", newToOldColMap, null,
                BasicSQLUtils.mySourceServerType, BasicSQLUtils.myDestinationServerType))
        {
            log.error("Table 'taxonname' didn't copy correctly");
        }

        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);
        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "taxon",
                BasicSQLUtils.myDestinationServerType);
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public GeographyTreeDef createStandardGeographyDefinitionAndItems()
    {
        // empty out any pre-existing tree definitions
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "geographytreedef",
                BasicSQLUtils.myDestinationServerType);
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "geographytreedefitem",
                BasicSQLUtils.myDestinationServerType);

        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        GeographyTreeDef def = new GeographyTreeDef();
        def.initialize();
        def.setName("Default Geography Definition");
        def.setRemarks("A simple continent/country/state/county geography tree");
        def.setFullNameDirection(TreeDefIface.REVERSE);
        // session.save(def);

        GeographyTreeDefItem planet = new GeographyTreeDefItem();
        planet.initialize();
        planet.setName("Planet");
        planet.setRankId(0);
        planet.setIsEnforced(true);
        planet.setFullNameSeparator(", ");
        // session.save(planet);

        GeographyTreeDefItem cont = new GeographyTreeDefItem();
        cont.initialize();
        cont.setName("Continent");
        cont.setRankId(100);
        cont.setFullNameSeparator(", ");
        // session.save(cont);

        GeographyTreeDefItem country = new GeographyTreeDefItem();
        country.initialize();
        country.setName("Country");
        country.setRankId(200);
        country.setIsInFullName(true);
        country.setFullNameSeparator(", ");
        // session.save(country);

        GeographyTreeDefItem state = new GeographyTreeDefItem();
        state.initialize();
        state.setName("State");
        state.setRankId(300);
        state.setIsInFullName(true);
        state.setFullNameSeparator(", ");
        // session.save(state);

        GeographyTreeDefItem county = new GeographyTreeDefItem();
        county.initialize();
        county.setName("County");
        county.setRankId(400);
        county.setIsInFullName(true);
        county.setFullNameSeparator(", ");
        // session.save(county);

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

        Set defItems = def.getTreeDefItems();
        defItems.add(planet);
        defItems.add(cont);
        defItems.add(country);
        defItems.add(state);
        defItems.add(county);

        localSession.save(def);

        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();

        return def;
    }

    @SuppressWarnings("unchecked")
    public static LithoStratTreeDef createStandardLithoStratDefinitionAndItems(final Connection dbConn)
    {
        // empty out any pre-existing tree definitions
        BasicSQLUtils.deleteAllRecordsFromTable(dbConn, "lithostrattreedef",
                BasicSQLUtils.myDestinationServerType);
        BasicSQLUtils.deleteAllRecordsFromTable(dbConn, "lithostrattreedefitem",
                BasicSQLUtils.myDestinationServerType);

        Session localSession = HibernateUtil.getCurrentSession();

        HibernateUtil.beginTransaction();

        LithoStratTreeDef def = DataBuilder.createLithoStratTreeDef("Standard LithoStrat Tree");
        List<Object> defItemsAndRootNode = BuildSampleDatabase.createSimpleLithoStrat(def);

        localSession.save(def);

        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();

        return def;
    }

    /**
     * @return
     */
    public boolean convertDeaccessionCollectionObject()
    {
        BasicSQLUtils.deleteAllRecordsFromTable("deaccessionpreparation",
                BasicSQLUtils.myDestinationServerType);

        if (BasicSQLUtils.getNumRecords(oldDBConn, "deaccessioncollectionobject") == 0) { return true; }

        Map<String, String> colNewToOldMap = createFieldNameMap(new String[] { "PreparationID",
                "CollectionObjectID", "DeaccessionPreparationID", "DeaccessionCollectionObjectID" });

        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "deaccessionpreparation",
                BasicSQLUtils.myDestinationServerType);
        if (copyTable(oldDBConn, newDBConn, "deaccessioncollectionobject",
                "deaccessionpreparation", colNewToOldMap, null, BasicSQLUtils.mySourceServerType,
                BasicSQLUtils.myDestinationServerType))
        {
            log.info("deaccessionpreparation copied ok.");
        } else
        {
            log.error("problems coverting deaccessionpreparation");
        }
        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "deaccessionpreparation",
                BasicSQLUtils.myDestinationServerType);

        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);

        return true;
    }

    /**
     * @param fields
     * @param fieldName
     */
    protected void removeFieldsFromList(final List<String> fields, final String[] fieldNames)
    {
        for (String fieldName : fieldNames)
        {
            int fndInx = -1;
            int inx = 0;
            for (String fld : fields)
            {
                if (StringUtils.contains(fld, fieldName))
                {
                    fndInx = inx;
                }
                inx++;
            }

            if (fndInx != -1)
            {
                fields.remove(fndInx);
            }
        }
    }

    /**
     * @param tableName
     */
    protected void convertLocalityExtraInfo(final String tableName)
    {
        List<String> localityDetailNamesTmp = new ArrayList<String>();
        getFieldNamesFromSchema(newDBConn, tableName, localityDetailNamesTmp,
                BasicSQLUtils.myDestinationServerType);

        List<String> localityDetailNames = new ArrayList<String>();
        Hashtable<String, Boolean> nameHash = new Hashtable<String, Boolean>();

        for (String fieldName : localityDetailNamesTmp)
        {
            localityDetailNames.add(fieldName);
            nameHash.put(fieldName, true);
        }

        String fieldList = buildSelectFieldList(localityDetailNames, tableName);
        log.info(fieldList);

        IdMapperIFace locIdMapper = idMapperMgr.get("locality", "LocalityID");

        try
        {
            Hashtable<String, Boolean> usedFieldHash = new Hashtable<String, Boolean>();

            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt
                    .executeQuery("select count(LocalityID) from locality,geography where locality.GeographyID = geography.GeographyID");
            if (rs.next())
            {
                frame.setProcess(0, rs.getInt(1));
            }
            rs.close();

            rs = stmt
                    .executeQuery("select locality.*, geography.* from locality,geography where locality.GeographyID = geography.GeographyID");

            StringBuilder colSQL = new StringBuilder();
            StringBuilder valuesSQL = new StringBuilder();

            int rows = 0;
            while (rs.next())
            {
                usedFieldHash.clear();
                valuesSQL.setLength(0);

                boolean hasData = false;
                ResultSetMetaData metaData = rs.getMetaData();
                int cols = metaData.getColumnCount();
                for (int i = 1; i <= cols; i++)
                {
                    String colName = metaData.getColumnName(i);
                    if (nameHash.get(colName) == null || usedFieldHash.get(colName) != null)
                    {
                        if (rows == 0)
                        {
                            System.out.println("Skipping[" + colName + "]");
                        }
                        continue;
                    }

                    usedFieldHash.put(colName, true);

                    if (rows == 0)
                    {
                        if (colSQL.length() > 0)
                            colSQL.append(",");
                        colSQL.append(colName);
                    }

                    String value;
                    if (colName.equals("LocalityID"))
                    {
                        Integer newId = locIdMapper.get(rs.getInt(i));
                        if (newId != null)
                        {
                            value = Integer.toString(newId);
                        } else
                        {
                            log.error("Couldn't map LocalityId oldId[" + rs.getInt(i) + "]");
                            value = "NULL";
                        }

                    } else
                    {
                        Object obj = rs.getObject(i);
                        if (obj != null && !colName.equals("TimestampCreated")
                                && !colName.equals("TimestampModified"))
                        {
                            hasData = true;
                        }
                        value = BasicSQLUtils.getStrValue(obj);
                    }
                    // System.out.println(colName+" ["+value+"]");

                    if (valuesSQL.length() > 0)
                        valuesSQL.append(",");
                    valuesSQL.append(value);
                }

                if (hasData)
                {
                    String insertSQL = "INSERT INTO "
                            + tableName
                            + " ("
                            + colSQL.toString()
                            + ", Version, CreatedByAgentID, ModifiedByAgentID, CollectionMemberID) "
                            + " VALUES(" + valuesSQL.toString() + ", 0, " + getCreatorAgentId(null)
                            + "," + getModifiedByAgentId(null) + "," + getCollectionMemberId()
                            + ")";

                    Statement updateStatement = newDBConn.createStatement();
                    BasicSQLUtils.removeForeignKeyConstraints(newDBConn,
                            BasicSQLUtils.myDestinationServerType);
                    if (false)
                    {
                        log.info(insertSQL);
                    }
                    try
                    {
                        updateStatement.executeUpdate(insertSQL);
                        updateStatement.clearBatch();
                        updateStatement.close();
                        updateStatement = null;

                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                rows++;
                frame.setProcess(rows);
            }

            rs.close();
            stmt.close();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    /**
     * 
     */
    public void convertLocality()
    {
        int errorsToShow = BasicSQLUtils.SHOW_ALL;
        log.debug("Preparing to convert localities");
        // Ignore these field names from new table schema when mapping IDs

        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "collectionobject",
                BasicSQLUtils.myDestinationServerType);

        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "locality",
                BasicSQLUtils.myDestinationServerType);

        BasicSQLUtils.deleteAllRecordsFromTable("locality", BasicSQLUtils.myDestinationServerType);

        String sql = "select locality.*, geography.* from locality,geography where locality.GeographyID = geography.GeographyID";

        String[] fieldsToIgnore = new String[] { "GML", "NamedPlaceExtent", "GeoRefAccuracyUnits",
                "GeoRefDetRef", "GeoRefDetDate", "GeoRefDetBy", "NoGeoRefBecause", "GeoRefRemarks",
                "GeoRefVerificationStatus", "NationalParkName", "Visibility", "VisibilitySetBy",
                "GeoRefDetByID",
                "Drainage", // TODO make sure this is right, meg added due to conversion non-mapping
                            // errors????
                "Island",// TODO make sure this is right, meg added due to conversion non-mapping
                            // errors????
                "IslandGroup",// TODO make sure this is right, meg added due to conversion
                                // non-mapping errors????
                "WaterBody",// TODO make sure this is right, meg added due to conversion non-mapping
                            // errors????
                "Version", "CreatedByAgentID", "ModifiedByAgentID", "CollectionMemberID",
                "ShortName", };
        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(fieldsToIgnore);

        errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for LocalityID
        BasicSQLUtils.setShowErrors(errorsToShow);

        if (copyTable(oldDBConn, newDBConn, sql, "locality", "locality", null, null,
                BasicSQLUtils.mySourceServerType, BasicSQLUtils.myDestinationServerType))
        {
            log.info("Locality/Geography copied ok.");
        } else
        {
            log.error("Copying locality/geography (fields) to new Locality");
        }

        convertLocalityExtraInfo("localitydetail");
        convertLocalityExtraInfo("geocoorddetail");

        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);
        BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "locality",
                BasicSQLUtils.myDestinationServerType);

    }

    /**
     * @param treeDef
     * @throws SQLException
     */
    public void convertGeography(GeographyTreeDef treeDef) throws SQLException
    {
        // empty out any pre-existing records
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "geography",
                BasicSQLUtils.myDestinationServerType);

        // create an ID mapper for the geography table (mainly for use in converting localities)
        IdTableMapper geoIdMapper = IdMapperMgr.getInstance().addTableMapper("geography",
                "GeographyID");
        Hashtable<Integer, Geography> oldIdToGeoMap = new Hashtable<Integer, Geography>();

        // get a Hibernate session for saving the new records
        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // get all of the old records
        String sql = "SELECT GeographyID,ContinentOrOcean,Country,State,County FROM geography";
        Statement statement = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        ResultSet oldGeoRecords = statement.executeQuery(sql);

        if (hasFrame)
        {
            if (oldGeoRecords.last())
            {
                setProcess(0, oldGeoRecords.getRow());
                oldGeoRecords.first();
            }
        } else
        {
            oldGeoRecords.first();
        }

        // setup the root Geography record (planet Earth)
        Geography planetEarth = new Geography();
        planetEarth.initialize();
        planetEarth.setName("Earth");
        planetEarth.setCommonName("Earth");
        planetEarth.setRankId(0);
        planetEarth.setDefinition(treeDef);
        for (GeographyTreeDefItem defItem : treeDef.getTreeDefItems())
        {
            if (defItem.getRankId() == 0)
            {
                planetEarth.setDefinitionItem(defItem);
                break;
            }
        }
        GeographyTreeDefItem defItem = treeDef.getDefItemByRank(0);
        planetEarth.setDefinitionItem(defItem);

        int counter = 0;
        // for each old record, convert the record
        do
        {
            if (counter % 100 == 0)
            {
                if (hasFrame)
                {
                    setProcess(counter);

                } else
                {
                    log.info("Converted " + counter + " geography records");
                }
            }

            // if (oldGeoRecords.isBeforeFirst())
            // {
            // grab the important data fields from the old record
            int oldId = oldGeoRecords.getInt(1);
            String cont = oldGeoRecords.getString(2);
            String country = oldGeoRecords.getString(3);
            String state = oldGeoRecords.getString(4);
            String county = oldGeoRecords.getString(5);

            // create a new Geography object from the old data
            List<Geography> newGeos = convertOldGeoRecord(cont, country, state, county, planetEarth);
            if (newGeos.size() > 0)
            {
                Geography lowestLevel = newGeos.get(newGeos.size() - 1);

                oldIdToGeoMap.put(oldId, lowestLevel);
            }
            // }
            counter++;
        } while (oldGeoRecords.next());

        if (hasFrame)
        {
            setProcess(counter);

        } else
        {
            log.info("Converted " + counter + " geography records");
        }

        TreeHelper.fixFullnameForNodeAndDescendants(planetEarth);
        planetEarth.setNodeNumber(1);
        fixNodeNumbersFromRoot(planetEarth);
        localSession.save(planetEarth);

        HibernateUtil.commitTransaction();
        log.info("Converted " + counter + " geography records");

        if (shouldCreateMapTables)
        {
            // add all of the ID mappings
            for (Integer oldId : oldIdToGeoMap.keySet())
            {
                Geography geo = oldIdToGeoMap.get(oldId);
                geoIdMapper.put(oldId, geo.getId());
            }
        }

        // set up Geography foreign key mapping for locality
        idMapperMgr.mapForeignKey("Locality", "GeographyID", "Geography", "GeographyID");
    }

    /**
     * Using the data passed in the parameters, create a new Geography object and attach it to the
     * Geography tree rooted at geoRoot.
     * 
     * @param cont continent or ocean name
     * @param country country name
     * @param state state name
     * @param county county name
     * @param geoRoot the Geography tree root node (planet)
     * @return a list of Geography items created, the lowest level being the last item in the list
     */
    protected List<Geography> convertOldGeoRecord(String cont,
                                                  String country,
                                                  String state,
                                                  String county,
                                                  Geography geoRoot)
    {
        List<Geography> newRecords = new Vector<Geography>();
        String levelNames[] = { cont, country, state, county };
        int levelsToBuild = 0;
        for (int i = 4; i > 0; --i)
        {
            if (levelNames[i - 1] != null)
            {
                levelsToBuild = i;
                break;
            }
        }

        Geography prevLevelGeo = geoRoot;
        for (int i = 0; i < levelsToBuild; ++i)
        {
            Geography newLevelGeo = buildGeoLevel(levelNames[i], prevLevelGeo);
            newRecords.add(newLevelGeo);
            prevLevelGeo = newLevelGeo;
        }

        return newRecords;
    }

    /**
     * @param nameArg
     * @param parentArg
     * @param sessionArg
     * @return
     */
    protected Geography buildGeoLevel(String nameArg, Geography parentArg)
    {
        String name = nameArg;
        if (name == null)
        {
            name = "N/A";
        }

        // search through all of parent's children to see if one already exists with the same name
        Set<Geography> children = parentArg.getChildren();
        for (Geography child : children)
        {
            if (name.equalsIgnoreCase(child.getName()))
            {
                // this parent already has a child by the given name
                // don't create a new one, just return this one
                return child;
            }
        }

        // we didn't find a child by the given name
        // we need to create a new Geography record
        Geography newGeo = new Geography();
        newGeo.initialize();
        newGeo.setName(name);
        newGeo.setParent(parentArg);
        parentArg.addChild(newGeo);
        newGeo.setDefinition(parentArg.getDefinition());
        int newGeoRank = parentArg.getRankId() + 100;
        GeographyTreeDefItem defItem = parentArg.getDefinition().getDefItemByRank(newGeoRank);
        newGeo.setDefinitionItem(defItem);
        newGeo.setRankId(newGeoRank);

        return newGeo;
    }

    /**
     * @param treeDef
     * @throws SQLException
     */
    public void convertLithoStrat(LithoStratTreeDef treeDef) throws SQLException
    {
        // empty out any pre-existing records
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "lithostrat",
                BasicSQLUtils.myDestinationServerType);

        // get a Hibernate session for saving the new records
        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // get all of the old records
        String sql = "SELECT StratigraphyID, SuperGroup, LithoGroup, Formation, Member, Bed FROM stratigraphy";
        Statement statement = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        ResultSet oldStratRecords = statement.executeQuery(sql);

        if (hasFrame)
        {
            if (oldStratRecords.last())
            {
                setProcess(0, oldStratRecords.getRow());
                oldStratRecords.first();
            }
        } else
        {
            oldStratRecords.first();
        }

        // setup the root Geography record (planet Earth)
        LithoStrat earth = new LithoStrat();
        earth.initialize();
        earth.setName("Earth");
        earth.setRankId(0);
        earth.setDefinition(treeDef);
        for (Object o : treeDef.getTreeDefItems())
        {
            LithoStratTreeDefItem defItem = (LithoStratTreeDefItem)o;
            if (defItem.getRankId() == 0)
            {
                earth.setDefinitionItem(defItem);
                break;
            }
        }
        LithoStratTreeDefItem defItem = treeDef.getDefItemByRank(0);
        earth.setDefinitionItem(defItem);
        localSession.save(earth);

        // create an ID mapper for the geography table (mainly for use in converting localities)
        IdTableMapper lithoStratIdMapper = IdMapperMgr.getInstance().addTableMapper("lithostrat",
                "LithoStratID");

        int counter = 0;
        // for each old record, convert the record
        do
        {
            if (counter % 100 == 0)
            {
                if (hasFrame)
                {
                    setProcess(counter);

                } else
                {
                    log.info("Converted " + counter + " Stratigraphy records");
                }
            }

            // grab the important data fields from the old record
            int oldId = oldStratRecords.getInt(1);
            String superGroup = oldStratRecords.getString(2);
            String lithoGroup = oldStratRecords.getString(3);
            String formation = oldStratRecords.getString(4);
            String member = oldStratRecords.getString(5);
            String bed = oldStratRecords.getString(6);

            // create a new Geography object from the old data
            LithoStrat newStrat = convertOldStratRecord(superGroup, lithoGroup, formation, member,
                    bed, earth, localSession);

            counter++;

            // add this new ID to the ID mapper
            if (shouldCreateMapTables)
            {
                lithoStratIdMapper.put(oldId, newStrat.getLithoStratId());
            }

        } while (oldStratRecords.next());

        if (hasFrame)
        {
            setProcess(counter);

        } else
        {
            log.info("Converted " + counter + " Stratigraphy records");
        }

        TreeHelper.fixFullnameForNodeAndDescendants(earth);
        earth.setNodeNumber(1);
        fixNodeNumbersFromRoot(earth);

        HibernateUtil.commitTransaction();
        log.info("Converted " + counter + " Stratigraphy records");

        // set up Geography foreign key mapping for locality
        idMapperMgr.mapForeignKey("Locality", "StratigraphyID", "LithoStrat", "LithoStratID");
    }

    protected Hashtable<String, LithoStrat> lithoStratHash = new Hashtable<String, LithoStrat>();

    /**
     * @param treeDef
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public LithoStrat convertLithoStratFromCSV(LithoStratTreeDef treeDef, final boolean doSave)
            throws SQLException
    {
        lithoStratHash.clear();

        File file = new File("Stratigraphy.csv");
        if (!file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "]");
            return null;
        }

        // empty out any pre-existing records
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "lithostrat",
                BasicSQLUtils.myDestinationServerType);

        // get a Hibernate session for saving the new records
        Session localSession = doSave ? HibernateUtil.getCurrentSession() : null;
        if (localSession != null)
        {
            HibernateUtil.beginTransaction();
        }

        List<String> lines = null;
        try
        {
            lines = FileUtils.readLines(file);

        } catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }

        // setup the root Geography record (planet Earth)
        LithoStrat earth = new LithoStrat();
        earth.initialize();
        earth.setName("Earth");
        earth.setRankId(0);
        earth.setDefinition(treeDef);
        for (Object o : treeDef.getTreeDefItems())
        {
            LithoStratTreeDefItem defItem = (LithoStratTreeDefItem)o;
            if (defItem.getRankId() == 0)
            {
                earth.setDefinitionItem(defItem);
                break;
            }
        }
        LithoStratTreeDefItem defItem = treeDef.getDefItemByRank(0);
        earth.setDefinitionItem(defItem);
        if (doSave)
        {
            localSession.save(earth);
        }

        // create an ID mapper for the geography table (mainly for use in converting localities)
        IdTableMapper lithoStratIdMapper = doSave ? IdMapperMgr.getInstance().addTableMapper(
                "lithostrat", "LithoStratID") : null;

        int counter = 0;
        // for each old record, convert the record
        for (String line : lines)
        {
            if (counter == 0)
            {
                counter = 1;
                continue; // skip header line
            }

            if (counter % 500 == 0)
            {
                if (hasFrame)
                {
                    setProcess(counter);

                } else
                {
                    log.info("Converted " + counter + " Stratigraphy records");
                }
            }

            String[] columns = StringUtils.splitPreserveAllTokens(line, ',');
            if (columns.length < 7)
            {
                log.error("Skipping[" + line + "]");
                continue;
            }

            // grab the important data fields from the old record
            int oldId = Integer.parseInt(columns[0]);
            String superGroup = columns[2];
            String lithoGroup = columns[3];
            String formation = columns[4];
            String member = columns[5];
            String bed = columns[6];

            // create a new Geography object from the old data
            LithoStrat newStrat = convertOldStratRecord(superGroup, lithoGroup, formation, member,
                    bed, earth, localSession);

            counter++;

            // add this new ID to the ID mapper
            if (shouldCreateMapTables && lithoStratIdMapper != null)
            {
                lithoStratIdMapper.put(oldId, newStrat.getLithoStratId());
            }

        }

        if (hasFrame)
        {
            setProcess(counter);

        } else
        {
            log.info("Converted " + counter + " Stratigraphy records");
        }

        TreeHelper.fixFullnameForNodeAndDescendants(earth);
        earth.setNodeNumber(1);
        fixNodeNumbersFromRoot(earth);

        if (doSave)
        {
            HibernateUtil.commitTransaction();
        }
        log.info("Converted " + counter + " Stratigraphy records");

        // set up Geography foreign key mapping for locality
        if (doSave)
        {
            idMapperMgr.mapForeignKey("Locality", "StratigraphyID", "LithoStrat", "LithoStratID");
        }

        lithoStratHash.clear();

        return earth;
    }

    protected LithoStrat buildLithoStratLevel(String nameArg,
                                              LithoStrat parentArg,
                                              Session sessionArg)
    {
        String name = nameArg;
        if (name == null)
        {
            name = "N/A";
        }

        // search through all of parent's children to see if one already exists with the same name
        Set<LithoStrat> children = parentArg.getChildren();
        for (LithoStrat child : children)
        {
            if (name.equalsIgnoreCase(child.getName()))
            {
                // this parent already has a child by the given name
                // don't create a new one, just return this one
                return child;
            }
        }

        // we didn't find a child by the given name
        // we need to create a new Geography record
        LithoStrat newStrat = new LithoStrat();
        newStrat.initialize();
        newStrat.setName(name);
        newStrat.setParent(parentArg);
        parentArg.addChild(newStrat);
        newStrat.setDefinition(parentArg.getDefinition());
        int newGeoRank = parentArg.getRankId() + 100;
        LithoStratTreeDefItem defItem = parentArg.getDefinition().getDefItemByRank(newGeoRank);
        newStrat.setDefinitionItem(defItem);
        newStrat.setRankId(newGeoRank);

        if (sessionArg != null)
        {
            sessionArg.save(newStrat);
        }

        return newStrat;
    }

    protected LithoStrat convertOldStratRecord(String superGroup,
                                               String lithoGroup,
                                               String formation,
                                               String member,
                                               String bed,
                                               LithoStrat stratRoot,
                                               Session localSession)
    {
        String levelNames[] = { superGroup, lithoGroup, formation, member, bed };
        int levelsToBuild = 0;
        for (int i = levelNames.length; i > 0; --i)
        {
            if (StringUtils.isNotEmpty(levelNames[i - 1]))
            {
                levelsToBuild = i;
                break;
            }
        }

        for (int i = 0; i < levelsToBuild; i++)
        {
            if (StringUtils.isEmpty(levelNames[i]))
            {
                levelNames[i] = "(Empty)";
            }
        }

        LithoStrat prevLevelGeo = stratRoot;
        for (int i = 0; i < levelsToBuild; ++i)
        {
            // LithoStrat strat = lithoStratHash.get(levelNames[i]);
            // if (strat == null)
            // {
            LithoStrat newLevelStrat = buildLithoStratLevel(levelNames[i], prevLevelGeo,
                    localSession);
            prevLevelGeo = newLevelStrat;
            // lithoStratHash.put(levelNames[i], newLevelStrat);
            // }
        }

        return prevLevelGeo;
    }

    public StorageTreeDef buildSampleStorageTreeDef()
    {
        // empty out any pre-existing tree definitions
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "locationtreedef",
                BasicSQLUtils.myDestinationServerType);
        BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "locationtreedefitem",
                BasicSQLUtils.myDestinationServerType);

        log.info("Creating a sample storage tree definition");

        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        StorageTreeDef locDef = TreeFactory.createStdStorageTreeDef("Sample storage tree", null);
        locDef
                .setRemarks("This definition is merely for demonstration purposes.  Consult documentation or support staff for instructions on creating one tailored for an institutions specific needs.");
        locDef.setFullNameDirection(TreeDefIface.FORWARD);
        localSession.save(locDef);

        // get the root def item
        StorageTreeDefItem rootItem = locDef.getTreeDefItems().iterator().next();
        rootItem.setFullNameSeparator(", ");
        localSession.save(rootItem);

        Storage rootNode = rootItem.getTreeEntries().iterator().next();
        localSession.save(rootNode);

        StorageTreeDefItem building = new StorageTreeDefItem();
        building.initialize();
        building.setRankId(100);
        building.setName("Building");
        building.setIsEnforced(false);
        building.setIsInFullName(false);
        building.setTreeDef(locDef);
        building.setFullNameSeparator(", ");
        localSession.save(building);

        StorageTreeDefItem room = new StorageTreeDefItem();
        room.initialize();
        room.setRankId(200);
        room.setName("Room");
        room.setIsEnforced(true);
        room.setIsInFullName(true);
        room.setTreeDef(locDef);
        room.setFullNameSeparator(", ");
        localSession.save(room);

        StorageTreeDefItem freezer = new StorageTreeDefItem();
        freezer.initialize();
        freezer.setRankId(300);
        freezer.setName("Freezer");
        freezer.setIsEnforced(true);
        freezer.setIsInFullName(true);
        freezer.setTreeDef(locDef);
        freezer.setFullNameSeparator(", ");
        localSession.save(freezer);

        rootItem.setChild(building);
        building.setParent(rootItem);
        building.setChild(room);
        room.setParent(building);
        room.setChild(freezer);
        freezer.setParent(room);

        locDef.addTreeDefItem(building);
        locDef.addTreeDefItem(room);
        locDef.addTreeDefItem(freezer);

        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();

        return locDef;
    }

    /**
     * Walks the old GTP records and creates a GTP tree def and items based on the ranks and rank
     * names found in the old records
     * 
     * @return the new tree def
     * @throws SQLException on any error while contacting the old database
     */
    public GeologicTimePeriodTreeDef convertGTPDefAndItems() throws SQLException
    {
        BasicSQLUtils.deleteAllRecordsFromTable("geologictimeperiodtreedef",
                BasicSQLUtils.myDestinationServerType);
        BasicSQLUtils.deleteAllRecordsFromTable("geologictimeperiodtreedefitem",
                BasicSQLUtils.myDestinationServerType);

        log.info("Inferring geologic time period definition from old records");
        int count = 0;

        // get all of the old records
        String sql = "SELECT RankCode, RankName from geologictimeperiod";
        Statement statement = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        ResultSet oldGtpRecords = statement.executeQuery(sql);

        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        GeologicTimePeriodTreeDef def = new GeologicTimePeriodTreeDef();
        def.initialize();
        def.setName("Inferred Geologic Time Period Definition");
        def.setRemarks("");
        def.setFullNameDirection(TreeDefIface.REVERSE);
        localSession.save(def);

        Vector<GeologicTimePeriodTreeDefItem> newItems = new Vector<GeologicTimePeriodTreeDefItem>();

        GeologicTimePeriodTreeDefItem rootItem = addGtpDefItem(0, "Time Root", def);
        rootItem.setIsEnforced(true);
        rootItem.setIsInFullName(false);
        rootItem.setFullNameSeparator(", ");
        localSession.save(rootItem);
        newItems.add(rootItem);
        ++count;

        while (oldGtpRecords.next())
        {
            // we're modifying the rank since the originals were 1,2,3,...
            // to make them 100, 200, 300, ... (more like the other trees)
            Integer rankCode = oldGtpRecords.getInt(1) * 100;
            String rankName = oldGtpRecords.getString(2);
            GeologicTimePeriodTreeDefItem newItem = addGtpDefItem(rankCode, rankName, def);
            if (newItem != null)
            {
                newItem.setFullNameSeparator(", ");
                localSession.save(newItem);
                newItems.add(newItem);
            }
            if (++count % 1000 == 0)
            {
                log.info(count + " geologic time period records processed");
            }
        }

        // sort the vector to put them in parent/child order
        Comparator<GeologicTimePeriodTreeDefItem> itemComparator = new Comparator<GeologicTimePeriodTreeDefItem>()
        {
            public int compare(GeologicTimePeriodTreeDefItem o1, GeologicTimePeriodTreeDefItem o2)
            {
                return o1.getRankId().compareTo(o2.getRankId());
            }
        };
        Collections.sort(newItems, itemComparator);

        // set the parent/child pointers
        for (int i = 0; i < newItems.size() - 1; ++i)
        {
            newItems.get(i).setChild(newItems.get(i + 1));
        }

        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();

        log.info("Finished inferring GTP tree definition and items");
        return def;
    }

    /**
     * Creates a new GTP def item if one with the same rank doesn't exist.
     * 
     * @param rankCode the rank of the new item
     * @param rankName the name of the new item
     * @param def the def to which the item is attached
     * @return the new item, or null if one already exists with this rank
     */
    protected GeologicTimePeriodTreeDefItem addGtpDefItem(Integer rankCode,
                                                          String rankName,
                                                          GeologicTimePeriodTreeDef def)
    {
        // check to see if this item already exists
        for (Object o : def.getTreeDefItems())
        {
            GeologicTimePeriodTreeDefItem item = (GeologicTimePeriodTreeDefItem)o;
            if (item.getRankId().equals(rankCode)) { return null; }
        }

        // create a new item
        GeologicTimePeriodTreeDefItem item = new GeologicTimePeriodTreeDefItem();
        item.initialize();
        item.setRankId(rankCode);
        item.setName(rankName);
        def.addTreeDefItem(item);
        return item;
    }

    public void convertGTP(GeologicTimePeriodTreeDef treeDef) throws SQLException
    {
        BasicSQLUtils.deleteAllRecordsFromTable("geologictimeperiod",
                BasicSQLUtils.myDestinationServerType);

        log.info("Converting old geologic time period records");
        int count = 0;

        // create an ID mapper for the geologictimeperiod table
        IdTableMapper gtpIdMapper = IdMapperMgr.getInstance().addTableMapper("geologictimeperiod",
                "GeologicTimePeriodID");
        Hashtable<Integer, GeologicTimePeriod> oldIdToGTPMap = new Hashtable<Integer, GeologicTimePeriod>();

        String sql = "SELECT g.GeologicTimePeriodID,g.RankCode,g.Name,g.Standard,g.Remarks,g.TimestampModified,g.TimestampCreated,p1.Age as Upper,p1.AgeUncertainty as UpperUncertainty,p2.Age as Lower,p2.AgeUncertainty as LowerUncertainty FROM geologictimeperiod g, geologictimeboundary p1, geologictimeboundary p2 WHERE g.UpperBoundaryID=p1.GeologicTimeBoundaryID AND g.LowerBoundaryID=p2.GeologicTimeBoundaryID ORDER BY Lower DESC, RankCode";
        Statement statement = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = statement.executeQuery(sql);

        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        Vector<GeologicTimePeriod> newItems = new Vector<GeologicTimePeriod>();

        GeologicTimePeriod allTime = new GeologicTimePeriod();
        allTime.initialize();
        allTime.setDefinition(treeDef);
        GeologicTimePeriodTreeDefItem rootDefItem = treeDef.getDefItemByRank(0);
        allTime.setDefinitionItem(rootDefItem);
        allTime.setRankId(0);
        allTime.setName("Time");
        allTime.setFullName("Time");
        allTime.setStartPeriod(100000f);
        allTime.setEndPeriod(0f);
        allTime.setEndUncertainty(0f);
        allTime.setTimestampCreated(now);
        ++count;
        newItems.add(allTime);

        while (rs.next())
        {
            Integer id = rs.getInt(1);
            Integer rank = rs.getInt(2) * 100;
            String name = rs.getString(3);
            String std = rs.getString(4);
            String rem = rs.getString(5);
            Date modTDate = rs.getDate(6);
            Date creTDate = rs.getDate(7);
            Timestamp modT = (modTDate != null) ? new Timestamp(modTDate.getTime()) : null;
            Timestamp creT = (creTDate != null) ? new Timestamp(creTDate.getTime()) : null;
            Float upper = rs.getFloat(8);
            Float uError = (Float)rs.getObject(9);
            Float lower = rs.getFloat(10);
            Float lError = (Float)rs.getObject(11);

            if (modT == null && creT == null)
            {
                creT = now;
                modT = now;

            } else if (modT == null && creT != null)
            {
                modT = new Timestamp(creT.getTime());
            } else if (modT != null && creT == null)
            {
                creT = new Timestamp(modT.getTime());
            }
            // else (neither are null, so do nothing)

            GeologicTimePeriod gtp = new GeologicTimePeriod();
            gtp.initialize();
            gtp.setName(name);
            gtp.setFullName(name);
            GeologicTimePeriodTreeDefItem defItem = treeDef.getDefItemByRank(rank);
            gtp.setDefinitionItem(defItem);
            gtp.setRankId(rank);
            gtp.setDefinition(treeDef);
            gtp.setStartPeriod(lower);
            gtp.setStartUncertainty(lError);
            gtp.setEndPeriod(upper);
            gtp.setEndUncertainty(uError);
            gtp.setStandard(std);
            gtp.setRemarks(rem);
            gtp.setTimestampCreated(creT);
            gtp.setTimestampModified(modT);

            newItems.add(gtp);

            oldIdToGTPMap.put(id, gtp);

            if (++count % 500 == 0)
            {
                log.info(count + " geologic time period records converted");
            }
        }

        // now we need to fix the parent/pointers
        for (int i = 0; i < newItems.size(); ++i)
        {
            GeologicTimePeriod gtp = newItems.get(i);
            for (int j = 0; j < newItems.size(); ++j)
            {
                GeologicTimePeriod child = newItems.get(j);
                if (isParentChildPair(gtp, child))
                {
                    gtp.addChild(child);
                }
            }
        }

        TreeHelper.fixFullnameForNodeAndDescendants(allTime);
        // fix node number, child node number stuff
        allTime.setNodeNumber(1);
        fixNodeNumbersFromRoot(allTime);
        localSession.save(allTime);

        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();

        if (shouldCreateMapTables)
        {
            // add all of the ID mappings
            for (Integer oldId : oldIdToGTPMap.keySet())
            {
                GeologicTimePeriod gtp = oldIdToGTPMap.get(oldId);
                gtpIdMapper.put(oldId, gtp.getId());
            }
        }

        // set up geologictimeperiod foreign key mapping for stratigraphy
        IdMapperMgr.getInstance().mapForeignKey("Stratigraphy", "GeologicTimePeriodID",
                "GeologicTimePeriod", "GeologicTimePeriodID");

        log.info(count + " geologic time period records converted");
    }

    /**
     * Regenerates all nodeNumber and highestChildNodeNumber field values for all nodes attached to
     * the given root. The nodeNumber field of the given root must already be set.
     * 
     * @param root the top of the tree to be renumbered
     * @return the highest node number value present in the subtree rooted at <code>root</code>
     */
    public static <T extends Treeable<T, ?, ?>> int fixNodeNumbersFromRoot(T root)
    {
        int nextNodeNumber = root.getNodeNumber();
        for (T child : root.getChildren())
        {
            child.setNodeNumber(++nextNodeNumber);
            nextNodeNumber = fixNodeNumbersFromRoot(child);
        }
        root.setHighestChildNodeNumber(nextNodeNumber);
        return nextNodeNumber;
    }

    protected boolean isParentChildPair(GeologicTimePeriod parent, GeologicTimePeriod child)
    {
        if (parent == child) { return false; }

        Float startParent = parent.getStartPeriod();
        Float endParent = parent.getEndPeriod();

        Float startChild = child.getStartPeriod();
        Float endChild = child.getEndPeriod();

        // remember, the numbers represent MYA (millions of yrs AGO)
        // so the logic seems a little backwards
        if (startParent >= startChild && endParent <= endChild
                && parent.getRankId() < child.getRankId()) { return true; }

        return false;
    }

    /**
     * Copies the filed names to the list and prepend the table name
     * 
     * @param list the destination list
     * @param fieldNames the list of field names
     * @param tableName the table name
     */
    protected void addNamesWithTableName(final List<String> list,
                                         final List<String> fieldNames,
                                         final String tableName)
    {
        for (String fldName : fieldNames)
        {
            list.add(tableName + "." + fldName);
        }
    }

    /**
     * @param rsmd
     * @param map
     * @param tableNames
     * @throws SQLException
     */
    protected void buildIndexMapFromMetaData(final ResultSetMetaData rsmd,
                                             final Hashtable<String, Integer> map,
                                             final String[] tableNames) throws SQLException
    {
        map.clear();

        // Find the missing table name by figuring our which one isn't used.
        Hashtable<String, Boolean> existsMap = new Hashtable<String, Boolean>();
        for (String tblName : tableNames)
        {
            existsMap.put(tblName, true);
            System.out.println("[" + tblName + "]");
        }
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {
            String tableName = rsmd.getTableName(i);
            // log.info("["+tableName+"]");
            if (StringUtils.isNotEmpty(tableName))
            {
                if (existsMap.get(tableName) != null)
                {
                    existsMap.remove(tableName);
                    log.info("Removing Table Name[" + tableName + "]");
                }
            }
        }

        String missingTableName = null;
        if (existsMap.size() == 1)
        {
            missingTableName = existsMap.keys().nextElement();
            log.info("Missing Table Name[" + missingTableName + "]");

        } else if (existsMap.size() > 1)
        {
            throw new RuntimeException("ExistsMap cannot have more than one name in it!");
        } else
        {
            log.info("No Missing Table Names.");
        }

        for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {
            strBuf.setLength(0);
            String tableName = rsmd.getTableName(i);
            strBuf.append(StringUtils.isNotEmpty(tableName) ? tableName : missingTableName);
            strBuf.append(".");
            strBuf.append(rsmd.getColumnName(i));
            map.put(strBuf.toString(), i);
        }
    }

    /**
     * @param rsmd
     * @param map
     * @param tableNames
     * @throws SQLException
     */
    protected void buildIndexMapFromMetaData(final ResultSetMetaData rsmd,
                                             final List<String> origList,
                                             final Hashtable<String, Integer> map)
            throws SQLException
    {
        map.clear();

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {
            sb.setLength(0);
            String tableName = rsmd.getTableName(i);
            String fieldName = rsmd.getColumnName(i);

            if (StringUtils.isNotEmpty(tableName))
            {
                sb.append(tableName);
            } else
            {
                for (String fullName : origList)
                {
                    String[] parts = StringUtils.split(fullName, ".");
                    if (parts[1].equals(fieldName))
                    {
                        sb.append(parts[0]);
                        break;
                    }
                }
            }
            sb.append(".");
            sb.append(fieldName);
            // log.info("["+strBuf.toString()+"] "+i);
            map.put(sb.toString(), i);
        }
    }

    protected void duplicateAddress(final Connection newDBConnArg,
                                    final Integer oldId,
                                    final Integer newId)
    {
        log.info("Duplicating [" + oldId + "] to [" + newId + "]");

        List<String> agentAddrFieldNames = new ArrayList<String>();
        getFieldNamesFromSchema(newDBConnArg, "address", agentAddrFieldNames,
                BasicSQLUtils.myDestinationServerType);
        String fieldList = buildSelectFieldList(agentAddrFieldNames, "address");
        log.info(fieldList);

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("SELECT ");
        sqlStr.append(fieldList);
        sqlStr.append(" from address where AddressID = " + oldId);

        if (true)
        {
            log.info(sqlStr.toString());
        }

        try
        {
            Statement stmt = newDBConnArg.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sqlStr.toString());

            if (rs.last())
            {
                setProcess(0, rs.getRow());
                rs.first();
            }

            sqlStr.setLength(0);
            sqlStr.append("INSERT INTO address ");
            sqlStr.append("(" + fieldList + ")");
            sqlStr.append(" VALUES (");

            ResultSetMetaData metaData = rs.getMetaData();
            int cols = metaData.getColumnCount();
            for (int i = 1; i <= cols; i++)
            {
                if (i == 1)
                {
                    sqlStr.append(newId);

                } else
                {
                    sqlStr.append(",");
                    sqlStr.append(BasicSQLUtils.getStrValue(rs.getObject(i)));
                }
            }
            sqlStr.append(") ");

            rs.close();
            stmt.close();

            Statement updateStatement = newDBConnArg.createStatement();
            BasicSQLUtils.removeForeignKeyConstraints(newDBConn,
                    BasicSQLUtils.myDestinationServerType);
            // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            if (true)
            {
                log.info(sqlStr.toString());
            }
            updateStatement.executeUpdate(sqlStr.toString());
            updateStatement.clearBatch();
            updateStatement.close();
            updateStatement = null;

        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

    }

    protected static Integer nextAddressId = 0;

    class AddressInfo
    {
        Integer                     oldAddrId;
        Integer                     newAddrId;
        Hashtable<Integer, Boolean> agentHash         = new Hashtable<Integer, Boolean>();
        Vector<Integer>             newIdsToDuplicate = new Vector<Integer>();
        boolean                     isUsed            = false;
        boolean                     wasAdded          = false;

        public AddressInfo(final Integer oldAddrId, final Integer newAddrId, final Integer agentId)
        {
            this.oldAddrId = oldAddrId;
            this.newAddrId = newAddrId;
            agentHash.put(agentId, true);
        }

        public AddressInfo(final Integer oldAddrId, final Integer newAddrId)
        {
            this.oldAddrId = oldAddrId;
            this.newAddrId = newAddrId;
        }

        public Hashtable<Integer, Boolean> getAgentHash()
        {
            return agentHash;
        }

        public Integer getNewAddrId()
        {
            return newAddrId;
        }

        public Integer getOldAddrId()
        {
            return oldAddrId;
        }

        public boolean isUsed()
        {
            return isUsed;
        }

        public void setUsed(boolean isUsed)
        {
            this.isUsed = isUsed;
        }

        public Integer addAgent(Integer agentId)
        {
            agentHash.put(agentId, true);

            if (agentHash.size() > 1)
            {
                newIdsToDuplicate.add(nextAddressId);
                nextAddressId++;
                return nextAddressId;
            }
            return newAddrId;
        }

        public boolean wasAdded()
        {
            return wasAdded;
        }

        public void setWasAdded(boolean wasAddedArg)
        {
            this.wasAdded = wasAddedArg;
        }

        public Vector<Integer> getNewIdsToDuplicate()
        {
            return newIdsToDuplicate;
        }
    }

    class AgentInfo
    {
        Integer                     oldAgentId;
        Integer                     newAgentId;
        Hashtable<Integer, Boolean> addrs    = new Hashtable<Integer, Boolean>();
        boolean                     isUsed   = false;
        boolean                     wasAdded = false;

        public AgentInfo(Integer oldAgentId, Integer newAgentId, Integer addrId)
        {
            super();
            this.oldAgentId = oldAgentId;
            this.newAgentId = newAgentId;
            addrs.put(addrId, true);
        }

        public AgentInfo(Integer oldAgentId, Integer newAgentId)
        {
            super();
            this.oldAgentId = oldAgentId;
            this.newAgentId = newAgentId;
        }

        public Hashtable<Integer, Boolean> getAddrs()
        {
            return addrs;
        }

        public Integer getNewAgentId()
        {
            return newAgentId;
        }

        public Integer getOldAgentId()
        {
            return oldAgentId;
        }

        public boolean isUsed()
        {
            return isUsed;
        }

        public void setUsed(boolean isUsed)
        {
            this.isUsed = isUsed;
        }

        public boolean wasAdded()
        {
            return wasAdded;
        }

        public void setWasAdded(boolean wasAddedArg)
        {
            this.wasAdded = wasAddedArg;
        }
    }

    /**
     * @param oldAgentId
     * @param agentIDMapper
     */
    protected void copyAgentFromOldToNew(final Integer oldAgentId, final IdTableMapper agentIDMapper)
    {
        boolean doDebug = false;

        StringBuilder sql = new StringBuilder("select ");
        if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
        {
            BasicSQLUtils.removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);
        }
        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);

        List<String> oldAgentFieldNames = new ArrayList<String>();
        getFieldNamesFromSchema(oldDBConn, "agent", oldAgentFieldNames, BasicSQLUtils.mySourceServerType);
        String oldFieldListStr = buildSelectFieldList(oldAgentFieldNames, "agent");
        sql.append(oldFieldListStr);
        sql.append(" from agent where AgentID = " + oldAgentId);

        // log.info(oldFieldListStr);

        List<String> newAgentFieldNames = new ArrayList<String>();
        getFieldNamesFromSchema(newDBConn, "agent", newAgentFieldNames, BasicSQLUtils.myDestinationServerType);
        String newFieldListStr = buildSelectFieldList(newAgentFieldNames, "agent");

        // log.info(newFieldListStr);

        Hashtable<String, Integer> oldIndexFromNameMap = new Hashtable<String, Integer>();
        int inx = 1;
        for (String fldName : oldAgentFieldNames)
        {
            oldIndexFromNameMap.put(fldName, inx++);
        }

        Hashtable<String, Integer> newIndexFromNameMap = new Hashtable<String, Integer>();
        inx = 1;
        for (String fldName : newAgentFieldNames)
        {
            newIndexFromNameMap.put(fldName, inx++);
        }

        try
        {
            // So first we hash each AddressID and the value is set to 0 (false)
            Statement stmtX = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rsX = stmtX.executeQuery(sql.toString());

            // log.debug(sql.toString());

            int cnt = 0;
            while (rsX.next())
            {
                int agentId = rsX.getInt(1);

                StringBuilder sqlStr = new StringBuilder();
                sqlStr.append("INSERT INTO agent ");
                sqlStr.append("(" + newFieldListStr);
                sqlStr.append(")");
                sqlStr.append(" VALUES (");

                int fCnt = 0;
                for (String fieldName : newAgentFieldNames)
                {
                    if (fCnt > 0)
                        sqlStr.append(", ");

                    if (StringUtils.contains(fieldName.toLowerCase(), "disciplineid"))
                    {
                        sqlStr.append(getDisciplineId());

                    } else
                    {
                        String value = "";
                        Integer index = oldIndexFromNameMap.get(fieldName);
                        if (index == null)
                        {
                            // System.out.println(fieldName);
                            value = "NULL";

                        } else if (fCnt == 0)
                        {
                            value = agentIDMapper.get(agentId).toString();

                        } else
                        {
                            value = BasicSQLUtils.getStrValue(rsX.getObject(index.intValue()));
                        }

                        BasicSQLUtilsMapValueIFace valueMapper = columnValueMapper.get(fieldName);
                        if (valueMapper != null)
                        {
                            value = valueMapper.mapValue(value);
                        }
                        sqlStr.append(value);
                    }
                    fCnt++;
                }
                sqlStr.append(")");
                // log.info(sqlStr.toString());

                Statement updateStatement = newDBConn.createStatement();
                // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                if (doDebug)
                {
                    log.info(sqlStr.toString());
                }
                updateStatement.executeUpdate(sqlStr.toString());
                updateStatement.clearBatch();
                updateStatement.close();
                updateStatement = null;

                cnt++;
                BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);

            }
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Specify 5.x points at AgentAdress instead of an Agent. The idea was that to point at an Agent
     * and possibly a differnt address that represents what that person does. This was really
     * confusing so we are changing it to point at an Agent instead.
     * 
     * So that means we need to pull apart these relationships and have all foreign keys that point
     * to an AgentAddress now point at an Agent and we then need to add in the Agents and then add
     * the Address to the Agents.
     * 
     * The AgentAdress, Agent and Address (triple) can have a NULL Address but it cannot have a NULL
     * Agent. If there is a NULL Agent then this method will throw a RuntimeException.
     */
    public boolean convertAgents() throws SQLException
    {
        boolean debugAgents = true;

        /*
         * BasicSQLUtils.clearValueMapper(); BasicSQLUtilsMapValueIFace
         * collectionMemberIDValueMapper = getCollectionMemberIDValueMapper();
         * BasicSQLUtilsMapValueIFace agentCreatorValueMapper = getAgentCreatorValueMapper();
         * BasicSQLUtilsMapValueIFace agentModiferValueMapper = getAgentModiferValueMapper();
         * BasicSQLUtilsMapValueIFace versionValueMapper = getVersionValueMapper();
         * BasicSQLUtils.addToValueMapper("CollectionMemberID", collectionMemberIDValueMapper);
         * BasicSQLUtils.addToValueMapper("CreatedByAgentID", agentCreatorValueMapper);
         * BasicSQLUtils.addToValueMapper("ModifiedByAgentID", agentModiferValueMapper);
         * BasicSQLUtils.addToValueMapper("Version", versionValueMapper);
         */

        log.debug("convert Agents");

        // GenericDBConversion.setShouldCreateMapTables(true);

        BasicSQLUtils.removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);

        // Create the mappers here, but fill them in during the AgentAddress Process
        IdTableMapper agentIDMapper = idMapperMgr.addTableMapper("agent", "AgentID");
        IdTableMapper addrIDMapper = idMapperMgr.addTableMapper("address", "AddressID");
        IdTableMapper agentAddrIDMapper = idMapperMgr.addTableMapper("agentaddress",
                "AgentAddressID");

        agentIDMapper.setInitialIndex(4);

        if (shouldCreateMapTables)
        {
            log.info("Mapping Agent Ids");
            agentIDMapper.mapAllIds();// .mapAllIds("select AgentID from agent order by AgentID");

            log.info("Mapping Address Ids");
            addrIDMapper.mapAllIds();// .mapAllIds("select AddressID from address order by
                                        // AddressID");
        }

        // Just like in the conversion of the CollectionObjects we
        // need to build up our own select clause because the MetaData of columns names returned
        // from
        // a query doesn't include the table names for all columns, this is far more predictable
        List<String> oldFieldNames = new ArrayList<String>();

        StringBuilder sql = new StringBuilder("select ");
        log.debug(sql);
        List<String> agentAddrFieldNames = new ArrayList<String>();
        getFieldNamesFromSchema(oldDBConn, "agentaddress", agentAddrFieldNames, BasicSQLUtils.mySourceServerType);
        sql.append(buildSelectFieldList(agentAddrFieldNames, "agentaddress"));
        sql.append(", ");
        addNamesWithTableName(oldFieldNames, agentAddrFieldNames, "agentaddress");

        List<String> agentFieldNames = new ArrayList<String>();
        getFieldNamesFromSchema(oldDBConn, "agent", agentFieldNames, BasicSQLUtils.mySourceServerType);
        sql.append(buildSelectFieldList(agentFieldNames, "agent"));
        log.debug(sql);
        sql.append(", ");
        addNamesWithTableName(oldFieldNames, agentFieldNames, "agent");

        List<String> addrFieldNames = new ArrayList<String>();
        getFieldNamesFromSchema(oldDBConn, "address", addrFieldNames, BasicSQLUtils.mySourceServerType);
        log.debug(sql);
        sql.append(buildSelectFieldList(addrFieldNames, "address"));
        addNamesWithTableName(oldFieldNames, addrFieldNames, "address");

        // Create a Map from the full table/fieldname to the index in the resultset (start at 1 not
        // zero)
        Hashtable<String, Integer> indexFromNameMap = new Hashtable<String, Integer>();

        sql.append(" From agent Inner Join agentaddress ON agentaddress.AgentID = agent.AgentID Inner Join address ON agentaddress.AddressID = address.AddressID Order By agentaddress.AgentAddressID Asc");

        // These represent the New columns of Agent Table
        // So the order of the names are for the new table
        // the names reference the old table
        String[] agentColumns = { "agent.AgentID", "agent.TimestampModified", "agent.AgentType",
                "agentaddress.JobTitle", "agent.FirstName", "agent.LastName",
                "agent.MiddleInitial", "agent.Title", "agent.Interests", "agent.Abbreviation",
                "agent.Name", "agentaddress.Email", "agentaddress.URL", "agent.Remarks",
                "agent.TimestampCreated", "agent.Visibility", "agent.VisibilitySetBy",// User/Security changes
                "agent.ParentOrganizationID", "agent.DisciplineID" };

        // See comments for agent Columns
        String[] addressColumns = { "address.AddressID", "address.TimestampModified",
                "address.Address", "address.Address2", "address.City", "address.State",
                "address.Country", "address.Postalcode", "address.Remarks",
                "address.TimestampCreated", "agentaddress.IsCurrent", "agentaddress.Phone1",
                "agentaddress.Phone2", "agentaddress.Fax", "agentaddress.RoomOrBuilding",
                "address.AgentID" };

        Hashtable<Integer, AddressInfo> addressHash = new Hashtable<Integer, AddressInfo>();
        Hashtable<Integer, AgentInfo> agentHash = new Hashtable<Integer, AgentInfo>();

        // Create a Hashtable to track which IDs have been handled during the conversion process
        try
        {
            log.info("Hashing Address Ids");

            // So first we hash each AddressID and the value is set to 0 (false)
            Statement stmtX = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rsX = stmtX.executeQuery("select AddressID from address order by AddressID");

            int cnt = 0;
            if (rsX.last())
            {
                setProcess(0, rsX.getRow());
                rsX.first();
            }
            // Needed to add in case AgentAddress table wasn't used.
            if (rsX.first())
            {
                do
                {
                    int addrId = rsX.getInt(1);
                    addressHash.put(addrId, new AddressInfo(addrId, addrIDMapper.get(addrId)));

                    if (cnt % 100 == 0)
                    {
                        setProcess(0, cnt);
                    }
                    cnt++;
                } while (rsX.next());
            }
            rsX.close();
            stmtX.close();

            setProcess(0, 0);

            // Next we hash all the Agents and set their values to 0 (false)
            log.info("Hashing Agent Ids");
            stmtX = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            rsX = stmtX.executeQuery("select AgentID from agent order by AgentID");

            cnt = 0;
            if (rsX.last())
            {
                setProcess(0, rsX.getRow());
                rsX.first();
            }

            do
            {
                int agentId = rsX.getInt(1);
                agentHash.put(agentId, new AgentInfo(agentId, agentIDMapper.get(agentId)));
                if (cnt % 100 == 0)
                {
                    setProcess(0, cnt);
                }
                cnt++;
            } while (rsX.next());

            rsX.close();
            stmtX.close();

            setProcess(0, 0);

            // Now we map all the Agents to their Addresses AND
            // All the Addresses to their Agents.
            //
            // NOTE: A single Address Record Mat be used by more than one Agent so
            // we will need to Duplicate the Address records later
            //
            log.info("Cross Mapping Agents and Addresses");

            stmtX = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            rsX = stmtX
                    .executeQuery("SELECT AgentAddressID, AddressID, AgentID FROM agentaddress a where AddressID is not null and AgentID is not null");

            cnt = 0;
            if (rsX.last())
            {
                setProcess(0, rsX.getRow());
                rsX.first();
            }
            // Needed to add incase AgentAddress table wasn't used.
            if (rsX.first())
            {
                do
                {
                    int agentAddrId = rsX.getInt(1);
                    int addrId = rsX.getInt(2);
                    int agentId = rsX.getInt(3);

                    // ///////////////////////
                    // Add Address to Agent
                    // ///////////////////////
                    AgentInfo agentInfo = agentHash.get(agentId);
                    if (agentInfo == null) { throw new RuntimeException("The AgentID [" + agentId
                            + "]in AgentAddress table id[" + agentAddrId + "] desn't exist"); }
                    agentInfo.getAddrs().put(addrId, true);

                    AddressInfo addrInfo = addressHash.get(addrId);
                    if (addrInfo == null) { throw new RuntimeException("The AddressID [" + addrId
                            + "] in AgentAddress table id[" + agentAddrId + "] desn't exist"); }
                    agentInfo.getAddrs().put(addrId, true);

                    if (cnt % 100 == 0)
                    {
                        setProcess(0, cnt);
                    }
                    cnt++;

                } while (rsX.next());
            }
            rsX.close();
            stmtX.close();

            setProcess(0, 0);

            // It OK if the address is NULL, but the Agent CANNOT be NULL
            log.info("Checking for null Agents");

            stmtX = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            rsX = stmtX
                    .executeQuery("SELECT count(AgentAddressID) FROM agentaddress a where AddressID is not null and AgentID is null");
            if (rsX.last())
            {
                setProcess(0, rsX.getRow());
                rsX.first();
            }

            // If there is a Single Record With a NULL Agent this would be BAD!
            int count = rsX.getInt(1);
            if (count > 0)
            {
                // throw new RuntimeException("There are "+count+" AgentAddress Records where the
                // AgentID is null and the AddressId is not null!");
            }
            rsX.close();
            stmtX.close();

            nextAddressId = BasicSQLUtils.getNumRecords(oldDBConn, "address") + 1;

            // ////////////////////////////////////////////////////////////////////////////////
            // This does the part of AgentAddress where it has both an Address AND an Agent
            // ////////////////////////////////////////////////////////////////////////////////

            log.info(sql.toString());

            // Example of the Query
            //
            // select agentaddress.AgentAddressID, agentaddress.TypeOfAgentAddressed,
            // agentaddress.AddressID, agentaddress.AgentID, agentaddress.OrganizationID,
            // agentaddress.JobTitle, agentaddress.Phone1, agentaddress.Phone2, agentaddress.Fax,
            // agentaddress.RoomOrBuilding, agentaddress.Email, agentaddress.URL,
            // agentaddress.Remarks, agentaddress.TimestampModified, agentaddress.TimestampCreated,
            // agentaddress.LastEditedBy, agentaddress.IsCurrent, agent.AgentID,
            // agent.AgentType, agent.FirstName, agent.LastName, agent.MiddleInitial, agent.Title,
            // agent.Interests, agent.Abbreviation, agent.Name, agent.ParentOrganizationID,
            // agent.Remarks, agent.TimestampModified, agent.TimestampCreated, agent.LastEditedBy,
            // address.AddressID, address.Address, address.City, address.State, address.Country,
            // address.Postalcode, address.Remarks, address.TimestampModified,
            // address.TimestampCreated, address.LastEditedBy From agent
            // Inner Join agentaddress ON agentaddress.AgentID = agent.AgentID Inner Join address ON
            // agentaddress.AddressID = address.AddressID Order By agentaddress.AgentAddressID Asc

            // select agentaddress.AgentAddressID, agentaddress.TypeOfAgentAddressed,
            // agentaddress.AddressID, agentaddress.AgentID, agentaddress.OrganizationID,
            // agentaddress.JobTitle, agentaddress.Phone1, agentaddress.Phone2, agentaddress.Fax,
            // agentaddress.RoomOrBuilding, agentaddress.Email, agentaddress.URL,
            // agentaddress.Remarks, agentaddress.TimestampModified, agentaddress.TimestampCreated,
            // agentaddress.LastEditedBy, agentaddress.IsCurrent, agent.AgentID, agent.AgentType,
            // agent.FirstName, agent.LastName, agent.MiddleInitial, agent.Title, agent.Interests,
            // agent.Abbreviation, agent.Name, agent.ParentOrganizationID, agent.Remarks,
            // agent.TimestampModified, agent.TimestampCreated, agent.LastEditedBy,
            // address.AddressID, address.Address, address.City, address.State, address.Country,
            // address.Postalcode, address.Remarks, address.TimestampModified,
            // address.TimestampCreated, address.LastEditedBy From agent Inner Join agentaddress ON
            // agentaddress.AgentID = agent.AgentID Inner Join address ON agentaddress.AddressID =
            // address.AddressID Order By agentaddress.AgentAddressID Asc

            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ResultSet rs = stmt.executeQuery(sql.toString());

            // Create Map of column name to column index number
            int inx = 1;
            for (String fldName : oldFieldNames)
            {
                // log.info("["+fldName+"] "+inx+" ["+rsmd.getColumnName(inx)+"]");
                indexFromNameMap.put(fldName, inx++);
            }

            // Figure out certain icolumn indexes we will need ater
            int agentIdInx = indexFromNameMap.get("agent.AgentID");
            int addrIdInx = indexFromNameMap.get("address.AddressID");
            int agentTypeInx = indexFromNameMap.get("agent.AgentType");
            int lastEditInx = indexFromNameMap.get("agent.LastEditedBy");

            // int newAddrId = -1;
            // int newAgentId = -1;

            int recordCnt = 0;
            while (rs.next())
            {
                byte agentType = rs.getByte(agentTypeInx);
                int agentAddressId = rs.getInt(1);
                int agentId = rs.getInt(agentIdInx);
                int addrId = rs.getInt(addrIdInx);
                String lastEditedBy = rs.getString(lastEditInx);

                AddressInfo addrInfo = addressHash.get(addrId);
                AgentInfo agentInfo = agentHash.get(agentId);

                // Now tell the AgentAddress Mapper the New ID to the Old AgentAddressID
                if (shouldCreateMapTables)
                {
                    agentAddrIDMapper.put(agentAddressId, agentInfo.getNewAgentId());
                }

                // Because of the old DB relationships we want to make sure we only add each agent
                // in one time
                // So start by checking the Hashtable to see if it has already been added
                if (!agentInfo.wasAdded())
                {
                    agentInfo.setWasAdded(true);

                    BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "agent",
                            BasicSQLUtils.myDestinationServerType);
                    // It has not been added yet so Add it
                    StringBuilder sqlStr = new StringBuilder();
                    sqlStr.append("INSERT INTO agent ");
                    sqlStr.append("(AgentID, TimestampModified, AgentType, JobTitle, FirstName, LastName, MiddleInitial, ");
                    sqlStr.append("Title, Interests, Abbreviation, Name, Email, URL, Remarks, TimestampCreated, ");
                    sqlStr.append("Visibility, VisibilitySetBy, ParentOrganizationID, DisciplineID, CreatedByAgentID, ModifiedByAgentID, Version)");
                    sqlStr.append(" VALUES (");

                    for (int i = 0; i < agentColumns.length; i++)
                    {
                        if (i > 0)
                            sqlStr.append(",");

                        if (i == 0)
                        {
                            sqlStr.append(agentInfo.getNewAgentId());

                        } else if (agentColumns[i].equals("agent.ParentOrganizationID"))
                        {
                            Object obj = rs.getObject(indexFromNameMap.get(agentColumns[i]));
                            if (obj != null)
                            {
                                int oldId = rs.getInt(agentColumns[i]);
                                Integer newID = agentIDMapper.get(oldId);
                                if (newID == null)
                                {
                                    log.error("Couldn't map ParentOrganizationID [" + oldId + "]");
                                }
                                sqlStr.append(BasicSQLUtils.getStrValue(newID));

                            } else
                            {
                                sqlStr.append("NULL");
                            }

                        } else if (agentColumns[i].equalsIgnoreCase("agent.DisciplineID"))
                        {
                            sqlStr.append(getDisciplineId());

                        } else if (agentColumns[i].equals("agent.Name"))
                        {
                            if (agentType == 1) // when it is an individual, clear the name field
                            {
                                sqlStr.append("NULL");
                            } else
                            {
                                inx = indexFromNameMap.get(agentColumns[i]);
                                sqlStr.append(BasicSQLUtils.getStrValue(rs.getObject(inx)));
                            }

                        } else if (agentColumns[i].equals("agent.Visibility"))// User/Security
                                                                                // changes
                        {
                            sqlStr.append(defaultVisibilityLevel);

                        } else if (agentColumns[i].equals("agent.VisibilitySetBy"))// User/Security
                                                                                    // changes
                        {
                            sqlStr.append("NULL");

                        } else
                        {
                            if (debugAgents)
                            {
                                log.info(agentColumns[i]);
                            }
                            inx = indexFromNameMap.get(agentColumns[i]);
                            sqlStr.append(BasicSQLUtils.getStrValue(rs.getObject(inx)));
                        }
                    }
                    sqlStr.append("," + getCreatorAgentId(lastEditedBy) + ","
                            + getModifiedByAgentId(lastEditedBy) + ",0");
                    sqlStr.append(")");

                    try
                    {
                        Statement updateStatement = newDBConn.createStatement();
                        // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                        if (debugAgents)
                        {
                            log.info(sqlStr.toString());
                        }
                        updateStatement.executeUpdate(sqlStr.toString());
                        updateStatement.clearBatch();
                        updateStatement.close();
                        updateStatement = null;

                    } catch (SQLException e)
                    {
                        log.error(sqlStr.toString());
                        log.error("Count: " + recordCnt);
                        e.printStackTrace();
                        log.error(e);
                        System.exit(0);
                        throw new RuntimeException(e);
                    }

                } else
                {
                    // The Agent has already been added so we use the tracker Hashtable
                    // to find out the new Id for the old Agent Id
                    // log.info("Agent already Used
                    // ["+BasicSQLUtils.getStrValue(rs.getObject(indexFromNameMap.get("agent.LastName")))+"]");
                }
                BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);
                // Now make sure we only add an address in one
                if (!addrInfo.wasAdded())
                {
                    addrInfo.setWasAdded(true);
                    BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "address", BasicSQLUtils.myDestinationServerType);
                    StringBuilder sqlStr = new StringBuilder("INSERT INTO address ");
                    sqlStr.append("(AddressID, TimestampModified, Address, Address2, City, State, Country, PostalCode, Remarks, TimestampCreated, ");
                    sqlStr.append("IsPrimary, Phone1, Phone2, Fax, RoomOrBuilding, AgentID, CollectionMemberID, CreatedByAgentID, ModifiedByAgentID, Version)");
                    sqlStr.append(" VALUES (");
                    for (int i = 0; i < addressColumns.length; i++)
                    {
                        if (i > 0)
                            sqlStr.append(",");

                        if (i == addressColumns.length - 1)
                        {
                            sqlStr.append(agentInfo.getNewAgentId());

                        } else
                        {
                            Integer inxInt = indexFromNameMap.get(addressColumns[i]);
                            String value;
                            if (i == 0)
                            {
                                value = addrInfo.getNewAddrId().toString();

                            } else if (inxInt == null && addressColumns[i].equals("address.Address2"))
                            {
                                value = "''";

                            } else if (addressColumns[i].equals("agentaddress.IsCurrent"))
                            {
                                value = rs.getInt(inxInt) == 0 ? "0" : "1"; // mapping a boolean

                            } else if (addressColumns[i].equals("address.Address"))
                            {
                                value = BasicSQLUtils.getStrValue(StringEscapeUtils.escapeJava(rs.getString(inxInt)));

                            } else
                            {
                                // log.info(addressColumns[i]);
                                value = BasicSQLUtils.getStrValue(rs.getObject(inxInt));
                            }
                            sqlStr.append(value);
                        }
                    }
                    sqlStr.append("," + getCollectionMemberId() + ","  + getCreatorAgentId(lastEditedBy) + "," + getModifiedByAgentId(lastEditedBy) + ",0");
                    sqlStr.append(")");

                    try
                    {
                        Statement updateStatement = newDBConn.createStatement();
                        // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                        if (debugAgents)
                        {
                            log.info(sqlStr.toString());
                        }
                        updateStatement.executeUpdate(sqlStr.toString());
                        updateStatement.clearBatch();
                        updateStatement.close();
                        updateStatement = null;

                    } catch (SQLException e)
                    {
                        log.error(sqlStr.toString());
                        log.error("Count: " + recordCnt);
                        e.printStackTrace();
                        log.error(e);
                        throw new RuntimeException(e);
                    }
                }
                BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "address",
                        BasicSQLUtils.myDestinationServerType);

                if (recordCnt % 250 == 0)
                {
                    log.info("AgentAddress Records: " + recordCnt);
                }
                recordCnt++;
            } // while

            log.info("AgentAddress Records: " + recordCnt);
            rs.close();
            stmt.close();

            // Now duplicate the Address Records
            for (Integer oldAddrId : addressHash.keySet())
            {
                AddressInfo addrInfo = addressHash.get(oldAddrId);

                for (Integer newAddrId : addrInfo.getNewIdsToDuplicate())
                {
                    duplicateAddress(newDBConn, addrInfo.getNewAddrId(), newAddrId);
                }
            }

            // ////////////////////////////////////////////////////////////////////////////////
            // This does the part of AgentAddress where it has JUST Agent
            // ////////////////////////////////////////////////////////////////////////////////
            log.info("******** Doing AgentAddress JUST Agent");

            int newRecordsAdded = 0;

            sql.setLength(0);
            sql.append("select ");
            sql.append(buildSelectFieldList(agentAddrFieldNames, "agentaddress"));
            sql.append(", ");

            getFieldNamesFromSchema(oldDBConn, "agent", agentFieldNames,
                    BasicSQLUtils.mySourceServerType);
            sql.append(buildSelectFieldList(agentFieldNames, "agent"));

            sql
                    .append(" From agent Inner Join agentaddress ON agentaddress.AgentID = agent.AgentID where agentaddress.AddressID is null Order By agentaddress.AgentAddressID Asc");

            log.info(sql.toString());

            // Example Query

            // select agentaddress.AgentAddressID, agentaddress.TypeOfAgentAddressed,
            // agentaddress.AddressID, agentaddress.AgentID, agentaddress.OrganizationID,
            // agentaddress.JobTitle, agentaddress.Phone1, agentaddress.Phone2, agentaddress.Fax,
            // agentaddress.RoomOrBuilding, agentaddress.Email, agentaddress.URL,
            // agentaddress.Remarks, agentaddress.TimestampModified, agentaddress.TimestampCreated,
            // agentaddress.LastEditedBy, agentaddress.IsCurrent, agent.AgentID,
            // agent.AgentType, agent.FirstName, agent.LastName, agent.MiddleInitial, agent.Title,
            // agent.Interests, agent.Abbreviation, agent.Name, agent.ParentOrganizationID,
            // agent.Remarks, agent.TimestampModified, agent.TimestampCreated, agent.LastEditedBy,
            // agent.AgentID, agent.AgentType, agent.FirstName, agent.LastName, agent.MiddleInitial,
            // agent.Title, agent.Interests, agent.Abbreviation, agent.Name,
            // agent.ParentOrganizationID, agent.Remarks, agent.TimestampModified,
            // agent.TimestampCreated, agent.LastEditedBy
            // From agent Inner Join agentaddress ON agentaddress.AgentID = agent.AgentID Order By
            // agentaddress.AgentAddressID Asc

            // select agentaddress.AgentAddressID, agentaddress.TypeOfAgentAddressed,
            // agentaddress.AddressID, agentaddress.AgentID, agentaddress.OrganizationID,
            // agentaddress.JobTitle, agentaddress.Phone1, agentaddress.Phone2, agentaddress.Fax,
            // agentaddress.RoomOrBuilding, agentaddress.Email, agentaddress.URL,
            // agentaddress.Remarks, agentaddress.TimestampModified, agentaddress.TimestampCreated,
            // agentaddress.LastEditedBy, agentaddress.IsCurrent, agent.AgentID, agent.AgentType,
            // agent.FirstName, agent.LastName, agent.MiddleInitial, agent.Title, agent.Interests,
            // agent.Abbreviation, agent.Name, agent.ParentOrganizationID, agent.Remarks,
            // agent.TimestampModified, agent.TimestampCreated, agent.LastEditedBy, agent.AgentID,
            // agent.AgentType, agent.FirstName, agent.LastName, agent.MiddleInitial, agent.Title,
            // agent.Interests, agent.Abbreviation, agent.Name, agent.ParentOrganizationID,
            // agent.Remarks, agent.TimestampModified, agent.TimestampCreated, agent.LastEditedBy
            // From agent Inner Join agentaddress ON agentaddress.AgentID = agent.AgentID Order By
            // agentaddress.AgentAddressID Asc

            stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(sql.toString());

            oldFieldNames.clear();
            addNamesWithTableName(oldFieldNames, agentAddrFieldNames, "agentaddress");
            addNamesWithTableName(oldFieldNames, agentFieldNames, "agent");

            indexFromNameMap.clear();
            inx = 1;
            for (String fldName : oldFieldNames)
            {
                // log.info("["+fldName+"] "+inx+" ["+rsmd.getColumnName(inx)+"]");
                indexFromNameMap.put(fldName, inx++);
            }

            agentIdInx = indexFromNameMap.get("agent.AgentID");
            lastEditInx = indexFromNameMap.get("agent.LastEditedBy");

            recordCnt = 0;
            while (rs.next())
            {
                int agentAddressId = rs.getInt(1);
                int agentId = rs.getInt(agentIdInx);
                String lastEditedBy = rs.getString(lastEditInx);

                AgentInfo agentInfo = agentHash.get(agentId);

                // Now tell the AgentAddress Mapper the New ID to the Old AgentAddressID
                if (shouldCreateMapTables)
                {
                    agentAddrIDMapper.put(agentAddressId, agentInfo.getNewAgentId());
                }

                recordCnt++;

                if (!agentInfo.wasAdded())
                {
                    agentInfo.setWasAdded(true);
                    BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "agent",
                            BasicSQLUtils.myDestinationServerType);
                    // Create Agent
                    StringBuilder sqlStr = new StringBuilder("INSERT INTO agent ");
                    sqlStr.append("(AgentID, TimestampModified, AgentType, JobTitle, FirstName, LastName, MiddleInitial, Title, Interests, ");
                    sqlStr.append("Abbreviation, Name, Email, URL, Remarks, TimestampCreated, Visibility, VisibilitySetBy, ParentOrganizationID, ");
                    sqlStr.append("DisciplineID, CreatedByAgentID, ModifiedByAgentID, Version)");
                    sqlStr.append(" VALUES (");
                    for (int i = 0; i < agentColumns.length; i++)
                    {
                        if (i > 0)
                            sqlStr.append(",");
                        if (i == 0)
                        {
                            sqlStr.append(agentInfo.getNewAgentId());

                        } else if (i == lastEditInx)
                        {
                            // Skip the field

                        } else if (agentColumns[i].equals("agent.Visibility"))// User/Security
                                                                                // changes
                        {
                            sqlStr.append(defaultVisibilityLevel);
                        } else if (agentColumns[i].equals("agent.VisibilitySetBy"))// User/Security
                                                                                    // changes
                        {
                            sqlStr.append("null");

                        } else if (agentColumns[i].equalsIgnoreCase("agent.DisciplineID"))
                        {
                            sqlStr.append(getDisciplineId());

                        } else
                        {
                            if (debugAgents)
                            {
                                // log.info(agentColumns[i]);
                            }
                            inx = indexFromNameMap.get(agentColumns[i]);
                            sqlStr.append(BasicSQLUtils.getStrValue(rs.getObject(inx)));
                        }
                    }
                    sqlStr.append("," + getCreatorAgentId(lastEditedBy) + "," + getModifiedByAgentId(lastEditedBy) + ",0");
                    sqlStr.append(")");

                    try
                    {
                        Statement updateStatement = newDBConn.createStatement();
                        // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                        if (debugAgents)
                        {
                            log.info(sqlStr.toString());
                        }
                        updateStatement.executeUpdate(sqlStr.toString());
                        updateStatement.clearBatch();
                        updateStatement.close();
                        updateStatement = null;

                        newRecordsAdded++;

                    } catch (SQLException e)
                    {
                        log.error(sqlStr.toString());
                        log.error("Count: " + recordCnt);
                        e.printStackTrace();
                        log.error(e);
                        throw new RuntimeException(e);
                    }

                }

                if (recordCnt % 250 == 0)
                {
                    log.info("AgentAddress (Agent Only) Records: " + recordCnt);
                }
            } // while
            log.info("AgentAddress (Agent Only) Records: " + recordCnt + "  newRecordsAdded " + newRecordsAdded);

            rs.close();
            stmt.close();

            setProcess(0, BasicSQLUtils.getNumRecords(oldDBConn, "agent"));
            setDesc("Adding Agents");

            // Now Copy all the Agents that where part of an Agent Address Conversions
            stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery("SELECT AgentID from agent");
            recordCnt = 0;
            while (rs.next())
            {
                Integer agentId = rs.getInt(1);
                AgentInfo agentInfo = agentHash.get(agentId);
                if (agentInfo == null || !agentInfo.wasAdded())
                {
                    copyAgentFromOldToNew(agentId, agentIDMapper);
                }
                recordCnt++;
                if (recordCnt % 50 == 0)
                {
                    setProcess(recordCnt);
                }
            }
            setProcess(recordCnt);
            BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);
            /*
             * if (oldAddrIds.size() > 0) { //log.info("Address Record IDs not used by
             * AgentAddress:");
             * 
             * StringBuilder sqlStr = new StringBuilder("select "); List<String> names = new
             * ArrayList<String>(); getFieldNamesFromSchema(oldDBConn, "address", names);
             * sqlStr.append(buildSelectFieldList(names, "address")); sqlStr.append(" from address
             * where AddressId in (");
             * 
             * cnt = 0; for (Enumeration<Integer> e=oldAddrIds.keys();e.hasMoreElements();) {
             * 
             * Integer id = e.nextElement(); Integer val = oldAddrIds.get(id); if (val == 0) {
             * addrIDMapper.put(id, newAddrId); newAddrId++;
             * 
             * if (cnt > 0) sqlStr.append(","); sqlStr.append(id); cnt++; } } sqlStr.append(")");
             * 
             * Hashtable<String, String> map = new Hashtable<String, String>();
             * map.put("PostalCode", "Postalcode"); String[] ignoredFields = {"IsPrimary",
             * "Address2", "Phone1", "Phone2", "Fax", "RoomOrBuilding", "AgentID"};
             * BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields); copyTable(oldDBConn,
             * newDBConn, sqlStr.toString(), "address", "address", map, null); // closes the
             * oldDBConn automatically BasicSQLUtils.setFieldsToIgnoreWhenMappingNames( null); }
             * 
             * if (oldAgentIds.size() > 0) { StringBuilder sqlStr = new StringBuilder("select ");
             * List<String> names = new ArrayList<String>(); getFieldNamesFromSchema(oldDBConn,
             * "agent", names); sqlStr.append(buildSelectFieldList(names, "agent")); sqlStr.append("
             * from agent where AgentId in (");
             * 
             * cnt = 0; for (Enumeration<Integer> e=oldAgentIds.keys();e.hasMoreElements();) {
             * 
             * Integer id = e.nextElement(); Integer val = oldAgentIds.get(id); if (val == 0) {
             * agentIDMapper.put(id, newAgentId); newAgentId++;
             * 
             * if (cnt > 0) sqlStr.append(","); sqlStr.append(id); cnt++; } } sqlStr.append(")");
             * 
             * String[] ignoredFields = {"JobTitle", "Email", "URL", "Visibility",
             * "VisibilitySetBy"};//User/Security changes
             * BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields); copyTable(oldDBConn,
             * newDBConn, sqlStr.toString(), "agent", "agent", null, null);
             * BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);
             * 
             *  } log.info("Agent Address SQL recordCnt "+recordCnt);
             */
            return true;

        } catch (SQLException ex)
        {
            log.error(ex);
            ex.printStackTrace();
            System.exit(0);
            throw new RuntimeException(ex);
        }

    }

    /**
     * 
     */
    public void createAndFillStatTable()
    {
        try
        {
            Statement stmtNew = newDBConn.createStatement();
            String str = "DROP TABLE `webstats`";
            try
            {
                stmtNew.executeUpdate(str);

            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }

            str = "CREATE TABLE `webstats` (`WebStatsID` int(11) NOT NULL default '0', "
                    + "`UniqueVisitors1` int(11), " + "`UniqueVisitors2` int(11), "
                    + "`UniqueVisitors3` int(11), " + "`UniqueVisitors4` int(11), "
                    + "`UniqueVisitors5` int(11), " + "`UniqueVisitors6` int(11), "
                    + "`UniqueVisitorsMon1` varchar(32), " + "`UniqueVisitorsMon2` varchar(32), "
                    + "`UniqueVisitorsMon3` varchar(32), " + "`UniqueVisitorsMon4` varchar(32), "
                    + "`UniqueVisitorsMon5` varchar(32), " + "`UniqueVisitorsMon6` varchar(32), "
                    + "`UniqueVisitorsYear` varchar(32), " + "`Taxon1` varchar(32), "
                    + "`TaxonCnt1` int(11), " + "`Taxon2` varchar(32), " + "`TaxonCnt2` int(11), "
                    + "`Taxon3` varchar(32), " + "`TaxonCnt3` int(11), " + "`Taxon4` varchar(32), "
                    + "`TaxonCnt4` int(11), " + "`Taxon5` varchar(32), " + "`TaxonCnt5` int(11), "
                    + " PRIMARY KEY (`WebStatsID`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1";
            // log.info(str);
            stmtNew.executeUpdate(str);

            str = "INSERT INTO webstats VALUES (0, 234, 189, 211, 302, 229, 276, "
                    + "'Nov', 'Dec', 'Jan', 'Feb', 'Mar', 'Apr', " + " 2621, "
                    + "'Etheostoma',  54," + "'notatus',  39," + "'lutrensis',  22,"
                    + "'anomalum',  12," + "'platostomus',  8" + ")";

            stmtNew.executeUpdate(str);

            stmtNew.clearBatch();
            stmtNew.close();

        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }

    }

    // --------------------------------------------------------------------
    // -- Static Methods
    // --------------------------------------------------------------------

    /**
     * @return wehether it should create the Map Tables, if false it assumes they have already been
     *         created
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

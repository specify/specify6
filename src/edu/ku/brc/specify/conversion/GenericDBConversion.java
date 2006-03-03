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
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.cleanAllTables;
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.copyTable;
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.createFieldNameMap;
import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.deleteAllRecordsFromTable;
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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.TaxonomyTreeDef;
import edu.ku.brc.specify.datamodel.User;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.Encryption;

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
        
        cleanAllTables(); // from DBCOnnection which is the new DB
       
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
       tableMaps.put("borrowreturnmaterial", createFieldNameMap(new String[] {"DateField", "Date1"}));
       tableMaps.put("collectors", createFieldNameMap(new String[] {"OrderNumber", "Order1"}));
       tableMaps.put("determination", createFieldNameMap(new String[] {"CollectionObjectID", "BiologicalObjectID", "IsCurrent", "Current1", "DateField", "Date1", "TaxonID", "TaxonNameID"}));
       tableMaps.put("loanreturnphysicalobject", createFieldNameMap(new String[] {"DateField", "Date1"}));
       tableMaps.put("referencework", createFieldNameMap(new String[] {"DateField", "Date1"}));
       tableMaps.put("stratigraphy", createFieldNameMap(new String[] {"LithoGroup", "Group1"}));
       tableMaps.put("taxoncitation", createFieldNameMap(new String[] {"TaxonID", "TaxonNameID"}));
      
       //tableMaps.put("locality", createFieldNameMap(new String[] {"NationalParkName", "", "ParentID", "TaxonParentID"}));
      
       for (String tableName : tablesToMoveOver)
       {
           if (!copyTable(oldDB.getConnectionToDB(), DBConnection.getConnection(), tableName, tableMaps.get(tableName)))
           {
               log.error("Table ["+tableName+"] didn't copy correctly.");
               break;
           }
       }
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
            
            List<String> newFieldNames = new ArrayList<String>();
            getFieldNamesFromSchema(newDBConn, "collectionobject", newFieldNames);
            
            log.info("Number of Fields in New CollectionObject "+newFieldNames.size());
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
                for (int i=0;i<newFieldNames.size();i++)
                {
                    if (i > 0) str.append(", ");
                    
                    String newFieldName = newFieldNames.get(i);
                    
                    Integer index = oldNameIndex.get(newFieldName);
                    if (index != null)
                    {
                        str.append(getStrValue(rs.getObject(index+1)));
                    } else
                    {
                        log.error("Couldn't find new field name["+newFieldName+"] in old field name Map");
                        stmt.close();
                        oldDBConn.close();
                        newDBConn.close();
                        return false;
                    }

                }
                str.append(")");
                //log.info("\n"+str.toString());
                if (count % 1000 == 0) log.info(count);
                
                try
                {
                    Statement updateStatement = newDBConn.createStatement();
                    updateStatement.executeUpdate(str.toString());
                    updateStatement.clearBatch();
                    
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
     * Creates a new User a new User 
     * @param username the user name of the user
     * @param password the password (not emcrypted)
     * @param privLevel the privLevel
     * @return the user object
     */
    public User createNewUser(final String username, final String password, final short privLevel)
    {
     
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            User user = new User();
            user.setName(username);
            user.setPassword(Encryption.encrypt(password));
            user.setPrivLevel(privLevel);
            
            session.save(user);
            
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
                                              final TaxonomyTreeDef taxaTreeDef,
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
            colObjDef.setTaxonomyTreeDef(set);
            colObjDef.setCatalogSeries(catalogSeriesSet);
            colObjDef.setAttrsDefs(new HashSet<Object>());
            
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
                                            final int geographyTreeDefId )
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
                      createFieldNameMap(new String[] {"TaxonID", "TaxonNameID", "ParentID", "ParentTaxonNameID", "Name", "TaxonName", "FullName", "FullTaxonName"})))
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
        
        if (copyTable(oldDB.getConnectionToDB(), DBConnection.getConnection(), sql, "geography", "locality", null))
        {
            log.info("Locality/Geography copied ok.");
        } else
        {
            log.error("Copying locality/geography (fields) to new Locality");
        }
        BasicSQLUtils.setShowMappingError(showMappingErrors);
    }
    
}

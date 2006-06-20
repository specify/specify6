package edu.ku.brc.specify;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.deleteAllRecordsFromTable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.GenericDBConversion;
import edu.ku.brc.specify.conversion.IdMapperMgr;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.dbsupport.ResultsPager;
import edu.ku.brc.specify.tests.ObjCreatorHelper;

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

    protected void testPaging()
    {
        boolean testPaging = false;
        if (testPaging)
        {
            /*
            long start;
            List list;
            ResultSet rs;
            java.sql.Statement stmt;

            start = System.currentTimeMillis();
            stmt = DBConnection.getConnection().createStatement();
            rs  = stmt.executeQuery("SELECT * FROM collectionobject c LIMIT 31000,32000");
            log.info("JDBC ******************** "+(System.currentTimeMillis() - start));

            Session session = HibernateUtil.getCurrentSession();
            //start = System.currentTimeMillis();
            //list = session.createQuery("from catalogseries in class CatalogSeries").setFirstResult(1).setMaxResults(1000).list();
            //log.info("HIBR ******************** "+(System.currentTimeMillis() - start));


            start = System.currentTimeMillis();
            stmt = DBConnection.getConnection().createStatement();
            rs  = stmt.executeQuery("SELECT * FROM collectionobject c LIMIT 31000,32000");
            log.info("JDBC ******************** "+(System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            list = session.createQuery("from collectionobject in class CollectionObject").setFirstResult(30000).setMaxResults(1000).list();
            log.info("HIBR ******************** "+(System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            list = session.createQuery("from collectionobject in class CollectionObject").setFirstResult(10000).setMaxResults(1000).list();
            log.info("HIBR ******************** "+(System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            list = session.createQuery("from collectionobject in class CollectionObject").setFirstResult(1000).setMaxResults(1000).list();
            log.info("HIBR ******************** "+(System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            stmt = DBConnection.getConnection().createStatement();
            rs  = stmt.executeQuery("SELECT * FROM collectionobject c LIMIT 1000,2000");
            ResultSetMetaData rsmd = rs.getMetaData();
            rs.first();
            while (rs.next())
            {
                for (int i=1;i<=rsmd.getColumnCount();i++)
                {
                    Object o = rs.getObject(i);
                }
            }
            log.info("JDBC ******************** "+(System.currentTimeMillis() - start));

           */

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
                if (pageNo % 100 == 0)
                {
                    log.info("******************** Page "+pageNo+" "+(System.currentTimeMillis() - start) / 1000.0);
                }
                pageNo++;

                for (Object co : list)
                {
                    if (pageNo % 1000 == 0)
                    {
                        log.info(((CollectionObject)co).getCatalogNumber());
                    }
                }
                list.clear();
                System.gc();
            } while (pager.isNextPage());

        }

    }


    /**
     * Utility method to associate an artist with a catObj
     */
    public static void main(String args[]) throws Exception
    {
    	HibernateUtil.initialize();
    	
        String oldDatabaseName = "demo_fish2";
        
        DBConnection.setUsernamePassword("rods", "rods");
        DBConnection.setDriver("com.mysql.jdbc.Driver");
        DBConnection.setDBName("jdbc:mysql://localhost/fish");
        
        if (false)
        {
            GenericDBConversion conversion = new GenericDBConversion("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/"+oldDatabaseName, "rods", "rods");
            conversion.createAndFillStatTable();
            return;
        }

        
        IdMapperMgr idMapperMgr = null;
        try
        {
        	GenericDBConversion.setShouldCreateMapTables(true);
            GenericDBConversion.setShouldDeleteMapTables(true);

            boolean doAll = true; // when converting

            boolean doConvert = true;
            if (doConvert)
            {
                GenericDBConversion conversion = new GenericDBConversion("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/"+oldDatabaseName, "rods", "rods");
                
                idMapperMgr = IdMapperMgr.getInstance();
                idMapperMgr.setDBs(conversion.getOldDBConnection(), conversion.getNewDBConnection());
                
                // NOTE: Within BasicSQLUtils the connection is for removing tables and records
                BasicSQLUtils.setDBConnection(conversion.getNewDBConnection());
                
                // This MUST be done before any of the table copies because it
                // creates the IdMappers for Agent, Address and mor eimportantly AgentAddress
                // NOTE: AgentAddress is actually mapping from the old AgentAddress table to the new Agent table
                boolean copyAgentAddressTables = false;
                if (copyAgentAddressTables || doAll)
                //if (copyAgentAddressTables)
                {
                    conversion.convertAgents();
                    
                } else
                {
                    idMapperMgr.addTableMapper("agent", "AgentID");
                    idMapperMgr.addTableMapper("address", "AddressID");
                    idMapperMgr.addTableMapper("agentaddress", "AgentAddressID");
                }
                
                // GTP needs to be converted here so the stratigraphy conversion can use
                // the IDs
                boolean doGTP = false;
                if( doGTP || doAll )
                {
                	GeologicTimePeriodTreeDef treeDef = conversion.convertGTPDefAndItems();
                	conversion.convertGTP(treeDef);
                }

                boolean mapTables = true;
                if (mapTables || doAll)
                {
                    // Ignore these field names from new table schema when mapping OR
                    // when mapping IDs
                    BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(new String[] {"MethodID",  "RoleID",  "CollectionID",  "ConfidenceID",
                                                                                "TypeStatusNameID",  "ObservationMethodID",  "StatusID",
                                                                                "TypeID",  "ShipmentMethodID", "RankID", "DirectParentRankID",
                                                                                "RequiredParentRankID"});
                    conversion.mapIds();
                    BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(null);
                }

                boolean convertCatalogSeriesDef = false;
                if (convertCatalogSeriesDef || doAll)
                {
                    int specifyUserId = conversion.createDefaultUser("admin");
                    conversion.convertCollectionObjectDefs(specifyUserId);
                    
                } else
                {
                    idMapperMgr.addTableMapper("CatalogSeriesDefinition", "CatalogSeriesDefinitionID");
                    idMapperMgr.addTableMapper("CollectionObjectType", "CollectionObjectTypeID");
                }
                

                boolean copyUSYSTables = false;
                if (copyUSYSTables || doAll)
                {
                    //conversion.convertUSYSTables();
                }

                boolean copyTables = false;
                if (copyTables || doAll)
                {
                    conversion.copyTables();
                }

                boolean doCollectionObjects = true;
                if (doCollectionObjects || doAll)
                {
                    if (true)
                    {
                        Map<String, PrepType> prepTypeMap = conversion.createPreparationTypesFromUSys();
                        prepTypeMap.put("n/a", prepTypeMap.get("misc"));
                        conversion.createPreparationRecords(prepTypeMap);
                    }
                    conversion.createCollectionRecords();
                }
                
                boolean doTaxonomy = false;
                if( doTaxonomy || doAll )
                {
                	conversion.copyTaxonTreeDefs();
                	conversion.convertTaxonTreeDefItems();
                	conversion.copyTaxonRecords();
                }
                
                boolean doGeography = false;
                if (doGeography || doAll)
                {
                	GeographyTreeDef treeDef = conversion.createStandardGeographyDefinitionAndItems();
                	conversion.convertGeography(treeDef);
                	conversion.convertLocality();
                }
                
                boolean doLocation = false;
                if( doLocation || doAll )
                {
                	conversion.buildSampleLocationTreeDef();
                }
                
                boolean doFurtherTesting = false;
                if (doFurtherTesting)
                {

                    BasicSQLUtils.deleteAllRecordsFromTable("datatype");
                    BasicSQLUtils.deleteAllRecordsFromTable("user");
                    BasicSQLUtils.deleteAllRecordsFromTable("usergroup");
                    BasicSQLUtils.deleteAllRecordsFromTable("collectionobjdef");

                    DataType          dataType  = ObjCreatorHelper.createDataType("Animal");
                    UserGroup         userGroup = ObjCreatorHelper.createUserGroup("Fish");
                    SpecifyUser       user      = ObjCreatorHelper.createSpecifyUser("rods", "rods", (short)0, userGroup);



                    Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CatalogSeries.class);
                    criteria.add(Expression.eq("catalogSeriesId", new Integer(0)));
                    java.util.List catalogSeriesList = criteria.list();

                    boolean doAddTissues = false;
                    if (doAddTissues)
                    {
                        deleteAllRecordsFromTable("catalogseries");
                        try
                        {
                            Session session = HibernateUtil.getCurrentSession();
                            HibernateUtil.beginTransaction();

                            CatalogSeries voucherSeries = null;
                            if (catalogSeriesList.size() == 0)
                            {
                                voucherSeries = new CatalogSeries();
                                voucherSeries.setIsTissueSeries(false);
                                voucherSeries.setTimestampCreated(new Date());
                                voucherSeries.setTimestampModified(new Date());
                                voucherSeries.setCatalogSeriesId(100);
                                voucherSeries.setCatalogSeriesPrefix("KUFISH");
                                voucherSeries.setSeriesName("Fish Collection");
                                session.saveOrUpdate(voucherSeries);

                            } else
                            {
                                voucherSeries = (CatalogSeries)catalogSeriesList.get(0);
                            }

                            if (voucherSeries != null)
                            {
                                CatalogSeries tissueSeries = new CatalogSeries();
                                tissueSeries.setIsTissueSeries(true);
                                tissueSeries.setTimestampCreated(new Date());
                                tissueSeries.setTimestampModified(new Date());
                                tissueSeries.setCatalogSeriesId(101);
                                tissueSeries.setCatalogSeriesPrefix("KUTIS");
                                tissueSeries.setSeriesName("Fish Tissue");
                                session.saveOrUpdate(tissueSeries);

                                voucherSeries.setTissue(tissueSeries);
                                session.saveOrUpdate(voucherSeries);

                                HibernateUtil.commitTransaction();
                            }

                        } catch (Exception e)
                        {
                            log.error("******* " + e);
                            e.printStackTrace();
                            HibernateUtil.rollbackTransaction();
                        }
                        return;
                    }

                        Set<CollectionObjDef>  colObjDefSet = conversion.createCollectionObjDef("Fish", dataType, user, null, null);//(CatalogSeries)catalogSeriesList.get(0));


                        Object obj = colObjDefSet.iterator().next();
                        CollectionObjDef colObjDef = (CollectionObjDef)obj;

                        conversion.convertBiologicalAttrs(colObjDef, null, null);
                }
            }

            if (GenericDBConversion.shouldDeleteMapTables())
            {
                idMapperMgr.cleanup();
            }


            log.info("Done.");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
            if (idMapperMgr != null && GenericDBConversion.shouldDeleteMapTables())
            {
                idMapperMgr.cleanup();
            }

        }
    }
}

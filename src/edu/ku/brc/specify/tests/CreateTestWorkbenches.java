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
package edu.ku.brc.specify.tests;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;

/**
 * @author megkumin
 *
 */
public class CreateTestWorkbenches {
    private static final Logger log = Logger.getLogger(CreateTestWorkbenches.class);
    protected static Calendar calendar = Calendar.getInstance();
	public CreateTestWorkbenches() {
		calendar.clear();
		// TODO Auto-generated constructor stub
	}
  
	
    /**
     * Retuturns the first item from a table
     * @param classObj the class of the item to get
     * @return null if no items in table
     */
    public static Object getDBObject(Class classObj)
    {
        return getDBObject(classObj, 0);
    }

    /**
     * Retuturns the first item from a table
     * @param classObj the class of the item to get
     * @return null if no items in table
     */
    public static Object getDBObject(Class classObj, final int index)
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(classObj);
        java.util.List list = criteria.list();
        if (list.size() == 0) return null;

        return list.get(index);
    }
    
    /**
     * Removes all the records from all the workbench tables from the current DBConnection
     */
    public static void cleanWorkbenchTables()
    {	log.info("cleanWorkbenchTables");
        try
        {
            Connection connection = DBConnection.getConnection();

            BasicSQLUtils.deleteAllRecordsFromTable(connection, "Workbench");
            BasicSQLUtils.deleteAllRecordsFromTable(connection, "WorkbenchDataItem");
            BasicSQLUtils.deleteAllRecordsFromTable(connection, "WorkbenchTemplate");
            BasicSQLUtils.deleteAllRecordsFromTable(connection, "WorkbenchTemplateMappingItem");

            connection.close();

        } catch (SQLException ex)
        {
            //e.printStackTrace();
            log.error(ex);
        }
    }  
    
    
    /**
     * Mock up a test template
     * @return true on success
     */
    public static boolean createTestSimpleTemplate() {
    	log.info("createSimpleTemplate");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            ObjCreatorHelper.setSession(session);
            HibernateUtil.beginTransaction();           
            WorkbenchTemplate testtemplate = ObjCreatorHelper.createWorkbenchTemplate("Test Template1", "This is a template for testing the application");
            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();  
            return true;
        }
        catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return true;    
    }
    

    /**
     * Given a name and description, create a template definition
     * @param templateName The name of the template to be created
     * @param templateDescription A description of the template being created
     * @return
     */
    public static boolean createTestSimpleTemplate(String templateName, String templateDescription) {
    	log.info("createSimpleTemplate");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            ObjCreatorHelper.setSession(session);
            HibernateUtil.beginTransaction();           
            WorkbenchTemplate testtemplate = ObjCreatorHelper.createWorkbenchTemplate(templateName, templateDescription);
            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();  
            return true;
        }
        catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return true;    
    }       
    /**
     * Create a test template with associated mapping values.
     * @return true on success
     */
    public static boolean createTestTemplateWithMappings() {
    	log.info("createTestWorkbench");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            ObjCreatorHelper.setSession(session);
            HibernateUtil.beginTransaction();           
            WorkbenchTemplate testtemplate = ObjCreatorHelper.createWorkbenchTemplate("Test Template", "This is a template for testing the application");
            
            String[][] mappingvalues = {
            		{"table1", "0", "field1", "captions1", "0", "String"},
            		{"table2", "1",  "field2", "captions2", "1", "String"},
            		{"table3", "2", "field3", "captions3", "2", "String"},
            		{"table4",  "3", "field4", "captions4", "3", "String"} };            
            addMappingItemsToTemplate(testtemplate, mappingvalues);
                        
            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();  
            return true;
        }
        catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return true;        
    }
    
    /**
     * Given a template, mock up some sample mapping values
     * @param template The template that should have the mapping items assigned to it.
     * @return true on success
     */
    public static boolean createTestSimpleTemplateMappingItems(final WorkbenchTemplate template) {
        Session session = HibernateUtil.getCurrentSession();
        ObjCreatorHelper.setSession(session);
        HibernateUtil.beginTransaction();           
    	String[][] mappingvalues = {
    			{"atable1",  "0", "afield1", "acaptions1", "0", "String"},
    			{"atable2",  "1",  "afield2", "acaptions2", "1", "String"} };            
    	addMappingItemsToTemplate(template, mappingvalues);  
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();  
        return true;        
    }
    
    /**
     * Creates and entire Workbench set.  Starts by creating a template, 
     * then assigns mapping items to the template.  Creates a workbench definition,
     * then populates the workbench with data.
     * Creates 3 full workbench set.
     * 
     * @return true on success
     */
    public static boolean createTestWorkbenchSet()
    {
    	log.info("createTestWorkbenchSet");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            ObjCreatorHelper.setSession(session);
            
            HibernateUtil.beginTransaction();            
            WorkbenchTemplate cefishtemplate = ObjCreatorHelper.createWorkbenchTemplate("Fish Template", "This is a template for KU fish");            
            String[][] mappingvalues = {
            		{"CollectingEvent",  "0",  "VerbatimLocality", "Locality", "3", "String"},
            		{"CollectingEvent", "1",  "StationFieldNumber", "Station Num", "4", "String"},
            		{"CollectingEvent","2",   "VerbatimDate", "Date","5","String"},
            		{"Locality", "3",  "Latitude1","Latitude1","6","String"},
            		{"Locality", "4",  "Longitude1","Longitude","7","String"},
            		{"Taxon",  "5",  "FullTaxon", "Full Taxon Name", "2", "String"},
            		{"Agent",  "6",  "FirstName","First Name", "0","String"},
            		{"Agent", "7",   "LastName","Last Name", "1","String"} };            
            addMappingItemsToTemplate(cefishtemplate, mappingvalues);
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();    
            Workbench wb1 = ObjCreatorHelper.createWorkbench("Seychelles 2005", "Workbench for entering data from Seychelles fishing expedition in May 2005", "", 0, cefishtemplate);            
            String[][] workbenchdata = {
            		{"Seychelles", "ACB-05-02", "April 14 2005", "9.22", "46.01", "Monodadyla argenteus", "Andy", "Bentely"},
            		{"Seychelles", "ACB-05-02", "April 14 2005", "9.22", "46.01", "Synnudus variegatus", "Andy", "Bentely"},
            		{"Seychelles", "ACB-05-02", "April 14 2005", "9.22", "46.01", "Corythoenthys sp", "Andy", "Bentely"},
            		{"Seychelles", "ACB-05-02", "April 14 2005", "9.22", "46.01", "Corythoenthys sp", "Andy", "Bentely"},
            		{"Seychelles", "ACB-05-04", "April 16 2005", "", "", "Lotjunius songuinus", "Andy", "Bentely"},
            		{"Seychelles", "ACB-05-04", "April 16 2005", "", "", "Esthunus affinis", "Andy", "Bentely"},
                                                             
            }; 
            addDataToWorkbench(wb1, workbenchdata);            
            HibernateUtil.commitTransaction();
                
            HibernateUtil.beginTransaction();
            Workbench wb2 = ObjCreatorHelper.createWorkbench("Kaw River 2002", "Workbench for entering specimens collected from the Kaw River", "", 0, cefishtemplate);
            String[][] workbenchdata1 ={
            		{"Kaw River", "KR-02-11", "Sep 4 2002", "37.57", "95.16", "Macrhybopsis aestivalis", "Andy", "Bentely"},
            		{"Kaw River", "KR-02-11", "Sep 4 2002", "37.57", "95.16", "Macrhybopsis aestivalis", "Andy", "Bentely"},
            		{"Kaw River", "KR-02-11", "Sep 4 2002", "37.57", "95.16", "Macrhybopsis aestivalis", "Andy", "Bentely"},
            		{"Kaw River", "KR-02-11", "Sep 4 2002", "37.57", "95.16", "Catfishiotis sp", "Andy", "Bentely"},
            		{"Kaw River", "KR-02-11", "Sep 4 2002", "37.57", "95.16", "Catfishiotis longisuinus", "Andy", "Bentely"},
            		{"Kaw River", "KR-02-11", "Sep 4 2002", "37.57", "95.16", "Catfishiotis maximotis", "Andy", "Bentely"},
                                                             
            };    
            addDataToWorkbench(wb2, workbenchdata1);   
            HibernateUtil.commitTransaction();

            HibernateUtil.beginTransaction();    
            WorkbenchTemplate giftfishtemplate = ObjCreatorHelper.createWorkbenchTemplate("Gift Template", "This is a template for gifting fish to KU fish");
            String[][] mappingvalues1 = {
            		{"Accession", "0",  "Number", "Accession Number", "0", "String"},
            		{"CollectionObject", "1",  "AltCatalogNumber", "Original Catalog Number", "1", "String"},
            		{"Taxon",  "2",  "FullTaxon", "Full Taxon Name", "6", "String"},};   
            addMappingItemsToTemplate(giftfishtemplate, mappingvalues1);
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();       
            Workbench wb4 = ObjCreatorHelper.createWorkbench("Gift from University of Missouri","Acquisition of MU's fish collection", "", 0, giftfishtemplate);
            String[][] workbenchdata2 ={
            		{"2006-ic-0001","MU123","lexisus minius"},  
            		{"2006-ic-0001","MU124","goldfishis goldiana"},   
            		{"2006-ic-0001","MU125","catfisher friemuper"},   
            		{"2006-ic-0001","MU126","carpobottum inthemudis"}};    
            addDataToWorkbench(wb4, workbenchdata2);   
            HibernateUtil.commitTransaction();               
        }catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return true;
    }
    
    /**
     * Given a template, create a simple, "empty" workbench
     * @param template that defines the Workbench
     * @return true on success
     */
    public static boolean createTestSimpleWorkbench(final WorkbenchTemplate template)
    {
    	log.info("createTestWorkbench");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            ObjCreatorHelper.setSession(session);
            HibernateUtil.beginTransaction();                       
            Workbench wb1 = ObjCreatorHelper.createWorkbench("Test WB", "Test workbench", "", 0, template);                    
            HibernateUtil.commitTransaction();         
        }catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
        return true;
    } 
    
    /**
     * Given a template, create and assign the following mapping items
     * @param template The template for which the mapping items are being created
     * @param values Each array has the data for a mapping items {tableName, fieldName, caption, viewOrder, dataType}
     * @return true on success
     */
    public static boolean addMappingItemsToTemplate(WorkbenchTemplate template, String[][] values)
    {
        log.info("addMappingItemsToTemplate");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            ObjCreatorHelper.setSession(session);
            HibernateUtil.beginTransaction();

            WorkbenchTemplateMappingItem[] mappingitems = new WorkbenchTemplateMappingItem[values.length];
            for (int i=0;i<values.length;i++)
            {     
            	String [] array = values[i];            	
    			String tableName = array[0];
    			Integer tableId =Integer.parseInt(array[1]);   
    			String fieldName = array[2];
    			String caption = array[3];
    			String dataType = array[5];
    			Integer viewOrder =Integer.parseInt(array[4]);  
    			
            	mappingitems[i] = ObjCreatorHelper.createMappingItem(tableName, tableId, fieldName, caption, dataType, viewOrder, template);
            }
            HibernateUtil.commitTransaction();
            return true;
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return false;
    }       
    /**
     * Given a workbench, creates WorkbenchDataItem for each row of data
     * @param workbench That the data is being assigned to.
     * @param values arrays of values that make up the data, each array is one set of rowdata 
     * @return true on success
     */
    public static boolean addDataToWorkbench(Workbench workbench, String[][] values)
    {
        log.info("addDataToWorkbench");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            ObjCreatorHelper.setSession(session);
            HibernateUtil.beginTransaction();

            WorkbenchDataItem[][] dataitems = new WorkbenchDataItem[values.length][values[0].length];
            for (int i=0;i<values.length;i++)
            {
            	String [] smallarray = values[i];
            	String rowData = "";
            	for(int j = 0; j < smallarray.length; j++) {
            		 rowData = smallarray[j];
            		 dataitems[i][j] = ObjCreatorHelper.createWorkbenchDataItem("" + i +"", "" + j +"",rowData, workbench);
            		 
            	}
            	
            }

            HibernateUtil.commitTransaction();
            return true;
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return false;
    }   
	/**
	 * @param args
	 */
	public static void main(String[] args) 
    {
        DBConnection dbConn = DBConnection.getInstance();
        dbConn.setUsernamePassword("rods", "rods");
        dbConn.setDriver("com.mysql.jdbc.Driver");
        dbConn.setConnectionStr("jdbc:mysql://localhost/");
        dbConn.setDatabaseName("demo_fish3");
        
		cleanWorkbenchTables();
		createTestWorkbenchSet();	
	}

}

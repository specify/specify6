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
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.UIHelper;
import static edu.ku.brc.specify.tests.HibernateHelper.stopHibernateTransaction;
import static edu.ku.brc.specify.tests.HibernateHelper.startHibernateTransaction;

/*
 * @code_status Unknown (auto-generated)
 **
 * @author megkumin
 *
 */
/**
 * @author megkumin
 *
 */
public class WorkbenchTestHelper {
	private static final Logger log = Logger.getLogger(WorkbenchTestHelper.class);
	/**
	 * 
	 */
	public WorkbenchTestHelper() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//        String databaseName = "fish";
//        // This will log us in and return true/false
//        if (!UIHelper.tryLogin("com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", databaseName, "jdbc:mysql://localhost/", "rods", "rods"))
//        {
//            throw new RuntimeException("Couldn't login into ["+databaseName+"] "+DBConnection.getInstance().getErrorMsg());
//        }Connection connection = DBConnection.getInstance().createConnection();
//        //UIHelper.
//        try
//        {
//            Statement stmt = connection.createStatement();
//        } catch (SQLException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        BasicSQLUtils.setDBConnection(connection);
//        BasicSQLUtils.deleteAllRecordsFromTable("workbenchdataitem");
//        BasicSQLUtils.deleteAllRecordsFromTable("workbenchtemplatemapping");
//        BasicSQLUtils.deleteAllRecordsFromTable("workbench");
//        BasicSQLUtils.deleteAllRecordsFromTable("workbenchtemplate");
	}
    /**
     * @param templateId
     * @return
     */
    public static boolean isTemplateInDB(int templateId) {
    	log.info("isTemplateInDB");
		boolean templateIsFound = false;	
        
		Criteria criteria = startHibernateTransaction().createCriteria(WorkbenchTemplate.class);//HibernateUtil.getCurrentSession().createCriteria(WorkbenchTemplate.class);
		java.util.List list = criteria.list();

		// make sure that the template that was just created is in teh database
		for (int i = 0; i < list.size(); i++) {
			WorkbenchTemplate template = (WorkbenchTemplate) list.get(i);
			if (template.getWorkbenchTemplateId() == templateId) 
				templateIsFound = true;
		}
        //shutdownHibernateTransaction();
		return templateIsFound;
	}
    
    /**
     * @param templateMappingId
     * @return
     */
    public static boolean isTemplateMappingItemInDB(int templateMappingId) {
		boolean mappingItemIsFound = false;		
		Criteria criteria = startHibernateTransaction().createCriteria(WorkbenchTemplateMappingItem.class);////HibernateUtil.getCurrentSession().createCriteria(WorkbenchTemplateMappingItem.class);
		java.util.List list = criteria.list();

		// make sure that the template that was just created is in teh database
		for (int i = 0; i < list.size(); i++) {
			WorkbenchTemplateMappingItem item = (WorkbenchTemplateMappingItem) list.get(i);
			if (item.getWorkbenchTemplateMappingItemId() == templateMappingId) 
				mappingItemIsFound = true;
		}
        //shutdownHibernateTransaction();
		return mappingItemIsFound;
	}    
        
    /**
     * @param workbenchId
     * @return
     */
    public static boolean isWorkbenchInDB(int workbenchId) {
		boolean mappingItemIsFound = false;		
		Criteria criteria = startHibernateTransaction().createCriteria(Workbench.class);//HibernateUtil.getCurrentSession().createCriteria(Workbench.class);
		java.util.List list = criteria.list();

		// make sure that the template that was just created is in teh database
		for (int i = 0; i < list.size(); i++) {
			Workbench workbench = (Workbench) list.get(i);
			if (workbench.getWorkbenchId() == workbenchId) 
				mappingItemIsFound = true;
		}
        //shutdownHibernateTransaction();
		return mappingItemIsFound;
	}    
 
    /**
     * @param templateId
     * @return
     */
    public static boolean deleteTemplateFromDB(int templateId) {
    	boolean isTemplateDeleted = false;
    	try {
			Session session = startHibernateTransaction();
            //Session session = HibernateUtil.getCurrentSession();
			//ObjCreatorHelper.setSession(session);
			//HibernateUtil.beginTransaction();
			Criteria criteria = session.createCriteria(WorkbenchTemplate.class);
			java.util.List list = criteria.list();
			
			//make sure that the template that was just created is in teh database
			for(int i = 0; i< list.size(); i++) {
				WorkbenchTemplate template = (WorkbenchTemplate)list.get(i);
				if(template.getWorkbenchTemplateId()== templateId) {
					session.delete(template);
					isTemplateDeleted = true;					
				}
			}
            stopHibernateTransaction();
			return isTemplateDeleted;
		} catch (Exception ex) {
			log.error("******* " + ex);
			ex.printStackTrace();
			HibernateUtil.rollbackTransaction();
			return false;
		} 
    } 
    
    
    /**
     * @param mappingItemId
     * @return
     */
    public static boolean deleteMappingItemFromDB(int mappingItemId) {
    	boolean isTemplateDeleted = false;
    	try {
            //Session session = HibernateUtil.getCurrentSession();
			Session session = startHibernateTransaction();
			//ObjCreatorHelper.setSession(session);
			//HibernateUtil.beginTransaction();
			Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(WorkbenchTemplateMappingItem.class);
			java.util.List list = criteria.list();
			
			//make sure that the template that was just created is in teh database
			for(int i = 0; i< list.size(); i++) {
				WorkbenchTemplateMappingItem item = (WorkbenchTemplateMappingItem)list.get(i);
				if(item.getWorkbenchTemplateMappingItemId()== mappingItemId) {
					session.delete(item);
					isTemplateDeleted = true;					
				}
			}
            stopHibernateTransaction();
			return isTemplateDeleted;
		} catch (Exception ex) {
			log.error("******* " + ex);
			ex.printStackTrace();
			HibernateUtil.rollbackTransaction();
			return false;
		} 
    }     
   
    /**
     * @param workbenchId
     * @return
     */
    public static boolean deleteWorkbenchFromDB(int workbenchId) {
    	boolean isWorkbenchDeleted = false;
    	try {
			Session session = startHibernateTransaction();
			//ObjCreatorHelper.setSession(session);
			//HibernateUtil.beginTransaction();
            
			Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Workbench.class);
			java.util.List list = criteria.list();
			
			//make sure that the template that was just created is in teh database
			for(int i = 0; i< list.size(); i++) {
				Workbench workbench = (Workbench)list.get(i);
				if(workbench.getWorkbenchId()== workbenchId) {
					session.delete(workbench);
					isWorkbenchDeleted = true;					
				}
			}
            stopHibernateTransaction();
			return isWorkbenchDeleted;
		} catch (Exception ex) {
			log.error("******* " + ex);
			ex.printStackTrace();
			HibernateUtil.rollbackTransaction();
			return false;
		} 
    }    
    
    /**
     * 
     */
//    public static void startHibernateTransaction() {
//		Session session = HibernateUtil.getCurrentSession();
//		ObjCreatorHelper.setSession(session);			
//        HibernateUtil.beginTransaction();
//    }
    
    /**
     * 
     */
//    public static void shutdownHibernateTransaction() {
//        HibernateUtil.commitTransaction();
//        HibernateUtil.closeSession();  
//    } 
}

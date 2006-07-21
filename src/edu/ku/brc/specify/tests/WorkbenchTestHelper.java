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

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.dbsupport.HibernateUtil;


/**
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
		// TODO Auto-generated method stub

	}
    /**
     * @param templateId
     * @return
     */
    public static boolean isTemplateInDB(int templateId) {
    	log.info("isTemplateInDB");
		boolean templateIsFound = false;		
		Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(WorkbenchTemplate.class);
		java.util.List list = criteria.list();

		// make sure that the template that was just created is in teh database
		for (int i = 0; i < list.size(); i++) {
			WorkbenchTemplate template = (WorkbenchTemplate) list.get(i);
			if (template.getWorkbenchTemplateID() == templateId) 
				templateIsFound = true;
		}
		return templateIsFound;
	}
    
    /**
     * @param templateMappingId
     * @return
     */
    public static boolean isTemplateMappingItemInDB(int templateMappingId) {
		boolean mappingItemIsFound = false;		
		Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(WorkbenchTemplateMappingItem.class);
		java.util.List list = criteria.list();

		// make sure that the template that was just created is in teh database
		for (int i = 0; i < list.size(); i++) {
			WorkbenchTemplateMappingItem item = (WorkbenchTemplateMappingItem) list.get(i);
			if (item.getWorkbenchTemplateMappingItemID() == templateMappingId) 
				mappingItemIsFound = true;
		}
		return mappingItemIsFound;
	}    
        
    /**
     * @param workbenchId
     * @return
     */
    public static boolean isWorkbenchInDB(int workbenchId) {
		boolean mappingItemIsFound = false;		
		Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Workbench.class);
		java.util.List list = criteria.list();

		// make sure that the template that was just created is in teh database
		for (int i = 0; i < list.size(); i++) {
			Workbench workbench = (Workbench) list.get(i);
			if (workbench.getWorkbenchID() == workbenchId) 
				mappingItemIsFound = true;
		}
		return mappingItemIsFound;
	}    
 
    /**
     * @param templateId
     * @return
     */
    public static boolean deleteTemplateFromDB(int templateId) {
    	boolean isTemplateDeleted = false;
    	try {
			Session session = HibernateUtil.getCurrentSession();
			ObjCreatorHelper.setSession(session);
			HibernateUtil.beginTransaction();
			Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(WorkbenchTemplate.class);
			java.util.List list = criteria.list();
			
			//make sure that the template that was just created is in teh database
			for(int i = 0; i< list.size(); i++) {
				WorkbenchTemplate template = (WorkbenchTemplate)list.get(i);
				if(template.getWorkbenchTemplateID()== templateId) {
					session.delete(template);
					isTemplateDeleted = true;					
				}
			}
			HibernateUtil.commitTransaction();
			HibernateUtil.closeSession();
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
			Session session = HibernateUtil.getCurrentSession();
			ObjCreatorHelper.setSession(session);
			HibernateUtil.beginTransaction();
			Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(WorkbenchTemplateMappingItem.class);
			java.util.List list = criteria.list();
			
			//make sure that the template that was just created is in teh database
			for(int i = 0; i< list.size(); i++) {
				WorkbenchTemplateMappingItem item = (WorkbenchTemplateMappingItem)list.get(i);
				if(item.getWorkbenchTemplateMappingItemID()== mappingItemId) {
					session.delete(item);
					isTemplateDeleted = true;					
				}
			}
			HibernateUtil.commitTransaction();
			HibernateUtil.closeSession();
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
			Session session = HibernateUtil.getCurrentSession();
			ObjCreatorHelper.setSession(session);
			HibernateUtil.beginTransaction();
			Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Workbench.class);
			java.util.List list = criteria.list();
			
			//make sure that the template that was just created is in teh database
			for(int i = 0; i< list.size(); i++) {
				Workbench workbench = (Workbench)list.get(i);
				if(workbench.getWorkbenchID()== workbenchId) {
					session.delete(workbench);
					isWorkbenchDeleted = true;					
				}
			}
			HibernateUtil.commitTransaction();
			HibernateUtil.closeSession();
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
    public static void startHibernateTransaction() {
		Session session = HibernateUtil.getCurrentSession();
		ObjCreatorHelper.setSession(session);			
        HibernateUtil.beginTransaction();
    }
    
    /**
     * 
     */
    public static void shutdownHibernateTransaction() {
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();  
    } 
}

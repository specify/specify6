/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.tests;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;

import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
//import edu.ku.brc.specify.datamodel.UserPermission;
/**
 * 
 * @code_status Unknown
 * 
 * @author megkumin
 *
 */
public class SpecifyUserTestHelper
{
    private static final Logger log = Logger.getLogger(SpecifyUserTestHelper.class);
    /**
     * Constructor 
     */
    public SpecifyUserTestHelper()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param userId
     * @return
     */
    public static boolean isSpecifyUserInDB(Integer userId) {
        log.info("isSpecifyUserInDB: " + userId);
        if(userId==null)return false;
        boolean found = false;        
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(SpecifyUser.class);
        java.util.List<?> list = criteria.list();

        // make sure that the template that was just created is in teh database
        for (int i = 0; i < list.size(); i++) {
            SpecifyUser user = (SpecifyUser) list.get(i);
            if (user.getId().equals(userId)) 
                found = true;
        }
        return found;
    }
//    
//    /**
//     * @param permId
//     * @return
//     */
//    public static boolean isUserPermissionInDB(Integer permId) {   
//        log.info("isUserPermissionInDB(Integer): " + permId);
//        if(permId==null)return false;
//        boolean found = false;        
//        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(UserPermission.class);
//        java.util.List<?> list = criteria.list();
//
//        for (int i = 0; i < list.size(); i++) {
//            UserPermission perm = (UserPermission) list.get(i);
//            if (perm.getId().equals(permId)) 
//                found = true;
//        }
//        return found;
//    }
    /**
     * @param groupId
     * @return
     */
    public static boolean isUserGroupInDB(Integer groupId) {
        log.info("isUserGroupInDB: " + groupId);
        if(groupId==null)return false;
        boolean found = false;        
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(SpPrincipal.class);
        java.util.List<?> list = criteria.list();

        for (int i = 0; i < list.size(); i++) {
            SpPrincipal user = (SpPrincipal) list.get(i);
            if (user.getId().equals(groupId))
                found = true;
        }
        return found;
    } 
    
    /**
     * @param groupId
     * @return
     */
    public static boolean deleteUserGroupFromDB(Integer groupId) {
        log.info("deleteUserGroupFromDB(): " + groupId);
        boolean isDeleted = false;
        try {
            HibernateUtil.beginTransaction();
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(SpPrincipal.class);
            java.util.List<?> list = criteria.list();
            
            for(int i = 0; i< list.size(); i++) {
                SpPrincipal group = (SpPrincipal)list.get(i);
                if(group.getUserGroupId().equals(groupId)) {
                    HibernateUtil.getCurrentSession().delete(group);
                    isDeleted = true;                   
                }
            }
            HibernateUtil.commitTransaction();
            return isDeleted;
        } catch (Exception ex) {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            return false;
        } 
    } 
    
    /**
     * @param userId
     * @return
     */
    public static boolean deleteSpecifyUserDB(Integer userId) {
        log.info("deleteSpecifyUserDB(): " + userId);
        boolean isDeleted = false;
        try {
            HibernateUtil.beginTransaction();
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(SpecifyUser.class);
            java.util.List<?> list = criteria.list();
            
            for(int i = 0; i< list.size(); i++) {
                SpecifyUser user = (SpecifyUser)list.get(i);
                if(user.getId().equals(userId)) {
                    HibernateUtil.getCurrentSession().delete(user);
                    isDeleted = true;                   
                }
            }
            HibernateUtil.commitTransaction();
            return isDeleted;
        } catch (Exception ex) {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            return false;
        } 
    }
    
//    /**
//     * @param permId
//     * @return
//     */
//    public static boolean deleteUserPermissionFromDB(Integer permId) {
//        log.info("deleteUserPermissionFromDB(): " + permId);
//        boolean isDeleted = false;
//        try {
//            HibernateUtil.beginTransaction();
//            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(UserPermission.class);
//            java.util.List<?> list = criteria.list();
//            
//            for(int i = 0; i< list.size(); i++) {
//                UserPermission perm = (UserPermission)list.get(i);
//                if(perm.getId().equals(permId)) {
//                    HibernateUtil.getCurrentSession().delete(perm);
//                    isDeleted = true;                   
//                }
//            }
//            HibernateUtil.commitTransaction();
//            return isDeleted;
//            
//        } catch (Exception ex) {
//            log.error("******* " + ex);
//            ex.printStackTrace();
//            HibernateUtil.rollbackTransaction();
//            return false;
//        } 
//    } 
}

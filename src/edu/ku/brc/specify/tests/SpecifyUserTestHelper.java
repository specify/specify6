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

import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.datamodel.UserPermission;
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
    public static boolean isSpecifyUserInDB(Long userId) {
        log.info("isSpecifyUserInDB: " + userId);
        if(userId==null)return false;
        boolean found = false;        
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(SpecifyUser.class);
        java.util.List list = criteria.list();

        // make sure that the template that was just created is in teh database
        for (int i = 0; i < list.size(); i++) {
            SpecifyUser user = (SpecifyUser) list.get(i);
            if (user.getId().equals(userId)) 
                found = true;
        }
        return found;
    }
    
    /**
     * @param permId
     * @return
     */
    public static boolean isUserPermissionInDB(Long permId) {   
        log.info("isUserPermissionInDB(Long): " + permId);
        if(permId==null)return false;
        boolean found = false;        
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(UserPermission.class);
        java.util.List list = criteria.list();

        for (int i = 0; i < list.size(); i++) {
            UserPermission perm = (UserPermission) list.get(i);
            if (perm.getId().equals(permId)) 
                found = true;
        }
        return found;
    }
    /**
     * @param groupId
     * @return
     */
    public static boolean isUserGroupInDB(Long groupId) {
        log.info("isUserGroupInDB: " + groupId);
        if(groupId==null)return false;
        boolean found = false;        
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(UserGroup.class);
        java.util.List list = criteria.list();

        for (int i = 0; i < list.size(); i++) {
            UserGroup user = (UserGroup) list.get(i);
            if (user.getId().equals(groupId))
                found = true;
        }
        return found;
    } 
    
    /**
     * @param groupId
     * @return
     */
    public static boolean deleteUserGroupFromDB(Long groupId) {
        log.info("deleteUserGroupFromDB(): " + groupId);
        boolean isDeleted = false;
        try {
            HibernateUtil.beginTransaction();
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(UserGroup.class);
            java.util.List list = criteria.list();
            
            for(int i = 0; i< list.size(); i++) {
                UserGroup group = (UserGroup)list.get(i);
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
    public static boolean deleteSpecifyUserDB(Long userId) {
        log.info("deleteSpecifyUserDB(): " + userId);
        boolean isDeleted = false;
        try {
            HibernateUtil.beginTransaction();
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(SpecifyUser.class);
            java.util.List list = criteria.list();
            
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
    
    /**
     * @param permId
     * @return
     */
    public static boolean deleteUserPermissionFromDB(Long permId) {
        log.info("deleteUserPermissionFromDB(): " + permId);
        boolean isDeleted = false;
        try {
            HibernateUtil.beginTransaction();
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(UserPermission.class);
            java.util.List list = criteria.list();
            
            for(int i = 0; i< list.size(); i++) {
                UserPermission perm = (UserPermission)list.get(i);
                if(perm.getId().equals(permId)) {
                    HibernateUtil.getCurrentSession().delete(perm);
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
}

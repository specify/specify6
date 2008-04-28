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
package edu.ku.brc.specify.tests;


import static edu.ku.brc.specify.tests.SpecifyUserTestHelper.deleteSpecifyUserDB;
import static edu.ku.brc.specify.tests.SpecifyUserTestHelper.isSpecifyUserInDB;
import static edu.ku.brc.specify.config.init.DataBuilder.createSpecifyUser;
import junit.framework.TestCase;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * 
 * @code_status alpha
 * 
 * @author megkumin
 * 
 */
public class SpecifyUserTest extends TestCase
{
    private static final Logger log = Logger.getLogger(SpecifyUserTest.class);

    /**
     * Constructor
     * 
     * @param arg0
     */
    public SpecifyUserTest(String arg0)
    {
        super(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {

        super.setUp();
        AppPreferenceHelper.setupPreferences();
        HibernateUtil.beginTransaction();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        HibernateUtil.commitTransaction();
    }

    public void testCreateSingleUser()
    {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        log.info("Testing creating and deleting SpecifyUser");
        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        String testUserName = "testuser";
        String testUserPWd = "testpwd";
        String testUserEmail = "testuser@ku.edu";
        String testUserRole = "testType";
        try
        {
            log.info("Creating SpecifyUser");
            SpecifyUser testUser = createSpecifyUser(testUserName, testUserEmail, /*(short)0,*/ testUserPWd, testUserRole);
            assertNotNull("SpecifyUser created is null. ", testUser);
            log.info("checking if the SpecifyUser exists in the database ID: " + testUser.getId());
            assertTrue("SpecifyUser was not found in th database.", isSpecifyUserInDB(testUser.getId()));
            assertTrue("SpecifyUser failed to be deleted from the database.", deleteSpecifyUserDB(testUser.getId()));
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }
    }

    public void testUserUniqueNames()
    {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        log.info("Testing creating 2 SpecifyUsers with the same name");
        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        String testUserName = "testuser";
        String testUserEmail = "testuser@ku.edu";
        String testUserRole = "Test Role";
        String testUserPwd = "testpwd";
        try
        {
            log.info("Creating SpecifyUser");
            SpecifyUser testUser = createSpecifyUser(testUserName, testUserEmail, /*(short) 0,*/ testUserPwd, testUserRole);
            assertNotNull("SpecifyUser created is null. ", testUser);
            log.info("Checking if the SpecifyUser exists in the database ID: " + testUser.getId());
            assertTrue("SpecifyUser was not found in th database.", isSpecifyUserInDB(testUser.getId()));
            HibernateUtil.commitTransaction();
            HibernateUtil.beginTransaction();
            boolean shouldNotBeCreated = false;
            try
            {
                log.info("Creating 2nd SpecifyUser with same name - this should not be possible");
                SpecifyUser testUser2 = createSpecifyUser(testUserName, testUserEmail, /*(short) 0,*/ testUserPwd, testUserRole);
                if (testUser2 == null)
                {
                    shouldNotBeCreated = true;
                }
            } catch (Exception i)
            {
                HibernateUtil.rollbackTransaction();
                shouldNotBeCreated = true;
            }
            assertTrue("SpecifyUser with duplicate name should not have been created in db", shouldNotBeCreated);
            assertTrue("SpecifyUser failed to be deleted from the database.", deleteSpecifyUserDB(testUser.getId()));
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue("Some sort of exception was caught.", false);
        }
    }

//    public void testCreateSingleGroup()
//    {
//        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        log.info("Testing creating and deleting SpPrincipal");
//        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        String testGroupName = "testgroup";
//        try
//        {
//            log.info("Creating SpPrincipal: " + testGroupName);
//            SpPrincipal group = createPrincipalGroup(testGroupName);
//            assertNotNull("SpPrincipal created is null. ", group);
//            log.info("checking if the SpPrincipal exists in the database ID: " + group.getId());
//            assertTrue("SpPrincipal was not found in th database.", isUserGroupInDB(group.getId()));
//            assertTrue("SpPrincipal was NOT deleted from the database.", deleteUserGroupFromDB(group.getId()));
//        } catch (Exception ex)
//        {
//            log.error("******* " + ex);
//            ex.printStackTrace();
//            HibernateUtil.rollbackTransaction();
//            assertTrue("exception caught trying to create a user", false);
//        }
//    }

//    public void testCreateUserWithGroup()
//    {
//        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        log.info("Testing creating and deleting SpecifyUser with SpPrincipal");
//        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        
//        String testUserName  = "testuser";
//        String testUserEmail = "testuser@ku.edu";
//        String testUserRole  = "Test Role";
//        String testGroupName = "testgroup";
//        String testUserPassword = "testuser";
//
//        try
//        {
//
//            log.info("Creating SpPrincipal: " + testGroupName);
//            SpPrincipal group = createPrincipalGroup(testGroupName);
//            assertNotNull("SpPrincipal created is null. ", group);
//            log.info("checking if the SpPrincipal exists in the database ID: " + group.getId());
//            assertTrue("SpPrincipal was not found in th database.", isUserGroupInDB(group.getId()));
//
//            //SpPrincipal[] userGroupsListSlider = { group };
//            List<SpPrincipal> groups = new ArrayList<SpPrincipal>();
//            SpPrincipal         userGroup = createPrincipalGroup("Fish");
//            groups.add(userGroup);
//            
//            log.info("Creating SpecifyUser");
//            SpecifyUser testUser = createSpecifyUser(testUserName, testUserEmail, /*(short) 0,*/ testUserPassword, groups);//, testUserRole);
//            assertNotNull("SpecifyUser created is null. ", testUser);
//
//            log.info("checking if the SpecifyUser exists in the database ID: " + testUser.getId());
//            assertTrue("SpecifyUser was not found in th database.", 
//                    isSpecifyUserInDB(testUser.getId()));
//            log.info("deleteing SpecifyUser from the database ID: " + testUser.getId());
//            assertTrue("SpecifyUser failed to be deleted from the database.", deleteSpecifyUserDB(testUser.getId()));
//            HibernateUtil.commitTransaction();
//            
//            HibernateUtil.beginTransaction();
//            assertTrue("SpPrincipal should not have been deleted when SpecifyUser was deleted.", isUserGroupInDB(group.getId()));
//            log.info("deleteing SpPrincipal from the database ID: " + group.getId());
//            
//            assertTrue("SpPrincipal failed to be deleted from teh database.", deleteUserGroupFromDB(group.getId()));
//            assertFalse("SpPrincipal should have been deleted.", isUserGroupInDB(group.getId()));
//            
//        } catch (Exception ex)
//        {
//            log.error("******* " + ex);
//            ex.printStackTrace();
//            HibernateUtil.rollbackTransaction();
//            assertTrue("Exception caught trying to testCreateUserWithGroup " + ex, false);
//        }
//    }
    
//    public void testUserGroupUniqueName()
//    {
//        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        log.info("Testing creating 2 UserGroups with the same name");
//        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        String testGroupName = "testgroup";
//        try
//        {
//            log.info("Creating SpPrincipal: " + testGroupName);
//            SpPrincipal group = createPrincipalGroup(testGroupName);
//            assertNotNull("SpPrincipal created is null. ", group);
//            log.info("checking if the SpPrincipal exists in the database ID: " + group.getId());
//            assertTrue("SpPrincipal was not found in th database.", isUserGroupInDB(group.getId()));
//            HibernateUtil.commitTransaction();
//            HibernateUtil.beginTransaction();
//            boolean shouldNotBeCreated = false;
//            try
//            {
//                log.info("Creating 2nd SpPrincipal with same name - this should not be possible");
//                SpPrincipal group2 = createPrincipalGroup(testGroupName);
//                if (group2 == null)
//                {
//                    shouldNotBeCreated = true;
//                }
//            } catch (Exception i)
//            {
//                HibernateUtil.rollbackTransaction();
//                shouldNotBeCreated = true;
//            }
//            assertTrue("SpPrincipal with duplicate name should not have been created in db", shouldNotBeCreated);
//            assertTrue("SpPrincipal failed to be deleted from the database.", deleteUserGroupFromDB(group.getId()));
//        } catch (Exception ex)
//        {
//            log.error("******* " + ex);
//            ex.printStackTrace();
//            HibernateUtil.rollbackTransaction();
//            assertTrue("Exception was caught trying testCreating2UserGroupsWithSameName" + ex , false);
//        }
//    }
    
//    public void testUserPermissionWithNullCollObjDef()
//    {
//        
//    }
    
//    public void testUserPermissionWithNullCollObjDef()
//    {
//        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        log.info("Testing creating and deleting UserPermission");
//        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        String testUserName = "testuser";
//        String testUserEmail = "testuser@ku.edu";
//        String testUserRole = "Test Role";
//        String testUserPwd = "testpwd";
//        try
//        {
//            log.info("Creating SpecifyUser");
//            log.info("createSpecifyUser");
//            SpecifyUser  user         = createSpecifyUser("rods", "rods@ku.edu", /*(short) 0,*/ "rods" );//,"CollectionManager");
//            TaxonTreeDef taxonTreeDef = createTaxonTreeDef("Sample Taxon Tree Def");
//            LithoStratTreeDef lithoStratTreeDef = createLithoStratTreeDef("Sample Litho Tree Def");
//            
//            SpecifyUser testUser = createSpecifyUser(testUserName, testUserEmail, /*(short) 0,*/ testUserPwd);//, testUserRole);
//            assertNotNull("SpecifyUser created is null. ", testUser);
//            log.info("checking if the SpecifyUser exists in the database ID: " + testUser.getId());
//            assertTrue("SpecifyUser was not found in th database.", isSpecifyUserInDB(testUser.getId()));
//            
//            Session session = HibernateUtil.getCurrentSession();
//            setSession(session);
//            HibernateUtil.beginTransaction();
//
//            log.info("createDataType");
//            DataType         dataType         = createDataType("fish");
//
//           // createMultipleLocalities();
//
//            HibernateUtil.commitTransaction();
//
//            log.info("createDiscipline");
//            Institution    institution    = createInstitution("Natural History Museum");
//            Division       division       = createDivision(institution, "fish", "Icthyology", "IT", "Icthyology");
//            Discipline discipline = createDiscipline(division, "fish", "fish", dataType, taxonTreeDef, null, null, null, lithoStratTreeDef);
//
//            //createDiscipline(dataType, testUser, "fish", "fish"); // creates TaxonTreeDef
//
//            
//
////            UserPermission permission = createUserPermission(testUser, collectionType, true, true);
////            assertNotNull("UserPermission is null", permission);
////            assertTrue("UserPermission failed to be deleted from the database.", deleteUserPermissionFromDB(permission.getId()));
////            assertTrue("SpecifyUser failed to be deleted from the database.", deleteSpecifyUserDB(testUser.getId()));
//
//        } catch (Exception ex)
//        {
//            //log.error("******* " + ex);
//            //ex.printStackTrace();
//            HibernateUtil.rollbackTransaction();
//            assertTrue("UserPermission should not have been created with a null CollObjDef", true);
//        }
//    }
//    
//    public void testUserPermssionWithNullUser() {
//        //UserPermission permission = createUserPermission(null, null, true, true); 
//        //assertNotNull("UserPermission is null", permission);
//        
//        //AccessionAgent aa = createAccessionAgent("doror", null, null, null);
//        //assertNull(aa);
//    }
//    
//    public void testSpecifyUserAndUserPermission()
//    {
//        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        log.info("Testing creating and deleting SpecifyUser and UserPermission and having User delete permission");
//        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        String testUserName = "testuser";
//        String testUserEmail = "testuser@ku.edu";
//        String testUserRole = "Test Role";
//        String testUserPwd = "testpwd";
//        try
//        {
//            Agent            userAgent        = createAgent("", "John", "", "Doe", "", "jd@ku.edu");
//            SpPrincipal        spPrincipal        = createPrincipalGroup("fish");
//            TaxonTreeDef     taxonTreeDef     = createTaxonTreeDef("Sample Taxon Tree Def");
//            LithoStratTreeDef lithoStratTreeDef = createLithoStratTreeDef("Sample Litho Tree Def");
//
//            log.info("Creating SpecifyUser");
//            SpecifyUser testUser = createSpecifyUser(testUserName, testUserEmail, /*(short) 0,*/testUserPwd);//, testUserRole);
//            assertNotNull("SpecifyUser created is null. ", testUser);
//            testUser.addReference(userAgent, "agents");
//            
//            log.info("checking if the SpecifyUser exists in the database ID: " + testUser.getId());
//            assertTrue("SpecifyUser was not found in th database.", isSpecifyUserInDB(testUser.getId()));
//            
//            log.info("createDataType");
//            DataType         dataType         = createDataType("fish");
//            
//            log.info("createSpecifyUser");
//            SpecifyUser      user             = createSpecifyUser("admin", "admin@ku.edu", /*(short) 0,*/ "embeddedSpecifyAppRootPwd");//,spPrincipal);//, "CollectionManager");
//
//            Institution    institution    = createInstitution("Natural History Museum");
//            Division       division       = createDivision(institution, "fish", "Icthyology", "IT", "Icthyology");
//            Discipline discipline = createDiscipline(division, "fish", "fish", dataType, taxonTreeDef, null, null, null, lithoStratTreeDef);
//            
////            UserPermission permission = createUserPermission(testUser, collectionType, true, true);
////            assertNotNull("UserPermission is null", permission);
////            HibernateUtil.commitTransaction();
////            
////            HibernateUtil.beginTransaction();
////            assertTrue("SpecifyUser failed to be deleted from the database.", deleteSpecifyUserDB(testUser.getId()));
////            HibernateUtil.commitTransaction();
////            
////            HibernateUtil.beginTransaction();
////            assertFalse("UserPermission failed to be deleted from db when SpecifyUser was.",isUserPermissionInDB(permission.getId() ));
//
//
//        } catch (Exception ex)
//        {
//            log.error("******* " + ex);
//            ex.printStackTrace();
//            HibernateUtil.rollbackTransaction();
//            assertTrue(false);
//        }
//    }
}

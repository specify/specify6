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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @code_status Unknown
 * 
 * @author megkumin
 *
 */
public class AllTests
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for edu.ku.brc.specify.tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(HibernateSchemaTest.class);
        suite.addTestSuite(PopupDlgTests.class);
        suite.addTestSuite(AppResourceTest.class);
        suite.addTestSuite(WorkbenchTest.class);
        suite.addTestSuite(AppContextTests.class);
        suite.addTestSuite(SpecifyUserTest.class);
        suite.addTestSuite(PreferenceTest.class);
        suite.addTestSuite(ViewSetMgrTests.class);
        suite.addTestSuite(DBSchemaTest.class);
        suite.addTestSuite(TestFormFactory.class);
        
        //$JUnit-END$
        return suite;
    }

}

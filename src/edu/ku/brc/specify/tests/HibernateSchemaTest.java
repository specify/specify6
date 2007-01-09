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

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import junit.framework.TestCase;

/**
 * 
 * @code_status Unknown
 * 
 * @author megkumin
 *
 */
public class HibernateSchemaTest extends TestCase
{
    protected static final Logger log = Logger.getLogger(HibernateSchemaTest.class);
    /**
     * Constructor 
     * @param arg0
     */
    public HibernateSchemaTest(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    /**
     * @throws Exception
     */
    public static void testGencode() 
    {
        log.info("Starting up ANT for testgencode task.");

        // Create a new project, and perform some default initialization
        Project project = new Project();
        try
        {
            project.init();
            project.setBasedir(".");

            ProjectHelper.getProjectHelper().parse(project, new File("build.xml"));

            project.executeTarget("testgencode");
            
        } catch (BuildException e)
        {
            assertTrue("TestGenCode failed", false);
        }
    }
}

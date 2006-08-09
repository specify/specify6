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

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class TestFormFactory extends TestCase
{
    private static final Logger log = Logger.getLogger(TestFormFactory.class);
    private static final String dataPath = "forms";
    
    /**
     * 
     * @param aFile the file name to be read
     * @param aDoValidate validate after file is read
     * @return true if read correctly
     */
    protected boolean readFile(File aFile, boolean aDoValidate)
    {
        /*try
        {
            ViewSetMgr.loadViewFile(new FileInputStream(aFile));
            if (aDoValidate)
            {
                ViewSetMgr.validate();
            }
            return true;
            
        } catch (Exception ex)
        {
            //ex.printStackTrace();
            log.info(ex);  
        }*/
         
        return false;
    }
    
    /**
     * Returns a path to the data directory
     * @param aFileName the file name
     * @return the path to the file
     */
    protected String getPath(String aFileName)
    {
        File file = new File(TestFormFactory.class.getResource("package.html").getFile());
        return file.getParent() + File.separator + dataPath + File.separator + aFileName;
    }

    /**
     * Read the file from a String path
     * @param aFileName the path to the file
     * @param aDoValidate do validation
     * @return true if read correctly, false if in error
     */
    protected boolean readFile(String aFileName, boolean aDoValidate)
    {
        return readFile(new File(aFileName), aDoValidate);
    }
    
    /**
     * Tests reading in a file that is assumed to be valid
     *
     */
    public void testReadValidViewFile()
    {
        log.info("Running Test testReadValidViewFile");
//      XXX ViewSetMgr.clearAll();
        
        assertTrue(readFile(getPath("view_valid.xml"), true));
        
        // XXX ViewLoader.save(ViewSetMgrTests.getViews("view valid"), getPath("view_valid_new.xml"));
    }
    
    /**
     * Two forms have same ID (in the same file)
     *
     */
    public void testDuplicateViewIds()
    {
        log.info("Running Test testDuplicateViewIds");
//      XXX ViewSetMgr.clearAll();
        assertFalse(readFile(getPath("duplicate_view_ids.xml"), true));
    }
    
    /*
     * A form's subview element references a non-existent form ID
     */
    public void testMissingSubViewId()
    {
        log.info("//      XXX Running Test testMissingSubViewId");
//      XXX ViewSetMgr.clearAll();
        assertFalse(readFile(getPath("missing_subview_id.xml"), true));
    }
    
    /**
     * 
     * Tests looking up a form
     */
    public void testLookups()
    {
        log.info("Running Test testLookups");
//      XXX ViewSetMgr.clearAll();
        boolean rs = readFile(getPath("view_valid.xml"), true);
        if (rs)
        {
//          XXX rs = ViewSetMgr.isViewSetNameInUse("view valid");
            if (rs)
            {
//              XXX rs = ViewSetMgr.isViewInUse("view valid", "2");
            }
        }
        assertTrue(rs);
    }
    
    /**
     * The second document reference a form in the first document
     *
     */
    public void testSubViewReference()
    {
        log.info("Running Test testSubViewReference");
//      XXX ViewSetMgr.clearAll();
        readFile(getPath("view_valid.xml"), false);    // don't validate
        assertTrue(getPath("view_valid2.xml"), true);  // asks the ViewManager to validate the entire set of forms
    }
}

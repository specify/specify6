/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.ViewSetMgr;
import edu.ku.brc.af.ui.forms.ViewSetMgrManager;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.FileCache;

/**
 * Tests the ViewSetMgr and the ViewSetMgrManager.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class ViewSetMgrTests extends TestCase
{
    private static final Logger log = Logger.getLogger(ViewSetMgrTests.class);
    private static String viewsetFileName = "fish.views.xml";
    private static String srcDirName = "srcTest";
    private static String dstDirName = "dstTest";
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp()
    {
        //-----------------------------------------------------
        // This is needed for loading views
        //-----------------------------------------------------
        UIRegistry.getInstance(); // initializes it first thing
        if (UIRegistry.getAppName() == null) // this is needed because the setUp gets run separately for each test
        {
            System.setProperty("edu.ku.brc.af.core.AppContextMgrFactory",   "edu.ku.brc.specify.config.SpecifyAppContextMgr"); // Needed by AppCOntextMgr
            System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
            System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");       // Needed By UIRegistry
            
            UIRegistry.getInstance(); // initializes it first thing
            UIRegistry.setAppName("Specify");

            // Load Local Prefs
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.setDirPath(UIRegistry.getAppDataDir());
            localPrefs.load();
    
            FileCache.setDefaultPath(UIRegistry.getDefaultWorkingPath());
    
        }
        
        
        // First Create a Source ViewSet by copying one from the release
        File srcDir = new File(srcDirName);
        if (srcDir.exists())
        {
            try
            {
                FileUtils.deleteDirectory(srcDir);
                
            } catch (IOException ex) {}
        }
        
        assertTrue(srcDir.mkdir());
        
        // Make sure we start with a clean destination
        File dstDir = new File(dstDirName);
        if (dstDir.exists())
        {
            try
            {
                FileUtils.deleteDirectory(dstDir);
            } catch (IOException ex) {}
        }
        assertTrue(dstDir.mkdir());
        
        // Add an entry and copy the "real" file over to the source dir
        ViewSetMgr srcVM = new ViewSetMgr("", srcDir);
        srcVM.addViewSetDef("user", "Fish Views", "Fish Views Title", viewsetFileName, null);
        

        assertTrue(copyTestViewSetFile(new File("config"), new File(srcDirName), viewsetFileName));
        
        // make sure it got copied over correctly
        File f = new File(srcDir.getAbsoluteFile() + File.separator + viewsetFileName);
        assertTrue(f.exists());

        // Save the ViewSet
        try
        {
            srcVM.save();
            
        } catch (IOException ex)
        {
            assertTrue(false);
        }
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown()
    {
        if (true)
        {
            File srcDir = new File(srcDirName);
            if (srcDir.exists())
            {
                try
                {
                    FileUtils.deleteDirectory(srcDir);
                } catch (IOException ex) {}
            }
            File dstDir = new File(dstDirName);
            if (dstDir.exists())
            {
                try
                {
                    FileUtils.deleteDirectory(dstDir);
                } catch (IOException ex) {}
            }
        }
    }
    
    
    
    /**
     * Helper method 
     * @param srcDir source dir
     * @param dstDir destination dir
     * @param viewsetFileNameStr the name of the viewSet
     * @return true if copy worked
     */
    protected boolean copyTestViewSetFile(final File srcDir, final File dstDir, final String viewsetFileNameStr)
    {
        
        if (!srcDir.exists())
        {
            throw new RuntimeException("Src Dir doesn't exist "+srcDir.getAbsolutePath());
        }
        
        if (!dstDir.exists())
        {
            throw new RuntimeException("Dst Dir doesn't exist "+dstDir.getAbsolutePath());
        }
        
        try
        {
            FileUtils.copyFile(new File(srcDir + File.separator + viewsetFileNameStr), 
                               new File(dstDir + File.separator + viewsetFileNameStr));
            return true;
            
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
            
        }
    }


    /**
     * 
     */
    public void testViewMgrCreation()
    {
        //-------------------------------------------
        // The destination dir should have already been created
        //-------------------------------------------

        log.info("testViewMgrCreation");
        
        ViewSetMgr srcVM = new ViewSetMgr("", new File(srcDirName));
        ViewSetMgr dstVM = new ViewSetMgr("", new File(dstDirName));
        
         
        //-------------------------------------------
        // Create Destination ViewSetMgr (it is empty)
        //-------------------------------------------
         
        ViewSetMgrManager.copyViewSet(srcVM, dstVM, "Fish Views", true);
        
        try
        {
            dstVM.save();
            
        } catch (IOException ex)
        {
            assertTrue(false);
        }
        
        //-------------------------------------------
        // Now Start from scratch and create the ViewSetMgr
        // and check for the new ViewSet
        //-------------------------------------------
        
        dstVM = new ViewSetMgr("", new File(dstDirName));
        assertTrue(dstVM.isViewInUse("Fish Views", "CollectionObject"));

    }
    
    /**
     * Test getting View from the "main" ViewSet and the then the Backstop ViewSet
     */
    public void testViewSetMgr()
    {
        ViewSetMgrManager.pushViewMgr(new ViewSetMgr("", new File(XMLHelper.getConfigDirPath(File.separator + ViewSetMgrManager.BACKSTOP)), false));
        ViewSetMgrManager.pushViewMgr(new ViewSetMgr("", new File(srcDirName)));
        
        // Check a Fish View
        assertNotNull(AppContextMgr.getInstance().getView("Fish Views", "CollectionObject"));
        
        // Now Check a backstop view
        assertNotNull(AppContextMgr.getInstance().getView("Search", "AgentSearch"));
    }

}

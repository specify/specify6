package edu.ku.brc.specify.tests;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.config.SpecifyConfig;
import edu.ku.brc.specify.extfilerepos.ExternalFileIFace;
import edu.ku.brc.specify.extfilerepos.impl.ExternalFileRepository;
import edu.ku.brc.specify.prefs.*;

public class PreferenceTest extends TestCase
{
    private static Log log = LogFactory.getLog(PreferenceTest.class);

    public PreferencesMgr prefsMgr = null;
    
    public void setUp()
    {
        log.info("In Setup.");
        try
        {
            //SpecifyConfig config = SpecifyConfig.getInstance();
            //config.init(null); // do this once
            
        } catch (Exception e)
        {
            log.info("Error with Configuration", e);
        }
        
        prefsMgr = (PreferencesMgr)PreferencesMgr.getInstance();
        
    }
    
    /**
     * 
     *
     */
    public void testLoad()
    {
        log.info("In testLoad.");
        
        assertTrue(prefsMgr.load());
        
        log.info("Out testLoad.");
    }

    /**
     * 
     *
     */
    public void testGetPrefByPath()
    {
        log.info("In testGetPrefByPath.");
        
        prefsMgr.load();
        assertNotNull(prefsMgr.getPrefByPath("application/general/main.separator.position"));
        
        log.info("Out testGetPrefByPath.");
    }

}


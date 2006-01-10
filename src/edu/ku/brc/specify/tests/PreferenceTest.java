package edu.ku.brc.specify.tests;

import junit.framework.TestCase;
import java.util.prefs.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PreferenceTest extends TestCase
{
    private static Log log = LogFactory.getLog(PreferenceTest.class);

    public static Preferences rootPref = Preferences.userRoot();
    
    /**
     * 
     *
     */
    public void testLoad()
    {
        log.info("In testLoad.");
        
        String networkPref = "/Specify/Network/XXX";
        rootPref.put(networkPref, "XXX");
        
        System.out.println(rootPref.get(networkPref, "N/A"));
        assertTrue(true);
        
        networkPref = "/Specify/Network/YYY";
        rootPref.put(networkPref, "YYY");
        
        System.out.println(rootPref.get(networkPref, "N/A"));
        assertTrue(true);
        
        log.info("Out testLoad.");
    }


}


package edu.ku.brc.specify.tests;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UIHelper;

/**
 * Tests the AppPreferences and AppPreferences cache
 *
 * @code_status Unknown (auto-generated)
 *
 * @author rods
 *
 */
public class PreferenceTest extends TestCase
{
    private static final Logger log = Logger.getLogger(PreferenceTest.class);
    
    private static final String databaseName = "fish";
    
    protected AppPreferences appPrefs = null;

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
            System.setProperty("edu.ku.brc.af.core.AppContextMgrFactory", "edu.ku.brc.specify.config.SpecifyAppContextMgr");
            System.setProperty("AppPrefsIOClassName", "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");
            
            UIRegistry.getInstance(); // initializes it first thing
            UIRegistry.setAppName("Specify");

            // Load Local Prefs
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.setDirPath(UIRegistry.getDefaultWorkingPath());
            localPrefs.load();
            
            String hostName = "localhost";
            // This will log us in and return true/false
            if (!UIHelper.tryLogin("com.mysql.jdbc.Driver", 
                                   "org.hibernate.dialect.MySQLDialect", 
                                   databaseName, 
                                   "jdbc:mysql://"+hostName+"/"+databaseName, 
                                   "rods", 
                                   "rods"))
            {
                throw new RuntimeException("Couldn't login into ["+databaseName+"] "+DBConnection.getInstance().getErrorMsg());
            } else
            {
                HibernateUtil.getCurrentSession();
                AppPreferences.getRemote().load(); // Loads prefs from the database
            }
        }
    }
    
    public void testLocalPrefs()
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.put("test", "test");
        
        try
        {
            localPrefs.flush();
            
        } catch (Exception ex)
        {
            assertTrue(false);
        }
        
        String test = localPrefs.get("test", null);
        assertNotNull(test);
        assertTrue(test.equals("test"));
    }

    /**
     * Upadtes a preference value and calls flush to make sure the listeners get notified
     * @param section the section
     * @param pref the preference node name
     * @param attr the actual preference attribute name
     * @param value the new value
     */
    protected void updatePref(final String section, final String pref, final String attr, final String value)
    {

        AppPreferences.getRemote().put(section+"."+pref+"."+attr, value);
        //try {
        //    AppPreferences.getInstance().flush();
        //} catch (BackingStoreException ex) {}

    }

    /**
     * Tests the Date formating
     */
    public void testStrings()
    {
        AppPreferences appPrefsMgr = AppPreferences.getRemote();
        appPrefsMgr.put("text.ui.str", "string value");
        appPrefsMgr.put("text.ui.formatting.str", "string value");
        appPrefsMgr.put("text.ui.formatting.xxx.str", "string value");

        String[] keys = appPrefsMgr.keys("text.ui");
        log.info("Keys:");
        for (String s : keys)
        {
            log.info(s);
        }
        assert(keys.length != 1);

        String[] names = appPrefsMgr.childrenNames("text.ui");
        log.info("Child Names:");
        for (String s : names)
        {
            log.info(s);
        }
        assert(names.length != 1);


        try
        {
            appPrefsMgr.flush();
            
        } catch (Exception ex)
        {
            assert(false);
        }
        
    }

    /**
     * Tests the Date formating
     */
    public void testSimpleDateFormat()
    {
        FastDateFormat fastDateFormat = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        SimpleDateFormat df = new SimpleDateFormat(fastDateFormat.getPattern());
        log.info(df.toPattern());
        log.info(df.format(new Date()));

        fastDateFormat = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM);
        df = new SimpleDateFormat(fastDateFormat.getPattern());
        log.info(df.toPattern());
        log.info(df.format(new Date()));

        fastDateFormat = FastDateFormat.getDateInstance(FastDateFormat.LONG);
        df = new SimpleDateFormat(fastDateFormat.getPattern());
        log.info(df.toPattern());
        log.info(df.format(new Date()));
    }

    /**
     * Tests the Date formating
     */
    @SuppressWarnings("unused")
    public void testColorWrapper()
    {
        try
        {
            ColorWrapper cw = new ColorWrapper("xx, 1, 1");
            assertFalse(false);

        } catch (RuntimeException re)
        {
            assertTrue(true);
        }

        try
        {
            ColorWrapper cw = new ColorWrapper("1, 1");
            assertFalse(false);

        } catch (RuntimeException re)
        {
            assertTrue(true);
        }

        try
        {
            ColorWrapper cw = new ColorWrapper("");
            assertFalse(false);

        } catch (RuntimeException re)
        {
            assertTrue(true);
        }

        try
        {
            ColorWrapper cw = new ColorWrapper((String)null);
            assertFalse(false);

        } catch (RuntimeException re)
        {
            assertTrue(true);
        }

        try
        {
            ColorWrapper cw = new ColorWrapper((Color)null);
            assertFalse(false);

        } catch (RuntimeException re)
        {
            assertTrue(true);
        }

        ColorWrapper cw = new ColorWrapper("128, 255, 32");
        log.info(cw+"  "+cw.getColor().toString());
        assertTrue(cw.getColor().getRed() == 128 && cw.getColor().getGreen() == 255 && cw.getColor().getBlue() == 32);

        cw = new ColorWrapper(128, 255, 32);
        log.info(cw+"  "+cw.getColor().toString());
        assertTrue(cw.getColor().getRed() == 128 && cw.getColor().getGreen() == 255 && cw.getColor().getBlue() == 32);

        cw = new ColorWrapper(128, 999, 32);
        log.info(cw+"  "+cw.getColor().toString());
        assertTrue(cw.getColor().getRed() == 128 && cw.getColor().getGreen() == 255 && cw.getColor().getBlue() == 32);

        cw = new ColorWrapper("128,255,32");
        log.info(cw+"  "+cw.getColor().toString());
        assertTrue(cw.getColor().getRed() == 128 && cw.getColor().getGreen() == 255 && cw.getColor().getBlue() == 32);

        cw = new ColorWrapper("0,0,0");
        log.info(cw+"  "+cw.getColor().toString());
        cw.setRGB(128, 999, 32);
        assertTrue(cw.getColor().getRed() == 128 && cw.getColor().getGreen() == 255 && cw.getColor().getBlue() == 32);

        cw = new ColorWrapper("0,0,0");
        log.info(cw+"  "+cw.getColor().toString());
        cw.setRGB("128,255,32");
        assertTrue(cw.getColor().getRed() == 128 && cw.getColor().getGreen() == 255 && cw.getColor().getBlue() == 32);

        cw = new ColorWrapper(new Color(128, 255, 32));
        log.info(cw+"  "+cw.getColor().toString());
        assertTrue(cw.getColor().getRed() == 128 && cw.getColor().getGreen() == 255 && cw.getColor().getBlue() == 32);

        cw = new ColorWrapper(new Color(0, 0, 0));
        log.info(cw+"  "+cw.getColor().toString());
        cw.setColor(new Color(128, 255, 32));
        assertTrue(cw.getColor().getRed() == 128 && cw.getColor().getGreen() == 255 && cw.getColor().getBlue() == 32);

    }

    /**
     * Tests the Date formating
     */
    public void testDateFormatCache()
    {

        SimpleDateFormat format = new SimpleDateFormat("MM/DD/yyyy");

        Date date = Calendar.getInstance().getTime();

        AppPrefsCache.register(format, "ui", "formatting", "dateTest");

        String newFormat = "yyyy/MM/DD";

        AppPreferences.getRemote().put("ui.formatting.dateTest", newFormat);
        
        //try {
        //    AppPreferences.getInstance().flush();
        //} catch (BackingStoreException ex) {}

        log.info("New Date Format: "+format.toPattern());
        log.info(format.format(date));

        log.info("Actual pref value["+AppPrefsCache.getValue("ui", "formatting", "dateTest")+"]");
        log.info("newFormat["+newFormat+"]");

        assertTrue(AppPrefsCache.getValue("ui", "formatting", "dateTest").equals(newFormat));
        assertTrue(format.toPattern().equals(newFormat));
    }

    /**
     * Tests the Color
     */
    public void testColorWrapperCache()
    {

        String attrName = "valErrColorTest";

        ColorWrapper colorWrapper = new ColorWrapper("255, 128, 64");

        Color oldColor = new Color(255, 128, 64);

        assertTrue(oldColor.toString().equals(colorWrapper.getColor().toString()));

        AppPrefsCache.register(colorWrapper, "ui", "formatting", attrName);

        String newColorStr = "64, 255, 128";
        //Color newColor = new Color(64, 255, 128);

        AppPreferences.getRemote().put("ui.formatting."+attrName, newColorStr);
        //try {
        //    AppPreferences.getInstance().flush();
        //} catch (BackingStoreException ex) {}

        log.info("New Color: "+ colorWrapper.toString()+" ["+newColorStr+"]");

        assertTrue(AppPrefsCache.getValue("ui", "formatting", attrName).equals(newColorStr));
        assertTrue(colorWrapper.toString().equals(newColorStr));
    }


    /**
     * Tests a simple set (register) and then gets the value
     */
    public void testPrefSetGet()
    {
        String rgb = "255, 0, 0";

        AppPrefsCache.register("ui", "formatting", "testColor", rgb);

        assertTrue(AppPrefsCache.getValue("ui", "formatting", "testColor").equals(rgb));
    }

    /**
     * Tests registering a value, then updates it external, then gets it
     */
    public void testPrefSetUpdateGet()
    {
        String rgb    = "255, 0, 0";
        String newRGB = "255, 255, 255";

        AppPrefsCache.register("ui", "formatting", "testColor", rgb);

        updatePref("ui", "formatting", "testColor", newRGB);

        log.info("New RGB: "+AppPrefsCache.getValue("ui", "formatting", "testColor"));
        log.info("newRGB:  "+newRGB);
        
        assertTrue(AppPrefsCache.getValue("ui", "formatting", "testColor").equals(newRGB));
    }


    /**
     * The best verification is to go check the actual prefs file
     */
    public void testRemovePrefs()
    {
        assertTrue(AppPrefsCache.remove("ui", "formatting", "testColor"));
        assertTrue(AppPrefsCache.remove("ui", "formatting", "dateTest"));
        assertTrue(AppPrefsCache.remove("ui", "formatting", "valErrColorTest"));
    }

}

package edu.ku.brc.specify;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.HashSet;

import org.hibernate.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.FetchMode;
import org.hibernate.cache.*;


// Straight JDBC
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;

import org.apache.log4j.Logger;
import edu.ku.brc.specify.prefs.*;
import edu.ku.brc.specify.dbsupport.*;

/**
 * Create more sample data, letting Hibernate persist it for us.
 */
public class InitializeData 
{
    protected static Hashtable prepTypeMapper    = new Hashtable();
    protected static int       attrsId           = 0;
    protected static boolean   classesWereLoaded = false;
    final static Logger   _logger = Logger.getLogger(InitializeData.class);
    public static int getIndex(String[] aOldNames, String aNewName)
    {
        for (int i=0;i<aOldNames.length;i++)
        {
            String fieldName = aOldNames[i].substring(aOldNames[i].indexOf(".")+1, aOldNames[i].length());            
            if (aNewName.equals(fieldName))
            {
                return i;
            }
        }
        return -1;
    }
    
    /*
     * 
     */
    public static void loadAttrs()
    {
        try
        {
            
            //------------------------------
            // Load PrepTypes and Prep Attrs
            //------------------------------
            /*String[] pages = {"Formatting", "Colors", "Application"};
            String[] formattingPrefs = {"date", "java.lang.String"};
            
            PrefGroupDAO prefGroupDAO = new PrefGroupDAO();
            PrefGroup    prefGroup    = new PrefGroup();
            
            prefGroup.setName("Formatting");
            prefGroup.setCreated(new Date());
            HashSet<Preference> set = new HashSet<Preference>();
            prefGroup.setPreferences(set);
            
            for (int i=0;i<formattingPrefs.length;i++)
            {
                Preference pref = new Preference();
                pref.setName(formattingPrefs[i++]);
                pref.setValueType(formattingPrefs[i]);
                pref.setValue("");
                pref.setCreated(new Date());
                pref.setPrefGroup(prefGroup);
                
                set.add(pref);
            }
            prefGroupDAO.makePersistent(prefGroup);
            */
            
            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();
              
            // Clean up after ourselves
            //sessionFactory.close();
             
        } catch (Exception e)
        {
            _logger.error("loadAttr - ", e);
        }
        
    }
    
    /**
     * Utility method to associate an artist with a catObj
     */
    //private static void addCatalogObjCollectionEvent(CatalogObj catObj, CollectionEvent artist) {
    //    catObj.getCollectionEvent().add(artist);
    //}

    public static void main(String args[]) throws Exception 
    {
        boolean doingHibernate = false;
        if (doingHibernate) 
        {
            // Create a configuration based on the properties file we've put
            // in the standard place.
            Configuration config = new Configuration();
    
            // Tell it about the classes we want mapped, taking advantage of
            // the way we've named their mapping documents.
    
            // Get the session factory we can use for persistence
            SessionFactory sessionFactory = config.buildSessionFactory();
    
            // Ask for a session using the JDBC information we've configured
            Session session = sessionFactory.openSession();
            
            // Clean up after ourselves
            sessionFactory.close();
            _logger.info("Done.");
        } else
        {
            loadAttrs();
        }
    }
}

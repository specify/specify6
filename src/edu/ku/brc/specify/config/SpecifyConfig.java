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
package edu.ku.brc.specify.config;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.ku.brc.helpers.AskForDirectory;
import edu.ku.brc.specify.extfilerepos.impl.ExternalFileRepository;

/**
 * This class is responsible for reading and writing all application specific properties to and from the "specify/properties" file.
 * This class is a singleton
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 */
public class SpecifyConfig
{
    private static final Logger        log           = Logger.getLogger(SpecifyConfig.class);
    private static SpecifyConfig specifyConfig = new SpecifyConfig();

    private Configuration config = null;
    
    /**
     * Default Constructor
     *
     */
    private SpecifyConfig()
    {
    }
    
    /**
     * 
     * @return the singleton
     */
    public static SpecifyConfig getInstance()
    {
        return specifyConfig;
    }

    /**
     * 
     * @return the interface to the configartion object
     */
    public Configuration getConfiguration()
    {
        return config;
    }
    
    /**
     * 
     * @return the absolute path to a the user's home directory where we will store the specify.properties file.
     */
    protected File getUserHomeConfigPath()
    {
        try 
        {
            return new File(System.getProperty("user.home") + System.getProperty("file.separator") + "specify.properties");
            
        } catch (SecurityException ex) {
            log.fatal("Unable to build user home path to Specify config file", ex);
        }
        return null;
    }
    
    /**
     * Initializes this object and singletons that require a UI component for error dialogs and such
     * @param aParent a parent for the dialogs
     * @throws NoSuchElementException if it can't find the file
     */
    public void init(JComponent aParent) throws NoSuchElementException
    {
        File specifyConfigFile = getUserHomeConfigPath();
        
        try
        {
            config = new PropertiesConfiguration(specifyConfigFile);
            for (Iterator iter=config.getKeys();iter.hasNext();)
            {
                Object key = iter.next();
                
                log.debug(key+"  "+config.getProperty((String)key).toString());
            }
 
            /*ConfigurationFactory factory = new ConfigurationFactory();
            URL configURL = new File("src/tables.xml").toURL();
            factory.setConfigurationFileName(configURL.toString());
            config = factory.getConfiguration();
            */
            //config = new XMLConfiguration("preferences.xml");
            //XMLHelper.printNode(((XMLConfiguration)config).getDocument(), 0);
            //System.out.println("["+config.getProperty("name")+"]");
            /*Object groups = config.getList("groups.group");
            
                for (Iterator iter=config.getKeys();iter.hasNext();)
                {
                    Object key = iter.next();
                    
                    System.out.println(key+"  "+config.getProperty((String)key).getClass().toString());
                }
            System.out.println("["+groups+"]");
            if(groups instanceof Collection)
            {
                System.out.println("Number of Groups: " + ((Collection) groups).size());
                Collection col = (Collection)groups;
                for (Iterator iter=col.iterator();iter.hasNext();)
                {
                    Object obj = iter.next();
                    System.out.println(obj.getClass().toString());
                }
            }
            */
            /*
            ConfigurationFactory factory = new ConfigurationFactory("config.xml");
            
            config = factory.getConfiguration();
            
            Object prop = config.getProperty("database.tables.table.name");
            if(prop instanceof Collection)
            {
                System.out.println("Number of tables: " + ((Collection) prop).size());
            }
            prop = config.getProperty("tables.table.name");
            if(prop instanceof Collection)
            {
                System.out.println("Number of tables: " + ((Collection) prop).size());
            }
            System.out.println("["+config.getProperty("name")+"]");
            //config.save("");
            //config.setAutoSave(true);
            */
            
            // Test
            /*XMLConfiguration xmlConfig = (XMLConfiguration)config;
            Document doc = xmlConfig.getDocument();
            Node node = new Node();
            doc.appendChild(node);
            xmlConfig.save();
            */
            
        } catch (Exception ex)
        {
            if (aParent != null)
            {
                JOptionPane.showMessageDialog(aParent, "Couldn't find specify.properties", "Not Found", JOptionPane.ERROR_MESSAGE); // XXX LOCALIZE
            }
            log.fatal("No Configuration File `specify.properties`");
        }
        
        ExternalFileRepository.createInstance(aParent == null ? null : new AskForDirectory(aParent), config);
    }

}

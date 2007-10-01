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
/**
 * 
 */
package edu.ku.brc.af.core.expresssearch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;

/**
 * A singleton service that manages the Search Configuration.
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Sep 13, 2007
 *
 */
public class SearchConfigService
{
    //private static final Logger log = Logger.getLogger(SearchConfigService.class);
    
    protected static SearchConfigService instance = new SearchConfigService();
    
    protected SearchConfig                 searchConfig    = null;
    protected SearchTableConfig            searchContext   = null;
    protected List<PropertyChangeListener> changeListeners = new Vector<PropertyChangeListener>();

    /**
     * Constructor/
     */
    protected SearchConfigService()
    {

    }
    
    /**
     * @return the searchConfig
     */
    public SearchConfig getSearchConfig()
    {
        if (searchConfig == null)
        {
            loadConfig();
        }
        return searchConfig;
    }

    /**
     * @return the instance
     */
    public static SearchConfigService getInstance()
    {

        return instance;
    }

    /**
     * @return the searchContext
     */
    public SearchTableConfig getSearchContext()
    {
        return searchContext;
    }

    /**
     * @param searchContext the searchContext to set
     */
    public void setSearchContext(SearchTableConfig searchContext)
    {
        this.searchContext = searchContext;
    }
    
    /**
     * Add PropertyChangeListener that are notified when the contents of the service changes.
     * @param pcl the listener
     */
    public void addPropertyChangeListener(final PropertyChangeListener pcl)
    {
        changeListeners.add(pcl);
    }

    /**
     * Remoaves a PropertyChangeListener that are notified when the contents of the service changes.
     * @param pcl the listener
     */
    public void removePropertyChangeListener(final PropertyChangeListener pcl)
    {
        changeListeners.remove(pcl);
    }

    /**
     * Loads the changes.
     */
    protected void loadConfig()
    {
        XStream xstream = new XStream();
        SearchConfig.configXStream(xstream);
        
        String xmlStr = AppContextMgr.getInstance().getResourceAsXML("ExpressSearchConfig");
        
        //log.debug(xmlStr);
        
        searchConfig = (SearchConfig)xstream.fromXML(xmlStr);
        
        if (searchConfig == null)
        {
            searchConfig = new SearchConfig();
        } else
        {
            searchConfig.initialize();
        }
    }
    
    /**
     * Saves the changes.
     */
    public void saveConfig()
    {
        XStream xstream = new XStream();
        SearchConfig.configXStream(xstream);
        
        // This is for testing only RELEASE
        try
        {
            FileUtils.writeStringToFile(new File("config.xml"), xstream.toXML(searchConfig));
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        AppContextMgr.getInstance().putResourceAsXML("ExpressSearchConfig", xstream.toXML(searchConfig));
        
        PropertyChangeEvent pce = new PropertyChangeEvent(this, "contentsChanged", null, null);
        for (PropertyChangeListener pcl : changeListeners)
        {
            pcl.propertyChange(pce);
        }
        
        boolean foundContext = false;
        for (SearchTableConfig stc : searchConfig.getTables())
        {
            if (stc == searchContext)
            {
                foundContext = true;
                break;
            }
        }
        
        if (!foundContext)
        {
            searchContext = null;
            pce = new PropertyChangeEvent(this, "noContext", searchContext, null);
            for (PropertyChangeListener pcl : changeListeners)
            {
                pcl.propertyChange(pce);
            }
        }
    }

}

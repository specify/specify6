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
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.dom4j.Element;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.helpers.XMLHelper;

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
    
    protected Hashtable<String, Hashtable<String, Boolean>> skipFieldHash = new Hashtable<String, Hashtable<String, Boolean>>();

    /**
     * Constructor/
     */
    protected SearchConfigService()
    {
        // no op
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
     * Removes a PropertyChangeListener that are notified when the contents of the service changes.
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
        Element root = XMLHelper.readDOMFromConfigDir("es_skipfields.xml");
        for (Object tblObj : root.selectNodes("/tables/table"))
        {
            Element tbl = (Element)tblObj;
            String name = XMLHelper.getAttr(tbl, "name", null);
            Hashtable<String, Boolean> fields = new Hashtable<String, Boolean>();
            for (Object fldObj : tbl.selectNodes("field"))
            {
                Element fld = (Element)fldObj;
                fields.put(fld.getTextTrim().toLowerCase(), true);
            }
            skipFieldHash.put(name, fields);
        }

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
     * @param tblInfo
     * @return
     */
    public List<DBFieldInfo> getPruncedFieldList(final DBTableInfo tblInfo)
    {
        List<DBFieldInfo> availFields = tblInfo.getFields();
        
        Hashtable<String, Boolean> allHash = skipFieldHash.get("all");
        Hashtable<String, Boolean> tblHash = skipFieldHash.get(tblInfo.getName());
        if (allHash != null || tblHash != null)
        {
            availFields = new Vector<DBFieldInfo>();
            for (DBFieldInfo fi : tblInfo.getFields())
            {
                if (allHash != null && allHash.get(fi.getName().toLowerCase()) != null)
                {
                    continue;
                }
                if (tblHash != null && tblHash.get(fi.getName()) != null)
                {
                    continue;
                }
                availFields.add(fi);
            }
        }
        return availFields;
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

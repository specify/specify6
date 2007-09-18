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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 13, 2007
 *
 */
public class SearchConfigService
{
    protected static SearchConfigService instance = new SearchConfigService();
    
    protected SearchConfig searchConfig = null;
    

    /**
     * 
     */
    public SearchConfigService()
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

    protected void loadConfig()
    {
        XStream xstream = new XStream();
        SearchConfig.configXStream(xstream);
        
        String xmlStr = AppContextMgr.getInstance().getResourceAsXML("ExpressSearchConfig");
        
        searchConfig = (SearchConfig)xstream.fromXML(xmlStr);
        
        if (searchConfig == null)
        {
            searchConfig = new SearchConfig();
        } else
        {
            searchConfig.initialize();
        }

    }
    
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
    }

}

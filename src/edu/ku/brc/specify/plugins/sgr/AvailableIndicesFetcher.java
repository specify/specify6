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
package edu.ku.brc.specify.plugins.sgr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Oct 28, 2011
 *
 */
public class AvailableIndicesFetcher
{

    public static Map<String, String> getIndices()
    {
        StringBuilder json = new StringBuilder();
        
        try
        {
            URL url = new URL("http://sgr.nhm.ku.edu:8983/solr/available_indices.json");
            BufferedReader stream = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = stream.readLine()) != null)
            {
                json.append(line);
            }
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        JSONObject data = JSONObject.fromObject(json.toString());
        JSONArray indices = data.getJSONArray("indices");
        Map<String, String> result = new HashMap<String, String>();
        for (Object index : indices)
        {
            String name = ((JSONObject) index).getString("name");
            String url = ((JSONObject) index).getString("url");
            result.put(name, url);
        }
        return result;
    }
    
    public static void main(String[] args)
    {
        getIndices();
        return;
    }
}

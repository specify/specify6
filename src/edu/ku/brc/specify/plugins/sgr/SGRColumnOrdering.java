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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Jun 3, 2011
 *
 */
public class SGRColumnOrdering
{
    private static final SGRColumnOrdering instance = new SGRColumnOrdering();
    
    public static SGRColumnOrdering getInstance() { return instance; }
    
    private SGRColumnOrdering() {}
    
    private String [] fields = {"id", "catalog_number",
            "collector_number", "collectors", "scientific_name",
            "determiner", "determination_date",
            "date_collected", "latitude", "longitude", "locality",
            "municipality_name", "county_name", "state_name",
            "country_name", "institution_code", "collection_code", 
            "source", "score"};
    
    private static final Map<String, String> fieldHeadings = new HashMap<String, String>();
    static 
    {
        fieldHeadings.put("id", "ID");
        fieldHeadings.put("catalog_number", "Catalog #");
        fieldHeadings.put("collector_number", "Collector/Field #");
        fieldHeadings.put("collectors", "Collectors");
        fieldHeadings.put("scientific_name", "Taxon Name");
        fieldHeadings.put("determiner", "Determiner");
        fieldHeadings.put("determination_date", "Det. Date");
        fieldHeadings.put("date_collected", "Date");
        fieldHeadings.put("latitude", "Latitude");
        fieldHeadings.put("longitude", "Longitude");
        fieldHeadings.put("locality", "Locality");
        fieldHeadings.put("municipality_name", "Municipality");
        fieldHeadings.put("county_name", "County");
        fieldHeadings.put("state_name", "State");
        fieldHeadings.put("country_name", "Country");
        fieldHeadings.put("institution_code", "Institution");
        fieldHeadings.put("collection_code", "Collection");
        fieldHeadings.put("source", "Source");
        fieldHeadings.put("score", "Score");
    }
    
    public String [] getFields() { return fields; }
    
    public void moveColumn(int from, int to)
    {
        if (to == from) return;
        String s = fields[from];
        fields[from] = fields[to];
        fields[to] = s;
    }
    
    public String getHeadingFor(String field)
    {
        return fieldHeadings.get(field);
    }
    
    public String [] getHeadings()
    {
        List<String> headings = new LinkedList<String>();
        for (String field : fields)
        {
            headings.add(getHeadingFor(field));
        }
        return headings.toArray(new String[headings.size()]);
    }
}

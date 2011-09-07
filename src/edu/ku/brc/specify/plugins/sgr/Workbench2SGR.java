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

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import edu.ku.brc.sgr.SGRRecord;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Jul 18, 2011
 *
 */
public class Workbench2SGR
{
    private final Map<String, Short> fieldName2Index;
    
    public Workbench2SGR(Workbench workbench)
    {
        final ImmutableMap.Builder<String, Short> mb = new ImmutableMap.Builder<String, Short>();
        
        for (WorkbenchTemplateMappingItem item : 
            workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
        {
            mb.put(item.getFieldName(), item.getViewOrder());
        }
        
        fieldName2Index = mb.build();
    }
    
    public SGRRecord row2SgrRecord(WorkbenchRow row)
    {
        SGRRecord.Builder doc = SGRRecord.builder("TOMATCH-" + row.getId());
        
        addCollectors(row, doc);
        addDeterminations(row, doc);
        
        addField("collector_number",    new String [] {"stationFieldNumber", "fieldNumber"}, 
                row, doc);
        
        addField("date_collected",      new String [] {"verbatimDate", "startDate", "endDate"}, 
                row, doc);
        
        addField("locality",            new String [] {"verbatimLocality"}, 
                row, doc);
        
        addField("catalog_number",      new String [] {"catalogNumber", "altCatalogNumber"}, 
                row, doc);
        
        addField("location",
                new String [] {"country", "state", "county", "localityName", "namedPlace"}, 
                row, doc);

        return doc.build(); 
    }
    
    private void addCollectors(WorkbenchRow row, SGRRecord.Builder doc)
    {
        for (int c = 1; c < 9; c++)
        {
            StringBuilder sb = new StringBuilder();
            
            for (String field : 
                new String [] {"collectorFirstName", "collectorMiddle", "collectorLastName"})
            {
                Short ind = fieldName2Index.get(field + c);
                if (ind != null) sb.append(row.getData(ind) + " ");
            }
            if (sb.length() > 0) doc.put("collectors", sb.toString().trim());
        }
    }
    
    private void addDeterminations(WorkbenchRow row, SGRRecord.Builder doc)
    {
        StringBuilder sb = new StringBuilder();
        
        for (int c = 1; c < 3; c++)
        {
            for (String field : 
                new String [] {"genus", "species", "subspecies"})
            {
                Short ind = fieldName2Index.get(field + c);
                if (ind != null) sb.append(row.getData(ind) + " ");
            }
        }
        
        if (sb.length() > 0) doc.put("scientific_name", sb.toString().trim());
    }
    
    private void addField(String outputField, String [] inputFields, 
                          WorkbenchRow row, SGRRecord.Builder doc)
    {
        StringBuilder sb = new StringBuilder();
        
        for (String field : inputFields)
        {
            Short ind = fieldName2Index.get(field);
            if (ind != null) sb.append(row.getData(ind) + " ");
        }
        
        if (sb.length() > 0) doc.put(outputField, sb.toString().trim());
    }
}

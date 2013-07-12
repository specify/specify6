/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tests;


import junit.framework.TestCase;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.expresssearch.DisplayFieldConfig;
import edu.ku.brc.af.core.expresssearch.SearchConfig;
import edu.ku.brc.af.core.expresssearch.SearchFieldConfig;

/**
 * Test manipulating the ExpressSearchConfig and tests to and from XML.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Sep 7, 2007
 *
 */
public class ExpressSearchConfigTest extends TestCase
{
    
    protected DisplayFieldConfig createDisplayField(final String fieldName, final String formatter, final Integer order)
    {
        return new DisplayFieldConfig(fieldName, formatter, order);
    }
    
    protected SearchFieldConfig createSearchField(final String fieldName, 
                                                   final Boolean isSortable,
                                                   final Boolean isAscending)
    {
        return new SearchFieldConfig(fieldName, isSortable, isAscending);
    }
    
    public void testToFromXML()
    {
        SearchConfig config = new SearchConfig();
        config.addDisplayField("Agent", createDisplayField("lastName", "", 0));
        config.addDisplayField("Agent", createDisplayField("firstName", "", 1));
        config.addDisplayField("Agent", createDisplayField("name", "", 2));
        
        config.addDisplayField("Taxon", createDisplayField("fullName", "", 0));
        
        config.addSearchField("Taxon", createSearchField("fullName", false, true));
        //config.addSearchField("Taxon", createSearchField("name", false, true));
        
        config.addSearchField("Agent", createSearchField("lastName", true, true));
        config.addSearchField("Agent", createSearchField("name", true, true));
        
        
        XStream xstream = new XStream();
        
        SearchConfig.configXStream(xstream);
        
        String xmlStr = xstream.toXML(config);
        
        System.out.println(xmlStr);
        
        SearchConfig config2 = (SearchConfig)xstream.fromXML(xmlStr);
        
        assertTrue(config2.isDisplayFieldInList("Agent", "lastName"));
        assertTrue(config2.isSearchFieldInList("Agent", "lastName"));
        
        assertTrue(config2.isDisplayFieldInList("Taxon", "fullName"));
        assertTrue(config2.isSearchFieldInList("Taxon", "fullName"));

    }

    public void testRemove()
    {
        SearchConfig config = new SearchConfig();
        config.addDisplayField("Agent", createDisplayField("lastName", "", 0));
        config.addDisplayField("Agent", createDisplayField("firstName", "", 1));
        config.addDisplayField("Agent", createDisplayField("name", "", 2));
        
        config.addDisplayField("Taxon", createDisplayField("fullName", "", 0));
        
        config.addSearchField("Taxon", createSearchField("fullName", false, true));
        //config.addSearchField("Taxon", createSearchField("name", false, true));
        
        config.addSearchField("Agent", createSearchField("lastName", true, true));
        config.addSearchField("Agent", createSearchField("name", true, true));
        
        assertTrue(config.isDisplayFieldInList("Agent", "lastName"));
        assertTrue(config.isSearchFieldInList("Agent", "lastName"));
        
        assertTrue(config.isDisplayFieldInList("Taxon", "fullName"));
        assertTrue(config.isSearchFieldInList("Taxon", "fullName"));
        
        config.removeDisplayField("Agent", "lastName");
        config.removeSearchField("Agent", "firstName");
        
        config.removeSearchField("Taxon", "fullName"); // Should generate log.error but no failure
        config.removeDisplayField("Taxon", "fullName");     // Should generate log.error but no failure

        assertFalse(config.isDisplayFieldInList("Agent", "lastName"));
        assertFalse(config.isSearchFieldInList("Agent", "firstName"));
        assertFalse(config.isDisplayFieldInList("Taxon", "fullName"));
        assertFalse(config.isSearchFieldInList("Taxon", "fullName"));

    }

}

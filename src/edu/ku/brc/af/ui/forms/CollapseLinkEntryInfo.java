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
package edu.ku.brc.af.ui.forms;

import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 20, 2011
 *
 */
public class CollapseLinkEntryInfo extends CollapseLinkInfo
{
    private ArrayList<CollapseLinkInfo> items = new ArrayList<CollapseLinkInfo>();
    
    /**
     * 
     */
    public CollapseLinkEntryInfo()
    {
        super();
    }

    /**
     * @param name
     * @param title
     * @param url
     */
    public CollapseLinkEntryInfo(String name, String title, String url)
    {
        super(name, title, url);
    }
    
    public boolean hasItems()
    {
        return items != null && items.size() > 0;
    }

    /**
     * @return the items
     */
    public ArrayList<CollapseLinkInfo> getItems()
    {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(ArrayList<CollapseLinkInfo> items)
    {
        this.items = items;
    }
    
    /**
     * @param xstream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.alias("entry",  CollapseLinkEntryInfo.class);
    }
}
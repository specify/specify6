/* Copyright (C) 2009, University of Kansas Center for Research
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

import com.thoughtworks.xstream.XStream;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 20, 2011
 *
 */
public class CollapseLinkInfo
{
    private String name;
    private String title;
    private String url;
    
    /**
     * 
     */
    public CollapseLinkInfo()
    {
        super();
    }

    /**
     * @param name
     * @param title
     * @param url
     */
    public CollapseLinkInfo(String name, String title, String url)
    {
        super();
        this.name = name;
        this.title = title;
        this.url = url;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return title;
    }

    /**
     * @param xstream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.alias("info",  CollapseLinkInfo.class);
        
        xstream.useAttributeFor(CollapseLinkInfo.class, "title");
        xstream.useAttributeFor(CollapseLinkInfo.class, "name");
        xstream.useAttributeFor(CollapseLinkInfo.class, "url");
    }
}

/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.web;


public class FieldDisplayInfo
{
    protected Integer  order;
    protected String   name;
    protected String   pickList       = null;
    protected boolean  isSkipped      = false;
    protected boolean  forDisplay     = true;
    protected boolean  availForSearch = true;
    
    public FieldDisplayInfo(final Integer order, 
                            final String  name, 
                            final String  pickList, 
                            final boolean forDisplay, 
                            final boolean availForSearch)
    {
        super();
        this.order = order;
        this.name = name;
        this.pickList = pickList;
        this.forDisplay = forDisplay;
        this.availForSearch = availForSearch;
    }

    public FieldDisplayInfo(final String name, 
                            final boolean forDisplay, 
                            final boolean availForSearch)
    {
        this(null, name, null, forDisplay, availForSearch);
    }
    
    /**
     * @return the order
     */
    public Integer getOrder()
    {
        return order;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the pickList
     */
    public String getPickList()
    {
        return pickList;
    }

    /**
     * @return the forDisplay
     */
    public boolean isForDisplay()
    {
        return forDisplay;
    }

    /**
     * @return the isSkipped
     */
    public boolean isSkipped()
    {
        return isSkipped;
    }

    /**
     * @param isSkipped the isSkipped to set
     */
    public void setSkipped(boolean isSkipped)
    {
        this.isSkipped = isSkipped;
    }

    /**
     * @return the availForSearch
     */
    public boolean isAvailForSearch()
    {
        return availForSearch;
    }

    /**
     * @param availForSearch the availForSearch to set
     */
    public void setAvailForSearch(boolean availForSearch)
    {
        this.availForSearch = availForSearch;
    }
}

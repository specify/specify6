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
package edu.ku.brc.specify.config.init;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2008
 *
 */
public class BldrPickListItem
{
    // Fields
    private String title;
    private String value;
    private Integer ordinal;

    // Constructors

    /** default constructor */
    public BldrPickListItem()
    {
        // do nothing
    }

    public BldrPickListItem(final String title, final String value)
    {
        super();
        this.title = title;
        this.value = value;
    }

    public BldrPickListItem(final String title)
    {
        super();
        this.title       = title;
        this.value       = null;
    }

    /**
     * 
     */
    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * 
     */
    public String getValue()
    {
        return this.value == null ? title : value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
    
    /**
     * @return the ordinal
     */
    public Integer getOrdinal()
    {
        return ordinal;
    }

    /**
     * @param ordinal the ordinal to set
     */
    public void setOrdinal(Integer ordinal)
    {
        this.ordinal = ordinal;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return title;
    }
}

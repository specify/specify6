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
package edu.ku.brc.dbsupport;

import org.apache.commons.lang.StringUtils;

/**
 * Hold the Schema Information, this is used for for L10N/I18N and whether the item is visible.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Oct 3, 2007
 *
 */
public class DBInfoBase implements Comparable<DBInfoBase>
{
    protected String  name;
    protected String  title;
    protected String  description;
    protected boolean isHidden = false;
    
    /**
     * Default Constructor.
     */
    public DBInfoBase()
    {
        this(null, null, null);
    }

    /**
     * Constructor with name.
     * @param name the name of the item
     */
    public DBInfoBase(final String name)
    {
        this(name, null, null);
    }

    /**
     * COnstructor with name and localized title and description
     * @param name
     * @param title
     * @param description
     */
    protected DBInfoBase(final String name, final String title, final String description)
    {
        super();
        this.name        = name;
        this.title       = title;
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getTitle()
    {
        if (StringUtils.isNotEmpty(title))
        {
            return title;
        }
        return name;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param devName the name to set
     */
    public void setTitle(final String title)
    {
        this.title = title;
    }

    /**
     * @return the isHidden
     */
    public boolean isHidden()
    {
        return isHidden;
    }

    /**
     * @param isHidden the isHidden to set
     */
    public void setHidden(final boolean isHidden)
    {
        this.isHidden = isHidden;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getTitle();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final DBInfoBase o)
    {
        return getTitle().compareTo(o.getTitle());
    }
    
    
}

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
package edu.ku.brc.specify.tools.datamodelgenerator;

import java.util.HashMap;
import java.util.Vector;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class TableMetaData
{
	private String  id;
    private String  className;
    private Display display;
    private boolean isForWorkBench;
    private boolean isSearchable;
    private String  businessRule;
    private String  abbrv;
    private Vector<FieldAlias> fieldAliase; 
    private HashMap<String, Boolean> likeManyToOneHash = null;

	/**
	 * @param id
	 * @param className
	 * @param display
	 */
	public TableMetaData(final String  id, 
                         final String  className, 
                         final Display display, 
                         final Vector<FieldAlias> fieldAliase,
                         final boolean isSearchable,
                         final String businessRule,
                         final String abbrv)
	{
		this.id             = id;
        this.className      = className;
        this.display        = display;
        this.fieldAliase    = fieldAliase;
        this.isSearchable   = isSearchable;
        this.businessRule   = businessRule;
        this.abbrv          = abbrv;
	}

	public String getId()
	{
		return id;
	}

	public String getClassName()
    {
        return className;
    }

	public void setId(String id)
	{
		this.id = id;
	}

    public Display getDisplay()
    {
        return display;
    }

    public boolean isForWorkBench()
    {
        return isForWorkBench;
    }

    /**
     * @return the isSearchable
     */
    public boolean isSearchable()
    {
        return isSearchable;
    }

    public String getBusinessRule()
    {
        return businessRule;
    }

    public String getAbbrv()
    {
        return abbrv;
    }

    /**
     * @return the fieldAliase
     */
    public Vector<FieldAlias> getFieldAliase()
    {
        return fieldAliase;
    }
    

    /**
     * @param relName
     * @param isLike
     */
    public void setIsLikeManyToOne(final String relName, final boolean isLike)
    {
        if (likeManyToOneHash == null)
        {
            likeManyToOneHash = new HashMap<String, Boolean>();
        }
        likeManyToOneHash.put(relName, isLike);
    }

    /**
     * @return the likeManyToOneHash
     */
    public HashMap<String, Boolean> getLikeManyToOneHash()
    {
        return likeManyToOneHash;
    }
    
}

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
package edu.ku.brc.specify.web;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 24, 2007
 *
 */
public class AdditionalDisplayField implements Comparable<AdditionalDisplayField>
{
    protected Integer index;
    protected String  type;
    protected String  label;
    protected String  level;
    protected String  fieldName;
    
    public AdditionalDisplayField(Integer index, String type, String label, String level, String fieldName)
    {
        super();
        this.index = index;
        this.label = label;
        this.level = level;
        this.fieldName = fieldName;
        this.type = type;
    }
    
    /**
     * @return the index
     */
    public Integer getIndex()
    {
        return index;
    }

    /**
     * @return the labelField
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @return the level
     */
    public String getLevel()
    {
        return level;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    public int compareTo(AdditionalDisplayField obj)
    {
        return index.compareTo(obj.index);
    }
    
    public boolean isSet()
    {
        return type.equals("set");
    }
}

/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.core.db;

import org.apache.log4j.Logger;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 3, 2007
 *
 */
public class DBRelationshipInfo extends DBInfoBase implements DBTableChildIFace
{
    protected static final Logger log = Logger.getLogger(DBRelationshipInfo.class);
    
    public enum RelationshipType { OneToOne, OneToMany, ManyToOne, ManyToMany, ZeroOrOne}
    
    protected RelationshipType type;
    protected String           className;
    protected String           colName;
    protected String           otherSide;
    protected String           joinTable;
    protected boolean          isRequired;
    protected boolean          isUpdatable;
    protected boolean          isSavable;
    protected boolean          isLikeManyToOne;
    
    protected Class<?>         dataClass = null;
    
    public DBRelationshipInfo(final String name, 
                              final RelationshipType type, 
                              final String className, 
                              final String colName, 
                              final String otherSide, 
                              final String joinTable, 
                              final boolean isRequired, 
                              final boolean isUpdatable, 
                              final boolean isSavable,
                              final boolean isLikeManyToOne)
    {
        super(name);
        
        this.type        = type;
        this.className   = className;
        this.colName     = colName;
        this.otherSide   = otherSide;
        this.isRequired  = isRequired;
        this.isUpdatable = isUpdatable;
        this.joinTable   = joinTable;
        this.isSavable   = isSavable;
        this.isLikeManyToOne = isLikeManyToOne;
               
        try
        {
            dataClass = Class.forName(className);
            
        } catch (ClassNotFoundException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DBRelationshipInfo.class, ex);
            log.error(ex);
        }
    }
    
    /**
     * @param type the type to set
     */
    public void setType(RelationshipType type)
    {
        this.type = type;
    }

    public String getClassName()
    {
        return className;
    }

    public String getColName()
    {
        return colName;
    }

    public String getName()
    {
        return name;
    }

    public RelationshipType getType()
    {
        return type;
    }

    /**
     * @return the otherSide
     */
    public String getOtherSide()
    {
        return otherSide;
    }

    /**
     * @return the joinTable
     */
    public String getJoinTable()
    {
        return joinTable;
    }

    /**
     * @return the isRequired
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /**
     * @return the isUpdatable
     */
    public boolean isUpdatable()
    {
        return isUpdatable;
    }

    public boolean isSavable()
    {
        return isSavable;
    }

    /**
     * @return the isLikeManyToOne
     */
    public boolean isLikeManyToOne()
    {
        return isLikeManyToOne;
    }

    /**
     * @return the dataClass
     */
    public Class<?> getDataClass()
    {
        return dataClass;
    }
}

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
    
    protected Class<?>         dataClass = null;
    
    public DBRelationshipInfo(final String name, 
                              final RelationshipType type, 
                              final String className, 
                              final String colName, 
                              final String otherSide, 
                              final String joinTable, 
                              final boolean isRequired, 
                              final boolean isUpdatable, 
                              final boolean isSavable)
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
     * @return the dataClass
     */
    public Class<?> getDataClass()
    {
        return dataClass;
    }
}

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
package edu.ku.brc.specify.datamodel;


import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 25, 2007
 *
 */
@MappedSuperclass
public abstract class SpLocaleBase extends DataModelObjBase
{
    private static final Logger  log      = Logger.getLogger(SpLocaleBase.class);
    
    protected String  name;
    protected String  type;
    protected Boolean isHidden;
    
    protected String  format;
    protected Boolean isUIFormatter;
    protected String  pickListName;
    
    /**
     * 
     */
    public SpLocaleBase()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        name     = null;
        type     = null;
        isHidden = false;
        format   = null;
        isUIFormatter = null;
        pickListName  = null;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        if (name != null && name.length() > 64)
        {
            log.error("String len: "+name.length()+ " is > 64 ["+name+"]");
            this.name = name.substring(0, 64);
            
        } else
        {
            this.name = name;
        }
    }

    /**
     * @return the type
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        if (type != null && type.length() > 32)
        {
            log.error("String len: "+type.length()+ " is > 32 ["+type+"]");
            this.type = type.substring(0, 32);
            
        } else
        {
            this.type = type;
        }
    }

    /**
     * @return the isHidden
     */
    @Column(name = "IsHidden", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsHidden()
    {
        return isHidden == null ? false : isHidden;
    }

    /**
     * @param isHidden the isHidden to set
     */
    public void setIsHidden(Boolean isHidden)
    {
        this.isHidden = isHidden;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }

    /**
     * @return the format
     */
    @Column(name = "Format", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getFormat()
    {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * @return the isUIFormatter
     */
    @Column(name = "IsUIFormatter", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsUIFormatter()
    {
        return isUIFormatter;
    }

    /**
     * @param isUIFormatter the isUIFormatter to set
     */
    public void setIsUIFormatter(Boolean isUIFormatter)
    {
        this.isUIFormatter = isUIFormatter;
    }

    /**
     * @return the pickListName
     */
    @Column(name = "PickListName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getPickListName()
    {
        return pickListName;
    }

    /**
     * @param pickListName the pickListName to set
     */
    public void setPickListName(String pickListName)
    {
        this.pickListName = pickListName;
    }
    
    
}

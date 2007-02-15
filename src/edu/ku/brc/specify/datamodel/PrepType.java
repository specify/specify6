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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.HashSet;
import java.util.Set;

/**
 * Each Preparation has a PrepType and Preparations can have additional "attributes" based on a PrepType.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Nov 14, 2006
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "preptype")
public class PrepType extends DataModelObjBase implements java.io.Serializable
{

    // Fields    

    protected Long              prepTypeId;
    protected String            name;
    protected Boolean           isLoanable;
    protected Set<Preparation>  preparations;
    protected Set<AttributeDef> attributeDefs;

    // Constructors

    /** default constructor */
    public PrepType()
    {
        //
    }

    /** constructor with id */
    public PrepType(Long prepTypeId)
    {
        this.prepTypeId = prepTypeId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        prepTypeId = null;
        name = null;
        isLoanable = true;
        preparations = new HashSet<Preparation>();
        attributeDefs = new HashSet<AttributeDef>();
    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "PrepTypeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getPrepTypeId()
    {
        return this.prepTypeId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.prepTypeId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return PrepType.class;
    }

    public void setPrepTypeId(Long prepTypeId)
    {
        this.prepTypeId = prepTypeId;
    }

    /**
     * 
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Column(name = "IsLoanable", unique=false, nullable=false, insertable=true, updatable=true)
    public Boolean getIsLoanable()
    {
        return isLoanable;
    }

    public void setIsLoanable(Boolean isLoanable)
    {
        this.isLoanable = isLoanable;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        if (name != null) return name;
        return super.getIdentityTitle();
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "prepType")
    public Set<Preparation> getPreparations()
    {
        return this.preparations;
    }

    public void setPreparations(Set<Preparation> preparations)
    {
        this.preparations = preparations;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "prepType")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AttributeDef> getAttributeDefs()
    {
        return this.attributeDefs;
    }

    public void setAttributeDefs(Set<AttributeDef> attributeDefs)
    {
        this.attributeDefs = attributeDefs;
    }

    // Add Methods

    public void addPreparations(final Preparation preparation)
    {
        this.preparations.add(preparation);
        preparation.setPrepType(this);
    }

    public void addAttributeDefs(final AttributeDef attributeDef)
    {
        this.attributeDefs.add(attributeDef);
        attributeDef.setPrepType(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removePreparations(final Preparation preparation)
    {
        this.preparations.remove(preparation);
        preparation.setPrepType(null);
    }

    public void removeAttributeDefs(final AttributeDef attributeDef)
    {
        this.attributeDefs.remove(attributeDef);
        attributeDef.setPrepType(null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 65;
    }

}

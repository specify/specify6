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
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.af.core.AppContextMgr;

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
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "preptype")
public class PrepType extends DataModelObjBase implements java.io.Serializable
{

    // Fields    

    protected Integer           prepTypeId;
    protected String            name;
    protected Boolean           isLoanable;
    protected Collection        collection;
    protected Set<AttributeDef> attributeDefs;

    // Constructors

    /** default constructor */
    public PrepType()
    {
        //
    }

    /** constructor with id */
    public PrepType(Integer prepTypeId)
    {
        this.prepTypeId = prepTypeId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        prepTypeId     = null;
        name           = null;
        isLoanable     = true;
        collection     = AppContextMgr.getInstance().getClassObject(Collection.class);
        attributeDefs  = new HashSet<AttributeDef>();
    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "PrepTypeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getPrepTypeId()
    {
        return this.prepTypeId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
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

    public void setPrepTypeId(Integer prepTypeId)
    {
        this.prepTypeId = prepTypeId;
    }

    /**
     * 
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
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
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "prepType")
    @Cascade( { CascadeType.MERGE, CascadeType.LOCK })
    public Set<AttributeDef> getAttributeDefs()
    {
        return this.attributeDefs;
    }

    public void setAttributeDefs(Set<AttributeDef> attributeDefs)
    {
        this.attributeDefs = attributeDefs;
    }

    /**
     * @return the collection
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Collection getCollection()
    {
        return collection;
    }

    /**
     * @param collection the collection to set
     */
    public void setCollection(Collection collection)
    {
        this.collection = collection;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Collection.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return collection != null ? collection.getId() : null;
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

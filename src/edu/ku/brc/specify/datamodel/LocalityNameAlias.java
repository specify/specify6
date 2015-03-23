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
package edu.ku.brc.specify.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Sep 12, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "localitynamealias")
@org.hibernate.annotations.Table(appliesTo="localitynamealias", indexes =
    {   @Index (name="LocalityNameAliasIDX", columnNames={"Name"})
    })
public class LocalityNameAlias extends DisciplineMember implements Cloneable
{
    
    protected Integer  localityNameAliasId;
    protected String   name;
    protected String   source;
    protected Locality locality;

    /**
     * 
     */
    public LocalityNameAlias()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        localityNameAliasId = null;
        name                = null;
        source              = null;
    }
    
    /**
     * @return the localityNameAliasId
     */
    @Id
    @GeneratedValue
    @Column(name = "LocalityNameAliasID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLocalityNameAliasId()
    {
        return localityNameAliasId;
    }

    /**
     * @param localityNameAliasId the localityNameAliasId to set
     */
    public void setLocalityNameAliasId(Integer localityNameAliasId)
    {
        this.localityNameAliasId = localityNameAliasId;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 255)
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @return the source
     */
    @Column(name = "Source", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getSource()
    {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source)
    {
        this.source = source;
    }

    /**
     * @return the locality
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocalityID", unique = false, nullable = false, insertable = true, updatable = true)
    public Locality getLocality()
    {
        return locality;
    }

    /**
     * @param locality the locality to set
     */
    public void setLocality(Locality locality)
    {
        this.locality = locality;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Locality.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
   public Integer getParentId()
    {
        return locality != null ? locality.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return LocalityNameAlias.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return localityNameAliasId;
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        LocalityNameAlias lla = (LocalityNameAlias)super.clone();
        lla.init();
        
        lla.localityNameAliasId = null;
        lla.locality            = null;
        
        return lla;
    }
    
    /**
     * @return the Table ID for the class.
     */
    @Transient
    public static int getClassTableId()
    {
        return 120;
    }

    @Override
    @Transient
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
    }


}

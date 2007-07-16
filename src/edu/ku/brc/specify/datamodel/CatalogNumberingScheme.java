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
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

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

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 11, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "catalognumberingscheme")
public class CatalogNumberingScheme extends DataModelObjBase implements java.io.Serializable
{
    protected Long            catalogNumberingSchemeId;

    protected String          schemeName;
    protected String          schemeClassName;
    protected Boolean         isNumericOnly;
    protected Set<Collection> collections;
    
    public CatalogNumberingScheme()
    {
        // no op
    }
    
    /** constructor with id */
    public CatalogNumberingScheme(Long catalogNumberingSchemeId) 
    {
        this.catalogNumberingSchemeId = catalogNumberingSchemeId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        catalogNumberingSchemeId = null;
        schemeName      = null;
        schemeClassName = null;
        collections     = new HashSet<Collection>();

    }
    
    // End Initializer
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.catalogNumberingSchemeId;
    }
    
    @Id
    @GeneratedValue
    @Column(name = "CatalogNumberingSchemeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getCatalogNumberingSchemeId()
    {
        return catalogNumberingSchemeId;
    }

    public void setCatalogNumberingSchemeId(Long catalogNumberingSchemeId)
    {
        this.catalogNumberingSchemeId = catalogNumberingSchemeId;
    }

    @Column(name = "SchemeName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getSchemeName()
    {
        return schemeName;
    }

    public void setSchemeName(String schemeName)
    {
        this.schemeName = schemeName;
    }

    @Column(name = "SchemeClassName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getSchemeClassName()
    {
        return schemeClassName;
    }

    public void setSchemeClassName(String schemeClassName)
    {
        this.schemeClassName = schemeClassName;
    }

    @Column(name="IsNumericOnly", unique=false, nullable=true, updatable=true, insertable=true)
    public Boolean getIsNumericOnly()
    {
        return isNumericOnly;
    }

    public void setIsNumericOnly(Boolean isNumericOnly)
    {
        this.isNumericOnly = isNumericOnly;
    }

    @OneToMany(cascade = {}, fetch = FetchType.EAGER, mappedBy = "catalogNumberingScheme")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<Collection> getCollections()
    {
        return collections;
    }

    public void setCollections(Set<Collection> collections)
    {
        this.collections = collections;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CatalogNumberingScheme.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return schemeName;
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
        return 97;
    }
}

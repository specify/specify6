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
 * Created Date: Aug 28, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "conservrecmdtype")
public class ConservRecmdType extends DataModelObjBase implements java.io.Serializable
{
    public static byte LIGHT     = 1;
    public static byte DISPLAY   = 2;
    public static byte OTHER     = 3;
    public static byte TREATMENT = 4;
    
    // Fields    

    protected Integer           conservRecmdTypeId;
    protected Byte              rcmdType;
    protected String            title;
    protected Set<ConservRecommendation> conservRecommendations;

    // Constructors

    /** default constructor */
    public ConservRecmdType()
    {
        //
    }

    /** constructor with id */
    public ConservRecmdType(Integer conservRecmdTypeId)
    {
        this.conservRecmdTypeId = conservRecmdTypeId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        conservRecmdTypeId     = null;
        title                  = null;
        conservRecommendations = new HashSet<ConservRecommendation>();
    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "ConservRecmdTypeId", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getConservRecmdTypeId()
    {
        return this.conservRecmdTypeId;
    }
    
    /**
     * 
     */
    @Column(name = "rcmdtype", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getRcmdType()
    {
        return this.rcmdType;
    }

    public void setRcmdType(Byte rcmdType)
    {
        this.rcmdType = rcmdType;
    }

    /**
     * 
     */
    @Column(name = "title", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "conservRecmdType")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<ConservRecommendation> getConservRecommendations()
    {
        return this.conservRecommendations;
    }

    public void setConservRecommendations(Set<ConservRecommendation> conservRecommendations)
    {
        this.conservRecommendations = conservRecommendations;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.conservRecmdTypeId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ConservRecmdType.class;
    }

    public void setConservRecmdTypeId(Integer conservRecmdTypeId)
    {
        this.conservRecmdTypeId = conservRecmdTypeId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        if (title != null) return title;
        return super.getIdentityTitle();
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
        return 105;
    }

}

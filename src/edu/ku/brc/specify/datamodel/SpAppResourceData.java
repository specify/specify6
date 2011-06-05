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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;






/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spappresourcedata")
public class SpAppResourceData extends DataModelObjBase implements java.io.Serializable, Cloneable
{

    // Fields    

     protected Integer       spAppResourceDataId;
     protected byte[]        data;
     protected SpAppResource spAppResource;
     protected SpViewSetObj  spViewSetObj;


    // Constructors

    /** default constructor */
    public SpAppResourceData() {
        //
    }
    
    /** constructor with id */
    public SpAppResourceData(Integer spAppResourceDataId) 
    {
        this.spAppResourceDataId = spAppResourceDataId;
    }
   
    
    @Override
    public void initialize()
    {
        super.init();
        spAppResourceDataId = null;
        data                = null;
        spAppResource       = null;
        spViewSetObj        = null;
    }
    

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "SpAppResourceDataID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpAppResourceDataId() 
    {
        return this.spAppResourceDataId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.spAppResourceDataId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpAppResourceData.class;
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
    
    public void setSpAppResourceDataId(Integer spAppResourceDataId) 
    {
        this.spAppResourceDataId = spAppResourceDataId;
    }

    /**
     * 
     */
    /*
    @Column(name = "data", unique = false, nullable = false, insertable = true, updatable = true, length = 1073741823)
    public java.sql.Blob getData() 
    {
        return this.data;
    }
    
    public void setData(java.sql.Blob data) 
    {
        this.data = data;
    }*/
    
    @Lob
    @Column(name = "data", unique = false, nullable = true, insertable = true, updatable = true, length=16000000)
    public byte[] getData() 
    {
        return this.data;
    }
    
    public void setData(final byte[] data) 
    {
        this.data = data;
    }
    
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpAppResourceID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpAppResource getSpAppResource() {
        return this.spAppResource;
    }
    
    public void setSpAppResource(SpAppResource AppResource) 
    {
        this.spAppResource = AppResource;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpViewSetObjID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpViewSetObj getSpViewSetObj() {
        return this.spViewSetObj;
    }
    
    public void setSpViewSetObj(SpViewSetObj ViewSetObj) 
    {
        this.spViewSetObj = ViewSetObj;
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
        return 515;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        SpAppResourceData obj  = (SpAppResourceData)super.clone();
        obj.spAppResourceDataId = null;
        obj.data              = data != null ? data.clone() : null;
        return obj;
    }

}

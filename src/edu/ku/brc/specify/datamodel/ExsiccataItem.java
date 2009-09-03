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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author mkelly
 * 
 * @code_status Alpha
 * 
 * Created Date: Aug 19, 2008
 */

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "exsiccataitem")
public class ExsiccataItem extends DataModelObjBase implements java.io.Serializable 
{

    // Fields    

     protected Integer          exsiccataItemId;
     protected String           number;
     protected String           fascicle;
     
     protected Exsiccata        exsiccata;
     protected CollectionObject collectionObject;


    // Constructors

    /** default constructor */
    public ExsiccataItem() {
        //
    }
    
    /** constructor with id */
    public ExsiccataItem(Integer exsiccataItemId) {
        this.exsiccataItemId = exsiccataItemId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        exsiccataItemId = null;
        number = null;
        fascicle = null;
        exsiccata = null;
        collectionObject = null;
    }
    // End Initializer

    // Property accessors

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ExsiccataItem.class;
    }
    
    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.exsiccataItemId;
    }

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "ExsiccataItemID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getExsiccataItemId() {
        return this.exsiccataItemId;
    }

    public void setExsiccataItemId(Integer exsiccataItemId) {
        this.exsiccataItemId = exsiccataItemId;
    }

    /**
     * 
     */
    @Column(name = "Fascicle", length = 16)
    public String getFascicle() {
        return this.fascicle;
    }
    
    public void setFascicle(String fascicle) {
        this.fascicle = fascicle;
    }

    /**
     * 
     */
    @Column(name = "Number", length = 16)
    public String getNumber() {
        return this.number;
    }
   
    public void setNumber(String number) {
        this.number = number;
    }
    
    /**
     *      * The associated exsiccata
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ExsiccataID", unique = false, nullable = false, insertable = true, updatable = true)
    public Exsiccata getExsiccata() {
        return this.exsiccata;
    }
    
    public void setExsiccata(Exsiccata exsiccata) {
        this.exsiccata = exsiccata;
    }

    /**
     *      * Biological Object cited
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = false, insertable = true, updatable = true)
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Exsiccata.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return exsiccata != null ? exsiccata.getId() : null;
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
        return 104;
    }

}

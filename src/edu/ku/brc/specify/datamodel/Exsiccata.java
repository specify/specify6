/* Copyright (C) 2021, Specify Collections Consortium
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
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

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
@Table(name = "exsiccata")
public class Exsiccata extends DataModelObjBase implements java.io.Serializable 
{

    // Fields    

     protected Integer exsiccataId;
     protected String  title;
     protected String remarks;
     protected String schedae;

     protected ReferenceWork referenceWork;
     
     protected Set<ExsiccataItem> exsiccataItems;
     
    // Constructors

    /** default constructor */
    public Exsiccata() {
        //
    }
    
    /** constructor with id */
    public Exsiccata(Integer exsiccataId) {
        this.exsiccataId = exsiccataId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        exsiccataId    = null;
        title          = null;
        referenceWork  = null;
        remarks = null;
        schedae = null;
        exsiccataItems = new HashSet<ExsiccataItem>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "ExsiccataID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getExsiccataId() {
        return this.exsiccataId;
    }

    public void setExsiccataId(Integer exsiccataId) {
        this.exsiccataId = exsiccataId;
    }

    /**
     *
     * @return
     */
    @Lob
    @Column(name = "Remarks", length = 65535)
    public String getRemarks() {
        return remarks;
    }

    /**
     *
     * @param remarks
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *
     * @return
     */
    @Column(name = "Schedae", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getSchedae() {
        return schedae;
    }

    /**
     *
     * @param schedae
     */
    public void setSchedae(String schedae) {
        this.schedae = schedae;
    }

    /**
     * @return the Title
     */
    @Column(name = "Title", unique = false, nullable = false, insertable = true, updatable = true)
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     *      * The associated reference
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ReferenceWorkID", unique = false, nullable = false, insertable = true, updatable = true)
    public ReferenceWork getReferenceWork()
    {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork)
    {
        this.referenceWork = referenceWork;
    }
    
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "exsiccata")
    @Cascade( {CascadeType.DELETE} )
    public Set<ExsiccataItem> getExsiccataItems()
    {
        return this.exsiccataItems;
    }
    
    public void setExsiccataItems(Set<ExsiccataItem> exsiccataItems)
    {
        this.exsiccataItems = exsiccataItems;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return ReferenceWork.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return referenceWork != null ? referenceWork.getId() : null;
    }
    
    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.exsiccataId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Exsiccata.class;
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
        return 89;
    }

}

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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 29, 2009
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spvisualquery")
@org.hibernate.annotations.Table(appliesTo="spvisualquery", indexes =
    {   @Index (name="SpVisualQueryNameIDX", columnNames={"Name"})
    })
public class SpVisualQuery extends DataModelObjBase
{
    protected Integer            spVisualQueryId;
    protected String             name;
    protected String             description;
    
    protected Set<LatLonPolygon> polygons;
    protected SpecifyUser        specifyUser;
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spVisualQueryId  = null;
        name             = null;
        description      = null;
        polygons         = new HashSet<LatLonPolygon>();
    }

    /**
     * @return the spVisualQueryId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpVisualQueryID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpVisualQueryId()
    {
        return spVisualQueryId;
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
     * @return the description
     */
    @Lob
    @Column(name = "Description", length = 4096)
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the polygons
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "visualQuery")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<LatLonPolygon> getPolygons()
    {
        return polygons;
    }

    /**
     * @return the specifyUser
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser()
    {
        return specifyUser;
    }

    /**
     * @param spVisualQueryId the spVisualQueryId to set
     */
    public void setSpVisualQueryId(Integer spVisualQueryId)
    {
        this.spVisualQueryId = spVisualQueryId;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param polygons the polygons to set
     */
    public void setPolygons(Set<LatLonPolygon> polygons)
    {
        this.polygons = polygons;
    }

    /**
     * @param specifyUser the specifyUser to set
     */
    public void setSpecifyUser(SpecifyUser specifyUser)
    {
        this.specifyUser = specifyUser;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpVisualQuery.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return spVisualQueryId;
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
        return 532;
    }
}

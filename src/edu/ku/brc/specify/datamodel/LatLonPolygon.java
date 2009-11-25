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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Table(name = "latlonpolygon")
@org.hibernate.annotations.Table(appliesTo="latlonpolygon")
public class LatLonPolygon extends DataModelObjBase
{
    protected Integer                 latLonPolygonId;
    protected String                  name;
    protected Boolean                 isPolyline;
    protected String                  description;
    
    protected Set<LatLonPolygonPnt>   points;
    protected SpVisualQuery           visualQuery;
    protected Locality                locality;
    
    /**
     * 
     */
    public LatLonPolygon()
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
        
        latLonPolygonId = null;
        name            = null;
        isPolyline      = false;
        description     = null;
        points          = new HashSet<LatLonPolygonPnt>();
    }

    /**
     * @return the latLonPolygonId
     */
    @Id
    @GeneratedValue
    @Column(name = "LatLonPolygonID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLatLonPolygonId()
    {
        return latLonPolygonId;
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
     * @return the isPolyline
     */
    @Column(name = "IsPolyline", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsPolyline()
    {
        return isPolyline;
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
     * @return the points
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "latLonPolygon")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<LatLonPolygonPnt> getPoints()
    {
        return points;
    }

    /**
     * @return the visualQuery
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpVisualQueryID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpVisualQuery getVisualQuery()
    {
        return visualQuery;
    }

    /**
     * @return the locality
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocalityID", unique = false, nullable = true, insertable = true, updatable = true)
    public Locality getLocality()
    {
        return locality;
    }

    /**
     * @param latLonPolygonId the latLonPolygonId to set
     */
    public void setLatLonPolygonId(Integer latLonPolygonId)
    {
        this.latLonPolygonId = latLonPolygonId;
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
     * @param points the points to set
     */
    public void setPoints(Set<LatLonPolygonPnt> points)
    {
        this.points = points;
    }

    /**
     * @param visualQuery the visualQuery to set
     */
    public void setVisualQuery(SpVisualQuery visualQuery)
    {
        this.visualQuery = visualQuery;
    }

    /**
     * @param isPolyline the isPolyline to set
     */
    public void setIsPolyline(Boolean isPolyline)
    {
        this.isPolyline = isPolyline;
    }


    /**
     * @param locality the locality to set
     */
    public void setLocality(Locality locality)
    {
        this.locality = locality;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        if (visualQuery != null)
        {
            return visualQuery.getId();
        }
        if (locality != null)
        {
            return locality.getId();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return LatLonPolygon.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return latLonPolygonId;
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
        return 136;
    }


}

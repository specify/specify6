package edu.ku.brc.specify.datamodel;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;

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
@Table(name = "latlonpolygonpnt")
@org.hibernate.annotations.Table(appliesTo="latlonpolygonpnt")
public class LatLonPolygonPnt implements FormDataObjIFace, Cloneable
{
    protected Integer       latLonPolygonPntId;
    protected BigDecimal    latitude;
    protected BigDecimal    longitude;
    protected Integer       elevation;
    protected Integer       ordinal;
    protected LatLonPolygon latLonPolygon;
    
    /**
     * 
     */
    public LatLonPolygonPnt()
    {
        super();
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#initialize()
     */
    @Override
    public void initialize()
    {
        latLonPolygonPntId = null;
        latitude           = null;
        longitude          = null;
        ordinal            = null;
        latLonPolygon      = null;
    }

    /**
     * @return the latLonPolygonPntId
     */
    @Id
    @GeneratedValue
    @Column(name = "LatLonPolygonPntID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLatLonPolygonPntId()
    {
        return latLonPolygonPntId;
    }

    /**
     * @return the latitude
     */
    @Column(name = "Latitude", unique = false, nullable = false, insertable = true, updatable = true, precision = 12, scale = 10)
    public BigDecimal getLatitude()
    {
        return latitude;
    }

    /**
     * @return the longitude
     */
    @Column(name = "Longitude", unique = false, nullable = false, insertable = true, updatable = true, precision = 12, scale = 10)
    public BigDecimal getLongitude()
    {
        return longitude;
    }

    /**
     * @return the elevation
     */
    @Column(name = "Elevation", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getElevation()
    {
        return elevation;
    }

    /**
     * @return the ordinal
     */
    @Column(name = "Ordinal", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getOrdinal()
    {
        return ordinal;
    }

    /**
     * @return the latLonPolygon
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LatLonPolygonID", unique = false, nullable = false, insertable = true, updatable = true)
    public LatLonPolygon getLatLonPolygon()
    {
        return latLonPolygon;
    }

    /**
     * @param latLonPolygonPntId the latLonPolygonPntId to set
     */
    public void setLatLonPolygonPntId(Integer latLonPolygonPntId)
    {
        this.latLonPolygonPntId = latLonPolygonPntId;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(BigDecimal latitude)
    {
        this.latitude = latitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(BigDecimal longitude)
    {
        this.longitude = longitude;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(Integer elevation)
    {
        this.elevation = elevation;
    }
    
    /**
     * @param ordinal the ordinal to set
     */
    public void setOrdinal(Integer ordinal)
    {
        this.ordinal = ordinal;
    }

    /**
     * @param latLonPolygon the latLonPolygon to set
     */
    public void setLatLonPolygon(LatLonPolygon latLonPolygon)
    {
        this.latLonPolygon = latLonPolygon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.af.ui.forms.FormDataObjIFace, java.lang.String, boolean)
     */
    @Override
    public void addReference(FormDataObjIFace ref, String fieldName, boolean doOtherSide)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.af.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void addReference(FormDataObjIFace ref, String fieldName)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#forceLoad()
     */
    @Override
    public void forceLoad()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return LatLonPolygonPnt.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return latLonPolygonPntId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getIdentityTitle()
     */
    @Transient
    @Override
    public String getIdentityTitle()
    {
        return latitude != null && longitude != null ? latitude.toString() + ", " + longitude.toString() : Integer.toString(hashCode());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getModifiedByAgent()
     */
    @Transient
    @Override
    public Agent getModifiedByAgent()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getParentId()
     */
    @Transient
    @Override
    public Integer getParentId()
    {
        return latLonPolygon != null ? latLonPolygon.getId() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getParentTableId()
     */
    @Transient
    @Override
    public Integer getParentTableId()
    {
        return LatLonPolygon.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getReferenceValue(java.lang.String)
     */
    @Transient
    @Override
    public Object getReferenceValue(String ref)
    {
        return null;
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
        return 137;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getTimestampCreated()
     */
    @Override
    @Transient
    public Timestamp getTimestampCreated()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getTimestampModified()
     */
    @Override
    @Transient
    public Timestamp getTimestampModified()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#getVersion()
     */
    @Transient
    @Override
    public Integer getVersion()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#isRestrictable()
     */
    @Transient
    @Override
    public boolean isRestrictable()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#onDelete()
     */
    @Override
    public void onDelete()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#onSave()
     */
    @Override
    public void onSave()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#onUpdate()
     */
    @Override
    public void onUpdate()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#removeReference(edu.ku.brc.af.ui.forms.FormDataObjIFace, java.lang.String, boolean)
     */
    @Override
    public void removeReference(FormDataObjIFace ref, String fieldName, boolean doOtherSide)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#removeReference(edu.ku.brc.af.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void removeReference(FormDataObjIFace ref, String fieldName)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#setCreatedByAgent(edu.ku.brc.specify.datamodel.Agent)
     */
    @Override
    public void setCreatedByAgent(Agent createdByAgent)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#setModifiedByAgent(edu.ku.brc.specify.datamodel.Agent)
     */
    @Override
    public void setModifiedByAgent(Agent modifiedByAgent)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#setTimestampCreated(java.sql.Timestamp)
     */
    @Override
    public void setTimestampCreated(Timestamp timestampCreated)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#setTimestampModified(java.sql.Timestamp)
     */
    @Override
    public void setTimestampModified(Timestamp timestampModified)
    {
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        LatLonPolygonPnt p = (LatLonPolygonPnt)super.clone();
        
        p.latLonPolygonPntId = null;
        
        return p;
    }
    
}

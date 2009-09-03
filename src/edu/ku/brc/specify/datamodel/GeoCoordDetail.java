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

import java.util.Calendar;

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
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 16, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "geocoorddetail")
public class GeoCoordDetail extends DataModelObjBase
{
    // Manis Fields
    protected Integer               geoCoordDetailId;
    protected Float                 namedPlaceExtent;
    protected String                geoRefAccuracyUnits;
    protected String                geoRefDetRef;
    protected Calendar              geoRefDetDate;
    protected Agent                 geoRefDetBy;
    protected String                noGeoRefBecause;
    protected String                geoRefRemarks;
    protected String                geoRefVerificationStatus; 
    
    // New Fields
    protected Float                 maxUncertaintyEst; 
    protected String                maxUncertaintyEstUnit; 
    protected String                originalCoordSystem; 
    protected String                validation; 
    protected String                protocol; 
    protected String                source; 
    
    protected Locality              locality;
    
    /**
     * 
     */
    public GeoCoordDetail()
    {
        // no op
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        // Manis Fields
        geoCoordDetailId      = null;
        namedPlaceExtent      = null;
        geoRefAccuracyUnits   = null;
        geoRefDetRef          = null;
        geoRefDetDate         = null;
        geoRefDetBy           = null;
        noGeoRefBecause       = null;
        geoRefRemarks         = null;
        geoRefVerificationStatus = null; 
        
        maxUncertaintyEst     = null;  
        maxUncertaintyEstUnit = null;  
        originalCoordSystem   = null;  
        validation            = null;  
        protocol              = null;  
        source                = null;  
        locality              = null;
    }

    /**
     * @return the geoCoordDetailId
     */
    @Id
    @GeneratedValue
    @Column(name = "GeoCoordDetailID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getGeoCoordDetailId()
    {
        return geoCoordDetailId;
    }

    /**
     * @param geoCoordDetailId the geoCoordDetailId to set
     */
    public void setGeoCoordDetailId(Integer geoCoordDetailId)
    {
        this.geoCoordDetailId = geoCoordDetailId;
    }
    
    /**
     * @return the namedPlaceExtent
     */
    @Column(name = "NamedPlaceExtent", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNamedPlaceExtent()
    {
        return namedPlaceExtent;
    }

    /**
     * @param namedPlaceExtent the namedPlaceExtent to set
     */
    public void setNamedPlaceExtent(Float namedPlaceExtent)
    {
        this.namedPlaceExtent = namedPlaceExtent;
    }

    /**
     * @return the geoRefAccuracyUnits
     */
    @Column(name = "GeoRefAccuracyUnits", unique = false, nullable = true, insertable = true, updatable = true, length = 20)
    public String getGeoRefAccuracyUnits()
    {
        return geoRefAccuracyUnits;
    }

    /**
     * @param geoRefAccuracyUnits the geoRefAccuracyUnits to set
     */
    public void setGeoRefAccuracyUnits(String geoRefAccuracyUnits)
    {
        this.geoRefAccuracyUnits = geoRefAccuracyUnits;
    }

    /**
     * @return the geoRefDetRef
     */
    @Column(name = "GeoRefDetRef", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
    public String getGeoRefDetRef()
    {
        return geoRefDetRef;
    }

    /**
     * @param geoRefDetRef the geoRefDetRef to set
     */
    public void setGeoRefDetRef(String geoRefDetRef)
    {
        this.geoRefDetRef = geoRefDetRef;
    }

    /**
     * @return the geoRefDetDate
     */
    @Column(name = "GeoRefDetDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getGeoRefDetDate()
    {
        return geoRefDetDate;
    }

    /**
     * @param geoRefDetDate the geoRefDetDate to set
     */
    public void setGeoRefDetDate(Calendar geoRefDetDate)
    {
        this.geoRefDetDate = geoRefDetDate;
    }

    /**
     * @return the geoRefDetBy
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getGeoRefDetBy()
    {
        return geoRefDetBy;
    }

    /**
     * @param geoRefDetBy the geoRefDetBy to set
     */
    public void setGeoRefDetBy(Agent geoRefDetBy)
    {
        this.geoRefDetBy = geoRefDetBy;
    }

    /**
     * @return the noGeoRefBecause
     */
    @Column(name = "NoGeoRefBecause", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
    public String getNoGeoRefBecause()
    {
        return noGeoRefBecause;
    }

    /**
     * @param noGeoRefBecause the noGeoRefBecause to set
     */
    public void setNoGeoRefBecause(String noGeoRefBecause)
    {
        this.noGeoRefBecause = noGeoRefBecause;
    }

    /**
     * @return the geoRefRemarks
     */
    @Lob
    @Column(name = "GeoRefRemarks", unique = false, nullable = true, insertable = true, updatable = true)
    public String getGeoRefRemarks()
    {
        return geoRefRemarks;
    }

    /**
     * @param geoRefRemarks the geoRefRemarks to set
     */
    public void setGeoRefRemarks(String geoRefRemarks)
    {
        this.geoRefRemarks = geoRefRemarks;
    }

    /**
     * @return the geoRefVerificationStatus
     */
    @Column(name = "GeoRefVerificationStatus", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getGeoRefVerificationStatus()
    {
        return geoRefVerificationStatus;
    }

    /**
     * @param geoRefVerificationStatus the geoRefVerificationStatus to set
     */
    public void setGeoRefVerificationStatus(String geoRefVerificationStatus)
    {
        this.geoRefVerificationStatus = geoRefVerificationStatus;
    }


    /**
     * @return the maxUncertaintyEst
     */
    @Column(name = "MaxUncertaintyEst", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getMaxUncertaintyEst()
    {
        return maxUncertaintyEst;
    }

    /**
     * @param maxUncertaintyEst the maxUncertaintyEst to set
     */
    public void setMaxUncertaintyEst(Float maxUncertaintyEst)
    {
        this.maxUncertaintyEst = maxUncertaintyEst;
    }

    /**
     * @return the maxUncertaintyEstUnit
     */
    @Column(name = "MaxUncertaintyEstUnit", unique = false, nullable = true, insertable = true, updatable = true, length = 8)
    public String getMaxUncertaintyEstUnit()
    {
        return maxUncertaintyEstUnit;
    }

    /**
     * @param maxUncertaintyEstUnit the maxUncertaintyEstUnit to set
     */
    public void setMaxUncertaintyEstUnit(String maxUncertaintyEstUnit)
    {
        this.maxUncertaintyEstUnit = maxUncertaintyEstUnit;
    }

    /**
     * @return the originalCoordSystem
     */
    @Column(name = "OriginalCoordSystem", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getOriginalCoordSystem()
    {
        return originalCoordSystem;
    }

    /**
     * @param originalCoordSystem the originalCoordSystem to set
     */
    public void setOriginalCoordSystem(String originalCoordSystem)
    {
        this.originalCoordSystem = originalCoordSystem;
    }

    /**
     * @return the validation
     */
    @Column(name = "Validation", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getValidation()
    {
        return validation;
    }

    /**
     * @param validation the validation to set
     */
    public void setValidation(String validation)
    {
        this.validation = validation;
    }

    /**
     * @return the protocol
     */
    @Column(name = "Protocol", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    /**
     * @return the source
     */
    @Column(name = "Source", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getSource()
    {
        return source;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocalityID", unique = false, nullable = true, insertable = true, updatable = true)
    public Locality getLocality() 
    {
        return this.locality;
    }
    
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
    
    /**
     * @param source the source to set
     */
    public void setSource(String source)
    {
        this.source = source;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return GeoCoordDetail.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return geoCoordDetailId;
    }

    /*
     * (non-Javadoc)
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
        return 123;
    }
}

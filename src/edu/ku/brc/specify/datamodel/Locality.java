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
package edu.ku.brc.specify.datamodel;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace;
import edu.ku.brc.specify.dbsupport.TypeCode;
import edu.ku.brc.specify.dbsupport.TypeCodeItem;
import edu.ku.brc.ui.UIRegistry;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "locality")
@org.hibernate.annotations.Table(appliesTo="locality", indexes =
    {   @Index (name="localityNameIDX", columnNames={"LocalityName"}),
        @Index (name="LocalityDisciplineIDX", columnNames={"DisciplineID"})
    })
public class Locality extends DisciplineMember implements AttachmentOwnerIFace<LocalityAttachment>, java.io.Serializable, MapLocationIFace 
{
    // Fields    
    protected Integer               localityId;
    protected String                namedPlace;
    protected String                shortName;
    protected String                relationToNamedPlace;
    protected String                localityName;
    protected String                verbatimElevation;
    protected String                originalElevationUnit;
    protected Double                minElevation;
    protected Double                maxElevation;
    protected String                elevationMethod;
    protected Double                elevationAccuracy;
    protected Integer               originalLatLongUnit;
    protected String                latLongType;
    protected BigDecimal            latitude1;
    protected BigDecimal            longitude1;
    protected BigDecimal            latitude2;
    protected BigDecimal            longitude2;
    protected String                latLongMethod;
    protected Double                latLongAccuracy;
    protected String                gml;
    protected String                datum;
    protected String                remarks;
    protected String                lat1text;   // The original value
    protected String                lat2text;   // The original value
    protected String                long1text;  // The original value
    protected String                long2text;  // The original value
    protected Byte                  visibility;
    protected SpecifyUser           visibilitySetBy;
    protected String                guid;
    
    // Source Data used for formatting
    // XXX.XXXXXXXX N    Decimal Degrees
    // XXX XX.XXXXXX N   Degree Decimal Minutes
    // XXX XX XX.XXXX N  Degrees Minutes Decimal Seconds
    protected Byte                  srcLatLongUnit;
    
    protected Geography               geography;
    protected Set<LocalityCitation>   localityCitations;
    //protected Set<CollectingEvent>    collectingEvents;
    protected Set<LocalityAttachment> localityAttachments;
    protected Set<LocalityNameAlias>  localityNameAliass;
    protected Set<LocalityDetail>     localityDetails;
    protected Set<GeoCoordDetail>     geoCoordDetails;


    // Constructors

    /** default constructor */
    public Locality()
    {
        // do nothing
    }
    
    /** constructor with id */
    public Locality(Integer localityId) {
        this.localityId = localityId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        localityId = null;
        shortName = null;
        namedPlace = null;
        relationToNamedPlace = null;
        localityName = null;
        verbatimElevation = null;
        originalElevationUnit = null;
        minElevation = null;
        maxElevation = null;
        elevationMethod = null;
        elevationAccuracy = null;
        originalLatLongUnit = null;
        latLongType = null;
        latitude1 = null;
        longitude1 = null;
        latitude2 = null;
        longitude2 = null;
        latLongMethod = null;
        latLongAccuracy = null;
        datum = null;
        remarks = null;

        lat1text = null;
        lat2text = null;
        long1text = null;
        long2text = null;
        visibility = null;
        guid       = null;
        
        // Source Data for Formatting
        srcLatLongUnit = 0;          // matches LATLON.DDDDDD
        
        //discipline          = AppContextMgr.getInstance().getClassObject(Discipline.class);
        geography           = null;
        localityCitations   = new HashSet<LocalityCitation>();
        //collectingEvents    = new HashSet<CollectingEvent>();
        localityNameAliass  = new HashSet<LocalityNameAlias>();
        localityAttachments = new HashSet<LocalityAttachment>();
        localityDetails     = new HashSet<LocalityDetail>();
        geoCoordDetails     = new HashSet<GeoCoordDetail>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "LocalityID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLocalityId() 
    {
        return this.localityId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.localityId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Locality.class;
    }
    
    public void setLocalityId(Integer localityId)
    {
        this.localityId = localityId;
    }

    /**
     * * The named place the locality is closest to
     */
    @Column(name = "NamedPlace", unique = false, nullable = true, insertable = true, updatable = true)
    public String getNamedPlace()
    {
        return this.namedPlace;
    }

    public void setNamedPlace(String namedPlace)
    {
        this.namedPlace = namedPlace;
    }

    /**
     * @return the shortName
     */
    @Column(name = "ShortName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getShortName()
    {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }

    /**
     * * Relation of the locality to the named place
     */
    @Column(name = "RelationToNamedPlace", unique = false, nullable = true, insertable = true, updatable = true, length = 120)
    public String getRelationToNamedPlace()
    {
        return this.relationToNamedPlace;
    }

    public void setRelationToNamedPlace(String relationToNamedPlace)
    {
        this.relationToNamedPlace = relationToNamedPlace;
    }

    /**
     * * The full name of the locality.
     */
    @Column(name = "LocalityName", unique = false, nullable = false, insertable = true, updatable = true)
    public String getLocalityName()
    {
        return this.localityName;
    }

    public void setLocalityName(String localityName)
    {
        this.localityName = localityName;
    }

    /**
     * * The verbatim elevation including units as given in the field notes
     */
    @Column(name = "VerbatimElevation", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getVerbatimElevation()
    {
        return this.verbatimElevation;
    }

    public void setVerbatimElevation(String verbatimElevation)
    {
        this.verbatimElevation = verbatimElevation;
    }

    /**
     * * i.e. Meters, Feet, ...
     */
    @Column(name = "OriginalElevationUnit", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getOriginalElevationUnit()
    {
        return this.originalElevationUnit;
    }

    public void setOriginalElevationUnit(String originalElevationUnit)
    {
        this.originalElevationUnit = originalElevationUnit;
    }

    /**
     * * The minimum elevation in Meters
     */
    @Column(name = "MinElevation", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getMinElevation()
    {
        return this.minElevation;
    }

    public void setMinElevation(Double minElevation)
    {
        this.minElevation = minElevation;
    }

    /**
     * * The maximum elevation in Meters
     */
    @Column(name = "MaxElevation", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getMaxElevation()
    {
        return this.maxElevation;
    }

    public void setMaxElevation(Double maxElevation)
    {
        this.maxElevation = maxElevation;
    }

    /**
     * * The method used to determine the elevation
     */
    @Column(name = "ElevationMethod", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getElevationMethod()
    {
        return this.elevationMethod;
    }

    public void setElevationMethod(String elevationMethod)
    {
        this.elevationMethod = elevationMethod;
    }

    /**
     * * plus or minus -- in meters
     */
    @Column(name = "ElevationAccuracy", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getElevationAccuracy()
    {
        return this.elevationAccuracy;
    }

    public void setElevationAccuracy(Double elevationAccuracy)
    {
        this.elevationAccuracy = elevationAccuracy;
    }

    /**
     * * i.e. Decimal, Deg/Decimal Min, Deg/Min/Decimal Sec, ...
     */
    @Column(name = "OriginalLatLongUnit", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getOriginalLatLongUnit()
    {
        return this.originalLatLongUnit == null ? 0 : this.originalLatLongUnit;
    }

    public void setOriginalLatLongUnit(Integer originalLatLongUnit)
    {
        this.originalLatLongUnit = originalLatLongUnit;
    }

    /**
     * * The type of area described by the lat long data (Point,Line,Rectangle)
     */
    @Column(name = "LatLongType", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLatLongType()
    {
        return this.latLongType;
    }

    public void setLatLongType(String latLongType)
    {
        this.latLongType = latLongType;
    }

    /**
     * * Latitude of first point
     */
    @Column(name = "Latitude1", unique = false, nullable = true, insertable = true, updatable = true, precision = 12, scale = 10)
    public BigDecimal getLatitude1()
    {
        return this.latitude1;
    }

    public void setLatitude1(BigDecimal latitude1)
    {
        this.latitude1 = latitude1;
    }

    /**
     * * Longitude of first point
     */
    @Column(name = "Longitude1", unique = false, nullable = true, insertable = true, updatable = true, precision = 13, scale = 10)
    public BigDecimal getLongitude1()
    {
        return this.longitude1;
    }

    public void setLongitude1(BigDecimal longitude1)
    {
        this.longitude1 = longitude1;
    }

    /**
     * * Latitude of second point
     */
    @Column(name = "Latitude2", unique = false, nullable = true, insertable = true, updatable = true, precision = 12, scale = 10)
    public BigDecimal getLatitude2()
    {
        return this.latitude2;
    }

    public void setLatitude2(BigDecimal latitude2)
    {
        this.latitude2 = latitude2;
    }

    /**
     * * Longitude of second point
     */
    @Column(name = "Longitude2", unique = false, nullable = true, insertable = true, updatable = true, precision = 13, scale = 10)
    public BigDecimal getLongitude2()
    {
        return this.longitude2;
    }

    public void setLongitude2(BigDecimal longitude2)
    {
        this.longitude2 = longitude2;
    }

    /**
     * * the method used to determine the LatitudeLongitude
     */
    @Column(name = "LatLongMethod", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLatLongMethod()
    {
        return this.latLongMethod;
    }

    public void setLatLongMethod(String latLongMethod)
    {
        this.latLongMethod = latLongMethod;
    }

    /**
     * * radius -- in decimal degrees
     */
    @Column(name = "LatLongAccuracy", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getLatLongAccuracy()
    {
        return this.latLongAccuracy;
    }

    public void setLatLongAccuracy(Double latLongAccuracy)
    {
        this.latLongAccuracy = latLongAccuracy;
    }

    @Lob
    @Column(name = "GML")
    public String getGml()
    {
        return gml;
    }

    public void setGml(String gml)
    {
        this.gml = gml;
    }

    /**
     * * GPSDatum
     */
    @Column(name = "Datum", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getDatum()
    {
        return this.datum;
    }

    public void setDatum(String datum)
    {
        this.datum = datum;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /**
     * 
     */
    @Column(name = "Lat1Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLat1text()
    {
        return this.lat1text;
    }

    public void setLat1text(String lat1text)
    {
        this.lat1text = lat1text;
    }

    /**
     * 
     */
    @Column(name = "Lat2Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLat2text()
    {
        return this.lat2text;
    }

    public void setLat2text(String lat2text)
    {
        this.lat2text = lat2text;
    }

    /**
     * 
     */
    @Column(name = "Long1Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLong1text()
    {
        return this.long1text;
    }

    public void setLong1text(String long1text)
    {
        this.long1text = long1text;
    }

    /**
     * 
     */
    @Column(name = "Long2Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLong2text()
    {
        return this.long2text;
    }

    public void setLong2text(String long2text)
    {
        this.long2text = long2text;
    }

    /**
     * @return the guid
     */
    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getGuid()
    {
        return guid;
    }

    /**
     * @param guid the guid to set
     */
    public void setGuid(String guid)
    {
        this.guid = guid;
    }
    
    /**
     * @return the srcLatLongUnit
     */
    @Column(name = "SrcLatLongUnit", unique = false, nullable = false, insertable = true, updatable = true)
    public Byte getSrcLatLongUnit()
    {
        return srcLatLongUnit;
    }

    /**
     * @param srcLatLongUnit the srcLatLongUnit to set
     */
    public void setSrcLatLongUnit(Byte srcLatLongUnit)
    {
        this.srcLatLongUnit = srcLatLongUnit;
    }

    /**
     * * Indicates whether this record can be viewed - by owner, by institution, or by all
     */
    @Column(name = "Visibility", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getVisibility()
    {
        return this.visibility;
    }

    /**
     * @param visibility
     */
    public void setVisibility(Byte visibility)
    {
        this.visibility = visibility;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isRestrictable()
     */
    @Transient
    @Override
    public boolean isRestrictable()
    {
        return true;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "VisibilitySetByID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpecifyUser getVisibilitySetBy()
    {
        return this.visibilitySetBy;
    }

    public void setVisibilitySetBy(SpecifyUser visibilitySetBy)
    {
        this.visibilitySetBy = visibilitySetBy;
    }

    /**
     * Link to Country, State, County, WaterBody, Island, IslandGroup ... info
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "GeographyID", unique = false, nullable = true, insertable = true, updatable = true)
    public Geography getGeography()
    {
        return this.geography;
    }

    public void setGeography(Geography geography)
    {
        this.geography = geography;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "locality")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<LocalityCitation> getLocalityCitations()
    {
        return this.localityCitations;
    }

    public void setLocalityCitations(Set<LocalityCitation> localityCitations)
    {
        this.localityCitations = localityCitations;
    }

    //@OneToMany(cascade = { javax.persistence.CascadeType.ALL }, mappedBy = "locality")
    @OneToMany(mappedBy = "locality")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<LocalityAttachment> getLocalityAttachments()
    {
        return localityAttachments;
    }

    public void setLocalityAttachments(Set<LocalityAttachment> localityAttachments)
    {
        this.localityAttachments = localityAttachments;
    }

    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, mappedBy = "locality")
    public Set<LocalityNameAlias> getLocalityNameAliass()
    {
        return localityNameAliass;
    }

    public void setLocalityNameAliass(Set<LocalityNameAlias> localityNameAliass)
    {
        this.localityNameAliass = localityNameAliass;
    }

    /*
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "locality")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<CollectingEvent> getCollectingEvents()
    {
        return this.collectingEvents;
    }

    public void setCollectingEvents(Set<CollectingEvent> collectingEvents)
    {
        this.collectingEvents = collectingEvents;
    }
    */
    
    @SuppressWarnings("unchecked")
    @Transient
    public List<CollectingEvent> getCollectingEvents()
    {
        if (getId() != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                if (session != null)
                {
                    List<CollectingEvent> ces = (List<CollectingEvent>)session.getDataList("FROM CollectingEvent WHERE localityId = "+getId());
                    for (CollectingEvent ce : ces)
                    {
                        ce.getCollectionObjects().size(); // force load of COs
                    }
                    return ces;
                }
            }
            finally
            {
                session.close();
            }
        }
        return null;
    }

    
    /**
     * @return the localityDetail
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "locality")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<LocalityDetail> getLocalityDetails()
    {
        return localityDetails;
    }

    /**
     * @param localityDetail the localityDetail to set
     */
    public void setLocalityDetails(Set<LocalityDetail> localityDetails)
    {
        this.localityDetails = localityDetails;
    }

    /**
     * @return the geoCoordDetail
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "locality")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<GeoCoordDetail> getGeoCoordDetails()
    {
        return geoCoordDetails;
    }

    /**
     * @param geoCoordDetail the geoCoordDetail to set
     */
    public void setGeoCoordDetails(Set<GeoCoordDetail> geoCoordDetails)
    {
        this.geoCoordDetails = geoCoordDetails;
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
        return 2;
    }

    // //////////////////////////////
    // MapLocationIFace methods
    // //////////////////////////////

    /*
     * (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace#getLat1()
     */
    @Transient
    public Double getLat1()
    {
        return (latitude1 != null) ? latitude1.doubleValue() : null;
    }

    /*
     * (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace#getLat2()
     */
    @Transient
    public Double getLat2()
    {
        return (latitude2 != null) ? latitude2.doubleValue() : null;
    }

    /*
     * (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace#getLong1()
     */
    @Transient
    public Double getLong1()
    {
        return (longitude1 != null) ? longitude1.doubleValue() : null;
    }

    /*
     * (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace#getLong2()
     */
    @Transient
    public Double getLong2()
    {
        return (longitude2 != null) ? longitude2.doubleValue() : null;
    }

    @Transient
    public Set<LocalityAttachment> getAttachmentReferences()
    {
        return localityAttachments;
    }

    /**
     * @return List of pick lists for predefined system type codes.
     * 
     * The QueryBuilder function is used to generate picklist criteria controls for querying,
     * and to generate text values for the typed fields in query results and reports.
     * 
     * The WB uploader will also need this function.
     * 
     */
    @Transient
    public static List<PickListDBAdapterIFace> getSpSystemTypeCodes()
    {
        List<PickListDBAdapterIFace> result = new Vector<PickListDBAdapterIFace>(1);
        Vector<PickListItemIFace> lltypes = new Vector<PickListItemIFace>(3);
        //lltypes.add(new TypeCodeItem(UIRegistry.getResourceString("Locality.LL_TYPE_NONE"), null));
        lltypes.add(new TypeCodeItem(UIRegistry.getResourceString("Locality.LL_TYPE_POINT"), "Point"));
        lltypes.add(new TypeCodeItem(UIRegistry.getResourceString("Locality.LL_TYPE_LINE"), "Line"));
        lltypes.add(new TypeCodeItem(UIRegistry.getResourceString("Locality.LL_TYPE_RECTANGLE"), "Rectangle"));
        result.add(new TypeCode(lltypes, "latLongType"));
        return result;
    }

    /**
     * @return a list (probably never containing more than one element) of fields
     * with predefined system type codes.
     */
    @Transient
    public static String[] getSpSystemTypeCodeFlds()
    {
        String[] result = {"latLongType"};
        return result;
    }

}

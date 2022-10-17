/* Copyright (C) 2022, Specify Collections Consortium
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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.math.BigDecimal;

import javax.persistence.*;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
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
@Table(name = "locality", uniqueConstraints = {
       @UniqueConstraint(columnNames={"DisciplineID", "UniqueIdentifier"} )
       }
)
@org.hibernate.annotations.Table(appliesTo="locality", indexes =
    {   @Index (name="localityNameIDX", columnNames={"LocalityName"}),
        @Index (name="LocalityDisciplineIDX", columnNames={"DisciplineID"}),
        @Index (name="NamedPlaceIDX", columnNames={"NamedPlace"}),
            @Index (name="LocalityUniqueIdentifierIDX", columnNames={"UniqueIdentifier"}),
        @Index (name="RelationToNamedPlaceIDX", columnNames={"RelationToNamedPlace"})
    })
@SuppressWarnings("serial")
public class Locality extends DisciplineMember implements AttachmentOwnerIFace<LocalityAttachment>, 
                                                          java.io.Serializable, 
                                                          MapLocationIFace,
                                                          Cloneable
{
    protected static final Logger log = Logger.getLogger(Locality.class);

    // Fields
    protected Integer               localityId;
    protected String                namedPlace;
    protected String                shortName;
    protected String                relationToNamedPlace;
    protected String                localityName;
    protected String                verbatimElevation;
    protected String                originalElevationUnit;
    protected BigDecimal                minElevation;
    protected BigDecimal                maxElevation;
    protected String			    verbatimLatitude;
    protected String                verbatimLongitude;
    protected String                elevationMethod;
    protected BigDecimal                elevationAccuracy;
    protected Integer               originalLatLongUnit;
    protected String                latLongType;
    protected BigDecimal            latitude1;
    protected BigDecimal            longitude1;
    protected BigDecimal            latitude2;
    protected BigDecimal            longitude2;
    protected String                latLongMethod;
    protected BigDecimal                latLongAccuracy;
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
    protected String                text1;
    protected String                text2;
    protected String                text3;
    protected String                text4;
    protected String                text5;
    protected Boolean               yesNo1;
    protected Boolean               yesNo2;
    protected Boolean               yesNo3;
    protected Boolean               yesNo4;
    protected Boolean               yesNo5;
    protected Byte                  sgrStatus;
    protected PaleoContext			paleoContext;
    protected String uniqueIdentifier;
    
    
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
    
    protected Set<LatLonPolygon>      latLonpolygons;


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
        verbatimLatitude = null;
        verbatimLongitude = null;
        latLongType = null;
        latitude1 = null;
        longitude1 = null;
        latitude2 = null;
        longitude2 = null;
        latLongMethod = null;
        latLongAccuracy = null;
        datum = null;
        remarks = null;
        sgrStatus = null;
        paleoContext = null;
        uniqueIdentifier = null;

        
        lat1text   = null;
        lat2text   = null;
        long1text  = null;
        long2text  = null;
        visibility = null;
        guid       = null;
        text1      = null;
        text2      = null;
        text3	   = null;
        text4      = null;
        text5      = null;
        yesNo1     = null;
        yesNo2     = null;
        yesNo3     = null;
        yesNo4     = null;
        yesNo5     = null;
        
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
        latLonpolygons      = new HashSet<LatLonPolygon>();
        
        hasGUIDField = true;
        setGUID();
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
     *
     */
    @Column(name = "UniqueIdentifier", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    /**
    *
    */
   @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
   @JoinColumn(name = "PaleoContextID", unique = false, nullable = true, insertable = true, updatable = true)
   public PaleoContext getPaleoContext()
   {
       return this.paleoContext;
   }

   public void setPaleoContext(PaleoContext paleoContext)
   {
       this.paleoContext = paleoContext;
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
	 * @return the sgrStatus
	 */
    @Column(name = "SGRStatus", unique = false, nullable = true, insertable = true, updatable = true)
	public Byte getSgrStatus() 
	{
		return sgrStatus;
	}

	/**
	 * @param sgrStatus the sgrStatus to set
	 */
	public void setSgrStatus(Byte sgrStatus) 
	{
		this.sgrStatus = sgrStatus;
	}

    /**
     * * The full name of the locality.
     */
    @Column(name = "LocalityName", unique = false, nullable = false, insertable = true, updatable = true, length=2048)
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
    @Column(name = "MinElevation", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
    public BigDecimal getMinElevation()
    {
        return this.minElevation;
    }

    public void setMinElevation(BigDecimal minElevation)
    {
        this.minElevation = minElevation;
    }

    /**
     * * The maximum elevation in Meters
     */
    @Column(name = "MaxElevation", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
    public BigDecimal getMaxElevation()
    {
        return this.maxElevation;
    }

    public void setMaxElevation(BigDecimal maxElevation)
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
    @Column(name = "ElevationAccuracy", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
    public BigDecimal getElevationAccuracy()
    {
        return this.elevationAccuracy;
    }

    public void setElevationAccuracy(BigDecimal elevationAccuracy)
    {
        this.elevationAccuracy = elevationAccuracy;
    }

    /**
     * * i.e. Decimal, Deg/Decimal Min, Deg/Min/Decimal Sec, ...
     */
    @Column(name = "OriginalLatLongUnit", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getOriginalLatLongUnit()
    {
        return this.originalLatLongUnit;
    }
    
    /**
     * @return
     */
    @Transient
    public boolean isOriginalLatLongUnitEmpty()
    {
        return this.originalLatLongUnit == null;
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
    @Column(name = "LatLongAccuracy", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
    public BigDecimal getLatLongAccuracy()
    {
        return this.latLongAccuracy;
    }

    public void setLatLongAccuracy(BigDecimal latLongAccuracy)
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
	 * @return the verbatimLatitude
	 */
    @Column(name = "VerbatimLatitude", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getVerbatimLatitude() {
		return verbatimLatitude;
	}

	/**
	 * @param verbatimLatitude the verbatimLatitude to set
	 */
	public void setVerbatimLatitude(String verbatimLatitude) {
		this.verbatimLatitude = verbatimLatitude;
	}

	/**
	 * @return the verbatimLongitude
	 */
    @Column(name = "VerbatimLongitude", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getVerbatimLongitude() {
		return verbatimLongitude;
	}

	/**
	 * @param verbatimLongitude the verbatimLongitude to set
	 */
	public void setVerbatimLongitude(String verbatimLongitude) {
		this.verbatimLongitude = verbatimLongitude;
	}

	/**
     * @return the guid
     */
    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = false, length = 128)
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
     * @return the text1
     */
    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1()
    {
        return text1;
    }

    /**
     * @param text1 the text1 to set
     */
    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     * @return the text2
     */
    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2()
    {
        return text2;
    }

    /**
     * @param text2 the text2 to set
     */
    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    /**
     * @return the text3
     */
    @Lob
    @Column(name = "Text3", length = 65535)
    public String getText3()
    {
        return text3;
    }

    /**
     * @param text3 the text3 to set
     */
    public void setText3(String text3)
    {
        this.text3 = text3;
    }

    /**
     * @return the text4
     */
    @Lob
    @Column(name = "Text4", length = 65535)
    public String getText4()
    {
        return text4;
    }

    /**
     * @param text4 the text4 to set
     */
    public void setText4(String text4)
    {
        this.text4 = text4;
    }

    /**
     * @return the text5
     */
    @Lob
    @Column(name = "Text5", length = 65535)
    public String getText5()
    {
        return text5;
    }

    /**
     * @param text5 the text5 to set
     */
    public void setText5(String text5)
    {
        this.text5 = text5;
    }

    
    /**
	 * @return the yesNo1
	 */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getYesNo1() {
		return yesNo1;
	}

	/**
	 * @param yesNo1 the yesNo1 to set
	 */
	public void setYesNo1(Boolean yesNo1) {
		this.yesNo1 = yesNo1;
	}

	/**
	 * @return the yesNo2
	 */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getYesNo2() {
		return yesNo2;
	}

	/**
	 * @param yesNo2 the yesNo2 to set
	 */
	public void setYesNo2(Boolean yesNo2) {
		this.yesNo2 = yesNo2;
	}

	/**
	 * @return the yesNo3
	 */
    @Column(name="YesNo3",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getYesNo3() {
		return yesNo3;
	}

	/**
	 * @param yesNo3 the yesNo3 to set
	 */
	public void setYesNo3(Boolean yesNo3) {
		this.yesNo3 = yesNo3;
	}

	/**
	 * @return the yesNo4
	 */
    @Column(name="YesNo4",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getYesNo4() {
		return yesNo4;
	}

	/**
	 * @param yesNo4 the yesNo4 to set
	 */
	public void setYesNo4(Boolean yesNo4) {
		this.yesNo4 = yesNo4;
	}

	/**
	 * @return the yesNo5
	 */
    @Column(name="YesNo5",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getYesNo5() {
		return yesNo5;
	}

	/**
	 * @param yesNo5 the yesNo5 to set
	 */
	public void setYesNo5(Boolean yesNo5) {
		this.yesNo5 = yesNo5;
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Discipline.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return discipline != null ? discipline.getId() : null;
    }
    
    /**
     * @return
     */
    @Transient
    public List<CollectingEvent> getCollectingEvents()
    {
        return getCollectingEvents(true);
    }
    
    /**
     * @param doLoadColObjs
     * @return
     */
    @SuppressWarnings("unchecked")
    @Transient
    public List<CollectingEvent> getCollectingEvents(final boolean doLoadColObjs)
    {
        if (getId() != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                if (session != null)
                {
                    List<CollectingEvent> ces = (List<CollectingEvent>)session.getDataList("FROM CollectingEvent WHERE localityId = "+getId());
                    ces.size(); // load all CEs
                    if (doLoadColObjs)
                    {
                        for (CollectingEvent ce : ces)
                        {
                            ce.getCollectionObjects().size(); // force load of COs
                        }
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

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        if (localityDetails != null) localityDetails.size();
        if (geoCoordDetails != null) geoCoordDetails.size();
        if (latLonpolygons != null) latLonpolygons.size();
        if (localityCitations != null) localityCitations.size();
        if (localityNameAliass != null) localityNameAliass.size();
        //if (localityAttachments != null) localityAttachments.size();
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
     * @param localityDetails the localityDetails to set
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
     * @param geoCoordDetails the geoCoordDetails to set
     */
    public void setGeoCoordDetails(Set<GeoCoordDetail> geoCoordDetails)
    {
        this.geoCoordDetails = geoCoordDetails;
    }

    /**
     * @return the latLonpolygons
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "locality")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<LatLonPolygon> getLatLonpolygons()
    {
        return latLonpolygons;
    }

    /**
     * @param latLonpolygons the latLonpolygons to set
     */
    public void setLatLonpolygons(Set<LatLonPolygon> latLonpolygons)
    {
        this.latLonpolygons = latLonpolygons;
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentTableId()
     */
    @Override
    @Transient
    public int getAttachmentTableId()
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
    
    /**
     * @param newLocality
     * @return
     * @throws CloneNotSupportedException
     */
    private Locality doClone(final Locality newLocality) throws CloneNotSupportedException
    {
        newLocality.localityDetails = new HashSet<LocalityDetail>();
        for (LocalityDetail obj : localityDetails)
        {
            LocalityDetail ld = (LocalityDetail)obj.clone();
            newLocality.localityDetails.add(ld);
            ld.setLocality(newLocality);
        }
        
        newLocality.geoCoordDetails = new HashSet<GeoCoordDetail>();
        for (GeoCoordDetail obj : geoCoordDetails)
        {
            GeoCoordDetail gcd = (GeoCoordDetail)obj.clone();
            newLocality.geoCoordDetails.add(gcd);
            gcd.setLocality(newLocality);
        }        
        
        newLocality.localityCitations = new HashSet<LocalityCitation>();
        for (LocalityCitation obj : localityCitations)
        {
            LocalityCitation lc = (LocalityCitation)obj.clone();
            newLocality.localityCitations.add(lc);
            lc.setLocality(newLocality);
        } 
        
        newLocality.localityNameAliass  = new HashSet<LocalityNameAlias>();
        for (LocalityNameAlias obj : localityNameAliass)
        {
            LocalityNameAlias lna = (LocalityNameAlias)obj.clone();
            newLocality.localityNameAliass.add(lna);
            lna.setLocality(newLocality);
        } 
        
        newLocality.localityAttachments = new HashSet<LocalityAttachment>();
        
        newLocality.latLonpolygons = new HashSet<LatLonPolygon>();
        for (LatLonPolygon obj : latLonpolygons)
        {
            LatLonPolygon llp = (LatLonPolygon)obj.clone();
            newLocality.latLonpolygons.add(llp);
            llp.setLocality(newLocality);
        }
        
        if (paleoContext != null) {
        	Discipline d = AppContextMgr.getInstance().getClassObject(Discipline.class);
        	if (d.getIsPaleoContextEmbedded()) {
        		PaleoContext newPc = (PaleoContext)paleoContext.clone();
        		newPc.setLocalities(new HashSet<Locality>());
        		newPc.getLocalities().add(newLocality);
        		newLocality.paleoContext = newPc;
        	}
        }
        		
        newLocality.setGUID();
        
        return newLocality;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Locality l = (Locality)super.clone();
        l.localityId = null;
        
        try
        {
            try
            {
                l = doClone(l);
                
            } catch (org.hibernate.LazyInitializationException hex)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    session.attach(this);
                    
                    l = doClone(l);
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ValComboBoxFromQuery.class, ex);
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                } 
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ValComboBoxFromQuery.class, ex);
        }
        return l;
    }

    /**
     *
     * @param originalObj
     * @param deep  if true then copy and clone children
     * @param session
     * @return
     */
    @Override
    public boolean initializeClone(DataModelObjBase originalObj, boolean deep, DataProviderSessionIFace session) {
        if (deep) {
            log.error(getClass().getName() + ": initializeClone is not implemented for deep = true.");
            return false;
        }
        return true;
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<LocalityAttachment> getAttachmentReferences()
    {
        return localityAttachments;
    }
    
    // Helpers for GeoCoord
    @Transient
    private GeoCoordDetail getGeoCoordDetail()
    {
        return geoCoordDetails == null || geoCoordDetails.size() == 0 ? null :
               geoCoordDetails.iterator().next();
    }
    
    @Transient
    public String getErrorPolygon()
    {
        GeoCoordDetail gcd = getGeoCoordDetail();
        if (gcd != null)
        {
            return gcd.getErrorPolygon();
        }
        return null;
    }

    @Transient
    public BigDecimal getErrorEstimate()
    {
        GeoCoordDetail gcd = getGeoCoordDetail();
        if (gcd != null)
        {
            return gcd.getMaxUncertaintyEst();
        }
        return null;
    }
    
    /**
     * @return
     */
    private GeoCoordDetail createGeoCoordDetail()
    {
        GeoCoordDetail gcd = new GeoCoordDetail();
        gcd.initialize();
        gcd.setLocality(this);
        if (geoCoordDetails == null)
        {
            geoCoordDetails = new HashSet<GeoCoordDetail>();
        }
        geoCoordDetails.add(gcd);
        return gcd;
    }
    
    /**
     * @param errorPolygon
     */
    public void setErrorPolygon(final String errorPolygon)
    {
        GeoCoordDetail gcd = getGeoCoordDetail();
        if (gcd == null)
        {
            gcd = createGeoCoordDetail();
        }
        gcd.setErrorPolygon(errorPolygon);
    }

    
    /**
     * @param errEst
     */
    public void setErrorEstimate(final BigDecimal errEst)
    {
        GeoCoordDetail gcd = getGeoCoordDetail();
        if (gcd == null)
        {
            gcd = createGeoCoordDetail();
        }
        gcd.setMaxUncertaintyEst(errEst);
        gcd.setMaxUncertaintyEstUnit("m");
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

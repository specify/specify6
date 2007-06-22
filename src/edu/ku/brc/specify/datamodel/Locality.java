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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "locality")
public class Locality extends DataModelObjBase implements java.io.Serializable, MapLocationIFace {

    // Fields    

     protected Long localityId;
     protected String namedPlace;
     protected String relationToNamedPlace;
     protected String localityName;
     protected String baseMeridian;
     protected String range;
     protected String rangeDirection;
     protected String township;
     protected String townshipDirection;
     protected String section;
     protected String sectionPart;
     protected String verbatimElevation;
     protected String originalElevationUnit;
     protected Double minElevation;
     protected Double maxElevation;
     protected String elevationMethod;
     protected Double elevationAccuracy;
     protected Integer originalLatLongUnit;
     protected String latLongType;
     protected BigDecimal latitude1;
     protected BigDecimal longitude1;
     protected BigDecimal latitude2;
     protected BigDecimal longitude2;
     protected String latLongMethod;
     protected Double latLongAccuracy;
     protected String datum;
     protected Integer groupPermittedToView;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Double number1;
     protected Double number2;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected String lat1text;
     protected String lat2text;
     protected String long1text;
     protected String long2text;
     protected String nationalParkName;
     protected String islandGroup;
     protected String island;
     protected String waterBody;
     protected String drainage;
     protected Integer visibility;
     protected String visibilitySetBy;
     protected Set<CollectionObjDef> collectionObjDefs;
     protected Geography geography;
     protected Set<LocalityCitation> localityCitations;
     protected Set<CollectingEvent> collectingEvents;
     protected Set<Attachment>          attachments;


    // Constructors

    /** default constructor */
    public Locality()
    {
        // do nothing
    }
    
    /** constructor with id */
    public Locality(Long localityId) {
        this.localityId = localityId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        localityId = null;
        namedPlace = null;
        relationToNamedPlace = null;
        localityName = null;
        baseMeridian = null;
        range = null;
        rangeDirection = null;
        township = null;
        townshipDirection = null;
        section = null;
        sectionPart = null;
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
        groupPermittedToView = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        lat1text = null;
        lat2text = null;
        long1text = null;
        long2text = null;
        nationalParkName = null;
        islandGroup = null;
        island = null;
        waterBody = null;
        drainage = null;
        visibility = null;
        collectionObjDefs = new HashSet<CollectionObjDef>();
        geography = null;
        localityCitations = new HashSet<LocalityCitation>();
        collectingEvents = new HashSet<CollectingEvent>();
        attachments = new HashSet<Attachment>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "LocalityID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getLocalityId() {
        return this.localityId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
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
    
    public void setLocalityId(Long localityId) {
        this.localityId = localityId;
    }

    /**
     *      * The named place the locality is closest to
     */
    @Column(name = "NamedPlace", unique = false, nullable = true, insertable = true, updatable = true)
    public String getNamedPlace() {
        return this.namedPlace;
    }
    
    public void setNamedPlace(String namedPlace) {
        this.namedPlace = namedPlace;
    }

    /**
     *      * Relation of the locality to the named place
     */
    @Column(name = "RelationToNamedPlace", unique = false, nullable = true, insertable = true, updatable = true, length = 120)
    public String getRelationToNamedPlace() {
        return this.relationToNamedPlace;
    }
    
    public void setRelationToNamedPlace(String relationToNamedPlace) {
        this.relationToNamedPlace = relationToNamedPlace;
    }

    /**
     *      * The full name of the locality.
     */
    @Column(name = "LocalityName", unique = false, nullable = false, insertable = true, updatable = true)
    public String getLocalityName() {
        return this.localityName;
    }
    
    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    /**
     *      * BaseMeridian for the Range/Township/Section data
     */
    @Column(name = "BaseMeridian", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getBaseMeridian() {
        return this.baseMeridian;
    }
    
    public void setBaseMeridian(String baseMeridian) {
        this.baseMeridian = baseMeridian;
    }

    /**
     *      * The Range of a legal description
     */
    @Column(name = "Range", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getRange() {
        return this.range;
    }
    
    public void setRange(String range) {
        this.range = range;
    }

    /**
     * 
     */
    @Column(name = "RangeDirection", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getRangeDirection() {
        return this.rangeDirection;
    }
    
    public void setRangeDirection(String rangeDirection) {
        this.rangeDirection = rangeDirection;
    }

    /**
     *      * The Township of a legal description
     */
    @Column(name = "Township", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getTownship() {
        return this.township;
    }
    
    public void setTownship(String township) {
        this.township = township;
    }

    /**
     * 
     */
    @Column(name = "TownshipDirection", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getTownshipDirection() {
        return this.townshipDirection;
    }
    
    public void setTownshipDirection(String townshipDirection) {
        this.townshipDirection = townshipDirection;
    }

    /**
     *      * The Section of a legal description
     */
    @Column(name = "Section", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getSection() {
        return this.section;
    }
    
    public void setSection(String section) {
        this.section = section;
    }

    /**
     * 
     */
    @Column(name = "SectionPart", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getSectionPart() {
        return this.sectionPart;
    }
    
    public void setSectionPart(String sectionPart) {
        this.sectionPart = sectionPart;
    }

    /**
     *      * The verbatim elevation including units as given in the field notes
     */
    @Column(name = "VerbatimElevation", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getVerbatimElevation() {
        return this.verbatimElevation;
    }
    
    public void setVerbatimElevation(String verbatimElevation) {
        this.verbatimElevation = verbatimElevation;
    }

    /**
     *      * i.e. Meters, Feet, ...
     */
    @Column(name = "OriginalElevationUnit", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getOriginalElevationUnit() {
        return this.originalElevationUnit;
    }
    
    public void setOriginalElevationUnit(String originalElevationUnit) {
        this.originalElevationUnit = originalElevationUnit;
    }

    /**
     *      * The minimum elevation in Meters
     */
    @Column(name = "MinElevation", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getMinElevation() {
        return this.minElevation;
    }
    
    public void setMinElevation(Double minElevation) {
        this.minElevation = minElevation;
    }

    /**
     *      * The maximum elevation in Meters
     */
    @Column(name = "MaxElevation", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getMaxElevation() {
        return this.maxElevation;
    }
    
    public void setMaxElevation(Double maxElevation) {
        this.maxElevation = maxElevation;
    }

    /**
     *      * The method used to determine the elevation
     */
    @Column(name = "ElevationMethod", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getElevationMethod() {
        return this.elevationMethod;
    }
    
    public void setElevationMethod(String elevationMethod) {
        this.elevationMethod = elevationMethod;
    }

    /**
     *      * plus or minus -- in meters
     */
    @Column(name = "ElevationAccuracy", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getElevationAccuracy() {
        return this.elevationAccuracy;
    }
    
    public void setElevationAccuracy(Double elevationAccuracy) {
        this.elevationAccuracy = elevationAccuracy;
    }

    /**
     *      * i.e. Decimal, Deg/Min/Sec, ...
     */
    @Column(name = "OriginalLatLongUnit", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Integer getOriginalLatLongUnit() {
        return this.originalLatLongUnit;
    }
    
    public void setOriginalLatLongUnit(Integer originalLatLongUnit) {
        this.originalLatLongUnit = originalLatLongUnit;
    }

    /**
     *      * The type of area described by the lat long data (Point,Line,Rectangle)
     */
    @Column(name = "LatLongType", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLatLongType() {
        return this.latLongType;
    }
    
    public void setLatLongType(String latLongType) {
        this.latLongType = latLongType;
    }

    /**
     *      * Latitude of first point
     */
    @Column(name = "Latitude1", unique = false, nullable = true, insertable = true, updatable = true, precision = 12, scale = 10)
    public BigDecimal getLatitude1() {
        return this.latitude1;
    }
    
    public void setLatitude1(BigDecimal latitude1) {
        this.latitude1 = latitude1;
    }

    /**
     *      * Longitude of first point
     */
    @Column(name = "Longitude1", unique = false, nullable = true, insertable = true, updatable = true, precision = 13, scale = 10)
    public BigDecimal getLongitude1() {
        return this.longitude1;
    }
    
    public void setLongitude1(BigDecimal longitude1) {
        this.longitude1 = longitude1;
    }

    /**
     *      * Latitude of second point
     */
    @Column(name = "Latitude2", unique = false, nullable = true, insertable = true, updatable = true, precision = 12, scale = 10)
    public BigDecimal getLatitude2() {
        return this.latitude2;
    }
    
    public void setLatitude2(BigDecimal latitude2) {
        this.latitude2 = latitude2;
    }

    /**
     *      * Longitude of second point
     */
    @Column(name = "Longitude2", unique = false, nullable = true, insertable = true, updatable = true, precision = 13, scale = 10)
    public BigDecimal getLongitude2() {
        return this.longitude2;
    }
    
    public void setLongitude2(BigDecimal longitude2) {
        this.longitude2 = longitude2;
    }

    /**
     *      * the method used to determine the LatitudeLongitude
     */
    @Column(name = "LatLongMethod", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLatLongMethod() {
        return this.latLongMethod;
    }
    
    public void setLatLongMethod(String latLongMethod) {
        this.latLongMethod = latLongMethod;
    }

    /**
     *      * radius -- in decimal degrees
     */
    @Column(name = "LatLongAccuracy", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getLatLongAccuracy() {
        return this.latLongAccuracy;
    }
    
    public void setLatLongAccuracy(Double latLongAccuracy) {
        this.latLongAccuracy = latLongAccuracy;
    }

    /**
     *      * GPSDatum
     */
    @Column(name = "Datum", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getDatum() {
        return this.datum;
    }
    
    public void setDatum(String datum) {
        this.datum = datum;
    }

    /**
     *      * The name of the group that this record is visible to. (Default to public)
     */
    @Column(name = "GroupPermittedToView", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     * 
     */
    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text1", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Double number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Double number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * 
     */
    @Column(name = "Lat1Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLat1text() {
        return this.lat1text;
    }
    
    public void setLat1text(String lat1text) {
        this.lat1text = lat1text;
    }

    /**
     * 
     */
    @Column(name = "Lat2Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLat2text() {
        return this.lat2text;
    }
    
    public void setLat2text(String lat2text) {
        this.lat2text = lat2text;
    }

    /**
     * 
     */
    @Column(name = "Long1Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLong1text() {
        return this.long1text;
    }
    
    public void setLong1text(String long1text) {
        this.long1text = long1text;
    }

    /**
     * 
     */
    @Column(name = "Long2Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLong2text() {
        return this.long2text;
    }
    
    public void setLong2text(String long2text) {
        this.long2text = long2text;
    }

    /**
     * 
     */
    @Column(name = "NationalParkName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getNationalParkName() {
        return this.nationalParkName;
    }
    
    public void setNationalParkName(String nationalParkName) {
        this.nationalParkName = nationalParkName;
    }

    /**
     * 
     */
    @Column(name = "IslandGroup", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getIslandGroup() {
        return this.islandGroup;
    }
    
    public void setIslandGroup(String islandGroup) {
        this.islandGroup = islandGroup;
    }

    /**
     * 
     */
    @Column(name = "Island", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getIsland() {
        return this.island;
    }
    
    public void setIsland(String island) {
        this.island = island;
    }

    /**
     * 
     */
    @Column(name = "WaterBody", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getWaterBody() {
        return this.waterBody;
    }
    
    public void setWaterBody(String waterBody) {
        this.waterBody = waterBody;
    }

    /**
     * 
     */
    @Column(name = "Drainage", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getDrainage() {
        return this.drainage;
    }
    
    public void setDrainage(String drainage) {
        this.drainage = drainage;
    }
    
    /**
     *      * Indicates whether this record can be viewed - by owner, by instituion, or by all
     */
    @Column(name = "Visibility", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Integer getVisibility() {
        return this.visibility;
    }
    
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
    
    @Transient
    @Override
    public boolean isRestrictable()
    {
        return true;
    }  
    
    /**
     * 
     */
    @Column(name = "VisibilitySetBy", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getVisibilitySetBy() {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(String visibilitySetBy) {
        this.visibilitySetBy = visibilitySetBy;
    }
    
    /**
     * 
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "colobjdef_locality", joinColumns = { @JoinColumn(name = "LocalityID", unique = false, nullable = false, insertable = true, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "CollectionObjDefID", unique = false, nullable = false, insertable = true, updatable = false) })
    public Set<CollectionObjDef> getCollectionObjDefs() {
        return this.collectionObjDefs;
    }
    
    public void setCollectionObjDefs(Set<CollectionObjDef> collectionObjDefs) {
        this.collectionObjDefs = collectionObjDefs;
    }

    /**
     *      * Link to Country, State, County, WaterBody, Island, IslandGroup ... info
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "GeographyID", unique = false, nullable = false, insertable = true, updatable = true)
    public Geography getGeography() {
        return this.geography;
    }
    
    public void setGeography(Geography geography) {
        this.geography = geography;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "locality")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<LocalityCitation> getLocalityCitations() {
        return this.localityCitations;
    }
    
    public void setLocalityCitations(Set<LocalityCitation> localityCitations) {
        this.localityCitations = localityCitations;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "locality")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "locality")
    public Set<CollectingEvent> getCollectingEvents() {
        return this.collectingEvents;
    }
    
    public void setCollectingEvents(Set<CollectingEvent> collectingEvents) {
        this.collectingEvents = collectingEvents;
    }

    // Add Methods

    public void addCollectionObjDefs(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDefs.add(collectionObjDef);
        collectionObjDef.getLocalities().add(this);
    }

    public void addLocalityCitations(final LocalityCitation localityCitation)
    {
        this.localityCitations.add(localityCitation);
        localityCitation.setLocality(this);
    }

    public void addCollectingEvents(final CollectingEvent collectingEvent)
    {
        this.collectingEvents.add(collectingEvent);
        collectingEvent.setLocality(this);
    }


    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjDefs(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDefs.remove(collectionObjDef);
        collectionObjDef.getLocalities().add(this);
    }

    public void removeLocalityCitations(final LocalityCitation localityCitation)
    {
        this.localityCitations.remove(localityCitation);
        localityCitation.setLocality(null);
    }

    public void removeCollectingEvents(final CollectingEvent collectingEvent)
    {
        this.collectingEvents.remove(collectingEvent);
        collectingEvent.setLocality(null);
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
        return 2;
    }

    ////////////////////////////////
    // MapLocationIFace methods
    ////////////////////////////////
    
    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace#getLat1()
     */
    @Transient
    public Double getLat1()
    {
        return (latitude1 != null) ? latitude1.doubleValue() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace#getLat2()
     */
    @Transient
    public Double getLat2()
    {
        return (latitude2 != null) ? latitude2.doubleValue() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace#getLong1()
     */
    @Transient
    public Double getLong1()
    {
        return (longitude1 != null) ? longitude1.doubleValue() : null;
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace#getLong2()
     */
    @Transient
    public Double getLong2()
    {
        return (longitude2 != null) ? longitude2.doubleValue() : null;
    }

}

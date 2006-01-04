package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="Locality"
 *     
 */
public class Locality  implements java.io.Serializable {

    // Fields    

     protected Integer localityId;
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
     protected Float minElevation;
     protected Float maxElevation;
     protected String elevationMethod;
     protected Float elevationAccuracy;
     protected Integer originalLatLongUnit;
     protected String latLongType;
     protected Float latitude1;
     protected Float longitude1;
     protected Float latitude2;
     protected Float longitude2;
     protected String latLongMethod;
     protected Float latLongAccuracy;
     protected String datum;
     protected Integer groupPermittedToView;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Integer elevationMethodId;
     protected Integer latLongTypeId;
     protected Integer latLongMethodId;
     protected Short yesNo1;
     protected Short yesNo2;
     protected String lat1text;
     protected String lat2text;
     protected String long1text;
     protected String long2text;
     protected Geography geographyByGeographyId1;
     protected Set localityCitations;
     protected Set collectingEvents;
     protected Set imageLocalities;


    // Constructors

    /** default constructor */
    public Locality() {
    }
    
    /** constructor with id */
    public Locality(Integer localityId) {
        this.localityId = localityId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="LocalityID"
     *         
     */
    public Integer getLocalityId() {
        return this.localityId;
    }
    
    public void setLocalityId(Integer localityId) {
        this.localityId = localityId;
    }

    /**
     *      *            @hibernate.property
     *             column="NamedPlace"
     *             length="255"
     *         
     */
    public String getNamedPlace() {
        return this.namedPlace;
    }
    
    public void setNamedPlace(String namedPlace) {
        this.namedPlace = namedPlace;
    }

    /**
     *      *            @hibernate.property
     *             column="RelationToNamedPlace"
     *             length="120"
     *         
     */
    public String getRelationToNamedPlace() {
        return this.relationToNamedPlace;
    }
    
    public void setRelationToNamedPlace(String relationToNamedPlace) {
        this.relationToNamedPlace = relationToNamedPlace;
    }

    /**
     *      *            @hibernate.property
     *             column="LocalityName"
     *             length="375"
     *             not-null="true"
     *         
     */
    public String getLocalityName() {
        return this.localityName;
    }
    
    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    /**
     *      *            @hibernate.property
     *             column="BaseMeridian"
     *             length="50"
     *         
     */
    public String getBaseMeridian() {
        return this.baseMeridian;
    }
    
    public void setBaseMeridian(String baseMeridian) {
        this.baseMeridian = baseMeridian;
    }

    /**
     *      *            @hibernate.property
     *             column="Range"
     *             length="50"
     *         
     */
    public String getRange() {
        return this.range;
    }
    
    public void setRange(String range) {
        this.range = range;
    }

    /**
     *      *            @hibernate.property
     *             column="RangeDirection"
     *             length="50"
     *         
     */
    public String getRangeDirection() {
        return this.rangeDirection;
    }
    
    public void setRangeDirection(String rangeDirection) {
        this.rangeDirection = rangeDirection;
    }

    /**
     *      *            @hibernate.property
     *             column="Township"
     *             length="50"
     *         
     */
    public String getTownship() {
        return this.township;
    }
    
    public void setTownship(String township) {
        this.township = township;
    }

    /**
     *      *            @hibernate.property
     *             column="TownshipDirection"
     *             length="50"
     *         
     */
    public String getTownshipDirection() {
        return this.townshipDirection;
    }
    
    public void setTownshipDirection(String townshipDirection) {
        this.townshipDirection = townshipDirection;
    }

    /**
     *      *            @hibernate.property
     *             column="Section"
     *             length="50"
     *         
     */
    public String getSection() {
        return this.section;
    }
    
    public void setSection(String section) {
        this.section = section;
    }

    /**
     *      *            @hibernate.property
     *             column="SectionPart"
     *             length="50"
     *         
     */
    public String getSectionPart() {
        return this.sectionPart;
    }
    
    public void setSectionPart(String sectionPart) {
        this.sectionPart = sectionPart;
    }

    /**
     *      *            @hibernate.property
     *             column="VerbatimElevation"
     *             length="50"
     *         
     */
    public String getVerbatimElevation() {
        return this.verbatimElevation;
    }
    
    public void setVerbatimElevation(String verbatimElevation) {
        this.verbatimElevation = verbatimElevation;
    }

    /**
     *      *            @hibernate.property
     *             column="OriginalElevationUnit"
     *             length="50"
     *         
     */
    public String getOriginalElevationUnit() {
        return this.originalElevationUnit;
    }
    
    public void setOriginalElevationUnit(String originalElevationUnit) {
        this.originalElevationUnit = originalElevationUnit;
    }

    /**
     *      *            @hibernate.property
     *             column="MinElevation"
     *             length="24"
     *         
     */
    public Float getMinElevation() {
        return this.minElevation;
    }
    
    public void setMinElevation(Float minElevation) {
        this.minElevation = minElevation;
    }

    /**
     *      *            @hibernate.property
     *             column="MaxElevation"
     *             length="24"
     *         
     */
    public Float getMaxElevation() {
        return this.maxElevation;
    }
    
    public void setMaxElevation(Float maxElevation) {
        this.maxElevation = maxElevation;
    }

    /**
     *      *            @hibernate.property
     *             column="ElevationMethod"
     *             length="50"
     *         
     */
    public String getElevationMethod() {
        return this.elevationMethod;
    }
    
    public void setElevationMethod(String elevationMethod) {
        this.elevationMethod = elevationMethod;
    }

    /**
     *      *            @hibernate.property
     *             column="ElevationAccuracy"
     *             length="24"
     *         
     */
    public Float getElevationAccuracy() {
        return this.elevationAccuracy;
    }
    
    public void setElevationAccuracy(Float elevationAccuracy) {
        this.elevationAccuracy = elevationAccuracy;
    }

    /**
     *      *            @hibernate.property
     *             column="OriginalLatLongUnit"
     *             length="10"
     *         
     */
    public Integer getOriginalLatLongUnit() {
        return this.originalLatLongUnit;
    }
    
    public void setOriginalLatLongUnit(Integer originalLatLongUnit) {
        this.originalLatLongUnit = originalLatLongUnit;
    }

    /**
     *      *            @hibernate.property
     *             column="LatLongType"
     *             length="50"
     *         
     */
    public String getLatLongType() {
        return this.latLongType;
    }
    
    public void setLatLongType(String latLongType) {
        this.latLongType = latLongType;
    }

    /**
     *      *            @hibernate.property
     *             column="Latitude1"
     *             length="24"
     *         
     */
    public Float getLatitude1() {
        return this.latitude1;
    }
    
    public void setLatitude1(Float latitude1) {
        this.latitude1 = latitude1;
    }

    /**
     *      *            @hibernate.property
     *             column="Longitude1"
     *             length="24"
     *         
     */
    public Float getLongitude1() {
        return this.longitude1;
    }
    
    public void setLongitude1(Float longitude1) {
        this.longitude1 = longitude1;
    }

    /**
     *      *            @hibernate.property
     *             column="Latitude2"
     *             length="24"
     *         
     */
    public Float getLatitude2() {
        return this.latitude2;
    }
    
    public void setLatitude2(Float latitude2) {
        this.latitude2 = latitude2;
    }

    /**
     *      *            @hibernate.property
     *             column="Longitude2"
     *             length="24"
     *         
     */
    public Float getLongitude2() {
        return this.longitude2;
    }
    
    public void setLongitude2(Float longitude2) {
        this.longitude2 = longitude2;
    }

    /**
     *      *            @hibernate.property
     *             column="LatLongMethod"
     *             length="50"
     *         
     */
    public String getLatLongMethod() {
        return this.latLongMethod;
    }
    
    public void setLatLongMethod(String latLongMethod) {
        this.latLongMethod = latLongMethod;
    }

    /**
     *      *            @hibernate.property
     *             column="LatLongAccuracy"
     *             length="24"
     *         
     */
    public Float getLatLongAccuracy() {
        return this.latLongAccuracy;
    }
    
    public void setLatLongAccuracy(Float latLongAccuracy) {
        this.latLongAccuracy = latLongAccuracy;
    }

    /**
     *      *            @hibernate.property
     *             column="Datum"
     *             length="50"
     *         
     */
    public String getDatum() {
        return this.datum;
    }
    
    public void setDatum(String datum) {
        this.datum = datum;
    }

    /**
     *      *            @hibernate.property
     *             column="GroupPermittedToView"
     *             length="10"
     *         
     */
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
     *             length="1073741823"
     *         
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      *            @hibernate.property
     *             column="Text1"
     *             length="300"
     *         
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      *            @hibernate.property
     *             column="Text2"
     *             length="300"
     *         
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      *            @hibernate.property
     *             column="Number1"
     *             length="24"
     *         
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      *            @hibernate.property
     *             column="Number2"
     *             length="24"
     *         
     */
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampCreated"
     *             length="23"
     *         
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampModified"
     *             length="23"
     *         
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      *            @hibernate.property
     *             column="LastEditedBy"
     *             length="50"
     *         
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      *            @hibernate.property
     *             column="ElevationMethodID"
     *             length="10"
     *         
     */
    public Integer getElevationMethodId() {
        return this.elevationMethodId;
    }
    
    public void setElevationMethodId(Integer elevationMethodId) {
        this.elevationMethodId = elevationMethodId;
    }

    /**
     *      *            @hibernate.property
     *             column="LatLongTypeID"
     *             length="10"
     *         
     */
    public Integer getLatLongTypeId() {
        return this.latLongTypeId;
    }
    
    public void setLatLongTypeId(Integer latLongTypeId) {
        this.latLongTypeId = latLongTypeId;
    }

    /**
     *      *            @hibernate.property
     *             column="LatLongMethodID"
     *             length="10"
     *         
     */
    public Integer getLatLongMethodId() {
        return this.latLongMethodId;
    }
    
    public void setLatLongMethodId(Integer latLongMethodId) {
        this.latLongMethodId = latLongMethodId;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo1"
     *             length="5"
     *         
     */
    public Short getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Short yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo2"
     *             length="5"
     *         
     */
    public Short getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Short yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      *            @hibernate.property
     *             column="Lat1Text"
     *             length="50"
     *         
     */
    public String getLat1text() {
        return this.lat1text;
    }
    
    public void setLat1text(String lat1text) {
        this.lat1text = lat1text;
    }

    /**
     *      *            @hibernate.property
     *             column="Lat2Text"
     *             length="50"
     *         
     */
    public String getLat2text() {
        return this.lat2text;
    }
    
    public void setLat2text(String lat2text) {
        this.lat2text = lat2text;
    }

    /**
     *      *            @hibernate.property
     *             column="Long1Text"
     *             length="50"
     *         
     */
    public String getLong1text() {
        return this.long1text;
    }
    
    public void setLong1text(String long1text) {
        this.long1text = long1text;
    }

    /**
     *      *            @hibernate.property
     *             column="Long2Text"
     *             length="50"
     *         
     */
    public String getLong2text() {
        return this.long2text;
    }
    
    public void setLong2text(String long2text) {
        this.long2text = long2text;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="GeographyID"         
     *         
     */
    public Geography getGeographyByGeographyId1() {
        return this.geographyByGeographyId1;
    }
    
    public void setGeographyByGeographyId1(Geography geographyByGeographyId1) {
        this.geographyByGeographyId1 = geographyByGeographyId1;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="LocalityID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.LocalityCitation"
     *         
     */
    public Set getLocalityCitations() {
        return this.localityCitations;
    }
    
    public void setLocalityCitations(Set localityCitations) {
        this.localityCitations = localityCitations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="LocalityID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectingEvent"
     *         
     */
    public Set getCollectingEvents() {
        return this.collectingEvents;
    }
    
    public void setCollectingEvents(Set collectingEvents) {
        this.collectingEvents = collectingEvents;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="LocalityID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ImageLocality"
     *         
     */
    public Set getImageLocalities() {
        return this.imageLocalities;
    }
    
    public void setImageLocalities(Set imageLocalities) {
        this.imageLocalities = imageLocalities;
    }




}
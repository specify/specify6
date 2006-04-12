package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

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
     protected Double minElevation;
     protected Double maxElevation;
     protected String elevationMethod;
     protected Double elevationAccuracy;
     protected Integer originalLatLongUnit;
     protected String latLongType;
     protected Double latitude1;
     protected Double longitude1;
     protected Double latitude2;
     protected Double longitude2;
     protected String latLongMethod;
     protected Double latLongAccuracy;
     protected String datum;
     protected Integer groupPermittedToView;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Double number1;
     protected Double number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
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
     protected Set<CollectionObjDef> collectionObjDefs;
     protected Geography geography;
     protected Set<LocalityCitation> localityCitations;
     protected Set<CollectingEvent> collectingEvents;
     protected Set<ExternalResource> externalResources;


    // Constructors

    /** default constructor */
    public Locality() {
    }
    
    /** constructor with id */
    public Locality(Integer localityId) {
        this.localityId = localityId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
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
        timestampCreated = Calendar.getInstance().getTime();
        timestampModified = null;
        lastEditedBy = null;
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
        collectionObjDefs = new HashSet<CollectionObjDef>();
        geography = null;
        localityCitations = new HashSet<LocalityCitation>();
        collectingEvents = new HashSet<CollectingEvent>();
        externalResources = new HashSet<ExternalResource>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getLocalityId() {
        return this.localityId;
    }
    
    public void setLocalityId(Integer localityId) {
        this.localityId = localityId;
    }

    /**
     *      * The named place the locality is closest to
     */
    public String getNamedPlace() {
        return this.namedPlace;
    }
    
    public void setNamedPlace(String namedPlace) {
        this.namedPlace = namedPlace;
    }

    /**
     *      * Relation of the locality to the named place
     */
    public String getRelationToNamedPlace() {
        return this.relationToNamedPlace;
    }
    
    public void setRelationToNamedPlace(String relationToNamedPlace) {
        this.relationToNamedPlace = relationToNamedPlace;
    }

    /**
     *      * The full name of the locality.
     */
    public String getLocalityName() {
        return this.localityName;
    }
    
    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    /**
     *      * BaseMeridian for the Range/Township/Section data
     */
    public String getBaseMeridian() {
        return this.baseMeridian;
    }
    
    public void setBaseMeridian(String baseMeridian) {
        this.baseMeridian = baseMeridian;
    }

    /**
     *      * The Range of a legal description
     */
    public String getRange() {
        return this.range;
    }
    
    public void setRange(String range) {
        this.range = range;
    }

    /**
     * 
     */
    public String getRangeDirection() {
        return this.rangeDirection;
    }
    
    public void setRangeDirection(String rangeDirection) {
        this.rangeDirection = rangeDirection;
    }

    /**
     *      * The Township of a legal description
     */
    public String getTownship() {
        return this.township;
    }
    
    public void setTownship(String township) {
        this.township = township;
    }

    /**
     * 
     */
    public String getTownshipDirection() {
        return this.townshipDirection;
    }
    
    public void setTownshipDirection(String townshipDirection) {
        this.townshipDirection = townshipDirection;
    }

    /**
     *      * The Section of a legal description
     */
    public String getSection() {
        return this.section;
    }
    
    public void setSection(String section) {
        this.section = section;
    }

    /**
     * 
     */
    public String getSectionPart() {
        return this.sectionPart;
    }
    
    public void setSectionPart(String sectionPart) {
        this.sectionPart = sectionPart;
    }

    /**
     *      * The verbatim elevation including units as given in the field notes
     */
    public String getVerbatimElevation() {
        return this.verbatimElevation;
    }
    
    public void setVerbatimElevation(String verbatimElevation) {
        this.verbatimElevation = verbatimElevation;
    }

    /**
     *      * i.e. Meters, Feet, ...
     */
    public String getOriginalElevationUnit() {
        return this.originalElevationUnit;
    }
    
    public void setOriginalElevationUnit(String originalElevationUnit) {
        this.originalElevationUnit = originalElevationUnit;
    }

    /**
     *      * The minimum elevation in Meters
     */
    public Double getMinElevation() {
        return this.minElevation;
    }
    
    public void setMinElevation(Double minElevation) {
        this.minElevation = minElevation;
    }

    /**
     *      * The maximum elevation in Meters
     */
    public Double getMaxElevation() {
        return this.maxElevation;
    }
    
    public void setMaxElevation(Double maxElevation) {
        this.maxElevation = maxElevation;
    }

    /**
     *      * The method used to determine the elevation
     */
    public String getElevationMethod() {
        return this.elevationMethod;
    }
    
    public void setElevationMethod(String elevationMethod) {
        this.elevationMethod = elevationMethod;
    }

    /**
     *      * plus or minus -- in meters
     */
    public Double getElevationAccuracy() {
        return this.elevationAccuracy;
    }
    
    public void setElevationAccuracy(Double elevationAccuracy) {
        this.elevationAccuracy = elevationAccuracy;
    }

    /**
     *      * i.e. Decimal, Deg/Min/Sec, ...
     */
    public Integer getOriginalLatLongUnit() {
        return this.originalLatLongUnit;
    }
    
    public void setOriginalLatLongUnit(Integer originalLatLongUnit) {
        this.originalLatLongUnit = originalLatLongUnit;
    }

    /**
     *      * The type of area described by the lat long data (Point,Line,Rectangle)
     */
    public String getLatLongType() {
        return this.latLongType;
    }
    
    public void setLatLongType(String latLongType) {
        this.latLongType = latLongType;
    }

    /**
     *      * Latitude of first point
     */
    public Double getLatitude1() {
        return this.latitude1;
    }
    
    public void setLatitude1(Double latitude1) {
        this.latitude1 = latitude1;
    }

    /**
     *      * Longitude of first point
     */
    public Double getLongitude1() {
        return this.longitude1;
    }
    
    public void setLongitude1(Double longitude1) {
        this.longitude1 = longitude1;
    }

    /**
     *      * Latitude of second point
     */
    public Double getLatitude2() {
        return this.latitude2;
    }
    
    public void setLatitude2(Double latitude2) {
        this.latitude2 = latitude2;
    }

    /**
     *      * Longitude of second point
     */
    public Double getLongitude2() {
        return this.longitude2;
    }
    
    public void setLongitude2(Double longitude2) {
        this.longitude2 = longitude2;
    }

    /**
     *      * the method used to determine the LatitudeLongitude
     */
    public String getLatLongMethod() {
        return this.latLongMethod;
    }
    
    public void setLatLongMethod(String latLongMethod) {
        this.latLongMethod = latLongMethod;
    }

    /**
     *      * radius -- in decimal degrees
     */
    public Double getLatLongAccuracy() {
        return this.latLongAccuracy;
    }
    
    public void setLatLongAccuracy(Double latLongAccuracy) {
        this.latLongAccuracy = latLongAccuracy;
    }

    /**
     *      * GPSDatum
     */
    public String getDatum() {
        return this.datum;
    }
    
    public void setDatum(String datum) {
        this.datum = datum;
    }

    /**
     *      * The name of the group that this record is visible to. (Default to public)
     */
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     * 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    public Double getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Double number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    public Double getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Double number2) {
        this.number2 = number2;
    }

    /**
     *      * Date the record was created
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      * Date the record was modified
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      * Name of the person last editing the record
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * 
     */
    public String getLat1text() {
        return this.lat1text;
    }
    
    public void setLat1text(String lat1text) {
        this.lat1text = lat1text;
    }

    /**
     * 
     */
    public String getLat2text() {
        return this.lat2text;
    }
    
    public void setLat2text(String lat2text) {
        this.lat2text = lat2text;
    }

    /**
     * 
     */
    public String getLong1text() {
        return this.long1text;
    }
    
    public void setLong1text(String long1text) {
        this.long1text = long1text;
    }

    /**
     * 
     */
    public String getLong2text() {
        return this.long2text;
    }
    
    public void setLong2text(String long2text) {
        this.long2text = long2text;
    }

    /**
     * 
     */
    public String getNationalParkName() {
        return this.nationalParkName;
    }
    
    public void setNationalParkName(String nationalParkName) {
        this.nationalParkName = nationalParkName;
    }

    /**
     * 
     */
    public String getIslandGroup() {
        return this.islandGroup;
    }
    
    public void setIslandGroup(String islandGroup) {
        this.islandGroup = islandGroup;
    }

    /**
     * 
     */
    public String getIsland() {
        return this.island;
    }
    
    public void setIsland(String island) {
        this.island = island;
    }

    /**
     * 
     */
    public String getWaterBody() {
        return this.waterBody;
    }
    
    public void setWaterBody(String waterBody) {
        this.waterBody = waterBody;
    }

    /**
     * 
     */
    public String getDrainage() {
        return this.drainage;
    }
    
    public void setDrainage(String drainage) {
        this.drainage = drainage;
    }

    /**
     * 
     */
    public Set<CollectionObjDef> getCollectionObjDefs() {
        return this.collectionObjDefs;
    }
    
    public void setCollectionObjDefs(Set<CollectionObjDef> collectionObjDefs) {
        this.collectionObjDefs = collectionObjDefs;
    }

    /**
     *      * Link to Country, State, County, WaterBody, Island, IslandGroup ... info
     */
    public Geography getGeography() {
        return this.geography;
    }
    
    public void setGeography(Geography geography) {
        this.geography = geography;
    }

    /**
     * 
     */
    public Set<LocalityCitation> getLocalityCitations() {
        return this.localityCitations;
    }
    
    public void setLocalityCitations(Set<LocalityCitation> localityCitations) {
        this.localityCitations = localityCitations;
    }

    /**
     * 
     */
    public Set<CollectingEvent> getCollectingEvents() {
        return this.collectingEvents;
    }
    
    public void setCollectingEvents(Set<CollectingEvent> collectingEvents) {
        this.collectingEvents = collectingEvents;
    }

    /**
     * 
     */
    public Set<ExternalResource> getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set<ExternalResource> externalResources) {
        this.externalResources = externalResources;
    }




    // Add Methods

    public void addCollectionObjDef(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDefs.add(collectionObjDef);
    }

    public void addLocalityCitation(final LocalityCitation localityCitation)
    {
        this.localityCitations.add(localityCitation);
    }

    public void addCollectingEvent(final CollectingEvent collectingEvent)
    {
        this.collectingEvents.add(collectingEvent);
    }

    public void addExternalResource(final ExternalResource externalResource)
    {
        this.externalResources.add(externalResource);
    }

    // Done Add Methods
}

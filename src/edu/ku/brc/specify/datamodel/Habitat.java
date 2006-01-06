package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="habitat"
 *     
 */
public class Habitat  implements java.io.Serializable {

    // Fields    

     protected Integer habitatId;
     protected Float airTempC;
     protected Float waterTempC;
     protected Float waterpH;
     protected String turbidity;
     protected String clarity;
     protected String salinity;
     protected String soilType;
     protected Float soilPh;
     protected Float soilTempC;
     protected String soilMoisture;
     protected String slope;
     protected String vegetation;
     protected String habitatType;
     protected String current1;
     protected String substrate;
     protected String substrateMoisture;
     protected Float heightAboveGround;
     protected String nearestNeighbor;
     protected String remarks;
     protected Float minDepth;
     protected Float maxDepth;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Integer habitatTypeId;
     protected Short yesNo1;
     protected Short yesNo2;
     protected Float number3;
     protected Float number4;
     protected Float number5;
     protected String text3;
     protected String text4;
     protected String text5;
     protected Short yesNo3;
     protected Short yesNo4;
     protected Short yesNo5;
     protected CollectingEvent collectingEvent;
     protected CollectionObjectType collectionObjectType;
     protected TaxonName taxonName;


    // Constructors

    /** default constructor */
    public Habitat() {
    }
    
    /** constructor with id */
    public Habitat(Integer habitatId) {
        this.habitatId = habitatId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="HabitatID"
     *         
     */
    public Integer getHabitatId() {
        return this.habitatId;
    }
    
    public void setHabitatId(Integer habitatId) {
        this.habitatId = habitatId;
    }

    /**
     *      *            @hibernate.property
     *             column="AirTempC"
     *             length="24"
     *         
     */
    public Float getAirTempC() {
        return this.airTempC;
    }
    
    public void setAirTempC(Float airTempC) {
        this.airTempC = airTempC;
    }

    /**
     *      *            @hibernate.property
     *             column="WaterTempC"
     *             length="24"
     *         
     */
    public Float getWaterTempC() {
        return this.waterTempC;
    }
    
    public void setWaterTempC(Float waterTempC) {
        this.waterTempC = waterTempC;
    }

    /**
     *      *            @hibernate.property
     *             column="WaterpH"
     *             length="24"
     *         
     */
    public Float getWaterpH() {
        return this.waterpH;
    }
    
    public void setWaterpH(Float waterpH) {
        this.waterpH = waterpH;
    }

    /**
     *      *            @hibernate.property
     *             column="Turbidity"
     *             length="50"
     *         
     */
    public String getTurbidity() {
        return this.turbidity;
    }
    
    public void setTurbidity(String turbidity) {
        this.turbidity = turbidity;
    }

    /**
     *      *            @hibernate.property
     *             column="Clarity"
     *             length="50"
     *         
     */
    public String getClarity() {
        return this.clarity;
    }
    
    public void setClarity(String clarity) {
        this.clarity = clarity;
    }

    /**
     *      *            @hibernate.property
     *             column="Salinity"
     *             length="50"
     *         
     */
    public String getSalinity() {
        return this.salinity;
    }
    
    public void setSalinity(String salinity) {
        this.salinity = salinity;
    }

    /**
     *      *            @hibernate.property
     *             column="SoilType"
     *             length="50"
     *         
     */
    public String getSoilType() {
        return this.soilType;
    }
    
    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    /**
     *      *            @hibernate.property
     *             column="SoilPh"
     *             length="24"
     *         
     */
    public Float getSoilPh() {
        return this.soilPh;
    }
    
    public void setSoilPh(Float soilPh) {
        this.soilPh = soilPh;
    }

    /**
     *      *            @hibernate.property
     *             column="SoilTempC"
     *             length="24"
     *         
     */
    public Float getSoilTempC() {
        return this.soilTempC;
    }
    
    public void setSoilTempC(Float soilTempC) {
        this.soilTempC = soilTempC;
    }

    /**
     *      *            @hibernate.property
     *             column="SoilMoisture"
     *             length="50"
     *         
     */
    public String getSoilMoisture() {
        return this.soilMoisture;
    }
    
    public void setSoilMoisture(String soilMoisture) {
        this.soilMoisture = soilMoisture;
    }

    /**
     *      *            @hibernate.property
     *             column="Slope"
     *             length="50"
     *         
     */
    public String getSlope() {
        return this.slope;
    }
    
    public void setSlope(String slope) {
        this.slope = slope;
    }

    /**
     *      *            @hibernate.property
     *             column="Vegetation"
     *             length="50"
     *         
     */
    public String getVegetation() {
        return this.vegetation;
    }
    
    public void setVegetation(String vegetation) {
        this.vegetation = vegetation;
    }

    /**
     *      *            @hibernate.property
     *             column="HabitatType"
     *             length="50"
     *         
     */
    public String getHabitatType() {
        return this.habitatType;
    }
    
    public void setHabitatType(String habitatType) {
        this.habitatType = habitatType;
    }

    /**
     *      *            @hibernate.property
     *             column="Current1"
     *             length="50"
     *         
     */
    public String getCurrent1() {
        return this.current1;
    }
    
    public void setCurrent1(String current1) {
        this.current1 = current1;
    }

    /**
     *      *            @hibernate.property
     *             column="Substrate"
     *             length="50"
     *         
     */
    public String getSubstrate() {
        return this.substrate;
    }
    
    public void setSubstrate(String substrate) {
        this.substrate = substrate;
    }

    /**
     *      *            @hibernate.property
     *             column="SubstrateMoisture"
     *             length="50"
     *         
     */
    public String getSubstrateMoisture() {
        return this.substrateMoisture;
    }
    
    public void setSubstrateMoisture(String substrateMoisture) {
        this.substrateMoisture = substrateMoisture;
    }

    /**
     *      *            @hibernate.property
     *             column="HeightAboveGround"
     *             length="24"
     *         
     */
    public Float getHeightAboveGround() {
        return this.heightAboveGround;
    }
    
    public void setHeightAboveGround(Float heightAboveGround) {
        this.heightAboveGround = heightAboveGround;
    }

    /**
     *      *            @hibernate.property
     *             column="NearestNeighbor"
     *             length="50"
     *         
     */
    public String getNearestNeighbor() {
        return this.nearestNeighbor;
    }
    
    public void setNearestNeighbor(String nearestNeighbor) {
        this.nearestNeighbor = nearestNeighbor;
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
     *             column="MinDepth"
     *             length="24"
     *         
     */
    public Float getMinDepth() {
        return this.minDepth;
    }
    
    public void setMinDepth(Float minDepth) {
        this.minDepth = minDepth;
    }

    /**
     *      *            @hibernate.property
     *             column="MaxDepth"
     *             length="24"
     *         
     */
    public Float getMaxDepth() {
        return this.maxDepth;
    }
    
    public void setMaxDepth(Float maxDepth) {
        this.maxDepth = maxDepth;
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
     *             column="HabitatTypeID"
     *             length="10"
     *         
     */
    public Integer getHabitatTypeId() {
        return this.habitatTypeId;
    }
    
    public void setHabitatTypeId(Integer habitatTypeId) {
        this.habitatTypeId = habitatTypeId;
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
     *             column="Number3"
     *             length="24"
     *         
     */
    public Float getNumber3() {
        return this.number3;
    }
    
    public void setNumber3(Float number3) {
        this.number3 = number3;
    }

    /**
     *      *            @hibernate.property
     *             column="Number4"
     *             length="24"
     *         
     */
    public Float getNumber4() {
        return this.number4;
    }
    
    public void setNumber4(Float number4) {
        this.number4 = number4;
    }

    /**
     *      *            @hibernate.property
     *             column="Number5"
     *             length="24"
     *         
     */
    public Float getNumber5() {
        return this.number5;
    }
    
    public void setNumber5(Float number5) {
        this.number5 = number5;
    }

    /**
     *      *            @hibernate.property
     *             column="Text3"
     *             length="300"
     *         
     */
    public String getText3() {
        return this.text3;
    }
    
    public void setText3(String text3) {
        this.text3 = text3;
    }

    /**
     *      *            @hibernate.property
     *             column="Text4"
     *             length="100"
     *         
     */
    public String getText4() {
        return this.text4;
    }
    
    public void setText4(String text4) {
        this.text4 = text4;
    }

    /**
     *      *            @hibernate.property
     *             column="Text5"
     *             length="100"
     *         
     */
    public String getText5() {
        return this.text5;
    }
    
    public void setText5(String text5) {
        this.text5 = text5;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo3"
     *             length="5"
     *         
     */
    public Short getYesNo3() {
        return this.yesNo3;
    }
    
    public void setYesNo3(Short yesNo3) {
        this.yesNo3 = yesNo3;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo4"
     *             length="5"
     *         
     */
    public Short getYesNo4() {
        return this.yesNo4;
    }
    
    public void setYesNo4(Short yesNo4) {
        this.yesNo4 = yesNo4;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo5"
     *             length="5"
     *         
     */
    public Short getYesNo5() {
        return this.yesNo5;
    }
    
    public void setYesNo5(Short yesNo5) {
        this.yesNo5 = yesNo5;
    }

    /**
     *      *            @hibernate.one-to-one
     *             class="edu.ku.brc.specify.datamodel.CollectingEvent"
     *             outer-join="auto"
     *             constrained="true"
     * 			cascade="delete"
     *         
     */
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }
    
    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="BiologicalObjectTypeCollectedID"         
     *         
     */
    public CollectionObjectType getCollectionObjectType() {
        return this.collectionObjectType;
    }
    
    public void setCollectionObjectType(CollectionObjectType collectionObjectType) {
        this.collectionObjectType = collectionObjectType;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="HostTaxonID"         
     *         
     */
    public TaxonName getTaxonName() {
        return this.taxonName;
    }
    
    public void setTaxonName(TaxonName taxonName) {
        this.taxonName = taxonName;
    }




}
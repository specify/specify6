package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="biologicalobjectattributes"
 *     
 */
public class BiologicalObjectAttribute  implements java.io.Serializable {

    // Fields    

     protected Integer biologicalObjectAttributesId;
     protected Integer biologicalObjectTypeId;
     protected String sex;
     protected String age;
     protected String stage;
     protected Float weight;
     protected Float length;
     protected Byte gosnerStage;
     protected Float snoutVentLength;
     protected String activity;
     protected Float lengthTail;
     protected String reproductiveCondition;
     protected String condition;
     protected Float lengthTarsus;
     protected Float lengthWing;
     protected Float lengthHead;
     protected Float lengthBody;
     protected Float lengthMiddleToe;
     protected Float lengthBill;
     protected Float totalExposedCulmen;
     protected Float maxLength;
     protected Float minLength;
     protected Float lengthHindFoot;
     protected Float lengthForeArm;
     protected Float lengthTragus;
     protected Float lengthEar;
     protected Float earFromNotch;
     protected Float wingspan;
     protected Float lengthGonad;
     protected Float widthGonad;
     protected Float lengthHeadBody;
     protected Float width;
     protected Float heightFinalWhorl;
     protected Float insideHeightAperture;
     protected Float insideWidthAperture;
     protected Short numberWhorls;
     protected Float outerLipThickness;
     protected Float mantle;
     protected Float height;
     protected Float diameter;
     protected String branchingAt;
     protected String text1;
     protected String text2;
     protected String text3;
     protected String text4;
     protected String text5;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Integer sexId;
     protected Integer stageId;
     protected Short yesNo1;
     protected Short yesNo2;
     protected Short yesNo3;
     protected Float number1;
     protected Float number2;
     protected Float number3;
     protected Float number4;
     protected Float number5;
     protected Float number6;
     protected Float number7;
     protected String text6;
     protected String text7;
     protected Short yesNo4;
     protected Short yesNo5;
     protected Short yesNo6;
     protected Short yesNo7;


    // Constructors

    /** default constructor */
    public BiologicalObjectAttribute() {
    }
    
    /** constructor with id */
    public BiologicalObjectAttribute(Integer biologicalObjectAttributesId) {
        this.biologicalObjectAttributesId = biologicalObjectAttributesId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="BiologicalObjectAttributesID"
     *         
     */
    public Integer getBiologicalObjectAttributesId() {
        return this.biologicalObjectAttributesId;
    }
    
    public void setBiologicalObjectAttributesId(Integer biologicalObjectAttributesId) {
        this.biologicalObjectAttributesId = biologicalObjectAttributesId;
    }

    /**
     *      *            @hibernate.property
     *             column="BiologicalObjectTypeID"
     *             length="10"
     *             not-null="true"
     *         
     */
    public Integer getBiologicalObjectTypeId() {
        return this.biologicalObjectTypeId;
    }
    
    public void setBiologicalObjectTypeId(Integer biologicalObjectTypeId) {
        this.biologicalObjectTypeId = biologicalObjectTypeId;
    }

    /**
     *      *            @hibernate.property
     *             column="Sex"
     *             length="50"
     *         
     */
    public String getSex() {
        return this.sex;
    }
    
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     *      *            @hibernate.property
     *             column="Age"
     *             length="50"
     *         
     */
    public String getAge() {
        return this.age;
    }
    
    public void setAge(String age) {
        this.age = age;
    }

    /**
     *      *            @hibernate.property
     *             column="Stage"
     *             length="50"
     *         
     */
    public String getStage() {
        return this.stage;
    }
    
    public void setStage(String stage) {
        this.stage = stage;
    }

    /**
     *      *            @hibernate.property
     *             column="Weight"
     *             length="24"
     *         
     */
    public Float getWeight() {
        return this.weight;
    }
    
    public void setWeight(Float weight) {
        this.weight = weight;
    }

    /**
     *      *            @hibernate.property
     *             column="Length"
     *             length="24"
     *         
     */
    public Float getLength() {
        return this.length;
    }
    
    public void setLength(Float length) {
        this.length = length;
    }

    /**
     *      *            @hibernate.property
     *             column="GosnerStage"
     *             length="3"
     *         
     */
    public Byte getGosnerStage() {
        return this.gosnerStage;
    }
    
    public void setGosnerStage(Byte gosnerStage) {
        this.gosnerStage = gosnerStage;
    }

    /**
     *      *            @hibernate.property
     *             column="SnoutVentLength"
     *             length="24"
     *         
     */
    public Float getSnoutVentLength() {
        return this.snoutVentLength;
    }
    
    public void setSnoutVentLength(Float snoutVentLength) {
        this.snoutVentLength = snoutVentLength;
    }

    /**
     *      *            @hibernate.property
     *             column="Activity"
     *             length="50"
     *         
     */
    public String getActivity() {
        return this.activity;
    }
    
    public void setActivity(String activity) {
        this.activity = activity;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthTail"
     *             length="24"
     *         
     */
    public Float getLengthTail() {
        return this.lengthTail;
    }
    
    public void setLengthTail(Float lengthTail) {
        this.lengthTail = lengthTail;
    }

    /**
     *      *            @hibernate.property
     *             column="ReproductiveCondition"
     *             length="50"
     *         
     */
    public String getReproductiveCondition() {
        return this.reproductiveCondition;
    }
    
    public void setReproductiveCondition(String reproductiveCondition) {
        this.reproductiveCondition = reproductiveCondition;
    }

    /**
     *      *            @hibernate.property
     *             column="Condition"
     *             length="50"
     *         
     */
    public String getCondition() {
        return this.condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthTarsus"
     *             length="24"
     *         
     */
    public Float getLengthTarsus() {
        return this.lengthTarsus;
    }
    
    public void setLengthTarsus(Float lengthTarsus) {
        this.lengthTarsus = lengthTarsus;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthWing"
     *             length="24"
     *         
     */
    public Float getLengthWing() {
        return this.lengthWing;
    }
    
    public void setLengthWing(Float lengthWing) {
        this.lengthWing = lengthWing;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthHead"
     *             length="24"
     *         
     */
    public Float getLengthHead() {
        return this.lengthHead;
    }
    
    public void setLengthHead(Float lengthHead) {
        this.lengthHead = lengthHead;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthBody"
     *             length="24"
     *         
     */
    public Float getLengthBody() {
        return this.lengthBody;
    }
    
    public void setLengthBody(Float lengthBody) {
        this.lengthBody = lengthBody;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthMiddleToe"
     *             length="24"
     *         
     */
    public Float getLengthMiddleToe() {
        return this.lengthMiddleToe;
    }
    
    public void setLengthMiddleToe(Float lengthMiddleToe) {
        this.lengthMiddleToe = lengthMiddleToe;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthBill"
     *             length="24"
     *         
     */
    public Float getLengthBill() {
        return this.lengthBill;
    }
    
    public void setLengthBill(Float lengthBill) {
        this.lengthBill = lengthBill;
    }

    /**
     *      *            @hibernate.property
     *             column="TotalExposedCulmen"
     *             length="24"
     *         
     */
    public Float getTotalExposedCulmen() {
        return this.totalExposedCulmen;
    }
    
    public void setTotalExposedCulmen(Float totalExposedCulmen) {
        this.totalExposedCulmen = totalExposedCulmen;
    }

    /**
     *      *            @hibernate.property
     *             column="MaxLength"
     *             length="24"
     *         
     */
    public Float getMaxLength() {
        return this.maxLength;
    }
    
    public void setMaxLength(Float maxLength) {
        this.maxLength = maxLength;
    }

    /**
     *      *            @hibernate.property
     *             column="MinLength"
     *             length="24"
     *         
     */
    public Float getMinLength() {
        return this.minLength;
    }
    
    public void setMinLength(Float minLength) {
        this.minLength = minLength;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthHindFoot"
     *             length="24"
     *         
     */
    public Float getLengthHindFoot() {
        return this.lengthHindFoot;
    }
    
    public void setLengthHindFoot(Float lengthHindFoot) {
        this.lengthHindFoot = lengthHindFoot;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthForeArm"
     *             length="24"
     *         
     */
    public Float getLengthForeArm() {
        return this.lengthForeArm;
    }
    
    public void setLengthForeArm(Float lengthForeArm) {
        this.lengthForeArm = lengthForeArm;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthTragus"
     *             length="24"
     *         
     */
    public Float getLengthTragus() {
        return this.lengthTragus;
    }
    
    public void setLengthTragus(Float lengthTragus) {
        this.lengthTragus = lengthTragus;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthEar"
     *             length="24"
     *         
     */
    public Float getLengthEar() {
        return this.lengthEar;
    }
    
    public void setLengthEar(Float lengthEar) {
        this.lengthEar = lengthEar;
    }

    /**
     *      *            @hibernate.property
     *             column="EarFromNotch"
     *             length="24"
     *         
     */
    public Float getEarFromNotch() {
        return this.earFromNotch;
    }
    
    public void setEarFromNotch(Float earFromNotch) {
        this.earFromNotch = earFromNotch;
    }

    /**
     *      *            @hibernate.property
     *             column="Wingspan"
     *             length="24"
     *         
     */
    public Float getWingspan() {
        return this.wingspan;
    }
    
    public void setWingspan(Float wingspan) {
        this.wingspan = wingspan;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthGonad"
     *             length="24"
     *         
     */
    public Float getLengthGonad() {
        return this.lengthGonad;
    }
    
    public void setLengthGonad(Float lengthGonad) {
        this.lengthGonad = lengthGonad;
    }

    /**
     *      *            @hibernate.property
     *             column="WidthGonad"
     *             length="24"
     *         
     */
    public Float getWidthGonad() {
        return this.widthGonad;
    }
    
    public void setWidthGonad(Float widthGonad) {
        this.widthGonad = widthGonad;
    }

    /**
     *      *            @hibernate.property
     *             column="LengthHeadBody"
     *             length="24"
     *         
     */
    public Float getLengthHeadBody() {
        return this.lengthHeadBody;
    }
    
    public void setLengthHeadBody(Float lengthHeadBody) {
        this.lengthHeadBody = lengthHeadBody;
    }

    /**
     *      *            @hibernate.property
     *             column="Width"
     *             length="24"
     *         
     */
    public Float getWidth() {
        return this.width;
    }
    
    public void setWidth(Float width) {
        this.width = width;
    }

    /**
     *      *            @hibernate.property
     *             column="HeightFinalWhorl"
     *             length="24"
     *         
     */
    public Float getHeightFinalWhorl() {
        return this.heightFinalWhorl;
    }
    
    public void setHeightFinalWhorl(Float heightFinalWhorl) {
        this.heightFinalWhorl = heightFinalWhorl;
    }

    /**
     *      *            @hibernate.property
     *             column="InsideHeightAperture"
     *             length="24"
     *         
     */
    public Float getInsideHeightAperture() {
        return this.insideHeightAperture;
    }
    
    public void setInsideHeightAperture(Float insideHeightAperture) {
        this.insideHeightAperture = insideHeightAperture;
    }

    /**
     *      *            @hibernate.property
     *             column="InsideWidthAperture"
     *             length="24"
     *         
     */
    public Float getInsideWidthAperture() {
        return this.insideWidthAperture;
    }
    
    public void setInsideWidthAperture(Float insideWidthAperture) {
        this.insideWidthAperture = insideWidthAperture;
    }

    /**
     *      *            @hibernate.property
     *             column="NumberWhorls"
     *             length="5"
     *         
     */
    public Short getNumberWhorls() {
        return this.numberWhorls;
    }
    
    public void setNumberWhorls(Short numberWhorls) {
        this.numberWhorls = numberWhorls;
    }

    /**
     *      *            @hibernate.property
     *             column="OuterLipThickness"
     *             length="24"
     *         
     */
    public Float getOuterLipThickness() {
        return this.outerLipThickness;
    }
    
    public void setOuterLipThickness(Float outerLipThickness) {
        this.outerLipThickness = outerLipThickness;
    }

    /**
     *      *            @hibernate.property
     *             column="Mantle"
     *             length="24"
     *         
     */
    public Float getMantle() {
        return this.mantle;
    }
    
    public void setMantle(Float mantle) {
        this.mantle = mantle;
    }

    /**
     *      *            @hibernate.property
     *             column="Height"
     *             length="24"
     *         
     */
    public Float getHeight() {
        return this.height;
    }
    
    public void setHeight(Float height) {
        this.height = height;
    }

    /**
     *      *            @hibernate.property
     *             column="Diameter"
     *             length="24"
     *         
     */
    public Float getDiameter() {
        return this.diameter;
    }
    
    public void setDiameter(Float diameter) {
        this.diameter = diameter;
    }

    /**
     *      *            @hibernate.property
     *             column="BranchingAt"
     *             length="50"
     *         
     */
    public String getBranchingAt() {
        return this.branchingAt;
    }
    
    public void setBranchingAt(String branchingAt) {
        this.branchingAt = branchingAt;
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
     *             length="50"
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
     *             length="50"
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
     *             column="SexID"
     *             length="10"
     *         
     */
    public Integer getSexId() {
        return this.sexId;
    }
    
    public void setSexId(Integer sexId) {
        this.sexId = sexId;
    }

    /**
     *      *            @hibernate.property
     *             column="StageID"
     *             length="10"
     *         
     */
    public Integer getStageId() {
        return this.stageId;
    }
    
    public void setStageId(Integer stageId) {
        this.stageId = stageId;
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
     *             column="Number6"
     *             length="24"
     *         
     */
    public Float getNumber6() {
        return this.number6;
    }
    
    public void setNumber6(Float number6) {
        this.number6 = number6;
    }

    /**
     *      *            @hibernate.property
     *             column="Number7"
     *             length="24"
     *         
     */
    public Float getNumber7() {
        return this.number7;
    }
    
    public void setNumber7(Float number7) {
        this.number7 = number7;
    }

    /**
     *      *            @hibernate.property
     *             column="Text6"
     *             length="100"
     *         
     */
    public String getText6() {
        return this.text6;
    }
    
    public void setText6(String text6) {
        this.text6 = text6;
    }

    /**
     *      *            @hibernate.property
     *             column="Text7"
     *             length="100"
     *         
     */
    public String getText7() {
        return this.text7;
    }
    
    public void setText7(String text7) {
        this.text7 = text7;
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
     *      *            @hibernate.property
     *             column="YesNo6"
     *             length="5"
     *         
     */
    public Short getYesNo6() {
        return this.yesNo6;
    }
    
    public void setYesNo6(Short yesNo6) {
        this.yesNo6 = yesNo6;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo7"
     *             length="5"
     *         
     */
    public Short getYesNo7() {
        return this.yesNo7;
    }
    
    public void setYesNo7(Short yesNo7) {
        this.yesNo7 = yesNo7;
    }




}
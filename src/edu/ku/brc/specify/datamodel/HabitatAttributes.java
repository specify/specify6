/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * May 8, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "habitatattributes", uniqueConstraints = { @UniqueConstraint(columnNames = { "HabitatAttributesID" }) })
public class HabitatAttributes extends DataModelObjBase
{
    protected Long habitatAttributesId;
    protected Float airTempC;
    protected Float waterTempC;
    protected Float waterPH;
    protected String turbidity;
    protected String clarity;
    protected String salinity;
    protected String text6;
    protected Float number6;
    protected Float number7;
    protected String text7;
    protected String slope;
    protected String vegetation;
    protected String habitatType;
    protected String text8;
    protected String text9;
    protected String text10;
    protected Float number8;
    protected String text11;
    protected String remarks;
    protected Float minDepth;
    protected Float maxDepth;
    protected String text1;
    protected String text2;
    protected Float number1;
    protected Float number2;
    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Float number3;
    protected Float number4;
    protected Float number5;
    protected String text3;
    protected String text4;
    protected String text5;
    protected Boolean yesNo3;
    protected Boolean yesNo4;
    protected Boolean yesNo5;
    protected Set<CollectingEvent> collectingEvents;

    // Constructors

    /** default constructor */
    public HabitatAttributes()
    {
        // do nothing
    }

    /** constructor with id */
    public HabitatAttributes(Long habitatAttributesId) 
    {
        this.habitatAttributesId = habitatAttributesId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        habitatAttributesId = null;
        airTempC = null;
        waterTempC = null;
        waterPH = null;
        turbidity = null;
        clarity = null;
        salinity = null;
        text6 = null;
        number6 = null;
        number7 = null;
        text7 = null;
        slope = null;
        vegetation = null;
        habitatType = null;
        text8 = null;
        text9 = null;
        text10 = null;
        number8 = null;
        text11 = null;
        remarks = null;
        minDepth = null;
        maxDepth = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        number3 = null;
        number4 = null;
        number5 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        yesNo3 = null;
        yesNo4 = null;
        yesNo5 = null;
        collectingEvents = new HashSet<CollectingEvent>();
    }
    // End Initializer
    
    @Id
    @GeneratedValue
    @Column(name = "HabitatAttributesID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getHabitatAttributesId()
    {
        return habitatAttributesId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.habitatAttributesId;
    }

    public void setHabitatAttributesId(Long habitatAttributesId)
    {
        this.habitatAttributesId = habitatAttributesId;
    }
    
    @Column(name = "AirTempC", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getAirTempC()
    {
        return airTempC;
    }

    public void setAirTempC(Float airTempC)
    {
        this.airTempC = airTempC;
    }

    @Column(name = "Clarity", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getClarity()
    {
        return clarity;
    }

    public void setClarity(String clarity)
    {
        this.clarity = clarity;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "habitatAttributes")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectingEvent> getCollectingEvents() {
        return this.collectingEvents;
    }
    
    public void setCollectingEvents(Set<CollectingEvent> collectingEvents) {
        this.collectingEvents = collectingEvents;
    }

    @Column(name = "HabitatType", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getHabitatType()
    {
        return habitatType;
    }

    public void setHabitatType(String habitatType)
    {
        this.habitatType = habitatType;
    }

    @Column(name = "MaxDepth", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getMaxDepth()
    {
        return maxDepth;
    }

    public void setMaxDepth(Float maxDepth)
    {
        this.maxDepth = maxDepth;
    }

    @Column(name = "MinDepth", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getMinDepth()
    {
        return minDepth;
    }

    public void setMinDepth(Float minDepth)
    {
        this.minDepth = minDepth;
    }

    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1()
    {
        return number1;
    }

    public void setNumber1(Float number1)
    {
        this.number1 = number1;
    }

    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2()
    {
        return number2;
    }

    public void setNumber2(Float number2)
    {
        this.number2 = number2;
    }

    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber3()
    {
        return number3;
    }

    public void setNumber3(Float number3)
    {
        this.number3 = number3;
    }

    @Column(name = "Number4", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber4()
    {
        return number4;
    }

    public void setNumber4(Float number4)
    {
        this.number4 = number4;
    }

    @Column(name = "Number5", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber5()
    {
        return number5;
    }

    public void setNumber5(Float number5)
    {
        this.number5 = number5;
    }

    @Column(name = "Number6", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber6()
    {
        return number6;
    }

    public void setNumber6(Float number6)
    {
        this.number6 = number6;
    }

    @Column(name = "Number7", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber7()
    {
        return number7;
    }

    public void setNumber7(Float number7)
    {
        this.number7 = number7;
    }

    @Column(name = "Number8", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber8()
    {
        return number8;
    }

    public void setNumber8(Float number8)
    {
        this.number8 = number8;
    }

    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    @Column(name = "Salinity", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getSalinity()
    {
        return salinity;
    }

    public void setSalinity(String salinity)
    {
        this.salinity = salinity;
    }

    @Column(name = "Slope", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getSlope()
    {
        return slope;
    }

    public void setSlope(String slope)
    {
        this.slope = slope;
    }

    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
    public String getText1()
    {
        return text1;
    }

    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    @Column(name = "Text10", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText10()
    {
        return text10;
    }

    public void setText10(String text10)
    {
        this.text10 = text10;
    }

    @Column(name = "Text11", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText11()
    {
        return text11;
    }

    public void setText11(String text11)
    {
        this.text11 = text11;
    }

    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
    public String getText2()
    {
        return text2;
    }

    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    @Column(name = "Text3", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
    public String getText3()
    {
        return text3;
    }

    public void setText3(String text3)
    {
        this.text3 = text3;
    }

    @Column(name = "Text4", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
    public String getText4()
    {
        return text4;
    }

    public void setText4(String text4)
    {
        this.text4 = text4;
    }

    @Column(name = "Text5", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
    public String getText5()
    {
        return text5;
    }

    public void setText5(String text5)
    {
        this.text5 = text5;
    }

    @Column(name = "Text6", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText6()
    {
        return text6;
    }

    public void setText6(String text6)
    {
        this.text6 = text6;
    }

    @Column(name = "Text7", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText7()
    {
        return text7;
    }

    public void setText7(String text7)
    {
        this.text7 = text7;
    }

    @Column(name = "Text8", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText8()
    {
        return text8;
    }

    public void setText8(String text8)
    {
        this.text8 = text8;
    }

    @Column(name = "Text9", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText9()
    {
        return text9;
    }

    public void setText9(String text9)
    {
        this.text9 = text9;
    }

    @Column(name = "Turbidity", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getTurbidity()
    {
        return turbidity;
    }

    public void setTurbidity(String turbidity)
    {
        this.turbidity = turbidity;
    }

    @Column(name = "Vegetation", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getVegetation()
    {
        return vegetation;
    }

    public void setVegetation(String vegetation)
    {
        this.vegetation = vegetation;
    }

    @Column(name = "WaterPH", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getWaterPH()
    {
        return waterPH;
    }

    public void setWaterPH(Float waterPH)
    {
        this.waterPH = waterPH;
    }

    @Column(name = "WaterTempC", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getWaterTempC()
    {
        return waterTempC;
    }

    public void setWaterTempC(Float waterTempC)
    {
        this.waterTempC = waterTempC;
    }

    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1()
    {
        return yesNo1;
    }

    public void setYesNo1(Boolean yesNo1)
    {
        this.yesNo1 = yesNo1;
    }

    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2()
    {
        return yesNo2;
    }

    public void setYesNo2(Boolean yesNo2)
    {
        this.yesNo2 = yesNo2;
    }

    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3()
    {
        return yesNo3;
    }

    public void setYesNo3(Boolean yesNo3)
    {
        this.yesNo3 = yesNo3;
    }

    @Column(name = "YesNo4", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo4()
    {
        return yesNo4;
    }

    public void setYesNo4(Boolean yesNo4)
    {
        this.yesNo4 = yesNo4;
    }

    @Column(name = "YesNo5", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo5()
    {
        return yesNo5;
    }

    public void setYesNo5(Boolean yesNo5)
    {
        this.yesNo5 = yesNo5;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return HabitatAttributes.class;
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
        return 92;
    }
}

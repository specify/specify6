/* Copyright (C) 2017, University of Kansas Center for Research
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

import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "preparationobjectproperty")
@org.hibernate.annotations.Table(appliesTo="preparationproperty", indexes =
        {
                @Index (name="PREPPROPColMemIDX", columnNames={"CollectionMemberID"})
        })
public class PreparationProperty extends CollectionMember implements Cloneable
{
    protected static final Logger log = Logger.getLogger(PreparationProperty.class);

    protected Integer preparationPropertyId;

    protected Double measurementNumberValue;
    protected String measurementAccuracy;
    protected Double number1;
    protected Double number2;
    protected Double number3;
    protected Double number4;
    protected Double number5;
    protected Double number6;
    protected Double number7;
    protected Double number8;
    protected Double number9;
    protected Double number10;
    protected Double number11;
    protected Double number12;
    protected Double number13;
    protected Double number14;
    protected Double number15;
    protected Double number16;
    protected Double number17;
    protected Double number18;
    protected Double number19;
    protected Double number20;

    protected Integer measurementIntegerValue;
    protected Integer integer1;
    protected Integer integer2;
    protected Integer integer3;
    protected Integer integer4;
    protected Integer integer5;
    protected Integer integer6;
    protected Integer integer7;
    protected Integer integer8;
    protected Integer integer9;
    protected Integer integer10;
    protected Integer integer11;
    protected Integer integer12;
    protected Integer integer13;
    protected Integer integer14;
    protected Integer integer15;
    protected Integer integer16;
    protected Integer integer17;
    protected Integer integer18;
    protected Integer integer19;
    protected Integer integer20;

    protected String measurementTextValue;
    protected String measurementType;
    protected String measurementRemark;
    protected String measurementUnit;
    protected String measurementID;
    protected String text1;
    protected String text2;
    protected String text3;
    protected String text4;
    protected String text5;
    protected String text6;
    protected String text7;
    protected String text8;
    protected String text9;
    protected String text10;
    protected String text11;
    protected String text12;
    protected String text13;
    protected String text14;
    protected String text15;
    protected String text16;
    protected String text17;
    protected String text18;
    protected String text19;
    protected String text20;
    protected String text21;
    protected String text22;
    protected String text23;
    protected String text24;
    protected String text25;
    protected String text26;
    protected String text27;
    protected String text28;
    protected String text29;
    protected String text30;
    protected String text31;
    protected String text32;
    protected String text33;
    protected String text34;
    protected String text35;
    protected String text36;
    protected String text37;
    protected String text38;
    protected String text39;
    protected String text40;

    protected Boolean measurementYesNoValue;
    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;
    protected Boolean yesNo4;
    protected Boolean yesNo5;
    protected Boolean yesNo6;
    protected Boolean yesNo7;
    protected Boolean yesNo8;
    protected Boolean yesNo9;
    protected Boolean yesNo10;
    protected Boolean yesNo11;
    protected Boolean yesNo12;
    protected Boolean yesNo13;
    protected Boolean yesNo14;
    protected Boolean yesNo15;
    protected Boolean yesNo16;
    protected Boolean yesNo17;
    protected Boolean yesNo18;
    protected Boolean yesNo19;
    protected Boolean yesNo20;

    protected Calendar measurementDateValue;
    protected Calendar measurementDeterminedDate;
    protected Calendar date1;
    protected Calendar date2;
    protected Calendar date3;
    protected Calendar date4;
    protected Calendar date5;
    protected Calendar date6;
    protected Calendar date7;
    protected Calendar date8;
    protected Calendar date9;
    protected Calendar date10;
    protected Calendar date11;
    protected Calendar date12;
    protected Calendar date13;
    protected Calendar date14;
    protected Calendar date15;
    protected Calendar date16;
    protected Calendar date17;
    protected Calendar date18;
    protected Calendar date19;
    protected Calendar date20;

    protected Agent measurementDeterminedBy;
    protected Agent agent1;
    protected Agent agent2;
    protected Agent agent3;
    protected Agent agent4;
    protected Agent agent5;
    protected Agent agent6;
    protected Agent agent7;
    protected Agent agent8;
    protected Agent agent9;
    protected Agent agent10;
    protected Agent agent11;
    protected Agent agent12;
    protected Agent agent13;
    protected Agent agent14;
    protected Agent agent15;
    protected Agent agent16;
    protected Agent agent17;
    protected Agent agent18;
    protected Agent agent19;
    protected Agent agent20;

    protected Preparation preparation;

    protected Agent  recordedBy;
    protected Calendar recordedDate;

    protected String remarks;

    // Constructors

    /** default constructor */
    public PreparationProperty()
    {
        // do nothing
    }

    /** constructor with id */
    public PreparationProperty(Integer preparationPropertyId)
    {
        this.preparationPropertyId = preparationPropertyId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        preparationPropertyId = null;

        measurementAccuracy = null;
        measurementDateValue = null;
        measurementDeterminedBy = null;
        measurementDeterminedDate = null;
        measurementID = null;
        measurementIntegerValue = null;
        measurementNumberValue = null;
        measurementRemark = null;
        measurementTextValue =  null;
        measurementType = null;
        measurementUnit = null;
        measurementYesNoValue = null;

        number1 = null;
        number2 = null;
        number3 = null;
        number4 = null;
        number5 = null;
        number6 = null;
        number7 = null;
        number8  = null;
        number9  = null;
        number10 = null;
        number11 = null;
        number12 = null;
        number13 = null;
        number14 = null;
        number15 = null;
        number16 = null;
        number17 = null;
        number18 = null;
        number19 = null;
        number20 = null;

        integer1 = null;
        integer2 = null;
        integer3 = null;
        integer4 = null;
        integer5 = null;
        integer6 = null;
        integer7 = null;
        integer8 = null;
        integer9 = null;
        integer10 = null;
        integer11 = null;
        integer12 = null;
        integer13 = null;
        integer14 = null;
        integer15 = null;
        integer16 = null;
        integer17 = null;
        integer18 = null;
        integer19 = null;
        integer20 = null;

        text1 = null;
        text2 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        text6 = null;
        text7 = null;
        text8 = null;
        text9 = null;
        text10 = null;
        text11 = null;
        text12 = null;
        text13 = null;
        text14 = null;
        text15 = null;
        text16 = null;
        text17 = null;
        text18 = null;
        text19 = null;
        text20 = null;
        text21 = null;
        text22 = null;
        text23 = null;
        text24 = null;
        text25 = null;
        text26 = null;
        text27 = null;
        text28 = null;
        text29 = null;
        text30 = null;
        text31 = null;
        text32 = null;
        text33 = null;
        text34 = null;
        text35 = null;
        text36 = null;
        text37 = null;
        text38 = null;
        text39 = null;
        text40 = null;

        yesNo1 = null;
        yesNo2 = null;
        yesNo3 = null;
        yesNo4 = null;
        yesNo5 = null;
        yesNo6 = null;
        yesNo7 = null;
        yesNo8 = null;
        yesNo9 = null;
        yesNo10 = null;
        yesNo11 = null;
        yesNo12 = null;
        yesNo13 = null;
        yesNo14 = null;
        yesNo15 = null;
        yesNo16 = null;
        yesNo17 = null;
        yesNo18 = null;
        yesNo19 = null;
        yesNo20 = null;

        date1 = null;
        date2 = null;
        date3 = null;
        date4 = null;
        date5 = null;
        date6 = null;
        date7 = null;
        date8 = null;
        date9 = null;
        date10 = null;
        date11 = null;
        date12 = null;
        date13 = null;
        date14 = null;
        date15 = null;
        date16 = null;
        date17 = null;
        date18 = null;
        date19 = null;
        date20 = null;

        agent1 = null;
        agent2 = null;
        agent3 = null;
        agent4 = null;
        agent5 = null;
        agent6 = null;
        agent7 = null;
        agent8 = null;
        agent9 = null;
        agent10 = null;
        agent11 = null;
        agent12 = null;
        agent13 = null;
        agent14 = null;
        agent15 = null;
        agent16 = null;
        agent17 = null;
        agent18 = null;
        agent19 = null;
        agent20 = null;


        recordedBy = null;
        recordedDate = null;
        remarks = null;
        preparation = null;
    }
    // End Initializer

    @Id
    @GeneratedValue
    @Column(name = "PreparationPropertyID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getPreparationPropertyId()
    {
        return preparationPropertyId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.preparationPropertyId;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreparationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Preparation getPreparation() {
        return this.preparation;
    }

    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }

    /**
     *
     * @return
     */
    @Column(name = "MeasurementNumberValue", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getMeasurementNumberValue() {
        return measurementNumberValue;
    }

    /**
     *
     * @param measurementNumberValue
     */
    public void setMeasurementNumberValue(Double measurementNumberValue) {
        this.measurementNumberValue = measurementNumberValue;
    }

    /**
     *
     * @return
     */
    @Column(name = "MeasurementAccuracy", length = 256)
    public String getMeasurementAccuracy() {
        return measurementAccuracy;
    }

    /**
     *
     * @param measurementAccuracy
     */
    public void setMeasurementAccuracy(String measurementAccuracy) {
        this.measurementAccuracy = measurementAccuracy;
    }

    /**
     *
     * @return
     */
    @Column(name = "MeasurementIntegerValue", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getMeasurementIntegerValue() {
        return measurementIntegerValue;
    }

    /**
     *
     * @param measurementIntegerValue
     */
    public void setMeasurementIntegerValue(Integer measurementIntegerValue) {
        this.measurementIntegerValue = measurementIntegerValue;
    }

    /**
     *
     * @return
     */
    @Lob
    @Column(name = "MeasurementTextValue", length = 65535)
    public String getMeasurementTextValue() {
        return measurementTextValue;
    }

    /**
     *
     * @param measurementTextValue
     */
    public void setMeasurementTextValue(String measurementTextValue) {
        this.measurementTextValue = measurementTextValue;
    }

    /**
     *
     * @return
     */
    @Column(name = "MeasurementType", length = 256)
    public String getMeasurementType() {
        return measurementType;
    }

    /**
     *
     * @param measurementType
     */
    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    /**
     *
     * @return
     */
    @Lob
    @Column(name = "MeasurementRemark", length = 65535)
    public String getMeasurementRemark() {
        return measurementRemark;
    }

    /**
     *
     * @param measurementRemark
     */
    public void setMeasurementRemark(String measurementRemark) {
        this.measurementRemark = measurementRemark;
    }

    /**
     *
     * @return
     */
    @Column(name = "MeasurementUnit", length = 128)
    public String getMeasurementUnit() {
        return measurementUnit;
    }

    /**
     *
     * @param measurementUnit
     */
    public void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    /**
     *
     * @return
     */
    @Column(name = "MeasurementID", length = 128)
    public String getMeasurementID() {
        return measurementID;
    }

    /**
     *
     * @param measurementID
     */
    public void setMeasurementID(String measurementID) {
        this.measurementID = measurementID;
    }

    /**
     *
     * @return
     */
    @Column(name = "MeasurementYesNoValue", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getMeasurementYesNoValue() {
        return measurementYesNoValue;
    }

    /**
     *
     * @param measurementYesNoValue
     */
    public void setMeasurementYesNoValue(Boolean measurementYesNoValue) {
        this.measurementYesNoValue = measurementYesNoValue;
    }

    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "MeasurementDateValue", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getMeasurementDateValue() {
        return measurementDateValue;
    }

    /**
     *
     * @param measurementDateValue
     */
    public void setMeasurementDateValue(Calendar measurementDateValue) {
        this.measurementDateValue = measurementDateValue;
    }

    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "MeasurementDeterminedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getMeasurementDeterminedDate() {
        return measurementDeterminedDate;
    }

    /**
     *
     * @param measurementDeterminedDate
     */
    public void setMeasurementDeterminedDate(Calendar measurementDeterminedDate) {
        this.measurementDeterminedDate = measurementDeterminedDate;
    }

    /**
     *
     * @return
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MeasurementDeterminedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getMeasurementDeterminedBy() {
        return measurementDeterminedBy;
    }

    /**
     *
     * @param measurementDeterminedBy
     */
    public void setMeasurementDeterminedBy(Agent measurementDeterminedBy) {
        this.measurementDeterminedBy = measurementDeterminedBy;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "RecordedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getRecordedDate() {
        return this.recordedDate;
    }

    public void setRecordedDate(Calendar recordedDate) {
        this.recordedDate = recordedDate;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "RecordedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getRecordedBy() {
        return this.recordedBy;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent1ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent1() {
        return agent1;
    }

    public void setAgent1(Agent agent1) {
        this.agent1 = agent1;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent2ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent2() {
        return agent2;
    }

    public void setAgent2(Agent agent2) {
        this.agent2 = agent2;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent3ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent3() {
        return agent3;
    }

    public void setAgent3(Agent agent3) {
        this.agent3 = agent3;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent4ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent4() {
        return agent4;
    }

    public void setAgent4(Agent agent4) {
        this.agent4 = agent4;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent5ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent5() {
        return agent5;
    }

    public void setAgent5(Agent agent5) {
        this.agent5 = agent5;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent6ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent6() {
        return agent6;
    }

    public void setAgent6(Agent agent6) {
        this.agent6 = agent6;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent7ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent7() {
        return agent7;
    }

    public void setAgent7(Agent agent7) {
        this.agent7 = agent7;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent8D", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent8() {
        return agent8;
    }

    public void setAgent8(Agent agent8) {
        this.agent8 = agent8;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent9ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent9() {
        return agent9;
    }

    public void setAgent9(Agent agent9) {
        this.agent9 = agent9;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent10ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent10() {
        return agent10;
    }

    public void setAgent10(Agent agent10) {
        this.agent10 = agent10;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent11ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent11() {
        return agent11;
    }

    public void setAgent11(Agent agent11) {
        this.agent11 = agent11;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent12ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent12() {
        return agent12;
    }

    public void setAgent12(Agent agent12) {
        this.agent12 = agent12;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent13ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent13() {
        return agent13;
    }

    public void setAgent13(Agent agent13) {
        this.agent13 = agent13;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent14ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent14() {
        return agent14;
    }

    public void setAgent14(Agent agent14) {
        this.agent14 = agent14;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent15ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent15() {
        return agent15;
    }

    public void setAgent15(Agent agent15) {
        this.agent15 = agent15;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent16ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent16() {
        return agent16;
    }

    public void setAgent16(Agent agent16) {
        this.agent16 = agent16;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent17ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent17() {
        return agent17;
    }

    public void setAgent17(Agent agent17) {
        this.agent17 = agent17;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent18ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent18() {
        return agent18;
    }

    public void setAgent18(Agent agent18) {
        this.agent18 = agent18;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent19ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent19() {
        return agent19;
    }

    public void setAgent19(Agent agent19) {
        this.agent19 = agent19;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent20ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent20() {
        return agent20;
    }

    public void setAgent20(Agent agent20) {
        this.agent20 = agent20;
    }

    /**
     *
     * @param recordedBy
     */
    public void setRecordedBy(Agent recordedBy) {
        this.recordedBy = recordedBy;
    }


    /**
     *
     * @return
     */
    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1()
    {
        return yesNo1;
    }
    public void setYesNo1(Boolean yesNo1)
    {
        this.yesNo1 = yesNo1;
    }


    /**
     *
     * @return
     */
    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2()
    {
        return yesNo2;
    }
    public void setYesNo2(Boolean yesNo2)
    {
        this.yesNo2 = yesNo2;
    }

    /**
     *
     * @return
     */
    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3()
    {
        return yesNo3;
    }

    public void setYesNo3(Boolean yesNo3)
    {
        this.yesNo3 = yesNo3;
    }

    /**
     *
     * @return
     */
    @Column(name = "YesNo4", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo4()
    {
        return yesNo4;
    }

    public void setYesNo4(Boolean yesNo4)
    {
        this.yesNo4 = yesNo4;
    }

    /**
     *
     * @return
     */
    @Column(name = "YesNo5", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo5()
    {
        return yesNo5;
    }

    public void setYesNo5(Boolean yesNo5)
    {
        this.yesNo5 = yesNo5;
    }

    /**
     *
     * @return
     */
    @Column(name = "YesNo6", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo6()
    {
        return yesNo6;
    }

    public void setYesNo6(Boolean yesNo6)
    {
        this.yesNo6 = yesNo6;
    }
    /**
     *
     * @return
     */
    @Column(name = "YesNo7", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo7()
    {
        return yesNo7;
    }

    public void setYesNo7(Boolean yesNo7)
    {
        this.yesNo7 = yesNo7;
    }

    /**
     * @return the yesNo8
     */
    @Column(name = "YesNo8", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo8() {
        return yesNo8;
    }

    /**
     * @param yesNo8 the yesNo8 to set
     */
    public void setYesNo8(Boolean yesNo8) {
        this.yesNo8 = yesNo8;
    }

    /**
     * @return the yesNo9
     */
    @Column(name = "YesNo9", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo9() {
        return yesNo9;
    }

    /**
     * @param yesNo9 the yesNo9 to set
     */
    public void setYesNo9(Boolean yesNo9) {
        this.yesNo9 = yesNo9;
    }

    /**
     * @return the yesNo10
     */
    @Column(name = "YesNo10", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo10() {
        return yesNo10;
    }

    /**
     * @param yesNo10 the yesNo10 to set
     */
    public void setYesNo10(Boolean yesNo10) {
        this.yesNo10 = yesNo10;
    }

    /**
     * @return the yesNo11
     */
    @Column(name = "YesNo11", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo11() {
        return yesNo11;
    }

    /**
     * @param yesNo11 the yesNo11 to set
     */
    public void setYesNo11(Boolean yesNo11) {
        this.yesNo11 = yesNo11;
    }

    /**
     * @return the yesNo12
     */
    @Column(name = "YesNo12", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo12() {
        return yesNo12;
    }

    /**
     * @param yesNo12 the yesNo12 to set
     */
    public void setYesNo12(Boolean yesNo12) {
        this.yesNo12 = yesNo12;
    }

    /**
     * @return the yesNo13
     */
    @Column(name = "YesNo13", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo13() {
        return yesNo13;
    }

    /**
     * @param yesNo13 the yesNo13 to set
     */
    public void setYesNo13(Boolean yesNo13) {
        this.yesNo13 = yesNo13;
    }

    /**
     * @return the yesNo14
     */
    @Column(name = "YesNo14", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo14() {
        return yesNo14;
    }

    /**
     * @param yesNo14 the yesNo14 to set
     */
    public void setYesNo14(Boolean yesNo14) {
        this.yesNo14 = yesNo14;
    }

    /**
     * @return the yesNo15
     */
    @Column(name = "YesNo15", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo15() {
        return yesNo15;
    }

    /**
     * @param yesNo15 the yesNo15 to set
     */
    public void setYesNo15(Boolean yesNo15) {
        this.yesNo15 = yesNo15;
    }

    /**
     * @return the yesNo16
     */
    @Column(name = "YesNo16", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo16() {
        return yesNo16;
    }

    /**
     * @param yesNo16 the yesNo16 to set
     */
    public void setYesNo16(Boolean yesNo16) {
        this.yesNo16 = yesNo16;
    }

    /**
     * @return the yesNo17
     */
    @Column(name = "YesNo17", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo17() {
        return yesNo17;
    }

    /**
     * @param yesNo17 the yesNo17 to set
     */
    public void setYesNo17(Boolean yesNo17) {
        this.yesNo17 = yesNo17;
    }

    /**
     * @return the yesNo18
     */
    @Column(name = "YesNo18", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo18() {
        return yesNo18;
    }

    /**
     * @param yesNo18 the yesNo18 to set
     */
    public void setYesNo18(Boolean yesNo18) {
        this.yesNo18 = yesNo18;
    }

    /**
     * @return the yesNo19
     */
    @Column(name = "YesNo19", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo19() {
        return yesNo19;
    }

    /**
     * @param yesNo19 the yesNo19 to set
     */
    public void setYesNo19(Boolean yesNo19) {
        this.yesNo19 = yesNo19;
    }

    /**
     * @return the yesNo20
     */
    @Column(name = "YesNo20", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo20() {
        return yesNo20;
    }

    /**
     * @param yesNo20 the yesNo20 to set
     */
    public void setYesNo20(Boolean yesNo20) {
        this.yesNo20 = yesNo20;
    }


    /**
     * @return the integer1
     */
    @Column(name = "Integer1", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger1() {
        return integer1;
    }

    /**
     * @param integer1 the integer1 to set
     */
    public void setInteger1(Integer integer1) {
        this.integer1 = integer1;
    }

    /**
     * @return the integer2
     */
    @Column(name = "Integer2", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger2() {
        return integer2;
    }

    /**
     * @param integer2 the integer2 to set
     */
    public void setInteger2(Integer integer2) {
        this.integer2 = integer2;
    }

    /**
     * @return the integer3
     */
    @Column(name = "Integer3", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger3() {
        return integer3;
    }

    /**
     * @param integer3 the integer3 to set
     */
    public void setInteger3(Integer integer3) {
        this.integer3 = integer3;
    }

    /**
     * @return the integer4
     */
    @Column(name = "Integer4", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger4() {
        return integer4;
    }

    /**
     * @param integer4 the integer4 to set
     */
    public void setInteger4(Integer integer4) {
        this.integer4 = integer4;
    }

    /**
     * @return the integer5
     */
    @Column(name = "Integer5", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger5() {
        return integer5;
    }

    /**
     * @param integer5 the integer5 to set
     */
    public void setInteger5(Integer integer5) {
        this.integer5 = integer5;
    }

    /**
     * @return the integer6
     */
    @Column(name = "Integer6", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger6() {
        return integer6;
    }

    /**
     * @param integer6 the integer6 to set
     */
    public void setInteger6(Integer integer6) {
        this.integer6 = integer6;
    }

    /**
     * @return the integer7
     */
    @Column(name = "Integer7", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger7() {
        return integer7;
    }

    /**
     * @param integer7 the integer7 to set
     */
    public void setInteger7(Integer integer7) {
        this.integer7 = integer7;
    }

    /**
     * @return the integer8
     */
    @Column(name = "Integer8", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger8() {
        return integer8;
    }

    /**
     * @param integer8 the integer8 to set
     */
    public void setInteger8(Integer integer8) {
        this.integer8 = integer8;
    }

    /**
     * @return the integer9
     */
    @Column(name = "Integer9", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger9() {
        return integer9;
    }

    /**
     * @param integer9 the integer9 to set
     */
    public void setInteger9(Integer integer9) {
        this.integer9 = integer9;
    }

    /**
     * @return the integer10
     */
    @Column(name = "Integer10", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger10() {
        return integer10;
    }

    /**
     * @param integer10 the integer10 to set
     */
    public void setInteger10(Integer integer10) {
        this.integer10 = integer10;
    }

    @Column(name = "Integer11", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger11() {
        return integer11;
    }

    public void setInteger11(Integer integer11) {
        this.integer11 = integer11;
    }

    @Column(name = "Integer12", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger12() {
        return integer12;
    }

    public void setInteger12(Integer integer12) {
        this.integer12 = integer12;
    }

    @Column(name = "Integer13", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger13() {
        return integer13;
    }

    public void setInteger13(Integer integer13) {
        this.integer13 = integer13;
    }

    @Column(name = "Integer14", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger14() {
        return integer14;
    }

    public void setInteger14(Integer integer14) {
        this.integer14 = integer14;
    }

    @Column(name = "Integer15", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger15() {
        return integer15;
    }

    public void setInteger15(Integer integer15) {
        this.integer15 = integer15;
    }

    @Column(name = "Integer16", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger16() {
        return integer16;
    }

    public void setInteger16(Integer integer16) {
        this.integer16 = integer16;
    }

    @Column(name = "Integer17", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger17() {
        return integer17;
    }

    public void setInteger17(Integer integer17) {
        this.integer17 = integer17;
    }

    @Column(name = "Integer18", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger18() {
        return integer18;
    }

    public void setInteger18(Integer integer18) {
        this.integer18 = integer18;
    }

    @Column(name = "Integer19", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger19() {
        return integer19;
    }

    public void setInteger19(Integer integer19) {
        this.integer19 = integer19;
    }

    @Column(name = "Integer20", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger20() {
        return integer20;
    }

    public void setInteger20(Integer integer20) {
        this.integer20 = integer20;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date1", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate1() {
        return date1;
    }

    public void setDate1(Calendar date1) {
        this.date1 = date1;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date2", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate2() {
        return date2;
    }

    public void setDate2(Calendar date2) {
        this.date2 = date2;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date3", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate3() {
        return date3;
    }

    public void setDate3(Calendar date3) {
        this.date3 = date3;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date4", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate4() {
        return date4;
    }

    public void setDate4(Calendar date4) {
        this.date4 = date4;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date5", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate5() {
        return date5;
    }

    public void setDate5(Calendar date5) {
        this.date5 = date5;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date6", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate6() {
        return date6;
    }

    public void setDate6(Calendar date6) {
        this.date6 = date6;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date7", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate7() {
        return date7;
    }

    public void setDate7(Calendar date7) {
        this.date7 = date7;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date8", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate8() {
        return date8;
    }

    public void setDate8(Calendar date8) {
        this.date8 = date8;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date9", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate9() {
        return date9;
    }

    public void setDate9(Calendar date9) {
        this.date9 = date9;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date10", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate10() {
        return date10;
    }

    public void setDate10(Calendar date10) {
        this.date10 = date10;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date11", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate11() {
        return date11;
    }

    public void setDate11(Calendar date11) {
        this.date11 = date11;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date12", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate12() {
        return date12;
    }

    public void setDate12(Calendar date12) {
        this.date12 = date12;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date13", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate13() {
        return date13;
    }

    public void setDate13(Calendar date13) {
        this.date13 = date13;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date14", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate14() {
        return date14;
    }

    public void setDate14(Calendar date14) {
        this.date14 = date14;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date15", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate15() {
        return date15;
    }

    public void setDate15(Calendar date15) {
        this.date15 = date15;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date16", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate16() {
        return date16;
    }

    public void setDate16(Calendar date16) {
        this.date16 = date16;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date17", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate17() {
        return date17;
    }

    public void setDate17(Calendar date17) {
        this.date17 = date17;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date18", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate18() {
        return date18;
    }

    public void setDate18(Calendar date18) {
        this.date18 = date18;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date19", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate19() {
        return date19;
    }

    public void setDate19(Calendar date19) {
        this.date19 = date19;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date20", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate20() {
        return date20;
    }

    public void setDate20(Calendar date20) {
        this.date20 = date20;
    }


    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber1()
    {
        return number1;
    }

    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber2()
    {
        return number2;
    }

    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber3()
    {
        return number3;
    }

    @Column(name = "Number4", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber4()
    {
        return number4;
    }

    @Column(name = "Number5", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber5()
    {
        return number5;
    }

    @Column(name = "Number6", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber6()
    {
        return number6;
    }

    @Column(name = "Number7", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber7()
    {
        return number7;
    }

    @Column(name = "Number8", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber8()
    {
        return number8;
    }

    @Column(name = "Number9", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber9()
    {
        return number9;
    }

    @Column(name = "Number10", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber10()
    {
        return number10;
    }

    @Column(name = "Number11", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber11()
    {
        return number11;
    }

    @Column(name = "Number12", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber12()
    {
        return number12;
    }

    @Column(name = "Number13", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber13()
    {
        return number13;
    }

    @Column(name = "Number14", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber14()
    {
        return number14;
    }

    @Column(name = "Number15", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber15()
    {
        return number15;
    }

    @Column(name = "Number16", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber16()
    {
        return number16;
    }

    @Column(name = "Number17", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber17()
    {
        return number17;
    }

    @Column(name = "Number18", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber18()
    {
        return number18;
    }

    @Column(name = "Number19", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber19()
    {
        return number19;
    }

    @Column(name = "Number20", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber20()
    {
        return number20;
    }

    /**
     *
     * @param number1
     */
    public void setNumber1(Double number1)
    {
        this.number1 = number1;
    }

    /**
     *
     * @param number2
     */
    public void setNumber2(Double number2)
    {
        this.number2 = number2;
    }

    /**
     *
     * @param number3
     */
    public void setNumber3(Double number3)
    {
        this.number3 = number3;
    }

    /**
     *
     * @param number4
     */
    public void setNumber4(Double number4)
    {
        this.number4 = number4;
    }

    /**
     *
     * @param number5
     */
    public void setNumber5(Double number5)
    {
        this.number5 = number5;
    }

    /**
     *
     * @param number6
     */
    public void setNumber6(Double number6)
    {
        this.number6 = number6;
    }

    /**
     *
     * @param number7
     */
    public void setNumber7(Double number7)
    {
        this.number7 = number7;
    }

    /**
     *
     * @param number8
     */
    public void setNumber8(Double number8)
    {
        this.number8 = number8;
    }

    /**
     *
     * @param number9
     */
    public void setNumber9(Double number9)
    {
        this.number9 = number9;
    }

    /**
     *
     * @param number10
     */
    public void setNumber10(Double number10)
    {
        this.number10 = number10;
    }

    /**
     *
     * @param number11
     */
    public void setNumber11(Double number11)
    {
        this.number11 = number11;
    }

    /**
     *
     * @param number12
     */
    public void setNumber12(Double number12)
    {
        this.number12 = number12;
    }

    /**
     *
     * @param number13
     */
    public void setNumber13(Double number13)
    {
        this.number13 = number13;
    }

    /**
     *
     * @param number14
     */
    public void setNumber14(Double number14)
    {
        this.number14 = number14;
    }

    /**
     *
     * @param number15
     */
    public void setNumber15(Double number15)
    {
        this.number15 = number15;
    }

    /**
     *
     * @param number16
     */
    public void setNumber16(Double number16)
    {
        this.number16 = number16;
    }

    /**
     *
     * @param number17
     */
    public void setNumber17(Double number17)
    {
        this.number17 = number17;
    }

    /**
     *
     * @param number18
     */
    public void setNumber18(Double number18)
    {
        this.number18 = number18;
    }

    /**
     *
     * @param number19
     */
    public void setNumber19(Double number19)
    {
        this.number19 = number19;

    }

    /**
     *
     * @param number20
     */
    public void setNumber20(Double number20)
    {
        this.number20 = number20;
    }


    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    @Lob
    @Column(name = "Text3", length = 65535)
    public String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    @Lob
    @Column(name = "Text4", length = 65535)
    public String getText4() {
        return text4;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    @Lob
    @Column(name = "Text5", length = 65535)
    public String getText5() {
        return text5;
    }

    public void setText5(String text5) {
        this.text5 = text5;
    }

    @Lob
    @Column(name = "Text6", length = 65535)
    public String getText6() {
        return text6;
    }

    public void setText6(String text6) {
        this.text6 = text6;
    }

    @Lob
    @Column(name = "Text7", length = 65535)
    public String getText7() {
        return text7;
    }

    public void setText7(String text7) {
        this.text7 = text7;
    }

    @Lob
    @Column(name = "Text8", length = 65535)
    public String getText8() {
        return text8;
    }

    public void setText8(String text8) {
        this.text8 = text8;
    }

    @Lob
    @Column(name = "Text9", length = 65535)
    public String getText9() {
        return text9;
    }

    public void setText9(String text9) {
        this.text9 = text9;
    }

    @Lob
    @Column(name = "Text10", length = 65535)
    public String getText10() {
        return text10;
    }

    public void setText10(String text10) {
        this.text10 = text10;
    }

    @Lob
    @Column(name = "Text11", length = 65535)
    public String getText11() {
        return text11;
    }

    public void setText11(String text11) {
        this.text11 = text11;
    }

    @Lob
    @Column(name = "Text12", length = 65535)
    public String getText12() {
        return text12;
    }

    public void setText12(String text12) {
        this.text12 = text12;
    }

    @Lob
    @Column(name = "Text13", length = 65535)
    public String getText13() {
        return text13;
    }

    public void setText13(String text13) {
        this.text13 = text13;
    }

    @Lob
    @Column(name = "Text14", length = 65535)
    public String getText14() {
        return text14;
    }

    public void setText14(String text14) {
        this.text14 = text14;
    }

    @Lob
    @Column(name = "Text15", length = 65535)
    public String getText15() {
        return text15;
    }

    public void setText15(String text15) {
        this.text15 = text15;
    }

    @Lob
    @Column(name = "Text16", length = 65535)
    public String getText16() {
        return text16;
    }

    public void setText16(String text16) {
        this.text16 = text16;
    }

    @Lob
    @Column(name = "Text17", length = 65535)
    public String getText17() {
        return text17;
    }

    public void setText17(String text17) {
        this.text17 = text17;
    }

    @Lob
    @Column(name = "Text18", length = 65535)
    public String getText18() {
        return text18;
    }

    public void setText18(String text18) {
        this.text18 = text18;
    }

    @Lob
    @Column(name = "Text19", length = 65535)
    public String getText19() {
        return text19;
    }

    public void setText19(String text19) {
        this.text19 = text19;
    }

    @Lob
    @Column(name = "Text20", length = 65535)
    public String getText20() {
        return text20;
    }

    public void setText20(String text20) {
        this.text20 = text20;
    }

    @Lob
    @Column(name = "Text21", length = 65535)
    public String getText21() {
        return text21;
    }

    public void setText21(String text21) {
        this.text21 = text21;
    }

    @Lob
    @Column(name = "Text22", length = 65535)
    public String getText22() {
        return text22;
    }

    public void setText22(String text22) {
        this.text22 = text22;
    }

    @Lob
    @Column(name = "Text23", length = 65535)
    public String getText23() {
        return text23;
    }

    public void setText23(String text23) {
        this.text23 = text23;
    }

    @Lob
    @Column(name = "Text24", length = 65535)
    public String getText24() {
        return text24;
    }

    public void setText24(String text24) {
        this.text24 = text24;
    }

    @Lob
    @Column(name = "Text25", length = 65535)
    public String getText25() {
        return text25;
    }

    public void setText25(String text25) {
        this.text25 = text25;
    }

    @Lob
    @Column(name = "Text26", length = 65535)
    public String getText26() {
        return text26;
    }

    public void setText26(String text26) {
        this.text26 = text26;
    }

    @Lob
    @Column(name = "Text27", length = 65535)
    public String getText27() {
        return text27;
    }

    public void setText27(String text27) {
        this.text27 = text27;
    }

    @Lob
    @Column(name = "Text28", length = 65535)
    public String getText28() {
        return text28;
    }

    public void setText28(String text28) {
        this.text28 = text28;
    }

    @Lob
    @Column(name = "Text29", length = 65535)
    public String getText29() {
        return text29;
    }

    public void setText29(String text29) {
        this.text29 = text29;
    }

    @Lob
    @Column(name = "Text30", length = 65535)
    public String getText30() {
        return text30;
    }

    public void setText30(String text30) {
        this.text30 = text30;
    }

    @Lob
    @Column(name = "Text31", length = 65535)
    public String getText31() {
        return text31;
    }

    public void setText31(String text31) {
        this.text31 = text31;
    }

    @Lob
    @Column(name = "Text32", length = 65535)
    public String getText32() {
        return text32;
    }

    public void setText32(String text32) {
        this.text32 = text32;
    }

    @Lob
    @Column(name = "Text33", length = 65535)
    public String getText33() {
        return text33;
    }

    public void setText33(String text33) {
        this.text33 = text33;
    }

    @Lob
    @Column(name = "Text34", length = 65535)
    public String getText34() {
        return text34;
    }

    public void setText34(String text34) {
        this.text34 = text34;
    }

    @Lob
    @Column(name = "Text35", length = 65535)
    public String getText35() {
        return text35;
    }

    public void setText35(String text35) {
        this.text35 = text35;
    }

    @Lob
    @Column(name = "Text36", length = 65535)
    public String getText36() {
        return text36;
    }

    public void setText36(String text36) {
        this.text36 = text36;
    }

    @Lob
    @Column(name = "Text37", length = 65535)
    public String getText37() {
        return text37;
    }

    public void setText37(String text37) {
        this.text37 = text37;
    }

    @Lob
    @Column(name = "Text38", length = 65535)
    public String getText38() {
        return text38;
    }

    public void setText38(String text38) {
        this.text38 = text38;
    }

    @Lob
    @Column(name = "Text39", length = 65535)
    public String getText39() {
        return text39;
    }

    public void setText39(String text39) {
        this.text39 = text39;
    }

    @Lob
    @Column(name = "Text40", length = 65535)
    public String getText40() {
        return text40;
    }

    public void setText40(String text40) {
        this.text40 = text40;
    }



    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setPreparationPropertyId(Integer preparationPropertyId)
    {
        this.preparationPropertyId = preparationPropertyId;
    }


    /* more generic matching stuff compared to localitydetail match method for example.
        Implemented, but not used in collectionobjectproperty

    private String[] unMatchables = {"id", "preparationpropertyid", "version", "createbyagentid", "modifiedbyagentid",
            "timestampmodified", "timestampcreated"}; //etc???
    private List<String> unMatchableList = Arrays.asList(unMatchables);//new ArrayList<>(unMatchables);
    private boolean matchThisField(Method m) {
        if (m.getAnnotation(javax.persistence.Column.class) != null || m.getAnnotation(javax.persistence.ManyToOne.class) != null) {
            return unMatchableList.indexOf(m.getName().replace("get", "").toLowerCase()) == -1;
        } else {
            return false;
        }
    }

    private boolean matchVals(Object o1, Object o2) {
        return (o1 == null && o2 == null) || (o1 != null && o2 != null && o1.equals(o2));
    }

    private boolean matches(PreparationProperty o, Method m) {
        if (matchThisField(m)) {
            try {
                Object mine = m.invoke(this);
                Object its = m.invoke(o);
                if (DataModelObjBase.class.isAssignableFrom(m.getReturnType())) {
                    return matchVals(mine == null ? null : ((DataModelObjBase) mine).getId(), its == null ? null : ((DataModelObjBase) its).getId());
                }
                return matchVals(mine, its);
            } catch (IllegalAccessException | InvocationTargetException x) {
                x.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }


    /**
     * @param o
     * @return true if 'non-system' fields all match.
     *
     *
    public boolean matches(PreparationProperty o) {
        if (o == null) {
            return false;
        }
        Method[] ms = getClass().getDeclaredMethods();
        for (Method m : ms) {
            if (!matches(o, m)) {
                return false;
            }
        }
        return true;
    } */


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        PreparationProperty obj    = (PreparationProperty)super.clone();
        obj.preparationPropertyId  = null;
        obj.preparation           = null;

        return obj;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        String str = DataObjFieldFormatMgr.getInstance().format(this, getDataClass());
        return StringUtils.isNotEmpty(str) ? str : Integer.valueOf(getTableId()).toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Preparation.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT PreparationID FROM preparation WHERE PreparationID = " + preparation.getId());
        if (ids.size() == 1)
        {
            return (Integer)ids.get(0);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return PreparationProperty.class;
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
        return 154;
    }
}

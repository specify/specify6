/* Copyright (C) 2021, Specify Collections Consortium
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

import java.util.HashSet;
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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

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
@Table(name = "collectingtripattribute")
@org.hibernate.annotations.Table(appliesTo="collectingtripattribute", indexes =
        {
                @Index (name="COLTRPSDispIDX", columnNames={"DisciplineID"})
        })
public class CollectingTripAttribute extends DisciplineMember implements Cloneable
{
    protected Integer collectingTripAttributeId;

    protected Float number1;
    protected Float number2;
    protected Float number3;
    protected Float number4;
    protected Float number5;
    protected Float number6;
    protected Float number7;
    protected Float number8;
    protected Float number9;
    protected Float number10;
    protected Float number11;
    protected Float number12;
    protected Float number13;
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

    protected String remarks;

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

    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;
    protected Boolean yesNo4;
    protected Boolean yesNo5;

    protected Set<CollectingTrip> collectingTrips;

    // Constructors
    /** default constructor */
    public CollectingTripAttribute()
    {
        // do nothing
    }

    /** constructor with id */
    public CollectingTripAttribute(Integer collectingTripAttributeId)
    {
        this.collectingTripAttributeId = collectingTripAttributeId;
    }

    @Column(name = "Number9", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber9()
    {
        return number9;
    }

    @Column(name = "Text16", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText16()
    {
        return text16;
    }

    @Id
    @GeneratedValue
    @Column(name = "CollectingTripAttributeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectingTripAttributeId()
    {
        return collectingTripAttributeId;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectingTripAttribute")
    public Set<CollectingTrip> getCollectingTrips() {
        return this.collectingTrips;
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

    @Column(name = "Text17", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText17()
    {
        return text17;
    }


    @Column(name = "Number12", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber12()
    {
        return number12;
    }

    @Column(name = "Number13", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber13()
    {
        return number13;
    }

    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1()
    {
        return number1;
    }

    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2()
    {
        return number2;
    }

    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber3()
    {
        return number3;
    }

    @Column(name = "Number4", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber4()
    {
        return number4;
    }

    @Column(name = "Number5", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber5()
    {
        return number5;
    }

    @Column(name = "Number6", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber6()
    {
        return number6;
    }

    @Column(name = "Number7", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber7()
    {
        return number7;
    }

    @Column(name = "Number8", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber8()
    {
        return number8;
    }

    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return remarks;
    }

    @Column(name = "Text14", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText14()
    {
        return text14;
    }

    @Column(name = "Text15", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText15()
    {
        return text15;
    }

    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1()
    {
        return text1;
    }

    @Column(name = "Text10", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText10()
    {
        return text10;
    }

    @Column(name = "Text11", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText11()
    {
        return text11;
    }

    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2()
    {
        return text2;
    }

    @Lob
    @Column(name = "Text3", length = 65535)
    public String getText3()
    {
        return text3;
    }

    @Column(name = "Text4", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
    public String getText4()
    {
        return text4;
    }

    @Column(name = "Text5", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
    public String getText5()
    {
        return text5;
    }

    @Column(name = "Text6", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText6()
    {
        return text6;
    }

    @Column(name = "Text7", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText7()
    {
        return text7;
    }

    @Column(name = "Text8", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText8()
    {
        return text8;
    }

    @Column(name = "Text9", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText9()
    {
        return text9;
    }

    @Column(name = "Text12", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText12()
    {
        return text12;
    }

    @Column(name = "Text13", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText13()
    {
        return text13;
    }

    @Column(name = "Number11", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber11()
    {
        return number11;
    }

    @Column(name = "Number10", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber10()
    {
        return number10;
    }

    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1()
    {
        return yesNo1;
    }

    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2()
    {
        return yesNo2;
    }

    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3()
    {
        return yesNo3;
    }

    @Column(name = "YesNo4", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo4()
    {
        return yesNo4;
    }

    @Column(name = "YesNo5", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo5()
    {
        return yesNo5;
    }

    // Initializer
    @Override
    public void initialize()
    {
        //NOTE: if fields are added to this table, the matches method must be updated accordingly!!!!
        super.init();
        collectingTripAttributeId = null;
        number9 = null;
        number10 = null;
        number11 = null;
        text12 = null;
        text16 = null;
        text14 = null;
        text6 = null;
        number6 = null;
        number7 = null;
        text7 = null;
        text15 = null;
        text13 = null;
        text17 = null;
        text8 = null;
        text9 = null;
        text10 = null;
        number8 = null;
        text11 = null;
        remarks = null;
        number13 = null;
        number12 = null;
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

        //NOTE: if fields are added to this table, the matches method must be updated accordingly!!!!

        collectingTrips = new HashSet<CollectingTrip>();
    }
    // End Initializer

    public void setNumber9(Float number9)
    {
        this.number9 = number9;
    }

    public void setText16(String text16)
    {
        this.text16 = text16;
    }

    public void setCollectingTripAttributeId(Integer collectingTripAttributeId)
    {
        this.collectingTripAttributeId = collectingTripAttributeId;
    }

    public void setCollectingTrips(Set<CollectingTrip> collectingTrips)
    {
        this.collectingTrips = collectingTrips;
    }

    public void setText17(String text17)
    {
        this.text17 = text17;
    }

    public void setNumber12(Float number12)
    {
        this.number12 = number12;
    }

    public void setNumber13(Float number13)
    {
        this.number13 = number13;
    }

    public void setNumber1(Float number1)
    {
        this.number1 = number1;
    }

    public void setNumber2(Float number2)
    {
        this.number2 = number2;
    }

    public void setNumber3(Float number3)
    {
        this.number3 = number3;
    }

    public void setNumber4(Float number4)
    {
        this.number4 = number4;
    }

    public void setNumber5(Float number5)
    {
        this.number5 = number5;
    }

    public void setNumber6(Float number6)
    {
        this.number6 = number6;
    }

    public void setNumber7(Float number7)
    {
        this.number7 = number7;
    }

    public void setNumber8(Float number8)
    {
        this.number8 = number8;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public void setText14(String text14)
    {
        this.text14 = text14;
    }

    public void setText15(String text15)
    {
        this.text15 = text15;
    }

    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    public void setText10(String text10)
    {
        this.text10 = text10;
    }

    public void setText11(String text11)
    {
        this.text11 = text11;
    }

    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    public void setText3(String text3)
    {
        this.text3 = text3;
    }

    public void setText4(String text4)
    {
        this.text4 = text4;
    }

    public void setText5(String text5)
    {
        this.text5 = text5;
    }

    public void setText6(String text6)
    {
        this.text6 = text6;
    }

    public void setText7(String text7)
    {
        this.text7 = text7;
    }

    public void setText8(String text8)
    {
        this.text8 = text8;
    }

    public void setText9(String text9)
    {
        this.text9 = text9;
    }

    public void setText12(String text12)
    {
        this.text12 = text12;
    }

    public void setText13(String text13)
    {
        this.text13 = text13;
    }

    public void setNumber11(Float number11)
    {
        this.number11 = number11;
    }

    public void setNumber10(Float number10)
    {
        this.number10 = number10;
    }

    public void setYesNo1(Boolean yesNo1)
    {
        this.yesNo1 = yesNo1;
    }

    public void setYesNo2(Boolean yesNo2)
    {
        this.yesNo2 = yesNo2;
    }

    public void setYesNo3(Boolean yesNo3)
    {
        this.yesNo3 = yesNo3;
    }

    public void setYesNo4(Boolean yesNo4)
    {
        this.yesNo4 = yesNo4;
    }

    public void setYesNo5(Boolean yesNo5)
    {
        this.yesNo5 = yesNo5;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        CollectingTripAttribute obj = (CollectingTripAttribute)super.clone();
        obj.collectingTripAttributeId = null;
        obj.collectingTrips           = new HashSet<CollectingTrip>();

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
        return StringUtils.isNotEmpty(str) ? str : "1";
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectingTripAttribute.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.collectingTripAttributeId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return CollectingTrip.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT CollectingTripID FROM collectingtrip WHERE CollectingTripAttributeID = "+ collectingTripAttributeId);
        if (ids.size() == 1)
        {
            return (Integer)ids.get(0);
        }
        return null;
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 157;
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


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    /**
     * @param o
     * @return true if 'non-system' fields all match.
     *
     */
    public boolean matches(CollectingTripAttribute o)
    {
        if (o == null)
        {
            return false;
        }

        return
                ((integer1 == null && o.integer1 == null) || ((integer1 != null && o.integer1 != null) && integer1.equals(o.integer1))) &&
                        ((integer2 == null && o.integer2 == null) || ((integer2 != null && o.integer2 != null) && integer2.equals(o.integer2))) &&
                        ((integer3 == null && o.integer3 == null) || ((integer3 != null && o.integer3 != null) && integer3.equals(o.integer3))) &&
                        ((integer4 == null && o.integer4 == null) || ((integer4 != null && o.integer4 != null) && integer4.equals(o.integer4))) &&
                        ((integer5 == null && o.integer5 == null) || ((integer5 != null && o.integer5 != null) && integer5.equals(o.integer5))) &&
                        ((integer6 == null && o.integer6 == null) || ((integer6 != null && o.integer6 != null) && integer6.equals(o.integer6))) &&
                        ((integer7 == null && o.integer7 == null) || ((integer7 != null && o.integer7 != null) && integer7.equals(o.integer7))) &&
                        ((integer8 == null && o.integer8 == null) || ((integer8 != null && o.integer8 != null) && integer8.equals(o.integer8))) &&
                        ((integer9 == null && o.integer9 == null) || ((integer9 != null && o.integer9 != null) && integer9.equals(o.integer9))) &&
                        ((integer10 == null && o.integer10 == null) || ((integer10 != null && o.integer10 != null) && integer10.equals(o.integer10))) &&

                        ((number1 == null && o.number1 == null) || ((number1 != null && o.number1 != null) && number1.equals(o.number1))) &&
                        ((number2 == null && o.number2 == null) || ((number2 != null && o.number2 != null) && number2.equals(o.number2))) &&
                        ((number3 == null && o.number3 == null) || ((number3 != null && o.number3 != null) && number3.equals(o.number3))) &&
                        ((number4 == null && o.number4 == null) || ((number4 != null && o.number4 != null) && number4.equals(o.number4))) &&
                        ((number5 == null && o.number5 == null) || ((number5 != null && o.number5 != null) && number5.equals(o.number5))) &&
                        ((number6 == null && o.number6 == null) || ((number6 != null && o.number6 != null) && number6.equals(o.number6))) &&
                        ((number7 == null && o.number7 == null) || ((number7 != null && o.number7 != null) && number7.equals(o.number7))) &&
                        ((number8 == null && o.number8 == null) || ((number8 != null && o.number8 != null) && number8.equals(o.number8))) &&
                        ((number9 == null && o.number9 == null) || ((number9 != null && o.number9 != null) && number9.equals(o.number9))) &&
                        ((number10 == null && o.number10 == null) || ((number10 != null && o.number10 != null) && number10.equals(o.number10))) &&
                        ((number11 == null && o.number11 == null) || ((number11 != null && o.number11 != null) && number11.equals(o.number11))) &&
                        ((number12 == null && o.number12 == null) || ((number12 != null && o.number12 != null) && number12.equals(o.number12))) &&
                        ((number13 == null && o.number13 == null) || ((number13 != null && o.number13 != null) && number13.equals(o.number13))) &&
                        ((remarks == null && o.remarks == null) || ((remarks != null && o.remarks != null) && remarks.equals(o.remarks))) &&
                        ((text1 == null && o.text1 == null) || ((text1 != null && o.text1 != null) && text1.equals(o.text1))) &&
                        ((text2 == null && o.text2 == null) || ((text2 != null && o.text2 != null) && text2.equals(o.text2))) &&
                        ((text3 == null && o.text3 == null) || ((text3 != null && o.text3 != null) && text3.equals(o.text3))) &&
                        ((text4 == null && o.text4 == null) || ((text4 != null && o.text4 != null) && text4.equals(o.text4))) &&
                        ((text5 == null && o.text5 == null) || ((text5 != null && o.text5 != null) && text5.equals(o.text5))) &&
                        ((text6 == null && o.text6 == null) || ((text6 != null && o.text6 != null) && text6.equals(o.text6))) &&
                        ((text7 == null && o.text7 == null) || ((text7 != null && o.text7 != null) && text7.equals(o.text7))) &&
                        ((text8 == null && o.text8 == null) || ((text8 != null && o.text8 != null) && text8.equals(o.text8))) &&
                        ((text9 == null && o.text9 == null) || ((text9 != null && o.text9 != null) && text9.equals(o.text9))) &&
                        ((text10 == null && o.text10 == null) || ((text10 != null && o.text10 != null) && text10.equals(o.text10))) &&
                        ((text11 == null && o.text11 == null) || ((text11 != null && o.text11 != null) && text11.equals(o.text11))) &&
                        ((text12 == null && o.text12 == null) || ((text12 != null && o.text12 != null) && text12.equals(o.text12))) &&
                        ((text13 == null && o.text13 == null) || ((text13 != null && o.text13 != null) && text13.equals(o.text13))) &&
                        ((text14 == null && o.text14 == null) || ((text14 != null && o.text14 != null) && text14.equals(o.text14))) &&
                        ((text15 == null && o.text15 == null) || ((text15 != null && o.text15 != null) && text15.equals(o.text15))) &&
                        ((text16 == null && o.text16 == null) || ((text16 != null && o.text16 != null) && text16.equals(o.text16))) &&
                        ((text17 == null && o.text17 == null) || ((text17 != null && o.text17 != null) && text17.equals(o.text17))) &&
                        ((yesNo1 == null && o.yesNo1 == null) || ((yesNo1 != null && o.yesNo1 != null) && yesNo1.equals(o.yesNo1))) &&
                        ((yesNo2 == null && o.yesNo2 == null) || ((yesNo2 != null && o.yesNo2 != null) && yesNo2.equals(o.yesNo2))) &&
                        ((yesNo3 == null && o.yesNo3 == null) || ((yesNo3 != null && o.yesNo3 != null) && yesNo3.equals(o.yesNo3))) &&
                        ((yesNo4 == null && o.yesNo4 == null) || ((yesNo4 != null && o.yesNo4 != null) && yesNo4.equals(o.yesNo4))) &&
                        ((yesNo5 == null && o.yesNo5 == null) || ((yesNo5 != null && o.yesNo5 != null) && yesNo5.equals(o.yesNo5)));
    }

}

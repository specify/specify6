/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.util.Date;
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

import org.hibernate.annotations.Index;

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
@Table(name = "preparationattribute")
@org.hibernate.annotations.Table(appliesTo="preparationattribute", indexes =
    {   @Index (name="PrepAttrsColMemIDX", columnNames={"CollectionMemberID"})
    })
public class PreparationAttribute extends CollectionMember
{
    protected Integer preparationAttributeId;
    protected Date attrDate;
    protected String text22;  // preparationType
    protected String text23;  // containerType
    protected String text24;  // medium
    protected Float number1;
    protected Float number2;
    protected Float number3;
    protected Integer number4;
    protected Integer number5;
    protected Integer number6;
    protected Integer number7;
    protected Integer number8;
    protected Short number9;
    protected String remarks;
    protected String text26;     // size
    protected String text25;
    protected String text1;
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
    protected String text2;
    protected String text20;
    protected String text21;
    protected String text3;
    protected String text4;
    protected String text5;
    protected String text6;
    protected String text7;
    protected String text8;
    protected String text9;
    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;
    protected Boolean yesNo4;
    
    protected Set<Preparation> preparations;
    

    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        preparationAttributeId = null;
        attrDate = null;
        text24 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        text6 = null;
        text7 = null;
        text8 = null;
        text9 = null;
        text26 = null;
        text10 = null;
        text11 = null;
        text12 = null;
        text13 = null;
        text14 = null;
        text15 = null;
        number4 = null;
        number5 = null;
        number6 = null;
        number7 = null;
        text16 = null;
        text17 = null;
        text18 = null;
        text25 = null;
        text22 = null;
        number8 = null;
        text23 = null;
        text19 = null;
        text20 = null;
        text21 = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        number3 = null;
        remarks = null;
        number9 = null;
        yesNo1 = null;
        yesNo2 = null;
        preparations = new HashSet<Preparation>();
    }

    /**
     * @return the preparationAttributeId
     */
    @Id
    @GeneratedValue
    @Column(name = "PreparationAttributeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getPreparationAttributeId()
    {
        return preparationAttributeId;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.preparationAttributeId;
    }
    
    /**
     * @return the preparations
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparationAttribute")
    public Set<Preparation> getPreparations()
    {
        return preparations;
    }

    /**
     * @return the Text23
     */
    @Column(name = "Text23", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText23()
    {
        return text23;
    }

    /**
     * @return the Text24
     */
    @Column(name = "Text24", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText24()
    {
        return text24;
    }

    /**
     * @return the number1
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1()
    {
        return number1;
    }

    /**
     * @return the number2
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2()
    {
        return number2;
    }

    /**
     * @return the number3
     */
    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber3()
    {
        return number3;
    }

    /**
     * @return the number4
     */
    @Column(name = "Number4", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber4()
    {
        return number4;
    }

    /**
     * @return the number5
     */
    @Column(name = "Number5", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber5()
    {
        return number5;
    }

    /**
     * @return the number6
     */
    @Column(name = "Number6", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber6()
    {
        return number6;
    }

    /**
     * @return the number7
     */
    @Column(name = "Number7", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber7()
    {
        return number7;
    }

    /**
     * @return the number8
     */
    @Column(name = "Number8", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber8()
    {
        return number8;
    }

    /**
     * @return the number9
     */
    @Column(name = "Number9", unique = false, nullable = true, insertable = true, updatable = true)
   public Short getNumber9()
    {
        return number9;
    }

    /**
     * @return the preparationType
     */
    @Column(name = "Text22", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public String getText22()
    {
        return text22;
    }

    /**
     * @return the preparedDate
     */
    @Column(name = "AttrDate", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Date getAttrDate()
    {
        return attrDate;
    }

    /**
     * @return the remarks
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return remarks;
    }

    /**
     * @return the size
     */
    @Column(name = "Text26", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText26()
    {
        return text26;
    }

    /**
     * @return the storageInfo
     */
    @Column(name = "Text25", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText25()
    {
        return text25;
    }

    /**
     * @return the text1
     */
    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
    public String getText1()
    {
        return text1;
    }

    /**
     * @return the text10
     */
    @Column(name = "Text10", unique = false, nullable = true, insertable = true, updatable = true, length = 300) // was URL
    public String getText10()
    {
        return text10;
    }

    /**
     * @return the text11
     */
    @Column(name = "Text11", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText11()
    {
        return text11;
    }

    /**
     * @return the text12
     */
    @Column(name = "Text12", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText12()
    {
        return text12;
    }

    /**
     * @return the text13
     */
    @Column(name = "Text13", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText13()
    {
        return text13;
    }

    /**
     * @return the text14
     */
    @Column(name = "Text14", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText14()
    {
        return text14;
    }

    /**
     * @return the text15
     */
    @Column(name = "Text15", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText15()
    {
        return text15;
    }

    /**
     * @return the text16
     */
    @Column(name = "Text16", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText16()
    {
        return text16;
    }

    /**
     * @return the text17
     */
    @Column(name = "Text17", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText17()
    {
        return text17;
    }

    /**
     * @return the text18
     */
    @Column(name = "Text18", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText18()
    {
        return text18;
    }

    /**
     * @return the text19
     */
    @Column(name = "Text19", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText19()
    {
        return text19;
    }

    /**
     * @return the text2
     */
    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
    public String getText2()
    {
        return text2;
    }

    /**
     * @return the text20
     */
    @Column(name = "Text20", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText20()
    {
        return text20;
    }

    /**
     * @return the text21
     */
    @Column(name = "Text21", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText21()
    {
        return text21;
    }

    /**
     * @return the text3
     */
    @Column(name = "Text3", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText3()
    {
        return text3;
    }

    /**
     * @return the text4
     */
    @Column(name = "Text4", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText4()
    {
        return text4;
    }

    /**
     * @return the text5
     */
    @Column(name = "Text5", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText5()
    {
        return text5;
    }

    /**
     * @return the text6
     */
    @Column(name = "Text6", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText6()
    {
        return text6;
    }

    /**
     * @return the text7
     */
    @Column(name = "Text7", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText7()
    {
        return text7;
    }

    /**
     * @return the text8
     */
    @Column(name = "Text8", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText8()
    {
        return text8;
    }

    /**
     * @return the text9
     */
    @Column(name = "Text9", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText9()
    {
        return text9;
    }

    /**
     * @return the yesNo1
     */
    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1()
    {
        return yesNo1;
    }

    /**
     * @return the yesNo2
     */
    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2()
    {
        return yesNo2;
    }

    /**
     * @return the yesNo3
     */
    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3()
    {
        return yesNo3;
    }

    /**
     * @param yesNo3 the yesNo3 to set
     */
    public void setYesNo3(Boolean yesNo3)
    {
        this.yesNo3 = yesNo3;
    }

    /**
     * @return the yesNo4
     */
    @Column(name = "YesNo4", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo4()
    {
        return yesNo4;
    }

    /**
     * @param yesNo4 the yesNo4 to set
     */
    public void setYesNo4(Boolean yesNo4)
    {
        this.yesNo4 = yesNo4;
    }

    /**
     * @param preparations the preparations to set
     */
    public void setPreparations(Set<Preparation> preparations)
    {
        this.preparations = preparations;
    }

    /**
     * @param containerType the containerType to set
     */
    public void setText23(String containerType)
    {
        this.text23 = containerType;
    }

    /**
     * @param medium the medium to set
     */
    public void setText24(String text24)
    {
        this.text24 = text24;
    }

    /**
     * @param number1 the number1 to set
     */
    public void setNumber1(Float number1)
    {
        this.number1 = number1;
    }

    /**
     * @param number2 the number2 to set
     */
    public void setNumber2(Float number2)
    {
        this.number2 = number2;
    }

    /**
     * @param number3 the number3 to set
     */
    public void setNumber3(Float number3)
    {
        this.number3 = number3;
    }

    /**
     * @param number4 the number4 to set
     */
    public void setNumber4(Integer number4)
    {
        this.number4 = number4;
    }

    /**
     * @param number5 the number5 to set
     */
    public void setNumber5(Integer number5)
    {
        this.number5 = number5;
    }

    /**
     * @param number6 the number6 to set
     */
    public void setNumber6(Integer number6)
    {
        this.number6 = number6;
    }

    /**
     * @param number7 the number7 to set
     */
    public void setNumber7(Integer number7)
    {
        this.number7 = number7;
    }

    /**
     * @param number8 the number8 to set
     */
    public void setNumber8(Integer number8)
    {
        this.number8 = number8;
    }

    /**
     * @param number9 the number9 to set
     */
    public void setNumber9(Short number9)
    {
        this.number9 = number9;
    }

    /**
     * @param preparationAttributeId the preparationAttributeId to set
     */
    public void setPreparationAttributeId(Integer preparationAttributeId)
    {
        this.preparationAttributeId = preparationAttributeId;
    }

    /**
     * @param preparationType the preparationType to set
     */
    public void setText22(String text22)
    {
        this.text22 = text22;
    }

    /**
     * @param attrDate the attrDate to set
     */
    public void setAttrDate(Date attrDate)
    {
        this.attrDate = attrDate;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /**
     * @param size the size to set
     */
    public void setText26(String text26)
    {
        this.text26 = text26;
    }

    /**
     * @param storageInfo the text25 to set
     */
    public void setText25(String text25)
    {
        this.text25 = text25;
    }

    /**
     * @param text1 the text1 to set
     */
    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     * @param text10 the text10 to set
     */
    public void setText10(String text10)
    {
        this.text10 = text10;
    }

    /**
     * @param text11 the text11 to set
     */
    public void setText11(String text11)
    {
        this.text11 = text11;
    }

    /**
     * @param text12 the text12 to set
     */
    public void setText12(String text12)
    {
        this.text12 = text12;
    }

    /**
     * @param text13 the text13 to set
     */
    public void setText13(String text13)
    {
        this.text13 = text13;
    }

    /**
     * @param text14 the text14 to set
     */
    public void setText14(String text14)
    {
        this.text14 = text14;
    }

    /**
     * @param text15 the text15 to set
     */
    public void setText15(String text15)
    {
        this.text15 = text15;
    }

    /**
     * @param text16 the text16 to set
     */
    public void setText16(String text16)
    {
        this.text16 = text16;
    }

    /**
     * @param text17 the text17 to set
     */
    public void setText17(String text17)
    {
        this.text17 = text17;
    }

    /**
     * @param text18 the text18 to set
     */
    public void setText18(String text18)
    {
        this.text18 = text18;
    }

    /**
     * @param text19 the text19 to set
     */
    public void setText19(String text19)
    {
        this.text19 = text19;
    }

    /**
     * @param text2 the text2 to set
     */
    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    /**
     * @param text20 the text20 to set
     */
    public void setText20(String text20)
    {
        this.text20 = text20;
    }

    /**
     * @param text21 the text21 to set
     */
    public void setText21(String text21)
    {
        this.text21 = text21;
    }

    /**
     * @param text3 the text3 to set
     */
    public void setText3(String text3)
    {
        this.text3 = text3;
    }

    /**
     * @param text4 the text4 to set
     */
    public void setText4(String text4)
    {
        this.text4 = text4;
    }

    /**
     * @param text5 the text5 to set
     */
    public void setText5(String text5)
    {
        this.text5 = text5;
    }

    /**
     * @param text6 the text6 to set
     */
    public void setText6(String text6)
    {
        this.text6 = text6;
    }

    /**
     * @param text7 the text7 to set
     */
    public void setText7(String text7)
    {
        this.text7 = text7;
    }

    /**
     * @param text8 the text8 to set
     */
    public void setText8(String text8)
    {
        this.text8 = text8;
    }

    /**
     * @param text9 the text9 to set
     */
    public void setText9(String text9)
    {
        this.text9 = text9;
    }

    /**
     * @param yesNo1 the yesNo1 to set
     */
    public void setYesNo1(Boolean yesNo1)
    {
        this.yesNo1 = yesNo1;
    }

    /**
     * @param yesNo2 the yesNo2 to set
     */
    public void setYesNo2(Boolean yesNo2)
    {
        this.yesNo2 = yesNo2;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return PreparationAttribute.class;
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
        return 91;
    }


}

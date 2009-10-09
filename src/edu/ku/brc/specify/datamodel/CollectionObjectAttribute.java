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
@Table(name = "collectionobjectattribute")
@org.hibernate.annotations.Table(appliesTo="collectionobjectattribute", indexes =
    {   
        @Index (name="COLOBJATTRSColMemIDX", columnNames={"CollectionMemberID"})
    })
public class CollectionObjectAttribute extends CollectionMember implements Cloneable
{
    protected Integer collectionObjectAttributeId;
    protected String text10;
    protected String text11;
    protected String text12;
    protected Float  number37;
    protected Float  number38;
    protected Byte   number8;
    protected Float  number9;
    protected String text8;
    protected Float  number10;
    protected String text13;
    protected String text14;
    protected Float number11;
    protected Float number12;
    protected Float number13;
    protected Float number14;
    protected Float number15;
    protected Float number16;
    protected Float number17;
    protected Float number39;
    protected Float number40;
    protected Float number18;
    protected Float number19;
    protected Float number20;
    protected Float number21;
    protected Float number22;
    protected Float number23;
    protected Float number24;
    protected Float number25;
    protected Float number26;
    protected Float number41;
    protected Float number27;
    protected Float number28;
    protected Float number29;
    protected Short number30;
    protected Float number31;
    protected Float number32;
    protected Float number42;
    protected Float number33;
    protected Float number34; // New
    protected Float number35; // New
    protected Float number36; // New
    protected String text9;
    protected String text1;
    protected String text2;
    protected String text3;
    protected String text4;
    protected String text5;
    protected String remarks;
    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;
    protected Float number1;
    protected Float number2;
    protected Float number3;
    protected Float number4;
    protected Float number5;
    protected Float number6;
    protected Float number7;
    protected String text6;
    protected String text7;
    protected Boolean yesNo4;
    protected Boolean yesNo5;
    protected Boolean yesNo6;
    protected Boolean yesNo7;
    
    protected Set<CollectionObject> collectionObjects;
    
    // Constructors

    /** default constructor */
    public CollectionObjectAttribute()
    {
        // do nothing
    }

    /** constructor with id */
    public CollectionObjectAttribute(Integer collectionObjectAttributeId) 
    {
        this.collectionObjectAttributeId = collectionObjectAttributeId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectionObjectAttributeId = null;
        text10 = null;
        text11 = null;
        text12 = null;
        number37 = null;
        number38 = null;
        number8 = null;
        number9 = null;
        text8 = null;
        number10 = null;
        text13 = null;
        text14 = null;
        number11 = null;
        number12 = null;
        number13 = null;
        number14 = null;
        number15 = null;
        number16 = null;
        number17 = null;
        number39 = null;
        number40 = null;
        number18 = null;
        number19 = null;
        number20 = null;
        number21 = null;
        number22 = null;
        number23 = null;
        number24 = null;
        number25 = null;
        number26 = null;
        number41 = null;
        number27 = null;
        number28 = null;
        number29 = null;
        number30 = null;
        number31 = null;
        number32 = null;
        number42 = null;
        number33 = null;
        number34 = null; // New
        number35 = null; // New
        number36 = null; // New
        text9 = null;
        text1 = null;
        text2 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        remarks = null;
        yesNo1 = null;
        yesNo2 = null;
        yesNo3 = null;
        number1 = null;
        number2 = null;
        number3 = null;
        number4 = null;
        number5 = null;
        number6 = null;
        number7 = null;
        text6 = null;
        text7 = null;
        yesNo4 = null;
        yesNo5 = null;
        yesNo6 = null;
        yesNo7 = null;
        collectionObjects = new HashSet<CollectionObject>();
    }
    // End Initializer

    @Id
    @GeneratedValue
    @Column(name = "CollectionObjectAttributeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectionObjectAttributeId()
    {
        return collectionObjectAttributeId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.collectionObjectAttributeId;
    }
    
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectionObjectAttribute")
    public Set<CollectionObject> getCollectionObjects()
    {
        return collectionObjects;
    }

    @Column(name = "Text11", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText11()
    {
        return text11;
    }

    @Column(name = "Text14", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText14()
    {
        return text14;
    }

    @Column(name = "Number42", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber42()
    {
        return number42;
    }

    @Column(name = "Number38", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber38()
    {
        return number38;
    }

    @Column(name = "Number39", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber39()
    {
        return number39;
    }

    @Column(name = "Number40", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber40()
    {
        return number40;
    }

    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1()
    {
        return number1;
    }

    @Column(name = "Number10", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber10()
    {
        return number10;
    }

    @Column(name = "Number11", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber11()
    {
        return number11;
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

    @Column(name = "Number14", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber14()
    {
        return number14;
    }

    @Column(name = "Number15", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber15()
    {
        return number15;
    }

    @Column(name = "Number16", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber16()
    {
        return number16;
    }

    @Column(name = "Number17", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber17()
    {
        return number17;
    }

    @Column(name = "Number18", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber18()
    {
        return number18;
    }

    @Column(name = "Number19", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber19()
    {
        return number19;
    }

    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2()
    {
        return number2;
    }

    @Column(name = "Number20", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber20()
    {
        return number20;
    }

    @Column(name = "Number21", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber21()
    {
        return number21;
    }

    @Column(name = "Number22", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber22()
    {
        return number22;
    }

    @Column(name = "Number23", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber23()
    {
        return number23;
    }

    @Column(name = "Number24", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber24()
    {
        return number24;
    }

    @Column(name = "Number25", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber25()
    {
        return number25;
    }

    @Column(name = "Number26", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber26()
    {
        return number26;
    }

    @Column(name = "Number27", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber27()
    {
        return number27;
    }

    @Column(name = "Number28", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber28()
    {
        return number28;
    }

    @Column(name = "Number29", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber29()
    {
        return number29;
    }

    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber3()
    {
        return number3;
    }

    @Column(name = "Number30", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getNumber30()
    {
        return number30;
    }

    @Column(name = "Number31", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber31()
    {
        return number31;
    }

    @Column(name = "Number32", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber32()
    {
        return number32;
    }

    @Column(name = "Number33", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber33()
    {
        return number33;
    }

    @Column(name = "Number34", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber34()
    {
        return number34;
    }

    @Column(name = "Number35", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber35()
    {
        return number35;
    }

    @Column(name = "Number36", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber36()
    {
        return number36;
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
    public Byte getNumber8()
    {
        return number8;
    }

    @Column(name = "Number9", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber9()
    {
        return number9;
    }

    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return remarks;
    }

    @Column(name = "Text13", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText13()
    {
        return text13;
    }

    @Column(name = "Text10", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText10()
    {
        return text10;
    }

    @Column(name = "Text12", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText12()
    {
        return text12;
    }

    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
    public String getText1()
    {
        return text1;
    }

    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
    public String getText2()
    {
        return text2;
    }

    @Column(name = "Text3", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
    public String getText3()
    {
        return text3;
    }

    @Column(name = "Text4", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText4()
    {
        return text4;
    }

    @Column(name = "Text5", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText5()
    {
        return text5;
    }

    @Column(name = "Text6", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
    public String getText6()
    {
        return text6;
    }

    @Column(name = "Text7", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
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

    @Column(name = "Number37", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber37()
    {
        return number37;
    }

    @Column(name = "Number41", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber41()
    {
        return number41;
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

    @Column(name = "YesNo6", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo6()
    {
        return yesNo6;
    }

    @Column(name = "YesNo7", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo7()
    {
        return yesNo7;
    }

    public void setText11(String text11)
    {
        this.text11 = text11;
    }

    public void setCollectionObjects(Set<CollectionObject> collectionObjects)
    {
        this.collectionObjects = collectionObjects;
    }

    public void setCollectionObjectAttributeId(Integer collectionObjectAttributeId)
    {
        this.collectionObjectAttributeId = collectionObjectAttributeId;
    }

    public void setText14(String text14)
    {
        this.text14 = text14;
    }

    public void setNumber42(Float number42)
    {
        this.number42 = number42;
    }

    public void setNumber38(Float number38)
    {
        this.number38 = number38;
    }

    public void setNumber39(Float number39)
    {
        this.number39 = number39;
    }

    public void setNumber40(Float number40)
    {
        this.number40 = number40;
    }

    public void setNumber1(Float number1)
    {
        this.number1 = number1;
    }

    public void setNumber10(Float number10)
    {
        this.number10 = number10;
    }

    public void setNumber11(Float number11)
    {
        this.number11 = number11;
    }

    public void setNumber12(Float number12)
    {
        this.number12 = number12;
    }

    public void setNumber13(Float number13)
    {
        this.number13 = number13;
    }

    public void setNumber14(Float number14)
    {
        this.number14 = number14;
    }

    public void setNumber15(Float number15)
    {
        this.number15 = number15;
    }

    public void setNumber16(Float number16)
    {
        this.number16 = number16;
    }

    public void setNumber17(Float number17)
    {
        this.number17 = number17;
    }

    public void setNumber18(Float number18)
    {
        this.number18 = number18;
    }

    public void setNumber19(Float number19)
    {
        this.number19 = number19;
    }

    public void setNumber2(Float number2)
    {
        this.number2 = number2;
    }

    public void setNumber20(Float number20)
    {
        this.number20 = number20;
    }

    public void setNumber21(Float number21)
    {
        this.number21 = number21;
    }

    public void setNumber22(Float number22)
    {
        this.number22 = number22;
    }

    public void setNumber23(Float number23)
    {
        this.number23 = number23;
    }

    public void setNumber24(Float number24)
    {
        this.number24 = number24;
    }

    public void setNumber25(Float number25)
    {
        this.number25 = number25;
    }

    public void setNumber26(Float number26)
    {
        this.number26 = number26;
    }

    public void setNumber27(Float number27)
    {
        this.number27 = number27;
    }

    public void setNumber28(Float number28)
    {
        this.number28 = number28;
    }

    public void setNumber29(Float number29)
    {
        this.number29 = number29;
    }

    public void setNumber3(Float number3)
    {
        this.number3 = number3;
    }

    public void setNumber30(Short number30)
    {
        this.number30 = number30;
    }

    public void setNumber31(Float number31)
    {
        this.number31 = number31;
    }

    public void setNumber32(Float number32)
    {
        this.number32 = number32;
    }

    public void setNumber33(Float number33)
    {
        this.number33 = number33;
    }

    public void setNumber34(Float number34)
    {
        this.number34 = number34;
    }

    public void setNumber35(Float number35)
    {
        this.number35 = number35;
    }

    public void setNumber36(Float number36)
    {
        this.number36 = number36;
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

    public void setNumber8(Byte number8)
    {
        this.number8 = number8;
    }

    public void setNumber9(Float number9)
    {
        this.number9 = number9;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public void setText13(String text13)
    {
        this.text13 = text13;
    }

    public void setText10(String text10)
    {
        this.text10 = text10;
    }

    public void setText12(String text12)
    {
        this.text12 = text12;
    }

    public void setText1(String text1)
    {
        this.text1 = text1;
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

    public void setNumber37(Float number37)
    {
        this.number37 = number37;
    }

    public void setNumber41(Float number41)
    {
        this.number41 = number41;
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

    public void setYesNo6(Boolean yesNo6)
    {
        this.yesNo6 = yesNo6;
    }

    public void setYesNo7(Boolean yesNo7)
    {
        this.yesNo7 = yesNo7;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        CollectionObjectAttribute obj    = (CollectionObjectAttribute)super.clone();
        obj.collectionObjectAttributeId  = null;
        obj.collectionObjects            = new HashSet<CollectionObject>();
        
        return obj;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return CollectionObject.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        if (collectionObjects != null && collectionObjects.size() == 1)
        {
            return ((CollectionObject)collectionObjects.toArray()[0]).getId();
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
        return CollectionObjectAttribute.class;
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
        return 93;
    }
}

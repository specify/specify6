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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.persistence.*;

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
@Table(name = "collectionobjectattribute")
@org.hibernate.annotations.Table(appliesTo="collectionobjectattribute", indexes =
    {   
        @Index (name="COLOBJATTRSColMemIDX", columnNames={"CollectionMemberID"})
    })
public class CollectionObjectAttribute extends CollectionMember implements Cloneable
{
    protected Integer collectionObjectAttributeId;

    protected Agent agent1;
    protected Calendar date1;
    protected Byte date1Precision;

    protected Float number1;
    protected Float number2;
    protected Float number3;
    protected Float number4;
    protected Float number5;
    protected Float number6;
    protected Float number7;
    protected Integer number8;
    protected Float  number9;
    protected Float  number10;
    protected Float number11;
    protected Float number12;
    protected Float number13;
    protected Float number14;
    protected Float number15;
    protected Float number16;
    protected Float number17;
    protected Float number18;
    protected Float number19;
    protected Float number20;
    protected Float number21;
    protected Float number22;
    protected Float number23;
    protected Float number24;
    protected Float number25;
    protected Float number26;
    protected Float number27;
    protected Float number28;
    protected Float number29;
    protected Integer number30;
    protected Float number31;
    protected Float number32;
    protected Float number42;
    protected Float number33;
    protected Float number34; // New
    protected Float number35; // New
    protected Float number36; // New
    protected Float number37;
    protected Float number38;
    protected Float number39;
    protected Float number40;
    protected Float number41;
    
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
    
    protected String remarks;
    
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
    
    protected Float   topDistance;
    protected Float   bottomDistance;
    protected String  distanceUnits; // "ft" or "m"
    protected String  direction;     // "up" or "down"
    protected String  positionState; // float or in-situ

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
        agent1 = null;
        date1 = null;
        date1Precision = 1;
        text10   = null;
        text11   = null;
        text12   = null;
        number37 = null;
        number38 = null;
        number8  = null;
        number9  = null;
        text8    = null;
        number10 = null;
        text13   = null;
        text14   = null;
        text15   = null;
        text16   = null;
        text17   = null;
        text18   = null;
        text19   = null;
        text20   = null;
        text21   = null;
        text22   = null;
        text23   = null;
        text24   = null;
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
 
        text25 = null;
        text26 = null;
        text27 = null;
        text28 = null;
        text29 = null;
        text30 = null;

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

        topDistance    = null;
        bottomDistance = null;
        distanceUnits  = null;
        direction      = null;
        positionState  = null;
        

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

    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "Date1", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate1() {
        return date1;
    }

    /**
     *
     * @param date1
     */
    public void setDate1(Calendar date1) {
        this.date1 = date1;
    }

    /**
     *
     * @return
     */
    @Column(name = "Date1Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate1Precision() {
        return date1Precision;
    }

    /**
     *
     * @param date1Precision
     */
    public void setDate1Precision(Byte date1Precision) {
        this.date1Precision = date1Precision;
    }

    /**
     *
     * @return
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent1ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent1() {
        return agent1;
    }

    /**
     *
     * @param agent1
     */
    public void setAgent1(Agent agent1) {
        this.agent1 = agent1;
    }

    /**
	 * @return the text25
	 */
    @Lob
    @Column(name = "Text25", length = 65535)
	public String getText25() {
		return text25;
	}

	/**
	 * @param text25 the text25 to set
	 */
	public void setText25(String text25) {
		this.text25 = text25;
	}

	/**
	 * @return the text26
	 */
    @Lob
    @Column(name = "Text26", length = 65535)
	public String getText26() {
		return text26;
	}

	/**
	 * @param text26 the text26 to set
	 */
	public void setText26(String text26) {
		this.text26 = text26;
	}

	/**
	 * @return the text27
	 */
    @Lob
    @Column(name = "Text27", length = 65535)
	public String getText27() {
		return text27;
	}

	/**
	 * @param text27 the text27 to set
	 */
	public void setText27(String text27) {
		this.text27 = text27;
	}

	/**
	 * @return the text28
	 */
    @Lob
    @Column(name = "Text28", length = 65535)
	public String getText28() {
		return text28;
	}

	/**
	 * @param text28 the text28 to set
	 */
	public void setText28(String text28) {
		this.text28 = text28;
	}

	/**
	 * @return the text29
	 */
    @Lob
    @Column(name = "Text29", length = 65535)
	public String getText29() {
		return text29;
	}

	/**
	 * @param text29 the text29 to set
	 */
	public void setText29(String text29) {
		this.text29 = text29;
	}

	/**
	 * @return the text30
	 */
    @Lob
    @Column(name = "Text30", length = 65535)
	public String getText30() {
		return text30;
	}

	/**
	 * @param text30 the text30 to set
	 */
	public void setText30(String text30) {
		this.text30 = text30;
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

    @Column(name = "Text15", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getText15()
    {
        return text15;
    }

    
    /**
	 * @return the text19
	 */
    @Lob
    @Column(name = "Text19", length = 65535)
	public String getText19() {
		return text19;
	}

	/**
	 * @param text19 the text19 to set
	 */
	public void setText19(String text19) {
		this.text19 = text19;
	}

	/**
	 * @return the text20
	 */
    @Lob
    @Column(name = "Text20", length = 65535)
	public String getText20() {
		return text20;
	}

	/**
	 * @param text20 the text20 to set
	 */
	public void setText20(String text20) {
		this.text20 = text20;
	}

	/**
	 * @return the text21
	 */
    @Lob
    @Column(name = "Text21", length = 65535)
	public String getText21() {
		return text21;
	}

	/**
	 * @param text21 the text21 to set
	 */
	public void setText21(String text21) {
		this.text21 = text21;
	}

	/**
	 * @return the text22
	 */
    @Lob
    @Column(name = "Text22", length = 65535)
	public String getText22() {
		return text22;
	}

	/**
	 * @param text22 the text22 to set
	 */
	public void setText22(String text22) {
		this.text22 = text22;
	}

	/**
	 * @return the text23
	 */
    @Lob
    @Column(name = "Text23", length = 65535)
	public String getText23() {
		return text23;
	}

	/**
	 * @param text23 the text23 to set
	 */
	public void setText23(String text23) {
		this.text23 = text23;
	}

	/**
	 * @return the text24
	 */
    @Lob
    @Column(name = "Text24", length = 65535)
	public String getText24() {
		return text24;
	}

	/**
	 * @param text24 the text24 to set
	 */
	public void setText24(String text24) {
		this.text24 = text24;
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
    public Integer getNumber30()
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
    public Integer getNumber8()
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

    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1()
    {
        return text1;
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

    @Lob
    @Column(name = "Text16", length = 65535)
    public String getText16()
    {
        return text16;
    }

    @Lob
    @Column(name = "Text17", length = 65535)
    public String getText17()
    {
        return text17;
    }

    @Lob
    @Column(name = "Text18", length = 65535)
    public String getText18()
    {
        return text18;
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

    public void setText15(String text15)
    {
        this.text15 = text15;
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

    public void setNumber30(Integer number30)
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

    public void setNumber8(Integer number8)
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

    public void setText16(String text16)
    {
        this.text16 = text16;
    }

    public void setText17(String text17)
    {
        this.text17 = text17;
    }

    public void setText18(String text18)
    {
        this.text18 = text18;
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
    
    
    /**
     * @return the direction
     */
    @Column(name="Direction", unique=false, nullable=true, insertable=true, updatable=true, length=32)
    public String getDirection()
    {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(String direction)
    {
        this.direction = direction;
    }

    /**
     * @return the distanceUnits
     */
    @Column(name="DistanceUnits", unique=false, nullable=true, insertable=true, updatable=true, length=16)
    public String getDistanceUnits()
    {
        return distanceUnits;
    }

    /**
     * @param distanceUnits the distanceUnits to set
     */
    public void setDistanceUnits(String distanceUnits)
    {
        this.distanceUnits = distanceUnits;
    }

    /**
     * @return the topDistance
     */
    @Column(name="TopDistance", unique=false, nullable=true, insertable=true, updatable=true)
    public Float getTopDistance()
    {
        return topDistance;
    }

    /**
     * @param topDistance the topDistance to set
     */
    public void setTopDistance(Float topDistance)
    {
        this.topDistance = topDistance;
    }

    /**
     * @return the bottomDistance
     */
    @Column(name="BottomDistance", unique=false, nullable=true, insertable=true, updatable=true)
    public Float getBottomDistance()
    {
        return bottomDistance;
    }

    /**
     * @param bottomDistance the bottomDistance to set
     */
    public void setBottomDistance(Float bottomDistance)
    {
        this.bottomDistance = bottomDistance;
    }

    /**
     * @return the positionState
     */
    @Column(name="PositionState", unique=false, nullable=true, insertable=true, updatable=true, length=32)
    public String getPositionState()
    {
        return positionState;
    }

    /**
     * @param positionState the positionState to set
     */
    public void setPositionState(String positionState)
    {
        this.positionState = positionState;
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
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT CollectionObjectID FROM collectionobject WHERE CollectionObjectAttributeID = "+ collectionObjectAttributeId);
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

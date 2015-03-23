/* Copyright (C) 2015, University of Kansas Center for Research
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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 15, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name="paleocontext")
@org.hibernate.annotations.Table(appliesTo="paleocontext", indexes =
    {   @Index (name="PaleoCxtNameIDX", columnNames={"PaleoContextName"}),
		@Index (name="PaleoCxtDisciplineIDX", columnNames={"DisciplineID"})
    })
public class PaleoContext extends DisciplineMember implements Cloneable
{
    protected Integer paleoContextId;
    
    protected String paleoContextName;
    
    
    protected String  remarks;
    
    protected String  text1;
    protected String  text2;
    protected String  text3;
    protected String  text4;
    protected String  text5;
    
    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;
    protected Boolean yesNo4;
    protected Boolean yesNo5;
    
    protected Double number1;
    protected Double number2;
    protected Double number3;
    protected Double number4;
    protected Double number5;
    
    protected Set<CollectionObject> collectionObjects;
    protected Set<CollectingEvent> collectingEvents;
    protected Set<Locality> localities;
    
    protected LithoStrat            lithoStrat;
    protected GeologicTimePeriod    bioStrat;
    protected GeologicTimePeriod    chronosStrat;
    protected GeologicTimePeriod    chronosStratEnd;
    
    /**
     * Constructor.
     */
    public PaleoContext()
    {
        super();
    }
    
    /**
     * @return the paleoContextId
     */
    @Id
    @GeneratedValue
    @Column(name="PaleoContextID", unique=false, nullable=false, insertable=true, updatable=true)
    public Integer getPaleoContextId()
    {
        return paleoContextId;
    }

    /**
     * @param paleoContextId the paleoContextId to set
     */
    public void setPaleoContextId(Integer paleoContextId)
    {
        this.paleoContextId = paleoContextId;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        paleoContextId = null;
        paleoContextName = null;
        
        remarks        = null;
        text1          = null;
        text2          = null;
        text3	       = null;
        text4		   = null;
        text5          = null;
        yesNo1         = null;
        yesNo2         = null;
        yesNo3         = null;
        yesNo4         = null;
        yesNo5         = null;
        number1        = null;
        number2        = null;
        number3        = null;
        number4        = null;
        number5        = null;
        
        collectionObjects = new HashSet<CollectionObject>();
        collectingEvents = new HashSet<CollectingEvent>();
        localities = new HashSet<Locality>();
        
        lithoStrat       = null;
        bioStrat         = null;
        chronosStrat     = null;
        chronosStratEnd  = null;

    }

    /**
     * @return the paleoContextName
     */
    @Column(name="PaleoContextName", unique=false, nullable=true, insertable=true, updatable=true, length=80)
    public String getPaleoContextName() {
        return paleoContextName;
    }

    /**
     * @param paleoContextName the paleoContextName to set
     */
    public void setPaleoContextName(String paleoContextName) {
    	this.paleoContextName = paleoContextName;
    }
    

    /**
     * @param collectionObjects
     */
    public void setCollectionObjects(Set<CollectionObject> collectionObjects) 
    {
        this.collectionObjects = collectionObjects;
    }
 
    /**
     * @param collectingEvents
     */
    public void setCollectingEvents(Set<CollectingEvent> collectingEvents) 
    {
        this.collectingEvents = collectingEvents;
    }
    
    /**
     * @param collectionObjects
     */
    public void setLocalities(Set<Locality> localities) 
    {
        this.localities = localities;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * @return the text1
     */
    @Column(name="Text1", unique=false, nullable=true, insertable=true, updatable=true, length=64)
    public String getText1()
    {
        return text1;
    }

    /**
     * @param text1 the text1 to set
     */
    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     * @return the text2
     */
    @Column(name="Text2", unique=false, nullable=true, insertable=true, updatable=true, length=64)
    public String getText2()
    {
        return text2;
    }

    /**
     * @param text2 the text2 to set
     */
    public void setText2(String text2)
    {
        this.text2 = text2;
    }
    
    /**
     * @return the text3
     */
    @Column(name="Text3", unique=false, nullable=true, insertable=true, updatable=true, length=500)
    public String getText3()
    {
        return text3;
    }

    /**
     * @param text3 the text3 to set
     */
    public void setText3(String text3)
    {
        this.text3 = text3;
    }
    /**
     * @return the text4
     */
    @Column(name="Text4", unique=false, nullable=true, insertable=true, updatable=true, length=500)
    public String getText4()
    {
        return text4;
    }

    /**
     * @param text4 the text4 to set
     */
    public void setText4(String text4)
    {
        this.text4 = text4;
    }
    /**
     * @return the text1
     */
    @Column(name="Text5", unique=false, nullable=true, insertable=true, updatable=true, length=500)
    public String getText5()
    {
        return text5;
    }

    /**
     * @param text5 the text5 to set
     */
    public void setText5(String text5)
    {
        this.text5 = text5;
    }


    /**
     * @return the yesNo1
     */
    @Column(name="YesNo1", unique=false, nullable=true, insertable=true, updatable=true)
    public Boolean getYesNo1()
    {
        return yesNo1;
    }

    /**
     * @param yesNo1 the yesNo1 to set
     */
    public void setYesNo1(Boolean yesNo1)
    {
        this.yesNo1 = yesNo1;
    }

    /**
     * @return the yesNo2
     */
    @Column(name="YesNo2", unique=false, nullable=true, insertable=true, updatable=true)
    public Boolean getYesNo2()
    {
        return yesNo2;
    }

    /**
     * @param yesNo2 the yesNo2 to set
     */
    public void setYesNo2(Boolean yesNo2)
    {
        this.yesNo2 = yesNo2;
    }
   
    /**
     * @return the yesNo3
     */
    @Column(name="YesNo3", unique=false, nullable=true, insertable=true, updatable=true)
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
    @Column(name="YesNo4", unique=false, nullable=true, insertable=true, updatable=true)
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
     * @return the yesNo5
     */
    @Column(name="YesNo5", unique=false, nullable=true, insertable=true, updatable=true)
    public Boolean getYesNo5()
    {
        return yesNo5;
    }

    /**
     * @param yesNo5 the yesNo5 to set
     */
    public void setYesNo5(Boolean yesNo5)
    {
        this.yesNo5 = yesNo5;
    }

    
    
    /**
	 * @return the number1
	 */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
	public Double getNumber1() {
		return number1;
	}

	/**
	 * @param number1 the number1 to set
	 */
	public void setNumber1(Double number1) {
		this.number1 = number1;
	}

	/**
	 * @return the number2
	 */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
	public Double getNumber2() {
		return number2;
	}

	/**
	 * @param number2 the number2 to set
	 */
	public void setNumber2(Double number2) {
		this.number2 = number2;
	}

	/**
	 * @return the number3
	 */
    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
	public Double getNumber3() {
		return number3;
	}

	/**
	 * @param number3 the number3 to set
	 */
	public void setNumber3(Double number3) {
		this.number3 = number3;
	}

	/**
	 * @return the number4
	 */
    @Column(name = "Number4", unique = false, nullable = true, insertable = true, updatable = true)
	public Double getNumber4() {
		return number4;
	}

	/**
	 * @param number4 the number4 to set
	 */
	public void setNumber4(Double number4) {
		this.number4 = number4;
	}

	/**
	 * @return the number5
	 */
    @Column(name = "Number5", unique = false, nullable = true, insertable = true, updatable = true)
	public Double getNumber5() {
		return number5;
	}

	/**
	 * @param number5 the number5 to set
	 */
	public void setNumber5(Double number5) {
		this.number5 = number5;
	}

	/**
     *
     */
    //@OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="paleoContext")
    
    @OneToMany(mappedBy = "paleoContext")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )

    public Set<CollectionObject> getCollectionObjects() 
    {
        return this.collectionObjects;
    }

    /**
     * @return
     */
    //@OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="paleoContext")
    @OneToMany(mappedBy = "paleoContext")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<CollectingEvent> getCollectingEvents() 
    {
        return this.collectingEvents;
    }

    /**
     * @return
     */
    //@OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="paleoContext")
    @OneToMany(mappedBy = "paleoContext")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Locality> getLocalities() 
    {
        return this.localities;
    }

    /**
     * @return the bioStrat
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name="BioStratID", unique=false, nullable=true, insertable=true, updatable=true)
    public GeologicTimePeriod getBioStrat()
    {
        return bioStrat;
    }

    /**
     * @param bioStrat the bioStrat to set
     */
    public void setBioStrat(GeologicTimePeriod bioStrat)
    {
        this.bioStrat = bioStrat;
    }

    /**
     * @return the chronosStrat
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name="ChronosStratID", unique=false, nullable=true, insertable=true, updatable=true)
    public GeologicTimePeriod getChronosStrat()
    {
        return chronosStrat;
    }

    /**
     * @param chronosStrat the chronosStrat to set
     */
    public void setChronosStrat(GeologicTimePeriod chronosStrat)
    {
        this.chronosStrat = chronosStrat;
    }

    /**
     * @return the chronosStratEnd
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name="ChronosStratEndID", unique=false, nullable=true, insertable=true, updatable=true)
    public GeologicTimePeriod getChronosStratEnd()
    {
        return chronosStratEnd;
    }

    /**
     * @param chronosStratEnd the chronosStratEnd to set
     */
    public void setChronosStratEnd(GeologicTimePeriod chronosStratEnd)
    {
        this.chronosStratEnd = chronosStratEnd;
    }

    /**
     * @return the lithoStrat
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name="LithoStratID", unique=false, nullable=true, insertable=true, updatable=true)
    public LithoStrat getLithoStrat()
    {
        return lithoStrat;
    }

    /**
     * @param lithoStrat the lithoStrat to set
     */
    public void setLithoStrat(LithoStrat lithoStrat)
    {
        this.lithoStrat = lithoStrat;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return PaleoContext.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return paleoContextId;
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
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT CollectionObjectID FROM collectionobject WHERE PaleoContextID = "+ paleoContextId);
        if (ids.size() == 1)
        {
            return (Integer)ids.get(0);
        }
        return null;
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
        return 32;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        PaleoContext pc = (PaleoContext)super.clone();
        
        pc.paleoContextId    = null;
        pc.collectionObjects = new HashSet<CollectionObject>();
        
        return pc;
    }

}

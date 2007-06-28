/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

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

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "stratigraphy")
public class Stratigraphy extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Long stratigraphyId;
     protected String superGroup;
     protected String lithoGroup;
     protected String formation;
     protected String member;
     protected String bed;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<CollectingEvent> collectingEvents;
     protected GeologicTimePeriod geologicTimePeriod;
     protected Set<GeologicTimePeriod> children;


    // Constructors

    /** default constructor */
    public Stratigraphy() {
        //
    }

    /** constructor with id */
    public Stratigraphy(Long stratigraphyId) {
        this.stratigraphyId = stratigraphyId;
    }




    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        stratigraphyId = null;
        superGroup = null;
        lithoGroup = null;
        formation = null;
        member = null;
        bed = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        collectingEvents = null;
        geologicTimePeriod = null;
        children = new HashSet<GeologicTimePeriod>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * CollectingEventID of related collecting event
     */
    @Id
    @GeneratedValue
    @Column(name = "StratigraphyID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getStratigraphyId() {
        return this.stratigraphyId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.stratigraphyId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Stratigraphy.class;
    }

    public void setStratigraphyId(Long stratigraphyId) {
        this.stratigraphyId = stratigraphyId;
    }

    /**
     *      * Lithostratigraphic supergroup
     */
    @Column(name = "SuperGroup", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getSuperGroup() {
        return this.superGroup;
    }

    public void setSuperGroup(String superGroup) {
        this.superGroup = superGroup;
    }

    /**
     *
     */
    @Column(name = "LithoGroup", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLithoGroup() {
        return this.lithoGroup;
    }

    public void setLithoGroup(String lithoGroup) {
        this.lithoGroup = lithoGroup;
    }

    /**
     *      * Lithostratigraphic formation
     */
    @Column(name = "Formation", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getFormation() {
        return this.formation;
    }

    public void setFormation(String formation) {
        this.formation = formation;
    }

    /**
     *      * Lithostratigraphic member
     */
    @Column(name = "Member", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getMember() {
        return this.member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    /**
     *      * Lithostratigraphic bed
     */
    @Column(name = "Bed", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getBed() {
        return this.bed;
    }

    public void setBed(String bed) {
        this.bed = bed;
    }

    /**
     *
     */
    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text1", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber1() {
        return this.number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber2() {
        return this.number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "stratigraphy")
    public Set<CollectingEvent> getCollectingEvents() {
        return this.collectingEvents;
    }

    public void setCollectingEvents(Set<CollectingEvent> collectingEvents) {
        this.collectingEvents = collectingEvents;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "GeologicTimePeriodID", unique = false, nullable = true, insertable = true, updatable = true)
    public GeologicTimePeriod getGeologicTimePeriod()
	{
		return geologicTimePeriod;
	}

	public void setGeologicTimePeriod(GeologicTimePeriod geologicTimePeriod)
	{
		this.geologicTimePeriod = geologicTimePeriod;
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
        return 73;
    }

}

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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class Stratigraphy  implements java.io.Serializable {

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
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected CollectingEvent collectingEvent;
     protected GeologicTimePeriod geologicTimePeriod;
     protected Set<GeologicTimePeriod> children;


    // Constructors

    /** default constructor */
    public Stratigraphy() {
    }

    /** constructor with id */
    public Stratigraphy(Long stratigraphyId) {
        this.stratigraphyId = stratigraphyId;
    }




    // Initializer
    public void initialize()
    {
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
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        yesNo1 = null;
        yesNo2 = null;
        collectingEvent = null;
        geologicTimePeriod = null;
        children = new HashSet<GeologicTimePeriod>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * CollectingEventID of related collecting event
     */
    public Long getStratigraphyId() {
        return this.stratigraphyId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.stratigraphyId;
    }

    public void setStratigraphyId(Long stratigraphyId) {
        this.stratigraphyId = stratigraphyId;
    }

    /**
     *      * Lithostratigraphic supergroup
     */
    public String getSuperGroup() {
        return this.superGroup;
    }

    public void setSuperGroup(String superGroup) {
        this.superGroup = superGroup;
    }

    /**
     *
     */
    public String getLithoGroup() {
        return this.lithoGroup;
    }

    public void setLithoGroup(String lithoGroup) {
        this.lithoGroup = lithoGroup;
    }

    /**
     *      * Lithostratigraphic formation
     */
    public String getFormation() {
        return this.formation;
    }

    public void setFormation(String formation) {
        this.formation = formation;
    }

    /**
     *      * Lithostratigraphic member
     */
    public String getMember() {
        return this.member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    /**
     *      * Lithostratigraphic bed
     */
    public String getBed() {
        return this.bed;
    }

    public void setBed(String bed) {
        this.bed = bed;
    }

    /**
     *
     */
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    public Float getNumber1() {
        return this.number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    public Float getNumber2() {
        return this.number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }

    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *
     */
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }

    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    public GeologicTimePeriod getGeologicTimePeriod()
	{
		return geologicTimePeriod;
	}

	public void setGeologicTimePeriod(GeologicTimePeriod geologicTimePeriod)
	{
		this.geologicTimePeriod = geologicTimePeriod;
	}

	/**
     *
     */
    public Set<GeologicTimePeriod> getChildren() {
        return this.children;
    }

    public void setChildren(Set<GeologicTimePeriod> children) {
        this.children = children;
    }





    // Add Methods

    public void addChildren(final GeologicTimePeriod children)
    {
        this.children.add(children);
        //children.set(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeChildren(final GeologicTimePeriod children)
    {
        this.children.remove(children);
        //children.setStratigraphy(null);
    }

    // Delete Add Methods
}

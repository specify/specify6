/* Copyright (C) 2023, Specify Collections Consortium
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

import java.util.Calendar;

import javax.persistence.*;

import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "agentidentifier")
public class AgentIdentifier extends DataModelObjBase implements java.io.Serializable {

    // Fields

    protected Integer agentIdentifierId;
    protected String identifier;
    protected String identifierType;
    protected String remarks;
    protected Agent agent;

    protected String text1;
    protected String text2;
    protected String text3;
    protected String text4;
    protected String text5;
    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;
    protected Boolean yesNo4;
    protected Boolean yesNo5;
    protected Calendar date1;
    protected Byte date1Precision;
    protected Calendar date2;
    protected Byte date2Precision;


    // Constructors

    /** default constructor */
    public AgentIdentifier()
    {
        //
    }

    /** constructor with id */
    public AgentIdentifier(Integer agentIdentifierId) {
        this.agentIdentifierId = agentIdentifierId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        agentIdentifierId = null;
        identifier = null;
        identifierType = null;
        remarks = null;
        agent = null;
        text1 = null;
        text2 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        yesNo1 = null;
        yesNo2 = null;
        yesNo3 = null;
        yesNo4 = null;
        yesNo5 = null;
        date1 = null;
        date1Precision = 1;
        date2 = null;
        date2Precision = 1;
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "AgentIdentifierID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAgentIdentifierId() {
        return this.agentIdentifierId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.agentIdentifierId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return AgentIdentifier.class;
    }

    public void setAgentIdentifierId(Integer agentIdentifierId) {
        this.agentIdentifierId = agentIdentifierId;
    }

    /**
     *
     */
    @Column(name = "Identifier", unique = false, nullable = false, insertable = true, updatable = true, length = 2048)
    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
     *      * User definable
     */
    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text3", length = 65535)
    public String getText3() {
        return this.text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text4", length = 65535)
    public String getText4() {
        return this.text4;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text5", length = 65535)
    public String getText5() {
        return this.text5;
    }

    public void setText5(String text5) {
        this.text5 = text5;
    }
    /**
     * @return the yesNo1
     */
    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1() {
        return yesNo1;
    }

    /**
     * @param yesNo1 the yesNo1 to set
     */
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     * @return the yesNo2
     */
    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2() {
        return yesNo2;
    }

    /**
     * @param yesNo2 the yesNo2 to set
     */
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * @return the yesNo3
     */
    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3() {
        return yesNo3;
    }

    /**
     * @param yesNo3 the yesNo3 to set
     */
    public void setYesNo3(Boolean yesNo3) {
        this.yesNo3 = yesNo3;
    }

    /**
     * @return the yesNo4
     */
    @Column(name = "YesNo4", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo4() {
        return yesNo4;
    }

    /**
     * @param yesNo4 the yesNo4 to set
     */
    public void setYesNo4(Boolean yesNo4) {
        this.yesNo4 = yesNo4;
    }

    /**
     * @return the yesNo5
     */
    @Column(name = "YesNo5", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo5() {
        return yesNo5;
    }

    /**
     * @param yesNo5 the yesNo5 to set
     */
    public void setYesNo5(Boolean yesNo5) {
        this.yesNo5 = yesNo5;
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
    @Temporal(TemporalType.DATE)
    @Column(name = "Date2", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate2() {
        return date2;
    }

    /**
     *
     * @param date2
     */
    public void setDate2(Calendar date2) {
        date2 = date2;
    }

    /**
     *
     * @return
     */
    @Column(name = "Date2Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate2Precision() {
        return date2Precision;
    }

    /**
     *
     * @param date2Precision
     */
    public void setDate2Precision(Byte date2Precision) {
        date2Precision = date2Precision;
    }

    /**
     *      * ID of agent identified by Identifier
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgent() {
        return this.agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Agent.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return agent != null ? agent.getId() : null;
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
        return 168;
    }

    /**
     * @return the identifierType
     */
    @Column(name = "IdentifierType", unique = false, insertable = true, updatable = true, length = 256)
    public String getIdentifierType()
    {
        return this.identifierType;
    }

    /**
     * @param identifierType the identifierType to set
     */
    public void setIdentifierType(String identifierType)
    {
        this.identifierType = identifierType;
    }

}

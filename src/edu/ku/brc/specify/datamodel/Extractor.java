/* Copyright (C) 2019, University of Kansas Center for Research
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.util.Orderable;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "extractor", uniqueConstraints = { @UniqueConstraint(columnNames = {"AgentID", "DNASequenceID"}) })
public class Extractor extends DataModelObjBase implements java.io.Serializable,
        Orderable,
        Comparable<Extractor>,
        Cloneable
{

    // Fields

    protected Integer         extractorId;
    protected Integer         orderNumber;
    protected String          remarks;
    protected String text1;
    protected String text2;
    protected Boolean yesNo1;
    protected Boolean yesNo2;

    protected DNASequence dnaSequence;
    protected Agent           agent;

    // Constructors

    /** default constructor */
    public Extractor()
    {
        //
    }

    /** constructor with id */
    public Extractor(Integer extractorId)
    {
        this.extractorId = extractorId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        extractorId     = null;
        orderNumber     = null;
        remarks         = null;
        text1 = null;
        text2 = null;
        yesNo1 = null;
        yesNo2 = null;
        dnaSequence = null;
        agent           = null;
    }
    // End Initializer

    // Property accessors
    @Lob
    @Column(name = "Text1", length = 4096)
    public String getText1()
    {
        return text1;
    }

    @Lob
    @Column(name = "Text2", length = 4096)
    public String getText2()
    {
        return text2;
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

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "ExtractorID")
    public Integer getExtractorId()
    {
        return this.extractorId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.extractorId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Extractor.class;
    }

    public void setExtractorId(Integer extractorId) {
        this.extractorId = extractorId;
    }

    /**
     *
     */
    @Column(name = "OrderNumber", nullable = false)
    public Integer getOrderNumber() {
        return this.orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
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
     *      * The DNAsequence the agent participated in
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DNASequenceID", nullable = false)
    public DNASequence getDnaSequence()
    {
        return this.dnaSequence;
    }

    public void setDnaSequence(DNASequence dnaSequence)
    {
        this.dnaSequence = dnaSequence;
    }

    /**
     *      * Link to Extractor's record in Agent table
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "AgentID", nullable = false)
    public Agent getAgent()
    {
        return this.agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        String name = "";
        if (agent != null)
        {
            name = agent.getIdentityTitle();
        }

        if (StringUtils.isNotEmpty(name))
        {
            return name;
        }

        return super.getIdentityTitle();
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
        return 160;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#getOrderIndex()
     */
    @Transient
    public int getOrderIndex()
    {
        return getOrderNumber();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#setOrderIndex(int)
     */
    public void setOrderIndex(int order)
    {
        setOrderNumber(order);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return DNASequence.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return dnaSequence != null ? dnaSequence.getId() : null;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Extractor obj)
    {
        return orderNumber != null && obj != null && obj.orderNumber != null ? orderNumber.compareTo(obj.orderNumber) : 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Extractor obj = (Extractor)super.clone();
        obj.setExtractorId(null);
        return obj;
    }

}

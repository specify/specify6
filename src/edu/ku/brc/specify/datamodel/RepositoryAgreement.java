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

import java.util.Calendar;
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "repositoryagreement")
@org.hibernate.annotations.Table(appliesTo="repositoryagreement", indexes =
    {   @Index (name="RefWrkNumberIDX", columnNames={"RepositoryAgreementNumber"}),
        @Index (name="RefWrkStartDate", columnNames={"StartDate"})
    })
public class RepositoryAgreement extends DataModelObjBase implements AttachmentOwnerIFace<RepositoryAgreementAttachment>, 
                                                                     java.io.Serializable 
{

    // Fields

    protected Integer                     repositoryAgreementId;
    protected String                      repositoryAgreementNumber;
    protected String                      status;
    protected Calendar                    startDate;
    protected Calendar                    endDate;
    protected Calendar                    dateReceived;
    protected String                      text1;
    protected String                      text2;
    protected String                      text3;
    protected Float                       number1;
    protected Float                       number2;
    protected String                      remarks;
    protected Boolean                     yesNo1;
    protected Boolean                     yesNo2;
    
    protected Set<AccessionAuthorization> repositoryAgreementAuthorizations;
    protected Set<AccessionAgent>         repositoryAgreementAgents;
    protected Set<Accession>              accessions;
    protected Agent                       originator;
    protected Set<RepositoryAgreementAttachment> repositoryAgreementAttachments;
    protected AddressOfRecord             addressOfRecord;
    protected Division                    division;

    // Constructors

    /** default constructor */
    public RepositoryAgreement() {
        //
    }

    /** constructor with id */
    public RepositoryAgreement(Integer repositoryAgreementId) {
        this.repositoryAgreementId = repositoryAgreementId;
    }




    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        repositoryAgreementId = null;
        repositoryAgreementNumber = null;
        status = null;
        startDate = null;
        endDate = null;
        dateReceived = null;
        text1 = null;
        text2 = null;
        text3 = null;
        number1 = null;
        number2 = null;
        remarks = null;
        yesNo1 = null;
        yesNo2 = null;
        repositoryAgreementAuthorizations = new HashSet<AccessionAuthorization>();
        repositoryAgreementAgents         = new HashSet<AccessionAgent>();
        accessions                        = new HashSet<Accession>();
        repositoryAgreementAttachments    = new HashSet<RepositoryAgreementAttachment>();
        originator                        = null;
        addressOfRecord                   = null;
        division                          = null;
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "RepositoryAgreementID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getRepositoryAgreementId() {
        return this.repositoryAgreementId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.repositoryAgreementId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return RepositoryAgreement.class;
    }

    public void setRepositoryAgreementId(Integer repositoryAgreementId) {
        this.repositoryAgreementId = repositoryAgreementId;
    }

    /**
     *
     */
    @Column(name = "RepositoryAgreementNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 60)
    public String getRepositoryAgreementNumber() {
        return this.repositoryAgreementNumber;
    }

    public void setRepositoryAgreementNumber(String number) {
        this.repositoryAgreementNumber = number;
    }

    /**
     *
     */
    @Column(name = "Status", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "StartDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EndDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateReceived", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateReceived() {
        return this.dateReceived;
    }

    public void setDateReceived(Calendar dateReceived) {
        this.dateReceived = dateReceived;
    }

    /**
     *
     */
    @Column(name = "Text1", length=255, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *
     */
    @Column(name = "Text2", length=255, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *
     */
    @Column(name = "Text3", length=255, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText3() {
        return this.text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    /**
     *
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1() {
        return this.number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2() {
        return this.number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
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
     *
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *
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
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "repositoryAgreement")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AccessionAuthorization> getRepositoryAgreementAuthorizations() {
        return this.repositoryAgreementAuthorizations;
    }

    public void setRepositoryAgreementAuthorizations(Set<AccessionAuthorization> repositoryAgreementAuthorizations) {
        this.repositoryAgreementAuthorizations = repositoryAgreementAuthorizations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "repositoryAgreement")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AccessionAgent> getRepositoryAgreementAgents() {
        return this.repositoryAgreementAgents;
    }

    public void setRepositoryAgreementAgents(Set<AccessionAgent> repositoryAgreementAgents) {
        this.repositoryAgreementAgents = repositoryAgreementAgents;
    }

    //@OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "repositoryAgreement")
    @OneToMany(mappedBy = "repositoryAgreement")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<RepositoryAgreementAttachment> getRepositoryAgreementAttachments()
    {
        return repositoryAgreementAttachments;
    }

    public void setRepositoryAgreementAttachments(Set<RepositoryAgreementAttachment> repositoryAgreementAttachments)
    {
        this.repositoryAgreementAttachments = repositoryAgreementAttachments;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getOriginator() {
        return this.originator;
    }

    public void setOriginator(Agent originator) {
        this.originator = originator;
    }
    
    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "repositoryAgreement")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<Accession> getAccessions() {
        return this.accessions;
    }
    
    public void setAccessions(Set<Accession> accessions) {
        this.accessions = accessions;
    }

    /**
     * @return the addressOfRecord
     */
    @ManyToOne(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AddressOfRecordID", unique = false, nullable = true, insertable = true, updatable = true)
    public AddressOfRecord getAddressOfRecord()
    {
        return addressOfRecord;
    }

    /**
     * @param addressOfRecord the addressOfRecord to set
     */
    public void setAddressOfRecord(AddressOfRecord addressOfRecord)
    {
        this.addressOfRecord = addressOfRecord;
    }
    
    /**
     * @return the division
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Division getDivision()
    {
        return division;
    }

    /**
     * @param division the division to set
     */
    public void setDivision(Division division)
    {
        this.division = division;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        // Throws exception when inlined
        Integer tblId = accessions != null && accessions.size() > 0 ? Accession.getClassTableId() : null;
        tblId = tblId != null ? tblId : division != null ? Division.getClassTableId() : null;
        return tblId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        
        if (division != null)
        {
            return division.getId();
        }
        if (accessions != null && accessions.size() == 1)
        {
            return ((FormDataObjIFace)accessions.toArray()[0]).getId();
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
        return 70;
    }

    @Transient
    public Set<RepositoryAgreementAttachment> getAttachmentReferences()
    {
        return repositoryAgreementAttachments;
    }

}

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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "repositoryagreement")
public class RepositoryAgreement extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Long repositoryAgreementId;
     protected String number;
     protected String status;
     protected Calendar startDate;
     protected Calendar endDate;
     protected Calendar dateReceived;
     protected String text1;
     protected String text2;
     protected String text3;
     protected Float number1;
     protected Float number2;
     protected String remarks;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<AccessionAuthorization> repositoryAgreementAuthorizations;
     protected Set<AccessionAgent>         repositoryAgreementAgents;
     protected Set<Accession>               accessions;
     protected Set<Attachment>          attachments;
     protected Agent originator;


    // Constructors

    /** default constructor */
    public RepositoryAgreement() {
        //
    }

    /** constructor with id */
    public RepositoryAgreement(Long repositoryAgreementId) {
        this.repositoryAgreementId = repositoryAgreementId;
    }




    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        repositoryAgreementId = null;
        number = null;
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
        originator = null;
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "RepositoryAgreementID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getRepositoryAgreementId() {
        return this.repositoryAgreementId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Long getId()
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

    public void setRepositoryAgreementId(Long repositoryAgreementId) {
        this.repositoryAgreementId = repositoryAgreementId;
    }

    /**
     *
     */
    @Column(name = "Number", unique = false, nullable = false, insertable = true, updatable = true, length = 60)
    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
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
    @Column(name = "Text1", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *
     */
    @Column(name = "Text2", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *
     */
    @Column(name = "Text3", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
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
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
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
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "repositoryAgreement")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }
    
    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 70;
    }

}

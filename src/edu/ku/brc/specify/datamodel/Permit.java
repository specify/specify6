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

import edu.ku.brc.ui.forms.FormDataObjIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "permit")
public class Permit extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Long permitId;
     protected String permitNumber;
     protected String type;
     protected Calendar issuedDate;
     protected Calendar startDate;
     protected Calendar endDate;
     protected Calendar renewalDate;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<AccessionAuthorizations> accessionAuthorizations;
     protected Agent issuedTo;
     protected Agent issuedBy;
     protected Set<Attachment>          attachments;


    // Constructors

    /** default constructor */
    public Permit() {
        //
        // do nothing
    }

    /** constructor with id */
    public Permit(Long permitId) {
        this.permitId = permitId;
    }




    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        permitId = null;
        permitNumber = null;
        type = null;
        issuedDate = null;
        startDate = null;
        endDate = null;
        renewalDate = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        accessionAuthorizations = new HashSet<AccessionAuthorizations>();
        issuedTo = null;
        issuedBy = null;
        attachments = new HashSet<Attachment>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "PermitID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getPermitId() {
        return this.permitId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.permitId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Permit.class;
    }

    public void setPermitId(Long permitId) {
        this.permitId = permitId;
    }

    /**
     *      * Identifier for the permit
     */
    @Column(name = "PermitNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getPermitNumber() {
        return this.permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    /**
     *      * Permit category - 'CITES', 'Migratory Bird Treaty Act', ...
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Date permit was issued
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "IssuedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getIssuedDate() {
        return this.issuedDate;
    }

    public void setIssuedDate(Calendar issuedDate) {
        this.issuedDate = issuedDate;
    }

    /**
     *      * Date permit becomes effective
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
     *      * Date permit expires
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
     *      * Date of renewal
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "RenewalDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getRenewalDate() {
        return this.renewalDate;
    }

    public void setRenewalDate(Calendar renewalDate) {
        this.renewalDate = renewalDate;
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
     *      * User definable
     */
    @Column(name = "Text1", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
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
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "permit")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<AccessionAuthorizations> getAccessionAuthorizations() {
        return this.accessionAuthorizations;
    }

    public void setAccessionAuthorizations(Set<AccessionAuthorizations> accessionAuthorizations) {
        this.accessionAuthorizations = accessionAuthorizations;
    }

    /**
     *      * AgentID of Issuee
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "IssuedToID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getIssuedTo() {
        return this.issuedTo;
    }

    public void setIssuedTo(Agent agentByIssuee) {
        this.issuedTo = agentByIssuee;
    }

    /**
     *      * AgentID of Issuer
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "IssuedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getIssuedBy() {
        return this.issuedBy;
    }

    public void setIssuedBy(Agent agentByIssuer) {
        this.issuedBy = agentByIssuer;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "permit")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return permitNumber != null ? permitNumber : super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 6;
    }

//    protected Set<AccessionAuthorizations> accessionAuthorizations;
//    protected Agent agentByIssuee;
//    protected Agent agentByIssuer;
//    protected Set<Attachment>          attachments;

    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        if (ref instanceof Attachment)
        {
            Attachment attach = (Attachment)ref;
            attachments.add(attach);
            attach.setPermit(this);
            return;
        }
        
        super.addReference(ref, refType);
    }

    @Override
    public Object getReferenceValue(String refName)
    {
        if (refName.equals("attachments"))
        {
            return attachments;
        }
        return super.getReferenceValue(refName);
    }

    @Override
    public void removeReference(FormDataObjIFace ref, String refType)
    {
        if (ref instanceof Attachment)
        {
            Attachment attach = (Attachment)ref;
            attachments.remove(attach);
            attach.setPermit(null);
            return;
        }
        
        super.removeReference(ref, refType);
    }
}

/* Copyright (C) 2020, Specify Collections Consortium
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

import java.util.*;

import javax.persistence.*;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tasks.InteractionsTask;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "disposal")
@org.hibernate.annotations.Table(appliesTo="disposal", indexes =
        {   @Index (name="DisposalNumberIDX", columnNames={"DisposalNumber"}),
                @Index (name="DisposalDateIDX", columnNames={"DisposalDate"})
        })
@SuppressWarnings("serial")
public class Disposal extends DataModelObjBase implements java.io.Serializable, OneToManyProviderIFace, AttachmentOwnerIFace<DisposalAttachment> {
    private static final Logger log = Logger.getLogger(Disposal.class);

    // Fields

    protected Integer                     disposalId;
    protected String                      type;
    protected String                      disposalNumber;
    protected Calendar                    disposalDate;
    protected String                      remarks;
    protected Boolean doNotExport; //don't export to aggregators if true.
    protected String                      text1;
    protected String                      text2;
    protected Float                       number1;
    protected Float                       number2;
    protected Boolean                     yesNo1;
    protected Boolean                     yesNo2;
    protected Set<DisposalAgent>       disposalAgents;
    protected Set<DisposalPreparation> disposalPreparations;
    protected Set<DisposalAttachment> disposalAttachments;
    protected Deaccession            deaccession;

    // Constructors

    /** default constructor */
    public Disposal() {
        //
    }

    /** constructor with id */
    public Disposal(Integer disposalId) {
        this.disposalId = disposalId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        disposalId = null;
        type = null;
        disposalNumber = null;
        disposalDate = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        disposalAgents = new HashSet<>();
        disposalPreparations = new HashSet<>();
        disposalAttachments = new HashSet<>();
        deaccession = null;
        doNotExport = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "DisposalID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getDisposalId() {
        return this.disposalId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.disposalId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Disposal.class;
    }

    public void setDisposalId(Integer disposalId) {
        this.disposalId = disposalId;
    }

    /**
     *      * Description of the Type of disposal; i.e. Gift, disposal, lost
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Name institution assigns to the deacession
     */
    @Column(name = "DisposalNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getDisposalNumber() {
        return this.disposalNumber;
    }

    public void setDisposalNumber(String disposalNumber) {
        this.disposalNumber = disposalNumber;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DisposalDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDisposalDate() {
        return this.disposalDate;
    }

    public void setDisposalDate(Calendar disposalDate) {
        this.disposalDate = disposalDate;
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
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "disposal")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<DisposalAgent> getDisposalAgents() {
        return this.disposalAgents;
    }

    public void setDisposalAgents(Set<DisposalAgent> disposalAgents) {
        this.disposalAgents = disposalAgents;
    }

    /**
     *
     */
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "disposal")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<DisposalPreparation> getDisposalPreparations() {
        return this.disposalPreparations;
    }

    public void setDisposalPreparations(Set<DisposalPreparation> disposalPreparations) {
        this.disposalPreparations = disposalPreparations;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK })
    @JoinColumn(name = "DeaccessionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Deaccession getDeaccession() {
        return deaccession;
    }

    public void setDeaccession(Deaccession deaccession) {
        this.deaccession = deaccession;
    }

    @Column(name="doNotExport",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getDoNotExport() {
        return doNotExport;
    }

    public void setDoNotExport(Boolean doNotExport) {
        this.doNotExport = doNotExport;
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
        disposalAgents.size();
        disposalPreparations.size();
    }

    @Transient
    public Set<Accession> getAccessions() {
        Set<Accession> result = new HashSet<>();
        for (DisposalPreparation dp : disposalPreparations) {
            if (dp.getPreparation() != null && dp.getPreparation().getCollectionObject() != null &&
                    dp.getPreparation().getCollectionObject().getAccession() != null) {
                result.add(dp.getPreparation().getCollectionObject().getAccession());
            }
        }
        return result;
    }

    @OneToMany(mappedBy = "disposal")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<DisposalAttachment> getDisposalAttachments()
    {
        return disposalAttachments;
    }

    public void setDisposalAttachments(Set<DisposalAttachment> disposalAttachments)
    {
        this.disposalAttachments = disposalAttachments;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentTableId()
     */
    @Override
    @Transient
    public int getAttachmentTableId()
    {
        return getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<DisposalAttachment> getAttachmentReferences()
    {
        return disposalAttachments;
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

    @Override
    @Transient
    public Set<? extends PreparationHolderIFace> getPreparationHolders() {
        return getDisposalPreparations();
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 34;
    }

    @Transient
    public Integer getTotalPreps() {
        return countContents(false, false);
    }

    @Transient
    public Integer getTotalItems() {
        return countContents(true, false);
    }

    protected Integer countContents(Boolean countQuantity, Boolean countUnresolved) {
        if (getId() == null) {
            return null;
        } else {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(countQuantity, getId()));
        }
    }

    protected static String getCountContentsSql(boolean countQuantity, int id) {
        return InteractionsTask.getCountContentsSql(countQuantity, false, id, getClassTableId());
    }

    @Transient
    public static List<String> getQueryableTransientFields() {
        List<String> result = new ArrayList<>();
        result.add("TotalPreps");
        result.add("TotalItems");
        return result;
    }

    public static Object getQueryableTransientFieldValue(String fldName, Object[] vals) {
        if (vals == null || vals[0] == null) {
            return null;
        } else if (fldName.equalsIgnoreCase("TotalPreps")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(false, (Integer)vals[0]));
        } else if (fldName.equalsIgnoreCase("TotalItems")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(true, (Integer)vals[0]));
        } else {
            log.error("Unknown calculated field: " + fldName);
            return null;
        }
    }

}

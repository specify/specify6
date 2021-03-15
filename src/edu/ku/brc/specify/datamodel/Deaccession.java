package edu.ku.brc.specify.datamodel;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "deaccession")
@org.hibernate.annotations.Table(appliesTo="deaccession", indexes =
        {   @Index(name="DeaccessionNumberIDX", columnNames={"DeaccessionNumber"}),
                @Index (name="DeaccessionDateIDX", columnNames={"DeaccessionDate"})
        })
@SuppressWarnings("serial")
public class Deaccession extends DataModelObjBase implements java.io.Serializable, OneToManyProviderIFace, AttachmentOwnerIFace<DeaccessionAttachment> {
    protected Integer                     deaccessionId;
    protected String                      type;
    protected String                      status;
    protected String                      deaccessionNumber;
    protected Calendar deaccessionDate;
    protected String                      remarks;
    protected String                      text1;
    protected String                      text2;
    protected Float                       number1;
    protected Float                       number2;
    protected Boolean                     yesNo1;
    protected Boolean                     yesNo2;
    protected Set<Disposal> disposals;
    protected Set<Gift> gifts;
    protected Set<ExchangeOut> exchangeOuts;
    protected Set<Accession> accessions;
    protected Set<DeaccessionAgent> deaccessionAgents;
    protected Set<DeaccessionAttachment> deaccessionAttachments;
    protected Set<OneToManyProviderIFace> removals;


    // Constructors

    @OneToMany(mappedBy = "deaccession")
    @Cascade( {org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK} )
    public Set<Disposal> getDisposals() {
        return disposals;
    }

    public void setDisposals(Set<Disposal> disposals) {
        this.disposals = disposals;
    }

    @OneToMany(mappedBy = "deaccession")
    @Cascade( {org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK} )
    public Set<Gift> getGifts() {
        return gifts;
    }

    public void setGifts(Set<Gift> gifts) {
        this.gifts = gifts;
    }

    @OneToMany(mappedBy = "deaccession")
    @Cascade( {org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK} )
    public Set<ExchangeOut> getExchangeOuts() {
        return exchangeOuts;
    }

    public void setExchangeOuts(Set<ExchangeOut> exchangeOuts) {
        this.exchangeOuts = exchangeOuts;
    }

    //    @OneToMany(mappedBy = "deaccession")
//    @Cascade( {org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK} )
    @Transient
    public Set<Accession> getAccessions() {
        if (accessions == null) {
            accessions = buildAccessions();
        }
        return accessions;
    }

    private Set<Accession> getAccessionsFromProvider(OneToManyProviderIFace provider) {
        Set<Accession> result = new HashSet<>();
        for (PreparationHolderIFace providee : provider.getPreparationHolders()) {
            if (providee.getPreparation() != null && providee.getPreparation().getCollectionObject() != null &&
                    providee.getPreparation().getCollectionObject().getAccession() != null) {
                result.add(providee.getPreparation().getCollectionObject().getAccession());
            }
        }
        return result;
    }

    public Set<Accession> buildAccessions() {
        //get accessions from associated removal(s)...
        Set<Accession> result = new HashSet<>();
        for (OneToManyProviderIFace removal : getRemovals()) {
            result.addAll(getAccessionsFromProvider(removal));
        }
        return result;
    }


    @OneToMany(mappedBy = "deaccession")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<DeaccessionAttachment> getDeaccessionAttachments()
    {
        return deaccessionAttachments;
    }

    public void setDeaccessionAttachments(Set<DeaccessionAttachment> deaccessionAttachments)
    {
        this.deaccessionAttachments = deaccessionAttachments;
    }

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "deaccession")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<DeaccessionAgent> getDeaccessionAgents() {
        return this.deaccessionAgents;
    }

    public void setDeaccessionAgents(Set<DeaccessionAgent> deaccessionAgents) {
        this.deaccessionAgents = deaccessionAgents;
    }

//    public void setAccessions(Set<Accession> accessions) {
//        this.accessions = accessions;
//    }

    @Transient
    public Set<OneToManyProviderIFace> getRemovals() {
        Set<OneToManyProviderIFace> result = new HashSet<>();
        for (Disposal d : getDisposals()) {
            result.add(d);
        }
        for (ExchangeOut e : getExchangeOuts()) {
            result.add(e);
        }
        for (Gift g : getGifts()) {
            result.add(g);
        }
        return result;
    }
    /** default constructor */
    public Deaccession() {
        //
    }

    /** constructor with id */
    public Deaccession(Integer deaccessionId) {
        this.deaccessionId = deaccessionId;
    }

    // Initializer
    @Override
    public void initialize() {
        super.init();
        deaccessionId = null;
        type = null;
        status = null;
        deaccessionNumber = null;
        deaccessionDate = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        disposals = new HashSet<>();
        gifts = new HashSet<>();
        exchangeOuts = new HashSet<>();
        deaccessionAttachments = new HashSet<>();
        deaccessionAgents = new HashSet<>();
        accessions = null;
        removals = null;

    }
    // End Initializer

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
        Set<PreparationHolderIFace> result = new HashSet<>();
        for (Disposal d : getDisposals()) {
            result.addAll(d.getPreparationHolders());
        }
        for (Gift g : getGifts()) {
            result.addAll(g.getPreparationHolders());
        }
        for (ExchangeOut g : getExchangeOuts()) {
            result.addAll(g.getPreparationHolders());
        }
        return result;
    }

    public static int getClassTableId() {
        return 163;
    }

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "DeaccessionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getDeaccessionId() {
        return this.deaccessionId;
    }
    public void setDeaccessionId(Integer deaccessionId) {
        this.deaccessionId = deaccessionId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.deaccessionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Deaccession.class;
    }

    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "Status", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    @Column(name = "DeaccessionNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getDeaccessionNumber() {
        return deaccessionNumber;
    }

    public void setDeaccessionNumber(String deaccessionNumber) {
        this.deaccessionNumber = deaccessionNumber;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "DeaccessionDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDeaccessionDate() {
        return deaccessionDate;
    }

    public void setDeaccessionDate(Calendar deaccessionDate) {
        this.deaccessionDate = deaccessionDate;
    }

    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber1() {
        return number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber2() {
        return number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
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
    public Set<DeaccessionAttachment> getAttachmentReferences()
    {
        return deaccessionAttachments;
    }

}

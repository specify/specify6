/* Copyright (C) 2021, Specify Collections Consortium
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

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tasks.InteractionsProcessor;
import edu.ku.brc.specify.tasks.InteractionsTask;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.*;

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
    private static final Logger log = Logger.getLogger(Deaccession.class);
    protected Integer                     deaccessionId;
    protected String                      type;
    protected String                      status;
    protected String                      deaccessionNumber;
    protected Calendar deaccessionDate;
    protected String                      remarks;
    protected String                      text1;
    protected String                      text2;
    protected String                      text3;
    protected String                      text4;
    protected String                      text5;
    protected Float                       number1;
    protected Float                       number2;
    protected Float                       number3;
    protected Float                       number4;
    protected Float                       number5;
    protected Integer                       integer1;
    protected Integer                       integer2;
    protected Integer                       integer3;
    protected Integer                       integer4;
    protected Integer                       integer5;
    protected Boolean                     yesNo1;
    protected Boolean                     yesNo2;
    protected Boolean                     yesNo3;
    protected Boolean                     yesNo4;
    protected Boolean                     yesNo5;
    protected Calendar date1;
    protected Calendar date2;
    protected Agent agent1;
    protected Agent agent2;
    protected Set<Disposal> disposals;
    protected Set<Gift> gifts;
    protected Set<ExchangeOut> exchangeOuts;
    protected Set<Accession> accessions;
    protected Set<DeaccessionAgent> deaccessionAgents;
    protected Set<DeaccessionAttachment> deaccessionAttachments;
    protected Set<OneToManyProviderIFace> removals;

    @Override
    public void forceLoad() {
        forceLoad(false);
    }

    private void forceLoadSet(Set<? extends DataModelObjBase> set) {
        for (DataModelObjBase s : set) {
            s.forceLoad();
        }
    }
    /**
     *
     * @param forcefully
     */
    public void forceLoad(boolean forcefully) {
        super.forceLoad();
        disposals.size();
        gifts.size();
        exchangeOuts.size();
        deaccessionAttachments.size();
        deaccessionAgents.size();

        if (forcefully) {
            forceLoadSet(disposals);
            forceLoadSet(gifts);
            forceLoadSet(exchangeOuts);
            forceLoadSet(deaccessionAttachments);
            forceLoadSet(deaccessionAgents);
        }
    }



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
        text3 = null;
        text4 = null;
        text5 = null;
        number1 = null;
        number2 = null;
        number3 = null;
        number4 = null;
        number5 = null;
        integer1 = null;
        integer2 = null;
        integer3 = null;
        integer4 = null;
        integer5 = null;
        yesNo1 = null;
        yesNo2 = null;
        yesNo3 = null;
        yesNo4 = null;
        yesNo5 = null;
        date1 = null;
        date2 = null;
        agent1 = null;
        agent2 = null;
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
    @Column(name = "DeaccessionNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
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

    @Temporal(TemporalType.DATE)
    @Column(name = "Date1", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate1() {
        return date1;
    }

    public void setDate1(Calendar date1) {
        this.date1 = date1;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date2", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate2() {
        return date2;
    }

    public void setDate2(Calendar date2) {
        this.date2 = date2;
    }


    /**
     *
     * @return
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent1ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent1() {
        return agent1;
    }
    /**
     *
     * @param agent1
     */
    public void setAgent1(Agent agent1) {
        this.agent1 = agent1;
    }

    /**
     *
     * @return
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent2ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent2() {
        return agent2;
    }
    /**
     *
     * @param agent2
     */
    public void setAgent2(Agent agent2) {
        this.agent2 = agent2;
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
    @Column(name = "Text3", length = 65535)
    public String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    @Lob
    @Column(name = "Text4", length = 65535)
    public String getText4() {
        return text4;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    @Lob
    @Column(name = "Text5", length = 65535)
    public String getText5() {
        return text5;
    }

    public void setText5(String text5) {
        this.text5 = text5;
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

    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true, length = 34)
    public Float getNumber3() {
        return number3;
    }

    public void setNumber3(Float number3) {
        this.number3 = number3;
    }

    @Column(name = "Number4", unique = false, nullable = true, insertable = true, updatable = true, length = 44)
    public Float getNumber4() {
        return number4;
    }

    public void setNumber4(Float number4) {
        this.number4 = number4;
    }

    @Column(name = "Number5", unique = false, nullable = true, insertable = true, updatable = true, length = 54)
    public Float getNumber5() {
        return number5;
    }

    public void setNumber5(Float number5) {
        this.number5 = number5;
    }

    @Column(name = "Integer1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Integer getInteger1() {
        return integer1;
    }

    public void setInteger1(Integer integer1) {
        this.integer1 = integer1;
    }

    @Column(name = "Integer2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Integer getInteger2() {
        return integer2;
    }

    public void setInteger2(Integer integer2) {
        this.integer2 = integer2;
    }

    @Column(name = "Integer3", unique = false, nullable = true, insertable = true, updatable = true, length = 34)
    public Integer getInteger3() {
        return integer3;
    }

    public void setInteger3(Integer integer3) {
        this.integer3 = integer3;
    }

    @Column(name = "Integer4", unique = false, nullable = true, insertable = true, updatable = true, length = 44)
    public Integer getInteger4() {
        return integer4;
    }

    public void setInteger4(Integer integer4) {
        this.integer4 = integer4;
    }

    @Column(name = "Integer5", unique = false, nullable = true, insertable = true, updatable = true, length = 54)
    public Integer getInteger5() {
        return integer5;
    }

    public void setInteger5(Integer integer5) {
        this.integer5 = integer5;
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

    @Column(name="YesNo3",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo3() {
        return yesNo3;
    }

    public void setYesNo3(Boolean yesNo3) {
        this.yesNo3 = yesNo3;
    }

    @Column(name="YesNo4",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo4() {
        return yesNo4;
    }

    public void setYesNo4(Boolean yesNo4) {
        this.yesNo4 = yesNo4;
    }

    @Column(name="YesNo5",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo5() {
        return yesNo5;
    }

    public void setYesNo5(Boolean yesNo5) {
        this.yesNo5 = yesNo5;
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


    @Transient
    public Integer getTotalPreps() {
	log.info("getTotalPreps()");
	return countContents(false);
    }

    @Transient
    public Integer getTotalItems() {
	log.info("getTotalItems");
        return countContents(true);
    }

    protected Integer countContents(Boolean countQuantity) {
        if (getId() == null) {
            return null;
        } else {
            return countContents(countQuantity, getId());
        }
    }

    /**
     *
     * @param countQuantity
     * @param id
     * @return
     */
    protected static Integer countContents(boolean countQuantity, int id) {
        java.sql.Connection conn = InteractionsProcessor.getConnForAvailableCounts();
        String sql = "(select disposalid, " + Disposal.getClassTableId() + " from disposal where deaccessionid = " + id
        +  ") union (select giftid, " + Gift.getClassTableId() + " from gift where deaccessionid = " + id
        + ") union (select exchangeoutid, " + ExchangeOut.getClassTableId() + " from exchangeout where deaccessionid = " + id + ")";
        List<Object[]> interactions = BasicSQLUtils.query(conn, sql);
        log.info("countContents: found " + interactions.size() +  " related interactions.");
        Integer result = 0;
        for (Object[] i : interactions) {
            log.info("   " + i[0] + ", " + i[1]);
            try {
                Integer key = Integer.valueOf(i[0].toString());
                Integer tblId = Integer.valueOf(i[1].toString());
	            log.info(InteractionsTask.getCountContentsSql(countQuantity, false, key, tblId));
	            BasicSQLUtils.setSkipTrackExceptions(false);
	            result += BasicSQLUtils.getCountAsInt(conn, InteractionsTask.getCountContentsSql(countQuantity, false, key, tblId));
	            BasicSQLUtils.setSkipTrackExceptions(true);
            } catch (Exception x){
                x.printStackTrace();
                log.error(x, x);
            }
        }
        return result;
    }

    @Transient
    public static List<String> getQueryableTransientFields() {
        List<String> result = new ArrayList<>();
        result.add("TotalPreps");
        result.add("TotalItems");
        return result;
    }

    public static Object getQueryableTransientFieldValue(String fldName, Object[] vals) {
        log.info("getQueryableTransientFieldValue(" + fldName + ", " + vals + ")");
        if (vals == null || vals[0] == null) {
            return null;
        } else if (fldName.equalsIgnoreCase("TotalPreps")) {
            return countContents(false, (Integer)vals[0]);
        } else if (fldName.equalsIgnoreCase("TotalItems")) {
            return countContents(true, (Integer)vals[0]);
        } else {
            log.error("Unknown calculated field: " + fldName);
            return null;
        }
    }

}

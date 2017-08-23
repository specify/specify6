/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**
 * @author timo
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "dnaprimer")
@org.hibernate.annotations.Table(appliesTo="dnaprimer", indexes =
{   @Index (name="DesignatorIDX", columnNames={"PrimerDesignator"})
})
public class DNAPrimer extends DataModelObjBase {
	protected Integer dnaPrimerId;
	protected String primerDesignator;
	protected String primerNameReverse;
	protected String primerNameForward;
	protected String primerReferenceCitationReverse;
	protected String primerReferenceCitationForward;
	protected String primerReferenceLinkReverse;
	protected String primerReferenceLinkForward;
	protected String primerSequenceReverse;
	protected String primerSequenceForward;
	protected String purificationMethod;
	

	protected String remarks;
	protected String text1;
	protected String text2;
	protected Float number1;
	protected Float number2;
	protected Integer integer1;
	protected Integer integer2;
	protected Boolean yesNo1;
	protected Boolean yesNo2;
	
	protected String reservedText3;
	protected String reservedText4;
	protected Integer reservedInteger3;
	protected Integer reservedInteger4;
	protected Float reservedNumber3;
	protected Float reservedNumber4;
	
	protected Set<DNASequencingRun> dnaSequencingRuns;

	/**
	 * 
	 */
	public DNAPrimer() {
		super();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize() {
		super.init();
		dnaPrimerId = null;
		primerDesignator = null;
		primerNameForward = null;
		primerNameReverse = null;
		primerReferenceCitationForward = null;
		primerReferenceCitationReverse = null;
		primerReferenceLinkForward = null;
		primerReferenceLinkReverse = null;
		primerSequenceForward = null;
		primerSequenceReverse = null;
		purificationMethod = null;

		remarks = null;
		text1 = null;
		text2 = null;
		number1 = null;
		number2 = null;
		integer1 = null;
		integer2 = null;
		yesNo1 = null;
		yesNo2 = null;

		reservedText3 = null;
		reservedText4 = null;
		reservedNumber3 = null;
		reservedNumber4 = null;
		reservedInteger3 = null;
		reservedInteger4 = null;
	
		dnaSequencingRuns = new HashSet<DNASequencingRun>();
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Override
	@Transient
	public Integer getId() {
		return dnaPrimerId;
	}

	
	/**
	 * @return the dnaPrimerId
	 */
    @Id
    @GeneratedValue
    @Column(name = "DNAPrimerID", unique = false, nullable = false, insertable = true, updatable = true)
	public Integer getDnaPrimerId() {
		return dnaPrimerId;
	}

	/**
	 * @param dnaPrimerId the dnaPrimerId to set
	 */
	public void setDnaPrimerId(Integer dnaPrimerId) {
		this.dnaPrimerId = dnaPrimerId;
	}

	/**
	 * @return the purificationMethod
	 */
	public String getPurificationMethod() {
		return purificationMethod;
	}

	/**
	 * @param purificationMethod the purificationMethod to set
	 */
	public void setPurificationMethod(String purificationMethod) {
		this.purificationMethod = purificationMethod;
	}

	/**
	 * @return the primerDesignator
	 */
    @Column(name = "PrimerDesignator", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getPrimerDesignator() {
		return primerDesignator;
	}

	/**
	 * @param primerDesignator the primerDesignator to set
	 */
	public void setPrimerDesignator(String primerDesignator) {
		this.primerDesignator = primerDesignator;
	}

	/**
	 * @return the primerNameReverse
	 */
    @Column(name = "PrimerNameReverse", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getPrimerNameReverse() {
		return primerNameReverse;
	}

	/**
	 * @param primerNameReverse the primerNameReverse to set
	 */
	public void setPrimerNameReverse(String primerNameReverse) {
		this.primerNameReverse = primerNameReverse;
	}

	
	/**
	 * @return the primerNameForward
	 */
    @Column(name = "PrimerNameForward", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getPrimerNameForward() {
		return primerNameForward;
	}

	/**
	 * @param primerNameForward the primerNameForward to set
	 */
	public void setPrimerNameForward(String primerNameForward) {
		this.primerNameForward = primerNameForward;
	}

	/**
	 * @return the primerReferenceCitationReverse
	 */
    @Column(name = "PrimerReferenceCitationReverse", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
	public String getPrimerReferenceCitationReverse() {
		return primerReferenceCitationReverse;
	}

	/**
	 * @param primerReferenceCitationReverse the primerReferenceCitationReverse to set
	 */
	public void setPrimerReferenceCitationReverse(String primerReferenceCitationReverse) {
		this.primerReferenceCitationReverse = primerReferenceCitationReverse;
	}

	/**
	 * @return the primerReferenceCitationForward
	 */
    @Column(name = "PrimerReferenceCitationForward", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
	public String getPrimerReferenceCitationForward() {
		return primerReferenceCitationForward;
	}

	/**
	 * @param primerReferenceCitationForward the primerReferenceCitationForward to set
	 */
	public void setPrimerReferenceCitationForward(String primerReferenceCitationForward) {
		this.primerReferenceCitationForward = primerReferenceCitationForward;
	}

	/**
	 * @return the primerReferenceLinkReverse
	 */
    @Column(name = "PrimerReferenceLinkReverse", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
	public String getPrimerReferenceLinkReverse() {
		return primerReferenceLinkReverse;
	}

	/**
	 * @param primerReferenceLinkReverse the primerReferenceLinkReverse to set
	 */
	public void setPrimerReferenceLinkReverse(String primerReferenceLinkReverse) {
		this.primerReferenceLinkReverse = primerReferenceLinkReverse;
	}

	/**
	 * @return the primerReferenceLinkForward
	 */
    @Column(name = "PrimerReferenceLinkForward", unique = false, nullable = true, insertable = true, updatable = true, length = 300)
	public String getPrimerReferenceLinkForward() {
		return primerReferenceLinkForward;
	}

	/**
	 * @param primerReferenceLinkForward the primerReferenceLinkForward to set
	 */
	public void setPrimerReferenceLinkForward(String primerReferenceLinkForward) {
		this.primerReferenceLinkForward = primerReferenceLinkForward;
	}

	/**
	 * @return the primerSequenceReverse
	 */
    @Column(name = "PrimerSequenceReverse", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getPrimerSequenceReverse() {
		return primerSequenceReverse;
	}

	/**
	 * @param primerSequenceReverse the primerSequenceReverse to set
	 */
	public void setPrimerSequenceReverse(String primerSequenceReverse) {
		this.primerSequenceReverse = primerSequenceReverse;
	}

	/**
	 * @return the primerSequenceForward
	 */
    @Column(name = "PrimerSequenceForward", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getPrimerSequenceForward() {
		return primerSequenceForward;
	}

	/**
	 * @param primerSequenceForward the primerSequenceForward to set
	 */
	public void setPrimerSequenceForward(String primerSequenceForward) {
		this.primerSequenceForward = primerSequenceForward;
	}

	/**
	 * @return the remarks
	 */
    @Lob
    @Column(name = "Remarks", length = 4096)
	public String getRemarks() {
		return remarks;
	}

	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	/**
	 * @return the text1
	 */
    @Lob
    @Column(name = "Text1", length = 4096)
	public String getText1() {
		return text1;
	}

	/**
	 * @param text1 the text1 to set
	 */
	public void setText1(String text1) {
		this.text1 = text1;
	}

	/**
	 * @return the text2
	 */
    @Lob
    @Column(name = "Text2", length = 4096)
	public String getText2() {
		return text2;
	}

	/**
	 * @param text2 the text2 to set
	 */
	public void setText2(String text2) {
		this.text2 = text2;
	}

	/**
	 * @return the number1
	 */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
	public Float getNumber1() {
		return number1;
	}

	/**
	 * @param number1 the number1 to set
	 */
	public void setNumber1(Float number1) {
		this.number1 = number1;
	}

	/**
	 * @return the number2
	 */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
	public Float getNumber2() {
		return number2;
	}

	/**
	 * @param number2 the number2 to set
	 */
	public void setNumber2(Float number2) {
		this.number2 = number2;
	}

	/**
	 * @return the integer1
	 */
    @Column(name = "Integer1", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getInteger1() {
		return integer1;
	}

	/**
	 * @param integer1 the integer1 to set
	 */
	public void setInteger1(Integer integer1) {
		this.integer1 = integer1;
	}

	/**
	 * @return the integer2
	 */
    @Column(name = "Integer2", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getInteger2() {
		return integer2;
	}

	/**
	 * @param integer2 the integer2 to set
	 */
	public void setInteger2(Integer integer2) {
		this.integer2 = integer2;
	}

	/**
	 * @return the yesNo1
	 */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
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
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
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
	 * @return the text1
	 */
    @Lob
    @Column(name = "ReservedText3", length = 4096)
	public String getReservedText3() {
		return reservedText3;
	}

	/**
	 * @param text3
	 */
	public void setReservedText3(String text3) {
		this.reservedText3 = text3;
	}

	/**
	 * @return 
	 */
    @Lob
    @Column(name = "ReservedText4", length = 4096)
	public String getReservedText4() {
		return reservedText4;
	}

	/**
	 * @param text4
	 */
	public void setReservedText4(String text4) {
		this.reservedText4 = text4;
	}

	/**
	 * @return 
	 */
    @Column(name = "ReservedNumber3", unique = false, nullable = true, insertable = true, updatable = true)
	public Float getReservedNumber3() {
		return reservedNumber3;
	}

	/**
	 * @param number3 
	 */
	public void setReservedNumber3(Float number3) {
		this.reservedNumber3 = number3;
	}

	/**
	 * @return 
	 */
    @Column(name = "ReservedNumber4", unique = false, nullable = true, insertable = true, updatable = true)
	public Float getReservedNumber4() {
		return reservedNumber4;
	}

	/**
	 * @param number4
	 */
	public void setReservedNumber4(Float number4) {
		this.reservedNumber4 = number4;
	}

	/**
	 * @return 
	 */
    @Column(name = "ReservedInteger3", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger3() {
		return reservedInteger3;
	}

	/**
	 * @param integer3
	 */
	public void setReservedInteger3(Integer integer3) {
		this.reservedInteger3 = integer3;
	}

	/**
	 * @return 	 
	 * */
    @Column(name = "ReservedInteger4", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger4() {
		return reservedInteger4;
	}

	/**
	 * @param integer4
	 * 	 
	 * */
	public void setReservedInteger4(Integer integer4) {
		this.reservedInteger4 = integer4;
	}

	/**
	 * @return the dnaSequencingRun
	 */
    @OneToMany(mappedBy = "dnaPrimer")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
	public Set<DNASequencingRun> getDnaSequencingRuns() {
		return dnaSequencingRuns;
	}

	/**
	 * @param dnaSequencingRun the dnaSequencingRun to set
	 */
	public void setDnaSequencingRuns(Set<DNASequencingRun> dnaSequencingRuns) {
		this.dnaSequencingRuns = dnaSequencingRuns;
	}

	/**
     * @return the Table ID for the class.
     */
	@Transient
    public static int getClassTableId()
    {
    	return 150;
    }
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
	 */
	@Override
	@Transient
	public int getTableId() {
		return getClassTableId();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
	 */
	@Override
	@Transient
	public Class<?> getDataClass() {
		return DNAPrimer.class;
	}

}

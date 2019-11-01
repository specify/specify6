package edu.ku.brc.specify.datamodel;

import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Vector;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "voucherrelationship")
@org.hibernate.annotations.Table(appliesTo="voucherrelationship", indexes =
        {
                @Index (name="VRXDATColMemIDX", columnNames={"CollectionMemberID"})
        })
public class VoucherRelationship extends CollectionMember implements Cloneable {
    protected static final Logger log = Logger.getLogger(VoucherRelationship.class);

    protected Integer voucherRelationshipId;

    protected String institutionCode;
    protected String collectionCode;
    protected String voucherNumber;
    protected String urlLink;
    protected String remarks;
    protected String text1;
    protected String text2;
    protected String text3;
    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;
    protected Float number1;
    protected Float number2;
    protected Float number3;
    protected Integer integer1;
    protected Integer integer2;
    protected Integer integer3;
    protected CollectionObject collectionObject;

    /** default constructor */
    public VoucherRelationship()
    {
        // do nothing
    }

    /** constructor with id */
    public VoucherRelationship(Integer voucherRelationshipId)
    {
        this.voucherRelationshipId = voucherRelationshipId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        voucherRelationshipId = null;

        number1 = null;
        number2 = null;
        number3 = null;
        integer1 = null;
        integer2 = null;
        integer3 = null;
        text1 = null;
        text2 = null;
        text3 = null;
        yesNo1 = null;
        yesNo2 = null;
        yesNo3 = null;
        remarks = null;
        collectionObject = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "VoucherRelationshipID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getVoucherRelationshipId()
    {
        return voucherRelationshipId;
    }

    public void setVoucherRelationshipId(Integer voucherRelationshipId) {
        this.voucherRelationshipId = voucherRelationshipId;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.voucherRelationshipId;
    }


    @Column(name = "InstitutionCode", length = 64)
    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    @Column(name = "CollectionCode", length = 64)
    public String getCollectionCode() {
        return collectionCode;
    }

    public void setCollectionCode(String collectionCode) {
        this.collectionCode = collectionCode;
    }

    @Column(name = "VoucherNumber", length = 256)
    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    @Column(name = "UrlLink", length = 1024)
    public String getUrlLink() {
        return urlLink;
    }

    public void setUrlLink(String urlLink) {
        this.urlLink = urlLink;
    }

    @Lob
    @Column(name = "Remarks", length = 65535)
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
        text1 = text1;
    }

    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        text2 = text2;
    }

    @Lob
    @Column(name = "Text3", length = 65535)
    public String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        text3 = text3;
    }

    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1() {
        return yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        yesNo1 = yesNo1;
    }

    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2() {
        return yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        yesNo2 = yesNo2;
    }

    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3() {
        return yesNo3;
    }

    public void setYesNo3(Boolean yesNo3) {
        yesNo3 = yesNo3;
    }

    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1() {
        return number1;
    }

    public void setNumber1(Float number1) {
        number1 = number1;
    }

    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2() {
        return number2;
    }

    public void setNumber2(Float number2) {
        number2 = number2;
    }

    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber3() {
        return number3;
    }

    public void setNumber3(Float number3) {
        number3 = number3;
    }

    @Column(name = "Integer1", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger1() {
        return integer1;
    }

    public void setInteger1(Integer integer1) {
        integer1 = integer1;
    }

    @Column(name = "Integer2", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger2() {
        return integer2;
    }

    public void setInteger2(Integer integer2) {
        integer2 = integer2;
    }

    @Column(name = "Integer3", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger3() {
        return integer3;
    }

    public void setInteger3(Integer integer3) {
        integer3 = integer3;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = false, insertable = true, updatable = true)
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }

    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        VoucherRelationship obj    = (VoucherRelationship)super.clone();
        obj.voucherRelationshipId  = null;
        obj.collectionObject = null;

        return obj;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        String str = DataObjFieldFormatMgr.getInstance().format(this, getDataClass());
        return StringUtils.isNotEmpty(str) ? str : Integer.valueOf(getTableId()).toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return CollectionObject.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId() {
        if (collectionObject != null && collectionObject.getId() != null) {
            //what is going on?
            Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT CollectionObjectID FROM collectionobject WHERE CollectionObjectID = " + collectionObject.getId());
            if (ids.size() == 1) {
                return (Integer) ids.get(0);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return VoucherRelationship.class;
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
        return 155;
    }

}
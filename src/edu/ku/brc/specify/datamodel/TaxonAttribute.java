package edu.ku.brc.specify.datamodel;

import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "taxonattribute")
public class TaxonAttribute extends DataModelObjBase implements Cloneable {
    protected Integer taxonAttributeId;

    protected String text1;
    protected String text2;
    protected String text3;

    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;

    protected Set<Taxon> taxons;

    // Constructors

    /**
     * default constructor
     */
    public TaxonAttribute() {
        // do nothing
    }

    /**
     * constructor with id
     */
    public TaxonAttribute(Integer taxonAttributeId) {
        this.taxonAttributeId = taxonAttributeId;
    }

    // Initializer
    @Override
    public void initialize() {
        super.init();
        taxonAttributeId = null;
        text1 = null;
        text2 = null;
        text3 = null;
        yesNo1 = null;
        yesNo2 = null;
        yesNo3 = null;
        taxons = new HashSet<Taxon>();
    }

    @Id
    @GeneratedValue
    @Column(name = "TaxonAttributeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getTaxonAttributeId()
    {
        return taxonAttributeId;
    }

    public void setTaxonAttributeId(Integer taxonAttributeId) {
        this.taxonAttributeId = taxonAttributeId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.taxonAttributeId;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "taxonAttribute")
    public Set<Taxon> getTaxons()
    {
        return taxons;
    }

    public void setTaxons(Set<Taxon> taxons) {
        this.taxons = taxons;
    }

    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    @Column(name = "Text3", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1() {
        return yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2() {
        return yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3() {
        return yesNo3;
    }

    public void setYesNo3(Boolean yesNo3) {
        this.yesNo3 = yesNo3;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        TaxonAttribute obj = (TaxonAttribute) super.clone();
        obj.taxonAttributeId = null;
        obj.taxons = new HashSet<Taxon>();

        return obj;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle() {
        return toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString() {
        String str = DataObjFieldFormatMgr.getInstance().format(this, getDataClass());
        return StringUtils.isNotEmpty(str) ? str : "1";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId() {
        return Taxon.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId() {
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT TaxonID FROM taxon WHERE TaxonAttributeID = " + taxonAttributeId);
        if (ids.size() == 1) {
            return (Integer) ids.get(0);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass() {
        return TaxonAttribute.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId() {
        return getClassTableId();
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId() {
        return 93;
    }
    /**
     * @param o
     * @return true if 'non-system' fields all match.
     *
     */
    public boolean matches(TaxonAttribute o) {
        if (o == null) {
            return false;
        }

        return ((text1 == null && o.text1 == null) || ((text1 != null && o.text1 != null) && text1.equals(o.text1))) &&
                ((text2 == null && o.text2 == null) || ((text2 != null && o.text2 != null) && text2.equals(o.text2))) &&
                ((text3 == null && o.text3 == null) || ((text3 != null && o.text3 != null) && text3.equals(o.text3))) &&
                ((yesNo1 == null && o.yesNo1 == null) || ((yesNo1 != null && o.yesNo1 != null) && yesNo1.equals(o.yesNo1))) &&
                ((yesNo2 == null && o.yesNo2 == null) || ((yesNo2 != null && o.yesNo2 != null) && yesNo2.equals(o.yesNo2))) &&
                ((yesNo3 == null && o.yesNo3 == null) || ((yesNo3 != null && o.yesNo3 != null) && yesNo3.equals(o.yesNo3)));
    }
}

package edu.ku.brc.specify.datamodel;

import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "taxonattribute")
public class TaxonAttribute extends DataModelObjBase implements Cloneable {

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
}

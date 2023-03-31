/* Copyright (C) 2023, Specify Collections Consortium
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

import static edu.ku.brc.helpers.XMLHelper.addAttr;
import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.dom4j.Element;
import org.hibernate.annotations.Index;

import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 17, 2007
 *
 */
/**
 * @author Administrator
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spquery")
@org.hibernate.annotations.Table(appliesTo="spquery", indexes =
    {   @Index (name="SpQueryNameIDX", columnNames={"Name"})
    })
public class SpQuery extends DataModelObjBase implements Cloneable
{
    protected Integer           spQueryId;
    protected String            name;
    protected String            contextName;
    protected Short             contextTableId;
    protected String            sqlStr;
    protected Boolean           isFavorite; //whether or not this query goes on the 'short list'
    protected boolean           named = true;
    protected Short             ordinal;
    protected String            remarks;
    protected Boolean           selectDistinct;
    protected Boolean			searchSynonymy;
    protected Boolean           countOnly;
    protected Boolean 		    smushed;
    protected Boolean           formatAuditRecIds;
    
    protected Set<SpQueryField> fields;
    protected Set<SpReport>     reports;
    protected SpecifyUser       specifyUser;

 
    /**
     * 
     */
    public SpQuery()
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spQueryId        = null;
        name             = null;
        contextName      = null;
        contextTableId   = null;
        sqlStr           = null;
        named            = true;
        fields           = new HashSet<SpQueryField>();
        reports          = new HashSet<SpReport>();
        specifyUser      = null;
        isFavorite       = null;
        ordinal          = null;
        remarks          = null;
        selectDistinct   = null;
        searchSynonymy   = null;
        countOnly        = null;
        smushed          = null;
        formatAuditRecIds = null;
    }
    
    
    /**
     * @param spQueryId the spQueryId to set
     */
    public void setSpQueryId(Integer spQueryId)
    {
        this.spQueryId = spQueryId;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param contextName the contextName to set
     */
    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }

    /**
     * @param contextTableId the contextTableId to set
     */
    public void setContextTableId(Short contextTableId)
    {
        this.contextTableId = contextTableId;
    }

    /**
     * @param sql the sql to set
     */
    public void setSqlStr(String sqlStr)
    {
        this.sqlStr = sqlStr;
    }

    /**
     * @param tables the fields to set
     */
    public void setFields(Set<SpQueryField> fields)
    {
        this.fields = fields;
    }
    
    public void setSpecifyUser(SpecifyUser owner)
    {
        this.specifyUser = owner;
    }
    
    public void setReports(Set<SpReport> reports)
    {
        this.reports = reports;
    }

    /**
     * @param isFavorite the isFavorite to set
     */
    public void setIsFavorite(Boolean isFavorite)
    {
        this.isFavorite = isFavorite;
    }
    
    /**
     * @param order the order to set
     */
    public void setOrdinal(Short order)
    {
        this.ordinal = order;
    }

    /**
     * @param smushed the smushed to smush
     */
    public void setSmushed(Boolean smushed) 
    {
    	this.smushed = smushed;
    }

    /**
     * @param formatAuditRecIds the formatAuditRecIds to set
     */
    public void setFormatAuditRecIds(Boolean formatAuditRecIds)
    {
        this.formatAuditRecIds = formatAuditRecIds;
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
     * @return the spQueryId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpQueryID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpQueryId()
    {
        return spQueryId;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 256)
    public String getName()
    {
        return name;
    }

    /**
     * @return the contextName
     */
    @Column(name = "ContextName", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getContextName()
    {
        return contextName;
    }

    /**
     * @return the contextTableId
     */
    @Column(name = "ContextTableId", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getContextTableId()
    {
        return contextTableId;
    }

    /**
     * @return the sql
     */
    @Lob
    @Column(name = "SqlStr", length = 4096)
    public String getSqlStr()
    {
        return sqlStr;
    }

    /**
     * @return the isFavorite
     */
    @Column(name = "IsFavorite", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsFavorite()
    {
        return isFavorite == null ? true : isFavorite;
    }

    /**
     * @return the formatAuditRecIds
     */
    @Column(name = "FormatAuditRecIds", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getFormatAuditRecIds()
    {
        return formatAuditRecIds == null ? false : formatAuditRecIds;
    }

    /**
     * @return the smushed
     */
    @Column(name = "Smushed", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getSmushed()
    {
        return smushed == null ? false : smushed;
    }

    /**
     * @return selectDistinct if non-null else false.
     */
    @Transient
    public boolean isSelectDistinct()
    {
    	return selectDistinct == null ? false : selectDistinct;
    }
    
    /**
	 * @return the selectDistinct
	 */
    @Column(name = "SelectDistinct", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getSelectDistinct() 
	{
		return selectDistinct == null ? false : selectDistinct;
	}

	/**
	 * @param selectDistinct the selectDistinct to set
	 */
	public void setSelectDistinct(Boolean selectDistinct) 
	{
		this.selectDistinct = selectDistinct;
	}

	/**
	 * @return the searchSynonymy
	 */
    @Column(name = "SearchSynonymy", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getSearchSynonymy() 
	{
		return searchSynonymy == null ? false : searchSynonymy;
	}

	/**
	 * @param searchSynonymy the searchSynonymy to set
	 */
	public void setSearchSynonymy(Boolean searchSynonymy) 
	{
		this.searchSynonymy = searchSynonymy;
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Override
    @Transient
    public boolean isChangeNotifier()
    {
        return false;
    }

    /**
	 * @return the countOnly
	 */
	@Column(name = "CountOnly", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getCountOnly()
	{
		return countOnly == null ? false : countOnly;
	}

	/**
	 * @param countOnly the countOnly to set
	 */
	public void setCountOnly(Boolean countOnly)
	{
		this.countOnly = countOnly;
	}

	/**
     * @return the order
     */
    @Column(name = "Ordinal", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getOrdinal()
    {
        return ordinal != null ? ordinal : Short.MAX_VALUE;
    }

    /**
     * @return the fields
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "query")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpQueryField> getFields()
    {
        return fields;
    }

    /**
     * @return the reports
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "query")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpReport> getReports()
    {
        return reports;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser() 
    {
        return this.specifyUser;
    }
    
    /**
     * @param forceReports
     * 
     * Assuming object is attached to an open session, loads lazy-loaded members.
     */
    public void forceLoad(boolean forceReports)
    {
        for (SpQueryField qf : getFields())
        {
            qf.getFieldName();
        }
        getSpecifyUser();
        if (forceReports)
        {
            for (SpReport r: getReports())
            {
                r.forceLoad();
            }
        }
    }
    
    
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
	 */
	@Override
	public void forceLoad()
	{
		forceLoad(false);
	}

	/**
     * @param sb
     */
    public void toXML(final StringBuilder sb)
    {
        sb.append("<query ");
        addAttr(sb, "name", name);
        addAttr(sb, "contextName", contextName);
        addAttr(sb, "contextTableId", contextTableId);
        addAttr(sb, "isFavorite", isFavorite);
        addAttr(sb, "named", named);
        addAttr(sb, "ordinal", ordinal);
        addAttr(sb, "appversion", UIRegistry.getAppVersion());
        addAttr(sb, "smushed", smushed);
        addAttr(sb, "searchSynonymy", searchSynonymy);
        addAttr(sb, "selectDistinct", selectDistinct);
        addAttr(sb, "countOnly", countOnly);
        addAttr(sb, "formatAuditRecIds", formatAuditRecIds);
        
        sb.append(">\r\n");
        
        if (sqlStr != null)
        {
            sb.append("<sqlStr><![CDATA[");
            sb.append(sqlStr);
            sb.append("]]></sqlStr>\r\n");
        }
        
        sb.append("<fields>");
        for (SpQueryField field : fields)
        {
            field.toXML(sb);
        }
        sb.append("</fields>\r\n");
        
        SpExportSchemaMapping mapping = getMapping();
        if (mapping != null)
        {
        	mapping.toXML(sb);
        }
        sb.append("</query>\r\n");
    }
    
    /**
     * @param element
     */
    public void fromXML(final Element element)
    {
        name            = getAttr(element, "name", null);
        contextName     = getAttr(element, "contextName", null);
        contextTableId  = getAttr(element, "contextTableId", (short)0);
        isFavorite      = getAttr(element, "isFavorite", false);
        named           = getAttr(element, "named", false);
        ordinal         = getAttr(element, "ordinal", (short)0);
        smushed         = getAttr(element, "smushed", false);
        searchSynonymy   = getAttr(element, "searchSynonymy", false);
        selectDistinct   = getAttr(element, "selectDistinct", false);
        countOnly   = getAttr(element, "countOnly", false);
        formatAuditRecIds = getAttr(element, "formatAuditRecIds", false);
        
        Element sqlNode = (Element)element.selectSingleNode("sqlStr");
        sqlStr = sqlNode != null ? sqlNode.getTextTrim() : null;
        
        for (Object obj : element.selectNodes("fields/field"))
        {
            Element fieldEl = (Element)obj;
            SpQueryField field = new SpQueryField();
            field.initialize();
            field.fromXML(fieldEl);
            field.setQuery(this);
            fields.add(field);
        }
        
        Element mapping = (Element )element.selectSingleNode("spexportschemamapping");
        if (mapping != null)
        {
        	SpExportSchemaMapping esm = new SpExportSchemaMapping();
        	esm.initialize();
        	esm.fromXML(mapping, this);
        }
    }
    
    /**
     * @return the SpExportSchemaMapping object for this query, if it exists.
     */
    @Transient
    public SpExportSchemaMapping getMapping()
    {
    	for (SpQueryField field : fields)
    	{
    		SpExportSchemaItemMapping mapItem = field.getMapping();
    		if (mapItem != null)
    		{
    			return mapItem.getExportSchemaMapping();
    		}
    	}
    	return null;
    }
    
    //----------------------------------------------------------------------
    //-- DataModelObjBase
    //----------------------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpQuery.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spQueryId;
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
        return 517;
    }

    /**
     * @return the named
     */
    @Transient
    public boolean isNamed()
    {
        return named;
    }

    /**
     * @param named the named to set
     */
    public void setNamed(boolean named)
    {
        this.named = named;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        SpQuery query = (SpQuery)super.clone();
        
        query.spQueryId = null;
        
        fields = new HashSet<SpQueryField>();
        for (SpQueryField field : fields)
        {
            SpQueryField fld = (SpQueryField)field.clone();
            fld.setQuery(query);
            query.fields.add(fld);
        }
         
        return query;
    }

    
	@Override
	@Transient
	public String getIdentityTitle()
	{
		return getName();
	}

	/**
	 * @param obj
	 * @return true if obj has same fields (in the same order) and tablecontext
	 * forceLoad() may need to be called for this query and q.
	 */
	public boolean isEquivalent(final SpQuery q)
	{
		if (!contextTableId.equals(q.getContextTableId()))
		{
			return false;
		}
		
		if (fields.size() != q.fields.size())
		{
			return false;
		}
		
		//gross.
		Vector<SpQueryField> theseFields = new Vector<SpQueryField>(fields);
		Vector<SpQueryField> thoseFields = new Vector<SpQueryField>(q.fields);
		//sort by column position
		Collections.sort(theseFields);
		Collections.sort(thoseFields);
		for (int f = 0; f < theseFields.size(); f++)
		{
			if (!theseFields.get(f).isEquivalent(thoseFields.get(f)))
			{
				return false;
			}
		}
		return true;
	}

    
}

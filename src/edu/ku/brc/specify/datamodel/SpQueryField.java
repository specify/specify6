/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 17, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spqueryfield")
public class SpQueryField extends DataModelObjBase implements Comparable<SpQueryField>, Cloneable
{
    protected static final Logger                   log = Logger.getLogger(SpQueryField.class);

    public static final Byte SORT_NONE = 0;
    public static final Byte SORT_ASC  = 1;
    public static final Byte SORT_DESC = 2;
    
    public enum SortType 
    {
        NONE(0),
        ASC(1),
        DESC(2);
        
        SortType(final int ord)
        { 
            this.ord = (byte)ord;
        }
        private byte ord;
        public  byte getOrdinal()         { return ord; }
        public  void set(final byte  ord) { this.ord = ord; }
        public  static String getString(final byte ord)
        {
            String[] names = {"None", "Ascending", "Descending"};
            return names[ord];
        }
        public static SortType valueOf(Byte ord) { return SortType.valueOf(ord.toString()); } 
    }
    
    public enum OperatorType 
    {
        LIKE(0),
        EQUALS(1),
        GREATERTHAN(2),
        LESSTHAN(3),
        GREATERTHANEQUALS(4),
        LESSTHANEQUALS(5),
        TRUE(6),
        FALSE(7),
        DONTCARE(8),
        BETWEEN(9),
        IN(10),
        CONTAINS(11),
        EMPTY(12);
        
        OperatorType(final int ord)
        { 
            this.ord = (byte)ord;
        }
        private byte ord;
        private static final String[] names = {"Like", "=", ">", "<", ">=", "<=", 
            UIRegistry.getResourceString("true"), 
            UIRegistry.getResourceString("false"),
            " ", UIRegistry.getResourceString("QB_BETWEEN"),
            UIRegistry.getResourceString("QB_IN"),
            UIRegistry.getResourceString("QB_CONTAINS"),
            UIRegistry.getResourceString("QB_EMPTY")};

        public  byte getOrdinal()         { return ord; }
        public  void set(final byte  ord) { this.ord = ord; }
        public  static String getString(final byte ord)
        {
            return names[ord];
        }
        public static OperatorType valueOf(Byte ord) { return OperatorType.valueOf(ord.toString()); }
        public static byte getOrdForName(final String name)
        {
            for (byte o = 0; o < names.length; o++)
            {
                if (names[o].equals(name))
                {
                    return o;
                }
            }
            return -1;
        }
        @Override
        public String toString()
        {
            return names[ord];
        }
    }
    
    protected Integer      spQueryFieldId;
    protected Short        position;      
    protected String       fieldName;
    protected Boolean      isNot;
    protected Boolean      isDisplay;
    protected Boolean      isPrompt;  //whether or not criteria for this field is requested when it's query is run
                                      //outside the querybuilder context.
    protected Boolean      isRelFld;  //true if this field represents a relationship 
                                      //(the data object or objects on the 'other' side of the relationship)
    protected Boolean      alwaysFilter; //true if criteria for this field should be applied in all situations, i.e. even
                                        //when query content is provided by a list of ids.
    
    protected String	   stringId; //unique name for the field within it's query
    
    protected Byte         operStart;
    protected Byte         operEnd;
    protected String       startValue;
    protected String       endValue;
    protected Byte         sortType;
    
    protected String       tableList;
    
    protected Set<SpExportSchemaItemMapping> mappings; //This a set to provide support the theoretical capability to
    													//map a single field to more than one concept, which, I think,
                                                        //is supported by TAPIR.
    
    /**
     * The tableId of the table that contains the database field represented by this object.
     */
    protected Integer      contextTableIdent; 
    /**
     * Unique column name among query's other fields.
     */
    protected String       columnAlias;
    
    /**
     * The name of the formatter to use for formatted or aggregated fields. 
     * (if null then default formatter is used)
     */
    protected String       formatName;
    
    protected SpQuery      query;
    
    /**
     * 
     */
    public SpQueryField()
    {
        // no op
    }

    /**
     * @param spQueryItemId the spQueryItemId to set
     */
    public void setSpQueryFieldId(Integer spQueryFieldId)
    {
        this.spQueryFieldId = spQueryFieldId;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(Short position)
    {
        this.position = position;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @param stringId the stringId to set
     */
    public void setStringId(String stringId)
    {
    	this.stringId = stringId;
    }
    
    /**
	 * @param formatName the formatName to set
	 */
	public void setFormatName(String formatName)
	{
		this.formatName = formatName;
	}

	/**
     * @param isNot the isNot to set
     */
    public void setIsNot(Boolean isNot)
    {
        this.isNot = isNot;
    }

    /**
     * @param isDisplay the isDisplay to set
     */
    public void setIsDisplay(Boolean isDisplay)
    {
        this.isDisplay = isDisplay;
    }

    /**
     * @param isPrompt the isPrompt to set
     */
    public void setIsPrompt(Boolean isPrompt)
    {
        this.isPrompt = isPrompt;
    }

    /**
     * @param alwaysFilter the alwaysFilter to set
     */
    public void setAlwaysFilter(Boolean alwaysFilter)
    {
        this.alwaysFilter = alwaysFilter;
    }

    /**
     * @param isRelFld the isRelFld to set
     */
    public void setIsRelFld(Boolean isRelFld)
    {
        this.isRelFld = isRelFld;
    }

    /**
     * @param operStart the operStart to set
     */
    public void setOperStart(Byte operStart)
    {
        this.operStart = operStart;
    }

    /**
     * @param operEnd the operEnd to set
     */
    public void setOperEnd(Byte operEnd)
    {
        this.operEnd = operEnd;
    }

    /**
     * @param startValue the startValue to set
     */
    public void setStartValue(String startValue)
    {
        this.startValue = startValue;
    }

    /**
     * @param endValue the endValue to set
     */
    public void setEndValue(String endValue)
    {
        this.endValue = endValue;
    }

    /**
     * @param sortType the sortType to set
     */
    public void setSortType(Byte sortType)
    {
        this.sortType = sortType;
    }

    /**
     * @param table the table to set
     */
    public void setQuery(SpQuery query)
    {
        this.query = query;
    }

    /**
     * @param tableList the tableList to set
     */
    public void setTableList(String tableList)
    {
        this.tableList = tableList;
    }

    /**
     * @return the spQueryItemId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpQueryFieldID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpQueryFieldId()
    {
        return spQueryFieldId;
    }

    /**
     * @return the position
     */
    @Column(name = "Position", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getPosition()
    {
        return position;
    }

    /**
     * @return the fieldName
     */
    @Column(name = "FieldName", unique = false, nullable = false, insertable = true, updatable = true, length = 32)
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @return the stringId
     */
    @Column(name = "StringId", unique = false, nullable = false, insertable = true, updatable = true, length = 500)
    public String getStringId()
    {
        return stringId;
    }

    
    /**
	 * @return the formatName
	 */
    @Column(name = "FormatName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getFormatName()
	{
		return formatName;
	}

	/**
     * @return the isNot
     */
    @Column(name = "IsNot", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsNot()
    {
        return isNot;
    }

    /**
     * @return the isDisplay
     */
    @Column(name = "IsDisplay", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsDisplay()
    {
        return isDisplay;
    }

    /**
     * @return the isPrompt
     */
    @Column(name = "IsPrompt", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsPrompt()
    {
        return isPrompt;
    }

    /**
     * @return the isRelFld
     */
    @Column(name = "IsRelFld", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsRelFld()
    {
        return isRelFld;
    }

    /**
     * @return the alwasyFilter
     */
    @Column(name = "AlwaysFilter", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getAlwaysFilter()
    {
        return alwaysFilter;
    }

    /**
     * @return the operStart
     */
    @Column(name = "OperStart", unique = false, nullable = false, insertable = true, updatable = true)
    public Byte getOperStart()
    {
        return operStart;
    }

    /**
     * @return the operEnd
     */
    @Column(name = "OperEnd", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getOperEnd()
    {
        return operEnd;
    }

    /**
     * @return the startValue
     */
    @Column(name = "StartValue", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getStartValue()
    {
        return startValue;
    }

    /**
     * @return the endValue
     */
    @Column(name = "EndValue", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getEndValue()
    {
        return endValue;
    }

    /**
     * @return the sortType
     */
    @Column(name = "SortType", unique = false, nullable = false, insertable = true, updatable = true)
    public Byte getSortType()
    {
        return sortType;
    }

    /**
     * @return the table
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpQueryID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpQuery getQuery()
    {
        return query;
    }

    /**
     * @return the fields
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "queryField")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<SpExportSchemaItemMapping> getMappings()
    {
        return mappings;
    }

    /**
     * @return the tableList
     */
    @Column(name = "TableList", unique = false, nullable = false, insertable = true, updatable = true, length = 500)
    public String getTableList()
    {
        return tableList;
    }
    
    /**
     * @param mapping the mapping to set
     * 
     * Sets mapping to be the single mapping for this SpQueryField.
     */
    public void setMapping(SpExportSchemaItemMapping mapping)
    {
//    	for (SpExportSchemaItemMapping currentMapping : mappings)
//    	{
//    		currentMapping.setExportSchemaMapping(null);
//    	}
    	mappings.clear();
    	if (mapping != null)
    	{
    		mappings.add(mapping);
    	}
    }
    
    /**
     * @return the first mapping. 
     */
    @Transient
    public SpExportSchemaItemMapping getMapping()
    {
    	if (mappings.size() > 0)
    	{
    		if (mappings.size() > 1)
    		{
    			log.warn("getMappig() was called for object with more than one mapping.");
    		}
    		return mappings.iterator().next();
    	}
    	return null;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spQueryFieldId = null;
        position       = null;
        fieldName      = null;
        isNot          = null;
        isDisplay      = null;
        isPrompt       = null;
        alwaysFilter   = null;
        isRelFld       = null;        
        operStart      = null;
        operEnd        = null;
        startValue     = null;
        endValue       = null;
        sortType       = null;
        query          = null;
        tableList      = null;
        stringId       = null;
        formatName     = null;
        columnAlias    = null;
        contextTableIdent = null;
        mappings = new HashSet<SpExportSchemaItemMapping>();
    }

    @Transient
    public OperatorType getStartOperator()
    {
        return OperatorType.valueOf(operStart);
    }
    
    @Transient
    public OperatorType getEndOperator()
    {
        return OperatorType.valueOf(operEnd);
    }
    
    @Transient
    public SortType getSort()
    {
        return SortType.valueOf(sortType);
    }
    
    public void setStartOper(OperatorType oper)
    {
        operStart = oper.getOrdinal();
    }
    
    public void setEndOper(OperatorType oper)
    {
        operEnd = oper.getOrdinal();
    }
    
    public void setSort(SortType sortType)
    {
        this.sortType = sortType.getOrdinal();
    }
            
    public void setMappings(Set<SpExportSchemaItemMapping> mappings)
    {
    	this.mappings = mappings;
    }
    
    //-------------------------------------------------------------------------
    //-- DataModelObjBase
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpQueryField.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spQueryFieldId;
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 518;
    }

    public int compareTo(SpQueryField o)
    {
        return position != null && o != null && o.position != null ? position.compareTo(o.position) : 0;
    }

    /**
     * @return the contextTableId
     */
    @Column(name = "ContextTableIdent", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getContextTableIdent()
    {
        return contextTableIdent;
    }

    /**
     * @param contextTableId the contextTableId to set
     */
    public void setContextTableIdent(Integer contextTableIdent)
    {
        this.contextTableIdent = contextTableIdent;
    }

    /**
     * @return the columnAlias
     */
    @Column(name = "ColumnAlias", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getColumnAlias()
    {
        return columnAlias;
    }

    /**
     * @return the columnAlias with the field's current title substituted for it's name.
     */
    @Transient
    public String getColumnAliasTitle()
    {
    	if (columnAlias == null)
    	{
    		columnAlias = fieldName;
    	}
    	DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(contextTableIdent);
    	if (tbl != null)
    	{
    		DBFieldInfo fld = tbl.getFieldByName(fieldName);
    		if (fld != null)
    		{
    			int nameIdx = columnAlias.lastIndexOf(fld.getName());
    			if (nameIdx >= 0)
    			{
    				int nameEndIdx = nameIdx + fld.getName().length();
    				return columnAlias.substring(0, nameIdx) + fld.getTitle() + columnAlias.substring(nameEndIdx);
    			}
    		}
    	}
    	log.error("Returning unprocessed columnAlias because FieldInfo was not found: " + columnAlias);
    	return columnAlias;
    }
    /**
     * @param columnAlias the columnAlias to set
     */
    public void setColumnAlias(String columnAlias)
    {
        this.columnAlias = columnAlias;
    }
    
    /**
     * @param columnAliasTitle
     * 
     * Substitutes the field's name for it's title in columnAliasTitle and sets columnAlias.
     */
    public void setColumnAliasTitle(String columnAliasTitle)
    {
    	DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(contextTableIdent);
    	if (tbl != null)
    	{
    		DBFieldInfo fld = tbl.getFieldByName(fieldName);
    		if (fld != null)
    		{
    			int nameIdx = columnAliasTitle.lastIndexOf(fld.getTitle());
    			//XXX - if the title for a field is changed while a query is open
    			//columnAliasTitle might contain the fld's old title???
    			if (nameIdx >= 0)
    			{
    				int nameEndIdx = nameIdx + fld.getTitle().length();
    				setColumnAlias(columnAliasTitle.substring(0, nameIdx) 
    						+ fld.getName() + columnAliasTitle.substring(nameEndIdx));
    				return;
    			}
    		}
    	}
    	log.error("Setting unprocessed alias because FieldInfo was not found: " + columnAliasTitle);
    	setColumnAlias(columnAliasTitle);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        SpQueryField field = (SpQueryField)super.clone();
        field.init();
        
        return field;
    }
    
	/**
     * @param sb
     */
    public void toXML(final StringBuilder sb)
    {
        sb.append("<field ");
        addAttr(sb, "position",   position);
        addAttr(sb, "fieldName",  fieldName);
        addAttr(sb, "isNot",      isNot);
        addAttr(sb, "isDisplay",  isDisplay);
        addAttr(sb, "isPrompt",   isPrompt);
        addAttr(sb, "isRelFld",   isRelFld);
        addAttr(sb, "alwaysFilter", alwaysFilter);
        addAttr(sb, "stringId",   stringId);
        addAttr(sb, "operStart",  operStart);
        addAttr(sb, "operEnd",    operEnd);
        addAttr(sb, "startValue", startValue);
        addAttr(sb, "endValue",   endValue);
        addAttr(sb, "sortType",   sortType);
        addAttr(sb, "tableList",  tableList);
        addAttr(sb, "contextTableIdent", contextTableIdent);
        addAttr(sb, "columnAlias", columnAlias);
        
        sb.append(" />\n");
    }
    
    /**
     * @param tblInfo
     * @param fieldToSetName
     * @param fieldToSet
     * @param value
     * 
     * Sets value modifying it if necessary to make it valid. Only check currently is on string lengths.
     */
    protected void setValue(DBTableInfo tblInfo, String fieldToSetName, Object value)
    {
    	try
    	{
    		DBFieldInfo fldInfo = tblInfo.getFieldByName(fieldToSetName);
    		String fieldSetterName = "set" + UploadTable.capitalize(fieldToSetName);
    		Method fieldSetter = SpQueryField.class.getMethod(fieldSetterName, fldInfo.getDataClass());
    		if (fldInfo.getDataClass().equals(String.class) && value != null)
    		{    			
    			String strVal = (String )value;
    			if (strVal.length() > fldInfo.getLength())
    			{
    				fieldSetter.invoke(this, ((String )value).substring(0, fldInfo.getLength()));
    				return;
    			}
    		}
    		fieldSetter.invoke(this, value);
    	} catch (Exception ex)
    	{
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpQueryField.class, ex);
            ex.printStackTrace();
    	}
    }
    
    public void fromXML(Element element)
    {
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(getTableId());
    	setValue(tblInfo, "position", getAttr(element, "position", (short)0));
        setValue(tblInfo, "fieldName", getAttr(element, "fieldName", null));
        setValue(tblInfo, "isNot", getAttr(element, "isNot", false));
        setValue(tblInfo, "isDisplay", getAttr(element, "isDisplay", false));
        setValue(tblInfo, "isPrompt", getAttr(element, "isPrompt", false));
        setValue(tblInfo, "isRelFld", getAttr(element, "isRelFld", false));
        setValue(tblInfo, "alwaysFilter", getAttr(element, "alwaysFilter", false));
		//XXX there is a problem with TreeLevels stringId properties.
		//Currently, the level 'names' are used in the stringId and there are potential problems with i18n.
		//If the rankIds are used then there may be problems with 'custom' levels
		//For the taxon and geography tress, problems will be minimal to nonexistent 
        //if the Specify6 Standard levels are used.
		//For Storage and other trees without standards there are likely to be many 
        //issues with imports/exports
        setValue(tblInfo, "stringId", getAttr(element, "stringId", null));
        setValue(tblInfo, "operStart", getAttr(element, "operStart", (byte)0));
        setValue(tblInfo, "operEnd", getAttr(element, "operEnd", (byte)0));
        if (operEnd.byteValue() == 0)
        {
        	operEnd = null;
        }
        setValue(tblInfo, "startValue", getAttr(element, "startValue", null));
        setValue(tblInfo, "endValue", getAttr(element, "endValue", null));
        setValue(tblInfo, "sortType", getAttr(element, "sortType", (byte)0));
        setValue(tblInfo, "tableList", getAttr(element, "tableList", null));
        setValue(tblInfo, "contextTableIdent", getAttr(element, "contextTableIdent", 0));
        setValue(tblInfo, "columnAlias", getAttr(element, "columnAlias", null));
    }

    /**
     * @param obj1
     * @param obj2
     * @return
     */
    protected boolean eq(final Object obj1, final Object obj2)
    {
    	if (obj1 == null && obj2 == null)
    	{
    		return true;
    	}
    	if (obj1 == null || obj2 == null)
    	{
    		return false;
    	}
    	return obj1.equals(obj2);
    }
    
    /**
     * @param val
     * @return
     */
    protected String nullIfBlank(String val)
    {
    	if (StringUtils.isEmpty(val))
    	{
    		return null;
    	}
    	return val;
    }
    
    /**
     * @param val
     * @return
     */
    protected Number nullIfZero(Number val)
    {
    	if (val != null && val.equals(0))
    	{
    		return null;
    	}
    	return val;
    }

	/**
	 * @param obj
	 * @return true if this and obj represent the same database field with the same relationship to the
	 * root query table, parameters.  and options.
	 */
	public boolean isEquivalent(Object obj)
	{
		if (obj instanceof SpQueryField)
		{
			SpQueryField f = (SpQueryField )obj;
			return fieldName.equals(f.fieldName)
				&& isNot.equals(f.isNot)
				&& isDisplay.equals(f.isDisplay)
				&& isPrompt.equals(f.isPrompt)
				&& alwaysFilter.equals(f.alwaysFilter)
				&& eq(nullIfZero(operStart), nullIfZero(f.operStart))
				&& eq(nullIfZero(operEnd), nullIfZero(f.operEnd))
				&& eq(nullIfBlank(startValue), nullIfBlank(f.startValue))
				&& eq(nullIfBlank(endValue), nullIfBlank(f.endValue))
				&& eq(nullIfZero(sortType), nullIfZero(f.sortType))
				&& eq(nullIfBlank(tableList), nullIfBlank(f.tableList))
				&& eq(nullIfZero(contextTableIdent), nullIfZero(f.contextTableIdent))
				&& eq(nullIfBlank(columnAlias), nullIfBlank(f.columnAlias));
		}
		return false;
	}

    
}

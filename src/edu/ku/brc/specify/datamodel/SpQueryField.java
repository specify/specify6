/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.specify.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

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
public class SpQueryField extends DataModelObjBase implements Comparable<SpQueryField>
{
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
        CONTAINS(11);
        
        OperatorType(final int ord)
        { 
            this.ord = (byte)ord;
        }
        private byte ord;
        private static final String[] names = {"Like", "=", ">", "<", ">=", "<=", 
            UIRegistry.getResourceString("WB_TRUE"), 
            UIRegistry.getResourceString("WB_FALSE"),
            " ", UIRegistry.getResourceString("QB_BETWEEN"),
            UIRegistry.getResourceString("QB_IN"),
            UIRegistry.getResourceString("QB_CONTAINS")};

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
    protected Short         position;      
    protected String       fieldName;
    protected Boolean      isNot;
    protected Boolean      isDisplay;
    protected Boolean      isPrompt;  //whether or not criteria for this field is requested when it's query is run
                                      //outside the querybuilder context.
    protected Boolean      isRelFld;  //true if this field represents a relationship 
                                      //(the data object or objects on the 'other' side of the relationship)
    
    protected Byte         operStart;
    protected Byte         operEnd;
    protected String       startValue;
    protected String       endValue;
    protected Byte         sortType;
    
    protected String       tableList;
    
    /**
     * The tableId of the table that contains the database field represented by this object.
     */
    protected Integer      contextTableIdent; 
    /**
     * Unique column name among query's other fields.
     */
    protected String       columnAlias;
    
    protected SpQuery      query;
    
    // Transient
    protected int[]        tableIds = null;

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
        this.tableIds  = null;
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
     * @return the tableList
     */
    @Column(name = "TableList", unique = false, nullable = false, insertable = true, updatable = true, length = 255)
    public String getTableList()
    {
        return tableList;
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
        operStart      = null;
        operEnd        = null;
        startValue     = null;
        endValue       = null;
        sortType       = null;
        query          = null;
        tableList      = null;
        columnAlias    = null;
        contextTableIdent = null;
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
    
    /**
     * @return
     */
    @Transient
    public int[] getTableIds()
    {
        if (tableIds == null)
        {
            if (StringUtils.isNotEmpty(tableList))
            {
                String[] toks = StringUtils.split(tableList, ',');
                tableIds = new int[toks.length];
                int i = 0;
                for (String tok : StringUtils.split(tableList, ','))
                {
                    tableIds[i++] = Integer.parseInt(tok);
                }
            }
        }
        return tableIds;
    }
    
    /**
     * @param tablesIds
     */
    public void setTableIds(final int[] tablesIds)
    {
        this.tableIds = tablesIds;
        if (tablesIds != null && tablesIds.length > 0)
        {
            StringBuilder sb = new StringBuilder();
            for (int id : tablesIds)
            {
                if (sb.length() > 0) sb.append(",");
                sb.append(id);
            }
            tableList = sb.toString();
        }
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
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 518;
    }

    public int compareTo(SpQueryField o)
    {
        return position.compareTo(o.position);
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
     * @param columnAlias the columnAlias to set
     */
    public void setColumnAlias(String columnAlias)
    {
        this.columnAlias = columnAlias;
    }
    
    
}

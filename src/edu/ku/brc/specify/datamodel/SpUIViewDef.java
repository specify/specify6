/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import static edu.ku.brc.ui.forms.persist.View.xmlAttr;
import static edu.ku.brc.ui.forms.persist.View.xmlNode;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.DataObjectSettable;
import edu.ku.brc.ui.forms.persist.FormCellIFace;
import edu.ku.brc.ui.forms.persist.FormColumnIFace;
import edu.ku.brc.ui.forms.persist.FormRowIFace;
import edu.ku.brc.ui.forms.persist.FormViewDefIFace;
import edu.ku.brc.ui.forms.persist.TableViewDefIFace;
import edu.ku.brc.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.ui.forms.persist.ViewLoader;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 25, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spuiviewdef")
@org.hibernate.annotations.Table(appliesTo="spuiviewdef", indexes =
    {   @Index (name="SpUIViewDefNameIDX", columnNames={"Name"})
    })
public class SpUIViewDef extends DataModelObjBase implements ViewDefIFace, TableViewDefIFace, FormViewDefIFace
{
    private static final Logger  log       = Logger.getLogger(SpUIViewDef.class);
    
    protected Integer          spUIViewDefId;
    protected String           typeName;
    protected String           name;
    protected String           dataClassName;
    protected String           gettableName;
    protected String           settableName;
    protected String           description;        // Memo
    protected String           enableRulesXML;     // Memo (XML)
    protected String           colDef;
    protected String           rowDef;
    protected Boolean          isAbsoluteLayout;
    protected String           definitionName;// formtable needs this

    protected Set<SpUIRow>     spRows;
    protected Set<SpUIColumn>  spCols;
    protected Set<SpUIAltView> spAltViews;
    protected SpUIViewSet      spViewSet;
    
    protected Short            xCoordDB;
    protected Short            yCoordDB;
    protected Short            heightDB;
    protected Short            widthDB;   
    
    // Transient
    protected DataObjectGettable gettable = null;
    protected DataObjectSettable settable = null;
    
    protected Vector<FormColumnIFace>   columns  = null;
    protected Hashtable<String, String> enableRules;
    
    /**
     * 
     */
    public SpUIViewDef()
    {
        // No Op
    }
    
    /**
     * @return the spUIViewDefId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpUIViewID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpUIViewDefId()
    {
        return spUIViewDefId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    @Transient
    public void initialize()
    {
        super.init();
        
        spUIViewDefId  = null;
        typeName       = null;
        name           = null;
        dataClassName  = null;
        gettableName   = null;
        settableName   = null;
        description    = null;
        enableRulesXML = null;
        colDef         = null;
        rowDef         = null;
        isAbsoluteLayout = false;
        definitionName = null;
        
        xCoordDB           = null;
        yCoordDB           = null;
        widthDB            = null;
        heightDB           = null;

        spRows      = new HashSet<SpUIRow>();
        spCols      = new HashSet<SpUIColumn>();
        spAltViews    = new HashSet<SpUIAltView>();
        spViewSet     = null;
    }

    /**
     * @return the colDef
     */
    @Column(name = "ColDef", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getColDef()
    {
        return colDef;
    }

    /**
     * @param colDef the colDef to set
     */
    public void setColDef(String colDef)
    {
        this.colDef = colDef;
    }

    /**
     * @return the dataClassName
     */
    @Column(name = "DataClassName", unique = false, nullable = false, insertable = true, updatable = true, length = 128)
    public String getDataClassName()
    {
        return dataClassName;
    }

    /**
     * @param dataClassName the dataClassName to set
     */
    public void setDataClassName(String dataClassName)
    {
        this.dataClassName = dataClassName;
    }

    /**
     * @return the desc
     */
    @Lob
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getDescription()
    {
        return description;
    }

    /**
     * @param desc the desc to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the enableRules
     */
    @Lob
    @Column(name = "EnableRulesXML", unique = false, nullable = true, insertable = true, updatable = true, length = 4098)
    public String getEnableRulesXML()
    {
        return enableRulesXML;
    }

    /**
     * @param enableRules the enableRules to set
     */
    public void setEnableRulesXML(String enableRulesXML)
    {
        this.enableRulesXML = enableRulesXML;
        
        this.enableRules = null; // clear the cache is it has one
    }

    /**
     * @return the gettableName
     */
    @Column(name = "GettableName", unique = false, nullable = false, insertable = true, updatable = true, length = 128)
    public String getGettableName()
    {
        return gettableName;
    }

    /**
     * @param gettableName the gettableName to set
     */
    public void setGettableName(String gettableName)
    {
        this.gettableName = gettableName;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the rowDef
     */
    @Column(name = "RowDef", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getRowDef()
    {
        return rowDef;
    }

    /**
     * @param rowDef the rowDef to set
     */
    public void setRowDef(String rowDef)
    {
        this.rowDef = rowDef;
    }
    /**
     * @return the settableName
     */
    @Column(name = "SettableName", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getSettableName()
    {
        return settableName;
    }

    /**
     * @param settableName the settableName to set
     */
    public void setSettableName(String settableName)
    {
        this.settableName = settableName;
    }

    /**
     * @param spUIViewDefId the spUIViewDefId to set
     */
    public void setSpUIViewDefId(Integer spUIViewDefId)
    {
        this.spUIViewDefId = spUIViewDefId;
    }

    /**
     * @return the type
     */
    @Column(name = "Type", unique = false, nullable = false, insertable = true, updatable = true, length = 16)
    public String getTypeName()
    {
        return typeName;
    }

    /**
     * @param type the type to set
     */
    public void setTypeName(String type)
    {
        this.typeName = type;
    }

    /**
     * @return the isAbsoluteLayout
     */
    @Column(name = "IsAbsoluteLayout", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsAbsoluteLayout()
    {
        return isAbsoluteLayout == null ? false : isAbsoluteLayout;
    }

    /**
     * @param isAbsoluteLayout the isAbsoluteLayout to set
     */
    public void setIsAbsoluteLayout(Boolean isAbsoluteLayout)
    {
        this.isAbsoluteLayout = isAbsoluteLayout;
    }

    /**
     * @return the definition
     */
    @Column(name = "DefinitionName", unique = false, nullable = true, insertable = true, updatable = true)
    public String getDefinitionName()
    {
        return definitionName;
    }

    /**
     * @return the rows
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spViewDef")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpUIRow> getSpRows()
    {
        return spRows;
    }

    /**
     * @param rows the rows to set
     */
    public void setSpRows(Set<SpUIRow> spRows)
    {
        this.spRows = spRows;
    }

    /**
     * @return the spCols
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spViewDef")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpUIColumn> getSpCols()
    {
        return spCols;
    }

    /**
     * @param spCols the spCols to set
     */
    public void setSpCols(Set<SpUIColumn> spCols)
    {
        this.spCols = spCols;
    }

    /**
     * @return the altViews
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spViewDef")
    //@Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpUIAltView> getSpAltViews()
    {
        return spAltViews;
    }

    /**
     * @param altViews the altViews to set
     */
    public void setSpAltViews(Set<SpUIAltView> spAltViews)
    {
        this.spAltViews = spAltViews;
    }
    
    /**
     * @return the viewSet
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpUIViewSetID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpUIViewSet getSpViewSet()
    {
        return spViewSet;
    }

    /**
     * @param viewSet the viewSet to set
     */
    public void setSpViewSet(SpUIViewSet spViewSet)
    {
        this.spViewSet = spViewSet;
    }

    /**
     * @return the xCoordDB
     */
    @Column(name = "XCoord", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getXCoordDB()
    {
        return xCoordDB;
    }

    /**
     * @param coordDB the xCoordDB to set
     */
    public void setXCoordDB(Short coordDB)
    {
        xCoordDB = coordDB;
    }

    /**
     * @return the yCoordDB
     */
    @Column(name = "YCoord", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getYCoordDB()
    {
        return yCoordDB;
    }

    /**
     * @param coordDB the yCoordDB to set
     */
    public void setYCoordDB(Short coordDB)
    {
        yCoordDB = coordDB;
    }
    
    /**
     * @return the height
     */
    @Column(name = "Height", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getHeightDB()
    {
        return heightDB;
    }

    /**
     * @param height the height to set
     */
    public void setHeightDB(Short height)
    {
        this.heightDB = height;
    }

    /**
     * @return the width
     */
    @Column(name = "Width", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getWidthDB()
    {
        return widthDB;
    }

    /**
     * @param width the width to set
     */
    public void setWidthDB(Short width)
    {
        this.widthDB = width;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpUIViewDef.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spUIViewDefId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
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
        return 508;
    }

    //------------------------------------------------
    // ViewDefIFace
    //------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#cleanUp()
     */
    public void cleanUp()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getClassName()
     */
    @Transient
    public String getClassName()
    {
        return dataClassName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDataGettable()
     */
    @Transient
    public DataObjectGettable getDataGettable()
    {
        if (gettable == null && StringUtils.isNotEmpty(gettableName))
        {
            try
            {
                Class<?> cls = Class.forName(gettableName);
                return gettable = (DataObjectGettable)cls.newInstance();
                
            } catch (ClassNotFoundException ex)
            {
                log.error(ex);
            } catch (InstantiationException ex)
            {
                log.error(ex);
            } catch (IllegalAccessException ex)
            {
                log.error(ex);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDataGettableName()
     */
    @Transient
    public String getDataGettableName()
    {
        return gettableName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDataSettable()
     */
    @Transient
    public DataObjectSettable getDataSettable()
    {
        if (settable == null && StringUtils.isNotEmpty(settableName))
        {
            try
            {
                Class<?> cls = Class.forName(settableName);
                return settable = (DataObjectSettable)cls.newInstance();
                
            } catch (ClassNotFoundException ex)
            {
                log.error(ex);
            } catch (InstantiationException ex)
            {
                log.error(ex);
            } catch (IllegalAccessException ex)
            {
                log.error(ex);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDesc()
     */
    @Transient
    public String getDesc()
    {
        return description;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getType()
     */
    @Transient
    public ViewType getType()
    {
        return ViewType.valueOf(typeName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#setType(edu.ku.brc.ui.forms.persist.ViewDefIFace.ViewType)
     */
    public void setType(ViewType type)
    {
        typeName = type.name();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDerivedInterface()
     */
    @Transient
    public Class<?> getDerivedInterface()
    {
        return null;
    }
    
    //-----------------------------------------------------
    //-- TableViewDefIFace
    //-----------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.TableViewDefIFace#addColumn(edu.ku.brc.ui.forms.persist.FormColumn)
     */
    public FormColumnIFace addColumn(FormColumnIFace column)
    {
        if (columns == null)
        {
            columns = new Vector<FormColumnIFace>();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.TableViewDefIFace#getColumns()
     */
    @Transient
    public List<FormColumnIFace> getColumns()
    {
        return columns;
    }

    //-----------------------------------------------------
    //-- FormViewDefIFace
    //-----------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#addRow(edu.ku.brc.ui.forms.persist.FormRow)
     */
    public FormRowIFace addRow(FormRowIFace row)
    {
        if (row instanceof SpUIRow)
        {
            spRows.add((SpUIRow)row);
            ((SpUIRow)row).setSpViewDef(this);
        }
        return row;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getColumnDef()
     */
    @Transient
    public String getColumnDef()
    {
        return this.colDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getEnableRules()
     */
    @Transient
    public Hashtable<String, String> getEnableRules()
    {
        if (enableRules == null && StringUtils.isNotEmpty(enableRulesXML))
        {
            try
            {
                Element element = XMLHelper.readStrToDOM4J(enableRulesXML);
                if (element != null)
                {
                    enableRules = ViewLoader.getEnableRules(element);
                }
                
            } catch (Exception ex)
            {
                log.error(ex);
            }
        }
        return enableRules;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getFormCellById(java.lang.String)
     */
    @Transient
    public FormCellIFace getFormCellById(String idStr)
    {
        for (FormRowIFace row : spRows)
        {
            for (FormCellIFace c : row.getCells())
            {
                if (c.getIdent().equals(idStr))
                {
                    return c;
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getFormCellByName(java.lang.String)
     */
    @Transient
    public FormCellIFace getFormCellByName(String nameStr)
    {
        for (FormRowIFace row : spRows)
        {
            for (FormCellIFace c : row.getCells())
            {
                if (c.getName().equals(nameStr))
                {
                    return c;
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getRows()
     */
    @Transient
    public List<FormRowIFace> getRows()
    {
        // XXX Not the best approach
        return new Vector<FormRowIFace>(spRows);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setColumnDef(java.lang.String)
     */
    @Transient
    public void setColumnDef(String columnDef)
    {
        this.colDef = columnDef;
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setDefinitionName(java.lang.String)
     */
    public void setDefinitionName(String definitionName)
    {
        this.definitionName = definitionName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setEnableRules(java.util.Hashtable)
     */
    public void setEnableRules(Hashtable<String, String> enableRules)
    {
        this.enableRules = enableRules;
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#isAbsoluteLayout()
     */
    @Transient
    public Boolean isAbsoluteLayout()
    {
        return getIsAbsoluteLayout();
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getXCoord()
     */
    @Transient
    public int getXCoord()
    {
        return xCoordDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getYCoord()
     */
    @Transient
    public int getYCoord()
    {
        return yCoordDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setXCoord(int)
     */
    public void setXCoord(int xCoord)
    {
        xCoordDB = (short)xCoord;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setYCoord(int)
     */
    public void setYCoord(int yCoord)
    {
        yCoordDB = (short)yCoord;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getHeight()
     */
    @Transient
    public int getHeight()
    {
        return heightDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getWidth()
     */
    @Transient
    public int getWidth()
    {
        return widthDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setHeight(int)
     */
    public void setHeight(int height)
    {
        heightDB = (short)height;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setWidth(int)
     */
    public void setWidth(int width)
    {
        widthDB = (short)width;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDataSettableName()
     */
    public String getDataSettableName()
    {
        return settableName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#setDataGettableName(java.lang.String)
     */
    public void setDataGettableName(String dataGettableName)
    {
        this.gettableName = dataGettableName;
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#setDataSettableName(java.lang.String)
     */
    public void setDataSettableName(String dataSettableName)
    {
        this.settableName = dataSettableName;
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

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#compareTo(edu.ku.brc.ui.forms.persist.ViewDefIFace)
     */
    public int compareTo(ViewDefIFace obj)
    {
        return name.compareTo(obj.getName());
    }
    
    /**
     * @param enableRules
     * @return
     */
    protected String createEnableRulesXML(final Hashtable<String, String> enableRules)
    {
        if (enableRules.keySet().size() > 0)
        {
            StringBuilder sb = new StringBuilder("<enableRules>");
            for (String key : enableRules.keySet())
            {
                sb.append("<rule id=\"");
                sb.append(key);
                sb.append("\"><![CDATA[");
                sb.append(enableRules.get(key));
                sb.append("]]></rule>");
            }
            sb.append("</enableRules>");
            return sb.toString();
        }
        return null;
    }
    
    /**
     * Copy the values from the source into the object.
     * @param vd the source
     */
    public void copyInto(final ViewDefIFace vd)
    {
        typeName      = vd.getType().toString();
        name          = vd.getName();
        dataClassName = vd.getClassName();
        gettableName  = vd.getDataGettableName();
        settableName  = vd.getDataSettableName();
        description   = vd.getDesc();
        
        //if (vd instanceof TableViewDefIFace)
        //{
        //    TableViewDefIFace tvd = (TableViewDefIFace)vd;
        //}
        
        if (vd instanceof FormViewDefIFace)
        {
            FormViewDefIFace fvd = (FormViewDefIFace)vd;
            enableRulesXML = createEnableRulesXML(fvd.getEnableRules());
            definitionName = fvd.getDefinitionName();
            colDef         = fvd.getColumnDef();
            rowDef         = fvd.getRowDef();
        }
        isAbsoluteLayout = vd.isAbsoluteLayout();
        
        /*
        protected Set<SpUIRow>     spRows;
        protected Set<SpUIColumn>  spCols;
        protected Set<SpUIAltView> altViews;
        protected SpUIViewSet      viewSet;
        protected Vector<FormColumnIFace>   columns  = null;
        */
        
        xCoordDB = (short)vd.getXCoord();
        yCoordDB = (short)vd.getYCoord();
        heightDB = (short)vd.getHeight();
        widthDB  = (short)vd.getWidth();
        
        // Transient
        gettable = vd.getDataGettable();
        settable = vd.getDataSettable();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(StringBuffer sb)
    {
        /*
         <viewdef
            type="iconview"
            name="AccessionIconView"
            class="edu.ku.brc.specify.datamodel.Accession"
            gettable="edu.ku.brc.ui.forms.DataGetterForObj"
            settable="edu.ku.brc.ui.forms.DataSetterForObj">
            <desc><![CDATA[The Accession Agent Icon Viewer]]></desc>
        </viewdef>
         */

        sb.append("  <viewdef ");
        xmlAttr(sb, "type", typeName.toString());
        xmlAttr(sb, "name", name);
        xmlAttr(sb, "class", dataClassName);
        xmlAttr(sb, "gettable", gettableName);
        xmlAttr(sb, "settable", settableName);
        xmlAttr(sb, "width", widthDB);
        xmlAttr(sb, "height", heightDB);
        sb.append(">\n");
        xmlNode(sb, "desc", description, true);
        sb.append("</viewdef>");        
        
    }
    
}

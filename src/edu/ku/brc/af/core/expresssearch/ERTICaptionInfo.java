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
package edu.ku.brc.af.core.expresssearch;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 10, 2007
 *
 */
public class ERTICaptionInfo
{
    private static final Logger log = Logger.getLogger(ERTICaptionInfo.class);
    
    protected String                  colName;
    protected String                  colLabel      = null;
    protected boolean                 isVisible;
    protected int                     posIndex;
    protected String                  description   = null;
    
    protected String                  aggregatorName;
    protected String                  orderCol;
    protected Vector<ColInfo>         colInfoList   = null;
    protected Class<?>                aggClass      = null;
    
    protected Class<?>                subClass      = null;
    protected String                  subClassFieldName = null;  // The name of the field if the subClass in the agg class
    
    // Transient 
    protected Class<?>                colClass         = null;
    protected int                     orderColIndex    = -1;
    protected DBFieldInfo             fieldInfo        = null;
    protected DataObjSwitchFormatter  dataObjFormatter = null;
    protected UIFieldFormatterIFace   uiFieldFormatter = null;
    

    /**
     * @param colName
     * @param colLabel
     * @param isVisible
     * @param formatter
     * @param posIndex
     */
    public ERTICaptionInfo(String  colName, 
                           String  colLabel, 
                           boolean isVisible, 
                           UIFieldFormatterIFace uiFieldFormatter,
                           int     posIndex)
    {
        super();
        this.colName          = colName;
        this.colLabel         = colLabel;
        this.isVisible        = isVisible;
        this.uiFieldFormatter = uiFieldFormatter;
        this.posIndex         = posIndex;
    }
    
    /**
     * Constructor. Position Index is set after object is created.
     * @param element
     */
    public ERTICaptionInfo(final Element element,
                           final ResourceBundle resBundle)
    {
        super();
        
        int tblId = getAttr(element, "tableid", -1);
        if (tblId == -1)
        {
            throw new RuntimeException("search_config.xml caption has bad id["+getAttr(element, "tableid", "N/A")+"]");
        }
        
        this.colName = element.attributeValue("col");
        
        String key = getAttr(element, "key", null);
        if (StringUtils.isNotEmpty(key))
        {
            colLabel    = resBundle.getString(key);
            description = resBundle.getString(key+"_desc");
            
        } else
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tblId);
            if (ti != null)
            {
                fieldInfo = ti.getFieldByColumnName(this.colName);
                if (fieldInfo == null)
                {
                    if (this.colName.endsWith("ID"))
                    {
                        colLabel    = this.colName;
                        description = "";
                    } else
                    {
                        throw new RuntimeException("Couldn't convert column Name["+this.colName+"] to a field name to find the field in table["+tblId+"]");
                    }
                }
            } else
            {
                throw new RuntimeException("Table Id is bad id["+getAttr(element, "tableid", "N/A")+"]");
            }
        }
        
        this.isVisible = getAttr(element, "visible", true);
        
        String dataObjFormatterName = getAttr(element, "dataobjformatter", null);
        String formatter            = getAttr(element, "formatter", dataObjFormatterName);
        
        String aggTableClassName = null;
        
        Element subElement = (Element)element.selectSingleNode("subobject");

        Element aggElement = (Element)element.selectSingleNode("agg");
        if (aggElement != null)
        {
            String aggClassName = getAttr(aggElement, "class", null);
            if (StringUtils.isNotEmpty(aggClassName))
            {
                aggTableClassName = aggClassName;
                try
                {
                    aggClass       = Class.forName(aggClassName);
                    aggregatorName = getAttr(aggElement, "aggregator", null);
                    orderCol       = getAttr(aggElement, "ordercol", null);
                    
                } catch (ClassNotFoundException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        
        if (subElement != null)
        {
            String subClassName = getAttr(subElement, "class", null);
            if (StringUtils.isNotEmpty(subClassName))
            {
                try
                {
                    subClass          = Class.forName(subClassName);
                    subClassFieldName = getAttr(subElement, "fieldname", null);
                    aggTableClassName = subClassName;
                    
                } catch (ClassNotFoundException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        
        String compositeClassName = null;
        if (StringUtils.isNotEmpty(aggTableClassName))
        {
            compositeClassName = aggTableClassName;
            
        } else if (StringUtils.isNotEmpty(dataObjFormatterName))
        {
            dataObjFormatter = DataObjFieldFormatMgr.getFormatter(dataObjFormatterName);
            if (dataObjFormatter != null)
            {
                compositeClassName = dataObjFormatter.getDataClass().getName();
                try
                {
                    aggClass = Class.forName(compositeClassName);
                    
                } catch (ClassNotFoundException ex)
                {
                    ex.printStackTrace();
                }
            } else
            {
                log.error("Couldn't find formatter["+dataObjFormatterName+"]");
            }
        }
        
        
        if (StringUtils.isNotEmpty(compositeClassName))
        {
            colInfoList = new Vector<ColInfo>();
            for (Object colObj : element.selectNodes("col"))
            {
                Element colInfoObj = (Element)colObj;
                ColInfo columnInfo = new ColInfo(getAttr(colInfoObj, "name", null), getAttr(colInfoObj, "field", null));
                colInfoList.add(columnInfo);
            }

            DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(compositeClassName);
            if (ti != null)
            {
                for (ColInfo columnInfo : colInfoList)
                {
                    DBFieldInfo fi = ti.getFieldByName(columnInfo.getFieldName());
                    if (fi != null)
                    {
                        columnInfo.setFieldClass(fi.getDataClass());
                    } else
                    {
                        log.error("Field Name is Aggregate Sub Class doesn't exist Class is not a Data Table["+compositeClassName+"] Field["+columnInfo.getFieldName()+"]");
                    }
                }
                
            } else
            {
                log.error("Aggregate Sub Class is not a Data Table["+compositeClassName+"]");
            }
        }
        
        if (StringUtils.isNotEmpty(formatter))
        {
            uiFieldFormatter = UIFieldFormatterMgr.getFormatter(formatter);
            if (uiFieldFormatter == null)
            {
                log.error("The UIFieldFormatter could not be found named ["+formatter+"]");
            }
        }
    }


    public String getColName()
    {
        return colName;
    }

    public String getColLabel()
    {
        if (colLabel == null)
        {
            return fieldInfo.getTitle();
        }
        return colLabel;
    }

    public boolean isVisible()
    {
        return isVisible;
    }

    public int getPosIndex()
    {
        return posIndex;
    }

    public void setPosIndex(int posIndex)
    {
        this.posIndex = posIndex;
    }

    /**
     * @return the fieldInfo
     */
    public DBFieldInfo getFieldInfo()
    {
        return fieldInfo;
    }

    /**
     * @param colName the colName to set
     */
    public void setColName(String colName)
    {
        this.colName = colName;
    }

    /**
     * @param colLabel the colLabel to set
     */
    public void setColLabel(String colLabel)
    {
        this.colLabel = colLabel;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        if (description == null)
        {
            return fieldInfo.getDescription();
        }
        return description;
    }

    /**
     * @param isVisible the isVisible to set
     */
    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }

    /**
     * @return the colClass
     */
    public Class<?> getColClass()
    {
        return colClass;
    }

    /**
     * @param colClass the colClass to set
     */
    public void setColClass(Class<?> colClass)
    {
        this.colClass = colClass;
    }

    /**
     * @return the aggregator
     */
    public String getAggregatorName()
    {
        return aggregatorName;
    }

    /**
     * @return the colInfo
     */
    public Vector<ColInfo> getColInfoList()
    {
        return colInfoList;
    }

    /**
     * @return the aggClass
     */
    public Class<?> getAggClass()
    {
        return aggClass;
    }

    /**
     * @return the subClass
     */
    public Class<?> getSubClass()
    {
        return subClass;
    }

    /**
     * @return the orderCol
     */
    public String getOrderCol()
    {
        return orderCol;
    }
    
    /**
     * @param aggClass the aggClass to set
     */
    public void setAggClass(Class<?> aggClass)
    {
        this.aggClass = aggClass;
    }

    /**
     * @return the orderColIndex
     */
    public int getOrderColIndex()
    {
        return orderColIndex;
    }

    /**
     * @param orderColIndex the orderColIndex to set
     */
    public void setOrderColIndex(int orderColIndex)
    {
        this.orderColIndex = orderColIndex;
    }

    /**
     * @return the subClassFieldName
     */
    public String getSubClassFieldName()
    {
        return subClassFieldName;
    }
    
    //-------------------------------------------------------------------
    // Inner Class
    //-------------------------------------------------------------------
    public class ColInfo
    {
        protected String   columnName;    // name of column in the query (not of the original table schema)
        protected String   fieldName;  // field name in Class
        protected int      position;
        protected Class<?> fieldClass;
        
        public ColInfo(String columnName, String fieldName)
        {
            super();
            this.columnName = columnName;
            this.fieldName  = fieldName;
        }

        /**
         * @return the colName
         */
        public String getColumnName()
        {
            return columnName;
        }

        /**
         * @return the fieldName
         */
        public String getFieldName()
        {
            return fieldName;
        }

        /**
         * @return the position
         */
        public int getPosition()
        {
            return position;
        }

        /**
         * @param position the position to set
         */
        public void setPosition(int position)
        {
            this.position = position;
        }

        /**
         * @return the fieldClass
         */
        public Class<?> getFieldClass()
        {
            return fieldClass;
        }

        /**
         * @param fieldClass the fieldClass to set
         */
        public void setFieldClass(Class<?> fieldClass)
        {
            this.fieldClass = fieldClass;
        }
        
    }



    /**
     * @return the dataObjFormatter
     */
    public DataObjSwitchFormatter getDataObjFormatter()
    {
        return dataObjFormatter;
    }

    /**
     * @return the uiFieldFormatter
     */
    public UIFieldFormatterIFace getUiFieldFormatter()
    {
        return uiFieldFormatter;
    }

}

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

import edu.ku.brc.af.core.db.DBFieldInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 12, 2007
 *
 */
public class DisplayFieldConfig implements TableNameRendererIFace, Comparable<DisplayFieldConfig>
{
    protected String  fieldName;
    protected String  formatter;
    protected Integer order             = null;
    protected Boolean isWebServiceField = false;

    // Transient
    protected SearchTableConfig      stc;
    protected boolean                isInUse        = false;
    protected boolean                isInitialInUse = false;
    protected DBFieldInfo            fieldInfo      = null;
    
    /**
     * 
     */
    public DisplayFieldConfig()
    {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * @param fieldName
     * @param formatter
     * @param order
     */
    public DisplayFieldConfig(String fieldName, 
                              String formatter, 
                              Integer order)
    {
        this.fieldName = fieldName;
        this.formatter = formatter;
        this.order = order;
    }
    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return fieldName;
    }
    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }
    /**
     * @return the formatter
     */
    public String getFormatter()
    {
        return formatter;
    }
    /**
     * @param formatter the formatter to set
     */
    public void setFormatter(String formatter)
    {
        this.formatter = formatter;
    }
    /**
     * @return the order
     */
    public Integer getOrder()
    {
        return order;
    }
    /**
     * @param order the order to set
     */
    public void setOrder(Integer order)
    {
        this.order = order;
    }
    
    /**
     * @return the isInUse
     */
    public boolean isInUse()
    {
        return isInUse;
    }
    /**
     * @param isInUse the isInUse to set
     */
    public void setInUse(boolean isInUse)
    {
        this.isInUse = isInUse;
    }
    /**
     * 
     */
    public void reset()
    {
        isInUse = isInitialInUse;
    }

    /**
     * @param isInitialInUse the isInitialInUse to set
     */
    public void setInitialInUse(boolean isInitialInUse)
    {
        this.isInitialInUse = isInitialInUse;
    }

    /**
     * @return the isWebServiceField
     */
    public Boolean getIsWebServiceField()
    {
        return isWebServiceField;
    }
    
    /**
     * @return false if null or the true/false
     */
    public boolean isWebServiceField()
    {
        return isWebServiceField == null ? false : isWebServiceField;
    }
    
    /**
     * @param isWebServiceField the isWebServiceField to set
     */
    public void setIsWebServiceField(Boolean isWebServiceField)
    {
        this.isWebServiceField = isWebServiceField;
    }
    /**
     * @return the stc
     */
    public SearchTableConfig getStc()
    {
        return stc;
    }
    /**
     * @param stc the stc to set
     */
    public void setStc(SearchTableConfig stc)
    {
        this.stc = stc;
    }
    
    /**
     * @return the fieldInfo
     */
    public DBFieldInfo getFieldInfo()
    {
        if (fieldInfo == null)
        {
            fieldInfo = stc.getTableInfo().getFieldByName(fieldName);
        }
        return fieldInfo;
    }

    /**
     * @param fieldInfo the fieldInfo to set
     */
    public void setFieldInfo(DBFieldInfo fieldInfo)
    {
        this.fieldInfo = fieldInfo;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return fieldInfo != null ? fieldInfo.getTitle() : fieldName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    //@Override
    public int compareTo(DisplayFieldConfig o)
    {
        // For now we will order by Name 
        // and if we implement ordering of display fields
        // then we should use the order.
        //return order.compareTo(o.order);
        return fieldName.compareTo(o.fieldName);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.TableNameRendererIFace#getIconName()
     */
    //@Override
    public String getIconName()
    {
        return fieldInfo.getTableInfo().getClassObj().getSimpleName();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.TableNameRendererIFace#getTitle()
     */
    //@Override
    public String getTitle()
    {
        return toString();
    }
    
    /**
     * Dumps contents for Debugging.
     */
    public void dump()
    {
        System.out.println("  fieldName:   "+fieldName);
        System.out.println("  formatter:   "+formatter);
        System.out.println("  isWebSrvFld: "+isWebServiceField);
        System.out.println("  order:       "+order);
    }

}

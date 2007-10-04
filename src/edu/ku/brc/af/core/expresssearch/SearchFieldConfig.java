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

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 12, 2007
 *
 */
public class SearchFieldConfig implements edu.ku.brc.af.core.expresssearch.TableNameRendererIFace, Comparable<SearchFieldConfig>
{
    protected String         fieldName;
    protected Boolean        isSortable;
    protected Boolean        isAscending;
    protected Integer        order        = null;
    
    // Transient
    protected boolean                isInUse   = false;
    protected DBFieldInfo            fieldInfo = null;
    protected SearchTableConfig      stc       = null;

    
    /**
     * 
     */
    public SearchFieldConfig()
    {
        // nothing
    }
    
    /**
     * @param fieldName
     * @param colOrder
     * @param isSortable
     * @param isAscending
     */
    public SearchFieldConfig(final String fieldName, 
                             final Boolean isSortable,
                             final Boolean isAscending)
    {
        this.fieldName = fieldName;
        this.isSortable = isSortable;
        this.isAscending = isAscending;
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
     * @return the isSortable
     */
    public Boolean getIsSortable()
    {
        return isSortable;
    }

    /**
     * @param isSortable the isSortable to set
     */
    public void setIsSortable(Boolean isSortable)
    {
        this.isSortable = isSortable;
    }

    /**
     * @return the isAscending
     */
    public Boolean getIsAscending()
    {
        return isAscending;
    }

    /**
     * @param isAscending the isAscending to set
     */
    public void setIsAscending(Boolean isAscending)
    {
        this.isAscending = isAscending;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return fieldInfo != null ? UIHelper.makeNamePretty(fieldInfo.getColumn()) : fieldName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    //@Override
    public int compareTo(SearchFieldConfig o)
    {
        return order.compareTo(o.order);
    }
    
}

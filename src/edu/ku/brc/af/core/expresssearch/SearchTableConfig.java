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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.DateWrapper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 7, 2007
 *
 */
public class SearchTableConfig implements DisplayOrderingIFace, 
                                          TableNameRendererIFace, 
                                          Comparable<SearchTableConfig>
{
    private static final Logger log = Logger.getLogger(SearchTableConfig.class);
    
    protected String                     tableName; // This is really the Class name, the table name is 
    protected Integer                    displayOrder;
    protected Vector<SearchFieldConfig>  searchFields  = new Vector<SearchFieldConfig>();
    protected Vector<DisplayFieldConfig> displayFields = new Vector<DisplayFieldConfig>();
    protected Vector<DisplayFieldConfig> wsFields      = new Vector<DisplayFieldConfig>();
    
    // Transient 
    protected DBTableInfo                tableInfo;
    
    /**
     * 
     */
    public SearchTableConfig()
    {
        // nothing
    }
    
    /**
     * 
     */
    public void initialize()
    {
        tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(tableName.toLowerCase()); 
        
        for (SearchFieldConfig sfc : searchFields)
        {
            sfc.setStc(this);
        }
        
        for (DisplayFieldConfig dfc : displayFields)
        {
            dfc.setStc(this);
        }
        
        if (wsFields != null)
        {
            for (DisplayFieldConfig wsfc : wsFields)
            {
                wsfc.setStc(this);
            }
        } else
        {
            wsFields = new Vector<DisplayFieldConfig>();
        }
    }

    /**
     * @param tableName
     * @param displayOrder
     */
    public SearchTableConfig(String tableName, Integer displayOrder)
    {
        this.tableName    = tableName;
        this.displayOrder = displayOrder;
    }
    
    public boolean hasConfiguredSearchFields()
    {
        for (SearchFieldConfig sfc : searchFields)
        {
            if (sfc.isInUse())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    /**
     * @return the searchFields
     */
    public Vector<SearchFieldConfig> getSearchFields()
    {
        return searchFields;
    }

    /**
     * @param searchFields the searchFields to set
     */
    public void setSearchFields(Vector<SearchFieldConfig> searchFields)
    {
        this.searchFields = searchFields;
    }

    /**
     * @return the displayFields
     */
    public Vector<DisplayFieldConfig> getDisplayFields()
    {
        return displayFields;
    }

    /**
     * @param displayFields the displayFields to set
     */
    public void setDisplayFields(Vector<DisplayFieldConfig> displayFields)
    {
        this.displayFields = displayFields;
    }

    /**
     * @return the wsFields
     */
    public Vector<DisplayFieldConfig> getWsFields()
    {
        return wsFields;
    }

    /**
     * @param wsFields the wsFields to set
     */
    public void setWsFields(Vector<DisplayFieldConfig> wsFields)
    {
        this.wsFields = wsFields;
    }

    /**
     * @return the displayOrder
     */
    public Integer getDisplayOrder()
    {
        return displayOrder;
    }

    /**
     * @param displayOrder the displayOrder to set
     */
    public void setDisplayOrder(Integer displayOrder)
    {
        this.displayOrder = displayOrder;
    }

    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        if (tableInfo == null)
        {
            tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(tableName.toLowerCase()); 
        }
        return tableInfo;
    }

    /**
     * @param tableInfo the tableInfo to set
     */
    public void setTableInfo(DBTableInfo tableInfo)
    {
        this.tableInfo = tableInfo;
    }
    
    /**
     * @param searchTerm
     * @param idsOnly
     * @return
     */
    public String getSQL(final String searchTerm, final boolean idsOnly)
    {
        return getSQL(searchTerm, idsOnly, null);
    }
    
    /**
     * @param searchTerm
     * @param idsOnly
     * @param ids
     * @return
     */
    public String getSQL(final String searchTerm, final boolean idsOnly, final Vector<Integer> ids)
    {
        DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
        
        // Check to see if it is date
        boolean isDate = false;
        try
        {
            scrDateFormat.getSimpleDateFormat().parse(searchTerm);
            isDate = true;
            
        } catch (Exception ex)
        {
        }
        
        boolean isNumeric;
        boolean hasDecimalPoint = false;
        
        if (!StringUtils.isNumeric(searchTerm))
        {
            isNumeric = false;
        } else
        {
            isNumeric       = true;
            hasDecimalPoint = StringUtils.contains(searchTerm, '.');
        }
        
        StringBuilder sqlStr = new StringBuilder("SELECT ");
        
        DBTableInfo ti = getTableInfo();
        
        String primaryKey = ti.getIdFieldName();
        sqlStr.append(primaryKey);
        
        if (!idsOnly)
        {
            for (DisplayFieldConfig field : displayFields)
            {
                sqlStr.append(',');
                sqlStr.append(field.getFieldInfo().getName());
            }
        }
        
        sqlStr.append(" FROM ");
        sqlStr.append(ti.getClassObj().getSimpleName());
        
        sqlStr.append(" WHERE ");
        
        if (ids != null)
        {
            sqlStr.append(primaryKey);
            sqlStr.append(" IN (");
            for (int i=0;i<ids.size();i++)
            {
                if (i > 0) sqlStr.append(',');
                sqlStr.append(ids.elementAt(i).toString());
            }
            sqlStr.append(") ");
        }
        
        StringBuilder orderBy = new StringBuilder();
        int orderByCnt = 0;
        
        int cnt = 0;

        for (SearchFieldConfig searchField : searchFields)
        {
            String clause;
            DBFieldInfo fi = searchField.getFieldInfo();
            
            if (ids == null)
            {
                if (fi.getDataClass() == Date.class || searchField.getFieldInfo().getDataClass() == Calendar.class)
                {
                    if (!isDate)
                    {
                        if (isNumeric && ! hasDecimalPoint && searchTerm.length() == 4)
                        {
                            clause = "YEAR("+fi.getName()+") = " + searchTerm;
                        } else
                        {
                            continue;
                        }
                    } else
                    {
                        clause = fi.getColumn() + " = " + "'" + searchTerm + "'";
                    }
                    
                } else if (fi.getDataClass() == Float.class || fi.getDataClass() == Double.class || fi.getDataClass() == BigDecimal.class)
                {
                    if (!isNumeric)
                    {
                        continue;
                    }
                    clause = fi.getColumn() + " = " + searchTerm;
                    
                } else if (fi.getDataClass() == Byte.class || fi.getDataClass() == Short.class || 
                           fi.getDataClass() == Integer.class|| fi.getDataClass() == Long.class)
                {
                    if (!isNumeric || hasDecimalPoint)
                    {
                        continue;
                    }
                    clause = fi.getColumn() + " = " + searchTerm;
                    
                } else 
                {
                    if (fi.getDataClass() == String.class)
                    {
                        log.error("Handling class ["+fi.getDataClass()+"] as a String");
                    }
                    clause = "lower(" + fi.getColumn() + ") like " + "'%" + searchTerm + "%'";
                }

                if (cnt > 0) sqlStr.append(" OR ");
                sqlStr.append(clause);
            }
            
            cnt++;
            
            if (!idsOnly)
            {
                if (searchField.getIsSortable())
                {
                    if (orderByCnt == 0)
                    {
                        orderBy.append(" ORDER BY ");
                    } else
                    {
                        orderBy.append(", ");
                    }
                    orderBy.append(searchField.getFieldName());
                    orderBy.append(searchField.getIsAscending() ? " ASC" : " DESC");
                    
                    orderByCnt++;
                }
            }
        }
        
        if (cnt == 0)
        {
            return null;
        }
        
        if (orderByCnt > 0)
        {
            sqlStr.append(orderBy);
        }

        return sqlStr.toString();

    }


    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    //@Override
    public int compareTo(SearchTableConfig o)
    {
        return tableName.compareTo(o.tableName);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return tableInfo.getTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.TableNameRendererIFace#getIconName()
     */
    //@Override
    public String getIconName()
    {
        return tableInfo.getClassObj().getSimpleName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.TableNameRendererIFace#getTitle()
     */
    //@Override
    public String getTitle()
    {
        return toString();
    }
    
}

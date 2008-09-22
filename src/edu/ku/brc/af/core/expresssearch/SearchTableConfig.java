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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * This class is the root of the configuration tree for all the searches for tables and related queries.
 * This class is used to persist the configuration.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Sep 7, 2007
 *
 */
public class SearchTableConfig implements DisplayOrderingIFace, 
                                          TableNameRendererIFace, 
                                          Comparable<SearchTableConfig>
{
    //private static final Logger log = Logger.getLogger(SearchTableConfig.class);
    
    protected static SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
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
        }
    }

    /**
     * @param tableName
     * @param displayOrder
     */
    public SearchTableConfig(final String tableName, final Integer displayOrder)
    {
        this.tableName    = tableName;
        this.displayOrder = displayOrder;
    }
    
    /**
     * @return
     */
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
     * @return the table id or -1 if it hasn't been initialized
     */
    public int getTableId()
    {
        return tableInfo != null ? tableInfo.getTableId() : -1;
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
    public String getSQL(final List<SearchTermField> terms, final boolean idsOnly, final boolean isHQL)
    {
        return getSQL(terms, idsOnly, null, isHQL);
    }
    
    /**
     * This code is not my finest hour. What needs to be re-worked is not paring each term each time.
     * @param searchTerm
     * @param idsOnly
     * @param ids
     * @return
     */
    public String getSQL(final List<SearchTermField> terms, 
                         final boolean idsOnly, 
                         final Vector<Integer> ids, 
                         final boolean isHQL)
    {
        StringBuilder sqlStr = new StringBuilder("SELECT ");
        
        DBTableInfo ti = getTableInfo(); // this sets the data member tableInfo
        
        String primaryKey = ti.getIdFieldName();
        if (!isHQL)
        {
            primaryKey = primaryKey.substring(0, 1).toUpperCase() + primaryKey.substring(1, primaryKey.length());
            primaryKey = StringUtils.replace(primaryKey, "Id", "ID");
        }
        
        sqlStr.append(tableInfo.getAbbrev());
        sqlStr.append('.');
        sqlStr.append(primaryKey);
        
        if (!idsOnly)
        {
            for (DisplayFieldConfig field : displayFields)
            {
                sqlStr.append(',');
                sqlStr.append(tableInfo.getAbbrev()); 
                sqlStr.append('.'); 
                sqlStr.append(field.getFieldInfo().getName());
            }
        }
        
        sqlStr.append(" FROM ");
        sqlStr.append(ti.getClassObj().getSimpleName());
        sqlStr.append(" as ");
        sqlStr.append(tableInfo.getAbbrev());

        String joinSnipet = QueryAdjusterForDomain.getInstance().getJoinClause(tableInfo, isHQL, tableInfo.getAbbrev(), false); 
        if (joinSnipet != null)
        {
            sqlStr.append(' ');
            sqlStr.append(joinSnipet);
            sqlStr.append(' ');
        }
        
        sqlStr.append(" WHERE ");
        
        boolean addParen = false;
        if (ids != null || terms.size() == 0)
        {
            sqlStr.append(tableInfo.getAbbrev()); 
            sqlStr.append('.'); 
            sqlStr.append(primaryKey);
            sqlStr.append(" IN (");
            for (int i=0;i<ids.size();i++)
            {
                if (i > 0) sqlStr.append(',');
                sqlStr.append(ids.elementAt(i).toString());
            }
            sqlStr.append(") ");
            
        } else
        {
            String sqlSnipet = QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, isHQL, false, tableInfo.getAbbrev()); 
            if (sqlSnipet != null)
            {
                sqlStr.append(sqlSnipet);
                sqlStr.append(" AND (");
                addParen = true;
            }
        }
        
        StringBuilder orderBy = new StringBuilder();
        int orderByCnt = 0;
        
        //----------------------------------------------------------------------------------------------
        // NOTE: If a full date was type in and it was parsed as such
        // and it couldn't be something else, then it only searches date fields.
        //----------------------------------------------------------------------------------------------
        
        int cnt = 0;
        
        if (terms.size() > 0)
        {
            for (SearchTermField term : terms)
            {
                if (!term.isSingleChar())
                {
                    String termStr = term.getTerm();
                    String abbrev  = tableInfo.getAbbrev(); 
    
                    for (SearchFieldConfig searchField : searchFields)
                    {
                        String numericTermStr   = null;
                        String      clause      = null;
                        DBFieldInfo fi          = searchField.getFieldInfo();
                        boolean     isFieldDate = fi.getDataClass() == Date.class || searchField.getFieldInfo().getDataClass() == Calendar.class;
                        String      fieldName   = isHQL ? fi.getName() : fi.getColumn();
                        
                        boolean isFormatted  = false;
                        UIFieldFormatterIFace formatter = fi.getFormatter();
                        
                        if (formatter != null)
                        {
                            if (formatter.isNumeric())
                            {
                                isFormatted = term.isOn(SearchTermField.IS_NUMERIC) && !term.isOn(SearchTermField.HAS_DEC_POINT);
                                if (isFormatted)
                                {
                                    numericTermStr = (String)formatter.formatFromUI(termStr);
                                }
                                
                            } else
                            {
                                if (formatter.isLengthOK(termStr.length()))
                                {
                                    isFormatted = true;
                                    if (!formatter.isValid(termStr))
                                    {
                                        continue;
                                    }
                                }
                            }
                        }
                        
                        if (ids == null)
                        {
                            if (isFieldDate)
                            {
                                boolean isDate = term.isOn(SearchTermField.IS_DATE);
                                if (isDate || term.isOn(SearchTermField.IS_YEAR_OF_DATE))
                                {
                                    if (isDate)
                                    {
                                        if (isFieldDate)
                                        {
                                            clause = abbrev + '.' + fieldName + " = " + "'" + termStr + "'";
                                        } else
                                        {
                                            continue;
                                        }
                                        
                                    } else
                                    {
                                        clause = "YEAR("+abbrev + '.' + fi.getName()+") = " + termStr;
                                    }
                                } else
                                {
                                    continue;
                                }
                                
                            } else 
                            {
                                if (fi.getDataClass() == Float.class || fi.getDataClass() == Double.class || fi.getDataClass() == BigDecimal.class)
                                {
                                    if (!term.isOn(SearchTermField.IS_NUMERIC))
                                    {
                                        continue;
                                    }
                                    clause = fieldName + " = " + termStr;
                                    
                                } else if (fi.getDataClass() == Byte.class || fi.getDataClass() == Short.class || 
                                           fi.getDataClass() == Integer.class|| fi.getDataClass() == Long.class)
                                {
                                    if (!term.isOn(SearchTermField.IS_NUMERIC) || term.isOn(SearchTermField.HAS_DEC_POINT))
                                    {
                                        continue;
                                    }
                                    clause = abbrev + '.' + fieldName + " = " + termStr;
                                    
                                } else if (isFormatted)
                                {
                                    clause = abbrev + '.' + fieldName + " = " + "'" + (numericTermStr != null ? numericTermStr : termStr) + "'";
                                    
                                } else
                                {
                                    clause = ESTermParser.createWhereClause(term, abbrev, fieldName);
                                    
                                    /*boolean startWildCard = term.isOn(SearchTermField.STARTS_WILDCARD);
                                    boolean endWildCard   = term.isOn(SearchTermField.ENDS_WILDCARD);
                                    if (startWildCard || endWildCard)
                                    {
                                        clause = "LOWER(" + abbrev + '.' + fieldName + ") LIKE " + (startWildCard ? "'%" : "'") + termStr + (endWildCard ? "%'" : "'");
                                    } else
                                    {
                                        clause = "LOWER(" + abbrev + '.' + fieldName + ") = " + "'" + termStr + "'";
                                    }*/
                                }
                            }
            
                            if (clause != null)
                            {
                                if (cnt > 0) sqlStr.append(" OR ");
                                sqlStr.append(clause);
                            }
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
                                orderBy.append(abbrev);
                                orderBy.append('.');
                                orderBy.append(searchField.getFieldName());
                                orderBy.append(searchField.getIsAscending() ? " ASC" : " DESC");
                                
                                orderByCnt++;
                            }
                        }
                    }
                }
            }
            
            if (addParen)
            {
                sqlStr.append(")");
            }
        }
        
        if (cnt == 0 && terms.size() > 0)
        {
            return null;
        }
        
        if (orderByCnt > 0)
        {
            sqlStr.append(orderBy);
        }

        //System.err.println(sqlStr.toString());
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
    
    /**
     * Dumps contents for Debugging.
     */
    public void dump()
    {
        System.out.println("\n----------- "+tableName+" ------------");
        System.out.println("Order: "+displayOrder);
        System.out.println("  --------- Search Fields -__-------");
        for (SearchFieldConfig sfc : searchFields)
        {
            sfc.dump();
        }
        
        System.out.println("  --------- Display Fields --------");
        for (DisplayFieldConfig dfc : displayFields)
        {
            dfc.dump();
        }
    }
    
}

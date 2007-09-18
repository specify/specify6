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
package edu.ku.brc.af.core.expresssearch;

import java.util.Collections;
import java.util.Vector;

import com.thoughtworks.xstream.XStream;

/**
 * This class keeps track of tables that are configured to participate in the ExpressSearch. BNoth for searching
 * and display. 
 * 
 * Note: I tried to use a Comparator and a bindary search and it didn't work, 
 * it couldn't find an item when only three were in the list.
 * Turns out just doing a straight walk of the list is fine and plenty fast.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Sep 7, 2007
 *
 */
public class SearchConfig
{
    //private static final Logger log = Logger.getLogger(ExpressSearchConfig.class);
    
    protected Vector<SearchTableConfig> tables         = new Vector<SearchTableConfig>();
    protected Vector<RelatedQuery>      relatedQueries = new Vector<RelatedQuery>();
    
    // Transient
    protected SearchTableConfig         searcher = new SearchTableConfig();
    
    /**
     * 
     */
    public SearchConfig()
    {
        // nothing
    }
    
    /**
     * Configures the XStream for I/O.
     * @param xstream the stream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.alias("search",       SearchConfig.class);
        xstream.alias("searchtable",  SearchTableConfig.class);
        xstream.alias("searchfield",  SearchFieldConfig.class);
        xstream.alias("displayfield", DisplayFieldConfig.class);
        xstream.alias("relatedquery", RelatedQuery.class);
        
        xstream.omitField(SearchConfig.class,       "searcher");
        
        xstream.omitField(SearchFieldConfig.class,  "isInUse");
        xstream.omitField(SearchFieldConfig.class,  "fieldInfo");
        xstream.omitField(SearchFieldConfig.class,  "stc");
        
        xstream.omitField(DisplayFieldConfig.class, "isInUse");
        xstream.omitField(DisplayFieldConfig.class, "fieldInfo");
        xstream.omitField(DisplayFieldConfig.class, "stc");
        
        xstream.omitField(RelatedQuery.class, "erti");
        xstream.omitField(RelatedQuery.class, "isInUse");
        xstream.omitField(RelatedQuery.class,  "tableInfo");

        xstream.omitField(SearchTableConfig.class,  "tableInfo");
    }
    
    /**
     * 
     */
    public void initialize()
    {
        for (SearchTableConfig table : tables)
        {
            table.initialize();
        }
    }
    
    /**
     * @param id
     * @return
     */
    public Integer getOrderForRelatedQueryId(final String id)
    {
        for (RelatedQuery rq : relatedQueries)
        {
            if (id.equals(rq.getId()))
            {
                return rq.getDisplayOrder();
            }
        }
        return Integer.MAX_VALUE;
    }
    
    /**
     * @param id
     * @return
     */
    public Integer getOrderForTableId(final String id)
    {
        for (SearchTableConfig stc : tables)
        {
            if (id.equals(stc.getTableInfo().getTableId()))
            {
                return stc.getDisplayOrder();
            }
        }
        return Integer.MAX_VALUE;
    }
    
    /**
     * @param tableName
     * @param fieldName
     * @return
     */
    public DisplayFieldConfig findDisplayField(final String tableName, 
                                               final String fieldName)
    {
        SearchTableConfig tbl = findTable(tableName, false);
        if (tbl != null)
        {
            for (DisplayFieldConfig dfc : tbl.getDisplayFields())
            {
                if (fieldName.equals(dfc.getFieldName()))
                {
                    return dfc;
                }
            }
        }
        return null;
    }

    /**
     * @param tableName
     * @param fieldName
     * @return
     */
    public SearchFieldConfig findSearchField(final String tableName, 
                                             final String fieldName)
    {
        SearchTableConfig tbl = findTable(tableName, false);
        if (tbl != null)
        {
            for (SearchFieldConfig sfc : tbl.getSearchFields())
            {
                if (fieldName.equals(sfc.getFieldName()))
                {
                    return sfc;
                }
            }
        }
        return null;
    }

    /**
     * @param table
     * @param fieldName
     * @return
     */
    public DisplayFieldConfig findDisplayField(final SearchTableConfig table, 
                                               final String            fieldName,
                                               final boolean           createWhenNotFound)
    {
        if (table != null)
        {
            for (DisplayFieldConfig dfc : table.getDisplayFields())
            {
                if (fieldName.equals(dfc.getFieldName()))
                {
                    dfc.setInUse(true);
                    return dfc;
                }
            }
        }
        
        if (createWhenNotFound)
        {
            DisplayFieldConfig dfc =  new DisplayFieldConfig(fieldName, "", Integer.MAX_VALUE);
            dfc.setInUse(false);
            if (table != null)
            {
                table.getDisplayFields().add(dfc);
            }
            return dfc;
        }
        
        return null;
    }

    /**
     * @param table
     * @param fieldName
     * @return
     */
    public SearchFieldConfig findSearchField(final SearchTableConfig table, 
                                             final String            fieldName,
                                             final boolean           createWhenNotFound)
    {
        if (table != null)
        {
            for (SearchFieldConfig sfc : table.getSearchFields())
            {
                if (fieldName.equals(sfc.getFieldName()))
                {
                    sfc.setInUse(true);
                    return sfc;
                }
            }
        }
        
        if (createWhenNotFound)
        {
            SearchFieldConfig sfc = new SearchFieldConfig(fieldName, false, true);
            sfc.setInUse(false);
            
            if (table != null)
            {
                table.getSearchFields().add(sfc);
            }
            return sfc;
        }
        
        return null;
    }

    /**
     * @param tableName
     * @param fieldName
     * @return
     */
    public boolean isDisplayFieldInList(final String tableName, 
                                        final String fieldName)
    {
        return findDisplayField(tableName, fieldName) != null;
    }
    
    /**
     * @param tableName
     * @param fieldName
     * @return
     */
    public boolean isSearchFieldInList(final String tableName, 
                                       final String fieldName)
    {
        return findSearchField(tableName, fieldName) != null;
    }
    
    /**
     * @param tableName
     * @param createWhenNotFnd
     * @return
     */
    public SearchTableConfig findTable(final String tableName, final boolean createWhenNotFnd)
    {
        if (searcher == null) // might be null because of xstream
        {
            searcher = new SearchTableConfig();
        }
        searcher.setTableName(tableName);
        int inx = Collections.binarySearch(tables, searcher);
        if (inx > -1)
        {
            return tables.get(inx);
        }
        
        if (createWhenNotFnd)
        {
            SearchTableConfig tbl = new SearchTableConfig(tableName, 0);   // YYY 
            tables.add(tbl);
            Collections.sort(tables);
            return tbl;
        }
        return null;
    }
    
    /**
     * @param tableName
     * @param field
     */
    public void addDisplayField(final String tableName, final DisplayFieldConfig field)
    {
        SearchTableConfig tbl = findTable(tableName, true);
        if (tbl != null)
        {
            Vector<DisplayFieldConfig> list = tbl.getDisplayFields();
            if (!list.contains(field))
            {
                list.addElement(field);
                Collections.sort(list);
            }
        }
    }
    
    /**
     * @param tableName
     * @param field
     */
    public void removeDisplayField(final String tableName, final String fieldName)
    {
        SearchTableConfig tbl = findTable(tableName, false);
        if (tbl != null)
        {
            for (DisplayFieldConfig sfc : tbl.getDisplayFields())
            {
                if (fieldName.equals(sfc.getFieldName()))
                {
                    tbl.getDisplayFields().remove(sfc);
                    break;
                }
            }
        }
    }

    /**
     * @param tableName
     * @param field
     */
    public void addSearchField(final String tableName, final SearchFieldConfig field)
    {
        SearchTableConfig tbl = findTable(tableName, true);
        if (tbl != null)
        {
            Vector<SearchFieldConfig> list = tbl.getSearchFields();
            if (!list.contains(field))
            {
                list.addElement(field);
            }
        }
    }
    
    /**
     * @param tableName
     * @param field
     */
    public void removeSearchField(final String tableName, final String fieldName)
    {
        SearchTableConfig tbl = findTable(tableName, false);
        if (tbl != null)
        {
            for (SearchFieldConfig sfc : tbl.getSearchFields())
            {
                if (fieldName.equals(sfc.getFieldName()))
                {
                    tbl.getSearchFields().remove(sfc);
                    break;
                }
            }
        }
    }

    /**
     * @return the tables
     */
    public Vector<SearchTableConfig> getTables()
    {
        return tables;
    }

    /**
     * @param tables the tables to set
     */
    public void setTables(Vector<SearchTableConfig> tables)
    {
        this.tables = tables;
    }

    /**
     * @return the relatedQueries
     */
    public Vector<RelatedQuery> getRelatedQueries()
    {
        return relatedQueries;
    }

    /**
     * @param relatedQueries the relatedQueries to set
     */
    public void setRelatedQueries(Vector<RelatedQuery> relatedQueries)
    {
        this.relatedQueries = relatedQueries;
    }
    
    /**
     * @param id
     * @param createWhenNotFound
     * @return
     */
    public RelatedQuery findRelatedQuery(final String id, final boolean createWhenNotFound)
    {
        for (RelatedQuery rq : relatedQueries)
        {
            if (rq.getId().equals(id))
            {
                return rq;
            }
        }
        
        if (createWhenNotFound)
        {
            RelatedQuery rq = new RelatedQuery(id, Integer.MAX_VALUE);
            relatedQueries.add(rq);
            return rq;
        }
        return null;
    }
}

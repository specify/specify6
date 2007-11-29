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
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * This class keeps track of tables that are configured to participate in the ExpressSearch. BNoth for searching
 * and display. 
 * 
 * This class and its children objects are created from a persistent store in XML.
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
    private static final Logger log = Logger.getLogger(SearchConfig.class);
    
    protected Vector<SearchTableConfig> tables         = new Vector<SearchTableConfig>();
    protected Vector<RelatedQuery>      relatedQueries = new Vector<RelatedQuery>();
    
    // Transient
    protected SearchTableConfig                    searcher = new SearchTableConfig(); // note: this is only used for doing a binary search.
    
    protected Hashtable<String, RelatedQuery>      relatedQueryIdHash;
    protected Hashtable<String, SearchTableConfig> tableIdHash;
    protected Hashtable<String, SearchTableConfig> tableNameHash;
    
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
        // Aliases
        xstream.alias("search",       SearchConfig.class);
        xstream.alias("searchtable",  SearchTableConfig.class);
        xstream.alias("searchfield",  SearchFieldConfig.class);
        xstream.alias("displayfield", DisplayFieldConfig.class);
        xstream.alias("relatedquery", RelatedQuery.class);
        
        xstream.aliasAttribute(RelatedQuery.class, "isActive", "isactive");
        
        // Things to omit
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
     * Call after this is read in from XML
     */
    public void initialize()
    {
        // Must be initialized here because it is serialized in from XML
        relatedQueryIdHash = new Hashtable<String, RelatedQuery>();
        tableIdHash        = new Hashtable<String, SearchTableConfig>();
        tableNameHash      = new Hashtable<String, SearchTableConfig>();
        
        for (SearchTableConfig table : tables)
        {
            table.initialize();
            tableIdHash.put(Integer.toString(table.getTableId()), table);
            tableNameHash.put(table.getTableName(), table);
        }
        
        for (RelatedQuery rq : relatedQueries)
        {
            relatedQueryIdHash.put(rq.getId(), rq);
        }
    }
    
    /**
     * @param id
     * @return
     */
    public Integer getOrderForRelatedQueryId(final String id)
    {
        RelatedQuery rq = relatedQueryIdHash.get(id);
        return rq != null ? rq.getDisplayOrder() : Integer.MAX_VALUE;
    }
    
    /**
     * @param id
     * @return
     */
    public Boolean isActiveForRelatedQueryId(final String id)
    {
        RelatedQuery rq = relatedQueryIdHash.get(id);
        return rq != null ? rq.getIsActive() : false;
    }
    
    /**
     * @param id
     * @return
     */
    public Integer getOrderForTableId(final String id)
    {
        SearchTableConfig stc = tableIdHash.get(id);
        return stc != null ? stc.getDisplayOrder() : Integer.MAX_VALUE;
    }
    
    /**
     * @param tableName
     * @param fieldName
     * @return
     */
    public DisplayFieldConfig findDisplayField(final String tableName, 
                                               final String fieldName)
    {
        SearchTableConfig stc = tableNameHash.get(tableName);
        if (stc != null)
        {
            for (DisplayFieldConfig dfc : stc.getDisplayFields())
            {
                if (fieldName.equals(dfc.getFieldName()))
                {
                    return dfc;
                }
            }
        } else
        {
            log.error("Couldn't find table by name["+tableName+"]");
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
        SearchTableConfig stc = tableNameHash.get(tableName);
        if (stc != null)
        {
            for (SearchFieldConfig sfc : stc.getSearchFields())
            {
                if (fieldName.equals(sfc.getFieldName()))
                {
                    return sfc;
                }
            }
        } else
        {
            log.error("Couldn't find table by name["+tableName+"]");
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
    public SearchTableConfig findTableOrCreate(final String tableName)
    {
        SearchTableConfig stc = tableNameHash.get(tableName);
        if (stc == null)
        {
            stc = new SearchTableConfig(tableName, 0);
            stc.initialize();
            tables.add(stc);
            Collections.sort(tables);
            tableIdHash.put(Integer.toString(stc.getTableId()), stc);
            tableNameHash.put(stc.getTableName(), stc);

        }
        return stc;
    }
    
    /**
     * @param tableName
     * @param field
     */
    public void addDisplayField(final String tableName, final DisplayFieldConfig field)
    {
        SearchTableConfig tbl = findTableOrCreate(tableName);
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
        SearchTableConfig stc = tableNameHash.get(tableName);
        if (stc != null)
        {
            for (DisplayFieldConfig sfc : stc.getDisplayFields())
            {
                if (fieldName.equals(sfc.getFieldName()))
                {
                    stc.getDisplayFields().remove(sfc);
                    break;
                }
            }
        } else
        {
            log.error("Couldn't find table by name["+tableName+"]");
        }
    }

    /**
     * @param tableName
     * @param field
     */
    public void addSearchField(final String tableName, final SearchFieldConfig field)
    {
        SearchTableConfig tbl = findTableOrCreate(tableName);
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
        SearchTableConfig stc = tableNameHash.get(tableName);
        if (stc != null)
        {
            for (SearchFieldConfig sfc : stc.getSearchFields())
            {
                if (fieldName.equals(sfc.getFieldName()))
                {
                    stc.getSearchFields().remove(sfc);
                    break;
                }
            }
        } else
        {
            log.error("Couldn't find table by name["+tableName+"]");
        }
    }

    /**
     * 
     * @return the tables
     */
    public Vector<SearchTableConfig> getTables()
    {
        return tables;
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
    public RelatedQuery findRelatedQueryOrCreate(final String id)
    {
        RelatedQuery rq = relatedQueryIdHash.get(id);
        if (rq == null)
        {
            rq = new RelatedQuery(id, Integer.MAX_VALUE, true);
            relatedQueries.add(rq);
            relatedQueryIdHash.put(rq.getId(), rq);
        }
        return rq;
    }
}

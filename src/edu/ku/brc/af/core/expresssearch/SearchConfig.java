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
 * This class keeps track of tables that are configured to participate in the ExpressSearch. Both for searching
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
     * Cleans up.
     */
    public void shutdown()
    {
        if (tables != null)
        {
            tables.clear();
        }
        if (relatedQueries != null)
        {
            relatedQueries.clear();
        }
        if (relatedQueryIdHash != null)
        {
            relatedQueryIdHash.clear();
        }
        if (tableIdHash != null)
        {
            tableIdHash.clear();
        }
        if (tableNameHash != null)
        {
            tableNameHash.clear();
        }
    }
    
    /**
     * Configures the XStream for I/O.
     * @param xstream the stream
     */
    public static void configXStream(final XStream xstream)
    {
        // Aliases
        xstream.alias("search",       SearchConfig.class); //$NON-NLS-1$
        xstream.alias("searchtable",  SearchTableConfig.class); //$NON-NLS-1$
        xstream.alias("searchfield",  SearchFieldConfig.class); //$NON-NLS-1$
        xstream.alias("displayfield", DisplayFieldConfig.class); //$NON-NLS-1$
        xstream.alias("relatedquery", RelatedQuery.class); //$NON-NLS-1$
        
        xstream.aliasAttribute(RelatedQuery.class, "isActive", "isactive"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Things to omit
        xstream.omitField(SearchConfig.class,       "searcher"); //$NON-NLS-1$
        
        xstream.omitField(SearchFieldConfig.class,  "isInUse"); //$NON-NLS-1$
        xstream.omitField(SearchFieldConfig.class,  "fieldInfo"); //$NON-NLS-1$
        xstream.omitField(SearchFieldConfig.class,  "stc"); //$NON-NLS-1$
        
        xstream.omitField(DisplayFieldConfig.class, "isInUse"); //$NON-NLS-1$
        xstream.omitField(DisplayFieldConfig.class, "fieldInfo"); //$NON-NLS-1$
        xstream.omitField(DisplayFieldConfig.class, "stc"); //$NON-NLS-1$
        
        xstream.omitField(RelatedQuery.class, "erti"); //$NON-NLS-1$
        xstream.omitField(RelatedQuery.class, "isInUse"); //$NON-NLS-1$
        xstream.omitField(RelatedQuery.class,  "tableInfo"); //$NON-NLS-1$

        xstream.omitField(SearchTableConfig.class,  "tableInfo"); //$NON-NLS-1$
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
     * Search's for SearchTableConfig by id and returns it.
     * @param id the table id
     * @return the config info.
     */
    public SearchTableConfig getSearchTableConfigById(final int id)
    {
        return tableIdHash.get(Integer.toString(id));
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
     * Returns the order number (ordinal) for the table with the ID.
     * @param id the id of the table
     * @return the ordinal (order) in the display list.
     */
    public Integer getOrderForTableId(final String id)
    {
        SearchTableConfig stc = tableIdHash.get(id);
        return stc != null ? stc.getDisplayOrder() : Integer.MAX_VALUE;
    }
    
    /**
     * Finds a field's DisplayFieldConfig info in a table.
     * @param tableName the table name
     * @param fieldName the field name the config info
     * @return the info
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
            log.error("Couldn't find table by name["+tableName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }

    /**
     * Finds a field's SearchFieldConfig info in a table.
     * @param tableName the table name
     * @param fieldName the field name the config info    
     * @return the config info
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
            log.error("Couldn't find table by name["+tableName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }

    /**
     * Finds the DisplayFieldConfig info for a SearchTableConfig object, and possibly creates it if it isn't found.
     * @param searchTableConfig the table config object
     * @param fieldName the field name
     * @param createWhenNotFound true - creates a new when one isn't found, false doesn't
     * @return the display field config info
     */
    public DisplayFieldConfig findDisplayField(final SearchTableConfig searchTableConfig, 
                                               final String            fieldName,
                                               final boolean           createWhenNotFound)
    {
        if (searchTableConfig != null)
        {
            for (DisplayFieldConfig dfc : searchTableConfig.getDisplayFields())
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
            DisplayFieldConfig dfc =  new DisplayFieldConfig(fieldName, "", Integer.MAX_VALUE); //$NON-NLS-1$
            dfc.setInUse(false);
            if (searchTableConfig != null)
            {
                searchTableConfig.getDisplayFields().add(dfc);
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
            log.error("Couldn't find table by name["+tableName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
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
            log.error("Couldn't find table by name["+tableName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    public void removeTable(final SearchTableConfig stc)
    {
        tables.remove(stc);
        tableIdHash.remove(Integer.toString(stc.getTableId()));
        tableNameHash.remove(stc.getTableName());
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
            rq = new RelatedQuery(id, Integer.MAX_VALUE, false);
            relatedQueries.add(rq);
            relatedQueryIdHash.put(rq.getId(), rq);
        }
        return rq;
    }
}

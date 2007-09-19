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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;


/**
 * This class provides a memory cache of the of the SearchConfig application resource (search_config.xml).
 * and maps ups different hash tables for figuring out what are the join tables for a given search table.
 * 
 * (based on the searches defined in the XML file, not on the schema)
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 10, 2007
 *
 */
public class ExpressSearchConfigCache
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ExpressSearchConfigCache.class);
            
    protected static ExpressSearchConfigCache        instance  = new ExpressSearchConfigCache();
    protected static WeakReference<TableInfoWeakRef> tableInfo = null;
    
    /**
     * Returns the HashMap of ExpressResultsTableInfo items mapped By Name.
     * @return the HashMap of ExpressResultsTableInfo items mapped By Name.
     */
    public static TableInfoWeakRef getTableInfoHashMaps()
    {
        TableInfoWeakRef tableInfoWR = null;
     
        if (instance != null)
        {
            if (tableInfo != null)
            {
                tableInfoWR = tableInfo.get();
            }
            
            if (tableInfoWR == null)
            {
                tableInfoWR = intializeTableInfo();
                tableInfo = new WeakReference<TableInfoWeakRef>(tableInfoWR);
            }
        }
        return tableInfoWR;
    }
    
    /**
     * Returns the TableInfo object by Name.
     * @return the TableInfo object by Name.
     */
    public static ExpressResultsTableInfo getTableInfoByName(final String name)
    {
        return getTableInfoHashMaps().getTables().get(name);
    }
    
    /**
     * Returns the HashMap of ExpressResultsTableInfo items mapped By Name.
     * @return the HashMap of ExpressResultsTableInfo items mapped By Name.
     */
    public static Hashtable<String, ExpressResultsTableInfo> getTableInfoHash()
    {
        return instance != null ? getTableInfoHashMaps().getTables() : null;
    }
    
    /**
     * Returns the Hash for Mapping Id to TableInfo.
     * @return the Hash for Mapping Id to TableInfo.
     */
    public static Hashtable<String, ExpressResultsTableInfo> getSearchIdToTableInfoHash()
    {
        return getTableInfoHashMaps().getSearchIdToTableInfoHash();
    }
    
    /**
     * Returns the Hash for Mapping by Join Id to TableInfo.
     * @return the Hash for Mapping by Join Id to TableInfo.
     */
    public static Hashtable<String, List<ExpressResultsTableInfo>> getJoinIdToTableInfoHash()
    {
        return getTableInfoHashMaps().getJoinIdToTableInfoHash();
    }

    /**
     * Collects information about all the tables that will be processed for any search.
     * @param tableItems the list of Elements to be processed
     * @param tables the table info hash
     */
    protected static void intializeTableInfo(final List<?> tableItems, 
                                             final Hashtable<String, ExpressResultsTableInfo> tables,
                                             final Hashtable<String, ExpressResultsTableInfo> byIdHash,
                                             final Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash,
                                             final boolean isExpressSearch)
    {
        for (Iterator<?> iter = tableItems.iterator(); iter.hasNext(); )
        {
            Element                 tableElement = (Element)iter.next();
            ExpressResultsTableInfo ti           = new ExpressResultsTableInfo(tableElement, isExpressSearch);
            if (byIdHash.get(ti.getId()) == null)
            {
                byIdHash.put(ti.getId(), ti);
                
                if (tables.get(ti.getName()) == null)
                {
                    tables.put(ti.getName(), ti);
                    
                    if (!ti.isIndexed())
                    {
                        ERTIJoinColInfo joinCols[] = ti.getJoins();
                        if (joinCols != null)
                        {
                            for (ERTIJoinColInfo jci :  joinCols)
                            {
                                List<ExpressResultsTableInfo> list = joinIdToTableInfoHash.get(jci.getJoinTableId());
                                if (list == null)
                                {
                                    list = new ArrayList<ExpressResultsTableInfo>();
                                    joinIdToTableInfoHash.put(jci.getJoinTableId(), list);
                                    //log.debug("Adding JOin Table ID["+jci.getJoinTableId()+"]");
                                }
                                list.add(ti);
                            }
                        }
                    }
                    
                } else
                {
                    log.error("Duplicate express Search name["+ti.getName()+"]");
                }

            } else
            {
                log.error("Duplicate Search Id["+ti.getId()+"]");
            }
        } 
    }

    /**
     * Collects information about all the tables that will be processed for any search.
     * @return hash of named ExpressResultsTableInfo
     */
    protected static TableInfoWeakRef intializeTableInfo()
    {
        if (instance != null)
        {
            Hashtable<String, ExpressResultsTableInfo>       tables                = new Hashtable<String, ExpressResultsTableInfo>();
            
            Hashtable<String, ExpressResultsTableInfo>       idToTableInfoHash     = new Hashtable<String, ExpressResultsTableInfo>();
            Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash = new Hashtable<String, List<ExpressResultsTableInfo>>();
            
            try
            {
                Element esDOM = AppContextMgr.getInstance().getResourceAsDOM("SearchConfig"); // Describes the definitions of the full text search
                
                intializeTableInfo(esDOM.selectNodes("/searches/express/table"), tables, idToTableInfoHash, joinIdToTableInfoHash, true);
                
                intializeTableInfo(esDOM.selectNodes("/searches/generic/table"), tables, idToTableInfoHash, joinIdToTableInfoHash, false);
    
                    
            } catch (Exception ex)
            {
                log.error(ex);
                ex.printStackTrace();
            }
            
            // This is sort of bad because it assumes the Task has already been created
            // It really shoud be in nearly all cases, but I can't absolutely guareentee it
            return instance.new TableInfoWeakRef(tables, idToTableInfoHash, joinIdToTableInfoHash);
        }
        // else
        return null;
    }
    
    //------------------------------------------------
    //-- TableInfoWeakRef Inner Class
    //------------------------------------------------
    public class TableInfoWeakRef
    {
        Hashtable<String, ExpressResultsTableInfo>       tables                = new Hashtable<String, ExpressResultsTableInfo>();
        
        Hashtable<String, ExpressResultsTableInfo>       idToTableInfoHash     = new Hashtable<String, ExpressResultsTableInfo>();
        Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash = new Hashtable<String, List<ExpressResultsTableInfo>>();
        
        public TableInfoWeakRef(final Hashtable<String, ExpressResultsTableInfo> tables, 
                                final Hashtable<String, ExpressResultsTableInfo> idToTableInfoHash, 
                                final Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash)
        {
            super();
            this.tables = tables;
            this.idToTableInfoHash = idToTableInfoHash;
            this.joinIdToTableInfoHash = joinIdToTableInfoHash;
        }

        public Hashtable<String, ExpressResultsTableInfo> getSearchIdToTableInfoHash()
        {
            return idToTableInfoHash;
        }

        public Hashtable<String, List<ExpressResultsTableInfo>> getJoinIdToTableInfoHash()
        {
            return joinIdToTableInfoHash;
        }

        public Hashtable<String, ExpressResultsTableInfo> getTables()
        {
            return tables;
        }
    }



}

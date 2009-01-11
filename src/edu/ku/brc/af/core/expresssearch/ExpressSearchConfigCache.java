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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;


/**
 * This class provides a memory cache of the of the SearchConfig application resource (search_config.xml).
 * and maps ups different hash tables for figuring out what are the join tables for a given search table.
 * 
 * (based on the searches defined in the XML file, not on the schema)
 * 
 * Basically this creates three hashtables
 * 1) A hash by ES Name
 * 2) A hash by ES Id
 * 3) A hash from the Actual Table Ids to a list of ExpressResultsTableInfo (Express Searches) 
 *    that are associated with that (so for any given actual Table Id I know what Express Searches
 *    that it particates in.
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
    protected static SoftReference<TableInfoSoftRef> tableInfo = null;
    
    /**
     * Returns the HashMap of ExpressResultsTableInfo items mapped By Name.
     * @return the HashMap of ExpressResultsTableInfo items mapped By Name.
     */
    public static synchronized TableInfoSoftRef getTableInfoWeakRef()
    {
        TableInfoSoftRef tableInfoWR = null;
     
        if (instance != null)
        {
            if (tableInfo != null)
            {
                tableInfoWR = tableInfo.get();
            }
            
            if (tableInfoWR == null)
            {
                tableInfoWR = intializeTableInfo();
                tableInfo = new SoftReference<TableInfoSoftRef>(tableInfoWR);
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
        return getTableInfoWeakRef().getTables().get(name);
    }
    
    /**
     * Returns the HashMap of ExpressResultsTableInfo items mapped By Name.
     * @return the HashMap of ExpressResultsTableInfo items mapped By Name.
     */
    public static Hashtable<String, ExpressResultsTableInfo> getTableInfoHash()
    {
        return instance != null ? getTableInfoWeakRef().getTables() : null;
    }
    
    /**
     * Returns the Hash for Mapping Id to TableInfo.
     * @return the Hash for Mapping Id to TableInfo.
     */
    public static Hashtable<String, ExpressResultsTableInfo> getSearchIdToTableInfoHash()
    {
        return getTableInfoWeakRef().getSearchIdToTableInfoHash();
    }
    
    /**
     * Returns the Hash for Mapping by Join Id to TableInfo.
     * @return the Hash for Mapping by Join Id to TableInfo.
     */
    public static Hashtable<String, List<ExpressResultsTableInfo>> getJoinIdToTableInfoHash()
    {
        return getTableInfoWeakRef().getJoinIdToTableInfoHash();
    }

    /**
     * Collects information about all the tables that will be processed for any search.
     * @param tableItems the list of Elements to be processed
     * @param tables the table info hash
     */
    protected static void intializeTableInfo(final List<?>  tableItems, 
                                             final Hashtable<String, ExpressResultsTableInfo> tables,
                                             final Hashtable<String, ExpressResultsTableInfo> byIdHash,
                                             final Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash,
                                             final boolean        isExpressSearch,
                                             final ResourceBundle resBundle)
    {
        // Basically this creates three hashtables
        // 1) A hash by ES Name
        // 2) A hash by ES Id
        // 3) A hash from the Actual Table Ids to a list of ExpressResultsTableInfo (Express Searches) 
        //    that are associated with that (so for any given actual Table Id I know what Express Searches
        //    that it particates in.
        for (Iterator<?> iter = tableItems.iterator(); iter.hasNext(); )
        {
            // So create the ERTI
            Element                 tableElement = (Element)iter.next();
            ExpressResultsTableInfo erti         = new ExpressResultsTableInfo(tableElement, isExpressSearch, resBundle);
            
            // check for duplicate Ids
            if (byIdHash.get(erti.getId()) == null)
            {
                // put into the Id hash
                byIdHash.put(erti.getId(), erti);
                
                // Check for duplicate names
                if (tables.get(erti.getName()) == null)
                {
                    tables.put(erti.getName(), erti);
                    
                    // Get the List of Join Info
                    ERTIJoinColInfo joinCols[] = erti.getJoins();
                    if (joinCols != null)
                    {
                        // For the current ERTI loop through the Join Info and 
                        // Look up to see if there is already an entry for that TableId
                        // and then add the ERTI to the list.
                        for (ERTIJoinColInfo jci :  joinCols)
                        {
                            List<ExpressResultsTableInfo> list = joinIdToTableInfoHash.get(jci.getJoinTableId());
                            if (list == null)
                            {
                                list = new ArrayList<ExpressResultsTableInfo>();
                                joinIdToTableInfoHash.put(jci.getJoinTableId(), list);
                                //log.debug("Adding Join Table ID["+jci.getJoinTableId()+"]");
                            }
                            list.add(erti);
                        }
                    }
                    
                } else
                {
                    log.error("Duplicate express Search name["+erti.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                }

            } else
            {
                log.error("Duplicate Search Id["+erti.getId()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } 
    }

    /**
     * Collects information about all the tables that will be processed for any search.
     * @return hash of named ExpressResultsTableInfo
     */
    protected static synchronized TableInfoSoftRef intializeTableInfo()
    {
        if (instance != null)
        {
            Hashtable<String, ExpressResultsTableInfo>       tables                = new Hashtable<String, ExpressResultsTableInfo>();
            Hashtable<String, ExpressResultsTableInfo>       idToTableInfoHash     = new Hashtable<String, ExpressResultsTableInfo>();
            Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash = new Hashtable<String, List<ExpressResultsTableInfo>>();
            
            ResourceBundle resBundle = ResourceBundle.getBundle("expresssearch"); //$NON-NLS-1$

            try
            {
                Element esDOM = AppContextMgr.getInstance().getResourceAsDOM("SearchConfig"); // Describes the definitions of the full text search //$NON-NLS-1$
                
                intializeTableInfo(esDOM.selectNodes("/searches/express/table"),  //$NON-NLS-1$
                                   tables, 
                                   idToTableInfoHash, 
                                   joinIdToTableInfoHash, 
                                   true,
                                   resBundle);
                
                intializeTableInfo(esDOM.selectNodes("/searches/generic/table"),  //$NON-NLS-1$
                                                     tables, 
                                                     idToTableInfoHash, 
                                                     joinIdToTableInfoHash, 
                                                     false,
                                                     resBundle);
                    
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExpressSearchConfigCache.class, ex);
                log.error(ex);
                ex.printStackTrace();
                
            }
            
            // This is sort of bad because it assumes the Task has already been created
            // It really shoud be in nearly all cases, but I can't absolutely guareentee it
            return instance.new TableInfoSoftRef(tables, idToTableInfoHash, joinIdToTableInfoHash);
        }
        // else
        return null;
    }
    
    protected static String getResourceString(ResourceBundle resBundle, final String key)
    {
        try 
        {
            return resBundle.getString(key);
            
        } catch (MissingResourceException ex) 
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExpressSearchConfigCache.class, ex);
            log.error("Couldn't find key["+key+"] in resource bundle ["+key+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return key;
        }
    }

    
    //------------------------------------------------
    //-- TableInfoWeakRef Inner Class
    //------------------------------------------------
    public class TableInfoSoftRef
    {
        protected Hashtable<String, ExpressResultsTableInfo>       tables                = new Hashtable<String, ExpressResultsTableInfo>();
        
        protected Hashtable<String, ExpressResultsTableInfo>       idToTableInfoHash     = new Hashtable<String, ExpressResultsTableInfo>();
        protected Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash = new Hashtable<String, List<ExpressResultsTableInfo>>();
        
        public TableInfoSoftRef(final Hashtable<String, ExpressResultsTableInfo> tables, 
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

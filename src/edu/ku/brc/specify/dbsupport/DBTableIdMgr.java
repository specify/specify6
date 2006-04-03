/* Filename:    $RCSfile: DBTableIdMgr.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.dbsupport;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;

import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;

/**
 * This manages all the tables and maps names to ids and can create queries for recordsets
 *
 * @author rods
 *
 */
public class DBTableIdMgr
{
    private static final   Log          log      = LogFactory.getLog(DBTableIdMgr.class);
    protected static final DBTableIdMgr instance;

    Hashtable<Integer, TableInfo> hash = new Hashtable<Integer, TableInfo>();

    static {
        instance = new DBTableIdMgr();
        instance.initialize();
    }

    /**
     * Protected Constructor for Singleton
     */
    protected DBTableIdMgr()
    {
    }
    
    /**
     * Temporary - Waiting for an XML or SOMETHING
     */
    protected void initialize()
    {
        try
        {
            //hash.put(1, new TableInfo(1, "edu.ku.brc.specify.datamodel.CollectionObj", "collectionobj", "collectionObjectId"));
            instance.hash.put(1,   new TableInfo(1, "edu.ku.brc.specify.datamodel.CollectionObject", "collectionobject", "collectionObjectId"));
            instance.hash.put(5,   new TableInfo(5, "edu.ku.brc.specify.datamodel.Agent", "agent", "agentId"));
            instance.hash.put(80,  new TableInfo(80, "edu.ku.brc.specify.datamodel.InfoRequest", "inforequest", "infoRequestID"));
            instance.hash.put(500, new TableInfo(500, "edu.ku.brc.specify.ui.db.PickList", "picklist", "picklist_id"));

        } catch (Exception ex)
        {
            log.error(ex);
        }
        
    }

    /**
     * This looks it up by table name (not Object name) the look up is case insensitive
     * @param name the name
     * @return the id of the table
     */
    public static int lookupIdByShortName(final String name)
    {
        for (TableInfo tableInfo : instance.hash.values())
        {
            String tableName = tableInfo.getTableName();
            int    inx       = tableName.lastIndexOf('.');

            tableName = inx > -1 ? tableName.substring(inx+1) : tableName;
            if (tableName.equalsIgnoreCase(name))
            {
                return tableInfo.getTableId();
            }
        }
        throw new RuntimeException("Couldn't find table id for table name["+name+"]");
    }

    /**
     * Creates a Query object for a table from a recordset, it uses an "in" clause
     * @param recordSet the recordset containing the record ids
     * @return a query object
     */
    public static Query getQueryForTable(final RecordSet recordSet)
    {
        Query     query     = null;
        TableInfo tableInfo = instance.hash.get(recordSet.getTableId());
        if (tableInfo != null)
        {
            StringBuffer strBuf = new StringBuffer("from ");
            strBuf.append(tableInfo.getTableName());
            strBuf.append(" in class ");
            strBuf.append(tableInfo.getShortClassName());
            strBuf.append(" where ");
            strBuf.append(tableInfo.getTableName());
            strBuf.append('.');
            strBuf.append(tableInfo.getPrimaryKeyName());
            strBuf.append(getInClause(recordSet));
            log.info(strBuf.toString());
            //query = HibernateUtil.getCurrentSession().createQuery("from catalogobj in class CollectionObj where catalogobj.collectionObjectId in ('30972.0','30080.0','27794.0','30582.0')");
            query = HibernateUtil.getCurrentSession().createQuery(strBuf.toString());
        }
        return query;
    }

    /**
     * Returns an "in" clause for a recordset
     * @param recordSet the recordset of ids
     * @return a string "in" clause
     */
    public static String getInClause(final RecordSet recordSet)
    {
        StringBuffer strBuf = new StringBuffer(" in (");
        Set set = recordSet.getItems();
        if (set == null)
        {
            throw new RuntimeException("RecordSet items is null!");
        }
        int i = 0;
        for (Iterator iter=set.iterator();iter.hasNext();)
        {
            RecordSetItem rsi = (RecordSetItem)iter.next();
            if (i > 0)
            {
                strBuf.append(",");
            }
            strBuf.append(rsi.getRecordId());
            i++;
        }
        strBuf.append(")");
        return strBuf.toString();
    }

    //------------------------------------------------------
    // Inner Classes
    //------------------------------------------------------
    class TableInfo
    {
        protected int    tableId;
        protected String className;
        protected String tableName;
        protected String primaryKeyName;
        protected Class  classObj;

        public TableInfo(int tableId, String className, String tableName, String primaryKeyName) throws ClassNotFoundException
        {
            this.tableId        = tableId;
            this.className      = className;
            this.tableName      = tableName;
            this.primaryKeyName = primaryKeyName;

            this.classObj       = Class.forName(className);
        }

        public String getShortClassName()
        {
            int inx = className.lastIndexOf('.');
            return inx == -1 ? className : className.substring(inx+1);

        }

        public int getTableId()
        {
            return tableId;
        }


        public String getClassName()
        {
            return className;
        }

        public String getTableName()
        {
            return tableName;
        }

        public String getPrimaryKeyName()
        {
            return primaryKeyName;
        }

        public Class getClassObj()
        {
            return classObj;
        }


    }

}

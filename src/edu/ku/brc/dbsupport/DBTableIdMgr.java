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

package edu.ku.brc.dbsupport;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Query;

import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.util.DatamodelHelper;

/**
 * This manages all the tables and maps names to ids and can create queries for
 * recordsets. (This needs to be updated for all the tables. XXX - Meg??????)
 * 
 * @code_status Alpha *
 * @author rods
 * 
 */
public class DBTableIdMgr
{
	private static final Logger log = Logger.getLogger(DBTableIdMgr.class);
	protected static final DBTableIdMgr instance;
	//private static String datamodelFilename = XMLHelper.getConfigDirPath("specify_datamodel.xml");
	Hashtable<Integer, TableInfo> hash = new Hashtable<Integer, TableInfo>();

	static
	{
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
	 * Reads in datamodel input file and populates the hashtable of teh
	 * DBTableMgr with TableInfo
	 */
	protected void initialize()
	{
		log.info("Reading in datamodel file: " + DatamodelHelper.getDatamodelFilePath()+ " to create and populate DBTableMgr");
		String classname = null;
		try
		{
			File datamodelFile = new File(DatamodelHelper.getDatamodelFilePath());
			FileInputStream fileInputStream = new FileInputStream(datamodelFile);
			SAXReader reader = new SAXReader();
			reader.setValidation(false);
			org.dom4j.Document doc = reader.read(fileInputStream);
			Element databaseNode = doc.getRootElement();

			if (databaseNode != null)
			{
				for (Iterator i = databaseNode.elementIterator("table"); i.hasNext();)
				{
					Element tableNode = (Element) i.next();
					classname = tableNode.attributeValue("classname");
					String tablename = tableNode.attributeValue("table");
					int tableId = Integer.parseInt(tableNode.attributeValue("tableid"));
					String defaultView = tableNode.attributeValue("view");
					String primaryKeyField = null;
					// iterate through child elements of id nodes, there should only be 1
					for (Iterator i2 = tableNode.elementIterator("id"); i2.hasNext();)
					{
						Element idNode = (Element) i2.next();
						primaryKeyField = idNode.attributeValue("name");
					}
					if (classname == null)
						log.error("populating DBTableMgr - classname is null; check input file");
					if (tablename == null)
						log.error("populating DBTableMgr - tablename is null; check input file");
					if (defaultView == null)
						log.debug("populating DBTableMgr - no default view provided for table: " + tablename + "; check input file");
					if (primaryKeyField == null)
						log.error("populating DBTableMgr - primary key is null; check input file");
					log.info("Populating hashtable for class: " + classname);
					instance.hash.put(tableId, new TableInfo(tableId, classname, tablename,
							primaryKeyField, defaultView));
				}
			} else
			{
				log.error("Reading in datamodel file.  SAX parser got null for the root of the document.");
			}

		} catch (java.lang.NumberFormatException numEx)
		{
			log.error("Specify datamodel input file: " + DatamodelHelper.getDatamodelFilePath()
					+ " failed to provide valid table id for class/table:" + classname);
			log.error(numEx);
		} catch (Exception ex)
		{
			log.error(ex);
			ex.printStackTrace();
		}

	}

	/**
	 * Returns the defualt form name for a given table ID
	 * 
	 * @param id
	 *            the ID of a table
	 * @return the default form name
	 */
	public static String lookupDefaultFormNameById(final int id)
	{
		// for now the default name will
		TableInfo tableInfo = instance.hash.get(id);
		if (tableInfo != null)
		{
			return tableInfo.getDefaultFormName();
		}
		return null;
	}

	/**
	 * This looks it up by table name (not Object name) the look up is case
	 * insensitive
	 * 
	 * @param name
	 *            the name
	 * @return the id of the table
	 */
	public static int lookupIdByShortName(final String name)
	{
		for (TableInfo tableInfo : instance.hash.values())
		{
			String tableName = tableInfo.getTableName();
			int inx = tableName.lastIndexOf('.');

			tableName = inx > -1 ? tableName.substring(inx + 1) : tableName;
			if (tableName.equalsIgnoreCase(name))
			{
				return tableInfo.getTableId();
			}
		}
		throw new RuntimeException("Couldn't find table id for table name[" + name + "]");
	}

	/**
	 * This looks it up by fully specified class name the look up is case
	 * sensitive
	 * 
	 * @param className
	 *            the full class name
	 * @return the id of the table
	 */
	public static int lookupIdByClassName(final String className)
	{
		for (TableInfo tableInfo : instance.hash.values())
		{
			if (tableInfo.getClassName().equalsIgnoreCase(className))
			{
				return tableInfo.getTableId();
			}
		}
		throw new RuntimeException("Couldn't find table id for table name[" + className + "]");
	}

	/**
	 * Creates a Query object for a table from a recordset, it uses an "in"
	 * clause
	 * 
	 * @param recordSet
	 *            the recordset containing the record ids
	 * @return a query object
	 */
	public static Query getQueryForTable(final RecordSet recordSet)
	{
		Query query = null;
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
			log.debug(strBuf.toString());
			// query = HibernateUtil.getCurrentSession().createQuery("from
			// catalogobj in class CollectionObj where
			// catalogobj.collectionObjectId in
			// ('30972.0','30080.0','27794.0','30582.0')");
			query = HibernateUtil.getCurrentSession().createQuery(strBuf.toString());
		}
		return query;
	}

	/**
	 * Creates a Query object for a table from a single Record ID
	 * 
	 * @param recordId
	 *            a single Record Id
	 * @return a query object
	 */
	public static Query getQueryForTable(final int tableId, final int recordId)
	{
		Query query = null;
		TableInfo tableInfo = instance.hash.get(tableId);
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
			strBuf.append(" = " + recordId);
			log.debug(strBuf.toString());
			// query = HibernateUtil.getCurrentSession().createQuery("from
			// catalogobj in class CollectionObj where
			// catalogobj.collectionObjectId in
			// ('30972.0','30080.0','27794.0','30582.0')");
			query = HibernateUtil.getSessionFactory().openSession().createQuery(strBuf.toString());
		}
		return query;
	}

	/**
	 * Returns an "in" clause for a recordset
	 * 
	 * @param recordSet
	 *            the recordset of ids
	 * @return a string "in" clause
	 */
	public static String getInClause(final RecordSet recordSet)
	{
		if (recordSet != null)
		{
			StringBuffer strBuf = new StringBuffer(" in (");
			Set set = recordSet.getItems();
			if (set == null)
			{
				throw new RuntimeException("RecordSet items is null!");
			}
			int i = 0;
			for (Iterator iter = set.iterator(); iter.hasNext();)
			{
				RecordSetItem rsi = (RecordSetItem) iter.next();
				if (i > 0)
				{
					strBuf.append(",");
				}
				strBuf.append(rsi.getRecordId());
				i++;
			}
			strBuf.append(")");
			return strBuf.toString();
		} else
		{
			return "";
		}
	}

	// ------------------------------------------------------
	// Inner Classes
	// ------------------------------------------------------
	class TableInfo
	{
		protected int tableId;

		protected String className;

		protected String tableName;

		protected String primaryKeyName;

		protected Class classObj;

		protected String defaultFormName;

		public TableInfo(int tableId, String className, String tableName, String primaryKeyName,
				String defaultFormName) throws ClassNotFoundException
		{
			this.tableId = tableId;
			this.className = className;
			this.tableName = tableName;
			this.primaryKeyName = primaryKeyName;
			this.defaultFormName = defaultFormName;
			try
			{
				this.classObj = Class.forName(className);
				//Class.
			} catch (ClassNotFoundException e)
			{
				log.error("Trying to find class: " + className + " but class was not found");
				e.printStackTrace();
			}
		}

		public String getShortClassName()
		{
			int inx = className.lastIndexOf('.');
			return inx == -1 ? className : className.substring(inx + 1);

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

		public String getDefaultFormName()
		{
			return defaultFormName;
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		@SuppressWarnings("unused") DBTableIdMgr manager;
	}
}

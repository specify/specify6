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

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.IconManager;
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
    public enum RelationshipType { OneToOne, OneToMany, ManyToOne, ManyToMany}
    
    // Static Data Members
	protected static final Logger log = Logger.getLogger(DBTableIdMgr.class);
    private static final DBTableIdMgr instance;
	//private static String datamodelFilename = XMLHelper.getConfigDirPath("specify_datamodel.xml");
    
    // Data Members
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
        // do nothing
	}
    
    /**
     * Returns the singleton.
     * @return the singleton.
     */
    public static DBTableIdMgr getInstance()
    {
        return instance;
    }

	/**
	 * Reads in datamodel input file and populates the hashtable of teh
	 * DBTableMgr with TableInfo
	 */
	protected void initialize()
	{
		log.debug("Reading in datamodel file: " + DatamodelHelper.getDatamodelFilePath()+ " to create and populate DBTableMgr");
		String classname = null;
		try
		{
			File            datamodelFile   = new File(DatamodelHelper.getDatamodelFilePath());
			FileInputStream fileInputStream = new FileInputStream(datamodelFile);
			SAXReader       reader          = new SAXReader();
			reader.setValidation(false);
            
			Document doc          = reader.read(fileInputStream);
			Element  databaseNode = doc.getRootElement();

			if (databaseNode != null)
			{
				for (Iterator<?> i = databaseNode.elementIterator("table"); i.hasNext();)
				{
					Element tableNode = (Element) i.next();
					classname = tableNode.attributeValue("classname");
                    
					String  tablename      = tableNode.attributeValue("table");
					int     tableId        = Integer.parseInt(tableNode.attributeValue("tableid"));
                    boolean isWorkbench    = XMLHelper.getAttr(tableNode, "workbench", false);

					String primaryKeyField = null;
                    
					// iterate through child elements of id nodes, there should only be 1
					for (Iterator<?> i2 = tableNode.elementIterator("id"); i2.hasNext();)
					{
						Element idNode = (Element) i2.next();
						primaryKeyField = idNode.attributeValue("name");
					}  

					if (classname == null)
                    {
						log.error("classname is null; check input file");
                    }
					if (tablename == null)
                    {
						log.error("tablename is null; check input file");
                    }
					if (primaryKeyField == null)
                    {
						log.error("primaryKeyField is null; check input file");
                    }
					//log.debug("Populating hashtable for class: " + classname);
                    
                    TableInfo tblInfo = new TableInfo(tableId, classname, tablename, primaryKeyField);
                    tblInfo.setForWorkBench(isWorkbench);
					instance.hash.put(tableId, tblInfo); 
                    
                    Element idElement = (Element)tableNode.selectSingleNode("id");
                    if (idElement != null)
                    {
                        tblInfo.setIdColumnName(getAttr(idElement, "column", null));
                        tblInfo.setIdFieldName(getAttr(idElement,  "name", null));
                        tblInfo.setIdType(getAttr(idElement,       "type", null));
                    }
                    
                    Element displayElement = (Element)tableNode.selectSingleNode("display");
                    if (displayElement != null)
                    {
                        tblInfo.setDefaultFormName(getAttr(displayElement,  "view", null));
                        tblInfo.setUiFormatter(getAttr(displayElement,      "uiformatter", null));
                        tblInfo.setDataObjFormatter(getAttr(displayElement, "dataobjformatter", null));
                        tblInfo.setSearchDialog(getAttr(displayElement,     "searchdlg", null));
                        tblInfo.setNewObjDialog(getAttr(displayElement,     "newobjdlg", null));
                        tblInfo.setObjTitle(getAttr(displayElement,         "objtitle", ""));
                    } else
                    {
                        tblInfo.setDefaultFormName("");
                        tblInfo.setUiFormatter("");
                        tblInfo.setDataObjFormatter("");
                        tblInfo.setSearchDialog("");
                        tblInfo.setNewObjDialog("");
                        tblInfo.setObjTitle("");  
                    }
                    
                    for (Iterator<?> ir = tableNode.elementIterator("relationship"); ir.hasNext();)
                    {
                        Element irNode = (Element) ir.next();
                        TableRelationship tblRel = new TableRelationship(
                                irNode.attributeValue("relationshipname"),
                                getRelationshipType(irNode.attributeValue("type")),
                                irNode.attributeValue("classname"),
                                irNode.attributeValue("columnname"));
                        tblInfo.getRelationships().add(tblRel);
                    }
                    
                    for (Iterator<?> ir = tableNode.elementIterator("field"); ir.hasNext();)
                    {
                        Element irNode = (Element) ir.next();
                        
                        int len = -1;
                        String lenStr = irNode.attributeValue("length");
                        if (StringUtils.isNotEmpty(lenStr) && StringUtils.isNumeric(lenStr))
                        {
                            len = Integer.parseInt(lenStr);
                        }
                        FieldInfo fieldInfo = new FieldInfo(tblInfo,
                                irNode.attributeValue("column"),
                                irNode.attributeValue("name"),
                                irNode.attributeValue("type"),
                                len);
                        tblInfo.addField(fieldInfo);
                    }
                    Collections.sort(tblInfo.getFields());
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
        log.debug("Done Reading in datamodel file: " + DatamodelHelper.getDatamodelFilePath());
	}
    
    /**
     * Helper to create a FieldInfo Object.
     * @param tableInfo TableInfo Object (owner)
     * @param column the column name
     * @param name the field name
     * @param type the type of field it is
     * @param length the length of the field
     * @return the FieldInfo object
     */
    public static FieldInfo createFieldInfo(TableInfo tableInfo, String column, String name, String type, int length)
    {
        return instance.new FieldInfo(tableInfo, column, name, type, length);
    }
    
    /**
     * Returns the full collection of Tables. 
     * @return a collection of TableInfo objects
     */
    public static Collection<TableInfo> getList()
    {
        return instance.hash.values();
    }

	/**
	 * Returns the defualt form name for a given table ID.
	 * @param id the ID of a table
	 * @return the default form name
	 */
	public static String getDefaultFormNameById(final int id)
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
	 * insensitive.
	 * @param name the name
	 * @return the id of the table
	 */
	public static int getIdByShortName(final String name)
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
     * sensitive.
     * 
     * @param className the full class name
     * @return the id of the table
     */
    public static int getIdByClassName(final String className)
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
     * This looks it up by fully specified class name the look up is case
     * sensitive.
     * 
     * @param className the full class name
     * @return the id of the table
     */
    public static TableInfo getByClassName(final String className)
    {
        for (TableInfo tableInfo : instance.hash.values())
        {
            if (tableInfo.getClassName().equalsIgnoreCase(className))
            {
                return tableInfo;
            }
        }
        throw new RuntimeException("Couldn't find table id for table name[" + className + "]");
    }

    /**
     * This looks it up by specified class (no path) name the look up is case sensitive.
     * 
     * @param className the full class name
     * @return the id of the table
     */
    public static TableInfo getByShortClassName(final String shortClassName)
    {
        for (TableInfo tableInfo : instance.hash.values())
        {
            if (tableInfo.getShortClassName().equalsIgnoreCase(shortClassName))
            {
                return tableInfo;
            }
        }
        throw new RuntimeException("Couldn't find table id for table name[" + shortClassName + "]");
    }

    /**
     * Returns the Info Object By Id.
     * @param tableId the id to look up
     * @return the table info object
     */
    public static TableInfo getInfoById(final int tableId)
    {
        return instance.hash.get(tableId);
    }

	/**
	 * Creates a Query object for a table from a recordset, it uses an "in" clause.
    * @param recordSet the recordset containing the record ids
	 * @return a query object
	 */
	public static String getQueryForTable(final RecordSetIFace recordSet)
	{
		TableInfo tableInfo = instance.hash.get(recordSet.getDbTableId());
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
            return strBuf.toString();
		}
		return null;
	}

	/**
	 * Creates a Query object for a table from a single Record ID.
	 * @param recordId a single Record Id
	 * @return a query object
	 */
	public static String getQueryForTable(final int tableId, final long recordId)
	{
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
            return strBuf.toString();
        }
        return null;
	}

	/**
	 * Returns an "in" clause for a recordset
	 * 
	 * @param recordSet the recordset of ids
	 * @return a string "in" clause
	 */
	public static String getInClause(final RecordSetIFace recordSet)
	{
		if (recordSet != null)
		{
			StringBuffer strBuf = new StringBuffer(" in (");
			Set<RecordSetItemIFace> set = recordSet.getItems();
			if (set == null)
			{
				throw new RuntimeException("RecordSet items is null!");
			}
			int i = 0;
			for (Iterator<RecordSetItemIFace> iter = set.iterator(); iter.hasNext();)
			{
				RecordSetItemIFace rsi = iter.next();
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
        // else
        return "";
	}
    
    /**
     * Converts a String to an Enum for Realtionship Type
     * @param relTypeStr the string
     * @return the relationship type
     */
    public static RelationshipType getRelationshipType(final String relTypeStr)
    {
        if (relTypeStr.equals("one-to-many"))
        {
            return RelationshipType.OneToMany;
            
        } else if (relTypeStr.equals("many-to-one"))
        {
            return RelationshipType.ManyToOne;
            
        } else if (relTypeStr.equals("many-to-many"))
        {
            return RelationshipType.ManyToMany;
            
        } else if (relTypeStr.equals("one-to-one"))
        {
            return RelationshipType.OneToOne;
        }
        return null;
    }

	// ------------------------------------------------------
	// Inner Classes
	// ------------------------------------------------------
	public class TableInfo implements Comparable<TableInfo>
	{
		protected int      tableId;
		protected String   className;
		protected String   tableName;
		protected String   primaryKeyName;
		protected Class<?> classObj;
        protected boolean  isForWorkBench   = false;
        
        // ID Fields
        protected String idColumnName;
        protected String idFieldName;
        protected String idType;
        
        // Display Items
		protected String defaultFormName;
        protected String uiFormatter;
        protected String dataObjFormatter;
        protected String searchDialog;
        protected String newObjDialog;
        protected String objTitle;      // Human readable name
        
        protected Set<TableRelationship> relationships;
        protected List<FieldInfo>        fields;

		public TableInfo(final int    tableId, 
                         final String className, 
                         final String tableName, 
                         final String primaryKeyName)
		{
			this.tableId = tableId;
			this.className = className;
			this.tableName = tableName;
			this.primaryKeyName = primaryKeyName;
			try
			{
				this.classObj = Class.forName(className);
				//Class.
			} catch (ClassNotFoundException e)
			{
				log.error("Trying to find class: " + className + " but class was not found");
				e.printStackTrace();
			}
            relationships = new HashSet<TableRelationship>();
            fields        = new Vector<FieldInfo>();
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

		public Class<?> getClassObj()
		{
			return classObj;
		}

		public String getDefaultFormName()
		{
			return defaultFormName;
		}
        
        public void setDefaultFormName(String defaultFormName)
        {
            this.defaultFormName = defaultFormName;
        }

        public Set<TableRelationship> getRelationships()
        {
            return relationships;
        }

        public ImageIcon getIcon(IconManager.IconSize size)
        {
            return IconManager.getIcon(getShortClassName(), size);
        }
        
        public String getDataObjFormatter()
        {
            return dataObjFormatter;
        }

        public void setDataObjFormatter(String dataObjFormatter)
        {
            this.dataObjFormatter = dataObjFormatter;
        }

        public String getUiFormatter()
        {
            return uiFormatter;
        }

        public void setUiFormatter(String uiFormatter)
        {
            this.uiFormatter = uiFormatter;
        }

        public String getNewObjDialog()
        {
            return newObjDialog;
        }

        public void setNewObjDialog(String newObjDialog)
        {
            this.newObjDialog = newObjDialog;
        }

        public String getEditObjDialog()
        {
            return getNewObjDialog();
        }

        public void setEditObjDialog(String editObjDialog)
        {
            setNewObjDialog(editObjDialog);
        }

        public String getObjTitle()
        {
            return objTitle;
        }

        public void setObjTitle(String objTitle)
        {
            this.objTitle = objTitle;
        }

        public String getSearchDialog()
        {
            return searchDialog;
        }

        public void setSearchDialog(String searchDialog)
        {
            this.searchDialog = searchDialog;
        }

        public String getIdColumnName()
        {
            return idColumnName;
        }

        public void setIdColumnName(String idColumnName)
        {
            this.idColumnName = idColumnName;
        }

        public String getIdFieldName()
        {
            return idFieldName;
        }

        public void setIdFieldName(String idFieldName)
        {
            this.idFieldName = idFieldName;
        }

        public String getIdType()
        {
            return idType;
        }

        public void setIdType(String idType)
        {
            this.idType = idType;
        }

        public boolean isForWorkBench()
        {
            return isForWorkBench;
        }

        public void setForWorkBench(boolean isForWorkBench)
        {
            this.isForWorkBench = isForWorkBench;
        }

        public TableRelationship getRelationshipByName(String name)
        {
            for (TableRelationship tr: relationships)
            {
                String relName = tr.getName();
                if (relName != null && relName.equals(name))
                {
                    return tr;
                }
            }
            return null;
        }

        public RelationshipType getRelType(final String fieldName)
        {
            for (Iterator<TableRelationship> iter = relationships.iterator();iter.hasNext();)
            {
                TableRelationship tblRel = iter.next();
                if (tblRel.getName().equals(fieldName))
                {
                    return tblRel.getType();
                }
            }
            return null;
        }
        
        public void addField(final FieldInfo fieldInfo)
        {
            fields.add(fieldInfo);
        }
        
        public List<FieldInfo> getFields()
        {
            return fields;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return StringUtils.isNotEmpty(objTitle) ? objTitle : tableName;
        }
        

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(TableInfo obj)
        {
            return toString().compareTo(obj.toString());
        }

	}
    
    public class TableRelationship
    {
        protected String           name;
        protected RelationshipType type;
        protected String           className;
        protected String           colName;
        
        public TableRelationship(String name, RelationshipType type, String className, String colName)
        {
            super();
            this.name = name;
            this.type = type;
            this.className = className;
            this.colName = colName;
        }

        public String getClassName()
        {
            return className;
        }

        public String getColName()
        {
            return colName;
        }

        public String getName()
        {
            return name;
        }

        public RelationshipType getType()
        {
            return type;
        }
        
    }
    
    public class FieldInfo implements Comparable<FieldInfo>
    {
        protected TableInfo tableInfo;
        protected String    column;
        protected String    name;
        protected String    type;
        protected int       length;
        
        public FieldInfo(TableInfo tableInfo, String column, String name, String type, int length)
        {
            super();
            this.tableInfo = tableInfo;
            this.column = column;
            this.name = name;
            this.type = type;
            this.length = length;
        }

        public String getColumn()
        {
            return column;
        }

        public int getLength()
        {
            return length;
        }

        public String getName()
        {
            return name;
        }

        public TableInfo getTableInfo()
        {
            return tableInfo;
        }

        public String getType()
        {
            return type;
        }
        
        public String toString()
        {
            return column;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(FieldInfo obj)
        {
            return name.compareTo(obj.name);
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

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
import java.util.Calendar;
import java.util.Collection;
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
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.BusinessRulesIFace;
import edu.ku.brc.util.DatamodelHelper;

/**
 * This manages all the tables and maps names to ids and can create queries for
 * recordsets. (This needs to be updated for all the tables. XXX - Meg??????).
 * <br>
 * TODO Many of the searches are linear and should be converted to bininary searches.
 * 
 * @code_status Betra
 * 
 * @author rods
 * 
 */
public class DBTableIdMgr
{
    public enum RelationshipType { OneToOne, OneToMany, ManyToOne, ManyToMany}
    
    // Static Data Members
	protected static final Logger log = Logger.getLogger(DBTableIdMgr.class);
    
    protected static  DBTableIdMgr instance = null;
    
    // Data Members
    protected Hashtable<Integer, TableInfo> hash = new Hashtable<Integer, TableInfo>();
    protected boolean                       isFullSchema = true;

    /**
     * Can now be created as a standalone class to read in other types of Schema Definitions (i.e. Workbench Schema).
     */
    public DBTableIdMgr(final boolean isFullSchema)
    {
        this.isFullSchema = isFullSchema;
    }
    
    /**
     * Returns the singleton.
     * @return the singleton.
     */
    public static DBTableIdMgr getInstance()
    {
        if (instance == null)
        {
            instance = new DBTableIdMgr(true);
            instance.initialize();
        }
        return instance;
    }

    /**
     * Reads in datamodel input file and populates the hashtable of teh
     * DBTableMgr with TableInfo
     */
    protected void initialize()
    {
        initialize(new File(DatamodelHelper.getDatamodelFilePath()));
    }

    /**
     * Reads in datamodel input file and populates the hashtable of teh
     * DBTableMgr with TableInfo
     */
    public void initialize(final File inputFile)
    {
		log.debug("Reading in datamodel file: " + inputFile.getAbsolutePath() + " to create and populate DBTableMgr");
		String classname = null;
		try
		{
			FileInputStream fileInputStream = new FileInputStream(inputFile);
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
                    boolean isQuery        = XMLHelper.getAttr(tableNode, "query", false);

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
					if (isFullSchema && primaryKeyField == null)
                    {
						log.error("primaryKeyField is null; check input file table["+tablename+"]");
                    }
					//log.debug("Populating hashtable ID["+tableId+"]for class: " + classname+" "+ inputFile.getName());
                    
                    TableInfo tblInfo = new TableInfo(tableId, classname, tablename, primaryKeyField);
                    tblInfo.setForQuery(isQuery);
                    tblInfo.setBusinessRule(XMLHelper.getAttr(tableNode, "businessrule", null));
                    
                    if (hash.get(tableId) != null)
                    {
                        log.error("Table ID used twice["+tableId+"]");
                    }
					hash.put(tableId, tblInfo); 
                    
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
                        
                    } else
                    {
                        tblInfo.setDefaultFormName("");
                        tblInfo.setUiFormatter("");
                        tblInfo.setDataObjFormatter("");
                        tblInfo.setSearchDialog("");
                        tblInfo.setNewObjDialog("");
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
                    //Collections.sort(tblInfo.getFields());
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
     * Cleanups internal state.
     */
    public void cleanUp()
    {
        for (TableInfo ti : hash.values())
        {
            ti.cleanUp();
        }
        hash.clear();
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
    public FieldInfo createFieldInfo(TableInfo tableInfo, String column, String name, String type, int length)
    {
        return new FieldInfo(tableInfo, column, name, type, length);
    }
    
    /**
     * Returns the full collection of Tables. 
     * @return a collection of TableInfo objects
     */
    public Collection<TableInfo> getList()
    {
        return hash.values();
    }

	/**
	 * Returns the defualt form name for a given table ID.
	 * @param id the ID of a table
	 * @return the default form name
	 */
	public String getDefaultFormNameById(final int id)
	{
		// for now the default name will
		TableInfo tableInfo = hash.get(id);
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
	public int getIdByShortName(final String name)
	{
		for (TableInfo tableInfo : hash.values())
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
    public int getIdByClassName(final String className)
    {
        for (TableInfo tableInfo : hash.values())
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
    public TableInfo getByClassName(final String className)
    {
        for (TableInfo tableInfo : hash.values())
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
    public TableInfo getByShortClassName(final String shortClassName)
    {
        // for now just use a brute force linear search
        for (TableInfo tableInfo : hash.values())
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
    public TableInfo getInfoById(final int tableId)
    {
        if (hash.get(tableId) == null)
        {
            log.error("Couldn't find tableId["+tableId+"]");
        }
        return hash.get(tableId);
    }
    
    /**
     * Returns the Info Object By table name (the all lowercase name of the table).
     * @param tableName the name of the table
     * @return the table info object
     */
    public TableInfo getInfoByTableName(final String tableName)
    {
        // for now just use a brute force linear search
        for (TableInfo tblInfo : hash.values())
        {
            if (tblInfo.getTableName().equals(tableName))
            {
                return tblInfo;
            }
        }
        return null;
    }
    
	/**
	 * Creates a Query object for a table from a recordset, it uses an "in" clause.
    * @param recordSet the recordset containing the record ids
	 * @return a query object
	 */
	public String getQueryForTable(final RecordSetIFace recordSet)
	{
		TableInfo tableInfo = hash.get(recordSet.getDbTableId());
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
	public String getQueryForTable(final int tableId, final long recordId)
	{
		TableInfo tableInfo = hash.get(tableId);
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
    
    /**
     * Returns a business rule object for a given class.
     * @param data the data that might have a business rule class
     * @return the business rule object or null
     */
    public BusinessRulesIFace getBusinessRule(Object data)
    {
        if (data != null)
        {
            return getBusinessRule(data.getClass());
        }
        return null;
    }

    /**
     * Returns a business rule object for a given class name.
     * @param classOfObj the class to look up
     * @return the business rule object or null
     */
    public BusinessRulesIFace getBusinessRule(String className)
    {
        try
        {
            return getBusinessRule(Class.forName(className));
            
        } catch (Exception ex)
        {
            //log.error(ex); // this isn't an error
        }
        return null;
    }

    /**
     * Returns a business rule object for a given class.
     * @param classOfObj the class to look up
     * @return the business rule object or null
     */
    public BusinessRulesIFace getBusinessRule(Class classOfObj)
    {
        TableInfo ti = getByClassName(classOfObj.getName());
        if (ti != null)
        {
            String br = ti.getBusinessRule();
            if (StringUtils.isNotEmpty(br))
            {
                try
                {
                    return (BusinessRulesIFace)Class.forName(br).newInstance();
                    
                } catch (Exception ex)
                {
                    log.error("Bad Business Rule class name["+br+"]");
                    log.error(ex);
                }
            }
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
        protected boolean  isForQuery       = false;
        protected String   businessRule;
        
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
				//e.printStackTrace();
			}
            this.objTitle = UIRegistry.getResourceString(this.classObj.getSimpleName());
            relationships = new HashSet<TableRelationship>();
            fields        = new Vector<FieldInfo>();
		}
        
        public void cleanUp()
        {
            relationships.clear();
            fields.clear();
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

        public boolean isForQuery()
        {
            return isForQuery;
        }

        public void setForQuery(boolean isForQuery)
        {
            this.isForQuery = isForQuery;
        }

        public String getBusinessRule()
        {
            return businessRule;
        }

        public void setBusinessRule(String busniessRule)
        {
            this.businessRule = busniessRule;
        }

        /**
         * Assumes all fields have names and returns a FieldInfo object by name
         * @param name the name of the field
         * @return the FieldInfo
         */
        public FieldInfo getFieldByName(final String name)
        {
            for (FieldInfo fldInfo : fields)
            {
                if (fldInfo.getName().equals(name))
                {
                    return fldInfo;
                }
            }
            return null;
        }

        public TableRelationship getRelationshipByName(final String name)
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
        
        public Class getDataClass()
        {
            if (StringUtils.isNotEmpty(type))
            {
                if (type.equals("calendar_date"))
                {
                    return Calendar.class;
                    
                } else if (type.equals("text"))
                {
                    return String.class;
                    
                } else if (type.equals("boolean"))
                {
                    return Boolean.class;
                    
                } else
                {
                    try
                    {
                        return Class.forName(type);
                        
                    } catch (Exception e)
                    {
                        log.error(e);
                    }
                }
            }
            return null;
        }
        
    }
}

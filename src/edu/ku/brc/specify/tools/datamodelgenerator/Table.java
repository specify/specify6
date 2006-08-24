package edu.ku.brc.specify.tools.datamodelgenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import edu.ku.brc.specify.tools.datamodelgenerator.Field;

/**
 * Create table data.
 *
 * @code_status Alpha
 * 
 * @author megkumin
 *
 */
public class Table
{
	private String name;
	private String table;
	private String defaultView;
	private String lazy;
	private String tableId;
	private Collection<Field> fields = new ArrayList<Field>();
	private Collection<Id> ids = new ArrayList<Id>();
	private Collection<Relationship> relationships = new ArrayList<Relationship>();

	/**
	 * @param aName
	 * @param aTable
	 * @param aLazy
	 * @param hash
	 */
	public Table(String name, String table, String lazy,
			String tableId, String defaultView)
	{
		this.name = name;
		this.table = table;
		this.lazy = lazy;
		this.tableId = tableId;
		this.defaultView = defaultView;
	}

	/**
	 * @return
	 * String
	 */
	public String getLazy()
	{
		return lazy;
	}

	/**
	 * @param lazy
	 * void
	 */
	public void setLazy(String lazy)
	{
		this.lazy = lazy;
	}

	/**
	 * @return
	 * String
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return
	 * String
	 */
	public String getTableId()
	{
		return tableId;
	}

	/**
	 * @param name
	 * @return
	 * String
	 */
	protected String createShortName(String name)
	{
		int inx = name.lastIndexOf('.');
		if (inx != -1)
		{
			return name.substring(inx + 1, name.length());
		} else
		{
			return name;
		}

	}


	/**
	 * @param name
	 * void
	 */
	public void setName(String name)
	{
		this.name = name;
	}


	/**
	 * @return
	 * String
	 */
	public String getTable()
	{
		return table;
	}

	/**
	 * @param table
	 * void
	 */
	public void setTable(String table)
	{
		this.table = table;
	}

	/**
	 * @param tableId
	 * void
	 */
	public void setTableId(String tableId)
	{
		this.tableId = tableId;
	}

	public void addField(Field aField)
	{
		fields.add(aField);
	}

	/**
	 * @param rel
	 * void
	 */
	public void addRelationship(Relationship rel)
	{
		relationships.add(rel);
	}


	/**
	 * @param anId
	 * void
	 */
	public void addId(Id anId)
	{
		ids.add(anId);
	}


	/**
	 * @return
	 * Collection<Field>
	 */
	public Collection<Field> getFields()
	{
		return fields;
	}


	/**
	 * @return
	 * Collection<Id>
	 */
	public Collection<Id> getIds()
	{
		return ids;
	}


	/**
	 * @return
	 * Collection<Relationship>
	 */
	public Collection<Relationship> getRelationships()
	{
		return relationships;
	}

	/**
	 * @return
	 * String
	 */
	public String getDefaultView()
	{
		return defaultView;
	}

	/**
	 * @param defaultView
	 * void
	 */
	public void setDefaultView(String defaultView)
	{
		this.defaultView = defaultView;
	}

}

package edu.ku.brc.specify.tools.datamodelgenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Create table data.
 *
 * @code_status Alpha
 * 
 * @author megkumin
 *
 */
public class Table implements Comparable<Table>
{
	private String                   name;
	private String                   table;
	private String                   lazy;
	private String                   tableId;
    private boolean                  query;
    private String                   businessRule;
    private String                   abbrv;
	private List<Field>              fields        = new ArrayList<Field>();
	private List<Id>                 ids           = new ArrayList<Id>();
	private List<Relationship>       relationships = new ArrayList<Relationship>();
    private Display                  display;
    private Desc                     desc;
    private Name                     nameDesc;
    
    // Transient
    private Vector<TableIndex>       indexes = new Vector<TableIndex>();
    
    
	/**
	 * @param aName
	 * @param aTable
	 * @param aLazy
	 * @param hash
	 */
	public Table(String name, 
                 String table, 
                 String lazy,
                 String tableId, 
                 Display display,
                 boolean query, 
                 String businessRule,
                 String abbrv)
	{
		this.name      = name;
		this.table     = table;
		this.lazy      = lazy;
		this.tableId   = tableId;
        this.display   = display;
        this.query     = query;
        this.businessRule = businessRule;
        this.abbrv     = abbrv;
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
     * Returns the name portion before the ".".
	 * @param nameArg the name to be shortened
	 * @return the shortened name
	 */
	protected String createShortName(String nameArg)
	{
		int inx = nameArg.lastIndexOf('.');
		if (inx != -1)
		{
			return nameArg.substring(inx + 1, nameArg.length());
		} else
		{
			return nameArg;
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

    public Display getDisplay()
    {
        return display;
    }

    public boolean getQuery()
    {
        return query;
    }

    public String getBusinessRule()
    {
        return businessRule;
    }
    
    /**
     * @return the indexes
     */
    public Vector<TableIndex> getIndexes()
    {
        return indexes;
    }

    /**
     * @param indexes the indexes to set
     */
    public void setIndexes(Vector<TableIndex> indexes)
    {
        this.indexes = indexes;
    }
    
    /**
     * @return the desc
     */
    public Desc getDesc()
    {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(Desc desc)
    {
        this.desc = desc;
    }

    public void updateIndexFields()
    {
        Collections.sort(fields);
        
        for (TableIndex ti : indexes)
        {
            for (String columnName : ti.getColumnNames())
            {
                for (Field f : fields)
                {
                    if (f.getColumn().equals(columnName))
                    {
                        f.setIndexName(ti.getIndexName());
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * @return the abbrv
     */
    public String getAbbrv()
    {
        return abbrv;
    }

    /**
     * @param abbrv the abbrv to set
     */
    public void setAbbrv(String abbrv)
    {
        this.abbrv = abbrv;
    }

    /**
     * @param nameDesc the nameDesc to set
     */
    public void setNameDesc(Name nameDesc)
    {
        this.nameDesc = nameDesc;
    }

    /**
     * @return the nameDesc
     */
    public Name getNameDesc()
    {
        return nameDesc;
    }

    // Comparable
    public int compareTo(Table obj)
    {
        if (obj == null)
        {
            return 0;
        }
       
        return name.compareTo(obj.name);
    }
}

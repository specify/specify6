package edu.ku.brc.specify.tools.datamodelgenerator;

import java.util.ArrayList;
import java.util.Collection;

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
    private boolean                  workbench;
    private boolean                  query;
    private String                   businessRule;
	private Collection<Field>        fields        = new ArrayList<Field>();
	private Collection<Id>           ids           = new ArrayList<Id>();
	private Collection<Relationship> relationships = new ArrayList<Relationship>();
    private Display                  display;

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
                 boolean workbench,
                 boolean query, 
                 String businessRule)
	{
		this.name      = name;
		this.table     = table;
		this.lazy      = lazy;
		this.tableId   = tableId;
        this.display   = display;
        this.workbench = workbench;
        this.query     = query;
        this.businessRule = businessRule;
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

    public boolean getWorkbench()
    {
        return workbench;
    }

    public boolean getQuery()
    {
        return query;
    }

    public String getBusinessRule()
    {
        return businessRule;
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

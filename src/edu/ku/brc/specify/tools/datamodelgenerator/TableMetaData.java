package edu.ku.brc.specify.tools.datamodelgenerator;

import java.util.Vector;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class TableMetaData
{
	private String  id;
    private String  className;
    private Display display;
    private boolean isForWorkBench;
    private boolean isSearchable;
    private String  businessRule;
    private String  abbrv;
    private Vector<FieldAlias> fieldAliase; 

	/**
	 * @param id
	 * @param className
	 * @param display
	 */
	public TableMetaData(final String  id, 
                         final String  className, 
                         final Display display, 
                         final Vector<FieldAlias> fieldAliase,
                         final boolean isSearchable,
                         final String businessRule,
                         final String abbrv)
	{
		this.id             = id;
        this.className      = className;
        this.display        = display;
        this.fieldAliase    = fieldAliase;
        this.isSearchable   = isSearchable;
        this.businessRule   = businessRule;
        this.abbrv          = abbrv;
	}

	public String getId()
	{
		return id;
	}

	public String getClassName()
    {
        return className;
    }

	public void setId(String id)
	{
		this.id = id;
	}

    public Display getDisplay()
    {
        return display;
    }

    public boolean isForWorkBench()
    {
        return isForWorkBench;
    }

    /**
     * @return the isSearchable
     */
    public boolean isSearchable()
    {
        return isSearchable;
    }

    public String getBusinessRule()
    {
        return businessRule;
    }

    public String getAbbrv()
    {
        return abbrv;
    }

    /**
     * @return the fieldAliase
     */
    public Vector<FieldAlias> getFieldAliase()
    {
        return fieldAliase;
    }
    
}

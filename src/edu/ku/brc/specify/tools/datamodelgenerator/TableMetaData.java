package edu.ku.brc.specify.tools.datamodelgenerator;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class TableMetaData
{
	private String id;
    private String className;
    private Display display;
    private boolean isForWorkBench;
    private boolean isForQuery;
    private String businessRule;

	/**
	 * @param id
	 * @param className
	 * @param display
	 */
	public TableMetaData(final String  id, 
                         final String  className, 
                         final Display display, 
                         final boolean isForQuery,
                         final String businessRule)
	{
		this.id             = id;
        this.className      = className;
        this.display        = display;
        this.isForQuery     = isForQuery;
        this.businessRule   = businessRule;
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

    public boolean isForQuery()
    {
        return isForQuery;
    }

    public String getBusinessRule()
    {
        return businessRule;
    }

}

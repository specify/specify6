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

	/**
	 * @param id
	 * @param className
	 * @param display
	 */
	public TableMetaData(final String id, final String className, final Display display)
	{
		this.id = id;
        this.className = className;
        this.display = display;
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

}

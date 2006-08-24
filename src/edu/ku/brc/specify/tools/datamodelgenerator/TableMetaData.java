package edu.ku.brc.specify.tools.datamodelgenerator;


public class TableMetaData
{
	private String id;
	private String view;

	/**
	 * @param id
	 * @param view
	 */
	public TableMetaData(String id, String view)
	{
		this.id = id;
		this.view = view;
	}

	/**
	 * @return
	 * String
	 */
	public String getDefaultView()
	{
		return view;
	}

	/**
	 * @param view
	 * void
	 */
	public void setDefaultView(String view)
	{
		this.view = view;
	}

	/**
	 * @return
	 * String
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id
	 * void
	 */
	public void setId(String id)
	{
		this.id = id;
	}

}

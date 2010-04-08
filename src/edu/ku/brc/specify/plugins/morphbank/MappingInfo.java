/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

/**
 * @author timo
 *
 */
public class MappingInfo implements Comparable<MappingInfo>
{
	final String name;
	final String dataType;
	final String mapping; //provides a path from the root context to the Specify field associated with the concept
	final int contextTableId; // the root specify table
	/**
	 * @param conceptName
	 * @param conceptMapping
	 * @param conceptContext
	 */
	public MappingInfo(String name, String dataType, String mapping,
			int contextTableId) 
	{
		super();
		this.name = name;
		this.dataType = dataType;
		this.mapping = mapping;
		this.contextTableId = contextTableId;
	}
	/**
	 * @return the conceptName
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * @return the conceptMapping
	 */
	public String getMapping()
	{
		return mapping;
	}
	/**
	 * @return the conceptContext
	 */
	public int getContextTableId()
	{
		return contextTableId;
	}
	
	
	/**
	 * @return the dataType
	 */
	public String getDataType()
	{
		return dataType;
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MappingInfo arg0)
	{
		if (getContextTableId() == arg0.getContextTableId())
		{
			return getName().compareTo(arg0.getName());
		}
		return getContextTableId() < arg0.getContextTableId() ? -1 : 1;
	}
	
	
}

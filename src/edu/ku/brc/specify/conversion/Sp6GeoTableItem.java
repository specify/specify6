package edu.ku.brc.specify.conversion;

public class Sp6GeoTableItem
{
	private int geographyId;
	private String name;
	private int rankId;
	private int nodeNumber;
	private int highChildNodeNumber;
	private int parentId;
	
	/**
	 * @param geographyId
	 * @param name
	 * @param rankId
	 * @param nodeNumber
	 * @param highChildNodeNumber
	 * @param parentId
	 */
	public Sp6GeoTableItem( int geographyId, String name, int rankId, int nodeNumber, int highChildNodeNumber, int parentId )
	{
		super();
		// TODO Auto-generated constructor stub
		this.geographyId = geographyId;
		this.name = name;
		this.rankId = rankId;
		this.nodeNumber = nodeNumber;
		this.highChildNodeNumber = highChildNodeNumber;
		this.parentId = parentId;
	}

	public int getGeographyId()
	{
		return geographyId;
	}

	public void setGeographyId( int geographyId )
	{
		this.geographyId = geographyId;
	}

	public int getHighChildNodeNumber()
	{
		return highChildNodeNumber;
	}

	public void setHighChildNodeNumber( int highChildNodeNumber )
	{
		this.highChildNodeNumber = highChildNodeNumber;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public int getNodeNumber()
	{
		return nodeNumber;
	}

	public void setNodeNumber( int nodeNumber )
	{
		this.nodeNumber = nodeNumber;
	}

	public int getParentId()
	{
		return parentId;
	}

	public void setParentId( int parentId )
	{
		this.parentId = parentId;
	}

	public int getRankId()
	{
		return rankId;
	}

	public void setRankId( int rankId )
	{
		this.rankId = rankId;
	}
}

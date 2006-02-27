package edu.ku.brc.specify.conversion;

public class GeoFileLine
{
	private int id;

	private int parentId;

	private int curId;
	
	private String geoLevels[];

	private String islandGrp;

	private String island;

	private String waterBody;

	private String drainage;

	private String full;

	/**
	 * @param id
	 * @param curId
	 * @param contOrOcean
	 * @param country
	 * @param state
	 * @param county
	 * @param islandGrp
	 * @param island
	 * @param waterBody
	 * @param drainage
	 * @param full
	 */
	public GeoFileLine( int id, int parentId, int curId, String contOrOcean,
			String country, String state, String county, String islandGrp,
			String island, String waterBody, String drainage, String full )
	{
		super();
		this.id = id;
		this.parentId = parentId;
		this.curId = curId;

		this.geoLevels = new String[4];
		this.geoLevels[0] = contOrOcean;
		this.geoLevels[1] = country;
		this.geoLevels[2] = state;
		this.geoLevels[3] = county;
		
		this.islandGrp = islandGrp;
		this.island = island;
		this.waterBody = waterBody;
		this.drainage = drainage;
		this.full = full;
	}

	public int getId()
	{
		return id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public int getParentId()
	{
		return parentId;
	}

	public void setParentId( int pid )
	{
		this.parentId = id;
	}

	public String getContOrOcean()
	{
		return geoLevels[0];
	}

	public String getCountry()
	{
		return geoLevels[1];
	}

	public String getCounty()
	{
		return geoLevels[3];
	}

	public int getCurId()
	{
		return curId;
	}

	public String getDrainage()
	{
		return drainage;
	}

	public String getFull()
	{
		return full;
	}

	public String[] getGeoLevels()
	{
		return geoLevels;
	}
	
	public String getIsland()
	{
		return island;
	}

	public String getIslandGrp()
	{
		return islandGrp;
	}

	public String getState()
	{
		return geoLevels[2];
	}

	public String getWaterBody()
	{
		return waterBody;
	}
}

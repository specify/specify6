package edu.ku.brc.specify.tools.webportal;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;

/**
 * @author timo
 *
 * Makes tree level instances (Genus, Species, Country, State, ...) used in querybuilder and exporter 
 * accessible via the DBTableIdMgr. 
 */
public class DBTreeLevelInfo implements DBTableChildIFace 
{

	final String rankName;
	final DBFieldInfo fldInfo;
	
	/**
	 * @param rankName
	 * @param fldInfo
	 */
	DBTreeLevelInfo(String rankName, DBFieldInfo fldInfo)
	{
		this.rankName = rankName;
		this.fldInfo = fldInfo;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.core.db.DBTableChildIFace#getDescription()
	 */
	@Override
	public String getDescription() 
	{
		String result = rankName;
		if (fldInfo != null)
		{
			result += ": " + fldInfo.getDescription();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.core.db.DBTableChildIFace#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) 
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.core.db.DBTableChildIFace#getTitle()
	 */
	@Override
	public String getTitle() 
	{
		String result = rankName;
		if (fldInfo != null && !fldInfo.getName().equalsIgnoreCase("name"))
		{
			result += " " + fldInfo.getTitle();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.core.db.DBTableChildIFace#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.core.db.DBTableChildIFace#getName()
	 */
	@Override
	public String getName() 
	{
		String result = rankName;
		if (fldInfo != null)
		{
			result += "." + fldInfo.getName();
		}
		return result;
	}

	@Override
	public boolean isHidden() 
	{
		if (fldInfo != null)
		{
			return fldInfo.isHidden();
		}
		return false;
	}

	@Override
	public void setHidden(boolean isHidden) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isUpdatable() 
	{
		if (fldInfo != null)
		{
			return fldInfo.isUpdatable();
		}
		return false;
	}

	@Override
	public boolean isRequired() 
	{
		if (fldInfo != null)
		{
			return fldInfo.isRequired();
		}
		return false;
	}

	@Override
	public Class<?> getDataClass() 
	{
		if (fldInfo != null)
		{
			return fldInfo.getDataClass();
		}
		return null;
	}

	/**
	 * @return the rankName
	 */
	public String getRankName() 
	{
		return rankName;
	}

	/**
	 * @return the fldInfo
	 */
	public DBFieldInfo getFldInfo() 
	{
		return fldInfo;
	}

	
}

/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.util.Calendar;
import java.util.Date;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tools.export.MappedFieldInfo;

/**
 * @author timo
 *
 */
public class MappingInfo implements Comparable<MappingInfo>
{
	final String name;
	final Class<?> dataType;
	final String mapping; //provides a path from the root context to the Specify field associated with the concept
	final int contextTableId; // the root specify table
	final boolean isFormatted;

	
	/**
	 * @param conceptName
	 * @param conceptMapping
	 * @param conceptContext
	 */
	public MappingInfo(String name, String dwcType, String mapping,
			int contextTableId, boolean isFormatted) 
	{
		super();
		this.name = name;
		this.mapping = mapping;
		this.contextTableId = contextTableId;
		this.dataType = getClassForDwcType(dwcType, name);
		this.isFormatted = isFormatted;
	}
	
	/**
	 * @param name
	 * @param mfi
	 */
	public MappingInfo(String name, MappedFieldInfo mfi)
	{
		super();
		this.name = name;
		this.mapping = mfi.getStringId();
		this.contextTableId = mfi.getContextTableId();
		this.dataType = mfi.getDataType();
		this.isFormatted = mfi.isRel();
	}
	
	/**
	 * @param dwcType
	 * @return
	 */
	protected Class<?> getClassForDwcType(String dwcType, String name)
	{
		
		if (dwcType == null)
		{
			//Some concepts don't have type is spexportschemaitem - possibly an import problem or a problem with our .xsd files??
			if (name.startsWith("DecimalLatitude") || name.equals("DecimalLongitude"))
			{
				return Double.class;
			}
			if (name.equalsIgnoreCase("DayCollected"))
			{
				return Calendar.class;
			}
			if (name.endsWith("Collected") || name.endsWith("Identified"))
			{
				return Integer.class;
			}
			
			return String.class;
		}
		
		if (dwcType.equals("xsd:string"))
		{
			return String.class;
		}
		if (dwcType.equals("xsd:dateTime"))
		{
			return Date.class;
		}
		if (dwcType.equals("xsd:decimal"))
		{
			return Double.class;
		}
		if (dwcType.equals("xsd:nonNegativeInteger"))
		{
			return Integer.class;
		}
		if (dwcType.equals("xsd:gYear"))
		{
			return Integer.class;
		}
		return null;
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
	public Class<?> getDataType()
	{
		return dataType;
	}
	
	
	/**
	 * @return the isFormatted
	 */
	public boolean isFormatted()
	{
		return isFormatted;
	}

	/**
	 * @return tableid for the table containing this field
	 */
	protected int getMyContextTblId()
	{
		String[] tbls = mapping.split(",");
		String tblSeg = tbls[tbls.length-1];
		String[] tblSegs = tblSeg.split("\\.");
		String tbl = tblSegs[0];
		return Integer.parseInt(tbl.split("-")[0]);
	}
	/**
	 * @return true if the field represents a field associated with a specified rank in a treeable table
	 */
	public boolean isTreeRank()
	{
		//This is a little iffy, but will probably work.
		DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(getMyContextTblId());
		if (Treeable.class.isAssignableFrom(tbl.getClassObj()))
		{
			if (tbl.getFieldByName(name) == null)
			{
				return true;
			}
		}
		return false;				
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MappingInfo arg0)
	{
		if (getContextTableId() == arg0.getContextTableId())
		{
			return getMapping().compareTo(arg0.getMapping());
		}
		return getContextTableId() < arg0.getContextTableId() ? -1 : 1;
	}
	
	
}

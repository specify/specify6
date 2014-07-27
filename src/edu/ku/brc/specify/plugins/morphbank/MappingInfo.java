/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tools.export.MappedFieldInfo;

/**
 * @author timo
 *
 */
public class MappingInfo implements Comparable<MappingInfo>
{
	protected final String name;
	protected final String term;
	protected final Class<?> dataType;
	protected final String mapping; //provides a path from the root context to the Specify field associated with the concept
	protected final int contextTableId; // the root specify table
	protected final boolean isFormatted;
	protected static Map<Class<?>,Set<String>> treeRankNames = new HashMap<Class<?>, Set<String>>();

	
	/**
	 * @param conceptName
	 * @param conceptMapping
	 * @param conceptContext
	 */
	public MappingInfo(String name, String termBaseUri, String dwcType, String mapping,
			int contextTableId, boolean isFormatted) 
	{
		super();
		this.name = name;
		String slash = termBaseUri.endsWith("/") ? "" : "/";
		this.term = StringUtils.isEmpty(termBaseUri) ? name : termBaseUri + slash + name; 
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
		this.term = mfi.getTerm();
		this.mapping = mfi.getStringId();
		this.contextTableId = mfi.getContextTableId();
		this.dataType = mfi.getDataType();
		this.isFormatted = mfi.isRel();
	}
	
	/**
	 * @param dwcType
	 * @return
	 */
	protected Class<?> getClassForDwcType(String aDwcType, String mappedName)
	{
		String name = mappedName.toLowerCase();
		String dwcType = aDwcType.toLowerCase();
		if (dwcType == null)
		{
			//Some concepts don't have type is spexportschemaitem - possibly an import problem or a problem with our .xsd files??
			if (name.startsWith("decimallatitude") || name.equals("decimallongitude"))
			{
				return Double.class;
			}
			if (name.equalsIgnoreCase("daycollected"))
			{
				return Calendar.class;
			}
			if (name.endsWith("collected") || name.endsWith("identified"))
			{
				return Integer.class;
			}
			
			return String.class;
		}
		
		if (dwcType.endsWith(":string"))
		{
			return String.class;
		}
		if (dwcType.endsWith(":datetime") || dwcType.endsWith(":datetimeiso"))
		{
			return Date.class;
		}
		if (dwcType.startsWith("dwc:decimal") || dwcType.endsWith(":decimal") || dwcType.endsWith(":double"))
		{
			return Double.class;
		}
		if (dwcType.endsWith(":nonnegativeinteger") || dwcType.endsWith(":positiveinteger"))
		{
			return Integer.class;
		}
		if (dwcType.endsWith(":gyear"))
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
	public int getMappedTblId()
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
		DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(getMappedTblId());
		if (Treeable.class.isAssignableFrom(tbl.getClassObj()))
		{
			return getTreeRanks(tbl.getClassObj()).contains(getMappedFieldName());
		}
		return false;				
	}
	
	protected Set<String> getTreeRanks(Class<?> cls)
	{
		Set<String> ranks = treeRankNames.get(cls);
		if (ranks == null)
		{
			ranks = buildTreeRankNames(cls);
			treeRankNames.put(cls, ranks);
		}
		return ranks;
	}
	
	/**
	 * build list of ranks for all trees associated with the type
	 * Assumes that cls is Treeable
	 */
	protected Set<String> buildTreeRankNames(Class<?> cls)
	{
		Set<String> result = new HashSet<String>();
		Vector<Object> ranks = BasicSQLUtils.querySingleCol("select distinct name from " + getTreeDefItemTblName(cls));
		for (Object rank : ranks)
		{
				result.add(rank.toString());
		}
		return result;
	}

	
	/**
	 * @param cls
	 * @return name of treedefitem table for cls
	 */
	protected String getTreeDefItemTblName(Class<?> cls)
	{
		return cls.getSimpleName().toLowerCase() + "treedefitem";
	}
	
	/**
	 * @return name of mapped specify db field.
	 */
	protected String getMappedFieldName()
	{
		String[] chunks = mapping.split("\\.");
		return chunks[chunks.length-1];
	}
	
	
	/**
	 * @return the term
	 */
	public String getTerm() 
	{
		return term;
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

package edu.ku.brc.specify.tools.webportal;

import java.lang.reflect.Field;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;

/**
 * @author timo
 *
 *
 */
public class WebPortalFieldDef 
{
	private final String colName;
	private final String solrName;
	private final String solrType;
	private final String title;
	private final String type;
	private final int width;
	private final String concept; //schema - darwincore or other - concept
	private final String conceptUrl;
	private final String spTable;
	private final String spTableTitle;
	private final String spFld;
	private final String spFldTitle;
	private final String spDescription;
	private final int colIdx; //col idx in db table
	private final ExportMappingInfo mapInfo;

	private boolean advancedSearch; //include in advanced search
	private int displayColIdx; //initial col idx in results display table
	
	/**
	 * @param mapInfo
	 */
	public WebPortalFieldDef(ExportMappingInfo mapInfo, String solrName, String solrType) 
	{
		this.mapInfo = mapInfo;
		this.solrName = solrName;
		this.solrType = solrType;
		this.colIdx = mapInfo.getColIdx();
		this.displayColIdx = this.colIdx;
		this.concept = mapInfo.getConcept();
		this.conceptUrl = mapInfo.getConceptSchema();
		this.spFld = mapInfo.getSpFldName();

		DBTableChildIFace dbInfo = mapInfo.getInfo();
		
		this.spFldTitle = dbInfo.getTitle();
		this.title = this.spFldTitle;
		this.spDescription = dbInfo.getDescription();
			
		if (dbInfo instanceof DBFieldInfo)
		{
			this.type = ((DBFieldInfo) dbInfo).getType();
			this.width = ((DBFieldInfo) dbInfo).getLength();

		} else if (dbInfo instanceof DBRelationshipInfo)
		{
			this.type = "java.lang.String";
			this.width = ExportMappingHelper.getDefaultFldLenForFormattedFld();
			
		} else if (dbInfo instanceof DBTreeLevelInfo) 
		{
			DBTreeLevelInfo treeInfo = (DBTreeLevelInfo )dbInfo;
			if (treeInfo.getFldInfo() != null)
			{
				this.type = treeInfo.getFldInfo().getType();
				this.width = treeInfo.getFldInfo().getLength();
			} else
			{
				this.type = "java.lang.String";
				this.width = ExportMappingHelper.getDefaultFldLenForFormattedFld();
			}
		} else
		{
			this.type = "java.lang.String";
			this.width = ExportMappingHelper.getDefaultFldLenForFormattedFld();
		}
		
		this.colName = mapInfo.getColName();
		this.spTable = mapInfo.getTblInfo().getName();
		this.spTableTitle = mapInfo.getTblInfo().getTitle();
	
		
		this.advancedSearch = true;
	}

	/**
	 * @param val
	 * @return
	 */
	protected String getJsonDelim(Object val) 
	{
		if (val instanceof Number)
		{
			return "";
		}
		return "\"";
	}
	
	/**
	 * @param name
	 * @param val
	 * @return
	 */
	protected String toJson(String name, Object val)
	{
		return "\"" + name + "\":" + getJsonDelim(val) + val + getJsonDelim(val);
	}
	
	/**
	 * @param f
	 * @return
	 */
	protected boolean isToJson(Field f)
	{
		return !"mapInfo".equalsIgnoreCase(f.getName());
	}
	
	/**
	 * @return
	 * @throws IllegalAccessException
	 */
	public String toJson() throws IllegalAccessException
	{
		StringBuilder result = new StringBuilder("{");
		boolean addSep = false;
		for (Field f : getClass().getDeclaredFields())
		{
			if (isToJson(f)) 
			{
				Object val = f.get(this);
				if (val != null)
				{
					if (addSep)
					{
						result.append(", ");
					} else
					{
						addSep = true;
					}
					result.append(toJson(f.getName().toLowerCase(), val));
				}
			}
		}
		result.append("}");
		return result.toString();
	}
	

	/**
	 * @return the advancedSearch
	 */
	public boolean isAdvancedSearch() {
		return advancedSearch;
	}

	/**
	 * @param advancedSearch the advancedSearch to set
	 */
	public void setAdvancedSearch(boolean advancedSearch) {
		this.advancedSearch = advancedSearch;
	}

	/**
	 * @return the displayColIdx
	 */
	public int getDisplayColIdx() {
		return displayColIdx;
	}

	/**
	 * @param displayColIdx the displayColIdx to set
	 */
	public void setDisplayColIdx(int displayColIdx) {
		this.displayColIdx = displayColIdx;
	}

	/**
	 * @return the solrName
	 */
	public String getSolrName() {
		return solrName;
	}

	
	/**
	 * @return the solrType
	 */
	public String getSolrType() {
		return solrType;
	}

	/**
	 * @return the colName
	 */
	public String getColName() {
		return colName;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the concept
	 */
	public String getConcept() {
		return concept;
	}

	/**
	 * @return the conceptUrl
	 */
	public String getConceptUrl() {
		return conceptUrl;
	}

	/**
	 * @return the spTable
	 */
	public String getSpTable() {
		return spTable;
	}

	/**
	 * @return the spTableTitle
	 */
	public String getSpTableTitle() {
		return spTableTitle;
	}

	/**
	 * @return the spFld
	 */
	public String getSpFld() {
		return spFld;
	}

	/**
	 * @return the spFldTitle
	 */
	public String getSpFldTitle() {
		return spFldTitle;
	}

	/**
	 * @return the spDescription
	 */
	public String getSpDescription() {
		return spDescription;
	}

	/**
	 * @return the colIdx
	 */
	public int getColIdx() {
		return colIdx;
	}

	/**
	 * @return the mapInfo
	 */
	public ExportMappingInfo getMapInfo() {
		return mapInfo;
	}
	
	
}

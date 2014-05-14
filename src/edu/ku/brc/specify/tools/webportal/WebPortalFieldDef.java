package edu.ku.brc.specify.tools.webportal;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;

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
	private String treeId;
	private Integer treeRank;
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
		
		if (dbInfo != null && conceptUrl != null) 
		{
			this.spFldTitle = dbInfo.getTitle();
			this.title = this.spFldTitle;
			this.spDescription = dbInfo.getDescription();
		} else 
		{
			this.spFldTitle = mapInfo.getSpFldName();
			this.title = conceptUrl != null ? this.spFldTitle : (StringUtils.isNotBlank(mapInfo.getConcept()) ? mapInfo.getConcept() : this.spFldTitle);
			this.spDescription = null;
		}
		
		boolean treed = false;
		
		if (dbInfo instanceof DBFieldInfo)
		{
			if (mapInfo.isRecordTyper()) {
				this.type = "java.lang.String";
				this.width = 60;
			} else {
				this.type = ((DBFieldInfo) dbInfo).getType();
				this.width = ((DBFieldInfo) dbInfo).getLength();
			}

		} else if (dbInfo instanceof DBRelationshipInfo)
		{
			this.type = "java.lang.String";
			this.width = ExportMappingHelper.getDefaultFldLenForFormattedFld();
			
		} else if (dbInfo instanceof DBTreeLevelInfo) 
		{
			DBTreeLevelInfo treeInfo = (DBTreeLevelInfo )dbInfo;
			if (treeInfo.getFldInfo() != null)
			{
				String tempType = treeInfo.getFldInfo().getType();
				if ("java.util.Calendar".equals(tempType)) 
            	{	
            		String fldId = mapInfo.getFldId();
            		if (fldId.endsWith("NumericDay") || fldId.endsWith("NumericMonth") || fldId.endsWith("NumericYear"))
            		{
            			tempType = "java.lang.Integer";
            			//XXX technically should change length also, but it probably doesn't matter
            		}		
            	}
				this.type = tempType;
            	this.width = treeInfo.getFldInfo().getLength();
            	if (treeInfo.getFldInfo().getName().equalsIgnoreCase("name"))
            	{
    				this.treeId = mapInfo.getTblInfo().getName();
    				this.treeRank = treeInfo.getRankId();
    				treed = true;            		
            	}
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
		
		if (!treed)
		{
			this.treeId = null;
			this.treeRank = null;
		}
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
	
	private String escape4Json(String str) {
		//escapes quotes,\, /, \r, \n, \b, \f, \t
		String[] badJson = {"\\", "\"", "\r", "\n", "\b", "\f", "\t"};
		String result = str;
		for (String bad : badJson) {
			result = result.replace(bad, "\\" + bad);
		}
		return result;
	}

	/**
	 * @param name
	 * @param val
	 * @return
	 */
	protected String toJson(String name, Object val)
	{
		return "\"" + name + "\":" + getJsonDelim(val) + escape4Json(val.toString()) + getJsonDelim(val);
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

	/**
	 * @return the treeId
	 */
	public String getTreeId() {
		return treeId;
	}

	/**
	 * @return the treeRank
	 */
	public Integer getTreeRank() {
		return treeRank;
	}
	
}

/**
 * 
 */
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.helpers.XMLHelper.addAttr;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.dom4j.Element;

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.SpQueryField.OperatorType;
import edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanel;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable;

/**
 * @author tnoble
 * 
 * Application that converts xml query definitions from Specify 5
 * format into Specify 6 format.
 *
 */
public class QueryConverter
{

	//Sp5QueryOps 
	protected final static int scLike = 0; 
	protected final static int scIn = 1; 
	protected final static int scBetween = 2; 
	protected final static int scExactlyLike = 3; 
	protected final static int scLessThan = 4;
    protected final static int scMoreThan = 5; 
    protected final static int scContains = 6; 
    protected final static int scDontCare = 7; 
    protected final static int scYes = 8; 
    protected final static int scNo = 9;
    protected final static int scEmpty = 10; 
    protected final static int scOn = 11; 
    protected final static int scBefore = 12; 
    protected final static int scAfter = 13; 
    protected final static int scPartial = 14;
	
    /**
     * @param sp5QueryOp
     * @return the SpQueryField operator equivalent to sp5QueryOp
     */
    protected static SpQueryField.OperatorType getSp6Op(int sp5QueryOp)
    {
    	switch (sp5QueryOp){
    	case scLike: 
    		return OperatorType.LIKE;
    	case scIn: 
    		return OperatorType.IN;
    	case scBetween: 
    		return OperatorType.BETWEEN;
    	case scExactlyLike: 
    	case scOn: 
    		return OperatorType.EQUALS;
    	case scLessThan: 
    	case scBefore: 
    		return OperatorType.LESSTHAN;
    	case scMoreThan: 
    	case scAfter: 
    		return OperatorType.GREATERTHAN;
    	case scContains: 
    		return OperatorType.CONTAINS;
    	case scDontCare: 
    		return OperatorType.DONTCARE;
    	case scYes: 
    		return OperatorType.TRUE;
    	case scNo: 
    		return OperatorType.FALSE;
    	case scEmpty: 
    		return OperatorType.EMPTY;
    	default: 
    		return null;
    	}
    }
	
	/**
	 * @param fiveTableName
	 * @param fiveSubType
	 * @return name of the Specify6 table equivalent to the supplied Specify5 tablename and type 
	 */
	protected static String getSixTableName(final String fiveTableName, final String fiveSubType) throws Exception
	{
		if (fiveTableName.equalsIgnoreCase("taxonname"))
		{
			return "taxon";
		}
		if (fiveTableName.equals("BorrowAgents"))
		{
			return "borrowagent";
		}
		if (fiveTableName.equalsIgnoreCase("collectors"))
		{
			return "collector";
		}
		if (fiveTableName.equals("CollectionObjectCatalog"))
		{
			return "collectionobject"; //XXX not sure about this
		}
		if (fiveTableName.equals("CollectionObject"))
		{
			if (fiveSubType != null && fiveSubType.endsWith("Preparation"))
			{
				return "preparation";
			}
			return "collectionobject";
		}
		if (fiveTableName.equals("LoanAgents"))
		{
			if (fiveSubType != null && fiveSubType.equals("Gift"))
			{
				return "giftagent";
			}
			return "loanagent";
		}
		if (fiveTableName.equals("Habitat"))
		{
			return "collectingeventattribute";
		}
		if (fiveTableName.equals("BiologicalObjectAttributes"))
		{
			return "collectionobjectattribute";
		}
		if (fiveTableName.equals("LoanPhysicalObject"))
		{
			if (fiveSubType.equals("Gift"))
			{
				return "giftpreparation";
			}
			return "loanpreparation";
		}
		if (fiveTableName.equals("Loan") && fiveSubType.equals("Gift"))
		{
			return "gift";
		}
		if (fiveTableName.equals("CatalogSeries"))
		{
			throw new Exception(fiveTableName);
		}
		//XXX lots more conditions...
		
		return fiveTableName.toLowerCase(); 
	}
		
	/**
	 * @param sixTblName
	 * @param fiveTblName
	 * @param fiveFldName
	 * @return Specify6 equivalent for a Specify5 table and field
	 */
	protected static String getSixFldName(final String sixTblName, final String fiveTblName, final String fiveFldName)
	{
		String result = UploadTable.deCapitalize(fiveFldName);
		if (result.equalsIgnoreCase("collectionobjectcatalogid"))
		{
			result = "collectionobjectid";
		}
		if (result.equalsIgnoreCase("continentOrOcean"))
		{
			result = "Continent";
		}
		if (result.equals("loanNumber") && sixTblName.equals("gift"))
		{
			result = "giftNumber";
		}		
		if (result.equals("name") && sixTblName.equals("agent"))
		{
			result = "lastName"; //XXX ???
		}
		if (result.equals("number") && sixTblName.equalsIgnoreCase("accession"))
		{
			result = "accessionNumber";
		}
		if (result.equalsIgnoreCase("preparationmethod"))
		{
			result = "name"; //for this to work, some trickery is required in getTableIdListAndRelFldName()
		}
		if (result.equalsIgnoreCase("taxonnameid"))
		{
			result = "taxonid";
		}
		if (result.equalsIgnoreCase("current") && sixTblName.equalsIgnoreCase("determination"))
		{
			result = "isCurrent";
		}
		if (result.equalsIgnoreCase("fulltaxonname"))
		{
			result = "fullName";
		}
		if (result.equalsIgnoreCase("FullGeographicName"))
		{
			result = "fullName";
		}
		if (result.equalsIgnoreCase("LastEditedBy"))
		{
			result = "lastName"; 
		}
		return result; 
	}
	
	/**
	 * @param FiveQueryXML an xml definition for a Specify 5 query
	 * @return a Specify 6 query xml definition
	 */
	public static void getSixQueryXML(final StringBuilder sb,
			final Element fiveQueryXML, final List<String> unconvertedFields) throws Exception
	{
		StringBuilder querySb = new StringBuilder();
		querySb.append("<query ");
		String queryName = fiveQueryXML.attributeValue("name");
		addAttr(querySb, "name", queryName);
		String fiveTblName = fiveQueryXML.attributeValue("contextName");
		String fiveSubType = fiveQueryXML.attributeValue("subType", null);
		String contextName = getSixTableName(fiveTblName, fiveSubType);
		addAttr(querySb, "contextName", contextName);
		addAttr(querySb, "contextTableId", DBTableIdMgr.getInstance().getIdByShortName(contextName));
		addAttr(querySb, "isFavorite", fiveQueryXML.attributeValue("isFavorite"));
		addAttr(querySb, "named", fiveQueryXML.attributeValue("named"));
		addAttr(querySb, "ordinal", fiveQueryXML.attributeValue("ordinal"));
		querySb.append(">\r\n");

		querySb.append("<fields>\r\n");
        
        //Using Set to filter possible duplicate fld defs caused by Sp5 sub types or unsupported fields/tables.
		SortedSet<String> queryLines = new TreeSet<String>(new Comparator<String>(){

			@Override
			public int compare(String arg0, String arg1)
			{
				//assuming no nulls.
				String fld0 = arg0.replaceFirst("position=\".*\" isNot", "isNot");
				String fld1 = arg1.replaceFirst("position=\".*\" isNot", "isNot");
				return fld0.compareTo(fld1);
			}
			
		});
        for (Object fldObj : fiveQueryXML.selectNodes("fields/field"))
        {
            String fldStr = getSixQueryFieldXML((Element )fldObj, contextName, queryName, unconvertedFields);
            if (fldStr != null && !queryLines.contains(fldStr))
            {
            	queryLines.add(fldStr);
            	querySb.append(fldStr + "\r\n");
            }	
        }
        querySb.append("</fields>\r\n");
        querySb.append("</query>");
        sb.append(querySb);
	}
	
	/**
	 * @param sb
	 * @param fiveXML
	 * @param unconvertedFields a list of fields that were not converted to 6
	 */
	protected static String getSixQueryFieldXML(final Element fiveXML, final String sixQueryTblName, 
			final String queryName, final List<String> unconvertedFields)
	{
		boolean converted = false;
		Exception exception = null;
		StringBuilder fldSb = new StringBuilder();
		String result = null;
		try
		{
		if (fiveXML.attributeValue("isConvertable", "true").equals("true"))
		{
			converted = true;
			fldSb.append("<field ");
			addAttr(fldSb, "position", fiveXML.attributeValue("position"));
			addAttr(fldSb, "isNot", fiveXML.attributeValue("isNot"));
			addAttr(fldSb, "isDisplay", fiveXML.attributeValue("isDisplay"));
			addAttr(fldSb, "isPrompt", fiveXML.attributeValue("isPrompt"));
			boolean isRelFld = fiveXML.attributeValue("isRelFld", "false").equals("false") ? false : true;
			addAttr(fldSb, "isRelFld", isRelFld);
			addAttr(fldSb, "isAlwaysFilter", fiveXML.attributeValue("isAlwaysFilter"));
			addAttr(fldSb, "startValue", fiveXML.attributeValue("startValue"));
			addAttr(fldSb, "endValue", fiveXML.attributeValue("endValue"));
			addAttr(fldSb, "sortType", fiveXML.attributeValue("sortType"));
			//addAttr(fldSb, "fieldAlias", fiveXML.attributeValue("fieldAlias")); //XXX ???

			String fiveTblName = fiveXML.attributeValue("contextTable");
			String fiveSubType = fiveXML.attributeValue("subType", null);
			String contextTable = getSixTableName(fiveTblName, fiveSubType);
			addAttr(fldSb, "contextTableIdent", DBTableIdMgr.getInstance().getIdByShortName(contextTable));
			String sixFldName = getSixFldName(contextTable, fiveTblName, fiveXML.attributeValue("name"));
			if (!isRelFld)
			{
				addAttr(fldSb, "fieldName", sixFldName);
				addAttr(fldSb, "fieldAlias", sixFldName);
			}

			String sixTblIdList = null;
			String[] idListInfo = processTableIdList(fiveXML, sixFldName, contextTable, isRelFld);
			if (idListInfo[0] != null)
			{
				sixTblIdList = idListInfo[0];
				if (isRelFld && idListInfo[1] != null)
				{
					sixFldName = idListInfo[1];
				}
				if (idListInfo[2] != null)
				{
					contextTable = idListInfo[2];
				}
			}
			else
			{
				converted = false;
			}
			
			if (converted)
			{
				if (isRelFld)
				{
					addAttr(fldSb, "fieldName", sixFldName);
					addAttr(fldSb, "fieldAlias", sixFldName);
				}
				addAttr(fldSb, "tableList", sixTblIdList);
				addAttr(fldSb, "stringId", sixTblIdList + "." + contextTable + "." + sixFldName);
				addAttr(fldSb, "operStart", 
						getOperatorAttribute(fiveXML.attributeValue("operStart", ""), 
								fiveXML.attributeValue("dataType")));
				addAttr(fldSb, "operEnd", fiveXML.attributeValue("operEnd"));
			}
		}
		} catch (Exception ex)
		{
			converted = false;
			exception = ex;
		}
		if (!converted)
		{
			
			String text = queryName + " - " + 
				fiveXML.attributeValue("contextTable", "unknown") + "." + fiveXML.attributeValue("name", "unknown");
			if (exception != null)
			{
				text += ": " + exception.getMessage();
			}
			unconvertedFields.add(text);
		}	
		else
		{
			fldSb.append(" />");
			result = fldSb.toString();
		}
		return result;
	}

	/**
	 * @param sp5Type
	 * @return a list of OperatorTypes associated with a data type.
	 */
	protected static SpQueryField.OperatorType[] getSp6Ops(String sp5Type)
	{
		if (sp5Type.equals("tree")) 
		{
			return new SpQueryField.OperatorType[] {
                SpQueryField.OperatorType.EQUALS,
                SpQueryField.OperatorType.LIKE,
                SpQueryField.OperatorType.IN,
                SpQueryField.OperatorType.EMPTY};
		}
//		else if (sp5Type.equals("picklist"))
//		{
//			return new SpQueryField.OperatorType[] {
//                    SpQueryField.OperatorType.EQUALS,
//                    SpQueryField.OperatorType.IN,
//                    SpQueryField.OperatorType.EMPTY};
//		}
		else
		{
			Class<?> cls = null;
			if (sp5Type.equals("string") || sp5Type.equals("picklist"))
			{
				cls = String.class;
			}
			else if (sp5Type.equals("boolean"))
			{
				cls = Boolean.class;
			}
			else if (sp5Type.equals("date"))
			{
				cls = java.sql.Timestamp.class;
			}
			return QueryFieldPanel.getComparatorListForClass(cls);
		}
	}
	
	/**
	 * @param sp5Op
	 * @param sp5Type
	 * @return the specify 6 operStart or operEnd attribute for the
	 * given Specify 5 operator and data type.
	 */
	protected static String getOperatorAttribute(String sp5Op, String sp5Type)
	{
		if (!sp5Op.equals(""))
		{
			SpQueryField.OperatorType sp6Op = getSp6Op(Integer.valueOf(sp5Op));
			SpQueryField.OperatorType[] ops = getSp6Ops(sp5Type);
			for (int op = 0; op < ops.length; op++)
			{
				if (ops[op].equals(sp6Op))
				{
					return String.valueOf(op);
				}
			}
		}
		return "";
	}
	
	/**
	 * @param fiveXML
	 * @param sixFldName
	 * @param sixTblName
	 * @param isRelFld
	 * @return a three element String array.
	 * Element 1 is the tableIdList for the field
	 * Element 2 is a transformed name for the field, or null if the name does not need to be changed.
	 * Element 3 is a transformed name for the field's table, or null.
	 */
	protected static String[] processTableIdList(final Element fiveXML, final String sixFldName,
			final String sixTblName, final boolean isRelFld) throws Exception
	{
		String fiveTblList = fiveXML.attributeValue("tableList").replaceAll(", ", ",");
		String newSixFldName = null;
		String newContextTblName = null;
		//CollectionObject, CollectingEvent-CollectingEventID:CollectingEventID, Locality-LocalityID:LocalityID, Geography-GeographyID:GeographyID
		String sixTblIdList = "";
		String[] fiveLinks = fiveTblList.split(",");
		String prevSixTbl = null;
		String prevSubType = null;
		for (int l = 0; l < fiveLinks.length; l++)
		{
			String fiveLink = preProcessLink(fiveLinks, l);
			if (fiveLink == null)
			{
				continue;
			}
			
			String[] linkParts = fiveLink.split("-");
			String tblPart = linkParts[0];
			String relPart = linkParts.length > 1 ? linkParts[1] : null;
			String[] tblParts = tblPart.split("\\.");
			String tblName = tblParts[0];
			String subType = tblParts.length > 1 ? tblParts[1] : null;
			String sixTbl = getSixTableName(tblName, subType == null ? prevSubType : subType);
			if (!sixTbl.equals(prevSixTbl))
			{
				DBTableInfo prevInfo = prevSixTbl != null 
					? DBTableIdMgr.getInstance().getInfoByTableName(prevSixTbl) : null;
				DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(sixTbl);
				String sixLink = String.valueOf(info.getTableId());
				if (relPart != null)
				{
					DBRelationshipInfo match = getRelationship(relPart, info, prevInfo, sixTbl, tblName);
					if (match != null)
					{
						if (!match.getName().equalsIgnoreCase(info.getName()))
						{
							sixLink += "-" + match.getName();
						}
						if (isRelFld)
						{
							newSixFldName = match.getName();
						}
					}
					else
					{
						return null;
					}
					
				}
				String[] specialStuff = processSpecialCases(sixLink, sixFldName, sixTblName, fiveXML.attributeValue("name"));
				if (sixTblIdList.length() > 0 && specialStuff[0] != null)
				{
					sixTblIdList += ",";
				}
				sixTblIdList += specialStuff[0];
				//very cheap...
				if (specialStuff[1] != null)
				{
					newContextTblName = specialStuff[1];
				}
				prevSixTbl = sixTbl;
				if (subType != null)
				{
					prevSubType = subType;
				}
			}
		}
		String[] result = new String[3];
		result[0] = sixTblIdList;
		result[1] = newSixFldName;
		result[2] = newContextTblName;
		return result;
	}
	
	/**
	 * @param relPart
	 * @param info
	 * @param prevInfo
	 * @param sixTbl
	 * @param tblName
	 * @return
	 */
	protected static DBRelationshipInfo getRelationship(final String relPart, 
			final DBTableInfo info, final DBTableInfo prevInfo, final String sixTbl, 
			final String tblName)
	{
		String[] relParts = relPart.split(":");
		String fromKey = getSixFldName(sixTbl, tblName, relParts[0]);
		//String toKey = relParts[1];
		Vector<DBRelationshipInfo> matches = new Vector<DBRelationshipInfo>();
		for (DBRelationshipInfo rel : prevInfo.getRelationships())
		{
			if (rel.getDataClass().equals(info.getClassObj()))
			{
				matches.add(rel);
			}
		}
		DBRelationshipInfo match = null;
		if (matches.size() > 0)
		{
			if (matches.size() == 1)
			{
				//just use it.
				match = matches.get(0);
			}
			else for (DBRelationshipInfo candidate : matches)
			{
				if (fromKey.equalsIgnoreCase(candidate.getColName()))
				{
					match = candidate;
				} 
			}
			
			if (match == null && info.getClassObj().equals(edu.ku.brc.specify.datamodel.Agent.class))
			{
				//remove modifiedby and createdby rels...
				for (int m = matches.size() - 1; m >= 0; m--)
				{
					DBRelationshipInfo rel = matches.get(m);
					if (rel.getName().equalsIgnoreCase("modifiedbyagent")
							|| rel.getName().equalsIgnoreCase("createdbyagent"))
					{
						matches.remove(m);
					}
				}
				if (matches.size() == 1)
				{
					match = matches.get(0);
				}
			}
		}
		return match;
	}
	
	/**
	 * @param fiveLinks an array of linked tables
	 * @param linkIndex the index of the link to process.
	 * @return Specify 6 equivalent for the specified Specify 5 relationship
	 */
	protected static String preProcessLink(final String[] fiveLinks, final int linkIndex)
	{
		String link = fiveLinks[linkIndex];	
		if (link.toLowerCase().startsWith("agentaddress-agent"))
		{
			String nextLink = fiveLinks[linkIndex+1];
			if (nextLink.toLowerCase().startsWith("agent"))
			{
				return null;
			}
			if (nextLink.toLowerCase().startsWith("address"))
			{
				//trouble. Need to switch/insert to Agent link, but don't know what foreign key to use.
				return "Agent-?:AgentID";
			}
			//else more trouble, but currently the link couldn't be realized in Sp6 anyway...
		}
		return fiveLinks[linkIndex];
	}
	
	/**
	 * @param node
	 * @param sixFldName
	 * @param sixTblName
	 * @return a 2 element array, the first element is the possibly transformed node, the second, if non-null
	 * is a transformed value for the contextTable for the field being processed.
	 */
	protected static String[] processSpecialCases(final String node, final String sixFldName, final String sixTblName, final String fiveFldName)
	{
		String[] result = new String[2];
		if (sixFldName.equalsIgnoreCase("name") && sixTblName.equalsIgnoreCase("preparation") && node.equalsIgnoreCase("63-preparations"))
		{
			result[0] = node + ",65";
			result[1] = "preptype";
		} else if (sixFldName.equalsIgnoreCase("lastName") && fiveFldName.equalsIgnoreCase("LastEditedBy"))
		{
			result[0] = node + ",5-modifiedByAgent";
			result[1] = "agent";
		}
		else
		{
			result[0] = node;
			result[1] = null;
		}
		return result;
	}
	
	/**
	 * @param args: InputFileName OutputFileName LogFileName
	 * 
	 * Reads an xml file containing Sp 5 query definitions,
	 * and writes an xml file containing Sp 6 query definitions for the Sp 5 queries.
	 * 
	 *  Unconvertable items are listed in the log file.
	 */
	public static void main(String[] args)
	{
		try
		{
	        if (args.length != 3)
	        {
	        	System.out.println("Usage: QueryConverter InputFileName OutputFileName LogFileName");
	        	System.exit(1);
	        }
//			for (String s : args)
//	        {
//	            String[] pairs = s.split("="); //$NON-NLS-1$
//	            if (pairs.length == 2)
//	            {
//	                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
//	                {
//	                    //System.err.println("["+pairs[0].substring(2, pairs[0].length())+"]["+pairs[1]+"]");
//	                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
//	                } 
//	            } else
//	            {
//	                String symbol = pairs[0].substring(2, pairs[0].length());
//	                //System.err.println("["+symbol+"]");
//	                System.setProperty(symbol, symbol);
//	            }
//	        }

	        String fiveQueryFile = args[0];
	        String sixQueryFile = args[1];
	        String logFile = args[2];
	        
			//Element fiveXML = XMLHelper.readFileToDOM4J(new File("C:\\SpecifyFiveSix\\sp5qtest.xml"));
			//Element fiveXML = XMLHelper.readFileToDOM4J(new File("C:\\SpecifyFiveSix\\KUI_FishQueries.xml"));
			Element fiveXML = XMLHelper.readFileToDOM4J(new File(fiveQueryFile));
			StringBuilder sb = new StringBuilder();
			sb.append("<queries>\r\n");
			Vector<String> unconvertable = new Vector<String>();
			Vector<String> allUnconvertables = new Vector<String>();
			for (Object fiveQ : fiveXML.selectNodes("/queries/sp5query"))
			{
				Element fiveQE = (Element )fiveQ;
				System.out.println("processing " + fiveQE.attributeValue("name", "unknown"));
				try 
				{
					getSixQueryXML(sb, fiveQE, unconvertable);
				} catch (Exception ex)
				{
					System.out.println("  " + ex.getClass().getName() + " - " + ex.getMessage());
					unconvertable.clear();
					allUnconvertables.add("unabled to convert query: " + fiveQE.attributeValue("name", "unknown"));
				}
				if (unconvertable.size() > 0)
				{
					allUnconvertables.addAll(unconvertable);
					unconvertable.clear();
				}	
				sb.append("\r\n");
			}
			sb.append("</queries>");
			//FileUtils.writeStringToFile(new File("C:\\SpecifyFiveSix\\convertedSp5Q.xml"), sb.toString());
			FileUtils.writeStringToFile(new File(sixQueryFile), sb.toString());
			//FileUtils.writeLines(new File("C:\\SpecifyFiveSix\\UnconvertedFields.txt"), allUnconvertables);
			FileUtils.writeLines(new File(logFile), allUnconvertables);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(1);
		}
		
	}
		
}


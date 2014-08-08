/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo.RelationshipType;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.PaleoContext;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class LocalityDuplicateRemover
{
	protected static String[] systemFldNames = {"TimestampCreated", "TimestampModified", "Version", "Lat1Text", "Lat2Text",
		"Long1Text", "Long2Text", "ModifiedByAgentID", "CreatedByAgentID", "LocalityID", "LocalityDetailID",
		"GeoCoordDetailID", "CollectingEventID", "CollectorID", "DisciplineID", "CollectingEventAttributeID", "CollectingTripID",
		"VisibilitySetByID", "GUID",
		"Datum"//XXX Hack!!!!!!!!!!!!!!!!!!!!!!!!!! This list currently needs to be adapted to the table being de-duped. e.g. for ce LocalityID and DisciplineID need to be removed.
	};
	
	public static final int GET_ALL_FLDS = 0;
	public static final int GET_USER_FLDS = 1;
	public static final int GET_SYS_FLDS = 2;
	
	protected static boolean isUserFld(final String column)
	{
		//XXX How inefficient is this???
		for (String systemFldName : systemFldNames)
		{
			if (systemFldName.equals(column))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @param tbl
	 * @param fldNo
	 * @return
	 */
	protected static Pair<String, Integer> getFldList(DBTableInfo tbl, int fldNo) {
		return getFldList(tbl, fldNo, GET_USER_FLDS);
	}

	
	/**
	 * @param getType
	 * @param fld
	 * @return
	 */
	protected static boolean isFldToGet(int getType, String fld) {
		if (getType == GET_ALL_FLDS) {
			return true;
		} else if (getType == GET_USER_FLDS) {
			return isUserFld(fld);
		} else if (getType == GET_SYS_FLDS) {
			return !isUserFld(fld);
		} else {
			return false;
		}
	}
	
	/**
	 * @param tbl
	 * @param fldNo
	 * @param getType
	 * @return
	 */
	protected static Pair<String, Integer> getFldList(DBTableInfo tbl, int fldNo, int getType)
	{
		String result = "";
		int fld = fldNo;
		for (DBFieldInfo fi : tbl.getFields())
		{
			System.out.println("getFldList: checking " + tbl.getName() + "." + fi.getColumn());
			if (isFldToGet(getType, fi.getColumn()))
			{
				result += (fld++ > 0 ? ", " : "") + tbl.getAbbrev() + "." + fi.getColumn();
			}
		}
		for (DBRelationshipInfo rel : tbl.getRelationships())
		{
			if (StringUtils.isNotBlank(rel.getColName()))
			{
				System.out.println("getFldList: checking " + tbl.getName() + "." + rel.getColName());
				if (isFldToGet(getType, rel.getColName()))
				{
					result += (fld++ > 0 ? ", " : "") + tbl.getAbbrev() + "." + rel.getColName();
				}
			}
		}
		return new Pair<String, Integer>(result, fld);
	}

	protected static boolean isSystemRel(DBRelationshipInfo rel) {
		if (rel.getClassName().equals("edu.ku.brc.specify.datamodel.Agent")) {
			return "CreatedByAgentID".equalsIgnoreCase(rel.getColName()) || "ModifiedByAgentID".equalsIgnoreCase(rel.getColName());
		} else if (rel.getClassName().equals("edu.ku.brc.specify.datamodel.Locality")) {
			return "LocalityID".equalsIgnoreCase(rel.getColName());
		}
		return false;
	}
	
	protected static int getRepsForTbl(DBTableInfo tbl) {
		if ("collector".equalsIgnoreCase(tbl.getName())) {
			return 4;
		} else {
			return 1;
		}
	}
	
	protected static List<Pair<Pair<DBTableInfo, Object>, Integer>> getFldsForTbl(Pair<DBTableInfo, DBRelationshipInfo> info) {
		return getFldsForTbl(info, GET_USER_FLDS);
	}

	/**
	 * @param getType
	 * @param rel
	 * @return
	 */
	protected static boolean isRelToGet(int getType, DBRelationshipInfo rel) {
		if (getType == GET_ALL_FLDS) {
			return true;
		} else if (getType == GET_USER_FLDS) {
			return !isSystemRel(rel);
		} else if (getType == GET_SYS_FLDS) {
			return isSystemRel(rel);
		} else {
			return false;
		}
	}
	
	/**
	 * @param info
	 * @param getType
	 * @return
	 */
	protected static List<Pair<Pair<DBTableInfo, Object>, Integer>> getFldsForTbl(Pair<DBTableInfo, DBRelationshipInfo> info, int getType) {
		List<Pair<Pair<DBTableInfo, Object>, Integer>> result = new ArrayList<Pair<Pair<DBTableInfo, Object>, Integer>>();
		DBTableInfo tbl = info.getFirst();
		DBRelationshipInfo parRel = info.getSecond();
		
		int maxR =  getRepsForTbl(tbl);
		for (int r = 0; r < maxR; r++) {
			for (DBFieldInfo fi : tbl.getFields()) {
				System.out.println("getFldsForTable: checking " + tbl.getName() + "." + fi.getColumn());
				if (isFldToGet(getType, fi.getColumn()))	{
					result.add(new Pair<Pair<DBTableInfo, Object>, Integer>(new Pair<DBTableInfo, Object>(tbl, fi), r));
				}
			}
			for (DBRelationshipInfo rel : tbl.getRelationships()) {
				if ((parRel == null || !(parRel.getOtherSide().equals(rel.getName()) && rel.getOtherSide().equals(parRel.getName()))) 
						&& isRelToGet(getType, rel) && StringUtils.isNotBlank(rel.getColName())) {
					System.out.println("getFldsForTable: checking " + tbl.getName() + "." + rel.getColName());
					DBTableInfo relTbl = DBTableIdMgr.getInstance().getByClassName(rel.getDataClass().getName());
					if (rel.getType().ordinal() == DBRelationshipInfo.RelationshipType.ManyToOne.ordinal() && !rel.getDataClass().getSimpleName().endsWith("Attribute")) {
						//if (isUserFld(rel.getColName())/* || rel.getColName().equalsIgnoreCase(relTbl.getPrimaryKeyName())*/); {
						result.add(new Pair<Pair<DBTableInfo, Object>, Integer>(new Pair<DBTableInfo, Object>(tbl, rel), r));
					}
				}
			}
		}
//		if (tbl.getTableId() == CollectingEvent.getClassTableId()) {
//			result.add(new Pair<DBTableInfo, Object> )
//		}
		return result;
	}
	
	protected static List<Pair<Pair<DBTableInfo, Object>, Integer>> getTblsFlds(Vector<Pair<DBTableInfo, DBRelationshipInfo>> tbls) {
		List<Pair<Pair<DBTableInfo, Object>, Integer>> result = new ArrayList<Pair<Pair<DBTableInfo, Object>, Integer>>();
		for (Pair<DBTableInfo, DBRelationshipInfo> tbl : tbls) {
			result.addAll(getFldsForTbl(tbl));
		}
		return result;
	}

	protected static String getDbObjName(Pair<DBTableInfo, Object> dbObj, boolean includeTblAbbrev, Integer seq) throws Exception
	{
		if (dbObj.getSecond() instanceof DBFieldInfo)
		{
			return (includeTblAbbrev ? dbObj.getFirst().getAbbrev() + (seq == null ? "" : seq) + "." : "") + ((DBFieldInfo)dbObj.getSecond()).getColumn();
		} else if (dbObj.getSecond() instanceof DBRelationshipInfo)
		{
			return (includeTblAbbrev ? dbObj.getFirst().getAbbrev() + (seq == null ? "" : seq) + "." : "") + ((DBRelationshipInfo)dbObj.getSecond()).getColName();
		}
		throw new Exception("Unknown db object type " + dbObj.getClass().getName());
	}
	
	protected static String getSqlFldsClause(List<Pair<Pair<DBTableInfo, Object>, Integer>> dbObjs) throws Exception {
		String result = "";
		boolean comma = false;
		for (Pair<Pair<DBTableInfo, Object>, Integer> dbObj : dbObjs) {
			if (comma) {
				result += ", ";
			} else {
				comma = true;
			}
			result += getDbObjName(dbObj.getFirst(), true, dbObj.getSecond());
		}
		return result;
	}
	
	protected static String getDistinctRecSql(DBTableInfo tbl, boolean countOnly, boolean dupsOnly) throws Exception
	{
		String result = "select " + (countOnly ? "count(distinct " : "");
		
		Vector<Pair<DBTableInfo, DBRelationshipInfo>> tbls = getOwnedOneOrManyRelatedTables(tbl);
		tbls.insertElementAt(new Pair<DBTableInfo, DBRelationshipInfo>(tbl, null), 0);
		List<Pair<Pair<DBTableInfo, Object>, Integer>> flds = getTblsFlds(tbls);
		result += getSqlFldsClause(flds);
		
		if (countOnly)
		{
			result += ")";
		} else
		{
			result += ", count( " + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName() + ")"; 
		}
		
		result += getJoinToOwnedChildren(tbl);
		if (!countOnly)
		{
			result += " group by ";
			for (int f = 1; f <= flds.size(); f++)
			{
				if (f > 1)
				{
					result += ", ";
				}
				result += f;
			}
		}
		if (dupsOnly)
		{
			result += " having count(" + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName() + ") > 1";
		}
		return result;
	}
	
	private static String getAddlJoinCriteria(DBTableInfo info, int seq) throws Exception {
		if ("collector".equalsIgnoreCase(info.getName())) {
			return " and " + info.getAbbrev() + seq + ".OrderNumber=" + seq;
			//return " and (" + info.getAbbrev() + seq + " is null or "+ info.getAbbrev() + seq + ".OrderNumber=" + seq + ")";
		} else {
			return "";
		}
	}
	
	private static String getJoinToOwnedChildren(DBTableInfo tbl) throws Exception
	{
		Vector<Pair<DBTableInfo, DBRelationshipInfo>> tbls = getOwnedOneOrManyRelatedTables(tbl);
		String result = " from " + tbl.getName() + " " + tbl.getAbbrev() + "0";
		for (Pair<DBTableInfo, DBRelationshipInfo> owned : tbls) {
			DBTableInfo info = owned.getFirst();
			DBRelationshipInfo rel = owned.getSecond();
			DBTableInfo other = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
			DBRelationshipInfo otherSide = other.getRelationshipByName(rel.getOtherSide());
			String colName = otherSide.getColName() == null ? other.getPrimaryKeyName() : otherSide.getColName();
			int maxR = getRepsForTbl(owned.getFirst());
			//if (maxR == 1) {
			//	result += " left join " + info.getName() + " " + info.getAbbrev() + " on "
			//			+ info.getAbbrev() + "." + colName + " = " + tbl.getAbbrev() + "." + tbl.getPrimaryKeyName();
			//} else {
				for (int r = 0; r < maxR; r++) {
					result += " left join " + info.getName() + " " + info.getAbbrev() + r + " on "
							+ info.getAbbrev() + r + "." + colName + " = " + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName()
							+ getAddlJoinCriteria(info, r);
				}
			//}
		}
		return result;
		//XXX get general!
		//return getLocalityTblsJoin();
	}
	
	private static String getLocalityTblsJoin()
	{
		DBTableInfo locTbl = DBTableIdMgr.getInstance().getByShortClassName("Locality");
		DBTableInfo locDetTbl = DBTableIdMgr.getInstance().getByShortClassName("LocalityDetail");
		DBTableInfo geoCoordDetTbl = DBTableIdMgr.getInstance().getByShortClassName("GeoCoordDetail");
		return " from locality " + locTbl.getAbbrev() + " left join localitydetail " + locDetTbl.getAbbrev()
		+ " on " + locDetTbl.getAbbrev() + ".LocalityID = " + locTbl.getAbbrev() + ".LocalityID left join "
		+ "geocoorddetail " + geoCoordDetTbl.getAbbrev() + " on " + geoCoordDetTbl.getAbbrev() + ".LocalityID = "
		+ locTbl.getAbbrev() + ".LocalityID";
		
	}
	
	
	public static Vector<Object[]> getDistinctRecs(final Connection conn, int tableId, boolean dupsOnly) throws Exception
	{
		DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(tableId);
		String sql = getDistinctRecSql(tbl, false, dupsOnly);
		return BasicSQLUtils.query(conn, sql);
	}
	
	public static Integer createDuplicates(Connection conn, Integer key, DBTableInfo tbl, Pair<DBTableInfo, Object> parentFld, 
			Integer parentId) throws Exception
	{
		String recSql = "insert into " + tbl.getName() + "(";
		Vector<String> colNames = new Vector<String>();
		for (DBFieldInfo fld : tbl.getFields())
		{
			colNames.add(fld.getColumn());
		}
		for (DBRelationshipInfo rel : tbl.getRelationships())
		{
			if (StringUtils.isNotBlank(rel.getColName()) && colNames.indexOf(rel.getColName()) == -1)
			{
				colNames.add(rel.getColName());
			}
		}
		int parentFldIdx = -1;
		if (parentFld != null && parentId != null)
		{
			parentFldIdx = colNames.indexOf(getDbObjName(parentFld, false, 0));
			if (parentFldIdx == -1)
			{
				colNames.add(getDbObjName(parentFld, false, 0));
				parentFldIdx = colNames.size() - 1;
			}
		}
		for (int c = 0; c < colNames.size(); c++)
		{
			recSql += (c > 0 ? ", " : "") + colNames.get(c);
		}
		
		recSql += ") select ";

		for (int c = 0; c < colNames.size(); c++)
		{
			if (c != parentFldIdx)
			{
				recSql += (c > 0 ? ", " : "") + colNames.get(c);
			} else
			{
				recSql += (c > 0 ? ", " : "") + parentId; 
			}
		}
		
		recSql += " from " + tbl.getName() + " where " + tbl.getPrimaryKeyName() + " = " + key;
		
		if (BasicSQLUtils.update(conn, recSql) > 0)
		{
			return BasicSQLUtils.querySingleObj(conn, "select max(" + tbl.getPrimaryKeyName() + ") from " + tbl.getName());
		} 
		return null;
	}
	
	public static Integer duplicateLocality(Connection conn, Integer locId) throws Exception
	{
		DBTableInfo locTbl = DBTableIdMgr.getInstance().getByShortClassName("Locality");
		Integer newLocId =  createDuplicates(conn, locId, locTbl, null, null);
		
		return newLocId; 
		
		//Now for loc and geo details		
//		DBTableInfo locDetTbl = DBTableIdMgr.getInstance().getByShortClassName("LocalityDetail");
//		DBTableInfo geoCoordDetTbl = DBTableIdMgr.getInstance().getByShortClassName("GeoCoordDetail");

	}
	
	private static boolean isString(Object obj)
	{
		if (obj instanceof DBFieldInfo)
		{
			return ((DBFieldInfo )obj).getDataClass().equals((String.class));
		}
		//else it's a relationship and the value is a numeric foreign key
		return false;
	}
	
	private static boolean isCalendar(Object obj) {
		if (obj instanceof DBFieldInfo)
		{
			return ((DBFieldInfo )obj).getDataClass().equals((Calendar.class));
		}
		//else it's a relationship and the value is a numeric foreign key
		return false;		
	}
	
	private static String getCondition(Pair<Pair<DBTableInfo, Object>, Integer> fld, Object value) throws Exception
	{
		
		System.out.print("getCondition() for " + getDbObjName(fld.getFirst(), true, fld.getSecond()));
		if (fld.getFirst().getSecond() instanceof DBFieldInfo)
		{
			System.out.print(" " + ((DBFieldInfo )fld.getFirst().getSecond()).getDataClass().getSimpleName());
		}
		System.out.println(" = " + value);
		
		if (value == null)
		{
			return getDbObjName(fld.getFirst(), true, fld.getSecond()) + " is null";
		}
		if (isString(fld.getFirst().getSecond()))
		{
			return getDbObjName(fld.getFirst(), true, fld.getSecond()) + "='" + BasicSQLUtils.escapeStringLiterals((String )value) + "'";
		} 
		if (isCalendar(fld.getFirst().getSecond()))
		{
			return getDbObjName(fld.getFirst(), true, fld.getSecond()) + "=date('" + value.toString() + "')";
		} 
		
		return getDbObjName(fld.getFirst(), true, fld.getSecond()) + "=" + value.toString();
	}
	
	private static Vector<Pair<DBTableInfo, DBRelationshipInfo>> getOwnedOneOrManyRelatedTables(DBTableInfo tbl) throws Exception
	{
		if (tbl.getTableId() != Locality.getClassTableId())
		{
			//throw new Exception("getOwnedOneOrManyRelatedTables(): unsupported tbl " + tbl.getName());
			System.out.println("getOwnedOneOrManyRelatedTables(): dup removal is kind of experimental for " + tbl.getName());
		}
		Vector<Pair<DBTableInfo, DBRelationshipInfo>> result = new Vector<Pair<DBTableInfo, DBRelationshipInfo>>();
		if (tbl.getTableId() == Locality.getClassTableId()) {
			for (DBRelationshipInfo rel : tbl.getRelationships())
			{
				//XXX THis only works for localitys
				if (rel.getType().equals(DBRelationshipInfo.RelationshipType.ZeroOrOne))
				{
					result.add(new Pair<DBTableInfo, DBRelationshipInfo>(DBTableIdMgr.getInstance().getByClassName(rel.getClassName()), rel));
				}
			}
		} else if (tbl.getTableId() == CollectingEvent.getClassTableId()) {
			for (DBRelationshipInfo rel : tbl.getRelationships())
			{
				if ((rel.getType().equals(DBRelationshipInfo.RelationshipType.OneToMany) && !"collectionObjects".equals(rel.getName()))
						|| "collectingEventAttribute".equals(rel.getName()))
					
				{
					result.add(new Pair<DBTableInfo, DBRelationshipInfo>(DBTableIdMgr.getInstance().getByClassName(rel.getClassName()), rel));
				}
			}
			
		}
		return result;
	}
	
	private static Vector<Pair<DBTableInfo, DBRelationshipInfo>> getNonOwnedOneToManyRelatedTables(DBTableInfo tbl) throws Exception
	{
		if (tbl.getTableId() != Locality.getClassTableId())
		{
			//throw new Exception("getNonOwnedOneOrManyRelatedTables(): unsupported tbl " + tbl.getName());
			System.out.println("getNonOwnedOneOrManyRelatedTables(): dup removal is kind of experimental for " + tbl.getName());
		}
		Vector<Pair<DBTableInfo, DBRelationshipInfo>> result = new Vector<Pair<DBTableInfo, DBRelationshipInfo>>();
		if (tbl.getTableId() == CollectingEvent.getClassTableId()) {
			for (DBRelationshipInfo rel : tbl.getRelationships())
			{
				//Ignoring CollectingEventAttribute, Attachments, etc
				if (rel.getType().equals(DBRelationshipInfo.RelationshipType.OneToMany) && "collectionObjects".equals(rel.getName()))
				{
					result.add(new Pair<DBTableInfo, DBRelationshipInfo>(DBTableIdMgr.getInstance().getByClassName(rel.getClassName()), rel));
				}
			}
			return result;
		}
		for (DBRelationshipInfo rel : tbl.getRelationships())
		{
			//XXX THis only works for localitys
			if (rel.getType().equals(DBRelationshipInfo.RelationshipType.OneToMany))
			{
				result.add(new Pair<DBTableInfo, DBRelationshipInfo>(DBTableIdMgr.getInstance().getByClassName(rel.getClassName()), rel));
			}
		}
		//Loc -> CE relationship is not included in the loc table due probs with hibernate
		if (tbl.getTableId() == Locality.getClassTableId())
		{
			result.add(new Pair<DBTableInfo, DBRelationshipInfo>(DBTableIdMgr.getInstance().getInfoById(CollectingEvent.getClassTableId()), null));
		}
		
		for (DBTableInfo info : DBTableIdMgr.getInstance().getTables()) {
			for (DBRelationshipInfo rel : info.getRelationships()) {
				if (rel.getClassName().equals(tbl.getClassName()) && rel.getType().equals(RelationshipType.ManyToOne) && rel.getOtherSide() == null) {
					result.add(new Pair<DBTableInfo, DBRelationshipInfo>(info, rel));
				}
			}
		}
		return result;
	}
	
	/**
	 * @param conn
	 * @param tblName
	 * @param rowValues
	 * @return
	 * @throws Exception
	 */
	public static List<Object> getDuplicates(Connection conn, String tblName, Object[] rowValues) throws Exception {
		DBTableInfo tbl = DBTableIdMgr.getInstance().getByShortClassName(tblName);
		Vector<Pair<DBTableInfo, DBRelationshipInfo>> children = getOwnedOneOrManyRelatedTables(tbl);
		children.insertElementAt(new Pair<DBTableInfo, DBRelationshipInfo>(tbl, null), 0);
		List<Pair<Pair<DBTableInfo, Object>, Integer>> flds = getTblsFlds(children);
		children.remove(0);
		String sql = "select " + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName() + getJoinToOwnedChildren(tbl)
			+ " where ";
		for (int f = 0; f < flds.size(); f++)
		{
			sql += (f > 0 ? " and " : "") + getCondition(flds.get(f), rowValues[f]);
		}
		
		return BasicSQLUtils.querySingleCol(conn, sql);
	}
	
	/**
	 * @param conn
	 * @param tblName
	 * @return
	 * @throws Exception
	 */
	public static List<Pair<Pair<DBTableInfo, Object>, Integer>> getFldsForTblAndOwnedTbls(String tblName) 
			throws Exception {
		DBTableInfo tbl = DBTableIdMgr.getInstance().getByShortClassName(tblName);
		Vector<Pair<DBTableInfo, DBRelationshipInfo>> children = getOwnedOneOrManyRelatedTables(tbl);
		children.insertElementAt(new Pair<DBTableInfo, DBRelationshipInfo>(tbl, null), 0);
		return getTblsFlds(children);
	}
	/**
	 * @param conn
	 * @param tblName
	 * @param rowValues
	 * @return
	 * @throws Exception
	 */
	public static Object[] getAllFlds(Connection conn, String tblName, Integer key) throws Exception {
		List<Pair<Pair<DBTableInfo, Object>, Integer>> flds = getFldsForTblAndOwnedTbls(tblName);
		DBTableInfo tbl = DBTableIdMgr.getInstance().getByShortClassName(tblName);
		String sql = "select " + getSqlFldsClause(flds) + getJoinToOwnedChildren(tbl)
			+ " where " + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName() + "=" + key;		
		return BasicSQLUtils.queryForRow(conn, sql);
	}
	
	private static Integer removeDuplicates(Connection conn, String tblName, Object[] rowValues) throws Exception
	{
		DBTableInfo tbl = DBTableIdMgr.getInstance().getByShortClassName(tblName);
		Vector<Pair<DBTableInfo, DBRelationshipInfo>> children = getOwnedOneOrManyRelatedTables(tbl);
		children.insertElementAt(new Pair<DBTableInfo, DBRelationshipInfo>(tbl, null), 0);
		List<Pair<Pair<DBTableInfo, Object>, Integer>> flds = getTblsFlds(children);
		children.remove(0);
		String sql = "select " + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName() + getJoinToOwnedChildren(tbl)
			+ " where ";
		for (int f = 0; f < flds.size(); f++)
		{
			sql += (f > 0 ? " and " : "") + getCondition(flds.get(f), rowValues[f]);
		}
		
		Vector<Object> ids = BasicSQLUtils.querySingleCol(conn, sql);
		if (ids == null || ids.size() < 2)
		{
			//throw new Exception("No duplicates retrieved: " + sql);
			System.out.println("No duplicates retrieved: " + sql);
			return 0;
		}
		
		Integer keeperID = (Integer )ids.get(0);
		
		Vector<Pair<DBTableInfo, DBRelationshipInfo>> updates = getNonOwnedOneToManyRelatedTables(tbl);
		for (Pair<DBTableInfo, DBRelationshipInfo> update : updates)
		{
			System.out.println("Updating " + update.getFirst().getName());
			//if (update.getFirst().getName().equalsIgnoreCase("groupperson")) {
			//	System.out.println("SKIPPING " + update.getFirst().getName() + "!!!!");
			//	continue;
			//}
			String foreignKey;
			if (update.getSecond() == null && update.getFirst().getName().equals("collectingevent"))
			{
				//the ce-Loc rel is missing in hibernate
				foreignKey = "LocalityID";
			} else if (update.getSecond().getType().equals(RelationshipType.OneToMany))
			{
				DBTableInfo other = DBTableIdMgr.getInstance().getByClassName(update.getSecond().getClassName());
				DBRelationshipInfo otherSide = other.getRelationshipByName(update.getSecond().getOtherSide());
				foreignKey = otherSide.getColName() == null ? other.getPrimaryKeyName() : otherSide.getColName();
			} else if  (update.getSecond().getType().equals(RelationshipType.ManyToOne)) {
				foreignKey = update.getSecond().getColName() == null ? update.getFirst().getPrimaryKeyName() : update.getSecond().getColName();
			} else {
				//foreignKey = update.getFirst().getPrimaryKeyName();
				throw new Exception("unexpected relationship type " + update.getFirst().getTitle() + " - " + update.getSecond().getTitle());
			}
			for (int i = 1; i < ids.size(); i++)
			{
				String updateSql = "update " + update.getFirst().getName() + " set " + foreignKey + " = " + keeperID
					+ " where " + foreignKey + " = " + ids.get(i);
				BasicSQLUtils.update(conn, updateSql);
			}
		}
		for (int i = 1; i < ids.size(); i++)
		{
			
//			String delSql = "delete from localitydetail where LocalityID = " + ids.get(i); //Locality only...
//			System.out.println(delSql);
//			if (BasicSQLUtils.update(conn, delSql) != 1)
//			{
//				throw new Exception("delete failed for this statement: " + delSql);
//			}
//			delSql = "delete from locality where LocalityID = " + ids.get(i); //Locality only...
//			System.out.println(delSql);
//			if (BasicSQLUtils.update(conn, delSql) != 1)
//			{
//				throw new Exception("delete failed for this statement: " + delSql);
//			}
			
			//NEED to delete ownedTbls first!!!!!!!

			for (Pair<DBTableInfo, DBRelationshipInfo> child : children)
			{
				System.out.println("deleting " + child.getFirst().getName());
				if (!child.getFirst().getName().endsWith("attribute")) {  //careful... this works currently but if new tables are added that end with ...attribute...
					String foreignKey;
					if (child.getSecond().getType().equals(RelationshipType.OneToMany) || child.getSecond().getType().equals(RelationshipType.ZeroOrOne))
					{
						DBTableInfo other = DBTableIdMgr.getInstance().getByClassName(child.getSecond().getClassName());
						DBRelationshipInfo otherSide = other.getRelationshipByName(child.getSecond().getOtherSide());
						foreignKey = otherSide.getColName() == null ? other.getPrimaryKeyName() : otherSide.getColName();
					}  else {
						//foreignKey = update.getFirst().getPrimaryKeyName();
						throw new Exception("unexpected relationship type " + child.getFirst().getTitle() + " - " + child.getSecond().getTitle());
					}
					String delSql = "delete from " + child.getFirst().getName() + " where " + foreignKey + " = " + ids.get(i);
					System.out.println(delSql);
					BasicSQLUtils.update(conn, delSql);
				} else {
					System.out.println("Need to manually delete unused " + child.getFirst().getName() + " records.");
				}
			}
			
			String delSql = "delete from " + tbl.getName() + " where " + tbl.getPrimaryKeyName() + " = " + ids.get(i);
			System.out.println(delSql);
			if (BasicSQLUtils.update(conn, delSql) != 1)
			{
				throw new Exception("delete failed for this statement: " + delSql);
			}
			
		}
		return null;
	}
	
	protected static void removeDuplicateAgents() throws Exception {
		//String connStr = "jdbc:mysql://localhost/wis6?characterEncoding=UTF-8&autoReconnect=true"; 
		String connStr = "jdbc:mysql://localhost/tishfish?characterEncoding=UTF-8&autoReconnect=true"; 
		try
		{
			Connection conn = DriverManager.getConnection(connStr, "Master", "Master");
			Vector<Object[]> distincts = getDistinctRecs(conn, Agent.getClassTableId(), true);
			System.out.println(distincts.size() + " dups.");
			Vector<Pair<String, Long>> dups = new Vector<Pair<String, Long>>();
			for (Object[] row : distincts)
			{
				Long count = (Long )row[row.length -1 ];
				if (count > 1)
				{
					String text = row[0] + ", " + row[1] + ", " + row[2]; 
					dups.add(new Pair<String, Long>(text, count));
					removeDuplicates(conn, "agent", row);
				}
			}
			System.out.println(dups.size() + " duplicates:");
			for (Pair<String, Long> loc : dups)
			{
				System.out.println(loc.getSecond() + " --- " + loc.getFirst());
			}
			//duplicateLocality(conn, 1);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	protected static void assignGeoCoordsFromGeoCoordTexts() throws Exception {
		String connStr = "jdbc:mysql://localhost/wisherbunfixed?characterEncoding=UTF-8&autoReconnect=true"; 
		Connection conn = DriverManager.getConnection(connStr, "Master", "Master");
		String sql = "select localityid, latitude1, lat1text, longitude1, long1text, " +
				" latitude2, lat2text, longitude2, long2text, localityname from locality where " +
				" ((lat1text is not null or long1text is not null) and (latitude1 is null or longitude1 is null)) "
				+ "or ((lat2text is not null and long2text is not null) and (latitude2 is null or longitude2 is null))";
		int la1 = 1, lat1 = 2, lo1 = 3, lot1 = 4, la2 = 5, lat2 = 6, lo2 = 7, lot2 = 8;
		GeoRefConverter geoconverter = new GeoRefConverter();
		List<Object[]> recs = BasicSQLUtils.query(conn, sql);
		List<String> unfixables = new ArrayList<String>();
		System.out.println("Number of Loc Recs to correct: " + recs.size());
		for (Object[] rec : recs) {
			try {
				System.out.println("Correcting " + rec[0] + ":" + rec[la1] + "," + rec[lat1] + "," +
					rec[lo1] + "," + rec[lot1] + ","  + rec[la2] + "," + rec[lat2] + "," +
							rec[lo2] + "," + rec[lot2]);
				String dlat1 = rec[la1] == null  && rec[lat1] != null ? geoconverter.convert(rec[lat1].toString(), GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name())
					: null;
				String dlong1 = rec[lo1] == null && rec[lot1] != null ? geoconverter.convert(rec[lot1].toString(), GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name())
					: null;
				String dlat2 = rec[la2] == null && rec[lat2] != null ? geoconverter.convert(rec[lat2].toString(), GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name())
					: null;
				String dlong2 = rec[lo2] == null && rec[lot2] != null ? geoconverter.convert(rec[lot2].toString(), GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name())
					: null;
				LatLonConverter.FORMAT f = geoconverter.getLatLonFormat(rec[lot1].toString());
				if (f.equals(LatLonConverter.FORMAT.None)) {
					f = LatLonConverter.FORMAT.DDMMSS;
				}
				System.out.println("  " + dlat1 + "," + dlong1 + " - " + dlat2 + "," + dlong2);
				sql = "update locality set latitude1 = " + (dlat1 != null ? dlat1 : "null") +
						", longitude1 = " + (dlong1 != null ? dlong1 : "null") +
						", latitude2 = " + (dlat2 != null ? dlat2 : "null") +
						", longitude2 = " + (dlong2 != null ? dlong2 : "null") +
						", latlongtype =" + (dlat2 != null ? "'Line'" : "'Point'") +
						", originallatlongunit =" +  f.ordinal() +
						", srclatlongunit =" + f.ordinal() +						
						" where localityid = " + rec[0].toString();
				
				if (BasicSQLUtils.update(conn, sql) == -1) {
					throw new Exception("update statement failed.");
				}
			} catch (Exception ex) {
				unfixables.add(rec[0].toString() + ", " + rec[9].toString() + ", " + ex.getLocalizedMessage());
			}
		}
		System.out.println("unable to fix " + unfixables.size() + " records.");
		FileUtils.writeLines(new File("D:/data/wisconsin/UnfixedLocalities"), unfixables);
	}
	
	protected static void removeDuplicateCollectingEvents() throws Exception {
		//String connStr = "jdbc:mysql://localhost/wis6?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/plant_shared?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/demopal?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/flamam6?characterEncoding=UTF-8&autoReconnect=true"; 
		String connStr = "jdbc:mysql://localhost/utapostshare?characterEncoding=UTF-8&autoReconnect=true"; 
		try
		{
			Connection conn = DriverManager.getConnection(connStr, "Master", "Master");
			Vector<Object[]> distincts = getDistinctRecs(conn, CollectingEvent.getClassTableId(), true);
			System.out.println(distincts.size() + " dups.");
			Vector<Pair<String, Long>> dups = new Vector<Pair<String, Long>>();
			for (Object[] row : distincts)
			{
				Long count = (Long )row[row.length -1 ];
				if (count > 1)
				{
					String text = row[0] + ", " + row[1] + ", " + row[2]; 
					dups.add(new Pair<String, Long>(text, count));
					removeDuplicates(conn, "collectingevent", row);
				}
			}
			System.out.println(dups.size() + " duplicates:");
			for (Pair<String, Long> loc : dups)
			{
				System.out.println(loc.getSecond() + " --- " + loc.getFirst());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	protected static void removeDuplicateLocalities() throws Exception {
		//String connStr = "jdbc:mysql://localhost/wis6?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/plant_shared?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/demopal?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/flamam6?characterEncoding=UTF-8&autoReconnect=true"; 
		String connStr = "jdbc:mysql://localhost/wam?characterEncoding=UTF-8&autoReconnect=true"; 
		try
		{
			Connection conn = DriverManager.getConnection(connStr, "Master", "Master");
			Vector<Object[]> distincts = getDistinctRecs(conn, Locality.getClassTableId(), true);
			System.out.println(distincts.size() + " dups.");
			Vector<Pair<String, Long>> dups = new Vector<Pair<String, Long>>();
			for (Object[] row : distincts)
			{
				Long count = (Long )row[row.length -1 ];
				if (count > 1)
				{
					String text = row[0] + ", " + row[1] + ", " + row[2]; 
					dups.add(new Pair<String, Long>(text, count));
					removeDuplicates(conn, "locality", row);
				}
			}
			System.out.println(dups.size() + " duplicates:");
			for (Pair<String, Long> loc : dups)
			{
				System.out.println(loc.getSecond() + " --- " + loc.getFirst());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	protected static void removeDuplicatePaleoContexts(String dbName, String masterUser, String masterPw) throws Exception {
		//String connStr = "jdbc:mysql://localhost/wis6?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/plant_shared?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/demopal?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/flamam6?characterEncoding=UTF-8&autoReconnect=true"; 
		String connStr = "jdbc:mysql://localhost/" + dbName + "?characterEncoding=UTF-8&autoReconnect=true"; 
		try
		{
			Connection conn = DriverManager.getConnection(connStr, masterUser, masterPw);
			List<Object[]> distincts = getDistinctRecs(conn, PaleoContext.getClassTableId(), true);
			System.out.println(distincts.size() + " dups.");
			Vector<Pair<String, Long>> dups = new Vector<Pair<String, Long>>();
			for (Object[] row : distincts)
			{
				Long count = (Long )row[row.length -1 ];
				if (count > 1)
				{
					String text = row[0] + ", " + row[1] + ", " + row[2]; 
					dups.add(new Pair<String, Long>(text, count));
					removeDuplicates(conn, "paleocontext", row);
				}
			}
			System.out.println(dups.size() + " duplicates:");
			for (Pair<String, Long> loc : dups)
			{
				System.out.println(loc.getSecond() + " --- " + loc.getFirst());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			//removeDuplicateAgents();
			//removeDuplicateCollectingEvents();
			//assignGeoCoordsFromGeoCoordTexts();
			//removeDuplicateLocalities();
			removeDuplicatePaleoContexts("flaip04", "Master", "Master");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}

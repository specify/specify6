package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import edu.ku.brc.af.core.db.*;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Collection;

public class RecordMatchUtils {
    protected static final Logger log = Logger.getLogger(UploadTable.class);

    private static String[] systemFldNames = {"TimestampCreated", "TimestampModified", "Version", "Lat1Text", "Lat2Text",
            "Long1Text", "Long2Text", "ModifiedByAgentID", "CreatedByAgentID", "GUID", "Fullname", "NodeNumber",
            "HighestChildNodeNumber"
    };

    private static final int GET_ALL = 0;
    private static final int GET_USER = 1;
    private static final int GET_SYS = 2;

    /**
     *
     * @param column
     * @return
     */
    private static boolean isUserFld(final String column) {
        for (String systemFldName : systemFldNames) {
            if (systemFldName.equalsIgnoreCase(column)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param rel
     * @return
     */
    private static boolean isSystemRel(DBRelationshipInfo rel) {
        return rel.getClassName().equals("edu.ku.brc.specify.datamodel.Agent") && ("CreatedByAgentID".equalsIgnoreCase(rel.getColName()) || "ModifiedByAgentID".equalsIgnoreCase(rel.getColName()));
    }

    /**
     * @param getType
     * @param fld
     * @return
     */
    private static boolean isFldToGet(int getType, String fld) {
        if (getType == GET_ALL) {
            return true;
        } else if (getType == GET_USER) {
            return isUserFld(fld);
        } else {
            return getType == GET_SYS && !isUserFld(fld);
        }
    }

    /**
     * @param getType
     * @param rel
     * @return
     */
    private static boolean isRelToGet(int getType, DBRelationshipInfo rel) {
        if (getType == GET_ALL) {
            return true;
        } else if (getType == GET_USER) {
            return !isSystemRel(rel);
        } else if (getType == GET_SYS) {
            return isSystemRel(rel);
        } else {
            return false;
        }
    }

    /**
     *
     * @param tbl
     * @return
     * @throws Exception
     */
    private static List<Pair<DBTableInfo, DBRelationshipInfo>> getOwnedOneOrManyRelatedTables(DBTableInfo tbl) throws Exception {
        if (tbl.getTableId() != Locality.getClassTableId()) {
            //throw new Exception("getOwnedOneOrManyRelatedTables(): unsupported tbl " + tbl.getName());
            //System.out.println("getOwnedOneOrManyRelatedTables(): dup removal is kind of experimental for " + tbl.getName());
        }
        List<Pair<DBTableInfo, DBRelationshipInfo>> result = new ArrayList<>();
        if (tbl.getTableId() == Locality.getClassTableId()) {
            for (DBRelationshipInfo rel : tbl.getRelationships()) {
                //XXX THis only works for localitys
                if (rel.getType().equals(DBRelationshipInfo.RelationshipType.ZeroOrOne)) {
                    result.add(new Pair<>(DBTableIdMgr.getInstance().getByClassName(rel.getClassName()), rel));
                }
            }
        } else if (tbl.getTableId() == CollectingEvent.getClassTableId()) {
            for (DBRelationshipInfo rel : tbl.getRelationships()) {
                if ((rel.getType().equals(DBRelationshipInfo.RelationshipType.OneToMany) && !"collectionObjects".equals(rel.getName()))
                        || "collectingEventAttribute".equals(rel.getName())) {
                    result.add(new Pair<>(DBTableIdMgr.getInstance().getByClassName(rel.getClassName()), rel));
                }
            }

        }
        return result;
    }

    /**
     *
     * @param tbls
     * @return
     */
    private static List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> getTblsFlds(List<Pair<DBTableInfo, DBRelationshipInfo>> tbls) {
        List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> result = new ArrayList<>();
        for (Pair<DBTableInfo, DBRelationshipInfo> tbl : tbls) {
            result.addAll(getFldsForTbl(tbl, false));
        }
        return result;
    }

    /**
     *
     * @param tbls
     * @return
     */
    private static List<Pair<DBRelationshipInfo, List<DBInfoBase>>>
    getTblsFldsForMatching(List<Pair<DBTableInfo, DBRelationshipInfo>> tbls) {
        List<Pair<DBRelationshipInfo, List<DBInfoBase>>> result = new ArrayList<>();
        for (Pair<DBTableInfo, DBRelationshipInfo> tbl : tbls) {
            List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> ughs = getFldsForTbl(tbl, true);
            List<DBInfoBase> ughughs = new ArrayList<>();
            for (Pair<Pair<DBTableInfo, DBInfoBase>, Integer> ugh : ughs) {
                ughughs.add(ugh.getFirst().getSecond());
            }
            result.add(new Pair<>(tbl.getSecond(), ughughs));
        }
        return result;
    }

    /**
     *
     * @param info
     * @return
     */
    private static List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> getFldsForTbl(Pair<DBTableInfo, DBRelationshipInfo> info, boolean forMatching) {
        return getFldsForTbl(info, GET_USER, forMatching);
    }

    /**
     *
     * @param tbl
     * @return
     */
    private static int getRepsForTbl(DBTableInfo tbl) {
        if ("collector".equalsIgnoreCase(tbl.getName())) {
            return 4;
        } else {
            return 1;
        }
    }

    /**
     * @param info
     * @param getType
     * @return
     */
    private static List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> getFldsForTbl(Pair<DBTableInfo, DBRelationshipInfo> info, int getType, boolean forMatching) {
        List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> result = new ArrayList<>();
        DBTableInfo tbl = info.getFirst();
        DBRelationshipInfo parRel = info.getSecond();

        int maxR =  forMatching ? 1 : getRepsForTbl(tbl);
        for (int r = 0; r < maxR; r++) {
            for (DBFieldInfo fi : tbl.getFields()) {
                //System.out.println("getFldsForTable: checking " + tbl.getName() + "." + fi.getColumn());
                if (isFldToGet(getType, fi.getColumn()))	{
                    result.add(new Pair<>(new Pair<>(tbl, fi), r));
                }
            }
            for (DBRelationshipInfo rel : tbl.getRelationships()) {
                if ((parRel == null || !(parRel.getOtherSide().equals(rel.getName()) && rel.getOtherSide().equals(parRel.getName())))
                        && isRelToGet(getType, rel)
                        && StringUtils.isNotBlank(rel.getColName())) {
                    //System.out.println("getFldsForTable: checking " + tbl.getName() + "." + rel.getColName());
                    if (rel.getType().ordinal() == DBRelationshipInfo.RelationshipType.ManyToOne.ordinal() && !rel.getDataClass().getSimpleName().endsWith("Attribute")) {
                        result.add(new Pair<>(new Pair<>(tbl, rel), r));
                    }
                }
            }
        }
        return result;
    }

    /**
     *
     * @param dbObj
     * @param includeTblAbbrev
     * @param seq
     * @return
     * @throws Exception
     */
    private static String getDbObjName(Pair<DBTableInfo, DBInfoBase> dbObj, boolean includeTblAbbrev, Integer seq) throws Exception {
        String result;
        if (dbObj.getSecond() instanceof DBFieldInfo) {
            result = (includeTblAbbrev ? dbObj.getFirst().getAbbrev() + (seq == null ? "" : seq) + "." : "") + ((DBFieldInfo)dbObj.getSecond()).getColumn();
        } else if (dbObj.getSecond() instanceof DBRelationshipInfo) {
            result =  (includeTblAbbrev ? (dbObj.getFirst().getAbbrev() + (seq == null ? "" : seq) + ".") : "") +
                    (((DBRelationshipInfo)dbObj.getSecond()).getColName() != null
                            ? ((DBRelationshipInfo)dbObj.getSecond()).getColName()
                            : dbObj.getFirst().getIdColumnName());
        } else {
            throw new Exception("Unknown db object type " + dbObj.getClass().getName());
        }
        return result;
    }

    /**
     *
     * @param dbObjs
     * @return
     * @throws Exception
     */
    private static String getSqlFldsClause(List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> dbObjs) throws Exception {
        String result = "";
        boolean comma = false;
        for (Pair<Pair<DBTableInfo, DBInfoBase>, Integer> dbObj : dbObjs) {
            if (comma) {
                result += ", ";
            } else {
                comma = true;
            }
            result += getDbObjName(dbObj.getFirst(), true, dbObj.getSecond());
        }
        return result;
    }

    /**
     *
     * @param info
     * @param seq
     * @return
     * @throws Exception
     */
    private static String getAddlJoinCriteria(DBTableInfo info, int seq) throws Exception {
        if ("collector".equalsIgnoreCase(info.getName())) {
            return " and " + info.getAbbrev() + seq + ".OrderNumber=" + seq;
        } else {
            return "";
        }
    }

    /**
     *
     * @param tbl
     * @param countOnly
     * @param dupsOnly
     * @param scopeID
     * @return
     * @throws Exception
     */
    protected static String getDistinctRecSql(DBTableInfo tbl, boolean countOnly, boolean dupsOnly,
                                              Integer scopeID) throws Exception {
        String result = "select " + (countOnly ? "count(distinct " : "");

        List<Pair<DBTableInfo, DBRelationshipInfo>> tbls = getOwnedOneOrManyRelatedTables(tbl);
        tbls.add(0, new Pair<>(tbl, null));
        List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> flds = getTblsFlds(tbls);
        result += getSqlFldsClause(flds);
        if (countOnly) {
            result += ")";
        } else {
            result += ", count( " + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName() + ")";
        }

        result += getJoinToOwnedChildren(tbl);
        if (CollectionMember.class.isAssignableFrom(tbl.getClassObj())) {
            if (scopeID != null) {
                result += " where " + tbl.getAbbrev() + "0.CollectionMemberID=" + scopeID;
            }
        }
        if (DisciplineMember.class.isAssignableFrom(tbl.getClassObj())) {
            if (scopeID != null) {
                result += " where " + tbl.getAbbrev() + "0.DisciplineID=" + scopeID;
            }
        }
        if (!countOnly) {
            result += " group by ";
            int maxF = flds.size();
            for (int f = 1; f <= maxF; f++) {
                if (f > 1) {
                    result += ", ";
                }
                result += f;
            }
        }
        if (dupsOnly) {
            result += " having count(" + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName() + ") > 1";
        }
        return result;
    }

    /**
     *
     * @param tbl
     * @return
     * @throws Exception
     */
    private static String getJoinToOwnedChildren(DBTableInfo tbl) throws Exception {
        List<Pair<DBTableInfo, DBRelationshipInfo>> tbls = getOwnedOneOrManyRelatedTables(tbl);
        String result = " from " + tbl.getName() + " " + tbl.getAbbrev() + "0";
        for (Pair<DBTableInfo, DBRelationshipInfo> owned : tbls) {
            DBTableInfo info = owned.getFirst();
            DBRelationshipInfo rel = owned.getSecond();
            DBTableInfo other = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
            DBRelationshipInfo otherSide = other.getRelationshipByName(rel.getOtherSide());
            String colName = otherSide.getColName() == null ? other.getPrimaryKeyName() : otherSide.getColName();
            int maxR = getRepsForTbl(owned.getFirst());
            for (int r = 0; r < maxR; r++) {
                result += " left join " + info.getName() + " " + info.getAbbrev() + r + " on "
                        + info.getAbbrev() + r + "." + colName + " = " + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName()
                        + getAddlJoinCriteria(info, r);
            }
        }
        return result;
    }

    /**
     *
     * @param tbl
     * @param tbls
     * @return
     * @throws Exception
     */
    private static String getJoinToOwnedChildrenForMatching(final DBTableInfo tbl, final List<Pair<DBRelationshipInfo, Integer>> tbls) throws Exception {
        String result = " from " + tbl.getName() + " " + tbl.getAbbrev() + "0";
        for (Pair<DBRelationshipInfo, Integer> owned : tbls) {
            if (owned.getSecond() > 0) {
                DBRelationshipInfo rel = owned.getFirst();
                if (rel != null) {
                    DBTableInfo info = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
                    DBRelationshipInfo otherSide = info.getRelationshipByName(rel.getOtherSide());
                    String joinToColName = otherSide.getColName() == null ? info.getPrimaryKeyName() : otherSide.getColName();
                    String joinFromColName = rel.getColName() == null ? tbl.getIdColumnName() : rel.getColName();
                    for (int r = 0; r < owned.getSecond(); r++) {
                        result += " left join " + info.getName() + " " + info.getAbbrev() + r + " on "
                                + info.getAbbrev() + r + "." + joinToColName + " = " + tbl.getAbbrev() + "0." + joinFromColName
                                + getAddlJoinCriteria(info, r);
                    }
                }
            }
        }
        return result;
    }
    /**
     *
     * @param rec
     * @param overrides
     * @return
     * @throws Exception
     */
    public static String getMatchingSql(final DataModelObjBase rec,
                                           final Map<DBInfoBase, Object> overrides) throws Exception {
        if (rec == null) {
            return null;
        }
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(rec.getClass().getName());
        List<Pair<DBTableInfo, DBRelationshipInfo>> tbls = getOwnedOneOrManyRelatedTables(tblInfo);
        tbls.add(0, new Pair<>(tblInfo, null));
        List<Pair<DBRelationshipInfo, List<DBInfoBase>>> flds = getTblsFldsForMatching(tbls);
        StringBuilder condStr = new StringBuilder();
        Pair<List<Pair<DBRelationshipInfo, Integer>>, List<String>> joinsAndConds = getSqlConditions(tblInfo, rec, flds, overrides);
        for (String condition : joinsAndConds.getSecond()) {
            if (condStr.length() > 0) condStr.append(" AND ");
            condStr.append(condition);
        }
        return "select " + tblInfo.getAbbrev() + "0." + tblInfo.getPrimaryKeyName()
                +  getJoinToOwnedChildrenForMatching(tblInfo, joinsAndConds.getFirst())
                + " where " + condStr;
    }

    /**
     *
     * @param baseTbl
     * @param rec
     * @param flds
     * @param overrides
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static Pair<List<Pair<DBRelationshipInfo, Integer>>, List<String>> getSqlConditions(
            final DBTableInfo baseTbl,
            final DataModelObjBase rec,
            final List<Pair<DBRelationshipInfo, List<DBInfoBase>>> flds,
            final Map<DBInfoBase, Object> overrides)
            throws InvocationTargetException, IllegalAccessException {
        List<Pair<DBRelationshipInfo, Integer>> joinInfo = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        for (Pair<DBRelationshipInfo, List<DBInfoBase>> tblFlds : flds) {
            Pair<Integer, List<String>> subResult =
                    getSqlConditionsForTbl(baseTbl, getRecsForCondition(rec, tblFlds.getFirst()), tblFlds, overrides);
            joinInfo.add(new Pair<>(tblFlds.getFirst(), subResult.getFirst()));
            conditions.addAll(subResult.getSecond());
        }
        return new Pair<>(joinInfo, conditions);
    }

    /**
     *
     * @param rec
     * @param rel
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static List<DataModelObjBase> getRecsForCondition(final DataModelObjBase rec, final DBRelationshipInfo rel)
            throws InvocationTargetException, IllegalAccessException {
        List<DataModelObjBase> result = new ArrayList<>();
        if (rel == null) {
            result.add(rec);
        } else {
            Method getter = getFldGetter(rel, DBTableIdMgr.getInstance().getByClassName(rec.getDataClass().getName()));
            if (getter != null) {
                Object invoked = getter.invoke(rec);
                if (invoked instanceof java.util.Collection) {
                    if (Comparable.class.isAssignableFrom(rel.getDataClass())) {
                        List tmp = new ArrayList((Collection)invoked);
                        Collections.sort(tmp);
                        result.addAll(tmp);
                    } else {
                        result.addAll((java.util.Collection) invoked);
                    }
                } else {
                    result.add((DataModelObjBase)invoked);
                }
            }
        }
        return result;
    }

    /**
     *
     * @param baseTbl
     * @param recs
     * @param tblFlds
     * @param overrides
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static Pair<Integer, List<String>> getSqlConditionsForTbl(final DBTableInfo baseTbl, final List<DataModelObjBase> recs,
                                                                        final Pair<DBRelationshipInfo, List<DBInfoBase>> tblFlds,
                                                                        final Map<DBInfoBase, Object> overrides)
            throws InvocationTargetException, IllegalAccessException {
        List<String> conditions = new ArrayList<>();
        Integer relatedCount = 0;
        DBRelationshipInfo rel = tblFlds.getFirst();
        DBTableInfo tbl = rel == null ? baseTbl : DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
        if (recs.size() > 0) {
            if (recs.size() == 1 && recs.get(0) == null) {
                conditions.add(baseTbl.getAbbrev() + "0." + rel.getColName() + " is null");
            } else {
                relatedCount = recs.size();
                List<DBInfoBase> flds = tblFlds.getSecond();
                int seq = 0;
                for (DataModelObjBase rec : recs) {
                    for (DBInfoBase obj : flds) {
                        boolean doOverride = overrides.containsKey(obj);
                        Object val = doOverride  ? overrides.get(obj) : getValueForFld(obj, rec, tbl);
                        conditions.add(getSQLCondition(obj, val, tbl, seq));
                    }
                    seq++;
                }
            }
        } else {
            //XXX add something requiring no related recs
            relatedCount = 1; //to force a join to the related table.
            conditions.add(tbl.getAbbrev() + "0." + tbl.getIdColumnName() + " is null");
        }
        return new Pair<>(relatedCount, conditions);
    }

    /**
     *
     * @param fld
     * @param val
     * @param tbl
     * @param seq
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static String getSQLCondition(final DBInfoBase fld, final Object val, final DBTableInfo tbl, final Integer seq)
            throws InvocationTargetException, IllegalAccessException {
        return getSQLForVal(fld, val, tbl, seq);
    }

    /**
     *
     * @param fld
     * @param val
     * @param tbl
     * @param seq
     * @return
     */
    private static String getSQLForVal(final DBInfoBase fld, final Object val, final DBTableInfo tbl, final Integer seq) {
        String name = fld instanceof DBFieldInfo ? ((DBFieldInfo)fld).getColumn() : ((DBRelationshipInfo)fld).getColName();
        Object valStr = val instanceof DataModelObjBase ? ((DataModelObjBase)val).getId() : BasicSQLUtils.getStrValue(val);
        return tbl.getAbbrev() + seq + "." + name + (val == null ? " is null" : " = " + valStr);
    }

    /**
     *
     * @param fld
     * @param rec
     * @param tbl
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static Object getValueForFld(final DBInfoBase fld, final DataModelObjBase rec, final DBTableInfo tbl)
            throws InvocationTargetException, IllegalAccessException {
        Method getter = getFldGetter(fld, tbl);
        if (getter != null) {
            return getter.invoke(rec);
        } else {
            return null;
        }
    }

    /**
     * snatched from UploadTable
     *
     * @param i
     * @return
     */
    private static Class<?> getFieldClass(DBInfoBase i) {
        if (i == null) { return Integer.class; }
        if (i instanceof DBRelationshipInfo) {
         return ((DBRelationshipInfo)i).getDataClass();
        } else {
            DBFieldInfo fi = (DBFieldInfo)i;
            String type = fi.getType();
            if (StringUtils.isNotEmpty(type)) {
                if (type.equals("calendar_date")) {
                    return Calendar.class;
                } else if (type.equals("text")) {
                    return String.class;
                } else if (type.equals("boolean")) {
                    return Boolean.class;
                } else if (type.equals("short")) {
                    return Short.class;
                } else if (type.equals("byte")) {
                    return Byte.class;
                } else {
                    try {
                        return Class.forName(type);
                    } catch (Exception e) {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UploadTable.class, e);
                        log.error(e);
                    }
                }
            }
        }
        throw new RuntimeException("Could not find [" + i.getName() + "]");
    }

    /**
     * snatched from UploadTable
     *
     * @param fld
     * @return the setter for fld, if it exists.
     */
    protected static Method getFldGetter(final DBInfoBase fld, final DBTableInfo tbl) {
        Class<?> tblClass = tbl.getClassObj();
        String methName = "get" + UploadTable.capitalize(fld.getName());
        try {
            return tblClass.getMethod(methName);
        } catch (NoSuchMethodException nsmEx) {
            // this should only happen for many-to-many relationships, in which cases the
            // field
            // actually gets handled via the parentSetters
            return null;
        }
    }


    /**
     *
     * @param tbl
     * @param rec
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static HashMap<DBInfoBase, Object> getOverridesFromRecVals(final DBTableInfo tbl, final DataModelObjBase rec)
            throws InvocationTargetException, IllegalAccessException {
        HashMap<DBInfoBase, Object> result = new HashMap<>();
        for (DBFieldInfo fld : tbl.getFields()) {
            if (isFldToGet(GET_USER, fld.getColumn())) {
                result.put(fld, getValueForFld(fld, rec, tbl));
            }
        }
        for (DBRelationshipInfo rel : tbl.getRelationships()) {
            if (isRelToGet(GET_USER, rel)) {
                result.put(rel, getValueForFld(rel, rec, tbl));
            }
        }
        return result;
    }

    /**
     *
     * @param tbl
     * @param reportDuplicates
     * @param createOverrides
     * @return
     */
    public static boolean testRecMatchingForTable(DBTableInfo tbl, boolean reportDuplicates, boolean createOverrides) {
        List<Integer> recIds = BasicSQLUtils.queryForInts("select " + tbl.getPrimaryKeyName() + " from " + tbl.getName());
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        boolean result = true;
        try {
            for (Integer recId : recIds) {
                DataModelObjBase rec = (DataModelObjBase)session.get(tbl.getClassObj(), recId);
                if (rec != null) {
                    String matchSql = RecordMatchUtils.getMatchingSql(rec, createOverrides ? getOverridesFromRecVals(tbl, rec) : new HashMap<>());
                    List<Integer> matches = BasicSQLUtils.queryForInts(matchSql);
                    if (!(matches.size() == 1 && matches.get(0).equals(recId))) {
                        if (matches.indexOf(recId) == -1) {
                            result = false;
                            System.out.println("no match for " + recId + ": " + matches.toString());
                        } else if (reportDuplicates) {
                            result = false;
                            System.out.println("duplicates for " + recId + ": " + matches.toString());
                        }
                    }
                } else {
                    System.out.println("null record for " + recId + ". What the hell?");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

}


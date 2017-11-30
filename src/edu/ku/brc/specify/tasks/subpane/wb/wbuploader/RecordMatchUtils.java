package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import edu.ku.brc.af.core.db.*;
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

    protected static String[] systemFldNames = {"TimestampCreated", "TimestampModified", "Version", "Lat1Text", "Lat2Text",
            "Long1Text", "Long2Text", "ModifiedByAgentID", "CreatedByAgentID", "GUID"
    };

    public static final int GET_ALL = 0;
    public static final int GET_USER = 1;
    public static final int GET_SYS = 2;

    /**
     *
     * @param column
     * @return
     */
    protected static boolean isUserFld(final String column) {
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
    protected static boolean isSystemRel(DBRelationshipInfo rel) {
        if (rel.getClassName().equals("edu.ku.brc.specify.datamodel.Agent")) {
            return "CreatedByAgentID".equalsIgnoreCase(rel.getColName()) || "ModifiedByAgentID".equalsIgnoreCase(rel.getColName());
        } else {
            return false;
        }
    }

    /**
     * @param getType
     * @param fld
     * @return
     */
    protected static boolean isFldToGet(int getType, String fld) {
        if (getType == GET_ALL) {
            return true;
        } else if (getType == GET_USER) {
            return isUserFld(fld);
        } else if (getType == GET_SYS) {
            return !isUserFld(fld);
        } else {
            return false;
        }
    }

    /**
     * @param getType
     * @param rel
     * @return
     */
    protected static boolean isRelToGet(int getType, DBRelationshipInfo rel) {
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
            System.out.println("getOwnedOneOrManyRelatedTables(): dup removal is kind of experimental for " + tbl.getName());
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
    protected static List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> getTblsFlds(List<Pair<DBTableInfo, DBRelationshipInfo>> tbls) {
        List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> result = new ArrayList<>();
        for (Pair<DBTableInfo, DBRelationshipInfo> tbl : tbls) {
            result.addAll(getFldsForTbl(tbl, false));
        }
        return result;
    }

    protected static List<Pair<Pair<DBTableInfo, DBRelationshipInfo>, List<DBInfoBase>>>
    getTblsFldsForMatching(List<Pair<DBTableInfo, DBRelationshipInfo>> tbls) {
        List<Pair<Pair<DBTableInfo, DBRelationshipInfo>, List<DBInfoBase>>> result = new ArrayList<>();
        for (Pair<DBTableInfo, DBRelationshipInfo> tbl : tbls) {
            List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> ughs = getFldsForTbl(tbl, true);
            List<DBInfoBase> ughughs = new ArrayList<>();
            for (Pair<Pair<DBTableInfo, DBInfoBase>, Integer> ugh : ughs) {
                ughughs.add(ugh.getFirst().getSecond());
            }
            result.add(new Pair<>(tbl, ughughs));
        }
        return result;
    }

    /**
     *
     * @param info
     * @return
     */
    protected static List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> getFldsForTbl(Pair<DBTableInfo, DBRelationshipInfo> info, boolean forMatching) {
        return getFldsForTbl(info, GET_USER, forMatching);
    }

    /**
     *
     * @param tbl
     * @return
     */
    protected static int getRepsForTbl(DBTableInfo tbl) {
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
    protected static List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> getFldsForTbl(Pair<DBTableInfo, DBRelationshipInfo> info, int getType, boolean forMatching) {
        List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> result = new ArrayList<>();
        DBTableInfo tbl = info.getFirst();
        DBRelationshipInfo parRel = info.getSecond();

        int maxR =  forMatching ? 1 : getRepsForTbl(tbl);
        for (int r = 0; r < maxR; r++) {
            for (DBFieldInfo fi : tbl.getFields()) {
                System.out.println("getFldsForTable: checking " + tbl.getName() + "." + fi.getColumn());
                if (isFldToGet(getType, fi.getColumn()))	{
                    result.add(new Pair<>(new Pair<>(tbl, fi), r));
                }
            }
            for (DBRelationshipInfo rel : tbl.getRelationships()) {
                if ((parRel == null || !(parRel.getOtherSide().equals(rel.getName()) && rel.getOtherSide().equals(parRel.getName())))
                        && isRelToGet(getType, rel)
                        && StringUtils.isNotBlank(rel.getColName())) {
                    System.out.println("getFldsForTable: checking " + tbl.getName() + "." + rel.getColName());
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
    protected static String getDbObjName(Pair<DBTableInfo, DBInfoBase> dbObj, boolean includeTblAbbrev, Integer seq) throws Exception {
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
    protected static String getSqlFldsClause(List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> dbObjs) throws Exception {
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

    protected static String getJoinToOwnedChildrenForMatching(final DBTableInfo tbl, final List<Pair<Pair<DBTableInfo, DBRelationshipInfo>, Integer>> tbls) throws Exception {
        String result = " from " + tbl.getName() + " " + tbl.getAbbrev() + "0";
        for (Pair<Pair<DBTableInfo, DBRelationshipInfo>, Integer> owned : tbls) {
            if (owned.getSecond() > 0) {
                DBTableInfo info = owned.getFirst().getFirst();
                DBRelationshipInfo rel = owned.getFirst().getSecond();
                if (rel != null) {
                    DBTableInfo other = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
                    DBRelationshipInfo otherSide = other.getRelationshipByName(rel.getOtherSide());
                    String colName = otherSide.getColName() == null ? other.getPrimaryKeyName() : otherSide.getColName();
                    for (int r = 0; r < owned.getSecond(); r++) {
                        result += " left join " + info.getName() + " " + info.getAbbrev() + r + " on "
                                + info.getAbbrev() + r + "." + colName + " = " + tbl.getAbbrev() + "0." + tbl.getPrimaryKeyName()
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
    protected static String getMatchingSql(final DataModelObjBase rec,
                                           final Map<DBInfoBase, Object> overrides) throws Exception {
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(rec.getClass().getName());
        List<Pair<DBTableInfo, DBRelationshipInfo>> tbls = getOwnedOneOrManyRelatedTables(tblInfo);
        tbls.add(0, new Pair<>(tblInfo, null));
        List<Pair<Pair<DBTableInfo, DBRelationshipInfo>, List<DBInfoBase>>> flds = getTblsFldsForMatching(tbls);
        StringBuilder condStr = new StringBuilder();
        Pair<List<Pair<Pair<DBTableInfo, DBRelationshipInfo>, Integer>>, List<String>> joinsAndConds = getSqlConditions(rec, flds, overrides);
        for (String condition : joinsAndConds.getSecond()) {
            if (condStr.length() > 0) condStr.append(" AND ");
            condStr.append(condition);
        }
        return "select " + tblInfo.getAbbrev() + "0." + tblInfo.getPrimaryKeyName()
                +  getJoinToOwnedChildrenForMatching(tblInfo, joinsAndConds.getFirst())
                + " where " + condStr;
    }

    protected static Pair<List<Pair<Pair<DBTableInfo, DBRelationshipInfo>, Integer>>, List<String>> getSqlConditions(final DataModelObjBase rec,
                                                   final List<Pair<Pair<DBTableInfo, DBRelationshipInfo>, List<DBInfoBase>>> flds,
                                                   final Map<DBInfoBase, Object> overrides)
            throws InvocationTargetException, IllegalAccessException {
        List<Pair<Pair<DBTableInfo, DBRelationshipInfo>, Integer>> joinInfo = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        for (Pair<Pair<DBTableInfo, DBRelationshipInfo>, List<DBInfoBase>> tblFlds : flds) {
            Pair<Integer, List<String>> subResult =
                    getSqlConditionsForTbl(getRecsForCondition(rec, tblFlds.getFirst()), tblFlds, overrides);
            joinInfo.add(new Pair<>(tblFlds.getFirst(), subResult.getFirst()));
            conditions.addAll(subResult.getSecond());
        }
        return new Pair<>(joinInfo, conditions);
    }

    protected static List<DataModelObjBase> getRecsForCondition(final DataModelObjBase rec, final Pair<DBTableInfo, DBRelationshipInfo> rel)
            throws InvocationTargetException, IllegalAccessException {
        List<DataModelObjBase> result = new ArrayList<>();
        if (rel.getSecond() == null) {
            result.add(rec);
        } else {
            Method getter = getFldGetter(rel.getSecond(), DBTableIdMgr.getInstance().getByClassName(rec.getDataClass().getName()));
            if (getter != null) {
                Object invoked = getter.invoke(rec);
                if (invoked instanceof java.util.Collection) {
                    if (Comparable.class.isAssignableFrom(rel.getSecond().getDataClass())) {
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

    protected static Pair<Integer, List<String>> getSqlConditionsForTbl(final List<DataModelObjBase> recs,
                                                                        final Pair<Pair<DBTableInfo, DBRelationshipInfo>, List<DBInfoBase>> tblFlds,
                                                                        final Map<DBInfoBase, Object> overrides)
            throws InvocationTargetException, IllegalAccessException {
        List<String> conditions = new ArrayList<>();
        Integer relatedCount = 0;
        DBRelationshipInfo rel = tblFlds.getFirst().getSecond();
        DBTableInfo tbl = tblFlds.getFirst().getFirst();
        if (recs.size() > 0) {
            if (recs.size() == 1 && recs.get(0) == null) {
                conditions.add(tbl.getAbbrev() + "0." + rel.getColName() + " is null");
            } else {
                relatedCount = recs.size();
                List<DBInfoBase> flds = tblFlds.getSecond();
                int seq = 0;
                for (DataModelObjBase rec : recs) {
                    for (DBInfoBase obj : flds) {
                        Object override = overrides.get(obj);
                        Object val = override != null ? override : getValueForFld(obj, rec, tbl);
                        conditions.add(getSQLCondition(obj, val, tbl, seq));
                    }
                    seq++;
                }
            }
        } else {
            //XXX add something requiring no related recs
            relatedCount = 1;
            DBTableInfo relTbl = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
            conditions.add(relTbl.getAbbrev() + "0." + relTbl.getIdColumnName() + " is null");
        }
        return new Pair<>(relatedCount, conditions);
    }

    protected static String getSQLCondition(final DBInfoBase fld, final Object val, final DBTableInfo tbl, final Integer seq)
            throws InvocationTargetException, IllegalAccessException {
        return getSQLForVal(fld, val, tbl, seq);
    }

    protected static String getSQLForVal(final DBInfoBase fld, final Object val, final DBTableInfo tbl, final Integer seq) {
        String name = fld instanceof DBFieldInfo ? ((DBFieldInfo)fld).getColumn() : ((DBRelationshipInfo)fld).getColName();
        Object valStr = val instanceof DataModelObjBase ? ((DataModelObjBase)val).getId() : BasicSQLUtils.getStrValue(val);
        return tbl.getAbbrev() + seq + "." + name + (val == null ? " is null" : " = " + valStr);
    }

    protected static Object getValueForFld(final DBInfoBase fld, final DataModelObjBase rec, final DBTableInfo tbl)
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

}


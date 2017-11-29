package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import edu.ku.brc.af.core.db.*;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
            result.addAll(getFldsForTbl(tbl));
        }
        return result;
    }

    /**
     *
     * @param info
     * @return
     */
    protected static List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> getFldsForTbl(Pair<DBTableInfo, DBRelationshipInfo> info) {
        return getFldsForTbl(info, GET_USER);
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
    protected static List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> getFldsForTbl(Pair<DBTableInfo, DBRelationshipInfo> info, int getType) {
        List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> result = new ArrayList<>();
        DBTableInfo tbl = info.getFirst();
        DBRelationshipInfo parRel = info.getSecond();

        int maxR =  getRepsForTbl(tbl);
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
     * @param rec
     * @param overrides
     * @return
     * @throws Exception
     */
    protected static String getMatchingSql(final DataModelObjBase rec, final List<Pair<DBFieldInfo, Object>> overrides) throws Exception {
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(rec.getClass().getName());
        List<Pair<DBTableInfo, DBRelationshipInfo>> tbls = getOwnedOneOrManyRelatedTables(tblInfo);
        tbls.add(0, new Pair<>(tblInfo, null));
        List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> flds = getTblsFlds(tbls);

        String result = "select " + tblInfo.getPrimaryKeyName() + " from " + tblInfo.getName().toLowerCase() + " where ";

        return result;
    }

    protected static List<Pair<DBInfoBase, String>> getSqlConditions(final DataModelObjBase rec, final List<Pair<Pair<DBTableInfo, DBInfoBase>, Integer>> flds)
            throws InvocationTargetException, IllegalAccessException {
        List<Pair<DBInfoBase, String>> result = new ArrayList<>();
        for (Pair<Pair<DBTableInfo, DBInfoBase>, Integer> fld : flds) {
            DBTableInfo tbl = fld.getFirst().getFirst();
            DBInfoBase obj = fld.getFirst().getSecond();
            Integer seq = fld.getSecond();
            result.add(new Pair<DBInfoBase, String>(obj, getSQLCondition(obj, rec, tbl, seq)));
        }
        return result;
    }

    protected static String getSQLCondition(final DBInfoBase fld, final DataModelObjBase rec, final DBTableInfo tbl, final Integer seq)
            throws InvocationTargetException, IllegalAccessException {
        return getSQLForVal(fld, getValueForFld(fld, rec, tbl), tbl, seq);
    }

    protected static String getSQLForVal(final DBInfoBase fld, final Object val, final DBTableInfo tbl, final Integer seq) {
        String name = fld instanceof DBFieldInfo ? ((DBFieldInfo)fld).getColumn() : ((DBRelationshipInfo)fld).getColName();
        return tbl.getAbbrev() + seq + "." + name + (val == null ? " is null" : " = " + BasicSQLUtils.getStrValue(val));
    }

    protected static Object getValueForFld(final DBInfoBase fld, final DataModelObjBase rec, final DBTableInfo tbl)
            throws InvocationTargetException, IllegalAccessException {
        Method getter = getFldGetter(fld, tbl);
        return getter.invoke(rec);
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
        Class<?> fldClass = getFieldClass(fld);
        Class<?> parTypes[] = new Class<?>[1];
        parTypes[0] = fldClass;
        Class<?> tblClass = tbl.getClassObj();
        String methName = "get" + UploadTable.capitalize(fld.getName());
        try {
            return tblClass.getMethod(methName, parTypes);
        } catch (NoSuchMethodException nsmEx) {
            // this should only happen for many-to-many relationships, in which cases the
            // field
            // actually gets handled via the parentSetters
            return null;
        }
    }

}


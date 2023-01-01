/* Copyright (C) 2023, Specify Collections Consortium
 *
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package edu.ku.brc.specify.tasks.subpane.qb;


import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.ArrayList;

public class ERTICaptionInfoAuditVal extends ERTICaptionInfoRecId {
    protected static final Logger log = Logger.getLogger(ERTICaptionInfoAuditVal.class);

    public ERTICaptionInfoAuditVal(String colName, String lbl, String stringId, DBFieldInfo fi) {
        super(colName, lbl, stringId, fi);
        for (DBRelationshipInfo.RelationshipType t : auditableRelTypes) {
            auditableRelTypeList.add(t);
        }
    }

    private static DBRelationshipInfo.RelationshipType[] auditableRelTypes = {DBRelationshipInfo.RelationshipType.ManyToOne,
        DBRelationshipInfo.RelationshipType.ZeroOrOne, DBRelationshipInfo.RelationshipType.OneToOne};
    private List<DBRelationshipInfo.RelationshipType> auditableRelTypeList = new ArrayList<>();

    private boolean isAuditableRel(DBRelationshipInfo relInfo) {
        boolean result = false;
        if (relInfo != null) {
            result = auditableRelTypeList.indexOf(relInfo.getType()) != -1;
        }
        return result;
    }

    @Override
    protected Object[] getLookupInfo(Object value) {
        if (value instanceof Object[] && ((Object[])value).length == 3) {
            Object[] result;
            Object val = ((Object[]) value)[0];
            if (val == null) {
                return null;
            }
            Short tableNum = (Short) ((Object[]) value)[1];
            if (tableNum == null) {
                return null;
            }

            String fldName = (String) ((Object[]) value)[2];
            if (fldName == null) {
                return null;
            }

            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById((int) tableNum);
            if (ti == null) {
                log.warn("No tableInfo found for " + tableNum);
                return null;
            }

            DBRelationshipInfo relInfo = ti.getRelationshipByName(fldName);

            if (!isAuditableRel(relInfo)) {
                result = new Object[1];
                result[0] = val;
                return result;
            }
            DBTableInfo relTblInfo = DBTableIdMgr.getInstance().getByClassName(relInfo.getDataClass().getName());
            if (relTblInfo == null) {
                log.warn("No tableInfo found for " + relInfo.getDataClass().getName());
                result = new Object[1];
                result[0] = val;
                return result;
            }
            String formatter = relTblInfo.getDataObjFormatter();
            if (formatter != null) {
                Integer keyVal = null;
                try {
                    keyVal = Integer.valueOf(val.toString());
                } catch (Exception e) {
                    //bad val I guess
                }
                if (keyVal != null) {
                    Object lookedUp = lookup(relTblInfo.getTableId(), keyVal);
                    if (lookedUp != null) {
                        result = new Object[1];
                        result[0] = lookedUp;
                        return result;
                    }
                }

                Object dataObj = keyVal != null ? getObjectFromKey(relTblInfo, keyVal) : null;
                result = new Object[3];
                result[0] = relTblInfo;
                result[1] = dataObj;
                result[2] = keyVal;
                return result;
            }
        }
        return null;
    }
}

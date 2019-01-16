/* Copyright (C) 2017, University of Kansas Center for Research
 *
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import java.util.Map;
import java.util.TreeMap;

public class ERTICaptionInfoRecId extends ERTICaptionInfoQB {
    protected static final Logger log = Logger.getLogger(ERTICaptionInfoRecId.class);

    protected Map<Integer, LookupsCache> lookUppers = new TreeMap<>();
    boolean lookItUp = true;
    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
    //int finds = 0;
    //int looks = 0;
    //boolean showFindStats = false;

    protected Object lookup(Integer tblId, Integer recId) {
        if (!lookItUp) {
            return null;
        }
        LookupsCache cache = lookUppers.get(tblId);
        if (cache != null) {
            return cache.lookupKey(recId);
        } else {
            return null;
        }
    }

    protected Object cache(Integer tblId, Integer recId, Object val) {
        if (!lookItUp || tblId == null || recId == null) {
            return val;
        }
        LookupsCache cache = lookUppers.get(tblId);
        if (cache == null) {
            cache = new LookupsCache();
            lookUppers.put(tblId, cache);
        }
        return cache.addKey(recId, val);
    }

    public ERTICaptionInfoRecId(String colName, String lbl, String stringId, DBFieldInfo fi) {
        super(colName, lbl, true, null, 0, stringId, null, fi);
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getAggClass()
     */
    @Override
    public Class<?> getAggClass()
    {
        // all agg stuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getAggregatorName()
     */
    @Override
    public String getAggregatorName()
    {
        // all agg stuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getColClass()
     */
    @Override
    public Class<?> getColClass()
    {
        return String.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getDataObjFormatter()
     */
    @Override
    public DataObjSwitchFormatter getDataObjFormatter()
    {
        // all agg/formatstuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getSubClass()
     */
    @Override
    public Class<?> getSubClass()
    {
        // all agg/formatstuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getSubClassFieldName()
     */
    @Override
    public String getSubClassFieldName()
    {
        // all agg/formatstuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getUiFieldFormatter()
     */
    @Override
    public UIFieldFormatterIFace getUiFieldFormatter()
    {
        // all agg/formatstuff is handled within this class
        return null;
    }

    protected Object[] getLookupInfo(Object value) {
        //looks++;
        //showFindStats = showFindStats || looks % 5000 == 0;
        if (value instanceof Object[] && ((Object[])value).length == 2) {
            Integer recId = (Integer) ((Object[]) value)[0];
            if (recId == null) {
                return null;
            }

            Short tableNum = (Short) ((Object[]) value)[1];
            if (tableNum == null) {
                return null;
            }
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById((int) tableNum);
            if (ti == null) {
                log.warn("No tableInfo found for " + tableNum);
                return null;
            }
            Object lookedUp = getValueFromKey(ti, recId);
            if (lookedUp != null) {
                //finds++;
                //if (showFindStats) {
                //    System.out.println(getColLabel() + ": " + finds + "/" + looks);
                //    showFindStats = false;
                //}
                Object[] result = new Object[1];
                result[0] = lookedUp;
                return result;
            } else {
                Object[] result = new Object[3];
                result[0] = ti;
                result[1] = getObjectFromKey(ti, recId);
                result[2] = recId;
                return result;
            }
        }
        return null;
    }

    @Override
    public Object processValue(final Object value) {
        Object[] lookupInfo = getLookupInfo(value);
        if (lookupInfo != null) {
            if (lookupInfo.length == 1) {
                return lookupInfo[0];
            }
            DBTableInfo ti = (DBTableInfo)lookupInfo[0];
            Object dataObj = lookupInfo[1];
            String formatter = getDataObjFormatter(ti);
            if (formatter != null && dataObj != null) {
                return cache(ti.getTableId(),(Integer)lookupInfo[2], DataObjFieldFormatMgr.getInstance().format(dataObj, formatter) + " {" + lookupInfo[2] + "}");
            } else {
                return cache(ti.getTableId(), (Integer)lookupInfo[2], ti.getTitle() + " {" + lookupInfo[2] + "}");
            }
        }
        return null;
    }

    protected String getDataObjFormatter(DBTableInfo ti) {
        String result = ti.getDataObjFormatter();
        //table info objects sometimes don't have dataobj formatters, till schema config is done???
        //maybe using dataobjfieldformatmgr will do better in these cases?
        if (result == null || "".equals(result)) {
            DataObjSwitchFormatter f = DataObjFieldFormatMgr.getInstance().getDataFormatter(ti.getName());
            if (f!= null && f.getDataClass().equals(ti.getClassObj())) {
                result = f.getName();
            }
        }
        return "".equals(result) ? null : result;
    }

    protected Object getValueFromKey(DBTableInfo tblInfo, Integer key) {
        return lookup(tblInfo.getTableId(), key);
    }

    protected Object getObjectFromKey(DBTableInfo tblInfo, Integer key) {
        return session.get(tblInfo.getClassObj(), key);
    }

    @Override
    protected void finalize() throws Throwable {
        if (session != null) {
            session.close();
        }
        super.finalize();
    }
}

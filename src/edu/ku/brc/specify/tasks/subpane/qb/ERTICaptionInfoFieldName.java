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
import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.tasks.subpane.qb.ERTICaptionInfoQB;
import edu.ku.brc.specify.tasks.subpane.qb.LookupsCache;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;

public class ERTICaptionInfoFieldName extends ERTICaptionInfoQB {
    protected static final Logger log = Logger.getLogger(edu.ku.brc.specify.tasks.subpane.qb.ERTICaptionInfoFieldName.class);


    public ERTICaptionInfoFieldName(String colName, String lbl, String stringId, DBFieldInfo fi) {
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

    @Override
    public Object processValue(final Object value) {
        if (value instanceof Object[] && ((Object[])value).length == 2) {
            String fieldName = (String)((Object[]) value)[0];
            if (fieldName == null) {
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
            DBInfoBase fi = ti.getFieldByName(fieldName);
            if (fi == null) {
                fi = ti.getRelationshipByName(fieldName);
            }
            if (fi == null) {
                return fieldName;
            } else {
                return fi.getTitle() + " {" + fieldName + "}";
            }
        }
        return null;
    }

}

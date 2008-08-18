/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.ui;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.forms.formatters.DataObjDataField;
import edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace;
import edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 26, 2008
 *
 */
public class CollectingEventDataObjFmt implements DataObjDataFieldFormatIFace
{
    protected static final String securityPrefix = "DO.";

    protected final String FIELD_NUM = "#FN#";
    protected final String LOC_DATE  = "#DT#";
    protected final String CONTINENT = "#CN#";
    protected final String COUNTRY   = "#CT#";
    protected final String STATE     = "#ST#";
    protected final String COUNTY    = "#CO#";
    protected final String LOC_STR   = "#LC#";
    protected final String LATITUDE  = "#LA#";
    protected final String LONGITUDE = "#LO#";
    
    protected String[] tokens = {FIELD_NUM, LOC_DATE, CONTINENT, COUNTRY, STATE, COUNTY, LOC_STR, LATITUDE, LONGITUDE};
    protected Hashtable<String, String> values = new Hashtable<String, String>();
    
    protected String name;
    protected String formatStr;
    
    /**
     * 
     */
    public CollectingEventDataObjFmt()
    {
        
    }
    
    /**
     * @param geo
     * @param rankId
     * @return
     */
    protected Geography getGeoByRank(final Geography geo, final int rankId)
    {
        Geography geography = geo;
        while (geography != null && geography.getRankId() != rankId)
        {
            geography = geography.getParent();
        }
        return geography != null && geography.getRankId() == rankId ? geography : null;
    }
    
    /**
     * @param geo
     * @param rankId
     * @return
     */
    protected String getGeoNameByRank(final Geography geo, final int rankId)
    {
        Geography g = getGeoByRank(geo, rankId);
        return g != null ? g.getName() : "";
    }
    
    /**
     * @param val
     * @return
     */
    protected String getGeoCoordAsStr(final BigDecimal val)
    {
        if (val != null)
        {
            return String.format("%5.2f", val);
        }
        return "";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#format(java.lang.Object)
     */
    public String format(final Object dataValue)
    {
        if (dataValue == null)
        {
            return "";
        }
        
        if (!(dataValue instanceof CollectingEvent))
        {
            throw new RuntimeException("The data value set into CollectingEventDataObjFmt is not a CollectingEvent ["+dataValue.getClass().getSimpleName()+"]");
        }
        
        boolean isSecurityOn = AppContextMgr.isSecurityOn();
        
        if (isSecurityOn)
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(CollectingEvent.getClassTableId());
            if (tblInfo != null)
            {
                SecurityMgr.PermissionBits perm = SecurityMgr.getInstance().getPermission(securityPrefix+tblInfo.getShortClassName());
                if (perm != null)
                {
                    if (!perm.canView())
                    {
                        return "(Restricted)"; // I18N
                    }
                }
            }
        }
        
        CollectingEvent ce = (CollectingEvent)dataValue;
        
        values.clear();
        
        String str = "";
        if (ce.getStartDate() != null)
        {
            DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
            str = scrDateFormat.format(ce.getStartDate().getTime());
        }
        values.put(LOC_DATE, str);
        
        str = ce.getStationFieldNumber();
        values.put(FIELD_NUM,  StringUtils.isNotEmpty(str) ? str : "");
        
        Locality locality = ce.getLocality();
        
        if (isSecurityOn && locality != null)
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(Locality.getClassTableId());
            if (tblInfo != null)
            {
                SecurityMgr.PermissionBits perm = SecurityMgr.getInstance().getPermission(securityPrefix+tblInfo.getShortClassName());
                if (perm != null)
                {
                    if (!perm.canView())
                    {
                        locality = null;
                    }
                }
            }
        }
        
        if (locality != null)
        {
            str = locality.getLocalityName();
            values.put(LOC_STR, StringUtils.isNotEmpty(str) ? str : "");
            
            if (locality != null)
            {
                Geography geo = locality.getGeography();
                if (isSecurityOn && locality != null)
                {
                    DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(Locality.getClassTableId());
                    if (tblInfo != null)
                    {
                        SecurityMgr.PermissionBits perm = SecurityMgr.getInstance().getPermission(securityPrefix+tblInfo.getName());
                        if (perm != null)
                        {
                            if (perm.canView())
                            {
                                values.put(CONTINENT, getGeoNameByRank(geo, 100));
                                values.put(COUNTRY,   getGeoNameByRank(geo, 200));
                                values.put(STATE,     getGeoNameByRank(geo, 300));
                                values.put(COUNTY,    getGeoNameByRank(geo, 400));
                            }
                        }
                    }
                }
            }
            values.put(LATITUDE, getGeoCoordAsStr(locality.getLatitude1()));
            values.put(LONGITUDE, getGeoCoordAsStr(locality.getLongitude1()));
        }
        
        if (StringUtils.isNotEmpty(formatStr))
        {
            String formattedValue = formatStr;
            for (String token : tokens)
            {
                if (StringUtils.contains(formattedValue, token))
                {
                    formattedValue = StringUtils.replace(formattedValue, token, values.get(token));
                }
            }
            return formattedValue;
        }
        
        StringBuilder formattedValue = new StringBuilder("");
        for (String token : tokens)
        {
            String val = values.get(token);
            if (StringUtils.isNotEmpty(val))
            {
                if (formattedValue.length() > 0) formattedValue.append(", ");
                formattedValue.append(val);
            }
        }
        
        if (formattedValue.length() == 0)
        {
            formattedValue.append("The Collecting Event is empty.");
        }
        return formattedValue.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return CollectingEvent.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getFields()
     */
    public DataObjDataField[] getFields()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getName()
     */
    public String getName()
    {
        return "CollectingEventDetailed";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getValue()
     */
    public String getValue()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setValue()
     */
    public void setValue(String value)
    {
        return;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#init(java.lang.String, java.util.Properties)
     */
    public void init(String nameArg, Properties properties)
    {
        this.name = nameArg;
        
        if (properties != null)
        {
            formatStr = properties.getProperty("format");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#isDefault()
     */
    public boolean isDefault()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#isDirectFormatter()
     */
    public boolean isDirectFormatter()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setTableAndFieldInfo()
     */
    public void setTableAndFieldInfo()
    {

    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#toXML(java.lang.StringBuilder)
     */
    public void toXML(StringBuilder sb)
    {
        sb.append("      <external");
        xmlAttr(sb, "class", getClass().getName());
        sb.append(">\n");
        
        // param: format
        if (StringUtils.isNotEmpty(formatStr))
        {
            sb.append("        <param");
            xmlAttr(sb, "name", "format");
            sb.append(">");
            sb.append(formatStr );
            sb.append("</param>\n");
        }
        
        sb.append("      </external>\n");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setDataObjSwitchFormatter(edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter)
     */
    public void setDataObjSwitchFormatter(DataObjSwitchFormatter objFormatter)
    {
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        CollectingEventDataObjFmt cedof = (CollectingEventDataObjFmt)super.clone();
        cedof.values = new Hashtable<String, String>();
        for (String key : values.keySet())
        {
            cedof.values.put(key, values.get(key));
        }
        return cedof;
    }
}

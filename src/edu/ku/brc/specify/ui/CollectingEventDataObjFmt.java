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

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.formatters.DataObjDataField;
import edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 26, 2008
 *
 */
public class CollectingEventDataObjFmt implements DataObjDataFieldFormatIFace, Cloneable
{
    protected static final String securityPrefix = "DO.";
    protected static final String DEF_FMT_STR    = "FN; DT; LC; LA, LO; CO, ST, CT";
    

    protected final String FIELD_NUM = "FN"; // Station Field Number
    protected final String LOC_DATE  = "DT";
    protected final String CONTINENT = "CN";
    protected final String COUNTRY   = "CT";
    protected final String STATE     = "ST";
    protected final String COUNTY    = "CO";
    protected final String LOC_STR   = "LC";
    protected final String LATITUDE  = "LA";
    protected final String LONGITUDE = "LO";
    
    protected String[] tokens = {FIELD_NUM, LOC_DATE, CONTINENT, COUNTRY, STATE, COUNTY, LOC_STR, LATITUDE, LONGITUDE};
    protected Hashtable<String, String> values      = new Hashtable<String, String>();
    protected Hashtable<String, Boolean> fieldsHash = new Hashtable<String, Boolean>();
    
    protected String name;
    protected String formatStr;
    
    // Needed for the Custom Editor
    protected ChangeListener changeListener = null;
    protected JTextField     textField      = null;
    
    /**
     * Constructor.
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
    
    private void fillGeoValues(final Geography geo)
    {
        String[] keys = {CONTINENT, COUNTRY, STATE, COUNTY};
        int rank = 100;
        for (String key : keys)
        {
            if (fieldsHash.get(key) != null)
            {
                values.put(key, getGeoNameByRank(geo, rank));
            }
            rank += 100;
        }
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
        
        if (fieldsHash.size() == 0 && StringUtils.isNotEmpty(formatStr))
        {
            return "The Collecting Event is empty."; // I18N
        }
        
        boolean isSecurityOn = UIHelper.isSecurityOn();
        
        if (isSecurityOn)
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(CollectingEvent.getClassTableId());
            if (tblInfo != null)
            {
                PermissionSettings perm = tblInfo.getPermissions();
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
        
        PermissionSettings localityPerms = null;
        if (isSecurityOn && locality != null)
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(Locality.getClassTableId());
            if (tblInfo != null)
            {
                localityPerms = tblInfo.getPermissions();
                if (localityPerms != null && !localityPerms.canView())
                {
                    locality = null;
                }
            }
        }
        
        Geography geo = null;
        if (locality != null)
        {
            geo = locality.getGeography();
            
            str = locality.getLocalityName();
            values.put(LOC_STR, StringUtils.isNotEmpty(str) ? str : "");
            
            if (fieldsHash.get(LATITUDE) != null)
            {
                values.put(LATITUDE,  getGeoCoordAsStr(locality.getLatitude1()));
            }
            
            if (fieldsHash.get(LONGITUDE) != null)
            {
                values.put(LONGITUDE, getGeoCoordAsStr(locality.getLongitude1()));
            }
        }
        
        if (geo != null)
        {
            if (isSecurityOn)
            {
                DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(Geography.getClassTableId());
                if (tblInfo != null)
                {
                    PermissionSettings perms = tblInfo.getPermissions();
                    if (perms.canView())
                    {
                        fillGeoValues(geo);
                    }
                }
            } else
            {
                fillGeoValues(geo);
            }
        }

        if (StringUtils.isNotEmpty(formatStr))
        {
            String formattedValue = formatStr;
            for (String token : tokens)
            {
                if (StringUtils.contains(formattedValue, token))
                {
                    String valStr = values.get(token);
                    
                    if (StringUtils.isEmpty(valStr))
                    {
                        int  inx = formattedValue.indexOf(token);
                        char sep = formattedValue.charAt(inx+token.length());
                        if (sep == ',' || sep == ';')
                        {
                            token += sep;
                        }
                    }
                    formattedValue = StringUtils.replace(formattedValue, token, valStr);
                }
            }
            return formattedValue;
        }
        return "";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getDataClass()
     */
    @Override
    public Class<?> getDataClass()
    {
        return CollectingEvent.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getFields()
     */
    @Override
    public DataObjDataField[] getFields()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getName()
     */
    @Override
    public String getName()
    {
        return "CollectingEventDetail";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getValue()
     */
    @Override
    public String getValue()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setValue()
     */
    @Override
    public void setValue(String value)
    {
        return;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#init(java.lang.String, java.util.Properties)
     */
    @Override
    public void init(final String nameArg, final Properties properties)
    {
        this.name = nameArg;
        
        if (properties != null)
        {
            formatStr = properties.getProperty("format");
        }
        
        if (StringUtils.isEmpty(formatStr))
        {
            formatStr = DEF_FMT_STR;
        }
        
        for (String token : tokens)
        {
            if (StringUtils.contains(formatStr, token))
            {
                fieldsHash.put(token, true);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#isDefault()
     */
    @Override
    public boolean isDefault()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#isDirectFormatter()
     */
    @Override
    public boolean isDirectFormatter()
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#getSingleField()
     */
    @Override
    public String getSingleField()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setTableAndFieldInfo()
     */
    @Override
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
    @Override
    public void toXML(StringBuilder sb)
    {
        sb.append("          <external");
        xmlAttr(sb, "class", getClass().getName());
        
        // param: format
        if (StringUtils.isNotEmpty(formatStr))
        {
            sb.append(">\n");
            sb.append("            <param");
            xmlAttr(sb, "name", "format");
            sb.append(">");
            sb.append(formatStr );
            sb.append("</param>\n");
            sb.append("          </external>\n");
        } else
        {
            sb.append("/>\n");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setDataObjSwitchFormatter(edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter)
     */
    @Override
    public void setDataObjSwitchFormatter(DataObjSwitchFormatter objFormatter)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#getCustomEditor(javax.swing.JButton)
     */
    @Override
    public JPanel getCustomEditor(final ChangeListener l)
    {
        this.changeListener = l;
        this.textField      = UIHelper.createTextField();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p"));
        pb.add(textField, cc.xy(1, 1));
        
        textField.setText(formatStr);
        
        textField.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        });
        return pb.getPanel();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#isCustom()
     */
    @Override
    public boolean isCustom()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#hasEditor()
     */
    @Override
    public boolean hasEditor()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#isValid()
     */
    @Override
    public boolean isValid()
    {
        return !textField.getText().isEmpty();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#getLabel()
     */
    @Override
    public String getLabel()
    {
        return "Format";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#doneEditting()
     */
    @Override
    public void doneEditting(final boolean wasCancelled)
    {
        formatStr = textField.getText();
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

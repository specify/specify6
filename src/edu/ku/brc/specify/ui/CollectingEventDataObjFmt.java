/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
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
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.formatters.DataObjDataField;
import edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

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
    protected List<Pair<Integer, String>> sepsList = new ArrayList<Pair<Integer, String>>(tokens.length);

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
        
        String restricted = FormHelper.checkForRestrictedValue(CollectingEvent.getClassTableId());
        if (restricted != null)
        {
            return restricted;
        }
        
        boolean isSecurityOn = AppContextMgr.isSecurityOn();
        
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
            if (sepsList.size() == 0)
            {
                ArrayList<Pair<Integer, String>> tokenMappings = new ArrayList<Pair<Integer,String>>();
                for (int i=0;i<tokens.length;i++)
                {
                    int inx = formatStr.indexOf(tokens[i]);
                    if (inx > -1)
                    {
                        tokenMappings.add(new Pair<Integer, String>(inx, tokens[i]));
                    }
                }
                
                Collections.sort(tokenMappings, new Comparator<Pair<Integer, String>>()
                {
                    @Override
                    public int compare(Pair<Integer, String> o1, Pair<Integer, String> o2)
                    {
                        return o1.first.compareTo(o2.first);
                    }
                });
                
                /*for (Pair<Integer, String> locPair : tokenMappings)
                {
                    System.out.println(locPair);
                }*/
                
                int inx = -1;
                int prv = -1;
                for (int i=0;i<tokenMappings.size();i++)
                {
                    Pair<Integer, String> locPair = tokenMappings.get(i);
                    if (prv > -1)
                    {
                        inx = locPair.first;
                        String sep = formatStr.substring(prv, inx);
                        Pair<Integer, String> p = new Pair<Integer, String>(i-1, sep);
                        sepsList.add(p);
                    } else
                    {
                        inx = 0;
                    }
                    prv = inx + locPair.second.length();
                }
            }
            
            if (sepsList.size() > 0)
            {
                StringBuilder sb  = new StringBuilder();
                String        sep = null;
                for (Pair<Integer, String> pair : sepsList)
                {
                    String valStr = values.get(tokens[pair.first]);
                    if (StringUtils.isNotEmpty(valStr))
                    {
                        if (sep != null) sb.append(sep);   
                        sb.append(valStr);  
                        sep = pair.second;
                    }
                }
                return sb.toString();
            }
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

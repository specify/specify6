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
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.ui.forms.formatters.DataObjDataField;
import edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.util.LatLonConverter;

/**
 * Formats a single Latitude or Longitude field from a locality object.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 17, 2007
 *
 */
public class LocalityLatLonFormatter implements DataObjDataFieldFormatIFace, Cloneable
{
    protected String                         name       = null;
    protected LatLonConverter.LATLON         latLonType = LatLonConverter.LATLON.Latitude;
    protected LatLonConverter.DEGREES_FORMAT degreesFMT = LatLonConverter.DEGREES_FORMAT.None;
    protected LatLonConverter.FORMAT         format     = LatLonConverter.FORMAT.DDDDDD;
    protected int                            which      = 1;
    
    /**
     * Default Constructor. 
     */
    public LocalityLatLonFormatter()
    {
        // no opt
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#init(properties)
     */
    public void init(final String nameArg, final Properties properties)
    {
        name = nameArg;
        
        String typeStr = properties.getProperty("type"); 
        if (StringUtils.isNotEmpty(typeStr))
        {
            if (typeStr.equals("Latitude"))
            {
                latLonType = LatLonConverter.LATLON.Latitude;
                
            } else if (typeStr.equals("Longitude"))
            {
                latLonType = LatLonConverter.LATLON.Longitude;
            } else
            {
                throw new RuntimeException("LocalityLatLonFormatter must be initialized with 'type' as 'latitude' or 'longitude'");
            }
        } else
        {
            throw new RuntimeException("LocalityLatLonFormatter cannot be initialized without a 'type' param");
        }
        
        String dirStr = properties.getProperty("dir"); 
        if (StringUtils.isNotEmpty(dirStr))
        {
            if (dirStr.equals("symbol"))
            {
                degreesFMT = LatLonConverter.DEGREES_FORMAT.Symbol;
                
            } else if (dirStr.equals("string"))
            {
                degreesFMT = LatLonConverter.DEGREES_FORMAT.String;
            } else
            {
                degreesFMT = LatLonConverter.DEGREES_FORMAT.None;
            }
        } else
        {
            degreesFMT = LatLonConverter.DEGREES_FORMAT.None;
        }
        
        String formatStr = properties.getProperty("format"); 
        if (StringUtils.isNotEmpty(formatStr))
        {
            if (formatStr.equals("DDMMMM"))
            {
                format = LatLonConverter.FORMAT.DDMMMM;
                
            } else if (formatStr.equals("DDMMSS"))
            {
                format = LatLonConverter.FORMAT.DDMMSS;
            }
            format = LatLonConverter.FORMAT.DDDDDD;
        } else
        {
            format = LatLonConverter.FORMAT.DDDDDD;
        }
        
        which = 1;
        String whichStr = properties.getProperty("which"); 
        if (StringUtils.isNotEmpty(whichStr) && whichStr.equals("2"))
        {
            which = 2;   
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return Locality.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getName()
     */
    public String getName()
    {
        return name;
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
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#getSingleField()
     */
    @Override
    public String getSingleField()
    {
        return null;
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
        
        if (!(dataValue instanceof Locality))
        {
            throw new RuntimeException("The data value set into LocalityLatLonFormatter is not a Locality ["+dataValue.getClass().getSimpleName()+"]");
        }
        
        Locality   locality = (Locality)dataValue;
        BigDecimal value;
        int decimalFmtLen;
        if (which == 1)
        {
            value = latLonType == LatLonConverter.LATLON.Latitude ? locality.getLatitude1() : locality.getLongitude1();
            decimalFmtLen = LatLonConverter.getDecimalLength(latLonType == LatLonConverter.LATLON.Latitude ? locality.getLat1text() : locality.getLong1text());
        } else
        {
            value = latLonType == LatLonConverter.LATLON.Latitude ? locality.getLatitude2() : locality.getLongitude2();
            decimalFmtLen = LatLonConverter.getDecimalLength(latLonType == LatLonConverter.LATLON.Latitude ? locality.getLat2text() : locality.getLong2text());
        }
        
        return LatLonConverter.format(value, latLonType, format, degreesFMT, decimalFmtLen);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getValue()
     */
    public String getValue()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setValue(java.lang.String)
     */
    public void setValue(String value)
    {
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getFields()
     */
    public DataObjDataField[] getFields()
    {
        return null;
    }

    public void toXML(StringBuilder sb)
    {
        sb.append("          <external");
        xmlAttr(sb, "class", getClass().getName());
        sb.append(">\n");

        // param: type
        sb.append("            <param");
        xmlAttr(sb, "name", "type");
        sb.append(">");
        sb.append(latLonType.name());
        sb.append("</param>\n");
        
        // param: dir
        sb.append("            <param");
        xmlAttr(sb, "name", "dir");
        sb.append(">");
        sb.append("symbol");
        sb.append("</param>\n");
        
        // param: format
        sb.append("            <param");
        xmlAttr(sb, "name", "dir");
        sb.append(">");
        sb.append(format.name());
        sb.append("</param>\n");
        
        // param: which
        sb.append("            <param");
        xmlAttr(sb, "name", "which");
        sb.append(">");
        sb.append(which);
        sb.append("</param>\n");
        
        sb.append("          </external>\n");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setTableAndFieldInfo()
     */
    public void setTableAndFieldInfo()
    {
    	return;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setDataObjSwitchFormatter(edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter)
     */
    @Override
    public void setDataObjSwitchFormatter(DataObjSwitchFormatter objFormatter)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#getCustomEditor(javax.swing.event.ChangeListener)
     */
    @Override
    public JPanel getCustomEditor(ChangeListener l)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#isValid()
     */
    @Override
    public boolean isValid()
    {
        return false;
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
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#getLabel()
     */
    @Override
    public String getLabel()
    {
        return "";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#doneEditting(boolean)
     */
    @Override
    public void doneEditting(final boolean wasCancelled)
    {
        
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}

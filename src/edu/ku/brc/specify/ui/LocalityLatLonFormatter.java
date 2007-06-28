/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui;

import java.math.BigDecimal;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.ui.forms.formatters.DataObjDataField;
import edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace;
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
public class LocalityLatLonFormatter implements DataObjDataFieldFormatIFace
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
            if (typeStr.equals("latitude"))
            {
                latLonType = LatLonConverter.LATLON.Latitude;
                
            } else if (typeStr.equals("longitude"))
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
        if (StringUtils.isNotEmpty(whichStr) && formatStr.equals("2"))
        {
            which = 2;   
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getClassObj()
     */
    public Class<?> getClassObj()
    {
        return Locality.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return Locality.class;
    }

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
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#format(java.lang.Object)
     */
    public String format(Object dataValue)
    {
        if (!(dataValue instanceof Locality))
        {
            throw new RuntimeException("The data value set into LocalityLatLonFormatter is not a Locality ["+dataValue.getClass().getSimpleName()+"]");
        }
        
        Locality locality = (Locality)dataValue;
        BigDecimal value;
        if (which == 1)
        {
            value = latLonType == LatLonConverter.LATLON.Latitude ? locality.getLatitude1() : locality.getLongitude1();
        } else
        {
            value = latLonType == LatLonConverter.LATLON.Latitude ? locality.getLatitude2() : locality.getLongitude2();
        }
        
        return LatLonConverter.format(value, latLonType, format, degreesFMT);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getValue()
     */
    public String getValue()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getFields()
     */
    public DataObjDataField[] getFields()
    {
        return null;
    }
}

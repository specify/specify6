/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.forms.formatters.DataObjDataField;
import edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

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
public class CatalogNumberFormatter implements DataObjDataFieldFormatIFace, Cloneable
{
    protected static final Logger log = Logger.getLogger(CatalogNumberStringUIFieldFormatter.class);
    
    protected String name;
    
    /**
     * Default Constructor. 
     */
    public CatalogNumberFormatter()
    {
        // no opt
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#init(properties)
     */
    public void init(final String nameArg, final Properties properties)
    {
        this.name = nameArg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getClassObj()
     */
    public Class<?> getClassObj()
    {
        return String.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return String.class;
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
        return true;
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
        if (dataValue != null && dataValue instanceof String)
        {
            return StringUtils.stripStart((String)dataValue, "0");
        }
        return "";
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
    	// not yet implemented
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

    /**
     * @param entry a list of one or more numeric cataloger numbers, possibly containing alphabetic prefixes or suffixes.
     * @param formatter the CatalogNumber formatter for the entry.
     * @return a comma delimited of numeric portions of the catalognumbers in the list. Non-numeric characters are treated as
     * delimiters.
     */
    public static String preParseNumericCatalogNumbers(final String entry, final UIFieldFormatterIFace formatter)
    {
    	if (formatter instanceof CatalogNumberUIFieldFormatter && ((CatalogNumberUIFieldFormatter )formatter).isNumeric())
    	{
    		//just go through char by char
    		StringBuilder result = new StringBuilder();
    		String numericCatNumChars = "0123456789"; //No negative signs and no decimal points in Numeric cat nums.
    		boolean separate = false;
    		for (int i = 0; i < entry.length(); i++)
    		{
    			String current = entry.substring(i, i+1);
    			if (numericCatNumChars.contains(current))
    			{
    				result.append(current);
    				separate = true;
    			}
    			else if (result.length() > 0 && separate)
    			{
    				result.append(", ");
    				separate = false;
    			}
    		}
    		return result.toString();
    	}
    	log.warn("not pre-parsing " + entry + " because the supplied formatter is not a numeric catalognumber formatter");
    	return entry;
    }

}

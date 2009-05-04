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
package edu.ku.brc.af.core.db;

import java.util.Properties;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 20, 2007
 *
 */
public interface AutoNumberIFace
{

    /**
     * Used for initialization.
     * @param properties the initialization properties
     */
    public void setProperties(final Properties properties);
    
    /**
     * Get the next number.
     * @param formatter the formatter to use
     * @param value the current value
     * @return the fully expressed number.
     */
    public abstract String getNextNumber(final UIFieldFormatterIFace formatter, String value);
    
    /**
     * @return true if using the defined 'generic' class for numbering, false if using an external class.
     */
    public abstract boolean isGeneric();
    
    /**
     * Appends a presentation of itself in XML to the StringBuilder
     * @param sb the StringBuilder
     */
    public abstract void toXML(StringBuilder sb);
    
    /**
     * 
     * @return true if there was a problem when trying to auto number and the code should check 
     * the ErrorKey for the localized error message.
     */
    public abstract boolean isInError();
    
    /**
     * @return the localized error message.
     */
    public abstract String getErrorMsg();
    
}

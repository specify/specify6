/* Filename:    $RCSfile: PrefsGettableImpl.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/10 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.prefs;

import java.util.prefs.Preferences;

import edu.ku.brc.specify.ui.forms.DataObjectGettable;

/**
 * This knows how to get a field's value from a POJO.<br><br>
 * Implementation idea: We may need to cache the method objects, 
 * and then the factory will want to create a different object per class that will be using this)
 * 
 * @author rods
 *
 */
public class PrefsGettableImpl implements DataObjectGettable
{
    // Static Data Members
    //private static Log log = LogFactory.getLog(PrefsGettableImpl.class);
    
    /**
     * Default constructor (needed for factory) 
     */
    public PrefsGettableImpl()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String)
     */
    public Object getFieldValue(Object dataObj, String fieldName) 
    {
        if (!(dataObj instanceof Preferences))
        {
            throw new RuntimeException("dataObj is not a Preference in call getFieldValue fieldName["+fieldName+"] data["+dataObj+"]");
        }

        //System.out.println("getFieldValue["+dataObj+"]  ["+fieldName+"]  ["+ ((Preferences)dataObj).get(fieldName, "")+"]");
        return ((Preferences)dataObj).get(fieldName, "");
    }  
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    public Object getFieldValue(Object dataObj, String fieldName, String format) 
    {
        
        if (format != null && format.length() > 0 && fieldName.indexOf(",") > -1)
        {

        }
        return ((Preferences)dataObj).get(fieldName, "");
    }    
    

}

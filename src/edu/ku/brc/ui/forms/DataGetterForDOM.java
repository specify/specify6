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
package edu.ku.brc.ui.forms;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;

/**
 * Class to get values from a DOM Document.
 * 
 * @author Rod Spears
 */
public class DataGetterForDOM implements DataObjectGettable
{
   
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String)
     */
    public Object getFieldValue(Object dataObj, String fieldName)
    {
        if (dataObj == null)
        {
            return null;
        }
        
        if (!(dataObj instanceof Element))
        {
            throw new RuntimeException("DataGetterForDOM the dataObj is NOT an a dom4j Element! "+dataObj.getClass().getSimpleName());
        }
        
        String fldName;
        String attr;
        
        if (fieldName.indexOf('#') > -1)
        {
            String[] tokens = StringUtils.split(fieldName, "#");
            fldName = tokens[0];
            attr    = tokens[1];
            
        } else
        {
            fldName = fieldName;
            attr    = null;
        }
        
        Element domRoot = (Element)dataObj;
        Element element = (Element)domRoot.selectSingleNode(fldName);
        if (element != null)
        {
            if (attr != null)
            {
                return XMLHelper.getAttr(element, attr, null);
                
            } else
            {
                return element.getTextTrim();
            }
        }
        return null;
    }
    
}

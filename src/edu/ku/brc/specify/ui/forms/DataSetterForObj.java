/* Filename:    $RCSfile: DataSetterForObj.java,v $
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
package edu.ku.brc.specify.ui.forms;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.datamodel.AttributeIFace;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.prefs.PrefsCache;


/**
 * This knows how to set a field's value into a POJO.<br><br>
 * Implementation idea: We may need to cache the method objects, 
 * and then the factory will want to create a different object per class that will be using this)
 * 
 * @author rods
 *
 */
public class DataSetterForObj implements DataObjectSettable
{
    // Static Data Members
    private static Log log = LogFactory.getLog(DataSetterForObj.class);
    
    protected Object[] args = new Object[1];
    
    /**
     * Default constructor (needed for factory) 
     */
    public DataSetterForObj()
    {

    }

    
    public void setFieldValue(Object dataObj, String fieldName, Object data)
    {
        try
        {
            PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, fieldName.trim());
            if (descr != null)
            {
                Method setter = PropertyUtils.getWriteMethod(descr);
                if (setter != null)
                {
                    args[0] = data;
                    setter.invoke(dataObj, args);
                }
            } else
            {
                log.error("We could not find a field named[" + fieldName.trim()+ "] in data object [" + dataObj.getClass().toString() + "]");
            }
        } catch (Exception ex)
        {
            log.error(ex);
        }
    }
    
}

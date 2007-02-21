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
/**
 * 
 */
package edu.ku.brc.ui.forms;

import edu.ku.brc.specify.datamodel.Workbench;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 20, 2007
 *
 */
public class DataGetterForGrid implements DataObjectGettable
{

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String)
     */
    public Object getFieldValue(Object dataObj, String fieldName)
    {
        if (dataObj instanceof Workbench.WorkbenchRow)
        {
            Workbench.WorkbenchRow wbr = (Workbench.WorkbenchRow)dataObj;
            return wbr.getData(Integer.parseInt(fieldName));
        }
        return "";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectGettable#usesDotNotation()
     */
    public boolean usesDotNotation()
    {
        // TODO Auto-generated method stub
        return false;
    }

}

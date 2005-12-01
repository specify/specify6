/* Filename:    $RCSfile: ChartPane.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
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
package edu.ku.brc.specify.core.subpane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.core.Taskable;

public class ChartPane extends BaseSubPane implements Chartable
{
    // Static Data Members
    private static Log log = LogFactory.getLog(ChartPane.class);
    
    // Data Members
    protected String title       = "";
    protected String xAxisTitle  = "";
    protected String yAxisTitle  = "";
    protected boolean isVertical = true;
    
    public ChartPane(final String name, 
                     final Taskable task)
    {
        super(name, task);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#getName()
     */
    public String getName()
    {
        return title != null && title.length() > 0 ? title : super.getName();
    }
       
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.subpane.Chartable#setTitle(java.lang.String)
     */
    public void setTitle(final String title)
    {
        this.title = title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.subpane.Chartable#setXAxis(java.lang.String)
     */
    public void setXAxis(final String title)
    {
        xAxisTitle = title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.subpane.Chartable#setYAxis(java.lang.String)
     */
    public void setYAxis(final String title)
    {
       yAxisTitle = title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.subpane.Chartable#setVertical(boolean)
     */
    public void setVertical(boolean isVertical)
    {
        this.isVertical = isVertical;
    }

    //-----------------------------------------------
    //-- Static Helpers
    //-----------------------------------------------
    public static float getFloat(Object valObj)
    {
        float value = 0.0f;
        if (valObj != null)
        {
            if (valObj instanceof Integer)
            {
                value = ((Integer)valObj).floatValue();
            } else if (valObj instanceof Long)
            {
                value = ((Long)valObj).floatValue();
            } else if (valObj instanceof Float)
            {
                value = ((Float)valObj).floatValue();
            } else if (valObj instanceof Double)
            {
                value = ((Double)valObj).floatValue();
            } else
            {
                System.out.println("getFloat - Class type is "+valObj.getClass().getName());
            }
        } else
        {
            log.error("getFloat - Result Object is null for["+valObj+"]");
        }
        return value;
    }
    
    public static int getInt(Object valObj)
    {
        int value = 0;
        if (valObj != null)
        {
            if (valObj instanceof Integer)
            {
                value = ((Integer)valObj).intValue();
            } else if (valObj instanceof Long)
            {
                value = ((Long)valObj).intValue();
            } else if (valObj instanceof Float)
            {
                value = ((Float)valObj).intValue();
            } else if (valObj instanceof Double)
            {
                value = ((Double)valObj).intValue();
            } else
            {
                System.out.println("getInt - Class type is "+valObj.getClass().getName());
            }
        } else
        {
            log.error("getInt - Result Object is null for["+valObj+"]");
        }
        return value;
    }
    
    public static String getString(Object valObj)
    {
        if (valObj != null)
        {
            if (valObj instanceof String)
            {
                return (String)valObj;
            } else
            {
                System.out.println("getString - Class type is "+valObj.getClass().getName()+" should be String");
            }
        } else
        {
            log.error("getString - Result Object is null for["+valObj+"] in getString");
        }
        return "";
   }
    

}

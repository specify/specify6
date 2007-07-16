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
package edu.ku.brc.af.core;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import org.dom4j.Element;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 10, 2007
 *
 */
public class ERTIColInfo
{
    protected int     position;
    protected boolean isIdColumn;
    protected String  colName;
    protected String  secondaryKey;
    protected String  formatter;
    protected int     idIndex = -1;
    
    public ERTIColInfo(final Element element)
    {
        position     = getAttr(element, "pos", -1);
        isIdColumn   = getAttr(element, "id", false);
        colName      = element.getTextTrim();
        secondaryKey = getAttr(element, "key", null);
        formatter    = getAttr(element, "formatter", null);
        
        if (isIdColumn)
        {
            idIndex = position;
        }
    }

    public int getPosition()
    {
        return position;
    }

    public boolean isIdColumn()
    {
        return isIdColumn;
    }

    public String getColName()
    {
        return colName;
    }

    public String getSecondaryKey()
    {
        return secondaryKey;
    }

    public String getFormatter()
    {
        return formatter;
    }

    public int getIdIndex()
    {
        return idIndex;
    }
}

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
public class ERTIJoinColInfo
{
    protected int    position;
    protected String joinTableId;
    protected int    joinTableIdAsInt;
    protected String colName;
    
    public ERTIJoinColInfo(final Element element)
    {
        position         = getAttr(element, "pos", -1);
        joinTableId      = getAttr(element, "tableid", null);
        joinTableIdAsInt = getAttr(element, "tableid", -1);
        colName          = element.getTextTrim();
    }

    public int getPosition()
    {
        return position;
    }

    public String getJoinTableId()
    {
        return joinTableId;
    }

    public int getJoinTableIdAsInt()
    {
        return joinTableIdAsInt;
    }

    public String getColName()
    {
        return colName;
    }
}

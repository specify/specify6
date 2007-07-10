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
package edu.ku.brc.ui.forms.formatters;

/**
 * This class aggregates to sevral Data Objects into a single value by taking the output from the formatters and appending them
 * together with a seaprator character (or stirng).
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 17, 2007
 *
 */
public class DataObjAggregator
{
    protected String          name;
    protected Class           dataClass;
    protected boolean         isDefault;
    protected String          separator;
    protected Integer         count      = null;
    protected String          ending;
    protected String          formatName;
    
    public DataObjAggregator(final String name, 
                             final Class dataClass,
                             final boolean isDefault, 
                             final String separator, 
                             final Integer count, 
                             final String ending, 
                             final String formatName)
    {
        super();
        this.name = name;
        this.isDefault = isDefault;
        this.dataClass = dataClass;
        this.separator = separator;
        this.count = count;
        this.ending = ending;
        this.formatName = formatName;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public Class getDataClass()
    {
        return dataClass;
    }

    public Integer getCount()
    {
        return count;
    }

    public String getEnding()
    {
        return ending;
    }

    public String getFormatName()
    {
        return formatName;
    }

    public String getName()
    {
        return name;
    }

    public String getSeparator()
    {
        return separator;
    }
}
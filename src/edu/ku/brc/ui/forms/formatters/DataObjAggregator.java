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

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import org.apache.commons.lang.StringUtils;

/**
 * This class aggregates to several Data Objects into a single value by taking the output from the formatters and appending them
 * together with a separator character (or string).
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
    protected Class<?>        dataClass;
    protected boolean         isDefault;
    protected String          separator;
    protected Integer         count      = null;
    protected String          ending;
    protected String          formatName;
    protected String          orderFieldName;
    protected boolean         useIdentity;
    
    public DataObjAggregator()
    {
    }
    
	public DataObjAggregator(final String   name, 
                             final Class<?> dataClass,
                             final boolean  isDefault, 
                             final String   separator, 
                             final Integer  count, 
                             final String   ending, 
                             final String   formatName,
                             final String   orderFieldName)
    {
        super();
        this.name           = name;
        this.isDefault      = isDefault;
        this.dataClass      = dataClass;
        this.separator      = separator;
        this.count          = count;
        this.ending         = ending;
        
        this.orderFieldName = orderFieldName;
        
        setFormatName(formatName); // sets boolean also
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public Class<?> getDataClass()
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

    /**
     * @return the useIdentity
     */
    public boolean useIdentity()
    {
        return useIdentity;
    }

    /**
     * @return the orderFieldName
     */
    public String getOrderFieldName()
    {
        return orderFieldName;
    }

	public void setName(String name)
    {
        this.name = name;
    }

    public void setDataClass(Class<?> dataClass)
    {
        this.dataClass = dataClass;
    }

    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    public void setSeparator(String separator)
    {
        this.separator = separator;
    }

    public void setCount(Integer count)
    {
        this.count = count;
    }

    public void setEnding(String ending)
    {
        this.ending = ending;
    }

    public void setFormatName(String formatName)
    {
        this.formatName  = formatName;
        this.useIdentity = formatName.equals("identity");
    }

    public void setOrderFieldName(String orderFieldName)
    {
        this.orderFieldName = orderFieldName;
    }

	public String toString()
	{
		//String countStr = "";
		String orderStr = "";
		//if (count != null)
		//{
		//	countStr = ""; // XXX: what does count mean?
		//}
		if (StringUtils.isNotEmpty(orderFieldName))
		{
			orderStr = " (sorted by " + orderFieldName + ")";
		}
		// following string should really be the formatter field name rather than the class simple name
		String simpleName = dataClass.getSimpleName();
		return simpleName + separator + simpleName + ending + orderStr;
	}

	/*
	 * 
	 */
	public void toXML(StringBuilder sb)
	{
		String padding = "\n               ";
		sb.append         ("    <aggregator");
		xmlAttr(sb, "name", 		  name);                       sb.append(padding);
		xmlAttr(sb, "class", 		  dataClass.getName());        sb.append(padding);
		xmlAttr(sb, "default", 	      String.valueOf(isDefault));  sb.append(padding);
		xmlAttr(sb, "separator", 	  separator);                  sb.append(padding);
		xmlAttr(sb, "ending", 		  ending);                     sb.append(padding);
		xmlAttr(sb, "count",          String.valueOf(count));      sb.append(padding);
		xmlAttr(sb, "formatName",     formatName);                 sb.append(padding);
        xmlAttr(sb, "orderFieldName", orderFieldName);             sb.append(padding);
		sb.append("/>\n\n");
	}
}
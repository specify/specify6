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
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 17, 2007
 *
 */
public class UIFieldFormatterField
{
    public enum FieldType {numeric, alphanumeric, alpha, separator, year}
    
    protected FieldType type;
    protected int       size;
    protected String    value;
    protected boolean   incrementer;
    protected boolean   byYear;
    
    public UIFieldFormatterField(final FieldType type, 
                                 final int       size, 
                                 final String    value, 
                                 final boolean   incrementer, 
                                 final boolean   byYear)
    {
        super();
        
        this.type        = type;
        this.size        = size;
        this.value       = value;
        this.incrementer = incrementer;
        this.byYear      = byYear;
        
        if (incrementer)
        {
            StringBuilder sb = new StringBuilder();
            for (int i=0;i<size;i++)
            {
                sb.append('#');
            }
            this.value = sb.toString();
        }
    }
    
    public UIFieldFormatterField(final FieldType type, 
                                 final int       size, 
                                 final String    value, 
                                 final boolean   incrementer)
    {
        this(type, size, value, incrementer, false);
    }

    public int getSize()
    {
        return size;
    }

    public FieldType getType()
    {
        return type;
    }

    public String getValue()
    {
        //if (type == FieldType.year)
        //{
        //    return String.format("%4d", Calendar.getInstance().get(Calendar.YEAR));
        //}
        return value;
    }

    public boolean isIncrementer()
    {
        return incrementer;
    }
    
    /**
     * @return the byYear
     */
    public boolean isByYear()
    {
        return byYear;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "Type["+type+"]  size["+size+"]  value["+value+"] incr["+incrementer+"]";
    }

}

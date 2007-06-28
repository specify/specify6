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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.util.Pair;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 17, 2007
 *
 */
public class UIFieldFormatter
{
    public enum PartialDateEnum {None, Full, Month, Year};
    
    protected String               name;
    protected Class                dataClass;
    protected boolean              isDate;
    protected PartialDateEnum      partialDateType;
    protected boolean              isDefault;
    protected List<UIFieldFormatterField> fields;
    protected boolean              isIncrementer;
    protected DateWrapper          dateWrapper = null;

    public UIFieldFormatter(final String  name, 
                            final boolean isDate, 
                            final PartialDateEnum partialDateType,
                            final Class   dataClass,
                            final boolean isDefault,
                            final boolean isIncrementer,
                            final List<UIFieldFormatterField> fields)
    {
        this.name      = name;
        this.dataClass = dataClass;
        this.partialDateType = partialDateType;
        this.isDate    = isDate;
        this.isDefault = isDefault;
        this.fields    = fields;
        this.isIncrementer = isIncrementer;
    }

    public List<UIFieldFormatterField> getFields()
    {
        return fields;
    }

    public String getName()
    {
        return name;
    }

    public boolean isDate()
    {
        return isDate;
    }

    public Class getDataClass()
    {
        return dataClass;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public boolean isIncrementer()
    {
        return isIncrementer;
    }

    public void setIncrementer(boolean isIncrementer)
    {
        this.isIncrementer = isIncrementer;
    }
    
    public PartialDateEnum getPartialDateType()
    {
        return partialDateType;
    }
    
    public int getLength()
    {
        int len = 0;
        for (UIFieldFormatterField field : fields)
        {
            len += field.getSize();
        }
        return len;
    }
    
    public Pair<Integer, Integer> getIncPosition()
    {
        int len = 0;
        for (UIFieldFormatterField field : fields)
        {
            if (field.isIncrementer())
            {
                return new Pair<Integer, Integer>(len, len+field.getSize());
            }
            len += field.getSize();
        }
        return null;
    }
    
    public String toPattern()
    {
        StringBuilder str = new StringBuilder();
        for (UIFieldFormatterField field : fields)
        {
            str.append(field.getValue());
        }
        return str.toString();
    }
    
    /**
     * @return the dateWrapper
     */
    public DateWrapper getDateWrapper()
    {
        return dateWrapper;
    }

    /**
     * Sets Date Wrapper.
     * @param dateWrapper the dateWrapper to set
     */
    public void setDateWrapper(final DateWrapper dateWrapper)
    {
        this.dateWrapper = dateWrapper;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Name["+name+"] isDate["+isDate+"]  dataClass["+dataClass.getSimpleName()+"] isDefault["+isDefault+"] isIncrementor["+isIncrementer+"]");
        for (UIFieldFormatterField f : fields)
        {
            s.append("\n  "+f.toString());
        }
        return s.toString();
    }

    /**
     * This is work in progress.
     * @return the next formatted ID
     */
    public String getNextId()
    {
        // For Demo
        try
        {
            Connection conn = DBConnection.getInstance().createConnection();
            Statement  stmt = conn.createStatement();
            // MySQL should use Hibernate
            ResultSet  rs   = stmt.executeQuery("select "+name+" from "+dataClass.getSimpleName()+" order by "+name+" desc limit 0,1");
            if (rs.first())
            {
                String numStr      = rs.getString(1);
                int    offsetStart = 1;
                int    offsetEnd   = numStr.length();
                for (UIFieldFormatterField ff : fields)
                {
                    if (!ff.isIncrementer())
                    {
                        offsetStart += ff.getSize();
                    } else
                    {
                        offsetEnd = offsetStart + ff.getSize();
                        break;
                    }
                }
                int num = Integer.parseInt(numStr.substring(offsetStart, offsetEnd));
                num++;
                return String.format("2006-%03d", new Object[] {num});
                
            } else
            {
                return "2006-001";
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    
}



/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.ui.forms.formatters;

import java.util.Vector;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.ui.BaseUIFieldFormatter;
import edu.ku.brc.util.Pair;

/**
 * This class is used for formatting Strings with no format (only a length constraint).
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * May 23, 2008
 *
 */
public class GenericStringUIFieldFormatter extends BaseUIFieldFormatter
{
    private Class<?> dataClass;
    
    /**
     * Constructs a string based non-formatter formatter.
     * @param name
     * @param tableClass
     * @param fieldName
     * @param localizedTitle
     * @param uiDisplayLen
     */
    public GenericStringUIFieldFormatter(final String   name,
                                         final Class<?> tableClass,
                                         final String   fieldName,
                                         final String   localizedTitle,
                                         final int      uiDisplayLen)
    {
        super();
        
        this.name      = name;
        this.title     = localizedTitle;
        
        DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(tableClass.getSimpleName());
        DBFieldInfo fi = ti.getFieldByName(fieldName);
        
        this.dataClass              = tableClass;
        this.length                 = fi.getLength();
        this.uiLength               = uiDisplayLen;
        this.isNumericCatalogNumber = false;
        this.isIncrementer          = false;
        this.autoNumber             = null;
        
        pattern    = UIFieldFormatterMgr.getFormatterPattern(isIncrementer, UIFieldFormatterField.FieldType.anychar, length);
        
        field      = new UIFieldFormatterField(UIFieldFormatterField.FieldType.anychar, length, pattern, isIncrementer, false); 
        fields     = new Vector<UIFieldFormatterField>();
        fields.add(field);
        incPos     = new Pair<Integer, Integer>(0, length);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.BaseUIFieldFormatter#isLengthOK(int)
     */
    @Override
    public boolean isLengthOK(int lengthOfData)
    {
        return lengthOfData < length;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.BaseUIFieldFormatter#getDataClass()
     */
    @Override
    public Class<?> getDataClass()
    {
        return dataClass;
    }
}

/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Calendar;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.specify.dbsupport.TypeCode;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ERTICaptionInfoQB extends ERTICaptionInfo
{
    /**
     * A unique identifier for the column within the QB query which is independent of the column's caption.
     */
    protected final String                 colStringId;
    protected final PickListDBAdapterIFace pickList;
    
    /**
     * @param colName
     * @param colLabel
     * @param isVisible
     * @param uiFieldFormatter
     * @param posIndex
     * @param colStringId
     * @param pickList
     */
    public ERTICaptionInfoQB(String  colName, 
                           String  colLabel, 
                           boolean isVisible, 
                           UIFieldFormatterIFace uiFieldFormatter,
                           int     posIndex,
                           String colStringId,
                           PickListDBAdapterIFace pickList,
                           DBFieldInfo fieldInfo)
    {
        super(colName, colLabel, isVisible, uiFieldFormatter, posIndex);
        this.fieldInfo = fieldInfo;
        this.colStringId = colStringId;
        this.pickList = pickList;
    }

    /**
     * @return the colStringId;
     */
    public String getColStringId()
    {
        return colStringId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ERTICaptionInfo#processValue(java.lang.Object)
     */
    @Override
    public Object processValue(Object value)
    {
        //This is a quick and dirty way to deal with PartialDates formatting.
    	if (value instanceof Object[])
        {
        	if (uiFieldFormatter != null)
            {
                return this.uiFieldFormatter.formatToUI((Object[] )value);
            }
            if (fieldInfo.isPartialDate() && ((Object[])value).length == 2)
            {
            	Object date = ((Object[])value)[0];
            	if (date == null)
            	{
            		return "";
            	}
            	
            	Byte precision = (Byte)((Object[])value)[1];
            	if (precision == null)
            	{
            		precision = 1;
            	}
            	UIFieldFormatterIFace.PartialDateEnum datePrec = UIFieldFormatterIFace.PartialDateEnum.values()[precision];
            	boolean isPartial = false;
            	String formatName = "Date";
            	if (datePrec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
            	{
            		isPartial = true;
            		formatName = "PartialDateMonth";
            	} else if (datePrec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
            	{
            		isPartial = true;
            		formatName = "PartialDateYear";
            	}
            	for (UIFieldFormatterIFace formatter : UIFieldFormatterMgr.getInstance().getDateFormatterList(isPartial))
            	{
                    if (formatter.getName().equals(formatName))
                    {
                    	//return formatter.formatToUI(date);
                    	return formatter.getDateWrapper().format(((Calendar)date).getTime());
                    }

            	}
            }
        }	
    	
    	if (value != null && fieldInfo.getDataClass().equals(java.sql.Timestamp.class)) 
    	{
    		String result = value.toString();
    		if (result.endsWith(".0")) {
    			result = result.substring(0, result.length() - 2);
    		}
    		return result;
    	}
    	//else another complication - formats for export to db
    	if (uiFieldFormatter instanceof ExportFieldFormatter)
    	{
    		return this.uiFieldFormatter.formatToUI(value);
    	}


    	//else
    	//XXX for large picklists the next two blocks could become time-consuming...
    	if (value != null && pickList instanceof TypeCode)
        {
            PickListItemIFace item = ((TypeCode )pickList).getItemByValue(value);
            if (item != null)
            {
                return item.getTitle();
            }
            return value.toString();
        }
    	if (value != null && pickList != null)
    	{
            for (PickListItemIFace item : pickList.getList())
            {
            	if (item.getValue() != null && item.getValue().equals(value))
            	{
            		return item.getTitle();
            	}
            }
            return value.toString();
    	}
    	//else
    	return super.processValue(value);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ERTICaptionInfo#getColClass()
     */
    @Override
    public Class<?> getColClass()
    {
        if (pickList instanceof TypeCode)
        {
            return String.class;
        }
        return super.getColClass();
    }
    
    
}

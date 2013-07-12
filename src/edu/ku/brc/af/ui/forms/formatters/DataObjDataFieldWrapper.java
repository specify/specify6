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
package edu.ku.brc.af.ui.forms.formatters;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;

/**
 * Wrapper for data object formatters.
 * Created to modify toString() method and display item nicely on JList.
 *  
 * @author ricardo
 *
 * @code_status Alpha
 *
 *
 */
public class DataObjDataFieldWrapper
{
	protected DataObjDataField fmtField;
	
	DataObjDataFieldWrapper(DataObjDataField fmtField)
	{
		this.fmtField = fmtField; 
	}
	
	public DataObjDataField getFormatterField()
	{
		return fmtField;
	}
	
	public boolean isPureField()
	{
		return fmtField.isPureField();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		if (StringUtils.isNotEmpty(fmtField.getDataObjFormatterName()))
		{
			// data obj switch formatter
			return fmtField.getDataObjFormatterName() + " (display format)";
		}

		// try and get the field formatter from static instance
		if (fmtField.getUiFieldFormatter() == null && StringUtils.isNotEmpty(fmtField.getUiFieldFormatterName()))
		{
			UIFieldFormatterIFace fmt = UIFieldFormatterMgr.getInstance().getFormatter(fmtField.getUiFieldFormatterName());
			fmtField.setUiFieldFormatter(fmt);
		}
		
		// field
		DBFieldInfo        fieldInfo = fmtField.getFieldInfo();
		DBRelationshipInfo relInfo   = fmtField.getRelInfo();
		
		String result = (relInfo != null)? relInfo.getTitle() + "." : "";
		String pattern = (fmtField.getUiFieldFormatter() != null)? " (" + fmtField.getUiFieldFormatter().toPattern() + ")" : "";
		return (fieldInfo != null)? result + fieldInfo.getTitle() + pattern : result + pattern;
	}
}

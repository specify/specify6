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

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import edu.ku.brc.af.core.db.DBFieldInfo;

/**
 * This class is used to build UIFieldFormatters from a formatting string for floating point numeric fields 
 * 
 * @author Ricardo
 *
 */
public class UIFloatFieldFormatterFactory extends UINumericFieldFormatterFactory
{
	/**
	 * Constructor
	 */
	public UIFloatFieldFormatterFactory(DBFieldInfo fieldInfo)
	{
		super(fieldInfo);
	}

	//@Override
	protected String getRegEx()
	{
		return "^(N+(\\.N+)?)$";
	}

	//@Override
	public String getHelpHtml()
	{
		return getResourceString("FFE_HELP_FLOAT_HTML");
	}

}

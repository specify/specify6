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

package edu.ku.brc.af.ui.forms.formatters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.FormatterType;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.PartialDateEnum;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField.FieldType;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 */
public abstract class UINumericFieldFormatterFactory extends UIFieldFormatterFactory
{
	/**
	 * Constructor
	 */
	public UINumericFieldFormatterFactory(DBFieldInfo fieldInfo)
	{
		super(fieldInfo);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterFactory#createFormat(java.lang.String)
	 */
    //@Override
	public UIFieldFormatter createFormat(String formattingString) throws UIFieldFormatterParsingException
	{
		Class<?> clazz = fieldInfo.getTableInfo().getClassObj();
		UIFieldFormatter fmt = new UIFieldFormatter(null, 
		                                            false, 
		                                            fieldInfo.getName(), 
				                                    FormatterType.numeric, 
				                                    PartialDateEnum.None, 
				                                    clazz, 
				                                    false, 
				                                    false, 
				                                    null);

		// separators and split pattern strings
		Pattern splitPattern = Pattern.compile(getRegEx());
		Matcher matcher = splitPattern.matcher(formattingString);

		if (matcher.find())
		{
			String value = matcher.group();
			UIFieldFormatterField field = new UIFieldFormatterField(FieldType.numeric, value.length(), value, false);
			fmt.addField(field);
		}
		else
		{
			throw new UIFieldFormatterParsingException("Invalid formatting string: " + formattingString, formattingString);
		}

		return fmt;
	}
	
	protected abstract String getRegEx();
}

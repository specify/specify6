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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField.FieldType;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.FormatterType;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.PartialDateEnum;

/**
 * This class is used to build UIFieldFormatters from a formatting string for text fields 
 * 
 * @author Ricardo
 *
 */
public class UITextFieldFormatterFactory extends UIFieldFormatterFactory
{
	/**
	 * Constructor
	 */
	public UITextFieldFormatterFactory(DBFieldInfo fieldInfo)
	{
		super(fieldInfo);
	}
	
	/**
	 * Factory that creates a new UIFieldFormatter from a formatting string
	 * 
	 * @param formattingString
	 *            Formatting string that defines the formatter
	 * @return The UIFieldFormatter corresponding to the formatting string
	 * @throws UIFieldFormattingParsingException
	 *             (if formatting string is invalid)
	 */
	public UIFieldFormatter createFormat(final String formattingString) throws UIFieldFormatterParsingException
	{
		Class<?> clazz = fieldInfo.getTableInfo().getClassObj();
		UIFieldFormatter fmt = new UIFieldFormatter(null, 
		                                            false, 
		                                            fieldInfo.getName(), 
				                                    FormatterType.generic, 
				                                    PartialDateEnum.None, 
				                                    clazz, 
				                                    false, 
				                                    false, 
				                                    null);

		AutoNumberIFace autoNumber = UIFieldFormatterMgr.getInstance().createAutoNumber("edu.ku.brc.af.core.db.AutoNumberGeneric", 
				                                                          clazz.getName(), 
				                                                          fieldInfo.getName(),
				                                                          fmt.getFields().size() == 1);
		fmt.setAutoNumber(autoNumber);

		// separators and split pattern strings
		Pattern splitPattern = Pattern.compile("([\\/\\-\\_ ])+");
		Matcher matcher      = splitPattern.matcher(formattingString);

		// Find all the separator matches and create individual fields by
		// calling formatter field factory
		UIFieldFormatterField field;
		String                fieldString   = "";
		int                   begin         = 0;
		boolean               isIncrementer = false;
		while (matcher.find())
		{
			// create a field with what's before the current separator
			fieldString = formattingString.substring(begin, matcher.start());
			field       = UIFieldFormatterField.factory(fieldString);
			fmt.addField(field);
			begin = matcher.end();
			if (field.isIncrementer())
			{
			    isIncrementer = true;
			}

			// create separator field
			String value = matcher.group();
			field = new UIFieldFormatterField(FieldType.separator, value.length(), value, false);
			fmt.addField(field);
		}

		// create last bit of formatter
		fieldString = formattingString.substring(begin);
		field       = UIFieldFormatterField.factory(fieldString);
		fmt.addField(field);
        if (field.isIncrementer())
        {
            isIncrementer = true;
        }
		fmt.setIncrementer(isIncrementer);

		return fmt;
	}

	/**
	 * Returns the help string (in HTML format) with instructions on how to create a formatting string
	 * for a text field. 
	 */
	public String getHelpHtml()
	{
		return getResourceString("FFE_HELP_TEXT_HTML");
	}

}

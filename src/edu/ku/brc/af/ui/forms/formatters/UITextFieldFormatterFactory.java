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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter.FormatterType;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter.PartialDateEnum;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField.FieldType;

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

		AutoNumberIFace autoNumber = UIFieldFormatterMgr.createAutoNumber("edu.ku.brc.dbsupport.AutoNumberGeneric", 
				                                                          clazz.getName(), 
				                                                          fieldInfo.getName());
		fmt.setAutoNumber(autoNumber);

		// separators and split pattern strings
		Pattern splitPattern = Pattern.compile("([\\/\\-\\_ ])+");
		Matcher matcher = splitPattern.matcher(formattingString);

		// Find all the separator matches and create individual fields by
		// calling formatter field factory
		UIFieldFormatterField field;
		String fieldString = "";
		int begin = 0;
		while (matcher.find())
		{
			// create a field with what's before the current separator
			fieldString = formattingString.substring(begin, matcher.start());
			field = UIFieldFormatterField.factory(fieldString);
			fmt.addField(field);
			begin = matcher.end();

			// create separator field
			String value = matcher.group();
			field = new UIFieldFormatterField(FieldType.separator, value
					.length(), value, false);
			fmt.addField(field);
		}

		// create last bit of formatter
		fieldString = formattingString.substring(begin);
		field = UIFieldFormatterField.factory(fieldString);
		fmt.addField(field);

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

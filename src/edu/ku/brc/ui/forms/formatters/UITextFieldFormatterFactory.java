package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ku.brc.dbsupport.AutoNumberIFace;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatter.FormatterType;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatter.PartialDateEnum;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterField.FieldType;

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
		UIFieldFormatter fmt = new UIFieldFormatter(null, false, fieldInfo.getName(), 
				FormatterType.generic, PartialDateEnum.None, clazz, false, false, null);

		AutoNumberIFace autoNumber = UIFieldFormatterMgr.createAutoNumber("edu.ku.brc.dbsupport.AutoNumberGeneric", 
				clazz.getName(), fieldInfo.getName());
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

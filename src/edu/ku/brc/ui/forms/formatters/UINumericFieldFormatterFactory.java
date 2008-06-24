package edu.ku.brc.ui.forms.formatters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ku.brc.dbsupport.AutoNumberIFace;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatter.FormatterType;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatter.PartialDateEnum;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterField.FieldType;

public abstract class UINumericFieldFormatterFactory extends UIFieldFormatterFactory
{
	/**
	 * Constructor
	 */
	public UINumericFieldFormatterFactory(DBFieldInfo fieldInfo)
	{
		super(fieldInfo);
	}

	//@Override
	public UIFieldFormatter createFormat(String formattingString)
			throws UIFieldFormatterParsingException
	{
		Class<?> clazz = fieldInfo.getTableInfo().getClassObj();
		UIFieldFormatter fmt = new UIFieldFormatter(null, false, fieldInfo.getName(), 
				FormatterType.numeric, PartialDateEnum.None, clazz, false, false, null);

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

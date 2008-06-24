package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import edu.ku.brc.dbsupport.DBFieldInfo;

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

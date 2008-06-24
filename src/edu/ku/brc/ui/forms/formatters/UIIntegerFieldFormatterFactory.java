package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import edu.ku.brc.dbsupport.DBFieldInfo;

/**
 * This class is used to build UIFieldFormatters from a formatting string for integer numeric fields 
 * 
 * @author Ricardo
 *
 */
public class UIIntegerFieldFormatterFactory extends UINumericFieldFormatterFactory
{
	/**
	 * Constructor
	 */
	public UIIntegerFieldFormatterFactory(DBFieldInfo fieldInfo)
	{
		super(fieldInfo);
	}

	//@Override
	protected String getRegEx()
	{
		return "^(N+)$";
	}

	//@Override
	public String getHelpHtml()
	{
		return getResourceString("FFE_HELP_INTEGER_HTML");
	}

}

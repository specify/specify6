/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author Administrator
 *
 *Formats dates for export to MySQL
 */
public class DateExportFormatter extends ExportFieldFormatter
{
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#formatToUI(java.lang.Object[])
	 */
	@Override
	public Object formatToUI(Object... data)
	{
		if (data[0] == null)
		{
			return null;
		}

		
		UIFieldFormatterIFace.PartialDateEnum datePrec = UIFieldFormatterIFace.PartialDateEnum.Full;
		if (data.length > 1 && data[1] instanceof Byte)
		{
			datePrec = UIFieldFormatterIFace.PartialDateEnum.values()[(Byte)data[1]];
		}
		
		Calendar calendar;
		if (data[0] instanceof Calendar)
		{
			calendar = (Calendar )data[0];
		}
		else
		{
			calendar = new GregorianCalendar();
			calendar.setTime((java.util.Date )data[0]);
		}
		
		String result = String.valueOf(calendar.get(Calendar.YEAR));
		if (datePrec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
		{
			result += "-00-00";
		} else
		{
			result += "-" + (calendar.get(Calendar.MONTH)+1);
			if (datePrec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
			{
				result += "-00";
			} else
			{
				result += "-" + calendar.get(Calendar.DAY_OF_MONTH);
			}
		}
		return result;
		
		//return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
	}
}

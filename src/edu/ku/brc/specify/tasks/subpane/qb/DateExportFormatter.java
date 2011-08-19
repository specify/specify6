/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
		return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
	}
}

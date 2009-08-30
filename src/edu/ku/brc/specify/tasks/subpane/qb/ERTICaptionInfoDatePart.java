/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Calendar;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.PartialDateEnum;
import edu.ku.brc.specify.tasks.subpane.qb.DateAccessorQRI.DATEPART;

/**
 * @author timo
 *
 */
public class ERTICaptionInfoDatePart extends ERTICaptionInfoQB
{

	protected final int fieldNumber;
	protected final boolean zeroBase;

    /**
     * @param colName
     * @param colLabel
     * @param colStringId
     * @param datePart
     * @param fieldInfo
     */
    public ERTICaptionInfoDatePart(String  colName, 
            String  colLabel, 
            String colStringId,
            DATEPART datePart,
            DBFieldInfo fieldInfo)
    {
    	super(colName, colLabel, true, null, 0, colStringId, null, fieldInfo);
    	switch (datePart) {
    		case NumericDay: fieldNumber = Calendar.DAY_OF_MONTH; break;
    		case NumericMonth: fieldNumber = Calendar.MONTH; break;
    		case NumericYear: fieldNumber = Calendar.YEAR; break;
    		default: fieldNumber = Calendar.YEAR;
    	}
    	zeroBase = fieldNumber == Calendar.MONTH;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.ERTICaptionInfoQB#processValue(java.lang.Object)
	 */
	@Override
	public Object processValue(Object value) {
		Calendar calVal = null;
		if (value instanceof Calendar)
		{
			calVal = (Calendar )value;
		}
		else if (value instanceof Object[])
		{
			Object[] arrayVal = (Object[] )value;
			if (arrayVal.length == 2 && arrayVal[0] instanceof Calendar)
			{
				calVal = (Calendar )arrayVal[0];
				Byte precision = (Byte )arrayVal[1];
				if (precision.intValue() == PartialDateEnum.Month.ordinal() && fieldNumber == Calendar.DAY_OF_MONTH)
				{
					return null;
				}
				if (precision.intValue() == PartialDateEnum.Year.ordinal() && (fieldNumber == Calendar.DAY_OF_MONTH || fieldNumber == Calendar.MONTH))
				{
					return null;
				}
			}
		}
		if (calVal != null)
		{
			int result = calVal.get(fieldNumber);
			if (zeroBase)
			{
				result++;
			}
			return result;
		}
		return super.processValue(value);
	}

    
}

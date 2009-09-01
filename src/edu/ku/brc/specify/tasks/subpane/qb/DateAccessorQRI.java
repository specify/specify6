/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.PartialDateEnum;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timo
 *
 */
public class DateAccessorQRI extends FieldQRI 
{
	public enum DATEPART{NumericDay, NumericMonth, NumericYear};
	
	protected final DATEPART datePart;
	
	public DateAccessorQRI(final TableQRI table, final DBFieldInfo fi, DATEPART datePart)
	{
		super(table, fi);
		this.datePart = datePart;
		title += " (" + UIRegistry.getResourceString("DateAccessorQRI." + datePart.name()) + ")";
	}

	/**
	 * @return the datePart
	 */
	public DATEPART getDatePart() 
	{
		return datePart;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#addPartialDateColumn(boolean, boolean)
	 */
	@Override
	protected boolean addPartialDateColumn(boolean forWhereClause,
			boolean forSchemaExport) 
	{
		return false;
	}

	@Override
	public String getStringId() {
		return super.getStringId() + datePart.name();
	}

	@Override
	public String getSQLFldSpec(TableAbbreviator ta, boolean forWhereClause,
			boolean forSchemaExport) {
        String fldExpr = ta.getAbbreviation(table.getTableTree()) + "." + getFieldName();
        
        String validPartialDates = null;
        String sqlFunction = null;
        switch (datePart) {
        	case NumericDay: 
        		sqlFunction = "DAY"; 
        		validPartialDates = "(" + String.valueOf(PartialDateEnum.Full.ordinal()) + ")";
        		break;
        	case NumericMonth: 
        		sqlFunction = "MONTH"; 
        		validPartialDates = "(" + String.valueOf(PartialDateEnum.Full.ordinal()) + ", " +
        			String.valueOf(PartialDateEnum.Month.ordinal()) + ")";
        		break;
        	case NumericYear: 
        		sqlFunction = "YEAR"; 
        		validPartialDates = "(" + String.valueOf(PartialDateEnum.Full.ordinal()) + ", " +
    				String.valueOf(PartialDateEnum.Month.ordinal()) + ", " +
    				String.valueOf(PartialDateEnum.Year.ordinal())+ ")";
        		break;
        }
        if (!forWhereClause)
        {
        	String partialDateExpr = ta.getAbbreviation(table.getTableTree()) + "." + getFieldInfo().getDatePrecisionName();
        	return "CASE WHEN " + partialDateExpr + " IN" + validPartialDates + " THEN " + sqlFunction + "(" + fldExpr
        		+ ") ELSE null END";
        }
        return sqlFunction + "(" + fldExpr + ")";
        
	}
	
	
}

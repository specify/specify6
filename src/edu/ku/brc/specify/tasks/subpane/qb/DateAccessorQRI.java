/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.PartialDateEnum;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
			boolean forSchemaExport, String formatName) {
        String fldExpr = ta.getAbbreviation(table.getTableTree()) + "." + getFieldName();
        Pair<String, String> specs = getSpecInfo();
        if (!forWhereClause) {
        	if (getFieldInfo().getDatePrecisionName() != null) {
        		String partialDateExpr = ta.getAbbreviation(table.getTableTree()) + "." + getFieldInfo().getDatePrecisionName();
        		return "CASE WHEN " + partialDateExpr + " IN" + specs.getSecond() + " THEN " + specs.getFirst() + "(" + fldExpr
        			+ ") ELSE null END";
        	}
        }
        return specs.getFirst() + "(" + fldExpr + ")";
	}

	private Pair<String, String> getSpecInfo() {
		Pair<String, String> result = new Pair<>(null, null);
		switch (datePart) {
			case NumericDay:
				result.setFirst("DAY");
				result.setSecond("(" + String.valueOf(PartialDateEnum.Full.ordinal()) + ")");
				break;
			case NumericMonth:
				result.setFirst("MONTH");
				result.setSecond("(" + String.valueOf(PartialDateEnum.Full.ordinal()) + ", " +
						String.valueOf(PartialDateEnum.Month.ordinal()) + ")");
				break;
			case NumericYear:
				result.setFirst("YEAR");
				result.setSecond("(" + String.valueOf(PartialDateEnum.Full.ordinal()) + ", " +
						String.valueOf(PartialDateEnum.Month.ordinal()) + ", " +
						String.valueOf(PartialDateEnum.Year.ordinal())+ ")");
				break;
		}
		return result;
	}

	@Override
	public String getNullCondition(TableAbbreviator ta, boolean forSchemaExport, boolean negate, String formatName) {
		Pair<String, String> specs = getSpecInfo();
		String fldExpr = specs.getFirst() + "(" + ta.getAbbreviation(table.getTableTree()) + "." + getFieldName() + ")";
		String partialDateExpr = ta.getAbbreviation(table.getTableTree()) + "." + getFieldInfo().getDatePrecisionName();
		if (negate) {
			return "(" + fldExpr + " is not null and not (" + fldExpr + "=1 and " + partialDateExpr + " not in" + specs.getSecond() + "))";
		} else {
			return "(" + fldExpr + " is null or (" + fldExpr + "=1 and " + partialDateExpr + " not in" + specs.getSecond() + "))";
		}
	}

	public String refineCriteria(TableAbbreviator ta, Boolean isNegated, String baseCriteria) {
		if (isNegated) {
			return "(" + baseCriteria + " or " + getNullCondition(ta, false, false, null) + ")";
		} else {
			return "(" + baseCriteria + " and " + getSQLFldSpec(ta, false, false, null) + " is not null)";
		}
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getDataClass()
	 */
	@Override
	public Class<?> getDataClass() {
		return Integer.class;
	}
	
	
}

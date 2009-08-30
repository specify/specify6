/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.af.core.db.DBFieldInfo;
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
		return true;
	}

	@Override
	public String getStringId() {
		return super.getStringId() + datePart.name();
	}
	
	
}

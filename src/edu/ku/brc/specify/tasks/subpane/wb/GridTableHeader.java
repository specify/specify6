/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

/**
 * @author ben
 *
 */
public interface GridTableHeader extends Comparable<GridTableHeader>
{

	Short getViewOrder();

	String getCaption();

	Short getDataFieldLength();

	Class<?> getDataType();

	String getFieldName();

	String getTableName();

}

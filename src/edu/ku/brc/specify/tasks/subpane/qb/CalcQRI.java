package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.ui.UIRegistry;

public class CalcQRI extends FieldQRI {
    protected String field;

    /**
     *
     * @param table
     * @param field
     */
    public CalcQRI(final TableQRI table, final String field) {
        super(table, null);
        this.field = field;
        title = UIRegistry.getResourceString(table.getTableInfo().getName() + "." + field);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getFieldName()
     */
    @Override
    public String getFieldName() {
        return field;
    }

    @Override
    public boolean isFieldHidden() {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getSQLFldSpec(edu.ku.brc.specify.tasks.subpane.qb.TableAbbreviator)
     */
    @Override
    public String getSQLFldSpec(TableAbbreviator ta, final boolean forWhereClause,
                                final boolean forSchemaExport, final String formatName, boolean formatAuditRecIds) {
        return ta.getAbbreviation(table.getTableTree()) + "." + getTableInfo().getIdFieldName();
    }
}

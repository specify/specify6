/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import javax.swing.JButton;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public interface QueryFieldPanelContainerIFace
{
    /**
     * @return the columnDefStr
     */
    String getColumnDefStr();
    /**
     * @param columnDefStr
     */
    void setColumnDefStr(final String columnDefStr);
    /**
     * @return Add Button control if it exists.
     */
    JButton getAddBtn();
    /**
     * @param qfp The panel to select.
     */
    void selectQFP(final QueryFieldPanel qfp);
    /**
     * @param qfp Then panel to remove.
     */
    void removeQueryFieldItem(final QueryFieldPanel qfp);
    /**
     * @return the number fields in the query.
     */
    int getFields();
    /**
     * @param index
     * @return field with index.
     */
    QueryFieldPanel getField(int index);
}

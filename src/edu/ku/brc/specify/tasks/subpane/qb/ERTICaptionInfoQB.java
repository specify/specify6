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

import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ERTICaptionInfoQB extends ERTICaptionInfo
{
    /**
     * A unique identifier for the column within the QB query which is independent of the column's caption.
     */
    protected final String colStringId;
    
    public ERTICaptionInfoQB(String  colName, 
                           String  colLabel, 
                           boolean isVisible, 
                           UIFieldFormatterIFace uiFieldFormatter,
                           int     posIndex,
                           String colStringId)
    {
        super(colName, colLabel, isVisible, uiFieldFormatter, posIndex);
        this.colStringId = colStringId;
    }

    /**
     * @return the colStringId;
     */
    public String getColStringId()
    {
        return colStringId;
    }
}

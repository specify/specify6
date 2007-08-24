/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.dbsupport;

import java.util.Set;

import javax.swing.ImageIcon;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public interface RecordSetIFace
{

    public abstract Integer getRecordSetId();

    public abstract void setRecordSetId(Integer recordSetId);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Integer getDbTableId();

    public abstract void setDbTableId(Integer tableId);

    public abstract Set<RecordSetItemIFace> getItems();

    public abstract void setItems(Set<RecordSetItemIFace> items);

    public abstract String getRemarks();

    public abstract void setRemarks(String remarks);

    public abstract RecordSetItemIFace addItem(final Integer recordId);

    public abstract RecordSetItemIFace addItem(final String recordId);

    public abstract RecordSetItemIFace addItem(final RecordSetItemIFace item);

    public abstract ImageIcon getDataSpecificIcon();

    public abstract void setDataSpecificIcon(ImageIcon dataSpecificIcon);

    public abstract int getTableId();
    
    public abstract RecordSetItemIFace getOnlyItem();

}
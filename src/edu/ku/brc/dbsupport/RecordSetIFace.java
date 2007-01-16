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

    public abstract Long getRecordSetId();

    public abstract void setRecordSetId(Long recordSetId);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Integer getDbTableId();

    public abstract void setDbTableId(Integer tableId);

    public abstract Set<RecordSetItemIFace> getItems();

    public abstract void setItems(Set<RecordSetItemIFace> items);

    public abstract String getRemarks();

    public abstract void setRemarks(String remarks);

    public abstract RecordSetItemIFace addItem(final Long recordId);

    public abstract RecordSetItemIFace addItem(final String recordId);

    public abstract RecordSetItemIFace addItem(final RecordSetItemIFace item);

    public abstract ImageIcon getDataSpecificIcon();

    public abstract void setDataSpecificIcon(ImageIcon dataSpecificIcon);

    public abstract Integer getTableId();

}
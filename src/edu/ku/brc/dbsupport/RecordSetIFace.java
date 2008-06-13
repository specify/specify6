/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.dbsupport;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

/**
 * @author rods
 *
 * @code_status Beta
 *
 */
public interface RecordSetIFace
{
    /**
     * @return
     */
    public abstract Integer getRecordSetId();

    /**
     * @param recordSetId
     */
    public abstract void setRecordSetId(Integer recordSetId);

    /**
     * @return
     */
    public abstract String getName();

    /**
     * @param name
     */
    public abstract void setName(String name);

    /**
     * @return
     */
    public abstract Integer getDbTableId();

    /**
     * @param tableId
     */
    public abstract void setDbTableId(Integer tableId);

    /**
     * @return
     */
    public abstract Set<RecordSetItemIFace> getItems();
    
    /**
     * Returns a list of items in the order in which they were added before persistence.
     * This is mostly useful for memory based RecordSets. Once the RS is saved or if it was retrieved 
     * from the database the order is whatever Hibernate has determined.
     * 
     * @return returns a list of items.
     */
    public abstract List<RecordSetItemIFace> getOrderedItems();

    /**
     * @param items
     */
    public abstract void setItems(Set<RecordSetItemIFace> items);

    /**
     * @return
     */
    public abstract String getRemarks();

    /**
     * @param remarks
     */
    public abstract void setRemarks(String remarks);
    
    /**
     * @return
     */
    public abstract int getNumItems();

    /**
     * @param recordId
     * @return
     */
    public abstract RecordSetItemIFace addItem(final Integer recordId);

    /**
     * @param recordId
     * @return
     */
    public abstract RecordSetItemIFace addItem(final String recordId);

    /**
     * @param item
     * @return
     */
    public abstract RecordSetItemIFace addItem(final RecordSetItemIFace item);
    
    /**
     * @param rsi
     */
    public abstract void removeItem(final RecordSetItemIFace rsi);
    
    /**
     * 
     */
    public abstract void clearItems();
    
    /**
     * @param list
     */
    public abstract void addAll(Collection<RecordSetItemIFace> list);

    /**
     * @return
     */
    public abstract ImageIcon getDataSpecificIcon();

    /**
     * @param dataSpecificIcon
     */
    public abstract void setDataSpecificIcon(ImageIcon dataSpecificIcon);

    /**
     * @return
     */
    public abstract int getTableId();
    
    /**
     * @return
     */
    public abstract RecordSetItemIFace getOnlyItem();
    
    /**
     * @return
     */
    public abstract Class<?> getDataClassFormItems();

}
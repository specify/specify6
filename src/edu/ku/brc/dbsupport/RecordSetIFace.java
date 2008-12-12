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
     * @return the name
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
     * Sets the name.
     * @param name the name
     */
    public abstract void setName(String name);

    /**
     * @return the Table ID of the items in the RecordSet
     */
    public abstract Integer getDbTableId();

    /**
     * @param tableId the ID of the type of items in the RecordSet
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
     * @return the remarks
     */
    public abstract String getRemarks();

    /**
     * Sets the remarks
     * @param remarks the remarks text
     */
    public abstract void setRemarks(String remarks);
    
    /**
     * @return the number of RecordSetItems
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
     * Clear all the items in the RecordSet
     */
    public abstract void clearItems();
    
    /**
     * Add all the items in the list to this RecordSet
     * @param list the items to be added
     */
    public abstract void addAll(Collection<RecordSetItemIFace> list);

    /**
     * @return An Icon that represents the data that is held in the RecordSet
     */
    public abstract ImageIcon getDataSpecificIcon();

    /**
     * @param dataSpecificIcon
     */
    public abstract void setDataSpecificIcon(ImageIcon dataSpecificIcon);

    /**
     * @return The table ID for the RecordSet class
     */
    public abstract int getTableId();
    
    /**
     * @return the first and only item
     */
    public abstract RecordSetItemIFace getOnlyItem();
    
    /**
     * @return
     */
    public abstract Class<?> getDataClassFormItems();
    
    /**
     * @param type
     */
    public abstract void setType(Byte type);

}
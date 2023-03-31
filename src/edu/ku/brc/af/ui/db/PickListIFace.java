/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.ui.db;

import java.sql.Timestamp;
import java.util.Set;

/**
 * Represents a PickList
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Nov 10, 2006
 *
 */
public interface PickListIFace
{
    public static final byte PL_WITH_ITEMS  = 0;
    public static final byte PL_WHOLE_TABLE = 1;
    public static final byte PL_TABLE_FIELD = 2;
    
    public static final byte PL_NO_SORT      = 0;
    public static final byte PL_TITLE_SORT   = 1;
    public static final byte PL_ORDINAL_SORT = 2;
    
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
    public abstract String getFieldName();

    /**
     * @param fieldName
     */
    public abstract void setFieldName(String fieldName);

    /**
     * @return
     */
    public abstract String getTableName();

    /**
     * @param tableName
     */
    public abstract void setTableName(String tableName);

    /**
     * @return
     */
    public abstract String getFormatter();

    /**
     * @param formatter
     */
    public abstract void setFormatter(String formatter);

    /**
     * @return
     */
    public abstract Byte getType();

    /**
     * @param type
     */
    public abstract void setType(Byte type);

    /**
     * @return
     */
    public abstract Boolean getReadOnly();

    /**
     * @param readOnly
     */
    public abstract void setReadOnly(Boolean readOnly);
    
    /**
     * @param filterName
     */
    public abstract void setFilterFieldName(String filterName);
    
    /**
     * @return
     */
    public abstract String getFilterFieldName();
    
    /**
     * @param filterName
     */
    public abstract void setFilterValue(String filterName);
    
    /**
     * @return
     */
    public abstract String getFilterValue();

    /**
     * @return
     */
    public abstract Integer getSizeLimit();

    /**
     * @param sizeLimit
     */
    public abstract void setSizeLimit(Integer sizeLimit);

    /**
     * @return
     */
    public abstract Timestamp getTimestampCreated();

    /**
     * @param created
     */
    public abstract void setTimestampCreated(Timestamp created);
    
    /**
     * @return
     */
    public abstract int getNumItems();

    /**
     * @return
     */
    public abstract Set<PickListItemIFace> getItems();

    /**
     * @param title
     * @param value
     * @return
     */
    public abstract PickListItemIFace addItem(final String title, final String value);

    /**
     * @param title
     * @param value
     * @return
     */
    public abstract PickListItemIFace addItem(final String title, final Object value);

    /**
     * @param item
     * @return
     */
    public abstract PickListItemIFace addItem(final PickListItemIFace item);
    
    /**
     * @param item
     */
    public abstract void removeItem(final PickListItemIFace item);
    
    /**
     * Reorders list if it is orderable.
     */
    public abstract void reorder();
    
    /**
     * @return whether the PickList is a System PickList
     */
    public abstract boolean isSystem();
    
}

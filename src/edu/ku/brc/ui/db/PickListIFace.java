/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.ui.db;

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
    public abstract Integer getType();

    /**
     * @param type
     */
    public abstract void setType(Integer type);

    /**
     * @return
     */
    public abstract Boolean getReadOnly();

    /**
     * @param readOnly
     */
    public abstract void setReadOnly(Boolean readOnly);

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

}

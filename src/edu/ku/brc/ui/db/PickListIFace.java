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
 * @code_status Alpha
 *
 * Created Date: Nov 10, 2006
 *
 */
public interface PickListIFace
{
    public abstract String getName();

    public abstract void setName(String name);
    
    public abstract String getFieldName();

    public abstract void setFieldName(String fieldName);

    public abstract String getTableName();

    public abstract void setTableName(String tableName);

    public abstract String getFormatter();

    public abstract void setFormatter(String formatter);

    public abstract Integer getType();

    public abstract void setType(Integer type);

    public abstract Boolean getReadOnly();

    public abstract void setReadOnly(Boolean readOnly);

    public abstract Integer getSizeLimit();

    public abstract void setSizeLimit(Integer sizeLimit);

    public abstract Timestamp getTimestampCreated();

    public abstract void setTimestampCreated(Timestamp created);
    
    public abstract int getNumItems();

    public abstract Set<PickListItemIFace> getItems();

    public abstract PickListItemIFace addItem(final String title, final String value);

    public abstract PickListItemIFace addItem(final String title, final Object value);

    public abstract PickListItemIFace addItem(final PickListItemIFace item);
    
    public abstract void removeItem(final PickListItemIFace item);

}

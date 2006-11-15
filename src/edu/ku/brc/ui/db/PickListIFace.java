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

import java.util.Date;
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
    public String getName();

    public void setName(String name);
    
    public String getFieldName();

    public void setFieldName(String fieldName);

    public String getTableName();

    public void setTableName(String tableName);

    public String getFormatter();

    public void setFormatter(String formatter);

    public Integer getType();

    public void setType(Integer type);

    public Boolean getReadOnly();

    public void setReadOnly(Boolean readOnly);

    public Integer getSizeLimit();

    public void setSizeLimit(Integer sizeLimit);

    public Date getTimestampCreated();

    public void setTimestampCreated(Date created);

    public Set<PickListItemIFace> getItems();

    public void setItems(Set<PickListItemIFace> items);

    public PickListItemIFace addPickListItem(final String title, final String value);

    public PickListItemIFace addPickListItem(final String title, final Object value);

    public PickListItemIFace addPickListItem(final PickListItemIFace item);

    public void removePickListItem(final PickListItemIFace item);


}

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
package edu.ku.brc.af.ui.db;

import java.sql.Timestamp;

/**
 * Represents a PickListItem.
 * 
 * Created Date: Nov 10, 2006
 *
 * @author rods
 * @code_status Beta
 */
public interface PickListItemIFace extends Comparable<PickListItemIFace>
{
    public abstract Integer getId();
    
	public abstract void setPickList(PickListIFace pickList);
	
	public abstract PickListIFace getPickList();
	
    public abstract String getTitle();

    public abstract void setTitle(String title);

    public abstract String getValue();

    public abstract void setValue(String value);
    
    public abstract Timestamp getTimestampCreated();

    public abstract void setTimestampCreated(Timestamp createdDate);
    
    // Non-Presisted Field
    
    public abstract Object getValueObject();

    public abstract void setValueObject(Object valueObject);
    
}

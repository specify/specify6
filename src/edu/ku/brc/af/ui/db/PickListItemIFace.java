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
    /**
     * @return
     */
    public abstract Integer getId();
    
	/**
	 * @param pickList
	 */
	public abstract void setPickList(PickListIFace pickList);
	
	/**
	 * @return
	 */
	public abstract PickListIFace getPickList();
	
    /**
     * @return
     */
    public abstract String getTitle();

    /**
     * @param title
     */
    public abstract void setTitle(String title);

    /**
     * @return
     */
    public abstract String getValue();

    /**
     * @param value
     */
    public abstract void setValue(String value);
    
    /**
     * @return
     */
    public abstract Timestamp getTimestampCreated();

    /**
     * @param createdDate
     */
    public abstract void setTimestampCreated(Timestamp createdDate);
    
    /**
     * @param ordinal
     */
    public abstract void setOrdinal(Integer ordinal);
    
    /**
     * @return
     */
    public abstract Integer getOrdinal();
    
    /**
     * @param pickList
     */
    public abstract void SetPickList(PickListIFace pickList);
    
    // Non-Presisted Field
    
    /**
     * @return
     */
    public abstract Object getValueObject();

    /**
     * @param valueObject
     */
    public abstract void setValueObject(Object valueObject);
    
}

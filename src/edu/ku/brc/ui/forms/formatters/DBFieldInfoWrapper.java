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

package edu.ku.brc.ui.forms.formatters;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableInfo;

/*
 * Wrapper for db fields. 
 * Ceated to modify toString() method and display item nicely on JList 
 */
public class DBFieldInfoWrapper
{
	protected DBRelationshipInfo relationshipInfo = null;
	protected DBTableInfo        tableInfo        = null;
	protected DBFieldInfo		 fieldInfo        = null;
	protected boolean 			 usePrefix        = false;
	
	DBFieldInfoWrapper(DBRelationshipInfo relationshipInfo,
			           DBTableInfo        tableInfo,
			           DBFieldInfo		  fieldInfo,
			           boolean 			  usePrefix)
	{
		this.relationshipInfo = relationshipInfo;
		this.tableInfo        = tableInfo;
		this.fieldInfo        = fieldInfo;
		this.usePrefix		  = usePrefix;
	}

	public DBRelationshipInfo getRelationshipInfo() 
	{
		return relationshipInfo;
	}

	public DBTableInfo getTableInfo() 
	{
		return tableInfo;
	}

	public DBFieldInfo getFieldInfo() 
	{
		return fieldInfo;
	}
	
	public boolean usePrefix() {
		return usePrefix;
	}

	public String toString()
	{
		return DBFieldInfoWrapper.toString(this);
	}

	public static String toString(DBFieldInfoWrapper wrapper)
	{
		String prefix = "";
		if (wrapper.usePrefix())
		{
			prefix = (wrapper.getRelationshipInfo() != null)? 
					wrapper.getRelationshipInfo().getTitle() : wrapper.getTableInfo().getTitle();    
			prefix = prefix + ".";
		}
		return prefix + wrapper.getFieldInfo().getTitle();
	}
}

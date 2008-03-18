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

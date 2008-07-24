package edu.ku.brc.specify.tasks.subpane.security;

import javax.swing.ImageIcon;

import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.af.auth.specify.principal.UserPrincipal;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.IconManager;

/**
 * Wraps a data model object that will be shown on the navigation tree. 
 * 
 * @author Ricardo
 *
 */
public class DataModelObjBaseWrapper 
{
	private DataModelObjBase dataObj;
	private ImageIcon icon;
	
	/**
	 * Constructor
	 * @param dataObj data object to be wrapped
	 */
	public DataModelObjBaseWrapper(DataModelObjBase dataObj)
	{
		this.dataObj = dataObj;

		// get icon
		DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(dataObj.getTableId());

		if (dataObj instanceof SpPrincipal)
		{
			icon = IconManager.getIcon("group", IconManager.IconSize.Std16);
		}
		else if (dataObj instanceof SpecifyUser)
		{
			icon = IconManager.getIcon("person", IconManager.IconSize.Std16);
		}
		else if (dataObj instanceof Discipline)
		{
			// try and get the discipline icon
			String iconName = "";
			Discipline discipline = (Discipline) dataObj;
			if      (discipline.getName().equals("fish"))			iconName = "Fish";
			else if (discipline.getName().equals("amphibian")) 		iconName = "Frogs";
			else if (discipline.getName().equals("reptile")) 		iconName = "Snake";
			else if (discipline.getName().equals("paleobotany")) 	iconName = "PaleoBotany";
			else if (discipline.getName().equals("invertpaleo")) 	iconName = "InvertPaleo";
			else if (discipline.getName().equals("vertpaleo")) 		iconName = "VertPaleo";
			else if (discipline.getName().equals("bird")) 			iconName = "Bird";
			else if (discipline.getName().equals("mammal")) 		iconName = "Mammal";
			else if (discipline.getName().equals("insect")) 		iconName = "Bug";
			else if (discipline.getName().equals("botany")) 		iconName = "Plants";
			else if (discipline.getName().equals("invertebrate")) 	iconName = "Spider";
			//else if (discipline.getName().equals("minerals")) 		iconName = "";
			//else if (discipline.getName().equals("fungi"))			iconName = "";
			//else if (discipline.getName().equals("anthropology"))	iconName = "";
			else iconName = "Discipline";

			icon = IconManager.getIcon(iconName, IconManager.IconSize.Std16);
		}
		else 
		{
			if (tableInfo == null)
			{
				// FIXME: SpPrincipal table isn't registered with DBTableIdMgr
				return;
			}
		
			String iconName = tableInfo.getClassObj().getSimpleName();
			icon = IconManager.getIcon(iconName, IconManager.IconSize.Std16);
		}
	}
	
	/**
	 * Returns wrapped data object
	 * @return Wrapped data object
	 */
	public DataModelObjBase getDataObj()
	{
		return dataObj;
	}
	
	/**
	 * 
	 * @return
	 */
	public ImageIcon getIcon()
	{
		return icon;
	}

	/**
	 * 
	 * @return
	 */
	public String getName()
	{
		return dataObj.getIdentityTitle();
	}

	/**
	 * Helper method that checks whether wrapped object is an institution object
	 * @return whether wrapped object is an institution object
	 */
	public boolean isInstitution()
	{
		return (dataObj.getClass().equals(Institution.class));
	}
	
	/**
	 * Helper method that checks whether wrapped object is an division object
	 * @return whether wrapped object is an division object
	 */
	public boolean isDivision()
	{
		return (dataObj.getClass().equals(Division.class));
	}
	
	/**
	 * Helper method that checks whether wrapped object is an discipline object
	 * @return whether wrapped object is an discipline object
	 */
	public boolean isDiscipline()
	{
		return (dataObj.getClass().equals(Discipline.class));
	}
	
	/**
	 * Helper method that checks whether wrapped object is an collection object
	 * @return whether wrapped object is an collection object
	 */
	public boolean isCollection()
	{
		return (dataObj.getClass().equals(Collection.class));
	}
	
	/**
	 * Helper method that checks whether wrapped object is an group object
	 * @return whether wrapped object is an group object
	 */
	public boolean isGroup()
	{
		if (dataObj instanceof SpPrincipal)
		{
			SpPrincipal pc = (SpPrincipal) dataObj;
			return pc.getGroupSubClass().equals(GroupPrincipal.class.getCanonicalName());
		}
		
		return (dataObj.getClass().equals(GroupPrincipal.class));
	}
	
	/**
	 * Helper method that checks whether wrapped object is an user object
	 * @return whether wrapped object is an user object
	 */
	public boolean isUser()
	{
		return (dataObj.getClass().equals(UserPrincipal.class) ||
				dataObj.getClass().equals(SpecifyUser.class) );
	}
	
	/**
	 * Returns the class name of the wrapped object 
	 * @return The class name of the wrapped object
	 */
	public String getType()
	{
		return dataObj.getClass().getCanonicalName();
	}
	
	/**
	 * Returns the string representation of the wrapped object
	 * This string is what is shown as label for the the navigation tree nodes 
	 */
	public String toString()
	{
		return dataObj.getIdentityTitle();
	}
}

package edu.ku.brc.specify.datamodel;

import java.util.Set;

import edu.ku.brc.util.Nameable;
import edu.ku.brc.util.Rankable;

public interface TreeDefinitionItemIface extends Rankable, Nameable
{
	public void initialize();
	
	public Integer getTreeDefItemId();
	public void setTreeDefItemId(Integer id);
	
	public String getRemarks();
	public void setRemarks(String remarks);

	public TreeDefinitionIface getTreeDefinition();
	public void setTreeDefinition(TreeDefinitionIface treeDef);
	
	public TreeDefinitionItemIface getParentItem();
	public void setParentItem(TreeDefinitionItemIface parent);
	
	public TreeDefinitionItemIface getChildItem();
	public void setChildItem(TreeDefinitionItemIface child);
	
	public Boolean getIsEnforced();
	public void setIsEnforced(Boolean isEnforced);

	public Boolean getIsInFullName();
	public void setIsInFullName(Boolean isInFullName);

	public Set getTreeEntries();
	public void setTreeEntries(Set treeables);
	
	public boolean canBeDeleted();
}

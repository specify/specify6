package edu.ku.brc.specify.datamodel;

import java.util.Set;

public interface TreeDefinitionItemIface
{
	public Integer getTreeDefItemId();
	public void setTreeDefItemId(Integer id);
	
	public String getName();
	public void setName(String name);
	
	public Integer getRankId();
	public void setRankId(Integer rank);
	
	public TreeDefinitionIface getTreeDefinition();
	public void setTreeDefinition(TreeDefinitionIface treeDef);
	
	public TreeDefinitionItemIface getParentItem();
	public void setParentItem(TreeDefinitionItemIface parent);
	
	public TreeDefinitionItemIface getChildItem();
	public void setChildItem(TreeDefinitionItemIface child);
	
	public Boolean getIsEnforced();
	public void setIsEnforced(Boolean enforced);
	
	public Set getTreeEntries();
	public void setTreeEntries(Set treeables);
    // Add Methods

    // Done Add Methods
}

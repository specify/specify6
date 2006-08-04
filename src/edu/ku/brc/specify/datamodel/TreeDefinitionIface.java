package edu.ku.brc.specify.datamodel;

import java.util.Set;

import edu.ku.brc.util.Nameable;

public interface TreeDefinitionIface extends Nameable
{
	public void initialize();
	
	public Integer getTreeDefId();
	public void setTreeDefId(Integer id);
	
	public String getRemarks();
	public void setRemarks(String remarks);
	
	public Set getTreeEntries();
	public void setTreeEntries(Set treeEntries);
	
	public Set getTreeDefItems();
	public void setTreeDefItems(Set treeDefItems);
	
	public Class getNodeClass();
	public TreeDefinitionItemIface getDefItemByRank(Integer rank);
	public TreeDefinitionItemIface getDefItemByName(String name);
	public boolean canChildBeReparentedToNode(Treeable child,Treeable newParent);
}

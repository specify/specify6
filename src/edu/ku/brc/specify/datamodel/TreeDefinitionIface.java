package edu.ku.brc.specify.datamodel;

import java.util.Set;

public interface TreeDefinitionIface
{
	/** Old Tree Definition Interface **/
//	public Integer getTreeNodeId();
//	public void setTreeNodeId(Integer id);
//	
//	public String getName();
//	public void setName(String name);
//	
//	public Integer getTreeDefId();
//	public void setTreeDefId(Integer id);
//	
//	public TreeDefinitionIface getParentDef();
//	public void setParentDef(TreeDefinitionIface parent);
	
	/** New Tree Definition Interface **/
	public Integer getTreeDefId();
	public void setTreeDefId(Integer id);
	
	public String getName();
	public void setName(String name);
	
	public String getRemarks();
	public void setRemarks(String remarks);
	
	public Set getTreeEntries();
	public void setTreeEntries(Set treeEntries);
	
	public Set getTreeDefItems();
	public void setTreeDefItems(Set treeDefItems);
}

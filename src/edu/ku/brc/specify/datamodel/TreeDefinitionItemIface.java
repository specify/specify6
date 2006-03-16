package edu.ku.brc.specify.datamodel;

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
}

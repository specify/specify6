package edu.ku.brc.specify.datamodel;

public interface TreeDefinitionIface
{
	public Integer getTreeNodeId();
	public void setTreeNodeId(Integer id);
	
	public String getName();
	public void setName(String name);
	
	public Integer getTreeDefId();
	public void setTreeDefId(Integer id);
	
	public TreeDefinitionIface getParentDef();
	public void setParentDef(TreeDefinitionIface parent);
}

package edu.ku.brc.specify.datamodel;

public interface TreeDefinitionIface
{
	public Integer getTreeDefId();
	public void setTreeDefId(Integer id);
	public String getName();
	public void setName(String name);
	public Integer getTreeNodeId();
	public void setTreeNodeId(Integer id);
	public Integer getParentNodeId();
	public void setParentNodeId(Integer id);
}

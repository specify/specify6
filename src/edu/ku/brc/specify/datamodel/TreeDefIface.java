package edu.ku.brc.specify.datamodel;

import java.util.Set;

import edu.ku.brc.util.Nameable;

public interface TreeDefIface<N,D,I>
	extends Nameable
{
	public void initialize();
	
	public Long getTreeDefId();
	public void setTreeDefId(Long id);
	
	public String getRemarks();
	public void setRemarks(String remarks);
	
	public Set<N> getTreeEntries();
	public void setTreeEntries(Set<N> treeEntries);
	
	public Set<I> getTreeDefItems();
	public void setTreeDefItems(Set<I> treeDefItems);
	
	public Class<N> getNodeClass();
	public I getDefItemByRank(Integer rank);
	public I getDefItemByName(String name);
	public boolean canChildBeReparentedToNode(N child,N newParent);
}

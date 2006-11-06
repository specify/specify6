/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.Set;

import edu.ku.brc.util.Nameable;
import edu.ku.brc.util.Rankable;

public interface TreeDefItemIface<N,D,I> extends Rankable, Nameable
{
	public void initialize();
	
	public Long getTreeDefItemId();
	public void setTreeDefItemId(Long id);
	
	public String getRemarks();
	public void setRemarks(String remarks);

	public D getTreeDef();
	public void setTreeDef(D treeDef);
	
	public I getParent();
	public void setParent(I parent);
	
	public I getChild();
	public void setChild(I child);
	
	public Boolean getIsEnforced();
	public void setIsEnforced(Boolean isEnforced);

	public Boolean getIsInFullName();
	public void setIsInFullName(Boolean isInFullName);

	public Set<N> getTreeEntries();
	public void setTreeEntries(Set<N> treeables);
    
    public String getTextBefore();
    public void setTextBefore(String text);
    
    public String getTextAfter();
    public void setTextAfter(String text);
    
    public String getFullNameSeparator();
    public void setFullNameSeparator(String text);
    
	public boolean canBeDeleted();
}

/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
	
	public boolean canBeDeleted();
}

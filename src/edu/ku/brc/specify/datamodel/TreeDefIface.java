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

public interface TreeDefIface<N,D,I>
	extends Nameable
{
    /**
     * An indicator that node full names should start with highest order
     * nodes and continue to the lowest order nodes.
     * @see #REVERSE
     */
    public static final int FORWARD = 1;
    /**
     * An indicator that node full names should start with lowest order
     * nodes and continue to the highest order nodes.
     * @see #FORWARD
     */
    public static final int REVERSE = -1;

    public void initialize();
	
	public Long getTreeDefId();
	public void setTreeDefId(Long id);
	
	public String getRemarks();
	public void setRemarks(String remarks);
	
	public Set<N> getTreeEntries();
	public void setTreeEntries(Set<N> treeEntries);
	
	public Set<I> getTreeDefItems();
	public void setTreeDefItems(Set<I> treeDefItems);
	
    public Integer getFullNameDirection();
    public void setFullNameDirection(Integer direction);
    
	public Class<N> getNodeClass();
	public I getDefItemByRank(Integer rank);
	public I getDefItemByName(String name);
	public boolean canChildBeReparentedToNode(N child,N newParent);
}

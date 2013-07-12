/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel;

import java.util.Set;

import edu.ku.brc.util.Nameable;
import edu.ku.brc.util.Rankable;

public interface TreeDefItemIface<N extends Treeable<N,D,I>,
                                  D extends TreeDefIface<N,D,I>,
                                  I extends TreeDefItemIface<N,D,I>>
                                    extends Rankable, Nameable
{
	public void initialize();
	
	public Integer getTreeDefItemId();
	public void setTreeDefItemId(Integer id);
	
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
	
	public boolean hasTreeEntries();
    
    public String getTextBefore();
    public void setTextBefore(String text);
    
    public String getTextAfter();
    public void setTextAfter(String text);
    
    public String getFullNameSeparator();
    public void setFullNameSeparator(String text);
    
	public boolean canBeDeleted();
	
	public String getDisplayText();
	public void setDisplayText(String text);
}

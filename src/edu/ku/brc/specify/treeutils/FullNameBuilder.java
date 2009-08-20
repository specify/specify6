/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.treeutils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * @author tnoble
 *
 *Called by FullNameRebuilder to build FullNames during TreeTraversal
 */
public class FullNameBuilder<T extends Treeable<T, D, I>, D extends TreeDefIface<T, D, I>, I extends TreeDefItemIface<T, D, I>> 
	
{
	protected final D treedef;
	protected final SortedSet<FullNameInfo> ranks;
	protected final int reverse;
	protected final FullNameInfo fullNameInfo = new FullNameInfo(null, null, null, -1);
	
	/**
	 * @param treedef
	 */
	public FullNameBuilder(final D treedef)
	{
		this.treedef = treedef;
		reverse = treedef.getFullNameDirection();
		ranks = new TreeSet<FullNameInfo>(new Comparator<FullNameInfo>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(FullNameInfo arg0, FullNameInfo arg1) {
				return reverse * arg0.getRank().compareTo(arg1.getRank());
			}
			
		});		
		for (I rank : treedef.getTreeDefItems())
		{
			if (rank.getIsInFullName())
			{
				ranks.add(new FullNameInfo(rank));
			}
		}
	}
	
	/**
	 * @param rankid
	 * @return true if the full name contains rank.
	 */
	public boolean isInFullName(int rankid)
	{
		fullNameInfo.setRank(rankid);
		return ranks.contains(fullNameInfo);
	}
	
	/**
	 * @param node
	 * @param parents
	 * @return the FullName for node.
	 */
	public String buildFullName(TreeNodeInfo node, LinkedList<TreeNodeInfo> parents)
	{
		StringBuilder sb = new StringBuilder();
		
		
		Iterator<TreeNodeInfo> parentIterator = reverse == TreeDefIface.REVERSE ? parents.descendingIterator()
				: parents.iterator();
		Iterator<FullNameInfo> fullNameIterator = ranks.iterator();
		boolean addedNode = false;
		String nextSeparator = null;
		while (fullNameIterator.hasNext())
		{
			FullNameInfo info = fullNameIterator.next();
			String name = null;
			if (info.getRank() == node.getRank())
			{
				addedNode = true;
				name = node.getName();
			}
			else 
			{
				TreeNodeInfo parentInfo = parentIterator.next();
				if (parentInfo.getRank() == info.getRank())
				{
					name = parentInfo.getName();
				}
			}
			if (name != null)
			{
				if (nextSeparator != null)
				{
					sb.append(nextSeparator);
				}
				if (info.getBefore() != null)
				{
					sb.append(info.getBefore());
				}
				sb.append(name);
				if (info.getAfter() != null)
				{
					sb.append(info.getAfter());
				}
				nextSeparator = info.getSeparator();
			}
		}
		
		if (!addedNode) //in other words, the node's rank is not in the full name
		{
			//XXX Is this right? I think this is what the current full name builder does (TreeHelper.java) but..
			//if sub genus is not in full name but genus then the full name for subgenus will be "GenusName SubgenusName".
			//Don't know if it's right. Don't know if it matters...
			if (reverse == TreeDefIface.REVERSE)
			{
				if (sb.length() > 0)
				{
					sb.insert(0, node.getName() + " ");
				}
				else
				{
					sb.append(node.getName());
				}
			}
			else
			{
				if (nextSeparator != null)
				{
					sb.append(nextSeparator);
				}
				sb.append(node.getName());
			}
		}
		return sb.toString();
	}
	
	/**
	 * @author timo
	 *
	 */
	public class FullNameInfo 
	{
		private final String before;
		private final String after;
		private final String separator;
		private Integer rank;
		
		public FullNameInfo(Integer rank)
		{
			this(null, null, null, rank);
		}
		/**
		 * @param before
		 * @param after
		 * @param rank
		 */
		public FullNameInfo(String before, String after, String separator, Integer rank) 
		{
			super();
			this.before = before;
			this.after = after;
			this.separator = separator;
			this.rank = rank;
		}

		/**
		 * @param rank
		 */
		public FullNameInfo(I rank)
		{
			this(rank.getTextBefore(), rank.getTextAfter(), rank.getFullNameSeparator(), rank.getRankId());
			
		}
		/**
		 * @return the before
		 */
		public String getBefore() 
		{
			return before;
		}

		/**
		 * @return the after
		 */
		public String getAfter() 
		{
			return after;
		}

		/**
		 * @return the separator
		 */
		public String getSeparator()
		{
			return separator;
		}
		/**
		 * @return the rank
		 */
		public Integer getRank() 
		{
			return rank;
		}
		
		/**
		 * @param rank the rank to set
		 */
		public void setRank(Integer rank)
		{
			this.rank = rank;
		}
	}
	
	
}

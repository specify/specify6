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
package edu.ku.brc.specify.tasks;

/**
 * An interface that all 'dual-view' search services must implement.  A dual-view
 * service is one that can display the results in one or both of two separate results
 * areas.
 *
 * @code_status Complete
 * 
 * @author jstewart
 */
public interface DualViewSearchable
{
    /** Display the results in the 'top' results area. */
    public static final int TOPVIEW    = 1;
    /** Display the results in the 'bottom' results area. */
    public static final int BOTTOMVIEW = 2;
    /** Display the results in the both results areas. */
    public static final int BOTHVIEWS  = TOPVIEW ^ BOTTOMVIEW;

	/**
	 * Perform a search for <code>key</code> and display the results
	 * in <code>where</code> using a wrapping search is <code>wrap</code>
	 * is <code>true</code>.
	 *
	 * @param key the key to be searched for
	 * @param where the results pane to use for results
	 * @param wrap whether or not to wrap the search
	 */
	public void find(String key,int where,boolean wrap, boolean isExact);
	/**
	 * Perform a search for <code>key</code> and display the results
	 * in <code>where</code> using a wrapping search is <code>wrap</code>
	 * is <code>true</code>.  If <code>key</code> is the same as in the last
	 * search performed, the next occurance of <code>key</code> should be
	 * returned.  If <code>key</code> is <code>null</code>, the value used in
	 * the previous search should be reused.
	 *
	 * @param key the key to be searched for or <code>null</code>
	 * @param where the results pane to use for results
	 */
	public void findNext(String key,int where,boolean wrap, boolean isExact);
}

/* Copyright (C) 2022, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.util;

/**
 * Defines capabilities needed to name an object.
 *
 * @code_status Complete
 * @author jstewart
 */
public interface Nameable
{
	/**
	 * Gets the name of the called object.
	 * 
	 * @return the name of the called object
	 */
	public String getName();
	
	/**
	 * Sets the name of the called object.
	 * 
	 * @param name the new name of the called object
	 */
	public void setName(String name);
}

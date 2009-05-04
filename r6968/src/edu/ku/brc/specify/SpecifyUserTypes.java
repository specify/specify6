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
package edu.ku.brc.specify;

/**
 * Implements an enum for describing user types.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Feb 16, 2009
 *
 */
public class SpecifyUserTypes
{

    public enum UserType {Manager, FullAccess, LimitedAccess, Guest}
    
    /*public enum UserType 
    {
        Manager(0,       "Manager"),
        FullAccess(1,    "FullAccess"),
        LimitedAccess(2, "LimitedAccess"),
        Guest(3,         "Guest");

        UserType(final int id, final String name)
        {
            this.id   = id;
            this.name = name;
        }

        private int    id;
        private String name;

        public Integer getId()          { return id; }
        public String  getName()        { return name; }
        public String  getDirName()     { return name.toLowerCase(); }
        
        @Override
        public String  toString()       { return name; }
    }*/

}

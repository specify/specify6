/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.core;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 19, 2008
 *
 */
public interface PermissionIFace
{
    /**
     * @return
     */
    public abstract boolean canModify();

    /**
     * @return
     */
    public abstract boolean canView();

    /**
     * @return
     */
    public abstract boolean canAdd();

    /**
     * @return
     */
    public abstract boolean canDelete();
    
    /**
     * @return
     */
    public abstract void setCanModify(boolean value);

    /**
     * @return
     */
    public abstract void setCanView(boolean value);

    /**
     * @return
     */
    public abstract void setCanAdd(boolean value);

    /**
     * @return
     */
    public abstract void setCanDelete(boolean value);

    /**
     * Clears all the permissions.
     */
    public abstract void clear();
    
    /**
     * @return
     */
    public abstract boolean isViewOnly();

    /**
     * @return
     */
    public abstract int getOptions();

    /**
     * @param options
     */
    public abstract void setOptions(int options);

    /**
     * @return
     */
    public abstract boolean hasNoPerm();

}

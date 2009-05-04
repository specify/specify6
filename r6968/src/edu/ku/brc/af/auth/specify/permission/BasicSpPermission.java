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
package edu.ku.brc.af.auth.specify.permission;

import java.security.BasicPermission;
import java.security.Permission;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

/**
 * The BasicSpPermission (Specify Permission) extends BasicPermission to implement
 * permissions for the various Specify objects. An instance of this defines  
 * the permission for a principal (user or user group) to view, modify, add, 
 * or delete a Specify object data object. The property "name" defines the object
 * which permission is being granted to, as well as the type of permission using
 * the following syntax:
 * 
 * <code>permission-type.target</code>
 * 
 * 
 * 
 * Examples of values for the name property are-
 * <ul>
 *   <li>Task.TaxonTreeTask</li>
 *   <li>Task.SecurityAdminTask</li>
 *   <li>Obj.Workbench (permission id = record id)</li>
 * </ul>
 * 
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: nov 15, 2007
 * 
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class BasicSpPermission extends BasicPermission
{
    protected static final Logger log = Logger.getLogger(BasicSpPermission.class);
    private Integer id;
    private String actions;
    public static String view   = "view"; //$NON-NLS-1$
    public static String modify = "modify"; //$NON-NLS-1$
    public static String add    = "add"; //$NON-NLS-1$
    public static String delete = "delete"; //$NON-NLS-1$

    /**
     * @param id
     * @param name
     * @param actions
     */
    public BasicSpPermission(final Integer id, final String name, final String actions)
    {
        super(name, actions);
        this.id = id;
    }
    
    /**
     * @param name
     * @param actions
     */
    public BasicSpPermission(final String name, final String actions)
    {
        super(name);
        this.actions = actions;
    }

    /**
     * @param id
     * @param name
     */
    public BasicSpPermission(final String name)
    {
        this(name, null);
    }

    /**
     * Checks if permission passed as parameter is implied by this permission
     */
    public boolean implies(final Permission p)
    {
		// check implication of name according to BasicPermission rules
    	if (!super.implies(p) || !(p instanceof BasicSpPermission))
    	{
    		// short circuit and return false if p doesn't imply this
    		return false;
    	}

    	// now check if p implies this according to both permissions actions
    	String[] thisActions  = actions.split(",");
    	String[] pActions     = p.getActions().split(",");
    	for (String pAction : pActions)
    	{
    		boolean found = false;
        	for (String action : thisActions)
        	{
        		if (action.equals(pAction))
        		{
        			found = true;
        			break;
        		}
        	}
        	if (!found)
        		return false;
    	}
    	return true;
    }
    
    /* (non-Javadoc)
     * @see java.security.BasicPermission#hashCode()
     */
    public int hashCode()
    {
        final HashCodeBuilder b = new HashCodeBuilder();
        b.append(getName());
        b.append(getActions());
        return b.toHashCode();
    }

    /* (non-Javadoc)
     * @see java.security.BasicPermission#equals(java.lang.Object)
     */
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof BasicSpPermission)) { return false; }

        final BasicSpPermission other = (BasicSpPermission)obj;
        final EqualsBuilder b = new EqualsBuilder();
        b.append(getName(), other.getName());
        b.append(getActions(), other.getActions());
        return b.isEquals();
    }

    /* (non-Javadoc)
     * @see java.security.Permission#toString()
     */
    public String toString()
    {
        final StringBuffer buf = new StringBuffer();
        buf.append("(name="); //$NON-NLS-1$
        buf.append(getName());
        buf.append(", actions="); //$NON-NLS-1$
        buf.append(getActions());
        buf.append(")"); //$NON-NLS-1$
        return buf.toString();
    }

    public Integer getId()
    {
        return id;
    }
    
    public void setId(Integer id)
    {
    	this.id = id;
    }

    /**
     * @return the actions
     */
    public String getActions()
    {
        return actions;
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(final String actions)
    {
        this.actions = actions;
    }
}

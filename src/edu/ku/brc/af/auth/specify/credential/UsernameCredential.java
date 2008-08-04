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
package edu.ku.brc.af.auth.specify.credential;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: Aug 26, 2007
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class UsernameCredential extends Credential
{
    private String name;

    /**
     * @param name
     */
    public UsernameCredential(final String name)
    {
        if (name == null)
        {
            throw new NullPointerException("name and/or id may not be null."); //$NON-NLS-1$
        }
        // else
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == this) { return true; }

        if (!(obj instanceof UsernameCredential))
        {
            return false;
        }
        // else
        UsernameCredential other = (UsernameCredential)obj;
        return getName().equals(other.getName());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("("); //$NON-NLS-1$
        buf.append("UsernameCredential: name="); //$NON-NLS-1$
        buf.append(getName());
        buf.append(")"); //$NON-NLS-1$
        return buf.toString();
    }
}
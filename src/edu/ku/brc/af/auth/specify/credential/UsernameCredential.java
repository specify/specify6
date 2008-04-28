package edu.ku.brc.af.auth.specify.credential;



@SuppressWarnings("serial")
public class UsernameCredential extends Credential
{
    private String name;

    public UsernameCredential(String name)
    {
        if (name == null)
        {
            throw new NullPointerException("name and/or id may not be null.");
        } 
        else
        {
            this.name = name;
        }
    }

    public String getName()
    {
        return name;
    }

//    public int hashCode()
//    {
//        return getName().hashCode() * 13 + getId().hashCode() * 13;
//    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == this) { return true; }

        if (!(obj instanceof UsernameCredential))
        {
            return false;
        } else
        {
            UsernameCredential other = (UsernameCredential)obj;
            return getName().equals(other.getName());
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        buf.append("UsernameCredential: name=");
        buf.append(getName());
        buf.append(")");
        return buf.toString();
    }
}
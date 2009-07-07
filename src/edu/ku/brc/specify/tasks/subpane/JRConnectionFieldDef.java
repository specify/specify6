/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

/**
 * @author Administrator
 *
 */
public class JRConnectionFieldDef
{
    protected final String   fldTitle;
    protected final String   fldName;
    protected final Class<?> fldClass;
    
    public JRConnectionFieldDef(final String fldName, final String fldTitle, final Class<?> fldClass)
    {
        this.fldName = fldName;
        this.fldTitle = fldTitle;
        this.fldClass = fldClass;
    }

    /**
     * @return
     */
    public String getFldName()
    {
    	return fldName;
    }
    /**
     * @return the fldClass
     */
    public Class<?> getFldClass()
    {
        return fldClass;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getFldTitle();
    }
    
    /**
     * @return the title
     */
    public String getFldTitle()
    {
        return fldTitle;
    }
}

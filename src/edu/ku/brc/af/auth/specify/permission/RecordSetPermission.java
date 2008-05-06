/**
 * 
 */
package edu.ku.brc.af.auth.specify.permission;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.UIRegistry;

/**
 * @author megkumin
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class RecordSetPermission extends BasicSpPermission
{
    protected static final Logger log = Logger.getLogger(RecordSetPermission.class);
	/**
     * @param id
     * @param name
     * @param actions
     */
    public RecordSetPermission(final String name, final String actions)
    {
        super( name, actions);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param id
     * @param name
     */
    public RecordSetPermission(final String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }
}

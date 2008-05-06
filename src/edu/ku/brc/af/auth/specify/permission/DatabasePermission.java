package edu.ku.brc.af.auth.specify.permission;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.UIRegistry;


@SuppressWarnings("serial") //$NON-NLS-1$
public class DatabasePermission extends BasicSpPermission
{
    protected static final Logger log = Logger.getLogger(DatabasePermission.class);

    /**
     * @param id
     * @param name
     * @param actions
     */
    public DatabasePermission(String name, String actions)
    {
        super(name, actions);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param id
     * @param name
     */
    public DatabasePermission(Integer id, String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }


}
package edu.ku.brc.af.auth.specify.permission;

import org.apache.log4j.Logger;


@SuppressWarnings("serial")
public class DisciplinePermission extends BasicSpPermission
{
    protected static final Logger log = Logger.getLogger(DisciplinePermission.class);

    /**
     * @param id
     * @param name
     * @param actions
     */
    public DisciplinePermission(String name, String actions)
    {
        super(name, actions);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param id
     * @param name
     */
    public DisciplinePermission(Integer id, String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }


}
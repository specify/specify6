/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.dbsupport.DBFieldInfo;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class FieldQRI extends BaseQRI
{
    protected DBFieldInfo fi;
    
    public FieldQRI(final BaseQRI parent, final DBFieldInfo fi)
    {
        super(parent, null);
        this.fi  = fi;
        title    = fi.getTitle();
        iconName = "BlankIcon";
    }
    
    public DBFieldInfo getFieldInfo()
    {
        return fi;
    }
    public boolean hasChildren()
    {
        return false;
    }

}
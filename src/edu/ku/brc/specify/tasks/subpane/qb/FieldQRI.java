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
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;

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
        if (fi != null)
        {
            title    = fi.getTitle();
        }
        iconName = "BlankIcon";
    }
    
    public DBFieldInfo getFieldInfo()
    {
        return fi;
    }
    
    @Override
    public boolean hasChildren()
    {
        return false;
    }

    public String getFieldName()
    {
        return fi.getName();
    }
    
    public UIFieldFormatterIFace getFormatter()
    {
        if (fi != null)
            return fi.getFormatter();
        return null;
    }
    
    public DBTableInfo getTableInfo()
    {
        if (fi != null)
            return fi.getTableInfo();
        return null;
    }
    
    public String getSQLFldSpec()
    {
        return parent.getTableTree().getAbbrev() + "." + getFieldName();
    }
    
    public String getSQLFldName()
    {
        return getSQLFldSpec();
    }
}
/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.UIHelper;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * QRIs that contain fields
 *
 */
public class ExpandableQRI extends BaseQRI
{
    protected DBTableInfo      ti;
    protected Vector<FieldQRI> fields = new Vector<FieldQRI>();

    /**
     * @param tableTree
     */
    public ExpandableQRI(final TableTree tableTree)
    {
        super(tableTree);
        
        this.ti       = tableTree.getTableInfo();
        this.iconName = ti.getClassObj().getSimpleName();
        this.title    = ti.getTitle();
        
        if (StringUtils.isEmpty(title))
        {
            title    = UIHelper.makeNamePretty(iconName);
        }
    }

    /**
     * @return
     */
    public DBTableInfo getTableInfo()
    {
        return ti;
    }

    /**
     * @return the kids
     */
    public int getFields()
    {
        return fields.size();
    }
    
    /**
     * @param f
     * @return
     */
    public FieldQRI getField(int f)
    {
        return fields.get(f);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#hasChildren()
     */
    @Override
    public boolean hasChildren()
    {
        return true;
    }
}

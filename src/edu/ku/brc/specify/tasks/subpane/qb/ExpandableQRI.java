/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBTableInfo;
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
    protected DBTableInfo ti;
    protected Vector<FieldQRI> fields = new Vector<FieldQRI>();

    /**
     * @param tableTree
     */
    public ExpandableQRI(TableTree tableTree)
    {
        super(tableTree);
        this.ti  = tableTree.getTableInfo();
        iconName = ti.getClassObj().getSimpleName();
        title    = ti.getTitle();
        if (StringUtils.isEmpty(title))
        {
            title    = UIHelper.makeNamePretty(iconName);
        }
    }

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
    
    public FieldQRI getField(int f)
    {
        return fields.get(f);
    }
}

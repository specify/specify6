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

import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class TableQRI extends BaseQRI
{
    protected DBTableInfo ti;
    protected Vector<FieldQRI> fields = new Vector<FieldQRI>();
    
    
    public TableQRI(final TableTree tableTree)
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
    
    public void addField(final DBFieldInfo fieldInfo)
    {
        fields.add(new FieldQRI(this, fieldInfo));
    }
    
    public void addField(final FieldQRI fieldQRI)
    {
        fieldQRI.setTable(this);
        fields.add(fieldQRI);
    }
    
    public void addFieldClone(final FieldQRI fieldQRI) throws CloneNotSupportedException
    {
        FieldQRI newField = (FieldQRI)fieldQRI.clone();
        newField.setTable(this);
        fields.add(newField);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        TableQRI result = (TableQRI)super.clone();
        result.fields = new Vector<FieldQRI>(fields.size());
        for (FieldQRI f : fields)
        {
            result.addFieldClone(f);
        }
        return result;
    }
    
    
    
}
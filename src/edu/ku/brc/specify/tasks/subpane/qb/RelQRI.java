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

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class RelQRI extends FieldQRI
{
    protected DBRelationshipInfo ri;
    
    public RelQRI(final TableQRI parent, final DBRelationshipInfo ri)
    {
        super(parent, null);

        this.ri = ri;

        try
        {
            iconName = Class.forName(ri.getClassName()).getSimpleName();
            title = ri.getTitle();
            if (StringUtils.isEmpty(title))
            {
                title = UIHelper.makeNamePretty(iconName);
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            iconName = "BlankIcon";
            title = "????";
        }
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#setIsInUse(java.lang.Boolean)
     */
    @Override
    public void setIsInUse(Boolean isInUse)
    {
        // TODO Auto-generated method stub
        super.setIsInUse(isInUse);
        table.setIsInUse(isInUse);
    }

    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getFieldName()
     */
    @Override
    public String getFieldName()
    {
        if (fi != null)
        {
            return super.getFieldName();
        }
        if (ri != null)
        {
            return ri.getName();
        }
        return table.getTableInfo().getName();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getTableInfo()
     */
    @Override
    public DBTableInfo getTableInfo()
    {
        return table.getTableInfo();    
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getSQLFldSpec(edu.ku.brc.specify.tasks.subpane.qb.TableAbbreviator)
     */
    @Override
    public String getSQLFldSpec(TableAbbreviator ta)
    {
        return ta.getAbbreviation(table.getTableTree().getParent()) + "." + ri.getName();
//        if (StringUtils.isNotEmpty(ri.getColName()))
//        {
//            //String fld = UploadTable.deCapitalize(ri.getColName()).replace("ID", "Id");
//            String fld = UploadTable.deCapitalize(ri.getColName()).replace("ID", "Id").replace("Id", "");
//            return ta.getAbbreviation(table.getTableTree().getParent()) + "." + fld;
//        }
//        return null;
    }

    @Override
    public boolean hasChildren()
    {
        return true;
    }
}
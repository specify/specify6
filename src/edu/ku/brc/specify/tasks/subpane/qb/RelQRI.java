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
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class RelQRI extends BaseQRI
{
    protected DBRelationshipInfo ri;
    protected TableTree          tableTree;
    
    public RelQRI(final BaseQRI parent, final TableTree tableTree, final DBRelationshipInfo ri)
    {
        super(parent, tableTree);
        
        int x = 0;
        x++;
        /*this.ri = ri;
        
        try
        {
            iconName = Class.forName(ri.getClassName()).getSimpleName();
            title    = ri.getTitle();
            if (StringUtils.isEmpty(title))
            {
                title    = UIHelper.makeNamePretty(iconName);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            iconName = "BlankIcon";
            title    = "????";
        }*/
    }
    
    /**
     * @return the tableTree
     */
    public TableTree getTableTree()
    {
        return tableTree;
    }

    /**
     * @param tableTree the tableTree to set
     */
    public void setTableTree(TableTree tableTree)
    {
        this.tableTree = tableTree;
        
        title    = tableTree.getTableInfo().getTitle();
        if (title == null)
        {
            int x = 0;
            x++;
        }
        iconName = tableTree.getTableInfo().getShortClassName();
    }

    public boolean hasChildren()
    {
        return true;//ri.getType() == DBRelationshipInfo.RelationshipType.OneToMany || ri.getType() == DBRelationshipInfo.RelationshipType.ManyToMany;
    }
}
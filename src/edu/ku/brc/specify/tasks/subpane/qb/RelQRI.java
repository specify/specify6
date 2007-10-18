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
    
    public RelQRI(final BaseQRI parent, final TableTree tableTree, final DBRelationshipInfo ri)
    {
        super(parent, tableTree);
        this.ri = ri;
        
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
        }
    }
    public DBRelationshipInfo getTableRelationship()
    {
        return ri;
    }        
    public boolean hasChildren()
    {
        return true;//ri.getType() == DBRelationshipInfo.RelationshipType.OneToMany || ri.getType() == DBRelationshipInfo.RelationshipType.ManyToMany;
    }
}
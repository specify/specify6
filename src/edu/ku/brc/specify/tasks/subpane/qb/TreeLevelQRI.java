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

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class TreeLevelQRI extends FieldQRI
{
    protected final int rankId;
    
    public TreeLevelQRI(final TableQRI parent, final DBFieldInfo fi, final int rankId)
            throws Exception
    {
        super(parent, fi);
        this.rankId = rankId;
        String treeDefName = parent.getTableTree().getTableInfo().getShortClassName()
                + "TreeDef";
        TreeDefIface<?, ?, ?> treeDef = Collection.getCurrentCollection().getCollectionType()
                .getTreeDef(treeDefName);
        TreeDefItemIface<?, ?, ?> treeDefItem = treeDef.getDefItemByRank(rankId);
        if (treeDefItem != null)
        {
            title = treeDefItem.getName();
        }
        else
        {
            throw new NoTreeDefItemException(rankId);
        }
    }
    
    public int getRankId()
    {
        return rankId;
    }
    
    @Override
    public String getFieldName()
    {
        return title;
    }
    
    @Override
    public DBTableInfo getTableInfo()
    {
        return table.getTableTree().getTableInfo();
    }
    
    protected String getSQLFldName(final TableAbbreviator ta)
    {
        StringBuilder result = new StringBuilder("(select treelevel.name from ");
        result.append(getTableInfo().getClassObj().getSimpleName());
        result.append(" treelevel where ");
        result.append(ta.getAbbreviation(table.getTableTree()));
        result.append(".nodeNumber between treelevel.nodeNumber and treelevel.highestChildNodeNumber and treelevel.rankId = ");
        result.append(String.valueOf(rankId));
        String treeDef = getTreeDefIdFldName();
        result.append(" and treelevel.");
        result.append(treeDef);
        result.append("=");
        result.append(ta.getAbbreviation(table.getTableTree()));
        result.append(".");
        result.append(treeDef);
        result.append(")");
        return result.toString();
    }
    
    protected String getTreeDefIdFldName()
    {
        //sql?
        //return getTableInfo().getClassObj().getSimpleName() + "treeDefId";
        
        //hql
        return "definition";
    }
    
    @Override
    public String getSQLFldSpec(final TableAbbreviator ta)
    {
        return getSQLFldName(ta) + " as " + getFieldName();
    }
    
    public class NoTreeDefItemException extends Exception
    {
        public NoTreeDefItemException(int rankId)
        {
            super("No TreeDefItem for " + String.valueOf(rankId));
        }
    }
}

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
    
    public TreeLevelQRI(final BaseQRI parent, final DBFieldInfo fi, final int rankId)
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
        throw new NoTreeDefItemException(rankId);
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
        return parent.getTableTree().getTableInfo();
    }
    
    @Override
    public String getSQLFldName()
    {
        StringBuilder result = new StringBuilder("(select treelevel.name from ");
        result.append(getTableInfo().getShortClassName());
        result.append("treelevel where ");
        result.append(parent.getTableTree().getAbbrev());
        result.append(".nodeNumber between treelevel.nodeNumber and treelevel.highestChildNodeNumber and treelevel.rankId = ");
        result.append(String.valueOf(rankId));
        result.append(")");
        return result.toString();
    }
    
    @Override
    public String getSQLFldSpec()
    {
        return getSQLFldName() + " as " + getFieldName();
    }
    
    public class NoTreeDefItemException extends Exception
    {
        public NoTreeDefItemException(int rankId)
        {
            super("No TreeDefItem for " + String.valueOf(rankId));
        }
    }
}

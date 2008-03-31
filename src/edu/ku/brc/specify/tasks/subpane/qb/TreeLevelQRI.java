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
    
    /**
     * @param parent
     * @param fi
     * @param rankId
     * @throws Exception
     */
    public TreeLevelQRI(final TableQRI parent, final DBFieldInfo fi, final int rankId)
            throws Exception
    {
        super(parent, fi);
        this.rankId = rankId;
        String treeDefName = parent.getTableTree().getTableInfo().getShortClassName()
                + "TreeDef";
        TreeDefIface<?, ?, ?> treeDef = Collection.getCurrentCollection().getDiscipline()
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
    
    /**
     * @return
     */
    public int getRankId()
    {
        return rankId;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getFieldName()
     */
    @Override
    public String getFieldName()
    {
        return title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getTableInfo()
     */
    @Override
    public DBTableInfo getTableInfo()
    {
        return table.getTableTree().getTableInfo();
    }
    
    /**
     * @param ta
     * @return complete specification of the field to be used in sql/hql fields clause.
     */
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
    
    /**
     * @return name of the field which links to the level's tree definition.
     */
    protected String getTreeDefIdFldName()
    {
        //sql?
        //return getTableInfo().getClassObj().getSimpleName() + "treeDefId";
        
        //hql
        return "definition";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getSQLFldSpec(edu.ku.brc.specify.tasks.subpane.qb.TableAbbreviator, boolean)
     */
    @Override
    public String getSQLFldSpec(final TableAbbreviator ta, final boolean forWhereClause)
    {
        String result = getSQLFldName(ta);
        if (!forWhereClause)
        {
            result = result + " as " + getFieldName().replace(' ', '_'); // can't figure out what
                                                                            // the delimiter is for
                                                                            // fld aliases so...
        }
        return result;
    }
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     */
    public class NoTreeDefItemException extends Exception
    {
        /**
         * @param rankId - rank of the level without a definition.
         */
        public NoTreeDefItemException(int rankId)
        {
            super("No TreeDefItem for " + String.valueOf(rankId));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#isFieldHidden()
     */
    @Override
    public boolean isFieldHidden()
    {
        //this object wouldn't have been created if it was hidden (not in the tree def).
        return false;
    }
    
    
}

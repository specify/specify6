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

import java.util.LinkedList;
import java.util.List;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.util.Pair;

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
        TreeDefIface<?, ?, ?> treeDef = null;
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            DataModelObjBase tempdisc = (DataModelObjBase )AppContextMgr.getInstance().getClassObject(Discipline.class);
            Discipline disc = (Discipline )session.get(tempdisc.getDataClass(), tempdisc.getId());
            treeDef = disc.getTreeDef(treeDefName);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            session.close();
        }
        TreeDefItemIface<?, ?, ?> treeDefItem = null;
        if (treeDef != null)
        {
        	treeDefItem = treeDef.getDefItemByRank(rankId);
        }
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
    
    protected String capitalize(final String toCap)
    {
        return toCap.substring(0, 1).toUpperCase().concat(toCap.substring(1));
    }
    
    public String getNodeNumberCriteria(final String criteria, final TableAbbreviator ta, 
                                        final String operStr, final boolean negate)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance()
        .createSession();
        try
        {
            TreeDefIface<?, ?, ?> treeDef = null;
            try
            {
                DataModelObjBase tempdef = (DataModelObjBase )AppContextMgr.getInstance().getClassObject(Collection.class).getDiscipline().getTreeDef(capitalize(getTableInfo().getClassObj().getSimpleName()) + "TreeDef");
                treeDef = (TreeDefIface<?, ?, ?> )session.get(tempdef.getDataClass(), tempdef.getId());
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }

            
            String className = getTableInfo().getClassObj().getSimpleName();
            List<?> matches = session.getDataList("from " + className + " where name " + operStr + " " +  criteria + " and " + className + "TreeDefId = " + treeDef.getTreeDefId()
                    + " and rankId =" + String.valueOf(rankId));
            List<Pair<Integer, Integer>> nodeInfo = new LinkedList<Pair<Integer, Integer>>();
            if (matches.size() == 0)
            {
                return "2+2=2"; //that'll do the trick. 
            }
            for (Object match : matches)
            {
                Treeable<?,?,?> node = (Treeable<?,?,?>)match;
                nodeInfo.add(new Pair<Integer, Integer>(node.getNodeNumber(), node.getHighestChildNodeNumber()));
            }
            String tblAlias = ta.getAbbreviation(table.getTableTree());
            StringBuilder result = new StringBuilder();
            for (Pair<Integer, Integer> node : nodeInfo)
            {
                if (result.length() > 0)
                {
                    if (negate)
                    {
                        result.append(" and ");
                    }
                    else
                    {
                        result.append(" or ");
                    }
                }
                result.append(tblAlias + ".nodeNumber");
                if (negate)
                {
                    result.append(" not "); 
                }
                result.append(" between ");
                result.append(node.getFirst());
                result.append(" and ");
                result.append(node.getSecond());
            }
            return "(" + result.toString() + ")";
        }
        finally
        {
            session.close();
        }
    }
    
}

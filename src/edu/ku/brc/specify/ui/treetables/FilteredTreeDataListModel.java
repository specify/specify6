/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.util.List;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class FilteredTreeDataListModel<T extends Treeable<T,D,I>,
                                        D extends TreeDefIface<T,D,I>,
                                        I extends TreeDefItemIface<T,D,I>>
                                        extends TreeDataListModel<T,D,I>
{
    protected List<T> nodesMatchingFilter;
    
    /**
     * @param treeDef
     */
    public FilteredTreeDataListModel(D treeDef, String leafNodeName)
    {
        super(treeDef);
    
        nodesMatchingFilter = dataService.findByName(treeDef, leafNodeName);
        
        hideChildren(root);
    }

//    @Override
//    public void addNewChild(T parent, T child)
//    {
//        super.setChildrenVisible(parent, false);
//        
//        //nodesMatchingFilter.add(child);
//        super.addNewChild(parent, child);
//        //makeNodeVisible(child);
//        
//        super.setChildrenVisible(parent, true);
//    }

    @Override
    protected int makeNodeVisible(T t)
    {
        if (matchesFilter(t))
        {
            return super.makeNodeVisible(t);
        }
        
        return -1;
    }
    
    protected boolean matchesFilter(T t)
    {
        if (nodesMatchingFilter == null)
        {
            // this is probably during the initial construction phase, let it pass
            return true;
        }
        
        // return true for any node that directly matches the sought after name
        if (nodesMatchingFilter.contains(t))
        {
            return true;
        }
        
        // otherwise, only return true for the nodes that are ancestors of nodes that
        // match the filter
        for (T matchingNode: nodesMatchingFilter)
        {
            if (matchingNode.isDescendantOf(t))
            {
                return true;
            }
            
            if (matchingNode == t)
            {
                return true;
            }
            
            if (matchingNode.getTreeId().longValue() == t.getTreeId().longValue())
            {
                return true;
            }
        }
        return false;
    }
}

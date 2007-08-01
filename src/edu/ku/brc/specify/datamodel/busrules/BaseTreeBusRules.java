package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

public abstract class BaseTreeBusRules<N extends Treeable<N,D,I>,
                                       D extends TreeDefIface<N,D,I>,
                                       I extends TreeDefItemIface<N,D,I>>
                                       extends BaseBusRules
{
    public BaseTreeBusRules(Class<?>... dataClasses)
    {
        super(dataClasses);
    }

    /**
     * Updates the fullname field of any nodes effected by changes to <code>node</code> that are about
     * to be saved to the DB.
     * 
     * @param node
     * @param session
     * @param nameChanged
     * @param parentChanged
     * @param rankChanged
     */
    protected void updateFullNamesIfNecessary(N node, DataProviderSessionIFace session)
    {
        if (fullNameNeedsUpdating(node, session))
        {
            // do the updates
        }

//        // if the name changed...
//        // update the node's fullname
//        // AND all descendants IF the node's level is in the fullname
//        if (nameChanged)
//        {
//            boolean isInFullname = false;
//            if ((node.getDefinitionItem().getIsInFullName() != null) && 
//                    (node.getDefinitionItem().getIsInFullName().booleanValue() == true))
//            {
//                isInFullname = true;
//            }
//
//            if (!isInFullname)
//            {
//                // just change the node's fullname field
//                String fullname = TreeHelper.generateFullname(node);
//                node.setFullName(fullname);
//            }
//            else
//            {
//                // must fix fullname for all descendants as well
//                TreeHelper.fixFullnameForNodeAndDescendants(node);
//            }
//        }
//
//        // if no levels above or equal to the new parent or old parent are included in the fullname
//        // do nothing
//        // otherwise, update fullname for node and all descendants
//
//        boolean higherLevelsIncluded = false;
//        N l = node.getParent();
//        while (l != null)
//        {
//            if ((l.getDefinitionItem().getIsInFullName() != null) && 
//                    (l.getDefinitionItem().getIsInFullName().booleanValue() == true))
//            {
//                higherLevelsIncluded = true;
//                break;
//            }
//            l = l.getParent();
//        }
//
//        // if no higher level is included in the new place in the tree, check the old place
//        // in the tree
//        if (higherLevelsIncluded == false)
//        {
//            l = origParent;
//            while (l != null)
//            {
//                if ((l.getDefinitionItem().getIsInFullName() != null) && 
//                        (l.getDefinitionItem().getIsInFullName().booleanValue() == true))
//                {
//                    higherLevelsIncluded = true;
//                    break;
//                }
//
//                l = l.getParent();
//            }
//        }
//
//        if (higherLevelsIncluded)
//        {
//            TreeHelper.fixFullnameForNodeAndDescendants(node);
//        }
//        else
//        {
//            String generated = TreeHelper.generateFullname(node);
//            node.setFullName(generated);
//        }
    }

    /**
     * @param node
     * @param session
     * @param origNode
     * @return
     */
    @SuppressWarnings("unchecked")
    protected boolean fullNameNeedsUpdating(N node, @SuppressWarnings("unused") DataProviderSessionIFace session)
    {
        // we need a way to determine if the name changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        N fromDB = (N)tmpSession.load(node.getClass(), node.getTreeId());
        N origParent = fromDB.getParent();
        tmpSession.close();

        boolean nameChanged = !(fromDB.getName().equals(node.getName()));
        boolean parentChanged = !(origParent.getTreeId().equals(node.getParent().getTreeId()));
        boolean rankChanged = !(origParent.getRankId().equals(node.getRankId()));

        return nameChanged || parentChanged || rankChanged;
    }
}

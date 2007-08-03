package edu.ku.brc.specify.datamodel.busrules;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeHelper;

public abstract class BaseTreeBusRules<T extends Treeable<T,D,I>,
                                       D extends TreeDefIface<T,D,I>,
                                       I extends TreeDefItemIface<T,D,I>>
                                       extends BaseBusRules
{
    private static final Logger log = Logger.getLogger(BaseTreeBusRules.class);

    protected T nodeBeforeSave;
    
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
    protected void updateFullNamesIfNecessary(T node, DataProviderSessionIFace session)
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
    protected boolean fullNameNeedsUpdating(T node, @SuppressWarnings("unused") DataProviderSessionIFace session)
    {
        if (node.getTreeId() == null)
        {
            // this is a new node
            // it shouldn't need updating since we set the fullname at creation time
            return false;
        }

        // we need a way to determine if the name changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        T fromDB = (T)tmpSession.load(node.getClass(), node.getTreeId());
        T origParent = fromDB.getParent();
        tmpSession.close();

        boolean nameChanged = !(fromDB.getName().equals(node.getName()));
        boolean parentChanged = !(origParent.getTreeId().equals(node.getParent().getTreeId()));
        boolean rankChanged = !(origParent.getRankId().equals(node.getRankId()));

        return nameChanged || parentChanged || rankChanged;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        if (dataObj instanceof Treeable)
        {
            // NOTE: the instanceof check can't check against 'T' since T isn't a class
            //       this has a SMALL amount of risk to it
            T node = (T)dataObj;
            
            // check to see if this node is brand new
            if (node.getTreeId() == null)
            {
                node.setDefinition(node.getParent().getDefinition());
                
                // this is a new object
                // set it's fullname
                String fullname = TreeHelper.generateFullname(node);
                node.setFullName(fullname);
            }
            nodeBeforeSave = node;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSave(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean afterSave(Object dataObj)
    {
        boolean success = true;
        
        // compare the dataObj values to the nodeBeforeSave values to determine if a node was moved or added
        if (dataObj instanceof Treeable)
        {
            // NOTE: the instanceof check can't check against 'T' since T isn't a class
            //       this has a SMALL amount of risk to it
            T node = (T)dataObj;
            
            // if the node doesn't have any assigned node number, it must be new
            boolean added = (node.getNodeNumber() == null);

            if (added)
            {
                log.info("Saved tree node was added.  Updating node numbers appropriately.");
                TreeDataService dataServ = TreeDataServiceFactory.createService();
                success = dataServ.updateNodeNumbersAfterNodeAddition(node);
            }
        }
        
        return success;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterDelete(java.lang.Object)
     */
    @Override
    public void afterDelete(Object dataObj)
    {
        if (dataObj instanceof Treeable)
        {
            // NOTE: the instanceof check can't check against 'T' since T isn't a class
            //       this has a SMALL amount of risk to it
            T node = (T)dataObj;

            log.info("A tree node was deleted.  Updating node numbers appropriately.");
            TreeDataService dataServ = TreeDataServiceFactory.createService();
            boolean success = dataServ.updateNodeNumbersAfterNodeDeletion(node);
        }
    }
}

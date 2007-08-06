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
    @SuppressWarnings("unchecked")
    protected void updateFullNamesIfNecessary(T node, @SuppressWarnings("unused") DataProviderSessionIFace session)
    {
        if (node.getTreeId() == null)
        {
            // this is a new node
            // it shouldn't need updating since we set the fullname at creation time
            return;
        }
        
        boolean updateNodeFullName = false;
        boolean updateDescFullNames = false;

        // we need a way to determine if the name changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        T fromDB = (T)tmpSession.load(node.getClass(), node.getTreeId());
        T origParent = fromDB.getParent();
        tmpSession.close();

        boolean parentChanged = false;
        T currentParent = node.getParent();
        if ((currentParent == null && origParent != null) || (currentParent != null && origParent == null))
        {
            // I can't imagine how this would ever happen, but just in case
            parentChanged = true;
        }
        if (currentParent != null && origParent != null && !currentParent.getTreeId().equals(origParent.getTreeId()))
        {
            // the parent ID changed
            parentChanged = true;
        }
        
        boolean higherLevelsIncluded = false;
        if (parentChanged)
        {
            higherLevelsIncluded = higherLevelsIncludedInFullname(node);
            higherLevelsIncluded |= higherLevelsIncludedInFullname(fromDB);
        }
        
        if (parentChanged && higherLevelsIncluded)
        {
            updateNodeFullName = true;
            updateDescFullNames = true;
        }
        
        boolean nameChanged = !(fromDB.getName().equals(node.getName()));
        boolean rankChanged = !(fromDB.getRankId().equals(node.getRankId()));
        if (rankChanged || nameChanged)
        {
            updateNodeFullName = true;
            if (booleanValue(fromDB.getDefinitionItem().getIsInFullName(), false) == true)
            {
                updateDescFullNames = true;
            }
            if (booleanValue(node.getDefinitionItem().getIsInFullName(), false) == true)
            {
                updateDescFullNames = true;
            }
        }

        if (updateNodeFullName)
        {
            if (updateDescFullNames)
            {
                // this could take a long time
                TreeHelper.fixFullnameForNodeAndDescendants(node);
            }
            else
            {
                // this should be really fast
                String fullname = TreeHelper.generateFullname(node);
                node.setFullName(fullname);
            }
        }
    }

    protected boolean higherLevelsIncludedInFullname(T node)
    {
        boolean higherLevelsIncluded = false;
        // this doesn't necessarily mean the fullname has to be changed
        // if no higher levels are included in the fullname, then nothing needs updating
        // so, let's see if higher levels factor into the fullname
        T l = node.getParent();
        while (l != null)
        {
            if ((l.getDefinitionItem().getIsInFullName() != null) && 
                    (l.getDefinitionItem().getIsInFullName().booleanValue() == true))
            {
                higherLevelsIncluded = true;
                break;
            }
            l = l.getParent();
        }
        
        return higherLevelsIncluded;
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
    @SuppressWarnings("unchecked")
    @Override
    public void afterDelete(Object dataObj)
    {
        if (dataObj instanceof Treeable)
        {
            // NOTE: the instanceof check can't check against 'T' since T isn't a class
            //       this has a SMALL amount of risk to it
            T node = (T)dataObj;

            log.info("A tree node was deleted.  Updating node numbers appropriately.");
            TreeDataService<T,D,I> dataServ = TreeDataServiceFactory.createService();
            @SuppressWarnings("unused")
            boolean success = dataServ.updateNodeNumbersAfterNodeDeletion(node);
        }
    }
    
    protected boolean booleanValue(Boolean bool, boolean defaultIfNull)
    {
        if (bool != null)
        {
            return bool.booleanValue();
        }
        return defaultIfNull;
    }
}

package edu.ku.brc.specify.datamodel.busrules;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
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

    /**
     * Constructor.
     * 
     * @param dataClasses a var args list of classes that this business rules implementation handles
     */
    public BaseTreeBusRules(Class<?>... dataClasses)
    {
        super(dataClasses);
    }
    
    public abstract String[] getRelatedTableAndColumnNames();
    
    @SuppressWarnings("unchecked")
    public boolean okToDeleteNode(T node)
    {
        Integer id = node.getTreeId();
        if (id == null)
        {
            return true;
        }
        String[] relationships = getRelatedTableAndColumnNames();

        // if the given node can't be deleted, return false
        if (!super.okToDelete(relationships, node.getTreeId()))
        {
            return false;
        }

        // now check the children

        // get a list of all descendent IDs
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        String queryStr = "SELECT n.id FROM " + node.getClass().getName() + " n WHERE n.nodeNumber <= :highChild AND n.nodeNumber > :nodeNum ORDER BY n.rankId DESC";
        QueryIFace query = session.createQuery(queryStr);
        query.setParameter("highChild", node.getHighestChildNodeNumber());
        query.setParameter("nodeNum", node.getNodeNumber());
        List<Integer> childIDs = (List<Integer>)query.list();
        session.close();

        // if there are no descendent nodes, return true
        if (childIDs.size() == 0)
        {
            return true;
        }

        // break the descendent checks up into chunks or queries
        
        // This is an arbitrary number.  Trial and error will determine a good value.  This determines
        // the number of IDs that wind up in the "IN" clause of the query run inside okToDelete().
        int chunkSize = 250;
        int lastRecordChecked = -1;

        boolean childrenDeletable = true;
        while (lastRecordChecked  + 1 < childIDs.size() && childrenDeletable)
        {
            int startOfChunk = lastRecordChecked + 1;
            int endOfChunk = Math.min(lastRecordChecked+1+chunkSize, childIDs.size());

            // grabs selected subset, exclusive of the last index
            List<Integer> chunk = childIDs.subList(startOfChunk, endOfChunk);
            
            Integer[] idChunk = chunk.toArray(new Integer[1]);
            childrenDeletable = super.okToDelete(relationships, idChunk);
            
            lastRecordChecked = endOfChunk - 1;
        }
        return childrenDeletable;
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
        T fromDB = (T)tmpSession.get(node.getClass(), node.getTreeId());
        tmpSession.close();
        
        if (fromDB == null)
        {
            // this node is new and hasn't yet been flushed to the DB, so we don't need to worry about updating fullnames
            //return;
            fromDB = node;
        }

        T       origParent    = fromDB.getParent();
        boolean parentChanged = false;
        T       currentParent = node.getParent();
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
        } else if (fromDB == node)
        {
            updateNodeFullName = true;
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
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof Treeable)
        {
            // NOTE: the instanceof check can't check against 'T' since T isn't a class
            //       this has a SMALL amount of risk to it
            T node = (T)dataObj;
            
            if (node.getFullName() == null)
            {
                // set it's fullname
                String fullname = TreeHelper.generateFullname(node);
                node.setFullName(fullname);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSaveCommit(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        if (super.beforeSaveCommit(dataObj,session) == false)
        {
            return false;
        }
        
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
                success = dataServ.updateNodeNumbersAfterNodeAddition(node,session);
            }
        }
        
        return success;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterDeleteCommit(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        if (dataObj instanceof Treeable)
        {
            // NOTE: the instanceof check can't check against 'T' since T isn't a class
            //       this has a SMALL amount of risk to it
            T node = (T)dataObj;

            log.info("A tree node was deleted.  Updating node numbers appropriately.");
            TreeDataService<T,D,I> dataServ = TreeDataServiceFactory.createService();
            @SuppressWarnings("unused")
            boolean success = dataServ.updateNodeNumbersAfterNodeDeletion(node,session);
            return success;
        }
        
        return true;
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link TreeDefItemIface}.  The real work of this method is to
     * update the 'fullname' field of all {@link Treeable} objects effected by the changes
     * to the passed in {@link TreeDefItemIface}.
     *
     * @param defItem the {@link TreeDefItemIface} being saved
     */
    @SuppressWarnings("unchecked")
    protected void beforeSaveTreeDefItem(I defItem)
    {
        // we need a way to determine if the 'isInFullname' value changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        I fromDB = (I)tmpSession.load(defItem.getClass(), defItem.getTreeDefItemId());
        tmpSession.close();

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(defItem);

        boolean changeThisLevel = false;
        boolean changeAllDescendants = false;
        
        boolean fromDBIsInFullname = makeNotNull(fromDB.getIsInFullName());
        boolean currentIsInFullname = makeNotNull(defItem.getIsInFullName());
        if (fromDBIsInFullname != currentIsInFullname)
        {
            changeAllDescendants = true;
        }
        
        // look for changes in the 'textBefore', 'textAfter' or 'fullNameSeparator' fields
        String fromDbBeforeText = makeNotNull(fromDB.getTextBefore());
        String fromDbAfterText = makeNotNull(fromDB.getTextAfter());
        String fromDbSeparator = makeNotNull(fromDB.getFullNameSeparator());
        
        String before = makeNotNull(defItem.getTextBefore());
        String after = makeNotNull(defItem.getTextAfter());
        String separator = makeNotNull(defItem.getFullNameSeparator());
        
        boolean textFieldChanged = false;
        boolean beforeChanged = !before.equals(fromDbBeforeText);
        boolean afterChanged = !after.equals(fromDbAfterText);
        boolean sepChanged = !separator.equals(fromDbSeparator);
        if (beforeChanged || afterChanged || sepChanged)
        {
            textFieldChanged = true;
        }
        
        if (textFieldChanged)
        {
            if (currentIsInFullname)
            {
                changeAllDescendants = true;
            }
            changeThisLevel = true;
        }
        
        if (changeThisLevel && !changeAllDescendants)
        {
            Set<T> levelNodes = defItem.getTreeEntries();
            for (T node: levelNodes)
            {
                String generated = TreeHelper.generateFullname(node);
                node.setFullName(generated);
            }
        }
        else if (changeThisLevel && changeAllDescendants)
        {
            Set<T> levelNodes = defItem.getTreeEntries();
            for (T node: levelNodes)
            {
                TreeHelper.fixFullnameForNodeAndDescendants(node);
            }
        }
        else if (!changeThisLevel && changeAllDescendants)
        {
            Set<T> levelNodes = defItem.getTreeEntries();
            for (T node: levelNodes)
            {
                // grab all child nodes and go from there
                for (T child: node.getChildren())
                {
                    TreeHelper.fixFullnameForNodeAndDescendants(child);
                }
            }
        }
        // else don't change anything
        
        session.close();
    }
    
    protected boolean booleanValue(Boolean bool, boolean defaultIfNull)
    {
        if (bool != null)
        {
            return bool.booleanValue();
        }
        return defaultIfNull;
    }
    
    /**
     * Converts a null string into an empty string.  If the provided String is not
     * null, it is returned unchanged.
     * 
     * @param s a string
     * @return the string or " ", if null
     */
    private String makeNotNull(String s)
    {
        return (s == null) ? "" : s;
    }
    
    /**
     * Returns the provided {@link Boolean}, or <code>false</code> if null
     * 
     * @param b the {@link Boolean} to convert to non-null
     * @returnthe provided {@link Boolean}, or <code>false</code> if null
     */
    private boolean makeNotNull(Boolean b)
    {
        return (b == null) ? false : b.booleanValue();
    }

}

/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UICacheManager.getLocalizedMessage;

import java.util.Set;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.treeutils.TreeHelper;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class GeologicTimePeriodBusRules extends BaseBusRules
{
    private static final Logger log = Logger.getLogger("edu.ku.brc.specify.datamodel.busrules");
    
    public GeologicTimePeriodBusRules()
    {
        super(GeologicTimePeriod.class,GeologicTimePeriodTreeDefItem.class);
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        return getLocalizedMessage("GEOLOGICAL_TIME_PERIOD_DELETED", ((GeologicTimePeriod)dataObj).getName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSave(java.lang.Object)
     */
    @Override
    public void afterSave(Object dataObj)
    {
        log.debug("enter");
        log.debug("exit");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj)
    {
        log.debug("enter");
        if (dataObj instanceof GeologicTimePeriod)
        {
            beforeSaveGeologicTimePeriod((GeologicTimePeriod)dataObj);
            log.debug("exit");
            return;
        }
        
        if (dataObj instanceof GeologicTimePeriodTreeDefItem)
        {
            beforeSaveGeologicTimePeriodTreeDefItem((GeologicTimePeriodTreeDefItem)dataObj);
            log.debug("exit");
            return;
        }
        log.debug("exit");
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link GeologicTimePeriod}.  The real work of this method is to
     * update the 'fullname' field of all {@link GeologicTimePeriod} objects effected by the changes
     * to the passed in {@link GeologicTimePeriod}.
     * 
     * @param gtp the {@link GeologicTimePeriod} being saved
     */
    protected void beforeSaveGeologicTimePeriod(GeologicTimePeriod gtp)
    {
        // check to see if this node is brand new
        if (gtp.getId() == null)
        {
            // this is a new object
            // just update it's fullname (and return)
            // since it can't have any children yet
            
            String fullname = TreeHelper.generateFullname(gtp);
            gtp.setFullName(fullname);
            return;
        }
        // else
        
        // we need a way to determine if the name changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        GeologicTimePeriod fromDB = tmpSession.load(GeologicTimePeriod.class, gtp.getId());
        GeologicTimePeriod origParent = fromDB.getParent();
        tmpSession.close();

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(gtp);
        
        boolean nameChanged = true;
        
        nameChanged = !(fromDB.getName().equals(gtp.getName()));
        
        
        // if the name changed...
        // update the node's fullname
        // AND all descendants IF the node's level is in the fullname
        if (nameChanged)
        {
            boolean isInFullname = false;
            if ((gtp.getDefinitionItem().getIsInFullName() != null) && 
                (gtp.getDefinitionItem().getIsInFullName().booleanValue() == true))
            {
                isInFullname = true;
            }
            
            if (!isInFullname)
            {
                // just change the node's fullname field
                String fullname = TreeHelper.generateFullname(gtp);
                gtp.setFullName(fullname);
            }
            else
            {
                // must fix fullname for all descendants as well
                TreeHelper.fixFullnameForNodeAndDescendants(gtp);
            }
            
            session.close();
            
            // it is assumed that you cannot perform a name change AND move a node in one transaction
            // so we're done here
            return;
        }
        
        // we need a way to determine if the parent changed
        // did the node change parents?
        boolean parentChanged = true;
        parentChanged = !(origParent.getId().equals(gtp.getParent().getId()));
        
        if (parentChanged)
        {
            // if no levels above or equal to the new parent or old parent are included in the fullname
            // do nothing
            // otherwise, update fullname for node and all descendants
            
            boolean higherLevelsIncluded = false;
            GeologicTimePeriod l = gtp.getParent();
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
            
            // if no higher level is included in the new place in the tree, check the old place
            // in the tree
            if (higherLevelsIncluded == false)
            {
                l = origParent;
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
            }
            
            if (higherLevelsIncluded)
            {
                TreeHelper.fixFullnameForNodeAndDescendants(gtp);
            }
            else
            {
                String generated = TreeHelper.generateFullname(gtp);
                gtp.setFullName(generated);
            }
        }
        
        session.close();
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link GeologicTimePeriodTreeDefItem}.  The real work of this method is to
     * update the 'fullname' field of all {@link GeologicTimePeriod} objects effected by the changes
     * to the passed in {@link GeologicTimePeriodTreeDefItem}.
     *
     * @param defItem the {@link GeologicTimePeriodTreeDefItem} being saved
     */
    protected void beforeSaveGeologicTimePeriodTreeDefItem(GeologicTimePeriodTreeDefItem defItem)
    {
        // we need a way to determine if the 'isInFullname' value changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        GeologicTimePeriodTreeDefItem fromDB = tmpSession.load(GeologicTimePeriodTreeDefItem.class, defItem.getId());
        tmpSession.close();

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(defItem);

        boolean changeThisLevel = false;
        boolean changeAllDescendants = false;
        
        boolean fromDBValue = (fromDB.getIsInFullName()!=null) ? fromDB.getIsInFullName() : false;
        boolean currentValue = (defItem.getIsInFullName()!=null) ? defItem.getIsInFullName() : false;
        if (fromDBValue != currentValue)
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
        if ( !before.equals(fromDbBeforeText) ||
             !after.equals(fromDbAfterText) ||
             !separator.equals(fromDbSeparator) )
        {
            textFieldChanged = true;
        }
        
        if (textFieldChanged)
        {
            if (defItem.getIsInFullName()!=null && defItem.getIsInFullName().booleanValue()==true)
            {
                changeThisLevel = true;
                changeAllDescendants = true;
            }
            else
            {
                changeAllDescendants = true;
            }
        }
        
        if (changeThisLevel && !changeAllDescendants)
        {
            Set<GeologicTimePeriod> levelNodes = defItem.getTreeEntries();
            for (GeologicTimePeriod node: levelNodes)
            {
                String generated = TreeHelper.generateFullname(node);
                node.setFullName(generated);
            }
        }
        else if (changeThisLevel && changeAllDescendants)
        {
            Set<GeologicTimePeriod> levelNodes = defItem.getTreeEntries();
            for (GeologicTimePeriod node: levelNodes)
            {
                TreeHelper.fixFullnameForNodeAndDescendants(node);
            }
        }
        else if (!changeThisLevel && changeAllDescendants)
        {
            Set<GeologicTimePeriod> levelNodes = defItem.getTreeEntries();
            for (GeologicTimePeriod node: levelNodes)
            {
                // grab all child nodes and go from there
                for (GeologicTimePeriod child: node.getChildren())
                {
                    TreeHelper.fixFullnameForNodeAndDescendants(child);
                }
            }
        }
        // else don't change anything
        
        session.close();
    }
    
    private String makeNotNull(String s)
    {
        return (s == null) ? "" : s;
    }
}

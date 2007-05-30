/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.util.Set;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.specify.treeutils.TreeHelper;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Location} or
 * {@link LocationTreeDefItem} objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class LocationBusRules extends BaseBusRules
{
    /**
     * A logger that emits any and all messages from this class.
     */
    private static final Logger log = Logger.getLogger("edu.ku.brc.specify.datamodel.busrules");
    
    /**
     * Constructor.
     */
    public LocationBusRules()
    {
        super(Location.class, LocationTreeDefItem.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        if (dataObj instanceof Location)
        {
            return getLocalizedMessage("LOCATION_DELETED", ((Location)dataObj).getName());
        }
        // else
        return super.getDeleteMsg(dataObj);
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
        if (dataObj instanceof Location)
        {
            beforeSaveLocation((Location)dataObj);
            log.debug("exit");
            return;
        }
        
        if (dataObj instanceof LocationTreeDefItem)
        {
            beforeSaveLocationTreeDefItem((LocationTreeDefItem)dataObj);
            log.debug("exit");
            return;
        }
        log.debug("exit");
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Location}.  The real work of this method is to
     * update the 'fullname' field of all {@link Location} objects effected by the changes
     * to the passed in {@link Location}.
     * 
     * @param loc the {@link Location} being saved
     */
    protected void beforeSaveLocation(Location loc)
    {
        // check to see if this node is brand new
        if (loc.getId() == null)
        {
            // this is a new object
            // just update it's fullname (and return)
            // since it can't have any children yet
            
            String fullname = TreeHelper.generateFullname(loc);
            loc.setFullName(fullname);
            return;
        }
        // else
        
        // we need a way to determine if the name changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        Location fromDB = tmpSession.load(Location.class, loc.getId());
        Location origParent = fromDB.getParent();
        tmpSession.close();

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(loc);
        
        boolean nameChanged = true;
        
        nameChanged = !(fromDB.getName().equals(loc.getName()));
        
        
        // if the name changed...
        // update the node's fullname
        // AND all descendants IF the node's level is in the fullname
        if (nameChanged)
        {
            boolean isInFullname = false;
            if ((loc.getDefinitionItem().getIsInFullName() != null) && 
                (loc.getDefinitionItem().getIsInFullName().booleanValue() == true))
            {
                isInFullname = true;
            }
            
            if (!isInFullname)
            {
                // just change the node's fullname field
                String fullname = TreeHelper.generateFullname(loc);
                loc.setFullName(fullname);
            }
            else
            {
                // must fix fullname for all descendants as well
                TreeHelper.fixFullnameForNodeAndDescendants(loc);
            }
            
            session.close();
            
            // it is assumed that you cannot perform a name change AND move a node in one transaction
            // so we're done here
            return;
        }
        
        // we need a way to determine if the parent changed
        // did the node change parents?
        boolean parentChanged = true;
        parentChanged = !(origParent.getId().equals(loc.getParent().getId()));
        
        if (parentChanged)
        {
            // if no levels above or equal to the new parent or old parent are included in the fullname
            // do nothing
            // otherwise, update fullname for node and all descendants
            
            boolean higherLevelsIncluded = false;
            Location l = loc.getParent();
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
                TreeHelper.fixFullnameForNodeAndDescendants(loc);
            }
            else
            {
                String generated = TreeHelper.generateFullname(loc);
                loc.setFullName(generated);
            }
        }
        
        session.close();
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link LocationTreeDefItem}.  The real work of this method is to
     * update the 'fullname' field of all {@link Location} objects effected by the changes
     * to the passed in {@link LocationTreeDefItem}.
     *
     * @param defItem the {@link LocationTreeDefItem} being saved
     */
    protected void beforeSaveLocationTreeDefItem(LocationTreeDefItem defItem)
    {
        // we need a way to determine if the 'isInFullname' value changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        LocationTreeDefItem fromDB = tmpSession.load(LocationTreeDefItem.class, defItem.getId());
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
            Set<Location> levelNodes = defItem.getTreeEntries();
            for (Location node: levelNodes)
            {
                String generated = TreeHelper.generateFullname(node);
                node.setFullName(generated);
            }
        }
        else if (changeThisLevel && changeAllDescendants)
        {
            Set<Location> levelNodes = defItem.getTreeEntries();
            for (Location node: levelNodes)
            {
                TreeHelper.fixFullnameForNodeAndDescendants(node);
            }
        }
        else if (!changeThisLevel && changeAllDescendants)
        {
            Set<Location> levelNodes = defItem.getTreeEntries();
            for (Location node: levelNodes)
            {
                // grab all child nodes and go from there
                for (Location child: node.getChildren())
                {
                    TreeHelper.fixFullnameForNodeAndDescendants(child);
                }
            }
        }
        // else don't change anything
        
        session.close();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj instanceof Location)
        {
            return okToDeleteLocation((Location)dataObj);
        }
        
        if (dataObj instanceof LocationTreeDefItem)
        {
            return okToDeleteLocDefItem((LocationTreeDefItem)dataObj);
        }
        
        return true;
    }
    
    /**
     * Handles the {@link #okToDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link Location}.
     * 
     * @param loc the {@link Location} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteLocation(Location loc)
    {
        if (!okToDelete("preparation", "LocationID", loc.getId()))
        {
            return false;
        }
        
        if (!okToDelete("container", "LocationID", loc.getId()))
        {
            return false;
        }

        if (!okToDelete("location", "ParentID", loc.getId()))
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * Handles the {@link #okToDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link LocationTreeDefItem}.
     * 
     * @param defItem the {@link LocationTreeDefItem} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteLocDefItem(LocationTreeDefItem defItem)
    {
        // never let the root level be deleted
        if (defItem.getRankId() == 0)
        {
            return false;
        }
        
        // don't let 'used' levels be deleted
        if (!okToDelete("location", "LocationTreeDefItemID", defItem.getId()))
        {
            return false;
        }
        
        return true;
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

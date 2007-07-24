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
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.treeutils.TreeHelper;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Taxon} or
 * {@link TaxonTreeDefItem} objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class TaxonBusRules extends BaseBusRules
{
    private static final Logger log = Logger.getLogger("edu.ku.brc.specify.datamodel.busrules");
    
    /**
     * Constructor.
     */
    public TaxonBusRules()
    {
        super(Taxon.class,TaxonTreeDefItem.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        return getLocalizedMessage("TAXON_DELETED", ((Taxon)dataObj).getName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj instanceof Taxon)
        {
            Taxon node = (Taxon)dataObj;
            return okToDeleteTaxon(node);
        }
        
        return false;
    }
    
    public boolean okToDeleteTaxon(Taxon t)
    {
        Long id = t.getId();
        if (id == null)
        {
            return true;
        }
        
//        boolean noDeters = super.okToDelete("determination", "TaxonID",         id);
//        boolean noCites  = super.okToDelete("taxoncitation", "TaxonID",         id);
//        boolean noHyb1   = super.okToDelete("taxon",         "HybridParent1ID", id);
//        boolean noHyb2   = super.okToDelete("taxon",         "HybridParent2ID", id);
//        boolean noSyns   = super.okToDelete("taxon",         "AcceptedID",      id);
//        boolean noChild  = super.okToDelete("taxon",         "ParentID",        id);

        // TODO: convert this check over to using something a little faster, if possible
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        Taxon tmpT = session.load(Taxon.class, id);
        boolean noDeters = (tmpT.getDeterminations().size() == 0);
        boolean noCites  = (tmpT.getTaxonCitations().size() == 0);
        boolean noHyb1   = (tmpT.getHybridChildren1().size() == 0);
        boolean noHyb2   = (tmpT.getHybridChildren2().size() == 0);
        boolean noSyns   = (tmpT.getAcceptedChildren().size() == 0);
        boolean okToDeleteChildren = true;
        for (Taxon child: tmpT.getChildren())
        {
            if ( !okToDelete(child) )
            {
                okToDeleteChildren = false;
                break;
            }
        }
        session.close();
        
        return noDeters && noCites && noHyb1 && noHyb2 && noSyns && okToDeleteChildren;

//        return false;
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
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        log.debug("enter");
        if (dataObj instanceof Taxon)
        {
            beforeSaveTaxon((Taxon)dataObj, session);
            log.debug("exit");
            return;
        }
        
        if (dataObj instanceof TaxonTreeDefItem)
        {
            beforeSaveTaxonTreeDefItem((TaxonTreeDefItem)dataObj);
            log.debug("exit");
            return;
        }
        log.debug("exit");
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Taxon}.  The real work of this method is to
     * update the 'fullname' field of all {@link Taxon} objects effected by the changes
     * to the passed in {@link Taxon}.
     * 
     * @param taxon the {@link Taxon} being saved
     */
    protected void beforeSaveTaxon(Taxon taxon, DataProviderSessionIFace session)
    {
        // check to see if this node is brand new
        if (taxon.getId() == null)
        {
            taxon.setDefinition(taxon.getParent().getDefinition());
            
            // this is a new object
            // just update it's fullname
            // it can't have any children yet
            
            String fullname = TreeHelper.generateFullname(taxon);
            taxon.setFullName(fullname);
            return;
        }
        // else (ID was not null, so this is an item in the DB already)
        
        // if this node is "accepted" then make sure it doesn't point to an accepted parent
        if (taxon.getIsAccepted() == null || taxon.getIsAccepted().booleanValue() == true)
        {
            taxon.setAcceptedTaxon(null);
        }
        
        // if this node isn't a hybrid then make sure it doesn't point at hybrid "parents"
        if (taxon.getIsHybrid() == null || taxon.getIsHybrid().booleanValue() == false)
        {
            taxon.setHybridParent1(null);
            taxon.setHybridParent2(null);
        }
     
        // we need a way to determine if the name changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        Taxon fromDB = tmpSession.load(Taxon.class, taxon.getId());
        Taxon origParent = fromDB.getParent();
        tmpSession.close();

        boolean nameChanged = !(fromDB.getName().equals(taxon.getName()));
        boolean parentChanged = !(origParent.getId().equals(taxon.getParent().getId()));

        if (nameChanged)
        {
            log.warn("Taxon record name changed.  Update the fullname for this node and any effected descendants.");
        }
        if (parentChanged)
        {
            log.warn("Taxon record parent changed.  Update the fullname for this node and any effected descendants.");
        }
        
//        //session.attach(taxon);
//        
//        // if the name changed...
//        // update the node's fullname
//        // AND all descendants IF the node's level is in the fullname
//        if (nameChanged)
//        {
//            boolean isInFullname = false;
//            if ((taxon.getDefinitionItem().getIsInFullName() != null) && 
//                (taxon.getDefinitionItem().getIsInFullName().booleanValue() == true))
//            {
//                isInFullname = true;
//            }
//            
//            if (!isInFullname)
//            {
//                // just change the node's fullname field
//                String fullname = TreeHelper.generateFullname(taxon);
//                taxon.setFullName(fullname);
//            }
//            else
//            {
//                // must fix fullname for all descendants as well
//                TreeHelper.fixFullnameForNodeAndDescendants(taxon);
//            }
//        }
//        
//        if (parentChanged)
//        {
//            // if no levels above or equal to the new parent or old parent are included in the fullname
//            // do nothing
//            // otherwise, update fullname for node and all descendants
//            
//            boolean higherLevelsIncluded = false;
//            Taxon l = taxon.getParent();
//            while (l != null)
//            {
//                if ((l.getDefinitionItem().getIsInFullName() != null) && 
//                    (l.getDefinitionItem().getIsInFullName().booleanValue() == true))
//                {
//                    higherLevelsIncluded = true;
//                    break;
//                }
//                l = l.getParent();
//            }
//            
//            // if no higher level is included in the new place in the tree, check the old place
//            // in the tree
//            if (higherLevelsIncluded == false)
//            {
//                l = origParent;
//                while (l != null)
//                {
//                    if ((l.getDefinitionItem().getIsInFullName() != null) && 
//                        (l.getDefinitionItem().getIsInFullName().booleanValue() == true))
//                    {
//                        higherLevelsIncluded = true;
//                        break;
//                    }
//                    
//                    l = l.getParent();
//                }
//            }
//            
//            if (higherLevelsIncluded)
//            {
//                TreeHelper.fixFullnameForNodeAndDescendants(taxon);
//            }
//            else
//            {
//                String generated = TreeHelper.generateFullname(taxon);
//                taxon.setFullName(generated);
//            }
//        }
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link TaxonTreeDefItem}.  The real work of this method is to
     * update the 'fullname' field of all {@link Taxon} objects effected by the changes
     * to the passed in {@link TaxonTreeDefItem}.
     *
     * @param defItem the {@link TaxonTreeDefItem} being saved
     */
    protected void beforeSaveTaxonTreeDefItem(TaxonTreeDefItem defItem)
    {
        // we need a way to determine if the 'isInFullname' value changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        TaxonTreeDefItem fromDB = tmpSession.load(TaxonTreeDefItem.class, defItem.getId());
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
            Set<Taxon> levelNodes = defItem.getTreeEntries();
            for (Taxon node: levelNodes)
            {
                String generated = TreeHelper.generateFullname(node);
                node.setFullName(generated);
            }
        }
        else if (changeThisLevel && changeAllDescendants)
        {
            Set<Taxon> levelNodes = defItem.getTreeEntries();
            for (Taxon node: levelNodes)
            {
                TreeHelper.fixFullnameForNodeAndDescendants(node);
            }
        }
        else if (!changeThisLevel && changeAllDescendants)
        {
            Set<Taxon> levelNodes = defItem.getTreeEntries();
            for (Taxon node: levelNodes)
            {
                // grab all child nodes and go from there
                for (Taxon child: node.getChildren())
                {
                    TreeHelper.fixFullnameForNodeAndDescendants(child);
                }
            }
        }
        // else don't change anything
        
        session.close();
    }
    
    @Override
    public void afterDelete(Object dataObj)
    {
        if (dataObj instanceof Taxon)
        {
            afterDeleteTaxon((Taxon)dataObj);
        }
    }
    
    public void afterDeleteTaxon(Taxon t)
    {
        System.out.println("Taxon " + t + " deleted.");
        System.out.println("\t" + t.getNodeNumber() + " : " + t.getHighestChildNodeNumber());
    }

    @Override
    public void beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        // do nothing
        
        // when this is called from the tree viewer, the session will be null since the tree viewer uses a session
        // that does not implement DataProviderSessionIFace
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

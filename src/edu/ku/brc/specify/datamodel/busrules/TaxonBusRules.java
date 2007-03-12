/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UICacheManager.getLocalizedMessage;

import java.util.Set;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.treeutils.TreeHelper;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class TaxonBusRules extends BaseBusRules
{
    public TaxonBusRules()
    {
        super(Taxon.class);
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
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSave(java.lang.Object)
     */
    @Override
    public void afterSave(Object dataObj)
    {
        System.err.println("afterSave() on Taxon object");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj)
    {
        System.err.println("beforeSave() on Taxon object");
        if (dataObj instanceof Taxon)
        {
            beforeSaveTaxon((Taxon)dataObj);
            return;
        }
        
        if (dataObj instanceof TaxonTreeDefItem)
        {
            beforeSaveTaxonTreeDefItem((TaxonTreeDefItem)dataObj);
            return;
        }
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Taxon}.  The real work of this method is to
     * update the 'fullname' field of all {@link Taxon} objects effected by the changes
     * to the passed in {@link Taxon}.
     * 
     * @param taxon the {@link Taxon} being saved
     */
    protected void beforeSaveTaxon(Taxon taxon)
    {
        // check to see if this node is brand new
        if (taxon.getId() == null)
        {
            // this is a new object
            // just update it's fullname (and return)
            // since it can't have any children yet
            
            String fullname = TreeHelper.generateFullname(taxon);
            taxon.setFullName(fullname);
            return;
        }
        // else
        
        // we need a way to determine if the name changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        Taxon fromDB = tmpSession.load(Taxon.class, taxon.getId());
        Taxon origParent = fromDB.getParent();
        tmpSession.close();

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(taxon);
        
        boolean nameChanged = true;
        
        nameChanged = !(fromDB.getName().equals(taxon.getName()));
        
        
        // if the name changed...
        // update the node's fullname
        // AND all descendants IF the node's level is in the fullname
        if (nameChanged)
        {
            boolean isInFullname = false;
            if ((taxon.getDefinitionItem().getIsInFullName() != null) && 
                (taxon.getDefinitionItem().getIsInFullName().booleanValue() == true))
            {
                isInFullname = true;
            }
            
            if (!isInFullname)
            {
                // just change the node's fullname field
                String fullname = TreeHelper.generateFullname(taxon);
                taxon.setFullName(fullname);
            }
            else
            {
                // must fix fullname for all descendants as well
                TreeHelper.fixFullnameForNodeAndDescendants(taxon);
            }
            
            session.close();
            
            // it is assumed that you cannot perform a name change AND move a node in one transaction
            // so we're done here
            return;
        }
        
        // we need a way to determine if the parent changed
        // did the node change parents?
        boolean parentChanged = true;
        parentChanged = !(origParent.getId().equals(taxon.getParent().getId()));
        
        if (parentChanged)
        {
            // if no levels above or equal to the new parent or old parent are included in the fullname
            // do nothing
            // otherwise, update fullname for node and all descendants
            
            boolean higherLevelsIncluded = false;
            Taxon l = taxon.getParent();
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
                TreeHelper.fixFullnameForNodeAndDescendants(taxon);
            }
            else
            {
                String generated = TreeHelper.generateFullname(taxon);
                taxon.setFullName(generated);
            }
        }
        
        session.close();
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
    
    private String makeNotNull(String s)
    {
        return (s == null) ? "" : s;
    }
}

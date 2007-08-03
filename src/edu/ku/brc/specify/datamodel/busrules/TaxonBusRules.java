/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.util.Set;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
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
public class TaxonBusRules extends BaseTreeBusRules<Taxon, TaxonTreeDef, TaxonTreeDefItem>
{
    //private static final Logger log = Logger.getLogger(TaxonBusRules.class);
    
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
    
    public boolean okToDeleteTaxon(Taxon taxon)
    {
        Long id = taxon.getId();
        if (id == null)
        {
            return true;
        }
        
        boolean noDeters = super.okToDelete("determination", "TaxonID",         id);
        boolean noCites  = super.okToDelete("taxoncitation", "TaxonID",         id);
        boolean noHyb1   = super.okToDelete("taxon",         "HybridParent1ID", id);
        boolean noHyb2   = super.okToDelete("taxon",         "HybridParent2ID", id);
        boolean noSyns   = super.okToDelete("taxon",         "AcceptedID",      id);

        boolean okSoFar = noDeters && noCites && noHyb1 && noHyb2 && noSyns;
        
        if (okSoFar)
        {
            // now check the children

            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            Taxon tmpT = session.load(Taxon.class, id);

            for (Taxon child: tmpT.getChildren())
            {
                if (!okToDeleteTaxon(child))
                {
                    // this child can't be deleted
                    // stop right here
                    okSoFar = false;
                    break;
                }
            }
            session.close();
        }
        
        return okSoFar;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSave(java.lang.Object)
     */
    @Override
    public boolean afterSave(Object dataObj)
    {
        return super.afterSave(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof Taxon)
        {
            Taxon taxon = (Taxon)dataObj;
            beforeSaveTaxon(taxon, session);

            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(taxon, session);
            
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
    protected void beforeSaveTaxon(Taxon taxon, @SuppressWarnings("unused") DataProviderSessionIFace session)
    {
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

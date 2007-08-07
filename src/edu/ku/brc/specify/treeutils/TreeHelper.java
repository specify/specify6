/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.treeutils;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * This is simply a collection of helper methods for manipulating trees and tree nodes.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class TreeHelper
{
    protected static final Logger log = Logger.getLogger(TreeHelper.class);
    
    /**
     * Generates the 'full' name of the given tree node.
     * 
     * THIS METHOD ASSUMES ALL DATA IS AVAILABLE.  IF USED WITH A JPA
     * PROVIDER THAT DOES LAZY LOADING, IT IS THE CALLER'S RESPONSIBILITY
     * TO LOAD ALL DATA BEFORE CALLING THIS METHOD.
     * 
     * @param <T> an implementation class of {@link Treeable}
     * @param <D> an implementation class of {@link TreeDefIface}
     * @param <I> an implementation class of {@link TreeDefItemIface}
     * @param treeNode a tree node
     * @return the full name
     */
    public static <T extends Treeable<T,D,I>,
                   D extends TreeDefIface<T,D,I>,
                   I extends TreeDefItemIface<T,D,I>>
                        String generateFullname(T treeNode)
    {
        log.debug("Generating fullname for " + treeNode.toString());
        // get all the nodes from this node on up, only grabbing the ones included in the fullname
        Vector<T> parts = new Vector<T>();
        parts.add(treeNode);
        T node = treeNode.getParent();
        while( node != null )
        {
            Boolean include = node.getDefinitionItem().getIsInFullName();
            if( include != null && include.booleanValue() == true )
            {
                parts.add(node);
            }
            
            node = node.getParent();
        }
        // now we have all the nodes
        
        // which order should they go in, ascending or descending?
        int direction = treeNode.getDefinition().getFullNameDirection();
        
        // assume about 10 characters per part (it's okay if we're off)
        StringBuilder fullNameBuilder = new StringBuilder(parts.size() * 10);
        
        // these two cases are basically the same code, but with the order of 
        // iteration reversed
        switch( direction )
        {
            case TreeDefIface.FORWARD:
            {
                for( int j = parts.size()-1; j > -1; --j )
                {
                    T part = parts.get(j);
                    String before = part.getDefinitionItem().getTextBefore();
                    String after = part.getDefinitionItem().getTextAfter();
                    String separator = part.getDefinitionItem().getFullNameSeparator();

                    if (before!=null)
                    {
                        fullNameBuilder.append(before);
                    }
                    fullNameBuilder.append(part.getName());
                    if (after!=null)
                    {
                        fullNameBuilder.append(after);
                    }
                    if(j!=0 && separator!=null)
                    {
                        fullNameBuilder.append(separator);
                    }
                }
                break;
            }
            case TreeDefIface.REVERSE:
            {
                for( int j = 0; j < parts.size(); ++j )
                {
                    T part = parts.get(j);
                    String before = part.getDefinitionItem().getTextBefore();
                    String after = part.getDefinitionItem().getTextAfter();
                    String separator = part.getDefinitionItem().getFullNameSeparator();
                    
                    if (before!=null)
                    {
                        fullNameBuilder.append(before);
                    }
                    fullNameBuilder.append(part.getName());
                    if (after!=null)
                    {
                        fullNameBuilder.append(after);
                    }
                    if(j!=parts.size()-1 && separator!=null)
                    {
                        fullNameBuilder.append(separator);
                    }
                }
                break;
            }
            default:
            {
                log.error("Invalid tree walk direction (for creating fullname field) found in tree definition");
                return null;
            }
        }
        
        return fullNameBuilder.toString().trim();
    }
    
    /**
     * Generates and sets the full name of the given node and all of its
     * descendants.
     * 
     * THIS METHOD ASSUMES ALL DATA IS AVAILABLE.  IF USED WITH A JPA
     * PROVIDER THAT DOES LAZY LOADING, IT IS THE CALLER'S RESPONSIBILITY
     * TO LOAD ALL DATA BEFORE CALLING THIS METHOD OR CALLING THIS METHOD
     * FROM WITHIN AN ENTITYMANAGER CONTEXT.
     * 
     * @param <T> an implementation class of {@link Treeable}
     * @param <D> an implementation class of {@link TreeDefIface}
     * @param <I> an implementation class of {@link TreeDefItemIface}
     * @param treeNode a tree node
     */
    public static <T extends Treeable<T,D,I>,
                   D extends TreeDefIface<T,D,I>,
                   I extends TreeDefItemIface<T,D,I>>
                         void fixFullnameForNodeAndDescendants(T treeNode)
    {
        // generics made it difficult to make this generic (that sounds weird)
        // so I settled for this type of implementation which has to be updated if we
        // ever add another tree class, which is unlikely
        
        if (treeNode instanceof Geography)
        {
            fixFullnameForNodeAndDescendants((Geography)treeNode);
            return;
        }
        
        if (treeNode instanceof GeologicTimePeriod)
        {
            fixFullnameForNodeAndDescendants((GeologicTimePeriod)treeNode);
            return;
        }
        
        if (treeNode instanceof Location)
        {
            fixFullnameForNodeAndDescendants((Location)treeNode);
            return;
        }
        
        if (treeNode instanceof LithoStrat)
        {
            fixFullnameForNodeAndDescendants((LithoStrat)treeNode);
            return;
        }
        
        if (treeNode instanceof Taxon)
        {
            fixFullnameForNodeAndDescendants((Taxon)treeNode);
            return;
        }
    }
    
    /**
     * @see #fixFullnameForNodeAndDescendants(Treeable)
     * @param a {@link Geography} node
     */
    public static void fixFullnameForNodeAndDescendants(Geography geo)
    {
        String generated = generateFullname(geo);
        geo.setFullName(generated);
        
        for (Geography child: geo.getChildren())
        {
            fixFullnameForNodeAndDescendants(child);
        }
    }

    /**
     * @see #fixFullnameForNodeAndDescendants(Treeable)
     * @param a {@link GeologicTimePeriod} node
     */
    public static void fixFullnameForNodeAndDescendants(GeologicTimePeriod gtp)
    {
        String generated = generateFullname(gtp);
        gtp.setFullName(generated);
        
        for (GeologicTimePeriod child: gtp.getChildren())
        {
            fixFullnameForNodeAndDescendants(child);
        }
    }
    
    /**
     * @see #fixFullnameForNodeAndDescendants(Treeable)
     * @param a {@link GeologicTimePeriod} node
     */
    public static void fixFullnameForNodeAndDescendants(LithoStrat litho)
    {
        String generated = generateFullname(litho);
        litho.setFullName(generated);
        
        for (LithoStrat child: litho.getChildren())
        {
            fixFullnameForNodeAndDescendants(child);
        }
    }

    /**
     * @see #fixFullnameForNodeAndDescendants(Treeable)
     * @param a {@link Location} node
     */
    public static void fixFullnameForNodeAndDescendants(Location loc)
    {
        String generated = generateFullname(loc);
        loc.setFullName(generated);
        
        for (Location child: loc.getChildren())
        {
            fixFullnameForNodeAndDescendants(child);
        }
    }

    /**
     * @see #fixFullnameForNodeAndDescendants(Treeable)
     * @param a {@link Taxon} node
     */
    public static void fixFullnameForNodeAndDescendants(Taxon taxon)
    {
        String generated = generateFullname(taxon);
        taxon.setFullName(generated);
        
        for (Taxon child: taxon.getChildren())
        {
            fixFullnameForNodeAndDescendants(child);
        }
    }
    
    /**
     * Initializes the {@link Set}s or {@link List}s of related objects.
     * 
     * THIS METHOD ASSUMES ALL DATA IS AVAILABLE.  IF USED WITH A JPA
     * PROVIDER THAT DOES LAZY LOADING, IT IS THE CALLER'S RESPONSIBILITY
     * TO LOAD ALL DATA BEFORE CALLING THIS METHOD OR CALLING THIS METHOD
     * FROM WITHIN AN ENTITYMANAGER CONTEXT.
     * 
     * @param <T> an implementation class of {@link Treeable}
     * @param <D> an implementation class of {@link TreeDefIface}
     * @param <I> an implementation class of {@link TreeDefItemIface}
     * @param treeNode any tree node
     */
    public static <T extends Treeable<T,D,I>,
                   D extends TreeDefIface<T,D,I>,
                   I extends TreeDefItemIface<T,D,I>>
                        void initializeRelatedObjects(T treeNode)
    {
        if (treeNode instanceof Geography)
        {
            initializeRelatedObjects((Geography)treeNode);
            return;
        }
        
        if (treeNode instanceof GeologicTimePeriod)
        {
            initializeRelatedObjects((GeologicTimePeriod)treeNode);
            return;
        }
        
        if (treeNode instanceof Location)
        {
            initializeRelatedObjects((Location)treeNode);
            return;
        }
        
        if (treeNode instanceof Taxon)
        {
            initializeRelatedObjects((Taxon)treeNode);
            return;
        }
    }
    
    public static void initializeRelatedObjects(Location loc)
    {
        loc.getContainers().size();
        loc.getPreparations().size();
    }
    
    public static void initializeRelatedObjects(GeologicTimePeriod gtp)
    {
        gtp.getStratigraphies().size();
    }
    
    public static void initializeRelatedObjects(Geography geo)
    {
        geo.getLocalities().size();
    }
    
    public static void initializeRelatedObjects(Taxon taxon)
    {
        taxon.getAttachments().size();
        taxon.getDeterminations().size();
        taxon.getTaxonCitations().size();
        taxon.getAcceptedChildren().size();
        taxon.getChildren().size();
        taxon.getHybridChildren1().size();
        taxon.getHybridChildren2().size();
    }
    
    public static <T extends Treeable<T,D,I>,
                             D extends TreeDefIface<T,D,I>,
                             I extends TreeDefItemIface<T,D,I>>
                             String createNodeRelationship(T source, T destination)
    {
        if (source instanceof Taxon)
        {
            return createSynonym((Taxon)source,(Taxon)destination);
        }
        return null;
    }
    
    protected static String createSynonym(Taxon oldName, Taxon newName)
    {
        oldName.setIsAccepted(false);
        oldName.setAcceptedTaxon(newName);
        
        // update all of the even older names to point at the latest name
        for (Taxon evenOlderName: oldName.getAcceptedChildren())
        {
            evenOlderName.setIsAccepted(false);
            evenOlderName.setAcceptedTaxon(newName);
        }
        
        return String.format(getResourceString("TaxonTreeRelationshipMsg"),oldName.getFullName(),newName.getFullName());
    }
    
    /**
     * Determines if the child node can be reparented to newParent while not
     * violating any of the business rules.  Currently, the only rule on
     * reparenting is that the new parent must be of rank equal to or less than
     * the next higher enforced rank in the child's tree definition.
     * 
     * @param child the node to be reparented
     * @param newParent the prospective new parent node
     * 
     * @return <code>true</code> if the action will not violate any reparenting rules, false otherwise
     */
    public static <T extends Treeable<T,D,I>,
                   D extends TreeDefIface<T,D,I>,
                   I extends TreeDefItemIface<T,D,I>>
                       boolean canChildBeReparentedToNode(T child, T newParent)
    {
        if( newParent.getRankId().intValue() >= child.getRankId().intValue() )
        {
            // a node cannot have a parent that is a peer or of lower rank (larger rank id)
            return false;
        }
        
        Integer nextEnforcedRank = getRankOfNextHighestEnforcedLevel(child);
        if( nextEnforcedRank == null )
        {
            // no higher ranks are being enforced
            // the node can be reparented all the way up to the root
            return true;
        }
        
        if( nextEnforcedRank.intValue() <= newParent.getRankId().intValue() )
        {
            // the next enforced rank is equal to or above the new parent rank
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns the next highest rank in the tree that is enforced by the
     * tree definition.
     * 
     * @param node the node to find the next highest enforced rank for
     * @return the next highest rank
     */
    public static <T extends Treeable<T,D,I>,
                   D extends TreeDefIface<T,D,I>,
                   I extends TreeDefItemIface<T,D,I>>
                       Integer getRankOfNextHighestEnforcedLevel( T node )
    {
        I defItem = node.getDefinitionItem();
        while( defItem.getParent() != null )
        {
            defItem = defItem.getParent();
            if( defItem.getIsEnforced() != null && defItem.getIsEnforced().booleanValue() == true )
            {
                return defItem.getRankId();
            }
        }
        
        return null;
    }
}

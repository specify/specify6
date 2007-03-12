package edu.ku.brc.specify.treeutils;

import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

public class TreeHelper
{
    protected static final Logger log = Logger.getLogger(TreeHelper.class);
    
    /**
     * THIS METHOD ASSUMES ALL DATA IS AVAILABLE.  IF USED WITH A JPA
     * PROVIDER THAT DOES LAZY LOADING, IT IS THE CALLER'S RESPONSIBILITY
     * TO LOAD ALL DATA BEFORE CALLING THIS METHOD.
     * 
     * @param node
     */
    public static <T extends Treeable<T,D,I>,
                   D extends TreeDefIface<T,D,I>,
                   I extends TreeDefItemIface<T,D,I>>
                        String generateFullname(T treeNode)
    {
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
        int direction = treeNode.getDefinition().getFullNameDirection();
        
        StringBuilder fullNameBuilder = new StringBuilder(parts.size() * 10);
        
        switch( direction )
        {
            case TreeDefIface.FORWARD:
            {
                for( int j = parts.size()-1; j > -1; --j )
                {
                    T part = parts.get(j);
                    String before = part.getDefinitionItem().getTextBefore();
                    String after = part.getDefinitionItem().getTextAfter();

                    if (before!=null)
                    {
                        fullNameBuilder.append(part.getDefinitionItem().getTextBefore());
                    }
                    fullNameBuilder.append(part.getName());
                    if (after!=null)
                    {
                        fullNameBuilder.append(part.getDefinitionItem().getTextAfter());
                    }
                    if(j!=parts.size()-1)
                    {
                        fullNameBuilder.append(parts.get(j).getFullNameSeparator());
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

                    if (before!=null)
                    {
                        fullNameBuilder.append(part.getDefinitionItem().getTextBefore());
                    }
                    fullNameBuilder.append(part.getName());
                    if (after!=null)
                    {
                        fullNameBuilder.append(part.getDefinitionItem().getTextAfter());
                    }
                    if(j!=parts.size()-1)
                    {
                        fullNameBuilder.append(parts.get(j).getFullNameSeparator());
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
        
        return fullNameBuilder.toString();
    }
    
    /**
     * THIS METHOD ASSUMES ALL DATA IS AVAILABLE.  IF USED WITH A JPA
     * PROVIDER THAT DOES LAZY LOADING, IT IS THE CALLER'S RESPONSIBILITY
     * TO LOAD ALL DATA BEFORE CALLING THIS METHOD OR CALLING THIS METHOD
     * FROM WITHIN AN ENTITYMANAGER CONTEXT.
     * 
     * @param node
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
        
        if (treeNode instanceof Taxon)
        {
            fixFullnameForNodeAndDescendants((Taxon)treeNode);
            return;
        }
    }
    
    public static void fixFullnameForNodeAndDescendants(Geography geo)
    {
        String generated = generateFullname(geo);
        geo.setFullName(generated);
        
        for (Geography child: geo.getChildren())
        {
            fixFullnameForNodeAndDescendants(child);
        }
    }

    public static void fixFullnameForNodeAndDescendants(GeologicTimePeriod gtp)
    {
        String generated = generateFullname(gtp);
        gtp.setFullName(generated);
        
        for (GeologicTimePeriod child: gtp.getChildren())
        {
            fixFullnameForNodeAndDescendants(child);
        }
    }

    public static void fixFullnameForNodeAndDescendants(Location loc)
    {
        String generated = generateFullname(loc);
        loc.setFullName(generated);
        
        for (Location child: loc.getChildren())
        {
            fixFullnameForNodeAndDescendants(child);
        }
    }

    public static void fixFullnameForNodeAndDescendants(Taxon taxon)
    {
        String generated = generateFullname(taxon);
        taxon.setFullName(generated);
        
        for (Taxon child: taxon.getChildren())
        {
            fixFullnameForNodeAndDescendants(child);
        }
    }
}

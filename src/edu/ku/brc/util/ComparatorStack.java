package edu.ku.brc.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * This class serves as a "stack" of {@link Comparator} objects.  This is useful when a caller
 * wants to sort a {@link Collection} of objects based on multiple criteria.  For example, if one
 * wanted to sort a set of {@link String}s based on the string length, then alphabetically, they 
 * would simply provide this class with two comparators: a string length comparator and an alphabetizing
 * comparator, in that order.
 * 
 * @author jstewart
 * @code_status Code_Freeze
 * @param <T> the type of objects being compared
 */
public class ComparatorStack<T> implements Comparator<T>
{
    /** The {@link List} of {@link Comparator}s */
    List<Comparator<T>> compList;
    
    /**
     * Create a stack of {@link Comparator}s
     * 
     * @param comparators a {@link List} of {@link Comparator}s
     */
    public ComparatorStack(Comparator<T>...comparators)
    {
        for (Comparator<T> comp: comparators)
        {
            compList.add(comp);
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(T o1, T o2)
    {
        // walk the list of Comparators, until you get deep enough to "see" a difference between the passed in
        // objects
        for (Comparator<T> comp: compList)
        {
            int res = comp.compare(o1, o2);
            if (res != 0)
            {
                return res;
            }
        }
        
        // if we get this far, the objects were equal according to all of the comparators
        return 0;
    }
}

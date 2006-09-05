/**
 * 
 */
package edu.ku.brc.specify.tasks;

/**
 * An interface that all 'dual-view' search services must implement.  A dual-view
 * service is one that can display the results in one or both of two separate results
 * areas.
 *
 * @code_status Complete
 * @author jstewart
 */
public interface DualViewSearchable
{
    /** Display the results in the 'top' results area. */
    public static final int TOPVIEW    = 1;
    /** Display the results in the 'bottom' results area. */
    public static final int BOTTOMVIEW = 2;
    /** Display the results in the both results areas. */
    public static final int BOTHVIEWS  = TOPVIEW ^ BOTTOMVIEW;

	/**
	 * Perform a search for <code>key</code> and display the results
	 * in <code>where</code> using a wrapping search is <code>wrap</code>
	 * is <code>true</code>.
	 *
	 * @param key the key to be searched for
	 * @param where the results pane to use for results
	 * @param wrap whether or not to wrap the search
	 */
	public void find(String key,int where,boolean wrap);
	/**
	 * Perform a search for <code>key</code> and display the results
	 * in <code>where</code> using a wrapping search is <code>wrap</code>
	 * is <code>true</code>.  If <code>key</code> is the same as in the last
	 * search performed, the next occurance of <code>key</code> should be
	 * returned.  If <code>key</code> is <code>null</code>, the value used in
	 * the previous search should be reused.
	 *
	 * @param key the key to be searched for or <code>null</code>
	 * @param where the results pane to use for results
	 */
	public void findNext(String key,int where,boolean wrap);
}

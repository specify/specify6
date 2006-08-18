/**
 * 
 */
package edu.ku.brc.specify.tasks;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public interface DualViewSearchable
{
    public static final int TOPVIEW    = 1;
    public static final int BOTTOMVIEW = 2;
    public static final int BOTHVIEWS  = TOPVIEW ^ BOTTOMVIEW;

	public void find(String key,int where,boolean wrap);
	public void findNext(int where,boolean wrap);
}

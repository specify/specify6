/**
 * 
 */
package edu.ku.brc.specify.web;

import org.jfree.util.Log;

import edu.ku.brc.exceptions.ExceptionTracker;
import edu.ku.brc.ui.FeedBackSenderItem;

/**
 * @author rods
 *
 */
public class SpecifyExplorerExceptionTracker extends ExceptionTracker
{

    /* (non-Javadoc)
     * @see edu.ku.brc.exceptions.ExceptionTracker#getSenderURL()
     */
    @Override
    protected String getSenderURL()
    {
        return super.getSenderURL();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.exceptions.ExceptionTracker#getFeedBackSenderItem(java.lang.Class, java.lang.Exception)
     */
    @Override
    protected FeedBackSenderItem getFeedBackSenderItem(final Class<?> cls, final Exception exception)
    {
        Log.error(exception.toString());
        
        return null;//new FeedBackSenderItem("SpecifyExplorer", "SpecifyExplorer", "", "", exception.toString(), cls.getName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.FeedBackSender#capture(java.lang.Class, java.lang.Exception)
     */
    @Override
    public void capture(Class<?> cls, Exception exception)
    {
        super.capture(cls, exception);
    }


}

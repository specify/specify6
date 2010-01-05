/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.conversion;

import java.util.Properties;

import edu.ku.brc.ui.FeedBackSender;
import edu.ku.brc.ui.FeedBackSenderItem;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 23, 2009
 *
 */
public class ConvertStatSender extends FeedBackSender
{
    
    private String cgiScript = "convinfo.php";
    
    /**
     * 
     */
    public ConvertStatSender()
    {
        
    }
    
    /**
     * 
     */
    public ConvertStatSender(final String cgiScript)
    {
        this.cgiScript = cgiScript;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.FeedBackSender#getSenderURL()
     */
    @Override
    protected String getSenderURL()
    {
        return UIRegistry.getResourceString("CGI_BASE_URL") + "/" + cgiScript;
    }
    
    public void senConvertInfo(final String collectionName, final int numColObj, final int convTime)
    {
        try
        {
            Properties props = new Properties();
            props.put("CollectionName", collectionName);
            props.put("num_colobj",     Integer.toString(numColObj));
            props.put("num_convtime",   Integer.toString(convTime));
            FeedBackSenderItem item = new FeedBackSenderItem();
            item.setProps(props);
            send(item);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void senVerifyInfo(final String collectionName, 
                              final int numColObj, 
                              final int convTime, 
                              final int numErrors)
    {
        try
        {
            Properties props = new Properties();
            props.put("CollectionName", collectionName);
            props.put("num_colobj",     Integer.toString(numColObj));
            props.put("num_convtime",   Integer.toString(convTime));
            props.put("num_verifyerrs", Integer.toString(numErrors));
            FeedBackSenderItem item = new FeedBackSenderItem();
            item.setProps(props);
            send(item);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}

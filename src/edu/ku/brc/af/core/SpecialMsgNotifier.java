/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.core;

import javax.swing.SwingWorker;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jan 12, 2010
 *
 */
public class SpecialMsgNotifier
{

    /**
     * 
     */
    public SpecialMsgNotifier()
    {
        super();
    }

    
    public void checkForMessages()
    {
        SwingWorker<Integer, Integer> msgCheckWorker = new SwingWorker<Integer, Integer>()
        {
            protected String msg = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                try
                {
                    Thread.sleep(30000); // 30 seconds
                    
                    String url       = UIRegistry.getResourceString("CGI_BASE_URL") + "/getmsg.php";
                    String installID = UsageTracker.getInstallId();
                    
                    msg = send(url, installID);
                    
                } catch (Exception ex)
                {
                    // die silently
                }                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                if (msg != null)
                {
                    if (StringUtils.isNotEmpty(msg) && !msg.equals("NOMSG"))
                    {
                        UIRegistry.showLocalizedMsg(msg); // This msg isn't a key, but use this to display it anyway.
                    }
                }
                
            }
        };
        
        msgCheckWorker.execute();
    }
    
    /**
     * @param item
     * @throws Exception
     */
    protected String send(final String url, final String id) throws Exception
    {
        // check the website for the info about the latest version
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        
        PostMethod postMethod = new PostMethod(url);
        
        NameValuePair[] postParams = new NameValuePair[1];
        postParams[0] = new NameValuePair("id", id);
        postMethod.setRequestBody(postParams);
        
        // connect to the server
        try
        {
            httpClient.executeMethod(postMethod);
            
            // get the server response
            String responseString = postMethod.getResponseBodyAsString();
            
            if (StringUtils.isNotEmpty(responseString))
            {
                return responseString;
            }
        }
        catch (Exception e)
        {
            // die silently
        }
        return null;
    }
}
